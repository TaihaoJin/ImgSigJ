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
import utilities.CustomDataTypes.DoubleRange;
import utilities.CommonFunctionValueParameterizer;
import utilities.statistics.MeanSem1;

/**
 *
 * @author Taihao
 */
public class ComposedFittingFunction extends Fitting_Function{
    protected ArrayList<Integer> m_nvStartingParIndexes;
    protected static int m_nNumFunctionTypes=10;//need to be modified each time add new function types, could be more than realy number of function types
    public ComposedFittingFunction(){
        m_svComponentFunctionTypes=new ArrayList();
        m_nvNumPars=new ArrayList();//number of parameters of each components
        m_nvStartingParIndexes=new ArrayList();
        m_svParNames=new ArrayList();
    }
    public ComposedFittingFunction(ArrayList<String> svFunctionTypes){
        this();
        updateComponents(svFunctionTypes);
    }
    public void setStartingTerms(int terms){}
    public void updateComponents(ArrayList<String> svFunctionTypes){
        m_nNumComponents=svFunctionTypes.size();
        int startingIndex=1,nPars;//0 is for the constant
        String sType;
        m_nNumPars=1;//to count the constant term
        for(int i=0;i<m_nNumComponents;i++){
            sType=new String(svFunctionTypes.get(i));
            m_nvStartingParIndexes.add(startingIndex);
            m_svComponentFunctionTypes.add(sType);
            nPars=getNumParsPerTerm(sType);
            startingIndex+=nPars;
            m_nvNumPars.add(nPars);
            m_nNumPars+=nPars;
        }
       updateParNamesAndExpression();
    }
    void updateParNamesAndExpression(){
        m_sFunctionExpression=new StringBuffer();
        m_svParNames.clear();
        getFunctionExpressionAndParNames(m_svComponentFunctionTypes,m_svParNames,m_sFunctionExpression);
    }
    public void addComponent(String sFunctionType){
        int startingIndex=m_svComponentFunctionTypes.size();
        int nPars=getNumParsPerTerm(sFunctionType);
        m_svComponentFunctionTypes.add(sFunctionType);
        m_nvNumPars.add(nPars);
        m_nNumPars+=nPars;
        m_nNumComponents++;
        updateParNamesAndExpression();
    }
    public void removeComponent(int index){
        int nPars=m_nvNumPars.get(index),startingIndex;
        m_svComponentFunctionTypes.remove(index);
        m_nvNumPars.remove(index);
        m_nvStartingParIndexes.remove(index);
        m_nNumComponents=m_svComponentFunctionTypes.size();
        for(int i=index;i<m_nNumComponents;i++){
            startingIndex=m_nvStartingParIndexes.get(i);
            startingIndex-=nPars;
            m_nvStartingParIndexes.set(i, startingIndex);
        }
        m_nNumPars-=nPars;
        updateParNamesAndExpression();
    }
    public double fun(double[] pars, double[] x){
        double dv=pars[0];
        for(int i=0;i<m_nNumComponents;i++){
            dv+=CommonMethods.StandardFunctionValue(pars, m_nvStartingParIndexes.get(i), m_nvNumPars.get(i), x, m_svComponentFunctionTypes.get(i), false);
        }
        return dv;
    };
    public void getEffctiveVarRanges(double[] pars, ArrayList<DoubleRange> cvVarRanges){
        for(int i=0;i<m_nNumComponents;i++){
            CommonMethods. StandardFunctionVarRanges(cvVarRanges,pars, m_nvStartingParIndexes.get(i), m_nvNumPars.get(i),m_svComponentFunctionTypes.get(i), false);
        }
    };
    public double fun(double[] pars, double[] x, double[] pdComponents){//this first elements of pdComponents is the constant
        double dv=pars[0],dvt;
        pdComponents[0]=dv;
        for(int i=0;i<m_nNumComponents;i++){
            dvt=CommonMethods.StandardFunctionValue(pars, m_nvStartingParIndexes.get(i), m_nvNumPars.get(i), x, m_svComponentFunctionTypes.get(i), false);
            pdComponents[i+1]=dvt;
            dv+=dvt;
        }
        return dv;
    };
    public double funValueAndGradient(double[] pars, double[] x, double[] pdGradient){//this first elements of pdVandD is the function value, and the rest are partial derivatives
        double dv=pars[0];//this method is correct as compared to the numerical one. 11815
        pdGradient[0]=1;

        int nPars=pars.length,i;
/*        double I=fun(pars,x),I1,delta;//this block has been used to test the gradien calucation
        double[] pars1=new double[nPars];
        double gradient1[]=new double[nPars];
        double[] pdI=new double[nPars];
        for(i=0;i<nPars;i++){
            delta=Math.max(0.000001*pars[i], 0.00000001);
            CommonStatisticsMethods.copyArray(pars, pars1);
            pars1[i]+=delta;
            I1=fun(pars1,x);
            pdI[i]=I1;
            gradient1[i]=(I1-I)/delta;
        }*/
        for(i=0;i<m_nNumComponents;i++){
            dv+=CommonMethods.StandardFunctionVAndD(pars, m_nvStartingParIndexes.get(i), m_nvNumPars.get(i), x, m_svComponentFunctionTypes.get(i), false,pdGradient);
        }
        return dv;
    };
    public double funPartialDerivative(double[] pars, double[] x, int index){//this first elements of pdVandD is the function value, and the rest are partial derivatives
        if(index==0) return 1;//partial derivative for the constant term
        int nPars=0,num,startingIndex;
        double gradient[]=new double[nPars],dv;
        for(int i=0;i<m_nNumComponents;i++){
            startingIndex=m_nvStartingParIndexes.get(i);
            num=m_nvNumPars.get(i);
            if(index>=startingIndex&&index<startingIndex+num){
                dv=CommonMethods.StandardFunctionVAndD(pars, startingIndex, num, x, m_svComponentFunctionTypes.get(i), false,gradient);
                break;
            }
        }
        return gradient[index];
    };
    public boolean expandable(){
        return true;
    };
    public double[] getExpandedModel(double[] pdPars){
        double[] newPars=null;
        return newPars;
    };
    public double[] getExpandedModel(double[] pdPars,double[] pdLocation){
        return getExpandedModel(pdPars,pdLocation,0);
    };
    public double[] getExpandedModel(double[] pdPars,double[] pdLocation,double diff){
        return null;//not going to need it
    };
    public void getDescriptions(ArrayList<String> svDescript,ArrayList<String> svExpressions,ArrayList<String> svBaseParNames,
            ArrayList<String> svExpandedParNames,ArrayList<Integer> nvParNumbers){        
    };
    public static void getFunctionExpressionAndParNames(ArrayList<String> svFunctionTypes, ArrayList<String> svParNames, StringBuffer sExpression){
        svParNames.clear();
        int nNumFunctionTypes=svFunctionTypes.size(),i,index,nPars,j;
        svParNames.add("Constant");
        for(i=0;i<nNumFunctionTypes;i++){
            getFunctionExpressionAndParNames(svFunctionTypes.get(i),i,sExpression,svParNames);
            nPars=svParNames.size();
        }
    }
    public static int getNumParsPerTerm(String sFunctionType){//not counting the constant
        int nPars=0,terms=0;
        boolean validFunc=false;
        if(sFunctionType.contentEquals("exponential")){
            validFunc=true;
            nPars=2;
        }
        if(sFunctionType.startsWith("polynomial")){
            validFunc=true;
            int index=sFunctionType.lastIndexOf("l");
            String sTerm=sFunctionType.substring(index+1);
            terms=Integer.parseInt(sTerm);
            nPars=terms;
        }
        if(sFunctionType.contentEquals("gaussian")){
            validFunc=true;
            nPars=3;
        }
        if(sFunctionType.contentEquals("gaussian_Hist")){
            validFunc=true;
            nPars=3;
        }
        if(sFunctionType.contentEquals("FluoDelta")){
            validFunc=true;
            nPars=5;
        }
        if(sFunctionType.contentEquals("gaussian2D")){
            validFunc=true;
            nPars=6;
        }
        if(sFunctionType.contentEquals("gaussian2D_GaussianPars")){
            validFunc=true;
            nPars=6;
       }
        if(sFunctionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            nPars=4;
        }
        if(!validFunc) IJ.error("undefined expression type: "+sFunctionType);
        return nPars;
    }
    public static void getFunctionExpressionAndParNames(String sFunctionType, int index0, StringBuffer expression, ArrayList<String> names){
        int i,nPars=0,terms=0;
        boolean validFunc=false;
        String sFunc="";
//        names.clear();
//        names.add("const");
        if(sFunctionType.contentEquals("exponential")){
            validFunc=true;
//            expression.add("a*exp(alpha*x)");
            sFunc+="a*exp(alpha*x)";
            names.add("a"+index0);
            names.add("alpha"+index0);
        }
        if(sFunctionType.startsWith("polynomial")){
            validFunc=true;
            int index=sFunctionType.lastIndexOf("l");
            String sTerm=sFunctionType.substring(index+1);
            terms=Integer.parseInt(sTerm);
            nPars=terms+1;
            for(i=0;i<terms;i++){
                names.add("a"+i+1);
                sFunc+="const"+"a"+(i+1)+"^("+(i+1)+")";
            }
//            expression.add(sFunc);
        }
        if(sFunctionType.contentEquals("gaussian")){
            validFunc=true;
            sFunc+="A"+index0+"*exp(-(x-"+index0+"xc"+index0+")*(x-xc"+index0+")/(2*sigma"+index0+"*sigma"+index0+"))";
            names.add("A"+index0);
            names.add("xc"+index0);
            names.add("sigma"+index0);
        }
        if(sFunctionType.contentEquals("FluoDelta")){
            validFunc=true;
            sFunc+="a*exp(-(x^2/(2*sigma1^2)))+b*exp(-((x-mu)^2/(2*sigma2^2)))+b*exp(-((x+mu)^2/(2*sigma2^2)))";
            names.add("a");
            names.add("b");
            names.add("mu");
            names.add("sigma1");
            names.add("sigma2");
        }
        if(sFunctionType.contentEquals("gaussian_Hist")){//need to update
            validFunc=true;
            sFunc+="A"+index0+"*exp(-(x-"+index0+"xc"+index0+")*(x-xc"+index0+")/(2*sigma"+index0+"*sigma"+index0+"))";
            names.add("A"+index0);
            names.add("xc"+index0);
            names.add("sigma"+index0);
        }
        if(sFunctionType.contentEquals("gaussian2D")){
            validFunc=true;
            names.add("A"+index0);
            names.add("a"+index0);
            names.add("b"+index0);
            names.add("c"+index0);
            names.add("xc"+index0);
            names.add("yc"+index0);
            sFunc+="A"+index0+"*exp(-a"+index0+"*(x-xc"+index0+")*(x-xc"+index0+")-b*(x-xc"+index0+")*(y-yc"+index0+")-c*(y-yc"+index0+")*(y-yc"+index0+")";
        }
        if(sFunctionType.contentEquals("gaussian2D_GaussianPars")){
            validFunc=true;
            names.add("A"+index0);
            names.add("sigmax"+index0);
            names.add("sigmay"+index0);
            names.add("theta"+index0);
            names.add("xc"+index0);
            names.add("yc"+index0);
            sFunc+="A"+index0+"*exp(-a"+index0+"*(x-xc"+index0+")*(x-xc"+index0+")-b*(x-xc"+index0+")*(y-yc"+index0+")-c*(y-yc"+index0+")*(y-yc"+index0+"))";
       }
        if(sFunctionType.contentEquals("gaussian2D_Circular")){
            validFunc=true;
            names.add("A"+index0);
            names.add("sigma"+index0);
            names.add("xc"+index0);
            names.add("yc"+index0);
            sFunc+="A"+index0+"*exp(-((x-xc"+index0+")*(x-xc"+index0+")+(y-yc"+index0+")*(y-yc"+index0+"))/(2*sigma"+index0+"*sigma"+index0+")))";
        }
        if(validFunc) {
            if(expression.length()>1) expression.append("+");
            expression.append(sFunc);
        }
        if(!validFunc) IJ.error("undefined expression type: "+sFunctionType);
    }
    public static double[] getDefaultPars(double[][] pdX, double[] pdY, String sFunctionType){
        int i,nPars=getNumParsPerTerm(sFunctionType)+1,len=pdX.length;
        double[] pdPars=new double[nPars];
        int ws=Math.min(5,len/10);
        if(sFunctionType.contentEquals("gaussian")){
            CommonFunctionValueParameterizer cfp=new CommonFunctionValueParameterizer(pdX,CommonStatisticsMethods.getRunningWindowAverage(pdY, 0, pdX.length-1, 1, ws,false));
            double dMax=cfp.getMax(),dMin=cfp.getMin(),mean=cfp.getMean(),xMax;
            MeanSem1 ms=cfp.getPopulationMeansem(0);
            int iMax=cfp.getMaxPosition();
            xMax=pdX[iMax][0];
            double h=dMax-dMin,w=Math.abs(pdX[0][0]-pdX[len-1][0]),area=mean*w;
            pdPars[0]=dMin;
            pdPars[1]=h;
            pdPars[2]=xMax;
            double sd=ms.getSD();
            if(ms.n<=1) sd=cfp.getSD();
            pdPars[3]=sd;
            return pdPars;
        }
        if(sFunctionType.contentEquals("gaussian_Hist")){
            CommonFunctionValueParameterizer cfp=new CommonFunctionValueParameterizer(pdX,CommonStatisticsMethods.getRunningWindowAverage(pdY, 0, pdX.length-1, 1, ws,false));
            double dMax=cfp.getMax(),dMin=cfp.getMin(),mean=cfp.getMean(),xMax;
            MeanSem1 ms=cfp.getPopulationMeansem(0);
            int iMax=cfp.getMaxPosition();
            xMax=pdX[iMax][0];
            double h=dMax,w=Math.abs(pdX[0][0]-pdX[len-1][0]),area=mean*w;
            pdPars[0]=0;
            pdPars[1]=1;
            pdPars[2]=xMax;
            double sd=ms.getSD();
            if(ms.n<=1) sd=cfp.getSD();
            pdPars[3]=sd;
            ArrayList<String> Types=new ArrayList();
            Types.add(sFunctionType);
            ComposedFittingFunction func=new ComposedFittingFunction(Types);
            double A=func.fun(pdPars,pdX[iMax]);
            pdPars[1]=h/A;
            return pdPars;
        }
        if(sFunctionType.contentEquals("FluoDelta")){
            CommonFunctionValueParameterizer cfp=new CommonFunctionValueParameterizer(pdX,CommonStatisticsMethods.getRunningWindowAverage(pdY, 0, pdX.length-1, 1, ws,false));
            double dMax=cfp.getMax(),dMin=cfp.getMin(),mean=cfp.getMean(),xMax;
            MeanSem1 ms=cfp.getPopulationMeansem(0);
            int iMax=cfp.getMaxPosition();
            xMax=pdX[iMax][0];
            double h=dMax,w=Math.abs(pdX[0][0]-pdX[len-1][0]),sum=ms.n;
            double sd=ms.getSD();
            if(ms.n<=1) sd=cfp.getSD();
            double a=0.8*sum,b=0.2*sum,mu=ms.getSD(),sigma1=sd/2,sigma2=sigma1*1.414;
            pdPars[0]=0;
            pdPars[1]=a/sigma1;
            pdPars[2]=b/sigma2;
            pdPars[3]=mu;
            pdPars[4]=sigma1;
            pdPars[5]=sigma2;
            return pdPars;
        }
        for(i=0;i<nPars;i++){
            pdPars[i]=1;
        }
        pdPars[0]=0;
        return pdPars;
    }
    public double[] getDefaultPars(){
        double[] pdPars=new double[m_nNumPars];
        for(int i=0;i<m_nNumPars;i++){
            pdPars[i]=1;
        }
        return pdPars;
    }
    public double[] getComponentPars(double[] pdPars0,int nComponent){//with constant term
        int nPars=m_nvNumPars.get(nComponent)+1,nStartingIndex=m_nvStartingParIndexes.get(nComponent);//to include the constant term
        double[] pdPars=new double[nPars];
        pdPars[0]=0;
        for(int i=1;i<nPars;i++){
            pdPars[i]=pdPars0[nStartingIndex+i-1];
        }
        return pdPars;
    }
    public ArrayList<String> getComponentParNames(int nComponent){//with constant term
        int nPars=m_nvNumPars.get(nComponent),nStartingIndex=m_nvStartingParIndexes.get(nComponent);//to include the constant term
        ArrayList<String> svParNames=new ArrayList();
        svParNames.add(m_svParNames.get(0));
        for(int i=0;i<nPars;i++){
            svParNames.add(m_svParNames.get(nStartingIndex+i));
        }
        return svParNames;
    }
    public static ArrayList<Integer> getNumParsArray(ArrayList<String> svFunctionTypes){
        ArrayList<Integer> nvNumPars=new ArrayList();
        for(int i=0;i<svFunctionTypes.size();i++){
            nvNumPars.add(getNumParsPerTerm(svFunctionTypes.get(i)));
        }
        return nvNumPars;
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

    public static int getGaussian2DParameters(double[] pdTransformedPars, int o1,double[] pdPars, int o2){//o: offset of the index
        double a,b,c,d,sigmax,sigmay,theta;
        int nParsPerTerm=6;
        int i;
        for(i=0;i<nParsPerTerm;i++){
            pdPars[o2+i]=pdTransformedPars[o1+i];
        }
        double A=pdTransformedPars[o1];
        a=pdTransformedPars[o1+1];
        b=pdTransformedPars[o1+2];
        c=pdTransformedPars[o1+3];

        Double small=0.0000000001;
        if(Math.abs(a-c)<small){
            double dx=2,dy=2;//an arbitary number
            double f=A*Math.exp(-a*dx*dx-b*dy*dx-c*dy*dy);
            sigmax=(dx*dx+dy*dy)/(2*Math.log(A/f));
            sigmax=Math.sqrt(sigmax);
            sigmay=sigmax;
            theta=0.8;//arbitrary, bc its a circular gaussian
        }else{
            d=a/(a+c);
            theta=0.5*Math.atan(2.*b/(c-a));
            double stheta=Math.sin(theta),ctheta=Math.cos(theta),c2theta=Math.cos(2.*theta);
            sigmay=c2theta*(1-2*d)/(2*(c-a)*(ctheta*ctheta-d));
            sigmax=sigmay*(ctheta*ctheta-d)/(d-stheta*stheta);
            sigmax=Math.sqrt(sigmax);
            sigmay=Math.sqrt(sigmay);
        }
        pdPars[o2+1]=sigmax;
        pdPars[o2+2]=sigmay;
        pdPars[o2+3]=theta;
        return 1;
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
    public static void toGaussian2D(double[] pdPars, ArrayList<String> svFunctionTypes,ArrayList<Integer> nvTransformedGaussianComponents){//transforms fitting functions (if exists) from "gaussian2D_GaussianPars" to "gaussian2D"
        nvTransformedGaussianComponents.clear();
        int i,nPars=1,nComponents=svFunctionTypes.size();
        String sType;
        for(i=0;i<nComponents;i++){
            sType=svFunctionTypes.get(i);
            if(sType.contentEquals("gaussian2D_GaussianPars")){
                ComposedFittingFunction.getTransformedGaussian2DParameters(pdPars, nPars, pdPars, nPars);
                svFunctionTypes.set(i, "gaussian2D");
                nvTransformedGaussianComponents.add(i);
            }
            nPars+=ComposedFittingFunction.getNumParsPerTerm(sType);
        }
    }
    public static void toGaussian2D_GaussianPars(double[] pdPars, ArrayList<String> svFunctionTypes, ArrayList<Integer> nvTransformedGaussianComponents){//transforms fitting functions (if exists) from "gaussian2D" to "gaussian2D_GaussianPars"
        int i,nPars=1,nComponents=svFunctionTypes.size();
        for(i=0;i<nComponents;i++){
            if(svFunctionTypes.get(i).contentEquals("gaussian2D")&&CommonMethods.containsContent(nvTransformedGaussianComponents, i)){
                ComposedFittingFunction.getGaussian2DParameters(pdPars, nPars, pdPars, nPars);
                svFunctionTypes.set(i, "gaussian2D_GaussianPars");
            }
            nPars+=ComposedFittingFunction.getNumParsPerTerm(svFunctionTypes.get(i));
        }
    }
}
