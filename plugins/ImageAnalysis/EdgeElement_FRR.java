/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import utilities.ArrayofArrays.IntPair;
import utilities.TTestTable;
import utilities.statistics.MeanSem0;
import utilities.CustomDataTypes.intRange;
import ij.ImagePlus;
import utilities.CommonMethods;
import java.util.ArrayDeque;
import java.awt.Color;
import utilities.ArrayofArrays.IntArray;
import utilities.ArrayofArrays.PixelPositionArray;
import utilities.ArrayofArrays.IntPairArray;
import utilities.ArrayofArrays.PixelPositionArray2;


/**
 *
 * @author Taihao
 */
public class EdgeElement_FRR {
    Runtime RT = Runtime.getRuntime();

    ArrayList <EdgeElementNode_E> eens;
    int[][] pixels;
    PixelPositionNode center,centero;
    int b1,b2,d1,d2,type,ws;
    int pLines,numOPs;//pLines: number of pixel lines. numOPs (left corner to right corner): number of orthogonal positions.
    int nb1,nb2,nd1,nd2;//The number of pixels in the regions
    int nb1Ext,nb2Ext,nd1Ext,nd2Ext;//The number of pixels in the regions
    int numExts;
    int numOCLPO;
    int numOCLP;//number of off-centerline pixels, computed in function buildDisplacementMatrices
    int deltaX, deltaY;//The difference in the coordinates of the same phase pixels of adjacent pixel lines.
//    int ddx[], ddy[];//Displacement array
//    int cddx[], cddy[];//cumulated displacement array
//    int ddxo[], ddyo[];//orthogonal displacement array
//    int cddxo[], cddyo[];//cumulated orthogonal displacement arry
//    int dddxo[], dddyo[];//cumulated orthogonal displacement array, one pixel from each pixel line
    int[] eD;//direction of the edge (dy,dx)
    int[] gD;//direction of the gradient (dyo, dxo)
    int connectionGap;
    double pValue,gradient,delta,pValue0,height;
    double lowCutoff,connectionCutoff,unitCutoff;
    double maxDelta;
    int[] dim_d1,dim_d2,dim_dExt1,dim_dExt2;
    IntPair[] edgeLine,edgeLineExt;
    intRange xRange,yRange,lRange;
    int numLines;
    boolean bNewPixelLine;
    PixelPositionNode ppnO,ppnT;//ppnO is always set as (0,0,0,0). ppnT is a general variable.
//    TTestTable tTable;
    MeanSem0 ms1,ms2,ms1Ext,ms2Ext;
    ArrayList <PixelPositionNode> maxDeltaLine;
    int buffer;//in order to compute the edge element at the pixel points at or close to the edge of the image, the has height
               //and width larger (by two times of buffer) than the original image size; The extended area are filled with
               //the pixel values randomly chosen frim the image. The true pixel values of the original image are stored in the range
               //[buffer, buffer+x) for x and [buffer, buffer+h) for y.
    int w,h;
    int tRows,tColumns;
    ArrayList <PixelPositionNode> firstPoints, firstOrthogonalPoints,eeCenters,lastPoints,lastOrthogonalPoints;
    double tPValues[], tTable[][];
    double lineAve_FRR[][],lineAve2_FRR[][],block1Ave_FRR[][],block1Ave2_FRR[][],block2Ave_FRR[][],block2Ave2_FRR[][],pixels_FRR[][];
    int eeneIndexes[][];
    int startLine,endLine;
    FiniteResolutionRotation frrNode;
    int lowestLine,smallestOP,highestLine,largestOP;
    int[][] eeNodeIndexes;
    double[][] pValues;

    int[][] scratchRectangle;
    static int segNumber;

    PixelPositionNode baseCorner, topCorner, leftCorner, rightCorner;//basePixel: the pixel at the corner with the minimum dx*x, and dy*y;
                                          //topPixel: the pixel at the corner with the maximum dx*x, and dy*y;
                                          //leftCorner: the pixel at the corner with minimum dy*y and maximum dx*x
                                          //rightCorner: the pixel at the corner with minimum dx*x and maximum dy*y
    public PixelPositionNode topLeft;
    public EdgeElement_FRR(){
        lowCutoff=0.1;
        connectionCutoff=0.01;
        unitCutoff=0.1;
        center=new PixelPositionNode();
        centero=new PixelPositionNode();
        xRange=new intRange();
        yRange=new intRange();
        ms1=new MeanSem0();
        ms2=new MeanSem0();
        ms1Ext=new MeanSem0();
        ms2Ext=new MeanSem0();
        maxDelta=0;
        startLine=-1;
        endLine=-1;
        maxDeltaLine=new ArrayList<PixelPositionNode> ();
    }
    public EdgeElement_FRR(int w, int h, int b1, int b2, int d1, int d2, int ws, int buffer, int type, int[] eD, int[] gD, TTestTable tTable, int gap){
        this();
        this.b1=b1;
        this.b2=b2;
        this.d1=d1;
        this.d2=d2;
        this.ws=ws;
        this.type=type;
        this.eD=eD;
        this.gD=gD;
        this.h=h;
        this.w=w;
        this.buffer=buffer;
        this.center.update(center);
        this.centero.update(center);
        implantTTable(tTable);
        this.numExts=0;
        this.xRange.expandRanges(0, w-1);
        this.yRange.expandRanges(0, h-1);
        frrNode=new FiniteResolutionRotation(eD, gD);
        numOCLP=frrNode.numOCLP;
        buildCorners();
        lowestLine=leftCorner.lineNumber;
        smallestOP=baseCorner.op;
        highestLine=rightCorner.lineNumber;
        largestOP=topCorner.op;
        firstPoints=firstPoints();
        lastPoints=lastPoints();
        lRange=new intRange(lowestLine, highestLine);
        firstOrthogonalPoints=firstOrthogonalPoints();
        lastOrthogonalPoints=lastOrthogonalPoints();
//        firstOrthogonalPoints=firstOrthogonalPoints(type,w,h);
        eeCenters=new ArrayList<PixelPositionNode>();
        pLines=rightCorner.lineNumber-leftCorner.lineNumber+1;
        numOPs=topCorner.op-baseCorner.op+1;
        lineAve_FRR=new double[pLines][numOPs];
        lineAve2_FRR=new double[pLines][numOPs];
        block1Ave_FRR=new double[pLines][numOPs];
        block1Ave2_FRR=new double[pLines][numOPs];
        block2Ave_FRR=new double[pLines][numOPs];
        block2Ave2_FRR=new double[pLines][numOPs];
        eeneIndexes=new int[pLines][numOPs];
        pixels_FRR=new double[pLines][numOPs];
        numLines=Math.max(b1+d1, b2+d2);
        startLine=-1;
        endLine=-1;
        maxDeltaLine=new ArrayList<PixelPositionNode> ();
        ppnO=frrNode.getPPN_XY(0, 0);
        connectionGap=gap;
    }
    void buildCorners(){
        topLeft=new PixelPositionNode(0,0,0,0,0,eD[1],eD[0]);
        int x=0,y=0;
        int dx=eD[1],dy=eD[0];
        switch (dx*dy){
            case 0:
                if(dx==0){
                    x=0;
                    y=h-1;
                    baseCorner=frrNode.getPPN_XY(x, y);
                    x=w-1;
                    y=0;
                    topCorner=frrNode.getPPN_XY(x, y);
                    x=0;
                    y=0;
                    leftCorner=frrNode.getPPN_XY(x, y);
                    x=w-1;
                    y=h-1;
                    rightCorner=frrNode.getPPN_XY(x, y);
                }else{
                    x=0;
                    y=0;
                    baseCorner=frrNode.getPPN_XY(x, y);
                    x=w-1;
                    y=h-1;
                    topCorner=frrNode.getPPN_XY(x, y);
                    x=w-1;
                    y=0;
                    leftCorner=frrNode.getPPN_XY(x, y);
                    x=0;
                    y=h-1;
                    rightCorner=frrNode.getPPN_XY(x, y);
                }
                break;
            default:
                if(dx>0){
                    if(dy>0){
                        x=0;
                        y=0;
                        baseCorner=frrNode.getPPN_XY(x, y);
                        x=w-1;
                        y=h-1;
                        topCorner=frrNode.getPPN_XY(x, y);
                        x=w-1;
                        y=0;
                        leftCorner=frrNode.getPPN_XY(x, y);
                        x=0;
                        y=h-1;
                        rightCorner=frrNode.getPPN_XY(x, y);
                    }else{
                        x=0;
                        y=h-1;
                        baseCorner=frrNode.getPPN_XY(x, y);
                        x=w-1;
                        y=0;
                        topCorner=frrNode.getPPN_XY(x, y);
                        x=0;
                        y=0;
                        leftCorner=frrNode.getPPN_XY(x, y);
                        x=w-1;
                        y=h-1;
                        rightCorner=frrNode.getPPN_XY(x, y);
                    }
                }else{
                    if(dy>0){
                        x=w-1;
                        y=0;
                        baseCorner=frrNode.getPPN_XY(x, y);
                        x=0;
                        y=h-1;
                        topCorner=frrNode.getPPN_XY(x, y);
                        x=w-1;
                        y=h-1;
                        leftCorner=frrNode.getPPN_XY(x, y);
                        x=0;
                        y=0;
                        rightCorner=frrNode.getPPN_XY(x, y);
                    }else{
                        x=w-1;
                        y=h-1;
                        baseCorner=frrNode.getPPN_XY(x, y);
                        x=0;
                        y=0;
                        topCorner=frrNode.getPPN_XY(x, y);
                        x=0;
                        y=h-1;
                        leftCorner=frrNode.getPPN_XY(x, y);
                        x=w-1;
                        y=0;
                        rightCorner=frrNode.getPPN_XY(x, y);
                    }
                }
                break;
        }
    }
    int shiftNegative0(int length, int position){
        if(position<0) position=length-position;
        return position;
    }
    int shiftNegative1(int length, int position){
        if(position<0) position=length-position-1;
        return position;
    }
    int arrayIndex1(int length, int position){//This function convert a non-zero number between -length and length to a array index between 0 and 2*length-1
        if(position >0) position--;
        if(position <0) {
            position*=-1;
            position+=length-1;
        }
        return position;
    }
    ArrayList <PixelPositionNode> firstPoints(){
        int x,y,l1,l2,l=0;
        firstPoints=new ArrayList<PixelPositionNode>();
        if(leftCorner.x==baseCorner.x){
            x=leftCorner.x;
            l1=leftCorner.lineNumber;
            l2=baseCorner.lineNumber;
            for(l=l1;l<=l2;l++){
                if(eD[1]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LX(l, x, 1,yRange);
//                    moveIntoOPRange(ppn);
                    firstPoints.add(ppn);
                }
            }
        }else{
            y=leftCorner.y;
            l1=leftCorner.lineNumber;
            l2=baseCorner.lineNumber;
            for(l=l1;l<=l2;l++){
                if(eD[0]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LY(l, y, 1,xRange);
//                    moveIntoOPRange(ppn);
                    firstPoints.add(ppn);
                }
            }
        }
        if(rightCorner.x==baseCorner.x){
            x=baseCorner.x;
            l2=rightCorner.lineNumber;
            l1=baseCorner.lineNumber;
            if(firstPoints.size()==0)l1--;
            for(l=l1+1;l<=l2;l++){
                if(eD[1]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LX(l, x, 1,yRange);
//                    moveIntoOPRange(ppn);
                    firstPoints.add(ppn);
                }
            }
        }else{
            y=baseCorner.y;
            l2=rightCorner.lineNumber;
            l1=baseCorner.lineNumber;
            if(firstPoints.size()==0)l1--;
            for(l=l1+1;l<=l2;l++){
                if(eD[0]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LY(l, y, 1,xRange);
//                    moveIntoOPRange(ppn);
                    firstPoints.add(ppn);
                }
            }
        }
        return firstPoints;
    }

    ArrayList <PixelPositionNode> lastPoints(){
        int x,y,l1,l2,l;
        lastPoints=new ArrayList<PixelPositionNode>();
        if(leftCorner.x==topCorner.x){
            x=leftCorner.x;
            l1=leftCorner.lineNumber;
            l2=topCorner.lineNumber;
            for(l=l1;l<=l2;l++){
                if(eD[1]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LX(l, x, -1,yRange);
//                    moveIntoOPRange(ppn);
                    lastPoints.add(ppn);
                }
            }
        }else{
            y=leftCorner.y;
            l1=leftCorner.lineNumber;
            l2=topCorner.lineNumber;
            for(l=l1;l<=l2;l++){
                if(eD[0]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LY(l, y,-1,xRange);
//                    moveIntoOPRange(ppn);
                    lastPoints.add(ppn);
                }
            }
        }
        if(topCorner.x==rightCorner.x){
            x=topCorner.x;
            l2=rightCorner.lineNumber;
            l1=topCorner.lineNumber;
            for(l=l1+1;l<=l2;l++){
                if(eD[1]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LX(l, x, -1,yRange);
//                    moveIntoOPRange(ppn);
                    lastPoints.add(ppn);
                }
            }
        }else{
            y=topCorner.y;
            l2=rightCorner.lineNumber;
            l1=topCorner.lineNumber;
            for(l=l1+1;l<=l2;l++){
                if(eD[0]!=0) {
                    PixelPositionNode ppn=frrNode.getPPN_LY(l, y, -1,xRange);
//                    moveIntoOPRange(ppn);
                    lastPoints.add(ppn);
                }
            }
        }
        return lastPoints;
    }
    void moveIntoOPRange(PixelPositionNode ppn){
        if(ppn.op<smallestOP) ppn.update(frrNode.positionDisplacement(ppn, smallestOP-ppn.op));
        if(ppn.op>largestOP) ppn.update(frrNode.positionDisplacement(ppn, largestOP-ppn.op));
    }
    ArrayList <PixelPositionNode> firstOrthogonalPoints(){
        int l,l0,steps,op,op1,op2,opi,opf,opt;
        int length=2*ws+1;
        firstOrthogonalPoints=new ArrayList<PixelPositionNode>();
        ArrayList <Integer> ops=new ArrayList <Integer>();
        op1=baseCorner.op;
        op2=topCorner.op;
        opi=op1-op1%length;
        if(opi<op1) opi+=length;
        opf=op2-op2%length;
        if(opf>op2) opf-=length;
        for(op=opi;op<=opf;op+=length){
            ops.add(op);
        }
        int index=0;
        opt=ops.get(index);
        int size=ops.size();
        int l1=baseCorner.lineNumber,l2=leftCorner.lineNumber;
        PixelPositionNode ppn;
        op1=baseCorner.op;
        opi=op1-op1%length;
        if(opi<op1) opi+=length;
        int line0=firstPoints.get(0).lineNumber;
        l0=l1;
        opi=opt;
        for(l=l1+1;l>=l2;l--){
            ppn=firstPoints.get(l-line0);
            op2=ppn.op;
            opf=op2-op2%length;
            if(opf<op2) opf+=length;
            for(op=opi;op<opf;op+=length){
                if(op==opt){
                    ppn=frrNode.getPPN_LO(l0, op);
                    if(!outOfRange(ppn)){
                        firstOrthogonalPoints.add(ppn);
                        index++;
                        if(index<size){
                            opt=ops.get(index);
                        }else{
                            return firstOrthogonalPoints;
                        }
                    }
                }
            }
            opi=opt;
            l0=l;
        }
//        opi=opf;
        op2=lastPoints.get(l2-line0).op;
        opf=op2-op2%length;
        if(opf>op2)opf-=length;
        for(op=opi;op<=opf;op+=length){
            if(op==opt){
                ppn=frrNode.getPPN_LO(l2, op);
                firstOrthogonalPoints.add(ppn);
                index++;
                if(index<size){
                    opt=ops.get(index);
                }else{
                    return firstOrthogonalPoints;
                }
            }
        }
        opi=opt;
        l2=topCorner.lineNumber;
        l1=leftCorner.lineNumber;
        for(l=l1+1;l<=l2;l++){
            ppn=lastPoints.get(l-line0);
            op2=ppn.op;
            opf=op2-op2%length;
            if(opf>op2) opf-=length;
            for(op=opi;op<=opf;op+=length){
                if(op==opt){
                    ppn=frrNode.getPPN_LO(l, op);
                    if(!outOfRange(ppn)){
                        firstOrthogonalPoints.add(ppn);
                        index++;
                        if(index<size){
                            opt=ops.get(index);
                        }else{
                            return firstOrthogonalPoints;
                        }
                    }
                }
            }
            opi=opt;
        }
        return firstOrthogonalPoints;
    }
    ArrayList <PixelPositionNode> lastOrthogonalPoints(){
        ArrayList <PixelPositionNode> lastOrthogonalPoints=new ArrayList <PixelPositionNode>();
//        ArrayList <PixelPositionNode> firstOrthogonalPoints=new ArrayList <PixelPositionNode>();
        firstOrthogonalPoints=new ArrayList <PixelPositionNode>();
        int size=firstPoints.size();
//        int line,op,i,length=2*ws+1;
        int line,op,i,length=1;//This is to computer first and last orthogonal points for every OP.
        int opi=smallestOP-smallestOP%length;
        if(opi<smallestOP)opi+=length;
        int opo=opi;
        int opf=largestOP-largestOP%length;
        if(opf>largestOP)opf-=length;
        int len=(opf-opi)/length+1;
        int[]lowest=new int[len],highest=new int[len];
        int index;
        for(i=0;i<len;i++){
            lowest[i]=highestLine+1;
            highest[i]=lowestLine-1;
        }
        PixelPositionNode ppn;
        for(i=0;i<size;i++){
            line=firstPoints.get(i).lineNumber;
            op=firstPoints.get(i).op;
            opi=op-op%length;
            if(opi<op)opi+=length;
            op=lastPoints.get(i).op;
            opf=op-op%length;
            if(opf>op)opf-=length;
            for(op=opi;op<=opf;op+=length){
                index=(op-opo)/length;
                if(line<lowest[index]) lowest[index]=line;
                if(line>highest[index]) highest[index]=line;
            }
        }
        for(i=0;i<len;i++){
            op=opo+i*length;
            firstOrthogonalPoints.add(frrNode.getPPN_LO(lowest[i], op));
            lastOrthogonalPoints.add(frrNode.getPPN_LO(highest[i], op));
        }
        return lastOrthogonalPoints;
    }
    public ArrayList<PixelPositionNode> getFirstPoints(){
        return firstPoints;
    }
    double getPValue(MeanSem0 ms1, MeanSem0 ms2){
        double s1=ms1.sem2, s2=ms2.sem2,m1=ms1.mean,m2=ms2.mean;
        int n1=ms1.n, n2=ms2.n;
        if(s1<0.001)s1=0.001;
        if(s2<0.001)s2=0.001;

        double s12=s1/n1+s2/n2;
        double t=Math.abs(m1-m2)/Math.sqrt(s12);
//        double s12=((n1-1)*s1+(n2-1)*s2)/(n1+n2-2);
//        double t=Math.abs(m1-m2)/(Math.sqrt(s12)*(1./n1+1./n2));
//        int df=(int)(s12*s12/((s1/n1)*(s1/n1)/(n1-1)+(s2/n2)*(s2/n2)/(n2-1)));
        int df=n1+n2-2;
        return getPValue(df, t);
    }

    void slideCenter(int steps){
        center=frrNode.positionDisplacement(center, steps);
    }

    int computePvalue_frr(){
    //This method obsolete other than some troubleshooting purposes
        int length=2*ws+1;
        int n1,n2,x,y,phase,shift1x,shift1y,shift2x,shift2y,x1,y1,x2,y2;
        double mean1,mean2,sem1,sem2;
        n1=d1*length;
        n2=d2*length;
        boolean extend=true;
        PixelPositionNode ppn;
        eeCenters.clear();
        eeCenters.add(new PixelPositionNode(center));
        if(bNewPixelLine){
            n1=d1*length;
            n2=d2*length;
            ppnT=frrNode.orthogonalPositionDisplacement(center,-b1-d1);
            x1=ppnT.x;
            y1=ppnT.y;
            ppnT=frrNode.orthogonalPositionDisplacement(center,b2);
            x2=ppnT.x;
            y2=ppnT.y;
            if(!xRange.contains(x1)||!yRange.contains(y1)||!xRange.contains(x2)||!yRange.contains(y2)){
                pValue=1.;
                slideCenter(length);
                return 0;
            }
            ppn=frrNode.getPPN_XY(x1, y1);
            mean1=block1Ave_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
            sem1=block1Ave2_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
            ppn=frrNode.getPPN_XY(x2, y2);
            mean2=block1Ave_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
            sem2=block1Ave2_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];

            ms1.updateMeanSquareSum(n1,mean1,sem1);
            ms2.updateMeanSquareSum(n2,mean2,sem2);

            pValue=getPValue(ms1,ms2);
            bNewPixelLine=false;
        }else{
            pValue=pValue0;
            ms1.update(ms1Ext);
            ms2.update(ms2Ext);
        }

        slideCenter(length);
        //extending the region if the significance level is higher than low cutoff value.
        double p1=1.;
        if(xRange.contains(center.x)&&yRange.contains(center.y)){
            ppnT=frrNode.orthogonalPositionDisplacement(center,-b1-d1);
            x1=ppnT.x;
            y1=ppnT.y;
            ppnT=frrNode.orthogonalPositionDisplacement(center,b2);
            x2=ppnT.x;
            y2=ppnT.y;
            if(xRange.contains(x1)&&yRange.contains(y1)&&xRange.contains(x2)&&yRange.contains(y2)){
                ppn=frrNode.getPPN_XY(x1, y1);
                mean1=block1Ave_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                sem1=block1Ave2_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                ppn=frrNode.getPPN_XY(x2, y2);
                mean2=block1Ave_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                sem2=block1Ave2_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                ms1Ext.updateMeanSquareSum(n1,mean1,sem1);
                ms2Ext.updateMeanSquareSum(n2,mean2,sem2);
                p1=getPValue(ms1Ext,ms2Ext);
            }
        }
//        p1=1.;
        numExts=0;
        while(extend&&pValue<lowCutoff&&p1<lowCutoff&&(ms1.mean-ms2.mean)*(ms1Ext.mean-ms2Ext.mean)>0&&getPValue(ms1,ms1Ext)>connectionCutoff&&getPValue(ms2,ms2Ext)>connectionCutoff){
            numExts++;
           if(type==1){
                type=type;
           }
            ms1.mergeSems(ms1Ext);
            ms2.mergeSems(ms2Ext);
            eeCenters.add(new PixelPositionNode(center));

            slideCenter(length);
            if(!xRange.contains(center.x)||!yRange.contains(center.y)) break;
                ppnT=frrNode.orthogonalPositionDisplacement(center,-b1-d1);
                x1=ppnT.x;
                y1=ppnT.y;
                ppnT=frrNode.orthogonalPositionDisplacement(center,b2);
                x2=ppnT.x;
                y2=ppnT.y;
                if(xRange.contains(x1)&&yRange.contains(y1)&&xRange.contains(x2)&&yRange.contains(y2)){
                    ppn=frrNode.getPPN_XY(x1, y1);
                    mean1=block1Ave_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                    sem1=block1Ave2_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                    ppn=frrNode.getPPN_XY(x2, y2);
                    mean2=block1Ave_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                    sem2=block1Ave2_FRR[ppn.lineNumber-lowestLine][ppn.op-smallestOP];
                    ms1Ext.updateMeanSquareSum(n1,mean1,sem1);
                    ms2Ext.updateMeanSquareSum(n2,mean2,sem2);
                    p1=getPValue(ms1Ext,ms2Ext);
                }else{
                    break;
                }
        }
        pValue=getPValue(ms1,ms2);
        double delta=Math.abs(getDelta());
        if(delta>maxDelta) maxDelta=delta;
        pValue0=pValue;
        bNewPixelLine=false;
        return 1;
    };

    public double getPvalue(PixelPositionNode center){
        this.updateCenter(center);
        computePvalue_frr();
        center.update(this.center);
        return pValue;
    }

    public int getType(){
        return type;
    }

    void drawGrids(){
//        ImagePlus impl=CommonMethods.newPlainRGBImage("type"+type+ "grids", w, h, Color.white);
        ImagePlus impl=CommonMethods.newPlainRGBImage("type"+type+ "grids_Anchors", w, h, Color.white);
//        drawGrids(impl,w,h);
//        drawGrids_Orthogonal(impl, w, h);
        drawGrids_Anchors(impl, w, h);
        impl.show();
    }
    /*
    int firstPointPhase(int line){
        int phase=0;
        int n=line%cycle;
        int x=dddxo[0][n];
        int y=dddyo[0][n];
        phase=calPhase(0,0,0,x,y);
        return phase;
    }*/
    public void drawGrids(ImagePlus impl, int w, int h){
        int size=firstPoints.size();
        int i,j,x,y,phase;
        int pixel;
        int rgb[]=new int[3];
        int pixels1[]=(int[])impl.getProcessor().getPixels();
        intRange xRange=new intRange(0,w-1);
        intRange yRange=new intRange(0,h-1);
        int count;
        int r=50,g=150,b=250;
        int nx=Math.max(w, h);
        int maxCount=0;
        int longestLine=0;
        int rl=0,gl=0,bl=0;
        int firstPhase=0;
        PixelPositionNode firstPoint,ppn;
        int op;

        for(i=0;i<size;i++){
            count=0;
            firstPoint=firstPoints.get(i);
            x=firstPoints.get(i).x;
            y=firstPoints.get(i).y;
            phase=firstPoints.get(i).phase;
            pixel=CommonMethods.randomColor().getRGB();
            CommonMethods.intTOrgb(pixel, rgb);
            pixel=(r<<16)|(rgb[1]<<8)|(rgb[2]);
            firstPhase=firstPoints.get(i).phase;
            while(xRange.contains(x)&&yRange.contains(y)){
                count++;
                ppn=frrNode.positionDisplacement(firstPoint,count-1);
                op=ppn.op;
                if(count>1){
                    pixels1[y*w+x]=pixel;
                    if(op%(2*ws+1)==0)pixels1[y*w+x]=(r<<16)|(0<<8)|(b);
                }
                else pixels1[y*w+x]=(r<<16)|(g<<8)|b;
                x+=frrNode.ddx(phase);
                y+=frrNode.ddy(phase);
                phase=frrNode.circularAddition(numOCLP+1,phase,1);
            }
            if(count>maxCount){
                longestLine=i;
                maxCount=count;
                rl=r;
                gl=rgb[1];
                bl=rgb[2];
            }
            r=(r+50)%250;
            g=(g+50)%250;
            b=(b+50)%250;
        }

        int pixel1=0;
        for(i=longestLine;i<=longestLine;i++){
            count=0;
            x=firstPoints.get(i).x;
            y=firstPoints.get(i).y;
            phase=firstPoints.get(i).phase;
            pixel=(rl<<16)|(gl<<8)|(bl);
            while(xRange.contains(x)&&yRange.contains(y)){
                count++;
                x+=frrNode.ddx(phase);
                y+=frrNode.ddy(phase);
                phase=frrNode.circularAddition(numOCLP+1,phase,1);
                if((count-nx)/(numOCLP+1)==((count-nx)%(numOCLP+1))&&(count-nx)>=0){
                    pixel1=(rl<<16)|(gl<<8)|(50*phase);
                    drawOrthogonalLine(impl,frrNode.getPPN_XY(x,y),pixel1);
                }
            }
        }
    }

    public void drawGrids_Orthogonal(ImagePlus impl, int w, int h){
        int size=firstOrthogonalPoints.size();
        int i;
        int pixel;
        int rgb[]=new int[3];
        int pixels1[]=(int[])impl.getProcessor().getPixels();
        intRange xRange=new intRange(0,w-1);
        intRange yRange=new intRange(0,h-1);
        int r=50,g=150,b=250;
        PixelPositionNode ppn;
        for(i=0;i<size;i++){
            ppn=firstOrthogonalPoints.get(i);
            if(outOfRange(ppn)) continue;
            pixel=CommonMethods.randomColor().getRGB();
            CommonMethods.intTOrgb(pixel, rgb);
            pixel=(r<<16)|(rgb[1]<<8)|(rgb[2]);
            drawOrthogonalLine(impl,ppn,pixel);
            pixels1[ppn.y*w+ppn.x]=(r<<16)|(g<<8)|(b);
            r=(r+50)%250;
            g=(g+50)%250;
            b=(b+50)%250;
        }
    }

    public void drawGrids_Anchors(ImagePlus impl, int w, int h){
        ArrayList <Integer> pixels=new ArrayList<Integer>();
        int size=firstOrthogonalPoints.size();
        int size0=firstPoints.size();
        int i,op;
        int pixel;
        int rgb[]=new int[3];
        int pixels1[]=(int[])impl.getProcessor().getPixels();
        intRange xRange=new intRange(0,w-1);
        intRange yRange=new intRange(0,h-1);
        int r=50,g=150,b=250;
        PixelPositionNode ppn;
        PixelPositionNode ppn0;
        for(i=0;i<size;i++){
            ppn=firstOrthogonalPoints.get(i);
            if(outOfRange(ppn)) continue;
            pixel=CommonMethods.randomColor().getRGB();
            CommonMethods.intTOrgb(pixel, rgb);
            pixel=(r<<16)|(rgb[1]<<8)|(rgb[2]);
            pixels.add(pixel);
            op=ppn.op;
            for(int j=0;j<size0;j++){
                ppn0=frrNode.getPPN_LO(j, op);
                if(!outOfRange(ppn0)) pixels1[ppn0.y*w+ppn0.x]=pixel;
            }
            r=(r+50)%250;
            g=(g+50)%250;
            b=(b+50)%250;
        }
        r=50;
        g=150;
        b=250;
        for(i=0;i<size;i++){
            ppn=firstOrthogonalPoints.get(i);
            if(outOfRange(ppn)) continue;
            pixel=pixels.get(i);
            CommonMethods.intTOrgb(pixel, rgb);
            pixels1[ppn.y*w+ppn.x]=(r<<16)|(g<<8)|(b);
            r=(r+50)%250;
            g=(g+50)%250;
            b=(b+50)%250;
        }
    }

    public void drawGrids_Anchors(){
        Color c=Color.BLACK;
        ImagePlus impl=CommonMethods.newPlainRGBImage("first, last, orthogonal points", w, h, c);
        int size=firstOrthogonalPoints.size();
        int size0=firstPoints.size();
        int i,j,op;
        int pixel;
        int rgb[]=new int[3];
        int pixels1[]=(int[])impl.getProcessor().getPixels();
        intRange xRange=new intRange(0,w-1);
        intRange yRange=new intRange(0,h-1);
        int r=50,g=150,b=250;
        PixelPositionNode ppn;
        PixelPositionNode ppn0;
        int[][] lRGB=new int[pLines][3];
        int[][] oRGB=new int[numOPs][3];

        for(i=0;i<pLines;i++){
            pixel=CommonMethods.randomColor().getRGB();
            CommonMethods.intTOrgb(pixel, rgb);
            for(j=0;j<3;j++){
                lRGB[i][j]=rgb[j];
            }
        }

        for(i=0;i<numOPs;i++){
            pixel=CommonMethods.randomColor().getRGB();
            CommonMethods.intTOrgb(pixel, rgb);
            for(j=0;j<3;j++){
                oRGB[i][j]=rgb[j];
            }
        }

        int l,o,opi,opf;
        int length=2*ws+1;
        for(l=0;l<pLines;l++){
            opi=firstPoints.get(l).op;
            opf=lastPoints.get(l).op;
            for(o=opi;o<=opf;o++){
                ppn=frrNode.getPPN_LO(l+lowestLine, o);
                if(outOfRange(ppn)){
                    ppn=ppn;
                }
                pixel=(lRGB[l][0]<<16)|(lRGB[l][1]<<8)|(lRGB[l][2]);
                if(o%length==0)pixel=(255)<<16|(255<<8)|(255);
                pixels1[ppn.y*w+ppn.x]=pixel;
            }
        }

        for(l=0;l<pLines;l++){
            opi=firstPoints.get(l).op;
            ppn=frrNode.getPPN_LO(l+lowestLine, opi);
            pixel=((l*50)%250<<16)|(lRGB[l][1]<<8)|(lRGB[l][2]);
            pixels1[ppn.y*w+ppn.x]=pixel;

            opf=lastPoints.get(l).op;
            ppn=frrNode.getPPN_LO(l+lowestLine, opf);
            pixel=(lRGB[l][0]<<16)|(lRGB[l][1]<<8)|((l*50)%250);
            pixels1[ppn.y*w+ppn.x]=pixel;
        }

        for(o=0;o<numOPs;o++){
            l=firstOrthogonalPoints.get(o).lineNumber;
            ppn=frrNode.getPPN_LO(l, o+smallestOP);
            pixel=(lRGB[l-lowestLine][0]<<16)|((o*50)%250<<8)|(lRGB[-lowestLine][2]);
            pixels1[ppn.y*w+ppn.x]=pixel;

            l=lastOrthogonalPoints.get(o).lineNumber;
            ppn=frrNode.getPPN_LO(l, o+smallestOP);
            pixel=(lRGB[-lowestLine][0]<<16)|((o*50)%250<<8)|(lRGB[-lowestLine][2]);
            pixels1[ppn.y*w+ppn.x]=pixel;
        }
        impl.show();
    }

    public void drawGrids_Anchors_orthogonal(){
        Color c=Color.BLACK;
        ImagePlus impl=CommonMethods.newPlainRGBImage("type"+type+": first, last, orthogonal points", w, h, c);
        int size=firstOrthogonalPoints.size();
        int size0=firstPoints.size();
        int i,j,op;
        int pixel;
        int rgb[]=new int[3];
        int pixels1[]=(int[])impl.getProcessor().getPixels();
        intRange xRange=new intRange(0,w-1);
        intRange yRange=new intRange(0,h-1);
        int r=50,g=150,b=250;
        PixelPositionNode ppn;
        PixelPositionNode ppn0;
        int[][] lRGB=new int[pLines][3];
        int[][] oRGB=new int[numOPs][3];

        for(i=0;i<pLines;i++){
            pixel=CommonMethods.randomColor().getRGB();
            CommonMethods.intTOrgb(pixel, rgb);
            for(j=0;j<3;j++){
                lRGB[i][j]=rgb[j];
            }
        }

        for(i=0;i<numOPs;i++){
            pixel=CommonMethods.randomColor().getRGB();
            CommonMethods.intTOrgb(pixel, rgb);
            for(j=0;j<3;j++){
                oRGB[i][j]=rgb[j];
            }
        }

        int l,o,opi,opf,li,lf;
        for(o=0;o<numOPs;o++){
            li=firstOrthogonalPoints.get(o).lineNumber;
            lf=lastOrthogonalPoints.get(o).lineNumber;
            for(l=li;l<=lf;l++){
                ppn=frrNode.getPPN_LO(l, o+smallestOP);
                if(outOfRange(ppn)){
                    ppn=ppn;
                }
                pixel=(oRGB[o][0]<<16)|(oRGB[o][1]<<8)|(oRGB[o][2]);
//                if(o%length==0)pixel=(255)<<16|(255<<8)|(255);
                pixels1[ppn.y*w+ppn.x]=pixel;
            }
        }

        for(l=0;l<pLines;l++){
            opi=firstPoints.get(l).op;
            ppn=frrNode.getPPN_LO(l+lowestLine, opi);
            pixel=((l*50)%250<<16)|(lRGB[l][1]<<8)|(lRGB[l][2]);
            pixels1[ppn.y*w+ppn.x]=pixel;

            opf=lastPoints.get(l).op;
            ppn=frrNode.getPPN_LO(l+lowestLine, opf);
            pixel=(lRGB[l][0]<<16)|(lRGB[l][1]<<8)|((l*50)%250);
            pixels1[ppn.y*w+ppn.x]=pixel;
        }

        for(o=0;o<numOPs;o++){
            l=firstOrthogonalPoints.get(o).lineNumber;
            ppn=frrNode.getPPN_LO(l, o+smallestOP);
            pixel=(oRGB[o][0]<<16)|((o*50)%250<<8)|(oRGB[o][2]);
            pixels1[ppn.y*w+ppn.x]=pixel;

            l=lastOrthogonalPoints.get(o).lineNumber;
            ppn=frrNode.getPPN_LO(l, o+smallestOP);
            pixel=(oRGB[o][0]<<16)|((o*50)%250<<8)|(oRGB[o][2]);
            pixels1[ppn.y*w+ppn.x]=pixel;
        }
        impl.show();
    }

    public void drawOrthogonalLine(ImagePlus impl,PixelPositionNode ppn0, int pixel){
        int pixels[]=(int[])impl.getProcessor().getPixels();
        int stepsP=0,stepsN=0,x,y;
        PixelPositionNode ppn;
        while(true){
            ppn=frrNode.orthogonalPositionDisplacement(ppn0,stepsP);
            x=ppn.x;
            y=ppn.y;
            if(!xRange.contains(x)||!yRange.contains(y))break;
            pixels[w*y+x]=pixel;
            stepsP++;
        }
        while(true){
            ppn=frrNode.orthogonalPositionDisplacement(ppn0,stepsN);
            x=ppn.x;
            y=ppn.y;
            if(!xRange.contains(x)||!yRange.contains(y))break;
            pixels[w*y+x]=pixel;
            stepsN--;
        }
    }
    public double getPValue(){
        return pValue;
    }
    public int getExts(){
        return numExts;
    }
    public void updateCenter(PixelPositionNode ppn){
        center.update(ppn);
        centero.update(ppn);
    }
    public IntPair getOrigin(){
        return new IntPair(centero.x,centero.y);
    }

    public ArrayList<PixelPositionNode> buildPixelLine(PixelPositionNode ppn1,PixelPositionNode ppn2){//include ppn1 and ppn2, returns an empty arraylist if the two pixels are not in the same pixelLine
        ArrayList<PixelPositionNode> pl=new ArrayList<PixelPositionNode>();
        int dist=frrNode.calDisplacement(ppn1,ppn2);
        if(dist==w+h+1)return pl;
        int sign=getSign(dist);
        int step=sign;
        if(step<0)step=numOCLP-step;
        if(step>0)step--;
        dist*=sign;
        int x=ppn1.x,y=ppn1.y,phase=ppn1.phase;
        int steps=0;
        while(steps<=dist){
            if(xRange.contains(x)&&yRange.contains(y))pl.add(frrNode.getPPN_XY(x,y));
            x+=frrNode.cddx(phase,step);
            y+=frrNode.cddy(phase,step);
            phase=frrNode.circularAddition(numOCLP+1,phase,sign);
            steps++;
        }
        return pl;
    }
    public int getSign(int x){
        if(x>0) return 1;
        if(x<0) return -1;
        return 0;
    }
    public ArrayList<PixelPositionNode> getEdgePoints(){/*
        ArrayList<IntPair> eps=new ArrayList<IntPair>();
        int length=2*ws+1;
        PixelPositionNode ppn1=positionDisplacement(centero,-ws);
        PixelPositionNode ppn2=positionDisplacement(centero,numExts*length+ws);
        eps=buildPixelLine(ppn1,ppn2);
        return eps;*/
        return maxDeltaLine;
//        return maxCollectiveBlockDeltaEdgePoints();
    }
    public boolean outOfRange(PixelPositionNode ppn){
        return !xRange.contains(ppn.x)||!yRange.contains(ppn.y);
    }
    public PixelPositionNode maxDeltaPosition(){//this methods searches along orthoganal pixel line to find and returns the the position with maximum delta in lineAve
        int line1=-(b1+d1),line2=b2+d2;
        PixelPositionNode ppn0,ppn;
        double ave0,ave,delta,maxDelta=-256.;
        int maxLine=0;
        ppn0=frrNode.orthogonalPositionDisplacement(centero,line1);
        if(outOfRange(ppn0))return null;
        ave0=lineAve_FRR[ppn0.lineNumber][ppn0.op];
        PixelPositionNode maxPpn=null;
        for(int line=line1+1;line<=line2;line++){
            ppn=frrNode.orthogonalPositionDisplacement(centero,line);
            if(outOfRange(ppn))return null;
            ave=lineAve_FRR[ppn.lineNumber][ppn.op];
            delta=Math.abs(ave-ave0);
            if(delta>maxDelta){
                maxDelta=delta;
                if(delta>0){
                    maxPpn=ppn0;
                }else if(delta<0){
                    maxPpn=ppn;
                }
            }
            ppn0=ppn;
            ave0=ave;
        }
        return maxPpn;
    }

    /* obsolete method
    public PixelPositionNode maxBlockDeltaPosition(){//this methods searches along orthoganal pixel line to find and returns the the position with maximum delta in lineAve
        int line1=-(b1+d1),line2=b2+d2;
        PixelPositionNode ppn1,ppn,ppn2;
        double ave1,ave2,delta,maxDelta=-256.;
        int maxLine=0;
        PixelPositionNode maxPpn=null;
        if(pValue<1e-7){
            ave1=0;
        }

        for(int line=line1;line<=line2;line++){
            ppn=frrNode.orthogonalPositionDisplacement(centero,line);
            ppn1=frrNode.orthogonalPositionDisplacement(ppn,line1);
            ppn2=frrNode.orthogonalPositionDisplacement(ppn,1);
            if(outOfRange(ppn1))return null;
            if(outOfRange(ppn2))return null;
            ave1=block1Ave[ppn1.y][ppn1.x];
            ave2=block2Ave[ppn2.y][ppn2.x];
            delta=Math.abs(ave2-ave1);
            if(delta>maxDelta){
                maxDelta=delta;
                maxPpn=ppn;
             }
        }
        return maxPpn;
    }

    public ArrayList<IntPair> maxCollectiveBlockDeltaEdgePoints(){//this methods searches along orthoganal pixel line to find and returns the the position with maximum delta in lineAve
        int line1=-(b1+d1),line2=b2+d2;
        PixelPositionNode ppn1,ppn,ppn2,ppn0;
        double ave1,ave2,delta,maxDelta=-256.;
        int maxLine=0;
        PixelPositionNode maxPpn=null;
        if(pValue<1e-7){
            ave1=0;
        }
        int i;

        for(int line=line1;line<=line2;line++){
            delta=0;
            for(i=0;i<=numExts;i++){
                ppn0=frrNode.positionDisplacement(centero,i);
                ppn=frrNode.orthogonalPositionDisplacement(ppn0,line);
                ppn1=frrNode.orthogonalPositionDisplacement(ppn,line1);
                ppn2=frrNode.orthogonalPositionDisplacement(ppn,1);
                if(outOfRange(ppn1))return null;
                if(outOfRange(ppn2))return null;
                ave1=block1Ave[ppn1.y][ppn1.x];
                ave2=block2Ave[ppn2.y][ppn2.x];
                delta+=Math.abs(ave2-ave1);
                if(delta>maxDelta){
                    maxDelta=delta;
                    maxPpn=ppn;
                    maxLine=line;
                 }
            }
        }
        ppn=frrNode.orthogonalPositionDisplacement(centero,maxLine);
        ppn1=frrNode.positionDisplacement(ppn,-ws);
        ppn2=frrNode.positionDisplacement(ppn,numExts*(2*ws+1)+ws);
        return buildPixelLine(ppn1,ppn2);
    }*/

    void implantTTable(TTestTable tTable0){
        tRows=tTable0.getRows();
        tColumns=tTable0.getColumns();
        tPValues=new double[tColumns];
        tTable=new double[tRows][tColumns];
        tTable0.getPValues(tPValues);
        tTable0.getTable(tTable);
    }

    double getPValue(int df,double t){
        double p=0.f;
        int row=getRow(df);
        int column=getColumn(row,t);
        p=tPValues[column];
        return p;
    }

    int getColumn(int row, double t){
//        int columnf=effectiveColumns[row];
        int columnf=27;
        if(t<tTable[row][0]) return 0;
        if(t>tTable[row][columnf]) return columnf;
        int c0=0,c1=tColumns-1,c=(c0+c1)/2;
        while(c0!=c){
            if(tTable[row][c]>=t) c1=c;
            else c0=c;
            c=(c0+c1)/2;
        }
        return c;
    }

    int getRow(int df){
        if(df<0)return 0;
        if(df<=30)return df-1;
        if(df<=60)return 29+(df-30)/10;
        if(df<100)return 32+(df-60)/20;
        if(df<1000)return 34;
        return 35;
    }
    public void setPixels(int [][] pixels){
        this.pixels=pixels;
        resetEENEs();
        calLineMeans_FRR();
        calBlockMeans();
    }

    void resetEENEs(){
        int i,j;
        for(i=0;i<pLines;i++){
            for(j=0;j<numOPs;j++){
                eeneIndexes[i][j]=-1;
            }
        }
    }

    public void calLineMeans_FRR(){
        int size=firstPoints.size();
        double mean1=0.,mean2=0.;
        int n=2*ws+1,num=0;
        int i,x,y,pi,op,opo,opi,opf;
        ArrayDeque <Double> aqAve=new ArrayDeque <Double>(), aqAve2=new ArrayDeque <Double>();
        PixelPositionNode ppn,ppn0;
        opo=baseCorner.op;
        int line;

        int lineBuffer=Math.max(b1+d1, b2+d2);
        int i0,opi0,opf0;
        for(i=0;i<size;i++){
            ppn=firstPoints.get(i);
            i0=i-lineBuffer;
            if(i0<0)i0=0;
            opi0=firstPoints.get(i0).op;
            opi=ppn.op;
            if(opi0<opi)opi=opi0;
            num=0;
            aqAve.clear();
            aqAve2.clear();
            mean1=0.;
            mean2=0.;
            opf=lastPoints.get(i).op;
            opf0=lastPoints.get(i0).op;
            if(opf0>opf) opf=opf0;
            line=ppn.lineNumber;


            for(op=opi-ws;op<=opi+ws;op++){
                ppn0=frrNode.getPPN_LO(line, op);
                pi=pixels[ppn0.y+buffer][ppn0.x+buffer];
                mean1+=pi;
                mean2+=pi*pi;
                aqAve.addLast((double)pi);
                aqAve2.addLast((double)pi*pi);
            }

           for(op=opi;op<=opf;op++){
                lineAve_FRR[line-lowestLine][op-smallestOP]=mean1/(double)n;
                lineAve2_FRR[line-lowestLine][op-smallestOP]=mean2/(double)n;
                ppn0=frrNode.getPPN_LO(line, op+ws+1);
                pi=pixels[ppn0.y+buffer][ppn0.x+buffer];
                ppn0=frrNode.getPPN_LO(line, op);
                pixels_FRR[line-lowestLine][op-smallestOP]=pixels[ppn0.y+buffer][ppn0.x+buffer];
                mean1+=pi;
                mean2+=pi*pi;
                aqAve.addLast((double)pi);
                aqAve2.addLast((double)pi*pi);

                mean1-=aqAve.getFirst();
                mean2-=aqAve2.getFirst();
                aqAve.removeFirst();
                aqAve2.removeFirst();
            }
        }
    }

    public void drawBlockAve(){
        Color c=new Color(Color.BITMASK);
        ImagePlus impl=CommonMethods.newPlainRGBImage("block average in xy", w, h, c);
        int[] pixels1=(int[])impl.getProcessor().getPixels();
        PixelPositionNode ppn,ppn1,ppn2;
        int l,o;
        double ave;
        int pixel;
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                ppn=frrNode.getPPN_XY(x, y);
                l=ppn.lineNumber-lowestLine;
                o=ppn.op-smallestOP;
                ave=Math.abs(block1Ave_FRR[l][o]-pixels[y+buffer][x+buffer]);
                pixel=(int)ave;
                pixel=(pixel<<16)|(pixel<<8)|pixel;
                pixels1[y*w+x]=pixel;
            }
        }
        impl.show();

        ImagePlus impl1=CommonMethods.newPlainRGBImage("block average in LO", numOPs, pLines, c);
        int[] pixels2=(int[])impl1.getProcessor().getPixels();

        int x,y,i,j,opi,opf;
        int size=firstPoints.size();
        for(i=0;i<size;i++){
            ppn=firstPoints.get(i);
            l=ppn.lineNumber;
            opi=ppn.op;
            ppn1=lastPoints.get(i);
            opf=ppn1.op;
            for(o=opi;o<=opf;o++){
                ave=block1Ave_FRR[l-lowestLine][o-smallestOP];
                ppn=frrNode.getPPN_LO(l, o);

                pixel=(int)ave;
                pixel=(pixel<<16)|(pixel<<8)|80;
                x=ppn.x;
                y=ppn.y;
                pixels2[y*numOPs+x]=pixel;
            }
        }
        impl1.show();
    }

    public void drawImage(double ratio){
        Color c=new Color(Color.BITMASK);
        ImagePlus impl=CommonMethods.newPlainRGBImage("original pixels, difference in xy and lo", w, h, c);
        int[] pixels1=(int[])impl.getProcessor().getPixels();
        PixelPositionNode ppn,ppn1,ppn2;
        int l,o;
        double ave;
        int pixel;
        int r,g,b;
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                r=0;g=0;b=0;
                ppn=frrNode.getPPN_XY(x, y);
                l=ppn.lineNumber-lowestLine;
                o=ppn.op-smallestOP;
                ave=pixels_FRR[l][o]-pixels[y+buffer][x+buffer];
                if(ave>0){
                    b=(int)Math.abs(ave);
                }else{
                    r=(int)Math.abs(ave);
                }
                pixel=(r<<16)|(g<<8)|b;
                pixels1[y*w+x]=pixel;
            }
        }
        impl.show();
    }
    public void drawRectangle(double ratio){
        Color c=new Color(Color.BITMASK);
        ImagePlus impl=CommonMethods.newPlainRGBImage("block average in xy", (int)ratio*w, (int)ratio*h, c);
        int[] pixels1=(int[])impl.getProcessor().getPixels();
        PixelPositionNode ppn,ppn1,ppn2;
        int l,o;
        double ave;
        int pixel;
        int lo=pLines/2;
        int l1=lo-(int)ratio*pLines/8,l2=lo+(int)ratio*pLines/8;
        int op1=Math.max(firstPoints.get(l1).op,firstPoints.get(l2).op);
        int op2=Math.min(lastPoints.get(l1).op,lastPoints.get(l2).op);
        op1+=(op2-op1)/8;
        op2-=(op2-op1)/8;
        intRange oRange=new intRange(op1,op2);
        intRange lRange=new intRange(l1,l2);
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                ppn=frrNode.getPPN_XY(x, y);
                l=ppn.lineNumber-lowestLine;
                o=ppn.op;
                ave=149;
                if(lRange.contains(l)&&oRange.contains(o)){
                    ave=157;
                }
                pixel=(int)ave;
                pixel=(pixel<<16)|(pixel<<8)|pixel;
                pixels1[y*w+x]=pixel;
            }
        }
        impl.show();
    }

    public void drawLineAve(){
        Color c=new Color(Color.BITMASK);
        ImagePlus impl=CommonMethods.newPlainRGBImage("line average in xy", w, h, c);
        int[] pixels1=(int[])impl.getProcessor().getPixels();
        PixelPositionNode ppn,ppn1,ppn2;
        int l,o;
        double ave;
        int pixel;
        int r,g,b;
        for(int y=0;y<h;y++){
            for(int x=0;x<w;x++){
                r=0;g=0;b=0;
                ppn=frrNode.getPPN_XY(x, y);
                l=ppn.lineNumber-lowestLine;
                o=ppn.op-smallestOP;
                ave=lineAve_FRR[l][o]-pixels[y+buffer][x+buffer];
                if(ave>0){
                    b=(int)Math.abs(ave);
                }else{
                    r=(int)Math.abs(ave);
                }
                pixel=(r<<16)|(g<<8)|b;
                pixels1[y*w+x]=pixel;
            }
        }
        impl.show();

        ImagePlus impl1=CommonMethods.newPlainRGBImage("line average in LO", numOPs, pLines, c);
        int[] pixels2=(int[])impl1.getProcessor().getPixels();

        int x,y,i,j,opi,opf;
        int size=firstPoints.size();
        for(i=0;i<size;i++){
            ppn=firstPoints.get(i);
            l=ppn.lineNumber;
            opi=ppn.op;
            ppn1=lastPoints.get(i);
            opf=ppn1.op;
            for(o=opi;o<=opf;o++){
                ave=lineAve_FRR[l-lowestLine][o-smallestOP];
                ppn=frrNode.getPPN_LO(l, o);

                pixel=(int)ave;
                pixel=(pixel<<16)|(pixel<<8)|80;
                x=ppn.x;
                y=ppn.y;
                pixels2[y*numOPs+x]=pixel;
            }
        }
        impl1.show();
    }
    public void calBlockMeans(){
        double mean1,mean2;
        int size=firstPoints.size();
        int op,opi,opf,i,j,opo=baseCorner.op;
        for(i=0;i<size-d1;i++){
            opi=firstPoints.get(i).op;
            opf=lastPoints.get(i).op;
            for(op=opi;op<=opf;op++){
                mean1=0.;
                mean2=0.;
                for(j=0;j<d1;j++){
                    mean1+=lineAve_FRR[i+j][op-opo];
                    mean2+=lineAve2_FRR[i+j][op-opo];
                }
                block1Ave_FRR[i][op-opo]=mean1/d1;
                block1Ave2_FRR[i][op-opo]=mean2/d1;
            }
        }
        for(i=size-d1;i<size;i++){
            opi=firstPoints.get(i).op;
            opf=lastPoints.get(i).op;
            for(op=opi;op<=opf;op++){
                block1Ave_FRR[i][op-opo]=lineAve_FRR[i][op-opo];
                block1Ave2_FRR[i][op-opo]=lineAve2_FRR[i][op-opo];
            }
        }
        if(d2==d1){
            block2Ave_FRR=block1Ave_FRR;
            block2Ave2_FRR=block1Ave2_FRR;
        }else{
            for(i=0;i<size-d2;i++){
                opi=firstPoints.get(i).op;
                opf=lastPoints.get(i).op;
                for(op=opi;op<=opf;op++){
                    mean1=0.;
                    mean2=0.;
                    for(j=0;j<d2;j++){
                        mean1+=lineAve_FRR[i+j][op-opo];
                        mean2+=lineAve2_FRR[i+j][op-opo];
                    }
                    block2Ave_FRR[i][op-opo]=mean1/d2;
                    block2Ave2_FRR[i][op-opo]=mean2/d2;
                }
            }
            for(i=size-d2;i<size;i++){
                opi=firstPoints.get(i).op;
                opf=lastPoints.get(i).op;
                for(op=opi;op<=opf;op++){
                    block2Ave_FRR[i][op-opo]=lineAve_FRR[i][op-opo];
                    block2Ave2_FRR[i][op-opo]=lineAve2_FRR[i][op-opo];
                }
            }
        }
    }
    public double getDelta(){
        return ms2.mean-ms1.mean;
    }
    public double getBase(){
        return ms1.mean;
    }
    public ArrayList <PixelPositionNode> getCenters(){
        ArrayList <PixelPositionNode> ppna=new ArrayList <PixelPositionNode>();
        int size=eeCenters.size();
        for(int i=0;i<size;i++){
            ppna.add(eeCenters.get(i));
        }
        return eeCenters;
    }
    public double getHeight(){
        return height;
    }
    public int getStartLine(){
        return startLine;
    }
    public int getEndLine(){
        return endLine;
    }
    public void completeEE(){
        PixelPositionNode ppn0,ppn1,ppn,ppn2;
        double ave1,ave2,delta,maxDelta=-256.;
        int maxLine=0;

        PixelPositionNode maxPpn=null;
        if(pValue<1e-7){
            ave1=0;
        }
        double left=ms1.mean,left0=left,leftMin=left,right=ms2.mean,right0=right,rightMax=right;
        double height0=right-left;
        int i,line;
        double sh=height0/Math.abs(height0);
        int numRD=0,numLI=0;//the number of right decreasing and left increasing.
        int cutoffRD=1,cutoffLI=1;

        endLine=centero.lineNumber+b2;
        startLine=centero.lineNumber-b1-d1;
        int endLine0=endLine,startLine0=startLine;
        line=0;
        boolean rightIncreasing=true,leftDecreasing=true;
//        if(centero.x==250&&numExts==15){
        if(numExts==16){
            ppn1=null;
        }
        while(leftDecreasing||rightIncreasing){
            line++;
            ave1=0.;
            ave2=0.;
            right0=right;
            left0=left;
            for(i=0;i<=numExts;i++){
                ppn0=frrNode.positionDisplacement(centero,i);
                ppn=frrNode.orthogonalPositionDisplacement(ppn0,line);
                ppn1=frrNode.orthogonalPositionDisplacement(ppn,-(d1+b1));
                ppn2=frrNode.orthogonalPositionDisplacement(ppn,b2);;
                if(outOfRange(ppn1))break;
                if(outOfRange(ppn2))break;
                ave1+=block1Ave_FRR[ppn1.lineNumber-lowestLine][ppn1.op-smallestOP];
                ave2+=block2Ave_FRR[ppn2.lineNumber-lowestLine][ppn2.op-smallestOP];
            }

            right=ave2/(numExts+1);
            if(rightIncreasing){
                if(sh*(right-rightMax)>0){
                    rightMax=right;
                    endLine=line+b2;
                }else if(sh*(right-right0)>0){
                    right0=right;
                }else{
                    numRD++;
                    if(numRD>cutoffRD) rightIncreasing=false;
                }
            }

            left=ave1/(numExts+1);
            if(leftDecreasing){
                if(sh*(left-leftMin)<0){
                    leftMin=left;
                    startLine=line-b1-d1;
                }else if(sh*(left0-left)>0.){
                    left0=left;
                }else{
                    numLI++;
                    if(numLI>cutoffLI) leftDecreasing=false;
                }
            }
            delta=Math.abs(right-left);
            if(delta>maxDelta){
                maxDelta=delta;
                maxLine=line;
            }
        }

        numRD=0;
        numLI=0;
        rightIncreasing=true;
        leftDecreasing=true;
        line=0;
        while(leftDecreasing||rightIncreasing){
            line--;
            ave1=0.;
            ave2=0.;
            right0=right;
            left0=left;
            for(i=0;i<=numExts;i++){
                ppn0=frrNode.positionDisplacement(centero,i);
                ppn=frrNode.orthogonalPositionDisplacement(ppn0,line);
                ppn1=frrNode.orthogonalPositionDisplacement(ppn,-d1);
                ppn2=ppn;
                if(outOfRange(ppn1))break;
                if(outOfRange(ppn2))break;
                ave1+=block1Ave_FRR[ppn1.lineNumber-lowestLine][ppn1.op-smallestOP];
                ave2+=block2Ave_FRR[ppn2.lineNumber-lowestLine][ppn2.op-smallestOP];
            }

            right=ave2/(numExts+1);
            if(rightIncreasing){
                if(sh*(right-rightMax)>0){
                    rightMax=right;
                    endLine=line+b2;
                }else if(sh*(right-right0)>0){
                    right0=right;
                }else{
                    numRD++;
                    if(numRD>cutoffRD) rightIncreasing=false;
                }
            }

            left=ave1/(numExts+1);
            if(leftDecreasing){
                if(sh*(left-leftMin)<0){
                    leftMin=left;
                    startLine=line-b1-d1;
                }else if(sh*(left0-left)>0.){
                    left0=left;
                }else{
                    numLI++;
                    if(numLI>cutoffLI) leftDecreasing=false;
                }
            }
            delta=Math.abs(right-left);
            if(delta>maxDelta){
                maxDelta=delta;
                maxLine=line;
            }
        }
        ppn=frrNode.orthogonalPositionDisplacement(centero,maxLine);
        ppn1=frrNode.positionDisplacement(ppn,-ws);
        ppn2=frrNode.positionDisplacement(ppn,numExts*(2*ws+1)+ws);
        maxDeltaLine=buildPixelLine(ppn1,ppn2);
        height=rightMax-leftMin;
    }
    public void startPixelLine(){
        bNewPixelLine=true;
    }

    public void buidEENodes(ArrayList <EdgeElementNode_E> eeNodes,int[][] eeNodeIndexes,double[][] pValues){
        this.eeNodeIndexes=eeNodeIndexes;
        this.pValues=pValues;
        RT.gc();
        int length=2*ws+1;
        int size=firstOrthogonalPoints.size();
        int opi=firstOrthogonalPoints.get(0).op, opf=firstOrthogonalPoints.get(size-1).op;
        opi-=opi%length;
        if(opi<smallestOP) opi+=length;
        opf-=opf%length;
        if(opf>largestOP) opf+=length;

        opi-=smallestOP;
        opf-=smallestOP;

        int size1=firstPoints.size();
        int eeunpIndexes0[]=new int[size1];
        int eeunpIndexes[]=new int[size1];
        ArrayList <EEUNPack> eeunps= new ArrayList <EEUNPack>();

        int i,op,line,li,lf;

        ArrayList <EdgeElementUnitNode> eeuns;
        EdgeElementUnitNode eeun;
        int size2;
        for(int phase=0;phase<length;phase++){
            for(i=0;i<size1;i++){
                eeunpIndexes0[i]=-1;
                eeunpIndexes[i]=-1;
            }
            for(op=opi+phase;op<=opf;op+=length){
                eeuns=findEEUNodes(op);
                size2=eeuns.size();
                for(i=0;i<size2;i++){
                    eeun=eeuns.get(i);
                    line=eeun.maxDeltaLine;
    //                eeunps[line].m_eeuns.add(eeun);
                    packEEUN(eeunpIndexes0,eeunpIndexes,size1,eeunps,eeun);
                }
                for(i=0;i<size1;i++){
                    eeunpIndexes0[i]=eeunpIndexes[i];
                    eeunpIndexes[i]=-1;
                }
            }
        }
        RT.gc();
        ArrayList <EdgeElementNode_E> eenes;
        eenes=buildEENodes(eeunps);
        registerEENs(eeNodes,eenes);
    }

    void registerEENs(ArrayList <EdgeElementNode_E> eeNodes, ArrayList <EdgeElementNode_E> eenes){
        
    }

    void packEEUN(int[] eeunpIndexes0, int[] eeunpIndexes, int size, ArrayList <EEUNPack> eeunps, EdgeElementUnitNode eeun){
        //TODO: the current veresion does not consider gap between eeun's. Implete the gap later by using eeunpIndexes[][] instead of
        //eeunpIdexes0 and eeunpIdexes0.
        //TODO: need to generate an input file that contains parameters important for edge detection: such as scope, connection threshold, lowcutoff ...
        int i,line,li,lf,size1,minDel,minLine,del;
        ArrayList <EdgeElementUnitNode> eeunst;
        EdgeElementUnitNode eeun0;
        li=eeun.leftStart-lowestLine;
        if(li<0) li=0;
        lf=eeun.rightEnd-lowestLine;
        if(lf>=size) lf=size-1;
        PixelPositionNode ppn=frrNode.getPPN_LO(eeun.maxDeltaLine, eeun.op);

        minDel=size;
        minLine=-1;
        int index;
        int scope=1,centerLine=eeun.maxDeltaLine-lowestLine;
        li=Math.min(centerLine-scope, li);
        if(li<0) li=0;
        lf=Math.max(centerLine+scope, lf);
        if(lf>=pLines) lf=pLines-1;
        for (line=li;line<=lf;line++){
            if(eeunps.size()<=0) break;
            index=eeunpIndexes0[line];
            if(index<0) continue;
            eeunst=eeunps.get(index).m_eeuns;
            size1=eeunst.size();
            if(size1>0){
                eeun0=eeunst.get(size1-1);
                if(overlapped(eeun0,eeun)) {
                    del=Math.abs(eeun0.maxDeltaLine-eeun.maxDeltaLine);
                    if(del<minDel){
                        minDel=del;
                        minLine=line;
                    }
                }
            }
        }
        line=eeun.maxDeltaLine;
        if(minDel==size){
            EEUNPack ep=new EEUNPack();
            ep.m_eeuns.add(eeun);
            eeunps.add(ep);
            eeunpIndexes[eeun.maxDeltaLine-lowestLine]=eeunps.size()-1;
        }else{
            index=eeunpIndexes0[minLine];
            eeunpIndexes[eeun.maxDeltaLine-lowestLine]=index;
            eeunps.get(index).m_eeuns.add(eeun);
        }
    }

    boolean overlapped(EdgeElementUnitNode eeun0, EdgeElementUnitNode eeun){
        boolean opd=false;
        if(eeun0.maxDeltaLine<=eeun.rightEnd&&eeun0.maxDeltaLine>=eeun.leftStart) opd=true;
        if(eeun.maxDeltaLine<=eeun0.rightEnd&&eeun.maxDeltaLine>=eeun0.leftStart) opd=true;
        return opd;
    }

    ArrayList <EdgeElementNode_E> buildEENodes(ArrayList <EEUNPack> eeunps){
        IntArray eenIndexes=new IntArray();
        ArrayList <EdgeElementNode_E> eens=new ArrayList <EdgeElementNode_E>();
        int len=eeunps.size(),length=2*w+1;
        int i,j,k,ki,kf,size;
        EdgeElementUnitNode eeun0,eeun;
        ArrayList <EdgeElementUnitNode> eeuns,eeuns1=new ArrayList <EdgeElementUnitNode>();
        for(i=0;i<len;i++){
            eeuns=eeunps.get(i).m_eeuns;
            size=eeuns.size();
            if(size==0)continue;
            eeun0=eeuns.get(0);
            j=1;
            while(j<size){
                eeuns1.clear();
                eeun=eeuns.get(j);
                while(mergable(eeun0,eeun)){
                    eeuns1.add(eeun0);
                    j++;
                    if(j>=size)break;
                    eeun0=eeun;
                    eeun=eeuns.get(j);
                }
                eeuns1.add(eeun0);
                eenIndexes.m_intArray.clear();
                EdgeElementNode_E eene=new EdgeElementNode_E();
                buildEENode(eeuns1,eenIndexes);
                registerEEN(eens,eene,eenIndexes);
                eeun0=eeun;
                j++;
            }
            eeuns1.clear();
            eeuns1.add(eeun0);
            EdgeElementNode_E eene=new EdgeElementNode_E();
            eenIndexes.m_intArray.clear();
            buildEENode(eeuns1,eenIndexes);
            registerEEN(eens,eene,eenIndexes);
        }
        return eens;
    }

    void registerEEN(ArrayList<EdgeElementNode_E> eens,EdgeElementNode_E eene,IntArray eenIndexes){
        int size=eens.size(),size0,size2;
        int index=size-1,i,j,index1;
        if(index<0) index=0;
        int size1=eenIndexes.m_intArray.size();
        PixelPositionNode ppn;
        ArrayList <EdgeElementNode_E> eenest=new ArrayList <EdgeElementNode_E>();
        ArrayList <EdgeElementNode_E> eenes=new ArrayList <EdgeElementNode_E>();
        EdgeElementNode_E eenet;
        if(size1==0){
            size0=eene.edgeLine.size();
            for(i=0;i<size0;i++){
                ppn=eene.edgeLine.get(i);
                eeNodeIndexes[ppn.lineNumber][ppn.op]=index;
            }
            eenest.add(eene);
        }else{
            for(i=0;i<size1;i++){
                index1=eenIndexes.m_intArray.get(i);
                EdgeElementNode_E eene1=eens.get(index1);
                eenes.clear();
                resolveCrossingEdges_SameType(eenes,eene,eene1);
                size2=eenes.size();
                for(j=0;j<size2;j++){
                    eenest.add(eenes.get(j));
                }
            }
        }
        size1=eenest.size();
        for(i=1;i<size1;i++){
            eenet=eenest.get(i);
            size0=eene.edgeLine.size();
            for(i=0;i<size0;i++){
                ppn=eene.edgeLine.get(i);
                eeNodeIndexes[ppn.lineNumber][ppn.op]=index;
            }
            eenest.add(eenet);
            index++;
        }
    }

    void resolveCrossingEdges_SameType(ArrayList<EdgeElementNode_E> eenes, EdgeElementNode_E eene, EdgeElementNode_E eene1){
        //this method is to resolve the crossed edge elements of the same type.
        PixelPositionNode ppn=eene.edgeLine.get(0), ppn1=eene1.edgeLine.get(0);
        int opi=ppn.op,opi1=ppn1.op;
        int size=eene.edgeLine.size(),size1=eene1.edgeLine.size();
        ppn=eene.edgeLine.get(size-1);
        ppn1=eene1.edgeLine.get(size1-1);
        int opf=ppn.op,opf1=ppn1.op;
        int on=Math.max(opi, opi1),ox=Math.min(opf, opf1);
        IntArray olpIndexes=new IntArray(), lnDists=new IntArray();
        int o,id,id1,lda0,ld,lda;

        IntPairArray olpsegs=new IntPairArray();
        int olpi=ox,olpf=on;

        lda0=3;
        for(o=on;o<=ox;o++){
            id=o-opi;
            olpIndexes.m_intArray.add(id);
            id1=o-opi1;
            ppn=eene.edgeLine.get(id);
            ppn1=eene1.edgeLine.get(id1);
            ld=ppn1.lineNumber-ppn.lineNumber;
            lnDists.m_intArray.add(ld);
            lda=Math.abs(ld);
            if(lda<2){
                if(lda0>=2){
                    olpi=o;
                }
            }else{//lda>=1
                if(lda0<=1){
                    olpf=o-1;
                    olpsegs.m_IntpairArray.add(new IntPair(olpi,olpf));
                }
            }
            lda0=Math.abs(lda);
        }

        int numOlpsegs=olpsegs.m_IntpairArray.size(),seg;

        ArrayList <MeanSem0> msls=new ArrayList <MeanSem0>(), msrs=new ArrayList <MeanSem0>(), msls1=new ArrayList <MeanSem0>(),msrs1=new ArrayList <MeanSem0>();
        PixelPositionArray2 left=new PixelPositionArray2(), right=new PixelPositionArray2(),left1=new PixelPositionArray2(),right1=new PixelPositionArray2();
        IntPair ip;
        int opit,opft,opit0=opi,opft0=opi,opit10=opi1,opft10=opi1,l,lLine,rLine,line,line1,rLine1;
        ArrayList <IntPair> loPairs=new ArrayList <IntPair>();

        for(seg=0;seg<numOlpsegs;seg++){
            ip=olpsegs.m_IntpairArray.get(seg);
            opit=ip.x;
            opft=ip.y;
            PixelPositionArray leftPixelLine=new PixelPositionArray();
            PixelPositionArray leftPixelLine1=new PixelPositionArray();
            PixelPositionArray rightPixelLine=new PixelPositionArray();
            PixelPositionArray rightPixelLine1=new PixelPositionArray();

            for(o=opft0+1;o<opit;o++){//constructing ms edge segment for eene
                id=o-opi;
                ppn=eene.edgeLine.get(id);
                line=ppn.lineNumber;
                lLine=eene.leftStart.get(id).lineNumber;
                rLine=eene.rightEnd.get(id).lineNumber;

                if(o>=opi1&&o<=opf1){
                    id1=o-opi1;
                    line1=eene1.edgeLine.get(id1).lineNumber;
                    if(line1<line&&line1>lLine) lLine=line1;
                    if(line1>line&&line1<rLine) rLine=line1;
                }

                loPairs.clear();
                leftPixelLine.m_PixelPositionArray.add(frrNode.getPPN_LO(lLine, o));
                if(line-b1-d1>lLine) lLine=line-b1-d1;
                for(l=line-b1;l>=lLine;l--){
                    loPairs.add(new IntPair(l,o));
                }
                msls.add(buildMeanSem_LO(loPairs));

                loPairs.clear();
                rightPixelLine.m_PixelPositionArray.add(frrNode.getPPN_LO(rLine, o));
                if(line+b2+d2<rLine) rLine=line+b2+d2;
                for(l=line+b2;l<=rLine;l++){
                    loPairs.add(new IntPair(l,o));
                }
                msls.add(buildMeanSem_LO(loPairs));
            }

            for(o=opft10+1;o<opit10;o++){//TODO: need to finish the implementation of this method after the paper.
                id=o-opi;
                ppn=eene.edgeLine.get(id);
                line=ppn.lineNumber;
                lLine=eene.leftStart.get(id).lineNumber;
                rLine=eene.rightEnd.get(id).lineNumber;

                if(o>=opi1&&o<=opf1){
                    id1=o-opi1;
                    line1=eene1.edgeLine.get(id1).lineNumber;
                    if(line1<line&&line1>lLine) lLine=line1;
                    if(line1>line&&line1<rLine) rLine=line1;
                }

                loPairs.clear();
                leftPixelLine.m_PixelPositionArray.add(frrNode.getPPN_LO(lLine, o));
                if(line-b1-d1>lLine) lLine=line-b1-d1;
                for(l=line-b1;l>=lLine;l--){
                    loPairs.add(new IntPair(l,o));
                }
                msls.add(buildMeanSem_LO(loPairs));

                loPairs.clear();
                rightPixelLine.m_PixelPositionArray.add(frrNode.getPPN_LO(rLine, o));
                if(line+b2+d2<rLine) rLine=line+b2+d2;
                for(l=line+b2;l<=rLine;l++){
                    loPairs.add(new IntPair(l,o));
                }
                msls.add(buildMeanSem_LO(loPairs));
            }

        }
    }
    MeanSem0 buildMeanSem_LO(ArrayList <IntPair> los){
        MeanSem0 ms=new MeanSem0();
        return ms;
    }
    void buildEdgeSides_SameType(ArrayList<EdgeElementNode_E> eenes, ArrayList<EdgeElementNode_E> eenes1, int indexi, int indexf, int indexi1, int indexf1, MeanSem0 msl, MeanSem0 msr, MeanSem0 msl1, MeanSem0 msr1){
        
    }

    ArrayList <Integer> getOverlappingPoints(ArrayList<PixelPositionNode> ppns, ArrayList<PixelPositionNode> ppns1){
        //this method assumes that hte difference in o or l between neighboring points are not greater than 1.
        //this method also assumes that the pexel position in the first arraylist ppns are in assedning order of op.
        segNumber++;
        ArrayList <Integer> olIndexes= new ArrayList <Integer>();
        int size=ppns.size(),size1=ppns1.size(),i,j,o,l;
        PixelPositionNode ppn;
        for(i=0;i<size1;i++){
            ppn=ppns1.get(i);
            l=ppn.lineNumber;
            o=ppn.op;
            scratchRectangle[l][o]=segNumber;
        }
        for(i=0;i<size;i++){
            ppn=ppns.get(i);
            l=ppn.lineNumber;
            o=ppn.op;
            if(scratchRectangle[l][o]==segNumber) olIndexes.add(i);
        }
        return olIndexes;
    }

    int FirstTopPosition(ArrayList<PixelPositionNode> ppns, int i0, int i1, int op){
        //this method returns the index of the pixel position whose OP is op and higher line number than its neighboring points whose OP are also op.
        PixelPositionNode ppn;
        int it=i0,i,o,l;
        int xl=lowestLine-1;
        for(i=i0+1;i<=i1;i++){
            ppn=ppns.get(i);
            o=ppn.op;
            l=ppn.lineNumber;
            if(xl>=lowestLine&&o!=op) break;
            if(l>xl&&o==op){
                it=i;
                xl=l;
            }
        }
        return it;
    }

    int FirstTopPosition(ArrayList<PixelPositionNode> ppns, int i0, int i1, int op, int l0){
        //this method returns the index of the pixel position whose OP is op and higher line number than its neighboring points whose OP are also op.
        //this method returns the index of the pixel if its OP is op and linenumber is  greater or equal to l0, otherwise, it will work as described in above comment line.
        PixelPositionNode ppn0=ppns.get(i0),ppn;
        int it=i0,i,o,l;
        int xl=lowestLine-1;
        for(i=i0+1;i<=i1;i++){
            ppn=ppns.get(i);
            o=ppn.op;
            l=ppn.lineNumber;
            if(xl>=lowestLine&&o!=op) break;
            if(l>xl&&o==op){
                it=i;
                xl=l;
                if(l>=l0) return it;
            }
        }
        return it;
    }

    int FirstBottomPosition(ArrayList<PixelPositionNode> ppns, int i0, int i1, int op){
        //this method returns the index of the pixel position whose OP is op and lower line number than its neighboring points whose OP are also op.
        PixelPositionNode ppn0=ppns.get(i0),ppn;
        int it=i0,i,o,l;
        int ml=highestLine+1;
        for(i=i0+1;i<=i1;i++){
            ppn=ppns.get(i);
            o=ppn.op;
            l=ppn.lineNumber;
            if(ml<=highestLine&&o!=op) break;
            if(l<ml&&o==op){
                it=i;
                ml=l;
            }
        }
        return it;
    }

    int FirstBottomPosition(ArrayList<PixelPositionNode> ppns, int i0, int i1, int op, int l0){
        //this method returns the index of the pixel position whose OP is op and lower line number than its neighboring points whose OP are also op.
        //this method returns the index of the pixel if its OP is op and linenumber smaller or equal to l0, otherwise, it will work as described in above comment line.
        PixelPositionNode ppn;
        int it=i0,i,o,l;
        int ml=highestLine+1;
        for(i=i0+1;i<=i1;i++){
            ppn=ppns.get(i);
            o=ppn.op;
            l=ppn.lineNumber;
            if(ml<=highestLine&&o!=op) break;
            if(l<ml&&o==op){
                it=i;
                ml=ppn.lineNumber;
                if(l<=l0) return it;
            }
        }
        return it;
    }

    EdgeElementNode_E buildEENode(ArrayList <EdgeElementUnitNode> eeuns, IntArray eenIndexes){
        EdgeElementNode_E eeNode=new EdgeElementNode_E();
        int i,j,op,li,lf,l;
        numExts=eeuns.size();
        ArrayList <IntPair> leftLOs=new ArrayList <IntPair>();
        ArrayList <IntPair> rightLOs=new ArrayList <IntPair>();
        EdgeElementUnitNode eeun;
        int index;
        for(i=0;i<numExts;i++){
            eeun=eeuns.get(i);
            op=eeun.op;
            li=eeun.leftStart;
            lf=eeun.leftEnd+d1-1;
            for(l=li;l<=lf;l++){
                leftLOs.add(new IntPair(l,op));
            }
            li=eeun.rightStart;
            lf=eeun.rightEnd+d2-1;
            for(l=li;l<=lf;l++){
                rightLOs.add(new IntPair(l,op));
            }
            for(j=-ws;j<=ws;j++){
//            for(j=0;j<=0;j++){
                PixelPositionNode ppn=frrNode.getPPN_LO(eeun.maxDeltaLine, op+j);
                index=eeNodeIndexes[ppn.lineNumber][ppn.op];
                if(index!=-1&&!eenIndexes.containsContent(index)){
                    eenIndexes.m_intArray.add(index);
                }
                if(j==0){
                    eeNode.edgePoints.add(ppn);
                }else{
                    eeNode.edgePoints.add(null);
                }
                eeNode.edgeLine.add(frrNode.getPPN_LO(eeun.maxDeltaLine, op+j));
                eeNode.leftEnd.add(frrNode.getPPN_LO(eeun.leftEnd, op+j));
                eeNode.leftStart.add(frrNode.getPPN_LO(eeun.leftStart, op+j));
                eeNode.rightEnd.add(frrNode.getPPN_LO(eeun.rightEnd, op+j));
                eeNode.rightStart.add(frrNode.getPPN_LO(eeun.rightStart, op+j));
            }
        }
        MeanSem0 ms1=new MeanSem0(),ms2=new MeanSem0();
        buildMeanSemLO(leftLOs,ms1);
        buildMeanSemLO(rightLOs,ms2);
        double p=getPValue(ms1,ms2);
        eeNode.pValue=p;
        eeNode.dx=eD[1];
        eeNode.dy=eD[0];
        eeNode.numExts=numExts;
        eeNode.base=ms1.mean;
        eeNode.height=ms2.mean-ms1.mean;
        eeNode.type=type;
        eeNode.n1=ms1.n;
        eeNode.n2=ms2.n;
        eeNode.numEPs=eeNode.edgePoints.size();
        return eeNode;
    }

    void maxDeltaEdge(EdgeElementNode_E eeNode, ArrayList <EdgeElementUnitNode> eeuns){
        int i;
        int size=eeuns.size();
        EdgeElementUnitNode eeun;
        int op,opi=eeuns.get(0).op,opf=eeuns.get(size-1).op;
        int l,minL=highestLine+1,maxL=lowestLine-1,maxLine,l1,l2;
        double maxDel=-256,del;
        
        for(i=0;i<size;i++){
            eeun=eeuns.get(i);
            l=eeun.maxDeltaLine;
            if(l<minL)minL=l;
            if(l>maxL)maxL=l;
        }
        maxLine=minL;
        for(l=minL;l<=maxL;l++){
            del=0;
            for(i=0;i<size;i++){
                eeun=eeuns.get(i);
                op=eeun.op;
                l1=l-d1-b1-lowestLine;
                l2=l+b2-lowestLine;
                del+=block2Ave_FRR[l2][op-smallestOP]-block1Ave_FRR[l1][op-smallestOP];
            }
            del=Math.abs(del);
            if(del>maxDel){
                maxDel=del;
                maxLine=l;
            }
        }
        eeNode.edgePoints.clear();
        int li,lf,ri,rf;
        ArrayList <IntPair> leftLOs=new ArrayList <IntPair>(), rightLOs=new ArrayList <IntPair>();
        MeanSem0 msL=new MeanSem0(), msR=new MeanSem0();
        for(i=0;i<size;i++){
            eeun=eeuns.get(i);
            op=eeun.op;
            eeNode.edgePoints.add(frrNode.getPPN_LO(maxLine, op));//edge point of current version only mark the pixel where the p value is evaluated.
//            li=Math.min(eeun.leftStart, maxLine-b1-d1);
            li=maxLine-b1-d1;
            lf=maxLine-1;
            for(l=li;l<=lf;l++){
                leftLOs.add(new IntPair(l,op));
            }

            ri=maxLine+b2;
//            rf=Math.max(eeun.rightEnd+b2+d2, maxLine+b2+d2);
            rf=maxLine+b2+d2;
            for(l=ri;l<=rf;l++){
                rightLOs.add(new IntPair(l,op));
            }
        }
        buildMeanSemLO(leftLOs,msL);
        buildMeanSemLO(rightLOs,msR);
        eeNode.base=msL.mean;
        eeNode.height=msR.mean-msL.mean;
        eeNode.pValue=getPValue(msL,msR);
    }


    void straitEdgePoints(EdgeElementNode_E eeNode){
        int size=eeNode.edgePoints.size();
        if(size>0){
            PixelPositionNode ppn1=eeNode.edgePoints.get(0);
            PixelPositionNode ppn2=eeNode.edgePoints.get(size-1);
            eeNode.edgePoints.clear();
            buildConnection(ppn1,ppn2,eeNode.edgePoints);
        }
    }

    public void buildConnection(PixelPositionNode ppn1, PixelPositionNode ppn2, ArrayList <PixelPositionNode> ppns){
        ArrayList <IntPair> ipa=new ArrayList <IntPair>();
        IntPair ip;
        PixelPositionNode ppn;
        EdgeElementNode_E.buildConnection(new IntPair(ppn1.x,ppn1.y), new IntPair(ppn2.x,ppn2.y),ipa);
        int size=ipa.size();
        for(int i=0;i<size;i++){
            ip=ipa.get(i);
            ppn=frrNode.getPPN_XY(ip.x, ip.y);
            if(!outOfRange(ppn)) ppns.add(ppn);
        }
    }

    void buildMeanSemLO(ArrayList <IntPair> LOs, MeanSem0 ms){//perform the range check.
        double m=0.,sem=0.;
        int i,n,l,op,num=0;
        int size=LOs.size();
        IntPair ip;
        PixelPositionNode ppn;

        for(i=0;i<size;i++){
            ip=LOs.get(i);
            l=ip.x;
            op=ip.y;
            ppn=frrNode.getPPN_LO(l, op);
            if(outOfRange_LO(l,op))continue;
            l-=lowestLine;
            op-=smallestOP;
            m+=lineAve_FRR[l][op];
            sem+=lineAve2_FRR[l][op];
            num++;
        }
        m/=num;
        sem/=num;
        n=num*(2*ws+1);
        ms.updateMeanSquareSum(n, m, sem);
    }

    double pValueLO(ArrayList <IntPair> leftLOs, ArrayList <IntPair> rightLOs){
        double p=1.;//The L and O are corrected by the lowestLine and the smallest OP;
        double m1=0.,m2=0.,sem1=0.,sem2=0.;
        int i,n1,n2,l,op;
        int size=leftLOs.size();
        n1=size*(2*ws+1);
        IntPair ip;
        int num=0;
        for(i=0;i<size;i++){
            ip=leftLOs.get(i);
            l=ip.x;
            op=ip.y;
            if(outOfRange_LO(l+lowestLine,op+smallestOP)) continue;
            m1+=lineAve_FRR[l][op];
            sem1+=lineAve2_FRR[l][op];
            num++;
        }
        if(num==0)return 1.;
        m1/=num;
        sem1/=num;
        MeanSem0 ms1=new MeanSem0();
        ms1.updateMeanSquareSum(n1, m1, sem1);

        size=leftLOs.size();
        n2=size*(2*ws+1);
        num=0;
        for(i=0;i<size;i++){
            ip=rightLOs.get(i);
            l=ip.x;
            op=ip.y;
            if(outOfRange_LO(l+lowestLine,op+smallestOP)) continue;
            m2+=lineAve_FRR[l][op];
            sem2+=lineAve2_FRR[l][op];
            num++;
        }
        if(num==0) return 1.;
        m2/=num;
        sem2/=num;
        MeanSem0 ms2=new MeanSem0();
        ms1.updateMeanSquareSum(n2, m2, sem2);

        p=getPValue(ms1,ms2);
        return p;
    }

    boolean mergable(EdgeElementUnitNode eeun0, EdgeElementUnitNode eeun){
        boolean m=true;
        if(Math.abs(eeun.op-eeun0.op)>(2*ws+1)*(connectionGap+1)) m=false;
        if(pValue(frrNode.getPPN_LO(eeun0.rightEnd, eeun0.op),frrNode.getPPN_LO(eeun.rightEnd, eeun0.op))<connectionCutoff) m=false;
        if(pValue(frrNode.getPPN_LO(eeun0.leftStart, eeun0.op),frrNode.getPPN_LO(eeun0.leftStart, eeun0.op))<connectionCutoff) m=false;
        if((eeun0.right-eeun0.left)*(eeun.right-eeun.left)<0) m=false;
        if(eeun0.pValue>lowCutoff||eeun.pValue>lowCutoff)m=false;
        return m;
    }

    double pValue(PixelPositionNode ppn){
        double p=1.;
        int length=2*ws+1;
        int n1,n2;
        double mean1,mean2,sem1,sem2;
        n1=d1*length;
        n2=d2*length;
        ppnT=frrNode.getPPN_LO(ppn.lineNumber-b1-d1, ppn.op);

        int il=ppn.lineNumber-b1-d1,io=ppn.op;
        if(outOfRange_LO(il,io)) return 1.;
        il-=lowestLine;
        io-=smallestOP;
        mean1=block1Ave_FRR[il][io];
        sem1=block1Ave2_FRR[il][io];

        il=ppn.lineNumber-b1-d1;
        if(outOfRange_LO(il,io+smallestOP)) return 1.;
        il-=lowestLine;

        mean2=block2Ave_FRR[il][io];
        sem2=block2Ave2_FRR[il][io];

        ms1.updateMeanSquareSum(n1,mean1,sem1);
        ms2.updateMeanSquareSum(n2,mean2,sem2);

        p=getPValue(ms1,ms2);
        return p;
    }

    boolean outOfRange_LO(int line, int op){
        boolean or=false;
        if(!lRange.contains(line)) return true;
        if(op<firstPoints.get(line-lowestLine).op||op>lastPoints.get(line-lowestLine).op) return true;
        return or;
    }


    ArrayList <EdgeElementUnitNode> findEEUNodes(int op){
        ArrayList <EdgeElementUnitNode> EEUNodes=new ArrayList <EdgeElementUnitNode>();
        int index=op/(2*ws+1);//op is already corrected by the lowestOP
        PixelPositionNode ppnl=firstOrthogonalPoints.get(index),ppnu=lastOrthogonalPoints.get(index);
        int lowerLimit=ppnl.lineNumber+d1+b1-lowestLine,upperLimit=ppnu.lineNumber-lowestLine;
        int line=lowerLimit;
        while(line <upperLimit){
            EdgeElementUnitNode eeun=findEEUN(op,line,upperLimit);
            if(eeun.pValue<unitCutoff){
                EEUNodes.add(eeun);
            }
            line=eeun.rightEnd-lowestLine;
        }
        return EEUNodes;
    }


    EdgeElementUnitNode findEEUN(int op,int line, int upperLimit){//line and op is already corrected by lowestLine and smallestOP
        EdgeElementUnitNode eeun=null;
        double negligible=1;
        double left=block1Ave_FRR[line-b1-d1][op], leftMin=left, leftMax=left, right=block2Ave_FRR[line+b2][op], rightMin=right, rightMax=right, delta=right-left;
        int maxLine=line;
        while(line<upperLimit&&Math.abs(delta)<negligible){
            line++;
            left=block1Ave_FRR[line-b1-d1][op];
            right=block2Ave_FRR[line+b2][op];
            delta=right-left;
            if(left>leftMax)leftMax=left;
            if(left<leftMin) leftMin=left;
            if(right>rightMax) rightMax=right;
            if(right<rightMin) rightMin=right;
        }
        int leftStart=line;
        double maxDelta=Math.abs(delta);
        double sign=delta/maxDelta;
        maxLine=line;

        while(line<upperLimit&&sign*delta>=negligible){
            line++;
            left=block1Ave_FRR[line-b1-d1][op];
            right=block2Ave_FRR[line+b2][op];
            delta=right-left;
            if(Math.abs(delta)>maxDelta) {
                maxDelta=Math.abs(delta);
                maxLine=line;
            }
            if(left>leftMax)leftMax=left;
            if(left<leftMin) leftMin=left;
            if(right>rightMax) rightMax=right;
            if(right<rightMin) rightMin=right;
        }
        int rightEnd=line;
        double p=Math.min(pValue(frrNode.getPPN_LO(leftStart-b1-d1+lowestLine, op+smallestOP), frrNode.getPPN_LO(rightEnd+b2+lowestLine, op+smallestOP)),pValue(frrNode.getPPN_LO(maxLine-d1-b1+lowestLine, op+smallestOP), frrNode.getPPN_LO(maxLine+b2+lowestLine, op+smallestOP)));

        int leftEnd=maxLine-b1-d1,rightStart=maxLine+b1;
        if(leftStart>leftEnd)leftStart=leftEnd;
        if(rightEnd<rightStart)rightEnd=rightStart;

        eeun=new EdgeElementUnitNode(p,leftMin, rightMax, leftStart+lowestLine,leftEnd+lowestLine,rightStart+lowestLine, rightEnd+lowestLine, maxLine+lowestLine, ws, op+smallestOP, b1, b2, d1, d2);
        return eeun;
    }



    /*ArrayList <EdgeElementUnitNode> findEEUNodes(int op){
        ArrayList <EdgeElementUnitNode> EEUNodes=new ArrayList <EdgeElementUnitNode>();
        int index=(op-firstOrthogonalPoints.get(0).op)/(2*ws+1);
        PixelPositionNode ppnl=firstOrthogonalPoints.get(index),ppnu=lastOrthogonalPoints.get(index);
        int lowerLimit=ppnl.lineNumber,upperLimit=ppnu.lineNumber;

        double[][] blockAve=block1Ave_FRR;
        if(d1>d2) blockAve=block2Ave_FRR;

        int line=lowerLimit,lowerLine=lowerLimit,upperLine;
        while(lowerLine <=upperLimit){
            upperLine=findBlockAveExtreme(lowerLine-lowestLine,op-smallestOP,upperLimit-lowestLine,blockAve);
            EdgeElementUnitNode eeun=buildEEUN(lowerLine,upperLine);
            if(eeun.pValue<unitCutoff){
                EEUNodes.add(eeun);
            }
            lowerLine=upperLine;
        }
        return EEUNodes;
    }This method need to be completed* The above method could be a better alternative, but I am keeping this not finished method in case this one is a better approach. 3/21/09
    */
    int findBlockAveExtreme(int lowerLine, int op, int upperLimit, double[][] blockAve){
        int upperLine=lowerLine+1;
        double ave0=blockAve[lowerLine][op],ave1=blockAve[upperLine][op],ave2;
        while(ave1-ave0==0.&&upperLine<=upperLimit){
            upperLine++;
            ave1=blockAve[upperLine][op];
        }
        double del1=ave1-ave0;
        if(upperLine<upperLimit) {
            upperLine++;
            ave2=blockAve[upperLine][op];
        }else{
            return upperLine;
        }
        ave1=ave2;
        while(upperLine<=upperLimit&&(ave2-ave1)*del1>=0){
            upperLine++;
            ave1=ave2;
            ave2=blockAve[upperLine][op];
        }
        return upperLine;
    }


    ArrayList <EdgeElementUnitNode> findEEUNodes_o(int op, int lowerLimit,int upperLimit){
        ArrayList <EdgeElementUnitNode> EEUNodes=new ArrayList <EdgeElementUnitNode>();

        int op0=firstOrthogonalPoints.get(0).op;
        int length=2*ws+1;
        int steps;
        double p;
        PixelPositionNode ppn=firstOrthogonalPoints.get((op-op0)/length);
        frrNode.moveLeft(ppn, -d1-b1);
        int line=ppn.lineNumber;
        if(lowerLimit>=upperLimit){
            line=line;
        }
        while(line-b1-d1>=lowerLimit&&line+b2+d2<=upperLimit){
            p=pValue(ppn);/*
            while(p>lowCutoff){
                frrNode.moveLeft(ppn, 1);
                line=ppn.lineNumber;
                if((line-d1-b1<lowerLimit||line+b2+d2>upperLimit)) return EEUNodes;
                p=pValue(ppn);
            }*/
            EdgeElementUnitNode eeun=completeEE(ppn, lowerLimit, upperLimit);//p<lowCutoff;
            p=eeun.pValue;
            if(p<unitCutoff){
                EEUNodes.add(eeun);
            }
                if(eeun.maxDeltaLine==126&&op==-30){
                    op=op;
                }
            lowerLimit=eeun.rightEnd;
            steps=eeun.rightEnd+b1+d1-ppn.lineNumber;
            frrNode.moveLeft(ppn, -steps);
            line=ppn.lineNumber;
        }
        return EEUNodes;
    }

    public EdgeElementUnitNode completeEE(PixelPositionNode ppno, int lowerLimit, int upperLimit){//This method uses ms1 and ms2 computed in pValue(ppn). So should pay attention the sequence in which this method is called.
        EdgeElementUnitNode eeun=null;
        double p,maxDelta=-256.;
        int maxLine=0;

        double left=ms1.mean,left0=left,leftMin=left,right=ms2.mean,right0=right,rightMax=right;
        double height0=right-left;
        int i,line,op;
        double sh=height0/Math.abs(height0);
        int numRD=0,numLI=0;//the number of right decreasing and left increasing.
        int cutoffRD=1,cutoffLI=1;
        op=ppno.op;

        endLine=ppno.lineNumber+b2;
        startLine=ppno.lineNumber-b1-d1;
        line=ppno.lineNumber;
        boolean rightIncreasing=true,leftDecreasing=true;
        maxLine=line;
        maxDelta=Math.abs(right-left);
        while(leftDecreasing||rightIncreasing){
            line++;
            if(line+b2>upperLimit) break;
            right0=right;
            left0=left;

            right=block1Ave_FRR[line+b2-lowestLine][op-smallestOP];
            if(rightIncreasing){
                if(sh*(right-rightMax)>0){
                    rightMax=right;
                    endLine=line+b2;
                }else if(sh*(right-right0)>0){
                    right0=right;
                }else{
                    numRD++;
                    if(numRD>cutoffRD) rightIncreasing=false;
                }
            }

            left=block1Ave_FRR[line-b1-d1-lowestLine][op-smallestOP];
            if(leftDecreasing){
                if(sh*(left-leftMin)<0){
                    leftMin=left;
                    startLine=line-b1-d1;
                }else if(sh*(left0-left)>0){
                    left0=left;
                }else{
                    numLI++;
                    if(numLI>cutoffLI) leftDecreasing=false;
                }
            }
            delta=Math.abs(right-left);
            if(delta>maxDelta){
                maxDelta=delta;
                maxLine=line;
            }
        }

        numRD=0;
        numLI=0;
        rightIncreasing=true;
        leftDecreasing=true;
        line=ppno.lineNumber;
        while(leftDecreasing||rightIncreasing){
            line--;
            if(line-b1-d1<lowerLimit) break;
            right0=right;
            left0=left;

            right=block1Ave_FRR[line+b2-lowestLine][op-smallestOP];
            if(rightIncreasing){
                if(sh*(right-rightMax)>0){
                    rightMax=right;
                    endLine=line+b2;
                }else if(sh*(right-right0)>0){
                    right0=right;
                }else{
                    numRD++;
                    if(numRD>cutoffRD) rightIncreasing=false;
                }
            }

            left=block1Ave_FRR[line-b1-d1-lowestLine][op-smallestOP];

            if(leftDecreasing){
                if(sh*(left-leftMin)<0){
                    leftMin=left;
                    startLine=line-b1-d1;
                }else if(sh*(left0-left)>0.){
                    left0=left;
                }else{
                    numLI++;
                    if(numLI>cutoffLI) leftDecreasing=false;
                }
            }
            delta=Math.abs(right-left);
            if(delta>maxDelta){
                maxDelta=delta;
                maxLine=line;
            }
        }
        int leftStart=startLine,leftEnd=maxLine-b1-d1,rightStart=maxLine+b1,rightEnd=endLine;
        p=pValue(frrNode.getPPN_LO(startLine, op),frrNode.getPPN_LO(endLine,op));
        if(leftStart>leftEnd)leftStart=leftEnd;
        if(rightEnd<rightStart)rightEnd=rightStart;
        eeun=new EdgeElementUnitNode(p,leftMin, rightMax, leftStart,leftEnd,rightStart, rightEnd, maxLine, ws, op, b1, b2, d1, d2);
        return eeun;
    }

    double pValue(PixelPositionNode ppn1,PixelPositionNode ppn2){
        double p=1.;
        int length=2*ws+1;
        int n1,n2;
        double mean1,mean2,sem1,sem2;
        n1=d1*length;
        n2=d2*length;
        if(outOfRange(ppn1)||outOfRange(ppn2)) return 1.;

        mean1=block1Ave_FRR[ppn1.lineNumber-lowestLine][ppn1.op-smallestOP];
        sem1=block1Ave2_FRR[ppn1.lineNumber-lowestLine][ppn1.op-smallestOP];
        mean2=block2Ave_FRR[ppn2.lineNumber-lowestLine][ppn2.op-smallestOP];
        sem2=block2Ave2_FRR[ppn2.lineNumber-lowestLine][ppn2.op-smallestOP];
        ms1.updateMeanSquareSum(n1,mean1,sem1);
        ms2.updateMeanSquareSum(n2,mean2,sem2);
        p=getPValue(ms1,ms2);
        return p;
    }
}