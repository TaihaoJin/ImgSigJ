/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;

/**
 *
 * @author Taihao
 */
import ij.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.*;
import ij.process.ByteProcessor;
import ij.io.Opener;
import ij.io.OpenDialog;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import utilities.CustomDataTypes.intRange;
import java.awt.geom.*;
import java.util.ArrayList;
import utilities.Geometry.ImageTransform2D;

public class Transform_Stacks implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */
    public void run(String arg) {
        ImagePlus impl=importImage("import the image stack to be transformed");//TODO: need to implement image stack type checking
        ArrayList<ImageTransform2D> trs=buildTransform();
        impl.show();
        transformStack(impl,trs);
    }

    ArrayList <ImageTransform2D> buildTransform(){
//        Point p10=new Point(33,143),p20=new Point(241,40),p1=new Point(219,237),p2=new Point(451,240);
//        Point2D.Double p10=new Point2D.Double(10,10),p20=new Point2D.Double(30,40),p1=new Point2D.Double(10,10),p2=new Point2D.Double(50,80);
        ArrayList<ImageTransform2D> trs=new ArrayList<ImageTransform2D>();
        Point p10=new Point(139,170),p20=new Point(242,128),p1=new Point(138,287),p2=new Point(190,189);
        int tx=p1.x-p10.x,ty=p1.y-p10.y;
        Point vecP0=new Point(p20.x-p10.x,p20.y-p10.y);
        Point vecP=new Point(p2.x-p1.x,p2.y-p1.y);
        Point vecPr=new Point();
        
        Point ptt=new Point();
        ImageTransform2D tr0=new ImageTransform2D();
        tr0.setToTranslation(-p10.x, -p10.y);
        tr0.transform(p10, ptt);
        trs.add(tr0);

        ImageTransform2D tr=new ImageTransform2D();

        double degree=tr.degree(vecP.x, vecP.y);
        double degree0=tr.degree(vecP0.x, vecP0.y);

        tr.setToRotation(degree-degree0);
        tr.transform(vecP0, vecPr);
        tr.transform(vecP, vecPr);
        Point pt1=new Point(), pt2=new Point();
        trs.add(tr);

        ImageTransform2D tr1=new ImageTransform2D();
        tr1.setToTranslation(p1.x, p1.y);
        tr1.transform(new Point(0,0), ptt);
        trs.add(tr1);

        pt1=new Point(p10.x,p10.y);
        pt2=new Point(p20.x,p20.y);
//        transforms(trs,pt1);
//        transforms(trs,pt2);

        Point[] pts0=new Point[2];
        Point[] pts=new Point[2];
        for(int i=0;i<2;i++){
            pts0[i]=new Point();
            pts[i]=new Point();
        }
        pts0[0].setLocation(p10);
        pts0[1].setLocation(p20);
        transforms(trs,pts0,pts);

        pts[0].setLocation(p10);
        pts[1].setLocation(p20);
        for(int i=0;i<2;i++){
            tr.transform(pts0[i], pts[i]);
        }


        return trs;
    }

    ArrayList <AffineTransform> buildTransform0(){
        AffineTransform tr=new AffineTransform();
//        Point p10=new Point(33,143),p20=new Point(241,40),p1=new Point(219,237),p2=new Point(451,240);
        Point2D.Double p10=new Point2D.Double(0,45),p20=new Point2D.Double(2047,45),p1=new Point2D.Double(609,300),p2=new Point2D.Double(2638,33);
        double tx=p1.getX()-p10.getX(),ty=p1.getY()-p10.getY();
        Point2D.Double vecP0=new Point2D.Double(p20.getX()-p10.getX(),p20.getY()-p10.getY());
        Point2D.Double vecP=new Point2D.Double(p2.getX()-p1.getX(),p2.getY()-p1.getY());
        Point2D.Double vecPr=new Point2D.Double();
        tr.setToRotation(vecP0.getX(), -vecP0.getY());
        tr.transform(vecP0, vecPr);
        tr.transform(vecP, vecPr);
        Point2D.Double pt1=new Point2D.Double(), pt2=new Point2D.Double();

        ArrayList<AffineTransform> trs=new ArrayList<AffineTransform>();
        AffineTransform tr1=new AffineTransform();
        tr1.setToTranslation(-p10.getX(), -p10.getY());
        tr.setToTranslation(-p10.getX(), -p10.getY());
        trs.add(tr1);

        AffineTransform tr2=new AffineTransform();
        tr2.setToRotation(vecPr.x, vecPr.y);
        tr.rotate(vecPr.x, vecPr.y);
        trs.add(tr2);

        AffineTransform tr3=new AffineTransform();
        tr3.setToTranslation(p1.getX(), p1.getY());
        tr.translate(p1.getX(), p1.getY());
        trs.add(tr3);

        pt1=new Point2D.Double(p10.x,p10.y);
        pt2=new Point2D.Double(p20.x,p20.y);
        transforms(trs,pt1);
        transforms(trs,pt2);

        Point[] pts0=new Point[2];
        Point[] pts=new Point[2];

        for(int i=0;i<2;i++){
            pts0[i]=new Point();
            pts[i]=new Point();
        }

        pts0[0].setLocation(p10);
        pts0[1].setLocation(p20);
        transforms(trs,pts0,pts);

        return trs;
    }

    void transforms(ArrayList<AffineTransform> trs, Point2D.Double pt){
        AffineTransform tr;
        int size=trs.size();
        for(int i=0;i<size;i++){
            tr=trs.get(i);
            tr.transform(pt, pt);
        }
    }

    void transforms(ArrayList<ImageTransform2D> trs, Point pt0, Point2D.Double pt){
        ImageTransform2D tr;
        int size=trs.size();
        pt.setLocation(pt0.x, pt0.y);
        for(int i=0;i<size;i++){
            tr=trs.get(i);
            tr.transform(pt, pt);
        }
    }

    void rTransforms(ArrayList<ImageTransform2D> trs, Point pt0, Point2D.Double pt){
        ImageTransform2D tr;
        int size=trs.size();
        pt.setLocation(pt0.x, pt0.y);
        for(int i=0;i<size;i++){
            tr=trs.get(size-1-i);
            tr.rTransform(pt, pt);
        }
    }

    void transforms(ArrayList<AffineTransform> trs, Point2D[] pts0, Point2D[] pts){
        int size=trs.size();
        int len=pts.length;
        Point2D.Double pt=new Point2D.Double();
        for(int i=0;i<len;i++){
            pt.setLocation(pts0[i]);
            transforms(trs,pt);
            pts[i].setLocation(pt);
        }
    }

    void transforms(ArrayList<ImageTransform2D> trs, Point[] pts0, Point[] pts){
        ImageTransform2D tr;
        int size=trs.size();
        int len=pts.length;
        int i,j;
        Point2D.Double pt0=new Point2D.Double(), pt=new Point2D.Double();
        for(i=0;i<len;i++){
            pt0.setLocation(pts0[i]);
            for(j=0;j<size;j++){
                tr=trs.get(j);
                tr.transform(pt0, pt);
                pt0.setLocation(pt);
            }
            pts[i].setLocation((int)(pt.x+0.5),(int)(pt.y+0.5));
        }
    }

    void transforms0(ArrayList<AffineTransform> trs, Point[] pts0, Point[] pts){
        int size=trs.size();
        int len=pts.length;
        Point2D.Double pt=new Point2D.Double();
        for(int i=0;i<len;i++){
            pt.setLocation(pts0[i]);
            transforms(trs,pt);
            pts[i].setLocation(pt);
            if(Math.abs(pts[i].x-pt.x)>0.001||Math.abs(pts[i].y-pt.y)>0.001){
                i=i;
            }
        }
    }

    boolean outOfRange(int min, int max, int x){
        return(x<min||x>max);
    }
    protected void transformStack(ImagePlus impl, ArrayList<ImageTransform2D> trs) {
        int w=impl.getWidth();
        int h=impl.getHeight();
        Point lt0=new Point(0,0), rt0=new Point(w-1,0), lb0=new Point(0,h-1), rb0=new Point(w-1,h-1);
        Point lt=new Point(), rt=new Point(), lb=new Point(), rb=new Point();
        Point[] corners0={lt0,rt0,rb0,lb0};
        Point[] corners={lt,rt,rb,lb};
        transforms(trs,corners0, corners);
        int i,j;

        intRange xRange=new intRange(0,0);
        intRange yRange=new intRange(0,0);
        intRange xRange0=new intRange(0,w-1);
        intRange yRange0=new intRange(0,h-1);

        for(i=0;i<4;i++){
            xRange.expandRange(corners[i].x);
        }
        for(i=0;i<4;i++){
            yRange.expandRange(corners[i].y);
        }
        int W=xRange.getMax()-xRange.getMin()+3;//TODO: need to figure out this small discrepancy later
        int H=yRange.getMax()-yRange.getMin()+4;

        int dx=xRange.getMin(),dy=yRange.getMin(),tx=0,ty=0;

        if(dx<0) {
            tx=-dx+2;
        }
        if(dy<0) {
            ty=-dy+1;
        }
        if(dx<0||dy<0){
            ImageTransform2D tr=new ImageTransform2D();
            tr.setToTranslation(tx, ty);
            trs.add(tr);
            xRange.setRange(xRange.getMin()+tx, xRange.getMax()+tx);
            yRange.setRange(yRange.getMin()+ty, yRange.getMax()+ty);
        }

        int len0=w*h, len=W*H,o;

        /*
        Point[] points0=new Point[len0], points=new Point[len0];
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                points0[o+j]=new Point(j,i);
                points[o+j]=new Point(j,i);
            }
        }

        transforms(trs,points0, points);

        Point[] points=new Point[len];
        for(i=0;i<len;i++){
            points[i]=null;
        }
        */

        int x,y;
        Point[] points=new Point[len];
        for(i=0;i<len;i++){
            points[i]=null;
        }
        
        Point2D.Double ptd=new Point2D.Double();
        Point pt=new Point();
        for(i=0;i<H;i++){
            o=i*W;
            for(j=0;j<W;j++){
                pt.setLocation(j, i);
                rTransforms(trs,pt,ptd);
                x=(int)(ptd.x+0.5);
                y=(int)(ptd.y+0.5);
                if(xRange0.contains(x)&&yRange0.contains(y)){
                    points[o+j]=new Point(x,y);
                }
            }
        }

        ImageStack is=new ImageStack(W,H);

        byte[] pixels,pixels0;

        int stackSize=impl.getStackSize();

        int index=0,x0,y0;
        Point p;
        for(index=1;index<=stackSize;index++) {
            ByteProcessor bp=new ByteProcessor(W,H);
            impl.setSlice(index);
            pixels=(byte[])bp.getPixels();
            pixels0=(byte[])impl.getProcessor().getPixels();
            for(i=0;i<len;i++){
                p=points[i];
                if(p!=null){
                    pixels[i]=pixels0[p.y*w+p.x];
                }
            }
            is.addSlice("", bp);
        }
        ImagePlus implm=new ImagePlus("transformed stack",is);
        implm.show();
    }
    protected void transformStack0(ImagePlus impl, ArrayList<AffineTransform> trs) {
        int w=impl.getWidth();
        int h=impl.getHeight();
        Point lt0=new Point(0,0), rt0=new Point(w-1,0), lb0=new Point(0,h-1), rb0=new Point(w-1,h-1);
        Point lt=new Point(), rt=new Point(), lb=new Point(), rb=new Point();
        Point[] corners0={lt0,rt0,rb0,lb0};
        Point[] corners={lt,rt,rb,lb};
        transforms(trs,corners0, corners);
        int i,j;
        intRange xRange=new intRange(0,0);
        intRange yRange=new intRange(0,0);
        for(i=0;i<4;i++){
            xRange.expandRange(corners[i].x);
        }
        for(i=0;i<4;i++){
            yRange.expandRange(corners[i].y);
        }
        int W=xRange.getMax()-xRange.getMin()+1;
        int H=yRange.getMax()-yRange.getMin()+1;

        int dx=xRange.getMin(),dy=yRange.getMin(),tx=0,ty=0;

        if(dx<0) {
            tx=-dx;
        }
        if(dy<0) {
            ty=-dy;
        }
        if(dx<0||dy<0){
            AffineTransform tr=new AffineTransform();
            tr.setToTranslation(tx, ty);
            trs.add(tr);
            xRange.setRange(xRange.getMin()+tx, xRange.getMax()+tx);
            yRange.setRange(yRange.getMin()+ty, yRange.getMax()+ty);
        }

        int len0=w*h, len=W*H,o;

        Point[] points0=new Point[len0], points=new Point[len0];
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                points0[o+j]=new Point(j,i);
                points[o+j]=new Point(j,i);
            }
        }

        transforms(trs,points0, points);


        ImageStack is=new ImageStack(W,H);

        byte[] pixels,pixels0;

        int stackSize=impl.getStackSize();

        int index=0,x,y,x0,y0;
        int pixel0,pixel;
        Point p0,p;
        Point2D.Double pt;
        for(index=1;index<=stackSize;index++) {
            ByteProcessor bp=new ByteProcessor(W,H);
            impl.setSlice(index);
            pixels=(byte[])bp.getPixels();
            pixels0=(byte[])impl.getProcessor().getPixels();
            for(i=0;i<len0;i++){
                p=points[i];
                p0=points0[i];
                if(outOfRange(0,W-1,p.x)||outOfRange(0,H-1,p.y)){
                    continue;
                }
                if(outOfRange(0,w-1,p0.x)||outOfRange(0,h-1,p0.y)){
                    continue;
                }
                pixels[p.y*W+p.x]=pixels0[p0.y*w+p0.x];
            }
            is.addSlice("", bp);
        }
        ImagePlus implm=new ImagePlus("transformed stack",is);
        implm.show();
    }

    public static ImagePlus newGary8Image(String title,int w, int h){
        byte pixels[]=new byte[w*h];
        for(int i=0;i<w*h;i++){
            pixels[i]=0;
        }
        ByteProcessor cp=new ByteProcessor(w,h);
        ImagePlus impl=new ImagePlus(title, cp);
        return impl;
    }
    public static ImagePlus importImage(String title){
        OpenDialog od=new OpenDialog(title,"");
        String dir=od.getDirectory();
        String name=od.getFileName();
        Opener op=new Opener();
        ImagePlus impl=op.openImage(dir+name);
        return impl;
    }
}
