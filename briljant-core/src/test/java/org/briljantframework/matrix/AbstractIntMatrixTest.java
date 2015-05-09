package org.briljantframework.matrix;

import org.briljantframework.complex.Complex;
import org.briljantframework.matrix.api.MatrixFactory;
import org.briljantframework.matrix.netlib.NetlibMatrixBackend;
import org.junit.Test;

import java.util.Arrays;
import java.util.IntSummaryStatistics;

import static org.briljantframework.matrix.MatrixAssert.assertMatrixEquals;
import static org.briljantframework.matrix.MatrixAssert.assertValuesEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AbstractIntMatrixTest {

  private final MatrixFactory bj = new NetlibMatrixBackend().getMatrixFactory();

  @Test
  public void testAssign() throws Exception {
    IntMatrix m = bj.intMatrix(3, 3);
    m.assign(3);
    assertMatrixEquals(3, m);
  }

  @Test
  public void testAssign1() throws Exception {
    IntMatrix m = bj.intMatrix(3, 3);
    m.assign(() -> 3);
    assertMatrixEquals(3, m);
  }

  @Test
  public void testAssign2() throws Exception {
    IntMatrix m = bj.intMatrix(3, 3);
    m.assign(3).update(x -> x * 2);
    assertMatrixEquals(6, m);
  }

  @Test
  public void testAssign3() throws Exception {
    DoubleMatrix d = bj.doubleMatrix(3, 3).assign(3);
    IntMatrix i = bj.intMatrix(3, 3).assign(d, x -> (int) x);
    assertMatrixEquals(3, i);
  }

  @Test
  public void testAssign4() throws Exception {
    ComplexMatrix c = bj.complexMatrix(3, 3).assign(Complex.valueOf(3));
    IntMatrix i = bj.intMatrix(3, 3).assign(c, Complex::intValue);
    assertMatrixEquals(3, i);
  }

  @Test
  public void testAssign5() throws Exception {
    LongMatrix l = bj.longMatrix(3, 3).assign(3L);
    IntMatrix i = bj.intMatrix(3, 3).assign(l, x -> (int) x);
    assertMatrixEquals(3, i);
  }

  @Test
  public void testAssign6() throws Exception {
    IntMatrix i = bj.intMatrix(3, 3).assign(bj.intMatrix(3, 3).assign(3));
    assertMatrixEquals(3, i);
  }

  @Test
  public void testAssign7() throws Exception {
    IntMatrix i = bj.intMatrix(3, 3).assign(bj.intMatrix(3, 3).assign(3), x -> x * 2);
    assertMatrixEquals(6, i);
  }

  @Test
  public void testAssign8() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(2);
    IntMatrix d = bj.intMatrix(3, 3).assign(5);
    x.assign(d, Integer::sum);
    assertMatrixEquals(7, x);
  }

  @Test
  public void testMap() throws Exception {
    IntMatrix i = bj.intMatrix(3, 3).assign(3);
    IntMatrix m = i.map(Integer::bitCount);
    assertMatrixEquals(2, m);
  }

  @Test
  public void testMapToLong() throws Exception {
    IntMatrix i = bj.intMatrix(3, 3).assign(3);
    LongMatrix l = i.mapToLong(x -> Integer.MAX_VALUE + (long) x);
    assertMatrixEquals(l, ((long) Integer.MAX_VALUE) + 3L);
  }

  @Test
  public void testMapToDouble() throws Exception {
    DoubleMatrix i = bj.intMatrix(3, 3).assign(3).mapToDouble(Math::sqrt);
    assertMatrixEquals(Math.sqrt(3), i, 0.0001);
  }

  @Test
  public void testMapToComplex() throws Exception {
    ComplexMatrix i = bj.intMatrix(3, 3).assign(-3).mapToComplex(Complex::sqrt);
    assertMatrixEquals(i, Complex.sqrt(-3));
  }

  @Test
  public void testFilter() throws Exception {
    IntMatrix i = bj.matrix(new int[]{0, 1, 2, 3, 4, 5, 6}).filter(x -> x > 3);
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), i);
  }

  @Test
  public void testSatisfies() throws Exception {
    BitMatrix i = bj.matrix(new int[]{0, 1, 2, 3, 4, 5}).satisfies(x -> x >= 3);
    MatrixAssert
        .assertValuesEquals(bj.matrix(new boolean[]{false, false, false, true, true, true}), i);
  }

  @Test
  public void testSatisfies1() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3);
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    BitMatrix z = x.satisfies(y, (a, b) -> a < b);
    assertMatrixEquals(z, true);
  }

  @Test
  public void testReduce() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    int sum = x.reduce(0, Integer::sum);
    assertEquals(3 * 9, sum);
  }

  @Test
  public void testReduce1() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    int squaredSum = x.reduce(0, Integer::sum, i -> i * 2);
    assertEquals(3 * 2 * 9, squaredSum);
  }

  @Test
  public void testReduceColumns() throws Exception {
    IntMatrix x = bj.intMatrix(3, 4).assign(3).reduceColumns(y -> y.reduce(0, Integer::sum));
    assertEquals(4, x.columns());
    assertEquals(1, x.rows());
    assertMatrixEquals(3 * 3, x);
  }

  @Test
  public void testReduceRows() throws Exception {
    IntMatrix x = bj.intMatrix(4, 3).assign(3).reduceRows(y -> y.reduce(0, Integer::sum));
    assertEquals(4, x.rows());
    assertEquals(1, x.columns());
    assertMatrixEquals(3 * 3, x);
  }

  @Test
  public void testReshape() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(2, 3);
    assertEquals(2, x.rows());
    assertEquals(3, x.columns());
  }

  @Test
  public void testGet() throws Exception {
    IntMatrix x = bj.matrix(new int[]{0, 1, 2, 3, 4, 5}).reshape(3, 2);
    assertEquals(0, x.get(0));
    assertEquals(5, x.get(5));
  }

  @Test
  public void testGet1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{0, 1, 2, 3, 4, 5}).reshape(3, 2);
    assertEquals(0, x.get(0, 0));
    assertEquals(3, x.get(0, 1));
    assertEquals(4, x.get(1, 1));
  }

  @Test
  public void testSet() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3);
    x.set(0, 0, 1);
    x.set(0, 1, 2);
    x.set(1, 1, 3);

    assertEquals(1, x.get(0, 0));
    assertEquals(2, x.get(0, 1));
    assertEquals(3, x.get(1, 1));
  }

  @Test
  public void testSet1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{0, 1, 2, 3});
    assertEquals(0, x.get(0));
    assertEquals(1, x.get(1));
    assertEquals(2, x.get(2));
    assertEquals(3, x.get(3));
  }

  @Test
  public void testAddTo() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 1, 1, 1});
    x.addTo(0, 10);
    assertEquals(11, x.get(0));
  }

  @Test
  public void testAddTo1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 1, 1, 1}).reshape(2, 2);
    x.addTo(0, 0, 10);
    x.addTo(0, 1, 10);
    assertEquals(11, x.get(0, 0));
    assertEquals(11, x.get(0, 1));
  }

  @Test
  public void testUpdate() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 1, 1, 1}).reshape(2, 2);
    x.update(0, 0, i -> i * 3);
    assertEquals(3, x.get(0, 0));
  }

  @Test
  public void testUpdate1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 1, 1, 1});
    x.update(0, i -> i * 3);
    assertEquals(3, x.get(0));
  }

  @Test
  public void testGetRowView() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    assertMatrixEquals(1, x.getRowView(0));
    assertMatrixEquals(2, x.getRowView(1));
    assertMatrixEquals(3, x.getRowView(2));
  }

  @Test
  public void testGetColumnView() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 1, 1, 2, 2, 2, 3, 3, 3}).reshape(3, 3);
    assertMatrixEquals(1, x.getColumnView(0));
    assertMatrixEquals(2, x.getColumnView(1));
    assertMatrixEquals(3, x.getColumnView(2));
  }

  @Test
  public void testGetDiagonalView() throws Exception {

  }

  @Test
  public void testGetView() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 1, 1, 1, 2, 2}).reshape(2, 3);
    assertMatrixEquals(1, x.getView(0, 0, 2, 2));
  }

  @Test
  public void testTranspose() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3}).reshape(2, 3).transpose();
    assertEquals(3, x.rows());
    assertEquals(2, x.columns());
    assertEquals(1, x.get(0, 0));
    assertEquals(3, x.get(2, 1));
  }

  @Test
  public void testCopy() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 1, 1, 1});
    IntMatrix y = x.copy();
    x.set(0, 1000);
    assertEquals(1, y.get(0));
  }

  @Test
  public void testNewEmptyMatrix() throws Exception {
    assertNotNull(bj.intMatrix(2, 2).newEmptyMatrix(2, 2));
  }

  @Test
  public void testNewEmptyVector() throws Exception {
    IntMatrix x = bj.intMatrix(2, 2).newEmptyVector(2);
    assertNotNull(x);
    assertEquals(2, x.rows());
    assertEquals(1, x.columns());
  }

  @Test
  public void testMmul() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(3, 2);
    IntMatrix y = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(2, 3);

    IntMatrix z = y.mmul(x);
    IntMatrix za = bj.matrix(new int[]{22, 28, 49, 64}).reshape(2, 2);
    assertMatrixEquals(za, z);

    z = x.mmul(y);
    za = bj.matrix(new int[]{9, 12, 15, 19, 26, 33, 29, 40, 51}).reshape(3, 3);
    assertMatrixEquals(za, z);
  }

  @Test
  public void testMmul1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(3, 2);
    IntMatrix y = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(2, 3);

    IntMatrix z = y.mmul(2, x);
    IntMatrix za = bj.matrix(new int[]{44, 56, 98, 128}).reshape(2, 2);
    assertMatrixEquals(za, z);

    z = x.mmul(4, y);
    za = bj.matrix(new int[]{36, 48, 60, 76, 104, 132, 116, 160, 204}).reshape(3, 3);
    assertMatrixEquals(za, z);
  }

  @Test
  public void testMmul2() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(3, 2);
    IntMatrix y = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(3, 2);

    IntMatrix z = y.mmul(T.YES, x, T.NO);
    IntMatrix za = bj.matrix(new int[]{14, 32, 32, 77}).reshape(2, 2);
    assertMatrixEquals(za, z);

    z = x.mmul(T.NO, y, T.YES);
    za = bj.matrix(new int[]{17, 22, 27, 22, 29, 36, 27, 36, 45}).reshape(3, 3);
    assertMatrixEquals(za, z);
  }

  @Test
  public void testMmul3() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(3, 2);
    IntMatrix y = bj.matrix(new int[]{1, 2, 3, 4, 5, 6}).reshape(3, 2);
    IntMatrix z = y.mmul(2, T.YES, x, T.NO);
    IntMatrix za = bj.matrix(new int[]{28, 64, 64, 154}).reshape(2, 2);
    assertMatrixEquals(za, z);

    z = x.mmul(2, T.NO, y, T.YES);
    za = bj.matrix(new int[]{34, 44, 54, 44, 58, 72, 54, 72, 90}).reshape(3, 3);
    assertMatrixEquals(za, z);
  }

  @Test
  public void testMul() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = x.mul(2);
    assertMatrixEquals(6, z);
  }

  @Test
  public void testMul1() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    IntMatrix y = bj.intMatrix(3, 3).assign(2);
    IntMatrix z = x.mul(y);
    assertMatrixEquals(6, z);
  }

  @Test
  public void testMul2() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    IntMatrix y = bj.intMatrix(3, 3).assign(2);
    IntMatrix z = x.mul(-1, y, -1);
    assertMatrixEquals(6, z);
  }

  @Test
  public void testMul3() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.mul(x, Dim.R);
    assertMatrixEquals(3, z.getColumnView(0));
    assertMatrixEquals(6, z.getColumnView(1));
    assertMatrixEquals(9, z.getColumnView(2));

    z = y.mul(x, Dim.C);
    assertMatrixEquals(3, z.getRowView(0));
    assertMatrixEquals(6, z.getRowView(1));
    assertMatrixEquals(9, z.getRowView(2));
  }

  @Test
  public void testMul4() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.mul(1, x, -1, Dim.R);
    assertMatrixEquals(-3, z.getColumnView(0));
    assertMatrixEquals(-6, z.getColumnView(1));
    assertMatrixEquals(-9, z.getColumnView(2));

    z = y.mul(1, x, -1, Dim.C);
    assertMatrixEquals(-3, z.getRowView(0));
    assertMatrixEquals(-6, z.getRowView(1));
    assertMatrixEquals(-9, z.getRowView(2));

  }

  @Test
  public void testAdd() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(2);
    assertMatrixEquals(5, x.add(3));
  }

  @Test
  public void testAdd1() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(2);
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    assertMatrixEquals(5, x.add(y));
  }

  @Test
  public void testAdd2() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(2);
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    assertMatrixEquals(-1, x.add(1, y, -1));
  }

  @Test
  public void testAdd3() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.add(x, Dim.R);
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getRowView(2));

    z = y.add(x, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getColumnView(2));
  }

  @Test
  public void testAdd4() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.add(1, x, -1, Dim.R);
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getRowView(2));

    z = y.add(1, x, -1, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getColumnView(2));
  }

  @Test
  public void testSub() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    assertMatrixEquals(1, x.sub(2));
  }

  @Test
  public void testSub1() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    IntMatrix y = bj.intMatrix(3, 3).assign(2);
    assertMatrixEquals(1, x.sub(y));
  }

  @Test
  public void testSub2() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3);
    IntMatrix y = bj.intMatrix(3, 3).assign(2);
    assertMatrixEquals(5, x.sub(1, y, -1));
  }

  @Test
  public void testSub3() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.sub(x, Dim.R);
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getRowView(2));

    z = y.sub(x, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{2, 1, 0}), z.getColumnView(2));
  }

  @Test
  public void testSub4() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.sub(1, x, -1, Dim.R);
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getRowView(2));

    z = y.sub(1, x, -1, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{4, 5, 6}), z.getColumnView(2));
  }

  @Test
  public void testRsub() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(2);
    IntMatrix y = x.rsub(3);
    assertMatrixEquals(1, y);
  }

  @Test
  public void testRsub1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.rsub(1, x, -1, Dim.R);
    assertValuesEquals(bj.matrix(new int[]{-4, -5, -6}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{-4, -5, -6}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{-4, -5, -6}), z.getRowView(2));

    z = y.rsub(1, x, -1, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{-4, -5, -6}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{-4, -5, -6}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{-4, -5, -6}), z.getColumnView(2));
  }

  @Test
  public void testRsub2() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(3);
    IntMatrix z = y.rsub(x, Dim.R);
    assertValuesEquals(bj.matrix(new int[]{-2, -1, 0}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{-2, -1, 0}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{-2, -1, 0}), z.getRowView(2));

    z = y.rsub(x, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{-2, -1, 0}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{-2, -1, 0}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{-2, -1, 0}), z.getColumnView(2));
  }

  @Test
  public void testDiv() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(4);
    IntMatrix y = x.div(2);
    assertMatrixEquals(2, y);
  }

  @Test
  public void testDiv1() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(4);
    IntMatrix y = bj.intMatrix(3, 3).assign(2);
    IntMatrix z = x.div(y);
    assertMatrixEquals(2, z);
  }

  @Test
  public void testDiv2() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(6);
    IntMatrix z = y.div(x, Dim.R);

    assertValuesEquals(bj.matrix(new int[]{6, 3, 2}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{6, 3, 2}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{6, 3, 2}), z.getRowView(2));

    z = y.div(x, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{6, 3, 2}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{6, 3, 2}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{6, 3, 2}), z.getColumnView(2));
  }

  @Test
  public void testDiv3() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    IntMatrix y = bj.intMatrix(3, 3).assign(6);
    IntMatrix z = y.div(2, x, 1, Dim.R);

    assertValuesEquals(bj.matrix(new int[]{12, 6, 4}), z.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{12, 6, 4}), z.getRowView(1));
    assertValuesEquals(bj.matrix(new int[]{12, 6, 4}), z.getRowView(2));

    z = y.div(2, x, 1, Dim.C);
    assertValuesEquals(bj.matrix(new int[]{12, 6, 4}), z.getColumnView(0));
    assertValuesEquals(bj.matrix(new int[]{12, 6, 4}), z.getColumnView(1));
    assertValuesEquals(bj.matrix(new int[]{12, 6, 4}), z.getColumnView(2));
  }

  @Test
  public void testRdiv() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(6);
    IntMatrix y = x.rdiv(12);
    assertMatrixEquals(2, y);
  }

  @Test
  public void testRdiv1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{12, 12, 12});
    IntMatrix y = bj.intMatrix(3, 3).assign(6);
    IntMatrix z = y.rdiv(x, Dim.R);

    assertMatrixEquals(2, z.getRowView(0));
    assertMatrixEquals(2, z.getRowView(1));
    assertMatrixEquals(2, z.getRowView(2));

    z = y.rdiv(x, Dim.C);
    assertMatrixEquals(2, z.getColumnView(0));
    assertMatrixEquals(2, z.getColumnView(1));
    assertMatrixEquals(2, z.getColumnView(2));
  }

  @Test
  public void testRdiv2() throws Exception {
    IntMatrix x = bj.matrix(new int[]{6, 6, 6});
    IntMatrix y = bj.intMatrix(3, 3).assign(6);
    IntMatrix z = y.rdiv(1, x, 2, Dim.R);

    assertMatrixEquals(2, z.getRowView(0));
    assertMatrixEquals(2, z.getRowView(1));
    assertMatrixEquals(2, z.getRowView(2));

    z = y.rdiv(1, x, 2, Dim.C);
    assertMatrixEquals(2, z.getColumnView(0));
    assertMatrixEquals(2, z.getColumnView(1));
    assertMatrixEquals(2, z.getColumnView(2));
  }

  @Test
  public void testNegate() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3).assign(3).negate();
    assertMatrixEquals(-3, x);
  }

  @Test
  public void testSlice1() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3}).reshape(3, 2);
    IntMatrix slice = x.slice(bj.range(3));
    assertValuesEquals(bj.matrix(new int[]{1, 2, 3}), slice);
  }

  @Test
  public void testSlice2() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    IntMatrix slice = x.slice(bj.range(2), Dim.R);
    assertEquals(2, slice.rows());
    assertValuesEquals(bj.matrix(new int[]{1, 1, 1}), slice.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{2, 2, 2}), slice.getRowView(1));
  }

  @Test
  public void testSlice3() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    IntMatrix s = x.slice(bj.range(2), bj.range(2));
    assertEquals(2, s.rows());
    assertEquals(2, s.columns());
    assertValuesEquals(bj.matrix(new int[]{1, 1}), s.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{2, 2}), s.getRowView(1));
  }

  @Test
  public void testSlice4() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    IntMatrix s = x.slice(Arrays.asList(0, 2, 5, 7));
    assertValuesEquals(bj.matrix(new int[]{1, 3, 3, 2}), s);
  }

  @Test
  public void testSlice5() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    IntMatrix s = x.slice(Arrays.asList(0, 2), Dim.R);
    assertValuesEquals(bj.matrix(new int[]{1, 1, 1}), s.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{3, 3, 3}), s.getRowView(1));
  }

  @Test
  public void testSlice6() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    IntMatrix s = x.slice(Arrays.asList(0, 1), Arrays.asList(0, 1));
    assertValuesEquals(bj.matrix(new int[]{1, 1}), s.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{2, 2}), s.getRowView(1));
  }

  @Test
  public void testSlice7() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    BitMatrix bits =
        bj.matrix(new boolean[]{true, true, true, false, false, false, false, false, false})
            .reshape(3, 3);
    IntMatrix s = x.slice(bits);
    assertValuesEquals(bj.matrix(new int[]{1, 2, 3}), s);
  }

  @Test
  public void testSlice() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 1, 2, 3, 1, 2, 3}).reshape(3, 3);
    IntMatrix s = x.slice(bj.matrix(new boolean[]{true, false, true}), Dim.R);
    assertValuesEquals(bj.matrix(new int[]{1, 1, 1}), s.getRowView(0));
    assertValuesEquals(bj.matrix(new int[]{3, 3, 3}), s.getRowView(1));
  }

  @Test
  public void testSwap() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3});
    x.swap(0, 2);
    assertValuesEquals(bj.matrix(new int[]{3, 2, 1}), x);
  }

  @Test
  public void testSetRow() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3);
    x.setRow(0, bj.matrix(new int[]{1, 2, 3}));
    assertValuesEquals(bj.matrix(new int[]{1, 2, 3}), x.getRowView(0));
  }

  @Test
  public void testSetColumn() throws Exception {
    IntMatrix x = bj.intMatrix(3, 3);
    x.setColumn(0, bj.matrix(new int[]{1, 2, 3}));
    assertValuesEquals(bj.matrix(new int[]{1, 2, 3}), x.getColumnView(0));
  }

  @Test
  public void testHashCode() throws Exception {

  }

  @Test
  public void testEquals() throws Exception {

  }

  @Test
  public void testToString() throws Exception {

  }

  @Test
  public void testIterator() throws Exception {
    IntMatrix x = bj.matrix(new int[]{1, 2, 3, 4, 5, 6});
    int i = 0;
    for (int v : x.flat()) {
      assertEquals(x.get(i++), v);
    }
  }

  @Test
  public void testStream() throws Exception {
    IntMatrix m = bj.intMatrix(3, 3).assign(3);
    IntSummaryStatistics s = m.stream().summaryStatistics();
    assertEquals(3 * 3 * 3, s.getSum());
  }
}
