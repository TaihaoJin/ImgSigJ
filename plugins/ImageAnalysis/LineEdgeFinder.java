/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ij.ImagePlus;
import utilities.CommonMethods;
import utilities.ArrayofArrays.IntPairArray2;
import utilities.Geometry.Vector2D;

/**
 *
 * @author Taihao
 */
public class LineEdgeFinder {
    
    public static IntPairArray2 getEdges(ImagePlus impl){
        int type=impl.getType();
        IntPairArray2 edges=null;
        switch (type) {
            case CommonMethods.GRAY8:
                edges=getEdges_Gray8(impl);
                break;
            default:
                break;
        }
        return edges;
    }
    public static IntPairArray2 getEdges_Gray8(ImagePlus impl){
        ImagePlus mask;        
        int w=impl.getHeight(),h=impl.getWidth();
        IntPairArray2 edges=new IntPairArray2();
        mask=CommonMethods.cloneImage("coloned for LineEdgeFiner", impl);
        byte pixels[]=(byte[])mask.getProcessor().getPixels();
        double hist[]=new double[256];
        double hist_cumu[]=new double[256];
        byte[][] ps=new byte[h][w];
        getGradientHist_Byte(pixels, ps, w, h, hist,hist_cumu);
        int windowSize=7;
        return edges;
    }
    public static IntPairArray2 getEdges_GM_Gray8(ImagePlus impl){
        ImagePlus mask;        
        int h=impl.getHeight(),w=impl.getWidth();
        IntPairArray2 edges=new IntPairArray2();
        mask=CommonMethods.cloneImage("coloned for LineEdgeFiner", impl);
        byte pixels[]=(byte[])mask.getProcessor().getPixels();
        double hist[]=new double[256];
        double hist_cumu[]=new double[256];
        double meanSem[]=new double[2];
        double [][] gX=new double[h][w], gY=new double[h][w];
        getHist_Gray8(pixels,hist,meanSem);
        
//        CommonMethods.pixelGradient_Gray8_Sobel(pixels, w, h, gX, gY);
        CommonMethods.pixelGradient_Gray8_NarrowLine(pixels, w, h, gX, gY);
        enhanceGradient(gX,gY,w,h,meanSem);
//        calGradientAmp(gX,gY,w,h);
        double gMax=gY[0][0];
        int i,j,o;
        double t;
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
//                pixels[o+j]=(byte)(255*gX[i][j]/gMax);
                t=gX[i][j];
                if(t>255)t=255;
                pixels[o+j]=(byte)(t);
            }
        }
        impl.getProcessor().setPixels(pixels);
        return edges;
    }
    public static void calGradientAmp(double[][] gX0, double[][] gY0, int w, int h){
        int i,j;
        double gx,gy,gxy;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                gx=gX0[i][j];
                gy=gY0[i][j];
                gxy=Math.sqrt(gx*gx+gy*gy);
                gX0[i][j]=gxy;
            }            
        }
    }
    public static void enhanceGradient(double[][] gX0, double[][] gY0, int w, int h, double[] meanSem){
        double[][] gX=new double[h][w], gY=new double[h][w];
        double gx,gy,gxy,gMax=0.;
        double hs=0.5*Math.sqrt(2.);
        int direction[]=new int [2];
        Vector2D g=new Vector2D(),g1=new Vector2D(),g2=new Vector2D();
        int i,j,i0,i1,j0,j1,dx,dy,dxo,dyo;//dx,dy define the direction of the gradient, while dx0 and dyo define the orthogonal direction.
        for(i=1;i<h-1;i++){
            for(j=1;j<w-1;j++){
                g.setComponents(gX0[i][j], gY0[i][j]);
                g.getDirection(direction);
                dx=direction[0];
                dy=direction[1];
                dxo=dy;
                dyo=-dx;
//                growVectElements(gX0,gY0,w,h,-dx,-dy,i,j,g);
                growVectElements(gX0,gY0,w,h,dxo,dyo,i,j,g,meanSem);
                gX[i][j]=g.dx;
                gY[i][j]=g.dy;
            }            
        }        
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                gx=gX[i][j];
                gy=gY[i][j];
                gxy=Math.sqrt(gx*gx+gy*gy);
                if(gxy>gMax) gMax=gxy;
                gX0[i][j]=gxy;
            }            
        }
        gY0[0][0]=gMax;
    }
    public static void growVectElements(double[][] gX0, double[][] gY0, int w, int h, int dx, int dy, int i0, int j0, Vector2D g, double[] meanSem){
        int i,j;
        double outerMean=0.;
        double interMean=0.;
        double gxy=0.;
        int dxo=dy;
        int dyo=-dx;
        Vector2D g1=new Vector2D();
        i=i0+dy;
        j=j0+dx;
        int nm=0;
        int neg=0;
        if(i<0||i>=h) neg=-2;
        if(j<0||j>=w) neg=-2;
        if(neg==0){
            g1.setComponents(gX0[i][j], gY0[i][j]);
            gxy=g.getProjection(g1);
        }
//        while(neg<1&&nm<5){
        while(neg<1){
            nm++;
            g.extend(gxy);
            i+=dy;
            j+=dx;
            if(i<0||i>=h) break;
            if(j<0||j>=w) break;
            try{g1.setComponents(gX0[i][j], gY0[i][j]);}
            catch(ArrayIndexOutOfBoundsException e){
            }
            gxy=g.getProjection(g1);
            if(gxy<0) neg++;
        }
        neg=0;
        dx=-dx;
        dy=-dy;
        gxy=0.;
        g1=new Vector2D();
        i=i0+dy;
        j=j0+dx;
        if(i<0||i>=h) neg=-2;
        if(j<0||j>=w) neg=-2;
        if(neg==0){
            g1.setComponents(gX0[i][j], gY0[i][j]);
            gxy=g.getProjection(g1);
        }
        while(neg<1){
            nm++;
            g.extend(gxy);
            i+=dy;
            j+=dx;
            if(i<0||i>=h) break;
            if(j<0||j>=w) break;
            try{g1.setComponents(gX0[i][j], gY0[i][j]);}
            catch(ArrayIndexOutOfBoundsException e){
            }
            gxy=g.getProjection(g1);
            if(gxy<0) neg++;
        }
    }
    public static void getGradientHist_Byte(byte[] pixels, byte[][] ps, int w, int h, double[] hist, double[] hist_cumu){   
        //have not verify yet (08n08)
        int i,j,o,k,d,l;
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                ps[i][j]=pixels[o+j];
            }
        }
        int sum=0;
        for(i=1;i<h/2-1;i+=2){
            for(j=1;j<w/2-1;j+=2){
                for(k=-1;k<=1;k++){
                    for(l=-1;l<=1;l++){
                        d=Math.abs(ps[i+k][i+l]-ps[i][j]);
                        hist[d]+=1.;
                        sum++;
                    }
                }
            }
        }
        double cumu=0.,prob=0.; 
        for(i=0;i<=255;i++){
            prob=hist[i]/(double)sum;
            hist[i]=prob;
            cumu+=prob;
            hist_cumu[i]=cumu;
        }
    }
    public static void getHist_Gray8(byte pixels[], double[] hist, double[] meanSem){
        int len=pixels.length;
        int hl=256,p,i;
        double mean=0.,sem=0.,dt;
        for(i=0;i<256;i++){
            hist[i]=0.;
        }
        for(i=0;i<len;i++){
            p=0xff&pixels[i];
            dt=p;
            sem+=dt*dt;
            mean+=dt;
            hist[p]+=1.;
        }
        for(i=0;i<256;i++){
            hist[i]/=len;
        }
        meanSem[0]=mean/len;
        meanSem[1]=Math.sqrt(sem/len-mean*mean);
    }
}
