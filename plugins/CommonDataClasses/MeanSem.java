/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package CommonDataClasses;

/**
 *
 * @author Taihao
 */
public class MeanSem {
    public double mean;
    public double sem;
    public double dev;
    public int size;
    public MeanSem(){
        mean=0.;
        sem=0.;
        dev=0.;
        size=0;
    }
    public MeanSem(double mean,double sem, double dev, int size){
        this.mean=mean;
        this.sem=sem;
        this.dev=dev;
        this.size=size;
    }
}
