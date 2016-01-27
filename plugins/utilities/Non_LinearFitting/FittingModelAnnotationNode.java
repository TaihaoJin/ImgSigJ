/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import utilities.CommonMethods;
import java.util.ArrayList;
import java.awt.Point;
import ImageAnalysis.AnnotatedImagePlus;
import ImageAnalysis.ImageAnnotationNode;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.CommonStatisticsMethods;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class FittingModelAnnotationNode extends ImageAnnotationNode{
    AnnotatedImagePlus impl;
    public int modelNumber;
    public int[][] pnVarIndexes;//indixes to locate data point in the original data array m_pdX (and m_pdY)
    public int nSlice;
    public FittingModelAnnotationNode(){
    }
    public FittingModelAnnotationNode(AnnotatedImagePlus impl, ImageShape cIS, double[] pixelSize, double[] DisplayOffset, int modelNumber, int[][] pnVarIndexes, int nSlice){
        this.nSlice=nSlice;
        this.impl=impl;
        this.modelNumber=modelNumber;
        this.pnVarIndexes=pnVarIndexes;
        this.DisplayOffset=DisplayOffset;
        this.pixelSize=pixelSize;
        super.setShape(cIS);
    }
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
    public String getAnnotationAsString(int x, int y){
        String st;
//        double holderSizeX=cIS.getXrange().getRange()+1;
        Point pt0=getDataPosition(x,y,0),pt=getDataPosition(x,y,1);
        double[] pixels=impl.getOriginalValues(nSlice);
        int w=impl.getWidth();
        double Y0=pixels[pt0.y*w+pt0.x],Y=pixels[pt.y*w+pt.x];
        double deltaY=Y-Y0;
        int yPrecision=PrintAssist.getPrintingPrecisionF(deltaY, 0.01);
        double[] coor=new double[2];
        coor[0]=pt0.x*pixelSize[0]+DisplayOffset[0];
        coor[1]=pt0.y*pixelSize[1]+DisplayOffset[1];
        st=PrintAssist.getCoordinatesAsString(coor, 0.01)+" ";
        st+="Y0="+PrintAssist.ToString(Y0, yPrecision)+" "+"Model"+modelNumber+" "+"Y="+PrintAssist.ToString(Y, yPrecision)+" "+"Y-Y0="+PrintAssist.ToString(Y-Y0, yPrecision);
        return st;
    }
}
