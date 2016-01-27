/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class LinearFittingFunction {
    ArrayList<String> m_cvFunctionTypes;
    int nComps;
    double[] pdComponents;
    public LinearFittingFunction(ArrayList<String> svTypes){
        m_cvFunctionTypes=svTypes;
        nComps=m_cvFunctionTypes.size();
        pdComponents=new double[nComps];
    }
    public double fun(double[] pdPars, double x){
        getComponents(x,pdComponents);
        double dv=0;
        for(int i=0;i<nComps;i++){
            dv+=pdPars[i]*pdComponents[i];
        }
        return dv;
    }
    public void getComponents(double x,double[]funcs){
        for(int i=0;i<nComps;i++){
            funcs[i]=getComponent(m_cvFunctionTypes.get(i),x);
        }
    }
    public double getComponent(String sType, double x){
        double dv=0;
        if(sType.contentEquals("const")) dv=1;
        if(sType.startsWith("pow")) {
            int power=Integer.parseInt(sType.substring(3));
            dv=Math.pow(x, power);
        }
        return dv;
    }
}
