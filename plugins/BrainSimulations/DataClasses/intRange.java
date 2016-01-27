/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations.DataClasses;

/**
 *
 * @author Taihao
 */
public class intRange {
        int nMin,nMax;
        public static int m_nPosInt=2147483647/4;
        public static int m_nNegInt=-2147483648/4;
        public void resetRange(){
            nMin=m_nPosInt;
            nMax=m_nNegInt;
        }
        public intRange(){
            nMin=m_nPosInt;
            nMax=m_nNegInt;
        }
        
        public intRange(FloatRange fr, float f){
            nMin=(int)(fr.getMin()/f);
            nMax=(int)(fr.getMax()/f);
        }
        
        public intRange(float x1, float x2, float f){
            nMin=(int)(x1/f);
            nMax=(int)(x2/f);
        }
        
        public int GetIndex(int x){
            if(empyRange()){
                expandRange(x);
            }
            if(x<nMin||x>nMax) return -1;
            return x-nMin;
        }
        
        public boolean empyRange(){
            return (nMin>nMax);
        }
        
        public intRange(int n1, int n2){
            nMin=n1;
            nMax=n2;
        }
        public intRange(intRange ir){
            nMin=ir.getMin();
            nMax=ir.getMax();
        }
        public int getMin(){
            return nMin;
        }
        public int getMax(){
            return nMax;
        }
        public void setMin(int x){
            nMin=x;
        }
        public void setMax(int x){
            nMax=x;
        }
        public boolean contains(int x)
        {
            if (x>=nMin&&x<=nMax) 
                return true;
            else
                return false;
        }
        
        public void expandRanges(int x1, int x2){
            if(x1<nMin) nMin=x1;
            if(x2>nMax) nMax=x2;
        }
        
        public void expandRange(int x){
            if(x<nMin) nMin=x;
            if(x>nMax) nMax=x;
        }
        
        public void expandRange(intRange ar){
            if(ar.getMin()<nMin)nMin=ar.getMin();
            if(ar.getMax()>nMax)nMax=ar.getMax();
        }
        
        public void mergeRanges(intRange Range2){
            expandRanges(Range2.nMin, Range2.nMax);
        }
        
        public void setRange(int x1, int x2){
            nMin=x1;
            nMax=x2;
        }
        
        public void copyRange(intRange ar){
           ar.resetRange();
           ar.expandRanges(nMin, nMax);
        }
        
        public boolean overlapOrconnected(intRange aRange){
            if(aRange.nMax<nMin-1||aRange.nMin>nMax+1) 
                return false;
            else
                return true;
        }
        
        public boolean overlapped(intRange aRange){
            if(aRange.nMax<nMin||aRange.nMin>nMax) 
                return false;
            else
                return true;
        } 
        
        public int smallerMax(intRange aRange){
            return Math.min(nMax, aRange.getMax());
        }
        
        public int largerMin(intRange aRange){
            return Math.max(nMin, aRange.getMin());
        }
        
        public intRange overlappedRange(intRange aRange){
            return new intRange(largerMin(aRange),smallerMax(aRange));
        }        
        
        public int getMidpoint(){
            return (nMax+nMin)/2;
        }
        public void recenter(int cx){
            int dx=cx-getMidpoint();
            nMax+=dx;
            nMin+=dx;
        }
        public int getRange(){
            return nMax-nMin-1;
        }
}
