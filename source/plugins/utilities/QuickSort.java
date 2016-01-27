/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.ArrayList;
public class QuickSort {
    private static long comparisons = 0;
    private static long exchanges   = 0;

   /***********************************************************************
    *  Quicksort code from Sedgewick 7.1, 7.2.
    ***********************************************************************/
    public static void quicksort(ArrayList<Double> a){
        int len=a.size();
        double d[]=new double[len];
        for(int i=0;i<len;i++){
            d[i]=a.get(i);
        }
        quicksort(d);
        a.clear();
        for(int i=0;i<len;i++){
            a.add(d[i]);
        }
    }
    public static void quicksort(double[] a) {
        shuffle(a);                        // to guard against worst-case
        quicksort(a, 0, a.length - 1);
    }

    public static void quicksort(double[] a, int[]indexes) {//the integers in b will be sorted together with a, ascending order of a's elements
        shuffle(a,indexes);                        // to guard against worst-case
        quicksort(a, indexes, 0, a.length - 1);
    }

    public static void quicksort_external(double[] a, int[]indexes, int indexI, int indexF) {//sort the elements from indexI to indexF (inclusive) only. the integers in b will be sorted together with a, ascending order of a's elements
        shuffle(a,indexes, indexI, indexF);                        // to guard against worst-case
        quicksort(a, indexes, indexI, indexF);
    }

    public static void quicksort(ArrayList<Double> ar, ArrayList <Integer> br) {//the integers in br will be sorted together with ar, ascending order of a's elements
        int size=ar.size();
        double [] a=new double[size];
        int[] b=new int[size];
        int i;
        for(i=0;i<size;i++){
            a[i]=ar.get(i);
            b[i]=br.get(i);
        }
        shuffle(a,b);                        // to guard against worst-case
        quicksort(a, b, 0, a.length - 1);
        for(i=0;i<size;i++){
            ar.set(i, a[i]);
            br.set(i, b[i]);
        }
    }

    public static void quicksort(double[] a, ArrayList <Integer> br) {//the integers in br will be sorted together with a, ascending order of a's elements
        int size=br.size();
        int[] b=new int[size];
        int i;
        for(i=0;i<size;i++){
            b[i]=br.get(i);
        }
        shuffle(a,b);                        // to guard against worst-case
        quicksort(a, b, 0, a.length - 1);
        for(i=0;i<size;i++){
            br.set(i, b[i]);
        }
    }

    // quicksort a[left] to a[right]
    public static void quicksort(double[] a, int left, int right) {
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }
    public static void quicksort(double[] a, int[]b,int left, int right) {
        if (right <= left) return;
        int i = partition(a, b,  left, right);
        quicksort(a,b, left, i-1);
        quicksort(a,b, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(double[] a,int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(a[++i], a[right]))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (less(a[right], a[--j]))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, i, j);                      // swap two elements into place
        }
        exch(a, i, right);                      // swap with partition element
        return i;
    }
    private static int partition(double[] a,int[] b, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (less(a[++i], a[right]))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (less(a[right], a[--j]))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, b, i, j);                      // swap two elements into place
        }
        exch(a,b, i, right);                      // swap with partition element
        return i;
    }

    // is x < y ?
    private static boolean less(double x, double y) {
        comparisons++;
        return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(double[] a, int i, int j) {
        exchanges++;
        double swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    private static void exch(double[] a, int[] b, int i, int j) {
        exchanges++;
        double swap = a[i];
        a[i] = a[j];
        a[j] = swap;

        int iwp=b[i];
        b[i]=b[j];
        b[j]=iwp;
    }

    // shuffle the array a[]
    private static void shuffle(double[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
            exch(a, i, r);
        }
    }
    private static void shuffle(double[] a, int[]b) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
            exch(a,b, i, r);
        }
    }
    private static void shuffle(double[] a, int[]b, int indexI, int indexF) {
        for (int i = indexI; i <=indexF; i++) {
            int r = i + (int) (Math.random() * (indexF-i));   // between i and N-1
            exch(a,b, i, r);
        }
    }
}

