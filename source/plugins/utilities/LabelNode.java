/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.awt.Font;
import utilities.Comparable;
import utilities.CustomDataTypes.intRange;
import BrainSimulations.BrainSimulationGraphicalObject;
import ij.ImagePlus;
import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */

public class LabelNode{
    public Color color;
    public int corner[];
    public int shift[];
    public int minShift[];
    String label;
    public Font font;
    public int stringWidth;
    public int stringHeight;
    public boolean overSized;
    public LabelNode(){
        corner=new int[2];
        shift=new int[2];
        minShift=new int[2];
        overSized=false;
        for(int i=0;i<2;i++){
            shift[i]=0;
            minShift[i]=0;
            corner[i]=0;
        }
    }
    public LabelNode(ImagePlus impl, BrainSimulationGraphicalObject go, Font font, Color color){
        this();
        int anchor[]=new int[2],x,y;
        go.getAnchor(anchor);
        y=anchor[0];
        x=anchor[1];
        Double prob=new Double(go.getProbType());
        String s=prob.toString();
        int len=Math.min(6, s.length()-1);
        s=prob.toString().substring(0,len);
        corner[0]=anchor[0];
        corner[1]=anchor[1];
        label=go.getType().Abbreviation+" ("+s+")";
        impl.getProcessor().setFont(font);
        stringHeight=font.getSize();
        stringWidth=impl.getProcessor().getStringWidth(s);
        this.color=color;
        this.font=font;
    }
    public boolean smaller(LabelNode a){
        if(this.corner[0]<a.corner[0])return true;
        else if(this.corner[0]>a.corner[0]) return false;
        else if(this.corner[1]<a.corner[1]) return true;
        return false;
    }
    public boolean greater(LabelNode a){
        if(this.corner[0]>a.corner[0])return true;
        else if(this.corner[0]<a.corner[0]) return false;
        else if(this.corner[1]>a.corner[1]) return true;
        return false;
    }
    public boolean xOverlapped(LabelNode a){
        intRange xr1=new intRange(this.corner[1]+this.shift[1],this.corner[1]+this.shift[1]+this.stringWidth);
        intRange xr2=new intRange(a.corner[1]+a.shift[1],a.corner[1]+a.shift[1]+a.stringWidth);
        if(xr1.overlapped(xr2)) return true;
        return false;
    }    
    public boolean yOverlapped(LabelNode a){
        intRange yr1=new intRange(this.corner[0]+this.shift[0],this.corner[0]+this.shift[0]+this.stringHeight);
        intRange yr2=new intRange(a.corner[0]+a.shift[0],a.corner[0]+a.shift[0]+a.stringHeight);
        if(yr1.overlapped(yr2)) return true;
        return false;
    }
    public boolean overLapped(LabelNode a){
        if(this.xOverlapped(a)&&this.yOverlapped(a)) return true;
        return false;
    }
}
