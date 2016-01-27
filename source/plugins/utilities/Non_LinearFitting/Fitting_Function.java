/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public abstract class Fitting_Function {
//    protected int m_nPars,m_nVars,m_nFixedPars,m_nAdjustablePars,m_pnFixedParIndexes[],m_pnAdjustableParIndexes[];//numbers of parameters and independet variables
    protected int m_nPars,m_nVars;//numbers of parameters and independet variables
    protected String m_sExpressionType;
    protected double[] m_pdOrigin, m_pdXPrime;
    public abstract void setStartingTerms(int nTerms);
    public abstract double fun(double[] pars, double[] x);
    public abstract double funValueAndGradient(double[]pars,double[]x,double[]pdGradient);
    public abstract double funPartialDerivative(double[]pars,double[]x, int index);
    public abstract boolean expandable();
    public abstract double[] getExpandedModel(double[] pdPars);
    public abstract double[] getExpandedModel(double[] pdPars,double[] pdLocation);
    public abstract double[] getExpandedModel(double[] pdPars,double[] pdLocation,double diff);
    public abstract void getDescriptions(ArrayList<String> svDescript,ArrayList<String> svExpressions,ArrayList<String> svBaseParNames,ArrayList<String> svExpandedParNames, ArrayList<Integer> nvParNumbers);
    protected ArrayList<String> m_svComponentFunctionTypes;
    protected ArrayList<String> m_svParNames;
    protected ArrayList<Integer> m_nvNumPars;
    protected StringBuffer m_sFunctionExpression;
    int m_nNumComponents,m_nNumPars;
    public int m_nNumBasePars;//11427
    public int m_nNumParsPerTerm;//11427
    public ArrayList<Integer> getNumParsInComponents(){
        return m_nvNumPars;
    }
    public StringBuffer getFunctionExpression(){
        return m_sFunctionExpression;
    }
    public ArrayList<String> getParNames(){
        return m_svParNames;
    }
    public int getNumPars(){
        return m_nNumPars;
    }
    public int getNumComponents(){
        return m_nNumComponents;
    }
    ArrayList<String> getComponentFunctionTypes(){
        return m_svComponentFunctionTypes;
    }
    public void setDefaultOrigin(int nVars){
        m_nVars=nVars;
        m_pdOrigin=new double[m_nVars];
        m_pdXPrime=new double[m_nVars];
        for(int i=0;i<m_nVars;i++){
            m_pdOrigin[i]=0;
        }
    }
    public void seOrigin(double[] pdOrigin){
        if(m_pdOrigin==null)setDefaultOrigin(pdOrigin.length);
        if(m_pdOrigin.length!=pdOrigin.length) setDefaultOrigin(pdOrigin.length);
        for(int i=0;i<m_nVars;i++){
            m_pdOrigin[i]=pdOrigin[i];
        }
    }
    protected void updateXPrime(double[] pdX){
        int nVars=pdX.length;
        if(m_pdOrigin==null)setDefaultOrigin(nVars);
        if(m_pdOrigin.length!=nVars) setDefaultOrigin(nVars);
        for(int i=0;i<nVars;i++){
            m_pdXPrime[i]=pdX[i]-m_pdOrigin[i];
        }
    }
    public String getExpressionType(){
        return m_sExpressionType;
    }
    ArrayList<String> copyComponentFunctionTypes(){
        ArrayList<String> sTypes=new ArrayList();
        int i,len=m_svComponentFunctionTypes.size();
        for(i=0;i<len;i++){
            sTypes.add(m_svComponentFunctionTypes.get(i));
        }
        return sTypes;
    }
}
