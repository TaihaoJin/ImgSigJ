package utilities.Gui;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//http://terai.xrea.jp/Swing/AutoScroll.html
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.event.*;

public class DragMoverListener extends MouseAdapter implements HierarchyListener{
    private static final int SPEED = 2;
    private final Cursor dc;
    private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final javax.swing.Timer scroller;
    private final JComponent label;
    private Point startPt = new Point();
    private Point move    = new Point();

    public DragMoverListener(JComponent comp) {
        this.label = comp;
        this.dc = comp.getCursor();
        this.scroller = new javax.swing.Timer(5, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JViewport vport = (JViewport)label.getParent();
                Point vp = vport.getViewPosition(); //= SwingUtilities.convertPoint(vport,0,0,label);
                vp.translate(-move.x, -move.y);
                label.scrollRectToVisible(new Rectangle(vp, vport.getSize())); //vport.setViewPosition(vp);
            }
        });
    }
    @Override public void hierarchyChanged(HierarchyEvent e) {
        JComponent c = (JComponent)e.getSource();
        if((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED)!=0 && !c.isDisplayable()) {
            scroller.stop();
        }
    }
    @Override public void mouseDragged(MouseEvent e) {
        scroller.stop();
        Point pt = e.getPoint();
        move.setLocation(SPEED*(pt.x-startPt.x), SPEED*(pt.y-startPt.y));
        startPt.setLocation(pt);
        scroller.start();
    }
    @Override public void mousePressed(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(hc); //label.setCursor(hc);
        startPt.setLocation(e.getPoint());
        scroller.stop();
    }
    @Override public void mouseReleased(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc); //label.setCursor(dc);
    }
    @Override public void mouseExited(MouseEvent e) {
        ((JComponent)e.getSource()).setCursor(dc); //label.setCursor(dc);
        move.setLocation(0, 0);
        scroller.stop();
    }
}
