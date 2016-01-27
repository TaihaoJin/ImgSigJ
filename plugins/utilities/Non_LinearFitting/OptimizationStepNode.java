/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import java.util.ArrayList;
import utilities.CommonStatisticsMethods;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.Constrains.ConstraintViolationChecker;
import utilities.CommonMethods;
/**
 *
 * @author Taihao
 */
public class OptimizationStepNode {
        public double dV;//the penalty is included
        double pars[];
        public ArrayList <Integer> nvViolatedIndexes;
        public ArrayList <Double> dvPenalties;
        public double penalty;
        public boolean accepted;
        public OptimizationStepNode(int nDim){
            pars=new double[nDim];
            nvViolatedIndexes=new ArrayList();
            dvPenalties=new ArrayList();
        }
        void update(double dV, double[] pars, ArrayList <Integer> nvViolatedIndexes,
                ArrayList <Double> dvPenalties,double penalty, boolean accepted){
            this.nvViolatedIndexes.clear();
            this.dvPenalties.clear();
            int nDim=pars.length;
            for(int i=0;i<nDim;i++){
                this.pars[i]=pars[i];
            }
            nDim=nvViolatedIndexes.size();
            for(int i=0;i<nDim;i++){
                this.nvViolatedIndexes.add(nvViolatedIndexes.get(i));
                this.dvPenalties.add(dvPenalties.get(i));
            }
            this.dV=dV;
            this.penalty=penalty;
            this.accepted=accepted;
        }
}
