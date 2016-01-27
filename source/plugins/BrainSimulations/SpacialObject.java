package BrainSimulations;
import BrainSimulations.DataClasses.FloatRange;
import utilities.CustomDataTypes.intRange;
import BrainSimulations.SpacialGrids;

/**
 *
 * @author Taihao
 */
abstract public class SpacialObject {
   int pixel;
   FloatRange m_fRanges[];
   abstract public int draw(int pixels[], float x0[], int of[], float delta, int w, int h);
//   abstract public boolean withinFrame(SpacialGrids sg);
   abstract void setRanges();
   float enclosingBoxVertexes[][];
   abstract public int  getPixel(float x, float y, float z);
   void setEnclosingBox(){
       float ranges[][]=new float[3][2];
       enclosingBoxVertexes=new float[8][3];
       int i,j,k;
       for(i=0;i<3;i++){
           ranges[i][0]=m_fRanges[i].getMin();
           ranges[i][1]=m_fRanges[i].getMax();
       }
       int index=0;
       for(i=0;i<2;i++){
           for(j=0;j<2;j++){
               for(k=0;k<2;k++){
                  enclosingBoxVertexes[index][0]=ranges[0][i];
                  enclosingBoxVertexes[index][1]=ranges[1][j];
                  enclosingBoxVertexes[index][2]=ranges[2][k];
                  index++;
               }
           }
       }
   }
   public float[][] getEnclosingBoxVertexes(){
       return enclosingBoxVertexes;
   }
   public FloatRange getRangeF(int oi){
       return new FloatRange(m_fRanges[oi].getMin(),m_fRanges[oi].getMax());
   } 
}
