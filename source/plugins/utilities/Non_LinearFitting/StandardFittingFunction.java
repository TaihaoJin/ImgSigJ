/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import utilities.CommonMethods;
import java.util.ArrayList;
import ij.IJ;
import utilities.CommonStatisticsMethods;
import utilities.io.MessageAssist;
import utilities.Non_LinearFitting.ComposedFittingFunction;

/**
 *
 * @author Taihao
 */
public class StandardFittingFunction extends Fitting_Function{
    int m_nNumStartingTerms;
    boolean m_bNumStartingTerms=false;
//    public StandardFittingFunction(String sType, int nNumPars, int nNumVars){
    public void setStartingTerms(int nTerms){
        m_nNumStartingTerms=nTerms;
        m_bNumStartingTerms=true;
    }
    public double funValueAndGradient(double[]pars,double[]x,double[]pdGradient){return 0;}//not implemented
    public double funPartialDerivative(double[]pars,double[]x, int index){return 0;};
    public StandardFittingFunction(String sType){//11626
        m_sExpressionType=sType;
        boolean validFunc=false;
        if(m_sExpressionType.contentEquals("exponential")){
            m_nNumParsPerTerm=2;
            m_nNumBasePars=1;
            validFunc=true;
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            m_nNumParsPerTerm=1;
            m_nNumBasePars=0;
            validFunc=true;
        }
        if(m_sExpressionType.contentEquals("gaussian")){
            m_nNumParsPerTerm=3;
            m_nNumBasePars=1;
            validFunc=true;
        }
        if(m_sExpressionType.contentEquals("gaussian2D")){
            validFunc=true;
            m_nNumParsPerTerm=6;
            m_nNumBasePars=1;
        }
        if(m_sExpressionType.contentEquals("gaussian2D_GaussianPars")){
            validFunc=true;
            m_nNumParsPerTerm=6;
            m_nNumBasePars=1;
        }
        if(m_sExpressionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            m_nNumParsPerTerm=4;
            m_nNumBasePars=1;
        }
        if(m_sExpressionType.contentEquals("gaussian2D_IPO")){
            validFunc=true;
            m_nNumParsPerTerm=4;
            m_nNumBasePars=1+6*m_nNumStartingTerms;
        }
        if(!validFunc) IJ.error("undefined expression type: "+m_sExpressionType);
    }
    public void setNumStartingTerms(int nTerms){
        m_nNumStartingTerms=nTerms;
        m_bNumStartingTerms=true;
    }
    public double fun(double[] pdPars, double[] pdX){
        double dv=0;
        int i,nPars=pdPars.length;
        boolean validFunc=false;
        if(m_sExpressionType.contentEquals("exponential")){
            updateXPrime(pdX);
            validFunc=true;
            int nTerms=nPars/2;
            dv=pdPars[2*nTerms];
            for(i=0;i<nTerms;i++){
                dv+=pdPars[i]*Math.exp(-m_pdXPrime[0]/pdPars[nTerms+i]);
            }
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            updateXPrime(pdX);
            validFunc=true;
            dv=0;
            for(i=0;i<nPars;i++){
                dv+=pdPars[i]*Math.pow(m_pdXPrime[0], i);
            }
        }
        if(m_sExpressionType.contentEquals("gaussian")){
            validFunc=true;
            dv=CommonMethods.StandardFunctionValue(pdPars, pdX, m_sExpressionType);
        }
        if(m_sExpressionType.contentEquals("gaussian2D")){
            validFunc=true;
            m_nNumParsPerTerm=6;
            m_nNumBasePars=1;
            dv=CommonMethods.StandardFunctionValue(pdPars, pdX, m_sExpressionType);
        }
        if(m_sExpressionType.contentEquals("gaussian2D_GaussianPars")){
            validFunc=true;
            m_nNumParsPerTerm=6;
            m_nNumBasePars=1;
            dv=CommonMethods.StandardFunctionValue(pdPars, pdX, m_sExpressionType);
        }
        if(m_sExpressionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            m_nNumParsPerTerm=4;
            m_nNumBasePars=1;
            dv=CommonMethods.StandardFunctionValue(pdPars, pdX, m_sExpressionType);
        }
        if(m_sExpressionType.contentEquals("gaussian2D_IPO")){
            if(!m_bNumStartingTerms) MessageAssist.error("the m_nNumStartingTerms need to set for function type gaussian2D_IPO");
            validFunc=true;
            m_nNumParsPerTerm=4;
            m_nNumBasePars=1+6*m_nNumStartingTerms;
            nPars=pdPars.length;
            dv=CommonMethods.StandardFunctionValue(pdPars, 0,m_nNumBasePars,pdX, "gaussian2D_GaussianPars",true);
            int nExpandedPars=nPars-m_nNumBasePars;
            if(nExpandedPars>0) {
                dv+=CommonMethods.StandardFunctionValue(pdPars,m_nNumBasePars,nExpandedPars,pdX, "gaussian2D_Circular",false);
            }
        }
        if(!validFunc) IJ.error("undefined expression type: "+m_sExpressionType);
        return dv;
    }
    ArrayList<double[]> copyPars(double[] pdPars, int num){
        ArrayList<double[]> pdvPars=new ArrayList();
        int nPars=pdPars.length;
        double[] pdParst;
        for(int i=0;i<num;i++){
            pdParst=new double[nPars];
            CommonStatisticsMethods.copyArray(pdPars, pdParst);
            pdvPars.add(pdParst);
        }
        return pdvPars;
    }
    public ArrayList<double[]> decomposeParameters(double[] pdPars){//including parameters yieding constant function values
        boolean validFunc=false;
        double pdParst[];
        int len,i,j,nTerm=0,nTerms;
        int nPars=pdPars.length;
        ArrayList<double[]> pdvPars=null;
        if(m_sExpressionType.contentEquals("exponential")){
            validFunc=true;
            nTerms=nPars/2;
            pdvPars=copyPars(pdPars,nTerms+1);            
            pdParst=pdvPars.get(nTerm);
            nTerm++;
            for(i=0;i<nTerms;i++){
                pdParst[i]=0;
            }
            for(i=0;i<nTerms;i++){
                pdParst=pdvPars.get(nTerm);
                pdParst[2*nTerms]=0;
                nTerm++;
                for(j=0;j<nTerms;j++){
                    if(i!=j) pdParst[j]=0;
                }
            }
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            validFunc=true;
            pdvPars=copyPars(pdPars,nPars);
            for(i=0;i<nPars;i++){
                pdParst=pdvPars.get(nTerm);
                nTerm++;
                for(j=0;j<nPars;j++){
                    if(i!=j) pdParst[j]=0;
                }
            }
        }
        if(m_sExpressionType.contentEquals("gaussian")){
            validFunc=true;
            int nParsPerTerm=3;
            nTerms=nPars/nParsPerTerm;

            pdvPars=copyPars(pdPars,nTerms+1);

            pdParst=pdvPars.get(nTerm);
            nTerm++;
            int o=1;
            for(i=0;i<nTerms;i++){
                pdParst[o]=0;
                o+=nParsPerTerm;
            }

            for(i=0;i<nTerms;i++){
                pdParst=pdvPars.get(nTerm);
                pdParst[0]=0;
                nTerm++;
                o=1;
                for(j=0;j<nTerms;j++){
                    if(i!=j) pdParst[o]=0;
                    o+=nParsPerTerm;
                }
            }
        }
        if(m_sExpressionType.contentEquals("gaussian2D")){
            validFunc=true;
            int nBasePars=1;
            int nParsPerTerm=6;
            nTerms=(nPars-nBasePars)/nParsPerTerm;

            pdvPars=copyPars(pdPars,nTerms+1);

            pdParst=pdvPars.get(nTerm);
            nTerm++;
            int o=1;
            for(i=0;i<nTerms;i++){
                pdParst[o]=0;
                o+=nParsPerTerm;
            }

            for(i=0;i<nTerms;i++){
                pdParst=pdvPars.get(nTerm);
                pdParst[0]=0;
                nTerm++;
                o=1;
                for(j=0;j<nTerms;j++){
                    if(i!=j) pdParst[o]=0;
                    o+=nParsPerTerm;
                }
            }
        }
        if(m_sExpressionType.contentEquals("gaussian2D_GaussianPars")){
            validFunc=true;
            int nBasePars=1;
            int nParsPerTerm=6;
            nTerms=(nPars-nBasePars)/nParsPerTerm;

            pdvPars=copyPars(pdPars,nTerms+1);

            pdParst=pdvPars.get(nTerm);
            nTerm++;
            int o=1;
            for(i=0;i<nTerms;i++){
                pdParst[o]=0;
                o+=nParsPerTerm;
            }

            for(i=0;i<nTerms;i++){
                pdParst=pdvPars.get(nTerm);
                pdParst[0]=0;
                nTerm++;
                o=1;
                for(j=0;j<nTerms;j++){
                    if(i!=j) pdParst[o]=0;
                    o+=nParsPerTerm;
                }
            }
        }
        if(m_sExpressionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            int nBasePars=1;
            int nParsPerTerm=4;
            nTerms=(nPars-nBasePars)/nParsPerTerm;

            pdvPars=copyPars(pdPars,nTerms+1);

            pdParst=pdvPars.get(nTerm);
            nTerm++;
            int o=1;
            for(i=0;i<nTerms;i++){
                pdParst[o]=0;
                o+=nParsPerTerm;
            }

            for(i=0;i<nTerms;i++){
                pdParst=pdvPars.get(nTerm);
                pdParst[0]=0;
                nTerm++;
                o=1;
                for(j=0;j<nTerms;j++){
                    if(i!=j) pdParst[o]=0;
                    o+=nParsPerTerm;
                }
            }
        }
        if(m_sExpressionType.contentEquals("gaussian2D_IPO")){
            if(!m_bNumStartingTerms) MessageAssist.error("the m_nNumStartingTerms need to set for function type gaussian2D_IPO");
            validFunc=true;
            int nParsPerTerm=4;
            int nParsPerStartingTerm=6;
            int nBasePars=1+nParsPerStartingTerm*m_nNumStartingTerms;
            nPars=pdPars.length;
            int nExpandedPars=nPars-nBasePars;

            nTerms=nExpandedPars/nParsPerTerm;

            pdvPars=copyPars(pdPars,nTerms+m_nNumStartingTerms+1);
            pdParst=pdvPars.get(0);
            nTerm++;
            int o=1;
            for(i=0;i<m_nNumStartingTerms;i++){
                pdParst[o]=0;
                o+=nParsPerStartingTerm;
            }
            for(i=0;i<nTerms;i++){
                pdParst[o]=0;
                o+=nParsPerTerm;
            }
            //made pars for the constant, now for the starting terms
            for(i=0;i<m_nNumStartingTerms;i++){
                pdParst=pdvPars.get(nTerm);
                nTerm++;
                pdParst[0]=0;
                o=1;

                for(j=0;j<m_nNumStartingTerms;j++){
                    if(i!=j) pdParst[o]=0;
                    o+=nParsPerStartingTerm;
                }
                for(j=0;j<nTerms;j++){
                    pdParst[o]=0;
                    o+=nParsPerTerm;
                }
            }
            //made pars for the starting terms, now for the expanded terms
            for(i=0;i<nTerms;i++){
                pdParst=pdvPars.get(nTerm);
                nTerm++;
                pdParst[0]=0;
                o=1;

                for(j=0;j<m_nNumStartingTerms;j++){
                    pdParst[o]=0;
                    o+=nParsPerStartingTerm;
                }
                for(j=0;j<nTerms;j++){
                    if(i!=j) pdParst[o]=0;
                    o+=nParsPerTerm;
                }
            }
        }
        if(!validFunc) IJ.error("undefined expression type: "+m_sExpressionType);
        return pdvPars;
    }

    public void getDescriptions(ArrayList<String> svDescription,ArrayList<String> svExpression,ArrayList<String> svBaseParNames,ArrayList<String> svExpandedParNames,ArrayList<Integer> nvNumParameters){
//        svExpression.clear();
//        svDescription.clear();
//        svBaseParNames.clear();
//        svExpandedParNames.clear();
//        nvNumParameters.clear();//should not clear these arrays, they hold the values for different models
        if(nvNumParameters!=null){
            nvNumParameters.add(m_nNumBasePars);
            nvNumParameters.add(m_nNumParsPerTerm);
            nvNumParameters.add(m_nNumStartingTerms);
        }
        if(m_sExpressionType.contentEquals("exponential")){
            if(svDescription!=null)svDescription.add("Exponential");
            if(svExpression!=null)svExpression.add("y=Const+Sum(Ai*exp(x/Taui)");
            if(svBaseParNames!=null)svBaseParNames.add("Const");
            if(svExpandedParNames!=null)svExpandedParNames.add("A");
            if(svExpandedParNames!=null)svExpandedParNames.add("Tau");
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            //no base par
            if(svDescription!=null)svDescription.add("Polynomial");
            if(svExpression!=null)svExpression.add("y=Sum(Ai*x^i)");
            if(svExpandedParNames!=null)svExpandedParNames.add("A");
        }
        if(m_sExpressionType.contentEquals("gaussian")){
            if(svDescription!=null)svDescription.add("Gaussian");
            if(svExpression!=null)svExpression.add("y=Const+Sum(Ai*exp(-(x-xci)*(x-xi)/(2*sigmai*sigmai))");
            if(svBaseParNames!=null)svBaseParNames.add("Const");
            if(svExpandedParNames!=null)svExpandedParNames.add("A");
            if(svExpandedParNames!=null)svExpandedParNames.add("xc");
            if(svExpandedParNames!=null)svExpandedParNames.add("sigma");
        }
        if(m_sExpressionType.contentEquals("gaussian2D")){
            if(svDescription!=null)svDescription.add("Gaussian2D");
            if(svExpression!=null)svExpression.add("y=Const+Sum(Ai*exp(-a*(x-xci)*(x-xci)-b*(x-xci)*(y-yci)-c*(y-yci)*(y-yci))");
            if(svBaseParNames!=null)svBaseParNames.add("Const");
            if(svExpandedParNames!=null)svExpandedParNames.add("A");
            if(svExpandedParNames!=null)svExpandedParNames.add("a");
            if(svExpandedParNames!=null)svExpandedParNames.add("b");
            if(svExpandedParNames!=null)svExpandedParNames.add("c");
            if(svExpandedParNames!=null)svExpandedParNames.add("xc");
            if(svExpandedParNames!=null)svExpandedParNames.add("yc");
        }
        if(m_sExpressionType.contentEquals("gaussian2D_GaussianPars")){
            if(svDescription!=null)svDescription.add("Gaussian_GaussianPars");
            if(svExpression!=null)svExpression.add("y=Const+Sum(Ai*exp(-a*(x-xci)*(x-xci)-b*(x-xci)*(y-yci)-c*(y-yci)*(y-yci))");
            if(svBaseParNames!=null)svBaseParNames.add("Const");
            if(svExpandedParNames!=null)svExpandedParNames.add("A");
            if(svExpandedParNames!=null)svExpandedParNames.add("sigmax");
            if(svExpandedParNames!=null)svExpandedParNames.add("sigmay");
            if(svExpandedParNames!=null)svExpandedParNames.add("theta");
            if(svExpandedParNames!=null)svExpandedParNames.add("xc");
            if(svExpandedParNames!=null)svExpandedParNames.add("yc");
        }
        if(m_sExpressionType.contentEquals("gaussian2D_Circular")){
            if(svDescription!=null)svDescription.add("Gaussian_Circular");
            if(svExpression!=null)svExpression.add("y=Const+Sum(Ai*exp(-(x-xci)*(x-xci)/(2*sigma*sigma)-(y-yci)*(y-yci)/(2*sigma*sigma))");
            if(svBaseParNames!=null)svBaseParNames.add("Const");
            if(svExpandedParNames!=null)svExpandedParNames.add("A");
            if(svExpandedParNames!=null)svExpandedParNames.add("sigma");
            if(svExpandedParNames!=null)svExpandedParNames.add("xc");
            if(svExpandedParNames!=null)svExpandedParNames.add("yc");
        }
        if(m_sExpressionType.contentEquals("gaussian2D_IPO")){
            if(svDescription!=null)svDescription.add("gaussian2D_IPO");
            if(svExpression!=null)svExpression.add("y=Const+Sum(Ai*exp(-a*(x-xci)*(x-xci)-b*(x-xci)*(y-yci)-c*(y-yci)*(y-yci))+Sum(Bj*exp(-(x-xcaj)*(x-xcaj)/(2*sigma*sigma-(y-ycaj)*(y-ycaj)/(2*sigma*sigma))");
            if(svBaseParNames!=null)svBaseParNames.add("Const");
            if(svBaseParNames!=null)svBaseParNames.add("A");
            if(svBaseParNames!=null)svBaseParNames.add("sigmax");
            if(svBaseParNames!=null)svBaseParNames.add("sigmay");
            if(svBaseParNames!=null)svBaseParNames.add("theta");
            if(svBaseParNames!=null)svBaseParNames.add("xc");
            if(svBaseParNames!=null)svBaseParNames.add("yc");
            if(svExpandedParNames!=null)svExpandedParNames.add("B");
            if(svExpandedParNames!=null)svExpandedParNames.add("sigma");
            if(svExpandedParNames!=null)svExpandedParNames.add("xca");
            if(svExpandedParNames!=null)svExpandedParNames.add("yca");
        }
    }
    public double[] getExpandedModel(double[] pdPars0){
        return getExpandedModel(pdPars0,null);
    }
    public double[] getExpandedModel(double[] pdPars0, double[] pdLocation){
        return getExpandedModel(pdPars0,pdLocation, 0);
    }
    public double[] getExpandedModel(double[] pdPars0, double[] pdLocation,double diff){
        int nPars0=pdPars0.length;
        double[] pdPars=null;
        boolean validFunc=false;
        int i,j;
        if(m_sExpressionType.contentEquals("exponetial")){
            validFunc=true;
            m_nPars=nPars0+2;
            pdPars=new double[m_nPars];
            for(i=0;i<nPars0;i++){
                pdPars[i]=pdPars0[i];
            }
            pdPars[nPars0]=pdPars0[nPars0-2]/10;
            pdPars[nPars0+1]=pdPars0[nPars0-1]*10;
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            validFunc=true;
            m_nPars=nPars0+1;
            pdPars=new double[m_nPars];
            for(i=0;i<nPars0;i++){
                pdPars[i]=pdPars0[i];
            }
            pdPars[nPars0]=pdPars0[nPars0-1]/10;
        }
        if(m_sExpressionType.contentEquals("gaussian")){
            validFunc=true;
            int nParsPerTerm=4;
            m_nPars=nPars0+nParsPerTerm;
            pdPars=new double[m_nPars];
            int nTerms=m_nPars/nParsPerTerm;
            int o=0,index;
            double par;
            for(i=nPars0;i<m_nPars;i++){
                pdPars[i]=0;
            }
            for(i=0;i<nTerms-1;i++){
                for(j=0;j<nParsPerTerm;j++){
                    index=o+j;
                    par=pdPars0[index];
                    pdPars[index]=par;
                    pdPars[nPars0+j]+=par;
                }
                o+=nParsPerTerm;
            }
            double dt=(double)(nTerms-1);
            for(i=nPars0;i<m_nPars;i++){
                pdPars[i]/=dt;
            }
        }
        if(m_sExpressionType.contentEquals("gaussian2D_GaussianPars")){
            validFunc=true;
            m_nNumParsPerTerm=6;
            m_nNumBasePars=1;
            m_nPars=nPars0+m_nNumParsPerTerm;
            pdPars=new double[m_nPars];
            int nTerms=(m_nPars-m_nNumBasePars)/m_nNumParsPerTerm;
            int o=0,index;
            double par;
            for(i=0;i<m_nPars;i++){
                if(i<m_nNumBasePars)
                    pdPars[i]=pdPars0[i];
                else
                    pdPars[i]=0.;
            }
            o=m_nNumBasePars;
            for(i=0;i<nTerms-1;i++){
                for(j=0;j<m_nNumParsPerTerm;j++){
                    index=o+j;
                    par=pdPars0[index];
                    pdPars[index]=par;
                    pdPars[nPars0+j]+=par;
                }
                o+=m_nNumParsPerTerm;
            }
            if(nTerms>2){
                double dt=(double)(nTerms-1);
                for(i=nPars0;i<m_nPars;i++){
                    pdPars[i]/=dt;
                }
            }else{
                double sigmax=pdPars[2];
                double sigmay=pdPars[3];
                pdPars[4]+=0.5*(sigmax+sigmay);
                pdPars[5]+=0.5*(sigmax+sigmay);
            }
            if(pdLocation!=null){
                pdPars[nPars0+4]=pdLocation[0];
                pdPars[nPars0+5]=pdLocation[1];
            }
        }
        if(m_sExpressionType.contentEquals("gaussian2D_IPO")){
            validFunc=true;
            m_nNumParsPerTerm=4;
            m_nPars=nPars0+m_nNumParsPerTerm;
            pdPars=new double[m_nPars];
            double par;
            for(i=0;i<m_nPars;i++){
                if(i<nPars0)
                    pdPars[i]=pdPars0[i];
                else
                    pdPars[i]=0.;
            }
            
            if(Math.abs(diff)<1)
                pdPars[nPars0]=100;
            else
                pdPars[nPars0]=diff;

            pdPars[nPars0+1]=2.5;
            if(pdLocation!=null){
                pdPars[nPars0+2]=pdLocation[0];
                pdPars[nPars0+3]=pdLocation[1];
            }else{
                pdPars[nPars0+2]=pdPars0[3]+3;
                pdPars[nPars0+3]=pdPars0[4]+3;
            }
        }
        if(m_sExpressionType.contentEquals("gaussian2D")){
            validFunc=true;
            m_nNumParsPerTerm=6;
            m_nNumBasePars=1;
            m_nPars=nPars0+m_nNumParsPerTerm;
            pdPars=new double[m_nPars];
            int nTerms=(m_nPars-m_nNumBasePars)/m_nNumParsPerTerm;
            int o=0,index;
            double par;
            for(i=0;i<m_nPars;i++){
                if(i<m_nNumBasePars)
                    pdPars[i]=pdPars0[i];
                else
                    pdPars[i]=0.;
            }
            o=m_nNumBasePars;
            for(i=0;i<nTerms-1;i++){
                for(j=0;j<m_nNumParsPerTerm;j++){
                    index=o+j;
                    par=pdPars0[index];
                    pdPars[index]=par;
                    pdPars[nPars0+j]+=par;
                }
                o+=m_nNumParsPerTerm;
            }
            if(nTerms>2){
                double dt=(double)(nTerms-1);
                for(i=nPars0;i<m_nPars;i++){
                    pdPars[i]/=dt;
                }
            }else{
                double[] pdParst=getGaussian2DParameters(pdPars0);
                double sigmax=pdParst[2];
                double sigmay=pdParst[3];
                pdPars[4]+=0.5*(sigmax+sigmay);
                pdPars[5]+=0.5*(sigmax+sigmay);
            }
            if(pdLocation!=null){
                pdPars[nPars0+4]=pdLocation[0];
                pdPars[nPars0+5]=pdLocation[1];
            }
        }
        if(!validFunc) IJ.error("undefined expression type: "+m_sExpressionType);
        return pdPars;
    }
    public static double[] getGaussian2DParameters(double[] pdTransformedPars){
        int nParsPerTerm=6,nPars=pdTransformedPars.length;
        double[] pdPars=new double[nPars];
        int nTerms=(nPars-1)/nParsPerTerm,o=1;
        for(int i=0;i<nTerms;i++){
            getGaussian2DParameters(pdTransformedPars,o,pdPars,o);
            o+=nParsPerTerm;
        }
        return pdPars;
    }
    
    public static void getGaussian2DParameters(double[] pdTransformedPars, int o1,double[] pdPars, int o2){//o: offset of the index
        double a,b,c,d;
        int nParsPerTerm=6;
        int i;
        for(i=0;i<nParsPerTerm;i++){
            pdPars[o2+i]=pdTransformedPars[o1+i];
        }
        a=pdTransformedPars[o1+1];
        b=pdTransformedPars[o1+2];
        c=pdTransformedPars[o1+3];

        d=a/(a+c);
        double theta=0.5*Math.atan(2.*b/(c-a));
        double stheta=Math.sin(theta),ctheta=Math.cos(theta),c2theta=Math.cos(2.*theta);
        double sigmay=c2theta*(1-2*d)/(2*(c-a)*(ctheta*ctheta-d));
        double sigmax=sigmay*(ctheta*ctheta-d)/(d-stheta*stheta);
        sigmax=Math.sqrt(sigmax);
        sigmay=Math.sqrt(sigmay);
        pdPars[o2+1]=sigmax;
        pdPars[o2+2]=sigmay;
        pdPars[o2+3]=theta;
//        pdPars[o2]/=2*Math.PI*sigmax*sigmay;
    }

    public static void getTransformedGaussian2DParameters(double[] pdPars, int o1,double[] pdTransformedPars, int o2){//o: offset of the index
        double a,b,c,sigmax2,sigmay2,theta;
        int nParsPerTerm=6;
        int i;
        for(i=0;i<nParsPerTerm;i++){
            pdTransformedPars[o2+i]=pdPars[o1+i];
        }

        sigmax2=pdPars[o1+1];
        sigmay2=pdPars[o1+2];
        theta=pdPars[o1+3];
        sigmax2*=sigmax2;
        sigmay2*=sigmay2;
//        pdTransformedPars[o2]*=2*Math.PI*sigmax2*sigmay2;

        double stheta=Math.sin(theta),ctheta=Math.cos(theta);
        a=0.5*ctheta*ctheta/sigmax2+0.5*stheta*stheta/sigmay2;
        b=0.5*stheta*ctheta*(1./sigmay2-1./sigmax2);
        c=0.5*stheta*stheta/sigmax2+0.5*ctheta*ctheta/sigmay2;

        pdTransformedPars[o2+1]=a;
        pdTransformedPars[o2+2]=b;
        pdTransformedPars[o2+3]=c;
    }

    public static double[] getTransformedGaussian2DParameters(double[] pdPars){//o: offset of the index
        int nParsPerTerm=6,nPars=pdPars.length;
        double[] pdTransformedPars=new double[nPars];
        int nTerms=(nPars-1)/nParsPerTerm,o;
        pdTransformedPars[0]=pdPars[0];
        o=1;
        for(int i=0;i<nTerms;i++){
            getTransformedGaussian2DParameters(pdPars,o,pdTransformedPars,o);
            o+=nParsPerTerm;
        }
        return pdTransformedPars;
    }

    public boolean expandable(){
        boolean expandable=false;
        boolean validFunc=false;
        int i;
        if(m_sExpressionType.contentEquals("exponetial")){
            validFunc=true;
            expandable=true;
        }
        if(m_sExpressionType.contentEquals("polynomial")){
            validFunc=true;
            expandable=true;
        }
        if(m_sExpressionType.contentEquals("gaussian")){
            validFunc=true;
            expandable=true;
        }
        if(m_sExpressionType.contentEquals("gaussian")){
            validFunc=true;
            expandable=true;
        }
        if(m_sExpressionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            expandable=true;
        }
        if(m_sExpressionType.contentEquals("gaussian2D_IPO")){
            validFunc=true;
            expandable=true;
        }
        if(!validFunc) IJ.error("undefined expression type: "+m_sExpressionType);
        return expandable;
    }
    static public ComposedFittingFunction getComposedFittingFunction(String sType, int nPars, int nStartingTerms){
        ArrayList<String> svTypes=new ArrayList();
        boolean validType=false;
        int nTerms,i;
        if(sType.contentEquals("gaussian2D_IPO")){
            for(i=0;i<nStartingTerms;i++){
                svTypes.add("gaussian2D_GaussianPars");
            }
            nTerms=(nPars-nStartingTerms*6-1)/4;
            for(i=0;i<nTerms;i++){
                svTypes.add("gaussian2D_Circular");
            }
            validType=true;
        }
        if(!validType) IJ.error("undefined expression type: "+sType);
        return new ComposedFittingFunction(svTypes);
    }
}
