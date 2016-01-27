/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import FluoObjects.IntensityPeakObjectHandler;
import FluoObjects.IntensityPeakObject;
import java.util.ArrayList;
import ij.ImagePlus;
import utilities.CustomDataTypes.intRange;
import ij.gui.Roi;
import utilities.ArrayofArrays.IntArray;
import utilities.ArrayofArrays.IntArray2;
import utilities.QuickSort;
import utilities.CommonMethods;
import java.awt.Color;
import java.awt.Point;
import ImageAnalysis.ContourFollower;
import utilities.ArrayofArrays.IntRangeArray;
import utilities.ArrayofArrays.IntRangeArray2;
import java.awt.Rectangle;
import utilities.QuickSortInteger;
import ImageAnalysis.LandscapeAnalyzer;
import ImageAnalysis.IPOPixelHeightsHandler;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.CommonStatisticsMethods;
import utilities.Geometry.ImageShapes.ImageShapeHandler;
import utilities.statistics.MeanSem0;
import utilities.Geometry.ImageShapes.RectangleImage;
import utilities.statistics.GaussianDistribution;
import FluoObjects.SliceIPOGaussianNode;
import utilities.Geometry.ImageShapes.Ring;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.statistics.MeanSem1;

/**
 *
 * @author Taihao
 */
public class IntensityPeakObjectHandler_GaussianNodeGroup extends IntensityPeakObjectHandler{
    double AmpCutoff,TotalSignalCutoff;
    ArrayList<IPOGaussianNode> m_cvIPOGNodes;
    SliceIPOGaussianNode m_cSliceIPO;
    ArrayList<IPOGaussianNode>[] m_pcvIPOGGs;
    public IntensityPeakObjectHandler_GaussianNodeGroup(){

    }
    public IntensityPeakObjectHandler_GaussianNodeGroup(SliceIPOGaussianNode cSliceIPO, int searchingDist, int r, int pixels[][], int pixelsCompensated[][],int[][] pixelst,
            int[][] pnScratch, int w, int h, int trackingLength,ImageShape cSigShape, ImageShape cSurShape){
        this.pnScratch=pnScratch;
        m_cSliceIPO=cSliceIPO;
        int slice=m_cSliceIPO.slice;
        m_cRefMS=null;
        m_dvBackgroundPixels=null;
//        m_nStackOffset=nStackOffset;
        m_cvIPOGNodes=m_cSliceIPO.IPOs;
//        m_cvLocalMaxima=localMaxima;
        buildLocalMaxima();
//        m_dvPixelHeights=dvPixelHeights;
//        buildPixelHeights();
        m_dvPixelHeights0=null;
        m_pnPercentileIndexes=null;
        m_pdPercentileCutoff=null;
        m_dvBackgroundPixelValues=null;
        m_cSigShape=cSigShape;
        m_cSurShape=cSurShape;
        this.z=slice-1;//slice number starts from 1, but z and t start from 0 in my system
        this.t=slice-1;
        this.r=r;
        this.w=w;
        this.h=h;
        this.searchingDist=searchingDist;
        this.pixelsp=null;
        this.pixelsCompensated=pixelsCompensated;
        this.pixelst=pixelst;
        this.trackingLength=trackingLength;
        this.minPeakPercentileIndex=0;
        this.backgroundPercentiles=null;
        IPOs=new ArrayList();
        backgroundIPOs=new ArrayList();
        int i,j;
        this.pixels=new int[h][w];
        backgroundIntensities=new int[16];
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                this.pixels[i][j]=pixels[i][j];
            }
        }

        m_bBasedOnPrecomputedPixelHeight=true;
        m_cvIPORings=null;
        AmpCutoff=m_cSliceIPO.RegionHCutoff;
        TotalSignalCutoff=m_cSliceIPO.TotalSignalCutoff;
        buildIPObjects_PrecomputedGaussianNodeGroup();
        calSignals();
        initIPOGrids();
        registerIPOs();
        initBackgroundIPOGrids();
        registerBackgroundIPOs();
    }
    void retrieveCutoffs(){
        int len=m_cvIPOGNodes.size(),i;
        AmpCutoff=Double.POSITIVE_INFINITY;
        TotalSignalCutoff=Double.POSITIVE_INFINITY;
        IPOGaussianNode IPOG;
        for(i=0;i<len;i++){
            IPOG=m_cvIPOGNodes.get(i);
            if(IPOG.dTotalSignal<TotalSignalCutoff) TotalSignalCutoff=IPOG.dTotalSignal;
            if(IPOG.Amp<AmpCutoff) AmpCutoff=IPOG.Amp;
        }
    }
    void buildLocalMaxima(){
        m_cvLocalMaxima=new ArrayList();
        intRange indexRange=new intRange();
        IPOGaussianNode IPOG;
        int len=m_cvIPOGNodes.size(),i;
        for(i=0;i<len;i++){
            IPOG=m_cvIPOGNodes.get(i);
//            m_cvLocalMaxima.add(new Point(IPOG.getXc(),IPOG.getYc()));
            indexRange.expandRange(IPOG.rIndex);
        }

        len=indexRange.getMax();
        ArrayList<IPOGaussianNode> pcvIPOGGs[]=new ArrayList[len];
        ArrayList<IPOGaussianNode> IPOGG;
        ArrayList<Point> points=new ArrayList();

        for(i=0;i<len;i++){
            pcvIPOGGs[i]=new ArrayList();
            points.add(null);
        }

        int index;
        Point pt;
        int num=0;
        len=m_cvIPOGNodes.size();
        for(i=0;i<len;i++){
            IPOG=m_cvIPOGNodes.get(i);
//            m_cvLocalMaxima.add(new Point(IPOG.getXc(),IPOG.getYc()));
            index=IPOG.rIndex-1;
            pcvIPOGGs[index].add(IPOG);
            pt=points.get(index);
            if(pt==null) {
                points.set(index, new Point(IPOG.xcr,IPOG.ycr));
                if(pcvIPOGGs[index].isEmpty()){
                    num=num;
                }
                num++;
            }
        }

        m_nNumLocalMaxima=num;
        m_cvLocalMaxima=new ArrayList();
        m_pcvIPOGGs=new ArrayList[num];
        len=indexRange.getMax();
        index=0;
        for(i=0;i<len;i++){
            IPOGG=pcvIPOGGs[i];
            if(IPOGG.isEmpty()) continue;
            m_pcvIPOGGs[index]=IPOGG;
            m_cvLocalMaxima.add(points.get(i));
            index++;
        }
    }
    void buildIPObjects_PrecomputedGaussianNodeGroup(){
        int x,y,i,j,len=m_cvIPOGNodes.size();
        Point p;
        double pixelHeight,pv,cutoff=0;
        IntensityPeakObject_GaussianNodeGroup IPOGG;
        MeanSem0 ms=CommonStatisticsMethods.buildMeanSem(pixelsCompensated);
        double mean=ms.mean,sd=ms.getSD();
        ArrayList<IPOGaussianNode> IPOGGs;
        for(i=0;i<m_nNumLocalMaxima;i++){
                p=m_cvLocalMaxima.get(i);
                x=p.x;
                y=p.y;
                IPOGGs=m_pcvIPOGGs[i];
                IPOGG=new IntensityPeakObject_GaussianNodeGroup(IPOGGs,r,pixels,w,h,IPOs.size(),trackingLength);
//                IPOG.setPixelHeight(IPOGNode.dTotalSignal);//this value is computed upon construction
                if(IPOGG.pixelHeight>=2*TotalSignalCutoff) IPOGG.bTrackOpening=true;
                IPOGG.setBackgroundPixel();
                IPOGG.setPeakPixel(pixels[y][x]);
                IPOGG.basedOnPrecomputedPixelHeight();
                IPOGG.pixelHeight0=IPOGG.getAmpAt(x, y);
                IPOGG.localMaximumIndex=i;
                pv=GaussianDistribution.Phi(IPOGG.pixelHeight0, 0, sd);//11924
                pv=1-pv;
                //this is the p value of finding a signle pixel with height of Amp. Need to implement computing the
                //p value of total signal.
//                if(pv<0.05){
                    IPOs.add(IPOGG);
//                }else{
//                    backgroundIPOs.add(IPOGG);
//                }
                if(pv<1E-20) {
                    pv=1E-20;
                }
                pv=Math.log10(pv);
                IPOGG.pValue=pv;
        }
    }

    public double getPeakPixelAve(int x, int y){
        double ave=0,num;
        int h=pixels.length,w=pixels[0].length;
        int i,j,j0=Math.max(0, x-1),j1=Math.min(x+1, w-1),i0=Math.max(0, y-1),i1=Math.min(h-1, y+1);
        ave=0;
        num=0;
        for(i=i0;i<=i1;i++){
            for(j=j0;j<=j1;j++){
                ave+=pixels[i][j];
            }
        }
        ave/=9;
        return ave;
    }

    public double getSurQuantile(int x, int y){
        m_cSurShape.setCenter(new Point(x,y));
        return ImageShapeHandler.getQuantile(pixelsCompensated, m_cSurShape, 0.5);
    }

    public double getPixelHeightCutoff(){
        return TotalSignalCutoff;
    }

    void calSignals(){
        boolean debug=false;
        int i,len=IPOs.size(),num,j,R=4;
        m_dvPixelHeights=new ArrayList();
        double sig,bkg,dTotalSignal;
        ImageShape cISbkg=new CircleImage(25),circle1=new CircleImage(1),bigCircle=new CircleImage(R),circle3=new CircleImage(3);
        ImageShape cISsig,cIS=new CircleImage(R);

        cISbkg.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle1.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle3.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        bigCircle.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

        ImageShapeHandler.filtPixels_Mean(pixelsCompensated, pixelst, circle1);

        ArrayList<IPOGaussianNode> IPOGs;
        IPOGaussianNode IPOG;
        IntensityPeakObject_GaussianNodeGroup IPO;
        ArrayList<Point> points=new ArrayList(),contour;
        Point pt,center;
        int area;
        double pixel,bigSig,peak1,peak3;
        int[][] totalPixels=null,signalPixels=null;
        int contourCutoff;
        MeanSem1 ms;
        for(i=0;i<len;i++){
            IPO=(IntensityPeakObject_GaussianNodeGroup)IPOs.get(i);
            IPOGs=IPO.IPOGNode.IPOGs;
            num=IPOGs.size();
            if(num==0) continue;
            IPOG=IPO.IPOGNode;
            center=IPOG.getCenter();

            if(debug){
                totalPixels=new int[h][w];
                signalPixels=new int[h][w];
            }
            circle1.setCenter(new Point(IPOG.xcr,IPOG.ycr));
            circle3.setCenter(new Point(IPOG.xcr,IPOG.ycr));
            bigCircle.setCenter(new Point(IPOG.xcr,IPOG.ycr));

            cISsig=new CircleImage(R);
            cISsig.setCenter(IPOG.getCenter());
            cISsig.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
            cIS.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

            for(j=1;j<num;j++){
                IPOG=IPOGs.get(j);
                cIS.setCenter(IPOG.getCenter());
                cISsig.mergeShape(cIS);
            }

//            ms=ImageShapeHandler.getContourMeanSem1(pixelst, 0, cISsig);
            ms=ImageShapeHandler.getContourMeanSem1(pixelst, 0, bigCircle);
    //        bkg=pixelst[IPOG.ycr][IPOG.xcr];
            bkg=ImageShapeHandler.getMean(pixelsCompensated, cISbkg);

            IPO.Amp=ImageShapeHandler.getMean(pixelsCompensated, circle1)+IPOGaussianNodeHandler.getAmpAt(IPOGs, IPOG.xcr, IPOG.ycr)-bkg;

            contourCutoff=(int)ms.max+20;
            IPO.buildIPOGContourShapes(pixelst, pnScratch, contourCutoff);

//            cISsig.getInnerPoints(points);
            bigCircle.getInnerPoints(points);
            num=points.size();
            sig=0;
            bigSig=0;
            area=0;
            for(j=0;j<num;j++){
                pt=points.get(j);
                pixel=pixelsCompensated[pt.y][pt.x]+IPOGaussianNodeHandler.getAmpAt(IPOGs,pt.x,pt.y);
                sig+=pixel;
                area++;
                if(debug){
                    totalPixels[pt.y][pt.x]=(int)pixel;
                    signalPixels[pt.y][pt.x]=(int)(pixel-bkg);
                }
            }

            dTotalSignal=sig-bkg*area;

            IPO.setTotalSignal(dTotalSignal);
            IPO.setBackground((int)(bkg+0.5));
            IPO.setArea(IPO.getContourShape().getArea());
            m_dvPixelHeights.add(dTotalSignal);
//            IPO.IPOGNode.setBackground(bkg);
//            IPO.IPOGNode.setTotalSignal(dTotalSignal);
            IPO.setPixelHeight(ImageShapeHandler.getMean(pixels, circle1)-bkg);
//            IPO.IPOGNode.setPixelHeight(ImageShapeHandler.getMean(pixels, circle));
            if(dTotalSignal>2*TotalSignalCutoff) IPO.bTrackOpening=true;
            if(debug){
//                CommonMethods.displayPixels(pixelsCompensated, "compensated slice" + IPOG.sliceIndex, ImagePlus.GRAY16);
                CommonMethods.displayPixels(totalPixels, "total slice" + IPOG.sliceIndex, ImagePlus.GRAY16);
//                CommonMethods.displayPixels(signalPixels, "signal slice" + IPOG.sliceIndex, ImagePlus.GRAY16);
            }
            peak1=ImageShapeHandler.getMean(pixels, circle1);
            peak3=ImageShapeHandler.getMean(pixels, circle3);
            IPO.setPeak1(peak1);
            IPO.setPeak3(peak3);
            debug=false;
        }
    }
}
