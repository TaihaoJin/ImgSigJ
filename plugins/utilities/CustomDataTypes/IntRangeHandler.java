/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.CustomDataTypes;
import java.util.ArrayList;
import utilities.CustomDataTypes.intRange;
/**
 *
 * @author Taihao
 */
public class IntRangeHandler {
    static final int headAscending=0, tailAscending=1, headDescending=2, tailDescending=3;
    public static ArrayList<intRange> getOverlappingRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2, int shift){
        ArrayList<intRange> irst=new ArrayList();
        int len=irs2.size();
        intRange ir;
        for(int i=0;i<len;i++){
            ir=new intRange(irs2.get(i));
            ir.shiftRange(shift);
            irst.add(ir);
        }
        return getOverlappingRanges(irs1,irst);
    }
    public static boolean overlapping(ArrayList<intRange> irs1, ArrayList<intRange> irs2, int shift){
        ArrayList<intRange> irst=new ArrayList();
        int len=irs2.size();
        intRange ir;
        for(int i=0;i<len;i++){
            ir=new intRange(irs2.get(i));
            ir.shiftRange(shift);
            irst.add(ir);
        }
        return overlapping(irs1,irst);
    }
    public static ArrayList<intRange> getOverlappingRanges(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        ArrayList <intRange> irs=new ArrayList();
        if(irs1.size()==0||irs2.size()==0) return irs;
        ArrayList<intRange> irst=new ArrayList();
        int currentIds[]=new int[2];
        int overlappingPair[]=new int[2];
        ArrayList<intRange>[] irsa=new ArrayList[2];
        irsa[0]=irs1;
        irsa[1]=irs2;
        currentIds[0]=0;
        currentIds[1]=0;
        int id,idI,idF;
        intRange ir;
        while(findFirstOverlappingRanges(irsa,currentIds,overlappingPair)){
            arrangeIrsa(irsa,overlappingPair,tailDescending);
            idI=overlappingPair[1];
            idF=getLastHeadleadingIndex(irsa[1],overlappingPair[1],irsa[0].get(overlappingPair[0]).nMax);
            ir=irsa[0].get(overlappingPair[0]);
            for(id=idI;id<=idF;id++){
                irs.add(ir.overlappedRange(irsa[1].get(id)));
            }
            overlappingPair[1]=idF;
            arrangeIrsa(irsa,overlappingPair,tailDescending);
            overlappingPair[1]+=1;
            if(overlappingPair[1]>=irsa[1].size()) break;
            currentIds[0]=overlappingPair[0];
            currentIds[1]=overlappingPair[1];
        }
        return irs;
    }

    public static boolean overlapping(ArrayList<intRange> irs1, ArrayList<intRange> irs2){
        if(irs1.size()==0||irs2.size()==0) return false;
        int currentIds[]=new int[2];
        int overlappingPair[]=new int[2];
        ArrayList<intRange>[] irsa=new ArrayList[2];

        irsa[0]=irs1;
        irsa[1]=irs2;
        currentIds[0]=0;
        currentIds[1]=0;
        if(findFirstOverlappingRanges(irsa,currentIds,overlappingPair)) return true;
        return false;
    }

    public static int getFirstTaillaggingIndex(ArrayList <intRange> irs, int nCurrentIndex, int nV){
        int len=irs.size();
        while(irs.get(nCurrentIndex).nMax<nV){
            nCurrentIndex++;
            if(nCurrentIndex>=len){
                return -1;//not found
            }
        }
        return nCurrentIndex;
    }
    public static int getLastHeadleadingIndex(ArrayList <intRange> irs, int nCurrentIndex, int nV){//nCurrentIndex has to satisfy the condition
        int len=irs.size();
        int id=-1;
        while(irs.get(nCurrentIndex).nMin<=nV){
            id=nCurrentIndex;
            nCurrentIndex++;
            if(nCurrentIndex>=len){
                return id;
            }
        }
        return id;
    }

    public static boolean findFirstOverlappingRanges(ArrayList<intRange> [] irsa, int[] currentIds, int[] idPair){
        ArrayList<intRange> o1=irsa[0];
        if(irsa[0].get(currentIds[0]).overlapped(irsa[1].get(currentIds[1]))){
            idPair[0]=currentIds[0];
            idPair[1]=currentIds[1];
            return true;
        }
        int len1=irsa[0].size();
        int len2=irsa[1].size();

        int id1,id2,idt;
        intRange irLead,irLag;
        boolean bFound=false;

        while(true) {
            arrangeIrsa(irsa,currentIds,headDescending);
            id1=currentIds[0];
            id2=currentIds[1];
            irLead=irsa[0].get(id1);
            id2=getFirstTaillaggingIndex(irsa[1],id2,irLead.nMin);
            if(id2<0) break;
            irLag=irsa[1].get(id2);
            if(irLead.overlapped(irLag)) {
                idPair[0]=id1;
                idPair[1]=id2;
                bFound=true;
                break;
            }
            id1++;
            if(id1>=irsa[0].size()) break;
            currentIds[0]=id1;//irLag is behind irLead
            currentIds[1]=id2;
        }
        if(!o1.equals(irsa[0])){
            swap(irsa,idPair);
        }
        return bFound;
    }

    public static void arrangeIrsa(ArrayList<intRange> [] irsa, int[] currentIds, int option){
        switch(option){
            case headAscending:
                if(irsa[0].get(currentIds[0]).nMin>irsa[1].get(currentIds[1]).nMin) swap(irsa, currentIds);
                break;
            case tailAscending:
                if(irsa[0].get(currentIds[0]).nMax>irsa[1].get(currentIds[1]).nMax) swap(irsa, currentIds);
                break;
            case headDescending:
                if(irsa[0].get(currentIds[0]).nMin<irsa[1].get(currentIds[1]).nMin) swap(irsa, currentIds);
                break;
            case tailDescending:
                if(irsa[0].get(currentIds[0]).nMax<irsa[1].get(currentIds[1]).nMax) swap(irsa, currentIds);
                break;
        }
    }
    public static void swap(ArrayList<intRange> [] irsa, int[] iDs){
        ArrayList<intRange> irs=irsa[0];
        int idt=iDs[0];
        irsa[0]=irsa[1];
        iDs[0]=iDs[1];
        irsa[1]=irs;
        iDs[1]=idt;
    }
}
