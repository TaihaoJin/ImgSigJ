/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import utilities.TTestTable;
import utilities.ArrayofArrays.IntPair;
import utilities.CommonMethods;
import ij.process.ImageConverter;
import utilities.CustomDataTypes.intRange;
import java.awt.Color;
/**
 *
 * @author Taihao
 */
public class EdgeElementHandler_FRR {
    int debugginPaus;
    EdgeElement_FRR[] EdgeArray;
    ArrayList <EdgeElementNode_E> EdgeElementNodes;
    TTestTable tTable;
    int h,w,buffer;
    int[][]eDirection;
    int[][]gDirection;
    int[] b1,b2,d1,d2,ws;
    ImagePlus impl;
    ImagePlus implFirstPoints;
    ImagePlus imple;
    ImagePlus implr;
    ImagePlus impler;
    ImagePlus GridImage;
    int[][] edgeRGBs;
    int[][] eeNodeIndexes;
    double[][] pValues;
    double minPValue, threshold;
    String imageTitle;
    double maxDelta,deltaThreshold;
    int connectionGap;

    public EdgeElementHandler_FRR(ImagePlus impl0){
        imageTitle=impl0.getTitle();
        ImagePlus implO=CommonMethods.cloneImage(imageTitle,impl0);
        ImageConverter ic=new ImageConverter(implO);
        ic.convertToGray8();
        ImageProcessor bp=implO.getProcessor();
        initImages(bp);
        initResponseMatrices();
        initEdgeRGBs();
        tTable=new TTestTable();
        initEdgeElements();
//        drawGrids();
//        drawRegions();
        initEEList();
        ImagePlus implt=impl, implte=imple;

        threshold=1./((byte[])impl.getProcessor().getPixels()).length;
        threshold=1.;
        minPValue=1.;
        impl=implr;
        imple=impler;
        maxDelta=0.;
        findEdges();

        threshold=minPValue/10.;
        deltaThreshold=maxDelta;/*
        findEdges();
        showEdges();
        imple.show();
        impl.show();*/

        impl=implt;
        imple=implte;
        findEdges();

        showEdges();
        imple.show();
        impl.show();
        connectionGap=1;
//        implFirstPoints.show();
    }

    void findEdges(){
        resetEdgeList();
        int i,j,k;
        h=impl.getHeight();
        w=impl.getWidth();
        int len=h*w;
        byte[] bps=(byte[])impl.getProcessor().getPixels();
        int[] edgePixels=(int[])imple.getProcessor().getPixels();

        len=bps.length;
        int[][]pixels=new int[h+2*buffer][w+2*buffer];
        for(i=0;i<buffer;i++){
            for(j=0;j<w+2*buffer;j++){
                pixels[i][j]=0xff&bps[(int)(Math.random()*(len-1))];
            }
        }
        for(i=buffer+h;i<h+2*buffer;i++){
            for(j=0;j<w+2*buffer;j++){
                pixels[i][j]=0xff&bps[(int)(Math.random()*(len-1))];
            }
        }
        int o;
        for(i=buffer;i<h+buffer;i++){
            for(j=0;j<buffer;j++){
                pixels[i][j]=0xff&bps[(int)(Math.random()*(len-1))];
            }
            o=(i-buffer)*w;
        /*
            for(j=buffer;j<w+buffer;j++){
                pixels[i][j]=0xff&bps[o+j-buffer];
            }
*/
            for(j=w+buffer;j<w+2*buffer;j++){
                pixels[i][j]=0xff&bps[(int)(Math.random()*(len-1))];
            }
        }
        int p;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                o=i*w;
                p=0xff&bps[o+j];
                if(p>180){
                    p=p;
                }
                pixels[i+buffer][j+buffer]=0xff&bps[o+j];
            }
        }

        for(k=0;k<=7;k+=4){
            EdgeArray[k].setPixels(pixels);
            findEdges(k);
        }
        markEEs(EdgeElementNodes);
    }

    void showEdges(){
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        int i,j,o,type,index;
        byte[] bps=(byte[])impl.getProcessor().getPixels();
        int[] edgePixels=(int[])imple.getProcessor().getPixels();
        double pValue;
        double base=-Math.log10(threshold);
        int pixel=0,r,g,b;
        Color randomColor;
        double delta;
        deltaThreshold=0.;
        ImagePlus impl2=CommonMethods.newPlainRGBImage("edge elements", w, h, Color.BLACK);
        int[] pixels2=(int[])impl2.getProcessor().getPixels();
        EdgeElementNode_E een;
        int l,dell=3;

        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                r=0;
                g=0;
                b=0;
                pValue=pValues[i][j];
                pixel=(int)(-Math.log10(pValue)*50./base);
                if(pValue<threshold) {
                    pixel=(int)(250*(Math.log10(pValue))/(Math.log10(minPValue)));
                }else{
                    pixel=(int)(50*(Math.log10(pValue))/(Math.log10(threshold)));
                }

                pixel=(int)(255*(Math.log10(pValue))/(Math.log10(2.56e-17)));

//                pixel=(int)(255*(Math.log10(pValue))/(Math.log10(minPValue)));
                if(pixel>255) pixel=255;
                bps[o+j]=(byte)pixel;
                if(pValue<threshold){
                    index=eeNodeIndexes[i][j];
                    een=EdgeElementNodes.get(index);
                    type=een.type;
                    delta=Math.abs(een.height);
                    randomColor=CommonMethods.randomColor();
                    if(delta>deltaThreshold) edgePixels[o+j]=(edgeRGBs[type][0]<<16)|(edgeRGBs[type][1]<<8)|(edgeRGBs[type][2]);
//                    edgePixels[o+j]=(randomColor.getRed()<<16)|(randomColor.getGreen()<<8)|(randomColor.getBlue());
                    pixel=(int)Math.abs(255*een.height/maxDelta);
                    if(EdgeElementNodes.get(index).height>0){
                        b=pixel;
                    }else{
                        r=pixel;
                    }
                }
                pixels2[i*w+j]=(r<<16)|(g<<8)|b;
            }
        }
        impl2.show();
    }

    int getSign(int x){
        if(x==0) return 0;
        if(x>0) return 1;
        return -1;
    }
    void drawPoints(ImagePlus impl0, ArrayList <IntPair> ipa, int type){
        int[] pixels=(int[]) impl0.getProcessor().getPixels();
        int w=impl0.getWidth();
        int h=impl0.getHeight();
        int size=ipa.size();
        IntPair ip;
        int x,y;
        int pixel=(edgeRGBs[type][0]<<16)|(edgeRGBs[type][1]<<8)|(edgeRGBs[type][2]);
        for(int i=0;i<size;i++){
            ip=ipa.get(i);
            x=ip.x;
            y=ip.y;
            pixels[y*w+x]=pixel;
        }
    }
    void findEdges(int eeType){
        EdgeElement_FRR ee=EdgeArray[eeType];
        ee.buidEENodes(EdgeElementNodes,eeNodeIndexes,pValues);
    }

    void initEdgeElements(){
        EdgeArray=new EdgeElement_FRR[8];
        for(int k=0;k<8;k++){
            EdgeArray[k]=new EdgeElement_FRR(w, h, b1[k], b2[k], d1[k], d2[k], ws[k], buffer, k, eDirection[k],
                                gDirection[k], tTable, connectionGap);
        }
    }

    void initImages(ImageProcessor bp){
        buffer=15;
        impl=new ImagePlus(imageTitle+"_EdgePoints",bp);
        h=impl.getHeight();
        w=impl.getWidth();
        int w=bp.getWidth(),h=bp.getHeight();
        int len=w*h;
        byte[] bytePixels=(byte[])bp.getPixels();

        ByteProcessor bpr=new ByteProcessor(w,h);
        byte[] bytesr=(byte[])bpr.getPixels();
        for(int i=0;i<len;i++){
            bytesr[i]=bytePixels[i];
        }
        CommonMethods.randomize(bytesr);
        implr=new ImagePlus("Randomized Image of "+imageTitle,bpr);
        imple=CommonMethods.cloneImage("Edge Points "+imageTitle, impl);
        ImageConverter ic=new ImageConverter(imple);
        ic.convertToRGB();

        impler=CommonMethods.cloneImage("Edge Points of the randomized image", implr);
        ImageConverter icr=new ImageConverter(impler);
        icr.convertToRGB();

        implFirstPoints=CommonMethods.cloneImage("First Points", impl);
        ImageConverter icf=new ImageConverter(implFirstPoints);
        icf.convertToRGB();
    }

    void initEEList(){
        eeNodeIndexes=new int[h][w];
        pValues=new double[h][w];
        int i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                eeNodeIndexes[i][j]=-1;
                pValues[i][j]=1.;
            }
        }
        EdgeElementNodes=new ArrayList <EdgeElementNode_E>();
    }

    void initEdgeRGBs(){
        edgeRGBs=new int [8][3];
        edgeRGBs[0][0]=255;
        edgeRGBs[0][1]=0;
        edgeRGBs[0][2]=0;

        edgeRGBs[1][0]=255;
        edgeRGBs[1][1]=126;
        edgeRGBs[1][2]=0;

        edgeRGBs[2][0]=0;
        edgeRGBs[2][1]=255;
        edgeRGBs[2][2]=0;

        edgeRGBs[3][0]=255;
        edgeRGBs[3][1]=0;
        edgeRGBs[3][2]=255;

        edgeRGBs[4][0]=0;
        edgeRGBs[4][1]=0;
        edgeRGBs[4][2]=255;

        edgeRGBs[5][0]=0;
        edgeRGBs[5][1]=255;
        edgeRGBs[5][2]=255;

        edgeRGBs[6][0]=255;
        edgeRGBs[6][1]=0;
        edgeRGBs[6][2]=119;

        edgeRGBs[7][0]=255;
        edgeRGBs[7][1]=255;
        edgeRGBs[7][2]=0;
    }

    void initResponseMatrices(){
        eDirection=new int[8][2];
        gDirection=new int[8][2];
        ws=new int[8];
        b1=new int[8];
        b2=new int[8];
        d1=new int[8];
        d2=new int[8];

        eDirection[0][0]=0;
        eDirection[0][1]=1;
        ws[0]=5;
        b1[0]=0;
        b2[0]=0;
        d1[0]=3;
        d2[0]=3;

        eDirection[1][0]=-1;
        eDirection[1][1]=2;
        ws[1]=5;
        b1[1]=1;
        b2[1]=1;
        d1[1]=3;
        d2[1]=3;

        eDirection[2][0]=-1;
        eDirection[2][1]=1;
        ws[2]=5;
        b1[2]=0;
        b2[2]=0;
        d1[2]=3;
        d2[2]=3;

        eDirection[3][0]=-2;
        eDirection[3][1]=1;
        ws[3]=5;
        b1[3]=1;
        b2[3]=1;
        d1[3]=3;
        d2[3]=3;

        eDirection[4][0]=-1;
        eDirection[4][1]=0;
        ws[4]=5;
        b1[4]=0;
        b2[4]=0;
        d1[4]=3;
        d2[4]=3;

        eDirection[5][0]=-2;
        eDirection[5][1]=-1;
        ws[5]=5;
        b1[5]=1;
        b2[5]=1;
        d1[5]=3;
        d2[5]=3;

        eDirection[6][0]=-1;
        eDirection[6][1]=-1;
        ws[6]=5;
        b1[6]=0;
        b2[6]=0;
        d1[6]=3;
        d2[6]=3;

        eDirection[7][0]=-1;
        eDirection[7][1]=-2;
        ws[7]=5;
        b1[7]=1;
        b2[7]=1;
        d1[7]=3;
        d2[7]=3;

        for(int i=0;i<8;i++){
            ws[i]=1;
            b1[i]=0;
            b2[i]=0;
            d1[i]=3;
            d2[i]=3;
            gDirection[i][0]=eDirection[i][1];
            gDirection[i][1]=-eDirection[i][0];
        }
    }

    void drawGrids(){
        int i,j,o,h=501,w=501,pixel=0;
        int[] gridPixels=new int[h*w];
        GridImage=CommonMethods.getRGBImage("Regins", w, h, gridPixels);
        int delta=20;
        int[] pixels=(int[])GridImage.getProcessor().getPixels();
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                pixels[o+j]=(255<<16)|(255<<8)|255;
            }
        }
        for(i=0;i<h;i++){
            o=i*w;
            if(i%delta==0){
                for(j=0;j<w;j++){
                    pixels[o+j]=0;
                }
            }else{
                for(j=0;j<w;j++){
                    if(j%delta==0) pixels[o+j]=0;
                }
            }
            GridImage.show();
        }
    }
    void markEEs(ArrayList <EdgeElementNode_E> eens){
        int size=eens.size();
        EdgeElementNode_E eeNode;
        int i,j;
        intRange xRange=new intRange(0,w-1);
        intRange yRange=new intRange(0,h-1);
        for(i=0;i<size;i++){
            eeNode=eens.get(i);
            double pValue=eeNode.pValue;
            double delta=Math.abs(eeNode.height);
            if(delta>maxDelta) maxDelta=delta;
            if(pValue<threshold) EdgeElementNodes.add(eeNode);
            if(pValue<minPValue){
                minPValue=pValue;
            }
            PixelPositionNode ip;
            for(j=0;j<eeNode.numEPs;j++){
                ip=eeNode.edgePoints.get(j);
                if(!xRange.contains(ip.x)||!yRange.contains(ip.y)){
                    continue;
                }
                if(pValue<pValues[ip.y][ip.x]){
                    pValues[ip.y][ip.x]=pValue;
                    if(pValue<threshold) markEE(pValue,eeNode.edgePoints.get(j));
                }
            }
        }
    }

    void markEE(double pValue, PixelPositionNode ep){
        EdgeElementNode_E eeNodeo;
        int index=EdgeElementNodes.size()-1;
        int x=ep.x,y=ep.y;
        int index0=eeNodeIndexes[y][x];
        if(index0<0){
            eeNodeIndexes[y][x]=index;
        }else{
            eeNodeo=EdgeElementNodes.get(index0);
            if(pValue<eeNodeo.pValue){
                eeNodeIndexes[y][x]=index;
                eeNodeo.removeEdgePoint(y, x);
            }
        }
    }

    void resetEdgeList(){
        int i,j;
        EdgeElementNodes.clear();
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                eeNodeIndexes[i][j]=-1;
                pValues[i][j]=1.;
            }
        }
    }
}
