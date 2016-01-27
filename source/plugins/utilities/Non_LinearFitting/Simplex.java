/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Non_LinearFitting;
import ij.IJ;
import utilities.Non_LinearFitting.Fitting_Function;

/**
 *
 * @author Taihao
 */
public class Simplex {
    int NMAX=5000;//Maximum allowed number of function evaluations.
    double little=0.001;
    int m_nDim;
    int m_nPts;
    int m_nIter;
    double psum[];
    double ptry[],y[];
    double[][] p;
    Non_Linear_Fitter m_cFitter;
    public Simplex(double [] pars, Non_Linear_Fitter fitter){
        this(pars,fitter,1);
    }

    public Simplex(double [] pars, Non_Linear_Fitter fitter, double scale){
        m_cFitter=fitter;
        m_nDim=pars.length;
        m_nPts=m_nDim+1;
        p=new double[m_nDim+1][m_nDim];
        y=new double[m_nDim+1];
        double dP;
        int i,j;
        for(j=0;j<m_nDim;j++){
            p[0][j]=pars[j];
        }
        
        for(i=1;i<=m_nDim;i++){
            for(j=0;j<m_nDim;j++){
                dP=pars[j];
                p[i][j]=dP;
                dP*=scale;
                if(i==j+1){
                    if(Math.abs(dP)<little) {
                        if(dP<0)
                            dP=-little;
                        else
                            dP=little;
                    }
                    p[i][j]+=dP;
                }
            }
        }

        for(i=0;i<=m_nDim;i++){
            y[i]=fitter.fun(p[i]);
        }
    }
    void GET_PSUM(){
        double sum=0.0;
        int i,j;
        for (j=0;j<m_nDim;j++) {
            sum=0;
            for (i=0;i<m_nPts;i++){
                sum += p[i][j];
            }
            psum[j]=sum;
        }
    }
    double[] vector(double dv, int nDim){
        double[] pd=new double[nDim];
        for(int i=0;i<nDim;i++){
            pd[i]=dv;
        }
        return pd;
    }
    
    void SWAP(double[] pdV, int i1, int i2){
        double dt=pdV[i1];
        pdV[i1]=pdV[i2];
        pdV[i2]=dt;
    }

    void SWAP(double[][] pdV, int i1, int j1, int i2, int j2){
        double dt=pdV[i1][j1];
        pdV[i1][j1]=pdV[i2][j2];
        pdV[i2][j2]=dt;
    }

    public void amoeba(double ftol){
        amoeba(p,y,m_nDim,ftol,m_cFitter);
    }

    public void amoeba(double p[][], double y[], int ndim, double ftol,
        Non_Linear_Fitter fitter)
        /*Multidimensional minimization of the function funk(x) where x[1..ndim] is a vector in ndim
        dimensions, by the downhill simplex method of Nelder and Mead. The matrix p[1..ndim+1]
        [1..ndim] is input. Its ndim+1 rows are ndim-dimensional vectors which are the vertices of
        the starting simplex. Also input is the vector y[1..ndim+1], whose components must be preinitialized
        to the values of funk evaluated at the ndim+1 vertices (rows) of p; and ftol the
        fractional convergence tolerance to be achieved in the function value (n.b.!). On output, p and
        y will have been reset to ndim+1 new points all within ftol of a minimum function value, and
        nfunk gives the number of function evaluations taken.*/
        {
        //double amotry(float **p, float y[], float psum[], int ndim,
        //float (*funk)(float []), int ihi, float fac);
            int i,ihi,ilo,inhi,j,mpts=ndim+1;
            double rtol,sum,swap,ysave,ytry;
            this.p=p;
            this.m_nDim=ndim;
            this.m_nPts=mpts;
            psum=vector(1,ndim);
            ptry=vector(1,ndim);
            m_nIter=0;
            GET_PSUM();
            while (true) {
                ilo=0;
            //First we must determine which point is the highest (worst), next-highest, and lowest
            //(best), by looping over the points in the simplex.
//            ihi = y[1]>y[2] ? (inhi=2,1) : (inhi=1,2);
                ihi=0;
                inhi=1;
                if(y[0]<y[1]){
                    ihi=1;
                    inhi=0;
                }
                for (i=1;i<mpts;i++) {
                    if (y[i] <= y[ilo]) ilo=i;
                    if (y[i] > y[ihi]) {
                        inhi=ihi;
                        ihi=i;
                    } else if (y[i] > y[inhi] && i != ihi) {
                        inhi=i;
                    }
                }
                rtol=2.0*Math.abs(y[ihi]-y[ilo])/(Math.abs(y[ihi])+Math.abs(y[ilo]));
                    //Compute the fractional range from highest to lowest and return if satisfactory.
                if (rtol < ftol) { //If returning, put best point and value in slot 0.
                    SWAP(y,0,ilo);
                    for (i=0;i<ndim;i++) {
                        //SWAP(p[1][i],p[ilo][i])
                        SWAP(p,0,i,ilo,i);
                    }
                    break;
                }
                if (m_nIter >= NMAX) IJ.error("NMAX exceeded");
                m_nIter += 2;
                    //Begin a new iteration. First extrapolate by a factor −1 through the face of the simplex
                    //across from the high point, i.e., reflect the simplex from the high point.
                ytry=amotry(p,y,psum,ndim,fitter,ihi,-1.0);
                if (ytry <= y[ilo])
                        //Gives a result better than the best point, so try an additional extrapolation by a factor 2.
                    ytry=amotry(p,y,psum,ndim,fitter,ihi,2.0);
                else if (ytry >= y[inhi]) {
                            //The reflected point is worse than the second-highest, so look for an intermediate lower point,
                            //i.e., do a one-dimensional contraction.
                        ysave=y[ihi];
                        ytry=amotry(p,y,psum,ndim,fitter,ihi,0.5);
                        if (ytry >= ysave) { //Can't seem to get rid of that high point. Better contract around
                            for (i=0;i<mpts;i++) { //the lowest (best) point.
                                if (i != ilo) {
                                    for (j=0;j<ndim;j++){
                                        p[i][j]=0.5*(p[i][j]+p[ilo][j]);
                                        psum[j]=0.5*(p[i][j]+p[ilo][j]);
                                    }
                                    y[i]=fitter.fun(psum);
                                }
                            }
                            m_nIter += ndim;// Keep track of function evaluations.
                            GET_PSUM();// Recompute psum.
                        }
                } else m_nIter--; //Correct the evaluation count.
            } //Go back for the test of doneness and the next iteration.
    }

//    double amotry(double [][]p, double y[], double psum[], int ndim,
//    float (*funk)(float []), int ihi, float fac)
    double amotry(double [][]p, double y[], double psum[], int ndim, Non_Linear_Fitter fitter,
    int ihi, double fac)
//    Extrapolates by a factor fac through the face of the simplex across from the high point, tries
//    it, and replaces the high point if the new point is better.
    {
        int j;
        double fac1,fac2,ytry;
        ptry=vector(1,ndim);
        fac1=(1.0-fac)/ndim;
        fac2=fac1-fac;
        for (j=0;j<ndim;j++) ptry[j]=psum[j]*fac1-p[ihi][j]*fac2;
        ytry=fitter.fun(ptry); //Evaluate the function at the trial point.
        if (ytry < y[ihi]) { //If it’s better than the highest, then replace the highest.
            y[ihi]=ytry;
            for (j=0;j<ndim;j++) {
                psum[j] += ptry[j]-p[ihi][j];
                p[ihi][j]=ptry[j];
            }
        }
        return ytry;
    }
    public void getFittingResults(double[][] p, double[] y){
        for(int i=0;i<=m_nDim;i++){
            y[i]=this.y[i];
            for(int j=0;j<m_nDim;j++){
                p[i][j]=this.p[i][j];
            }
        }
    }
    public double[] getFittedPars(){
        return p[0];
    }
    double[][] getSimplex(){
        return p;
    }
    double[] getVertexHeights(){
        return y;
    }
}