/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package stacks;
import java.io.File;
import ij.io.OpenDialog;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.io.Opener;
import utilities.CommonMethods;
import java.util.ArrayList;
import ij.ImageStack;
import java.awt.Color;
import ImageAnalysis.LandscapeAnalyzer;
import java.awt.Point;
import ImageAnalysis.ContourFollower;
import ImageAnalysis.MinimalGraphicalObject;
import utilities.CustomDataTypes.intRange;
import java.util.ArrayList;
import ij.process.ColorProcessor;
/**
 *
 *
 * @author Taihao
 */
public class Stitching_Stacks implements PlugIn {
    int rows,columns,channels,w,h,z;
    ImagePlus impl1,impl2,stitchedImg;
    String dir;
    ArrayList<String> imgFileNames;
    public void run(String Arg)
    {
        impl1=CommonMethods.importImage();
        impl2=CommonMethods.importImage();
        stitchImgs();
    }
    void stitchImgs(){
        int threshold=70;
        w=impl2.getWidth();
        h=impl2.getHeight();
        int wp=100,hp=100;
        if(wp>w)wp=w;
        if(hp>h)wp=h;
        int[] r=new int[h*w],g=new int[h*w],b=new int[h*w];
        int[] r1=new int[h*w],g1=new int[h*w],b1=new int[h*w];
        int[][] pixels=new int[h][w],pps=new int[h][wp];
        int[][] stamp=new int[h][wp];
        int ns=impl1.getNSlices();
        int i,j,k,o;
        ArrayList<Point> contour=null;
        MinimalGraphicalObject mgo=new MinimalGraphicalObject();
        boolean validObject;
        intRange yRange;
        int x,y;
        int dx0=w-178,dy0=0;
        Point shift;
        ArrayList<Point> shifts=new ArrayList<Point>();
        intRange xr=new intRange(),yr=new intRange();
        int pixel;
        for(i=1;i<=ns;i++){
            impl1.setSlice(i);
            impl2.setSlice(i);
            validObject=false;
            CommonMethods.getRGB(impl1, r, g, b);
            for(j=0;j<h;j++){
                o=j*w;
                for(k=0;k<w;k++){
                    pixels[j][k]=b[o+k];
                }
            }
            CommonMethods.getRGB(impl2, r, g, b);
            for(j=0;j<h;j++){
                o=j*w;
                for(k=0;k<wp;k++){
                    pps[j][k]=b[o+k];
                }
            }
            LandscapeAnalyzer.stampPixels(wp, h, pps, stamp);
            for(j=0;j<h;j++){
                for(k=0;k<wp;k++){
                    if(stamp[j][k]==LandscapeAnalyzer.localMaximum){
                        pixel=pps[j][k];
                        if(pixel<150) continue;
                        contour=ContourFollower.getContour_Out(pps, wp, h, new Point(k,j), threshold, 255);
                        if(contour.size()<20) continue;
                        mgo.setContour(pps, contour, new intRange(threshold,255));
                        if((mgo.getYRange().getMax()-mgo.getYRange().getMin())>4) validObject=true;
                    }
                    if(validObject) break;
                }
                if(validObject) break;
            }
            shift=getShift(contour,w,h,wp,h,pixels,pps,dx0,dy0);
            shifts.add(shift);
            xr.expandRange(shift.x);
            yr.expandRange(shift.y);
        }
        int wn=2*w-178+xr.getMax();
        int hn=h+Math.max(Math.abs(yr.getMin()), Math.abs(yr.getMax()));
        ImageStack is=new ImageStack(wn,hn);
        ImageStack is1=new ImageStack(2*w,h);
        int[] pns;
        int sy;
        int[] p1s,p2s;
        int br,bg,bb;
        for(i=0;i<ns;i++){
            impl1.setSlice(i);
            impl2.setSlice(i);
            p1s=(int[])impl1.getProcessor().getPixels();
            p2s=(int[])impl2.getProcessor().getPixels();
            ColorProcessor cp=new ColorProcessor(wn,hn);
            ColorProcessor cp1=new ColorProcessor(2*w,h);
            pns=(int[])cp.getPixels();
            int[] pns1=(int[])cp1.getPixels();
            shift=shifts.get(i);
            sy=Math.max(0, -shift.y);
            for(j=0;j<h;j++){
                for(k=0;k<w;k++){
                    y=j+sy;
                    x=k;
                    pixel=p1s[j*w+k];
                    pns[y*wn+x]=pixel;
                    pns1[2*j*h+k]=pixel;
                    y=j+shift.y;
                    x=shift.x+dx0+k;
                    pixel=p2s[j*w+k];
                    br=0xff&pixel>>16;
                    bg=0xff&pixel>>8;
                    bb=0xff&pixel;
                    pns1[2*j*h+k+w]=pixel;
                    pns[y*wn+x]=pixel;
//                    pns[y*wn+x]=255|255|255;
                }
            }
            is.addSlice("", cp);
            is1.addSlice("", cp1);
        }
        ImagePlus impl=new ImagePlus("Stitched image",is);
        ImagePlus impl0=new ImagePlus("Merged image",is1);
        impl1.show();
        impl2.show();
        impl.show();
        impl0.show();
    }
    Point getShift(ArrayList <Point> contour,int w, int h, int wp, int hp, int[][] pixels, int[][] pps, int dx0, int dy0){
        int dx,dy,xn=-50,xx=50,yn=-50,yx=50;
        int val=0;
        int x,y,xi,yi;
        int size=contour.size(),i;
        int vx=0;
        Point shift=new Point();
        Point p0,p,p1,pin=new Point();
        boolean first=true;

        for(dy=yn;dy<=yx;dy++){
            for(dx=xn;dx<=xx;dx++){
                val=0;
                p0=contour.get(size-2);
                p=contour.get(size-1);
                for(i=0;i<size;i++){
                    p1=contour.get(i);
                    if(p0.x==p.x&&p.x==p1.x){
                        pin.y=p.y;
                        pin.x=p.x+p0.y-p1.y;
                    }
                    else if(p0.y==p.y&&p.y==p1.y){
                        pin.x=p.x;
                        pin.y=p.y+p1.x-p0.x;
                    }else if(p0.x==p.x){
                        pin.x=p1.x;
                        pin.y=p0.y;
                    }else if(p0.y==p.y){
                        pin.x=p0.x;
                        pin.y=p1.y;
                    }
                    x=dx0+dx+p.x;
                    y=dy0+dy+p.y;
                    xi=dx0+dx+pin.x;
                    yi=dy0+dy+pin.y;
                    if(x<0||x>=w) continue;
                    if(y<0||y>=h) continue;
                    if(xi<0||xi>=w) continue;
                    if(yi<0||yi>=h) continue;
                    val+=(pps[p.y][p.x]-pps[pin.y][pin.x])*(pixels[y][x]-pixels[yi][xi]);
                }
                if(first){
                    first=false;
                    vx=val;
                }else{
                    if(val>vx){
                        vx=val;
                        shift.setLocation(dx,dy);
                    }
                }
            }
        }
        return shift;
    }
}
