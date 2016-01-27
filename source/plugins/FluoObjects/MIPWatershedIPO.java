/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package FluoObjects;
import ImageAnalysis.PixelTrailHandler;
import java.util.ArrayList;
import java.awt.Point;

/**
 *
 * @author Taihao
 */
public class MIPWatershedIPO {
    PixelTrailHandler m_cInnerPart;
    PixelTrailHandler m_cContour;
    public int m_nPixelHeight;
    public int m_nDistToContour2;
    public MIPWatershedIPO(PixelTrailHandler innerPart, PixelTrailHandler contour){
        m_cInnerPart=innerPart;
        m_cContour=contour;
        build();
    }

    public void drawIPO(int[] pixels, int w, int h, int pixel){
        m_cContour.drawPeakPoint(pixels, w, h, pixel);
        m_cInnerPart.drawPeakPoint(pixels, w, h, pixel);
    }

    void build(){
        m_nPixelHeight=m_cInnerPart.m_nPeakValue-(int)m_cContour.m_dAve;
        int nSize=m_cContour.m_vcTrail.size();
        Point p0=m_cInnerPart.m_cPeakPoint;
        Point p;
        int nn=0,dx,dy,dist2;
        for(int i=0;i<nSize;i++){
            p=m_cContour.m_vcTrail.get(i);
            dx=p.x-p0.x;
            dy=p.y-p0.y;
            dist2=dx*dx+dy*dy;
            if(i==0){
                nn=dist2;
            }else{
                if(dist2<nn) nn=dist2;
            }
        }
        m_nDistToContour2=nn;
    }
}
