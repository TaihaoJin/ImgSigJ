/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import BrainSimulations.*;
import BrainSimulations.DataClasses.*;
import utilities.ArrayofArrays.*;
import java.util.ArrayList;
/**
 *
 * @author Taihao
 */
public class StructureFrame {
    ArrayList <SubstructureEssential> substructures;
    ArrayList <SpacialObject> m_SpacialObjects;
    float center[];
    intRange m_intRange[];
    FloatRange m_fRanges[];
    float m_fScale;
    boolean m_frameBuilt;
    int size;
    
    public StructureFrame(){
        m_frameBuilt=false;
    }  
    public boolean frameBuilt(){
        return m_frameBuilt;
    }
    public StructureFrame(ArrayList <SubstructureEssential> substructures0){
        substructures=substructures0;
        size=substructures.size();
        m_fScale=substructures.get(0).getScale();
        m_frameBuilt=true;
        setRanges();
        setCenter();
    }
    
    public void setCenter(){
        center=new float[3];
        for(int i=0;i<3;i++){
            center[i]=0.5f*(m_fRanges[i].getMin()+m_fRanges[i].getMax());
        }
    }
    
    public void setRanges(){
        int nSize=substructures.size();
        m_fRanges=new FloatRange[3];
        for(int i=0;i<3;i++){
            m_fRanges[i]=new FloatRange();
            for(int j=0;j<nSize;j++){
                m_fRanges[i].mergeRange(substructures.get(j).getRangeF(i));            
            }
        }
    }
    
    public FloatRange getRangeF(int oi){
        return m_fRanges[oi];
    } 
    
    public FloatRange[] getRangesF(){
        return m_fRanges;
    }
    
    public float[] getCenter(){
        return center;
    }
    public ArrayList<SubstructureEssential> getSubstructures(){
        return substructures;
    }
    public float getScale(){
        return m_fScale;
    }
}
