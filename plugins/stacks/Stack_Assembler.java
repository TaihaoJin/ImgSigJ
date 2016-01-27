package stacks;
/**
 *
 * @author Taihao
 */
import ij.*;
import ij.process.*;
import ij.plugin.*;
import java.lang.*;
import ij.process.ByteProcessor;
import ij.io.Opener;
import ij.io.OpenDialog;
import java.util.ArrayList;
import java.io.*;
import java.util.StringTokenizer;

    class StackAssembleParameterNode{
        public String imageStack, transformationFile;
        public int[] stackDimention, scalingFactor, xyzShift;
        StackAssembleParameterNode(){
            stackDimention=new int[3];
            xyzShift=new int[3];
            scalingFactor=new int[3];
        }
    }
    
public class Stack_Assembler implements PlugIn {

    // --------------------------------------------------
    /** Splits the stack. */

    ArrayList <StackAssembleParameterNode> sapNodes;
    int[] sDimension;
    int numStacks;


    public void run(String arg) {

        assembleStacks();
    }

    protected void assembleStacks() {
        try{
            importParameterNodes();
        } catch (IOException e) {
            IJ.error(e+" when importing the assemble parameters");
        }
        calStackDimention();
        int W=sDimension[0],H=sDimension[1],N=sDimension[2];
        ImageStack is=new ImageStack(W,H);

        int size=sapNodes.size();
        int i,j;
        for(i=0;i<N;i++){
            ByteProcessor bp=new ByteProcessor(W,H);
            is.addSlice(i+"-th slice", bp);
        }
        ImagePlus impl=new ImagePlus("assembled stack",is);
        impl.show();
        for(i=0;i<size;i++){
            loadImages(is,i);
        }
    }
    void loadImages(ImageStack is, int i0){
        StackAssembleParameterNode sapNode=sapNodes.get(i0);
        Opener op=new Opener();
        ImagePlus impl=op.openImage(sapNode.imageStack);
        impl.show();
        int w=sapNode.stackDimention[0],h=sapNode.stackDimention[1],n=sapNode.stackDimention[2];
        int sx=sapNode.scalingFactor[0],sy=sapNode.scalingFactor[1],sn=sapNode.scalingFactor[2];
        int dx=sapNode.xyzShift[0],dy=sapNode.xyzShift[1],dn=sapNode.xyzShift[2];
//        int W=sDimension[0],H=sDimension[1],N=sDimension[2];
        int W=is.getWidth(),H=is.getHeight(),N=sDimension[2];
        int i,j,k,x,y,index;
        byte[] pixels0,pixels;
        int o,O;
        int pixel,pixel0;
        for(i=1;i<=n;i+=sn){
            index=i+dn;
            impl.setSlice(i);
            pixels0=(byte[])impl.getProcessor().getPixels();
            pixels=(byte[])is.getPixels(index);
            for(j=0;j<h;j+=sy){
                y=(j+dy)/sy;
                o=j*w;
                for(k=0;k<w;k+=sx){
                    x=(k+dx)/sx;
                    if(y*W+x>W*H-1){
                        i=i;
                    }
                    pixel=0xff&pixels[y*W+x];
                    pixel0=0xff&pixels0[o+k];
                    if(pixel0>pixel)pixels[y*W+x]=pixels0[o+k];
                }
            }
        }
        impl.close();
    }
    void importParameterNodes()throws IOException {
        sapNodes=new ArrayList <StackAssembleParameterNode>();


        OpenDialog od=new OpenDialog("importing the stack assemble parameter file","");
        String dir=od.getDirectory();
        String name=od.getFileName();

        File file=new File(dir+name);
        FileReader f=new FileReader(file);
        BufferedReader br=new BufferedReader(f);

        String s,st;
        s=br.readLine();
        StringTokenizer stk=new StringTokenizer(s," ",false);
        st=stk.nextToken();
        st=stk.nextToken();
        Integer Itg=new Integer(st);
        numStacks=Itg.intValue();
        int i,j;
        for(i=0;i<numStacks;i++){
            StackAssembleParameterNode sapNode=new StackAssembleParameterNode();
            s=br.readLine();
            while(s.length()==0){
               s=br.readLine();              
            }
            sapNode.imageStack=s;
            s=br.readLine();
            stk=new StringTokenizer(s," ",false);
            st=stk.nextToken();
            for(j=0;j<3;j++){
                Itg=new Integer(stk.nextToken());
                sapNode.stackDimention[j]=Itg.intValue();
            }

            st=stk.nextToken();
            for(j=0;j<3;j++){
                Itg=new Integer(stk.nextToken());
                sapNode.xyzShift[j]=Itg.intValue();
            }

            st=stk.nextToken();
            for(j=0;j<3;j++){
                Itg=new Integer(stk.nextToken());
                sapNode.scalingFactor[j]=Itg.intValue();
            }
            s=br.readLine();
            stk=new StringTokenizer(s," ",false);
            st=stk.nextToken();
            sapNode.transformationFile=stk.nextToken();
            sapNodes.add(sapNode);
        }
        br.close();
        f.close();
    }
    void calStackDimention(){
        int n=0,w=0,h=0,num;
        int numSlices=0;
        int size,i,j;
        size=sapNodes.size();
        sDimension=new int[3];
        for(i=0;i<3;i++){
            sDimension[i]=0;
        }
        StackAssembleParameterNode sapNode;
        for(i=0;i<size;i++){
            sapNode=sapNodes.get(i);
            for(j=0;j<3;j++){
                sDimension[j]=Math.max(sDimension[j], ((sapNode.stackDimention[j]-1+sapNode.xyzShift[j])/sapNode.scalingFactor[j]+1));
            }
        }
    }
    public static ImagePlus newGary8Image(String title,int w, int h){
        byte pixels[]=new byte[w*h];
        for(int i=0;i<w*h;i++){
            pixels[i]=0;
        }
        ByteProcessor cp=new ByteProcessor(w,h);
        ImagePlus impl=new ImagePlus(title, cp);
        return impl;
    }
    public static ImagePlus importImage(String title){
        OpenDialog od=new OpenDialog(title,"");
        String dir=od.getDirectory();
        String name=od.getFileName();
        Opener op=new Opener();
        ImagePlus impl=op.openImage(dir+name);
        return impl;
    }
}

