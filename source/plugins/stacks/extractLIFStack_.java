/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package stacks;
import java.io.File;
import ij.io.OpenDialog;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.io.Opener;
import utilities.CommonMethods;
import java.util.ArrayList;
import ij.ImageStack;
import java.awt.Color;
/**
 *
 *
 * @author Taihao
 */
public class extractLIFStack_ implements PlugIn {
    int rows,columns,channels,w,h,z;
    ImagePlus umergedImages, sampleImg;
    String dir;
    ArrayList<String> imgFileNames;
    public void run(String Arg)
    {
        importFileList();
        sortImgFileNames();
        loadImgs_extractedImage();
    }
    void loadImgs_extractedImage(){
        int size=imgFileNames.size();
        int i;
        columns=4;
        rows=4;
        int i1=1,i2=1,j1=3,j2=3;
        int h0=h/rows,w0=w/columns;
        int ni=i2-i1+1,nj=j2-j1+1;
        int h1=ni*h0,w1=w0*nj;
        ImageStack is=new ImageStack(w1,h1);
        int[]pixels,pixels0;
        ImagePlus impl,impl0;
        int i0=i1,j0=j1,oi,oj,o,index,index0;
        int x,y;
        for(i=0;i<size;i++){
            impl=CommonMethods.importImage(dir+imgFileNames.get(i));
            impl0=CommonMethods.newPlainRGBImage("", w1, h1, Color.white);
            pixels=(int[]) impl.getProcessor().getPixels();
            pixels0=(int[]) impl0.getProcessor().getPixels();
            for(y=0;y<h1;y++){
                o=y*w1;
                for(x=0;x<w1;x++){
                    index0=o+x;
                    index=w*(i0*h0+y)+j0*w0+x;
                    pixels0[index0]=pixels[index];
                }
            }
            is.addSlice("", impl0.getProcessor());
        }
        umergedImages=new ImagePlus("",is);
        umergedImages.show();
        sampleImg.show();
    }
    void importFileList(){
        OpenDialog od=new OpenDialog("Opening an image file","");
        imgFileNames=new ArrayList<String>();
        dir=od.getDirectory();
        String name=od.getFileName();
        Opener op=new Opener();
        sampleImg=op.openImage(dir+name);
        w=sampleImg.getWidth();
        h=sampleImg.getHeight();
        File f=new File(dir);
        String list[]=f.list();
        int len=list.length;
        int i,j;
//        ArrayList<String> imgFileNames=new ArrayList<String>();
        String path;
        for(i=0;i<len;i++){
            String fileName=list[i];
            path=dir+fileName;
            File ft=new File(path);
            if(!ft.isDirectory()){
                if(CommonMethods.getFileExtention(fileName).equalsIgnoreCase(".tif")) imgFileNames.add(fileName);
            }
        }
    }
    void sortImgFileNames(){
        channels=getNChannels();
        int size=imgFileNames.size();
        String name,t;
        int c,z,index;
        for(int i=0;i<size;i++){
            name=imgFileNames.get(i);
            z=getZ(name);
            c=getChannel(name);
            index=z*channels+c;
            t=new String(imgFileNames.get(index));
            imgFileNames.set(index, name);
            imgFileNames.set(i, t);
        }
    }
    int getZ(String imgFileName){
        int index =imgFileName.indexOf("_z");
        Integer z=new Integer(imgFileName.substring(index+2,index+4));
        return z.intValue();
    }
    int getChannel(String imgFileName){
        int index =imgFileName.indexOf("_ch");
        if(index<0) return 0;
        Integer ch=new Integer(imgFileName.substring(index+3,index+5));
        return ch.intValue();
    }
    int getNChannels(){
        int size=imgFileNames.size();
        boolean firstn=true,firstx=true;
        String name;
        int cn=0,cx=0,c;
        for(int i=0;i<size;i++){
            name=imgFileNames.get(i);
            c=getChannel(name);
            if(firstn){
                cn=c;
            }else{
                if(c<cn)cn=c;
            }
            if(firstx){
                cx=c;
            }else{
                if(c>cx)cx=c;
            }
        }
        return cx-cn+1;
    }
}
