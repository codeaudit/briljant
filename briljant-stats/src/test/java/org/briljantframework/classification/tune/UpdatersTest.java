/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Isak Karlsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.briljantframework.classification.tune;

import org.briljantframework.classification.KNearestNeighbors;
import org.junit.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UpdatersTest {

  @Test
  public void testRange() throws Exception {
    KNearestNeighbors.Builder knnBuilder = KNearestNeighbors.withNeighbors(1);
    ParameterUpdater<KNearestNeighbors.Builder> updater =
        Updaters.range("n", KNearestNeighbors.Builder::withNeighbors, 0, 10, 2);
    while (updater.hasUpdate()) {
      updater.update(knnBuilder);
    }
    assertEquals(10, knnBuilder.neighbors);
  }

  @Test
  public void testOptions() throws Exception {
    KNearestNeighbors.Builder knnBuilder = KNearestNeighbors.withNeighbors(1);
    // Tuners.split(knnBuilder, null,
    // range("n", KNearestNeighbors.Builder::withNeighbors, 0, 10, 2));
    // enumeration("d", KNearestNeighbors.Builder::distance, Distance.EUCLIDEAN,
    // Distance.MANHATTAN));
    //
  }

  @Test
  public void testCombinations() throws Exception {
    // Updater<PrintStream> updater = ;
    // Updater<PrintStream> updater1 = enumeration(PrintStream::println, 10, 11, 12);

    List<ParameterUpdater<PrintStream>> updaters = new ArrayList<>();
    updaters.add(Updaters.range("r", PrintStream::println, 0.0, 3, 1));
    updaters.add(Updaters.enumeration("e", PrintStream::println, 10, 11, 12));
    // updaters.add(enumeration("e", PrintStream::println, 10, 11, 12));

    testCombinations(updaters, System.out);

  }

  private <T> void testCombinations(List<ParameterUpdater<T>> ud, T toUpdate) {
    cartesian(new int[][] {new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 10}, new int[] {10, 11}});
    cartesian(ud, toUpdate, 0);

  }

  private <T> void cartesian(List<ParameterUpdater<T>> updaters, T toUpdate, int n) {
    if (n != updaters.size()) {
      ParameterUpdater<T> updater = updaters.get(n);
      while (updater.hasUpdate()) {
        updater.update(toUpdate);
        cartesian(updaters, toUpdate, n + 1);
      }
      updater.restore();
    }
  }

  public void cartesian(int[][] lists) {
    cartesian(lists, new int[lists.length], 0);
  }

  public void cartesian(int[][] lists, int[] values, int n) {
    if (n == lists.length) {
      System.out.println(Arrays.toString(values));
    } else {
      for (int i : lists[n]) {
        values[n] = i;
        cartesian(lists, values, n + 1);
      }
    }
  }
}
