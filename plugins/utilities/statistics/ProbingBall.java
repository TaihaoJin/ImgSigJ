/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.awt.Color;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;
import utilities.CommonMethods;
import utilities.CustomDataTypes.DoubleRange;
import utilities.RunningWindowRankingKeeper;
import utilities.statistics.MeanSem1;
import utilities.CommonGuiMethods;
import utilities.Gui.PlotWindowPlus;
import ij.gui.PlotWindow;
import utilities.Non_LinearFitting.LineEnveloper;
import utilities.CommonGeometryMethods;

/**
 *
 * @author Taihao
 */
public class ProbingBall {
    public static final int Upward=-1,Downward=1;//rolling downward means the ball is on the upper (or regarded as left side when roling along x axis) of the line.
    
    double[] m_pdX,m_pdY,m_pdProbeTrail,m_pdProbeTrailUpward,m_pdProbeTrailDown;
    double m_dRx,m_dRy;
    int m_nRanking,m_direction;
    int[] m_pnContactingPositionsL,m_pnContactingPositionsR;
    ArrayList<Integer> m_nvProbeContactingPoints,m_nvProbeContactingPointsUpward,m_nvProbeContactingPointsDownward;
    ArrayList<Integer> m_nvHighRankingPoints;
    ArrayList<Integer> m_nvVerticalContackingPoints,m_nvVerticalContackingPointsUpward,m_nvVerticalContackingPointsDownward;
    ArrayList<Integer> m_nvBallContactingPointsUpward,m_nvBallContactingPointsDownward;
    public ProbingBall(double[] pdX, double[] pdY, double Rx, double Ry, int nRanking){
        m_pdX=pdX;
        m_pdY=pdY;
        m_dRx=Rx;
        m_dRy=Ry;
        m_nRanking=nRanking;
        if(m_dRy<0) calDefaultYScale();
        init();
        buildProbeTrail(0);
        rebuildProbTrail();
        buildProbeTrail(1);
        rebuildProbTrail();
//        buildRollingBallContactingPoints();//need some improvement, therefore not in use in current version. //12728
    }
    double getHeightAdjust(double deltaX){
        double deltaY=0;
        double dSDelX=deltaX/m_dRx;//scaled delta x
        double dSDelY=1-Math.sqrt(1-dSDelX*dSDelX);
        deltaY=dSDelY*m_dRy;
//        deltaY=0;//12d05
        return deltaY;
    }
    void rebuildProbTrail(){
        int len=m_nvVerticalContackingPoints.size(),i,j,l,r,le,re,sign;
        l=0;
        double xL,xR,yL,yR,x,y,y0,Y;
        
        sign=1;
        if(m_direction==Upward) sign=-1;
        
        xL=m_pdX[0];
        yL=m_pdProbeTrail[0];
        for(i=0;i<=len;i++){
            if(i<len)
                r=m_nvVerticalContackingPoints.get(i);
            else
                r=m_pdProbeTrail.length-1;
            
            le=CommonStatisticsMethods.getNextRisingPosition(m_pdProbeTrail, l, sign, 1);
            re=CommonStatisticsMethods.getNextRisingPosition(m_pdProbeTrail, r, sign, -1);
            if(re<=le||le<0||re<0){
                l=r;
                continue;
            }
            xL=m_pdX[le];
            yL=m_pdProbeTrail[le];
            xR=m_pdX[re];
            yR=m_pdProbeTrail[re];
            for(j=le+1;j<re;j++){
                x=m_pdX[j];
                y=CommonMethods.getLinearIntoplation(xL, yL, xR, yR, x);
                y0=m_pdProbeTrail[j];
                Y=m_pdY[j];
                if(sign*(y-y0)>0) y=y0;
                if(sign*(y-Y)<0) y=Y;
                m_pdProbeTrail[j]=y;
            }
            l=r;
        }
    }
    void init(){
        int len=m_pdX.length;
        m_pdProbeTrailUpward=new double[len];
        m_pdProbeTrailDown=new double[len];
        m_pnContactingPositionsL=new int[len];
        m_pnContactingPositionsR=new int[len];
        m_nvProbeContactingPointsUpward=new ArrayList();
        m_nvProbeContactingPointsDownward=new ArrayList();
        m_nvBallContactingPointsUpward=new ArrayList();
        m_nvBallContactingPointsDownward=new ArrayList();
        m_nvVerticalContackingPointsUpward=new ArrayList();
        m_nvVerticalContackingPointsDownward=new ArrayList();
    }
    void buildProbeTrail(int dir){
        if(dir==0){ 
            m_direction=Downward;
            m_nvProbeContactingPoints=m_nvProbeContactingPointsDownward;
            m_pdProbeTrail=m_pdProbeTrailDown;
            m_nvVerticalContackingPoints=m_nvVerticalContackingPointsDownward;
            
        } else {
            m_direction=Upward;
            m_nvProbeContactingPoints=m_nvProbeContactingPointsUpward;
            m_pdProbeTrail=m_pdProbeTrailUpward;
            m_nvVerticalContackingPoints=m_nvVerticalContackingPointsUpward;
        }
        
        int i,j,index,len=m_pdX.length;
        int[] indexes=new int[len];
        double[] pdAdjustedY=new double[len];
        int left=0,right=0;
        double dl,dr,xc,xt,dn;

        for(i=0;i<len;i++){
            xc=m_pdX[i];
            dl=m_pdX[left];
            while(xc-dl>m_dRx){
                left++;
                dl=m_pdX[left];
            }
            dr=m_pdX[right];
            while(dr-xc<m_dRx&&right<len){
                right++;
                if(right>=len) {
                    right=len-1;
                    break;
                }
                dr=m_pdX[right];
            }
            if(dr-xc>m_dRx) right--;//left and right limites are determined

            for(j=left;j<=i;j++){
                xt=m_pdX[j];
                pdAdjustedY[j]=m_pdY[j]-getHeightAdjust(xc-xt)*m_direction;
                indexes[j]=j;
            }
            CommonStatisticsMethods.PartialSort(pdAdjustedY, indexes, m_nRanking, -1*m_direction, left, i, 1);
            index=indexes[left+m_nRanking];
            m_pnContactingPositionsL[i]=index;
            if(i==index) m_nvVerticalContackingPoints.add(i);
            dl=m_pdY[index]-getHeightAdjust(xc-m_pdX[index])*m_direction;
            for(j=i;j<=right;j++){
                xt=m_pdX[j];
                pdAdjustedY[j]=m_pdY[j]-getHeightAdjust(xc-xt)*m_direction;
                indexes[j]=j;
            }
            CommonStatisticsMethods.PartialSort(pdAdjustedY, indexes, m_nRanking, -1*m_direction, i, right, 1);
            index=indexes[Math.min(len-1,i+m_nRanking)];
            m_pnContactingPositionsR[i]=index;
            if(i==index) m_nvVerticalContackingPoints.add(i);
            dr=m_pdY[index]-getHeightAdjust(xc-m_pdX[index])*m_direction;
            dn=dl;
            if(m_direction*(dr-dl)<0) dn=dr;
            m_pdProbeTrail[i]=dn;
        }
        
        //remove redundancy and sort m_nvVerticalContackingPoints
        boolean[] pbt=new boolean[len];
        CommonStatisticsMethods.setElements(pbt,false);
        for(i=0;i<m_nvVerticalContackingPoints.size();i++){
            pbt[m_nvVerticalContackingPoints.get(i)]=true;
        }
        m_nvVerticalContackingPoints.clear();
        for(i=0;i<len;i++){
            if(pbt[i])m_nvVerticalContackingPoints.add(i);
        }
        
        if(dir==0){ 
            getProbeContactingPoints(m_nvProbeContactingPointsDownward,m_nvBallContactingPointsDownward);
            m_nvProbeContactingPoints=m_nvProbeContactingPointsDownward;
        } else {
            getProbeContactingPoints(m_nvProbeContactingPointsUpward,m_nvBallContactingPointsUpward);
            m_nvProbeContactingPoints=m_nvProbeContactingPointsUpward;
        }
//        displayProbeContactingPoints();
    }

    void getProbeContactingPoints(ArrayList<Integer> nvProbeContactingPoints, ArrayList<Integer> nvBallContactingPoints){

        int len=m_pdX.length,i,j,index;
        boolean[] pbLC=new boolean[len],pbRC=new boolean[len];
        CommonStatisticsMethods.setElements(pbLC, false);
        CommonStatisticsMethods.setElements(pbRC, false);
        for(i=0;i<len;i++){
            index=m_pnContactingPositionsL[i];
            pbLC[index]=true;
            index=m_pnContactingPositionsR[i];
            pbRC[index]=true;
        }

        for(i=0;i<len;i++){
            if(pbLC[i]||pbRC[i]) nvProbeContactingPoints.add(i);
            if(pbLC[i]&&pbRC[i]) nvBallContactingPoints.add(i);
        }
    }

    public void getProbContactingPoints(ArrayList<Double> dvX, ArrayList<Double> dvY,int direction){
        ArrayList<Integer> nvPoints;
        if(direction==Downward)
            nvPoints=m_nvProbeContactingPointsDownward;
        else
            nvPoints=m_nvProbeContactingPointsUpward;
        
        dvX.clear();
        dvY.clear();
        int i,index,len=nvPoints.size();
        for(i=0;i<len;i++){
            index=nvPoints.get(i);
            dvX.add(m_pdX[index]);
            dvY.add(m_pdY[index]);
        }
    }

    public void displayProbeContactingPoints(){
        int len=m_pdX.length,i,j,index;
        boolean[] pbLC=new boolean[len],pbRC=new boolean[len];
        CommonStatisticsMethods.setElements(pbLC, false);
        CommonStatisticsMethods.setElements(pbRC, false);
        for(i=0;i<len;i++){
            index=m_pnContactingPositionsL[i];
            pbLC[index]=true;
            index=m_pnContactingPositionsR[i];
            pbRC[index]=true;
        }

        ArrayList<Double> dvLx=new ArrayList(), dvRx=new ArrayList(), dvLRx=new ArrayList();
        ArrayList<Double> dvLy=new ArrayList(), dvRy=new ArrayList(), dvLRy=new ArrayList();

        for(i=0;i<len;i++){
            if(pbLC[i]){
                if(pbRC[i]){
                    dvLRx.add(m_pdX[i]);
                    dvLRy.add(m_pdY[i]);
                }else{
                    dvLx.add(m_pdX[i]);
                    dvLy.add(m_pdY[i]);
                }
            }else if(pbRC[i]){
                if(pbLC[i]){
                    dvLRx.add(m_pdX[i]);
                    dvLRy.add(m_pdY[i]);
                }else{
                    dvRx.add(m_pdX[i]);
                    dvRy.add(m_pdY[i]);
                }
            }
        }
        PlotWindowPlus pw=CommonGuiMethods.displayNewPlotWindowPlus("Boll Contacting Points", m_pdX, m_pdY, 1, PlotWindow.LINE, Color.red);
        pw.addPlot("Left", dvLx, dvLy, 3, PlotWindow.CIRCLE, Color.red, false);
        pw.addPlot("Right", dvRx, dvRy, 3, PlotWindow.CIRCLE, Color.blue, false);
        pw.addPlot("LR", dvLRx, dvLRy, 3, PlotWindow.CIRCLE, Color.orange, true);
    }

    public void displayProbeCircle(PlotWindowPlus pw){
        int points=100,i;
        DoubleRange xRange=CommonStatisticsMethods.getRange(m_pdX), yRange=CommonStatisticsMethods.getRange(m_pdY);
        double[] pdxc=new double[2*points+1],pdyc=new double [2*points+1];
        double dx,dy;
        double xc=xRange.getMidpoint(),yc=yRange.getMidpoint();
        double delta=2./points;
        pdxc[0]=xc-m_dRx;
        pdyc[0]=yc;
        for(i=0;i<points;i++){
            dx=-1+delta*i;
            dy=Math.sqrt(1-dx*dx);
            pdxc[i+1]=xc+dx*m_dRx;
            pdyc[i+1]=yc+dy*m_dRy;
            pdxc[i+points+1]=xc-dx*m_dRx;
            pdyc[i+points+1]=yc-dy*m_dRy;
        }
        pw.addPlot("circle", pdxc, pdyc, 1, 2, Color.CYAN, true);
    }

    void calDefaultYScale(){
        double[] pdDelta=CommonStatisticsMethods.getDeltaArray(m_pdY, null, 2);
        MeanSem1 ms=new MeanSem1();
        ArrayList<Integer> outliars=new ArrayList();
        CommonStatisticsMethods.findOutliars(pdDelta, 0.01, ms, outliars);
        m_dRy=1.*ms.getSD();
    }
    
    public double[] getProbeTrail(int direction){
        if(direction==Downward)
            return m_pdProbeTrailDown;
        else
           return m_pdProbeTrailUpward; 
    }
    
    public void  buildRollingBallContactingPoints(){//Not a very good way to pick higher points
        if(m_nvProbeContactingPoints==null) m_nvProbeContactingPoints=new ArrayList();
        if(m_nvHighRankingPoints==null) m_nvHighRankingPoints=new ArrayList();
        m_nvProbeContactingPoints.clear();
        m_nvHighRankingPoints.clear();

        int len=m_pdX.length,left=0,right=1,index,i,nRank;
        int firstOutRangePoint,firstUpperPoint,contactingPoint;
        double xl,xr,yl,yr,dx,dy,angle;
        double[] center=new double[4],ref=new double[2];
        ArrayList<Double> dvAngles=new ArrayList();
        ArrayList<Integer> nvIndexes=new ArrayList();

        int cIndexX,cIndexY;
        if(m_direction==1){//ball is on upper side
            cIndexX=0;
            cIndexY=1;
            ref[0]=1;
            ref[1]=0;
        }else{
            cIndexX=2;
            cIndexY=3;
            ref[0]=-1;
            ref[1]=0;
        }

        while(right<len){
            if(left==5){
                left=left;
            }
            xl=m_pdX[left];
            yl=m_pdY[left];
            xr=m_pdX[right];
            while(xr-xl<2*m_dRx&&right<len-1){
                right++;
                if(right>=len) break;
                xr=m_pdX[right];
            }
            if(right<len){
                right--;
                xr=m_pdX[right];
            }

            firstOutRangePoint=-1;
            contactingPoint=-1;
            dvAngles.clear();
            nvIndexes.clear();

            for(index=left+1;index<=right;index++){
                xr=m_pdX[index];
                yr=m_pdY[index];
                dx=(xr-xl)/m_dRx;
                dy=(yr-yl)/m_dRy;

                if(dx*dx+dy*dy>4){
                    if(dy>0){
                        angle=CommonGeometryMethods.getAngle(ref[0], ref[1], 0, 0, dx, dy);
                        dvAngles.add(angle);
                    }else if(firstOutRangePoint<0)firstOutRangePoint=index;
                }else{
                    nvIndexes.add(index);
                    CommonGeometryMethods.getCircleCenter(0, 0, dx, dy, 1, center);
                    double dist1=CommonGeometryMethods.dist2(0, 0, center[0], center[1]),dist2=CommonGeometryMethods.dist2(dx, dy, center[0], center[1]);
                    double dist3=CommonGeometryMethods.dist2(0, 0, center[2], center[3]),dist4=CommonGeometryMethods.dist2(dx, dy, center[2], center[3]);
                    angle=CommonGeometryMethods.getAngle(ref[0], ref[1], 0, 0, center[cIndexX], center[cIndexY]);
                    dvAngles.add(angle);
                }
            }

            if(firstOutRangePoint>=0&&dvAngles.size()<=m_nRanking){
                contactingPoint=firstOutRangePoint;
                m_nvProbeContactingPoints.add(contactingPoint);
                left=contactingPoint;
                right=left+1;
            }else{
                nRank=Math.min(m_nRanking, nvIndexes.size()-1);
                if(nRank<0) break;
                CommonStatisticsMethods.PartialSort(dvAngles, nvIndexes, nRank, -1);
                for(i=0;i<nRank;i++){
                    m_nvHighRankingPoints.add(nvIndexes.get(i));
                }
                contactingPoint=nvIndexes.get(nRank);
                m_nvProbeContactingPoints.add(contactingPoint);
                left=contactingPoint;
                right=left+1;
            }
        }

        ArrayList<Integer> nvLine=new ArrayList();
        ArrayList<Double> dvX=new ArrayList(),dvY=new ArrayList();
        len=nvLine.size();
        len=m_nvProbeContactingPoints.size();
        for(i=0;i<len;i++){
            index=m_nvProbeContactingPoints.get(i);
            dvX.add(m_pdX[index]);
            dvY.add(m_pdY[index]);
        }
        PlotWindowPlus pw2=CommonGuiMethods.displayNewPlotWindowPlus("EnvLine Whell", m_pdX, m_pdY, 1, PlotWindow.LINE, Color.red);
        pw2.addPlot("Contacting Points", dvX, dvY, 3, PlotWindow.CIRCLE, Color.red, false);

        int points=100;
        DoubleRange xRange=CommonStatisticsMethods.getRange(m_pdX), yRange=CommonStatisticsMethods.getRange(m_pdY);
        double[] pdxc=new double[2*points+1],pdyc=new double [2*points+1];
        double xc=xRange.getMidpoint(),yc=yRange.getMidpoint();
        double delta=2./points;
        pdxc[0]=xc-m_dRx;
        pdyc[0]=yc;
        for(i=0;i<points;i++){
            dx=-1+delta*i;
            dy=Math.sqrt(1-dx*dx);
            pdxc[i+1]=xc+dx*m_dRx;
            pdyc[i+1]=yc+dy*m_dRy;
            pdxc[i+points+1]=xc-dx*m_dRx;
            pdyc[i+points+1]=yc-dy*m_dRy;
        }
        pw2.addPlot("circle", pdxc, pdyc, 1, 2, Color.CYAN, true);
    }
    
    public ArrayList <Integer> getProbeContactingPositions(int direction){
        if(direction==Downward)
            return m_nvProbeContactingPointsDownward;
        else
            return m_nvProbeContactingPointsUpward;                
    }
    
    public ArrayList <Integer> getBallContactingPositions(int direction){
        if(direction==Downward)
            return m_nvBallContactingPointsDownward;
        else
            return m_nvBallContactingPointsUpward;                
    }
    
    public static ArrayList<Integer> getProbeContactingPoints(double[] pdX, double[] pdY, double Rx, double Ry, int nRanking,int direction){
        ProbingBall pb=new ProbingBall(pdX,pdY,Rx,Ry,nRanking);
        return pb.getProbeContactingPositions(direction);
    }
}
