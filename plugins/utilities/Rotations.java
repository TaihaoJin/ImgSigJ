/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;

/**
 *
 * @author Taihao
 */
public class Rotations {
    public Rotations(){ };
    public static void rotationX(double x[], double angle){
        double a=Angles.radian(angle);
        double rm[][]=new double[3][3];
        rm[0][0]=1;
        rm[0][1]=0;
        rm[0][2]=0;
        rm[1][0]=0;
        rm[1][1]=Math.cos(a);
        rm[1][2]=Math.sin(a);
        rm[2][0]=0;
        rm[2][1]=-Math.sin(a);
        rm[2][2]=Math.cos(a);
        rotation(x,rm);
    }
    public static void rotationY(double x[], double angle){
        double a=Angles.radian(angle);
        double rm[][]=new double[3][3];
        rm[0][0]=Math.cos(a);
        rm[0][1]=0.;
        rm[0][2]=-Math.sin(a);
        rm[1][0]=0.;
        rm[1][1]=1.;
        rm[1][2]=0.;
        rm[2][0]=Math.sin(a);
        rm[2][1]=0.;
        rm[2][2]=Math.cos(a);
        rotation(x,rm);
    }
    public static void rotationZ(double x[], double angle){
        double a=Angles.radian(angle);
        double rm[][]=new double[3][3];
        rm[0][0]=Math.cos(a);
        rm[0][1]=Math.sin(a);
        rm[0][2]=0;
        rm[1][0]=-Math.sin(a);
        rm[1][1]=Math.cos(a);
        rm[1][2]=0.;
        rm[2][0]=0;
        rm[2][1]=0.;
        rm[2][2]=1;
        rotation(x,rm);
    }
    public static void rotation(double x0[],double rm[][]){
        double x[]=new double[3];
        int i,j;
        for(i=0;i<3;i++){
            x[i]=0;
            for(j=0;j<3;j++){
                x[i]+=x0[j]*rm[i][j];
            }
        }
        for(i=0;i<3;i++){
            x0[i]=x[i];
        }
    }
    public static void rotationX(float x[], float angle){
        float a=(float)Angles.radian(angle);
        float rm[][]=new float[3][3];
        rm[0][0]=(float)1.;
        rm[0][1]=(float)0.;
        rm[0][2]=(float)0.;
        rm[1][0]=(float)0.;
        rm[1][1]=(float)Math.cos(a);
        rm[1][2]=(float)Math.sin(a);
        rm[2][0]=(float)0.;
        rm[2][1]=-(float)Math.sin(a);
        rm[2][2]=(float)Math.cos(a);
        rotation(x,rm);
    }
    public static void rotationY(float x[], float angle){
        float a=(float)Angles.radian(angle);
        float rm[][]=new float[3][3];
        rm[0][0]=(float)Math.cos(a);
        rm[0][1]=(float)0.;
        rm[0][2]=-(float)Math.sin(a);
        rm[1][0]=(float)0.;
        rm[1][1]=(float)1.;
        rm[1][2]=(float)0.;
        rm[2][0]=(float)Math.sin(a);
        rm[2][1]=(float)0.;
        rm[2][2]=(float)Math.cos(a);
        rotation(x,rm);
    }
    public static void rotationZ(float x[], float angle){
        float a=(float)Angles.radian(angle);
        float rm[][]=new float[3][3];
        rm[0][0]=(float)Math.cos(a);
        rm[0][1]=(float)Math.sin(a);
        rm[0][2]=0;
        rm[1][0]=-(float)Math.sin(a);
        rm[1][1]=(float)Math.cos(a);
        rm[1][2]=(float)0.;
        rm[2][0]=(float)0.;
        rm[2][1]=(float)0.;
        rm[2][2]=(float)1.;
        rotation(x,rm);
    }
    public static void rotation(float x0[],float rm[][]){
        float x[]=new float[3];
        int i,j;
        for(i=0;i<3;i++){
            x[i]=0;
            for(j=0;j<3;j++){
                x[i]+=x0[j]*rm[i][j];
            }
        }
        for(i=0;i<3;i++){
            x0[i]=x[i];
        }
    }
}
