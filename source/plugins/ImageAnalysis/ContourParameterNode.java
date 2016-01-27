/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageAnalysis;
import java.util.ArrayList;
import java.awt.Point;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.io.PrintAssist;
import utilities.CustomDataTypes.intRange;
import ij.ImagePlus;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CustomDataTypes.DoublePair;
import utilities.CommonGuiMethods;
import utilities.Gui.PlotWindowPlus;
import java.awt.Color;
import ImageAnalysis.ContourFollower;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import ij.IJ;
import utilities.io.PrintAssist;
import ij.gui.PointRoi;
import FluoObjects.IPOGContourParameterNode;
import ij.gui.ImageCanvas;
import ImageAnalysis.RegionBoundaryAnalyzer;
import ImageAnalysis.LandscapeAnalyzerPixelSorting;
import ImageAnalysis.RegionNode;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.Point2D;
import utilities.Geometry.ConvexPolygon;
/**
 *
 * @author Taihao
 */
public class ContourParameterNode implements MouseMotionListener{
    public static final int IPOG=0, GaussianMean=1;
    public static final int UpperBound=-1, LowerBound=1, Band=0;
    public int FunctionType;
    public ArrayList<Point> Contour;
    public ArrayList<DoublePair> SubpixelContour;
    public ArrayList<Double> Curvatures,SmoothedDentDepth,Dists,SmoothedDists;
    public int numConcaveCurves,numCurvatureMaxima;
    public ArrayList<Integer> nvCurvatureMaxima,nvCurvatureMinima,nvDistMinima,nvDistMaxima;
    public double LinearityDevL,LinearityDevS,Ds,Dl,Rll,Rls,Rsl,Rss,AsymmetryRl,AsymmetryRs;
    public double deltaX,deltaY;
    public double positionDevX,positionDevN,curvatureCircularAsymmetry,distCircularAsymmetry;  
    public ArrayList<Double> dvDentDepth;
    DoubleRange cCurvatureRange,cDistRange,cValueRange,cAmpRange;
    RegionBoundaryAnalyzer cRBA;
    int nWsSmoothing;
    DoublePair origin;
    TwoDFunction fun;  
    int nMag,nRangeType;
    
    boolean Ovalish,showContour;
    Point Centroid,PeakPoint,LowestPoint,DisplayShift;
    intRange xContourRange,yContourRange;
    ImagePlus implContour;
    PlotWindowPlus pwCurvature, pwDistance;
    public double MeanDist,Signal;
    int Area,w,h; 
    double width,height;
    intRange xRange,yRange;
    int[][] pixels;
    ImageShape m_cIS;
    boolean bRegular;
    DoublePair CentralPeakShift;
    
    public ContourParameterNode(TwoDFunction fun, DoublePair origin, int mag){
        this.fun=fun;
        this.FunctionType=fun.getTowDFunctionType();
        this.origin=new DoublePair(origin);
        dvDentDepth=new ArrayList();
        nMag=mag;
        deltaX=1./(double)mag;
        deltaY=1./(double)mag;
        nWsSmoothing=5;
        this.showContour=showContour;
        h=-1;
        w=-1;
        bRegular=false;
        pixels=null;
        DoubleRange xRange=new DoubleRange(), yRange=new DoubleRange();
        fun.getFrameRanges(xRange, yRange);
        CentralPeakShift=new DoublePair(0,0);
        if(xRange.isRegularRange()) //it's true only for GaussianMean and GaussianMeanRaw
            buildLandscape(xRange, yRange);
    }
    int buildLandscape(DoubleRange xDR, DoubleRange yDR){
        xRange=new intRange((int)(xDR.getMin()+0.5),(int)(xDR.getMax()+0.5));
        yRange=new intRange((int)(yDR.getMin()+0.5),(int)(yDR.getMax()+0.5));
        w=nMag*xRange.getRange();
        h=nMag*yRange.getRange();
        pixels=new int[h][w];
        int x0=xRange.getMin(),y0=yRange.getMin(),i,j,pixel;
        double x,y;
        for(i=0;i<h;i++){
            y=y0+i*deltaY+origin.right;
            for(j=0;j<w;j++){
                x=x0+j*deltaX+origin.left;
                pixels[i][j]=(int)(fun.func(x, y)+0.5);
            }
        }
        if(false){
            ImagePlus impl=CommonMethods.displayPixels(pixels, "Contour", ImagePlus.GRAY32);
            impl.show();
            CommonMethods.showLandscapeAndComplex(impl, true, true, 0., false, 0, h-1, 0, w-1);
        }
        cRBA=CommonMethods.buildRegionComplex(pixels);
        
        Point pm=((SubpixelGaussianMean)fun).Center;
        pm=new Point(Math.min((pm.x-xRange.getMin())*nMag+nMag/2,w-1),Math.min((pm.y-yRange.getMin())*nMag+nMag/2,h-1));
        Point pt=cRBA.getRegionPeak(pm);
        if(pt==null){
            pt=pt;
        }
        if(pt!=null)
            CentralPeakShift=new DoublePair((pt.x-pm.x)/(double)nMag,(pt.y-pm.y)/(double)nMag);
        else
            CentralPeakShift=new DoublePair(200,200);//it indicates unreliable contour
        return 1;
    }

    public ArrayList<Point> getRegionBoundaryPoints(Point p0){
        RegionNode aNode=cRBA.getRegionNode(p0);
        ArrayList<Point> points=new ArrayList();
        ArrayList<RegionBorderSegmentNode> rbSegs=aNode.boundarySegments;
        ArrayList<RegionBorderNode> rbs;
        int i,j,len=rbSegs.size(),len1;
        RegionBorderSegmentNode rbSeg;
        for(i=0;i<len;i++){
            rbSeg=rbSegs.get(i);
            rbs=rbSeg.BoundaryNodes;
            len1=rbs.size();
            for(j=0;j<len1;j++){
                points.add(rbs.get(j).location);
            }
        
        }
        return points;
    }
    public double getRegionBondaryPeakValue(Point p0){
        int x0=xRange.getMin(),y0=yRange.getMin(),i,j,pixel;
        Point p=new Point(p0.x-x0*nMag,p0.y-y0*nMag);        
        ArrayList<Point> points=getRegionBoundaryPoints(p);
        DoubleRange dr=new DoubleRange();
//        ImagePlus impl=CommonMethods.getBlankImage(ImagePlus.GRAY32, 100, 100);
//        float[] pixels=(float[]) impl.getProcessor().getPixels();
        
        double dt;
        for(i=0;i<points.size();i++){
            p=points.get(i);
            dt=fun.func(x0+p.x*deltaX+origin.left, y0+p.y*deltaX+origin.right);
            dr.expandRange(dt);
//            pixels[p.y*100+p.x]=(float) dt;
        }
//        impl.show();
        return dr.getMax();
    }
    public int buildContourParameterNode(Point InsidePoint, DoublePair peakPoint, DoubleRange dr, int RangeType, boolean showContour){
        this.fun=fun;
        this.FunctionType=fun.getTowDFunctionType();
        this.origin=new DoublePair(origin);
        cValueRange=new DoubleRange(dr);
        Contour=new ArrayList();
        SubpixelContour=new ArrayList();
        Ovalish=false;
        nWsSmoothing=5;
        this.showContour=showContour;
        DoubleRange drt=new DoubleRange(dr);
        if(Double.isNaN(dr.getMin())){
            dr=dr;
        }
        if(Double.isNaN(dr.getMax())){
            dr=dr;
        }
        nRangeType=RangeType;
        if(nRangeType==LowerBound)
            drt.setMax(Double.POSITIVE_INFINITY);
        else if(nRangeType==UpperBound)
            drt.setMin(Double.NEGATIVE_INFINITY);
        
        double dt=fun.func(origin.left+InsidePoint.x*deltaX, origin.right+InsidePoint.y*deltaY);
        if(Double.isNaN(dt)){
            dt=dt;
        }
        if(!drt.contains(dt)){
            drt=fun.getValueRange();
            drt=new DoubleRange(drt.getRange()*dr.getMin()/dr.getMax(),drt.getRange());
            origin=fun.getPeak();
            InsidePoint=new Point((int) origin.left,(int) origin.right);
            origin=new DoublePair(origin.left-InsidePoint.x,origin.right-InsidePoint.y);
            InsidePoint.setLocation(InsidePoint.x*nMag,InsidePoint.y*nMag);
            dt=fun.func(origin.left+InsidePoint.x*deltaX, origin.right+InsidePoint.y*deltaY);
        }
        
        
        ContourFollower.getContour_Out(fun, origin.left, origin.right, deltaX, deltaY, InsidePoint, drt, Contour, SubpixelContour);    
        if(Contour.size()>=9) {
            m_cIS=ImageShapeHandler.buildImageShape(Contour);
            Contour=m_cIS.getPerimeterPoints();
            if(Contour.size()>9){
                SubpixelContour.clear();
                for(int i=0;i<Contour.size();i++){
                    SubpixelContour.add(new DoublePair(Contour.get(i).x,Contour.get(i).y));
                }
                parameterizeContour();
                bRegular=true;
            }
        }        
        if(Contour.size()>=9) if(showContour) showContour();
        return 1;
    }
    public boolean IsRegular(){
        return bRegular;
    }
    public void getContours(ArrayList<Point> Contour, ArrayList<DoublePair> SubpixelContour){
        Contour.clear();
        SubpixelContour.clear();
        for(int i=0;i<this.Contour.size();i++){
            Contour.add(this.Contour.get(i));
            SubpixelContour.add(this.SubpixelContour.get(i));
        }
    }
    public double getValue(int x, int y){
        return fun.func(origin.left+x*deltaX, origin.right+y*deltaY);
    }
    public int showContour(){
        if(Contour==null) return -1;
        if(Contour.isEmpty()) return -1;
        int nExt=100;
        int w=Math.max(PeakPoint.x-xContourRange.getMin(), xContourRange.getMax()-PeakPoint.x)+nExt;
        int h=Math.max(PeakPoint.y-yContourRange.getMin(), yContourRange.getMax()-PeakPoint.y)+nExt;
        DisplayShift=new Point(w/2-PeakPoint.x,h/2-PeakPoint.y);
        implContour=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w, h);
        String title="Contour: "+getFunctionTypeAsString();
        implContour.setTitle(title);
        CommonGuiMethods.showTempImage(implContour);
        implContour.getCanvas().addMouseMotionListener(this);
        int[] pixels=(int[])implContour.getProcessor().getPixels();
        Point ct=new Point(w/2,h/2);
        DoublePair subShift=origin;
        double x0=subShift.left,y0=subShift.right;
        
        int x,y,o,r,g,b,pixel;
        double amp,peakAmp=cAmpRange.getMax(),lowestAmp=peakAmp;
        for(y=0;y<h;y++){
            o=y*w;
            for(x=0;x<w;x++){
                amp=getValue(x-DisplayShift.x,y-DisplayShift.y);
                if(amp>-40&&amp<lowestAmp){//amp>-40 is to remove the default -50 from the image
                    lowestAmp=amp;
                    LowestPoint.setLocation(x,y);       
                }
            }
        }
        peakAmp-=lowestAmp;
        for(y=0;y<h;y++){
            o=y*w;
            for(x=0;x<w;x++){
                amp=getValue(x-DisplayShift.x,y-DisplayShift.y)-lowestAmp;
                if(amp<0) amp=0;
                pixel=(int)(amp*150/peakAmp)+100;
                r=pixel;
                g=pixel;
                b=pixel;
                pixels[o+x]=(r<<16)|(g<<8)|b;
            }
        }
        showContour_Dists(implContour);
        if(m_cIS==null) m_cIS=ImageShapeHandler.buildImageShape(Contour);
        m_cIS.drawPolygon(implContour, Color.BLUE);
        m_cIS.drawMECRectangle(implContour, Color.red);
        IPOGContourParameterNode par=getIPOGContourParameterNode();
        
//        String[][] psData;
//        psData=getContourParsAsStringArray();
//        psData=par.getContourParsAsStrings();        
//        CommonGuiMethods.displayTable("Contour Parameters: "+getFunctionTypeAsString(), psData);
        int mag=4;
        implContour.getCanvas().setMagnification(mag);
        implContour.getWindow().setSize(mag*w,mag*h);
        CommonMethods.refreshImage(implContour);
        implContour.getWindow().toFront();

//        if(implContour!=null){
        if(false){
            ArrayList<Point> points=new ArrayList();
//            m_cIS.setCenter(new Point(w/2,h/2));
            m_cIS.setCenter(new Point(w/2,h/2));
            m_cIS.getInnerPoints(points);
            ImagePlus implt=CommonMethods.cloneImage(implContour);
            CommonMethods.drawTrail(implt, points, Color.BLACK.getRGB());
            CommonGuiMethods.showTempImage(implt);
        }

        return 1;
    }
    void calContourRanges(){
        int i,len=Contour.size();
        xContourRange=new intRange();
        yContourRange=new intRange();
        Point p;
        for(i=0;i<len;i++){
            p=Contour.get(i);
            xContourRange.expandRange(p.x);
            yContourRange.expandRange(p.y);
        }
    }
    void calCentroid(){
        int i,len=Contour.size(),X=0,Y=0;
        Point p;
        for(i=0;i<len;i++){
            p=Contour.get(i);
            X+=p.x;
            Y+=p.y;
        }
        Centroid=new Point(X/len,Y/len);
    }
    void calPeakPoint(){
        int x,y;
        PeakPoint=new Point(xContourRange.getMin(),yContourRange.getMin());
        LowestPoint=new Point(PeakPoint);
        double zMax=fun.func(origin.left+PeakPoint.x*deltaX, origin.right+PeakPoint.y*deltaY),z,zMin=zMax;
        Area=0;
        Signal=0;
        DoubleRange drt=new DoubleRange(cValueRange);
        
        if(nRangeType==LowerBound)
            drt.setMax(Double.POSITIVE_INFINITY);
        else if(nRangeType==UpperBound)
            drt.setMin(Double.NEGATIVE_INFINITY);
        
        for(y=yContourRange.getMin();y<=yContourRange.getMax();y++){
            for(x=xContourRange.getMin();x<=xContourRange.getMax();x++){
                z=fun.func(origin.left+x*deltaX, origin.right+y*deltaY);
                if(z>zMax) {
                    zMax=z;
                    PeakPoint.setLocation(x,y);
                }
                if(z<zMin) {
                    zMin=z;
                    LowestPoint.setLocation(x,y);
                }
                if(drt.contains(z)){
                    Area++;
                    cValueRange.expandRange(z);
                    Signal+=z;
                }
            }
        }
        cAmpRange=new DoubleRange(zMin,zMax);
    }
    public void parameterizeContour(){
                
        calContourRanges();
        calPeakPoint();
        calCentroid();
        calDists();
        
        double r=0.5*CommonMethods.getDistance(SubpixelContour.get(0),SubpixelContour.get(1));
        boolean ContainsNull=false;
        for(int i=0;i<SubpixelContour.size();i++){
            if(SubpixelContour.get(i)==null){
                ContainsNull=true;
                break;
            }
        }
//        if(!ContainsNull) Curvatures=ContourFollower.calCurvatures_Subpixel(fun,SubpixelContour,r,cValueRange);
        if(!ContainsNull) Curvatures=m_cIS.getDentDepths();
//        Curvatures=Dists;
        SmoothedDentDepth=CommonStatisticsMethods.getSoomthedCircularArray(Curvatures, nWsSmoothing);
        cCurvatureRange=CommonStatisticsMethods.getRange(SmoothedDentDepth);
        nvCurvatureMinima=new ArrayList(); 
        nvCurvatureMaxima=new ArrayList();
        ArrayList<Double> dvHeights=new ArrayList();
        
        int lenc=Curvatures.size(),lend=Dists.size(),i;
        double[] pdXc=new double[lenc],pdXd=new double[lend];
        for(i=0;i<Math.max(lenc, lend);i++){
            if(i<lend) pdXd[i]=i+1;
            if(i<lenc) pdXc[i]=i+1;
        }
        String title=getFunctionTypeAsString();
        CommonStatisticsMethods.getLocalExtrema_Circular(SmoothedDentDepth, nvCurvatureMinima, nvCurvatureMaxima,dvHeights);
        if(nvCurvatureMinima.isEmpty()){
            nvCurvatureMinima.add(0);
            nvCurvatureMaxima.add(lenc/2);
            dvHeights.add(0.);
        }
//        if(showContour){
        if(false){
            pwDistance=CommonGuiMethods.displayNewPlotWindowPlus("Distance "+title, pdXd, CommonStatisticsMethods.copyToDoubleArray(Dists), 1, 2, Color.black);
            pwCurvature=CommonGuiMethods.displayNewPlotWindowPlus("Curvatures "+title, pdXc, CommonStatisticsMethods.copyToDoubleArray(Curvatures), 1, 2, Color.black);
            pwDistance.getImagePlus().getCanvas().addMouseMotionListener(this);
            pwCurvature.getImagePlus().getCanvas().addMouseMotionListener(this);

            pwDistance.addPlot("Smoothed", pdXd, CommonStatisticsMethods.copyToDoubleArray(SmoothedDists),2,2,Color.black);
            pwCurvature.addPlot("Smoothed", pdXc, CommonStatisticsMethods.copyToDoubleArray(SmoothedDentDepth),2,2,Color.black);            
        }
        nvDistMinima=new ArrayList();
        nvDistMaxima=new ArrayList();
        CommonStatisticsMethods.getLocalExtrema_Circular(SmoothedDists, nvDistMinima, nvDistMaxima,dvHeights);
        if(nvDistMinima.isEmpty()){
            nvDistMinima.add(0);
            nvDistMaxima.add(lend/2);
            dvHeights.add(0.);
        }
        int p,p0,num;
        numConcaveCurves=0;
        num=SmoothedDentDepth.size();
        double cur=0;
        for(i=0;i<num;i++){
            cur+=Math.abs(SmoothedDentDepth.get(i));
        }
        cur/=num;
        double cutoff=0.01*cur;
        
        ArrayList<Integer> crossingPoints=CommonStatisticsMethods.getCrossingPositions(CommonStatisticsMethods.copyToDoubleArray(SmoothedDentDepth), 0);
        num=crossingPoints.size();
        numConcaveCurves=0;
        if(num>0){
            int l=crossingPoints.get(num-1),R;
            lenc=SmoothedDentDepth.size();
            double cMin=0;
            DoubleRange dr=new DoubleRange();
            
            for(i=0;i<num;i++){
                R=crossingPoints.get(i)+1;
                p=l;
                cMin=0;
                while(p!=R){
                    cur=SmoothedDentDepth.get(p);
                    if(cur<cMin) cMin=cur;
                    p=CommonStatisticsMethods.getCircularIndex(p+1, lenc);                
                }
                if(-1*cMin>cutoff&&Math.abs(cMin)>1)numConcaveCurves++;//13225 now cMin is the dent depth
                l=R;
            }
        }
        
        num=nvDistMinima.size();
        intRange numRange=new intRange();
        p0=nvDistMinima.get(num-1);;
        for(i=0;i<num;i++){
            p=nvDistMinima.get(i);
            numRange.expandRange(CommonStatisticsMethods.getCircularDist(p0, p, lend));
            p0=p;
        }
        positionDevN=(double)(numRange.getMax()-numRange.getMin())/(double)(numRange.getMax()+numRange.getMin());
        
        num=nvDistMaxima.size();
        numRange=new intRange();
        p0=nvDistMaxima.get(num-1);
        for(i=0;i<num;i++){
            p=nvDistMaxima.get(i);
            numRange.expandRange(CommonStatisticsMethods.getCircularDist(p0, p, lend));
            p0=p;
        }
        positionDevX=(double)(numRange.getMax()-numRange.getMin())/(double)(numRange.getMax()+numRange.getMin());
        
        numCurvatureMaxima=nvCurvatureMaxima.size();
        curvatureCircularAsymmetry=CommonStatisticsMethods.getCircularAsymmetry(SmoothedDentDepth);
        distCircularAsymmetry=CommonStatisticsMethods.getCircularAsymmetry(SmoothedDists);
        if(nvDistMaxima.size()==2&&numConcaveCurves==0) Ovalish=true;
        if(Ovalish) calOvalishPars();        
    }
    public boolean isOvalish(){
        return Ovalish;
    }
    int calOvalishPars(){
        if(!Ovalish) return -1;
        double dt;
        Point pl1=Contour.get(nvDistMaxima.get(0)),pl2=Contour.get(nvDistMaxima.get(1))
                ,ps1=Contour.get(nvDistMinima.get(0)),ps2=Contour.get(nvDistMinima.get(1)),pp=PeakPoint;
        Dl=CommonMethods.getDistance(pl1.x, pl1.y, pl2.x, pl2.y);
        Ds=CommonMethods.getDistance(ps1.x, ps1.y, ps2.x, ps2.y);
        Rll=CommonMethods.getDistance(pl1.x, pl1.y, pp.x, pp.y);
        Rls=CommonMethods.getDistance(pl2.x, pl2.y, pp.x, pp.y);
        Rsl=CommonMethods.getDistance(ps1.x, ps1.y, pp.x, pp.y);
        Rss=CommonMethods.getDistance(ps2.x, ps2.y, pp.x, pp.y);
        if(Rll<Rls){
            dt=Rll;
            Rll=Rls;
            Rls=dt;
        }
        if(Rsl<Rss){
            dt=Rsl;
            Rsl=Rss;
            Rss=dt;
        }
        LinearityDevL=CommonMethods.getDistanceToLine(pl1, pl2, pp)/Dl;
        LinearityDevS=CommonMethods.getDistanceToLine(ps1, ps2, pp)/Ds;
        AsymmetryRl=(Rll-Rls)/(Rll+Rls);
        AsymmetryRs=(Rsl-Rss)/(Rsl+Rss);
        return 1;
    }
    public String[][] getContourParsAsStringArray(){
        ArrayList<String> names=new ArrayList(), values=new ArrayList();
        getContourParsAsStrings(names,values);
        int i,len=names.size();
        String[][] psData=new String[2][len];
        for(i=0;i<len;i++){
            psData[0][i]=names.get(i);
            psData[1][i]=values.get(i);            
        }
        return psData;
    }
    public int getContourParsAsStrings(ArrayList<String> names, ArrayList<String> values){
        names.clear();
        values.clear();
        names.add("nXd");
        values.add(PrintAssist.ToString(numCurvatureMaxima));
        names.add("nXc");
        values.add(PrintAssist.ToString(nvDistMaxima.size()));
        names.add("nCV");
        values.add(PrintAssist.ToString(numConcaveCurves));
        names.add("RatioD");
        values.add(PrintAssist.ToString(cDistRange.getMax()/cDistRange.getMin(),3));
//        names.add("PositionDevX");
//        values.add(PrintAssist.ToString(positionDevX,3));
//        names.add("PositionDevN");
//        values.add(PrintAssist.ToString(positionDevN,3));
        names.add("CASd");
        values.add(PrintAssist.ToString(distCircularAsymmetry,4));
        
        names.add("CASc");
        values.add(PrintAssist.ToString(curvatureCircularAsymmetry,4));
        if(!Ovalish) return -1;
        
        names.add("Rl");
        values.add(PrintAssist.ToString(0.5*(Rls+Rll),3));
        
        names.add("Rs");
        values.add(PrintAssist.ToString(0.5*(Rss+Rsl),3));
        
        names.add("A.S.R");
        values.add(PrintAssist.ToString(Math.max(AsymmetryRl,AsymmetryRs),3));
        
        names.add("L. Dev.");
        values.add(PrintAssist.ToString(Math.max(Math.abs(LinearityDevL), Math.abs(LinearityDevS)),3));
        return 1;
    }
    public Point getPeak(){
        return PeakPoint;
    }
    public int showContour(ImagePlus impl){
        if(impl.getType()!=ImagePlus.COLOR_RGB) return -1;
        int w=impl.getWidth(),h=impl.getHeight(),i;
        int ctX=w/2,ctY=h/2,shiftX=ctX-PeakPoint.x,shiftY=ctY-PeakPoint.y,pixel;
        int[] pixels=(int[])impl.getProcessor().getPixels();
        double cur;
        Point p;
        int index,x,y;
        for(i=0;i<Contour.size();i++){
            p=Contour.get(i);
            cur=SmoothedDentDepth.get(i);
            pixel=CommonMethods.RBSpectrumColor(cur, cCurvatureRange.getMin()-1, cCurvatureRange.getMin(), cCurvatureRange.getMax()).getRGB();
            x=p.x+shiftX;
            y=p.y+shiftY;
            pixels[y*w+x]=pixel;
        }
        double dMin=cCurvatureRange.getMin(),dMax=cCurvatureRange.getMax();
        for(y=0;y<h;y++){
            cur=CommonMethods.getLinearIntoplation(h-1, dMin, 0, dMax, y);
            pixel=CommonMethods.RBSpectrumColor(cur, cCurvatureRange.getMin()-1, cCurvatureRange.getMin(), cCurvatureRange.getMax()).getRGB();
            for(x=w-10;x<w;x++){
                pixels[y*w+x]=pixel;
            }
        }
        highlightContourImage(PeakPoint,Color.RED.getRGB());
        highlightContourImage(Centroid,Color.BLUE.getRGB());
        pixel=Color.BLUE.getRGB();
        for(i=0;i<nvCurvatureMinima.size();i++){
            highlightContourImage(Contour.get(nvCurvatureMinima.get(i)),pixel);
        }
        pixel=Color.GREEN.getRGB();
        for(i=0;i<nvCurvatureMaxima.size();i++){
            highlightContourImage(Contour.get(nvCurvatureMaxima.get(i)),pixel);
        }
        return 1;
    }
    public int highlightContourImage(Point p, int pixel){
        if(implContour==null) return -1;        
        int x,y,o,w=implContour.getWidth(),h=implContour.getHeight(),xt,yt,shiftX=w/2-PeakPoint.x,shiftY=h/2-PeakPoint.y;
        int[] pixels=(int[]) implContour.getProcessor().getPixels();
        for(y=p.y-1;y<=p.y+1;y++){
            yt=y+shiftY;
            if(yt<0||yt>=h) continue;
            o=yt*w;
            for(x=p.x-1;x<=p.x+1;x++){
                xt=x+shiftX;
                if(xt<0||xt>=w) continue;
                pixels[o+xt]=pixel;
            }
        }
        return 1;
    }
    public int showContour_Dists(ImagePlus impl){
        if(impl.getType()!=ImagePlus.COLOR_RGB) return -1;
        int w=impl.getWidth(),h=impl.getHeight(),i;
        int ctX=w/2,ctY=h/2,shiftX=ctX-PeakPoint.x,shiftY=ctY-PeakPoint.y,pixel;
        int[] pixels=(int[])impl.getProcessor().getPixels();
        double dist;
        Point p;
        int x,y;
        for(i=0;i<Contour.size();i++){
            p=Contour.get(i);
            dist=SmoothedDists.get(i);
//            pixel=CommonMethods.RBSpectrumColor(dist, cDistRange.getMin()-1, cDistRange.getMin(), cDistRange.getMax()).getRGB();
            pixel=Color.YELLOW.getRGB();
            x=p.x+shiftX;
            y=p.y+shiftY;
            if(y<0||y>=h) continue;
            if(x<0||x>=w) continue;
            pixels[y*w+x]=pixel;
        }
        double dMin=cDistRange.getMin(),dMax=cDistRange.getMax();
        for(y=0;y<h;y++){
            dist=CommonMethods.getLinearIntoplation(h-1, dMin, 0, dMax, y);
            pixel=CommonMethods.RBSpectrumColor(dist, cDistRange.getMin()-1, cDistRange.getMin(), cDistRange.getMax()).getRGB();
            for(x=w-10;x<w;x++){
//                pixels[y*w+x]=pixel;
            }
        }
        drawPolygon(impl,m_cIS.getMECPolygonVertices(),Color.blue);
        drawPolygon(impl,m_cIS.getMECRectangleVertices(),Color.green);
        if(true) return 1;
        highlightContourImage(PeakPoint,Color.RED.getRGB());
        highlightContourImage(Centroid,Color.BLUE.getRGB());
        pixel=Color.BLUE.getRGB();
        for(i=0;i<nvDistMinima.size();i++){
            highlightContourImage(Contour.get(nvDistMinima.get(i)),pixel);
        }
        pixel=Color.GREEN.getRGB();
        for(i=0;i<nvDistMinima.size();i++){
            highlightContourImage(Contour.get(nvDistMaxima.get(i)),pixel);
        }
        return 1;
    }
    public int showContour_FloatPoint(ImagePlus impl){
        if(impl.getType()!=ImagePlus.GRAY32) return -1;
        int w=impl.getWidth(),h=impl.getHeight(),i;
        int ctX=w/2,ctY=h/2,shiftX=ctX-PeakPoint.x,shiftY=ctY-PeakPoint.y,pixel;
        float[] pixels=(float[])impl.getProcessor().getPixels();
        double cur;
        Point p;
        int index,x,y;
        for(i=0;i<Contour.size();i++){
            p=Contour.get(i);
            pixel=0;
            x=p.x+shiftX;
            y=p.y+shiftY;
            pixels[y*w+x]=pixel;
        }
        
        impl.show();
        return 1;
    }
    public void calDists(){
        int i,x,y,len,nX=0,nY=0;
        double X=0,Y=0;
        len=SubpixelContour.size();
        DoublePair dp;
        double[] pdDev=new double[len];
        Point p;
        MeanDist=0;
        for(i=0;i<len;i++){
            dp=SubpixelContour.get(i);
            X+=dp.left;
            Y+=dp.right;   
            
            p=Contour.get(i);
            pdDev[i]=CommonMethods.getDistance(dp.left*nMag,dp.right*nMag, p.x, p.y);
            nX+=p.x;
            nY+=p.y;
        }
        double dist;
        X/=len;
        Y/=len;
        nX/=len;
        nY/=len;
        Dists=new ArrayList();
        DoubleRange dr=CommonStatisticsMethods.getRange(pdDev);
        for(i=0;i<len;i++){
            dp=SubpixelContour.get(i);
            dist=CommonMethods.getDistance(dp.left,dp.right, X, Y)/nMag;
            Dists.add(dist);
            MeanDist+=dist;            
        }
        SmoothedDists=CommonStatisticsMethods.getSoomthedCircularArray(Dists, nWsSmoothing);
        cDistRange=CommonStatisticsMethods.getRange(SmoothedDists);
        MeanDist/=len;
    }
    public void mouseMoved(MouseEvent e){    
        Point pt=new Point();
        ImageCanvas contourCanvas=null,CurvatureCanvas=null,DistCanvas=null;
        if(implContour!=null) contourCanvas=implContour.getCanvas();
        if(pwCurvature!=null) CurvatureCanvas=pwCurvature.getImagePlus().getCanvas();
        if(pwDistance!=null) DistCanvas=pwDistance.getImagePlus().getCanvas();
        if(e.getSource()==contourCanvas){
            Point mouse=CommonGuiMethods.getCursorLocation_ImageCoordinates(implContour);
            mouse.setLocation(mouse.x-DisplayShift.x,mouse.y-DisplayShift.y);
            String st="x="+mouse.x+",y="+mouse.y+",z="+PrintAssist.ToString(getValue(mouse.x,mouse.y),1);
            IJ.showStatus(st);
        }else if(e.getSource()==CurvatureCanvas){
            double x=pwCurvature.getCursorX();
            int index=(int)(x+0.5);
            Point p=Contour.get(index);
            pt.setLocation(p.x+DisplayShift.x,p.y+DisplayShift.y);
            if(contourCanvas!=null) CommonGuiMethods.highlightPoint(implContour, pt, Color.GREEN);
        }else if(e.getSource()==DistCanvas){
            double x=pwDistance.getCursorX();
            int index=(int)(x+0.5);
            if(index>=Contour.size()) index=Contour.size()-1;
            Point p=Contour.get(index);
            pt.setLocation(p.x+DisplayShift.x,p.y+DisplayShift.y);
            if(contourCanvas!=null) CommonGuiMethods.highlightPoint(implContour, pt, Color.GREEN);
        }
    }
    public void mouseDragged(MouseEvent e){
        
    }    
    public IPOGContourParameterNode getIPOGContourParameterNode(){
        IPOGContourParameterNode pNode=new IPOGContourParameterNode();
        if(Contour.isEmpty()) return pNode;
        pNode.type=FunctionType;
        pNode.nMag=nMag;
        pNode.CircularCurvatureAsymmetry=curvatureCircularAsymmetry;
        pNode.CircularDistAsymmetry=distCircularAsymmetry;
        DoublePair dp=fun.getPeak();
        pNode.setPeakAmp(fun.func(dp.left, dp.right));
        pNode.Centroid=new Point(Centroid);
        pNode.PeakPoint=new Point(PeakPoint);//PeakPoint is the highest point inside the contour (computed)
        pNode.LinearityDevL=this.LinearityDevL;
        pNode.LinearityDevS=this.LinearityDevS;
        pNode.cValueRange=new DoubleRange(cValueRange);
        pNode.dvDistancesMaxima=new ArrayList();
        pNode.dvDistancesMinima=new ArrayList();
        pNode.dvCurvaturesMaxima=new ArrayList();
        pNode.dvCurvaturesMinima=new ArrayList();
        int shift=0;
        if(nvDistMaxima.get(0)>nvDistMinima.get(0)) shift=1;
        pNode.PhaseShift=shift;
        CommonStatisticsMethods.copyDoubleArray(Dists,nvDistMaxima,pNode.dvDistancesMaxima);
        CommonStatisticsMethods.copyDoubleArray(Dists,nvDistMinima,pNode.dvDistancesMinima);
        CommonStatisticsMethods.copyDoubleArray(Curvatures,nvCurvatureMaxima,pNode.dvCurvaturesMaxima);
        CommonStatisticsMethods.copyDoubleArray(Curvatures,nvCurvatureMinima,pNode.dvCurvaturesMinima);
        pNode.nCurvatureMaxima=this.numCurvatureMaxima;
        pNode.nConcaveCurves=numConcaveCurves;
        pNode.nDistMaxima=this.nvDistMaxima.size();
        pNode.origin=new DoublePair(origin);
        pNode.MeanDist=MeanDist;
        pNode.Signal=Signal;
        pNode.area=Area;
        
        
        pNode.Height=fun.getHeight();
        pNode.width=m_cIS.getWidth();
        pNode.length=m_cIS.getHeight();
        pNode.dvSmoothedDentDepth=SmoothedDentDepth;
        pNode.dvSmoothedDist=SmoothedDists;
        pNode.CentralPeakShift=new DoublePair(CentralPeakShift);
        
        if(m_cIS!=null) 
            pNode.cMECPolygon=m_cIS.getMECPolygonVertices();
        else
            pNode.cMECPolygon=new ArrayList();        
        pNode.calDistRanges();
        return pNode;
    }
    public boolean ConfinedContour(){
        DoubleRange xFRange=new DoubleRange(), yFRange=new DoubleRange();
        fun.getFrameRanges(xFRange, yFRange);
        Point p;
        DoublePair dp;
        int i,len=Contour.size();
        for(i=0;i<len;i++){
            p=Contour.get(i);
            if(p==null) return false;
            if(!xFRange.contains(origin.left+p.x*deltaX)||!yFRange.contains(origin.right+p.y*deltaY)) return false;
        }
        for(i=0;i<len;i++){
            dp=SubpixelContour.get(i);
            if(dp==null) return false;
            if(!xFRange.contains(dp.left)||!yFRange.contains(dp.right)) return false;
        }
        return true;
    }
    public String getFunctionTypeAsString(){
        String st="Undefined";
        if(FunctionType==IPOGContourParameterNode.GaussianMean) st="Graussian Mean";
        if(FunctionType==IPOGContourParameterNode.GaussianMeanRaw) st="Graussian Mean Raw";
        if(FunctionType==IPOGContourParameterNode.IPOG) st="IPOG";
        return st;
    }
    public int drawPolygon(ImagePlus impl, ArrayList<Point> vertices, Color c){
        ArrayList<Point> points=new ArrayList();
        if(impl==null) return -1;
        int i,len=vertices.size();
        Point pt;
        for(i=0;i<len;i++){
            pt=vertices.get(i);
            points.add(new Point(pt.x+DisplayShift.x,pt.y+DisplayShift.y));
        }
        CommonMethods.drawPolygon(impl, c, points);
        impl.draw();
        return 1;
    }
}
