/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class LineFittingSegmentGroup extends PolynomialLineFittingSegmentNode{
    public ArrayList<PolynomialLineFittingSegmentNode> cvSegments;//assuming these are the consecutive segments of the same line, in order
    public ArrayList<Double> dvCrossPoints;
    int nSegments;
    public LineFittingSegmentGroup(){
        cvSegments=new ArrayList();
        dvCrossPoints=new ArrayList();
        nSegments=0;
    }
    public void addSegment(PolynomialLineFittingSegmentNode seg){
        if(nSegments==0) {
            nStart=seg.nStart;
            nEnd=seg.nEnd;
            pbSelected=seg.pbSelected;
            pdX=seg.pdX;
            pdY=seg.pdY;
            pdSD=seg.pdSD;
        }else{
            dvCrossPoints.add(getCrossPoint(cvSegments.get(nSegments-1),seg));
            nEnd=seg.nEnd;
        }
        cvSegments.add(seg);
        nSegments++;
    }
    public double getCrossPoint(PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR){
        double[] pdX=segL.pdX;
        int start=segL.nStart,end=segR.nEnd,i=start,limit=pdX.length-1;
        double delta0=segL.predict(pdX[i])-segR.predict(pdX[i]),delta;
        double sign=1;
        if(delta0>0) sign=-1;
        i++;
        delta=segL.predict(pdX[i])-segR.predict(pdX[i]);
        while(delta0*delta>0){
            i++;
            if(i>limit)
                break;
            delta0=delta;
            delta=segL.predict(pdX[i])-segR.predict(pdX[i]);
        }
//        delta0=sign*delta0;
        if(i>limit){
            return 0.5*(pdX[segL.nEnd]+pdX[segR.nStart]);
        }
        double x0=pdX[i-1],x1=pdX[i],x=x0,delta1=delta;

        double tol=0.01*segL.pdSD[start],tolX=0.01*Math.abs(pdX[0]-pdX[pdX.length-1])/(pdX.length+1);
        while(Math.abs(delta1-delta0)>tol&&Math.abs(x1-x0)>tolX){
            x=0.5*(x0+x1);
            delta=segL.predict(x)-segR.predict(x);
            if(sign*delta>0){
                x1=x;
                delta1=delta;
            }else{
                x0=x;
                delta0=delta;
            }
        }
        return x;
    }
    public double predict(double x){
//        int index=getSegmentIndex(x);
//        return cvSegments.get(index).predict(x);
        PolynomialLineFittingSegmentNode seg=getClosestSegment(x);
        return seg.predict(x);
    }
    public int getSegmentIndex(double x){
        double left=Double.NEGATIVE_INFINITY,right;
        int i,len=dvCrossPoints.size();
        if(len==0) return 0;
        for(i=0;i<len;i++){
            right=dvCrossPoints.get(i);
            if(x>left&&x<=right) return i;
            left=right;
        }
        return len-1;
    }
    public PolynomialLineFittingSegmentNode getClosestSegment(double x){
        int i,len=cvSegments.size(), it=0;
        double dn=Double.POSITIVE_INFINITY,dist;
        for(i=0;i<len;i++){
            dist=cvSegments.get(i).getDistance(x);
            if(dist<dn) {
                dn=dist;
                it=i;
            }
        }
        return cvSegments.get(it);
    }
    public boolean isEmpty(){
            return cvSegments.size()==0;
    }
    public ArrayList<Integer> getSelectedIndexes(){
        ArrayList<Integer> indexes=new ArrayList(),nvT;
        int i,j,len=cvSegments.size();
        for(i=0;i<len;i++){
            nvT=cvSegments.get(i).getSelectedIndexes();
            for(j=0;j<nvT.size();j++){
                indexes.add(nvT.get(j));
            }
        }
        return indexes;
    }
}
