package org.briljantframework.matrix;

import static com.google.common.primitives.Ints.checkedCast;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;

import org.briljantframework.IndexComparator;
import org.briljantframework.QuickSort;
import org.briljantframework.complex.Complex;
import org.briljantframework.exceptions.TypeConversionException;
import org.briljantframework.matrix.storage.Storage;

import com.google.common.collect.ImmutableMap;

/**
 * @author Isak Karlsson
 */
public final class Matrices {
  private final static Map<Class, Function<Integer, Matrix>> NATIVE_TO_VECTOR = ImmutableMap.of(
      Double.TYPE, DefaultDoubleMatrix::new, Integer.TYPE, DefaultIntMatrix::new, Complex.class,
      DefaultComplexMatrix::new, Long.TYPE, DefaultLongMatrix::new);

  private final static Map<Class, BiFunction<Integer, Integer, Matrix>> NATIVE_TO_MATRIX =
      ImmutableMap.of(Double.TYPE, DefaultDoubleMatrix::new, Integer.TYPE, DefaultIntMatrix::new,
          Complex.class, DefaultComplexMatrix::new, Long.TYPE, DefaultLongMatrix::new);

  private Matrices() {}

  public static BitMatrix newBitMatrix(boolean... values) {
    return new DefaultBitMatrix(values);
  }

  public static BitMatrix newBitVector(int size) {
    return new DefaultBitMatrix(size);
  }

  public static BitMatrix newBitMatrix(int rows, int cols) {
    return new DefaultBitMatrix(rows, cols);
  }

  public static DoubleMatrix newDoubleMatrix(double... values) {
    return new DefaultDoubleMatrix(values);
  }

  public static DoubleMatrix newDoubleVector(int size) {
    return new DefaultDoubleMatrix(size);
  }

  public static DoubleMatrix newDoubleMatrix(int rows, int columns) {
    return new DefaultDoubleMatrix(rows, columns);
  }

  public static IntMatrix newIntVector(int n) {
    return new DefaultIntMatrix(n);
  }

  public static IntMatrix newIntMatrix(int... values) {
    return new DefaultIntMatrix(values, 1).assign(values);
  }

  public static Matrix zeros(int size, Class<?> type) {
    Function<Integer, Matrix> f = NATIVE_TO_VECTOR.get(type);
    if (f == null) {
      throw new TypeConversionException(type.toString());
    }
    return f.apply(size);
  }

  public static DoubleMatrix zeros(int size) {
    return newDoubleVector(size);
  }

  public static Matrix zeros(int rows, int columns, Class<?> type) {
    BiFunction<Integer, Integer, Matrix> f = NATIVE_TO_MATRIX.get(type);
    if (f == null) {
      throw new TypeConversionException(type.toString());
    }
    return f.apply(rows, columns);
  }

  public static DoubleMatrix zeros(int rows, int columns) {
    return newDoubleMatrix(rows, columns);
  }

  public static ComplexMatrix ones(int size) {
    return fill(size, Complex.ONE);
  }

  static ComplexMatrix fill(int size, Complex fill) {
    return DefaultComplexMatrix.withDefaultValue(size, 1, fill);
  }

  public static IntMatrix range(int start, int end) {
    return range(start, end, 1);
  }

  public static IntMatrix range(int start, int end, int step) {
    return Range.range(start, end, step).copy();
  }

  public static IntMatrix take(IntMatrix a, IntMatrix b) {
    return take((Matrix) a, b).asIntMatrix();
  }

  /**
   * <p>
   * Take values in {@code a}, using the indexes in {@code indexes}.
   * <p>
   * For example,
   * </p>
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    IntMatrix a = Ints.newMatrix(1, 2, 3, 4);
   *    IntMatrix indexes = Ints.newMatrix(0, 0, 1, 2, 3);
   *    IntMatrix taken = Anys.take(a, indexes).asIntMatrix();
   *    1
   *    1
   *    2
   *    3
   *    4
   *    shape: 5x1 type: int
   * </pre>
   *
   * @param a the source matrix
   * @param indexes the indexes of the values to extract
   * @return a new matrix; the returned matrix has the same type as {@code a} (as returned by
   *         {@link org.briljantframework.matrix.Matrix#newEmptyMatrix(int, int)}).
   */
  public static Matrix take(Matrix a, IntMatrix indexes) {
    Matrix taken = a.newEmptyVector(indexes.size());
    for (int i = 0; i < indexes.size(); i++) {
      taken.set(i, a, indexes.get(i));
    }
    return taken;
  }

  /**
   * Changes the values of a copy of {@code a} according to the values of the {@code mask} and the
   * values in {@code values}. The value at {@code i} in a copy of {@code a} is set to value at
   * {@code i} from {@code values} if the boolean at {@code i} in {@code mask} is {@code true}.
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    IntMatrix a = Ints.range(0, 10).reshape(5, 2)
   *    BitMatrix mask = a.greaterThan(5)
   *    IntMatrix values = a.mul(2)
   *    IntMatrix result = Anys.mask(a, mask, values).asIntMatrix()
   * 
   *    0   5
   *    1   12
   *    2   14
   *    3   16
   *    4   18
   *    shape: 5x2 type: int
   * </pre>
   *
   * @param a a source array
   * @param mask the mask; same shape as {@code a}
   * @param values the values; same shape as {@code a}
   * @return a new matrix; the returned matrix has the same type as {@code a}.
   */
  public static Matrix mask(Matrix a, BitMatrix mask, Matrix values) {
    Check.equalShape(a, mask);
    Check.equalShape(a, values);

    Matrix masked = a.copy();
    putMask(masked, mask, values);
    return masked;
  }

  /**
   * Changes the values of {@code a} according to the values of the {@code mask} and the values in
   * {@code values}.
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    IntMatrix a = Ints.range(0, 10).reshape(5, 2)
   *    BitMatrix mask = a.greaterThan(5)
   *    IntMatrix values = a.mul(2)
   *    Anys.putMask(a, mask, values)
   *    System.out.println(a)
   * 
   *    0   5
   *    1   12
   *    2   14
   *    3   16
   *    4   18
   *    shape: 5x2 type: int
   * </pre>
   *
   * @param a the target matrix
   * @param mask the mask; same shape as {@code a}
   * @param values the mask; same shape as {@code a}
   * @see #mask(org.briljantframework.matrix.Matrix, org.briljantframework.matrix.BitMatrix,
   *      org.briljantframework.matrix.Matrix)
   */
  public static void putMask(Matrix a, BitMatrix mask, Matrix values) {
    Check.equalShape(a, mask);
    Check.equalShape(a, values);
    for (int i = 0; i < a.size(); i++) {
      if (mask.get(i)) {
        a.set(i, values, i);
      }
    }
  }

  /**
   * Selects the values in {@code a} according to the values in {@code where}, replacing those not
   * selected with {@code replace}.
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    IntMatrix a = Ints.range(0, 10).reshape(5, 2)
   *    BitMatrix mask = a.greaterThan(5)
   *    IntMatrix b = Anys.select(a, mask, -1)
   * 
   *    -1  -1
   *    -1   6
   *    -1   7
   *    -1   8
   *    -1   9
   *    shape: 5x2 type: int
   * </pre>
   *
   * @param a the source matrix
   * @param where the selection matrix; same shape as {@code a}
   * @param replace the replacement value
   * @return a new matrix; the returned matrix has the same type as {@code a}.
   */
  public static Matrix select(Matrix a, BitMatrix where, Number replace) {
    Check.equalShape(a, where);
    Matrix copy = a.copy();
    Storage storage = copy.getStorage();
    for (int i = 0; i < a.size(); i++) {
      if (!where.get(i)) {
        // TODO: either do check on storage.getNativeType() or implement several select(..) methods.
        storage.setNumber(i, replace);
      }
    }
    return copy;
  }

  /**
   * Selects the values in {@code a} according to the values in {@code where}.
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    IntMatrix a = Ints.range(0, 10).reshape(5, 2)
   *    BitMatrix mask = a.greaterThan(5)
   *    DoubleMatrix b = Anys.select(a.asDoubleMatrix(), mask).asDoubleMatrix()
   * 
   *    6.0000
   *    7.0000
   *    8.0000
   *    9.0000
   *    shape: 4x1 type: double
   * </pre>
   *
   * @param a the source matrix
   * @param where the selection matrix; same shape as {@code a}
   * @return a new matrix; the returned matrix has the same type as {@code a}
   */
  public static Matrix select(Matrix a, BitMatrix where) {
    Check.equalShape(a, where);
    Matrix.IncrementalBuilder builder = a.newIncrementalBuilder();
    for (int i = 0; i < a.size(); i++) {
      if (where.get(i)) {
        builder.add(a, i);
      }
    }
    return builder.build();
  }

  /**
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    DoubleMatrix a = Doubles.randn(10, 1)
   *    DoubleMatrix x = Anys.sort(a).asDoubleMatrix()
   * 
   *    -1.8718
   *    -0.8834
   *    -0.6161
   *    -0.0953
   *    0.0125
   *    0.3538
   *    0.4326
   *    0.4543
   *    1.0947
   *    1.1936
   *    shape: 10x1 type: double
   * </pre>
   *
   * @param matrix the source matrix
   * @return a new matrix; the returned matrix has the same type as {@code a}
   */
  public static Matrix sort(Matrix matrix) {
    return sort(matrix, Matrix::compare);
  }

  /**
   * <p>
   * Sorts the source matrix {@code a} in the order specified by {@code comparator}
   * </p>
   * For example, reversed sorted
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    DoubleMatrix a = Doubles.randn(12, 1)
   *    DoubleMatrix x = Anys.sort(a, (c, i, j) -> -c.compare(a, b)).asDoubleMatrix()
   * </pre>
   * <p>
   * {@link org.briljantframework.complex.Complex} and {@link ComplexMatrix} do not have a natural
   * sort order.
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    ComplexMatrix a = Doubles.randn(12, 1).asComplexMatrix().map(Complex::sqrt)
   *    ComplexMatrix x = Anys.sort(a, (c, i, j) -> Double.compare(c.getAsComplex(i).abs(),
   *        c.getAsComplex(j).abs()).asComplexMatrix()
   * 
   *    0.1499 + 0.0000i
   *    0.5478 + 0.0000i
   *    0.5725 + 0.0000i
   *    0.0000 + 0.5916i
   *    0.0000 + 0.6856i
   *    0.0000 + 0.8922i
   *    0.0000 + 0.9139i
   *    0.0000 + 1.0130i
   *    0.0000 + 1.1572i
   *    1.1912 + 0.0000i
   *    1.2493 + 0.0000i
   *    1.2746 + 0.0000i
   *    shape: 12x1 type: complex
   * </pre>
   *
   * @param a the source matrix
   * @param comparator the comparator; first argument is the container, and the next are indexes
   * @return a new sorted matrix; the returned matrix has the same type as {@code a}
   */
  public static Matrix sort(Matrix a, IndexComparator<? super Matrix> comparator) {
    Matrix out = a.copy();
    QuickSort.quickSort(0, checkedCast(out.size()), (x, y) -> comparator.compare(out, x, y), out);
    return out;
  }

  /**
   * Sort {@code a} each dimension, set by {@code axis}, in increasing order. For example, if
   * {@code axis == Axis.ROW}, each row is sorted in increasing order.
   * <p>
   * <p>
   *
   * <pre>
   *  > import org.briljantframework.matrix.*;
   *    DoubleMatrix a = Doubles.randn(12, 1).reshape(3,4)
   *    AnyMatrix x = Anys.sort(a, Axis.COLUMN)
   *    -0.2836   0.0603  -1.1870  -0.7840
   *    0.1644   0.2489   0.2159   0.6990
   *    0.4199   0.5131   0.9911   1.7952
   *    shape: 3x4 type: double
   * 
   *    AnyMatrix y = Anys.sort(a, Axis.ROW)
   *    -0.7840   0.0603   0.4199   0.9911
   *    -0.2836   0.2159   0.5131   1.7952
   *    -1.1870   0.1644   0.2489   0.6990
   *    shape: 3x4 type: double
   * </pre>
   *
   * @param a the source matrix
   * @param axis the axis to sort
   * @return a new matrix; the returned matrix has the same type as {@code a}
   */
  public static Matrix sort(Matrix a, Axis axis) {
    return sort(a, axis, Matrix::compare);
  }

  public static Matrix sort(Matrix a, Axis axis, IndexComparator<? super Matrix> comparator) {
    Matrix out = a.copy();
    if (axis == Axis.ROW) {
      for (int i = 0; i < a.rows(); i++) {
        Matrix row = out.getRowView(i);
        QuickSort.quickSort(0, checkedCast(row.size()), (x, y) -> comparator.compare(row, x, y),
            row);
      }
    } else {
      for (int i = 0; i < a.columns(); i++) {
        Matrix col = out.getColumnView(i);
        QuickSort.quickSort(0, checkedCast(col.size()), (x, y) -> comparator.compare(col, x, y),
            col);
      }
    }
    return out;
  }

  public static Matrix selectIndex(Matrix matrix, IntPredicate predicate) {
    Matrix.IncrementalBuilder builder = matrix.newIncrementalBuilder();
    for (int i = 0; i < matrix.size(); i++) {
      if (predicate.test(i)) {
        builder.add(matrix, i);
      }
    }
    return builder.build();
  }
}