/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.WindowManager;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.plugin.PlugIn;

/**
 *
 * @author Taihao
 */
public class MinimumDiff_MedianFilter implements PlugIn{
    public void run(String arg){
        ImagePlus impl=WindowManager.getCurrentImage();
        if(impl.getType()==CommonMethods.GRAY8){
            int w=impl.getWidth();
            int h=impl.getHeight();
            byte[] pixels=(byte[])impl.getProcessor().getPixels();
            for(int i=0;i<50;i++){
                byteMDMedianFilter(pixels, w, h);
                double per=(double)i/(double)50;
                CommonMethods.showStatusAndProgress("iteration "+i+" of "+50, per);
            }
            impl.getProcessor().setPixels(pixels);
        }
    }
    public void byteMDMedianFilter(byte [] pixels, int w, int h){
        byte[][] p0=new byte[h][w];
        int i,j,o,k,l;
        for(i=0;i<h;i++){
            o=i*w;
            for(j=0;j<w;j++){
                p0[i][j]=pixels[o+j];
            }
        }
        int p,p1,p2,dp;
        xy xy1=new xy(), xy2=new xy();
        for(i=1;i<h-1;i++){
            double per=(double)i/(double)h;
            CommonMethods.showStatusAndProgress("MDMedianFilter "+i+" of "+h, per);
            o=i*w;
            for(j=1;j<w-1;j++){
                if(j==1040&&i==890){
                    i=i;
                }
                p=p0[i][j];
                p1=250;
                p2=250;
                for(k=i-1;k<=i+1;k++){
                    for(l=j-1;l<=j+1;l++){
                        if(k==i&&l==j)continue;
                        dp=Math.abs(p-p0[k][l]);
                        if(dp<p1){
                            p2=p1;
                            xy2.x=xy1.x;
                            xy2.y=xy1.y;
                            p1=dp;
                            xy1.x=l;
                            xy1.y=k;
                        }else if(dp<p2){                                                        
                            p2=dp;
                            xy2.x=l;
                            xy2.y=k;
                        }
                    }
                }
                pixels[o+j]=p0[xy2.y][xy2.x];
            }
        }
    }
    class xy{
        public int x;
        public int y;
        public xy(){
            x=0;
            y=0;
        }
        public xy(int x0,int y0){
            x=x0;
            y=y0;
        }
    }
}
