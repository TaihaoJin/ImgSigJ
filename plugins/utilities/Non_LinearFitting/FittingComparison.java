/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.distribution.FDistributionImpl;
import org.apache.commons.math.MathException;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class FittingComparison {
    public static double getFittingComparison(FittingModelNode model1, FittingModelNode model2){
        return getFittingComparison_LeastSquare(model1.SSE,model1.getNumFreePars(),model2.SSE,model2.getNumFreePars(),model1.m_pdY.length);
    }
    public static double getFittingComparison_LeastSquare(double sse1, double df1, double sse2, double df2, double n){
        //sse is the sum of the square error of the fitting, n is the number of data points in the fitting. return the p value
        double t=(sse1-sse2)*(n-df2)/(df1*sse2);
        double df1F=df1, df2F=n-df2;
        FDistributionImpl FDist=new FDistributionImpl(df1F,df2F);
        double p=1;
        try{p=1-FDist.cumulativeProbability(t);}
        catch(MathException e) {IJ.error("MathException during getFittingComparison_LeastSquare");}
        return p;
    }
    public static double getFittingComparison_MaximumLikelihood(double df1, double df2, double LLR){
        //LLR is the natural logarithm of the likelihood ratio. return the p value
        double df=df2-df1;
        double chi2=2*LLR;
        ChiSquaredDistributionImpl chi2_dist=new ChiSquaredDistributionImpl(df);
        double p=1;
        try{p=1-chi2_dist.cumulativeProbability(chi2);}
        catch(MathException e) {IJ.error("MathException during getFittingComparison_MaximumLikelihood");}
        return p;
    }
}
