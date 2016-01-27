/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Process;
import ij.ImagePlus;
import utilities.CommonMethods;
import ij.process.EllipseFitter;
import ij.ImagePlus;
import ij.gui.Roi;
import javax.swing.JTextArea;
import utilities.Gui.OneScrollPaneFrame;
import utilities.io.PrintAssist;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.JFrame;

/**
 *
 * @author Taihao
 */
public class Fit_Ellipse implements ij.plugin.PlugIn {
    public void run(String args){
        EllipseFitter ef=new EllipseFitter();
        ImagePlus impl=CommonMethods.getCurrentImage();
        impl.getProcessor().setMask(CommonMethods.makeMask(impl.getRoi()));
        ef.fit(impl.getProcessor(),null);
        ef.drawEllipse(impl.getProcessor());
        impl.draw();
        JTextArea ta=new JTextArea("");
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        String newLine="\n";
        
        String st="xc= "+PrintAssist.ToString(ef.xCenter, 3);
        ta.setText(st);
        st="    yc= "+PrintAssist.ToString(ef.yCenter, 3)+newLine;
        ta.append(st);
        
        st="major ="+PrintAssist.ToString(ef.major, 3);
        ta.append(st);
        
        st="    minor ="+PrintAssist.ToString(ef.minor, 3)+"    Ratio ="+PrintAssist.ToString(ef.major/ef.minor, 3)+ newLine;
        ta.append(st);
        st="angel= "+PrintAssist.ToString(ef.angle, 3)+newLine;
        ta.append(st);
        
        JViewport jvp=new JViewport();
        jvp.setView(ta);
        
        JScrollPane jsp=new JScrollPane();
        jsp.setViewport(jvp);
//        OneScrollPaneFrame.main("Ellipse Parameters", jsp);
        //Create and set up the window.         
        JFrame frame = new JFrame("TextDemo");         
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);           //Add contents to the window.         
        frame.add(jsp);           //Display the window.         
        frame.pack();         
        frame.setVisible(true);         
        
    }
}
