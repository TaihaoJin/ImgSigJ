/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import java.awt.Color;
import utilities.CustomDataTypes.IntPair;
import utilities.CustomDataTypes.intRange;
import utilities.Geometry.ImageShapes.*;
import java.util.ArrayList;
import java.awt.Point;
import utilities.statistics.PlaneFitter;
import utilities.statistics.TwoDEnveloper;
import utilities.Gui.PlotWindowPlus;
import java.awt.image.BufferedImage;
import java.awt.Component;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.util.Formatter;
import utilities.io.FileAssist;
import utilities.statistics.GaussianDistribution;
import utilities.CustomDataTypes.DoublePair;
import ImageAnalysis.*;
/**
 *
 * @author Taihao
 */
public class CommonImageMethods {
    public static void YMean(ImagePlus impl, int ws){
        impl.show();
        int i,j,h=impl.getHeight(),w=impl.getWidth(),len=2*ws+1;
        int[][] pixels0=CommonMethods.getPixelValues(impl);
        int[][] pnYsum=CommonStatisticsMethods.getSumLine_Y(pixels0);
        int[][] pixels=new int[h][w];
        for(i=ws+1;i<h-ws;i++){
            for(j=0;j<w;j++){
                pixels[i][j]=(pnYsum[i+ws][j]-pnYsum[i-ws-1][j])/len;
            }
        }
        for(i=0;i<=ws;i++){
            for(j=0;j<w;j++){
                pixels[i][j]=pixels[ws+1][j];
            }
        }
        for(i=0;i<ws;i++){
            for(j=0;j<w;j++){
                pixels[h-ws+i][j]=pixels[h-ws-1][j];
            }
        }
        CommonMethods.setPixels(impl, pixels);
        impl.draw();
    }
    public static void XMean(ImagePlus impl, int ws){
        impl.show();
        int i,j,h=impl.getHeight(),w=impl.getWidth(),len=2*ws+1;
        int[][] pixels0=CommonMethods.getPixelValues(impl);
        int[][] pnXsum=CommonStatisticsMethods.getSumLine_X(pixels0);
        int[][] pixels=new int[h][w];
        for(i=0;i<h;i++){
            for(j=ws+1;j<w-ws;j++){
                pixels[i][j]=(pnXsum[i][j+ws]-pnXsum[i][j-ws-1])/len;
            }
        }
        for(i=0;i<h;i++){
            for(j=0;j<=ws;j++){
                pixels[i][j]=pixels[i][ws+1];
            }
            for(j=0;j<ws;j++){
                pixels[i][w-ws+j]=pixels[i][w-ws-j];
            }
        }
        CommonMethods.setPixels(impl, pixels);
        impl.draw();
    }
    public static void findYGradient(ImagePlus impl, int ws){
        impl.show();
        ImagePlus implXM=CommonMethods.cloneImage(impl);
        XMean(implXM,ws);
        int i,j,h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels0=CommonMethods.getPixelValues(implXM);
        implXM.close();
        int[][] pixels=new int[h][w];
        
        for(i=1;i<h-1;i++){
            for(j=0;j<w;j++){
                pixels[i][j]=Math.abs(pixels0[i+1][j]-pixels0[i-1][j]);
            }
        }
            for(j=0;j<w;j++){
                pixels[0][j]=pixels[1][j];
            }
            for(j=0;j<w;j++){
                pixels[h-1][j]=pixels[h-2][j];
            }
        CommonMethods.setPixels(impl, pixels);        
        impl.draw();
    }
    public static void findXGradient(ImagePlus impl, int ws){
        impl.show();
        ImagePlus implYM=CommonMethods.cloneImage(impl);
        YMean(implYM,ws);
        int i,j,h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels0=CommonMethods.getPixelValues(implYM);
        implYM.close();
        
        int[][] pixels=new int[h][w];
        
        for(i=0;i<h;i++){
            for(j=1;j<w-1;j++){
                pixels[i][j]=Math.abs(pixels0[i][j+1]-pixels0[i][j-1]);
            }
        }
        for(i=0;i<h;i++){
            pixels[i][0]=pixels[i][1];
            pixels[i][w-1]=pixels[i][w-2];
        }
        CommonMethods.setPixels(impl, pixels);        
        impl.draw();
    }
    public static void findYGradient0(ImagePlus impl, int ws){
        impl.show();
        int i,j,h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels0=CommonMethods.getPixelValues(impl);
        int[][] pnYsum=CommonStatisticsMethods.getSumLine_Y(pixels0);
        int[][] pixels=new int[h][w];
        for(i=ws+1;i<h-ws;i++){
            for(j=0;j<w;j++){
                pixels[i][j]=Math.abs((pnYsum[i+ws][j]-pnYsum[i][j])-(pnYsum[i-1][j]-pnYsum[i-ws-1][j]))/(ws);
            }
        }
        for(i=0;i<=ws;i++){
            for(j=0;j<w;j++){
                pixels[i][j]=pixels[ws+1][j];
            }
        }
        for(i=0;i<ws;i++){
            for(j=0;j<w;j++){
                pixels[h-ws+i][j]=pixels[h-ws-1][j];
            }
        }
        CommonMethods.setPixels(impl, pixels);        
        impl.draw();
    }
    public static void findXGradient0(ImagePlus impl, int ws){
        impl.show();
        int i,j,h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels0=CommonMethods.getPixelValues(impl);
        int[][] pnXsum=CommonStatisticsMethods.getSumLine_X(pixels0);
        int[][] pixels=new int[h][w];
        for(i=0;i<h;i++){
            for(j=ws+1;j<w-ws;j++){
                pixels[i][j]=Math.abs((pnXsum[i][j+ws]-pnXsum[i][j])-(pnXsum[i][j-1]-(pnXsum[i][j-ws-1])))/ws;
            }
        }
        for(i=0;i<h;i++){
            for(j=0;j<=ws;j++){
                pixels[i][j]=pixels[i][ws+1];
            }
        }
        for(i=0;i<h;i++){
            for(j=0;j<ws;j++){
                pixels[i][w-ws+j]=pixels[i][w-ws-1];
            }
        }
        CommonMethods.setPixels(impl, pixels);        
        impl.draw();
    }
    public static void findYGradientExclusive(ImagePlus impl, int ws){
       impl.show();
       ImagePlus implc=CommonMethods.cloneImage(impl);
       findYGradient(impl,ws);
       findXGradient(implc,ws);
       subtractImagePixels_AbsoluteValues(impl, implc);
       impl.draw();
    }
    public static void findXLine(ImagePlus impl, int ws, int hw){//hw is half of line with
        impl.show();
        int i,j,h=impl.getHeight(),w=impl.getWidth(),hl=ws+hw,len=2*hw+1;
        int[][] pixels0=CommonMethods.getPixelValues(impl);
        int[][] pnYsum=CommonStatisticsMethods.getSumLine_Y(pixels0);
        int[][] pixels=new int[h][w];
        for(i=hl+1;i<h-hl;i++){
            for(j=0;j<w;j++){
                pixels[i][j]=Math.abs(-(pnYsum[i+hl][j]-pnYsum[i+hw][j]+pnYsum[i-hw-1][j]-pnYsum[i-hl-1][j])/ws+2*(pnYsum[i+hw][j]-pnYsum[i-hw-1][j])/len)/2;
            }
        }
        for(i=0;i<=hl;i++){
            for(j=0;j<w;j++){
                pixels[i][j]=pixels[hl+1][j];
            }
        }
        for(i=0;i<hl;i++){
            for(j=0;j<w;j++){
                pixels[h-hl+i][j]=pixels[h-hl-1][j];
            }
        }
        CommonMethods.setPixels(impl, pixels);        
        impl.draw();
    }
    public static void findYLine(ImagePlus impl, int ws, int hw){//hw is half of line with
        impl.show();
        int i,j,h=impl.getHeight(),w=impl.getWidth(),hl=ws+hw,len=2*ws+1;
        int[][] pixels0=CommonMethods.getPixelValues(impl);
        int[][] pnXsum=CommonStatisticsMethods.getSumLine_X(pixels0);
        int[][] pixels=new int[h][w];
        for(i=0;i<h;i++){
            for(j=hl+1;j<w-hl;j++){
                pixels[i][j]=((pnXsum[i][j+hl]-pnXsum[i][j+hw]+pnXsum[i][j-hw-1]-pnXsum[i][j-hl-1])/ws-2*(pnXsum[i][j+ws]-pnXsum[i][j-ws-1])/len)/2;
            }
        }
        for(i=0;i<h;i++){
            for(j=0;j<=hl;j++){
                pixels[i][j]=pixels[i][hl+1];
            }
        }
        for(i=0;i<h;i++){
            for(j=0;j<hl;j++){
                pixels[i][w-hl+j]=pixels[i][w-hl-1];
            }
        }
        CommonMethods.setPixels(impl, pixels);        
        impl.draw();
    }
    public static void findXLineExclusive(ImagePlus impl, int ws, int hw){//hw is half of line with
        impl.show();
        ImagePlus implc=CommonMethods.cloneImage(impl);
        findXLine(impl,ws,hw);
        findYLine(implc,ws,hw);
        subtractImagePixels_AbsoluteValues(impl, implc);
        impl.draw();
    }
    public static void findYLineExclusive(ImagePlus impl, int ws, int hw){//hw is half of line with
        impl.show();
        ImagePlus implc=CommonMethods.cloneImage(impl);
        findYLine(impl,ws,hw);
        findXLine(implc,ws,hw);
        subtractImagePixels_AbsoluteValues(impl, implc);
        impl.draw();
    }
    public static void subtractImagePixels_AbsoluteValues(ImagePlus impl0, ImagePlus impl){
        impl.show();
        int[][] pixels0=CommonMethods.getPixelValues(impl0),pixels=CommonMethods.getPixelValues(impl);
        CommonStatisticsMethods.addArray_AbsoluteValues(pixels0, pixels, pixels0, -1);
        CommonMethods.setPixels(impl0, pixels0);
        impl.draw();
    }
    public static ImagePlus copyImage(ImagePlus impl, int type){
        return CommonMethods.newImage(CommonMethods.getPixelValues(impl), type);
    }
    public static ImagePlus copyImage(String title, ImagePlus impl, int type){
        ImagePlus implc=copyImage(impl,type);
        implc.setTitle(title);
        return implc;
    }
    public static ImagePlus linkHorizontalLines(ImagePlus impl){
        ImagePlus implc=copyImage("Line Segments of "+impl.getTitle(),impl,ImagePlus.COLOR_RGB);
        int w=impl.getWidth(),h=impl.getHeight(),i,j,pixel,r=(255<<16)|(0<<8)|(0);
        boolean[][] hLine=new boolean[h][w];
        int[][] pixels=CommonMethods.getPixelValues(impl),pixelsc=CommonMethods.getPixelValues(implc);
        for(j=0;j<w;j++){
            for(i=1;i<h-1;i++){
                pixel=pixels[i][j];
                if(pixel>=pixels[i-1][j]&&pixel>=pixels[i+1][j])pixelsc[i][j]=r;
            }
        }
        CommonMethods.setPixels(implc, pixelsc);
        implc.show();
        return implc;
    }
    public static void findRidges_X(ImagePlus impl){
        int w=impl.getWidth(),h=impl.getHeight(),i,j,pixel,pl,pr,sign;
        int[][] pixels0=CommonMethods.getPixelValues(impl),stamp=new int[h][w],pixels=new int[h][w];
        IntPair ip;
        intRange xRange=new intRange(0,w-1), yRange=new intRange(0,h-1);
        CommonStatisticsMethods.markExtrema_Y(pixels0, stamp);
        CommonStatisticsMethods.setElements(pixels, 0);
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                sign=stamp[i][j];
                if(sign==0) continue;
                ip=CommonStatisticsMethods.nextPositionWithTheValue(stamp, j, i-1, 0, -1, -sign, xRange,yRange);
                if(ip==null) continue;
                pl=pixels0[ip.right][ip.left];
                ip=CommonStatisticsMethods.nextPositionWithTheValue(stamp, j, i+1, 0, 1, -sign, xRange,yRange);
                if(ip==null) continue;
                pr=pixels0[ip.right][ip.left];
                pixel=pixels0[i][j];
                pixels[i][j]=pixel-(pl+pr)/2;
             }
        }
        CommonMethods.setPixels(impl, pixels);
        impl.show();
    }
    public static void PlaneFittingFilter(ImagePlus impl, int ws){
        int i,j,len=2*ws+1,h=impl.getHeight(),w=impl.getWidth(),i1,j1;
        RectangleImage r=new RectangleImage(len,len);
        r.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        int[][] pixels0=CommonMethods.getPixelValues(impl),pixels=new int[h][w];
        CommonStatisticsMethods.copyArray(pixels0, pixels);
        int n=len*len,index;
        double pdData[][]=new double[n][3];
        double pdFittedData[]=new double[n];
        PlaneFitter cPF=new PlaneFitter();
        int x,y,z;
        for(i=ws;i<h-ws;i++){
            for(j=ws;j<w-ws;j++){
                index=0;
                for(i1=-ws;i1<=ws;i1++){
                    for(j1=-ws;j1<=ws;j1++){
                        x=j+j1;
                        y=i+i1;
                        pdData[index][0]=x;
                        pdData[index][1]=y;
                        pdData[index][2]=pixels0[y][x];
                        index++;
                    }
                }
                cPF.update(pdData, pdFittedData);
                pixels[i][j]=(int)cPF.calZ(j, i);
            }
        }
        CommonMethods.setPixels(impl, pixels);
        impl.show();
    }
    public static void PlaneFittingFilter_OptimalFitting(ImagePlus impl, int ws){
        int i,j,len=2*ws+1,h=impl.getHeight(),w=impl.getWidth(),i1,j1;
        RectangleImage r=new RectangleImage(len,len);
        r.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        int[][] pixels=CommonMethods.getPixelValues(impl),pixelsU=new int[h][w],pixelsL=new int[h][w];
        int n=len*len,index;
        double pdData[][]=new double[n][3],delta, pdDeltas[][]=new double[h][w],pdDev[][]=new double[h][w];
        double dev,pdFittedData[]=new double[n];
        PlaneFitter cPF=new PlaneFitter();
        int x,y,z;
        CommonStatisticsMethods.setElements(pdDev,Double.POSITIVE_INFINITY);
        buildEnvImages(impl,ws,pixelsU,pixelsL);
        int pnXY[][]=new int[n][2];
        boolean valid;
        for(i=ws;i<h-ws;i++){
            for(j=ws;j<w-ws;j++){
                index=0;
                for(i1=-ws;i1<=ws;i1++){
                    for(j1=-ws;j1<=ws;j1++){
                        x=j+j1;
                        y=i+i1;
                        pdData[index][0]=x;
                        pdData[index][1]=y;
                        pdData[index][2]=pixels[y][x];
                        pnXY[index][0]=x;
                        pnXY[index][1]=y;
                        index++;
                    }
                }
                cPF.update(pdData, pdFittedData);
                valid=true;
                
                for(index=0;index<n;index++){
                    x=pnXY[index][0];
                    y=pnXY[index][1];
                    z=(int) (pdFittedData[index]+0.5);
                    if(z>pixelsU[y][x]||z<pixelsL[y][x]){
                        valid=false;
                        break;
                    }
                }
                if(!valid) 
                    continue;
                for(index=0;index<n;index++){
                    x=pnXY[index][0];
                    y=pnXY[index][1];
                    
                    dev=cPF.getSS2();
                    if(dev<pdDev[y][x]){
                        pdDeltas[y][x]=pdFittedData[index]-pixels[y][x];
                        pdDev[y][x]=dev;
                    }                    
                }
            }
        }
        for(i=ws;i<h-ws;i++){
            for(j=ws;j<w-ws;j++){
                pixels[i][j]+=(int)pdDeltas[i][j];
            }
        }
        CommonMethods.setPixels(impl, pixels);
        impl.show();
    }
    public static void PlaneFittingFilter_MinChange(ImagePlus impl, int ws){
        int i,j,len=2*ws+1,h=impl.getHeight(),w=impl.getWidth(),i1,j1;
        RectangleImage r=new RectangleImage(len,len);
        r.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        int[][] pixels=CommonMethods.getPixelValues(impl);
        int n=len*len,index;
        double pdData[][]=new double[n][3],delta, pdDeltas[][]=new double[h][w];
        double pdFittedData[]=new double[n];
        PlaneFitter cPF=new PlaneFitter();
        int x,y,z;
        CommonStatisticsMethods.setElements(pdDeltas,Double.POSITIVE_INFINITY);
        int pnXY[][]=new int[n][2];
        for(i=ws;i<h-ws;i++){
            for(j=ws;j<w-ws;j++){
                index=0;
                for(i1=-ws;i1<=ws;i1++){
                    for(j1=-ws;j1<=ws;j1++){
                        x=j+j1;
                        y=i+i1;
                        pdData[index][0]=x;
                        pdData[index][1]=y;
                        pdData[index][2]=pixels[y][x];
                        pnXY[index][0]=x;
                        pnXY[index][1]=y;
                        index++;
                    }
                }
                cPF.update(pdData, pdFittedData);
                for(index=0;index<n;index++){
                    x=pnXY[index][0];
                    y=pnXY[index][1];
                    delta=pdFittedData[index]-pixels[y][x];
                    if(Math.abs(delta)<Math.abs(pdDeltas[y][x]))pdDeltas[y][x]=delta;                    
                }
            }
        }
        for(i=ws;i<h-ws;i++){
            for(j=ws;j<w-ws;j++){
                pixels[i][j]+=(int)pdDeltas[i][j];
            }
        }
        CommonMethods.setPixels(impl, pixels);
        impl.show();
    }
    public static void showEnvImages(ImagePlus impl, int ws){
        int h=impl.getHeight(),w=impl.getWidth();
        int[][] pixelsU=new int[h][w],pixelsL=new int[h][w];
        buildEnvImages(impl,ws,pixelsU,pixelsL);
    }
    public static void buildEnvImages(ImagePlus impl, int ws, int[][] pixelsU, int[][] pixelsL){
        int i,j,len=2*ws+1,h=impl.getHeight(),w=impl.getWidth(),i1,j1;
        RectangleImage r=new RectangleImage(len,len);
        r.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        int[][] pixels=CommonMethods.getPixelValues(impl);
        CommonStatisticsMethods.copyArray(pixels,pixelsU);
        CommonStatisticsMethods.copyArray(pixels,pixelsL);
        int n=len*len,index;
        double[][] pdEnvU=new double[h][w],pdEnvL=new double[h][w];
        int x,y,z;
        TwoDEnveloper TDEnv=new TwoDEnveloper();
        TDEnv.buildEnvelopes(pixels, r, pdEnvU, pdEnvL);
        for(i=ws;i<h-ws;i++){
            for(j=ws;j<w-ws;j++){
                pixelsU[i][j]=(int)(pdEnvU[i][j]+0.5);
                pixelsL[i][j]=(int)(pdEnvL[i][j]+0.5);
            }
        }
        CommonMethods.displayAsImage("Upper Envelop of "+impl.getTitle(), w, h, pixelsU, ImagePlus.GRAY32);
        CommonMethods.displayAsImage("Lower Envelop of "+impl.getTitle(), w, h, pixelsL, ImagePlus.GRAY32);
    }
    public static ImagePlus composePlotImages(ArrayList<PlotWindowPlus> plots){
        int i,j,len=plots.size(),cols,rows;
        cols=(int) Math.sqrt(len);
        rows=cols;
        while(cols*rows<len){
            if(cols<rows)
                cols++;
            else
                rows++;
        }
        
        PlotWindowPlus pw=plots.get(0);
        ImagePlus implT,impl=pw.getImagePlus();
        int w0=impl.getWidth(),h0=impl.getHeight(),w=w0*cols,h=h0*rows,r,c,index,ox,oy,y,x;
        int[][]pixels=new int[h][w],pixels0=new int[h0][w0];
        String title=plots.get(0).getTitle();
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                index=r*cols+c;
                if(index<len){
                    pw=plots.get(index);
                    pw.refreshPlot();
                    impl=pw.getImagePlus();
                    impl.show();
                }
                if(index>0&&index<len) title+="; "+plots.get(index).getTitle();
                CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels0);
                ox=c*w0;
                oy=r*h0;
                for(i=0;i<h0;i++){
                    y=i+oy;
                    for(j=0;j<w0;j++){
                        x=ox+j;
                        if(index<len){
                            pixels[y][x]=pixels0[i][j];
                        }else
                            pixels[y][x]=Color.white.getRGB();
                    }
                }
            }
        }
        implT=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w, h);
        CommonMethods.setPixels(implT, pixels, true);
        implT.setTitle(title);
        implT.show();
        return implT;
    }
    public static BufferedImage getScreenshort(Component cmp){
//        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage capture=null;
        try {
            Point p=cmp.getLocationOnScreen();
            Rectangle r=new Rectangle(cmp.getBounds());
            r.setLocation(p);
            capture = new Robot().createScreenCapture(r);
        }
        catch (AWTException e){};
        return capture;
    }
    public static void markBackground(ImagePlus impl){
        int i,j,h=impl.getHeight(),w=impl.getWidth();
        int[][] pixels=CommonMethods.getPixelValues(impl);
//        boolean[][] pbBackground=getBackground_PixelBased(pixels);
        boolean[][] pbBackground=getBackground_RegionBased(pixels);
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                if(pbBackground[i][j]) 
                    pixels[i][j]=0;
                else 
                    pixels[i][j]=1;
            }
        }
        CommonMethods.setPixels(impl, pixels);
        impl.draw();
    }
    public static boolean[][] getBackground_PixelBased(int[][] pixels){
        DoublePair meanSD=getBackgroundDistribution(pixels);
        int h=pixels.length,w=pixels[0].length;
        double dNum=w*h,mean=meanSD.left,sd=meanSD.right,p=Math.min(0.05, 1./dNum);
        double cutoff=GaussianDistribution.getZatP(1-p, mean, sd, 0.001);
//        cutoff=1287;
        boolean[][] pbBackground=new boolean[h][w];
        int i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                if(pixels[i][j]<cutoff)
                    pbBackground[i][j]=true;
                else
                    pbBackground[i][j]=false;
            }
        }
        return pbBackground;
    }
    public static boolean[][] getBackground_RegionBased(int[][] pixels){
        DoublePair meanSD=getBackgroundDistribution(pixels);
        int h=pixels.length,w=pixels[0].length;
        double dNum=w*h,mean=meanSD.left,sd=meanSD.right,p=Math.min(0.05, 1./dNum)/10000;
        boolean[][] pbBackground=new boolean[h][w];
        RegionBoundaryAnalyzer cRBA=CommonMethods.buildRegionComplex(pixels);
//        p=1./cRBA.getRegionNodes().size();
        cRBA.pickBackgroundRegions_BackgroundDistribution(pixels, mean, sd, p);
        return cRBA.getBackground();
    }
    public static DoublePair getBackgroundDistribution(int[][] pixels){
        ArrayList<Double> LowPixels=new ArrayList();
        int mean=CommonStatisticsMethods.getHistPeakValue(pixels,1.,0);
        CommonMethods.getSmallNumbers(pixels, LowPixels, mean);
        CommonMethods.symmetrizeNumbers(LowPixels, mean,-1);
        double sd=CommonStatisticsMethods.getSD_MAD(LowPixels);
        return new DoublePair(mean,sd);
    }
}
