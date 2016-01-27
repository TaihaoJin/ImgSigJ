/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import utilities.ArrayofArrays.IntArray;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class BlockDiagonalizer {
    class blockNode{
        public ArrayList<Integer> ColumnIndexes,RowIndexes;
        public int blockIndex;
        public blockNode(){
            ColumnIndexes=new ArrayList();
            RowIndexes=new ArrayList();
        }
        public void merge(blockNode aNode){
            int len=aNode.ColumnIndexes.size(),i,index;
            for(i=0;i<len;i++){
                index=aNode.ColumnIndexes.get(i);
                if(!CommonMethods.containsContent(ColumnIndexes, index))ColumnIndexes.add(index);
            }
            len=aNode.RowIndexes.size();
            for(i=0;i<len;i++){
                index=aNode.RowIndexes.get(i);
                if(!CommonMethods.containsContent(RowIndexes, index)) RowIndexes.add(index);
            }
        }
    }
    public BlockDiagonalizer(){
        
    }
    public void blockdiagonalizeIntMatrix(int[][] IM,ArrayList <IntArray> m_viaRowIndexes,ArrayList <IntArray> m_viaColumnIndexes){
        int m_nDim=IM.length;
        blockNode pcBlockNodes_Column[]=new blockNode[m_nDim], pcBlockNodes_Row[]=new blockNode[m_nDim];
        ArrayList<blockNode> cvBlockNodes=new ArrayList();
        int i,j;
        for(i=0;i<m_nDim;i++){
            pcBlockNodes_Column[i]=null;
            pcBlockNodes_Row[i]=null;
        }
        blockNode bc,br,bn;
        for(i=0;i<m_nDim;i++){
            if(i==19){
                i=i;
            }
            for(j=0;j<m_nDim;j++){
                if(IM[i][j]!=0){
                    br=pcBlockNodes_Row[i];
                    bc=pcBlockNodes_Column[j];
                    if(br==null&&bc==null){
                        bn=new blockNode();
                        bn.blockIndex=cvBlockNodes.size();
                        cvBlockNodes.add(bn);
                        bn.ColumnIndexes.add(j);
                        bn.RowIndexes.add(i);
                        pcBlockNodes_Column[j]=bn;
                        pcBlockNodes_Row[i]=bn;
                    }else if(br==null&&bc!=null){
                        bc.RowIndexes.add(i);
                        pcBlockNodes_Row[i]=bc;
                    }else if(br!=null&&bc==null){
                        br.ColumnIndexes.add(j);
                        pcBlockNodes_Column[j]=br;
                    }else if(br!=null&&bc!=null){
                        if(bc.blockIndex<br.blockIndex){
                            mergeBlockNodes(cvBlockNodes,pcBlockNodes_Column,pcBlockNodes_Row,bc,br);
                        }else{
                            mergeBlockNodes(cvBlockNodes,pcBlockNodes_Column,pcBlockNodes_Row,br,bc);
                        }
                    }
                }
            }
        }
        int len=cvBlockNodes.size();
        for(i=0;i<len;i++){
            bn=cvBlockNodes.get(i);
            m_viaRowIndexes.add(new IntArray(bn.RowIndexes));
            m_viaColumnIndexes.add(new IntArray(bn.ColumnIndexes));
        }
    }
    int mergeBlockNodes(ArrayList<blockNode> cvBlockNodes,blockNode[] pcBC, blockNode[] pcBR, blockNode b1, blockNode b2){
        if(b1==b2) {
            return -1;
        }
        b1.merge(b2);
        int i,len,index;
        len=b2.ColumnIndexes.size();
        for(i=0;i<len;i++){
            pcBC[b2.ColumnIndexes.get(i)]=b1;
        }
        len=b2.RowIndexes.size();
        for(i=0;i<len;i++){
            pcBR[b2.RowIndexes.get(i)]=b1;
        }
        index=b2.blockIndex;
        len=cvBlockNodes.size();
        for(i=index;i<len;i++){
            cvBlockNodes.get(i).blockIndex--;
        }
        cvBlockNodes.remove(b2);
        return 1;
    }
}
