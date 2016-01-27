package io;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import ij.*;
import ij.io.*;
import ij.plugin.PlugIn;

public class OpenSIF_ implements PlugIn {

	public void run(String arg) {
		OpenDialog od = new OpenDialog("Open SPE...", arg);
		String file = od.getFileName();
		if (file == null) return;
		String directory = od.getDirectory();
		ImagePlus imp = open(directory, file);
		if (imp != null ) {
			imp.show();
		} else {
			IJ.showMessage("Open SPE...", "Failed.");
		}
	}

	public static ImagePlus open(String directory, String file) {
	    int MAXBYTES = 1000;
	    int i, index, offset=0, k;
	    byte b;
	    int Xdim = 512, Ydim = 512, Zdim = 1;
	    int Xbin = 1, Ybin = 1;
	    int mod;
	    int height, width, stacksize;
	    int keepreading;
	    boolean showInfoMessage = true;
	    
		File f = new File(directory, file);
		try {
			FileInputStream in = new FileInputStream(f);
			int FileHeaderLength = 19;
			offset = 0;
			// skip through the first section of the header. 
			for (i = 0; i < FileHeaderLength; i++){
				while((keepreading = in.read())!= 10){
				offset++;
				}
				offset++;
			}
			
			// now read out the size information in the image header
			//line 20
			byte[] byte_buffer = new byte[MAXBYTES];
			index = 0;
			int byteOrMinus1;
			for (i = 0; i<10; i++){
				
				// collect until you hit a space or new line
				while((byteOrMinus1 = in.read()) != 32 && byteOrMinus1 !=10){
					b = (byte) byteOrMinus1;
					offset++;
					byte_buffer[index]=b;
					index++;
				}
				// digest if needed
				if (i == 3){
					String Xdim_s = new String(byte_buffer, 0,index);
					Integer Xdim_i = new Integer(Integer.parseInt(Xdim_s));
					Xdim = Xdim_i.intValue();
				}
				if (i==4){
					String Ydim_s = new String(byte_buffer, 0,index);
					Integer Ydim_i = new Integer(Integer.parseInt(Ydim_s));
					Ydim = Ydim_i.intValue();
				}
				 if (i == 6){
				 	String Zdim_s = new String(byte_buffer, 0,index);
					Integer Zdim_i = new Integer(Integer.parseInt(Zdim_s));
					Zdim = Zdim_i.intValue();
				}	
				
				index = 0;
				offset++;
			}
			
			//line 21
			index = 0;
			for (i = 0; i<8; i++){
			// collect until you hit a space or new line
				while((byteOrMinus1 = in.read()) != 32 && byteOrMinus1 != 10){
					b = (byte) byteOrMinus1;
					offset++;
					byte_buffer[index]=b;
					index++;
				}
				// digest if needed
				if (i == 5){
					String Xbin_s = new String(byte_buffer, 0,index);
					Integer Xbin_i = new Integer(Integer.parseInt(Xbin_s));
					Xbin = Xbin_i.intValue();
				}
				if (i == 6){
					String Ybin_s = new String(byte_buffer, 0,index);
					Integer Ybin_i = new Integer(Integer.parseInt(Ybin_s));
					Ybin = Ybin_i.intValue();
				}
				index = 0;
				offset++;
			}
			
			mod = Xdim%Xbin;
			height = (Xdim-mod)/Xbin;
			mod = Ydim%Ybin;
			width = (Ydim-mod)/Ybin;
			stacksize = Zdim;
			
			/*
			The rest of the file is a time stamp for the frame followed 
			new line. 
			*/
			offset = offset+2*Zdim;	
			if (showInfoMessage){
				IJ.showMessage("Image height is "+height+".\nImage width is "+width+".\nStacksize is "+stacksize+".\nOffset is "+offset+".");	
			}
			
			/* 
			Now that the size and the offset of the image is known
			we can open the image/stack.
			*/

			FileInfo fi = new FileInfo();
			fi.directory = directory;
			fi.fileFormat = fi.RAW;
			fi.fileName = file;
			// GRAY32 is the file type for .SIF's 
			fi.fileType = 4;
			fi.gapBetweenImages = 0;
			fi.height = height;
			fi.intelByteOrder = true;
			fi.nImages = Zdim;
			fi.offset = offset;
			fi.width = width;
			FileOpener fo = new FileOpener(fi);
			ImagePlus imp = fo.open(false);
			IJ.showStatus("");
			return imp;
		} catch (IOException e) {
			IJ.error("An error occured reading the file.\n \n" + e);
			IJ.showStatus("");
			return null;
		}
	}
}
