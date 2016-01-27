/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;

public class GaussianDistribution {//downloaded from http://www.cs.princeton.edu/introcs/21function/ErrorFunction.java.html
    // absolute value
    public static double abs(double x) {
        if      (x == 0.0) return  x;    // for -0 and +0
        else if (x >  0.0) return  x;
        else               return -x;
    }

    // exponentiation - special case for negative input improves accuracy
    public static double exp(double x) {
        double term = 1.0;
        double sum  = 1.0;
        for (int N = 1; sum != sum + term; N++) {
            term = term * Math.abs(x) / N;
            sum  = sum + term;
        }
        if (x >= 0) return sum;
        else        return 1.0 / sum;
    }

    // calculate square root using Newton's method
    public static double sqrt(double c) {
        if (c == 0) return c;
        if (c <  0) return Double.NaN;
        double EPSILON = 1E-15;
        double t = c;
        while (Math.abs(t - c/t) > EPSILON * t) {
            t = (c/t + t) / 2.0;
        }
        return t;
    }

    // fractional error in math formula less than 1.2 * 10 ^ -7.
    // although subject to catastrophic cancellation when z in very close to 0
    public static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        // use Horner's method
        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                                            t * ( 1.00002368 +
                                            t * ( 0.37409196 +
                                            t * ( 0.09678418 +
                                            t * (-0.18628806 +
                                            t * ( 0.27886807 +
                                            t * (-1.13520398 +
                                            t * ( 1.48851587 +
                                            t * (-0.82215223 +
                                            t * ( 0.17087277))))))))));
        if (z >= 0) return  ans;
        else        return -ans;
    }

    // fractional error less than x.xx * 10 ^ -4.
    public static double erf2(double z) {
        double t = 1.0 / (1.0 + 0.47047 * Math.abs(z));
        double poly = t * (0.3480242 + t * (-0.0958798 + t * (0.7478556)));
        double ans = 1.0 - poly * Math.exp(-z*z);
        if (z >= 0) return  ans;
        else        return -ans;
    }

    public static double phi(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI);
    }

    public static double phi(double x, double mu, double sigma) {
        return phi((x - mu) / sigma) / sigma;
    }

    // accurate with absolute error less than 8 * 10^-16
    // Reference: http://www.jstatsoft.org/v11/i04/v11i04.pdf
    public static double Phi2(double z) {
        if (z >  8.0) return 1.0;    // needed for large values of z
        if (z < -8.0) return 0.0;    // probably not needed
        double sum = 0.0, term = z;
        for (int i = 3; sum + term != sum; i += 2) {
            sum  = sum + term;
            term = term * z * z / i;
        }
        return 0.5 + sum * phi(z);
    }

    // cumulative normal distribution
    public static double Phi(double z) {
        return 0.5 * (1.0 + erf(z / (Math.sqrt(2.0))));
    }

    // cumulative normal distribution with mean mu and std deviation sigma
    public static double Phi(double z, double mu, double sigma) {
        return Phi((z - mu) / sigma);
    }

    public static double getZatP(double p, double mu, double sigma, double accuracy) {//get z value at which the cummulative distribution is p
        double z=mu;
        int sign=1;
        if(p<0.5) sign=-1;
        double z0=mu;
        double z1=z0+sign*sigma;
        double p0=0.5,p1=Phi(z1,mu,sigma);
        while((p1-p)*(p0-p)>0){
            z0=z1;
            z1+=sign*sigma;
            p0=Phi(z0,mu,sigma);
            p1=Phi(z1,mu,sigma);
        }
        z=0.5*(z0+z1);
        if(Math.abs(z-z0)<accuracy) return z0;
        if(Math.abs(z-z1)<accuracy) return z1;
        double pt=Phi(z,mu,sigma);
        while(Math.abs(z1-z0)>accuracy){
            if((pt-p)*sign>0){
                z1=z;
                p1=pt;
            }else{
                z0=z;
                p0=pt;
            }
            z=0.5*(z0+z1);
            pt=Phi(z,mu,sigma);
        }
        return z;
    }

    // random integer between 0 and N-1
    public static int random(int N) {
        return (int) (Math.random() * N);
    }

    // random number with standard Gaussian distribution
    public static double gaussian() {
        double U = Math.random();
        double V = Math.random();
        return Math.sin(2 * Math.PI * V) * Math.sqrt((-2 * Math.log(1 - U)));
    }

    // random number with Gaussian distribution of mean mu and stddev sigma
    public static double gaussian(double mu, double sigma) {
        return mu + sigma * gaussian();
    }


   /*************************************************************************
    *  Hyperbolic trig functions
    *************************************************************************/
    public static double cosh(double x) {
        return (Math.exp(x) + Math.exp(-x)) / 2.0;
    }

    public static double sinh(double x) {
        return (Math.exp(x) - Math.exp(-x)) / 2.0;
    }

    public static double tanh(double x) {
        return sinh(x) / cosh(x);
    }
}
