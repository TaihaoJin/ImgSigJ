/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package stacks;
import ij.ImagePlus;
import utilities.CustomDataTypes.IntPair;
import utilities.CommonMethods;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.IntHistogram;
import ij.IJ;
import utilities.io.FileAssist;
import utilities.io.PrintAssist;
import java.util.Formatter;
import utilities.io.AsciiInputAssist;
/**
 *
 * @author Taihao
 */
public class InternalSticher {
    class shiftXY{
        public int sx,sy;
        public double delta;
    }
    class shift{
        public int[] shiftL,shiftB;
        public double deltaL,deltaB;
        public shift(){
            shiftL=null;
            shiftB=null;
        }
    }
    ImagePlus impl, implStiched;
    int rows, cols,N,w,h;
    int[][][] pixels, sPixels;
    shift[][] pcShifts;
    shift[] pcCenterSlideShifts;
    int[] Zshift;
    int[][] XYshift;
    boolean pbHasSignal[][],pbEmptySlice[];
    int[][] pnNetShifts;
    public InternalSticher(ImagePlus impl, int rows, int cols){
        this.impl=impl;
        this.rows=rows;
        this.cols=cols;
        N=impl.getNSlices();
        h=impl.getHeight();
        w=impl.getWidth();
        pixels=new int[N][h][w];
        for(int i=1;i<=N;i++){
            impl.setSlice(i);
            CommonMethods.getPixelValue(impl, i, pixels[i-1]);
        }
        checkSignals();
        calShifts();
//        checkVerticalConsistancy();
        calNetShifts();
        buildStichedImage();
    }
    public InternalSticher(ImagePlus impl, int rows, int cols, String path){
        double pdA[][]=AsciiInputAssist.readAsciiArray(path);
        this.impl=impl;
        this.rows=rows;
        this.cols=cols;
        N=impl.getNSlices();
        h=impl.getHeight();
        w=impl.getWidth();
        pixels=new int[N][h][w];
        for(int i=1;i<=N;i++){
            impl.setSlice(i);
            CommonMethods.getPixelValue(impl, i, pixels[i-1]);
        }
        buildShiftMatrices(pdA);
        calNetShifts();
        buildStichedImage();
    }
    void buildShiftMatrices(double[][] pd){
        int i,len=rows*cols;
        pcCenterSlideShifts=new shift[len];
        shift sl;
        for(i=0;i<len;i++){
            sl=new shift();
            sl.shiftB=new int[3];
            sl.shiftL=new int[3];
            sl.shiftL[0]=(int)(pd[i][1]+0.5);
            sl.shiftL[1]=(int)(pd[i][2]+0.5);
            sl.shiftL[2]=(int)(pd[i][3]+0.5);
            sl.shiftB[0]=(int)(pd[i][4]+0.5);
            sl.shiftB[1]=(int)(pd[i][5]+0.5);
            sl.shiftB[2]=(int)(pd[i][6]+0.5);
            pcCenterSlideShifts[i]=sl;
        }
    }
    void checkSignals(){
        int i,c,r,len=rows*cols,x0,y0,w0=w/cols,h0=h/rows;
        pbHasSignal=new boolean[N][len];
        pbEmptySlice=new boolean[N];
        boolean empty;
        IntHistogram hist0=new IntHistogram(pixels[N/2],0,h-1,0,w-1),hist;
        int cutoff=hist0.getNumAtRant(0.2);
        for(i=0;i<N;i++){
            empty=true;
            for(r=0;r<rows;r++){
                y0=r*h0;
                for(c=0;c<cols;c++){
                    x0=c*w0;
                    hist=new IntHistogram(pixels[i],y0,y0-1+h0,x0,x0-1+w0);
                    if(hist.getNumAtRant(0.01)<cutoff) {
                        pbHasSignal[i][r*cols+c]=false;                        
                    } else {
                        pbHasSignal[i][r*cols+c]=true;
                        empty=false;
                    }
                }
            }
            pbEmptySlice[i]=empty;
        }
       
    }
    void calShifts(){
        String path=FileAssist.getFilePath("shift file", "", "txt", "txt", false);
        pcShifts=new shift[N][rows*cols];
        int i,len=rows*cols,z0,z,dzL=0,dzB=0;
        int[][] pixels0,pixels;
        int[][] stichedPixels=new int[h][w];
        pnNetShifts=new int[len][3];
        shiftXY sl,slx=new shiftXY(),sb,sbx=new shiftXY();
        for(z0=N/2;z0<=N/2;z0++){
            for(i=0;i<len;i++){
                slx.delta=Double.POSITIVE_INFINITY;
                sbx.delta=Double.POSITIVE_INFINITY;
                pixels0=this.pixels[z0];

                for(z=z0-3;z<=z0;z++){
                    IJ.showStatus("i="+i+" z="+z);
                    if(z<0||z>=N) continue;
                    pixels=this.pixels[z];
                    sl=calShift_Left(pixels0,pixels,i);
                    if(sl!=null){
                        if(sl.delta<slx.delta){
                            slx=sl;
                            dzL=z-z0;
                        }
                    }
                 }
                for(z=z0;z<=z0+2;z++){
                    IJ.showStatus("i="+i+" z="+z);
                    if(z<0||z>=N) continue;
                    pixels=this.pixels[z];
                    sb=calShift_Bottom(pixels0,pixels,i);  
                    if(sb!=null){
                        if(sb.delta<sbx.delta) {
                            sbx=sb;
                            dzB=z-z0;
                        }
                    }
                }
                shift s=new shift();
                int[] l=new int[3],b=new int[3];
                l[0]=slx.sx;
                l[1]=slx.sy;
                l[2]=dzL;
                b[0]=sbx.sx;
                b[1]=sbx.sy;
                b[2]=dzB;
                s.shiftB=b;
                s.shiftL=l;
                s.deltaL=slx.delta;
                s.deltaB=sbx.delta;
                pcShifts[z0][i]=s;
            }
        }
        pcCenterSlideShifts=pcShifts[N/2];
        Formatter fm=PrintAssist.getQuickFormatter(path);
        double pdA[][]=new double[len][7];
        for(i=0;i<len;i++){
            PrintAssist.printNumber(fm, i, 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftL[0], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftL[1], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftL[2], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftB[0], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftB[1], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftB[2], 0);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
    void calShifts0(){
        String path=FileAssist.getFilePath("shift file", "", "txt", "txt", false);
        pcShifts=new shift[N][rows*cols];
        int i,len=rows*cols,z0,z,dzL=0,dzB=0;
        int[][] pixels0,pixels;
        shiftXY sl,slx=new shiftXY(),sb,sbx=new shiftXY();
        for(z0=N/2;z0<=N/2;z0++){
            for(i=0;i<len;i++){
                slx.delta=Double.POSITIVE_INFINITY;
                sbx.delta=Double.POSITIVE_INFINITY;
                pixels0=this.pixels[z0];

                for(z=z0-3;z<=z0;z++){
                    IJ.showStatus("i="+i+" z="+z);
                    if(z<0||z>=N) continue;
                    pixels=this.pixels[z];
                    sl=calShift_Left(pixels0,pixels,i);
                    if(sl!=null){
                        if(sl.delta<slx.delta){
                            slx=sl;
                            dzL=z-z0;
                        }
                    }
                 }
                for(z=z0;z<=z0+2;z++){
                    IJ.showStatus("i="+i+" z="+z);
                    if(z<0||z>=N) continue;
                    pixels=this.pixels[z];
                    sb=calShift_Bottom(pixels0,pixels,i);  
                    if(sb!=null){
                        if(sb.delta<sbx.delta) {
                            sbx=sb;
                            dzB=z-z0;
                        }
                    }
                }
                shift s=new shift();
                int[] l=new int[3],b=new int[3];
                l[0]=slx.sx;
                l[1]=slx.sy;
                l[2]=dzL;
                b[0]=sbx.sx;
                b[1]=sbx.sy;
                b[2]=dzB;
                s.shiftB=b;
                s.shiftL=l;
                s.deltaL=slx.delta;
                s.deltaB=sbx.delta;
                pcShifts[z0][i]=s;
            }
        }
        pcCenterSlideShifts=pcShifts[N/2];
        Formatter fm=PrintAssist.getQuickFormatter(path);
        double pdA[][]=new double[len][7];
        for(i=0;i<len;i++){
            PrintAssist.printNumber(fm, i, 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftL[0], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftL[1], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftL[2], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftB[0], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftB[1], 0);
            PrintAssist.printNumber(fm, pcCenterSlideShifts[i].shiftB[2], 0);
            PrintAssist.endLine(fm);
        }
        fm.close();
    }
    double meanDelta(int[][] pixels0, int[][] pixels, int xi, int xf, int yi, int yf, int dx, int dy){
        double delta=0,d;
        int x0,y0,x,y,num=0;
        for(y0=yi;y0<=yf;y0++){
            y=y0+dy;
            if(y<0||y>=h) continue;
            for(x0=xi;x0<=xf;x0++){
                x=x0+dx;
                if(x<0||x>=w) continue;
                num++;
                d=pixels0[y0][x0]-pixels[y][x];
                delta+=d*d;
            }
        }
        return delta/num;
    }
    shiftXY calShift_Left(int[][]pixels0, int[][]pixels,int index){
        int row=index/cols,col=index-cols*row;
        if(col==cols-1) return null;
        shiftXY s=new shiftXY();
        int L=50;
        int h0=h/rows,w0=w/cols;
        int dx,dy=0,xi,xf,yi,yf,y0;
        int dxn=-220,dxx=-150,dyn=10,dyx=50;
        double delta,deltan=Double.POSITIVE_INFINITY;
        int sx=0,sy=0;
        for(dx=dxn;dx<=dxx;dx++){
            xf=(col+1)*w0-1;
            for(dy=dyn;dy<=dyx;dy++){
                y0=h0*row;
                if(dy<=0){
                    yi=y0;
                    yf=y0+h0-1+dy;
                }else{
                    yi=y0+dy;
                    yf=y0+h0-1;
                }    
                delta=meanDelta(pixels0,pixels,xf-L,xf,yi,yf,-dx,-dy);
                if(delta<deltan) {
                    deltan=delta;
                    sx=dx;
                    sy=dy;
                }
            }             
        }
        s.sx=sx;
        s.sy=sy;
        s.delta=deltan;
        return s;
    }
    
    shiftXY calShift_Bottom(int[][]pixels0, int[][]pixels,int index){
        int row=index/cols,col=index-cols*row;
        if(row==rows-1) return null;
        shiftXY s=new shiftXY();
        int L=50;
        int h0=h/rows,w0=w/cols;
        int dx=0,dy=0,xi,xf,yi,yf,x0;
        int dxn=-25,dxx=-10,dyn=-95,dyx=-70;
        double delta,deltan=Double.POSITIVE_INFINITY;
        int sx=0,sy=0;
        for(dy=dyn;dy<=dyx;dy++){
            yf=h0*(col+1)-1;
            yi=yf-L;
            for(dx=dxn;dx<=dxx;dx++){
                x0=w0*col;
                if(dx<=0){
                    xi=x0;
                    xf=x0+w0-1+dx;
                }else{
                    xi=x0+dx;
                    xf=x0+w0-1;
                }    
                delta=meanDelta(pixels0,pixels,xi,xf,yf-L,yf,-dx,-dy);
                if(delta<deltan) {
                    deltan=delta;
                    sx=dx;
                    sy=dy;
                }
            }             
        }
        s.sx=sx;
        s.sy=sy;
        s.delta=deltan;
        return s;
    }
    void checkVerticalConsistancy(){
        int z,r,c,i,len=rows*cols,in=0,dz,dzt,dzn,dx,dy,dxt,dyt;
        int[] zs=new int[len];
        Zshift=new int[len];
        XYshift=new int[len][len];
        for(z=N/2;z<=N/2;z++){
            dz=0;
            dzn=0;
            dx=0;
            dy=0;
            zs[0]=0;
            for(r=0;r<rows;r++){
                if(r==0){
                    for(c=0;c<cols;c++){
                        if(c==0) continue;
                        dx+=pcShifts[z][c-1].shiftL[0];
                        dy+=pcShifts[z][c-1].shiftL[1];
                        dz+=pcShifts[z][c-1].shiftL[2];
                        zs[c]=dz;
                        XYshift[c][0]=dx;
                        XYshift[c][1]=dy;
                        if(dz<dzn){
                            in=c;
                            dzn=dz;
                        }
                    }
                    continue;
                }
                for(c=0;c<cols;c++){
                    i=r*rows+c;
                    dxt=XYshift[i-cols][0]+pcShifts[z][i-cols].shiftB[0];
                    dyt=XYshift[i-cols][1]+pcShifts[z][i-cols].shiftB[1];
                    dzt=zs[i-cols]+pcShifts[z][i-cols].shiftB[2];
                    if(c==0){
                        dz=dzt;
                        dx=dxt;
                        dy=dyt;
                    }else{
                        dx+=pcShifts[z][i-1].shiftL[0];
                        dy+=pcShifts[z][i-1].shiftL[1];
                        dz+=pcShifts[z][i-1].shiftL[2];
//                        if(dz!=dzt) IJ.error("Z inconsistency");
                    }
                    zs[i]=dz;
                        XYshift[i][0]=dx;
                        XYshift[i][1]=dy;
                    if(dz<dzn){
                        in=i;
                        dzn=dz;
                    }                    
                }
            }
            for(i=0;i<len;i++){
                Zshift[i]=zs[i]-dzn;
            }
        }
    }
    void calNetShifts(){
        int r,c,i,len=rows*cols,in=0,dz,dzt,dzn,dx,dy,dxt,dyt;
        int[] zs=new int[len];
        Zshift=new int[len];
        XYshift=new int[len][len];
//        for(z=N/2;z<=N/2;z++){
            dz=0;
            dzn=0;
            dx=0;
            dy=0;
            zs[0]=0;
            for(r=0;r<rows;r++){
                if(r==0){
                    for(c=0;c<cols;c++){
                        if(c==0) continue;
                        dx+=pcCenterSlideShifts[c-1].shiftL[0];
                        dy+=pcCenterSlideShifts[c-1].shiftL[1];
                        dz+=pcCenterSlideShifts[c-1].shiftL[2];
                        zs[c]=dz;
                        XYshift[c][0]=dx;
                        XYshift[c][1]=dy;
                        if(dz<dzn){
                            in=c;
                            dzn=dz;
                        }
                    }
                    continue;
                }
                for(c=0;c<cols;c++){
                    i=r*rows+c;
                    dxt=XYshift[i-cols][0]+pcCenterSlideShifts[i-cols].shiftB[0];
                    dyt=XYshift[i-cols][1]+pcCenterSlideShifts[i-cols].shiftB[1];
                    dzt=zs[i-cols]+pcCenterSlideShifts[i-cols].shiftB[2];
                    if(c==0){
                        dz=dzt;
                        dx=dxt;
                        dy=dyt;
                    }else{
                        dx+=pcCenterSlideShifts[i-1].shiftL[0];
                        dy+=pcCenterSlideShifts[i-1].shiftL[1];
                        dz+=pcCenterSlideShifts[i-1].shiftL[2];
//                        if(dz!=dzt) IJ.error("Z inconsistency");
                    }
                    zs[i]=dz;
                        XYshift[i][0]=dx;
                        XYshift[i][1]=dy;
                    if(dz<dzn){
                        in=i;
                        dzn=dz;
                    }                    
                }
//            }
            for(i=0;i<len;i++){
                Zshift[i]=zs[i]-dzn;
            }
        }
    }
    void buildStichedImage(){
        int i,len=rows*cols,z,j,pixel,c,r,x0,y0,dx,dy,xt,yt,x,y,w0=w/cols,h0=h/rows,xtt,ytt;
        int[][]pixels,pixelst;
        sPixels=new int[N][h][w];
        int[] zs=Zshift;
        for(i=0;i<N;i++){
            pixelst=sPixels[i];
            for(j=0;j<len;j++){
                z=i+zs[j];
                if(z<0||z>=N) continue;
                pixels=this.pixels[z];
                r=j/cols;
                c=j-r*cols;
                x0=c*w0;
                y0=r*h0;
                dx=XYshift[j][0];
                dy=XYshift[j][1];
                for(y=0;y<h0;y++){
                    yt=y0+y;
                    ytt=yt+dy;
                    if(ytt<0||ytt>=h) continue;
                    for(x=0;x<w0;x++){
                        xt=x0+x;
                        xtt=xt+dx;
                        if(xtt<0||xtt>=h) continue;
                        pixel=pixels[yt][xt];
                        if(pixel>pixelst[ytt][xtt])pixelst[ytt][xtt]=pixel;
                    }
                }
            }
        }
        implStiched=CommonMethods.getNewImage(w, h, N, impl.getType());
        CommonMethods.setPixels(implStiched, sPixels);
        implStiched.setTitle("StichedImage");
        implStiched.show();
    }
}
