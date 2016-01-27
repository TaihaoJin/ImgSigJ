/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import utilities.ArrayofArrays.IntArray;
import java.util.ArrayList;
public class QuickSortInteger{
    private static long comparisons = 0;
    private static long exchanges   = 0;

   /***********************************************************************
    *  Quicksort code from Sedgewick 7.1, 7.2.
    ***********************************************************************/
    public static void quicksort(int[] a) {
        shuffle(a);                        // to guard against worst-case
        quicksort(a, 0, a.length - 1);
    }

    public static void quicksort(IntArray ir) {
        int size=ir.m_intArray.size();
        int[] a=new int[size];
        for(int i=0;i<size;i++){
            a[i]=ir.m_intArray.get(i);
        }
        quicksort(a);
        for(int i=0;i<size;i++){
            ir.m_intArray.set(i,a[i]);
        }
    }

    public static void quicksort(ArrayList<Integer> ir) {
        int size=ir.size();
        int[] a=new int[size];
        for(int i=0;i<size;i++){
            a[i]=ir.get(i);
        }
        quicksort(a);
        for(int i=0;i<size;i++){
            ir.set(i,a[i]);
        }
    }

    // quicksort a[left] to a[right]
    public static void quicksort(int[] a, int left, int right) {
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(int[] a, int left, int right) {
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

    // is x < y ?
    private static boolean less(int x, int y) {
        comparisons++;
        return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(int[] a, int i, int j) {
        exchanges++;
        int swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    // shuffle the array a[]
    private static void shuffle(int[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
            exch(a, i, r);
        }
    }
}

