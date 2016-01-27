/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.DataClasses;
import utilities.Constants;

/**
 *
 * @author Taihao
 */
public class FloatRange {
//    float smallRatio=0.0001f;
    float smallRatio=0.0f;
    float fMin;
    float fMax;
    public FloatRange(float x1, float x2){
        fMin=x1;
        fMax=x2;
    }
    public FloatRange(){
        fMin=Float.POSITIVE_INFINITY;
        fMax=Float.NEGATIVE_INFINITY;
    }
    
    public FloatRange(intRange ir, float x){
        fMin=ir.getMin()*x;
        fMax=(ir.getMax()+1-smallRatio)*x;
    }
    
    public FloatRange(int nMin,int nMax, float x){
        fMin=nMin*x;
        fMax=(nMax+1-smallRatio)*x;
    }
    
    public FloatRange getRange(){
        return new FloatRange(fMin,fMax);
    }
    public boolean isValid(){
        return (fMin<=fMax);
    }
    
    public void resetRange(){
        fMin=Float.POSITIVE_INFINITY;
        fMax=Float.NEGATIVE_INFINITY;
    }
    public float getExpand(){
        return fMax-fMin;
    }
    public void mergeRange(FloatRange aRange){
        if(isValid()){            
            if(fMin>aRange.getMin()) fMin=aRange.getMin();
            if(fMax<aRange.getMax()) fMax=aRange.getMax();
        }else{            
            fMin=aRange.getMin();
            fMax=aRange.getMax();
        }            
    }
    public boolean contains(float x){
        return (x>=fMin&&x<=fMax);
    }
    public float getMin(){
        return fMin;
    }
    public float getMax(){
        return fMax;
    }
    public void setMin(float x){
        fMin=x;
    }
    public void setMax(float x){
        fMax=x;
    }
    public boolean overlap(FloatRange afr){
        if(fMin>afr.fMax||fMax<afr.fMin) return false;
        return true;
    }
    public Float getMin(float x){
        if(x<fMin) return x;
        return fMin;
    }
    public Float getMax(float x){
        if(x>fMax) return x;
        return fMax;
    }
    public Float getSmallerMin(FloatRange afr){
        float x=afr.getMin();
        if(fMin<=x) return fMin;
        return x;
    }
    public Float getSmallerMax(FloatRange afr){
        float x=afr.getMax();
        if(fMax<=x) return fMax;
        return x;
    }
    public Float getBiggerMin(FloatRange afr){
        float x=afr.getMin();
        if(fMin>=x) return fMin;
        return x;
    }
    public Float getBiggerMax(FloatRange afr){
        float x=afr.getMax();
        if(fMax>=x) return fMax;
        return x;
    }
    public Float getSmallerMax(float x){
        if(fMax<x) return fMax;
        return x;
    }
    public Float getBiggerMin(float x){
        if(fMin>x) return fMin;
        return x;
    }
}
