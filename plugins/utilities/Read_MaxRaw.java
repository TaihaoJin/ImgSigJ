/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import ij.ImagePlus;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import ij.plugin.PlugIn;
import ij.io.FileOpener;
import ij.io.OpenDialog;
import java.io.FileNotFoundException;
import java.io.IOException;
import ij.IJ;
import ij.ImageStack;
import ij.process.ShortProcessor;
import ij.process.ByteProcessor;
import java.io.DataInputStream;
import utilities.io.ByteConverter;
import utilities.io.FileAssist;


/**
 *
 * @author Taihao
 */
public class Read_MaxRaw implements PlugIn{
    public void run(String arg){
        importMaxRaw();
    }
    void importMaxRaw(){
        int w,h,bytesPerPixel;
        bytesPerPixel=2;
        w=256;
        h=256;
        OpenDialog od=new OpenDialog("input a raw image without meta data","");
        String path=od.getDefaultDirectory()+od.getFileName();
        File f=new File(path);
        FileInputStream fi=null;
        try{fi=new FileInputStream(f);}
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }

        DataInputStream in= new DataInputStream(fi);
        BufferedInputStream bi=new BufferedInputStream(fi);
        int numBytes=0;
        try{
            numBytes=bi.available();
        }
        catch (IOException e){

        }

        int pixelsPerSlice=w*h;
        int bytesPerSlice=pixelsPerSlice*bytesPerPixel;
        int nSlices=numBytes/bytesPerSlice;
        
        int dataSize=nSlices*pixelsPerSlice*bytesPerPixel;

        byte[] rb=new byte[dataSize];
        int actualSize=0;
        try{actualSize=bi.read(rb);}
        catch (IOException e){
            IJ.error("cound not read the data");
        }
        if(actualSize!=dataSize){
            IJ.error("data sizes do not match when reading raw image");
        }

        ImageStack is=new ImageStack(w,h);
        int i,offset=0;
        short pixels[];
        for(i=0;i<nSlices;i++){
            ShortProcessor sp=new ShortProcessor(w,h);
            pixels=(short[])sp.getPixels();
            ByteConverter.getShortArray_LittleEndian(rb, offset, bytesPerSlice, pixels, 0, pixelsPerSlice);
            is.addSlice("", sp);
            offset+=bytesPerSlice;
        }

        try{bi.close();}
        catch (IOException e){

        }
        try{fi.close();}
        catch (IOException e){

        }

        try{fi=new FileInputStream(f);}
        catch(FileNotFoundException e){
            IJ.error("File Not Found");
        }

        ImagePlus impl=new ImagePlus("the raw file",is);
        impl.setTitle(FileAssist.getFileName(path));
        impl.show();
    }
}
