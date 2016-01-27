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
/**
 *
 *
 * @author Taihao
 */
public class buildUnmergedLifStacks_ implements PlugIn {
    int lows,columns,channels,w,h,z;
    ImagePlus umergedImages[], stitchedImg,sampleImg,imgO;
    String dir;
    ArrayList<String> imgFileNames;
    public void run(String Arg)
    {
        importFileList();
        sortImgFileNames();
        loadImgs();
    }
    void loadImgs(){
        int size=imgFileNames.size();
        int i;
        ImageStack is=new ImageStack(w,h);
        for(i=0;i<size;i++){
            ImagePlus impl=CommonMethods.importImage(dir+imgFileNames.get(i));
            is.addSlice("", impl.getProcessor());
        }
        imgO=new ImagePlus("",is);
        imgO.show();
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
