/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.CustomDataTypes;
import java.util.ArrayList;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class intRange {
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
        public intRange(int nMin, int nMax){
            this.nMin=nMin;
            this.nMax=nMax;
        }

        public boolean contenEquals(intRange ir){
            return (nMin==ir.getMin()&&nMax==ir.getMax());
        }

        public intRange(FloatRange fr, float f){
            nMin=(int)(fr.getMin()/f);
            nMax=(int)(fr.getMax()/f);
        }

        public intRange(float x1, float x2, float f){
            nMin=(int)(x1/f);
            nMax=(int)(x2/f);
        }
        public boolean contains(intRange ir){
            return contains(ir.nMax)&&contains(ir.nMin);
        }
        public static intRange getMaxRange(){
            return new intRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public int GetIndex(int x){
            if(emptyRange()){
                expandRange(x);
            }
            if(x<nMin||x>nMax) return -1;
            return x-nMin;
        }

        public int mirrorIntoRange(int nV){
            if(emptyRange()) IJ.error("Cannot use mirrorIntoRange of an impty intRange");
            if(nMax==nMin) return nMin;
            if(nV<nMin) nV=nMin+(nMin-nV);
            if(nV<=nMax) return nV;
            int diff=(nV-nMax)%(2*(nMax-nMin));
            nV=nMax-diff;
            diff=nMin-nV;
            if(diff>0) nV=nMin+diff;
            return nV;
        }

        public boolean emptyRange(){
            return (nMin>nMax);
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

        public int smallerMax(int shift, intRange aRange){//shift is on this
            return Math.min(nMax+shift, aRange.getMax());
        }

        public int smallerMax(intRange aRange, int shift){//shift is on aRange
            return Math.min(nMax, aRange.getMax()+shift);
        }

        public int largerMin(intRange aRange){
            return Math.max(nMin, aRange.getMin());
        }

        public int largerMin(int shift, intRange aRange){//shift is on this
            return Math.max(nMin+shift, aRange.getMin());
        }
        
        public int largerMin(intRange aRange, int shift){//shift is on aRange
            return Math.max(nMin, aRange.getMin()+shift);
        }

        public intRange overlappedRange(intRange aRange){
            return new intRange(largerMin(aRange),smallerMax(aRange));
        }

        public intRange overlappedRange(intRange aRange, int shift){//shift is on aRange
            return new intRange(largerMin(aRange, shift),smallerMax(aRange, shift));
        }

        public intRange overlappedRange(int shift, intRange aRange){//shift is on this
            return new intRange(largerMin(shift, aRange),smallerMax(shift, aRange));
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
            return Math.max(0,nMax-nMin+1);
        }
        public int getClosestIntInRange(int n){
            if(n<nMin) return nMin;
            if(n>nMax) return nMax;
            return n;
        }
        public int getClosestIntInRange(int n, int side){
            if(n<nMin&&side<0) return nMin;
            if(n>nMax&&side>0) return nMax;
            return n;
        }
        public boolean enclosed(intRange aRange){
            if(aRange.getMin()<nMin) return false;
            if(aRange.getMax()>nMax) return false;
            return true;
        }
        
        public ArrayList<intRange> excludedNonEmptyRanges(intRange ir){
            ArrayList<intRange> irs=new ArrayList();
            if(!overlapped(ir)){
                irs.add(new intRange(nMin,nMax));
                return irs;
            }
            int nMin1=ir.getMin();
            int nMax1=ir.getMax();
            if(nMin<nMin1) irs.add(new intRange(nMin,nMin1-1));
            if(nMax>nMax1) irs.add(new intRange(nMax1+1,nMax));
            return irs;
        }

        public void reset(intRange ir){
            nMin=ir.getMin();
            nMax=ir.getMax();
        }

        public void shiftRange(int delta){
            nMin+=delta;
            nMax+=delta;
        }

        public boolean isBorder(int x){
            if(x==nMin||x==nMax) return true;
            return false;
        }
        public boolean isBorder(int x, int direction){
            if(x==getBorder(direction)) return true;
            return false;
        }
        public int getBorder(int direction){
            if(direction>0) return nMax;
            return nMin;
        }
        public boolean overlapped(intRange ir, int shift){
            if(ir.emptyRange()) return false;
            if(ir.nMin+shift>nMax||ir.nMax+shift<nMin) return false;
            return true;
        }
        
        public static intRange glossRange(ArrayList<intRange> irs){
            int len=irs.size();
            return new intRange(irs.get(0).nMin, irs.get(len-1).nMax);
        }

        public static intRange commonRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2){//both list of ranges are assumed to be in ascending order.
            return glossRange(irs1).overlappedRange(glossRange(irs2));
        }

        public static ArrayList<intRange> getOverlappingRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2, int shift2){//both list of ranges are assumed to be in ascending order.
            ArrayList<intRange> overlappingRanges=new ArrayList();
            int len1=irs1.size(),len2=irs2.size(),i,j;
            if(len1==0||len2==0) return overlappingRanges;
            int nn1=irs1.get(0).nMin,nx1=irs1.get(len1-1).nMax;
            int nn2=irs2.get(0).nMin+shift2,nx2=irs2.get(len2-1).nMax+shift2;

            int idI1,idI2,idF1,idF2,id1,id2;


            if(nn1>nx2||nx1<nn2) return overlappingRanges;

            int nn,nx;

            intRange ir,irt;
            idI1=0;
            idI2=0;
            boolean bDone=false;
            while(idI1<len1&&idI2<len2&&!bDone){
                nn1=irs1.get(idI1).nMin;
                nn2=irs2.get(idI2).nMin+shift2;
                if(nn1<=nn2){
                    while(irs1.get(idI1).nMax<nn2){
                        idI1++;
                        if(idI1>=len1){
                            bDone=true;
                            break;
                        }
                    }
                }else{
                    nn1-=shift2;
                    while(irs2.get(idI2).nMax<nn1){
                        idI2++;
                        if(idI2>=len2){
                            bDone=true;
                            break;
                        }
                    }
                }

                if(bDone) break;
                
                idF1=idI1;
                idF2=idI2;
                nx1=irs1.get(idF1).nMax;
                nx2=irs2.get(idF2).nMax+shift2;


                if(nx1<=nx2){
//                    idF1++;
                    while(irs1.get(idF1).nMin<=nx2){
                        idF1++;
                        if(idF1>=len1){
                            bDone=true;
                            break;
                        }
                    }
                    idF1--;
                    
                    ir=irs2.get(idI2);
                    for(id1=idI1;id1<=idF1;id1++){
                        irt=ir.overlappedRange(shift2,irs1.get(id1));
                        if(irt.emptyRange()) continue;
                        overlappingRanges.add(irt);
                    }


                    if(idF1>idI1)
                        idI1=idF1;
                    else
                        idI1=idF1+1;

                    idI2++;
                }else{
//                    idF2++;
                    while(irs2.get(idF2).nMin<=nx1){
                        idF2++;
                        if(idF2>=len2){
                            bDone=true;
                            break;
                        }
                    }
                    idF2--;

                    ir=irs1.get(idI1);
                    for(id2=idI2;id2<=idF2;id2++){
                        irt=ir.overlappedRange(irs2.get(id2),shift2);
                        if(irt.emptyRange()) continue;
                        overlappingRanges.add(irt);
                    }

                    idI1++;

                    if(idF2>idI2)
                        idI2=idF2;
                    else
                        idI2=idF2+1;

                }
            }

            return overlappingRanges;
        }

        public static boolean overlapped(ArrayList<intRange> irs1, ArrayList<intRange> irs2, int shift2){//both list of ranges are assumed to be in ascending order.
            int len1=irs1.size(),len2=irs2.size(),i,j;
            int nn1=irs1.get(0).nMin,nx1=irs1.get(len1-1).nMax;
            int nn2=irs2.get(0).nMin+shift2,nx2=irs2.get(len2-1).nMax+shift2;

            if(nn1>nx2||nx1<nn2) return false;

            intRange ir1,ir2;
            int ji=0,jf=len2-1;
            for(i=0;i<len1;i++){
                ir1=irs1.get(i);
                if(ir1.nMax<nn2) continue;
                if(ir1.nMin>nx2) break;
                for(j=ji;j<=jf;j++){
                    ir2=irs2.get(j);
                    if(ir2.nMax<nn1){
                        ji=j+1;
                        continue;
                    }
                    if(ir1.nMin>nx2){
                        jf=j-1;
                        break;
                    }
                    if(ir1.overlapped(ir2,shift2)) return true;
                }
            }
            return false;
        }
        public void setCommonRange(intRange ir){
            nMin=Math.max(nMin, ir.getMin());
            nMax=Math.min(nMax, ir.getMax());
        }
        public void setCommonRange(int n, int x){
            nMin=Math.max(nMin, n);
            nMax=Math.min(nMax, x);
        }
        public boolean equivalent(intRange ir){
            return (ir.nMax==nMax&&ir.nMin==nMin);
        }
        static public intRange getContainingRange(ArrayList<intRange> ranges, int iT){
            int i,len=ranges.size();
            intRange ir;
            for(i=0;i<len;i++){
                ir=ranges.get(i);
                if(ir.contains(iT)) return ir;
            }
            return null;
        }
        static public boolean contains(ArrayList<intRange> ranges, int iT){
            int i,len=ranges.size();
            for(i=0;i<len;i++){
                if(ranges.get(i).contains(iT)) return true;
            }
            return false;
        }
        static public boolean enclosed(ArrayList<intRange> ranges, intRange ir){
            int i,len=ranges.size();
            for(i=0;i<len;i++){
                if(ranges.get(i).enclosed(ir)) return true;
            }
            return false;
        }
        static public boolean overlapped(ArrayList<intRange> ranges, intRange ir){
            int i,len=ranges.size();
            for(i=0;i<len;i++){
                if(ranges.get(i).overlapped(ir)) return true;
            }
            return false;
        }
        static public ArrayList<intRange> getOverlappedRanges(ArrayList<intRange> ranges, intRange ir){
            int i,len=ranges.size();
            ArrayList<intRange> rangesT=new ArrayList();
            intRange irT;
            for(i=0;i<len;i++){
                irT=ranges.get(i).overlappedRange(ir);
                if(!irT.emptyRange()) rangesT.add(irT);
            }
            return rangesT;
        }
        public static ArrayList<intRange> excludeOverlappedRange(intRange ir1, intRange ir2){
            ArrayList<intRange> irs=new ArrayList();
            intRange ir;
            ir=new intRange(ir1.nMin,Math.min(ir1.nMax,ir2.nMin));
            if(!ir.emptyRange()) irs.add(ir);
            ir=new intRange(Math.max(ir1.nMin, ir2.nMax),ir1.nMax);
            if(!ir.emptyRange()) irs.add(ir);
            return irs;
        }
        public static ArrayList<intRange> excludeOverlappedRange(ArrayList<intRange> irs0, intRange ir){
            ArrayList<intRange> irs=new ArrayList(),irst;
            int i,len=irs0.size(),len1,j;
            intRange irt;
            for(i=0;i<len;i++){
                irst=excludeOverlappedRange(irs0.get(i),ir);
                for(j=0;j<irst.size();j++){
                    irs.add(irst.get(j));
                }
            }
            return irs;
        }
        public int getDist(int it){
            if(contains(it)) return 0;
            if(it<nMin) return it-nMin;
            return it-nMax;//(it>nMax) 
        }
}
