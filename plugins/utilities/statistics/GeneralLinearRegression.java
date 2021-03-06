/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.statistics;
import utilities.Non_LinearFitting.ComposedFittingFunction;
import ij.IJ;
import utilities.CommonStatisticsMethods;
/**
 *
 * @author Taihao
 */
public class GeneralLinearRegression {

public static double svdfit(double[] x, double[] y, double[] pbSD, double[] pdPars,
    double[][] u, double[][] v, double [] w,LinearFittingFunction funcs)
    {
            int i,j;
            final double TOL=1.0e-13;
            double wmax,tmp,thresh,sum,dv,chisq;

            int ndata=x.length;
            int ma=pdPars.length;
//            Vec_DP b(ndata),afunc(ma);
            double[] b=new double[ndata], afunc=new double[ma];
            for (i=0;i<ndata;i++) {
//                    funcs(x[i],afunc);
                funcs.getComponents(x[i], afunc);
                tmp=1.0/pbSD[i];
                for (j=0;j<ma;j++) u[i][j]=afunc[j]*tmp;
                b[i]=y[i]*tmp;
            }
            
            if(!CommonStatisticsMethods.regularDoubleArray(b))
                return Double.NaN;
            svdcmp(u,w,v);
            wmax=0.0;
            for (j=0;j<ma;j++)
                    if (w[j] > wmax) wmax=w[j];
            thresh=TOL*wmax;
            for (j=0;j<ma;j++)
                    if (w[j] < thresh) w[j]=0.0;
            svbksb(u,w,v,b,pdPars);
            chisq=0.0;
            for (i=0;i<ndata;i++) {
                sum=funcs.fun(pdPars,x[i]);
                tmp=(y[i]-sum)/pbSD[i];
                chisq += tmp*tmp;
//                    chisq += (tmp=(y[i]-sum)/pbSD[i],tmp*tmp);
            }
            return chisq;
    }
    static public void svbksb(double[][] u, double[] w, double[][] v, double[]b, double[]x)
    {
            int jj,j,i;
            double s;

            int m=u.length;
            int n=u[0].length;
            double[] tmp=new double[n];
            for (j=0;j<n;j++) {
                    s=0.0;
                    if (w[j] != 0.0) {
                            for (i=0;i<m;i++) s += u[i][j]*b[i];
                            s /= w[j];
                    }
                    tmp[j]=s;
            }
            for (j=0;j<n;j++) {
                    s=0.0;
                    for (jj=0;jj<n;jj++) s += v[j][jj]*tmp[jj];
                    x[j]=s;
            }
    }

    static public void svdvar(double[][] v, double[] w, double[][] cvm)
    {
            int i,j,k;
            double sum;

            int ma=w.length;
            double[] wti=new double[ma];
            for (i=0;i<ma;i++) {
                    wti[i]=0.0;
                    if (w[i] != 0.0) wti[i]=1.0/(w[i]*w[i]);
            }
            for (i=0;i<ma;i++) {
                    for (j=0;j<i+1;j++) {
                            sum=0.0;
                            for (k=0;k<ma;k++)
                                    sum += v[i][k]*v[j][k]*wti[k];
                            cvm[j][i]=cvm[i][j]=sum;
                    }
            }
    }
    public static double SIGN(double a, double b){
        a=Math.abs(a);
        if(b>0) return a;
        return -a;
    }
    public static void svdcmp(double[][] a, double[] w, double[][] v)
    {
            boolean flag;
            int i,its,j,jj,k,l=0,nm=0;
            double anorm,c,f,g,h,s,scale,x,y,z;

            int m=a.length;
            int n=a[0].length;
            double[] rv1=new double[n];
            g=scale=anorm=0.0;
            for (i=0;i<n;i++) {
                    l=i+2;
                    rv1[i]=scale*g;
                    g=s=scale=0.0;
                    if (i < m) {
                            for (k=i;k<m;k++) scale += Math.abs(a[k][i]);
                            if (scale != 0.0) {
                                    for (k=i;k<m;k++) {
                                            a[k][i] /= scale;
                                            s += a[k][i]*a[k][i];
                                    }
                                    f=a[i][i];
                                    g = -SIGN(Math.sqrt(s),f);
                                    h=f*g-s;
                                    a[i][i]=f-g;
                                    for (j=l-1;j<n;j++) {
                                            for (s=0.0,k=i;k<m;k++) s += a[k][i]*a[k][j];
                                            f=s/h;
                                            for (k=i;k<m;k++) a[k][j] += f*a[k][i];
                                    }
                                    for (k=i;k<m;k++) a[k][i] *= scale;
                            }
                    }
                    w[i]=scale *g;
                    g=s=scale=0.0;
                    if (i+1 <= m && i+1 != n) {
                            for (k=l-1;k<n;k++) scale += Math.abs(a[i][k]);
                            if (scale != 0.0) {
                                    for (k=l-1;k<n;k++) {
                                            a[i][k] /= scale;
                                            s += a[i][k]*a[i][k];
                                    }
                                    f=a[i][l-1];
                                    g = -SIGN(Math.sqrt(s),f);
                                    h=f*g-s;
                                    a[i][l-1]=f-g;
                                    for (k=l-1;k<n;k++) rv1[k]=a[i][k]/h;
                                    for (j=l-1;j<m;j++) {
                                            for (s=0.0,k=l-1;k<n;k++) s += a[j][k]*a[i][k];
                                            for (k=l-1;k<n;k++) a[j][k] += s*rv1[k];
                                    }
                                    for (k=l-1;k<n;k++) a[i][k] *= scale;
                            }
                    }
                    anorm=Math.max(anorm,(Math.abs(w[i])+Math.abs(rv1[i])));
            }
            for (i=n-1;i>=0;i--) {
                    if (i < n-1) {
                            if (g != 0.0) {
                                    for (j=l;j<n;j++)
                                            v[j][i]=(a[i][j]/a[i][l])/g;
                                    for (j=l;j<n;j++) {
                                            for (s=0.0,k=l;k<n;k++) s += a[i][k]*v[k][j];
                                            for (k=l;k<n;k++) v[k][j] += s*v[k][i];
                                    }
                            }
                            for (j=l;j<n;j++) v[i][j]=v[j][i]=0.0;
                    }
                    v[i][i]=1.0;
                    g=rv1[i];
                    l=i;
            }
            for (i=Math.min(m,n)-1;i>=0;i--) {
                    l=i+1;
                    g=w[i];
                    for (j=l;j<n;j++) a[i][j]=0.0;
                    if (g != 0.0) {
                            g=1.0/g;
                            for (j=l;j<n;j++) {
                                    for (s=0.0,k=l;k<m;k++) s += a[k][i]*a[k][j];
                                    f=(s/a[i][i])*g;
                                    for (k=i;k<m;k++) a[k][j] += f*a[k][i];
                            }
                            for (j=i;j<m;j++) a[j][i] *= g;
                    } else for (j=i;j<m;j++) a[j][i]=0.0;
                    ++a[i][i];
            }
            for (k=n-1;k>=0;k--) {
                    for (its=0;its<30;its++) {
                            flag=true;
                            for (l=k;l>=0;l--) {
                                    nm=l-1;
                                    if (Math.abs(rv1[l])+anorm == anorm) {
                                            flag=false;
                                            break;
                                    }
                                    if(nm<0){
                                        nm=nm;
                                        break;
                                    }
                                    if (Math.abs(w[nm])+anorm == anorm) break;
                            }
                            if (flag) {
                                    c=0.0;
                                    s=1.0;
                                    for (i=l;i<k+1;i++) {
                                            f=s*rv1[i];
                                            rv1[i]=c*rv1[i];
                                            if (Math.abs(f)+anorm == anorm) break;
                                            g=w[i];
                                            h=pythag(f,g);
                                            w[i]=h;
                                            h=1.0/h;
                                            c=g*h;
                                            s = -f*h;
                                            for (j=0;j<m;j++) {
                                                    y=a[j][nm];
                                                    z=a[j][i];
                                                    a[j][nm]=y*c+z*s;
                                                    a[j][i]=z*c-y*s;
                                            }
                                    }
                            }
                            z=w[k];
                            if (l == k) {
                                    if (z < 0.0) {
                                            w[k] = -z;
                                            for (j=0;j<n;j++) v[j][k] = -v[j][k];
                                    }
                                    break;
                            }
                            if (its == 29) IJ.error("no convergence in 30 svdcmp iterations");
                            x=w[l];
                            nm=k-1;
                            y=w[nm];
                            g=rv1[nm];
                            h=rv1[k];
                            f=((y-z)*(y+z)+(g-h)*(g+h))/(2.0*h*y);
                            g=pythag(f,1.0);
                            f=((x-z)*(x+z)+h*((y/(f+SIGN(g,f)))-h))/x;
                            c=s=1.0;
                            for (j=l;j<=nm;j++) {
                                    i=j+1;
                                    g=rv1[i];
                                    y=w[i];
                                    h=s*g;
                                    g=c*g;
                                    z=pythag(f,h);
                                    rv1[j]=z;
                                    c=f/z;
                                    s=h/z;
                                    f=x*c+g*s;
                                    g=g*c-x*s;
                                    h=y*s;
                                    y *= c;
                                    for (jj=0;jj<n;jj++) {
                                            x=v[jj][j];
                                            z=v[jj][i];
                                            v[jj][j]=x*c+z*s;
                                            v[jj][i]=z*c-x*s;
                                    }
                                    z=pythag(f,h);
                                    w[j]=z;
//                                    if (z) {
                                    if (z!=0) {
                                            z=1.0/z;
                                            c=f*z;
                                            s=h*z;
                                    }
                                    f=c*g+s*y;
                                    x=c*y-s*g;
                                    for (jj=0;jj<m;jj++) {
                                            y=a[jj][j];
                                            z=a[jj][i];
                                            a[jj][j]=y*c+z*s;
                                            a[jj][i]=z*c-y*s;
                                    }
                            }
                            rv1[l]=0.0;
                            rv1[k]=f;
                            w[k]=x;
                    }
            }
    }

    public static double pythag(double a, double b)
    {
            double absa,absb;

            absa=Math.abs(a);
            absb=Math.abs(b);
            if (absa > absb) return absa*Math.sqrt(1.0+SQR(absb/absa));
            else return (absb == 0.0 ? 0.0 : absb*Math.sqrt(1.0+SQR(absa/absb)));
    }
    public static double SQR(double a){
        return a*a;
    }
}
