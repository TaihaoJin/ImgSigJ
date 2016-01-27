/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import utilities.CommonGuiMethods;
import utilities.CommonMethods;
import ij.ImagePlus;
import java.util.ArrayList;
import ij.IJ;
import utilities.io.PrintAssist;
import utilities.io.FileAssist;
import utilities.QuickFormatter;
import java.util.Formatter;
import utilities.statistics.MeanSem0;
import utilities.statistics.MeanSem1;
import utilities.QuickSortInteger;
import utilities.CustomDataTypes.intRange;
import ij.gui.Roi;
import java.awt.Rectangle;
import utilities.statistics.MeanSemFractional;
import java.awt.Point;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;
import utilities.ArrayofArrays.DoubleArray;
import utilities.CustomDataTypes.DoubleRange;
import utilities.statistics.Histogram;
import utilities.Geometry.ImageShapes.ImageShape;
import org.apache.commons.math.stat.regression.SimpleRegression;
import utilities.statistics.GaussianDistribution;
import utilities.statistics.PolynomialRegression;
import utilities.statistics.PolynomialLineFittingSegmentNode;
import java.util.StringTokenizer;
import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.MathException;
import utilities.CustomDataTypes.IntPair;
import utilities.Gui.Dialogs.OneTextFieldInputDialog;
import utilities.SortedDoubleArray;
import utilities.statistics.OneDKMeans;
import utilities.TTestTable;
import utilities.statistics.HistogramHandler;
import utilities.statistics.IndexOrganizerHist;

/**
 *
 * @author Taihao
 */
public class CommonStatisticsMethods {
    static public TTestTable tTestTable;
    public static final int Small=0,Large=1,TwoSide=2;
    
    //this method is disabled because imsl is no longer available for ShapiroWilkWTest(). 1/14/2014
    //ShapiroWilkWTest is also available from R, so need to implement this change next time when this method is needed.
    //http://stat.ethz.ch/R-manual/R-patched/library/stats/html/shapiro.test.html
    public static double testNormalityOfLowPixels(ImagePlus impl, int reductionFactor){
        double p=1;
        if(reductionFactor>1) impl=CommonMethods.getReducedImage(impl, reductionFactor);
        int h=impl.getHeight(),w=impl.getWidth();
        int[][]pixels=new int[h][w];
        CommonMethods.getPixelValue(impl, impl.getCurrentSlice(), pixels);
        ArrayList<Double> LowPixels=new ArrayList();
        int background=CommonMethods.getMostProbablePixelValue(impl, 0);
                CommonMethods.getSmallNumbers(pixels, LowPixels, background);

        String path=FileAssist.getFilePath("the symmetrized low value pixels", "","txt file", "txt", false);
        Formatter fm=QuickFormatter.getFormatter(path);
        int nSize=LowPixels.size();
        for(int i=0;i<nSize;i++){
            PrintAssist.printNumber(fm, LowPixels.get(i), 12, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();

        CommonMethods.symmetrizeNumbers(LowPixels, background,0.99);
        path=FileAssist.getFilePath("the symmetrized low value pixels", "","txt file", "txt", false);
        fm=QuickFormatter.getFormatter(path);
        nSize=LowPixels.size();
        for(int i=0;i<nSize;i++){
            PrintAssist.printNumber(fm, LowPixels.get(i), 12, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();

        ArrayList<Double> LowPixels1=new ArrayList();
        CommonMethods.reduceDataSize_distr(LowPixels, LowPixels1, 2000, 1.);

        path=FileAssist.getFilePath("the symmetrized low value pixels", "","txt file", "txt", false);
        fm=QuickFormatter.getFormatter(path);
        nSize=LowPixels1.size();
        for(int i=0;i<nSize;i++){
            PrintAssist.printNumber(fm, LowPixels1.get(i), 12, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();

        nSize=LowPixels.size();
        if(nSize>2000) nSize=2000;//largest size allowed by the method in NormalityTest.
        double[] pdLowPixels=new double[nSize];
        CommonMethods.getRandomSamples(LowPixels1, pdLowPixels, nSize);

        path=FileAssist.getFilePath("the symmetrized low value pixels", "","txt file", "txt", false);
        fm=QuickFormatter.getFormatter(path);

        for(int i=0;i<nSize;i++){
            PrintAssist.printNumber(fm, pdLowPixels[i], 12, 0);
            PrintAssist.endLine(fm);
        }
        fm.close();

/*        NormalityTest nt=new NormalityTest(pdLowPixels);
        try{p=nt.ShapiroWilkWTest();}
        catch(com.imsl.stat.NormalityTest.NoVariationInputException e){
            IJ.error("e");
        }
        catch(com.imsl.stat.InverseCdf.DidNotConvergeException e1){
            IJ.error("e1");
        }*/
        p=-1.;//this is to warn the caller for an invalid value
        return p;
    }
    public static int[][] dataReduction(int[][] pnData0, int rFactor, int cFactor){//sampling one point in a ractangle of xFactor by yFactor
        int rows0=pnData0.length,cols0=pnData0[0].length;
        int rows=rows0/rFactor, cols=cols0/cFactor,r,c,r0=rFactor/2,c0;
        int[][] pnData=new int[rows][cols];
        for(r=0;r<rows;r++){
            c0=cFactor/2;
            for(c=0;c<cols;c++){
                pnData[r][c]=pnData0[r0][c0];
                c0+=cFactor;
            }
            r0+=rFactor;
        }
        return pnData;
    }
    public static void makeMedianReflection(double[] pdData){
        int i,len=pdData.length,in,ix;
        double pdt[]=CommonStatisticsMethods.copyArray(pdData);
        int[] indexes=CommonMethods.getDefaultRankingIndexes(len);

        QuickSort.quicksort(pdt,indexes);
        double median=pdt[len/2];
        for(i=0;i<len/2;i++){
            in=indexes[i];
            ix=indexes[len-1-i];
            pdData[ix]=2*median-pdData[in];
        }
    }
    public static MeanSem0 buildMeanSem(double[] pdData){
        return buildMeanSem(pdData,0,pdData.length-1,1);
    }
    public static MeanSem0 buildMeanSem(double[] pdData, int nI, int nF, int nDelta){
        MeanSem0 ms=new MeanSem0();
        double mean=0, mss=0;
        int n=(nF-nI)/nDelta+1;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            d=pdData[i];
            mean+=d;
            mss+=d*d;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss);
        return ms;
    }
    public static MeanSem0 buildMeanSem(ArrayList <Double> dvData, int nI, int nF, int nDelta){
        MeanSem0 ms=new MeanSem0();
        double mean=0, mss=0;
        int n=(nF-nI)/nDelta+1;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            d=dvData.get(i);
            mean+=d;
            mss+=d*d;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss);
        return ms;
    }
    public static double[] copyArray(double[] pdA){
        if(pdA==null) return pdA;
        int dim1=pdA.length;
        double[] pdA1=new double[dim1];
        copyArray(pdA,pdA1);
        return pdA1;
    }
    public static boolean[] copyArray(boolean[] pbA){
        if(pbA==null) return pbA;
        int dim1=pbA.length;
        boolean[] pbA1=new boolean[dim1];
        copyArray(pbA,pbA1);
        return pbA1;
    }
    public static void copyArray(boolean pb1[], boolean pb2[]){
        int i,len=pb1.length;
        for(i=0;i<len;i++){
            pb2[i]=pb1[i];
        }
    }
    public static int copyArray(double[] pdA, ArrayList<Double> dvData){
        int dim1=pdA.length;
        for(int i=0;i<dim1;i++){
            dvData.add(pdA[i]);
        }
        return 1;
     }
    public static double[] copyArray(float[] pdA){
        int dim1=pdA.length;
        double[] pdA1=new double[dim1];
        copyArray(pdA,pdA1);
        return pdA1;
    }
    public static int[] copyArray(int[] pnA){
        if(pnA==null) return null;
        int dim1=pnA.length;
        int[] pnA1=new int[dim1];
        copyArray(pnA,pnA1);
        return pnA1;
    }
    public static ArrayList<String> copyStringArray(ArrayList<String> svA){
        ArrayList<String> strings=new ArrayList();
        int i,len=svA.size();
        for(i=0;i<len;i++){
            strings.add(new String(svA.get(i)));
        }
        return strings;
    }
    public static String[] copyStringArray1(ArrayList<String> svA){
        int i,len=svA.size();
        String[] ps=new String[len];
        for(i=0;i<len;i++){
            ps[i]=svA.get(i);
        }
        return ps;
    }
    public static ArrayList<String> copyStringArray(String[] psA){
        ArrayList<String> strings=new ArrayList();
        int i,len=psA.length;
        for(i=0;i<len;i++){
            strings.add(new String(psA[i]));
        }
        return strings;
    }
    public static ArrayList<Integer> copyIntArray(ArrayList<Integer> nvA){
        ArrayList<Integer> nv=new ArrayList();
        int i,len=nvA.size();
        for(i=0;i<len;i++){
            nv.add(new Integer(nvA.get(i)));
        }
        return nv;
    }
    public static ArrayList<DoubleRange> copyDoubleRangeArray(ArrayList<DoubleRange> cvA){
        ArrayList<DoubleRange> cv=new ArrayList();
        int i,len=cvA.size();
        for(i=0;i<len;i++){
            cv.add(new DoubleRange(cvA.get(i)));
        }
        return cv;
    }
    public static double[][] copyArray(double[][] pdA){
        int dim1=pdA.length,dim2=pdA[0].length;
        double[][] pdA1=new double[dim1][dim2];
        copyArray(pdA,pdA1);
        return pdA1;
    }
    public static MeanSem0 buildMeanSem(int[] pdData){
        return buildMeanSem(pdData,0,pdData.length-1,1);
    }
    public static MeanSem0 buildMeanSem(int[] pdData, int nI, int nF, int nDelta){
        MeanSem0 ms=new MeanSem0();
        double mean=0, mss=0;
        int n=(nF-nI)/nDelta+1;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            d=pdData[i];
            mean+=d;
            mss+=d*d;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss);
        return ms;
    }
    public static MeanSem0 buildMeanSem(float[] pfData, int nI, int nF, int nDelta){
        MeanSem0 ms=new MeanSem0();
        double mean=0, mss=0;
        int n=(nF-nI)/nDelta+1;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            d=pfData[i];
            mean+=d;
            mss+=d*d;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss);
        return ms;
    }
    public static MeanSem0 buildMeanSem(double[][] pdData, int column){
        return buildMeanSem(pdData,0,pdData.length-1,1,column);
    }
    public static MeanSem0 buildMeanSem(double[][] pdData, int rowI, int rowF, int delta, int column){
        MeanSem0 ms=new MeanSem0();
        double mean=0, mss=0;
        int n=(rowF-rowI)/delta+1;
        double d;
        for(int i=rowI;i<=rowF;i+=delta){
            d=pdData[i][column];
            mean+=d;
            mss+=d*d;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss);
        return ms;
    }
    public static MeanSem0 buildMeanSem(int[][] pdData, Roi roi){
        int rows=pdData.length;
        int cols=pdData[0].length;
        int r,c;
        MeanSem0 ms=new MeanSem0();
        double mean=0, mss=0;
        int n=0;
        double d;
        Rectangle br=roi.getBounds();
        int xo=br.x,yo=br.y,rw=br.width,rh=br.height,x,y;

        for(r=0;r<rh;r++){
            y=yo+r;
            for(c=0;c<rw;c++){
                x=xo+c;
                if(!roi.contains(x, y))continue;
                d=pdData[y][x];
                mean+=d;
                mss+=d*d;
                n++;
            }
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss);
        return ms;
    }
    public static MeanSem0 buildMeanSem(int[][] pnData){
        int rows=pnData.length;
        int cols=pnData[0].length;
        int r,c;
        MeanSem0 ms=new MeanSem0();
        double mean=0, mss=0;
        int n=rows*cols;
        double d;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                d=pnData[r][c];
                mean+=d;
                mss+=d*d;
            }
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss);
        return ms;
    }
    public static double crossCorrelationCoefficient(double pdData1[], double pdData2[], int nShift){
        double coef=0.;
        int len1=pdData1.length,len2=pdData2.length,nDelta=1,i,j,nI,nF;
        nI=Math.max(0, nShift);
        nF=Math.min(len2-nShift-1, len1-1);
        MeanSem0 ms1=buildMeanSem(pdData1,nI,nF,nDelta);
        MeanSem0 ms2=buildMeanSem(pdData2,nI+nShift,nF+nShift,nDelta);
        double m1=ms1.mean,m2=ms2.mean;
        double cp=0;
        for(i=nI;i<=nF;i++){
            cp+=(pdData1[i]-m1)*(pdData2[i]-m2);
        }
        coef=cp/(Math.sqrt(ms1.sem2*ms2.sem2)*(nF-nI));
        return coef;
    }
    public static void copyArray(int[][] pnData1, int[][] pnData2){
        int h=pnData1.length,w=pnData1[0].length,i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pnData2[i][j]=pnData1[i][j];
            }
        }
    }
    public static void copyArray_differentSizes(int[][] pnData1, int[][] pnData2){
        int h1=pnData1.length,w1=pnData1[0].length,i,j;
        int h2=pnData2.length,w2=pnData2[0].length,i1,j1;
        for(i=0;i<h2;i++){
            i1=i%h1;
            for(j=0;j<w2;j++){
                j1=j%w1;
                pnData2[i][j]=pnData1[i1][j1];
            }
        }
    }
    public static void replaceElement(int[][] pnData1, int[][] pnData2, int nV){//this method replace all
        // elements in pnData2 whose values are nV with the counterparts in pnData1
        int h=pnData1.length,w=pnData1[0].length,i,j,nt;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                nt=pnData2[i][j];
                if(nt==nV) nt=pnData1[i][j];
                pnData2[i][j]=nt;
            }
        }
    }
    public static void addArray(int[][] pnData1, int[][] pnData2,int[][] pnData3,int sign){
        int h=pnData1.length,w=pnData1[0].length,i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pnData3[i][j]=pnData1[i][j]+sign*pnData2[i][j];
            }
        }
    }
    public static void addArray_AbsoluteValues(int[][] pnData1, int[][] pnData2,int[][] pnData3,int sign){
        int h=pnData1.length,w=pnData1[0].length,i,j,sn;
        int abs1,abs2,abs3;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                sn=1;
                abs1=pnData1[i][j];
                if(abs1<0) sn=-1;
                abs1=Math.abs(abs1);
                abs2=Math.abs(pnData2[i][j]);
                if(sign>0)
                    abs3=abs1+abs2;
                else
                    abs3=Math.max(abs1-abs2,0);              
                pnData3[i][j]=sn*abs3;
            }
        }
    }
    public static void addArray(int[][] pnData1, int[][] pnData2,int[][] pnData3,int iI,int iF, int jI, int jF, int sign){
        int h=pnData1.length,w=pnData1[0].length,i,j;
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                pnData3[i][j]=pnData1[i][j]+sign*pnData2[i][j];
            }
        }
    }
    public static void addArray(int[][] pnData1, int[][] pnData2,int iI,int iF, int jI, int jF, int sign, int dI, int dJ){
        int h=pnData1.length,w=pnData1[0].length,i,j;
        if(iI<0) iI=0;
        if(iF>=h) iF=h-1;
        if(jI<0) jI=0;
        if(jF>=w) jF=w-1;
        int x,y;
        for(i=iI;i<=iF;i++){
            y=i+dI;
            if(y<0||y>=h) continue;
            for(j=jI;j<=jF;j++){
                x=j+dJ;
                if(x<0||x>=w) continue;
                pnData2[y][x]+=sign*pnData1[i][j];
            }
        }
    }
    public static void copyArray(double[][] pdData, int[][] pnData){
        int h=pdData.length,w=pdData[0].length,i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pnData[i][j]=(int)(pdData[i][j]+0.5);
            }
        }
    }
    public static void copyArray(double[][] pdData1, double[][] pdData2){
        int h=pdData1.length,w=pdData1[0].length,i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pdData2[i][j]=pdData1[i][j];
            }
        }
    }
    public static void addArray(double[][] pdData1, double[][] pdData2){
        int h=pdData1.length,w=pdData1[0].length,i,j;
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pdData2[i][j]+=pdData1[i][j];
            }
        }
    }
    public static void copyArray(int[][] pnData1, int[][] pnData2,int iI, int iF, int jI, int jF){
        int h=pnData1.length,w=pnData1[0].length,i,j;
        if(iI<0) iI=0;
        if(iF>=h) iF=h-1;
        if(jI<0) jI=0;
        if(jF>=w) jF=w-1;
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                pnData2[i][j]=pnData1[i][j];
            }
        }
    }
    public static void scaleArray(int[][] pnData, double scale, int iI, int iF, int jI, int jF){
        int h=pnData.length,w=pnData[0].length,i,j;
        if(iI<0) iI=0;
        if(iF>=h) iF=h-1;
        if(jI<0) jI=0;
        if(jF>=w) jF=w-1;
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                pnData[i][j]=(int)(pnData[i][j]*scale+0.5);
            }
        }
    }
    public static void scaleArray(double[]pdData, double scale){
        for(int i=0;i<pdData.length;i++){
            pdData[i]*=scale;
        }
    }
    public static void copyArray(int[][] pnData1, int[][] pnData2,int iI, int iF, int jI, int jF,int dI, int dJ){
        int h=pnData1.length,w=pnData1[0].length,i,j;
        int h2=pnData2.length,w2=pnData2[0].length;
        if(iI<0) iI=0;
        if(iF>=h) iF=h-1;
        if(jI<0) jI=0;
        if(jF>=w) jF=w-1;
        int x,y;
        for(i=iI;i<=iF;i++){
            y=i+dI;
            if(y<0||y>=h2) continue;
            for(j=jI;j<=jF;j++){
                x=j+dJ;
                if(x<0||x>=w2) continue;
                pnData2[y][x]=pnData1[i][j];
            }
        }
    }
    public static void meanFiltering(int[][] pnData,int r){
        meanFiltering(pnData,0,pnData.length-1,0,pnData[0].length-1,r);
    }
    public static int meanFiltering(int[][] pnData,int iI, int iF, int jI, int jF, int r){
        int h0=pnData.length,w0=pnData[0].length,h=iF-iI+1,w=jF-jI+1,i,j,x,y,xt,yt;
        if(h0<3||w0<3) return -1;
        int[][] pnt=new int[h][w];
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                pnt[i-iI][j-jI]=0;
                for(y=i-r;y<=i+r;y++){
                    yt=y;
                    if(y<iI)
                        yt=iI+(iI-y);
                    else if(y > iF)
                        yt = iF-(y-iF);

                    for(x=j-r;x<=j+r;x++){
                        xt=x;
                        if(x<jI)
                            xt=jI+(jI-x);
                        else if(x >jF)
                            xt = jF-(x-jF);
                        pnt[i-iI][j-jI]+=pnData[yt][xt];
                    }
                }
            }
        }
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                pnData[i][j]=pnt[i-iI][j-jI]/((2*r+1)*(2*r+1));
            }
        }
        return 1;
    }
    public static int[][] copyArray(int[][] pnData1){
        int h=pnData1.length,w=pnData1[0].length,i,j;
        int pnData2[][]=new int[h][w];
        for(i=0;i<h;i++){
            for(j=0;j<w;j++){
                pnData2[i][j]=pnData1[i][j];
            }
        }
        return pnData2;
    }
    public static int[][] copyArray(int[][] pnData1,int iI,int iF, int jI,int jF){
        int h=iF-iI+1,w=jF-jI+1,i,j;
        int pnData2[][]=new int[h][w];
        for(i=iI;i<=iF;i++){
            for(j=jI;j<=jF;j++){
                pnData2[i-iI][j-jI]=pnData1[i][j];
            }
        }
        return pnData2;
    }
    public static void copyPointArray(ArrayList<Point> cv, ArrayList<Point> cv1){
        int i,len=cv.size();
        cv1.clear();
        for(i=0;i<len;i++){
            cv1.add(cv.get(i));
        }
    }
    public static void copyArrayElements(double[][] pdData1, double[][] pdData2,ArrayList<Point> points){
        int i,len=points.size(),x,y;
        Point pt;
        for(i=0;i<len;i++){
            pt=points.get(i);
            x=pt.x;
            y=pt.y;
            pdData2[y][x]=pdData1[y][x];
        }
    }
    public static void copyArrayElements(int[][] pnData1, int[][] pnData2,ArrayList<Point> points){
        int i,len=points.size(),x,y;
        Point pt;
        for(i=0;i<len;i++){
            pt=points.get(i);
            x=pt.x;
            y=pt.y;
            pnData2[y][x]=pnData1[y][x];
        }
    }
    public static void shiftArray(int[][] pnData, int nShift){
        int h=pnData.length,w=pnData[0].length,i,j;
        for(i=0;i<h;i++){
            for(j=0;j<h;j++){
                pnData[i][j]+=nShift;
            }
        }
    }
    public static int getQuantile(int[][] pnData, double quantile){
        int h=pnData.length,w=pnData[0].length,i,j;
        int len=h*w;
        int[] pn=new int[len];
        int o;
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<h;j++){
                pn[o+j]=pnData[i][j];
            }
        }
        QuickSortInteger.quicksort(pn);
        int index=(int)(len*quantile+0.5);
        if(index>=len) index=len-1;
        return pn[index];
    }
    public static int getQuantile(int[]pnData, double quantile){
        int len=pnData.length;
        QuickSortInteger.quicksort(pnData);
        int index=(int)(len*quantile+0.5);
        if(index>=len) index=len-1;
        return pnData[index];
    }
    public static int getMin(int[][] pnData){
        int rows=pnData.length;
        int cols=pnData[0].length;
        int r,c,it;
        int nMin=Integer.MAX_VALUE;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                it=pnData[r][c];
                if(it<nMin) nMin=it;
            }
        }
        return nMin;
    }
    public static double getMin(double[] pdData){
        int rows=pdData.length;
        double dMin=Double.POSITIVE_INFINITY,dt;
        for(int r=0;r<rows;r++){
                dt=pdData[r];
                if(dt<dMin) dMin=dt;
        }
        return dMin;
    }
    public static int getMax(int[][] pnData){
        int rows=pnData.length;
        int cols=pnData[0].length;
        int r,c,it;
        int nMax=Integer.MIN_VALUE;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                it=pnData[r][c];
                if(it>nMax) nMax=it;
            }
        }
        return nMax;
    }
    public static DoubleRange getRange(ArrayList<Double> dv){
        int i,len=dv.size();
        DoubleRange cDr=new DoubleRange();
        for(i=0;i<len;i++){
            cDr.expandRange(dv.get(i));
        }
        return cDr;
    }
    public static intRange getRange(int[][] pnData){
        int rows=pnData.length;
        int cols=pnData[0].length;
        int r,c,it;
        int nMax=Integer.MIN_VALUE;
        int nMin=Integer.MAX_VALUE;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                it=pnData[r][c];
                if(it>nMax) nMax=it;
                if(it<nMin) nMin=it;
            }
        }
        return new intRange(nMin,nMax);
    }
    public static intRange getRange(int[][] pnData,int rI, int rF, int cI, int cF){
        int r,c,it;
        int nMax=Integer.MIN_VALUE;
        int nMin=Integer.MAX_VALUE;
        for(r=rI;r<=rF;r++){
            for(c=cI;c<=cF;c++){
                it=pnData[r][c];
                if(it>nMax) nMax=it;
                if(it<nMin) nMin=it;
            }
        }
        return new intRange(nMin,nMax);
    }
    public static DoubleRange getRange(double[][] pdData, int index){
        int len=pdData.length;
        DoubleRange dr=new DoubleRange();
        for(int r=0;r<len;r++){
            dr.expandRange(pdData[r][index]);
        }
        return dr;
    }
    public static DoubleRange getRange(double[] pdData){
        int len=pdData.length;
        DoubleRange dr=new DoubleRange();
        for(int r=0;r<len;r++){
            dr.expandRange(pdData[r]);
        }
        return dr;
    }
    public static void addToElements(int[][] pnData, int nV){
        int rows=pnData.length;
        int cols=pnData[0].length;
        int r,c;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                pnData[r][c]+=nV;
            }
        }
    }
    public static void setElements(int[] pnData, int nV){
        int len=pnData.length;
        setElements(pnData,0,len-1,nV);
    }
    public static void setElements(byte[] pbData, byte bV){
        int len=pbData.length;
        setElements(pbData,bV,0,len-1,1);
    }
    public static void setElements(boolean[] pbData, boolean bt){
        int len=pbData.length,i;
        for(i=0;i<len;i++){
            pbData[i]=bt;
        }
    }
    public static void setElements(boolean[][] pbData, boolean bt){
        int r,c,rows=pbData.length,cols=pbData[0].length;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                pbData[r][c]=bt;
            }
        }
    }
    public static void setElements(boolean[] pbData, ArrayList<Integer> nv, boolean bt){
        int len=nv.size(),i;
        for(i=0;i<len;i++){
            pbData[nv.get(i)]=bt;
        }
    }
    public static void setElements_AND(boolean[] pbData, ArrayList<Integer> nv1, ArrayList<Integer> nv2, boolean bt){
        int lenD=pbData.length,len1=nv1.size(),len2=nv2.size(),len=Math.max(len1, len2),i;
        int[] pn=new int[lenD];
        setElements(pn,0);
        for(i=0;i<len;i++){
            if(i<len1) pn[nv1.get(i)]++;
            if(i<len2) pn[nv2.get(i)]++;
        }
        for(i=0;i<lenD;i++){
            if(pn[i]==2) pbData[i]=true;
        }
    }
    public static void setElements(byte[] pbData, byte bt,int iI, int iF, int delta){
        for(int i=iI;i<=iF;i+=delta){
            pbData[i]=bt;
        }
    }
    public static void setElements(boolean[] pbData, boolean bt,int iI, int iF, int delta){
        for(int i=iI;i<=iF;i+=delta){
            pbData[i]=bt;
        }
    }
    public static void setElements(boolean[] pbData, int iI, int iF, boolean bt){
        int len=pbData.length,i;
        for(i=iI;i<=iF;i++){
            pbData[i]=bt;
        }
    }
    public static void setElements(double[] pdData, double dV){
        int len=pdData.length;
        setElements(pdData,0,len-1,dV);
    }
    public static void setElements(int[] pnData, int indexI, int indexF, int nV){
        for(int i=indexI;i<=indexF;i++){
            pnData[i]=nV;
        }
    }
    public static void setElements(double[] pdData, int indexI, int indexF, double dV){
        for(int i=indexI;i<=indexF;i++){
            pdData[i]=dV;
        }
    }
    public static void setElements(int[][] pnData, int nV){
        int rows=pnData.length;
        int cols=pnData[0].length;
        setElements(pnData,0,rows-1,0,cols-1,nV);
    }
    public static void setElements(double[][] pdData, int nV){
        int rows=pdData.length;
        int cols=pdData[0].length;
        setElements(pdData,0,rows-1,0,cols-1,nV);
    }
    public static void setElementsInProximity(int[][] pnData, int i0, int j0, int r, int nV){
        setElements(pnData,i0-r,i0+r,j0-r,j0+r,nV);
    }
    public static void setElements(int[][] pnData, int rI, int rF, int cI, int cF, int nV){
        int r,c;
        int h=pnData.length,w=pnData[0].length;
        rI=Math.max(0, rI);
        cI=Math.max(0, cI);
        rF=Math.min(rF, h-1);
        cF=Math.min(cF,w-1);
        for(r=rI;r<=rF;r++){
            for(c=cI;c<=cF;c++){
                pnData[r][c]=nV;
            }
        }
    }
    public static void setElements(double[][] pdData, int rI, int rF, int cI, int cF, int nV){
        int r,c;
        int h=pdData.length,w=pdData[0].length;
        rI=Math.max(0, rI);
        cI=Math.max(0, cI);
        rF=Math.min(rF, h-1);
        cF=Math.min(cF,w-1);
        for(r=rI;r<=rF;r++){
            for(c=cI;c<=cF;c++){
                pdData[r][c]=nV;
            }
        }
    }
    public static MeanSem0 mergedMeanSem(MeanSem0[][] meanSems,int rI,int rF, int nDelta,int c){
        MeanSem0 ms=new MeanSem0();
        for(int r=rI;r<=rF;r+=nDelta){
            ms.mergeSems(meanSems[r][c]);
        }
        return ms;
    }
    public static MeanSem0 mergedMeanSem(MeanSem0[][] meanSems,int[] indexes, int rI, int rF, int nDelta,int c){
        MeanSem0 ms=new MeanSem0();
        int index;
        for(int r=rI;r<=rF;r+=nDelta){
            index=indexes[r];
            ms.mergeSems(meanSems[index][c]);
        }
        return ms;
    }
    public static MeanSem1 mergedMeanSem(MeanSem1[][] meanSems,int rI,int rF, int nDelta,int c){
        MeanSem1 ms=new MeanSem1();
        for(int r=rI;r<=rF;r+=nDelta){
            ms.mergeSems(meanSems[r][c]);
        }
        return ms;
    }
    public static MeanSem1 mergedMeanSem(MeanSem1[][] meanSems,int[] indexes, int rI, int rF, int nDelta,int c){
        MeanSem1 ms=new MeanSem1();
        int index;
        for(int r=rI;r<=rF;r+=nDelta){
            index=indexes[r];
            ms.mergeSems(meanSems[index][c]);
        }
        return ms;
    }
    public static MeanSemFractional mergedMeanSem(MeanSemFractional[][] meanSems,int rI,int rF, int nDelta,int c){
        MeanSemFractional ms=new MeanSemFractional();
        for(int r=rI;r<=rF;r+=nDelta){
            ms.mergeSems(meanSems[r][c]);
        }
        return ms;
    }
    public static MeanSemFractional mergedMeanSem(MeanSemFractional[][] meanSems,int[] indexes, int rI, int rF, int nDelta,int c){
        MeanSemFractional ms=new MeanSemFractional();
        int index;
        for(int r=rI;r<=rF;r+=nDelta){
            index=indexes[r];
            ms.mergeSems(meanSems[index][c]);
        }
        return ms;
    }
    public static void clearMeanSems(MeanSem0[][] meanSems){
        int rows=meanSems.length;
        int cols=meanSems[0].length;
        int r,c;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                meanSems[r][c].clear();
            }
        }
    }
    public static void clearMeanSems(MeanSem1[][] meanSems){
        int rows=meanSems.length;
        int cols=meanSems[0].length;
        int r,c;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                meanSems[r][c].clear();
            }
        }
    }
    public static void clearMeanSems(MeanSemFractional[][] meanSems){
        int rows=meanSems.length;
        int cols=meanSems[0].length;
        int r,c;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                meanSems[r][c].clear();
            }
        }
    }
    public static void clearMeanSems(MeanSemFractional[] meanSems){
        int len=meanSems.length;
        int i;
        for(i=0;i<len;i++){
            meanSems[i].clear();
        }
    }
    public static void clearMeanSems(ArrayList<MeanSem0> meanSems){
        int rows=meanSems.size();
        int r;
        for(r=0;r<rows;r++){
            meanSems.get(r).clear();
        }
    }
    public static void copyMeanSems(ArrayList<MeanSem0> meanSems, ArrayList<MeanSem0> meanSems1){
        int rows=meanSems.size();
        meanSems1.clear();
        int r;
        MeanSem0 ms;
        for(r=0;r<rows;r++){
            ms=new MeanSem0();
            ms.update(meanSems.get(r));
            meanSems1.add(ms);
        }
    }
    public static MeanSem0 mergedMeanSem_Normalized(MeanSem0[][] meanSems,int[] indexes, int rI,int rF, int nDelta,int c0, int c){
        MeanSem0 ms=new MeanSem0(),mst=new MeanSem0();
        int index;
        for(int r=rI;r<=rF;r+=nDelta){
            index=indexes[r];
            mst.update(meanSems[index][c]);
            mst.scale(1./meanSems[index][c0].mean);
            ms.mergeSems(mst);
        }
        return ms;
    }
    public static MeanSem1 mergedMeanSem_Normalized(MeanSem1[][] meanSems,int[] indexes, int rI,int rF, int nDelta,int c0, int c){
        MeanSem1 ms=new MeanSem1(),mst=new MeanSem1();
        int index;
        for(int r=rI;r<=rF;r+=nDelta){
            index=indexes[r];
            mst.update(meanSems[index][c]);
            mst.scale(1./meanSems[index][c0].mean);
            ms.mergeSems(mst);
        }
        return ms;
    }
    public static MeanSemFractional mergedMeanSem_Normalized(MeanSemFractional[][] meanSems,int[] indexes, int rI,int rF, int nDelta,int c0, int c){
        MeanSemFractional ms=new MeanSemFractional(),mst=new MeanSemFractional();
        int index;
        for(int r=rI;r<=rF;r+=nDelta){
            index=indexes[r];
            mst.update(meanSems[index][c]);
            mst.scale(1./meanSems[index][c0].mean);
            ms.mergeSems(mst);
        }
        return ms;
    }
    public static void copyArray(int[] pnData1, int[] pnData2){
        int len=pnData1.length;
        if(len!=pnData2.length) IJ.error("two arrays must be the same length for the method copyArray(int[],int[])");
        for(int i=0;i<len;i++){
            pnData2[i]=pnData1[i];
        }
    }
    public static void copyArray(double[] pnData1, double[] pnData2){
        int len=pnData1.length;
        if(len!=pnData2.length) IJ.error("two arrays must be the same length for the method copyArray(int[],int[])");
        for(int i=0;i<len;i++){
            pnData2[i]=pnData1[i];
        }
    }
    public static void subtractArray(double[] pdData1, double[] pdData2){
        int len=pdData1.length;
        if(len!=pdData2.length) IJ.error("two arrays must be the same length for the method subtractArray(int[],int[])");
        for(int i=0;i<len;i++){
            pdData1[i]-=pdData2[i];
        }
    }
    public static void copyArray(float[] pdData1, double[] pdData2){
        int len=pdData1.length;
        if(len!=pdData2.length) IJ.error("two arrays must be the same length for the method copyArray(double[],double[])");
        for(int i=0;i<len;i++){
            pdData2[i]=pdData1[i];
        }
    }
    public static float[] copyToFloatArray(double[] pdData){
        int len=pdData.length,i;
        float[] pfData=new float[len];
        for(i=0;i<len;i++){
            pfData[i]=(float)pdData[i];
        }
        return pfData;
    }
    public static double[] getDoubleArray(ArrayList<Double> dv){
        int len=dv.size();
        double[] pd=new double[len];
        for(int i=0;i<len;i++){
            pd[i]=dv.get(i);
        }
        return pd;
    }
    public static void smoothMeanSemArray(ArrayList<MeanSem0> MSs, int ws, int indexI,int indexF){
        ArrayList<MeanSem0> MSs1=new ArrayList();
        int len=indexF-indexI+1,iI,iF,len1=2*ws+1,i,j;
        if(len<len1) IJ.error("too big smoothing window size for the array in smoothMeanSemArray");
        copyMeanSems(MSs,MSs1);
        clearMeanSems(MSs);
        MeanSem0 ms;
        for(i=indexI;i<=indexF;i++){
            iI=Math.max(indexI, i-ws);
            iF=iI+len1-1;
            if(iF>=indexF){
                iF=indexF;
                iI=iF-len1+1;
            }
            ms=MSs.get(i);
            for(j=iI;j<=iF;j++){
                ms.mergeSems(MSs1.get(j));
            }
        }
    }
    public static void copyArray(ArrayList<Integer> nvI1, ArrayList<Integer> nvI2){
        nvI2.clear();
        int i,len=nvI1.size();
        for(i=0;i<len;i++){
            nvI2.add(nvI1.get(i));
        }
    }
    public static void copyStringArray(ArrayList<String> svI1, ArrayList<String> svI2){
        svI2.clear();
        int i,len=svI1.size();
        for(i=0;i<len;i++){
            svI2.add(svI1.get(i));
        }
    }
     public static void copyDoubleArray(ArrayList<Double> dvI1, ArrayList<Double> dvI2){
        dvI2.clear();
        int i,len=dvI1.size();
        for(i=0;i<len;i++){
            dvI2.add(dvI1.get(i));
        }
    }
     public static void appendDoubleArray(ArrayList<Double> dvI1, ArrayList<Double> dvI2){
         appendDoubleArray(dvI1,dvI2,0,dvI1.size()-1);
     }
     public static void copyDoubleArray(ArrayList<Double> dvI1, ArrayList<Integer> indexes,ArrayList<Double> dvI2){
         dvI2.clear();
         for(int i=0;i<indexes.size();i++){
             dvI2.add(dvI1.get(indexes.get(i)));
         }
     }
     public static void appendDoubleArray(ArrayList<Double> dvI1, ArrayList<Double> dvI2,int iI, int iF){
        int i,len=dvI1.size();
        for(i=iI;i<=iF;i++){
            dvI2.add(dvI1.get(i));
        }
    }
     public static void appendIntArray(ArrayList<Integer> dnI1, ArrayList<Integer> dnI2,int iI, int iF){
        int i,len=dnI2.size();
        for(i=iI;i<=iF;i++){
            dnI1.add(dnI2.get(i));
        }
    }
     public static ArrayList<Double> copyDoubleArray(ArrayList<Double> dvI1){
        ArrayList<Double> dvI2=new ArrayList();
        int i,len=dvI1.size();
        for(i=0;i<len;i++){
            dvI2.add(dvI1.get(i));
        }
        return dvI2;
    }

    public static void copyArray(float pfData[], int ntI1, int num, int delta1, double pdData[], int ntI2, int delta2){
        for(int i=0;i<num;i++){
            pdData[ntI2+i*delta2]=pfData[ntI1+i*delta1];
        }
    }
    public static double[] getRunningWindowAverage(double[] pdData, int indexI, int indexF, int nWs, boolean extendable){
        int nDelta=1;
        return getRunningWindowAverage(pdData,indexI,indexF,nDelta,nWs,extendable);
    }
    public static double[] getRunningWindowAverage(ArrayList<Double> dvX, ArrayList<Double> dvY,int iI, int iF, double dWSL, double dWSR){
        int index,l,r;
        double x,xl,xr;
        SimpleRegression sr=new SimpleRegression();
        l=iI;
        r=iI;
        xl=dvX.get(l);
        xr=dvX.get(r);
        int nData=0,nDataSize=iF-iI+1;
        double[] pdRWA=new double[nDataSize];
        double sum=0;

        for(index=iI;index<iF;index++){
            x=dvX.get(index);
            while(x-xl>dWSL&&l>=iI){
                sum-=dvY.get(l);
                nData--;
                l++;
                xl=dvX.get(l);
            }
            while(xr-x<=dWSR&&r<iF){
                sum+=dvY.get(r);
                nData++;
                r++;
                if(r>=nDataSize) break;
                xr=dvX.get(r);
            }
            if(nData>0)
                pdRWA[index]=sum/nData;
            else
                pdRWA[index]=0;
        }
        return pdRWA;
    }
    public static double[] getRunningWindowAverage(double[] pdData, int indexI, int indexF, int nDelta, int nWs, boolean extendable){
        if(indexF>=pdData.length){
            indexF=pdData.length-1;
        }
        int lenT=(indexF-indexI)/nDelta+1,positionI,positionF,i;
        indexF=indexI+nDelta*(lenT-1);
        int len=2*nWs+1;
        if(len>lenT) nWs=lenT/2;
        double[] pdRWA=new double[lenT];
        double sum=0;
        
        if(len>=lenT){//the length of the segment to compute the rwa is smaller than the defined window size
            double ave=CommonStatisticsMethods.getMean(pdData, null, indexI, indexF, nDelta);
            for(i=indexI;i<=indexF;i+=nDelta){
                pdRWA[(i-indexI)/nDelta]=ave;
            }
            return pdRWA;
        }
        
        if(extendable){
            positionI=indexI;
            positionF=indexF;
        }else {
            positionI=indexI+nWs*nDelta;
            positionF=indexF-nWs*nDelta;
        }
        for(i=-nWs;i<=nWs;i++){
            sum+=pdData[positionI+i*nDelta];
        }
        pdRWA[(positionI-indexI)/nDelta]=sum/len;
        for(i=positionI+nDelta;i<=positionF;i+=nDelta){
            sum-=pdData[i-(nWs+1)*nDelta];
            sum+=pdData[i+nWs*nDelta];
            pdRWA[(i-indexI)/nDelta]=sum/len;
        }
        double ave;
        if(!extendable){
            ave=pdRWA[positionI-indexI];
            for(i=0;i<nWs;i++){
                pdRWA[i]=ave;
            }
            ave=pdRWA[positionF-indexI];
            for(i=1;i<=nWs;i++){
                pdRWA[(positionF-indexI)/nDelta+i]=ave;
            }
        }
        return pdRWA;
    }
    public static int[] toIntArrayRound(double[] pdV){
        int len=pdV.length;
        int[] pnV=new int[len];
        for(int i=0;i<len;i++){
            pnV[i]=Math.round((int)pdV[i]);
        }
        return pnV;
    }
    public static MeanSem1 getDeltaMeanSem(double pdData[], int nI, int nF, int nDelta, int nWs, int nSpan, boolean extendable){
        double m=0;
        double pdRWA[]=getRunningWindowAverage(pdData,nI,nF,nDelta,nWs,extendable);
        int len=pdRWA.length,i;
        for(i=0;i<len-nSpan;i++){
            pdRWA[i]=pdRWA[i+nSpan]-pdRWA[i];
        }
        MeanSem1 ms=buildMeanSem1(pdRWA,0,len-1-nSpan,1);
        return ms;
    }
    public static MeanSem1 buildMeanSem1(double[] pdData, int nI, int nF, int nDelta){
        MeanSem1 ms=new MeanSem1();
        double mean=0, mss=0, max=Double.MIN_VALUE,min=Double.MAX_VALUE;
        int n=(nF-nI)/nDelta+1;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            d=pdData[i];
            mean+=d;
            mss+=d*d;
            if(d<min) min=d;
            if(d>max) max=d;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss,max,min);
        return ms;
    }
    public static MeanSem1 buildMeanSem1(ArrayList<Double> dvData, int nI, int nF, int nDelta){
        MeanSem1 ms=new MeanSem1();
        double mean=0, mss=0, max=Double.MIN_VALUE,min=Double.MAX_VALUE;
        int n=(nF-nI)/nDelta+1;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            d=dvData.get(i);
            mean+=d;
            mss+=d*d;
            if(d<min) min=d;
            if(d>max) max=d;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss,max,min);
        return ms;
    }
    public static MeanSem1 buildMeanSem1(double[] pdData, boolean[] pbOutliars, int nI, int nF, int nDelta){
        MeanSem1 ms=new MeanSem1();
        double mean=0, mss=0, max=Double.MIN_VALUE,min=Double.MAX_VALUE;
        int n=0;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            if(pbOutliars[i]) continue;
            d=pdData[i];
            mean+=d;
            mss+=d*d;
            if(d<min) min=d;
            if(d>max) max=d;
            n++;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss,max,min);
        return ms;
    }
    public static MeanSem1 buildMeanSem1_Selected(double[] pdData, boolean[] pbSelected, int nI, int nF, int nDelta){
        MeanSem1 ms=new MeanSem1();
        double mean=0, mss=0, max=Double.MIN_VALUE,min=Double.MAX_VALUE;
        int n=0;
        double d;
        for(int i=nI;i<=nF;i+=nDelta){
            if(!pbSelected[i]) continue;
            d=pdData[i];
            mean+=d;
            mss+=d*d;
            if(d<min) min=d;
            if(d>max) max=d;
            n++;
        }
        mean/=n;
        mss/=n;
        ms.updateMeanSquareSum(n, mean, mss,max,min);
        return ms;
    }
    public static int getLargerElementIndex(ArrayList<Integer> vnInts, int nInt){
        int len=vnInts.size(),i,nv;
        for(i=0;i<len;i++){
            nv=vnInts.get(i);
            if(nv>nInt) return i;
        }
        return -1;
    }
    public static void invertArray(ArrayList<Integer> nvT){
        ArrayList<Integer> nv=new ArrayList();
        int len=nvT.size(),i;
        for(i=0;i<len;i++){
            nv.add(nvT.get(len-1-i));
        }
        nvT.clear();
        for(i=0;i<len;i++){
            nvT.add(nv.get(i));
        }
    }
    public static void setElements(int[][] pnData,ArrayList<Point> points,int nV){
        int len=points.size();
        Point p;
        for(int i=0;i<len;i++){
            p=points.get(i);
            pnData[p.y][p.x]=nV;
        }
    }
    public static ArrayList<Point> getCopy(ArrayList<Point> points){
        ArrayList<Point> pts=new ArrayList();
        int i,len=points.size();
        for(i=0;i<len;i++){
            pts.add(new Point(points.get(i)));
        }
        return pts;
    }
    public static void translatePoints(ArrayList<Point> points, int dx, int dy){
        int i,len=points.size();
        for(i=0;i<len;i++){
            points.get(i).translate(dx, dy);
        }
    }
    public static double getMean(double[][] pdV, int iI, int iF, int jI, int jF){
        double m=0,num=0;
        for(int i=iI;i<=iF;i++){
            for(int j=jI;j<=jF;j++){
                m+=pdV[i][j];
                num+=1.;
            }
        }
        return m/num;
    }
    public static double getMean(int[][] nA,ArrayList<Point> locations){
        double m=0;
        int i,len,h=nA.length,w=nA[0].length;
        if(locations!=null) 
            len=locations.size();
        else
            len=h*w;
        if(locations!=null){
            Point p;
            for(i=0;i<len;i++){
                p=locations.get(i);
                m+=nA[p.y][p.x];
            }
        }else{
            int j;
            for(i=0;i<h;i++){
                for(j=0;j<w;j++){
                    m+=nA[i][j];
                }
            }
        }
        m/=len;
        return m;
    }
    public static boolean equalArrays(double[][] pdA, double[][] pdA1){
        int dim1=pdA.length,dim2=pdA[0].length;
        int i,j;
        double dA;
        for(i=0;i<dim1;i++){
            for(j=0;j<dim2;j++){
                if(pdA[i][j]!=pdA1[i][j]) return false;
            }
        }
        return true;
    }
    public static boolean equalArrays(double[]pdA, double[]pdA1){
        int dim=pdA.length;
        if(dim!=pdA1.length) return false;
        int i;
        double dA;
        for(i=0;i<dim;i++){
            if(pdA[i]!=pdA1[i]) return false;
        }
        return true;
    }
    public static ArrayList<Integer> removeDuplicatedEliments(ArrayList<Integer> nvA){
        TreeSet<Integer> nsA=new TreeSet();
        int i,len=nvA.size();
        for(i=0;i<len;i++){
            nsA.add(nvA.get(i));
        }
        return getArray(nsA);
    }
    public static ArrayList<Integer> getArray(Set<Integer> set){
        ArrayList<Integer> ia=new ArrayList();
        Iterator it=set.iterator();
        while(it.hasNext()){
            ia.add((Integer)it.next());
        }
        return ia;
    }
    public static void transposeArrays(ArrayList<DoubleArray> arrays){
        ArrayList<DoubleArray> arrayst=new ArrayList();
        int i,j,len=arrays.size();
        DoubleArray da;
        for(i=0;i<len;i++){
            da=arrays.get(i);
            arrayst.add(new DoubleArray(da));
            da.m_DoubleArray.clear();
        }
        int len1=arrays.get(0).m_DoubleArray.size();
        for(i=0;i<len1;i++){
            arrays.clear();
            da=new DoubleArray();
            for(j=0;j<len;j++){
                da.m_DoubleArray.add(arrayst.get(j).m_DoubleArray.get(i));
            }
            arrays.add(da);
        }
    }
    public static void transposeArrays(double[][] pdData, double[][] pdDataT){
        int i,j,rows=pdData.length,cols=pdData[0].length;
        for(i=0;i<rows;i++){
            for(j=0;j<cols;j++){
                pdDataT[j][i]=pdData[i][j];
            }
        }
    }
    public static void copyArray_Row(double[][] pdData, int row, int nI, int nF, int nDelta, double[] pdData1, int nI1,int nDelta1){
        int position=nI1;
        for(int i=nI;i<=nF;i+=nDelta){
            pdData1[position]=pdData[row][i];
            position+=nDelta1;
        }
    }
    public static void copyArray_Column(double[][] pdData, int column, int nI, int nF, int nDelta, double[] pdData1, int nI1,int nDelta1){
        int position=nI1;
        for(int i=nI;i<=nF;i+=nDelta){
            pdData1[position]=pdData[i][column];
            position+=nDelta1;
        }
    }
    public static ArrayList<DoubleRange> getDoubleValueRanges(double [][]pdX){
        ArrayList<DoubleRange> varRanges=new ArrayList();
        int i,j,nVars=pdX[0].length,len=pdX.length;
        for(i=0;i<nVars;i++){
            DoubleRange dr=new DoubleRange();
            for(j=0;j<len;j++){
                dr.expandRange(pdX[j][i]);
            }
            varRanges.add(dr);
        }
        return varRanges;
    }
    public static int[][] getIntArray2(int[][] pnV2, int w, int h){
        if(pnV2==null) return new int[h][w];
        if(pnV2.length!=h) return new int[h][w];
        if(pnV2[0].length!=w) return new int[h][w];
        return pnV2;
    }
    public static int[] getIntArray(int[] pnV, int len){
        if(pnV==null) return new int[len];
        if(pnV.length!=len) return new int[len];
        return pnV;
    }
    public static intRange[] getIntRangeArray(intRange[] pnV, int len){
        if(pnV==null) return new intRange[len];
        if(pnV.length!=len) return new intRange[len];
        return pnV;
    }
    public static Point[] getPointArray(Point[] pnV, int len){
        if(pnV==null) return new Point[len];
        if(pnV.length!=len) return new Point[len];
        return pnV;
    }
    public static double[][] getDoubleArray2(double[][] pdV2, int w, int h){
        if(pdV2==null) return new double[h][w];
        if(pdV2.length!=h) return new double[h][w];
        if(pdV2[0].length!=w) return new double[h][w];
        return pdV2;
    }
    public static double getTrailMean(int[][] pixels, ArrayList<Point> points){
        int i,len=points.size();
        Point p;
        double mean=0;
        for(i=0;i<len;i++){
            p=points.get(i);
            mean+=pixels[p.y][p.x];
        }
        return mean/len;
    }
    public static double getTrailPercentileValue(int[][] pixels, ArrayList<Point> points,double percentile){
        int i,len=points.size();
        Point p;
        ArrayList<Double> dvPixels=new ArrayList();
        for(i=0;i<len;i++){
            p=points.get(i);
            dvPixels.add((double)pixels[p.y][p.x]);
        }
        Histogram hist=new Histogram(dvPixels);
        hist.setPercentile(percentile);
        return hist.getPercentileValue();
    }
    public static int[][] getMean_ImageShape(int[][] pnData, ImageShape shape){
        int h=pnData.length,w=pnData[0].length;
        shape.setFrameRanges(new intRange(0,w-1), new intRange(0,h-1));
        return getMean_ImageShape(pnData,shape,0,w-1,0,h-1);
    }
    public static int[][] getMean_ImageShape(int[][] pnData, ImageShape shape, int colI, int colF, int rowI, int rowF){
        Histogram hist=new Histogram();
        int w=pnData[0].length,h=pnData.length;
        intRange ir=CommonStatisticsMethods.getRange(pnData);
        hist.update(0, ir.getMin(), ir.getMax(), 1.);
        shape.setLocation(new Point(rowI,colI));
        ArrayList <Point> aInnerPoints=new ArrayList();
        shape.setCenter(new Point(rowI,colI));
        shape.getInnerPoints( aInnerPoints);
        ArrayList <Point> oldPoints=new ArrayList(), newPoints=new ArrayList();
        int len=aInnerPoints.size();
        Point p;
        int i,pixel;
        for(i=0;i<len;i++){
            p=aInnerPoints.get(i);
            pixel=pnData[p.y][p.x];
            hist.addData(pixel);
        }

        int sign=1,len1,len2;
        int pnData0[][]=new int[h][w];
        int dx,dy;

        p=new Point();
        Point p0=new Point(),pt;
        p.x=colI;
        p.y=rowI;

        p0.setLocation(p);
        while(p.y>=rowI&&p.y<=rowF){
            while(p.x>=colI&&p.x<=colF){
                dx=p.x-p0.x;
                dy=p.y-p0.y;
                shape.getInnerPointReplacement(dx, dy, newPoints, oldPoints);
                shape.setCenter(p);
                len1=newPoints.size();
                len2=oldPoints.size();

                for(i=0;i<len1;i++){
                    pt=newPoints.get(i);
                    pixel=pnData[pt.y][pt.x];
                    hist.addData(pixel);
                }

                for(i=0;i<len2;i++){
                    pt=oldPoints.get(i);
                    pixel=pnData[pt.y][pt.x];
                    hist.removeData(pixel);
                }

                pnData0[p.y][p.x]=(int) hist.getMean();
                p0.setLocation(p);
                p.translate(sign, 0);
            }
            p.translate(-sign, 1);
            sign*=-1;
        }
        return pnData0;
    }
    public static Histogram getHistogram(int[][] pdData){
        Histogram hist=new Histogram(getElements(pdData));
        return hist;
    }
    public static ArrayList<Double> getElements(int[][] pdData){
        int rows=pdData.length,cols=pdData[0].length,r,c;
        ArrayList<Double> dvData=new ArrayList();
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                dvData.add((double)pdData[r][c]);
            }
        }
        return dvData;
    }
    public static Histogram getHistogram(int[][] pdData,boolean[][] pbSelected){
        int rows=pdData.length,cols=pdData[0].length,r,c;
        ArrayList<Double> dvData=new ArrayList();
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                if(pbSelected[r][c])dvData.add((double)pdData[r][c]);
            }
        }
        Histogram hist=new Histogram(dvData);
        return hist;
    }
    public static void setGridLines(int[][] pixels, int gw, int gh, int pixel){
        int h=pixels.length,w=pixels[0].length,rows=h/(gh+1),cols=w/(gw+1),r,c,x,y;
        for(r=0;r<rows;r++){
            y=r*(gh+1)+gh;
            for(x=0;x<w;x++){
                pixels[y][x]=pixel;
            }
        }
        for(c=0;c<cols;c++){
            x=c*(gw+1)+gw;
            for(y=0;y<h;y++){
                pixels[y][x]=pixel;
            }
        }
    }
    public static SimpleRegression getSimpleLinearRegression(ArrayList<Double> dvX, ArrayList<Double> dvData, int index1, int index2){
        SimpleRegression sr=new SimpleRegression();
        double x;
        for(int i=index1;i<=index2;i++){
            x=dvX.get(i);
            sr.addData(x, dvData.get(i));
        }
        return sr;
    }
    public static PolynomialRegression getPolynomialRegression(ArrayList<Double> dvX, ArrayList<Double> dvY, int order, int nModes,double dOutliarRatio){
        return new PolynomialRegression(CommonStatisticsMethods.getDoubleArray(dvX),getDoubleArray(dvY),order,nModes,dOutliarRatio);
    }
    public static SimpleRegression getSimpleLinearRegression(double[] pdX, double[] pdY, int index1, int index2){
        return getSimpleLinearRegression(pdX,pdY,index1,index2,1);
    }
    public static SimpleRegression getSimpleLinearRegression(double[] pdX, double[] pdY, int index1, int index2, int delta){
        SimpleRegression sr=new SimpleRegression();
        double x;
        int index=index1;
        while((index2-index)*delta>=0){
            x=pdX[index];
            sr.addData(x, pdY[index]);
            index+=delta;
        }
        return sr;
    }
    public static SimpleRegression getSimpleLinearRegression(double[] pdData, int index1, int index2){
        SimpleRegression sr=new SimpleRegression();
        for(int i=index1;i<=index2;i++){
            sr.addData(i, pdData[i]);
        }
        return sr;
    }
    public static ArrayList<Integer> getNewIntArray(ArrayList<Integer> nV){
        if(nV==null) return new ArrayList<Integer>();
        nV.clear();
        return nV;
    }
    public static void addToArray(ArrayList<Double> dv, double delta){
        int i,len=dv.size();
        for(i=0;i<len;i++){
            dv.set(i, dv.get(i)+delta);
        }
    }
    static public int findOutliars(double[] pdData, double pValue,MeanSem1 mso, ArrayList<Integer> nvOutliars){
        findOutliars(pdData,pValue,mso,nvOutliars,0,pdData.length-1,1);
        return 1;
    }
    static public int findOutliars(double[] pdData, double pValue,MeanSem1 mso, ArrayList<Integer> nvOutliars, int iI,int iF, int nDelta){
        return findOutliars(pdData,null,pValue,mso,nvOutliars,iI,iF,nDelta);
/*        int i,len=pdData.length;
        if(nvOutliars==null) 
            nvOutliars=new ArrayList();
        else
            nvOutliars.clear();

        boolean pbOutliars[]=new boolean[len];
        setElements(pbOutliars,false);

        MeanSem1 ms;
        double mean,sd,dMin,delta,dMaxDelta;

        int newOutliars=0,maxIter=5,iter=0;

        do{
            ms=buildMeanSem1(pdData,pbOutliars,iI,iF,nDelta);
            mean=ms.mean;
            sd=ms.getSD();
            dMin=GaussianDistribution.getZatP(pValue, mean, sd, 0.01*pValue);
            dMaxDelta=Math.abs(mean-dMin);
            newOutliars=0;
            for(i=0;i<len;i++){
                delta=Math.abs(pdData[i]-mean);
                if(delta>dMaxDelta){
                    if(!pbOutliars[i])newOutliars++;
                    pbOutliars[i]=true;
                }
                else
                {
                    pbOutliars[i]=false;
                }
            }
            iter++;
        }while(newOutliars>0&&iter<=maxIter);

        mso.update(ms);

        for(i=0;i<len;i++){
            if(pbOutliars[i]) nvOutliars.add(i);
        }

        return 1;*/
    }
    static public int findOutliars(double[] pdData, boolean[] pbSelected, double pValue,MeanSem1 mso, ArrayList<Integer> nvOutliars){
        findOutliars(pdData,pbSelected,pValue,mso,nvOutliars,0,pdData.length-1,1);
        return 1;
    }
    static public boolean[] getInvert(boolean pb[]){
        int i,len=pb.length;
        boolean[] pbt=new boolean[len];
        for(i=0;i<len;i++){
            pbt[i]=!pb[i];
        }
        return pbt;
    }
    static public int findOutliars(double[] pdData, boolean[] pbSelected, double pValue,MeanSem1 mso, ArrayList<Integer> nvOutliars, int iI,int iF, int nDelta){
        int i,len=pdData.length;
        if(nvOutliars==null) 
            nvOutliars=new ArrayList();
        else
            nvOutliars.clear();
        boolean pbOutliars[];
        if(pbSelected!=null) {
            pbOutliars=getInvert(pbSelected);
        }else{
            pbOutliars=new boolean[len];
            setElements(pbOutliars,false);
        }
//        setElements(pbOutliars,false);

        MeanSem1 ms;
        double mean,sd,dMin,delta,dMaxDelta;

        int newOutliars=0,maxIter=5,iter=0;

        do{
            ms=buildMeanSem1(pdData,pbOutliars,iI,iF,nDelta);
            mean=ms.mean;
            sd=ms.getSD();
            dMin=GaussianDistribution.getZatP(pValue, mean, sd, 0.01*pValue);
            dMaxDelta=Math.abs(mean-dMin);
            newOutliars=0;
            for(i=0;i<len;i++){
                if(pbSelected!=null){
                    if(!pbSelected[i]) continue;
                }
                delta=Math.abs(pdData[i]-mean);
                if(delta>dMaxDelta){
                    if(!pbOutliars[i])newOutliars++;
                    pbOutliars[i]=true;
                }
                else
                {
                    pbOutliars[i]=false;
                }
            }
            iter++;
        }while(newOutliars>0&&iter<=maxIter);

        mso.update(ms);

        for(i=0;i<len;i++){
            if(pbOutliars[i]) nvOutliars.add(i);
        }

        return 1;
    }
    static public void makeAscendingIndexes(int[] indexes){
        int i,len=indexes.length;
        for(i=0;i<len;i++){
            indexes[i]=i;
        }
    }
    static public double[] getRWLogDevP(double[] pdY, int ws, double mu){
        double sd=CommonStatisticsMethods.buildMeanSem(pdY).getSD();
        double[] pdSD=new double[pdY.length];
        CommonStatisticsMethods.setElements(pdSD, sd);
        return getRWLogDevP(pdY,ws,mu,pdSD);
    }
    
    static public double[] getRWSD(double pdY[],int ws){
        int i,len=pdY.length,len1=2*ws+1;
        double[] pdSD=new double[len];
        double mean=0,meanSS=0,y;
        MeanSem0 ms=new MeanSem0();
        for(i=0;i<len1;i++){
            y=pdY[i];
            mean+=y;
            meanSS+=y*y;
        }
        ms.updateMeanSquareSum(len1, mean/len1, meanSS/len1);
        for(i=0;i<=ws;i++){
            pdSD[i]=ms.getSD();
        }
        for(i=ws+1;i<len-ws;i++){
            y=pdY[i-ws-1];
            mean-=y;
            meanSS-=y*y;
            y=pdY[i+ws];
            mean+=y;
            meanSS+=y*y;
            ms.updateMeanSquareSum(len1, mean/len1, meanSS/len1);
            pdSD[i]=ms.getSD();
        }
        for(i=len-ws;i<len;i++){
            pdSD[i]=ms.getSD();
        }
//        CommonStatisticsMethods.setElements(pdSD, 40);
        return pdSD;
    }
    
    static public double[] getRWSD_Cumulative(double pdY[],boolean[] pbSelected, int ws){
        return getRWMeanSD_Cumulative(pdY,pbSelected,ws).get(1);
    }
    
    static public ArrayList<double[]> getRWMeanSD_Cumulative(double pdY[],boolean[] pbSelected, int ws){
        int i,len=pdY.length,len1=2*ws+1;
        double[] pdSD=new double[len],pdMean=new double[len];
        ArrayList<double[]> pdvMeanSD=new ArrayList();
        pdvMeanSD.add(pdMean);
        pdvMeanSD.add(pdSD);
        
        double mean=0,meanSS=0,y;
        MeanSem0 ms=new MeanSem0();
        int num=0,position=-1;
        i=0;
        if(len<=len1){
            MeanSem1 mst=buildMeanSem1(pdY,getInvert(pbSelected),0,len-1,1);
            setElements(pdSD,mst.getSD());
            setElements(pdMean,mst.mean);
            return pdvMeanSD;
        }
        while(num<len1){
            i++;
            if(i>=len) break;
            position++;
            if(!pbSelected[i]) continue;
            y=pdY[i];
            mean+=y;
            meanSS+=y*y;
            num++;
        }
        ms.updateMeanSquareSum(num, mean/num, meanSS/num);
        for(i=0;i<=position;i++){
            pdSD[i]=ms.getSD();
            pdMean[i]=ms.mean;
        }
        for(i=position+1;i<len;i++){
            if(pbSelected[i]){
                y=pdY[i];
                mean+=y;
                meanSS+=y*y;
                num++;
                ms.updateMeanSquareSum(num, mean/num, meanSS/num);
            }
            pdSD[i]=ms.getSD();
            pdMean[i]=ms.mean;
        }
        double sd,sdMax=Double.NEGATIVE_INFINITY;
        for(i=pdSD.length-1;i>=0;i--){
            sd=pdSD[i];
            if(sd>sdMax)
                sdMax=sd;
            else
                pdSD[i]=sdMax;
        }
        return pdvMeanSD;
    }
    
    static public double[] getSimpleRegressionDev(SimpleRegression cSr,double[] pdX, double[] pdY,boolean[] pbSelected, int iI, int iF,ArrayList<Integer> nvSelectedIndexes){
        int i;
        nvSelectedIndexes.clear();
        ArrayList<Double> dvDev=new ArrayList();
        boolean selective=pbSelected!=null;
        for(i=iI;i<=iF;i++){
            if(selective&&!pbSelected[i]) continue;
            nvSelectedIndexes.add(i);
            dvDev.add(pdY[i]-cSr.predict(pdX[i]));
        }
        double[] pdDev=CommonStatisticsMethods.getDoubleArray(dvDev);
        return pdDev;
    }
    
    static public int getSimpleRegressionDev(SimpleRegression cSr,double[] pdX, double[] pdY,boolean[] pbSelected, int iI, int iF,ArrayList<Integer> nvSelectedIndexes, ArrayList<Double> dvDev){
        int i;
        nvSelectedIndexes.clear();
        dvDev.clear();
        boolean selective=pbSelected!=null;
        for(i=iI;i<=iF;i++){
            if(selective&&!pbSelected[i]) continue;
            nvSelectedIndexes.add(i);
            dvDev.add(pdY[i]-cSr.predict(pdX[i]));
        }
        return 1;
    }
    
    static public int getSimpleRegressionDev_SameSideNeighbors(SimpleRegression cSr,double[] pdX, double[] pdY,boolean[] pbSelected, int position, intRange positionRange, ArrayList<Integer> nvSelectedIndexes, ArrayList<Double> dvDev){
        int i;
        nvSelectedIndexes.clear();
        dvDev.clear();
        boolean selective=pbSelected!=null;
        int iI=position,iF=position;
        iI=getLastSameSidePosition(cSr,pdX,pdY,position,-1);
        iF=getLastSameSidePosition(cSr,pdX,pdY,position,1);
        for(i=iI;i<=iF;i++){
            if(!positionRange.contains(i)) continue;
            if(selective&&!pbSelected[i]) continue;
            nvSelectedIndexes.add(i);
            dvDev.add(pdY[i]-cSr.predict(pdX[i]));
        }
        return 1;
    }
    
    static public int getLastSameSidePosition(SimpleRegression cSr, double[] pdX, double[] pdY, int position0, int delta){
        int iF=pdX.length-1,position;
        if(delta<0) iF=0;
        position=position0;
        double y0=pdY[position0]-cSr.predict(pdX[position0]),y;
        position+=delta;
        if(delta*(position-iF)>0) return position-delta;
        y=pdY[position]-cSr.predict(pdX[position]);
        while(y*y0>0){
            position+=delta;
            if(delta*(position-iF)>0) return position-delta;
            y=pdY[position]-cSr.predict(pdX[position]);
        }

        return position-delta;
    }
    
    static public void copyArrayToList(double[] pdY, ArrayList<Double> dvY, int iI, int iF, int delta){
        dvY.clear();
        for(int i=iI;i<=iF;i+=delta){
            dvY.add(pdY[i]);
        }
    }
    static public double[] getRWLogDevP(double[] pdDev, int ws, double mu, double[] pdSD){
        int i,j,len=pdDev.length,len1=2*ws+1;
        double[] pdLogDevP=new double[len];
        double dLog10=Math.log(10),p,logp;
        double mean=0,meanSS=0,y;
        MeanSem0 ms=CommonStatisticsMethods.buildMeanSem(pdDev);
        for(i=0;i<len1;i++){
            y=pdDev[i];
            mean+=y;
            meanSS+=y*y;
        }
        ms.updateMeanSquareSum(len1, mean/len1, meanSS/len1);
        p=1.-GaussianDistribution.Phi(Math.abs(ms.mean), 0, ms.getSD());
        p=1.-GaussianDistribution.Phi(Math.abs(mean/len1), mu, pdSD[0]/Math.sqrt(len1));
        if(p<1.E-20) p=1E-20;
        logp=Math.log(p)/dLog10;
        for(i=0;i<ws+1;i++){
            pdLogDevP[i]=logp;
        }
        for(i=ws+1;i<len-ws;i++){
            y=pdDev[i-ws-1];
            mean-=y;
            meanSS-=y*y;
            y=pdDev[i+ws];
            mean+=y;
            meanSS+=y*y;
//            ms.updateMeanSquareSum(len1, mean/len1, meanSS/len1);
//            p=1.-GaussianDistribution.Phi(Math.abs(ms.mean), 0, ms.getSD());
            p=1.-GaussianDistribution.Phi(Math.abs(mean/len1), 0,pdSD[i]/Math.sqrt(len1));
            if(p<1.E-20) p=1E-20;
            logp=Math.log(p)/dLog10;
            pdLogDevP[i]=logp;
        }
        for(i=len-ws;i<len;i++){
            pdLogDevP[i]=logp;
        }
        return pdLogDevP;
    }
    static public double getSD_DeltabasedOutliarExcludingMeanSem(double[] pdY, int nInterval,double pV){
        return getSD_DeltabasedOutliarExcludingMeanSem(pdY,null,nInterval,pV);
    }
    static public double getSD_DeltabasedOutliarExcludingMeanSem(double[] pdY, boolean[] pbExcessiveDelta, int nInterval,double pV){
        double sd;
        int nDataSize=pdY.length;
        int[] pnIntervals=null;
        if(pbExcessiveDelta!=null) pnIntervals=calRisingIntervals(pdY,nInterval);
        double pdDelta[]=CommonStatisticsMethods.getDeltaArray(pdY, pnIntervals, nInterval);
        int i;
//        CommonStatisticsMethods.makeMedianReflection(pdDelta);
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> indexes=new ArrayList();
        CommonStatisticsMethods.findOutliars(pdDelta, pV, ms, indexes);
        int j,jI,jF;
        if(pbExcessiveDelta!=null){
            CommonStatisticsMethods.setElements(pbExcessiveDelta, false);
            for(i=0;i<indexes.size();i++){
                jI=indexes.get(i);
                jF=jI+pnIntervals[i];
                for(j=jI;j<=jF;j++){
                    pbExcessiveDelta[j]=true;
                }
            }
        }
        sd=ms.getSD()/Math.sqrt(2);
        return sd;
    }
    static public double getMaxDeltaDev(double[] pdY, double yo, int ws){
        double delMax=0,dev0,dev,delta;
        ArrayList<Integer> crossPoints=getCrossingPositions(pdY,yo);
        int i,p,n,len=crossPoints.size();
        p=0;
        n=crossPoints.get(0);
        dev0=getMaxAbsDev(pdY,p,n,yo,ws);
        for(i=1;i<len;i++){
            p=n+1;
            n=crossPoints.get(i);
            dev=getMaxAbsDev(pdY,p,n,yo,ws);
            delta=dev-dev0;
            if(Math.abs(delta)>Math.abs(delMax)) delMax=delta;
            dev0=dev;
        }
        p=n+1;
        n=pdY.length-1;
        dev=getMaxAbsDev(pdY,p,n,yo,ws);
        delta=dev-dev0;
        if(Math.abs(delta)>Math.abs(delMax)) delMax=delta;
        return delMax;
    }
    static public double getMaxDeltaDev(double[] pdY, double[] pdSD, double yo, int ws){
        double delMax=0,dev0,dev,delta,sd;
        ArrayList<Integer> crossPoints=getCrossingPositions(pdY,yo);
        int i,p,n,len=crossPoints.size();
        p=0;
        n=crossPoints.get(0);
        dev0=getMaxAbsDev(pdY,pdSD,p,n,yo,ws);
        for(i=1;i<len;i++){
            p=n+1;
            n=crossPoints.get(i);
            sd=pdSD[i];
            dev=getMaxAbsDev(pdY,pdSD,p,n,yo,ws);
            delta=dev-dev0;
            if(Math.abs(delta)>Math.abs(delMax)) delMax=delta;
            dev0=dev;
        }
        p=n+1;
        n=pdY.length-1;
        dev=getMaxAbsDev(pdY,pdSD,p,n,yo,ws);
        delta=dev-dev0;
        if(Math.abs(delta)>Math.abs(delMax)) delMax=delta;
        return delMax;
    }

    static public double getMaxAbsDev(double[] pdY, int iI, int iF, double yo, int ws){
        double devMax=0,dev;
        iF-=ws-1;
        int i,j;
        for(i=iI;i<=iF;i++){
            dev=0;
            for(j=0;j<ws;j++){
                if(i+j>=pdY.length) return 0;
                dev+=pdY[i+j]-yo;
            }
            if(Math.abs(dev)>Math.abs(devMax)) devMax=dev;
        }
        return devMax;
    }
    static public double getMaxAbsDev(double[] pdY, double[] pdSD, int iI, int iF, double yo, int ws){
        double devMax=0,dev;
        iF-=ws-1;
        int i,j;
        for(i=iI;i<=iF;i++){
            dev=0;
            for(j=0;j<ws;j++){
                if(i+j>=pdY.length) return 0;
                dev+=(pdY[i+j]-yo)/pdSD[i];
            }
            if(Math.abs(dev)>Math.abs(devMax)) devMax=dev;
        }
        return devMax;
    }

    static public ArrayList<Integer> getCrossingPositions(double[] pdY, double yo){//finding positions where (pdY[i]-yo)*(pdY[i+1]-yo)<=0.
        return getCrossingPositions(pdY,yo,0,pdY.length-1,1);
    }
    static public ArrayList<Integer> getCrossingPositions(double[] pdY, double yo, int iI, int iF, int deltaI){//finding positions where (pdY[i]-yo)*(pdY[i+1]-yo)<=0.
        ArrayList<Integer> indexes=new ArrayList();
        double delta0,delta;
        if(iI>=pdY.length) return indexes;
        delta0=pdY[iI]-yo;
        for(int i=iI;i<iF;i+=deltaI){
            delta=pdY[i+1]-yo;
            if(delta0*delta<=0) indexes.add(i);
            delta0=delta;
        }
        return indexes;
    }
    static public int getNextCrossingPosition(PolynomialRegression cPr, double[] pdX, double[] pdY, int iI, int iF, int nDelta){
        //finding the first point where the prediction line cross the data line
        //returning -iF if there is no crossing
        double delta0,delta;
        delta0=pdY[iI]-cPr.predict(pdX[iI]);
        int i=iI+nDelta;
        while(!((i-iI)*(i-iF)>0)){
            delta=pdY[i]-cPr.predict(pdX[i]);
            if(delta0*delta<=0) return i;
            delta0=delta;
            i+=nDelta;
        }
        return -iF;//there is no crossing point in the region
    }
    static public int getNextCrossingPosition(PolynomialRegression cPr, double[] pdX, double[] pdY, boolean[] pbSelected, int iI, int iF, int nDelta){
        //finding the first point where the prediction line cross the data line
        //returning -iF if there is no crossing
        double delta0,delta;
        iI=getFirstSelectedPosition(pbSelected, true,iI+nDelta,nDelta);
        if(iI<0) return -1;
        iF=getFirstSelectedPosition(pbSelected,true,iF-nDelta,-nDelta);
        if(iF<0) return -1;
        if(nDelta*(iI-iF)>=0) return -1;
        delta0=pdY[iI]-cPr.predict(pdX[iI]);
        int i=getFirstSelectedPosition(pbSelected,true,iI+nDelta,nDelta);
        while(!((i-iI)*(i-iF)>0)){
            delta=pdY[i]-cPr.predict(pdX[i]);
            if(delta0*delta<=0) return i;
            delta0=delta;
            i=getFirstSelectedPosition(pbSelected,true,i+nDelta,nDelta);
        }
        return -iF;//there is no crossing point in the region
    }
    static public ArrayList<Integer> getCrossingPositions(ArrayList<Integer> dv, double yo, int iI, int iF, int deltaI){//finding positions where (pdY[i]-yo)*(pdY[i+1]-yo)<=0.
        ArrayList<Integer> indexes=new ArrayList();
        double delta0,delta;
        delta0=dv.get(iI) -yo;
        for(int i=iI;i<iF;i+=deltaI){
            delta=dv.get(i+1)-yo;
            if(delta0*delta<=0) indexes.add(i);
            delta0=delta;
        }
        return indexes;
    }
    static public ArrayList<Integer> getCrossingPositions(double[] pdY, double[] pdRef, int iI, int iF, int deltaI){//finding positions where (pdY[i]-pdYRef[i])*(pdY[i+1]-pdYRef[i+1)<=0.
        ArrayList<Integer> indexes=new ArrayList();
        double delta0,delta;
        delta0=pdY[0]-pdRef[0];
        for(int i=iI;i<iF;i+=deltaI){
            delta=pdY[i+1]-pdRef[i+1];
//            if(delta0*delta<=0) indexes.add(i);
            if(delta==0||delta0*delta<0) indexes.add(i);//13123
            delta0=delta;
        }
        return indexes;
    }
    static public void markExcessiveDelta(double[] pdY, boolean pbExcessiveDelta[], int interval, double pV){
        getSD_DeltabasedOutliarExcludingMeanSem(pdY,pbExcessiveDelta,interval,pV);
    }
    public static boolean[] getEmptyBooleanArray(boolean[] pbT, int len){
        if(pbT==null) return new boolean[len];
        if(pbT.length!=len) return new boolean[len];
        return pbT;
    }
    public static int [] getEmptyIntArray(int[] pnT, int len){
        if(pnT==null) return new int[len];
        if(pnT.length!=len) return new int[len];
        return pnT;
    }
    public static double[] getEmptyDoubleArray(double[] pdT, int len){
        if(pdT==null) return new double[len];
        if(pdT.length!=len) return new double[len];
        return pdT;
    }
    public static double[] getDeltaBasedPositionDependentSD(double[] pdY, int nMaxRisingInterval, int ws){
        int len=pdY.length;
        if(len<nMaxRisingInterval+3) return null;
        int[] pnIntervals=new int[len];
        pnIntervals=calRisingIntervals(pdY,nMaxRisingInterval);
        return getDeltaBasedPositionDependentSD(pdY,pnIntervals,ws);
    }
    public static double[] getDeltaBasedPositionDependentSD(double[] pdY, int[] pnIntervals, int ws){
        double[] pdSD=null;
        double[] pdDelta=getDeltaArray(pdY,pnIntervals,0);
        double[] pdDelta1=new double[pdY.length];
        double sr2=Math.sqrt(2);
        int i,len=pdY.length,len1=pdDelta.length;
        for(i=0;i<len;i++){
            if(i<len1)
                pdDelta1[i]=pdDelta[i]/sr2;
            else
                pdDelta1[i]=pdDelta[len1-1]/sr2;
        }
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> nvOutliarIndexes=new ArrayList();
        CommonStatisticsMethods.findOutliars(pdDelta1, 0.001, ms, nvOutliarIndexes);
        boolean pbSelected[]=new boolean[pdY.length];
        CommonStatisticsMethods.setElements(pbSelected, true);
        len=nvOutliarIndexes.size();
        for(i=0;i<len;i++){
            pbSelected[nvOutliarIndexes.get(i)]=false;
        }
        pdSD=CommonStatisticsMethods.getRWSD_Cumulative(pdDelta1, pbSelected, ws);
        return pdSD;
    }
    public static double[] getDeltaBasedPositionDependentSD(double[] pdX, double[] pdY, int[] pnIntervals, boolean[] pbSelected, int ws){
        
        double[] pdDelta=getDeltaArray(pdY,pnIntervals,0);
        
        boolean[] pbSelectedDelta=CommonStatisticsMethods.getDeltaSelection_AND(pbSelected, pnIntervals);
        interpolate(pdX,pdDelta,pbSelectedDelta);
        
        double[] pdDelta1=new double[pdY.length];
        int i,len=pdY.length,len1=pdDelta.length;
        for(i=0;i<len;i++){
            if(i<len1)
                pdDelta1[i]=pdDelta[i];
            else
                pdDelta1[i]=pdDelta[len1-1];
        }
        double[] pdSD=getDeltaBasedPositionDependentSD(pdDelta1,ws);
        return pdSD;
    }
    public static double[] getDevArray(double[] pdX, double[] pdY){
         int i,len=pdX.length;
         double[] pdDev=new double[len];
         for(i=1;i<len-1;i++){
             pdDev[i]=pdY[i]-CommonMethods.getLinearIntoplation(pdX[i-1], pdY[i-1], pdX[i+1], pdY[i+1], pdX[i]);
         }
         pdDev[0]=-pdDev[1];
         pdDev[len-1]=-pdDev[len-2];
         return pdDev;
    }
    public static double[] getDevArray(double[] pdX, double[] pdY, ArrayList<Integer> positions, int interval){
         int i,len=pdX.length,len1=positions.size(),position,left,right;
         double[] pdDev=new double[len1];
         double y,yl,yr,sign;
         for(i=1;i<len1-1;i++){
             position=positions.get(i);
             left=Math.max(0, position-interval);
             right=Math.min(len-1, position+interval);
             y=pdY[position];
             yl=pdY[left];
             yr=pdY[right];
             sign=1.;
             if(y<yr) sign=-1;
             yl=y+sign*(Math.abs(y-yl));
//             pdDev[i]=pdY[position]-CommonMethods.getLinearIntoplation(pdX[left], pdY[left], pdX[right], pdY[right], pdX[position]);
             pdDev[i]=y-CommonMethods.getLinearIntoplation(pdX[left], yl, pdX[right], yr, pdX[position]);
         }
         pdDev[0]=-pdDev[1];
         pdDev[len1-1]=-pdDev[len1-2];
         return pdDev;
    }
//    public static int getRisingInterval(int[] pnRisingIntervals, int index0, int direction){
    public static double[] getDevArray(double[] pdX, double[] pdY, int[] pnRisingIntervals){
         int len=pdX.length,position,left,right,intervalL,intervalR;
         double[] pdDev=new double[len];
         setElements(pdDev,0);
         double y,yl,yr,h;
         for(position=0;position<len;position++){
             if(position==28){
                 position=position;
             }
             intervalL=getRisingInterval(pnRisingIntervals, position,-1);
             intervalR=getRisingInterval(pnRisingIntervals, position,1);
             
             if(intervalL==0||intervalR==0) continue;
             
             left=position+intervalL;
             right=position+intervalR;
             y=pdY[position];
             yl=pdY[left];
             yr=pdY[right];
             h=y-CommonMethods.getLinearIntoplation(pdX[left], yl, pdX[right], yr, pdX[position]);
             pdDev[position]=h;
         }
         return pdDev;
    }
    public static double[] getDevArray(double[] pdX, double[] pdY, int interval){
         int len=pdX.length,position,left,right,intervalL,intervalR;
         double[] pdDev=new double[len];
         setElements(pdDev,0);
         double y,yl,yr,h;
         for(position=interval;position<len-interval;position++){             
             left=position-interval;
             right=position+interval;
             y=pdY[position];
             yl=pdY[left];
             yr=pdY[right];
             h=y-CommonMethods.getLinearIntoplation(pdX[left], yl, pdX[right], yr, pdX[position]);
             pdDev[position]=h;
         }
         return pdDev;
    }
    public static ArrayList<double[]> getDevArray(double[] pdX, double[] pdY, ArrayList<Integer> nvPositions){
         int len=nvPositions.size(),i,position,left,right;
         double[] pdDev=new double[len],pdXT=new double[len];
         double y,yl,yr,h;
         for(i=1;i<len-1;i++){     
             position=nvPositions.get(i);
             left=nvPositions.get(i-1);
             right=nvPositions.get(i+1);
             y=pdY[position];
             yl=pdY[left];
             yr=pdY[right];
             h=y-CommonMethods.getLinearIntoplation(pdX[left], yl, pdX[right], yr, pdX[position]);
             pdDev[i]=h;
             pdXT[i]=pdX[position];
         }
         pdXT[0]=pdX[nvPositions.get(0)];
         pdDev[0]=0;
         pdXT[len-1]=pdX[nvPositions.get(len-1)];
         pdDev[len-1]=0;
         ArrayList<double[]> pdv=new ArrayList();
         pdv.add(pdXT);
         pdv.add(pdDev);
         return pdv;
    }
    public static ArrayList<Integer> getConvexPoints(double[] pdX, double[] pdY, int interval, int sign){
        ArrayList<Integer> points=new ArrayList();
        double[] pdDev=getDevArray(pdX,pdY,interval);
        for(int i=0;i<pdDev.length;i++){
            if(pdDev[i]*sign>0) points.add(i);
        }
        return points;
    }
    public static double[] getDeltaBasedPositionDependentSD(double[] pdDeltaY, int ws){
        int i,len=pdDeltaY.length;
        double[] pdSD=new double[len];
        boolean[] pbSelected=new boolean[len];
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> nvOutliarIndexes=new ArrayList();
        CommonStatisticsMethods.findOutliars(pdDeltaY, 0.01, ms, nvOutliarIndexes);
        CommonStatisticsMethods.setElements(pbSelected, true);
        len=nvOutliarIndexes.size();
        for(i=0;i<len;i++){
            pbSelected[nvOutliarIndexes.get(i)]=false;
        }
        for(i=0;i<pdDeltaY.length;i++){
            if(pdDeltaY[i]==0) pbSelected[i]=false;
        }
        pdSD=CommonStatisticsMethods.getRWSD_Cumulative(pdDeltaY, pbSelected, ws);
        scaleArray(pdSD,1./Math.sqrt(2.));
        return pdSD;
    }
    public static ArrayList<double[]> getRWSD_Cumulative(double[] pdData, boolean[] pbSelected, int ws, double dPValue){
        int i,len=pdData.length;
        double[] pdSD=new double[len];
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> nvOutliarIndexes=new ArrayList();
        CommonStatisticsMethods.findOutliars(pdData, pbSelected, dPValue, ms, nvOutliarIndexes);
        CommonStatisticsMethods.setElements(pbSelected, true);
        len=nvOutliarIndexes.size();
        for(i=0;i<len;i++){
            pbSelected[nvOutliarIndexes.get(i)]=false;
        }
        return CommonStatisticsMethods.getRWMeanSD_Cumulative(pdData, pbSelected, ws);
    }
    public static double[] getDeltaArray(double[] pdData, int[] pnIntervals, int intervalMax){
        if(pnIntervals==null) pnIntervals=calRisingIntervals(pdData,intervalMax);
        int i,len=pdData.length;
        double[] pdDelta=new double[len-intervalMax];
        for(i=0;i<pnIntervals.length;i++){
            pdDelta[i]=pdData[i]-pdData[i+pnIntervals[i]];
        }
        return pdDelta;
    }
    public static void markOutliars(double[] pdData, boolean[] pbOutliars,ArrayList<Integer> indexes, double sd, double pValue){
        int i,len=pdData.length;
        MeanSem0 ms=buildMeanSem(pdData);
        double z=GaussianDistribution.getZatP(pValue, ms.mean, sd, pValue*0.01);
        double devMax=Math.abs(z-ms.mean),dev;
        setElements(pbOutliars,false);
        indexes.clear();
        for(i=0;i<len;i++){
            dev=Math.abs(pdData[i]-ms.mean);
            if(dev>devMax) {
                pbOutliars[i]=true;
                indexes.add(i);
            }
        }
    }
    public static void consolidateRanges(ArrayList<intRange> ira){
        int index,i,j,len=ira.size(),len1;
        intRange ir,ir1;
        for(i=0;i<len;i++){
            len1=ira.size();
            if(len1<=i) break;
            ir=ira.get(i);
            for(j=len1-1;j>i;j--){
                ir1=ira.get(j);
                if(ir.overlapOrconnected(ir1)){
                    ir.mergeRanges(ir1);
                    ira.remove(j);
                }
            }
        }
    }
    public static float[] getFloatArray(double[] pdData, intRange ir){
        float[] pfData=new float[ir.getRange()];
        int iI=ir.getMin(),iF=ir.getMax();
        for(int i=iI;i<=iF;i++){
            pfData[i-iI]=(float)pdData[i];
        }
        return pfData;
    }
    public static double[] getDoubleArray(double[] pdData, intRange ir){
        double[] pfData=new double[ir.getRange()];
        int iI=ir.getMin(),iF=ir.getMax();
        for(int i=iI;i<=iF;i++){
            pfData[i-iI]=pdData[i];
        }
        return pfData;
    }
    static public void markExcessiveDeltaPoints(double[] pdY, boolean[] pbExcessiveDeltaPoints, ArrayList<intRange> ira, int interval, double pValue){
        int i,j,len=pdY.length;
        int[] pnIntervals=calRisingIntervals(pdY,interval);
        double[] pbDelta=CommonStatisticsMethods.getDeltaArray(pdY,pnIntervals,interval);
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> indexes=new ArrayList();
        CommonStatisticsMethods.setElements(pbExcessiveDeltaPoints, false);
        CommonStatisticsMethods.findOutliars(pbDelta, pValue, ms, indexes);
        CommonStatisticsMethods.markOutliars(pbDelta,pbExcessiveDeltaPoints,indexes,ms.getSD(),pValue);
        ira.clear();
        int index;
        intRange ir;
        for(i=0;i<indexes.size();i++){
            index=indexes.get(i);
            ira.add(new intRange(index,index+pnIntervals[i]));
        }
        for(i=0;i<ira.size();i++){
            ir=ira.get(i);
            for(j=ir.getMin();j<=ir.getMax();j++){
                pbExcessiveDeltaPoints[j]=true;
            }
        }
    }
    static public void markExcessiveDeltaPoints(double[] pdY, double[] pdSD, double factor, ArrayList<intRange> ira, int interval, double pValue){
        int i,j;
        int[] pnIntervals=calRisingIntervals(pdY,interval);
        double[] pdDelta=CommonStatisticsMethods.getDeltaArray(pdY,pnIntervals,interval);
        ira.clear();
        int len=pdDelta.length;
        intRange ir;
        double delta,p,sd;
        for(i=0;i<len;i++){
            delta=pdDelta[i];
            sd=pdSD[i]*factor;
            p=1.-GaussianDistribution.Phi(Math.abs(delta), 0, sd);
            if(p<pValue){
                ira.add(new intRange(i,i+pnIntervals[i]));
            }
        }
    }
    static public String[][] addRow(String[][] psData, String[][] psData1, int row){
        int i,len=psData.length;
        String[][] psT=new String[len+1][];
        for(i=0;i<len;i++){
            psT[i]=psData[i];
        }
        psT[len]=psData1[row];
        return psT;
    }
    static public intRange getNonNullElementRange(Object[] poData){
        int i,len=poData.length,start=0,end=len-1;
        while(poData[start]==null&&start<len){
            start++;
        }
        while(poData[end]==null&&end>=0){
            end--;
        }
        return new intRange(start,end);
    }
    static public double[] copyLogrithmOfArray(double[] pdData){
        int i,len=pdData.length;
        double[] pdT=new double[len];
        for(i=0;i<len;i++){
            pdT[i]=Math.log10(pdData[i]);
        }
        return pdT;
    }
    public static int[] calRisingIntervals(ArrayList<Double> dvY, int maxRisingInterval){
        int i,len=dvY.size(),interval,maxInterval,len1=len-maxRisingInterval;
        if(len1<0) return null;
        double y,y0,delta0,delta;
        int pnIntervals[]=new int[len1];
        for(i=0;i<len1;i++){
            interval=1;
            y0=dvY.get(i);
            y=dvY.get(i+interval);
            delta0=y-y0;
            y0=y;
            while(interval<maxRisingInterval){
                interval++;
                y=dvY.get(i+interval);
                delta=y-y0;
                if(delta0*delta<=0){
                    interval--;
                    break;
                }
                y0=y;
                delta0=delta;
            }
            pnIntervals[i]=interval;
        }
        return pnIntervals;
    }
    public static int[] calRisingIntervals0(double[] pdY, int maxRisingInterval){
        int i,len=pdY.length,interval,len1=len-maxRisingInterval,nIntervalX;
        if(len<=0) return null;
        double y,y0,delta,dMax;
        int pnIntervals[]=new int[len1];
        for(i=0;i<len1;i++){
            interval=0;
            nIntervalX=1;
            y0=pdY[i];
            dMax=0;
            while(interval<maxRisingInterval){
                interval++;
                y=pdY[i+interval];
                delta=Math.abs(y-y0);
                if(delta>dMax){
                    dMax=delta;
                    nIntervalX=interval;
                }
            }
            pnIntervals[i]=nIntervalX;
        }
        return pnIntervals;
    }
    public static int[] calRisingIntervals(double[] pdY, int maxRisingInterval){
        int i,len=pdY.length,interval,len1=len-maxRisingInterval;
        if(len<=0) return null;
        double y,y0,delta0,delta;
        int pnIntervals[]=new int[len1];
        for(i=0;i<len1;i++){
            interval=1;
            y0=pdY[i];
            y=pdY[i+interval];
            delta0=y-y0;
            y0=y;
            while(interval<maxRisingInterval){
                interval++;
                y=pdY[i+interval];
                delta=y-y0;
                if(delta0*delta<=0){
                    interval--;
                    break;
                }
                y0=y;
                delta0=delta;
            }
            pnIntervals[i]=interval;
        }
        return pnIntervals;
    }
    public static int getRisingInterval(int[] pnRisingIntervals, int index0, int direction){//returns a negative interval for negative direction //12o16
        if(index0<0||index0>=pnRisingIntervals.length) return 0;
        if(index0==0&&direction<0) return 0;
        if(index0==pnRisingIntervals.length-1&&direction>0) return 0;
        if(direction==1) return pnRisingIntervals[index0];
        int interval=-1,index=index0-1;
        if(direction==-1) {
            if(index+pnRisingIntervals[index]<index0) return interval;
            while(index>0){
                interval--;
                index=index0+interval;
                if(index+pnRisingIntervals[index]<index0){
                    interval++;
                    break;
                }
            }
        }
        return interval;
    }
    public static ArrayList<Double> copyFrontElements(ArrayList<Double> dv,int nNum){
        ArrayList<Double> dvt=new ArrayList();
        nNum=Math.min(nNum, dv.size());
        for(int i=0;i<nNum;i++){
            dvt.add(dv.get(i));
        }
        return dvt;
    }
    public static double[] mergeArray(ArrayList<Double> dvL, ArrayList<Double> dvR){
        ArrayList<Double> dv=new ArrayList();

        int i,len=dvL.size()+dvR.size();
        double pdV[] =new double[len];
        int it=0;
        for(i=0;i<dvL.size();i++){
            pdV[it]=dvL.get(i);
            it++;
        }
        for(i=0;i<dvR.size();i++){
            pdV[it]=dvR.get(i);
            it++;
        }
        return pdV;
    }
    public static double[] mergeArray(double[] pdL, double[] pdR){
        ArrayList<Double> dv=new ArrayList();

        int i,len=pdL.length+pdR.length;
        double pdV[] =new double[len];
        int it=0;
        for(i=0;i<pdL.length;i++){
            pdV[it]=pdL[i];
            it++;
        }
        for(i=0;i<pdR.length;i++){
            pdV[it]=pdR[i];
            it++;
        }
        return pdV;
    }
    public static intRange getSelectedRange(boolean[] pbSelection, boolean selection){
        intRange ir=new intRange();
        int i,len=pbSelection.length;
        for(i=0;i<len;i++){
            if(pbSelection[i]==selection) ir.expandRange(i);
        }
        return ir;
    }
    public static ArrayList<intRange> getConsecutiveIndexRanges(ArrayList<Integer> nvIndexes){
        intRange ir=getDataRange(nvIndexes);
        int i,len=ir.getMax()+1;
        boolean[] pb=new boolean[len];
        setElements(pb,false);
        setElements(pb,nvIndexes,true);
        return getSelectedRanges(pb);
    }
    public static intRange getDataRange(ArrayList<Integer> nvt){
        intRange ir=new intRange();
        for(int i=0;i<nvt.size();i++){
            ir.expandRange(nvt.get(i));
        }
        return ir;
    }
    public static ArrayList<intRange> getSelectedRanges(boolean pbSelected[]){
        int i,len=pbSelected.length;
        ArrayList<intRange> cvRanges=new ArrayList();
        intRange[] pcRanges=new intRange[len];
        intRange ir=null;
        if(pbSelected[0]) {
            ir=new intRange(0,0);
            cvRanges.add(ir);
            pcRanges[0]=ir;
        }
        for(i=1;i<len;i++){
            if(pbSelected[i]){
                ir=pcRanges[i-1];
                if(ir==null){
                    ir=new intRange(i,i);
                    pcRanges[i]=ir;
                    cvRanges.add(ir);
                } else{
                    ir.expandRange(i);
                }
                pcRanges[i]=ir;
            }
        }
        return cvRanges;
    }
    public static ArrayList<Double> copySelectedDataToArrayList(double[] pdY, boolean[] pbSelected, int iI, int iF, int delta){
        ArrayList<Double> dvY=new ArrayList();
        boolean selective=pbSelected!=null;
        for(int i=iI;i<=iF;i++){
            if(selective) {
                if(!pbSelected[i]) continue;
            }
            dvY.add(pdY[i]);
        }
        return dvY;
    }
    public static double[] copyArrayToArray(double[] pdData,int iI, int iF, int delta){
        double[] pdT=new double[iF-iI+1];
        for(int i=iI;i<=iF;i+=delta){
            pdT[i-iI]=pdData[i];
        }
        return pdT;
    }
    public static double[] copyArrayToArray(double[] pdData,boolean[] pbSelected, int iI, int iF, int delta){
        double[] pdT=new double[iF-iI+1];
        for(int i=iI;i<=iF;i+=delta){
            if(pbSelected!=null){
                if(!pbSelected[i]) continue;
            }
            pdT[i-iI]=pdData[i];
        }
        return pdT;
    }
    public static double getSum(double pdData[], int iI, int iF, int delta){
        double sum=0;
        for(int i=iI;i<=iF;i+=delta){
            sum+=pdData[i];
        }
        return sum;
    }
    public static void divideArrays(double[] pdV1, double[] pdV2){
        int i,len=pdV1.length;
        for(i=0;i<len;i++){
            pdV1[i]/=pdV2[i];
        }
    }
    public static void divideArrays(double[] pdV1, ArrayList<Double> dvV2){
        int i,len=pdV1.length;
        for(i=0;i<len;i++){
            pdV1[i]/=dvV2.get(i);
        }
    }
    public static ArrayList<double[]> getSelectedData(boolean pbSelected[],ArrayList<Integer> nvSelectedIndexes,  ArrayList<double[]> pdvData){
       int i,j,index,len=pbSelected.length,len1=pdvData.size();
        ArrayList<double[]> pdvSelectedData= new ArrayList();
        nvSelectedIndexes.clear();

        for(i=0;i<len;i++){
            if(pbSelected[i]) nvSelectedIndexes.add(i);
        }
        len=nvSelectedIndexes.size();
        for(i=0;i<len1;i++){
            pdvSelectedData.add(new double[len]);
        }
        for(i=0;i<len;i++){
            index=nvSelectedIndexes.get(i);
            for(j=0;j<len1;j++){
                pdvSelectedData.get(j)[i]=pdvData.get(j)[index];
            }
        }
        return pdvSelectedData;
    }
    public static String getStringArrayAsDelimitedString(String[] psT,String sDelimiter){
        String sT="";
        int i,len=psT.length;
        for(i=0;i<len;i++){
            sT+=psT[i];
            sT+=sDelimiter;
        }
        return sT;
    }
    public static ArrayList<String> getTokensAsStringArrayList(String sT,String sDelimiters){
        StringTokenizer stk=new StringTokenizer(sT,sDelimiters);
        ArrayList<String> svT=new ArrayList();
        while(stk.hasMoreTokens()){
            svT.add(stk.nextToken());
        }
        return svT;
    }
    public static String[][] getDelimitedStringAsStringArray(String psT, String newString, String newLine){
        ArrayList<String> svLines=getTokensAsStringArrayList(psT,newLine),svWords=new ArrayList();
        int i,lines=svLines.size(),j,words;
        String[][] psData=new String[lines][];
        String line;
        for(i=0;i<lines;i++){
            line=svLines.get(i);
            svWords=getTokensAsStringArrayList(line, newString);
            words=svWords.size();
            String[] psTT=new String[words];
            for(j=0;j<words;j++){
                psTT[j]=svWords.get(j);
            }
            psData[i]=psTT;
        }
        return psData;
    }
    public static ArrayList<Integer> getConnectedOrOverlappedIndexes(ArrayList<intRange> irs, intRange ir){
        ArrayList<Integer> indexes=new ArrayList();
        intRange irt;
        for(int i=0;i<irs.size();i++){
            irt=irs.get(i);
            if(ir.overlapOrconnected(irt)) {
                ir.expandRange(irt);
                indexes.add(i);
            }
        }
        return indexes;
    }
    public static double getMean(double[] pdData, boolean[] pbSelection, int iI, int iF, int delta){
        double mean=0,num=0;
        boolean selecting=pbSelection!=null;
        int i=iI;
        while(!((i-iI)*(i-iF)>0)){
            if(selecting){
                if(!pbSelection[i]) {
                    i+=delta;
                    continue;
                }
            }
            mean+=pdData[i];
            num+=1;
            i+=delta;
        }
        return mean/num;
    }
    
    public static double getMedian(double[] pdData, boolean[] pbSelection, int iI, int iF, int delta){
        DoubleRange dRange=new DoubleRange();
        getRange(pdData,iI,iF,delta,dRange);
        IndexOrganizerHist hist=new IndexOrganizerHist();
        boolean selecting=pbSelection!=null;
        int nDim=20;
        int num=0;
        hist.update(0,nDim, dRange.getMin(), dRange.getRange()/nDim,true,false);//this will not affect quantile calculations
        for(int i=iI;i<=iF;i+=delta){
            if(selecting){
                if(!pbSelection[i]) continue;
            }
            hist.addIndex(pdData[i],i);
            num++;
        }
        int rank=(num-1)/2;
        if(num==0) return Double.NaN;
        double median=hist.getRankingData(rank);
        if(num%2==0){
            median+=hist.getRankingData(rank+1);
            median*=0.5;
        }
        return median;
    }
    
    public static ArrayList<Integer> getPairedNumbers(ArrayList<IntPair> cvPairs, int iT){
        int i,len=cvPairs.size();
        ArrayList<Integer> nvNums=new ArrayList();
        IntPair ip;
        for(i=0;i<len;i++){
            ip=cvPairs.get(i);
            if(!ip.contains(iT)) continue;
            nvNums.add(ip.getPairedNumber(iT));
        }
        return nvNums;
    }
    static public ArrayList<Integer> getSmallerElements(ArrayList<Integer> nvT, int nT){
        int i,len=nvT.size(),it;
        ArrayList<Integer> nvS=new ArrayList();
        for(i=0;i<len;i++){
            it=nvT.get(i);
            if(it<nT) nvS.add(it);
        }
        return nvS;
    }
    static public ArrayList<Integer> getLargerElements(ArrayList<Integer> nvT, int nT){
        int i,len=nvT.size(),it;
        ArrayList<Integer> nvL=new ArrayList();
        for(i=0;i<len;i++){
            it=nvT.get(i);
            if(it>nT) nvL.add(it);
        }
        return nvL;
    }
    static public ArrayList<intRange> mergeRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        int len1=irs1.size(),len2=irs2.size(),len=Math.max(len1, len2);
        int i,j;
        intRange ir=new intRange();
        for(i=0;i<len;i++){
            if(i<len1) ir.expandRange(irs1.get(i));
            if(i<len2) ir.expandRange(irs2.get(i));
        }
        if(ir.emptyRange()) return new ArrayList<intRange>();
        boolean pbMarked[]=new boolean[ir.getMax()+1];
        setElements(pbMarked,false);

        for(i=0;i<len;i++){
            if(i<len1){
                ir=irs1.get(i);
                setElements(pbMarked,true,ir.getMin(),ir.getMax(),1);
            }
            if(i<len2){
                ir=irs2.get(i);
                setElements(pbMarked,true,ir.getMin(),ir.getMax(),1);
            }
        }

        return getMarkedRanges(pbMarked);
    }
    static public ArrayList<intRange> getMarkedRanges(boolean[] pbMarked){
        int i,len=pbMarked.length;
        intRange ir;
        ArrayList<intRange> irs=new ArrayList();
        int start=-1,end;
        boolean bMarked=false;
        for(i=0;i<len;i++){
            bMarked=pbMarked[i];
            if(bMarked){
                if(start<0) start=i;
            }else{
                if(start>0){
                    end=i-1;
                    irs.add(new intRange(start,end));
                    start=-1;
                }
            }
        }
        if(start>0){
            irs.add(new intRange(start,len-1));
        }
        return irs;
    }
    static public void PartialSort(double[] pdData, int[] indexes, int nRank, int order, int iI,int iF, int delta){
        //order >0 for ascending sort
        int i,j,len=pdData.length,in,it,index;
        double dt,dMin;
        
        for(i=iI;i<=iF;i++){
            indexes[i]=i;
        }
        
        for(i=0;i<=nRank;i+=delta){
            index=iI+i;
            in=index;
            if(index>=len) continue;
            dMin=pdData[index];
            for(j=index+1;j<=iF;j++){
                dt=pdData[j];
                if(order*(dt-dMin)<0){
                    in=j;
                    dMin=dt;
                }
            }
            dt=pdData[in];
            pdData[in]=pdData[index];
            pdData[index]=dt;

            it=indexes[in];
            indexes[in]=indexes[index];
            indexes[index]=it;
        }
    }

    static public void PartialSort(ArrayList<Double> dvData, ArrayList<Integer> indexes, int nRank, int order){//order==1 to sort the array ascending, and order==-1 to sort descending 
        int i,j,len=dvData.size(),in,it,index;
        double dt,dMin;
        for(i=0;i<=nRank;i++){
            index=i;
            in=index;
            if(index>=len) continue;
            dMin=dvData.get(index);
            for(j=index+1;j<len;j++){
                dt=dvData.get(j);
                if(order*(dt-dMin)<0){
                    in=j;
                    dMin=dt;
                }
            }
            dt=dvData.get(in);
            dvData.set(in, dvData.get(index));
            dvData.set(index, dt);

            it=indexes.get(in);
            indexes.set(in, indexes.get(index));
            indexes.set(index, it);
        }
    }
    static public int getRankingIndex(ArrayList<Double> dvData0, int nRank, int order){//order==1 to sort the array ascending, and order==-1 to sort descending 
        ArrayList<Integer> indexes=new ArrayList();
        ArrayList<Double> dvData=CommonStatisticsMethods.copyDoubleArray(dvData0);
        int i,j,len=dvData.size(),in,it,index;
        for(i=0;i<len;i++){
            indexes.add(i);
        }
        double dt,dMin;
        for(i=0;i<=nRank;i++){
            index=i;
            in=index;
            if(index>=len) continue;
            dMin=dvData.get(index);
            for(j=index+1;j<len;j++){
                dt=dvData.get(j);
                if(order*(dt-dMin)<0){
                    in=j;
                    dMin=dt;
                }
            }
            dt=dvData.get(in);
            dvData.set(in, dvData.get(index));
            dvData.set(index, dt);

            it=indexes.get(in);
            indexes.set(in, indexes.get(index));
            indexes.set(index, it);
        }
        return indexes.get(nRank);
    }
    
    public static boolean isLocalExtrema(double[] pdY, int position0, int sign){//sign>0 for local maxima
        //end positions (0 or pdY.lengh-1) are regarded as a local extremum if the neighboring point is lower (for sign>0). 
        //a point in a flat line is not an extremum.
        double y0=pdY[position0],comp;
        boolean lFlat=false,rFlat=false;
        int l,r,len=pdY.length;
        if(position0>0){
            comp=sign*(y0-pdY[position0-1]);
            if(comp<0) return false;
            if(comp==0){
                l=getNextRisingPosition(pdY,position0,-sign,-1);
                if(l>0) 
                    return (pdY[l]==y0);
                else {//must be l==-1
                    comp=sign*(y0-pdY[0]);
                    if(comp<0) return false;
                    lFlat=true;
                }
            }
        }
        
        if(position0==len-1) return !lFlat;
        
        comp=sign*(y0-pdY[position0+1]);
        if(comp<0) return false;
        if(comp==0) {
            r=getNextRisingPosition(pdY,position0,-sign,1);
            if(r>0){
                return pdY[r]==y0;
            }else{
                comp=sign*(y0-pdY[len-1]);
                if(comp<0) return false;
                if(comp>0) 
                    return true;//should not be the case
                //comp==0
                return !lFlat;                
            }
        }
        //comp >0
        
        return true;
    }
    public static int getNextUnEqualPosition(double[] pdY, int position0, int delta){
        intRange ir=new intRange(0,pdY.length-1);
        double y0=pdY[position0];
        int position=position0+delta;
        if(!ir.contains(position)) return -1;
        while(pdY[position]==y0){
            position+=delta;
            if(!ir.contains(position)) return -1;
        }
        return position;
    }
    public static int getLargestPositionInRange(double[] pdY, ArrayList<Integer> positions, intRange ir, int sign){
        //return the smallest if sign<0
        int i,len=positions.size(),px,position;
        if(len==0) return -1;
        double dx=0,dt;
        px=-1;
        for(i=0;i<len;i++){
            position=positions.get(i);
            if(!ir.contains(position)) continue;
            if(px==-1){
                px=position;
                dx=pdY[px];
            }else{
                dt=pdY[position];
                if(sign*(dt-dx)>0){
                    px=position;
                    dx=dt;
                }
            }
        }
        return px;
    }
    public static ArrayList<intRange> getAbsMaximaSegmenter(double[] pdY, ArrayList<intRange> nv){
        int sign=1,i,len=pdY.length,len1=nv.size(),position,p0,p1;
        double[] pdAbs=getAbs(pdY);
        ArrayList<Integer> nvE;
        ArrayList<intRange> cvLx=new ArrayList();
        for(i=0;i<len1;i++){
            position=nv.get(i).getMin();
            if(position<0||position>=len) continue;
            nvE=getExtremaPositions(pdAbs,position,1);
            if(nvE.size()==1){
                p0=nvE.get(0);
                if(p0<0||p0>=len) continue;
                cvLx.add(new intRange(p0,p0+1));
            }else if(nvE.size()==2){
                p0=nvE.get(0);
                p1=nvE.get(1);
                if(pdAbs[p0]>=pdAbs[p1])
                    cvLx.add(new intRange(p0,p0+1));
                else
                    cvLx.add(new intRange(p1,p1+1));
            }
        }
        return cvLx;
    }
    public static ArrayList<Integer> getAbsMaximaPositions(double[] pdY, ArrayList<Integer> nv){
        int sign=1,i,len=pdY.length,len1=nv.size(),position,p0,p1;
        double[] pdAbs=getAbs(pdY);
        ArrayList<Integer> nvE,nvLx=new ArrayList();
        for(i=0;i<len1;i++){
            position=nv.get(i);
            if(position<0||position>=len) continue;
            nvE=getExtremaPositions(pdAbs,position,1);
            if(nvE.size()==1){
                p0=nvE.get(0);
                if(p0<0||p0>=len) continue;
                nvLx.add(p0);
            }else if(nvE.size()==2){
                p0=nvE.get(0);
                p1=nvE.get(1);
                if(pdAbs[p0]>=pdAbs[p1])
                    nvLx.add(p0);
                else
                    nvLx.add(p1);
            }
        }
        return nvLx;
    }
    public static ArrayList<Integer> getExtremaPositions(double[] pdY, int position0, int sign){//sign>0 for local maxima
        //returns position0 if it is a local extremum.
        //if position0 is not an extremum, it will find the extremum position according to the gradient.
        //if position0 is an extremum of the opposite sign, it will return the extrema on both sides.
        ArrayList<Integer> positions=new ArrayList();
        if(isLocalExtrema(pdY,position0,sign)) {
            positions.add(position0);
            return positions;
        }
        int l=getNextUnEqualPosition(pdY,position0,-1),r=getNextUnEqualPosition(pdY,position0,1),len=pdY.length;
        double dL,dR;
        if(l==-1) {
            r=getFirstExtremumIndex(pdY,position0,sign,1);//r will be -1 if now extremum at the direction
            positions.add(r);
        } else if(r==-1) {             
            l=getFirstExtremumIndex(pdY,position0,sign,-1);
            positions.add(l);
        } else {
            dL=pdY[l];
            dR=pdY[r];
            if(dL==dR){ //possible only when position0 is an extremum of the oppisite sign
                l=getFirstExtremumIndex(pdY,position0,sign,-1);
                if(l<0){
                    if(pdY[0]>pdY[1]) l=0;
                }
                r=getFirstExtremumIndex(pdY,position0,sign,1);
                if(r<0){
                    if(pdY[len-1]>pdY[len-2]) r=len-1;
                }
                positions.add(l);
                positions.add(r);
            }else if(sign*(dL-dR)>0){ 
                l=getFirstExtremumIndex(pdY,position0,sign,-1);
                if(l<0){
//                    if(pdY[0]>pdY[1]) l=0;
                    if(sign*(pdY[0]-pdY[1])>0) l=0;//11n17
                }
                positions.add(l);
            } else {
                r=getFirstExtremumIndex(pdY,position0,sign,1);
                if(r<0){
//                    if(pdY[len-1]>pdY[len-2]) r=len-1;
                    if(sign*(pdY[len-1]-pdY[len-2])>0) r=len-1;
                }
                positions.add(r);
            }
        }
        return positions;
    }

    public static void removeSpikes(double[] pdY, ArrayList<intRange> cvSpikes, int sign){//sign>0 indicates upward spikes
        int i,j,len=cvSpikes.size(),left0,right0,left,right,ix=0;
        double dl,dr,peak,peak0,dt,dx,dn;
        intRange ir;
        for(i=0;i<len;i++){
            ir=cvSpikes.get(i);
            left0=ir.getMin();
            right0=ir.getMax();
            dl=pdY[left0];
            peak0=dl;
            dx=dl;
            dn=dl;
            for(j=left0;j<=right0;j++){
                dt=pdY[j];
                if(sign*(dt-dx)>0){
                    dx=dt;
                    ix=j;
                }
                if(sign*(dt-dn)<0){
                    dn=dt;
                }
            }
            peak0=dx;
            left=CommonStatisticsMethods.getFirstExtremumIndex(pdY,left0,sign,-1);
            dl=pdY[left];
            right=CommonStatisticsMethods.getFirstExtremumIndex(pdY,right0,sign,1);
            dr=pdY[right];
            peak=CommonMethods.getLinearIntoplation(left, dl, right, dr, ix);
            for(j=left0;j<=right0;j++){
                dt=pdY[j];
                pdY[j]=CommonMethods.getLinearIntoplation(dn, dn, peak0, peak, dt);
            }
        }
    }
    public static double[] getRunningWindowQuantile(ArrayList<Double> dvY,int ws, int Rank, ArrayList<Integer> indexes){
        return getRunningWindowQuantile(CommonStatisticsMethods.getDoubleArray(dvY),ws,Rank,indexes);
    }
    public static double[] getRunningWindowQuantile(double[] pdY,int ws0, int rank0,ArrayList<Integer> indexes){
        if(indexes!=null) indexes.clear();//indexes of the numbers whose rankings are rank0
        double[] pdFiltedY=new double[pdY.length];
        int i,j,len=pdY.length,len1=2*ws0+1,index,size,i0,i1,anchor,anchor0=-1,rank=rank0;
//        double ratio=(double)rank0/(double)len1;
        int r1,r2,left0,right0,ws;
        double r,w1,w2,y1,y2;
        SortedDoubleArray sda=new SortedDoubleArray();
        pdFiltedY[0]=pdFiltedY[1];
        
        left0=-1;
        right0=-1;
        for(i=0;i<len;i++){
            ws=Math.min(i, ws0);
            ws=Math.min(ws,len-1-i);
            
            i0=i-ws-1;
            i1=i+ws;
//            if(i1<len) sda.addData(pdY[i1], i1);
//            if(i0>=0) {//13102
//                sda.removeData(pdY[i0], i0);
//            }
            for(j=right0+1;j<=i1;j++){
                sda.addData(pdY[j], j);
            }
            for(j=left0+1;j<=i0;j++){
                sda.removeData(pdY[j],j);
            }
            if(i0>0) left0=0;
            if(i1<len) right0=i1;
            size=sda.getDataSize();
            if(i>=0&&i<len){
                rank=rank0;
                if(size<len1){
                    r=CommonMethods.getLinearIntoplation(0, 0, len1-1, size-1, rank0);
                    r1=(int)r;
                    w1=1-(r-r1);
                    y1=sda.getData(r1);
                    
                    r2=r1+1;
                    if(r2<size){
                        w2=1-(r2-r);
                        y2=sda.getData(r2);
                    }else{
                        y2=0;
                        w2=0;
                    }                    
                    pdFiltedY[i]=(y1*w1+y2*w2)/(w1+w2);
                    continue;
                }
                pdFiltedY[i]=sda.getData(rank);
                if(indexes!=null){
                    anchor=sda.getIndex(rank);
                    if(anchor!=anchor0){
                        indexes.add(anchor);
                        anchor0=anchor;
                    }
                }
            }
        }
        return pdFiltedY;
    }
    public static double[] getRunningWindowQuantile(double[] pdY,int ws0, int rank0,int[] indexes){
        double[] pdFiltedY=new double[pdY.length];
        int i,j,len=pdY.length,len1=2*ws0+1,index,size,i0,i1,anchor,anchor0=-1,rank=rank0;
//        double ratio=(double)rank0/(double)len1;
        int r1,r2,left0,right0,ws;
        double r,w1,w2,y1,y2;
        SortedDoubleArray sda=new SortedDoubleArray();
        pdFiltedY[0]=pdFiltedY[1];
        
        left0=-1;
        right0=-1;
        for(i=0;i<len;i++){
            ws=Math.min(i, ws0);
            ws=Math.min(ws,len-1-i);
            
            i0=i-ws-1;
            i1=i+ws;
//            if(i1<len) sda.addData(pdY[i1], i1);
//            if(i0>=0) {//13102
//                sda.removeData(pdY[i0], i0);
//            }
            for(j=right0+1;j<=i1;j++){
                sda.addData(pdY[j], j);
            }
            for(j=left0+1;j<=i0;j++){
                sda.removeData(pdY[j],j);
            }
            if(i0>0) left0=0;
            if(i1<len) right0=i1;
            size=sda.getDataSize();
            
            rank=rank0;
            if(size<len1){
                r=CommonMethods.getLinearIntoplation(0, 0, len1-1, size-1, rank0);
                r1=(int)r;
                w1=1-(r-r1);
                rank=r1;
                if(w1<0.5&&r1<size-2) rank++;
            }
            pdFiltedY[i]=sda.getData(rank);
            indexes[i]=sda.getIndex(rank);
        }
        return pdFiltedY;
    }
    public static double[] getRunningWindowQuantile(double[] pdY,boolean[] pbSelected,int ws, int rank0,ArrayList<Integer> indexes){
        if(indexes!=null) indexes.clear();//indexes of the numbers whose rankings are rank0
        ArrayList<Integer> nvSelectedPositions=new ArrayList();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSelected, new intRange(0,pbSelected.length-1), nvSelectedPositions);
        double[] pdFiltedY=new double[pdY.length];
        int i,len=pdY.length,len1=2*ws+1,size,i0,i1,anchor,anchor0=-1,rank=rank0,numSelected=nvSelectedPositions.size();
        int r1,r2,j;
        double r,w1,w2,y1,y2,y;
        SortedDoubleArray sda=new SortedDoubleArray();
        pdFiltedY[0]=pdFiltedY[1];
        int p,p0=0,left,right;
        for(i=-ws;i<numSelected+ws;i++){
            i0=i-ws-1;
            i1=i+ws;
//            p=nvSelectedPositions.get(i);
            if(i0>=0){
                left=nvSelectedPositions.get(i0);
                sda.removeData(pdY[left], left);
            }else{
                left=0;
            }
            
            if(i1<numSelected){
                right=nvSelectedPositions.get(i1);
                sda.addData(pdY[right], right);
            }else{
                right=len-1;
            }
            
            size=sda.getDataSize();
            if(i>=0&&i<numSelected){
                rank=rank0;
                p=nvSelectedPositions.get(i);
                
                if(size<len1){
                    r=CommonMethods.getLinearIntoplation(0, 0, len1-1, size-1, rank0);
                    r1=(int)r;
                    w1=1-(r-r1);
                    y1=sda.getData(r1);
                    
                    r2=r1+1;
                    if(r2<size){
                        w2=1-(r2-r);
                        y2=sda.getData(r2);
                    }else{
                        y2=0;
                        w2=0;
                    }                    
//                    pdFiltedY[i]=(y1*w1+y2*w2)/(w1+w2);
                    if(w1>w2)
                        y=y1;
                    else
                        y=y2;
                    
                }else{
                    y=sda.getData(rank);
                    if(indexes!=null){
                        anchor=sda.getIndex(rank);
                        if(anchor!=anchor0){
                            indexes.add(anchor);
                            anchor0=anchor;
                        }
                    }
                }
                
                for(j=p0;j<=p;j++){
                    pdFiltedY[j]=y;
                }
                p0=p+1;
            }
        }
        return pdFiltedY;
    }
    public static int getSmallerElimentIndex(double[] pdY, int index1, int index2){
        if(pdY[index1]<=pdY[index2]) return index1;
        return index2;
    }
    public static int getSmallerElimentIndex(double[] pdY, int index1, int index2, int sign){
        if(sign*pdY[index1]<=sign*pdY[index2]) return index1;
        return index2;
    }
    public static int getTheOther(int Int, int Int1, int Int2){
        if(Int==Int1) return Int2;
        return Int1;
    }
    public static void getLocalExtremaPositions(double[] pdY, ArrayList<Integer> nvLn, ArrayList<Integer> nvLx, int[] pnLnPositions, int[] pnLxPositions){
        //index=pnLxPositions[i], index is the index of the local maximum that will cover the position i. 
        //index will be a negative number if position i is a local minimum, and the "index" is the index of the local minimum plus 1. addition of 1 is to avoid zero.
        //similar is true for pnLnPositions
        CommonStatisticsMethods.setElements(pnLxPositions,-1);
        CommonStatisticsMethods.setElements(pnLnPositions,-1);
        int i,index,len=pdY.length,j,shift=-1,nX=nvLx.size(),nN=nvLn.size();
        if(nvLx.get(0)>nvLn.get(0)) shift=0;
        if(nvLx.isEmpty()) CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        int index0=0;
        
        if(shift==-1){
            for(i=0;i<=nvLx.get(0);i++){
                pnLnPositions[i]=-1;
            }
            pnLxPositions[0]=0;
            index0=nvLn.get(0);
        }else{
            for(i=0;i<=nvLn.get(0);i++){
                pnLxPositions[i]=-1;
            }
            pnLnPositions[0]=0;
            index0=0;
        }
        for(i=0;i<nX;i++){
            index=nvLx.get(i);
            pnLnPositions[index]=-(i+1);
            for(j=index0+1;j<index;j++){
                pnLnPositions[j]=i+shift;
            }
            index0=index;
        }

        if(shift==-1)
            index0=0;
        else
            index0=nvLn.get(0);
        
        for(i=0;i<nN;i++){
            index=nvLn.get(i);
            pnLxPositions[index]=-(i+1);
            for(j=index0+1;j<index;j++){
                pnLxPositions[j]=i-(1+shift);
            }
            index0=index;
        }
        int lastMaximum=nvLx.get(nX-1),lastMinimum=nvLn.get(nN-1);
        len=pdY.length;
        if(lastMaximum>lastMinimum){
            for(i=lastMinimum+1;i<lastMaximum;i++){
                pnLxPositions[i]=nX-1;
            }
            for(i=lastMaximum;i<len;i++){
                pnLnPositions[i]=-nX;
                pnLxPositions[i]=nX-1;
            }
            pnLxPositions[lastMinimum]=-nN;
        }else{
            for(i=lastMaximum+1;i<lastMinimum;i++){
                pnLnPositions[i]=nN-1;
            }
            for(i=lastMinimum;i<len;i++){
                pnLxPositions[i]=-nN;
                pnLnPositions[i]=nN-1;
            }
            pnLnPositions[lastMaximum]=-(nX-1);
        }
    }
    public static double[] copyToDoubleArray(ArrayList<Double> dvT){        
        int i,len=dvT.size();
        double[] pdT=new double[len];
        for(i=0;i<len;i++){
            pdT[i]=dvT.get(i);
        }
        return pdT;
    }
    public static double[] copyToDoubleArray(int[] pnT){        
        int i,len=pnT.length;
        double[] pdT=new double[len];
        for(i=0;i<len;i++){
            pdT[i]=pnT[i];
        }
        return pdT;
    }
    public static int getExtremaIntervals(double[] pdX,double[] pdY,ArrayList<Double> dvX,ArrayList<Double> dvIntervals){                
        ArrayList<Integer> nvLn=new ArrayList(), nvLx=new ArrayList(),nv0=nvLx, nv1=nvLn;
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        if(nvLn.get(0)<nvLx.get(0)){
            nv0=nvLn;
            nv1=nvLx;
        }
        
        int l0=nv0.size(),l1=nv1.size(),len=Math.min(l0,l1),i,position0,position1;
        double delta;
        if(len<2) return -1;
        
        position0=nv0.get(0);
        dvX.clear();
        dvIntervals.clear();
        for(i=1;i<len-1;i++){
            position1=nv1.get(i);        
            delta=pdX[position1]-pdX[position0];
            dvIntervals.add(delta);
            dvX.add(pdX[position0]);
            position0=position1;
            
            position1=nv0.get(i+1);        
            delta=pdX[position1]-pdX[position0];
            dvIntervals.add(delta);
            dvX.add(pdX[position0]);
            position0=position1;
        }     
        return 1;
    }
    public static int lastPositionInRange(double[] pdX, int iI, double dInterval){
        int iX=iI, len=pdX.length;
        double delta=0,x0=pdX[iI];
        while(delta<=dInterval){
            iX++;
            if(iX>=len) return len-1;
            delta=pdX[iX]-x0;
        }
        return iX-1;
    }
    public static int[] getLocalExtrema(double[] pdY){
        ArrayList<Integer> nvLx=new ArrayList(), nvLn=new ArrayList(), nv0=nvLx,nv1=nvLn;
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        if(nvLx.get(0)>nvLn.get(0)){
            nv0=nvLn;
            nv1=nvLx;
        }
        int l0=nv0.size(),l1=nv1.size(),len=Math.min(l0, l1),i,position;
        int[] positions=new int[l0+l1];
        int index=0;
        for(i=0;i<len;i++){
            position=nv0.get(i);
            positions[index]=position;
            index++;
            
            position=nv1.get(i);
            positions[index]=position;
            index++;
        }
        if(l0>l1){
            position=nv0.get(len);
            positions[index]=position;
        }else if(l0<l1){
            position=nv1.get(len);
            positions[index]=position;
        }
        return positions;
    }
    public static int[] getLocalExtrema(float[] pfY){
        ArrayList<Integer> nvLx=new ArrayList(), nvLn=new ArrayList(), nv0=nvLx,nv1=nvLn;
        CommonMethods.LocalExtrema(pfY, nvLn, nvLx);
        if(nvLx.size()==0||nvLn.size()==0) return null;
        if(nvLx.get(0)>nvLn.get(0)){
            nv0=nvLn;
            nv1=nvLx;
        }
        int l0=nv0.size(),l1=nv1.size(),len=Math.min(l0, l1),i,position;
        int[] positions=new int[l0+l1];
        int index=0;
        for(i=0;i<len;i++){
            position=nv0.get(i);
            positions[index]=position;
            index++;
            
            position=nv1.get(i);
            positions[index]=position;
            index++;
        }
        if(l0>l1){
            position=nv0.get(len);
            positions[index]=position;
        }else if(l0<l1){
            position=nv1.get(len);
            positions[index]=position;
        }
        return positions;
    }
    public static void getRange(double[] pdY, int[] positions, int iI, int iF, int delta, DoubleRange dR){
        dR.resetRange();
        for(int i=iI;i<=iF;i++){
            dR.expandRange(pdY[positions[i]]);
        }
    }
    public static void getRange(double[] pdY, int iI, int iF, int delta, DoubleRange dR){
        dR.resetRange();
        for(int i=iI;i<=iF;i++){
            dR.expandRange(pdY[i]);
        }
    }
    public static intRange getTransitionRange(double[] pdX, double[] pdY, int position0, int position1, double dMaxInterval){
        int iI,iT,iX,iXR;
        double dt,dX;
        iI=position0;
        iT=CommonStatisticsMethods.lastPositionInRange(pdX, position0, dMaxInterval);
        if(iT>position1) return new intRange(position0,position1);
        dX=Math.abs(pdY[position0]-pdY[position1]);
        iX=iI;
        iXR=iT;
        while(iT<position1){
            iI++;
            iT=CommonStatisticsMethods.lastPositionInRange(pdX, iI, dMaxInterval);
            dt=Math.abs(pdY[iT]-pdY[iI]);
            if(dt>dX){
                 dX=dt;
                 iX=iI;
                 iXR=iT;
            }
        }
        return new intRange(iX,iXR);
    }
    public static double getMinAbsDelta(double[] pdX,int iI,int iF, int nDelta){
        double dx0,dx, delta,dm=Double.POSITIVE_INFINITY;
        int len=pdX.length;
        dx0=pdX[iI];
        for(int i=iI+1;i<=iF;i+=nDelta){
            if(i<0||i>=len) continue;
            dx=pdX[i];
            delta=Math.abs(dx-dx0);
            if(delta<dm) dm=delta;
            dx0=dx;
        }
        return dm;
    }
    public static double[] getGradient(double[] pdX, double[] pdY, int interval){
        int i,len=pdX.length,left,right;
        double[] pdG=new double[len];
        for(i=interval;i<len-interval;i++){
            left=i-interval;
            if(left<0) left=0;
            right=i+interval;
            if(right>len-1) right=len-1;
            if(pdX[i]>=0.1996){
                i=i;
            }
            pdG[i]=(pdY[right]-pdY[left])/(pdX[right]-pdX[left]);
        }
        return pdG;
    }
    public static double[] getGradient(double[] pdX, double[] pdY, int Range, int interval){
        int i,j,len=pdX.length,left,right;
        double[] pdG=new double[len];
        double dl,dr;
        for(i=Range;i<len-Range;i++){
            dl=0;
            for(j=i-Range;j<i;j+=interval){
                dl+=pdY[j];
            }
            dr=0;
            for(j=i+1;j<=i+Range;j+=interval){
                dr+=pdY[j];
            }
            pdG[i]=(dr-dl)/(0.5*(pdX[i+interval]-pdX[i-interval]));
        }
        return pdG;
    }
    public static double[] get2ndGradient(double[] pdX, double[] pdY, int interval){
        int i,len=pdX.length,left,right;
        double[] pdG=new double[len];
        for(i=interval;i<len-interval;i++){
            left=i-interval;
            if(left<0) left=0;
            right=i+interval;
            if(right>len-1) right=len-1;
            if(pdX[i]>=0.1996){
                i=i;
            }
            pdG[i]=2*(pdY[right]+pdY[left]-2.*pdY[i])/(pdX[right]-pdX[left]);
        }
        return pdG;
    }
    public static void normalizeByPeaks(double[] pdY){
        int[] pnLocalExtrema=getLocalExtrema(pdY);
        int sign=1,i,len=pdY.length,len1=pnLocalExtrema.length,o,index,j,ip0=pnLocalExtrema[0],ip1=pnLocalExtrema[1];
        if(pdY[pnLocalExtrema[0]]<pdY[pnLocalExtrema[1]]) sign=-1;
        double peak0=pdY[ip0],peak1=pdY[ip1];
        ip0=0;
        for(i=1;i<len1;i++){
            ip1=pnLocalExtrema[i];
            peak1=pdY[ip1];
            for(j=ip0;j<ip1;j++){
                pdY[j]=CommonMethods.getLinearIntoplation(peak0, sign, peak1, -sign, pdY[j]);
            }
            if(i<len1-1){
                ip0=ip1;
                peak0=peak1;
                sign*=-1;
            }else{
                i=i;
            }
        }
        for(j=ip1;j<len;j++){
             pdY[j]=CommonMethods.getLinearIntoplation(peak0, sign, peak1, -sign, pdY[j]);
        }
    }
    public static int copyDoubleInRangeToFloatArray(double[] pdX, double[] pdY,double xI, double xF, ArrayList<float[]> pfv){
        pfv.clear();
        int i,len=pdX.length,iI=-1,iF=len+1;
        double x;
        for(i=0;i<len;i++){
            x=pdX[i];
            if(x>=xI&&iI<0) iI=i;
            if(x<=xF) iF=i;
        }
        if(iI<0||iF>len) return -1;
        len=iF-iI+1;
        float[] pfX=new float[len],pfY=new float[len];
        for(i=iI;i<=iF;i++){
            pfX[i-iI]=(float)pdX[i];
            pfY[i-iI]=(float)pdY[i];
            
        }
        pfv.add(pfX);
        pfv.add(pfY);
        return 1;
    }
    public static float[] copyToFloatArray(float[] pfA){
        int i,len=pfA.length;
        float[] pfB=new float[len];
        for(i=0;i<len;i++){
            pfB[i]=pfA[i];
        }
        return pfB;
    }
    public static int reduceDataSize_ExtremaBased(float[] pfX, float[] pfY, int size, ArrayList<float[]> pfv){
        float[] pfXT=copyToFloatArray(pfX),pfYT=copyToFloatArray(pfY);
        int[] pnExtrema=CommonStatisticsMethods.getLocalExtrema(pfYT);
        if(pnExtrema==null) return -1;
        int i,len=pnExtrema.length;
        while(len>size){
            pfXT=new float[len];
            pfYT=new float[len];
            for(i=0;i<len;i++){
                pfXT[i]=pfX[pnExtrema[i]];
                pfYT[i]=pfY[pnExtrema[i]];
            }
            pnExtrema=CommonStatisticsMethods.getLocalExtrema(pfYT);
            len=pnExtrema.length;
        }
        pfv.clear();
        pfv.add(pfX);
        pfv.add(pfY);
        return 1;
    }
    public static int[] getPointNumberArray(int[] pnPositions,int len){
        int[] pnNumPoints=new int[len];
        int i,j,len1=pnPositions.length,position0,position,num;
        position=0;
        num=0;
        for(i=0;i<len1;i++){
            position=pnPositions[i];
            if(i==len1-1) position=len-1;
            for(j=position;j<position;j++){
                pnNumPoints[j]=num;
            }
            position0=position;
            num++;
        }
        return pnNumPoints;
    }
    
    public static ArrayList<Integer> getSegmentationPoints(double[] pdX, double[] pdY, int range, int interval){
        double[] pdG=getGradient(pdX,pdY,range,interval);
        int i,len=pdX.length,position;
        
        boolean[] pbSegPoints=new boolean[len];
        setElements(pbSegPoints,false);
        
        int[] pnPositions=getLocalExtrema(pdY);
        len=pnPositions.length;
        for(i=0;i<len;i++){
            pbSegPoints[pnPositions[i]]=true;
        }
        
        ArrayList<Integer> lnLn=new ArrayList(), lnLx=new ArrayList();
        CommonMethods.LocalExtrema(pdG, lnLn, lnLx);
        len=lnLn.size();
        for(i=0;i<len;i++){
            position=lnLn.get(i);
            if(pdG[position]>0){
                pbSegPoints[position]=true;
            }
        }
        
        len=lnLx.size();
        for(i=0;i<len;i++){
            position=lnLx.get(i);
            if(pdG[position]<0){
                pbSegPoints[position]=true;
            }
        }
        
        ArrayList<Integer> nvSegPoints=new ArrayList();
        len=pdX.length;
        for(i=0;i<len;i++){
            if(pbSegPoints[i]) nvSegPoints.add(i);
        }
        return nvSegPoints;
    }
    public static void setElements(double[][] pdData, double dV){
        int rows=pdData.length,cols=pdData[0].length,r,c;
        for(r=0;r<rows;r++){
            for(c=0;c<cols;c++){
                pdData[r][c]=dV;
            }
        } 
    }
    public static double[][]getDeltaYMatrix(double[] pdY,ArrayList<Integer> positions, double defaultValue, int ws){
        int i,j,len=positions.size(),left,right;
        double[][] pdDelta=new double[len][2*ws+1];
        setElements(pdDelta,defaultValue);
        for(i=0;i<len;i++){
            left=Math.max(0, i-ws);
            right=Math.min(len-1, i+ws);
            for(j=left;j<=right;j++){
                pdDelta[i][j-i+ws]=pdY[i]-pdY[j];
            }
        }
        return pdDelta;
    }
    static public int minAbsDeltaPosition(double[] pdY, int position, int left, int right){
        double dMin=Double.POSITIVE_INFINITY, dt,dy0=pdY[position];
        int np=left,i;
        for(i=left;i<=right;i++){
            if(i==position) continue;
            dt=Math.abs(dy0-pdY[i]);
            if(dt<dMin){
                np=i;
                dMin=dt;
            }
        }
        return np;
    }
    static public double[] getWeightedCenterLine(double[] pdYUp, double[] pdYDown){//only w=0.5 can ensure the center line stays in between the two lines.         
        int i,len=pdYUp.length;
        
        double[] pdYC=new double[len];
        double w=0.;
        pdYC[0]=0.5*(pdYUp[0]+pdYDown[0]);
        double dMin,dMax,dU,dD,dMax0,dMax1,delta;
        for(i=1;i<len;i++){
            dU=pdYUp[i]-pdYUp[i-1];
            dD=pdYDown[i]-pdYDown[i-1];
            if(Math.abs(dU)>Math.abs(dD)){
                dMin=dD;
                dMax=dU;
            }else{
                dMin=dU;
                dMax=dD;
            }
            dMax0=Math.signum(dMax)*Math.abs(dMin);
            dMax1=Math.signum(dMax)*(Math.abs(dMax)-Math.abs(dMin));
            delta=0.5*(dMax0+dMin)+w*dMax1;
            pdYC[i]=pdYC[i-1]+delta;
        }
        
//        double[] pdDU=getDeltaArray(pdYUp,null,2);
        return pdYC;
    }
    static public double[] getWeightedDeltaLine(double[] pdYUp, double[] pdYDown){
        double[] pdU=getDeltaArray(pdYUp,null,2);
        double[] pdD=getDeltaArray(pdYDown,null,2);
        
        int i,len=pdU.length;        
        
        double[] pdYC=new double[len];
        double w=0.;
        
        double dMin,dMax,dU,dD,dMax0,dMax1,dC;
        for(i=0;i<len;i++){
            dU=pdU[i];
            dD=pdD[i];
            if(Math.abs(dU)>Math.abs(dD)){
                dMin=dD;
                dMax=dU;
            }else{
                dMin=dU;
                dMax=dD;
            }
            dMax0=Math.signum(dMax)*Math.abs(dMin);
            dMax1=Math.signum(dMax)*(Math.abs(dMax)-Math.abs(dMin));
//            dC=0.5*(dMax0+dMin)+w*dMax1;
            dC=dMin;
            pdYC[i]=dC;
        }        
//        double[] pdDU=getDeltaArray(pdYUp,null,2);
        return pdYC;
    }
    static public double[] getTrimmedDoubleArray(double[] pdT,int iI, int iF, int delta){
        int i,len=(iF-iI)/delta+1;
        double[] pdTt=new double[len];
        int num=0;
        for(i=iI;i<=iF;i+=delta){
            pdTt[num]=pdT[i];
            num++;
        }
        return pdTt;
    }
    
    static public int getFirstExtremumIndex(double[] pdData, int index, int sign, int delta){//return index itself if this is a correct extremum, this is 
        //a newer version of getNextEtrema
        //sign>0 for local maxima and sign <0 for local minima
        //returns -1 if no extremum found utill end of pdData (0 or pdData.length-1)
        int len=pdData.length;
        if(index<0||index>=len) return -1;
        
        double x0,x,x1;        
        int iN=0,iX=len-1,i0,i,i1;
        if(delta==-1){
            iN=len-1;
            iX=0;
        }
        
        i0=index-delta;
        if(delta*(i0-iN)<0) i0+=delta;
        i=i0+delta;
        i1=i+delta;
        
        x0=pdData[i0];
        x=pdData[i];
        if(delta*(i1-iX)>0) return -1;
        x1=pdData[i1];
        
        while(sign*(x-x0)<=0||sign*(x-x1)<=0){
            if(x1!=x){
                i0=i;
                x0=x;
            }
            x=x1;
            i=i1;
            i1+=delta;
            if(delta*(i1-iX)>0) return -1;
            x1=pdData[i1];
        }
        i-=((i-i0)/(2*delta))*delta;//this is for the case of a few consecutive equal value points make extrema
        return i;
    }
    static public int getNextExtremumIndex(ArrayList<Double> dvData, int index, int sign, int delta){//return index itself if this is a correct extremum, this is 
        //a newer version of getNextEtrema
        //sign>0 for local maxima and sign <0 for local minima
        //returns -1 if no extremum found utill end of pdData (0 or pdData.length-1)
        int len=dvData.size();
        if(index<0||index>=len) return -1;
        
        double x0,x,x1;        
        int iN=0,iX=len-1,i0,i,i1;
        if(delta==-1){
            iN=len-1;
            iX=0;
        }
        
        i0=index-delta;
        if(delta*(i0-iN)<0) i0+=delta;
        i=i0+delta;
        i1=i+delta;
        
        x0=dvData.get(i0);
        x=dvData.get(i);
        if(delta*(i1-iX)>0) return -1;
        x1=dvData.get(i1);
        
        while(sign*(x-x0)<=0||sign*(x-x1)<=0){
            if(x1!=x){
                i0=i;
                x0=x;
            }
            x=x1;
            i=i1;
            i1+=delta;
            if(delta*(i1-iX)>0) return -1;
            x1=dvData.get(i1);
        }
        i-=((i-i0)/(2*delta))*delta;//this is for the case of a few consecutive equal value points make extrema
        return i;
    }
    static public int getCircularIndex(int index, int size){
        while(index<0){
            index+=size;
        }
        if(index>=size) index=index%size;
        return index;
    }
    static public int getCircularDist(int index1, int index2, int len){
        int dist;
        if(index1<=index2)
            dist=index2-index1;
        else
            dist=index2-(index1-len);
        return dist;
    }
    static public int getNextExtremumIndex_Circular(ArrayList<Double> dvData, int index, int sign, int delta){//return index itself if this is a correct extremum, this is 
        //a newer version of getNextEtrema
        //sign>0 for local maxima and sign <0 for local minima
        //returns -1 if no extremum found utill end of pdData (0 or pdData.length-1)
        int len=dvData.size(),indexo;
        
        double x0,x,x1;        
        int i0,i,i1,num=0;
        
        index=getCircularIndex(index,len);
        indexo=index;
        i0=getCircularIndex(index-delta,len);
        i=index;
        i1=getCircularIndex(i+delta,len);
        
        x0=dvData.get(i0);
        x=dvData.get(i);
        x1=dvData.get(i1);
        
        while(sign*(x-x0)<=0||sign*(x-x1)<=0){
            if(x1!=x){
                i0=i;
                x0=x;
            }
            x=x1;
            i=i1;
            num++;
            if(num>len) return -1;
            i1=getCircularIndex(i1+delta,len);
            x1=dvData.get(i1);
        }
//        i-=(getCircularDist(i0,i,len)/(2*delta))*delta;//this is for the case of a few consecutive equal value points make extrema
        i=getCircularIndex(i-(getCircularDist(i0,i,len)/(2*delta))*delta,len);
        return getCircularIndex(i,len);
    }
   static public int getNextRisingPosition(double[] pdData, int index, int sign, int delta){//returns -1 if no rising position is found before 0 or pdData.length-1
        //including the first position: index
        int len=pdData.length,position=-1;
        if(index<0||index>=len) return -1;
        double y0=pdData[index],y;
        index+=delta;
        while(index>=0&&index<len){
            y=pdData[index];
            if(sign*(y-y0)>0){
                position=index-delta;
                break;
            }
            y0=y;
            index+=delta;
        }
        return position;
    }
    static public PolynomialRegression getOptimalStartingRegression(double[] pdX, double[] pdY, boolean[] pbSelected, int index0, int minLen, int maxLen, int nOrder){
        if(pbSelected==null){
            pbSelected=getBooleanArray(pdX.length,true);
        }
        PolynomialRegression cPr=null,cPrt;
        int len,iF=pdX.length-1,i,start,end;
        maxLen=Math.min(maxLen, iF-index0+1);
        if(maxLen<minLen){
            len=maxLen;
            if(len<=0){
                len=1;
            }
            start=index0;
            end=start+len-1;
            cPr=new PolynomialRegression(pdX,pdY,pbSelected,nOrder,start,end,1);
        }
        for(len=minLen;len<=maxLen;len++){
            start=index0;
            end=start+len-1;
            cPrt=new PolynomialRegression(pdX,pdY,pbSelected,nOrder,start,end,1);
            if(PolynomialRegression.isBetterThan(cPrt, cPr)) cPr=cPrt;
        }
        return cPr;
    }
    static public PolynomialRegression getOptimalStartingRegression(double[] pdX, double[] pdY, boolean[] pbSelected, double[] pdSD, int index0, int minLen, int maxLen, int nOrder){
        if(pbSelected==null){
            pbSelected=getBooleanArray(pdX.length,true);
        }
        PolynomialRegression cPr=null,cPrt;
        int len,iF=pdX.length-1,i,start,end;
        double sig,maxSig=1;
        maxLen=Math.min(maxLen, iF-index0+1);
        if(maxLen<minLen){
            len=maxLen;
            if(len<=0){
                len=1;
            }
            start=index0;
            end=start+len-1;
            cPr=new PolynomialRegression(pdX,pdY,pbSelected,nOrder,start,end,1);
        }
        for(len=minLen;len<=maxLen;len++){
            start=index0;
            end=start+len-1;
            cPrt=new PolynomialRegression(pdX,pdY,pbSelected,pdSD,nOrder,start,end,1);
            if(PolynomialRegression.isBetterThan(cPrt, cPr)) cPr=cPrt;
        }
        return cPr;
    }
    static public boolean[] getBooleanArray(int len,boolean init){
        boolean[] pb=new boolean[len];
        setElements(pb,init);
        return pb;
    }
    static public PolynomialRegression getOptimalEndingRegression(double[] pdX, double[] pdY, boolean[] pbSelected, int index, int minLen, int maxLen, int nOrder){
        PolynomialRegression cPr=null,cPrt;
        int index0,len,iF=pdX.length-1,i,start, end;
        if(pbSelected==null){
            pbSelected=getBooleanArray(pdX.length,true);
        }
        double sig,maxSig=1;
        maxLen=Math.min(maxLen, index+1);
        if(maxLen<minLen){
            len=maxLen;
            start=index-(len-1);
            end=index;
            if(len<=0) {
                len=1;
            }
            cPr=new PolynomialRegression(pdX,pdY,pbSelected,nOrder,start,end,1);
        }
        for(len=minLen;len<=maxLen;len++){
            start=index-(len-1);
            end=index;
            cPrt=new PolynomialRegression(pdX,pdY,pbSelected,nOrder,start,end,1);
            if(PolynomialRegression.isBetterThan(cPrt, cPr)) cPr=cPrt;
        }
        return cPr;
    }
    static public PolynomialRegression getOptimalEndingRegression(double[] pdX, double[] pdY, boolean[] pbSelected, double[] pdSD, int index, int minLen, int maxLen, int nOrder){
        PolynomialRegression cPr=null,cPrt;
        int index0,len,iF=pdX.length-1,i,start, end;
        if(pbSelected==null){
            pbSelected=getBooleanArray(pdX.length,true);
        }
        double sig,maxSig=1;
        maxLen=Math.min(maxLen, index+1);
        if(maxLen<minLen){
            len=maxLen;
            start=index-(len-1);
            end=index;
            if(len<=0) {
                len=1;
            }
            cPr=new PolynomialRegression(pdX,pdY,pbSelected,nOrder,start,end,1);
        }
        for(len=minLen;len<=maxLen;len++){
            start=index-(len-1);
            end=index;
            cPrt=new PolynomialRegression(pdX,pdY,pbSelected,pdSD,nOrder,start,end,1);
            if(PolynomialRegression.isBetterThan(cPrt, cPr)) cPr=cPrt;
        }
        return cPr;
    }
    static public int getPositionInArrayWithMissingPoints(double[] pdData, double dT, double delta){
        int it,it1,len=pdData.length,iI=0,iF=len-1;;
        double dI=pdData[0],dF=pdData[len-1];
        if((dT-dI)*(dT-dF)>0) return -1;        
        it=(int)((dT-dI)/delta+0.5);
        if(it<0) it=0;
        if(it>=len) it=len-1;
        double d0=(dT-pdData[it])/delta,d1;
        if(Math.abs(d0)<0.5) return it;
        
        int sign=1;
        
        if(d0<0) {
            sign=-1;
            iF=0;
        }
                
        it1=it+sign;
        if(sign*(it1-iF)>0) return it;
        
        d1=(dT-pdData[it1])/delta;
        
        while(Math.abs(d1) >0.5){
            if(d0*d1<0){
                if(Math.abs(d1)<Math.abs(d0)) 
                    return it1;
                else
                    return it;
            }
            it1=it+sign;
            if(sign*(it1-iF)>0) return it; 
            d0=d1;
            d1=(dT-pdData[it1])/delta;
            it=it1;
        }
        return it1;
    }
    
    public static boolean regularDoubleArray(double[] pdData){
        int i,len=pdData.length;
        double dt;
        for(i=0;i<len;i++){
            dt=pdData[i];           
            if(Double.isInfinite(dt) ||Double.isNaN(dt)) return false;
        }
        return true;
    }
    public static int markHighClusterPositions(OneDKMeans cKM, boolean[] pbt, int sign){
        if(cKM==null) return -1;
        int i,len=pbt.length;
        CommonStatisticsMethods.setElements(pbt, true);
        int[] pnIndexes=cKM.getClusterIndexes();
        if(pnIndexes.length!=pbt.length) return -1;
        int index=cKM.getLowestClusterIndex(sign);
        for(i=0;i<len;i++){
            if(pnIndexes[i]==index) pbt[i]=false;
        }
        return 1;
    }
    public static int getPositionsWithinRange(ArrayList<Integer> nvPositions, boolean[] pbSelection, int index, int delta, intRange ir, ArrayList<Integer> indexes){
        int iF=nvPositions.size()-1;
        if(delta<0) iF=0;
        if(indexes==null) return -1;
        indexes.clear();
        while(ir.contains(nvPositions.get(index))){
            if(pbSelection[index]) indexes.add(index);
            if(index==iF) break;
            index+=delta;
        }
        return 1;
    }
    public static int getSelectedPositionsWithinRange(boolean[] pbSelection, int position0, int delta, intRange ir, ArrayList<Integer> positions){
        //position0 should be within the range: ir
        int position=position0,iF=pbSelection.length-1;
        if(positions==null) return -1;
        positions.clear();
        while(ir.contains(position)){
            if(pbSelection[position]) positions.add(position);
            if(position<0||position>iF) break;
            position+=delta;
        }
        return 1;
    }
    public static int getSelectedPositionsWithinRange(boolean[] pbSelection, intRange ir, ArrayList<Integer> positions){
        //position0 should be within the range: ir
        if(positions==null) return -1;
        int len=pbSelection.length;
        positions.clear();
        int position=ir.getMin();
        while(ir.contains(position)){
            if(pbSelection[position]) positions.add(position);
            if(position<0||position>=len) break;
            position++;
        }
        return 1;
    }
    public static int getNonZeroPositionsWithinRange(int[] pnSelection, int position0, int delta, intRange ir, ArrayList<Integer> positions){
        //position0 should be within the range: ir
        int position=position0,iF=pnSelection.length-1;
        if(positions==null) return -1;
        positions.clear();
        while(ir.contains(position)){
            if(pnSelection[position]!=0) positions.add(position);
            if(position<0||position>iF) break;
            position+=delta;
        }
        return 1;
    }
    public static int markSelections(boolean[] pbSelections, ArrayList<Integer> positions){
        if(pbSelections==null) return -1;
        if(positions==null) return -1;
        setElements(pbSelections,false);
        int iF=pbSelections.length-1,i,position,len=positions.size();
        for(i=0;i<len;i++){
            position=positions.get(i);
            if(position<0||position>iF) continue;
            pbSelections[position]=true;
        }
        return 1;
    }
    public static double getMaxAbsElement(ArrayList<Double> dvData){
        double dt,dx=Math.abs(dvData.get(0));
        int i,len=dvData.size(),ix=0;
        for(i=1;i<len;i++){
            dt=Math.abs(dvData.get(i));
            if(dt>dx) {
                ix=i;
                dx=dt;
            }
        }
        return dvData.get(ix);
    }
    public static double getMaxAbsElement(double dt1, double dt2){
        if(Math.abs(dt1)>Math.abs(dt2)) return dt1;
        return dt2;
    }
    public static double getMinAbsElement(double dt1, double dt2){
        if(Math.abs(dt1)<Math.abs(dt2)) return dt1;
        return dt2;
    }
    public static double getMinAbsElement(ArrayList<Double> dvData){
        double dt,dn=Math.abs(dvData.get(0));
        int i,len=dvData.size(),ix=0;
        for(i=1;i<len;i++){
            dt=Math.abs(dvData.get(i));
            if(dt<dn) {
                ix=i;
                dn=dt;
            }
        }
        return dvData.get(ix);
    }
    static public boolean isAbsMinimum(double[] pdData, int position){
        int len=pdData.length;
        if(position<0||position>=len) return false;
        double x0,x,x1;
        x=Math.abs(pdData[position]);
        if(position>0)
            x0=pdData[position-1];
        else
            x0=x+1;
        if(x>=x0) return false;
        if(position==len-1) return true;
        x1=Math.abs(pdData[position+1]);
        if(x>=x1) return false;
        return true;
    }
    static public boolean isAbsMaximum(double[] pdData, int position){
        int len=pdData.length;
        if(position<0||position>=len) return false;
        double x0,x,x1;
        x=Math.abs(pdData[position]);
        if(position>0)
            x0=pdData[position-1];
        else
            x0=x-1;
        if(x<=x0) return false;
        if(position==len-1) return true;
        x1=Math.abs(pdData[position+1]);
        if(x<=x1) return false;
        return true;
    }
    
    static public int getMaxAbsPosition(double[] pdData, int position){
        if(isAbsMaximum(pdData,position)) return position;
        int len=pdData.length,iN=0,iX=len-1,l,r;
        if(position<0||position>=len) return -1;
        double x=Math.abs(pdData[position]);
        int positionx=position;
        if(position==0){
            l=0;
        } else{
            if(Math.abs(pdData[position-1])>=x)
                l=getMaxAbsPosition(pdData,position,-1);
            else
                l=-1;
        }
        
        if(position==len-1){
            r=-1;
        } else {
            if(Math.abs(pdData[position+1])>=x)
                r=getMaxAbsPosition(pdData,position,1);
            else
                r=-1;
        }
        
        if(l==-1) 
            positionx=r;
        else if(r==-1)
            positionx=l;
        else {
            if(position-l>r-position)
                positionx=r;
            else if(position-l<r-position)
                positionx=1;
            else{
                if(Math.abs(pdData[l])>Math.abs(pdData[r])) 
                    positionx=l;
                else
                    positionx=r;
            }
        }
        if(positionx<0){
            position=position;
        }
        return positionx;
    }
    
    static public int getMaxPosition(double[] pdData, int position){
        if(isAbsMaximum(pdData,position)) return position;
        int len=pdData.length,iN=0,iX=len-1,l,r;
        if(position<0||position>=len) return -1;
        double x=Math.abs(pdData[position]);
        int positionx=position;
        if(position==0){
            l=0;
        } else{
            if(Math.abs(pdData[position-1])>=x)
                l=getMaxAbsPosition(pdData,position,-1);
            else
                l=-1;
        }
        
        if(position==len-1){
            r=-1;
        } else {
            if(Math.abs(pdData[position+1])>=x)
                r=getMaxAbsPosition(pdData,position,1);
            else
                r=-1;
        }
        
        if(l==-1) 
            positionx=r;
        else if(r==-1)
            positionx=l;
        else {
            if(position-l>r-position)
                positionx=r;
            else if(position-l<r-position)
                positionx=1;
            else{
                if(Math.abs(pdData[l])>Math.abs(pdData[r])) 
                    positionx=l;
                else
                    positionx=r;
            }
        }
        if(positionx<0){
            position=position;
        }
        return positionx;
    }
    
    static public int getMaxAbsPosition(double[] pdData, int position, int delta){//return index itself if this is a correct extremum, this is 
        //a newer version of getNextEtrema
        int len=pdData.length;
        if(position<0||position>=len) return -1;
        if(isAbsMaximum(pdData,position)) return position;
        
        double x0,x,x1;        
        int iN=0,iX=len-1,i0,i,i1;
        if(delta==-1){
            iN=len-1;
            iX=0;
        }
        
        if(position==iX){
            if(Math.abs(pdData[position])>Math.abs(pdData[position-delta]))
                return position;
            else
                return -1;
        }
        
        i0=position-delta;
        if(delta*(i0-iN)<0) 
            i0=position;//i0==position;
        i=i0+delta;
        if(delta*(i-iX)>0) {
            if(Math.abs(pdData[i0])>Math.abs(pdData[i0-delta]))
                return i0;
            else
                return -1;
        }
        i1=i+delta;
        if(delta*(i1-iX)>0) {
            if(Math.abs(pdData[i])>Math.abs(pdData[i-delta]))
                return i;
            else
                return -1;
        }
        
        x0=Math.abs(pdData[i0]);
        x=Math.abs(pdData[i]);
        x1=Math.abs(pdData[i1]);
        
        while(x<=x0||x<=x1){
            if(x1!=x){
                i0=i;
                x0=x;
            }
            x=x1;
            i=i1;
            i1+=delta;
            if(delta*(i1-iX)>0) {
                if(Math.abs(pdData[i])>Math.abs(pdData[i0]))
                    return i;
                else
                    return -1;
            }
            x1=Math.abs(pdData[i1]);
        }
        i-=(i-i0)/2;//this is for the case of a few consecutive equal value points make extrema
        return i;
    }
    public static double getPValue_TTest(int df,double t){
/*        if(tTestTable==null){
            tTestTable=new TTestTable();
        }
        return tTestTable.getPValue_TTest(df, Math.abs(t));*/
        TDistributionImpl tdist=new TDistributionImpl(df);
        double p=1.1;
        try{
            p=1-tdist.cumulativeProbability(t);
        }
        catch (MathException e){
            IJ.error("MathException in method extendsTTable");
        }
        return p;
    }
    public static double getPValue_ZTest(double z, double mean, double sd){
        double p=GaussianDistribution.Phi(z,mean, sd);
        if(p>0.5) p=1-p;
        return p;
    }
    public static double getMean(ArrayList<Double> dv){
        return getMean(dv,0,dv.size()-1,1);
    }
    public static double getMean(ArrayList<Double> dv,int iI, int iF, int delta){
        int i,num=0;
        double mean=0;
        for(i=iI;i<=iF;i+=delta){
            mean+=dv.get(i);
            num++;
        }
        return mean/num;
    }
    public static double getMean(double[] pdt,int iI, int iF, int delta){
        int i,num=0;
        double mean=0;
        for(i=iI;i<=iF;i+=delta){
            mean+=pdt[i];
            num++;
        }
        return mean/num;
    }
    public static ArrayList<Double> copyDoubleArrayList(ArrayList<Double> dvT,double shift){
        ArrayList<Double> dv=new ArrayList();
        for(int i=0;i<dvT.size();i++){
            dv.add(dvT.get(i)+shift);
        }
        return dv;
    }
    public static int trimDoubleArrayList(ArrayList<Double> dv, int targetSize, boolean trimEnd){
        if(dv==null) return-1;
        int nSize=dv.size(),i,position,num=nSize-targetSize;
        if(trimEnd) 
            position=targetSize;
        else
            position=0;
        for(i=0;i<num;i++){
            dv.remove(position);
        }
        return 1;
    }
    public static OneDKMeans buildAbsKMeans(double[] pdY, int K){
        int i,len=pdY.length;
        double[] pdAbs=new double[len];
        int[] indexes=new int[len],pnClusterIndexes=new int[len];
        for(i=0;i<len;i++){
            pdAbs[i]=Math.abs(pdY[i]);
            indexes[i]=i;
            pnClusterIndexes[i]=0;
        }
        if(len<K) return null;
        QuickSort.quicksort(pdAbs, indexes);
        for(i=1;i<K;i++){
            pnClusterIndexes[indexes[len-i]]=K-i;
        }
        return new OneDKMeans(pdAbs,K,pnClusterIndexes);
    }
    public static int getLargeAbsPoints(double[] pdY,int positionI,int positionF,int delta,double cutoff, ArrayList<Integer> highAbsPositions){//crossingPositions 
        //stores positive and negative elements. the absolute value of the elements indicates the position, and the sign indicates 
        //the crossing direction.
        highAbsPositions.clear();
        int iF=pdY.length-1;
        double y,ay;
        for(int position=positionI;position<=positionF;position++){
            if(position<0||position>iF) continue;
            y=pdY[position];
            ay=Math.abs(y);
            if(ay>cutoff) highAbsPositions.add(position);            
        }
        return 1;
    }
    public static void getPositions(int iI, int iF, int delta, ArrayList<Integer> nv){
        nv.clear();
        for(int i=iI;i<=iF;i+=delta){
            nv.add(i);
        }
    }
    public static void ShiftIntArray(ArrayList<Integer> nv, int shift){
        int i,len=nv.size();
        for(i=0;i<len;i++){
            nv.set(i, nv.get(i)+shift);
        }
    }
    public static double[] getAbs(double[] pdData){
        int i,len=pdData.length;
        double[] pdDataA=new double[len];
        for(i=0;i<len;i++){
            pdDataA[i]=Math.abs(pdData[i]);
        }
        return pdDataA;
    }
    public static int getNextLargeAbsPosition(double[] pdData,int position, int delta,double cutoff){
        int len=pdData.length;
        position+=delta;
        if(position<0||position>=len) return -1;
        double dA=Math.abs(pdData[position]);
        while(dA<cutoff){
            position+=delta;
            if(position<0||position>=len) return -1;
            dA=Math.abs(pdData[position]);
        }
        return position;
    }
    public static int getNumOfZero(int[] pn){
        int i,len=pn.length,num=0;
        for(i=0;i<len;i++){
            if(pn[i]!=0) continue;
            num++;
        }
        return num;
    }
    public static int getFirstPositionWithDifferentElement(int[] pn,int position0,int delta,int it){
        int position=position0,iF=pn.length-1;
        if(position0<0||position0>iF) return -1;
        if(delta<0) iF=0;
        while(pn[position]==it){
            position+=delta;
            if(delta*(position-iF)>0) return -1;
        }
        return position;
    }
    public static int getFirstPositionWithTheElement(int[] pn,int position0,int delta,int it){
        int position=position0,iF=pn.length-1;
        if(position0<0||position0>iF) return -1;
        if(delta<0) iF=0;
        while(pn[position]!=it){
            position+=delta;
            if(delta*(position-iF)>0) return -1;
        }
        return position;
    }
    public static double[] makeLine(double[] pdX, double[] pdY, ArrayList<Integer> positions){
        int i,p0,p,len=pdX.length,len1=positions.size();
        double[] pdLineY=new double[len];
        p0=0;
        for(i=0;i<len1;i++){
            p=positions.get(i);
            fillLine(pdX,pdY,pdLineY,p0,p);
            p0=p;
        }
        fillLine(pdX,pdY,pdLineY,p0,len-1);
        return pdLineY;
    }
    
    public static void fillLine(double[] pdX, double[] pdY,double[] pdLineY, int p1, int p2){
        double x1=pdX[p1],x2=pdX[p2],y1=pdY[p1],y2=pdY[p2];
        for(int p=p1;p<=p2;p++){
            pdLineY[p]=CommonMethods.getLinearIntoplation(x1, y1, x2, y2, pdX[p]);
        }
    }
    public static boolean[] getDeltaSelection_AND(boolean[] pbSelected, int[] pnRisingIntervals){
        int len=pnRisingIntervals.length,l,r;
        boolean[] pbd=new boolean[len];
        setElements(pbd,false);
        for(l=0;l<len;l++){
            r=l+pnRisingIntervals[l];
            if(r>=pbSelected.length){
                r=r;
            }
            if(pbSelected[l]&&pbSelected[r]) pbd[l]=true;
        }
        return pbd;
    }
    public static boolean[] getDeltaSelection_OR(boolean[] pbSelected, int[] pnRisingIntervals){
        int len=pnRisingIntervals.length,l,r;
        boolean[] pbd=new boolean[len];
        setElements(pbd,false);
        for(l=0;l<len;l++){
            r=l+pnRisingIntervals[l];
            if(pbSelected[l]||pbSelected[r]) pbd[l]=true;
        }
        return pbd;
    }
    public static boolean[] getSelectedRisingNeighbors(boolean[] pbSelected, int[] pnRisingIntervals){
        int len=pbSelected.length,i,l,r;
        boolean[] pbd=new boolean[len];
        setElements(pbd,false);
        for(i=0;i<len;i++){
            if(!pbSelected[i]) continue;
            l=i+getRisingInterval(pnRisingIntervals,i,-1);
            r=i+getRisingInterval(pnRisingIntervals,i,1);
            pbd[l]=true;
            pbd[i]=true;
            pbd[r]=true;
        }
        return pbd;
    }
    public static int interpolate(double[] pdX, double[] pdY,boolean[] pbComputed){
        int p0=getFirstSelectedPosition(pbComputed,true,0,1),p1=getFirstSelectedPosition(pbComputed,true,p0+1,1),iF=pdX.length-1;
        if(p0<0||p1<0) return -1;
 /*       if(p0>0){//should not extrapolate 12o05
            pdY[0]=CommonMethods.interpolation(pdX[p0], pdY[p0], pdX[p1], pdY[p1], pdX[0]);
            interpolate(pdX,pdY,0,p0);
        }        */
        while(p1>0){
            interpolate(pdX,pdY,p0,p1);
            p0=p1;
            p1=getFirstSelectedPosition(pbComputed,true,p1+1,1);
        }/*
        pdY[iF]=CommonMethods.interpolation(pdX[p0-1], pdY[p0-1], pdX[p0], pdY[p0], pdX[iF]);
        interpolate(pdX,pdY,p0,iF);*/
        return 1;
    }
    public static int interpolate(double[] pdX,double[] pdY, int p0, int p1){
        double x0=pdX[p0],x1=pdX[p1],y0=pdY[p0],y1=pdY[p1];
        for(int p=p0+1;p<p1;p++){
            pdY[p]=CommonMethods.interpolation(x0, y0, x1, y1, pdX[p]);
        }
        return 1;
    }
    public static int getFirstSelectedPosition(boolean[] pb, boolean selection, int position, int delta){
        intRange ir=new intRange(0,pb.length-1);
        if(!ir.contains(position)) return -1;
        while(pb[position]!=selection){
            position+=delta;
            if(!ir.contains(position)) return -1;            
        }
        return position;
    }
    public static int getFirstSelectedPosition(boolean[] pb1, boolean[] pb2, boolean selection1, boolean selection2, int position, int delta){
        intRange ir=new intRange(0,pb1.length-1);
        if(!ir.contains(position)) return -1;
        while(pb1[position]!=selection1||pb2[position]!=selection2){
            position+=delta;
            if(!ir.contains(position)) return -1;            
        }
        return position;
    }
    public static int setElements(intRange pcRanges[], intRange ir){
        if(pcRanges==null) return -1;
        for(int i=0;i<pcRanges.length;i++){
            pcRanges[i]=ir;
        }
        return 1;
    }
    public static int getRangesWithinRange(intRange[] pcRanges, ArrayList<intRange> irs, ArrayList<Integer> Positions, int iI, int iF){
        if(pcRanges==null||irs==null) return -1;
        irs.clear();
        Positions.clear();
        intRange ir;
        for(int i=iI;i<=iF;i++){
            ir=pcRanges[i];
            if(ir==null) continue;
            irs.add(ir);
            Positions.add(i);
        }
        return 1;
    }
    public static boolean equalContents(ArrayList<Integer> nv1, ArrayList<Integer> nv2){
        int i, len=nv1.size(),j,n1;
        if(len!=nv2.size()) return false;
        ArrayList<Integer> nv3=CommonStatisticsMethods.copyIntArray(nv2);
        boolean match;
        for(i=0;i<len;i++){
            n1=nv1.get(i);
            match=false;
            for(j=0;j<nv3.size();j++){
                if(n1==nv3.get(j)){
                    match=true;
                    nv3.remove(j);
                    break;
                }
            }
            if(!match) return false;
        }
        return true;
    }
    public static int copyArray(ArrayList<Double> dvData, double[] pdData){
        int i,len=dvData.size();
        if(len!=pdData.length) return -1;
        for(i=0;i<len;i++){
            pdData[i]=dvData.get(i);
        }
        return 1;
    }
    public static OneDKMeans OneDKMeans_LargestAttenuated(String title, ArrayList<Double> dvData, int K){
        double[] pdData=CommonStatisticsMethods.copyToDoubleArray(dvData);
        int i,len=pdData.length;
        if(pdData.length<2) return null;
        if(pdData.length<3) return new OneDKMeans(pdData,K);
        int[] indexes=new int[len], pnClusterIndexes=new int[len];
        
        for(i=0;i<len;i++){
            indexes[i]=i;
            pnClusterIndexes[i]=0;
        }
        PartialSort(pdData,indexes,2, -1,0,len-1,1);
        
        double y0=pdData[0],y1=pdData[1],y2=pdData[2];
        double delta=y1-y2,y01=Math.min(y0, y1+0.5*delta);
        if(y01<0.5*y0) y01=0.5*y0;
        
        copyArray(dvData,pdData);
        int index=indexes[0];
        
        pdData[index]=y01;
        pnClusterIndexes[index]=1;
        
        OneDKMeans cKM=new OneDKMeans(title+" -attenuated",pdData,K,pnClusterIndexes);
        return cKM;
    }
    public static boolean containsString(ArrayList<String> strings, String string){
        return findStringPosition(strings,string)>=0;
    }
    public static int findStringPosition(ArrayList<String> strings, String string){
        int i,len=strings.size();
        for(i=0;i<len;i++){
            if(strings.get(i).contains(string)) return i;
        }
        return -1;
    }
    public static int getMaxLength(ArrayList<String> strings){
        if(strings==null) return -1;
        int i,len,maxLen=0;
        for(i=0;i<strings.size();i++){
            len=strings.get(i).length();
            if(len>maxLen) maxLen=len;
        }
        return maxLen;
    }
    public static int getFirstIntegerPosition(ArrayList<Integer> nv, int iT){
        int i,len=nv.size();
        for(i=0;i<len;i++){
            if(nv.get(i)==iT) return i;
        }
        return -1;
    }
    public static int getFirstStringPosition(ArrayList<String> sv, String st){
        int i,len=sv.size();
        for(i=0;i<len;i++){
            if(sv.get(i).contentEquals(st)) return i;
        }
        return -1;
    }
    public static int getSmallestLargerElement(ArrayList<Integer> nv, int nt0){
        int nn=Integer.MAX_VALUE,nt;
        for(int i=0;i<nv.size();i++){
            nt=nv.get(i);
            if(nt>nt0&&nt<nn){
                nn=nt;
            }
        }
        return nn;
    }
    public static int getLargestSmallerElement(ArrayList<Integer> nv, int nt0){
        int nx=Integer.MIN_VALUE,nt;
        for(int i=0;i<nv.size();i++){
            nt=nv.get(i);
            if(nt<nt0&&nt>nx){
                nx=nt;
            }
        }
        return nx;
    }
    public static ArrayList<Integer> getArrayIntersection(ArrayList<Integer> nv1, ArrayList<Integer> nv2){
        ArrayList<Integer> nv=new ArrayList();
        int len1=nv1.size(),len2=nv2.size(),i,j,n1,n2;
        for(i=0;i<len1;i++){
            n1=nv1.get(i);
            for(j=0;j<len2;j++){
                if(n1==nv2.get(j)){
                    nv.add(n1);
                    break;
                }
            }
        }
        return nv;
    }
    public static int copyArray(double[] pd1, double[] pd2, ArrayList<Integer> nv){
        int index,len=Math.min(pd1.length, pd2.length);
        for(int i=0;i<nv.size();i++){
            index=nv.get(i);
            if(index>=len) continue;
            pd2[index]=pd1[index];
        }
        return 1;
    }
    public static int copyArray(double[] pd1, double[] pd2, boolean[] pbSelected){
        int i,len=Math.min(pd1.length, pd2.length);
        len=Math.min(len, pbSelected.length);
        for(i=0;i<len;i++){
            if(!pbSelected[i]) continue;
            pd2[i]=pd1[i];
        }
        return 1;
    }
    public static int copyArray(double[] pd1, double[] pd2, ArrayList<Integer> nv1,ArrayList<Integer> nv2){
        int index1,index2,len=Math.min(pd1.length, pd2.length);
        for(int i=0;i<nv1.size();i++){
            index1=nv1.get(i);
            index2=nv2.get(i);
            if(index1>=len||index2>=len) continue;
            pd2[index2]=pd1[index1];
        }
        return 1;
    }
    public static ArrayList<intRange> getCommonIntRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        ArrayList<intRange> irs=new ArrayList();
        int i,j,len1=irs1.size(),len2=irs2.size();
        intRange ir1,ir2,ir;
        for(i=0;i<len1;i++){
            ir1=irs1.get(i);
            for(j=0;j<len2;j++){
                ir2=irs2.get(j);
                if(ir1.equivalent(ir1)){
                    irs.add(ir1);
                }
            }
        }
        removeRedundancy(irs);
        return irs;
    }
    public static ArrayList<intRange> getCommonIntRanges0(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        ArrayList<intRange> irs=new ArrayList();
        int i,j,len1=irs1.size(),len2=irs2.size();
        intRange ir1,ir2,ir;
        for(i=0;i<len1;i++){
            ir1=irs1.get(i);
            for(j=0;j<len2;j++){
                ir2=irs2.get(j);
                ir=ir1.overlappedRange(ir2);
                if(ir.getRange()>1){
                    irs.add(ir);
                }
            }
        }
        removeRedundancy(irs);
        return irs;
    }
    public static void removeRedundancy(ArrayList<intRange> cvRanges){
        int i,j,len=cvRanges.size();
        int[] pnt=new int[len];
//        setElements(pnt,-1);
        intRange ir,ir1,irt;
        for(i=0;i<len;i++){
            if(pnt[i]==-1) continue;
            ir=cvRanges.get(i);
            for(j=i+1;j<len;j++){
                ir1=cvRanges.get(j);
                if(ir.contains(ir1)||ir1.contains(ir)){
                    ir.expandRange(ir1);
                    pnt[j]=-1;
                }                    
            }
        }
        for(j=len-1;j>=0;j--){
            if(pnt[j]==-1) cvRanges.remove(j);
        }
    }
    public static IntPair[] getRisingNeighbors(int[] pnRisingIntervals, int nMaxRisingInterval,int nDataSize){
        IntPair[] pcNeighbors=new IntPair[nDataSize];
        int i,l,r,len=pnRisingIntervals.length;
        for(i=0;i<nDataSize;i++){
            l=i+getRisingInterval(pnRisingIntervals,i,-1);
            r=i+getRisingInterval(pnRisingIntervals,i,1);
            pcNeighbors[i]=new IntPair(l,r);
        }
        return pcNeighbors;
    }
    public static ArrayList<double[]> getSegmentLines(double[] pdX, double[] pdY, ArrayList<intRange> irs){
        int i,l,r,len;
        double[] pdXT,pdYT;
        len=Math.min(pdX.length, pdY.length);
        ArrayList<double[]> pdvLines=new ArrayList();
        intRange ir;
        l=0;
        for(i=0;i<irs.size();i++){
            ir=irs.get(i);
            r=ir.getMin();
            if(r>=len) continue;
            if(r<l) {
                l=ir.getMax();
                continue;
            }
            if(l<r){
                pdXT=copyToDoubleArray(pdX,l,r,1);
                pdYT=copyToDoubleArray(pdY,l,r,1);
            }else{
                pdXT=new double[2];
                pdYT=new double[2];
                pdXT[0]=pdX[l];
                pdXT[1]=pdX[l];
                pdYT[0]=pdY[l];
                pdYT[1]=pdY[l];
            }
            pdvLines.add(pdXT);
            pdvLines.add(pdYT);
            l=ir.getMax();
        }
        if(l<len){
            r=len-1;
            pdXT=copyToDoubleArray(pdX,l,r,1);
            pdYT=copyToDoubleArray(pdY,l,r,1);
            pdvLines.add(pdXT);
            pdvLines.add(pdYT);
        }
        return pdvLines;
    }
    public static double[] copyToDoubleArray(double[] pdData, int iI, int iF, int delta){
        int i,len=iF-iI+1/delta,position=iI;
        double[] pdT=new double[len];
        for(i=0;i<len;i++){
            pdT[i]=pdData[position];
            position+=delta;
        }
        return pdT;
    }
    public static ArrayList<DoubleRange> getDoubleRanges(double[] pdData, ArrayList<intRange> cvRanges){
        ArrayList<DoubleRange> cvDr=new ArrayList();
        int i,len=cvRanges.size(),l,r,len1=pdData.length;
        intRange ir;
        for(i=0;i<len;i++){
            ir=cvRanges.get(i);
            l=ir.getMin();
            r=ir.getMax();
            if(l<0||l>=len1) continue;
            if(r<0||r>=len1) continue;
            cvDr.add(new DoubleRange(pdData[l],pdData[r]));
        }
        return cvDr;
    }
    public static ArrayList<double[]> getSegmentLines(double[] pdX, double[] pdY,double[] pdX1, ArrayList<intRange> irs){
        int i,l,r,len;
        double[] pdXT,pdYT;
        ArrayList<DoubleRange> cvDr=getDoubleRanges(pdX1,irs);
        ArrayList<intRange> segmenters=getSegmenters(pdX,cvDr);
        return getSegmentLines(pdX,pdY,segmenters);
    }
    public static ArrayList<intRange> getSegmenters(double[] pdData,ArrayList<DoubleRange> cvSegmenters){
        int i,len=pdData.length;
        double dL=pdData[0],dR;
        ArrayList<intRange> segmenters=new ArrayList();
        for(i=1;i<len;i++){
            dR=pdData[i];
            if(isSegmented(dL,dR,cvSegmenters)) segmenters.add(new intRange(i-1,i));
        }
        return segmenters;
    }
    public static boolean isSegmented(double x1, double x2, ArrayList<DoubleRange> cvSegmenters){
        int i,len=cvSegmenters.size();
        for(i=0;i<len;i++){
            if(cvSegmenters.get(i).isIntersecting(x1,x2)) return true;
        }
        return false;
    }
    public static ArrayList<intRange> excludeCommonRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        ArrayList<intRange> irs=new ArrayList(),irst,irst0;
        intRange ir1,ir2,ir;
        int i,j,len1=irs1.size(),len2=irs2.size();
        for(i=0;i<len1;i++){
            ir1=irs1.get(i);
            ir=new intRange(ir1);
            irst=new ArrayList();
            irst.add(ir);
            irst0=irst;
            for(j=0;j<len2;j++){
                ir2=irs2.get(j);
                irst=intRange.excludeOverlappedRange(irst, ir2);
                if(irst.isEmpty()) break;                
                if(!irst.get(0).equivalent(irst0.get(0))){
                    j=j;
                }
            }
            for(j=0;j<irst.size();j++){
                irs.add(irst.get(j));
            }
        }
        return irs;
    }    
    public static intRange getXIndexRange(double[] pdX,DoubleRange cDr){
        int i,len=pdX.length,iI=len,iF=-1;
        double x;
        for(i=0;i<len;i++){
            x=pdX[i];
            if(cDr.contains(x)){
                if(i<iI) iI=i;
                if(i>iF) iF=i;
            }
        }
        return new intRange(iI,iF);
    }
    public static boolean[] getSelection(double[] pdX, double[] pdX0, boolean[] pbSelected0){
        //assumpsion: pdX and pdX0 are ascedingly sorted
        int i,j,len=pdX.length;
        boolean[] pbSelected=new boolean[len];
        ArrayList<intRange> cvRanges0=getSelectedRanges(pbSelected0);
        ArrayList<DoubleRange> cvDr=new ArrayList();
        int len1=cvRanges0.size();
        intRange ir;
        DoubleRange dr;
        for(i=0;i<len1;i++){
            ir=cvRanges0.get(i);
            cvDr.add(new DoubleRange(pdX0[ir.getMin()],pdX0[ir.getMax()]));
        }
        double x;
        for(i=0;i<len;i++){
            pbSelected[i]=false;
            x=pdX[i];
            for(j=0;j<len1;j++){
                dr=cvDr.get(j);
                if(dr.contains(x)){
                    pbSelected[i]=true;
                    break;
                }
            }
        }
        return pbSelected;
    }
    public static double[] getLinearInterpolation(double[] pdX0, double[] pdY0,double[] pdX){
        int i,len=pdX.length,len0=pdX0.length;
        double[] pdY=new double[len];
        double x,y;
        for(i=0;i<len;i++){
            x=pdX[i];
            y=getInterpolation(pdX0,pdY0,x);
            pdY[i]=y;
        }
        return pdY;
    }
    public static double getInterpolation(double[] pdX, double[] pdY, double x){
        int len=pdX.length;
        if(x<=pdX[1]) return 0.5*(pdY[0]+pdY[1]);
        if(x>=pdX[len-2]) return 0.5*(pdY[len-2]+pdY[len-1]);
        int i=1;
        double x0=pdX[0],x1=pdX[1];
        while((x-x0)*(x-x1)>0){
            x0=x1;
            i++;
            x1=pdX[i];
        }
        return CommonMethods.getLinearIntoplation(x0, pdY[i-1], x1, pdY[i], x);
    }
    public static int[] calRisingIntervals(double[] pdX, double[] pdY, double maxRisingInterval){
        int i,len=pdY.length,interval,i1;
        if(len<=0) return null;
        double y,y0,delta0,delta,dInterval;
        int pnIntervals[]=new int[len];
        for(i=0;i<len;i++){
            interval=1;
            i1=i+interval;
            if(i1>=len){
                pnIntervals[i]=0;
                continue;
            }
            y0=pdY[i];
            y=pdY[i1];
            delta0=y-y0;
            dInterval=pdX[i1]-pdX[i];
            y0=y;
            while(dInterval<maxRisingInterval){
                interval++;
                i1=i+interval;
                if(i1>=len) {
                    interval--;
                    break;
                }
                dInterval=pdX[i1]-pdX[i];
                y=pdY[i1];
                delta=y-y0;
                if(delta0*delta<=0){
                    interval--;
                    break;
                }
                y0=y;
                delta0=delta;
            }
            pnIntervals[i]=interval;
        }
        return pnIntervals;
    }
    public static int[] calRisingIntervals(double[] pdX, double[] pdY, double maxRisingInterval, int iI, int iF){
        int i,len=iF-iI+1,interval,position,p1,last=pdY.length-1;
        if(len<=0) return null;
        double y,y0,delta0,delta,dInterval;
        int pnIntervals[]=new int[len];
        for(i=0;i<len;i++){
            position=iI+i;
            interval=1;
            p1=position+interval;
            if(p1>last){
                pnIntervals[i]=0;
                continue;
            }
            y0=pdY[position];
            y=pdY[p1];
            delta0=y-y0;
            dInterval=pdX[p1]-pdX[position];
            y0=y;
            while(dInterval<maxRisingInterval){
                interval++;
                p1=position+interval;
                if(p1>last) {
                    interval--;
                    break;
                }
                dInterval=pdX[p1]-pdX[position];
                y=pdY[p1];
                delta=y-y0;
                if(delta0*delta<=0){
                    interval--;
                    break;
                }
                y0=y;
                delta0=delta;
            }
            pnIntervals[i]=interval;
        }
        return pnIntervals;
    }
    static public ArrayList<double[]> calRWSD_DevBased(double[] pdX, double[] pdY, double dMaxInterval,int nWs, double dPValue){
        boolean[] pbSelected=new boolean[pdX.length];
        setElements(pbSelected,true);
        return calRWSD_DevBased(pdX,pdY,pbSelected,dMaxInterval,nWs,dPValue);
    }
    static public ArrayList<double[]> calRWSD_DevBased(double[] pdX, double[] pdY, boolean[] pbSelected, double dMaxInterval,int nWs, double dPValue){
        ArrayList<double[]> pdvMeanSD=new ArrayList();
        int[] pnRisingIntervals=calRisingIntervals(pdX,pdY,dMaxInterval);
        double[] pdDev=getDevArray(pdX,pdY,pnRisingIntervals);
        pdvMeanSD=getRWSD_Cumulative(pdDev,pbSelected,nWs,dPValue);
        return pdvMeanSD;
    }
    static public ArrayList<double[]> calRWSD_SelectedDataDevBased(double[] pdX, double[]pdY, boolean[] pbSelected, int nMaxRisingInterval, int nRWWs, double dPValue){
        double dMaxInterval=pdX[nMaxRisingInterval]-pdX[0];
        ArrayList<double[]> pdvMeanSD0,pdvMeanSD=new ArrayList();
        ArrayList<Integer> positions=new ArrayList();
        int i,len=pbSelected.length,position;
        getSelectedPositionsWithinRange(pbSelected,new intRange(0,len-1),positions);
        int len1=positions.size();
        double[] pdXT=new double[len1],pdYT=new double[len1];
        boolean[] pbSelectedt=new boolean[len1];
        setElements(pbSelectedt,true);
        for(i=0;i<len1;i++){
            pdXT[i]=pdX[positions.get(i)];
            pdYT[i]=pdY[positions.get(i)];
        }
        pdvMeanSD0=calRWSD_DevBased(pdXT,pdYT,pbSelectedt,dMaxInterval,nRWWs,dPValue);
        double[] pdMean0=pdvMeanSD0.get(0),pdSD0=pdvMeanSD0.get(1),pdMean=new double[len],pdSD=new double[len];
        
        pdvMeanSD.add(getLinearInterpolation(pdXT,pdMean0,pdX));
        pdvMeanSD.add(getLinearInterpolation(pdXT,pdSD0,pdX));
        return pdvMeanSD;
    }
    public static double[] getPValues(double[] pdData, int iI, int iF, int delta, double pVOutliars){
        int i,len=pdData.length;
        boolean selected[]=new boolean[len];
        setElements(selected,false);
        for(i=iI;i<=iF;i+=delta){
            selected[i]=true;
        }
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> nvOutliars=new ArrayList();
        findOutliars(pdData,selected,pVOutliars,ms,nvOutliars);
        double m=ms.mean,sd=ms.getSD();
        double[] pdPV=new double[len];
        setElements(pdPV,1.1);
        double p;
        
        for(i=0;i<len;i++){
            p=GaussianDistribution.Phi(pdData[i], m, sd);
            if(p>0.5) p=1-p;
            pdPV[i]=p;
        }
        return pdPV;
    }
    public static int getClosestRangeIndex(ArrayList<intRange> irs, int it){
        int i,len=irs.size();
        if(len==0) return -1;
        int in=0,dist,distN=Math.abs(irs.get(0).getDist(it));
        for(i=1;i<len;i++){
            dist=Math.abs(irs.get(i).getDist(it));
            if(dist<distN){
                distN=dist;
                in=i;
            }
        }
        return in;
    }
    public static int getSortedIndependetDelta(double[] pdX,double[] pdY,int iI, int iF,double dMaxRisingInterval,int nMaxNumDelta,ArrayList<Double> dvDeltas,ArrayList<Integer> nvPositions){
        int i,len=iF-iI+1,left,right,ix,num=0,position,p0;
        int[] pnRisingIntervals=calRisingIntervals(pdX,pdY,dMaxRisingInterval,iI,iF);
        boolean[] pbSelection=new boolean[len];
        setElements(pbSelection,true);
        double[] pdDelta=new double[len];
        for(i=0;i<len;i++){
            right=iI+i+pnRisingIntervals[i];
            pdDelta[i]=(pdY[iI+i]-pdY[right]);
            if(right>iF) pbSelection[i]=false;
        }
        dvDeltas.clear();
        nvPositions.clear();
        while(num<nMaxNumDelta){
            position=getLargestAbsPosition(pdDelta,0,len-1,1,pbSelection,0);
            if(position<0) break;
            dvDeltas.add(pdDelta[position]);
            nvPositions.add(position+iI);
            left=Math.max(getRisingInterval(pnRisingIntervals,position,-1)+position,0);
            right=Math.min(getRisingInterval(pnRisingIntervals,position,1)+position,len-1);
            for(i=left;i<=right;i++){
                pbSelection[i]=false;
            }
            num++;
        }
        return 1;
    }
    public static int getLargestAbsPosition(double[] pdY, int iI, int iF, int delta, boolean[] pbSelection, int iIb){
        int px=-1,i,len=(iF-iI)/delta+1,position;
        double maxA=Double.NEGATIVE_INFINITY,dt;
        for(i=0;i<len;i++){            
            if(pbSelection!=null){
                if(!pbSelection[iIb+i*delta]) continue;
            }
            position=iI+i*delta;
            dt=Math.abs(pdY[position]);
            if(dt>maxA){
                px=position;
                maxA=dt;
            }
        }
        return px;
    }
    public static boolean IsOutlier(double value, double dMin, double dMax, int nTaileOption){
        switch (nTaileOption) {
            case Small:
                return value<dMin;
            case Large:
                return value> dMax;
            case TwoSide:
                return value<dMin||value>dMax;
            default:
                return value<dMin||value>dMax;                
        }
    }
    public static ArrayList<Integer> findOutliers(double[] pdData, MeanSem1 ms, double pValue, int TaileOption){
        ArrayList<Integer> nvOutliers=new ArrayList();
        double dMin=GaussianDistribution.getZatP(pValue, ms.mean, ms.getSD(), 0.0001*ms.getSD());
        double dMax=GaussianDistribution.getZatP(1-pValue, ms.mean, ms.getSD(), 0.0001*ms.getSD());
        for(int i=0;i<pdData.length;i++){
            if(IsOutlier(pdData[i],dMin,dMax,TaileOption)) nvOutliers.add(i);
        }
        return nvOutliers;
    }
    public static int findGapPosition(ArrayList<Double> dvDiffs, int[] pnIndexes){//All elements in dvDiffs are non negative. pnIndexes is the ascending ranking position, i. e. 
        //pnIndex[0] is the position of the smallest element of dvDiffs.
        int len=dvDiffs.size(),index=len-1,gap=0,index0=len-1,p0=pnIndexes[index0],p;
        double dt,dt0=dvDiffs.get(p0), dx=dvDiffs.get(index0),r=0,rx=0,rCutoff=2.;
        index=index0-1;
        if(index>=0) dx=dvDiffs.get(pnIndexes[index]);
        while(index>=0&&r<rCutoff){
            p=pnIndexes[index];
            dt=dvDiffs.get(p);
            r=(dt0-dt)/dt;
            if(r>rx){
                rx=r;
                gap=index+1;
            }
            if(dx/dt>rCutoff) break;
            dt0=dt;
            index--;
        }
        return len-1-gap;//convert the gap position into the one in a descending order
    }
    public static int buildAncoredLine(double[] pdX, double[] pdY, double[] pdLine, ArrayList<Integer> nvAnchors){
        if(nvAnchors.isEmpty()) return -1;
        if(pdX==null||pdY==null) return -1;
        if(pdLine==null) return -1;
        int i,j,start=0,len=nvAnchors.size(),end=nvAnchors.get(0);
        double x0=pdX[0],x1=pdX[end],y0=pdY[0],y1=pdY[end],x,y;
        for(i=start;i<end;i++){
            x=pdX[i];
            pdLine[i]=CommonMethods.getLinearIntoplation(x0, y0, x1, y1, x);
        }
        x0=x1;
        y0=y1;
        start=end;
        for(i=1;i<len;i++){
            end=nvAnchors.get(i);
            x1=pdX[end];
            y1=pdY[end];
            for(j=start;j<end;j++){
                x=pdX[j];
                pdLine[j]=CommonMethods.getLinearIntoplation(x0, y0, x1, y1, x);
            }
            x0=x1;
            y0=y1;
            start=end;
        }
        
        end=pdX.length-1;
        x1=pdX[end];
        y1=pdY[end];
        
        for(i=start;i<=end;i++){
            x=pdX[i];
            pdLine[i]=CommonMethods.getLinearIntoplation(x0, y0, x1, y1, x);
        }
        return 1;
    }
    public static int getElements(double[] pdt,ArrayList<Integer> nvt, ArrayList<Double> dvt){
        if(pdt==null||nvt==null) return -1;
        if(dvt==null) return -1;
        dvt.clear();
        for(int i=0;i<nvt.size();i++){
            dvt.add(pdt[nvt.get(i)]);
        }
        return 1;
    }
    public static int setSortedIndexes(int[] pnIndexes){
        if(pnIndexes==null) return -1;
        for(int i=0;i<pnIndexes.length;i++){
            pnIndexes[i]=i;
        }
        return 1;
    }
    public static double[] getExtremaLine(double[] pdY){
        int i,len=pdY.length,p,p0,p1;
        double y,y0;
        ArrayList<Integer> nvLn=new ArrayList(),nvLx=new ArrayList();
        boolean[] pbt=new boolean[len];
        double pdEnv[]=new double[len];
        setElements(pbt,false);
        CommonMethods.LocalExtrema(pdY, nvLn, nvLx);
        for(i=0;i<nvLn.size();i++){
            pbt[nvLn.get(i)]=true;
        }
        for(i=0;i<nvLx.size();i++){
            pbt[nvLx.get(i)]=true;
        }
        p0=0;
        y0=pdY[p0];
        p1=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p0+1, 1);
        while(p1>0){
            for(p=p0+1;p<=p1;p++){
                pdEnv[p]=pdY[p1];
            }
            p0=p1;
            p1=CommonStatisticsMethods.getFirstSelectedPosition(pbt, true, p0+1, 1);
        }
        p1=len-1;
        for(p=p0+1;p<=p1;p++){
            pdEnv[p]=pdY[p];
        }
        return pdEnv;
    }
    public static ArrayList<Double> getSoomthedCircularArray(ArrayList<Double> dv,int ws){
        ws=Math.min(ws, dv.size()/2);
        int i,p,l,r,len=2*ws+1,size=dv.size();
        double mean=0;
        p=0;
        l=CommonMethods.circularAddition(size, p, -ws);
        r=CommonMethods.circularAddition(size, p, ws);
        p=l;
        for(i=0;i<len;i++){
            mean+=dv.get(p);     
            p=getCircularIndex(p+1,size);
        }
        ArrayList<Double> dvs=new ArrayList();
        dvs.add(mean/len);
        for(p=1;p<size;p++){
            l=CommonMethods.circularAddition(size, p, -ws-1);
            r=CommonMethods.circularAddition(size, p, ws);
            mean-=dv.get(l);
            mean+=dv.get(r);
            dvs.add(mean/len);
        }
        return dvs;
    }
    public static int getLocalExtrema_Circular(ArrayList<Double> dv,ArrayList<Integer> nvMinima, ArrayList<Integer> nvMaxima,ArrayList<Double> dvHeights){
        int delta=1,sign,i,phaseShift=0,size;
        dvHeights.clear();
        nvMinima.clear();
        nvMaxima.clear();
        int pp=getNextExtremumIndex_Circular(dv, 0, 1, delta),pn=getNextExtremumIndex_Circular(dv, 0, -1, delta),p0,p;
        if(pn<0||pp<0) return -1;//all elements in the arraylist are equal
        nvMinima.add(pn);
        nvMaxima.add(pp);
        if(pn>pp){
            sign=1;
            p0=pp;
            p=getNextExtremumIndex_Circular(dv, pn, sign, delta);
            phaseShift=0;
        }else{//pp>pn
            sign=-1;
            p0=pn;
            p=getNextExtremumIndex_Circular(dv, pp, sign, delta);
            phaseShift=1;
        }
        while(p!=p0){
            if(sign>0)
                nvMaxima.add(p);
            else
                nvMinima.add(p);
            sign*=-1;
            p=getNextExtremumIndex_Circular(dv,p,sign,delta);
        }
        size=nvMinima.size();
        double dx,dn,dh;
        for(i=0;i<nvMaxima.size();i++){
            p=nvMaxima.get(i);
            p0=nvMinima.get(getCircularIndex(p-phaseShift,size));
            dx=dv.get(p);
            dn=dv.get(p0);
            dh=dx-dn;
            dvHeights.add(dh);
            
            p0=nvMinima.get(getCircularIndex(p+1-phaseShift,size));
            dn=dv.get(p0);
            dh=dx-dn;
            dvHeights.add(dh);
        }
        return 1;
    }
    public static double getCircularAsymmetry(ArrayList<Double> dv){
        int i,len=dv.size(),i1,shift=len/2;
        double d1,d2,as=0,d,dx=0,dt;
        DoubleRange dr=new DoubleRange();
        for(i=0;i<len;i++){
            i1=getCircularIndex(i+shift,len);
            d1=dv.get(i);
            d2=dv.get(i1);
            d=(d1-d2)/(d1+d2);
            as+=Math.abs(d);
            dr.expandRange(Math.abs(d));
        }
        as/=len;
//        return as;
        return dr.getMax();//13308
    } 
    public static int getFirstSelectedPosition_Circular(boolean[] pbt, int i0, int delta){
        int i=i0,dist=0,len=pbt.length;
        while(!pbt[i]){
            dist+=delta;
            if(dist>=len) return -1;
            i=getCircularIndex(i+delta,len);
        }
        return i;
    }
    public static void coppyToDoubleArrayList(double[] pdY, ArrayList<Double> dvt, int iI, int iF, int delta){
        dvt.clear();
        for(int i=iI;i<=iF;i+=delta){
            dvt.add(pdY[i]);
        }
    }
    public static void getRange(double[] pdt, DoubleRange dr, int iI, int iF, int delta, int exempted){
        dr.resetRange();
        for(int i=iI;i<=iF;i++){
            if(i==exempted) continue;
            dr.expandRange(pdt[i]);
        }
    }
    public static void getRange(double[] pdt, DoubleRange dr, int iI, int iF, int delta, int exempted1, int exempted2){
        dr.resetRange();
        for(int i=iI;i<=iF;i++){
            if(i==exempted1||i==exempted2) continue;
            dr.expandRange(pdt[i]);
        }
    }
    public static double[] getMultiscaleFiltedLine_EdgeProtection(double[] pdY, double[] pdYF0, double[] pdYF1, int nMaxRisingInterval, double ratioCutoff, double[] pdYF){
        //this method return a multiscale filtered line of original signal pdY. pdYF0 and pdYF1 are filted line of shorter and longer scale, respectively.
        //this method replace the segments that may contribute to signal jump with the filted line of shorter scale. 
        int i,j,k,len=pdY.length,pm,p0,p,l,r,l1,r1,pMax,pMin,nRank=1;
        int[] indexes=new int[len];
        double[] pdt;
        if(pdYF==null) pdYF=new double[len];
        copyArray(pdYF1,pdYF);
//        int[] pnIntervals=calRisingIntervals(pdY,nMaxRisingInterval);
        
        int[] pnIntervals=calRisingIntervals(pdY,nMaxRisingInterval);
//        CommonStatisticsMethods.setElements(pnIntervals, 4);
        double[] pdDiff0=getDeltaArray(pdYF0,pnIntervals,nMaxRisingInterval);
        double[] pdDiff1=getDeltaArray(pdYF1,pnIntervals,nMaxRisingInterval);
        double[] pdDA0=getAbs(pdDiff0);
        double[] pdDA1=getAbs(pdDiff1);
        DoubleRange drt=CommonStatisticsMethods.getRange(pdDA1);
        double dMaxDiff=drt.getMax();
        
        pdt=copyArray(pdYF0);
        subtractArray(pdt,pdYF1);   
        
        DoubleRange dr=new DoubleRange();
        ArrayList<Integer> crossingPoints=getCrossingPositions(pdt,0.,0,len-1,1),nvLx=new ArrayList(), nvLn=new ArrayList();
        CommonMethods.LocalExtrema(pdDA0, nvLn, nvLx);
        double delta,dt,ratio,dCutoff;
        int len1=pdDA0.length,sign;
        int signL,signR;
        p0=0;
        
        CommonStatisticsMethods.copyArray(pdYF0,pdt);
//        for(i=0;i<crossingPoints.size();i++){
        for(i=0;i<crossingPoints.size()-1;i++){
            p=crossingPoints.get(i);
            if(p0==131||p0==131){
                p=p;
            }
            if(p0<=8&&p>=8){
                p=p;
            }
            if(p-p0<nRank) {
                p0=p+1;
                continue;
            }//13201
            pm=(p0+p)/2;
            sign=1;
            dt=Math.min(pdYF[p0], pdYF[p]);
            
            if((pdYF[p0]+pdYF[p])<(pdYF0[p]+pdYF0[p])) {
                dt=Math.max(pdYF[p0], pdYF[p]);
                sign=-1;
            }
            
            for(j=p0;j<=p;j++){
                pdYF[j]=dt;
                if(sign*(pdYF0[j]-dt)>0) 
                    pdYF[j]=pdYF0[j];
            }
            
            if(p>=len1) break;
            
            sign=1;
            if((pdYF[p0]+pdYF[p])<(pdYF0[p0]+pdYF0[p])) sign=-1;
            
            signR=1;
            if(pdDiff0[p]<0) signR=-1;
            signL=-1*signR;
            
            if(p0==0){
                l=0;
            }else{
                l=Math.max(CommonStatisticsMethods.getFirstExtremumIndex(pdDiff0, p0-1, signL, -1),p0+getRisingInterval(pnIntervals,p0,-1));                
            }
            r=Math.max(CommonStatisticsMethods.getFirstExtremumIndex(pdDiff0, p, signR, -1),p+1+getRisingInterval(pnIntervals,p+1,-1));
            if(r>=len1){
               break;
            }
            if(l<0||r<0){
                l=l;
            }
            pMax=l;//pMax is the position with greater delta
            pMin=r;
            if(pdDA0[r]>pdDA0[l]) {
                pMax=r;
                pMin=l;
            }
            
            if(dMaxDiff/pdDA0[pMax]>3.&&pMin>2) {
                p0=p+1;
                continue;
            }
            
            if((pdDA0[pMax]/pdDA1[pMax]>2&&sign>0)&&pMin>2) {
                p0=p+1;
                continue;
            }
            
            if(dMaxDiff/pdDA0[pMin]<2&&pMin>2){//13211 over look the first rising phase
                p0=p+1;
                continue;
            }
            
            if(l<0) l=0;
            if(r<0) r=0;
            l1=l+getRisingInterval(pnIntervals,l,1);
            if(r<len-1)
                r1=r+1+getRisingInterval(pnIntervals,r+1,-1)-1;
            else
                r1=r+getRisingInterval(pnIntervals,r,-1);
            if(l1<0) l1=0;
            if(r1<0) r1=0;
            if(r1>=l1)
                getRange(pdDA0,l1,r1,1,dr);
            else
                dr.expandRange(0);            
            
            delta=pdDA0[pMax];
            dt=Math.max(dr.getMax(),pdDA0[pMin]);
            ratio=delta/dt;
            
            if(ratio<ratioCutoff){
                p0=p0+1;
                continue;
            }
            
            //this segment may contribute to the signal jump height.
            //will not replace the shorter scale flited line, except the points that are 
            //higher (or lower, depending on the sign) than the specified ranking.
            for(k=0;k<=nRank;k++){
                indexes[k]=k;
            }
            CommonStatisticsMethods.PartialSort(pdt, indexes, nRank, sign, p0, p, 1);
            dCutoff=pdYF0[indexes[p0+nRank]];
            
            for(j=p0;j<=p;j++){
                dt=pdYF0[j];
                if(sign*(dt-dCutoff)<0) continue;
                pdYF[j]=dt;
            }
            
            p0=p+1;
        }
        return pdYF;
    }
    public static int getPositionOfPoint(ArrayList<Point> points, Point pt){
        int i,len=points.size(),x=pt.x,y=pt.y;
        for(i=0;i<len;i++){
            pt=points.get(i);
            if(pt.x==x&&pt.y==y) return i;
        }
        return -1;
    }
    public static int[] getArrayIndexes(int len){
        int[] pnIndexes=new int[len];
        for(int i=0;i<len;i++){
            pnIndexes[i]=i;
        }
        return pnIndexes;
    }
    public static int[] getRandomizedArrayIndexes(int len){
        int i,j,it,index,n;
        int[] pnIndexes=getArrayIndexes(len);
        for(i=0;i<len;i++){
            n=len-i;
            index=len-1-(int)(Math.random()*n);
            it=pnIndexes[i];
            pnIndexes[i]=pnIndexes[index];
            pnIndexes[index]=it;
        }
        return pnIndexes;
    }
    public static int getLowerBoundIndex(double[] pdData, double dv,int order){//pdData is a sorted double array. order >0 for ascending order.
        int i,len=pdData.length;
        double lower=Double.NEGATIVE_INFINITY,upper;
        if(order<0) lower=Double.POSITIVE_INFINITY;
        for(i=0;i<len;i++){
            upper=pdData[i];
            if(dv==lower) return i;
            if((lower-dv)*(upper-dv)<0) return i;
            lower=upper;
        }
        return len-1;
    }
    public static int[][] getSumLine_Y(int[][] pixels){
        int h=pixels.length,w=pixels[0].length,i,j,sum;
        int[][] pnSum=new int[h][w];
        CommonStatisticsMethods.setElements(pnSum, 0);
        CommonStatisticsMethods.copyArray(pixels[0], pnSum[0]);
        for(i=1;i<h;i++){
            for(j=0;j<w;j++){
                pnSum[i][j]=pnSum[i-1][j]+pixels[i][j];
            }
        }
        return pnSum;
    }
    public static int[][] getSumLine_X(int[][] pixels){
        int h=pixels.length,w=pixels[0].length,i,j,sum;
        int[][] pnSum=new int[h][w];
        CommonStatisticsMethods.setElements(pnSum, 0);
        for(i=0;i<h;i++){
            pnSum[i][0]=pixels[i][0];
        };
        for(i=0;i<h;i++){
            for(j=1;j<w;j++){
                pnSum[i][j]=pnSum[i][j-1]+pixels[i][j];
            }
        }
        return pnSum;
    }
    public static void markExtrema_X(int[][] pixels, int[][] stamp){
        ArrayList<Integer> nvx=new ArrayList(), nvn=new ArrayList();
        ArrayList<Double> dvY=new ArrayList();
        setElements(stamp,0);
        int rows=pixels.length,cols=pixels[0].length,r,c,i,len;
        for(r=0;r<rows;r++){
            dvY.clear();
            for(c=0;c<cols;c++){
                dvY.add((double)pixels[r][c]);
            }
            CommonMethods.LocalExtrema(dvY, nvn, nvx);
            len=nvn.size();
            for(i=0;i<len;i++){
                stamp[r][nvn.get(i)]=-1;
            }
            len=nvx.size();
            for(i=0;i<len;i++){
                stamp[r][nvx.get(i)]=1;
            }
        }
    }
    public static void markExtrema_Y(int[][] pixels, int[][] stamp){
        ArrayList<Integer> nvx=new ArrayList(), nvn=new ArrayList();
        ArrayList<Double> dvY=new ArrayList();
        setElements(stamp,0);
        int rows=pixels.length,cols=pixels[0].length,r,c,i,len;
        for(c=0;c<cols;c++){
            dvY.clear();
            for(r=0;r<rows;r++){
                dvY.add((double)pixels[r][c]);
            }
            CommonMethods.LocalExtrema(dvY, nvn, nvx);
            len=nvn.size();
            for(i=0;i<len;i++){
                stamp[nvn.get(i)][c]=-1;
            }
            len=nvx.size();
            for(i=0;i<len;i++){
                stamp[nvx.get(i)][c]=1;
            }
        }
    }
    public static IntPair nextPositionWithTheValue(int[][] intA, int x, int y, int dx, int dy, int Val, intRange xRange, intRange yRange){
        if(!xRange.contains(x)) return null;
        if(!yRange.contains(y)) return null;
        
        while(intA[y][x]!=Val){
            x+=dx;
            y+=dy;
            if(!xRange.contains(x)) return null;
            if(!yRange.contains(y)) return null;
        }
        return new IntPair(x,y);
    }
    public static MeanSem1[] getSummaryTrace(ArrayList<double[]> pdvY){
        int i,j,num=pdvY.size(),len=pdvY.get(0).length;
        double[] pdY=new double[num];
        MeanSem1[] mss=new MeanSem1[len];
        for(i=0;i<len;i++){
            for(j=0;j<num;j++){
                pdY[j]=pdvY.get(j)[i];
            }
            mss[i]=new MeanSem1(pdY);
        }
        return mss;
    }
    public static int getFirstPositionWithinRange_Ascending(double[] pdX, DoubleRange xRange){//return first position where pdX[p] >=xRange.dMin. pdX is an ascending array
        int len=pdX.length,iI=0,iF=len-1,im=(iI+iF)/2,c;
        double x=pdX[im],x0=xRange.getMin();
        while(im>iI){            
            if(x<x0)
                iI=im;
            else
                iF=im;
            im=(iI+iF)/2;
            x=pdX[im];
        }
        return iI+1;
    }
    public static double getMAD(ArrayList<Double> dvA){
        int i,len=dvA.size();
        ArrayList<Double> dvD=new ArrayList(),dvA1=copyDoubleArray(dvA);
        double median=getMedian(dvA1);
        double dt;
        for(i=0;i<len;i++){
            dt=dvA.get(i)-median;
            dvA1.set(i, Math.abs(dt));
        }
        median=getMedian(dvA1);
        return median;
    }
    public static double getMAD(double[] pdA){
        int i,len=pdA.length;
        double[] pdA1=copyArray(pdA);
        double median=getMedian(pdA1);
        double dt;
        for(i=0;i<len;i++){
            dt=pdA[i]-median;
            pdA1[i]=Math.abs(dt);
        }
        median=getMedian(pdA1);
        return median;
    }
    public static double getSD_MAD(ArrayList<Double> dvA){
        return 1.4826*getMAD(dvA);
    }
    public static double getMedian(ArrayList<Double> dvA0){
        double[] pdA=CommonStatisticsMethods.copyToDoubleArray(dvA0);
        return getMedian(pdA);
    }
    public static double getSD_MAD(double[] pdA){
        return 1.4826*getMAD(pdA);
    }
    public static double getMedian(double[] pdA0){
        double[] pdA=copyArray(pdA0);
        QuickSort.quicksort(pdA);
        double median;
        int len=pdA.length,im=len/2;
        if(len%2==0)
            median=.5*(pdA[im]+pdA[im-1]);
        else
            median=pdA[im];
        return median;
    }
    public static double getZPrimeFacter(ArrayList<Double> dvAp, ArrayList<Double> dvAn){
        double v=0.;
        MeanSem0 msP=new MeanSem0(dvAp),msN=new MeanSem0(dvAn);
        v=1.-3*(msP.getSD()+msN.getSD())/(msP.mean-msN.mean);
        return v;
    }
    public static double getZPrimeFacter_MAD(ArrayList<Double> dvAp, ArrayList<Double> dvAn){
        double v=0.;
        MeanSem0 msP=new MeanSem0(dvAp),msN=new MeanSem0(dvAn);
        double sdP=getSD_MAD(dvAp),sdN=getSD_MAD(dvAn);
        v=1.-3*(sdP+sdN)/(msP.mean-msN.mean);
        return v;
    }
    public static Object[] getObjectArray_StringArray(String[] ps){
        int i,len=ps.length;
        Object[] po=new Object[len];
        for(i=0;i<len;i++){
            po[i]=ps[i];
        }
        return po;
    }
    public static double[] getPValues(double[] pdVs){
        MeanSem0 ms=new MeanSem0(pdVs);
        int i,len=pdVs.length;
        double[] pdPs=new double[len];
        double p,m=ms.mean,sd=ms.getSD();
        for(i=0;i<len;i++){
            p=GaussianDistribution.Phi(pdVs[i], m, sd);
            p=Math.min(p, 1.-p);
            pdPs[i]=p;
        }
        return pdPs;
    }
    public static double[] getPValues_MAD(double[] pdVs){
        double p,m=getMedian(pdVs),sd=getSD_MAD(pdVs);
        int i,len=pdVs.length;
        double[] pdPs=new double[len];
        for(i=0;i<len;i++){
            p=GaussianDistribution.Phi(pdVs[i], m, sd);
            p=Math.min(p, 1.-p);
            pdPs[i]=p;
        }
        return pdPs;
    }
    public static ArrayList<Integer> getOutliars(double[] pdVs, double p){
        ArrayList<Integer> indexes=new ArrayList();
        double[] dpPs=getPValues(pdVs);
        for(int i=0;i<dpPs.length;i++){
            if(dpPs[i]<p) indexes.add(i);
        }
        return indexes;
    }
    public static ArrayList<Integer> getOutliars_MAD(double[] pdVs, double p){
        ArrayList<Integer> indexes=new ArrayList();
        double[] dpPs=getPValues_MAD(pdVs);
        for(int i=0;i<dpPs.length;i++){
            if(dpPs[i]<p) indexes.add(i);
        }
        return indexes;
    }
    public static int getHistPeakValue(int[][] pixels, double dMinDelta, int Ranking){
        ArrayList<Double> dvData=getElements(pixels);
        Histogram hist=new Histogram(dvData);
//        hist.smoothHistogram(nWS);
        HistogramHandler hh=new HistogramHandler();
        hh.update(hist);
        ArrayList<Integer> pis=hh.getPeakIndexess();
        int p=pis.get(Ranking),pixel;
        double delta=hist.getDelta(),position,dn,dx;
        
        while(delta>dMinDelta){
            position=hist.getPosition(p);
            dn=position-delta;
            dx=position+1.5*delta;
            dvData=getElementsInRange(dvData,new DoubleRange(dn,dx));
            hist=new Histogram(dvData);
            delta=hist.getDelta();
            hh.update(hist);
            p=hh.getMainPeak();
        }
        pixel=(int)(hist.getPosition(p)+0.5);
        return pixel;
    }
    public static ArrayList<Double> getElementsInRange(ArrayList<Double> dvData0, DoubleRange dr){
        ArrayList<Double> dvData=new ArrayList();
        double dt;
        for(int i=0;i<dvData0.size();i++){
            dt=dvData0.get(i);
            if(dr.contains(dt)) dvData.add(dt);
        }
        return dvData;
    }
    public static void copyToArrayListofObjectArray(ArrayList<ArrayList<String>> vvsSt, ArrayList<Object[]> vpo){
        vpo.clear();
        ArrayList<String> svSt;
        int i,len=vvsSt.size(),len1,j;
        for(i=0;i<len;i++){
            svSt=vvsSt.get(i);
            len1=svSt.size();
            Object[] op=new Object[len1];
            for(j=0;j<len1;j++){
                op[j]=svSt.get(j);
            }
            vpo.add(op);
        }
    }
}
