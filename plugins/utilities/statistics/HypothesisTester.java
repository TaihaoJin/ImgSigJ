/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import org.apache.commons.math.stat.inference.TestUtils;
import java.util.ArrayList;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CommonStatisticsMethods;
import org.apache.commons.math.MathException;
import utilities.statistics.GaussianDistribution;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class HypothesisTester {
    static public double chiSquareTestDataSetsComparison(double[] pdV1, double[] pdV2){//return pValue
        int i,len1=pdV1.length,len2=pdV2.length;
        ArrayList<Double> dv1=new ArrayList(),dv2=new ArrayList();
        for(i=0;i<len1;i++){
            dv1.add(pdV1[i]);
        }
        for(i=0;i<len1;i++){
            dv2.add(pdV2[i]);
        }
        return chiSquareTestDataSetsComparison(dv1,dv2);
    }
    static public double chiSquareTestDataSetsComparison(ArrayList<Double> dv1, ArrayList<Double> dv2){//return pValue
        int len1=dv1.size(),len2=dv2.size(),i;
        ArrayList<Double> dv=new ArrayList();

        DoubleRange cDr=CommonStatisticsMethods.getRange(dv1);
        cDr.expandRange(CommonStatisticsMethods.getRange(dv2));

        int nNumBins=50;
        if(nNumBins>len1+len2) nNumBins=len1+len2;

        Histogram hist1=new Histogram(), hist2=new Histogram();
        hist1.update(cDr.getMin(), cDr.getMax(), cDr.getRange()/nNumBins);
        hist2.update(cDr.getMin(), cDr.getMax(), cDr.getRange()/nNumBins);

        hist1.addData(dv1);
        hist2.addData(dv2);

        ArrayList <Integer> nvCounts1=new ArrayList(), nvCounts2=new ArrayList();
        int len=hist1.getDim(),count1,count2;

        for(i=0;i<len;i++){
            count1=hist1.getCounts(i);
            count2=hist2.getCounts(i);
            if(count1==0&&count2==0) continue;
            nvCounts1.add(count1);
            nvCounts2.add(count2);
        }

        len=nvCounts1.size();

        long[] plDist1=new long[len],plDist2=new long[len];
        for(i=0;i<len;i++){
            plDist1[i]=nvCounts1.get(i);
            plDist2[i]=nvCounts2.get(i);
        }

//       double p=org.apache.commons.math.stat.inference.TestUtils.chiSquareDataSetsComparison(plDist1, plDist2);
       double p=1.1;
       try{
           p=org.apache.commons.math.stat.inference.TestUtils.chiSquareTestDataSetsComparison(plDist1, plDist2);
       }
       catch(MathException e){
//           IJ.error("MathException in chiSquareTestDataSetsComparison(ArrayList<Double> dv1, ArrayList<Double> dv2)");
           p=1.1;
       }
       return p;
    }
    public static double tTest(ArrayList<Double> dv1, ArrayList<Double> dv2){
        int len1=dv1.size(),len2=dv2.size(),i;
        double pd1[]=new double[len1],pd2[]=new double[len2];
        for(i=0;i<len1;i++){
            pd1[i]=dv1.get(i);
        }
        for(i=0;i<len2;i++){
            pd2[i]=dv2.get(i);
        }
        double p=1.;
        try{
            p=TestUtils.tTest(pd1, pd2);
        }
        catch(IllegalArgumentException e){
//            IJ.error("IllegalArgumentException in tTest");
            p=1.1;
        }
        catch(MathException e){
//            IJ.error("MathException in tTest");
            p=1.1;
            
        }
        return p;
    }
    public static double tTest(double[] pd1,double[] pd2){
        double p=1.;
        try{
            p=TestUtils.tTest(pd1, pd2);
        }
        catch(IllegalArgumentException e){
//            IJ.error("IllegalArgumentException in tTest");
            p=1.1;
        }
        catch(MathException e){
//            IJ.error("MathException in tTest");
            p=1.1;
            
        }
        return p;
    }
    public static double tTest(MeanSem1 ms1, MeanSem1 ms2){
       double p=1.;
        try{
            p=TestUtils.tTest(ms1, ms2);
        }
        catch(IllegalArgumentException e){
//            IJ.error("IllegalArgumentException in tTest");
            p=1.1;
        }
        catch(MathException e){
            IJ.error("MathException in tTest");
            p=1.1;
        }
        return p;        
    }
    public static double tTest(double mu, MeanSem1 ms){
       double p=1.;
        try{
            p=TestUtils.tTest(mu, ms);
        }
        catch(IllegalArgumentException e){
//            IJ.error("IllegalArgumentException in tTest");
            p=1.1;
        }
        catch(MathException e){
            IJ.error("MathException in tTest");
            p=1.1;
        }
        double m=Math.abs(mu-ms.mean);
        int df=(int)ms.getN()-1;
        p=CommonStatisticsMethods.getPValue_TTest(df, m/ms.sem);
        return p;        
    }
    public static double tTest(ArrayList<Double> dvT, double mean){
        int len=dvT.size(),i;
        double pdT[]=new double[len];
        for(i=0;i<len;i++){
            pdT[i]=dvT.get(i);
        }
        double p=1.;
        try{
            p=TestUtils.tTest(mean, pdT);
        }
        catch(IllegalArgumentException e){
//            IJ.error("IllegalArgumentException in tTest");
            p=1.1;
        }
        catch(MathException e){
            IJ.error("MathException in tTest");
            p=1.1;
        }
        return p;
    }
    public static double tTest(ArrayList<Double> dvT, double mu, double sd){
        int len=dvT.size(),i;
        if(len<=1) return 1.1;
        double mean=0;
        for(i=0;i<len;i++){
            mean+=dvT.get(i);
        }
        mean/=len;
        mean=Math.abs(mean);
        double p=CommonStatisticsMethods.getPValue_TTest(len-1, (Math.abs(mean-mu)/(sd/Math.sqrt(len))));
        return p;
    }
    public static double MannWhitneyUTest(ArrayList<Double> dv1, ArrayList<Double> dv2){
        int len1=dv1.size(),len2=dv2.size(),i;
        double pd1[]=new double[len1],pd2[]=new double[len2];
        if(len1<5||len2<5) return -1;
        for(i=0;i<len1;i++){
            pd1[i]=dv1.get(i);
        }
        for(i=0;i<len2;i++){
            pd2[i]=dv2.get(i);
        }
        double p=1.;
        MannWhitneyUTest test=new MannWhitneyUTest();
        return test.mannWhitneyUTest(pd1, pd2);
    }
    public static double generalTTest(ArrayList<Double> dv1, ArrayList<Double> dv2, double dSD){        
        int minLen=3,len1=dv1.size(),len2=dv2.size();
        if(len1>=minLen&&len2>=minLen) return tTest(dv1,dv2);
        return tTestGivenSD(dv1,dv2,dSD);
    }
    public static double tTestGivenSD(ArrayList<Double> dv1, ArrayList<Double> dv2, double dSD){     
        int df=dv1.size()+dv2.size()-1;
        if(df<2) return -1;
        double t=Math.abs((CommonStatisticsMethods.getMean(dv1)-CommonStatisticsMethods.getMean(dv2)));
        t/=(dSD*Math.sqrt(1./dv1.size()+1./dv2.size()));
        return CommonStatisticsMethods.getPValue_TTest(df, t);
    }   
}
