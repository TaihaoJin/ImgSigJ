/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.statistics;
import utilities.CustomDataTypes.IntPair;
import utilities.CustomDataTypes.IntpairCouple;
import java.util.ArrayList;
import utilities.statistics.PolynomialRegression;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Gui.PlotWindowPlus;
import ij.gui.PlotWindow;
import java.awt.Color;
import utilities.CommonStatisticsMethods;
/**
 *
 * @author Taihao
 */
public class LineSegmentNode {
    public int start,end;
    public intRange cSmoothRangeL, cSmoothRangeR;
    public PolynomialRegression cPrL,cPrR;
    public double[] pdX, pdY;
    public double dStartX,dEndX,dStartY,dEndY;
    public LineSegmentNode(double[] pdX, double[] pdY, intRange irL, intRange irR, PolynomialRegression cPrL, PolynomialRegression cPrR){
        this.pdX=pdX;
        this.pdY=pdY;
        cSmoothRangeL=irL;
        cSmoothRangeR=irR;
        this.cPrL=cPrL;
        this.cPrR=cPrR;
        start=irL.getMin();
        end=irR.getMax();
        dStartX=pdX[start];
        dEndX=pdX[end];
        if(cPrL.isValid()){
            dStartY=cPrL.predict(dStartX);
            dEndY=cPrR.predict(dEndX);
        }else{            
            dStartY=cPrL.getMeanY();
            dEndY=cPrR.getMeanY();
        }
    }

    public LineSegmentNode(double[] pdX, double[] pdY, boolean[] pbSelected, int indexI, int indexF, int minLen, int maxLen, int nOrder){
        start=indexI;
        end=indexF;
        this.pdX=pdX;
        this.pdY=pdY;
        dEndX=pdX[end];
        dStartX=pdX[start];
        int len=indexF-indexI+1,iI=indexI,iF;
        ArrayList<Integer> nvSelected=new ArrayList();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSelected, new intRange(start,end), nvSelected);
        len=nvSelected.size();
        if(len>0)
            iF=nvSelected.get(len-1);
        else {
            iF=iI;
            nvSelected.add(iI);
            len=1;
        }
        maxLen=Math.min(maxLen,iF-iI+1);
        
        cPrL=CommonStatisticsMethods.getOptimalStartingRegression(pdX, pdY, pbSelected,indexI, minLen, maxLen, nOrder);
        if(cPrL.nDataSize<1){
            cPrL=cPrL;
        }
        if(cPrL.nDataSize>0)
            iF=nvSelected.get(cPrL.nDataSize-1);
        else
            iF=iI;
        cSmoothRangeL=new intRange(iI,iF);
        
        cPrR=CommonStatisticsMethods.getOptimalEndingRegression(pdX, pdY, pbSelected, indexF, minLen, maxLen, nOrder);
        iF=indexF;
        if(cPrR.nDataSize>0)
            iI=nvSelected.get(len-cPrR.getDataSize());
        else
            iI=iF;
        cSmoothRangeR=new intRange(iI,iF);
        dStartY=cPrL.predict(dStartX);
        dEndY=cPrR.predict(dEndX);
    }
    public LineSegmentNode(double[] pdX, double[] pdY, int iI, int iF, PolynomialLineFittingSegmentNode segL, PolynomialLineFittingSegmentNode segR){
        start=iI;
        end=iF;
        this.pdX=pdX;
        this.pdY=pdY;
        dEndX=pdX[end];
        dStartX=pdX[start];
        
        cPrL=segL.cPr;
        cSmoothRangeL=new intRange(segL.nStart,segL.nEnd);
        
        cPrR=segR.cPr;
        cSmoothRangeR=new intRange(segR.nStart,segR.nEnd);
        dStartY=cPrL.predict(dStartX);
        dEndY=cPrR.predict(dEndX);
    }
    public LineSegmentNode(double[] pdX, double[] pdY, boolean[] pbSelected, double[] pdSD, int indexI, int indexF, int minLen, int maxLen, int nOrder){
        start=indexI;
        end=indexF;
        this.pdX=pdX;
        this.pdY=pdY;
        dEndX=pdX[end];
        dStartX=pdX[start];
        int len=indexF-indexI+1;
        int iI=indexI,iF;
        ArrayList<Integer> nvSelected=new ArrayList();
        CommonStatisticsMethods.getSelectedPositionsWithinRange(pbSelected, new intRange(start,end), nvSelected);
        len=nvSelected.size();
        if(len>0)
            iF=nvSelected.get(len-1);
        else
            iF=iI;
        maxLen=Math.min(maxLen,iF-iI+1);
        
        cPrL=CommonStatisticsMethods.getOptimalStartingRegression(pdX, pdY, pbSelected,pdSD,indexI, minLen, maxLen, nOrder);
        if(cPrL.nDataSize>0)
            iF=nvSelected.get(cPrL.nDataSize-1);
        else
            iF=iI;
        cSmoothRangeL=new intRange(iI,iF);
        
        cPrR=CommonStatisticsMethods.getOptimalEndingRegression(pdX, pdY, pbSelected, pdSD, indexF, minLen, maxLen, nOrder);
        
        iF=indexF;
        if(cPrR.nDataSize>0)
            iI=nvSelected.get(len-cPrR.getDataSize());
        else
            iI=iF;
        cSmoothRangeR=new intRange(iI,iF);
        
        dStartY=cPrL.predict(dStartX);
        dEndY=cPrR.predict(dEndX);
    }
    public void segRegressionNodeL(PolynomialRegression cPr,int iI, int iF){
        cPrL=cPr;        
        cSmoothRangeL=new intRange(iI,iF);
    }
    public void segRegressionNodeR(PolynomialRegression cPr,int iI, int iF){
        cPrR=cPr;        
        cSmoothRangeR=new intRange(iI,iF);
    }
    public int display(PlotWindowPlus pw, double xL, double yL){
        double[] pdXC=new double[2], pdYC=new double[2];
        pdXC[0]=xL;
        pdYC[0]=yL;
        pdXC[1]=dStartX;
        pdYC[1]=dStartY;
        
        int endL=cSmoothRangeL.getMax();//discrepancy, index vs. value of x 12801
        int startR=cSmoothRangeR.getMin();
        if(startR<endL){
            endL=(endL+startR)/2;
            startR=endL+1;
        }
        int len=endL-start+end-startR+2,i;
        double[] pdXT=new double[len], pdYT=new double[len];
        int position=0;
        double x;
        for(i=start;i<=endL;i++){
            x=pdX[i];
            if(position>=len){
                continue;
            }
            pdXT[position]=x;
            if(cPrL.isValid())
                pdYT[position]=cPrL.predict(x);
            else
                pdYT[position]=dStartY;
            position++;
        }
        for(i=startR;i<=end;i++){
            x=pdX[i];
            if(position>=len){
                continue;
            }
            pdXT[position]=x;
            if(cPrR.isValid())
                pdYT[position]=cPrR.predict(x);
            else
                pdYT[position]=dEndY;
            position++;
        }
        pw.addPlot("Segment", pdXC, pdYC, 2, PlotWindow.LINE, Color.RED,false);
        pw.addPlot("Segment", pdXT, pdYT, 2, PlotWindow.LINE, Color.BLUE,true);
        return 1;
    }
}
