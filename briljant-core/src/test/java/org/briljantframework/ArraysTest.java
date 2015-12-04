/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Isak Karlsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.briljantframework;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.briljantframework.array.Arrays;
import org.briljantframework.array.ComplexArray;
import org.briljantframework.array.DoubleArray;
import org.briljantframework.array.IntArray;
import org.junit.Test;

/**
 * @author Isak Karlsson <isak-kar@dsv.su.se>
 */
public class ArraysTest {


  @Test
  public void testVsplit2d() throws Exception {
    int m = 6;
    int n = 3;
    IntArray x = Arrays.range(m * n).reshape(m, n);
    List<IntArray> split = Arrays.vsplit(x, 3);
    assertEquals(Arrays.newIntVector(0, 1, 6, 7, 12, 13).reshape(2, 3), split.get(0));
    assertEquals(Arrays.newIntVector(2, 3, 8, 9, 14, 15).reshape(2, 3), split.get(1));
    assertEquals(Arrays.newIntVector(4, 5, 10, 11, 16, 17).reshape(2, 3), split.get(2));
  }

  @Test
  public void testVsplitnd() throws Exception {
    IntArray x = Arrays.range(6 * 3 * 3).reshape(6, 3, 3);
    List<IntArray> split = Arrays.vsplit(x, 3);
    assertEquals(
        Arrays.newIntVector(2, 3, 8, 9, 14, 15, 20, 21, 26, 27, 32, 33, 38, 39, 44, 45, 50, 51)
            .reshape(2, 3, 3), split.get(1));
  }

  @Test
  public void testHsplit2d() throws Exception {
    int m = 6;
    int n = 3;
    IntArray x = Arrays.range(n * m).reshape(n, m);
    List<IntArray> split = Arrays.hsplit(x, 3);
    assertEquals(Arrays.newIntVector(0, 1, 2, 3, 4, 5).reshape(3, 2), split.get(0));
    assertEquals(Arrays.newIntVector(6, 7, 8, 9, 10, 11).reshape(3, 2), split.get(1));
    assertEquals(Arrays.newIntVector(12, 13, 14, 15, 16, 17).reshape(3, 2), split.get(2));
  }

  @Test
  public void testVstacknd() throws Exception {
    IntArray x = Arrays.range(6 * 3 * 3).reshape(6, 3, 3);
    List<IntArray> split = Arrays.vsplit(x, 3);
    IntArray vstack = Arrays.vstack(split);
    assertEquals(x, vstack);
  }

  @Test
  public void testRepeat() throws Exception {
    IntArray x = Arrays.range(3 * 3).reshape(3, 3);
    System.out.println(Arrays.repeat(x, 3));
  }

  @Test
  public void testRepeatNd() throws Exception {
    IntArray x = Arrays.range(3 * 3).reshape(3, 3);
    System.out.println(x);

    System.out.println(Arrays.repeat(0, x, 3));
    System.out.println(Arrays.repeat(1, x, 3));
    System.out.println(Arrays.repeat(0, Arrays.range(3), 3));
    System.out.println(Arrays.repeat(Arrays.range(2 * 2).reshape(2, 2), 2));
  }

  @Test
  public void testHstackedNd() throws Exception {
    IntArray x = Arrays.range(3 * 6 * 3).reshape(3, 6, 3);
    System.out.println(x);
    List<IntArray> split = Arrays.hsplit(x, 3);
    IntArray hstack = Arrays.hstack(split);
    assertEquals(x, hstack);
  }

  @Test
  public void testMeshgrid() throws Exception {
    IntArray x = Arrays.range(3);
    List<IntArray> meshgrid = Arrays.meshgrid(x, x);
    IntArray x1 = meshgrid.get(1);
    IntArray x2 = meshgrid.get(0);
    assertEquals(3, x1.size(0));
    assertEquals(3, x1.size(1));
    assertEquals(3, x2.size(0));
    assertEquals(3, x2.size(1));

    for (int i = 0; i < x2.vectors(0); i++) {
      assertEquals(x, x2.getVector(0, i));
    }

    for (int i = 0; i < x1.vectors(1); i++) {
      assertEquals(x, x1.getVector(1, i));
    }
  }

  @Test
  public void testBisectLeft() throws Exception {
    IntArray a = IntArray.of(1, 2, 9, 10, 12);
    System.out.println(Arrays.bisectLeft(a, 12));
  }

  @Test
  public void testOrder() throws Exception {
    DoubleArray array = DoubleArray.of(2, 3, 1, 9, 1);
    assertEquals(IntArray.of(2, 4, 0, 1, 3), Arrays.order(array));
  }

  @Test
  public void testOrderDimension() throws Exception {
    DoubleArray array = DoubleArray.of(1, 9, 1, 9, 2, 4).reshape(3, 2);
    assertEquals(IntArray.of(0, 2, 1, 1, 2, 0).reshape(3, 2), Arrays.order(0, array));
  }

  @Test
  public void testConcatenate() throws Exception {
    IntArray x = Arrays.range(2 * 2 * 3).reshape(2, 2, 3);

    IntArray concat_0 = Arrays.concatenate(java.util.Arrays.asList(x, x, x), 0);
    IntArray concat_1 = Arrays.concatenate(java.util.Arrays.asList(x, x, x), 1);
    IntArray concat_2 = Arrays.concatenate(java.util.Arrays.asList(x, x, x), 2);

    IntArray expected_0 =
        IntArray.of(0, 1, 0, 1, 0, 1, 2, 3, 2, 3, 2, 3, 4, 5, 4, 5, 4, 5, 6, 7, 6, 7, 6, 7, 8, 9,
            8, 9, 8, 9, 10, 11, 10, 11, 10, 11);
    IntArray expected_1 =
        IntArray.of(0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 4, 5, 6, 7, 4, 5, 6, 7, 4, 5, 6, 7, 8, 9,
            10, 11, 8, 9, 10, 11, 8, 9, 10, 11);
    IntArray expected_2 =
        IntArray.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0,
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);

    assertEquals(expected_0.reshape(6, 2, 3), concat_0);
    assertEquals(expected_1.reshape(2, 6, 3), concat_1);
    assertEquals(expected_2.reshape(2, 2, 9), concat_2);
  }

  @Test
  public void testSplit() throws Exception {
    IntArray x = Arrays.range(2 * 2 * 3).reshape(2, 2, 3);
    assertEquals(x, Arrays.concatenate(Arrays.split(x, 2, 0), 0));
    assertEquals(x, Arrays.concatenate(Arrays.split(x, 2, 1), 1));
    assertEquals(x, Arrays.concatenate(Arrays.split(x, 3, 2), 2));
  }

  @Test
  public void testWhere() throws Exception {
    DoubleArray c = DoubleArray.of(1, 0, 1, 2, 1);
    ComplexArray x = ComplexArray.of(1, 1, 1, 1, 1);
    ComplexArray y = ComplexArray.of(-1, 2, 3, -10, 3);
    assertEquals(Arrays.where(c.gte(2), x, y), ComplexArray.of(-1, 2, 3, 1, 3));
  }
}
