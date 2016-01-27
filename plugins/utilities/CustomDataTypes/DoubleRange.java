/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.CustomDataTypes;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class DoubleRange {
//    double smallRatio=0.0001f;
    double smallRatio=0.0f;
    double dMin;
    double dMax;
    public DoubleRange(DoubleRange dr){
        this(dr.getMin(),dr.getMax());
    }
    public DoubleRange(double x1, double x2){
        dMin=x1;
        dMax=x2;
    }
    public DoubleRange(){
        dMin=Double.POSITIVE_INFINITY;
        dMax=Double.NEGATIVE_INFINITY;
    }

    public DoubleRange(intRange ir, double x){
        dMin=ir.getMin()*x;
        dMax=(ir.getMax()+1-smallRatio)*x;
    }

    public DoubleRange(int nMin,int nMax, double x){
        dMin=nMin*x;
        dMax=(nMax+1-smallRatio)*x;
    }

    public double getRange(){
        return dMax-dMin;
    }
    public boolean isValid(){
        return (dMin<=dMax);
    }

    public void resetRange(){
        dMin=Double.POSITIVE_INFINITY;
        dMax=Double.NEGATIVE_INFINITY;
    }
    public void setRange(double dn, double dx){
        dMin=dn;
        dMax=dx;
    }
    public double getExpand(){
        return dMax-dMin;
    }
    public void mergeRange(DoubleRange aRange){
        if(isValid()){
            if(dMin>aRange.getMin()) dMin=aRange.getMin();
            if(dMax<aRange.getMax()) dMax=aRange.getMax();
        }else{
            dMin=aRange.getMin();
            dMax=aRange.getMax();
        }
    }
    public boolean contains(double x){
        return (x>=dMin&&x<=dMax);
    }
    public boolean contains(DoubleRange dr){
        return (contains(dr.getMin())&&contains(dr.getMax()));
    }
    public double getMin(){
        return dMin;
    }
    public double getMax(){
        return dMax;
    }
    public double getMidpoint(){
        return 0.5*(dMin+dMax);
    }
    public void setMin(double x){
        dMin=x;
    }
    public void setMax(double x){
        dMax=x;
    }
    public boolean overlap(DoubleRange adr){
        if(dMin>adr.dMax||dMax<adr.dMin) return false;
        return true;
    }
    public double getMin(double x){
        if(x<dMin) return x;
        return dMin;
    }
    public double getMax(double x){
        if(x>dMax) return x;
        return dMax;
    }
    public DoubleRange getOverlapRange(DoubleRange adr){
        return new DoubleRange(Math.max(dMin, adr.getMin()),Math.min(dMax,adr.getMax()));
    }
    public double getSmallerMin(DoubleRange adr){
        double x=adr.getMin();
        if(dMin<=x) return dMin;
        return x;
    }
    public double getSmallerMax(DoubleRange adr){
        double x=adr.getMax();
        if(dMax<=x) return dMax;
        return x;
    }
    public double getBiggerMin(DoubleRange adr){
        double x=adr.getMin();
        if(dMin>=x) return dMin;
        return x;
    }
    public double getBiggerMax(DoubleRange adr){
        double x=adr.getMax();
        if(dMax>=x) return dMax;
        return x;
    }
    public double getSmallerMax(double x){
        if(dMax<x) return dMax;
        return x;
    }
    public double getBiggerMin(double x){
        if(dMin>x) return dMin;
        return x;
    }
    public void expandRange(double dV){
        if(dV<dMin) dMin=dV;
        if(dV>dMax) dMax=dV;
    }
    public void expandRange(DoubleRange dr){
        expandRange(dr.getMin());
        expandRange(dr.getMax());
    }
    public String getDataRangeAsString(int precision){
        String st=PrintAssist.ToString(dMin,precision)+" to "+PrintAssist.ToString(dMax,precision);
        return st;
    }
    public void trimRange(double xI, double xF){
        if(dMin<xI) dMin=xI;
        if(dMax>xF) dMax=xF;
    }
    public boolean isIntersecting(double x1, double x2){
        if(contains(x1)&&!contains(x2)) return true;
        if(contains(x2)&&!contains(x1)) return true;
        return false;
    }
    public boolean isRegularRange(){
        if(Double.isInfinite(dMin)) return false;
        if(Double.isNaN(dMin)) return false;
        if(Double.isInfinite(dMax)) return false;
        if(Double.isNaN(dMax)) return false;
        return true;
    }
    public int getComparison(double d){
        if(d<dMin) return -1;
        if(d>dMax) return 1;
        return 0;
    }
    public void expandRange(double[] pdY){
        for(int i=0;i<pdY.length;i++){
            expandRange(pdY[i]);
        }
    }
    public void expandRange(double[] pdY, int iI, int iF){
        for(int i=iI;i<=iF;i++){
            expandRange(pdY[i]);
        }
    }
}
