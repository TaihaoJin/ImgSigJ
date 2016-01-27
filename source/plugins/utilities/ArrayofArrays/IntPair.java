/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.ArrayofArrays;

/**
 *
 * @author Taihao
 */
public class IntPair {
        public int x;
        public int y;
        public IntPair(){
            this.x=0;
            this.y=0;
        }
        public IntPair(int x, int y){
            this();
            this.x=x;
            this.y=y;
        }
        public void setXY(int x,int y){            
            this.x=x;
            this.y=y;
        }
        public void setXY(IntPair ip){            
            this.x=ip.x;
            this.y=ip.y;
        }
        public IntPair(IntPair ip){
            this(ip.x,ip.y);
        }
        public boolean equals(IntPair ip){
            if(ip.x==x&&ip.y==y) return true;
            return false;
        }
}
