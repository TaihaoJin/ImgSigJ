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
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.Geometry.ImageShapes.Ring;
import utilities.statistics.GaussianDistribution;
import FluoObjects.SliceIPOGaussianNode;
import utilities.CustomDataTypes.intRange;

/**
 *
 * @author Taihao
 */
public class IntensityPeakObjectHandler_GaussianNode extends IntensityPeakObjectHandler{
    double AmpCutoff,TotalSignalCutoff;
    ArrayList<IPOGaussianNode> m_cvIPOGNodes;
    SliceIPOGaussianNode m_cSliceIPO;
    public IntensityPeakObjectHandler_GaussianNode(){

    }
    public IntensityPeakObjectHandler_GaussianNode(SliceIPOGaussianNode cSliceIPO, int searchingDist, int r, int pixels[][], int pixelsCompensated[][],int[][] pixelst,
            int w, int h, int trackingLength,ImageShape cSigShape, ImageShape cSurShape){
        m_cSliceIPO=cSliceIPO;
        int slice=m_cSliceIPO.slice;
        m_cRefMS=null;
        m_dvBackgroundPixels=null;
//        m_nStackOffset=nStackOffset;
        m_cvIPOGNodes=m_cSliceIPO.IPOs;
        m_nNumLocalMaxima=m_cvIPOGNodes.size();
//        m_cvLocalMaxima=localMaxima;
        buildLocalMaxima();
//        m_dvPixelHeights=dvPixelHeights;
        buildPixelHeights();
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
        buildIPObjects_PrecomputedGaussianNode();
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
        IPOGaussianNode IPOG;
        int len=m_cvIPOGNodes.size(),i;
        for(i=0;i<len;i++){
            IPOG=m_cvIPOGNodes.get(i);
            m_cvLocalMaxima.add(new Point(IPOG.getXc(),IPOG.getYc()));
        }
    }
    void buildPixelHeights(){
        m_dvPixelHeights=new ArrayList();
        IPOGaussianNode IPOG;
        int len=m_cvIPOGNodes.size(),i;
        for(i=0;i<len;i++){
            IPOG=m_cvIPOGNodes.get(i);
            m_dvPixelHeights.add(IPOG.dTotalSignal);
        }
    }
    void buildIPObjects_PrecomputedGaussianNode(){

        int x,y,i,j,len=m_cvIPOGNodes.size();
        Point p;
        double pixelHeight,pv,cutoff=0;
        IPOGaussianNode IPOGNode;
        IntensityPeakObject_GaussianNode IPOG;
        MeanSem0 ms=CommonStatisticsMethods.buildMeanSem(pixelsCompensated);
        double mean=ms.mean,sd=ms.getSD();
        for(i=0;i<len;i++){
                p=m_cvLocalMaxima.get(i);
                x=p.x;
                y=p.y;
                IPOGNode=m_cvIPOGNodes.get(i);
                IPOG=new IntensityPeakObject_GaussianNode(IPOGNode,r,pixels,w,h,IPOs.size(),trackingLength);
                IPOG.setPixelHeight(IPOGNode.dTotalSignal);
                if(IPOG.pixelHeight>=2*TotalSignalCutoff) IPOG.bTrackOpening=true;
                IPOG.setBackgroundPixel((int)(IPOGNode.cnst+0.5));
                IPOG.setPeakPixel(pixels[y][x]);
                IPOG.basedOnPrecomputedPixelHeight();
                IPOG.pixelHeight0=IPOGNode.Amp;
                IPOG.localMaximumIndex=i;
                pv=GaussianDistribution.Phi(IPOGNode.Amp, 0, sd);//11924
                pv=1-pv;
                //this is the p value of finding a signle pixel with height of Amp. Need to implement computing the
                //p value of total signal.
                if(pv<0.05){
                    IPOs.add(IPOG);
                }else{
                    backgroundIPOs.add(IPOG);
                }
                pv=1.-pv;
                if(pv<1E-20) {
                    pv=1E-20;
                }
                pv=Math.log10(pv);
                IPOG.pValue=pv;
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
        int i,len=IPOs.size(),num,j,R=4;
        double sig,bkg,dTotalSignal;
        ImageShape cISbkg=new CircleImage(25),circle1=new CircleImage(1),circle3=new CircleImage(1),circle4=new CircleImage(1);
        cISbkg.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle1.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle3.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        circle4.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));

        ImageShapeHandler.filtPixels_Mean(pixels, pixelst, circle1);
        ImageShape cISsig,cIS=new CircleImage(R);

        IPOGaussianNode IPOG;
        IntensityPeakObject_GaussianNode IPO;
        ArrayList<Point> points=new ArrayList(),contour;
        Point pt;
        int area;
        double pixel,peak1,peak3;
        for(i=0;i<len;i++){
            IPO=(IntensityPeakObject_GaussianNode)IPOs.get(i);
            IPOG=IPO.IPOGNode;

            circle1.setCenter(new Point(IPOG.xcr,IPOG.ycr));
            circle3.setCenter(new Point(IPOG.xcr,IPOG.ycr));
            circle4.setCenter(new Point(IPOG.xcr,IPOG.ycr));
            IPO.Amp=ImageShapeHandler.getMean(pixelsCompensated, circle1)+IPOG.getAmpAt(IPOG.xcr, IPOG.ycr);

            bkg=ImageShapeHandler.getMean(pixelsCompensated, cISbkg);

            circle4.getInnerPoints(points);
            num=points.size();
            sig=0;
            area=0;

            for(j=0;j<num;j++){
                pt=points.get(j);
                pixel=pixelsCompensated[pt.y][pt.x]+IPOG.getAmpAt(pt.x,pt.y);
                sig+=pixel;
                area++;
            }

            dTotalSignal=sig-bkg*area;
            IPO.dTotalSignal=dTotalSignal;
            IPO.backgroundPixel=(int)(bkg+0.5);

            peak1=ImageShapeHandler.getMean(pixels, circle1);
            peak3=ImageShapeHandler.getMean(pixels, circle3);
            IPO.IPOGNode.setBackground(bkg);
            IPO.IPOGNode.setTotalSignal(dTotalSignal);
            IPO.IPOGNode.setPeak1(peak1);
            IPO.IPOGNode.setPeak3(peak3);
            IPO.IPOGNode.setArea(area);
            IPO.bTrackOpening=true;
//            if(dTotalSignal>2*TotalSignalCutoff) IPO.bTrackOpening=true;
        }
    }
    public void buildIPOGContourShape(){

    }
}
