/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting.ModelExpanders;
import utilities.Non_LinearFitting.FittingResultsNode;
import java.util.ArrayList;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Non_LinearFitting.FittingModelExpander;
import utilities.Non_LinearFitting.Non_Linear_Fitter;
import utilities.Non_LinearFitting.Fitting_Function;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.CommonMethods;
import utilities.Non_LinearFitting.Constrains.*;
import java.awt.Point;
import utilities.CommonStatisticsMethods;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import ij.ImagePlus;
import utilities.Non_LinearFitting.ModelExpanders.IPOGaussianExpander;
import utilities.Geometry.ImageShapes.RectangleImage;
import utilities.Geometry.ImageShapes.CircleImage;
import FluoObjects.IPOGaussianNodeComplex;

/**
 *
 * @author Taihao
 */
public class IPOGaussianExpander implements FittingModelExpander{
    ImageShape m_cIS;
    Point cornerLT,cornerRB;
    FittingResultsNode m_cResultsNode;
    Non_Linear_Fitter m_cFitter;
    ArrayList<Point> m_cLocalMaxima;
    int meanFilteringRadius;
    int[][] pixels,stamp;
//    FittingModelNode lastModel;
    int[][] pnVarIndexes;
    int m_nMaxNewComponents;
    String m_sNewComponentType;
    boolean m_bIncludingBoudaryPoints;
    boolean m_bInitializing;
    FittingModelNode m_cOriginalModel,m_cExpandedModel;
    public IPOGaussianExpander(){
        m_bInitializing=false;
        meanFilteringRadius=-1;
    }
    public FittingModelNode buildExpandedModel(FittingModelNode originalModel, String sType, int nMaxNewComponents, boolean includingBoundaryPoints, boolean bIninializing, int filteringRadius){
        m_bInitializing=bIninializing;
        meanFilteringRadius=filteringRadius;
        FittingModelNode aNode=buildExpandedModel(originalModel, sType, nMaxNewComponents,includingBoundaryPoints);
        m_bInitializing=false;
        meanFilteringRadius=-1;
        return aNode;
    }
    public FittingModelNode buildExpandedModel(FittingModelNode originalModel, String sType, int nMaxNewComponents, boolean includingBoundaryPoints, boolean bIninializing){
        m_bInitializing=bIninializing;
        FittingModelNode aNode=buildExpandedModel(originalModel, sType, nMaxNewComponents,includingBoundaryPoints);
        m_bInitializing=false;
        return aNode;
    }
    public FittingModelNode buildExpandedModel(FittingModelNode originalModel, String sType, int nMaxNewComponents, boolean includingBoundaryPoints,int filteringRadius){
        meanFilteringRadius=filteringRadius;
        FittingModelNode aNode=buildExpandedModel(originalModel, sType, nMaxNewComponents,includingBoundaryPoints);
        meanFilteringRadius=-1;
        return aNode;
    }
    public FittingModelNode buildExpandedModel(FittingModelNode originalModel, String sType, int nMaxNewComponents, boolean includingBoundaryPoints){
        m_cOriginalModel=originalModel;
        m_bIncludingBoudaryPoints=includingBoundaryPoints;
        m_nMaxNewComponents=nMaxNewComponents;
        m_cExpandedModel=new FittingModelNode(originalModel);
        m_cOriginalModel.updateFittedData();
        m_cIS=m_cOriginalModel.m_cIS;
        m_sNewComponentType=sType;
        if(m_cIS==null){
            double[][] pdX=m_cOriginalModel.m_pdX;
            int len=pdX.length;
            int[] pnVarIndexes={0,1};
            m_cIS=ImageShapeHandler.buildDrawingImageShape(pdX, pnVarIndexes, 2000, 0, len-1, 1);
            m_cOriginalModel.m_cIS=m_cIS;
            m_cExpandedModel.m_cIS=new ImageShape(m_cIS);
        }
        int status=findLocalMaximaOfDifference();
        if(status<0) return null;
        buildExpandedModel();
        return m_cExpandedModel;
    }
    void buildExpandedModel(){

        ArrayList<String> svFunctionTypes0=m_cOriginalModel.svFunctionTypes,svFunctionTypes=CommonStatisticsMethods.copyStringArray(svFunctionTypes0);
        int nPars0=m_cOriginalModel.nNumPars;
        if(m_bInitializing){
            DoubleRange dr=CommonStatisticsMethods.getRange(m_cOriginalModel.m_pdY);
            CommonStatisticsMethods.setElements(m_cOriginalModel.pdFittedY, dr.getMin());
            m_cOriginalModel.pdPars[0]=dr.getMin();
        }
        int i,j,len=m_cLocalMaxima.size();

        if(len>m_nMaxNewComponents) len=m_nMaxNewComponents;
        int nParsPerTerm=4;
        if(m_sNewComponentType.contentEquals("gaussian2D_GaussianPars"))nParsPerTerm=6;

        if(m_cOriginalModel.cvConstraintNodes==null) m_cOriginalModel.cvConstraintNodes=new ArrayList();
        ArrayList<ConstraintNode> constraints=m_cOriginalModel.cvConstraintNodes;
        if(constraints==null) constraints=new ArrayList();
        ConstraintViolationChecker checker;
        Point pt;
        int[] pnDim;
        int nPars=nPars0+nParsPerTerm*len;
        double[] pdPars=new double[nPars],pdPars0=m_cOriginalModel.pdPars,pdY=m_cOriginalModel.m_pdY,pdFittedY=m_cOriginalModel.pdFittedY;
        for(i=0;i<nPars0;i++){
            pdPars[i]=pdPars0[i];
        }

        Point location=m_cIS.getLocation();
        int xo=location.x,yo=location.y;
        double[] constraintPars,constraintOrigin;
        int num=nPars0,index;
        double sigma;

        ArrayList<Integer> constraintIndexes;
        for(i=0;i<len;i++){
            pt=m_cLocalMaxima.get(i);
            index=pnVarIndexes[pt.y][pt.x];
            pnDim=getPeakDimension(pt);
            pdPars[num]=pdY[index]-pdFittedY[index];
            num++;
            sigma=0.1*(pnDim[0]+pnDim[1]);
            if(sigma<1.5) sigma=1.5;
            pdPars[num]=sigma;
            num++;
            if(m_sNewComponentType.contentEquals("gaussian2D_GaussianPars")){
                pdPars[num]=sigma;
                num++;
                pdPars[num]=0.8;
                num++;
            }
            pdPars[num]=pt.x+xo;
            num++;
            pdPars[num]=pt.y+yo;
            num++;
            svFunctionTypes.add(m_sNewComponentType);

            constraintIndexes=new ArrayList();
            constraintIndexes.add(num-2);
            constraintIndexes.add(num-1);
            constraintPars=new double[2];
            constraintPars[0]=20;
            constraintPars[1]=2000;
            DistantConstraintFunction cf=new DistantConstraintFunction("exponential",constraintIndexes,m_cOriginalModel.m_pdX[index],constraintPars);

            checker=new ConstraintLocationChecker_ImageShape(constraintIndexes,m_cIS);
            constraints.add(new ConstraintNode(cf,checker));
        }
        m_cExpandedModel.updateModel(svFunctionTypes, constraints, pdPars);
    }
    public boolean expandModel(Non_Linear_Fitter fitter, int nMaxNewComponents){
        FittingResultsNode aResultsNode=fitter.getFittingReults();
        m_cFitter=fitter;
        m_cResultsNode=fitter.getFittingReults();
        m_cOriginalModel=m_cResultsNode.m_cvModels.get(m_cResultsNode.nModels-1);
        m_cIS=fitter.getImageShape();
        if(m_cIS==null){
            double[][] pdX=m_cResultsNode.m_pdX;
            int len=pdX.length;
            int[] pnVarIndexes={0,1};
            m_cIS=ImageShapeHandler.buildDrawingImageShape(pdX, pnVarIndexes, 2000, 0, len-1, 1);
        }
        m_bIncludingBoudaryPoints=false;
        m_nMaxNewComponents=nMaxNewComponents;
        m_cExpandedModel=new FittingModelNode(m_cOriginalModel);
        m_cOriginalModel.updateFittedData();
        findLocalMaximaOfDifference();
        buildExpandedModel();
        if(!m_bInitializing) optimizeNewComponents();
        applyExpandedModel(fitter);
        return true;
    }
    void optimizeNewComponents(){
        int nComponents0=m_cOriginalModel.nComponents,nComponents1=m_cExpandedModel.nComponents;
        for(int nComponent=nComponents0;nComponent<nComponents1;nComponent++){
            m_cExpandedModel.OptimizeOneComponentPars(nComponent);
        }
    }
    void applyExpandedModel(Non_Linear_Fitter fitter){
        ComposedFittingFunction func= new ComposedFittingFunction(m_cExpandedModel.svFunctionTypes);
        fitter.update(m_cExpandedModel.pdPars, func, fitter.gexFixedParIndexes());
        fitter.setConstraints(m_cExpandedModel.cvConstraintNodes);
    }
    int findLocalMaximaOfDifference(){
        double[][] pdX=m_cOriginalModel.m_pdX;
        m_cOriginalModel.updateFittedData();
        double[] pdY=m_cOriginalModel.m_pdY,pdFittedY=m_cOriginalModel.pdFittedY;
        if(!CommonStatisticsMethods.regularDoubleArray(pdFittedY)){
            pdX=pdX;
            return -1;
        }
        
        cornerLT=new Point();
        cornerLT.x=(int)(m_cOriginalModel.cvVarRanges.get(0).getMin()+.5);
        cornerLT.y=(int)(m_cOriginalModel.cvVarRanges.get(1).getMin()+.5);
        cornerRB=new Point();
        cornerRB.x=(int)(m_cOriginalModel.cvVarRanges.get(0).getMax()+.5);
        cornerRB.y=(int)(m_cOriginalModel.cvVarRanges.get(1).getMax()+.5);

        int w=cornerRB.x-cornerLT.x+1,h=cornerRB.y-cornerLT.y+1;
        pnVarIndexes=new int[h][w];
        pixels=new int[h][w];
        stamp=new int[h][w];
        int[][] pixelso=new int[h][w];
        int[][] stampo=new int[h][w];
//        int pixels[][]=new int[h][w],stamp[][]=new int[h][w];
        int len=pdX.length,i,j,iMaxDiff=0;
        double[] pdDiff=new double[len];
        double dn=Double.POSITIVE_INFINITY,dx=Double.NEGATIVE_INFINITY,dy;
        double dno=Double.POSITIVE_INFINITY,dxo=Double.NEGATIVE_INFINITY,dyo;

        for(i=0;i<len;i++){
            dy=pdY[i]-pdFittedY[i];
            pdDiff[i]=dy;
            if(dy>dx){
                dx=dy;
                iMaxDiff=i;
            }
            if(dy<dn) dn=dy;

            dyo=pdFittedY[i];
            if(dyo>dxo){
                dxo=dyo;
            }
            if(dyo<dno) dno=dyo;
        }
        Point px=new Point();
        px.x=(int)(pdX[iMaxDiff][0]+0.5);
        px.y=(int)(pdX[iMaxDiff][1]+0.5);
        double dist=0;

        px.translate(-cornerLT.x, -cornerLT.y);
        intRange ir=new intRange();
        intRange iro=new intRange();

        int x,y,pixel;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                dist=Math.sqrt((px.x-j)*(px.x-j)+(px.y-i)*(px.y-i));
                pixel=(int)(dn-dist);
                pixels[i][j]=pixel;
                ir.expandRange(pixel);

                pixel=(int)(dno-dist);
                pixelso[i][j]=pixel;
                iro.expandRange(pixel);
            }
        }
        for(i=0;i<len;i++){
            x=(int)(pdX[i][0]+0.5)-cornerLT.x;
            y=(int)(pdX[i][1]+0.5)-cornerLT.y;
            pnVarIndexes[y][x]=i;
            pixel=(int)pdDiff[i];
            if(pixel>2147483){
                i=i;
            }
            pixels[y][x]=pixel;
            ir.expandRange(pixel);

            pixel=(int)pdFittedY[i];
            pixelso[y][x]=pixel;
            iro.expandRange(pixel);
        }
        
        if(ir.emptyRange()){
            return -1;
        }

        if(meanFilteringRadius>0) CommonStatisticsMethods.meanFiltering(pixels, 0, pixels.length-1, 0, pixels[0].length-1, meanFilteringRadius);
        int[] pixelRange={ir.getMin(),ir.getMax()};
        if(pixelRange[1]-pixelRange[0]+1<0){
            pixelRange=pixelRange;
        }
        int[] pixelRangeo={iro.getMin(),iro.getMax()};


        LandscapeAnalyzerPixelSorting la=new LandscapeAnalyzerPixelSorting(w,h,pixelRange);
        la.updateAndStampPixels(pixels, stamp);

        LandscapeAnalyzerPixelSorting lao=new LandscapeAnalyzerPixelSorting(w,h,pixelRangeo);
        lao.updateAndStampPixels(pixelso, stampo);
        ArrayList<Point> cLocalMaximao=new ArrayList();
        m_cLocalMaxima=new ArrayList();
        Point p,po,location=m_cIS.getLocation();

        for(i=1;i<h-1;i++){
            for(j=1;j<w-1;j++){
                if(LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[i][j])==LandscapeAnalyzerPixelSorting.localMaximum) {
                    m_cLocalMaxima.add(new Point(j,i));
                }
                if(LandscapeAnalyzerPixelSorting.getLandscapeType(stampo[i][j])==LandscapeAnalyzerPixelSorting.localMaximum) {
                    cLocalMaximao.add(new Point(j,i));
                }
            }
        }
        len=m_cLocalMaxima.size();
        int leno=cLocalMaximao.size();
        int xo=location.x,yo=location.y;

        for(i=len-1;i>=0;i--){
            p=m_cLocalMaxima.get(i);
            if(!m_cIS.contains(p.x+xo,p.y+yo)){
                m_cLocalMaxima.remove(i);
                continue;
            }
            if(m_cIS.isOnEdge(p.x+xo,p.y+yo)){
                m_cLocalMaxima.remove(i);
                continue;
            }
            if(m_bInitializing) continue;
            for(j=0;j<leno;j++){
                po=cLocalMaximao.get(j);
                if(Math.abs(po.x-p.x)+Math.abs(po.y-p.y)<3){
                    m_cLocalMaxima.remove(i);
                    j=leno+1;
                }
            }
        }

        ArrayList<Object> oA=new ArrayList();
        len=m_cLocalMaxima.size();
        double[] pdt=new double[len];
        for(i=0;i<len;i++){
            po=m_cLocalMaxima.get(i);
            oA.add(po);
            pdt[i]=pixels[po.y][po.x];
        }

        CommonMethods.sortObjectArray(oA, pdt);
        for(i=0;i<len;i++){
            po=(Point)oA.get(len-i-1);
            m_cLocalMaxima.set(i, po);
         }
        return 1;


//        ImagePlus impl=CommonMethods.displayPixels(pixels, "cSI", ImagePlus.GRAY32);
//        impl.show();
//        ImagePlus implo=CommonMethods.displayPixels(pixelso, "cSI", ImagePlus.GRAY32);
//        implo.show();
    }
/*    void expandModel(){
        ArrayList<String> svFunctionTypes0=m_cOriginalModel.svFunctionTypes,svFunctionTypes=CommonStatisticsMethods.copyStringArray(svFunctionTypes0);
        int nPars0=m_cOriginalModel.nNumPars;
        int i,j,len=m_cLocalMaxima.size();
        ArrayList<ConstraintNode> constraints=m_cFitter.getConstraints();
        ConstraintViolationChecker checker;
        ConstraintFunction cFunc;
        Point pt;
        int[] pnDim;
        int nPars=nPars0+4*len;
        double[] pdPars=new double[nPars],pdPars0=m_cOriginalModel.pdPars,pdY=m_cOriginalModel.m_pdY,pdFittedY=m_cOriginalModel.pdFittedY;
        for(i=0;i<nPars0;i++){
            pdPars[i]=pdPars0[i];
        }

        Point location=m_cIS.getLocation();
        int xo=location.x,yo=location.y;
        double[] constraintPars,constraintOrigin;
        int num=nPars0,index;
        double sigma;
        ArrayList<Integer> constraintIndexes;
        for(i=0;i<len;i++){
            pt=m_cLocalMaxima.get(i);
            index=pnVarIndexes[pt.y][pt.x];
            pnDim=getPeakDimension(pt);
            pdPars[num]=pdY[index]-pdFittedY[index];
            sigma=0.1*(pnDim[0]+pnDim[1]);
            if(sigma<1.5) sigma=1.5;
            pdPars[num+1]=sigma;
            pdPars[num+2]=pt.x+xo;
            pdPars[num+3]=pt.y+yo;
            svFunctionTypes.add("gaussian2D_Circular");

            constraintIndexes=new ArrayList();
            constraintIndexes.add(num+2);
            constraintIndexes.add(num+3);
            constraintPars=new double[2];
            constraintPars[0]=20;
            constraintPars[1]=2000;
            DistantConstraintFunction cf=new DistantConstraintFunction("exponential",constraintIndexes,m_cOriginalModel.m_pdX[index],constraintPars);

            checker=new ConstraintLocationChecker_ImageShape(constraintIndexes,m_cIS);
            constraints.add(new ConstraintNode(cf,checker));
            num+=ComposedFittingFunction.getNumParsPerTerm("gaussian2D_Circular");
        }

        ComposedFittingFunction func= new ComposedFittingFunction(svFunctionTypes);
        m_cFitter.update(pdPars, func, m_cFitter.gexFixedParIndexes());
        m_cFitter.setConstraints(constraints);
    }*/
    int[] getPeakDimension(Point p){
        int[] pnDim=new int[2];
        int xn,xx,yn,yx,x0=p.x,y0=p.y;
        Point l=m_cIS.getLocation();
        int xl=l.x,yl=l.y;
        int w=m_cIS.getXrange().getRange(),h=m_cIS.getYrange().getRange();
        int wt=stamp[0].length,ht=stamp.length;

        x0=p.x;
        xn=x0-1;
        xx=x0+1;
        while(xn>=0){
            if(!m_cIS.contains(xn+xl,y0+yl)||LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[y0][xn])!=LandscapeAnalyzerPixelSorting.regular) break;
            xn--;
        }
        xn++;
        while(xx<w&&xx<wt){
            if(!m_cIS.contains(xx+xl,y0+yl)||LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[y0][xx])!=LandscapeAnalyzerPixelSorting.regular) break;
            xx++;
        }
        xx--;

        x0=p.x;
        yn=y0-1;
        yx=y0+1;
        while(yn>=0){
            if(!m_cIS.contains(x0+xl,yn+yl)||LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[yn][x0])!=LandscapeAnalyzerPixelSorting.regular) break;
            yn--;
        }
        yn++;
        while(yx<h&&yx<ht){
            if(x0>=w) {
                x0=x0;
            }
            if(yx>=h){
                yx=yx;
            }
            if(!m_cIS.contains(x0+xl,yx+yl)||LandscapeAnalyzerPixelSorting.getLandscapeType(stamp[yx][x0])!=LandscapeAnalyzerPixelSorting.regular) break;
            yx++;
        }
        yx--;
        pnDim[0]=xx-xn+1;
        pnDim[1]=yx-yn+1;
        return pnDim;
    }
    public static FittingModelNode getInitialIPOGaussianFittingModel(int[][] pixels,int[][] pixelsp, int[][] stamp, int[][] exclusionStamp, int exclusion, ImageShape cIS, ArrayList<Point> cvBoundaryPoints, String sFunctionType, double hCutoff,double base,ArrayList<Point> peaks){
        //sFunctionType: "gaussian2D_GaussianPars" or "gaussian2D_GaussianCircular"
        ArrayList<Point> points=new ArrayList();
        ArrayList<ConstraintNode> constraints=new ArrayList();
        ArrayList<Integer> constraintIndexes;
        ConstraintViolationChecker checker;
        Point pt;
        cIS.getInnerPoints(points);
        int len=points.size()+cvBoundaryPoints.size(),i,j,nn=Integer.MAX_VALUE,pixel,pixelp,len1=points.size();
        ArrayList<String> svFunctionTypes=new ArrayList();

        double[] pdY=new double[len];
        double[][] pdX=new double[len][2];
        FittingModelNode aModel=new FittingModelNode(pdX,pdY);
        aModel.nI=0;
        aModel.nF=len-1;
        aModel.nDelta=1;

        for(i=0;i<len;i++){
            if(i<len1){
                pt=points.get(i);
            }else{
                pt=cvBoundaryPoints.get(i-len1);
            }
            pixelp=pixelsp[pt.y][pt.x];
            pixel=pixels[pt.y][pt.x];
            pdY[i]=pixel;
            pdX[i][0]=pt.x;
            pdX[i][1]=pt.y;

            if(pixelp<nn) nn=pixelp;
            if(LandscapeAnalyzerPixelSorting.isLocalMaximum(stamp[pt.y][pt.x])){
                if(pixelp-base<hCutoff) continue;
                if(exclusionStamp[pt.y][pt.x]!=exclusion) peaks.add(pt);
            }
        }

        aModel.m_pdX=pdX;
        aModel.m_pdY=pdY;
        len=peaks.size();
        double[] constraintPars,constraintOrigin;
        int nParsPerTerm=ComposedFittingFunction.getNumParsPerTerm(sFunctionType);
        int num=len*nParsPerTerm+1,index;
        double[] pdPars=new double[num];
        double sigma;

        num=0;
        pdPars[0]=base;
        num++;
        for(i=0;i<len;i++){
            pt=peaks.get(i);
            svFunctionTypes.add(sFunctionType);
            pixelp=pixelsp[pt.y][pt.x];
            pdPars[num]=pixelp-base;
            num++;
            sigma=1.5;
            pdPars[num]=sigma;
            num++;
            if(sFunctionType.contentEquals("gaussian2D_GaussianPars")){
                pdPars[num]=sigma;
                num++;
                pdPars[num]=0.8;//theta
                num++;
            }
            pdPars[num]=pt.x;
            num++;
            pdPars[num]=pt.y;
            num++;

            constraintIndexes=new ArrayList();
            constraintIndexes.add(num-2);
            constraintIndexes.add(num-1);
            constraintPars=new double[2];
            constraintPars[0]=20;
            constraintPars[1]=2000;
            double[] pd={pt.x,pt.y};
            DistantConstraintFunction cf=new DistantConstraintFunction("exponential",constraintIndexes,pd,constraintPars);
            checker=new ConstraintLocationChecker_ImageShape(constraintIndexes,cIS);
            constraints.add(new ConstraintNode(cf,checker));
        }
        aModel.updateModel(svFunctionTypes, constraints, pdPars);
        return aModel;
    }

     public static FittingModelNode getDefaultIPOGaussianFittingModel(int[][] pixels,ImageShape cIS, String sFunctionType, ArrayList<Point> peaks, double scale){
         return getDefaultIPOGaussianFittingModel(pixels,cIS,sFunctionType,peaks,scale,-1,false);
     }
     public static FittingModelNode getDefaultIPOGaussianFittingModel(int[][] pixels,ImageShape cIS, String sFunctionType, ArrayList<Point> peaks, double scale,int filteringRadius, boolean singleRegion){
        //sFunctionType: "gaussian2D_GaussianPars" or "gaussian2D_GaussianCircular"
        int w=pixels[0].length,h=pixels.length;
        intRange xRange=cIS.getXrange(),yRange=cIS.getYrange();
        ArrayList<Point> points=new ArrayList();
        ArrayList<ConstraintNode> constraints=new ArrayList();
        ArrayList<Integer> constraintIndexes;
        ConstraintViolationChecker checker;
        Point pt;
        ImageShape circle=new CircleImage(1);
        circle.setFrameRanges(cIS.getXFrameRange(), cIS.getYFrameRange());
        int nMin=Integer.MAX_VALUE;

        if(singleRegion){
            Point ptt=cIS.getLocation();
            int[][]pixelst=CommonStatisticsMethods.copyArray(pixels,yRange.getMin()+ptt.y,yRange.getMax()+ptt.y,xRange.getMin()+ptt.x,xRange.getMax()+ptt.x);
            int x,y;
            if(filteringRadius>0) CommonStatisticsMethods.meanFiltering(pixelst, filteringRadius);
            CommonMethods.displayPixels(pixelst, "region ", ImagePlus.GRAY32);
            int[][] stamp=new int[pixelst.length][pixelst[0].length];
            LandscapeAnalyzerPixelSorting.stampPixels(pixelst, stamp);

            ArrayList<Point> pts=new ArrayList();
            int rId=LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[peaks.get(0).y-ptt.y][peaks.get(0).x-ptt.x]);

            for(y=0;y<h;y++){
                for(x=0;x<w;x++){
                    if(LandscapeAnalyzerPixelSorting.getRegionIndex(stamp[y][x])==rId){
                        pts.add(new Point(x,y));
                    }
                }
            }
            cIS=ImageShapeHandler.buildImageShape_Scattered(pts);
            cIS.setLocation(ptt);
        }
        
        cIS.getInnerPoints(points);
        int len=points.size(),i,j,nn=Integer.MAX_VALUE,pixel,len1=points.size();
        ArrayList<String> svFunctionTypes=new ArrayList();

        double[] pdY=new double[len];
        double[][] pdX=new double[len][2];
        intRange pixelRange=new intRange();
        ImageShapeHandler.getPixels(pixels, cIS, pdX, pdY,scale,pixelRange);
        FittingModelNode aModel=new FittingModelNode(pdX,pdY);

        aModel.nI=0;
        aModel.nF=len-1;
        aModel.nDelta=1;

        len=peaks.size();
        double[] constraintPars,constraintOrigin;
        int nParsPerTerm=ComposedFittingFunction.getNumParsPerTerm(sFunctionType);
        int num=len*nParsPerTerm+1,index;
        double[] pdPars=new double[num];
        double sigma;

        double base=pixelRange.getMin(),pixelp;

        num=0;
        pdPars[0]=base;
        num++;

        for(i=0;i<len;i++){
            pt=peaks.get(i);
            svFunctionTypes.add(sFunctionType);
            circle.setCenter(pt);
            pixelp=ImageShapeHandler.getMean(pixels, circle,cIS)*scale;
            pdPars[num]=pixelp-base;
            num++;
            sigma=1.5;
            pdPars[num]=sigma;
            num++;
            if(sFunctionType.contentEquals("gaussian2D_GaussianPars")){
                pdPars[num]=sigma;
                num++;
                pdPars[num]=0.8;//theta
                num++;
            }
            pdPars[num]=pt.x;
            num++;
            pdPars[num]=pt.y;
            num++;

            constraintIndexes=new ArrayList();
            constraintIndexes.add(num-2);
            constraintIndexes.add(num-1);
            constraintPars=new double[2];
            constraintPars[0]=20;
            constraintPars[1]=2000;
            double[] pd={pt.x,pt.y};
            DistantConstraintFunction cf=new DistantConstraintFunction("exponential",constraintIndexes,pd,constraintPars);
            checker=new ConstraintLocationChecker_ImageShape(constraintIndexes,cIS);
            constraints.add(new ConstraintNode(cf,checker));
        }
        aModel.updateModel(svFunctionTypes, constraints, pdPars);
        aModel.m_cIS=cIS;
        return aModel;
    }
    public static void reduceToBasicIPOGaussianFittingModel(int[][] pixelsp,FittingModelNode aModel){
        //sFunctionType: "gaussian2D_GaussianPars" or "gaussian2D_GaussianCircular"
        ArrayList<Point> peaks=aModel.mainPeaks;
        ArrayList<ConstraintNode> constraints=new ArrayList();
        ArrayList<Integer> constraintIndexes;
        ConstraintViolationChecker checker;
        Point pt;
        int len=aModel.m_pdX.length,i,nn=Integer.MAX_VALUE,pixelp;
        double dt,dn=Double.POSITIVE_INFINITY;
        ArrayList<String> svFunctionTypes=new ArrayList();
        double pdY[]=aModel.m_pdY;

        ImageShape cIS=new RectangleImage(3, 3);
        cIS.setFrameRanges(new intRange(0,pixelsp[0].length-1), new intRange(0,pixelsp.length-1));

        ArrayList<Point> points=new ArrayList();
        aModel.m_cIS.getInnerPoints(points);
        len=points.size();
        for(i=0;i<len;i++){
            pt=points.get(i);
            cIS.setCenter(pt);
            dt=ImageShapeHandler.getMean(pixelsp, cIS);
            if(dt<dn) dn=dt;
        }

        len=peaks.size();
        double[] constraintPars;
        int nParsPerTerm=6;
        int num=len*nParsPerTerm+1,index;
        double[] pdPars=new double[num];
        double sigma=1.5;

        num=0;
        pdPars[0]=dn;
        num++;
        for(i=0;i<len;i++){
            pt=peaks.get(i);
            svFunctionTypes.add("gaussian2D_GaussianPars");
            cIS.setCenter(pt);
            pixelp=(int)ImageShapeHandler.getMean(pixelsp, cIS);
            pdPars[num]=pixelp-dn;
            num++;
            sigma=1.5;
            pdPars[num]=sigma;
            num++;
            pdPars[num]=sigma;
            num++;
            pdPars[num]=0.8;//theta
            num++;
            pdPars[num]=pt.x;
            num++;
            pdPars[num]=pt.y;
            num++;
            constraintIndexes=new ArrayList();
            constraintIndexes.add(num-2);
            constraintIndexes.add(num-1);
            constraintPars=new double[2];
            constraintPars[0]=20;
            constraintPars[1]=2000;
            double[] pd={pt.x,pt.y};
            DistantConstraintFunction cf=new DistantConstraintFunction("exponential",constraintIndexes,pd,constraintPars);
            checker=new ConstraintLocationChecker_ImageShape(constraintIndexes,aModel.m_cIS);
            constraints.add(new ConstraintNode(cf,checker));
            aModel.freeAllPars();
        }
        aModel.updateModel(svFunctionTypes, constraints, pdPars);
    }
    public static void freezePeakLocations(FittingModelNode aModel, boolean freezeConstant){
        //sFunctionType: "gaussian2D_GaussianPars" or "gaussian2D_GaussianCircular"
        ArrayList<String> svFunctionTypes=aModel.svFunctionTypes;
        String sType;
        int i,len=svFunctionTypes.size();
        ArrayList<Integer> nvNumPars=aModel.nvNumParameters;
        int nPars=1,num=0,index;
        ArrayList<Integer> indexes=new ArrayList();
        if(freezeConstant&&!CommonMethods.containsContent(aModel.pnFixedParIndexes, 0)) indexes.add(0);
        for(i=0;i<len;i++){
            num=nvNumPars.get(i);
            nPars+=num;
            sType=svFunctionTypes.get(i);
            if(!sType.startsWith("gaussian2D"))continue;
            index=nPars-2;
            if(!CommonMethods.containsContent(aModel.pnFixedParIndexes,index)) indexes.add(index);
            index=nPars-1;
            if(!CommonMethods.containsContent(aModel.pnFixedParIndexes,index)) indexes.add(index);
        }
        if(aModel.pnFixedParIndexes!=null){
            len=aModel.pnFixedParIndexes.length;
            for(i=0;i<len;i++){
                indexes.add(aModel.pnFixedParIndexes[i]);
            }
        }
        len=indexes.size();
        aModel.pnFixedParIndexes=new int[len];
        for(i=0;i<len;i++){
            aModel.pnFixedParIndexes[i]=indexes.get(i);
        }
    }

    public static int freePeakLocations(FittingModelNode aModel, boolean freeConstant){
        //sFunctionType: "gaussian2D_GaussianPars" or "gaussian2D_GaussianCircular"
        if(aModel.pnFixedParIndexes==null) return -1;
        ArrayList<String> svFunctionTypes=aModel.svFunctionTypes;
        String sType;
        int i,len=svFunctionTypes.size(),index;
        ArrayList<Integer> nvNumPars=aModel.nvNumParameters;
        int nPars=1,num=0;
        ArrayList<Integer> indexes=new ArrayList(),indexes0=new ArrayList();
        if (freeConstant) indexes.add(0);
        for(i=0;i<len;i++){
            num=nvNumPars.get(i);
            nPars+=num;
            sType=svFunctionTypes.get(i);
            if(!sType.startsWith("gaussian2D"))continue;
            index=nPars-2;
            indexes.add(index);
            index=nPars-1;
            indexes.add(index);
        }

        len=aModel.pnFixedParIndexes.length;
        int len1=indexes.size(),j;
        boolean exists;
        for(i=0;i<len;i++){
            index=aModel.pnFixedParIndexes[i];
            exists=false;
            for(j=0;j<len1;j++){
                if(index==indexes.get(j)){
                    exists=true;
                    break;
                }
            }
            if(exists) continue;
            indexes0.add(index);
        }
        len=indexes0.size();
        if(len==0){
            aModel.pnFixedParIndexes=null;
            return 1;
        }
        aModel.pnFixedParIndexes=new int[len];
        for(i=0;i<len;i++){
            aModel.pnFixedParIndexes[i]=indexes0.get(i);
        }
        return 1;
    }

    public static void addOneGaussianComponent(FittingModelNode aModel,int[][] pixels,Point peak, String sFunctionType){
        //sFunctionType: "gaussian2D_GaussianPars" or "gaussian2D_GaussianCircular"
        ArrayList<Point> points=new ArrayList();
        ArrayList<ConstraintNode> constraints=aModel.cvConstraintNodes;
        ArrayList<Integer> constraintIndexes;
        ConstraintViolationChecker checker;
        Point pt;
        int len=aModel.m_pdY.length,i,j;
        ImageShape cIS= aModel.m_cIS;
        if(cIS==null){
            for(i=0;i<len;i++){
                points.add(new Point((int)(aModel.m_pdX[i][0]+0.5),(int)(aModel.m_pdX[i][1]+0.5)));
            }
            cIS=ImageShapeHandler.buildImageShape_Scattered(points);
        }

        double base=CommonStatisticsMethods.getMin(aModel.m_pdY);

        int nPars0=aModel.nNumPars;

        aModel.addOneComponent(sFunctionType);

        double[] constraintPars,constraintOrigin;

        int num=nPars0;
        double[] pdPars=aModel.pdPars;
        pdPars[num]=pixels[peak.y][peak.x]-base;
        num++;
        double sigma=1.5;
        pdPars[num]=sigma;
        num++;
        if(sFunctionType.contentEquals("gaussian2D_GaussianPars")){
            pdPars[num]=sigma;
            num++;
            pdPars[num]=0.8;//theta
            num++;
        }
        pdPars[num]=peak.x;
        num++;
        pdPars[num]=peak.y;
        num++;

        constraintIndexes=new ArrayList();
        constraintIndexes.add(num-2);
        constraintIndexes.add(num-1);
        constraintPars=new double[2];
        constraintPars[0]=20;
        constraintPars[1]=2000;
        double[] pd={peak.x,peak.y};
        DistantConstraintFunction cf=new DistantConstraintFunction("exponential",constraintIndexes,pd,constraintPars);
        checker=new ConstraintLocationChecker_ImageShape(constraintIndexes,cIS);
        
        constraints.add(new ConstraintNode(cf,checker));
    }
    public static void mergeCrowedPeaks(ArrayList<Point> peaks, double cutoff){
        int merge=mergePeakPair(peaks,cutoff);
        while(merge==1){
            merge=mergePeakPair(peaks,cutoff);
        }
    }

    public static int mergePeakPair(ArrayList<Point> peaks, double cutoff){
        int len=peaks.size(),i,j;
        double dist2=cutoff*cutoff;
        Point p1,p2;
        for(i=0;i<len-1;i++){
            p1=peaks.get(i);
            for(j=i+1;j<len;j++){
                p2=peaks.get(j);
                if(CommonMethods.getDist2(p1.x, p1.y, p2.x, p2.y)<dist2){
                    Point pt=new Point((p1.x+p2.x)/2,(p1.y+p2.y)/2);
                    peaks.remove(j);
                    peaks.remove(i);
                    peaks.add(pt);
                    return 1;
                }
            }
        }
        return -1;
    }
    public static FittingModelNode getDummyIPOGModel(){
        FittingModelNode cModel=new FittingModelNode();
        cModel.pdPars=new double[7];
        double[] pdPars=cModel.pdPars;
        pdPars[0]=1000;
        pdPars[1]=100;
        pdPars[2]=1.5;
        pdPars[3]=1.5;
        pdPars[4]=1;
        pdPars[5]=0;
        pdPars[6]=0;
        return cModel;                
    }
    public static IPOGaussianNodeComplex getDummyIPOG(){
        double[] pdPars=new double[7];
        pdPars[0]=1000;
        pdPars[1]=100;
        pdPars[2]=1.5;
        pdPars[3]=1.5;
        pdPars[4]=1;
        pdPars[5]=0;
        pdPars[6]=0;
        IPOGaussianNodeComplex IPOG=new IPOGaussianNodeComplex(pdPars);
        IPOG.setAsDummy();
        return IPOG;                
    }
    public static boolean isDummyIPOGModel(FittingModelNode aModel){
        FittingModelNode cModel=getDummyIPOGModel();
        return CommonStatisticsMethods.equalArrays(aModel.pdPars,cModel.pdPars);
    }
    public static boolean isDummyIPOG(IPOGaussianNodeComplex IPOG){
        IPOGaussianNodeComplex IPOGt=getDummyIPOG();
        return CommonStatisticsMethods.equalArrays(IPOG.pdPars,IPOGt.pdPars);
    }
}
