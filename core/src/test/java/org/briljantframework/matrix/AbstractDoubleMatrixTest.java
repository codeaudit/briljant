package org.briljantframework.matrix;

import static java.util.Arrays.asList;
import static org.briljantframework.matrix.Matrices.*;
import static org.briljantframework.matrix.MatrixAssert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.DoubleSummaryStatistics;

import org.briljantframework.complex.Complex;
import org.junit.Test;

public class AbstractDoubleMatrixTest {
  private final double epsilon = 0.00001;

  @Test
  public void testAssign() throws Exception {
    DoubleMatrix m = newDoubleMatrix(3, 3);
    m.assign(3);
    MatrixAssert.assertMatrixEquals(m, 3, epsilon);
  }

  @Test
  public void testAssign1() throws Exception {
    DoubleMatrix m = newDoubleMatrix(3, 3);
    m.assign(() -> 3);
    MatrixAssert.assertMatrixEquals(m, 3, epsilon);
  }

  @Test
  public void testAssign2() throws Exception {
    DoubleMatrix m = newDoubleMatrix(3, 3);
    m.assign(3).assign(x -> x * 2);
    MatrixAssert.assertMatrixEquals(m, 6, epsilon);
  }

  @Test
  public void testAssign3() throws Exception {
    DoubleMatrix d = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix i = newDoubleMatrix(3, 3).assign(d, x -> (int) x);
    MatrixAssert.assertMatrixEquals(i, 3, epsilon);
  }

  @Test
  public void testAssign4() throws Exception {
    ComplexMatrix c = newComplexMatrix(3, 3).assign(Complex.valueOf(3));
    DoubleMatrix i = newDoubleMatrix(3, 3).assign(c, Complex::intValue);
    MatrixAssert.assertMatrixEquals(i, 3, epsilon);
  }

  @Test
  public void testAssign5() throws Exception {
    DoubleMatrix l = newDoubleMatrix(3, 3).assign(3L);
    DoubleMatrix i = newDoubleMatrix(3, 3).assign(l, x -> (int) x);
    MatrixAssert.assertMatrixEquals(i, 3, epsilon);
  }

  @Test
  public void testAssign6() throws Exception {
    DoubleMatrix i = newDoubleMatrix(3, 3).assign(newDoubleMatrix(3, 3).assign(3));
    MatrixAssert.assertMatrixEquals(i, 3, epsilon);
  }

  @Test
  public void testAssign7() throws Exception {
    DoubleMatrix i = newDoubleMatrix(3, 3).assign(newDoubleMatrix(3, 3).assign(3), x -> x * 2);
    MatrixAssert.assertMatrixEquals(i, 6, epsilon);
  }

  @Test
  public void testAssign8() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(2);
    DoubleMatrix d = newDoubleMatrix(3, 3).assign(5);
    x.assign(d, Double::sum);
    assertMatrixEquals(x, epsilon, 7);
  }

  @Test
  public void testMap() throws Exception {
    DoubleMatrix i = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix m = i.map(Math::sqrt);
    MatrixAssert.assertMatrixEquals(m, Math.sqrt(3), epsilon);
  }

  @Test
  public void testMapToInt() throws Exception {
    DoubleMatrix i = newDoubleMatrix(3, 3).assign(Integer.MAX_VALUE + 10L);
    IntMatrix l = i.mapToInt(x -> (int) (x - Integer.MAX_VALUE));
    MatrixAssert.assertMatrixEquals(10, l);
  }

  @Test
  public void testMapToLong() throws Exception {
    LongMatrix i = newDoubleMatrix(3, 3).assign(3.3).mapToLong(Math::round);
    assertMatrixEquals(i, 3);
  }

  @Test
  public void testMapToComplex() throws Exception {
    ComplexMatrix i = newDoubleMatrix(3, 3).assign(-3).mapToComplex(Complex::sqrt);
    assertMatrixEquals(i, Complex.sqrt(-3));
  }

  @Test
  public void testFilter() throws Exception {
    DoubleMatrix i = newDoubleVector(0, 1, 2, 3, 4, 5, 6).filter(x -> x > 3);
    assertValueEquals(i, newDoubleVector(4, 5, 6), epsilon);
  }

  @Test
  public void testSatisfies() throws Exception {
    BitMatrix i = newDoubleVector(0, 1, 2, 3, 4, 5).satisfies(x -> x >= 3);
    assertValuesEquals(newBitVector(false, false, false, true, true, true), i);
  }

  @Test
  public void testSatisfies1() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    BitMatrix z = x.satisfies(y, (a, b) -> a < b);
    assertMatrixEquals(z, true);
  }

  @Test
  public void testReduce() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    double sum = x.reduce(0, Double::sum);
    assertEquals(3 * 9, sum, epsilon);
  }

  @Test
  public void testReduce1() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    double squaredSum = x.reduce(0, Double::sum, i -> i * 2);
    assertEquals(3 * 2 * 9, squaredSum, epsilon);
  }

  @Test
  public void testReduceColumns() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 4).assign(3).reduceColumns(y -> y.reduce(0, Double::sum));
    assertEquals(4, x.columns());
    assertEquals(1, x.rows());
    MatrixAssert.assertMatrixEquals(x, 3 * 3, epsilon);
  }

  @Test
  public void testReduceRows() throws Exception {
    DoubleMatrix x = newDoubleMatrix(4, 3).assign(3).reduceRows(y -> y.reduce(0, Double::sum));
    assertEquals(4, x.rows());
    assertEquals(1, x.columns());
    MatrixAssert.assertMatrixEquals(x, 3 * 3, epsilon);
  }

  @Test
  public void testReshape() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(2, 3);
    assertEquals(2, x.rows());
    assertEquals(3, x.columns());
  }

  @Test
  public void testGet() throws Exception {
    DoubleMatrix x = newDoubleVector(0, 1, 2, 3, 4, 5).reshape(3, 2);
    assertEquals(0, x.get(0), epsilon);
    assertEquals(5, x.get(5), epsilon);
  }

  @Test
  public void testGet1() throws Exception {
    DoubleMatrix x = newDoubleVector(0, 1, 2, 3, 4, 5).reshape(3, 2);
    assertEquals(0, x.get(0, 0), epsilon);
    assertEquals(3, x.get(0, 1), epsilon);
    assertEquals(4, x.get(1, 1), epsilon);
  }

  @Test
  public void testSet() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3);
    x.set(0, 0, 1);
    x.set(0, 1, 2);
    x.set(1, 1, 3);

    assertEquals(1, x.get(0, 0), epsilon);
    assertEquals(2, x.get(0, 1), epsilon);
    assertEquals(3, x.get(1, 1), epsilon);
  }

  @Test
  public void testSet1() throws Exception {
    DoubleMatrix x = newDoubleVector(0, 1, 2, 3);
    assertEquals(0, x.get(0), epsilon);
    assertEquals(1, x.get(1), epsilon);
    assertEquals(2, x.get(2), epsilon);
    assertEquals(3, x.get(3), epsilon);
  }

  // @Test
  // public void testAddTo() throws Exception {
  // DoubleMatrix x = newDoubleMatrix(1, 1, 1, 1);
  // x.addTo(0, 10);
  // assertEquals(11, x.get(0));
  // }
  //
  // @Test
  // public void testAddTo1() throws Exception {
  // DoubleMatrix x = newDoubleMatrix(1, 1, 1, 1).reshape(2, 2);
  // x.addTo(0, 0, 10);
  // x.addTo(0, 1, 10);
  // assertEquals(11, x.get(0, 0));
  // assertEquals(11, x.get(0, 1));
  // }
  //
  // @Test
  // public void testUpdate() throws Exception {
  // DoubleMatrix x = newDoubleMatrix(1, 1, 1, 1).reshape(2, 2);
  // x.update(0, 0, i -> i * 3);
  // assertEquals(3, x.get(0, 0));
  // }
  //
  // @Test
  // public void testUpdate1() throws Exception {
  // DoubleMatrix x = newDoubleMatrix(1, 1, 1, 1);
  // x.update(0, i -> i * 3);
  // assertEquals(3, x.get(0));
  // }

  @Test
  public void testGetRowView() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    MatrixAssert.assertMatrixEquals(x.getRowView(0), 1, epsilon);
    MatrixAssert.assertMatrixEquals(x.getRowView(1), 2, epsilon);
    MatrixAssert.assertMatrixEquals(x.getRowView(2), 3, epsilon);
  }

  @Test
  public void testGetColumnView() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 1, 1, 2, 2, 2, 3, 3, 3).reshape(3, 3);
    MatrixAssert.assertMatrixEquals(x.getColumnView(0), 1, epsilon);
    MatrixAssert.assertMatrixEquals(x.getColumnView(1), 2, epsilon);
    MatrixAssert.assertMatrixEquals(x.getColumnView(2), 3, epsilon);
  }

  @Test
  public void testGetDiagonalView() throws Exception {

  }

  @Test
  public void testGetView() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 1, 1, 1, 2, 2).reshape(2, 3);
    MatrixAssert.assertMatrixEquals(x.getView(0, 0, 2, 2), 1, epsilon);
  }

  @Test
  public void testTranspose() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3).reshape(2, 3).transpose();
    assertEquals(3, x.rows());
    assertEquals(2, x.columns());
    assertEquals(1, x.get(0, 0), epsilon);
    assertEquals(3, x.get(2, 1), epsilon);
  }

  @Test
  public void testCopy() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 1, 1, 1);
    DoubleMatrix y = x.copy();
    x.set(0, 1000);
    assertEquals(1, y.get(0), epsilon);
  }

  @Test
  public void testNewEmptyMatrix() throws Exception {
    assertNotNull(newDoubleMatrix(2, 2).newEmptyMatrix(2, 2));
  }

  @Test
  public void testNewEmptyVector() throws Exception {
    DoubleMatrix x = newDoubleMatrix(2, 2).newEmptyVector(2);
    assertNotNull(x);
    assertEquals(2, x.rows());
    assertEquals(1, x.columns());
  }

  @Test
  public void testMmul() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(3, 2);
    DoubleMatrix y = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(2, 3);

    DoubleMatrix z = y.mmul(x);
    DoubleMatrix za = newDoubleVector(22, 28, 49, 64).reshape(2, 2);
    assertMatrixEquals(za, z, epsilon);

    z = x.mmul(y);
    za = newDoubleVector(9, 12, 15, 19, 26, 33, 29, 40, 51).reshape(3, 3);
    assertMatrixEquals(za, z, epsilon);
  }

  @Test
  public void testMmul1() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(3, 2);
    DoubleMatrix y = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(2, 3);
    DoubleMatrix z = y.mmul(2, x);
    DoubleMatrix za = newDoubleVector(44, 56, 98, 128).reshape(2, 2);
    assertMatrixEquals(za, z, epsilon);

    z = x.mmul(4, y);
    za = newDoubleVector(36, 48, 60, 76, 104, 132, 116, 160, 204).reshape(3, 3);
    assertMatrixEquals(za, z, epsilon);
  }

  @Test
  public void testMmul2() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(3, 2);
    DoubleMatrix y = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(3, 2);

    DoubleMatrix z = y.mmul(Transpose.YES, x, Transpose.NO);
    DoubleMatrix za = newDoubleVector(14, 32, 32, 77).reshape(2, 2);
    assertMatrixEquals(za, z, epsilon);

    z = x.mmul(Transpose.NO, y, Transpose.YES);
    za = newDoubleVector(17, 22, 27, 22, 29, 36, 27, 36, 45).reshape(3, 3);
    assertMatrixEquals(za, z, epsilon);
  }

  @Test
  public void testMmul3() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(3, 2);
    DoubleMatrix y = newDoubleVector(1, 2, 3, 4, 5, 6).reshape(3, 2);
    DoubleMatrix z = y.mmul(2, Transpose.YES, x, Transpose.NO);
    DoubleMatrix za = newDoubleVector(28, 64, 64, 154).reshape(2, 2);
    assertMatrixEquals(za, z, epsilon);

    z = x.mmul(2, Transpose.NO, y, Transpose.YES);
    za = newDoubleVector(34, 44, 54, 44, 58, 72, 54, 72, 90).reshape(3, 3);
    assertMatrixEquals(za, z, epsilon);
  }

  @Test
  public void testMul() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = x.mul(2);
    MatrixAssert.assertMatrixEquals(z, 6, epsilon);
  }

  @Test
  public void testMul1() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(2);
    DoubleMatrix z = x.mul(y);
    MatrixAssert.assertMatrixEquals(z, 6, epsilon);
  }

  @Test
  public void testMul2() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(2);
    DoubleMatrix z = x.mul(-1, y, -1);
    MatrixAssert.assertMatrixEquals(z, 6, epsilon);
  }

  @Test
  public void testMul3() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.mul(x, Axis.ROW);
    MatrixAssert.assertMatrixEquals(z.getColumnView(0), 3, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(1), 6, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(2), 9, epsilon);

    z = y.mul(x, Axis.COLUMN);
    MatrixAssert.assertMatrixEquals(z.getRowView(0), 3, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(1), 6, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(2), 9, epsilon);
  }

  @Test
  public void testMul4() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.mul(1, x, -1, Axis.ROW);
    MatrixAssert.assertMatrixEquals(z.getColumnView(0), -3, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(1), -6, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(2), -9, epsilon);

    z = y.mul(1, x, -1, Axis.COLUMN);
    MatrixAssert.assertMatrixEquals(z.getRowView(0), -3, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(1), -6, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(2), -9, epsilon);

  }

  @Test
  public void testAdd() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(2);
    MatrixAssert.assertMatrixEquals(x.add(3), 5, epsilon);
  }

  @Test
  public void testAdd1() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(2);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    MatrixAssert.assertMatrixEquals(x.add(y), 5, epsilon);
  }

  @Test
  public void testAdd2() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(2);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    MatrixAssert.assertMatrixEquals(x.add(1, y, -1), -1, epsilon);
  }

  @Test
  public void testAdd3() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.add(x, Axis.ROW);
    assertValueEquals(z.getRowView(0), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(4, 5, 6), epsilon);

    z = y.add(x, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(4, 5, 6), epsilon);
  }

  @Test
  public void testAdd4() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.add(1, x, -1, Axis.ROW);
    assertValueEquals(z.getRowView(0), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(2, 1, 0), epsilon);

    z = y.add(1, x, -1, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(2, 1, 0), epsilon);
  }

  @Test
  public void testSub() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    MatrixAssert.assertMatrixEquals(x.sub(2), 1, epsilon);
  }

  @Test
  public void testSub1() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(2);
    MatrixAssert.assertMatrixEquals(x.sub(y), 1, epsilon);
  }

  @Test
  public void testSub2() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(2);
    MatrixAssert.assertMatrixEquals(x.sub(1, y, -1), 5, epsilon);
  }

  @Test
  public void testSub3() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.sub(x, Axis.ROW);
    assertValueEquals(z.getRowView(0), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(2, 1, 0), epsilon);

    z = y.sub(x, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(2, 1, 0), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(2, 1, 0), epsilon);
  }

  @Test
  public void testSub4() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.sub(1, x, -1, Axis.ROW);
    assertValueEquals(z.getRowView(0), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(4, 5, 6), epsilon);

    z = y.sub(1, x, -1, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(4, 5, 6), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(4, 5, 6), epsilon);
  }

  @Test
  public void testRsub() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(2);
    DoubleMatrix y = x.rsub(3);
    MatrixAssert.assertMatrixEquals(y, 1, epsilon);
  }

  @Test
  public void testRsub1() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.rsub(1, x, -1, Axis.ROW);
    assertValueEquals(z.getRowView(0), newDoubleVector(-4, -5, -6), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(-4, -5, -6), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(-4, -5, -6), epsilon);

    z = y.rsub(1, x, -1, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(-4, -5, -6), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(-4, -5, -6), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(-4, -5, -6), epsilon);
  }

  @Test
  public void testRsub2() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(3);
    DoubleMatrix z = y.rsub(x, Axis.ROW);
    assertValueEquals(z.getRowView(0), newDoubleVector(-2, -1, 0), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(-2, -1, 0), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(-2, -1, 0), epsilon);

    z = y.rsub(x, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(-2, -1, 0), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(-2, -1, 0), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(-2, -1, 0), epsilon);
  }

  @Test
  public void testDiv() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(4);
    DoubleMatrix y = x.div(2);
    MatrixAssert.assertMatrixEquals(y, 2, epsilon);
  }

  @Test
  public void testDiv1() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(4);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(2);
    DoubleMatrix z = x.div(y);
    MatrixAssert.assertMatrixEquals(z, 2, epsilon);
  }

  @Test
  public void testDiv2() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(6);
    DoubleMatrix z = y.div(x, Axis.ROW);

    assertValueEquals(z.getRowView(0), newDoubleVector(6, 3, 2), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(6, 3, 2), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(6, 3, 2), epsilon);

    z = y.div(x, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(6, 3, 2), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(6, 3, 2), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(6, 3, 2), epsilon);
  }

  @Test
  public void testDiv3() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(6);
    DoubleMatrix z = y.div(2, x, 1, Axis.ROW);

    assertValueEquals(z.getRowView(0), newDoubleVector(12, 6, 4), epsilon);
    assertValueEquals(z.getRowView(1), newDoubleVector(12, 6, 4), epsilon);
    assertValueEquals(z.getRowView(2), newDoubleVector(12, 6, 4), epsilon);

    z = y.div(2, x, 1, Axis.COLUMN);
    assertValueEquals(z.getColumnView(0), newDoubleVector(12, 6, 4), epsilon);
    assertValueEquals(z.getColumnView(1), newDoubleVector(12, 6, 4), epsilon);
    assertValueEquals(z.getColumnView(2), newDoubleVector(12, 6, 4), epsilon);
  }

  @Test
  public void testRdiv() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(6);
    DoubleMatrix y = x.rdiv(12);
    MatrixAssert.assertMatrixEquals(y, 2, epsilon);
  }

  @Test
  public void testRdiv1() throws Exception {
    DoubleMatrix x = newDoubleVector(12, 12, 12);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(6);
    DoubleMatrix z = y.rdiv(x, Axis.ROW);

    MatrixAssert.assertMatrixEquals(z.getRowView(0), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(1), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(2), 2, epsilon);

    z = y.rdiv(x, Axis.COLUMN);
    MatrixAssert.assertMatrixEquals(z.getColumnView(0), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(1), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(2), 2, epsilon);
  }

  @Test
  public void testRdiv2() throws Exception {
    DoubleMatrix x = newDoubleVector(6, 6, 6);
    DoubleMatrix y = newDoubleMatrix(3, 3).assign(6);
    DoubleMatrix z = y.rdiv(1, x, 2, Axis.ROW);

    MatrixAssert.assertMatrixEquals(z.getRowView(0), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(1), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getRowView(2), 2, epsilon);

    z = y.rdiv(1, x, 2, Axis.COLUMN);
    MatrixAssert.assertMatrixEquals(z.getColumnView(0), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(1), 2, epsilon);
    MatrixAssert.assertMatrixEquals(z.getColumnView(2), 2, epsilon);
  }

  @Test
  public void testNegate() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3).assign(3).negate();
    MatrixAssert.assertMatrixEquals(x, -3, epsilon);
  }

  @Test
  public void testSlice1() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3).reshape(3, 2);
    DoubleMatrix slice = x.slice(Range.range(3));
    assertValueEquals(slice, newDoubleVector(1, 2, 3), epsilon);
  }

  @Test
  public void testSlice2() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    DoubleMatrix slice = x.slice(Range.range(2), Axis.ROW);
    assertEquals(2, slice.rows());
    assertValueEquals(slice.getRowView(0), newDoubleVector(1, 1, 1), epsilon);
    assertValueEquals(slice.getRowView(1), newDoubleVector(2, 2, 2), epsilon);
  }

  @Test
  public void testSlice3() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    DoubleMatrix s = x.slice(Range.range(2), Range.range(2));
    assertEquals(2, s.rows());
    assertEquals(2, s.columns());
    assertValueEquals(s.getRowView(0), newDoubleVector(1, 1), epsilon);
    assertValueEquals(s.getRowView(1), newDoubleVector(2, 2), epsilon);
  }

  @Test
  public void testSlice4() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    DoubleMatrix s = x.slice(asList(0, 2, 5, 7));
    assertValueEquals(s, newDoubleVector(1, 3, 3, 2), epsilon);
  }

  @Test
  public void testSlice5() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    DoubleMatrix s = x.slice(asList(0, 2), Axis.ROW);
    assertValueEquals(s.getRowView(0), newDoubleVector(1, 1, 1), epsilon);
    assertValueEquals(s.getRowView(1), newDoubleVector(3, 3, 3), epsilon);
  }

  @Test
  public void testSlice6() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    DoubleMatrix s = x.slice(asList(0, 1), asList(0, 1));
    assertMatrixEquals(s.getRowView(0), 1, 1);
    assertMatrixEquals(s.getRowView(1), 2, 2);
  }

  @Test
  public void testSlice7() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    BitMatrix bits =
        newBitVector(true, true, true, false, false, false, false, false, false).reshape(3, 3);
    DoubleMatrix s = x.slice(bits);
    assertValueEquals(s, newDoubleVector(1, 2, 3), epsilon);
  }

  @Test
  public void testSlice() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 1, 2, 3, 1, 2, 3).reshape(3, 3);
    DoubleMatrix s = x.slice(newBitVector(true, false, true), Axis.ROW);
    assertValueEquals(s.getRowView(0), newDoubleVector(1, 1, 1), epsilon);
    assertValueEquals(s.getRowView(1), newDoubleVector(3, 3, 3), epsilon);
  }

  @Test
  public void testSwap() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3);
    x.swap(0, 2);
    assertValueEquals(x, newDoubleVector(3, 2, 1), epsilon);
  }

  @Test
  public void testSetRow() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3);
    x.setRow(0, newDoubleVector(1, 2, 3));
    assertValueEquals(x.getRowView(0), newDoubleVector(1, 2, 3), epsilon);
  }

  @Test
  public void testSetColumn() throws Exception {
    DoubleMatrix x = newDoubleMatrix(3, 3);
    x.setColumn(0, newDoubleVector(1, 2, 3));
    assertValueEquals(x.getColumnView(0), newDoubleVector(1, 2, 3), epsilon);
  }

  @Test
  public void testHashCode() throws Exception {

  }

  @Test
  public void testEquals() throws Exception {}

  @Test
  public void testToString() throws Exception {

  }

  @Test
  public void testIterator() throws Exception {
    DoubleMatrix x = newDoubleVector(1, 2, 3, 4, 5, 6);
    int i = 0;
    for (double v : x) {
      assertEquals(x.get(i++), v, epsilon);
    }
  }

  @Test
  public void testStream() throws Exception {
    DoubleMatrix m = newDoubleMatrix(3, 3).assign(3);
    DoubleSummaryStatistics s = m.stream().summaryStatistics();
    assertEquals(3 * 3 * 3, s.getSum(), epsilon);
  }
}
