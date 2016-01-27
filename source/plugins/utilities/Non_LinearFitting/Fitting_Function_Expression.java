/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
//import org.lsmp.djep.xjep.XJep;
//import org.nfunk.jep.JEP;
//import org.lsmp.djep.rpe.RpEval;
//import org.nfunk.jep.Node;
//import org.lsmp.djep.rpe.RpCommandList;
//import org.nfunk.jep.ParseException;
import java.util.StringTokenizer;
import ij.IJ;
import java.util.Hashtable;
import utilities.CommonMethods;
/**
 *
 * @author Taihao
 */
//public class Fitting_Function_Expression extends Fitting_Function{//will try to make it work later
public class Fitting_Function_Expression {//will try to make it work later
/*    int m_nMinimizationOption;
    String m_sIndependentVars;
    String m_sExpression;
    String m_sDependentVar;
    String m_sPars;
    String[] m_psPars;
    String[] m_psVars;
    XJep m_cParser;
    Node m_cExpressionNode;
    RpEval m_cRpe;

    public Fitting_Function_Expression(int minimizationOption, String sIndependentVars, String sPars, String sDependentVar, String sExpression){
        m_nMinimizationOption=minimizationOption;
        m_sIndependentVars=sIndependentVars;
        m_sExpression=sExpression;
        m_sDependentVar=sDependentVar;
        m_sPars=sPars;
        try{
            init();
        }
        catch(org.nfunk.jep.ParseException e){
            IJ.error("Parse Exception in init() of Fitting_Function_Expression: "+e);
        }
    }
    protected void makeVarList(){
        StringTokenizer stk=new StringTokenizer(m_sIndependentVars,",");
        m_nVars=stk.countTokens();
        m_psVars=new String[m_nVars];
        int i;
        for(i=0;i<m_nVars;i++){
            m_psVars[i]=stk.nextToken();
        }
    }
    protected void makeParList(){
        StringTokenizer stk=new StringTokenizer(m_sPars,",");
        m_nPars=stk.countTokens();
        m_psPars=new String[m_nPars];
        int i;
        for(i=0;i<m_nPars;i++){
            m_psPars[i]=stk.nextToken();
        }
    }
    protected void init ()throws ParseException{
        makeParList();
        makeVarList();

        m_cParser = new XJep();
        m_cParser.addStandardConstants();
        m_cParser.addStandardFunctions();
//      m_cParser.addComplex();
        m_cParser.setAllowUndeclared(true);
        m_cParser.setImplicitMul(false);
        m_cParser.setAllowAssignment(true);

        addPars();
        addVars();
        double dv;
        m_cExpressionNode = m_cParser.parse(m_sExpression);
        dv=m_cParser.getValue();
        dv=(Double)m_cParser.evaluate(m_cExpressionNode);
        m_cExpressionNode=m_cParser.preprocess(m_cExpressionNode);
        m_cExpressionNode=m_cParser.simplify(m_cExpressionNode);
        
        m_cRpe = new RpEval(m_cParser);
    }
    void addPars(){
        int len=m_psPars.length;
        for(int i=0;i<len;i++){
            try{m_cParser.addVariable(m_psPars[i],0);}
            catch (Exception e){
                IJ.error("Exception in addPars of Fitting_Function_Expression.java");
            }
        }
    }
    void addVars(){
        int len=m_psVars.length;
        for(int i=0;i<len;i++){
            try{m_cParser.addVariable(m_psVars[i],0);}
            catch (Exception e){
                IJ.error("Exception in addVars of Fitting_Function_Expression.java");
            }
        }
    }
    public double fun(double[] pars, double[] x){
        double dv=0;
        if(m_nMinimizationOption==Non_Linear_Fitter.Maximum_Likelihood) return dv;
        setPars(pars);
        setVars(x);
        try{
            dv=(Double)m_cParser.evaluate(m_cExpressionNode);
        }
        catch (org.nfunk.jep.ParseException e){
            IJ.error("Parse error in fun of Fitting_Function_Expression.java");
        }
        return rapidEval();
    }
    protected void setPars(double pdPars[]){
        for(int i=0;i<m_nPars;i++){
            m_cParser.setVarValue(m_psPars[i], pdPars[i]);
        }
    }
    protected void setVars(double pdVars[]){
        for(int i=0;i<m_nVars;i++){
            m_cParser.setVarValue(m_psVars[i], pdVars[i]);
        }
    }
    protected double rapidEval(){
        RpCommandList list =null;
        try{
            list=m_cRpe.compile(m_cExpressionNode);
        }
        catch (org.nfunk.jep.ParseException e){
            IJ.error("Parse error in rapidEval");
        }
        double val = m_cRpe.evaluate(list);
        return val;
    }
    public double fun(double[] pars, double[] x, double y){
        double dv=0;
        if(m_nMinimizationOption==Non_Linear_Fitter.Least_Square) return dv;
        return dv;
    }
    public static double[] getParameters(String st, Hashtable table){
        StringTokenizer stk=new StringTokenizer(st);
        return getParameters(stk, table);
    }
    public static double[] getParameters(StringTokenizer stk, Hashtable table){
        int len=stk.countTokens();
        double pdPars[]=new double[len];
        int i;
        String par;
        String sv;
        for(i=0;i<len;i++){
            par=stk.nextToken();
            sv=(String)CommonMethods.retrieveVariable(table, par+":");
            pdPars[i]=Double.parseDouble(sv);
        }
        return pdPars;
    }*/
    public boolean expandable(){
        return false;
    }
    public double[] getExpandedModel(double[] pdPars){
        return null;
    }
}
