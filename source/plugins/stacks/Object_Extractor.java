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

public class Object_Extractor implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */
    public void run(String arg) {
//        ImagePlus impl=importImage("import the image stack for object extraction");//TODO: need to implement image stack type checking
//        impl.show();
//        IdentifyObjects(impl);
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
//        transforms(trs,corners0, corners);
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
//                rTransforms(trs,pt,ptd);
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
}
