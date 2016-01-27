/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
public class QuickSortByte {
    private static long comparisons = 0;
    private static long exchanges   = 0;

   /***********************************************************************
    *  Quicksort code from Sedgewick 7.1, 7.2.
    ***********************************************************************/
    public static void quicksort(byte[] a) {
        shuffle(a);                        // to guard against worst-case
        quicksort(a, 0, a.length - 1);
    }

    // quicksort a[left] to a[right]
    public static void quicksort(byte[] a, int left, int right) {
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    private static int partition(byte[] a, int left, int right) {
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
    private static boolean less(byte x, byte y) {
        comparisons++;
        return (x < y);
    }

    // exchange a[i] and a[j]
    private static void exch(byte[] a, int i, int j) {
        exchanges++;
        byte swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }

    // shuffle the array a[]
    private static void shuffle(byte[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (Math.random() * (N-i));   // between i and N-1
            exch(a, i, r);
        }
    }
}

