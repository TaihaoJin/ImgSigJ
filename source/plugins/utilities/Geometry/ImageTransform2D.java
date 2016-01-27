/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Geometry;
import java.awt.Point;
import java.awt.geom.Point2D.Double;

/**
 *
 * @author Taihao
 */
public class ImageTransform2D {
    double mTr[][],tr[][],t[][],rTr[][];
    public ImageTransform2D(){
        mTr=new double[3][3];
        rTr=new double[3][3];
        tr=new double[3][3];
        t=new double[3][3];
        setToIdentity(mTr);
        setToIdentity(tr);
    }
    void buildReverse(){
        int i,j;
        for(i=0;i<3;i++){
            for(j=0;j<3;j++){
                if(i==j){
                    rTr[i][j]=mTr[i][j];
                }else{
                    rTr[i][j]=-mTr[i][j];
                }
            }
        }
    }
    public void setToRotation(double degree){//the image rotate counter clockwise.
        setToIdentity(mTr);
        double r=degree*Math.PI/180.;
        tr[0][0]=Math.cos(r);
        tr[0][1]=Math.sin(r);
        tr[0][2]=0.;
        tr[1][0]=-Math.sin(r);
        tr[1][1]=Math.cos(r);
        tr[1][2]=0.;
        tr[2][0]=0.;
        tr[2][1]=0.;
        tr[2][2]=1.;

        apply();

    }
    void apply(){
        int i,j,k;
        for(i=0;i<3;i++){
            for(j=0;j<3;j++){
                t[i][j]=0.;
                for(k=0;k<3;k++){
                    t[i][j]+=tr[i][k]*mTr[k][j];
                }
            }
        }
        for(i=0;i<3;i++){
            for(j=0;j<3;j++){
                mTr[i][j]=t[i][j];
            }
        }
    }
    public void setToTranslation(int tx, int ty){
        setToIdentity(mTr);
        tr[0][0]=1.;
        tr[0][1]=0.;
        tr[0][2]=tx;
        tr[1][0]=0.;
        tr[1][1]=1.;
        tr[1][2]=ty;
        tr[2][0]=0.;
        tr[2][1]=0.;
        tr[2][2]=1.;
        apply();
    }
    public void setToRotation(int vecx, int vecy){//the pixel (vecx,vecy) will be on the positive x axis afer the rotation.
        double degree=degree(vecx,vecy);
        setToRotation(-degree);
    }
    public double degree(int vecx, int vecy){//The degree is the negative of the amount of counter clockwise rotation needed to align the vector to the positive x axis.
        double degree=0.;
        if(vecy==0){
            if(vecx>=0){
                degree=0.;
            }else{
                degree=180;
            }
        }else{
            if(vecx==0){
                if(vecy<0) degree=90;
                else degree=-90;
            }else{
                double signY=1.;
                if(vecy<0) signY=-1.;
                degree=-signY*Math.acos((double)vecx/Math.sqrt(vecx*vecx+vecy*vecy))*180/Math.PI;
            }
        }
        return degree;
    }
    void setToIdentity(double[][] trm){
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                trm[i][j]=0.;
                if(i==j){
                    trm[i][j]=1.;
                }
            }
        }
    }

    public void transform(java.awt.geom.Point2D.Double p0, java.awt.geom.Point2D.Double p){
        double x=p0.x, y=p0.y;
        double px=x*mTr[0][0]+y*mTr[0][1]+mTr[0][2];
        double py=x*mTr[1][0]+y*mTr[1][1]+mTr[1][2];
        p.setLocation(px, py);
    }

    public void rTransform(java.awt.geom.Point2D.Double p0, java.awt.geom.Point2D.Double p){
        double x=p0.x, y=p0.y;
        buildReverse();
        double px=x*rTr[0][0]+y*rTr[0][1]+rTr[0][2];
        double py=x*rTr[1][0]+y*rTr[1][1]+rTr[1][2];
        p.setLocation(px, py);
    }

    public void transform(Point p0, Point p){
        double x=p0.getX(), y=p0.getY();
        int px=(int)(x*mTr[0][0]+y*mTr[0][1]+mTr[0][2]+0.5);
        int py=(int)(x*mTr[1][0]+y*mTr[1][1]+mTr[1][2]+0.5);
        p.setLocation(px, py);
    }

    public void rTransform(Point p0, Point p){
        double x=p0.getX(), y=p0.getY();
        buildReverse();
        int px=(int)(x*rTr[0][0]+y*rTr[0][1]+rTr[0][2]+0.5);
        int py=(int)(x*rTr[1][0]+y*rTr[1][1]+rTr[1][2]+0.5);
        p.setLocation(px, py);
    }

    public void transform(Point[]pts0, Point[]pts){
        int len=pts0.length;
        for(int i=0;i<len;i++){
            transform(pts0[i],pts[i]);
        }
    }
}
