/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//This is an extended version of EdgeElementNode
package ImageAnalysis;
import utilities.ArrayofArrays.IntPair;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class EdgeElementNode_E {
    int debugginPaus;
    boolean nullified;
    public int dx,dy;
    public int numExts,type,n1,n2;
    public double pValue;
    public int numEPs;
    public double height, base;
    ArrayList<PixelPositionNode> leftStart, leftEnd, rightStart, rightEnd,edgePoints,edgeLine;
    public EdgeElementNode_E(){
        numExts=-1;
        pValue=-1.;
        edgePoints=new ArrayList<PixelPositionNode>();
        leftStart=new ArrayList<PixelPositionNode>();
        leftEnd=new ArrayList<PixelPositionNode>();
        rightStart=new ArrayList<PixelPositionNode>();
        rightEnd=new ArrayList<PixelPositionNode>();
        edgePoints=new ArrayList<PixelPositionNode>();
    }
    public EdgeElementNode_E(EdgeElement_FRR el){
        this();
        el.completeEE();
        pValue=el.getPValue();
        numExts=el.getExts();
        type=el.getType();
        el.getCenters();
        edgePoints=el.getEdgePoints();
//        edgePoints=el.maxCollectiveBlockDeltaEdgePoints();
        if(edgePoints!=null){
            numEPs=edgePoints.size();
        }else{
            numEPs=0;
        }

        height=el.getHeight();
        base=el.getBase();
        dx=el.eD[1];
        dy=el.eD[0];
    }
    public EdgeElementNode_E(EdgeElementNode_E een){
        this();//TODO: need to expand it.
        pValue=een.pValue;
        numExts=een.numExts;
        type=een.type;
        int size=een.edgePoints.size();
        for(int i=0;i<size;i++){
            edgePoints.add(new PixelPositionNode(een.edgePoints.get(i)));
        }
        height=een.height;
        base=een.base;
    }
    public void removeEdgePoint(int y, int x){
        PixelPositionNode ip;
        for(int i=0;i<numEPs;i++){
            ip=edgePoints.get(i);
            if(ip.x==x&&ip.y==y){
                edgePoints.remove(i);
                break;
            }
        }
        numEPs--;
    }
    public boolean parallel(EdgeElementNode_E een){
        if(height*een.height>0) return true;
        return false;
    }
    /*
    public int consolidateEdgePoints(EdgeElementNode_E een){
        if(!parallel(een)) return -1;
            ArrayList <PixelPositionNode> ipa1=edgePoints, ipa2=een.edgePoints;
            if(een.base<base){
                ipa1=een.edgePoints;
                ipa2=edgePoints;
            }
            int size1=ipa1.size(), size2=ipa2.size();
            PixelPositionNode ip1i=ipa1.get(0), ip1f=ipa1.get(size1-1), ip2i=ipa2.get(0),ip2f=ipa2.get(size2-1);
            int xPrime, yPrime, xPrime1i=ip1i.x*dx+ip1i.y*dy,xPrime1f=ip1f.x*dx+ip1f.y*dy,xPrime2i=ip2i.x*dx+ip2i.y*dy,xPrime2f=ip2f.x*dx+ip2f.y*dy;
            int yPrime1=ip1i.y*dx-ip1i.x*dy,yPrime2=ip2i.y*dx-ip2i.x*dy;
            ArrayList <IntPair> ipa=new ArrayList <IntPair>();
            int x,y,dxy2=dx*dx+dy*dy;
            PixelPositionNode ip1, ip2;
            if(xPrime2i<xPrime1i){
                ip1=ipa2.get(0);
                ipa.add(new IntPair(ip1));
                x=(xPrime1i*dx-yPrime2*dy)/dxy2;
                y=(xPrime1i*dy+yPrime2*dx)/dxy2;
                ip2=new IntPair(x,y);
                buildConnection(ip1,ip2,ipa);
                ipa.add(new IntPair(ip2));
                buildConnection(ip2,ipa1.get(0),ipa);
            }
            int size=ipa1.size();
            for(int i=0;i<size;i++){
                ipa.add(new IntPair(ipa1.get(i)));
            }
            if(xPrime2f>xPrime1f){
                ip1=ipa1.get(size-1);
                x=(xPrime1f*dx-yPrime2*dy)/dxy2;
                y=(xPrime1f*dy+yPrime2*dx)/dxy2;
                ip2=new IntPair(x,y);
                buildConnection(ip1,ip2,ipa);
                ipa.add(new IntPair(ip2));
                ip1=ip2;
                size=ipa2.size();
                ip2=ipa2.get(size-1);
                buildConnection(ip1,ip2,ipa);
                ipa.add(new IntPair(ip2));
            }
        return 1;
    }*/

    static public void buildConnection(IntPair ip1, IntPair ip2, ArrayList <IntPair> ipa){
        //This method add all grid points that are necessary to connect the two points (ip1 and ip2) into the
        //array ipa, but without including ip1 or ip2. All the adjacent grid points must be either horizontally (having
        //the same y coordinates) or vertically (having the same x coordinates) connected.
        int x1=ip1.x, x2=ip2.x, y1=ip1.y, y2=ip2.y;
        int x=x1,y;
        int delta;
        if(x1==x2){
            delta=1;
            if(y2<y1) delta=-1;
            y=y1+delta;
            while(delta*(y2-y)>=0){
                ipa.add(new IntPair(x1,y));
                y+=delta;
            }
        }else if(y1==y2){
            delta=1;
            if(x2<x1) delta=-1;
            x=x1+delta;
            while(delta*(x2-x)>=0){
                ipa.add(new IntPair(x,y1));
                x+=delta;
            }
        }else{
            double deltaX=x2-x1;
            double deltaY=y2-y1;
            double k=deltaY/deltaX;
            double xd,yd;
            int xSign=1, ySign=1;
            if(deltaX<0) xSign=-1;
            if(deltaY<0) ySign=-1;
            x=x1;
            int yi=y1,yf=y2;
            xd=x1;
            while(xSign*(x2-x)>0){
                xd=x+0.5*xSign;
                yd=y1+k*(xd-x1);
                yf=(int)yd;
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line only includes pixels that are traversed
                //(at lest two points are on the line) by the center line.
                y=yi+ySign;
                while(ySign*(yf-y)>=0){
                    ipa.add(new IntPair(x,y));
                    y+=ySign;
                }
                x+=xSign;
                yi=yf;/*
                if(ySign*(yd-(int)yd)<0.5||ySign*(yd-(int)yd)>0.5){
                    yi=yf;
                }
                else{//this is for the  case of (yd-(int)yd)==0.5
                    yi=yf+ySign;
                }*/
                y=yi;
                xd=x;
                yd=y1+k*(xd-x1);
                yf=(int)yd;
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line only includes pixels that are traversed
                while(ySign*(yf-y)>=0){
                    ipa.add(new IntPair(x,y));
                    y+=ySign;
                }
            }
        }
        int size=ipa.size();
        if(size>0){
            ipa.remove(size-1);
        }
    }
}
