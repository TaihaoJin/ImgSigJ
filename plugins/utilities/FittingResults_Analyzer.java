/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import utilities.CommonMethods;
import ij.WindowManager;
import utilities.io.FileAssist;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import utilities.io.MessageAssist;
import java.io.IOException;
import utilities.Non_LinearFitting.FittingResultsNode;
import utilities.statistics.Histogram;
import utilities.statistics.Histogram;
import utilities.CustomDataTypes.IntPair;
import utilities.Non_LinearFitting.StandardFittingFunction;
import utilities.CustomDataTypes.DoubleRange;
import utilities.io.PrintAssist;
import utilities.Non_LinearFitting.Fitting_Function;
import ij.IJ;
import java.awt.Point;
import ImageAnalysis.AnnotatedImagePlus;
import ImageAnalysis.ImageAnnotationNode;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.CustomDataTypes.intRange;
import utilities.Non_LinearFitting.FittingResultAnalyzerForm;
import utilities.Non_LinearFitting.ImageFittingGUI;
import utilities.Non_LinearFitting.FittingModelAnnotationNode;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import utilities.Non_LinearFitting.FittingModelComponentNode;
import utilities.Non_LinearFitting.FittingResultComponentNode;
/**
 *
 * @author Taihao
 */
/*
class FittingModelAnnotationNode extends ImageAnnotationNode{
    public int modelNumber;
    public int[][] pnVarIndexes;
    public boolean annotated(int x, int y){
        Point pt=new Point(x,y);
        int holderSizeX=cIS.getXrange().getRange()+1;
        for(int i=0;i<3;i++){
            if(cIS.contains(pt)) return true;
            pt.translate(-holderSizeX, 0);
        }
        return false;
    }
    public void initVarIndexes(){
        int w=cIS.getXrange().getRange(),h=cIS.getYrange().getRange();
        pnVarIndexes=new int[h][w];
        CommonStatisticsMethods.setElements(pnVarIndexes, -1);
    }
    public int getVarIndex(int x, int y){
        Point pt=cIS.getInnerCoordinates(new Point(x,y));
        pt.x=pt.x%(w+1);
        return pnVarIndexes[pt.y][pt.x];
    }
    public void storeVarIndex(int x, int y, int index){
        Point pt=cIS.getInnerCoordinates(new Point(x,y));
        pnVarIndexes[pt.y][pt.x]=index;
    }
    public int[][] getVarIndexes(){
        return pnVarIndexes;
    }
    public void setVarIndexes(int[][] varIndexes){
        pnVarIndexes=varIndexes;
    }
    public Point getDataPosition(int x, int y, int index){//index 0 for the data, 1 for fitting value, 2 for the difference
        int dx=x-displayLocation.x;
        int lx=w+1;
        dx=dx%lx+index*lx;
        return new Point(displayLocation.x+dx,y);
    }
    public int getModelNumber(){
        return modelNumber;
    }
    public ArrayList<Point> getCorrespondingDataPositions(int x, int y){
        ArrayList<Point> points=new ArrayList();
        Point p;
        for(int i=0;i<3;i++){
            p=getDataPosition(x,y,i);
            if(p.x!=x) points.add(p);
        }
        return points;
    }
}
*/
public class FittingResults_Analyzer implements PlugIn{

    static int maxImageLength=2000;
    ArrayList<FittingResultsNode> m_cFittingResultNodes;
    ArrayList<double[]> m_pdvPatchPixelSizes;
    ArrayList<int[]> m_pnvImagePatchHolderSizes;
    int[] m_pnDrawingVarIndexes;
    int[] m_pnCanvusSize;
    int m_nCanvusSizeUnit,m_nPatchesPerRow;
    AnnotatedImagePlus  implDrawing;
    ArrayList<Fitting_Function[]> m_cvFuncs;
    ArrayList<ImageShape> m_cvFittedVarShapes;
    String sFileName;

    public void run(String args){
        showFittingResults();
        FittingResultAnalyzerForm aForm=new FittingResultAnalyzerForm(this);
        aForm.setTitle(sFileName);
        aForm.setVisible(true);
    }
    void showFittingResults(){
        m_pnDrawingVarIndexes=new int[2];
        m_pnDrawingVarIndexes[0]=0;
        m_pnDrawingVarIndexes[1]=1;
        m_nCanvusSizeUnit=100;
        m_nPatchesPerRow=3;
        m_pdvPatchPixelSizes=new ArrayList();
        m_cvFuncs=new ArrayList();
        String dir=FileAssist.defaultDirectory;
        String sFittingResultFile=FileAssist.getFilePath("importing the fitting result file", dir, "txt file", "txt", true);
        sFileName=FileAssist.getFileName(sFittingResultFile);
        m_cFittingResultNodes=new ArrayList();
        readFittingResults(sFittingResultFile);
        m_pnCanvusSize=getCanvusSize();
        drawFittingResults();
        implDrawing.show();
    }
    void drawFittingResults(){
        int w=m_pnCanvusSize[0],h=m_pnCanvusSize[1],len=w*h;
        int nSlices=m_cFittingResultNodes.size(),slice;
//        if(nSlices<2) nSlices=2;
        float[] pixels;
        ImagePlus implt=CommonMethods.getBlankImageStack(ImagePlus.GRAY32, w, h, Math.max(nSlices, 2));
        double[][] ppdOriginalPixels=new double[nSlices][len];
        double pdOriginalPixels[];
        ArrayList<ImageAnnotationNode> cvAnnotations=new ArrayList();
        ArrayList<ImageAnnotationNode>[] pcvFittingModelAnnotations=new ArrayList[nSlices];
        pcvFittingModelAnnotations=new ArrayList[nSlices];

        int[] holderSize;
        int pnVarMarks[][];
        int nModels;
        FittingResultsNode aNode;
        Fitting_Function[] funcs;
        m_cvFittedVarShapes=new ArrayList();

        for(slice=1;slice<=nSlices;slice++){
            cvAnnotations=new ArrayList();
            aNode=m_cFittingResultNodes.get(slice-1);
            nModels=aNode.nModels;
            funcs=new Fitting_Function[nModels];
            implt.setSlice(slice);
            pixels=(float[])implt.getProcessor().getPixels();
            holderSize=m_pnvImagePatchHolderSizes.get(slice-1);
            pdOriginalPixels=ppdOriginalPixels[slice-1];
            setPixels(pixels,aNode,m_pdvPatchPixelSizes.get(slice-1),m_pnvImagePatchHolderSizes.get(slice-1),pdOriginalPixels,funcs,cvAnnotations);
            m_cvFuncs.add(funcs);
            pcvFittingModelAnnotations[slice-1]=cvAnnotations;
        }
        implDrawing=new AnnotatedImagePlus(implt);
        implDrawing.setTitle(sFileName);
        implDrawing.setAnnotations(pcvFittingModelAnnotations);
        implDrawing.setOriginalValues(ppdOriginalPixels);
        implDrawing.setDescription("FittingComponents");
        implDrawing.storeNote(m_cFittingResultNodes, "FittingResultNodes");
    }
    void setPixels(float[] pixels, FittingResultsNode aNode, double[] pixelSizes, int holderSize[],double[] pdOriginalPixels,Fitting_Function[] funcs, ArrayList<ImageAnnotationNode> cvAnnotations){
        int len=pixels.length;
        ComposedFittingFunction func;
        double pdPars[];
        ArrayList<String> svFuncTypes;;
        double pixelSize,pdX[][],pdY[],dXn,dX,dY,dY0;
        int i,j,row,col,shiftX,shiftY,x,y,positions[],position,varIndex,nModels=aNode.nModels;
        int w=m_pnCanvusSize[0],h=m_pnCanvusSize[1];
        int nI,nF,nDelta;
        positions=new int[2];
        pdX=aNode.m_pdX;
        pdY=aNode.m_pdY;
        DoubleRange cSignalRange=new DoubleRange();
        DoubleRange[] cSignalRangesModel=new DoubleRange[nModels];
        DoubleRange[] cSignalRangesDelta=new DoubleRange[nModels];
        double signalMidpoint;

        if(funcs==null) funcs=new Fitting_Function[nModels];
        if(funcs.length!=nModels) funcs=new Fitting_Function[nModels];

        double[] pdX0=new double[2];
        for(j=0;j<2;j++){
            varIndex=m_pnDrawingVarIndexes[j];
            dXn=aNode.cvVarRanges.get(varIndex).getMin();
            pdX0[j]=dXn;
        }
        Point corner=new Point(positions[0],positions[1]);

        int nVars=pdX[0].length,nPars;
        FittingModelAnnotationNode FittingAnnotation;
//        ImageAnnotationNode annotation;
        FittingModelNode aModel;
        for(i=0;i<nModels;i++){
            aModel=aNode.m_cvModels.get(i);
            pdPars=aModel.pdPars;
            nPars=pdPars.length;
            svFuncTypes=aModel.svFunctionTypes;
            func=new ComposedFittingFunction(svFuncTypes);
            funcs[i]=func;
            cSignalRangesModel[i]=new DoubleRange();
            cSignalRangesDelta[i]=new DoubleRange();
        }

        aModel=aNode.m_cvModels.get(0);//here we deal with on the case that all models fit the same data sets.
//        nI=aModel.nI;
//        nF=aModel.nF;
        nI=0;
        nF=pdX.length;//11804
        nDelta=aModel.nDelta;
        ArrayList<Point> points=new ArrayList();
        int holderSizeX=holderSize[0];
        double dn=Double.MAX_VALUE,delta;
        for(i=nI;i<nF;i++){
            dY0=pdY[i];
            if(dY0<dn) dn=dY0;
            cSignalRange.expandRange(dY0);
            for(j=0;j<2;j++){
                varIndex=m_pnDrawingVarIndexes[j];
                dXn=aNode.cvVarRanges.get(varIndex).getMin();
                dX=pdX[i][varIndex];

                pixelSize=pixelSizes[j];
                dX-=dXn;
                x=(int)(dX/pixelSize+0.5);
                positions[j]=x;
            }
            points.add(new Point(positions[0],positions[1]));
            for(j=0;j<nModels;j++){
                aModel=aNode.m_cvModels.get(j);
                dY=funcs[j].fun(aModel.pdPars, pdX[i]);
                row=j/m_nPatchesPerRow;
                col=j%m_nPatchesPerRow;
                shiftX=3*col*holderSize[0];
                shiftY=row*holderSize[1];
                x=positions[0]+shiftX;
                y=positions[1]+shiftY;
                position=w*y+x;
                if(position>=pixels.length){
                    position=position;
                }
                if(position>=pixels.length){
                    position=position;
                }
                pixels[position]=(float)dY0;
                pdOriginalPixels[position]=dY0;

                x+=holderSizeX;
                position=w*y+x;
                pixels[position]=(float)dY;
                pdOriginalPixels[position]=dY;
                x+=holderSizeX;
                position=w*y+x;
                delta=(dY0-dY);
                pixels[position]=(float)(delta);
                pdOriginalPixels[position]=delta;
                if(dY<dn) dn=dY;
                cSignalRangesModel[j].expandRange(dY);
                cSignalRangesDelta[j].expandRange(delta);
            }
        }
        signalMidpoint=cSignalRange.getMidpoint();
        ImageShape cIS=ImageShapeHandler.buildImageShape_Scattered(points);
        m_cvFittedVarShapes.add(cIS);;

        double signalMidpointModel[]=new double[nModels];
        double signalMidpointDelta[]=new double[nModels];
        int[][] varIndexes=null;
        Point pt;

        for(i=0;i<nModels;i++){
            row=i/m_nPatchesPerRow;
            col=i%m_nPatchesPerRow;
            shiftX=3*col*holderSize[0];
            shiftY=row*holderSize[1];
            FittingAnnotation=new FittingModelAnnotationNode();
            FittingAnnotation.type="FittingModelAnnotationNode";
            FittingAnnotation.displayLocation=new Point(shiftX,shiftY);
            FittingAnnotation.setShape(new ImageShape(cIS));
            FittingAnnotation.cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
            FittingAnnotation.updateLocation();
            FittingAnnotation.modelNumber=i;
            if(i==0){
                FittingAnnotation.initVarIndexes();
                len=points.size();
                for(j=0;j<len;j++){
                    pt=points.get(j);
                    FittingAnnotation.storeVarIndex(pt.x,pt.y,j);
                }
                varIndexes=FittingAnnotation.getVarIndexes();
            }else{
                FittingAnnotation.setVarIndexes(varIndexes);
            }
            cvAnnotations.add(FittingAnnotation);
            signalMidpointModel[i]=cSignalRangesModel[i].getMidpoint();
            signalMidpointDelta[i]=cSignalRangesDelta[i].getMidpoint();
        }

        int nDigits=PrintAssist.getDigits(cSignalRange.getExpand());
        double signalMidpoint1=PrintAssist.trimDigits(signalMidpoint,nDigits-1);

        int o,ix=0;
        len=pixels.length;
        for(i=0;i<len;i++){
            pixels[i]=(float)signalMidpoint1;
        }
        Point point;
        double adjust;
        int holdersizeX=holderSize[0];
        for(i=0;i<nModels;i++){
            cIS=cvAnnotations.get(i).cIS;
            points.clear();
            cIS.getInnerPoints(points);
            len=points.size();
            for(j=0;j<len;j++){
                point=points.get(j);
                x=point.x;
                y=point.y;
                position=y*w+x;
                adjust=signalMidpoint-signalMidpoint1;
                pixels[position]=(float)(pdOriginalPixels[position]-adjust);
                x+=holdersizeX;
                position=y*w+x;
                adjust=signalMidpointModel[i]-signalMidpoint1;
                pixels[position]=(float)(pdOriginalPixels[position]-adjust);
                x+=holdersizeX;
                position=y*w+x;
                adjust=signalMidpointDelta[i]-signalMidpoint1;
                pixels[position]=(float)(pdOriginalPixels[position]-adjust);
            }
        }
    }
    int getModel(int x, int y, int z){
        int[] holderSize=m_pnvImagePatchHolderSizes.get(z);
        int row=y/(3*holderSize[1]),col=x/(3*holderSize[0]);
        return row*m_nPatchesPerRow+col;
    }
    public void setDrawingVarIndexes(int index0, int index1){
        m_pnDrawingVarIndexes[0]=index0;
        m_pnDrawingVarIndexes[1]=index1;
    }
    void readFittingResults(String path){
        File file = new File(path);
        FileReader f=null;
        try{
            f=new FileReader(file);
        }
        catch (FileNotFoundException e){
            MessageAssist.error(e+" in ImportFittingResults_MultipleLines(String path)");
        }
        BufferedReader br=new BufferedReader(f);

        m_cFittingResultNodes=new ArrayList();
        FittingResultsNode aNode=new FittingResultsNode();
        int status=-1;
        
        try{
            status=aNode.ImportFittingResults_MultipleLines(br);
        }
        catch (IOException e){
            MessageAssist.error("IOException when import FittingResults");
        }

        while(status==1){
            m_cFittingResultNodes.add(aNode);
            aNode=new FittingResultsNode();
            try{
                status=aNode.ImportFittingResults_MultipleLines(br);
            }
            catch (IOException e){
                MessageAssist.error("IOException when import FittingResults");
            }
        }
    }
    public void setMaxImageLength(int len){
        maxImageLength=len;
    }
    int[] getCanvusSize(){
        int[] pnCanvusSize=new int[2];
        m_pnvImagePatchHolderSizes=getImagePatchHolderSizes();
        int len=m_pnvImagePatchHolderSizes.size();
        int h=0,w=0,ht,wt,holderSize[],i,nModels,rows;
        FittingResultsNode aNode;
        for(i=0;i<len;i++){
            holderSize=m_pnvImagePatchHolderSizes.get(i);
            aNode=m_cFittingResultNodes.get(i);
            nModels=aNode.nModels;
            rows=(nModels+1)/m_nPatchesPerRow;//one slot for the origional data
            if((nModels+1)%m_nPatchesPerRow>0)rows++;

            wt=3*m_nPatchesPerRow*holderSize[0];
            if(wt>w) w=wt;

            ht=rows*holderSize[1];
            if(ht>h) h=ht;
        }
        pnCanvusSize[0]=w;
        pnCanvusSize[1]=h;
        return pnCanvusSize;
    }
    ArrayList<int[]> getImagePatchHolderSizes(){
        ArrayList<int[]> holderSizes=new ArrayList();
        double[] pixelSize;
        FittingResultsNode aNode;
        int len=m_cFittingResultNodes.size();
        m_pdvPatchPixelSizes.clear();
        for(int i=0;i<len;i++){
            aNode=m_cFittingResultNodes.get(i);
            pixelSize=getPatchPixelSize(aNode);
            m_pdvPatchPixelSizes.add(pixelSize);
            holderSizes.add(getImagePatchHolderSize(aNode,pixelSize));
        }
        return holderSizes;
    }
    int[] getImagePatchHolderSize(FittingResultsNode aNode, double[] pdPixelSize){
        int[] pnHolderSize=new int[2];
        double dPatchSize,dPixelSize;
        int i,varIndex,numPixels,nHolderSize;
        for(i=0;i<2;i++){
            varIndex=m_pnDrawingVarIndexes[i];
            dPatchSize=aNode.cvVarRanges.get(varIndex).getExpand();
            dPixelSize=pdPixelSize[i];
            numPixels=(int)(dPatchSize/dPixelSize);
            if(dPixelSize*numPixels<dPatchSize) numPixels++;
            numPixels++;//because an image "starts&&ends" with pixels. 6/10/2011
            nHolderSize=numPixels+1;
            pnHolderSize[i]=nHolderSize;
        }
        return pnHolderSize;
    }
    double[] getPatchPixelSize(FittingResultsNode aNode){
        double[] pixelSize=new double[2];
        double[][] pdX=aNode.m_pdX;
        FittingModelNode aModel=aNode.m_cvModels.get(0);
        int nI=aModel.nI,nF=aModel.nF, nDelta=aModel.nDelta;
        int nDataPoints=(nF-nI)/nDelta+1;
        if(nDataPoints<0){
            nDataPoints=nDataPoints;
        }
        int i,varIndex;
        double delta;
        for(i=0;i<2;i++){
            varIndex=m_pnDrawingVarIndexes[i];
            double[] pdT=new double[nDataPoints];
            CommonStatisticsMethods.copyArray_Column(pdX,varIndex,nI,nF,nDelta,pdT,0,1);
            delta=Histogram.getOptimalBinSize(pdT, maxImageLength);
            pixelSize[i]=delta;
        }
        return pixelSize;
    }
    public AnnotatedImagePlus getResultImage(){
        return implDrawing;
    }
    public String getFittingResultsAsString(AnnotatedImagePlus antdImpl, Point pt, int slice){
        int w=antdImpl.getWidth();
        int x=pt.x,y=pt.y,z=slice-1,i,varIndex;
        Point p;
        String results;
        FittingResultsNode aNode=m_cFittingResultNodes.get(z);
        int yPrecision;
        ArrayList<ImageAnnotationNode> annotations=antdImpl.getAnnotations(x,y,slice,"FittingModelAnnotationNode");
        double[] pdOritinalPixels=antdImpl.getOriginalValues(slice);
        FittingModelAnnotationNode fittingAnnotation;
        ImageAnnotationNode annotation;
        if(!annotations.isEmpty()){
            annotation=annotations.get(0);
            fittingAnnotation=(FittingModelAnnotationNode)annotation;
            varIndex=fittingAnnotation.getVarIndex(x, y);
            double Y0=aNode.m_pdY[varIndex];
            p=fittingAnnotation.getDataPosition(x, y, 1);
            double Y=pdOritinalPixels[y*w+p.x];
            double deltaY=Y-Y0;
            yPrecision=PrintAssist.getPrintingPrecisionF(deltaY, 0.01);
            results=PrintAssist.getCoordinatesAsString(aNode.m_pdX[varIndex], 0.01)+" ";
            results+="Y0="+PrintAssist.ToString(Y0, yPrecision)+" "+"Model"+fittingAnnotation.getModelNumber()+" "+"Y="+PrintAssist.ToString(Y, yPrecision)+" "+"Y-Y0="+PrintAssist.ToString(Y-Y0, yPrecision);
        }else{
            results="Background";
        }
        return results;
    }
    public static ArrayList<Point> getCorespondingDataPositions(AnnotatedImagePlus antdImpl, Point pt, int slice){
        ArrayList<Point> points=new ArrayList();
        int x=pt.x,y=pt.y,z=slice-1,i;
        Point p;
        ArrayList<ImageAnnotationNode> annotations=antdImpl.getAnnotations(x,y,slice,"FittingModelAnnotationNode");
        FittingModelAnnotationNode fittingAnnotation;
        ImageAnnotationNode annotation;
        if(!annotations.isEmpty()){
            annotation=annotations.get(0);//there is only one annotation
            fittingAnnotation=(FittingModelAnnotationNode)annotation;
            points=fittingAnnotation.getCorrespondingDataPositions(x, y);
        }
        return points;
    }
    public int adjustFitting(AnnotatedImagePlus antdImpl, Point pt, int slice){
        ArrayList<Point> points=new ArrayList();
        int x=pt.x,y=pt.y,z=slice-1,i;
        ArrayList<ImageAnnotationNode> annotations=antdImpl.getAnnotations(x,y,slice,"FittingModelAnnotationNode");
        FittingModelAnnotationNode fittingAnnotation;
        int nModel=-1;
        ImageAnnotationNode annotation;
        if(!annotations.isEmpty()){
            annotation=annotations.get(0);//there is only one annotation
            fittingAnnotation=(FittingModelAnnotationNode)annotation;
            nModel=fittingAnnotation.modelNumber;
        }
        if(nModel<0) return -1;
        if(!antdImpl.hasNote("FittingResultNodes")) return -1;

        ArrayList<FittingResultsNode> cvFittingResultNodes=(ArrayList<FittingResultsNode>)antdImpl.retrieveNote("FittingResultNodes");
        ImageFittingGUI.main(cvFittingResultNodes.get(slice-1),nModel);
        return 1;
    }
    public AnnotatedImagePlus showFittingComponents(Point pt, int slice){
        int w=implDrawing.getWidth(),h=implDrawing.getHeight(),len=w*h;
        int x=pt.x,y=pt.y,z=slice-1,i,varIndex;
        ArrayList<ImageAnnotationNode> cvAnnotationst=implDrawing.getAnnotations(x, y, slice, "FittingModelAnnotationNode");
        if(cvAnnotationst.isEmpty()) return null;
        FittingModelAnnotationNode aFANode=(FittingModelAnnotationNode)(cvAnnotationst.get(0));
        int nModelNumber=aFANode.modelNumber;
        ImageShape cIS=aFANode.getImageShape();
        int holderSizeX=cIS.getXrange().getRange()+1;
        int holderSizeY=cIS.getYrange().getRange()+1;
        int[] holderSize={holderSizeX,holderSizeY};

        FittingResultsNode aNode=m_cFittingResultNodes.get(z);
        AnnotatedImagePlus impl=null;
        FittingModelNode aModel=aNode.m_cvModels.get(nModelNumber);

        double[] pdPars=aModel.pdPars;
        int nPars=pdPars.length;
        int nVars=aNode.m_pdX[0].length;

        FittingResultComponentNode aCompnentNode=new FittingResultComponentNode(aNode, nModelNumber);
        int nModels=aCompnentNode.nModels;
        w=m_nPatchesPerRow*(3*holderSizeX)-1;
        int rows=nModels/m_nPatchesPerRow;
        if(nModels%holderSizeY!=0) rows++;
        h=rows*holderSizeY;
        len=w*h;
        
        m_pnCanvusSize[0]=w;
        m_pnCanvusSize[1]=h;

        ImagePlus implt=CommonMethods.getBlankImageStack(ImagePlus.GRAY32, w, h, 1);
        int nSlices=implt.getNSlices();
        double[][] ppdOriginalPixels=new double[nSlices][len];
        float[] pixels=(float[])implt.getProcessor().getPixels();
        ArrayList<ImageAnnotationNode> cvAnnotations=new ArrayList();
        ArrayList<ImageAnnotationNode>[] pcvFittingModelAnnotations=new ArrayList[nSlices];
        pcvFittingModelAnnotations=new ArrayList[nSlices];
        
        setPixels(pixels,aCompnentNode,m_pdvPatchPixelSizes.get(z),holderSize,ppdOriginalPixels[0],null,cvAnnotations);
        implt.setTitle("Components of "+PrintAssist.ToString_Order(slice)+"Fitting, Model"+nModelNumber);
        pcvFittingModelAnnotations[0]=cvAnnotations;

        impl=new AnnotatedImagePlus(implt);
        impl.setAnnotations(pcvFittingModelAnnotations);
        impl.setOriginalValues(ppdOriginalPixels);
        impl.setDescription("FittingComponents");
        return impl;
    }
    public ArrayList<FittingResultsNode> getFittingResultNodes(){
        return m_cFittingResultNodes;
    }
    public void setToSliceContainPoint(int x, int y){
        int len=m_cFittingResultNodes.size(),i;
        boolean found=false;
        for(i=0;i<len;i++){
            if(thisSliceContainsPoint(i+1,x,y)){
                implDrawing.setSlice(i+1);
                found=true;
                IJ.showStatus("The point ("+x+","+y+") is in slide "+i);
                break;
            }
        }
        if(!found) IJ.showStatus("The point ("+x+","+y+") is not found");
    }
    public boolean thisSliceContainsPoint(int slice, int x, int y){//this method applies only to image fitting results
        FittingResultsNode aNode=m_cFittingResultNodes.get(slice-1);
        int x0=(int)(aNode.cvVarRanges.get(0).getMin()+0.5),y0=(int)(aNode.cvVarRanges.get(1).getMin()+0.5);
        ImageShape cIS=m_cvFittedVarShapes.get(slice-1);
        return(cIS.contains(x-x0,y-y0));
    }
    
}
