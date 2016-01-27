/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import utilities.CommonMethods;
import java.util.ArrayList;
import utilities.Geometry.ImageShapes.*;
import ImageAnalysis.ImageScanner;
import ij.ImagePlus;
import utilities.CustomDataTypes.intRange;
import utilities.statistics.Histogram;
import java.awt.Point;
import utilities.ArrayofArrays.PointArray;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.CustomDataTypes.IndexRegisterer;
import utilities.statistics.MeanSem0;
import utilities.QuickSort;
import ImageAnalysis.export_AutoCorrelation;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class PixelHeights {
    ImageShape m_signalShape;
    ImageShape m_surroundingShape;
    int[][]m_pixels;
    intRange m_xFRange, m_yFRange;
    public PixelHeights(intRange xFRange, intRange yFRange){
        setDefaultShapes();
        m_pixels=null;
    }
    void setDefaultShapes(){
        int rI=3,rO=6;
        m_signalShape=new CircleImage(rI,m_xFRange,m_yFRange);
        m_surroundingShape=new Ring(rI,rO,m_xFRange,m_yFRange);
    }
    public PixelHeights(ImageShape sigShape, ImageShape surShape, int[][] pixels){
        m_pixels=pixels;
        m_signalShape=sigShape;
        m_surroundingShape=surShape;
        m_xFRange=new intRange(sigShape.getXFrameRange());
        m_yFRange=new intRange(sigShape.getYFrameRange());
    }
    public void getPixelHeights(ArrayList<Point> points){

    }
    public static void getPixelHeights(ImagePlus impl, ImagePlus implh, int nChoice){
        switch(nChoice){
            case 0:
                getPixelHeights_ImageShape0(impl,implh);
                break;
            case 1:
                getPixelHeights_ImageShape1(impl,implh);
                break;
            default:
                break;
        }
    }
    public static void getPixelHeights_ImageShape0(ImagePlus impl, ImagePlus implh){
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        int pixels0[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        double dPixelRange[]=new double[2];
        CommonMethods.getPixelValueRange_Stack(impl, dPixelRange);
        Histogram hist;
        double dPercentile=0.5;

        intRange xFRange=new intRange(0,w-1), yFRange=new intRange(0,h-1);
        ArrayList <Histogram> hists=new ArrayList();
        ArrayList<ImageShape> shapes=new ArrayList();
        int ri=1,rf=5,r;
        shapes.add(new CircleImage(ri,xFRange,yFRange));
        hist=new Histogram();
        hist.setPercentile(dPercentile);
        hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
        hists.add(hist);
        for(r=ri;r<=rf;r++){
            shapes.add(new Ring(r,r+1,xFRange,yFRange));
            hist=new Histogram();
            hist.setPercentile(dPercentile);
            hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
            hists.add(hist);
        }
        int num=shapes.size();
        ImageScanner isnr=new ImageScanner(new intRange(0,w-1),new intRange(0,h-1),new intRange(0,h-1),new intRange(0,h-1),shapes);
        Point p,p0;
        double ph,m0,m=0,counts;
        int i,j,lt;
        int in;

        PointArray paNew=new PointArray(), paOld=new PointArray();
        ArrayList<Point> newPoints=paNew.m_pointArray, oldPoints=paOld.m_pointArray;
        while(true){
            p0=isnr.getPosition();
            counts=0.;
            for(i=0;i<num;i++){//update the hists
                hist=hists.get(i);
                isnr.getPoints(paNew, paOld, i);
                newPoints=paNew.m_pointArray;
                oldPoints=paOld.m_pointArray;

                lt=newPoints.size();
                for(j=0;j<lt;j++){
                    p=newPoints.get(j);
                    hist.addData(pixels[p.y][p.x]);
                }

                lt=oldPoints.size();
                for(j=0;j<lt;j++){
                    p=oldPoints.get(j);
                    hist.removeData(pixels[p.y][p.x]);
                }
            }
            hist=hists.get(0);
            ph=hist.getSum();
//            m0=hist.getMean();
            m0=hist.getPercentileValue();
            counts=hist.getTotalCounts();
            in=0;
            for(i=1;i<num;i++){
                hist=hists.get(i);
                m=hist.getMean();
                m=hist.getPercentileValue();
                if(m>m0) {
                    break;
                }
                counts+=hist.getTotalCounts();
                ph+=hist.getSum();
                m0=m;
                in=i;
            }
            ph-=counts*m0;
            pixels0[p0.y][p0.x]=(int)(ph+0.5);
            if(isnr.done()) break;
            isnr.move();
        }
        int nRange[]=new int[2];
        nRange[0]=0;
        nRange[1]=(int)CommonMethods.getMaxPixelValue(implh.getType());
        CommonMethods.setPixels(implh, pixels0,true);
//        CommonMethods.setPixels(implh, pixels0);
    }

    public static void getPixelHeights_ImageShape1(ImagePlus impl, ImagePlus implh){
        int w=impl.getWidth(),h=impl.getHeight();
        int pixels[][]=new int[h][w];
        int pixels0[][]=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(),pixels);
        double dPixelRange[]=new double[2];
        CommonMethods.getPixelValueRange_Stack(impl, dPixelRange);
        Histogram hist;
        double dPercentile=0.5;

        intRange xFRange=new intRange(0,w-1), yFRange=new intRange(0,h-1);
        ArrayList <Histogram> hists=new ArrayList();
        ArrayList<ImageShape> shapes=new ArrayList();
        int ri=3,rf=5,r;
        shapes.add(new CircleImage(ri,xFRange,yFRange));
        hist=new Histogram();
        hist.setPercentile(dPercentile);
        hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
        hists.add(hist);

        int dr=rf-ri;
        for(r=ri;r<rf;r+=dr){
            shapes.add(new Ring(r,r+dr,xFRange,yFRange));
            hist=new Histogram();
            hist.setPercentile(dPercentile);
            hist.update(0, dPixelRange[0], dPixelRange[1], 1.);
            hists.add(hist);
        }
        int num=shapes.size();
        ImageScanner isnr=new ImageScanner(new intRange(0,w-1),new intRange(0,h-1),new intRange(0,h-1),new intRange(0,h-1),shapes);
        Point p,p0;
        double ph,m0,m=0,counts;
        int i,j,lt;
        int in;

        PointArray paNew=new PointArray(), paOld=new PointArray();
        ArrayList<Point> newPoints=paNew.m_pointArray, oldPoints=paOld.m_pointArray;
        while(true){
            p0=isnr.getPosition();
            counts=0.;
            for(i=0;i<num;i++){//update the hists
                hist=hists.get(i);
                isnr.getPoints(paNew, paOld, i);
                newPoints=paNew.m_pointArray;
                oldPoints=paOld.m_pointArray;

                lt=newPoints.size();
                for(j=0;j<lt;j++){
                    p=newPoints.get(j);
                    hist.addData(pixels[p.y][p.x]);
                }

                lt=oldPoints.size();
                for(j=0;j<lt;j++){
                    p=oldPoints.get(j);
                    hist.removeData(pixels[p.y][p.x]);
                }
            }
            hist=hists.get(0);
            ph=hist.getSum();
//            m0=hist.getMean();
            m0=hist.getPercentileValue();
            counts=hist.getTotalCounts();
            in=0;
            for(i=1;i<num;i++){
                hist=hists.get(i);
                m=hist.getMean();
                m=hist.getPercentileValue();
                if(m>m0) {
                    break;
                }
                counts+=hist.getTotalCounts();
                ph+=hist.getSum();
                m0=m;
                in=i;
            }
            ph-=counts*m0;
            ph/=counts;
            pixels0[p0.y][p0.x]=(int)(ph+0.5);
            if(isnr.done()) break;
            isnr.move();
        }
        int nRange[]=new int[2];
        nRange[0]=0;
        nRange[1]=(int)CommonMethods.getMaxPixelValue(implh.getType());
//        CommonMethods.setPixels(implh, pixels0,true);
        CommonMethods.adjustMinimum(pixels0, 0);
        CommonMethods.setPixels(implh, pixels0);
    }

    public static void getPixelHeigths(ImagePlus impl, ImageShape sigShape, ImageShape surShape, ArrayList<Point> points, ArrayList<Double> pixelHeights, int nHType, double dQ){
        int[][] pixels=CommonMethods.getPixelValues(impl);
        pixelHeights.clear();
        int len=points.size();
        Point pt;
        double dSig, dSur;
        for (int i=0;i<len;i++){
            pt=points.get(i);
            sigShape.setCenter(pt);
            surShape.setCenter(pt);
            dSig=ImageShapeHandler.getMean(pixels, sigShape);
            switch(nHType){
                case export_AutoCorrelation.localMeanRef:
                    dSur=ImageShapeHandler.getMean(pixels, surShape);
                    break;
                case export_AutoCorrelation.localMedianRef:
                    dSur=ImageShapeHandler.getQuantile(pixels, surShape, dQ);
                    break;
                default:
                    dSur=0;
                    break;
            }
            pixelHeights.add(dSig-dSur);
        }
    }

    public static void getPixelHeights_localMaxima1(ImagePlus original, ImagePlus mask, ArrayList <Point> points,
            ArrayList<MeanSem0> sigPixelMSs, ArrayList<MeanSem0> surPixelMSs, ArrayList<Integer> sigNeighbors,
            ArrayList<Integer> surNeighbors, ArrayList<Integer> sigUncoveredN, ArrayList surUncoveredN){
        Runtime r = Runtime.getRuntime();
        sigPixelMSs.clear();
        surPixelMSs.clear();
        sigNeighbors.clear();
        surNeighbors.clear();
        sigUncoveredN.clear();
        surUncoveredN.clear();

        int h=original.getHeight(),w=original.getWidth();
        int pixelso[][]=new int[h][w];
        int pixelsm[][]=new int[h][w];

        CommonMethods.getPixelValue(original, mask.getCurrentSlice(), pixelso);
        CommonMethods.getPixelValue(mask, mask.getCurrentSlice(), pixelsm);

        int rI=4,rO=7;
        ImageShape sig=new ImageShape(new CircleImage(rI));
        ImageShape sur=new ImageShape(new Ring(rI,rO));
        sig.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        sur.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

        double background=CommonMethods.getMostProbablePixelValue(original, 20);

        IndexRegisterer iRG=new IndexRegisterer(0,h,rO,0,w,rO);

        CommonMethods.getSpecialLandscapePoints(pixelsm, w, h, LandscapeAnalyzer.localMaximum, points);

        int len=points.size(),len1,index,it;
        int i,j,x,y;
        Point p,pt;


        ImageShape[] sigShapes=new ImageShape[len], surShapes=new ImageShape[len];

        int peakPixelsm[]=new int[len];//pixel values of the local maxima (mask image)
        ArrayList<Integer> [] surNbrIndexes=new ArrayList[len];//indexes of sigShapes overlapping with surShapes
        ArrayList<Integer> [] sigNbrIndexes=new ArrayList[len];//indexes of sigShapes overlapping with sigShapes

        ImageShape isSig;
        ImageShape isSigt;
        ImageShape isSur;
        ImageShape isSurt;
        ImageShape overlapShape;
        MeanSem0 mst,sigMS,surMS;

        double coor[]=new double[2], prox[]=new double[2];
        prox[0]=rI+rO;
        prox[1]=rI+rO;



        for(i=0;i<len;i++){//registering the indexes, and creating shapes
            p=points.get(i);
            coor[0]=p.y;
            coor[1]=p.x;
            iRG.addIndex(coor, i);
            sigShapes[i]=new ImageShape(sig);
            surShapes[i]=new ImageShape(sur);
            sigShapes[i].setCenter(p);
            surShapes[i].setCenter(p);
            peakPixelsm[i]=pixelsm[p.y][p.x];
        }

        ArrayList<Integer> indexes=new ArrayList(),indexest;//storing indexes of the overlapping neighbors

        int nMaxNbrs=0;
        ImageShape sigt=new ImageShape(sig);
        for(i=0;i<len;i++){
            p=points.get(i);
            coor[0]=p.y;
            coor[1]=p.x;
            iRG.getIndexes(coor, prox, indexes);

            len1=indexes.size();
            for(it=0;it<len1;it++){
                index=indexes.get(it);
                if(index==i){
                    indexes.remove(it);
                    break;
                }
            }

            sur.setCenter(p);
            indexest=ImageShapeHandler.getOverlappingCenterIndexes(sur, sig, points, indexes);
            surNbrIndexes[i]=indexest;

            it=indexest.size();
            if(it>nMaxNbrs) nMaxNbrs=it;
            sig.setCenter(p);
            sigNbrIndexes[i]=ImageShapeHandler.getOverlappingCenterIndexes(sig, sigt, points, indexest);
        }

        double[][] dVs2=new double[nMaxNbrs][];//hould them for using later.
        int[][] nVs2=new int[nMaxNbrs][];
        for(i=0;i<nMaxNbrs;i++){
            dVs2[i]=new double[i+1];
            nVs2[i]=new int[i+1];
        }

        double dVs[];
        int nVs[];
        ImageShape ist;

        for(i=0;i<len;i++){//sorting the neighbors
            indexes=sigNbrIndexes[i];
            len1=indexes.size();
            if(len1<2) continue;
            dVs=dVs2[len1-1];
            for(j=0;j<len1;j++){
                p=points.get(indexes.get(j));
                dVs[j]=pixelsm[p.y][p.x];
            }

            QuickSort.quicksort(dVs,indexes);
        }

        MeanSem0[] surMSs=new MeanSem0[len];
        MeanSem0 surMST=new MeanSem0();
        for(i=0;i<len;i++){//excluding overlapping area from sur shapes, and computing the pixel ms.
            indexes=surNbrIndexes[i];
            len1=indexes.size();
            ist=surShapes[i];
            for(j=0;j<len1;j++){
                sig.setCenter(points.get(indexes.get(j)));
                ist.excludeShape(sig);
            }
            surMSs[i]=getPixelMeanSem(pixelso,ist);
            surMST.mergeSems(surMSs[i]);
            surNeighbors.add(len1);
            surUncoveredN.add(ist.getArea());
        }

        ArrayList<Integer> emptySurs=new ArrayList();
        for(i=0;i<len;i++){//computing sur pixel ms
            indexes=surNbrIndexes[i];
            len1=indexes.size();
            mst=new MeanSem0(surMSs[i]);
            for(j=0;j<len1;j++){
                mst.mergeSems(surMSs[indexes.get(j)]);
            }
            surPixelMSs.add(mst);
            if(mst.n==0){
                mst.update(surMST);
                emptySurs.add(i);
            }
        }
/*
        for(i=0;i<len;i++){//computing sur pixel ms
            indexes=sigNbrIndexes[i];
            len1=indexes.size();
            mst=new MeanSem0(surMSs[i]);
            for(j=0;j<len1;j++){
                mst.mergeSems(surMSs[indexes.get(j)]);
            }
            surPixelMSs.add(mst);
            if(mst.n==0){
                mst.update(surMST);
                emptySurs.add(i);
            }
        }
*/
        double peakPV, ratio, nbrPeak, dm, w1, w2;
        boolean belowSur=false;
        MeanSem0 surMSt;
        int xt=128,yt=68;
        double surMean=0;
        for(i=0;i<len;i++){
            indexes=sigNbrIndexes[i];
            len1=indexes.size();
            p=points.get(i);
            surMean=surPixelMSs.get(i).mean;

            if(p.x==xt&&p.y==yt)
            {
                p=p;
            }
            peakPV=pixelsm[p.y][p.x];
//            w1=peakPV-surMean;
            w1=peakPV-background;
            belowSur=false;
            if(w1<0) {
                w1=0;
                belowSur=true;
            }

            sigt=sigShapes[i];
            sigMS=new MeanSem0();
            for(j=0;j<len1;j++){
                index=indexes.get(j);
                if(i==index) continue;
                pt=points.get(index);
                nbrPeak=pixelsm[pt.y][pt.x];
//                surMean=surPixelMSs.get(index).mean;

                if(!belowSur){
//                    w2=nbrPeak-surMean;
                    w2=nbrPeak-background;
                    if(w2<0) w2=0;
                    ratio=w2/(w1+w2);
                }else{
                    ratio=1;
                }

                sig.setCenter(pt);

                overlapShape=new ImageShape(sigt);
                overlapShape.overlapShape(sig);
                mst=getPixelMeanSem(pixelso,overlapShape);

                dm=mst.mean-surMean;
                if(dm>0){
                    dm*=ratio;
                    mst.shiftMean(-dm);
                }
                
                sigMS.mergeSems(mst);
                sigt.excludeShape(overlapShape);
            }
            sigMS.mergeSems(getPixelMeanSem(pixelso,sigt));
            sigPixelMSs.add(sigMS);
            sigUncoveredN.add(sigt.getArea());
            sigNeighbors.add(len1);
        }
    }
    public static void getPixelHeights_localMaxima10(ImagePlus original, ImagePlus mask, ArrayList <Point> points,
            ArrayList<MeanSem0> sigPixelMSs, ArrayList<MeanSem0> surPixelMSs, ArrayList<Integer> sigNeighbors,
            ArrayList<Integer> surNeighbors, ArrayList<Integer> sigUncoveredN, ArrayList surUncoveredN){
        Runtime r = Runtime.getRuntime();
        sigPixelMSs.clear();
        surPixelMSs.clear();
        sigNeighbors.clear();
        surNeighbors.clear();
        sigUncoveredN.clear();
        surUncoveredN.clear();

        int h=original.getHeight(),w=original.getWidth();
        int pixelso[][]=new int[h][w];
        int pixelsm[][]=new int[h][w];

        CommonMethods.getPixelValue(original, mask.getCurrentSlice(), pixelso);
        CommonMethods.getPixelValue(mask, mask.getCurrentSlice(), pixelsm);

        int rI=4,rO=7;
        ImageShape sig=new ImageShape(new CircleImage(rI));
        ImageShape sur=new ImageShape(new Ring(rI,rO));
        sig.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        sur.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

        IndexRegisterer iRG=new IndexRegisterer(0,h,rO,0,w,rO);

        CommonMethods.getSpecialLandscapePoints(pixelsm, w, h, LandscapeAnalyzer.localMaximum, points);

        int len=points.size(),len1,index,it;
        int i,j,x,y;
        Point p,pt;


        ImageShape[] sigShapes=new ImageShape[len], surShapes=new ImageShape[len];
        MeanSem0[] surMSs=new MeanSem0[len];

        int peakPixelsm[]=new int[len];//pixel values of the local maxima (mask image)
        int[][] neighborIndexes=new int[len][];//indexes of sigShapes overlapping with a surShape

        ImageShape isSig;
        ImageShape isSigt;
        ImageShape isSur;
        ImageShape isSurt;
        ImageShape overlapShape;
        MeanSem0 mst,sigMS,surMS;

        double coor[]=new double[2], prox[]=new double[2];
        prox[0]=rI+rO;
        prox[1]=rI+rO;


        for(i=0;i<len;i++){//registering the indexes
            p=points.get(i);
            coor[0]=p.y;
            coor[1]=p.x;
            iRG.addIndex(coor, i);
            isSig=new ImageShape(sig);
            isSur=new ImageShape(sur);
            isSig.setCenter(p);
            isSur.setCenter(p);
            sigShapes[i]=isSig;
            surShapes[i]=isSur;
            peakPixelsm[i]=pixelsm[p.y][p.x];
        }


        int nMaxNeighbors=0;

        double peakPixelst[];//pixel values (mask image) of the neighboring local maxima

        ArrayList<ImageShape> surSigs=new ArrayList();
        ArrayList <double[]> peakPixelsts=new ArrayList();
        //surSigs, ppIndexests, peakPixelsts are objects and arrays pregenerated, and used for holding the neighboring shapes, local maxima indexes and the pixel values
        //at the local maxima. These are arrays of various size pregenerated so that to be used in the case of different number of neiboring shapes. A given size of
        //of array will bo reused for a image shape with a given number of overlapping neighbors, instead of regenerating ones. This is out of the code performance concern.

        ArrayList<Integer> indexes=new ArrayList();//storing indexes of the overlapping neighbors
        ArrayList <Integer> indexest=new ArrayList();//storing indexes of the neighbors in the range

        double peakPV,ratio;

        int sigNbrs=0,surNbrs=0;
        for(i=0;i<len;i++){
//            r.gc();
            sigMS=new MeanSem0();
            surMS=new MeanSem0();

//            if((i%10)==1) IJ.showStatus("pixel height: i= "+utilities.PrintAssist.ToString(i));
            IJ.showStatus("pixel height: i= "+utilities.io.PrintAssist.ToString(i));
            p=points.get(i);

            x=p.x;
            y=p.y;
            peakPV=pixelsm[y][x];

            coor[0]=y;
            coor[1]=x;
            iRG.getIndexes(coor, prox, indexest);

            len1=indexest.size();
            indexes.clear();

            sur.setCenter(p);
            for(it=0;it<len1;it++){
                index=indexest.get(it);
                if(index==i) continue;
                pt=points.get(index);
                sig.setCenter(pt);
                if(!sur.overlapping(sig)) continue;
                indexes.add(index);
            }

            len1=indexes.size();

            if(len1==0) {
                neighborIndexes[i]=new int[0];
                sigPixelMSs.add(getPixelMeanSem(pixelso,sigShapes[i]));
                surMSs[i]=getPixelMeanSem(pixelso,surShapes[i]);
                sigNeighbors.add(0);
                surNeighbors.add(0);
                sigUncoveredN.add(sig.getArea());
                surUncoveredN.add(sur.getArea());
                continue;
            }

            if(len1>nMaxNeighbors){
                for(it=nMaxNeighbors;it<len1;it++){
                    surSigs.add(new ImageShape(sig));
                    peakPixelsts.add(new double[it+1]);
                }

                nMaxNeighbors=len1;
            }

            neighborIndexes[i]=new int[len1];

            peakPixelst=peakPixelsts.get(len1-1);

            for(j=0;j<len1;j++){
                index=indexes.get(j);
                neighborIndexes[i][j]=index;
                pt=points.get(index);
                peakPixelst[j]=pixelsm[pt.y][pt.x];
            }

            QuickSort.quicksort(peakPixelst, neighborIndexes[i]);

            isSig=sigShapes[i];
            isSur=surShapes[i];

            sigNbrs=0;
            surNbrs=0;

            for(j=0;j<len1;j++){
                index=neighborIndexes[i][j];
                if(i==index) continue;

                pt=points.get(index);
                ratio=peakPV/(peakPV+pixelsm[pt.y][pt.x]);

                isSigt=surSigs.get(j);
                isSigt.setCenter(pt);

                if(isSig.overlapping(isSigt)){
                    overlapShape=new ImageShape(isSig);
                    overlapShape.overlapShape(isSigt);
                    mst=getPixelMeanSem(pixelso,overlapShape);
                    mst.scale(ratio);
                    sigMS.mergeSems(mst);
                    isSig.excludeShape(overlapShape);
                    sigNbrs++;
                }

                isSur.excludeShape(isSigt);
//                r.gc();
            }
            sigMS.mergeSems(getPixelMeanSem(pixelso,isSig));
            sigPixelMSs.add(sigMS);
            surMSs[i]=getPixelMeanSem(pixelso,isSur);
            sigNeighbors.add(sigNbrs);
            surNeighbors.add(len1);
            sigUncoveredN.add(isSig.getArea());
            surUncoveredN.add(isSur.getArea());
        }

        for(i=0;i<len;i++){
            mst=new MeanSem0();
            surPixelMSs.add(mst);
            len1=neighborIndexes[i].length;
            for(j=0;j<len1;j++){
                index=neighborIndexes[i][j];
                mst.mergeSems(surMSs[index]);
            }
        }
    }

    public static MeanSem0 getPixelMeanSem(int[][]pixels,ImageShape shape){
        MeanSem0 pixelMS=new MeanSem0();
        int pixel,sum=0,sum2=0;
        ArrayList<Point> points=new ArrayList();
        shape.getInnerPoints(points);
        int len=points.size();
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            pixel=pixels[p.y][p.x];
            sum+=pixel;
            sum2+=pixel*pixel;
        }
        double m;
        if(len==0)
            m=0;
        else
            m=sum/len;
        pixelMS.updateMeanSquareSum(len, m, sum2);
        return pixelMS;
    }
}
