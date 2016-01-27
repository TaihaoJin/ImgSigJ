/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import java.awt.Point;
import ij.gui.PlotWindow;
import ij.ImagePlus;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import utilities.CustomDataTypes.DoubleRange;
import utilities.CommonGuiMethods;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import ij.gui.Roi;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 * @author Taihao
 */
public class PlotWindowTuner implements MouseMotionListener, MouseListener, ActionListener {
    PlotWindow pw;
    ImagePlus impl;
    Point highlighter;
    Color color;
    public PlotWindowTuner(PlotWindow pw){
        this.pw=pw;
        impl=pw.getImagePlus();
        impl.getWindow().getCanvas().addMouseListener(this);
        impl.getWindow().getCanvas().addMouseMotionListener(this);
        impl.getWindow().getCanvas().addPaintListener(this);
    }
    public void mouseClicked(MouseEvent me){
        Point cursor=CommonGuiMethods.getCursorLocation_ImageCoordinates(impl);
    }
    public double getX(){
        String coor=pw.getCoordinate();
        StringTokenizer stk=new StringTokenizer(coor,"X=Y(,)");
        if(!stk.hasMoreElements())return Double.NaN;
        double x=Double.parseDouble(stk.nextToken());
        return x;
    }
    public double getY(){
        String coor=pw.getCoordinate();
        StringTokenizer stk=new StringTokenizer(coor,"X=Y(,)");
        if(!stk.hasMoreElements())return Double.NaN;
        stk.nextToken();
        if(!stk.hasMoreElements())return Double.NaN;
        double y=Double.parseDouble(stk.nextToken());
        return y;
    }
    public void refresh(){
        impl.draw();
    }
    public void highlighCurrentDataPoint(double x, double y){
        refresh();
        Point position=pw.getPixelCoordinates(x, y);
        highlightPoint(position, Color.green);
    }
    public void highlightPoint(Point pt, Color c){
        highlighter=pt;
        color =c;
        CommonGuiMethods.highlightPoint(impl, pt, c);
    }
    public void mouseMoved(MouseEvent me){
//        highlighCurrentDataPoint();
    }
    public void mouseDragged(MouseEvent me){}
    public void mouseExited(MouseEvent me){}
    public void mouseEntered(MouseEvent me){}
    public void mousePressed(MouseEvent me){}
    public void mouseReleased(MouseEvent me){}
    public void actionPerformed(ActionEvent ae){
        if(highlighter!=null){
            CommonGuiMethods.highlightPoint(impl, highlighter, color);
        }
    }
}
