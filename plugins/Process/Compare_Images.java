/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Process;
import ij.ImagePlus;
import ij.WindowManager;
import utilities.CommonMethods;
import ij.gui.GenericDialog;
import java.util.ArrayList;
import utilities.io.PrintAssist;

/**
 *
 * @author Taihao
 */
public class Compare_Images implements ij.plugin.PlugIn {
    ArrayList<ImagePlus> images,openImages;
    ArrayList<Boolean> adjustMinimum;
    int m_nImages, m_nRows, m_nColumns, m_nSpacing,m_basePixel;
    ImagePlus impl;
    int standardId;
    int m_nRanges[][];
    ArrayList <Integer> m_refIds;
    public void run(String args){
        chooseImages();
        combineImages();
        impl.show();
    }
    void chooseImages(){
        getParameters();
        ArrayList<ImagePlus> imgs=openImages;
        int len=imgs.size();
        ArrayList <String> vcItems=new ArrayList();

        String defaultImg=WindowManager.getCurrentImage().getTitle();

        int i, id=0;
        String sTitle;
        for(i=0;i<len;i++){
            sTitle=imgs.get(i).getTitle();
            vcItems.add(sTitle);
            if(sTitle.contentEquals(defaultImg)) id=i;
        }

        String[] items=new String[len];
        for(i=0;i<len;i++){
            items[i]=vcItems.get(i);
        }

        String label="choose images to be added to the list";
        images=new ArrayList();
        adjustMinimum=new ArrayList();

        m_refIds=new ArrayList();

        standardId=-1;
        int refId;
        ImagePlus img;
        for(i=0;i<m_nImages;i++){
            refId=i;
            GenericDialog gd=new GenericDialog(label);
            gd.addChoice(PrintAssist.ToString_Order(i+1)+" image", items, items[i]);
            gd.addCheckbox("adjust minimum pixel value", false);
            gd.addCheckbox("Using as the standard", false);
            gd.addCheckbox("a dependent image", false);
            gd.showDialog();
            id=gd.getNextChoiceIndex();
            img=imgs.get(id);
            images.add(img);
            adjustMinimum.add(gd.getNextBoolean());
            if(gd.getNextBoolean()) standardId=i;
            if(gd.getNextBoolean()) {
                GenericDialog gd1=new GenericDialog("choose the reference image for "+img.getTitle());
                gd1.addChoice("Reference image:", items, items[i]);
                gd1.showDialog();
                refId=gd1.getNextChoiceIndex();
            }
            m_refIds.add(refId);
        }
    }
    void combineImages(){
        int num=images.size();
        int i,j,nType=images.get(0).getType();
        int wMax=Integer.MIN_VALUE,hMax=Integer.MIN_VALUE,w,h;
        ImagePlus img;
        for(i=0;i<num;i++){
            img=images.get(i);
            w=img.getWidth();
            h=img.getHeight();
            if(w>wMax) wMax=w;
            if(h>hMax) hMax=h;
        }
        w=m_nColumns*(wMax+m_nSpacing)-m_nSpacing;
        h=m_nRows*(wMax+m_nSpacing)-m_nSpacing;
        calBasePixelAndRanges();

        int nRange1[],nRange2[]=null;
        if(standardId>=0) {
            nRange2=m_nRanges[standardId];
            m_basePixel=nRange2[0];
        }
        impl=CommonMethods.getBlankImage(nType, w, h,m_basePixel);
        int x0,y0;
        int pixels[][];
        int r,c,refId;
        int adjust=0;


        for(i=0;i<num;i++){
            r=i/m_nColumns;
            c=i-r*m_nColumns;
            img=images.get(i);
            w=img.getWidth();
            h=img.getHeight();
            x0=c*(wMax+m_nSpacing);
            y0=r*(hMax+m_nSpacing);
            pixels=new int[h][w];
            CommonMethods.getPixelValue(img, img.getCurrentSlice(), pixels);
            if(adjustMinimum.get(i))
                adjust=m_basePixel;
            else
                adjust=0;
            if(standardId>=0){
                if(i!=standardId){
                    refId=m_refIds.get(i);
                    nRange1=m_nRanges[refId];
                    CommonMethods.scaleData(pixels,nRange1,nRange2);
                }
                CommonMethods.setPixels(impl, pixels,x0,y0,w,h);
            }else{
                CommonMethods.setPixels(impl, pixels, x0, y0, w, h,adjust);
            }
        }
    }
    void calBasePixelAndRanges(){
        int num=images.size();
        m_nRanges=new int[num][2];
        m_basePixel=Integer.MAX_VALUE;
        int pr[]=new int[2],pn;
        int anchors=0;
        for(int i=0;i<num;i++){
            CommonMethods.getPixelValueRange_Stack(images.get(i), m_nRanges[i]);
            if(adjustMinimum.get(i)) continue;
            pn=m_nRanges[i][0];
            if(pn<m_basePixel) m_basePixel=pn;
            anchors++;
        }
        if(anchors==0) m_basePixel=0;
    }
    void getParameters(){
        openImages=CommonMethods.getAllOpenImages();
        int num=openImages.size();
        int c=(int)Math.sqrt(num)+1;
        int r=num/c;
        if(num>r*c) r++;
        String label="number of images to compare";
        GenericDialog gd=new GenericDialog(label);
        gd.addNumericField("number of images to compare", num, 0);
        gd.addNumericField("number of rows", r, 0);
        gd.addNumericField("number of columns", c, 0);
        gd.addNumericField("number of pixels for spacing", 20, 0);
        gd.showDialog();
        if(gd.wasOKed()){
            m_nImages=(int) (gd.getNextNumber()+0.5);
            m_nRows=(int) (gd.getNextNumber()+0.5);
            m_nColumns=(int) (gd.getNextNumber()+0.5);
            m_nSpacing=(int) (gd.getNextNumber()+0.5);
        }else{
            m_nImages=0;
            m_nRows=0;
            m_nColumns=0;
            m_nSpacing=0;
        }
    }
}
