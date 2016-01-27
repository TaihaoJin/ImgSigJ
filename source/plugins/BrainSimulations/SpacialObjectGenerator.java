/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;
import java.util.ArrayList;
import BrainSimulations.*;
import BrainSimulations.DataClasses.*;
import BrainSimulations.SpacialGrids;
import ij.IJ;
 /*
 * @author Taihao
 */
public class SpacialObjectGenerator {
    ArrayList <SpacialObject> spacialObjects;
    StructureFrame structureFrame;
    FloatRange m_fRanges[];
    SpacialGrids m_spGrids;
    int size;
    public SpacialObjectGenerator(){
        
    }
    public void SpacialObjectGenerator(int size0){
        size=size0;
    }
    public SpacialObjectGenerator(StructureFrame sFrame){
        structureFrame=sFrame;
        m_fRanges=structureFrame.getRangesF();
    }
    
    public void setGrids(SpacialGrids spGrids){
        m_spGrids=spGrids;
    }
    
    public SpacialObject newSphere(){
        float center[]=new float[3];
        float r=20f+(float)(20*Math.random());
        SphereSimulator ss;
        
//        for(int i=0;i<3;i++){
//           center[i]=m_fRanges[i].getMin()+(float)Math.random()*m_fRanges[i].getExpand();
 //       }
        do {
            do {        
                for(int i=0;i<3;i++){
                   center[i]=m_fRanges[i].getMin()+(float)Math.random()*m_fRanges[i].getExpand();
                }
            }while(m_spGrids.contains(center)<0);

            ss=new SphereSimulator(center,r);
        }while(!ss.withinFrame(m_spGrids));
        
        return new SphereSimulator(center,r);
    }
    
    public SpacialObject newEllipsoid(){
        float center[]=new float[3];
        float r[]=new float[3];
        float angle[]=new float[3];
        for(int i=0;i<3;i++){
            r[i]=200f+(float)(100*Math.random());
            angle[i]=(float)(180.*Math.random());
        }
        EllipsoidSimulator es;
        do {
            do {        
                for(int i=0;i<3;i++){
                   center[i]=m_fRanges[i].getMin()+(float)Math.random()*m_fRanges[i].getExpand();
//                   center[i]=(float)(m_fRanges[i].getMin()+0.5*m_fRanges[i].getExpand());
                }
            }while(m_spGrids.contains(center)<0);
            es=new EllipsoidSimulator(center,r,angle);
        }while(!es.withinFrame(m_spGrids));
        
        return es;
    }
    
    public SpacialObject theEllipsoid(){
        float center[]=new float[3];
        float r[]=new float[3];
        float angle[]=new float[3];
        for(int i=0;i<3;i++){
            r[i]=700*(3-i);
            center[i]=(float)(m_fRanges[i].getMin()+0.5*m_fRanges[i].getExpand());
        }
        r[0]=2500.f;
        r[1]=100.f;
        r[2]=400.f;
        angle[0]=0;
        angle[1]=60;
        angle[2]=0;
        
        EllipsoidSimulator es;
        es=new EllipsoidSimulator(center,r,angle);
        return es;
    }
    
    public ArrayList <SpacialObject> getSphere(int number){
        ArrayList <SpacialObject> sos=new ArrayList <SpacialObject>();
        for(int i=0;i<number;i++){
            sos.add(newSphere());
        }
        return sos;
    }
    public ArrayList <SpacialObject> getEllipsoids(int number){
        ArrayList <SpacialObject> sos=new ArrayList <SpacialObject>(number);
        for(int i=0;i<number;i++){
            IJ.showStatus("Adding Ellipsoids " + (i+1) + "/" + number);
            IJ.showProgress((double)(i+1)/number);
            sos.add(newEllipsoid());
        }
        return sos;
    }
    public ArrayList <SpacialObject> getTheEllipsoids(){
        ArrayList <SpacialObject> sos=new ArrayList <SpacialObject>();
        sos.add(theEllipsoid());
        return sos;
    }
    public void setStructureFrame(StructureFrame sFrame){
        structureFrame=sFrame;
        m_fRanges=structureFrame.getRangesF();
    }
    public void generateSpheres(int nSize, FloatRange rRange, intRange idRange){
    }
}
