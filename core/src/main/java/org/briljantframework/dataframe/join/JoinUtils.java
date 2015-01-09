package org.briljantframework.dataframe.join;

import java.util.Collection;

import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.matrix.ArrayIntMatrix;
import org.briljantframework.matrix.IntMatrix;
import org.briljantframework.vector.Value;
import org.briljantframework.vector.Vector;

import com.carrotsearch.hppc.ObjectIntMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * Created by Isak Karlsson on 09/01/15.
 */
public class JoinUtils {

  public static final int MISSING = Integer.MIN_VALUE;

  /**
   * @return retVal[0] := indexer, retVal[1] := counts
   */
  public static IntMatrix[] groupSortIndexer(IntMatrix index, int maxGroups) {
    int[] counts = new int[maxGroups + 1];
    int n = index.size();
    for (int i = 0; i < n; i++) {
      counts[index.get(i) + 1] += 1;
    }

    int[] where = new int[maxGroups + 1];
    for (int i = 1; i < maxGroups + 1; i++) {
      where[i] = where[i - 1] + counts[i - 1];
    }

    int[] result = new int[n];
    for (int i = 0; i < n; i++) {
      int label = index.get(i) + 1;
      result[where[label]] = i;
      where[label] += 1;
    }

    return new IntMatrix[] {ArrayIntMatrix.wrap(result), ArrayIntMatrix.wrap(counts)};
  }

  public static JoinKeys getJoinKeys(DataFrame a, DataFrame b, Collection<Integer> keys) {
    int[] newLeftPool = new int[a.rows()];
    int[] newRightPool = new int[b.rows()];

    int noGroups = 1;
    for (int index : keys) {
      JoinKeys pool = getJoinKeys(a.getColumn(index), b.getColumn(index));

      IntMatrix left = pool.getLeft();
      IntMatrix right = pool.getRight();
      for (int i = 0; i < newLeftPool.length; i++) {
        newLeftPool[i] += left.getAsInt(i) * noGroups;
      }

      for (int i = 0; i < newRightPool.length; i++) {
        newRightPool[i] += right.getAsInt(i) * noGroups;
      }
      noGroups = noGroups * (pool.getMaxGroups() + 1);
    }
    return new JoinKeys(ArrayIntMatrix.wrap(newLeftPool), ArrayIntMatrix.wrap(newRightPool),
        noGroups);
  }

  public static JoinKeys getJoinKeys(Vector a, Vector b) {
    int[] left = new int[a.size()];
    int[] right = new int[b.size()];
    ObjectIntMap<Value> pool = new ObjectIntOpenHashMap<>();

    int j = 0;
    for (int i = 0; i < a.size(); i++) {
      Value val = a.getAsValue(i);
      int ref = pool.getOrDefault(val, MISSING);
      if (ref != MISSING) {
        left[i] = pool.get(val);
      } else {
        left[i] = j;
        pool.put(val, j);
        j += 1;
      }
    }

    for (int i = 0; i < b.size(); i++) {
      Value val = b.getAsValue(i);
      int ref = pool.getOrDefault(val, MISSING);
      if (ref != MISSING) {
        right[i] = pool.get(val);
      } else {
        right[i] = j;
        pool.put(val, j);
        j += 1;
      }
    }

    return new JoinKeys(ArrayIntMatrix.wrap(left), ArrayIntMatrix.wrap(right), pool.size());
  }

}