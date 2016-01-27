/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import utilities.Non_LinearFitting.FittingModelNode;
import utilities.CustomDataTypes.intRange;
import utilities.CustomDataTypes.DoubleRange;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import java.util.ArrayList;


/**
 *
 * @author Taihao
 */
public class FittingModelHandler {
    public static void addFittedValues(FittingModelNode cModel,double[][] pdV, intRange ir1, intRange ir2){
        //this methods is for a 2d function
        ComposedFittingFunction func=new ComposedFittingFunction(cModel.svFunctionTypes);
        double[] pdX=new double[2],pdPars=cModel.pdPars;
        double dv;
        int x,y,xI=ir1.getMin(),xF=ir1.getMax(),yI=ir2.getMin(),yF=ir2.getMax(),h=pdV.length,w=pdV[0].length;
        for(y=yI;y<=yF;y++){
            if(y<0||y>=h)continue;
            for(x=xI;x<=xF;x++){
                if(x<0||x>=w) continue;
                if(x==67&&y==3){
                    x=x;
                }
                pdX[0]=x;
                pdX[1]=y;
                dv=func.fun(pdPars, pdX);
                pdV[y][x]+=dv-pdPars[0];
            }
        }
    }
    public static void addFittedValues(FittingModelNode cModel,double[][] pdV){
        ArrayList<DoubleRange> varRanges=new ArrayList();
        varRanges.add(new DoubleRange());
        varRanges.add(new DoubleRange());
        ComposedFittingFunction func=new ComposedFittingFunction(cModel.svFunctionTypes);
        func.getEffctiveVarRanges(cModel.pdPars, varRanges);
        intRange ir1=new intRange((int)varRanges.get(0).getMin()-1,(int)varRanges.get(0).getMax()+1);
        intRange ir2=new intRange((int)varRanges.get(1).getMin()-1,(int)varRanges.get(1).getMax()+1);
        addFittedValues(cModel,pdV,ir1,ir2);
    }
}
