/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import java.util.ArrayList;
import ij.ImagePlus;
import utilities.CommonMethods;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.Histogram;
import java.awt.Point;
import utilities.statistics.MeanSemHandler;
import java.util.Formatter;
import utilities.io.PrintAssist;
import java.awt.Color;
import ImageAnalysis.ContourFollower;
import utilities.Geometry.ImageShapes.ImageShape;

/**
 *
 * @author Taihao
 */
public class IPObjectTrack {
    ArrayList <intRange> transitionRanges;
    ArrayList <IntensityPeakObject> IPOs;
    IPObjectTrack preIPOT, postIPOT;
    ArrayList<IntensityPeakObjectHandler> IPOHs;
    int tcx,tcy,trackLength;//the trajectory center coordinate. The average position of all peaks of the objects in the track.
    int firstFrame, lastFrame;
    MeanSemHandler connectionDistMS,straydistMs,secondDistMs,aveMS,ave_RWAMS;
    int rws; //running window size
    ArrayList<Integer> aveLxPositions,ave_RWALxPositions;
    ArrayList<Integer> aveLnPositions,ave_RWALnPositions;
    ArrayList<Double> connectionDists, secondDists,strayDists;//connectionDist: the distance between the peak position and that of the following IPO of the track. secondDist: The closest distance
        //to the peak position of the objects that are no included in the track, the closest in the preceding, current and following frames. 
    ArrayList<Double> aves,aves_RWA,avesLN,avesLN_RWA,avesLX,avesLX_RWA;
    Histogram aveHist, ave_RWAHist,aveLNHist, aveLN_RWAHist,aveLXHist, aveLX_RWAHist;
    boolean shortTrack,bToDisplay;
    intRange xRange,yRange;
    int m_nTrackType;
    int m_nTrackIndex;
    int m_nAbfChannels;
    int m_nBundleSize;
    int m_nBundleIndex;
    int m_nTrackExportMode=0;

    public IPObjectTrack(){
        rws=2;
        m_nAbfChannels=11;
        IPOs=new ArrayList();
        preIPOT=null;
        postIPOT=null;
        bToDisplay=true;
        m_nBundleIndex=-1;
    }
    
    public IPObjectTrack(ArrayList <IntensityPeakObject> IPOs){
        this();
        this.IPOs=IPOs;
        m_nTrackIndex=IPOs.get(0).TrackIndex;
    }
    public void addIPO(IntensityPeakObject IPO){
        int len=IPOs.size();
        IntensityPeakObject IPO0=IPOs.get(len-1);
        IPO.TrackIndex=IPO0.TrackIndex;
        IPO.preIPO=IPO0;
        IPO0.postIPO=IPO;
        IPOs.add(IPO);
    }
    public IPObjectTrack(ArrayList <IntensityPeakObject> IPOs,int trackIndex){
        this(IPOs);
        m_nTrackIndex=trackIndex;
        int len=IPOs.size();
        for(int i=0;i<len;i++){
            this.IPOs.get(i).TrackIndex=trackIndex;
        }
    }
    int computeParameters(){
        xRange=new intRange();
        yRange=new intRange();
        shortTrack=false;
        int size=IPOs.size();
        if(size==0){
            size=size;
        }
        trackLength=size;
        IntensityPeakObject IPO0,IPO;
        int i,j,k;
        double cdist,sdist,sdist1,sdist2;
        IPO0=IPOs.get(0);
        firstFrame=IPO0.t;
        IPO=IPOs.get(size-1);
        lastFrame=IPO.t;
        tcx=IPO0.cx;
        tcy=IPO0.cy;
        xRange.expandRange(tcx);
        yRange.expandRange(tcy);
        connectionDists=new ArrayList<Double>();
        secondDists=new ArrayList<Double>();
        aves=new ArrayList<Double>();

        aves.add(IPO0.ave);
        connectionDists.add(0.);
        secondDists.add(0.);
        int x,y;

        for(i=1;i<size;i++){
            IPO=IPOs.get(i);
            x=IPO.cx;
            y=IPO.cy;
            tcx+=x;
            tcy+=y;
            xRange.expandRange(x);
            yRange.expandRange(y);

            cdist=IPO.dist(IPO0);

            if(IPO0.postIPOs.size()>1){
                sdist1=IPO0.dist(IPO0.postIPOs.get(1));
            }else{
                sdist1=9999.;
            }
            if(IPO.preIPOs.size()>1){
                sdist2=IPO.dist(IPO.preIPOs.get(1));
            }else{
                sdist2=9999.;
            }
            sdist=Math.min(sdist1, sdist2);
            connectionDists.add(cdist);
            secondDists.add(sdist);
            if(i==1)secondDists.set(1, sdist);
            aves.add(IPO.ave);
            IPO0=IPO;
        }
        tcx/=size;
        tcy/=size;
        if(trackLength<=2*rws+1)shortTrack=true;
        strayDists=new ArrayList<Double>();
        if(!shortTrack){
            aves_RWA=CommonMethods.runningWindowAverage(aves, rws);
            finishingRWAs();
        }
        double ave;
        for(i=0;i<size;i++){
            IPO=IPOs.get(i);
            strayDists.add(IPO.dist(tcx,tcy));
        }
        aveLxPositions=new ArrayList<Integer>();
        ave_RWALxPositions=new ArrayList<Integer>();
        aveLnPositions=new ArrayList<Integer>();
        ave_RWALnPositions=new ArrayList<Integer>();
        avesLN=new ArrayList<Double>();
        avesLX=new ArrayList<Double>();
        avesLN_RWA=new ArrayList<Double>();
        avesLX_RWA=new ArrayList<Double>();
        if(!shortTrack) CommonMethods.LocalExtrema(aves, avesLN, avesLX,0);
        if(!shortTrack) CommonMethods.LocalExtrema(aves_RWA, avesLN_RWA, avesLX_RWA,0);
        if(!shortTrack) CommonMethods.LocalExtrema(aves, aveLnPositions, aveLxPositions);
        if(!shortTrack) CommonMethods.LocalExtrema(aves_RWA, ave_RWALnPositions, ave_RWALxPositions);
        aveHist=new Histogram(aves);
        if(!shortTrack)ave_RWAHist=new Histogram(aves_RWA);
        if(!shortTrack)aveLNHist=new Histogram(avesLN);
        if(!shortTrack)aveLXHist=new Histogram(avesLX);
        if(!shortTrack)aveLN_RWAHist=new Histogram(avesLN_RWA);
        if(!shortTrack)aveLX_RWAHist=new Histogram(avesLX_RWA);
        connectionDistMS=new MeanSemHandler(connectionDists);
        straydistMs=new MeanSemHandler(strayDists);
        secondDistMs=new MeanSemHandler(secondDists);
        aveMS=new MeanSemHandler(aves);
        if(!shortTrack)detectTransitions();
        if(!shortTrack)ave_RWAMS=new MeanSemHandler(aves_RWA);
        return 1;
    }
    public void finishingRWAs(){
        int i,size=aves.size();
        for(i=0;i<rws;i++){
            aves_RWA.add(0,aves.get(rws-1-i));
            aves_RWA.add(aves.get(size-1));
        }
    }
    public int getLength(){
        return IPOs.size();
    }
    public IntensityPeakObject getIPO(int index){
        return IPOs.get(index);
    }
    public void markTrack(ImagePlus impl, Color ct, int perIndex){
        markTrack(impl,ct,ct,perIndex);
    }
    public int markTrack(ImagePlus impl, Color cb, Color ct, int perIndex){
        if(!bToDisplay) return -1;
        int len=IPOs.size();
        int i,slice;
        int x,y,xi,xf,yi,yf,radius,w=impl.getWidth(),h=impl.getHeight();
        IntensityPeakObject IPO;
        int slice0=0,is;
        int pixel,b;
        ArrayList<Point> contour;
        Color ci;//color for the percentile index
        intRange xRanget=new intRange(), yRanget=new intRange();
        double par;
        ImageShape shape;
        int z;
        Point[] conners=new Point[4];

        for(i=0;i<len;i++){
            IPO=IPOs.get(i);
            x=IPO.getCX();
            y=IPO.getCY();
            z=IPO.getCZ();
            contour=IPO.getContour();
            ContourFollower.getXYRanges(contour, xRanget, yRanget);
            slice=IPO.getCZ()+1;
            if(i==0)slice0=slice;
            for(is=slice0;is<=slice0;is++){
                impl.setSlice(slice);
                radius=IPO.radius;
                xi=x-radius;
                if(xi<0)xi=0;
                xf=x+radius;
                if(xf>w-1) xf=w-1;

                yi=y-radius;
                if(yi<0)yi=0;
                yf=y+radius;
                if(yf>h-1) yf=h-1;
                impl.setColor(ct);
                b=255-IPO.percentileIndex*30;
                if(b<0) b=0;
                if(b>255) b=0;
                impl.setColor(cb);
                ci=new Color(0,0,b);
                if(xRanget.getRange()>=yRanget.getRange())
                    ContourFollower.markContourX(impl, contour, cb,ct,ci,xRanget);
                else
                    ContourFollower.markContourX(impl, contour, cb,ct,ci,xRanget);

                slice0=slice+1;
            }
            shape=IPO.getEnclosingShape();
            conners=shape.getConnerPoints();//return leftTop, rightTop, leftBottom and rightBottom conners

            par=IPO.pixelHeightC;
            pixel=CommonMethods.getRGBPixel((int)par);
            CommonMethods.drawDot(impl, conners[0], pixel);

            par=IPO.getEnclosingShape().getArea();
            pixel=CommonMethods.getRGBPixel((int)par);
            CommonMethods.drawDot(impl, conners[1], pixel);

            par=IPO.getBundleSize();
            pixel=CommonMethods.getRGBPixel((int)par);
            CommonMethods.drawDot(impl, conners[2], pixel);

            pixel=CommonMethods.getRGBPixel(m_nTrackIndex);
            CommonMethods.drawDot(impl, conners[3], pixel);
        }
        return 1;
    }
    public void detectTransitions(){
        int ws=5;
        double cutoff=20,cutoff1=0.;
        transitionRanges=new ArrayList<intRange>();
        ArrayList<Double> deltaPeak=getDeltaPeakNX(ws);
        int i,size=deltaPeak.size(),pl,pr;
        double dp=0.;
        for(i=0;i<size;i++){
            dp=deltaPeak.get(i);
            if(dp>cutoff){
                pl=ave_RWALxPositions.get(i);
                pr=ave_RWALnPositions.get(i);
                transitionRanges.add(new intRange(pl,pr));
            }
        }
    }
    
    public void detectTransitions1(){
        int ws=5;
        double cutoff=20,cutoff1=0.;

        transitionRanges=new ArrayList<intRange>();
        ArrayList<Double> deltaPeak=getDeltaPeakNX(ws);
        ArrayList<Double> deltaPeak1=getDeltaPeakNX1(ws);
        int i,size=deltaPeak.size(),size1=deltaPeak1.size(),pl,pr;
        double dp=0.;
        for(i=0;i<size1;i++){
            if(i==0){
                dp=deltaPeak.get(i);
                if(dp>cutoff){
                    pl=ave_RWALxPositions.get(i);
                    pr=ave_RWALnPositions.get(i);
                    transitionRanges.add(new intRange(pl,pr));
                }
            }
            dp=deltaPeak1.get(i);
            if(dp>cutoff1){
                pl=ave_RWALxPositions.get(i+1);
                pr=ave_RWALnPositions.get(i+1);
                transitionRanges.add(new intRange(pl,pr));
            }
        }
    }
    ArrayList<Double>  dlp;
    public ArrayList<Double> getDeltaPeakNX(int ws){//smallest local maximum of left side vs the largest local maximum of right side, and checking at each local maximum point.
        dlp=new ArrayList<Double> ();
        int i,j,size=ave_RWALxPositions.size();
        double delta,ln,rx,v;
        int li,rf,p;
        for(i=0;i<size-1;i++){
            li=Math.max(0, i-ws+1);
            rf=Math.min(size-1, i+ws);
            ln=256;
            rx=-256;
            for(j=i;j<=i;j++){//not looking at
                p=ave_RWALxPositions.get(j);
                v=aves_RWA.get(p);
                if(v<ln) ln=v;
            }
            for(j=i+1;j<=rf;j++){
                p=ave_RWALxPositions.get(j);
                v=aves_RWA.get(p);
                if(v>rx) rx=v;
            }
            delta=ln-rx;
            dlp.add(delta);
        }
        return dlp;
    }
    public ArrayList<Double> getDeltaPeakNX1(int ws){//smallest local minimum of left side vs the largest local maximum of right side, checking at each local minimum.
        dlp=new ArrayList<Double> ();
        int i,j,size=ave_RWALnPositions.size(),size1=ave_RWALnPositions.size();
        double delta,ln,rx,v;
        int li,rf,p;
        for(i=0;i<size-1;i++){
            li=Math.max(0, i-ws+1);
            rf=Math.min(size1-1, i+ws);
            ln=256;
            rx=-256;
            if(i+1>rf) continue;
            for(j=li;j<=i;j++){
                p=ave_RWALnPositions.get(j);
                v=aves_RWA.get(p);
                if(v<ln) ln=v;
            }
            for(j=i+1;j<=rf;j++){
                p=ave_RWALxPositions.get(j);
                v=aves_RWA.get(p);
                if(v>rx) rx=v;
            }
            delta=ln-rx;
            dlp.add(delta);
        }
        return dlp;
    }
    public ArrayList<Double> getTransitionLine(int ws){
        ArrayList<Double> tl=new ArrayList<Double>();
        int i,size=transitionRanges.size(),pl,pr,li,rf,size0=aves_RWA.size(),p0=0,j,pm;
        intRange ir;
        double dl=0,dr=0;
        for(i=0;i<size0;i++){
            tl.add(-10.);
        }
        for(i=0;i<size;i++){
            ir=transitionRanges.get(i);
            pl=ir.getMin();
            pr=ir.getMax();
            pm=(p0+pl)/2;
            li=Math.max(0,pl-ws);
            li=Math.max(li,pm);
            rf=Math.min(size0-1, pr+ws);
//            dl=CommonMethods.getMean(aves_RWA, li, pl);
//            dr=CommonMethods.getMean(aves_RWA, pr, rf);
            dl=aves_RWA.get(pl);
            dr=aves_RWA.get(pr);
            for(j=li;j<pl;j++){
                tl.set(j,dl);
            }
            for(j=pl;j<=pr;j++){
                tl.set(j,CommonMethods.getLinearIntoplation(pl, dl, pr, dr, j));
            }
            for(j=pr+1;j<=rf;j++){
                tl.set(j,dr);
            }
            p0=pr;
        }
        return tl;
    }
    public int exportTrackToAbf(float pfData[], int indexT, int positionI){
        int zI=IPOHs.get(0).z,index;
        int position=positionI;
        int num=IPOs.size();
        int i,zMax=IPOHs.size()-1;
        int cx0,cy0;
        IntensityPeakObject IPO,IPO0;
        double cutoff;
        IPO0=IPOs.get(0);
        cx0=IPO0.cx;
        cy0=IPO0.cy;
        position=exportSpacerToAbf(pfData,indexT,position,m_nAbfChannels);
        position=exportDefaultIPOTraceToAbf(pfData,position,IPO0.cx,IPO0.cy,zI,IPO0.cz-2-zI,m_nAbfChannels);
        position=exportIPOTMarkToAbf(pfData,position,IPO0,IPO0.cz-1,m_nAbfChannels);
        for(i=0;i<num;i++){
           IPO=IPOs.get(i);
           index=IPO.cz-zI;
           cutoff=IPOHs.get(index).getPixelHeightCutoff();
           position=exportDefaultIPOTraceToAbf(pfData,position,IPO0.cx,IPO0.cy,IPO0.cz+1-zI,IPO.cz-1-zI,m_nAbfChannels);
           position=exportIPOToAbf(pfData,position,IPO,cx0,cy0,m_nAbfChannels,cutoff);
           IPO0=IPO;
        }
        if(IPO0.cz<zMax) position=exportIPOTMarkToAbf(pfData,position,IPO0,IPO0.cz+1-zI,m_nAbfChannels);
        
        position=exportDefaultIPOTraceToAbf(pfData,position,IPO0.cx,IPO0.cy,IPO0.cz+2-zI,zMax,m_nAbfChannels);
        return position;
    }
    public int exportTrackToAbfChannel(float pfData[], int positionI){
        //positionI is the index of the first floating point data to be stored in pfData[].
        //returning the index ringth after that of the last element of pfData stored in pfData[] by the method.
        int position=positionI;
        int zMax=IPOHs.size()-1;
        int length=pfData.length;
        int num=IPOs.size();
        int i;
        int cx0,cy0;
        float fv=-1000;
        if(m_nTrackExportMode==1)fv=-100;
        IntensityPeakObject IPO,IPO0;
        IPO0=IPOs.get(0);
        cx0=IPO0.cx;
        cy0=IPO0.cy;
        position=exportSpacerToAbfChannel(pfData,position);
        position=exportDefaultIPOTraceToAbfChannel(pfData,position,IPO0.cx,IPO0.cy,0,IPO0.cz-2);
        pfData[position]=fv;
        position++;
        for(i=0;i<num;i++){
           IPO=IPOs.get(i);
           position=exportDefaultIPOTraceToAbfChannel(pfData,position,IPO0.cx,IPO0.cy,IPO0.cz+1,IPO.cz-1);
           position=exportIPOToAbfChannel(pfData,position,IPO,cx0,cy0);
           IPO0=IPO;
        }
        if(IPO0.cz<zMax){
            if(position<length){
                pfData[position]=fv;
                position++;
            }else{
                position+=0;
            }
        }
        position=exportDefaultIPOTraceToAbfChannel(pfData,position,IPO0.cx,IPO0.cy,IPO0.cz+2,zMax);
        return position;
    }

    int getTrackAbfLength(int numChannels){//number of data points to write to pfData of an abf file
        return numChannels*(IPOHs.size()+getSpacerLength());
    }
    int getTrackAbfLengthChannel(){//number of data points to write to pfData of an abf file
        return IPOHs.size()+getSpacerLengthChannel();
    }
    public int getNumAbfChannels(){
        return m_nAbfChannels;
    }
    int exportIPOToAbf(float[]pfData,int positionI, IntensityPeakObject IPO, int cx0, int cy0,int nAbfChannels, double cutoff){
        int position=positionI,i;
        pfData[position]=(float)IPO.pixelHeight0;
        position++;
        pfData[position]=(float)IPO.pixelHeight;
        position++;
        pfData[position]=(float)(IPO.peakPixel_ave-IPO.sur_quantile);
        position++;
        pfData[position]=(float)IPO.sur_quantile;
        position++;
        pfData[position]=(float)IPO.getBackgroundPixel();
        position++;
        pfData[position]=IPO.cx-cx0;
        position++;
        pfData[position]=IPO.cy-cy0;
        position++;
        int z=IPO.cz;
        pfData[position]=z;
        position++;
        pfData[position]=(float)IPO.pValue;
        position++;
        pfData[position]=(float)IPO.currentIPOs.size();
        position++;
        pfData[position]=(int)cutoff;
        position++;
        for(i=11;i<nAbfChannels;i++){
            pfData[position]=0;
            position++;
        }
        return position;
    }
    public static void setADCChannelNamesAndUnits(char[][] channelNames, char[][] units){
        int len=channelNames.length;
        String[] names=new String[len];
        String[] sUnits=new String[len];
        int i,j;
        for(i=0;i<len;i++){
            names[i]="";
            sUnits[i]="";
        }
        int used=0;
        names[0]="h0";
        used++;
        names[1]="h";
        used++;
        names[2]="peakAve0";
        used++;
        names[3]="sur";
        used++;
        names[4]="bkgrnd";
        used++;
        names[5]="cx";
        used++;
        names[6]="cy";
        used++;
        names[7]="cz";
        used++;
        names[8]="log(p)";
        used++;
        names[9]="nbhrs";
        used++;
        names[10]="cutoff";
        int lenc=channelNames[0].length,lenu=units[0].length,len1;
        for(i=0;i<len;i++){
            len1=names[i].length();
            for(j=0;j<lenc;j++){
                if(j<len1){
                    channelNames[i][j]=names[i].charAt(j);
                }else{
                    channelNames[i][j]=0;
                }
                if(j<lenu) units[i][j]=0;
            }
        }
    }
    int exportIPOTMarkToAbf(float[]pfData,int positionI, IntensityPeakObject IPO, int z, int nAbfChannels){
        int position=positionI,i;
        float fV;
        
        if(m_nTrackExportMode==0)
            fV=-1000;
        else
            fV=-100;

        pfData[position]=fV;
        position++;
        pfData[position]=fV;
        position++;
        pfData[position]=fV;
        position++;
        pfData[position]=fV;
        position++;
        pfData[position]=IPO.cx;
        position++;
        pfData[position]=IPO.cy;
        position++;
        pfData[position]=z;
        position++;
        for(i=8;i<=nAbfChannels;i++){
            pfData[position]=fV;
            position++;
        }
        return position;
    }
    int exportIPOToAbfChannel(float[]pfData,int positionI, IntensityPeakObject IPO, int cx0, int cy0){
        //export pixelHeight only
        int position=positionI,i;
        if(m_nTrackExportMode==0){
            pfData[position]=(float)IPO.pixelHeight;
        }else{
            pfData[position]=(float)IPO.pixelHeight0;
        }
        position++;
        return position;
    }

    public int getTrackType(){
//        if(m_nTrackType<0)
        calTrackType();
        return m_nTrackType;
    }

    void calTrackType(){
       int num=IPOs.size();
       IntensityPeakObject IPOi=IPOs.get(0),IPOf=IPOs.get(num-1);
       ArrayList<IntensityPeakObject> preIPOs=IPOi.preIPOs, postIPOs=IPOf.postIPOs;
       boolean bOptimalHead,bOptimalEnd;
       int len,ti;

       len=preIPOs.size();
       if(len>0){
           ti=preIPOs.get(0).TrackIndex;
           if(ti<0)
               bOptimalHead=true;
           else//the optimal preceding IPO belongs to other track
               bOptimalHead=false;
       }else{
           bOptimalHead=true;
       }

       len=postIPOs.size();
       if(len>0){
           ti=postIPOs.get(0).TrackIndex;
           if(ti<0)
               bOptimalEnd=true;
           else//the optimal following IPO belongs to other track
               bOptimalEnd=false;
       }else{
           bOptimalEnd=true;
       }

       if(bOptimalHead){
           if(bOptimalEnd)
               m_nTrackType=0;
           else
               m_nTrackType=1;
       }else{
           if(bOptimalEnd)
               m_nTrackType=2;
           else
               m_nTrackType=3;
       }
    }

    
    void exportSpacer(Formatter fm, int indexT){
        IntensityPeakObject IPOi,IPOf;
        IPOi=IPOs.get(0);
        int size=IPOs.size();
        IPOf=IPOs.get(size-1);

        int z=IPOi.cz;
        int len=10;
        int n1=indexT/100,n2=indexT%100;
        int i;
        for(i=0;i<len;i++){
           PrintAssist.printNumber(fm, 0, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n1, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n2, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
        }
        n1=z/100;
        n2=z%100;
        for(i=0;i<len;i++){
           PrintAssist.printNumber(fm, 0, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n1, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n2, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
        }
        z=IPOf.cz;
        n1=z/100;
        n2=z%100;
        for(i=0;i<len;i++){
           PrintAssist.printNumber(fm, 0, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n1, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n2, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
        }
        n1=IPOi.cx;
        n2=IPOi.cy;
        for(i=0;i<len;i++){
           PrintAssist.printNumber(fm, 0, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n1, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n2, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
        }
        n1=0;
        n2=IPOi.peakPixel;
        for(i=0;i<len;i++){
           PrintAssist.printNumber(fm, 0, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n1, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           PrintAssist.printNumber(fm, n2, 8, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
        }
    }
    int exportSpacerToAbf(float[] pfData, int indexT, int positionI, int numChannels){
        //numChannels the number of channels in Abf, indexT the track Index,
        //positionI the first position in pfData
        int position=positionI;
        IntensityPeakObject IPOi,IPOf;
        IPOi=IPOs.get(0);
        int size=IPOs.size();
        IPOf=IPOs.get(size-1);

        int len=10;
        float fV;
        if(m_nTrackExportMode==0){
            fV=-1000;
        }else{
            fV=-100;
        }
        int i,j;
        for(i=0;i<len;i++){
            pfData[position]=fV;
            position++;
            pfData[position]=indexT;
            position++;
            for(j=2;j<numChannels;j++){
                pfData[position]=0;
                position++;
            }
        }//export the track index, using 10*numChannels points. export to the second and third channels. indexT=Y2*100+Y3

        for(i=0;i<len;i++){
            pfData[position]=0;
            position++;
            pfData[position]=IPOi.cx;
            position++;
            pfData[position]=IPOi.cy;
            position++;
            pfData[position]=IPOi.cz;
            position++;
            pfData[position]=IPOi.peakPixel;
            position++;
            for(j=5;j<numChannels;j++){
                pfData[position]=0;
                position++;
            }
        }//export the z of the first IPO, using 10*numChannels points. export to the second and third channels. z=Y2*100+Y3
        //export the x and y of the firt IPO to the fourth and fifth channels.
        for(i=0;i<len;i++){
            pfData[position]=0;
            position++;
            pfData[position]=IPOf.cx;
            position++;
            pfData[position]=IPOf.cy;
            position++;
            pfData[position]=IPOf.cz;
            position++;
            pfData[position]=IPOf.peakPixel;
            position++;
            for(j=5;j<numChannels;j++){
                pfData[position]=0;
                position++;
            }
        }
        return position;
    }
    int getSpacerLength(){//assuming spacer length is not larger than 2000
        int position=0;
        float[] pfData=new float[2000];
        position=exportSpacerToAbf(pfData,position,0,m_nAbfChannels);
        return position/m_nAbfChannels;
    }
    int exportSpacerToAbfChannel(float[] pfData, int positionI){
        //numChannels the number of channels in Abf, indexT the track Index,
        //positionI the first position in pfData

        //The space beginning with 5 points of -50
        //The next five points indicates x coordinate of the first identified IPO of the track
        //The next five points indicates y coordinate
        //The next five points indicates z coordinate
        //The next five points indicates the peakPixel of the first IPO

        //The next five points indicates x coordinate of the last identified IPO of the track
        //The next five points indicates y coordinate
        //The next five points indicates z coordinate
        //The next five points indicates the peakPixel of the first IPO
        //The space beginning with 5 points of -50
        int position=positionI;
        IntensityPeakObject IPOi,IPOf;
        IPOi=IPOs.get(0);
        int size=IPOs.size();
        IPOf=IPOs.get(size-1);

        float fV;
        if(m_nTrackExportMode==0){
            fV=-1000;
        }else{
            fV=-100;
        }

        int len=5;
        int n0=-50;
        int i,j;
        for(i=0;i<len;i++){
            pfData[position]=fV;
            position++;
        }//mark the beginning of the track

        for(i=0;i<len;i++){
            pfData[position]=IPOi.TrackIndex;
            position++;
        }
        for(i=0;i<len;i++){
            pfData[position]=IPOi.cx;
            position++;
        }
        for(i=0;i<len;i++){
            pfData[position]=IPOi.cy;
            position++;
        }
        for(i=0;i<len;i++){
            pfData[position]=IPOi.cz;
            position++;
        }
        n0=IPOi.peakPixel;
        for(i=0;i<len;i++){
            pfData[position]=IPOi.peakPixel;
            position++;
        }
        pfData[position]=-fV;
        position++;
        for(i=0;i<len;i++){
            pfData[position]=IPOf.cx;
            position++;
        }
        for(i=0;i<len;i++){
            pfData[position]=IPOf.cy;
            position++;
        }
        for(i=0;i<len;i++){
            pfData[position]=IPOf.cz;
            position++;
        }
        for(i=0;i<len;i++){
            pfData[position]=IPOf.peakPixel;
            position++;
        }
        pfData[position]=fV;
        position++;
        return position;
    }

    int getSpacerLengthChannel(){
        int position=0;
        float[] pfData=new float[1000];
        position=exportSpacerToAbfChannel(pfData,position);
        return position;
    }

    public void exportTrack(Formatter fm){
       int num=IPOs.size();
       int i;
       IntensityPeakObject IPO,IPO0;
       IPO0=IPOs.get(0);
       PrintAssist.printString(fm, "FrameNumber",15);
       PrintAssist.printString(fm, "cx",15);
       PrintAssist.printString(fm, "cy",15);
       PrintAssist.printString(fm, "pixelHeight",15);
       PrintAssist.printString(fm, "peakPixel",15);
       PrintAssist.printString(fm, "defaultHeight",15);
       PrintAssist.printString(fm, "defualtPixel",15);
       PrintAssist.printString(fm, PrintAssist.newline, 1);
       int cx0=IPO0.cx,cy0=IPO0.cy;
       exportDefaultIPOTrack(fm,IPO0.cx,IPO0.cy,cx0,cy0,0,IPO0.cz-1);
       for(i=0;i<num;i++){
           IPO=IPOs.get(i);
           exportDefaultIPOTrack(fm,IPO0.cx,IPO0.cy,cx0,cy0,IPO0.cz+1,IPO.cz-1);
           PrintAssist.printNumber(fm, IPO.cz, 15, 0);
           PrintAssist.printNumber(fm, IPO.cx, 15, 0);
           PrintAssist.printNumber(fm, IPO.cy, 15, 0);
           PrintAssist.printNumber(fm, IPO.pixelHeight, 15, 0);
           PrintAssist.printNumber(fm, IPO.peakPixel, 15, 0);
           PrintAssist.printNumber(fm, IPO.getPixelHeight(cx0, cy0), 15, 0);
           PrintAssist.printNumber(fm, IPO.getPixel(cx0, cy0), 15, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
           IPO0=IPO;
       }
       exportDefaultIPOTrack(fm,IPO0.cx,IPO0.cy,cx0,cy0,IPO0.cz+1,IPOHs.size()-1);
    }


    public int exportDefaultIPOTraceToAbf(float []pfData,int positionI, int x, int y, int Ii, int If,int numChannels){
//        if(!goodTrack()) return -1;
        int position=positionI;
        IntensityPeakObjectHandler IPOH;
        int w=IPOHs.get(0).w,h=IPOHs.get(0).h;
        int[][]pixels,pixelStamp;
        int i,j;
        IntensityPeakObject IPO;
        ArrayList<IntensityPeakObject> IPOs0=new ArrayList(),IPOs=new ArrayList(),IPOst=new ArrayList();
        ArrayList<Double> drs=new ArrayList();
        int searchingDist=7;
        double pixelHeight=0;
        int nPercentileIndex=0;
        int numIPOs=0,numIPOs0=0;
        double cutoff=0;
        double ph0,ph,phC,phC90p,peakAve,surQuantile;

        int nArea;
        double dist;
        double background;
        IntensityPeakObject IPO1;
        double[]pdSigSur=new double[2];
        int neighbors;

        for(i=Ii;i<=If;i++){
           IPOH=IPOHs.get(i);
           cutoff=IPOH.getPixelHeightCutoff();
           searchingDist=IPOH.searchingDist;
           IPOs.clear();
           drs.clear();
//           IPOH.getClosestIPOs(searchingDist, 5, x, y, 0, IPOs, drs);
           IPOH.getClosestIPOs(searchingDist, 5, x, y, 0, IPOs0, drs);
           IPOH.getClosestBackgroundIPOs(searchingDist, 5, x, y, 0, IPOs, drs);

           IPOH.getClosestIPOs(searchingDist, 10, x, y, 0, IPOst, drs);
           neighbors=IPOst.size();
           IPOH.getClosestBackgroundIPOs(searchingDist, 10, x, y, 0, IPOs, drs);
           neighbors+=IPOst.size();

           int nTrackIndex=-1;

           numIPOs=IPOs.size();
           numIPOs0=IPOs0.size();

           nArea=-1;
           dist=100;
           background=-1;

           if(numIPOs>0){//picking background objects
               IPO=IPOs.get(0);
               x=IPO.cx;
               y=IPO.cy;
               nTrackIndex=IPO.TrackIndex;
               if(m_nTrackExportMode==0){
                   pixelHeight=IPO.pixelHeight;
               }else{
                    pixelHeight=IPO.pixelHeight0;
               }
               nPercentileIndex=IPO.percentileIndex;
               ph=IPO.pixelHeight;
               ph0=IPO.pixelHeight0;
               phC=IPO.pixelHeightC;
               phC90p=IPO.pixelHeightC90p;
               peakAve=IPO.peakPixel_ave;
               surQuantile=IPO.sur_quantile;

               nArea=IPO.getArea();
               dist=IPO.getDistToNeighbor();
               background=IPO.getBackgroundPixel();

           }else if(numIPOs0>0){//a partical object
               IPO=IPOs0.get(0);
               x=IPO.cx;
               y=IPO.cy;
               if(m_nTrackExportMode==0){
                   pixelHeight=IPO.pixelHeight;
               }else{
                    pixelHeight=IPO.pixelHeight0;
               }
               nTrackIndex=IPO.TrackIndex;
               nPercentileIndex=IPO.percentileIndex;
               ph=IPO.pixelHeight;
               ph0=IPO.pixelHeight0;
               phC=IPO.pixelHeightC;
               phC90p=IPO.pixelHeightC90p;

               nArea=IPO.getArea();
               dist=IPO.getDistToNeighbor();
               background=IPO.getBackgroundPixel();
               peakAve=IPO.peakPixel_ave;
               surQuantile=IPO.sur_quantile;

           }else{
               pixels=IPOH.pixels;
               if(m_nTrackExportMode==0){
                   pixelHeight=IPOH.getPixelHeight(x, y,pdSigSur);
               }else{
                   pixelHeight=IPOH.getPixelHeight0(x, y);
               }
               nPercentileIndex=IPOH.getPercentileIndex(pixelHeight);
               ph=pixelHeight;
               ph0=-1;
               phC=0;
               phC90p=0;
               nTrackIndex=-1;
               background=pdSigSur[1];
               peakAve=IPOH.getPeakPixelAve(x, y);
               surQuantile=IPOH.getSurQuantile(x, y);
           }

           if(position>=pfData.length){
               i=i;
           }
           pfData[position]=(float)ph0;
           position++;
           pfData[position]=(float)ph;
           position++;
           pfData[position]=(float)(peakAve-surQuantile);
           position++;
           pfData[position]=(float)surQuantile;
           position++;
           pfData[position]=(float)background;
           position++;
           pfData[position]=x;
           position++;
           pfData[position]=y;
           position++;
           pfData[position]=i;
           position++;
           pfData[position]=nTrackIndex;
           position++;
           pfData[position]=(float)neighbors;
           position++;
           pfData[position]=(int)cutoff;
           position++;
           for(j=11;j<numChannels;j++){
               pfData[position]=0;
               position++;
           }
        }
        return position;
    }
    public int exportDefaultIPOTraceToAbfChannel(float []pfData,int positionI, int x, int y, int Ii, int If){
//        if(!goodTrack()) return -1;
        //searching for the closest IPO and export its pixel height. it exports a default height calulated
        //by IPOH.getPixelHeight(x,y) if no IPOs found within the searching distance.
        int position=positionI;
        IntensityPeakObjectHandler IPOH;
        ArrayList <Point> localMaxima=new ArrayList();
        ArrayList <Double> pixelHeights=new ArrayList();
        int size;
        int w=IPOHs.get(0).w,h=IPOHs.get(0).h;
        int[][]pixels,pixelStamp;
        int i,j;
        IntensityPeakObject IPO;
        ArrayList<IntensityPeakObject> IPOs=new ArrayList();
        ArrayList<Double> drs=new ArrayList();
        int searchingDist=7;
        double pixelHeight=0;
        int numIPOs=0;
        double pdSigSur[]=new double[2];

        for(i=Ii;i<=If;i++){
           IPOH=IPOHs.get(i);
           searchingDist=IPOH.searchingDist;
           IPOs.clear();
           drs.clear();
//           IPOH.getClosestIPOs(searchingDist, 5, x, y, 0, IPOs, drs);
           IPOH.getClosestBackgroundIPOs(searchingDist, 5, x, y, 0, IPOs, drs);
           numIPOs=IPOs.size();
           if(numIPOs>0){
               IPO=IPOs.get(0);
               x=IPO.cx;
               y=IPO.cy;
               if(m_nTrackExportMode==0){
                   pixelHeight=IPO.pixelHeight;
               }else{
                   pixelHeight=IPO.pixelHeight0;
               }
           }else{
               pixels=IPOH.pixels;
               if(m_nTrackExportMode==0){
                   pixelHeight=IPOH.getPixelHeight(x, y,pdSigSur);
               }else{
                   pixelHeight=IPOH.getPixelHeight0(x, y);
               }
           }
           pfData[position]=(float)pixelHeight;
           position++;
        }
        return position;
    }
    public int exportDefaultIPOTrack(Formatter fm,int x0, int y0, int x1, int y1, int Ii, int If){
        if(!goodTrack()) return -1;
        IntensityPeakObjectHandler IPOH;
        int w=IPOHs.get(0).w,h=IPOHs.get(0).h;
        int[][]pixels,pixelStamp;
        int i,j;
        for(i=Ii;i<=If;i++){
           IPOH=IPOHs.get(i);
           pixels=IPOH.pixels;
           PrintAssist.printNumber(fm, i, 15, 0);
           PrintAssist.printNumber(fm, x0, 15, 0);
           PrintAssist.printNumber(fm, y0, 15, 0);
           PrintAssist.printNumber(fm, CommonMethods.getPixelHeight(pixels, w, h, x0, y0, 5), 15, 0);
           PrintAssist.printNumber(fm, pixels[y0][x0], 15, 0);
           PrintAssist.printNumber(fm, CommonMethods.getPixelHeight(pixels, w, h, x1, y1, 5), 15, 0);
           PrintAssist.printNumber(fm, pixels[y1][x1], 15, 0);
           PrintAssist.printString(fm, PrintAssist.newline, 1);
        }
        return 1;
    }
    public void setIPOHs(ArrayList <IntensityPeakObjectHandler> IPOHs){
        this.IPOHs=IPOHs;
    }
    public boolean goodTrack(){
        if(firstFrame>20) return false;
//        if((lastFrame-firstFrame) <20 )return false;
        return true;
    }
    public IntensityPeakObject getLastIPO(){
        int len=IPOs.size();
        return IPOs.get(len-1);
    }
    public void setTrackExportMode(int mode){
        m_nTrackExportMode=mode;
    }
    public int getTrackIndex(){
        return m_nTrackIndex;
    }
    public void setTrackIndex(int index){
        int len=IPOs.size();
        m_nTrackIndex=index;
        for(int i=0;i<len;i++){
            IPOs.get(i).setTrackIndex(index);
        }
    }
    public ArrayList<IntensityPeakObject> getIPOs(){
        return IPOs;
    }
    public boolean commonHead(IPObjectTrack IPOT){
        return(IPOs.get(0)==IPOT.IPOs.get(0));
    }
    public boolean commonEnd(IPObjectTrack IPOT){
        return (getLastIPO()==IPOT.getLastIPO());
    }
    public void setBundleSizeAndIndex(int size, int bundleIndex){
        int len=IPOs.size();
        m_nBundleIndex=bundleIndex;
        for(int i=0;i<len;i++){
            IPOs.get(i).setBundleSize(size);
            IPOs.get(i).m_nBundleIndex=bundleIndex;
        }
    }
    public int setDisplayability(boolean bDisplayAll, int nMinLength, int nLatestTrackHead){
        if(bDisplayAll){
            bToDisplay=true;
            return 1;
        }
        IntensityPeakObject IPO=IPOs.get(0);
        if(IPO.cz>nLatestTrackHead){
            bToDisplay=false;
            return -1;
        }
        int len=IPOs.size();
        if(len<nMinLength){
            bToDisplay=false;
            return -1;
        }
        for(int i=0;i<len;i++){
            IPO=IPOs.get(i);
            if(IPO.bTrackOpening){
                bToDisplay=true;
                return 1;
            }
        }
        bToDisplay=false;
        return -1;
    }
    public boolean toDisplay(){
        return bToDisplay;
    }
    public void fillGaps(){
        int len=IPOs.size(),zf=IPOs.get(len-1).cz,z,z0,i;
        IntensityPeakObject IPO;
        i=len-1;
        IPO=IPOs.get(i);
        z0=IPO.cz;
        for(i=len-2;i>=0;i--){
            IPO=IPOs.get(i);
            if(IPO==null){
                z0--;
                continue;
            }
            z=IPO.cz;
            for(z=z+1;z<z0;z++){
                IPOs.add(i+1,null);
            }
            z0=z;
        }
    }
    public double getTotalSiganlAtZ(int z){
        IntensityPeakObject IPO=getIPOAtZ(z);
        if(IPO==null) return 0;
        return IPO.dTotalSignal;
    }
    public IntensityPeakObject getIPOAtZ(int z){
        int zi=IPOs.get(0).cz,len=IPOs.size(),zf=IPOs.get(len-1).cz;
        if(z<zi||z>zf) return null;
        if(len<zf-zi+1) fillGaps();
        return IPOs.get(z-zi);
    }
}
