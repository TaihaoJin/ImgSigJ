/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;

/**
 *
 * @author Taihao
 */
public class PixelPositionNode {
    public int x,y,phase,lineNumber,op,dx,dy;//op is the orthogonal position. The pixel at the corner where the values x*dx and y*dy are minimum has zero op, all others have positive op.
    public PixelPositionNode(int x, int y, int phase, int lineNumber, int op, int dx, int dy){
        this.x=x;
        this.y=y;
        this.phase=phase;
        this.lineNumber=lineNumber;
        this.op=op;
        this.dx=dx;
        this.dy=dy;
    }
    public PixelPositionNode(){
    }
    public PixelPositionNode(PixelPositionNode ppn){
        update(ppn);
    }
    public void update(PixelPositionNode ppn){
        x=ppn.x;
        y=ppn.y;
        phase=ppn.phase;
        lineNumber=ppn.lineNumber;
        op=ppn.op;
        dx=ppn.dx;
        dy=ppn.dy;
    }
    public void setPhase(int phase){
        this.phase=phase;
    }
    public void setLineNumber(int lineNumber){
        this.lineNumber=lineNumber;
    }
    public void setOP(int op){
        this.op=op;
    }
    public boolean equals_LO(PixelPositionNode ppn){
        return (ppn.lineNumber==lineNumber&&ppn.op==op);
    }
    public boolean equals_XY(PixelPositionNode ppn){
        return (ppn.x==x&&ppn.y==y);
    }
}
