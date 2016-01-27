/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.ArrayList;

/**
 *
 * @author Taihao
 */
public class QuickSort_Comparable {

    private static long comparisons = 0;
    private static long exchanges   = 0;

   /***********************************************************************
    *  Quicksort code from Sedgewick 7.1, 7.2.
    ***********************************************************************/
    public static void quicksort(ArrayList <Comparable> a) {
        shuffle(a);                        // to guard against worst-case
        int n=a.size();
        quicksort(a, 0, n-1);
    }

    // quicksort a[left] to a[right]
    public static void quicksort(ArrayList <Comparable> a, int left, int right) {
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(ArrayList <Comparable> a, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (a.get(++i).smaller(a.get(right)))      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (a.get(right).smaller(a.get(--j)))      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, i, j);                      // swap two elements into place        
        }
        exch(a, i, right);                      // swap with partition element
        return i;
    }
   
    // is x < y ?
    // exchange a[i] and a[j]
    private static void exch(ArrayList <Comparable> a, int i, int j) {
        exchanges++;
        Comparable swap = a.get(i);
        a.set(i, a.get(j));
        a.set(j, swap);
    }

    // shuffle the array a[]
    private static void shuffle(ArrayList <Comparable> a) {
        int N = a.size();
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
            exch(a, i, r);
        }
    }
}
