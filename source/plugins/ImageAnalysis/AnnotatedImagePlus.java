/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ij.ImagePlus;
import java.util.ArrayList;
import utilities.CommonMethods;
import ij.ImageStack;
import ij.process.ImageProcessor;
import java.util.Hashtable;
/**
 *
 * @author Taihao
 */
public class AnnotatedImagePlus extends ImagePlus{
    public static final int Annotated=105;
//    ImagePlus faceImage;
    double[][] m_ppdOriginalValues;
    int m_nCurrentSlice,originalType;
    String sDescription;
    ArrayList<ImageAnnotationNode>[] m_pcvAnnotations;
    Hashtable m_cAdditionalNotes;
    int w,h;
    public AnnotatedImagePlus(ImagePlus impl){
        super(impl.getTitle(),impl.getStack());
        m_cAdditionalNotes=new Hashtable();
        originalType=impl.getType();
    }
    public void storeNote(Object o, String key){
        m_cAdditionalNotes.put(key, o);
    }
    public Object retrieveNote(String key){
        return m_cAdditionalNotes.get(key);
    }
    public Hashtable getNotes(){
        return m_cAdditionalNotes;
    }
    public void setDescription(String description){
        sDescription=new String(description);
    }
    public String getDescription(){
        return sDescription;
    }
    public double[] getOriginalValues(int slice){
        return m_ppdOriginalValues[slice-1];
    }
    public void setOriginalValues(double[][] ppdV){
        m_ppdOriginalValues=ppdV;
    }
    public void setAnnotations(ArrayList<ImageAnnotationNode>[] pdvAnnotations){
        m_pcvAnnotations=pdvAnnotations;
    }
    public void setOriginalValues(double[] pdV, int slice){
        m_ppdOriginalValues[slice-1]=pdV;
    }
    public double getOriginalValue(int x, int y){
        return m_ppdOriginalValues[m_nCurrentSlice][y*w+x];
    }
    public boolean annotated(int x, int y, int slice, String type){
        return (getAnnotations(x,y,slice,type).size()>0);
    }
    public ArrayList<ImageAnnotationNode> getAnnotations(int x, int y, int slice,String type){
        ArrayList<ImageAnnotationNode> annotations=new ArrayList();
        ArrayList<ImageAnnotationNode> annotationst=m_pcvAnnotations[slice-1];
        ImageAnnotationNode annotation;
        int len=annotationst.size(),i;
        for(i=0;i<len;i++){
            annotation=annotationst.get(i);
            if(annotation.annotated(x, y, type)) annotations.add(annotation);
        }
        return annotations;
    }
    public String getAnnotaionsAsString(int x, int y, int slice, String type){
        ArrayList<ImageAnnotationNode> annotations=getAnnotations(x,y,slice,type);
        String st="";
        int i,len=annotations.size();
        for(i=0;i<len;i++){
            if(i>0) st+="    ";
            st+=annotations.get(i).getAnnotationAsString(x, y);
        }
        return st;
    }
    public int getType(){
        return Annotated;
    }
    public boolean hasNote(String key){
        return m_cAdditionalNotes.containsKey(key);
    }
    public int getOriginalImageType(){
        return originalType;
    }
}
