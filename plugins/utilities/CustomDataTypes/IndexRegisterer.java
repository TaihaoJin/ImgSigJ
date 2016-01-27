/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.CustomDataTypes;
import java.util.ArrayList;
import utilities.CustomDataTypes.FloatRange;
import utilities.ArrayofArrays.IntArray;

/**
 *
 * @author Taihao
 */
public class IndexRegisterer {
    IntArray m_vnIndexArrays[];//The array that stores the arraylist of the indexes
    double[] m_pdDataMins; //minum of the data on each dimension
    int m_nDim; //The number of dimension
    int m_nTotalGridPoints; //Total number of the grid points
    int[] m_pnLength; //the number of grid points along each dimension.
    int[] m_pnIncrements;
    double[] m_pdGridSizes;

    public IndexRegisterer(double dMin, double dMax, double dDelta){
        m_nDim=1;
        ArrayList<Double> dMins=new ArrayList(), dDeltas=new ArrayList();
        ArrayList<Integer> nLengths=new ArrayList();
        dMins.add(dMin);
        dDeltas.add(dDelta);
        nLengths.add((int)((dMax-dMin)/dDelta)+1);

        init(dMins,dDeltas,nLengths);
    }

    public IndexRegisterer(double dMin1, double dMax1, double dDelta1, double dMin2, double dMax2, double dDelta2){
        m_nDim=1;
        ArrayList<Double> dMins=new ArrayList(), dDeltas=new ArrayList();
        ArrayList<Integer> nLengths=new ArrayList();
        dMins.add(dMin1);
        dDeltas.add(dDelta1);
        nLengths.add((int)((dMax1-dMin1)/dDelta1)+1);

        dMins.add(dMin2);
        dDeltas.add(dDelta2);
        nLengths.add((int)((dMax2-dMin2)/dDelta2)+1);

        init(dMins,dDeltas,nLengths);
    }

    public IndexRegisterer(double dMin1, double dMax1, double dDelta1, double dMin2, double dMax2, double dDelta2, double dMin3, double dMax3, double dDelta3){
        m_nDim=1;
        ArrayList<Double> dMins=new ArrayList(), dDeltas=new ArrayList();

        ArrayList<Integer> nLengths=new ArrayList();
        dMins.add(dMin1);
        dDeltas.add(dDelta1);
        nLengths.add((int)((dMax1-dMin1)/dDelta1)+1);

        dMins.add(dMin2);
        dDeltas.add(dDelta2);
        nLengths.add((int)((dMax2-dMin2)/dDelta2)+1);

        dMins.add(dMin3);
        dDeltas.add(dDelta3);
        nLengths.add((int)((dMax3-dMin3)/dDelta3)+1);

        init(dMins,dDeltas,nLengths);
    }

    void init(ArrayList<Double> dMins, ArrayList <Double> dDeltas, ArrayList<Integer> nLengths){//All array list mush be the same size
        m_nDim=dMins.size();
        m_pdDataMins=new double[m_nDim];
        m_pnLength=new int[m_nDim];
        m_pnIncrements=new int[m_nDim];
        m_pdGridSizes=new double[m_nDim];
        m_nTotalGridPoints=1;

        int i,len,id;
        for(i=0;i<m_nDim;i++){
            m_pdDataMins[i]=dMins.get(i);
            len=nLengths.get(i);
            m_pnLength[i]=len;
            id=m_nDim-i-1;
            m_pnIncrements[id]=m_nTotalGridPoints;
            m_nTotalGridPoints*=len;
            m_pdGridSizes[i]=dDeltas.get(i);
        }
        m_vnIndexArrays=new IntArray[m_nTotalGridPoints];
        for(i=0;i<m_nTotalGridPoints;i++){
            m_vnIndexArrays[i]=new IntArray();
        }
    }

    public void addIndex(ArrayList<Double> coordinates,int index){
        int position=getPosition(coordinates);
        m_vnIndexArrays[position].m_intArray.add(index);
    }

    public void addIndex(double[]coordinates, int index){
        int i,position=0,nc;

        for(i=0;i<m_nDim;i++){
            nc=(int)((coordinates[i]-m_pdDataMins[i])/m_pdGridSizes[i]);
            position+=nc*m_pnIncrements[i];
        }

        m_vnIndexArrays[position].m_intArray.add(index);
    }
    public int getPosition(ArrayList<Double> coordinates){
        double dCoor, dMin;
        int nCoor, len;
        int position=0;
        for(int i=0;i<m_nDim;i++){
            nCoor=(int)((coordinates.get(i)-m_pdDataMins[i])/m_pdGridSizes[i]);
            position+=nCoor*m_pnIncrements[i];
        }
        return position;
    }

    public void getIndexes(double[] coordinates, double[] proximities, ArrayList<Integer> indexes){
        indexes.clear();
        int i,j;
        int ncMins[]=new int[m_nDim], ncMaxs[]=new int[m_nDim],nprox, ncMin,ncMax,hi;
        double dCoor, dDelta,dProx;

        int num=1;
        int nCoor[]=new int[m_nDim];
        for(i=0;i<m_nDim;i++){
            hi=m_pnLength[i]-1;
            dCoor=coordinates[i];
            dDelta=m_pdGridSizes[i];
            dProx=proximities[i];
            ncMin=(int)((dCoor-dProx)/dDelta);
            ncMax=(int)((dCoor+dProx)/dDelta);
            if(ncMin<0) ncMin=0;
            if(ncMax>hi) ncMax=hi;
            ncMins[i]=ncMin;
            ncMaxs[i]=ncMax;
            num*=(ncMax-ncMin+1);
            nCoor[i]=ncMin;
        }

        int pnPositions[]=new int[num];

        int level=0,nc,lh=m_nDim-1,index=0;
        boolean bLowerLevelFull=false;
        while(!bLowerLevelFull&&nCoor[0]<=ncMaxs[0]){
            if(level==lh){
                for(nc=ncMins[level];nc<=ncMaxs[level];nc++){
                    nCoor[level]=nc;
                    pnPositions[index]=getPosition(nCoor);
                    index++;
                }
                bLowerLevelFull=true;
                level--;
            }

            if(level<0) break;//when m_nDim==1

            nc=nCoor[level];
            if(bLowerLevelFull){
                if(nc<ncMaxs[level]){
                    nCoor[level]++;
                    level++;
                    bLowerLevelFull=false;
                }else{
                    level--;
                }
            }else{
                nCoor[level]=ncMins[level];
                level++;
            }
        }

        int len;
        ArrayList<Integer> ia;
        for(i=0;i<num;i++){
            ia=m_vnIndexArrays[pnPositions[i]].m_intArray;
            len=ia.size();
            for(j=0;j<len;j++){
                indexes.add(ia.get(j));
            }
        }
    }

    int getPosition(int[] nCoor){
        int position=0;
        for(int i=0;i<m_nDim;i++){
            position+=nCoor[i]*m_pnIncrements[i];
        }
        return position;
    }


    public void getPositions(ArrayList<Double> coordinates, ArrayList<Double> proximities, ArrayList<Integer> vnPositions){

    }
}
