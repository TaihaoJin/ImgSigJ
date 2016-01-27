/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import java.util.ArrayList;
import java.awt.Point;

/**
 *
 * @author Taihao
 */
public class LineConnector {
    public static ArrayList <Point> getConnection (Point p1, Point p2){
         //This method add all grid points that are necessary to connect the two points (ip1 and ip2) into the
        //array ipa, but without including ip1 or ip2. All the adjacent grid points must be either horizontally (having
        //the same y coordinates) or vertically (having the same x coordinates) connected.

        //This methods builds a pixel line connecting two geometric dots p1=(x1,y1) and p2=(x2,y2).
        //A pixel line is a collection of pixels traversed by the geometric line segment p1p2.
        //A pixel is regarded as a geometric unit square and centered at
        //a geometric dot whose x and y coordinates are integers.
        ArrayList <Point> points=new ArrayList();
        int x1=p1.x,x2=p2.x,y1=p1.y, y2=p2.y;
        int x=x1,y;
        int delta;
        if(x1==x2){
            delta=1;
            if(y2<y1) delta=-1;
            y=y1+delta;
            while(delta*(y2-y)>=0){
                points.add(new Point(x1,y));
                y+=delta;
            }
        }else if(y1==y2){
            delta=1;
            if(x2<x1) delta=-1;
            x=x1+delta;
            while(delta*(x2-x)>=0){
                points.add(new Point(x,y1));
                x+=delta;
            }
        }else{
            int index=0;
            Point pt;
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
            for(x=x1;xSign*x<xSign*x2;x+=xSign){
                xd=x+0.5*xSign;
                yd=y1+k*(xd-x1);//(xd,yd) is the coordinates of the geometric point where the geometric line cross
                //the verticle line X=x+0.5*xSign;
                yf=(int)yd; //yf is the y coordinate of the pixel containing the point(xd,yd).
                if(ySign*(yd-yf)>0.5) yf+=ySign; //this line is different from the above line in that the pixel line
                //only includes pixels that are traversed
                //(at lest two points are on the line) by the center line.
                for(y=yi+ySign;y*ySign<=yf*ySign;y+=ySign){
                    pt=new Point(x,y);
                    points.add(pt);
                }
                yi=yf;
                xd=x+xSign;
                yd=y1+k*(xd-x1);//(xd,yd) is now the coordinates of the geometric point where the geometric line cross
                //the verticle line X=x+0.5*xSign;
                yf=(int)yd;//yf is the y coordinate of the pixel containing the point(xd,yd).
                if(ySign*(yd-yf)>0.5) yf+=ySign;
                for(y=yi;y*ySign<=yf*ySign;y+=ySign){
                    pt=new Point(x+xSign,y);
                    points.add(pt);
                }
                yi=yf;
            }
        }
        int len=points.size();
        if(len>0) points.remove(len-1);
        return points;
    }
}
