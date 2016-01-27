/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;

/**
 *
 * @author Taihao
 */
public class EdgeElementUnitNode {
    double pValue,left,right;
    int leftStart,leftEnd,rightStart,rightEnd,maxDeltaLine,numLLines,numRLines,ws,op,b1,b2,d1,d2;
//    PixelPositionNode center;
    public EdgeElementUnitNode(){
//        center=null;
    }

    public EdgeElementUnitNode(double p,double l, double r, int lstart, int lend, int rstart, int rend, int md, int w, int op,int b1, int b2, int d1, int d2){
        this.op=op;
        pValue=p;
        left=l;
        right=r;
        leftStart=lstart;
        leftEnd=lend;
        rightStart=rstart;
        rightEnd=rend;
        maxDeltaLine=md;
        ws=w;
        this.b1=b1;
        this.b2=b2;
        this.d1=d1;
        this.d2=d2;
    }
}
