/*
 * ADEB - machine learning pipelines made easy Copyright (C) 2014 Isak Karlsson
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */

package org.briljantframework.matrix;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;

import org.briljantframework.matrix.slice.Slicer;

import com.carrotsearch.hppc.DoubleArrayList;

/**
 * Created by Isak Karlsson on 28/08/14.
 */
public interface Matrix extends MatrixLike, Iterable<Double> {

  /**
   * Index int.
   *
   * @param row the row
   * @param col the col
   * @return the int
   */
  public static int columnMajorIndex(int row, int col, int nrows, int ncols) {
    if (col >= ncols || col < 0) {
      throw new IllegalArgumentException(String.format(
          "index out of bounds; value %d out of bound %d", col, ncols));
    } else if (row >= nrows || row < 0) {
      throw new IllegalArgumentException(String.format(
          "index out of bounds; value %d out of bound %d", row, nrows));
    } else {
      return col * nrows + row;
    }
  }

  /**
   * Get vector at row
   *
   * @param i row
   * @return a vector
   */
  Matrix getRow(int i);

  /**
   * Get rows from <code>start</code> to <code>end</code>
   *
   * @param start index
   * @param end index
   * @return a new matrix containing rows [start, end]
   */
  Matrix getRows(int start, int end);

  /**
   * Gets rows.
   *
   * @param slicer the slicer
   * @return rows rows
   */
  Matrix getRows(Slicer slicer);

  Matrix dropRow(int i);

  // Matrix dropRows(int start, int end);
  //
  // Matrix dropRows(Slicer slicer);

  /**
   * Gets column.
   *
   * @param index the index
   * @return the column
   */
  Matrix getColumn(int index);

  /**
   * Gets columns.
   *
   * @param start the start
   * @param end the end
   * @return columns columns
   */
  Matrix getColumns(int start, int end);

  /**
   * Gets columns.
   *
   * @param slicer the slicer
   * @return columns columns
   */
  Matrix getColumns(Slicer slicer);

  Matrix dropColumn(int index);

  // Matrix dropColumns(int start, int end);
  //
  // Matrix dropColumns(Slicer slicer);

  /**
   * Slice matrix.
   *
   * @param rows the rows
   * @param cols the cols
   * @return matrix matrix
   */
  Matrix slice(Slicer rows, Slicer cols);

  // Matrix drop(Slicer rows, Slicer cols);

  /**
   * Multiply by diagonal.
   *
   * @param diagonal the diagonal
   * @return matrix matrix
   */
  Matrix mmuld(Diagonal diagonal);

  @Override
  default Iterator<Double> iterator() {
    return new Iterator<Double>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < size();
      }

      @Override
      public Double next() {
        return get(index++);
      }
    };
  }

  /**
   * Less than.
   *
   * @param other the other
   * @return the boolean matrix
   */
  default BooleanMatrix lessThan(Matrix other) {
    checkArgument(hasCompatibleShape(other.getShape()), "can't compare a %s matrix to a %s matrix",
        getShape(), other.getShape());

    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int i = 0; i < other.rows(); i++) {
      for (int j = 0; j < other.columns(); j++) {
        bm.put(i, j, get(i, j) < other.get(i, j));
      }
    }

    return bm;
  }

  /**
   * Less than.
   *
   * @param value the value
   * @return the boolean matrix
   */
  default BooleanMatrix lessThan(double value) {
    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        bm.put(i, j, get(i, j) < value);
      }
    }
    return bm;
  }

  /**
   * Less than equal.
   *
   * @param other the other
   * @return the boolean matrix
   */
  default BooleanMatrix lessThanEqual(Matrix other) {
    checkArgument(hasCompatibleShape(other.getShape()), "can't compare a %s matrix to a %s matrix",
        getShape(), other.getShape());

    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int i = 0; i < other.rows(); i++) {
      for (int j = 0; j < other.columns(); j++) {
        bm.put(i, j, get(i, j) <= other.get(i, j));
      }
    }

    return bm;
  }

  /**
   * Less than equal.
   *
   * @param value the value
   * @return the boolean matrix
   */
  default BooleanMatrix lessThanEqual(double value) {
    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        bm.put(i, j, get(i, j) <= value);
      }
    }
    return bm;
  }

  /**
   * Greater than.
   *
   * @param other the other
   * @return the boolean matrix
   */
  default BooleanMatrix greaterThan(Matrix other) {
    checkArgument(hasCompatibleShape(other.getShape()), "can't compare a %s matrix to a %s matrix",
        getShape(), other.getShape());

    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int i = 0; i < other.rows(); i++) {
      for (int j = 0; j < other.columns(); j++) {
        bm.put(i, j, get(i, j) > other.get(i, j));
      }
    }

    return bm;
  }

  /**
   * Greater than.
   *
   * @param value the value
   * @return the boolean matrix
   */
  default BooleanMatrix greaterThan(double value) {
    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        bm.put(i, j, get(i, j) > value);
      }
    }
    return bm;
  }

  /**
   * Greater than equal.
   *
   * @param other the other
   * @return the boolean matrix
   */
  default BooleanMatrix greaterThanEquals(Matrix other) {
    checkArgument(hasCompatibleShape(other.getShape()), "can't compare a %s matrix to a %s matrix",
        getShape(), other.getShape());

    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int i = 0; i < other.rows(); i++) {
      for (int j = 0; j < other.columns(); j++) {
        bm.put(i, j, get(i, j) >= other.get(i, j));
      }
    }

    return bm;
  }

  /**
   * Greater than equals.
   *
   * @param value the value
   * @return the boolean matrix
   */
  default BooleanMatrix greaterThanEquals(double value) {
    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        bm.put(i, j, get(i, j) >= value);
      }
    }
    return bm;
  }

  /**
   * Equal to.
   *
   * @param other the other
   * @return the boolean matrix
   */
  default BooleanMatrix equalsTo(Matrix other) {
    checkArgument(hasCompatibleShape(other.getShape()), "can't compare a %s matrix to a %s matrix",
        getShape(), other.getShape());

    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int i = 0; i < other.rows(); i++) {
      for (int j = 0; j < other.columns(); j++) {
        bm.put(i, j, get(i, j) == other.get(i, j));
      }
    }

    return bm;
  }

  /**
   * Equals to.
   *
   * @param value the value
   * @return the boolean matrix
   */
  default BooleanMatrix equalsTo(double value) {
    BooleanMatrix bm = new BooleanMatrix(getShape());
    for (int j = 0; j < columns(); j++) {
      for (int i = 0; i < rows(); i++) {
        bm.put(i, j, get(i, j) == value);
      }
    }
    return bm;
  }

  /**
   * Find vector.
   *
   * @param matrix the matrix
   * @return the vector
   */
  default Matrix find(BooleanMatrix matrix) {
    checkArgument(hasCompatibleShape(matrix.getShape()));
    DoubleArrayList list = new DoubleArrayList();
    for (int i = 0; i < rows(); i++) {
      for (int j = 0; j < columns(); j++) {
        if (matrix.has(i, j)) {
          list.add(get(i, j));
        }
      }
    }
    return DenseMatrix.of(list.size(), 1, list.toArray());
  }

  /**
   * Create a copy of this matrix. This contract stipulates that modifications of the copy does not
   * affect the original.
   *
   * @return the copy
   */
  Matrix copy();

  /**
   * Transpose matrix like.
   *
   * @return the matrix like
   */
  Matrix transpose();

  // Arithmetic

  /**
   * Multiply r.
   *
   * @param other the other
   * @return r r
   */
  Matrix mmul(Matrix other);

  /**
   * @param other
   * @return
   */
  Matrix mul(Matrix other);

  /**
   * Multiply inplace.
   *
   * @param scalar the scalar
   * @return this (modified)
   */
  Matrix muli(double scalar);

  /**
   * Elementwise multiply.
   *
   * @param other the other
   * @return r r
   */
  Matrix muli(Matrix other);

  /**
   * Add r.
   *
   * @param other the other
   * @return r r
   */
  Matrix add(Matrix other);

  /**
   * Add r.
   *
   * @param scalar the scalar
   * @return r r
   */
  Matrix add(double scalar);

  /**
   * Add inplace.
   *
   * @param other the other
   * @return r r
   */
  Matrix addi(Matrix other);

  /**
   * Add inplace.
   *
   * @param scalar the scalar
   * @return r r
   */
  Matrix addi(double scalar);

  /**
   * this - other
   *
   * @param other the other
   * @return r r
   */
  Matrix sub(Matrix other);

  /**
   * this - other
   *
   * @param scalar the scalar
   * @return r r
   */
  Matrix sub(double scalar);

  /**
   * this <- this - other
   *
   * @param other the other
   * @return r r
   */
  Matrix subi(Matrix other);

  /**
   * this <- this - other
   *
   * @param scalar the scalar
   * @return r r
   */
  Matrix subi(double scalar);

  /**
   * other - this
   *
   * @param other the other
   * @return r r
   */
  Matrix rsub(Matrix other);

  /**
   * other - this
   *
   * @param scalar the scalar
   * @return r r
   */
  Matrix rsub(double scalar);

  /**
   * this <- other - this
   *
   * @param other the other
   * @return r r
   */
  Matrix rsubi(Matrix other);

  /**
   * this <- other - this
   *
   * @param scalar the scalar
   * @return r r
   */
  Matrix rsubi(double scalar);

  /**
   * this / other
   *
   * @param other the other
   * @return r r
   */
  Matrix div(Matrix other);

  /**
   * this / other
   *
   * @param other the scalar
   * @return r r
   */
  default Matrix div(double other) {
    return mul(1.0 / other);
  }

  /**
   * Multiply r.
   *
   * @param scalar the scalar
   * @return r r
   */
  Matrix mul(double scalar);

  /**
   * this <- this / other
   *
   * @param other the other
   * @return r r
   */
  Matrix divi(Matrix other);

  /**
   * this <- this / other
   *
   * @param other the scalar
   * @return r r
   */
  Matrix divi(double other);

  Matrix rdiv(Matrix other);

  Matrix rdivi(Matrix other);

  /**
   * other / this
   *
   * @param other the denominator
   * @return the r
   */
  Matrix rdiv(double other);

  /**
   * this <- other / this
   *
   * @param other the nominator
   * @return the r
   */
  Matrix rdivi(double other);

  /**
   * new matrix with elements negated
   *
   * @return the r
   */
  Matrix negate();

  /**
   * Set value at row i and column j to value
   *
   * @param i row
   * @param j column
   * @param value value
   */
  void put(int i, int j, double value);

  /**
   * Puts <code>value</code> at the linearized position <code>index</code>.
   *
   * @param index the index
   * @param value the value
   * @see #get(int)
   */
  void put(int index, double value);

  /**
   * Raw view of the column-major underlying array. In some instances it might be possible to mutate
   * this (e.g., if the implementation provides a direct reference. However, there are nos such
   * guarantees).
   *
   * @return the underlying array. Touch with caution.
   */
  double[] asDoubleArray();

  /**
   * Created by Isak Karlsson on 04/09/14.
   *
   * @param <T> the type parameter
   */
  @FunctionalInterface
  interface Copy<T extends Matrix> {

    /**
     * Copy the tensor while retaining the shape
     *
     * @param matrix a tensorLike
     * @return a copy of tensorLike
     */
    default T copyMatrix(MatrixLike matrix) {
      return copyMatrix(matrix.getShape(), matrix);
    }

    /**
     * Copy the tensor and perhaps change the shape
     *
     * @param shape the new shape
     * @param matrix the matrix
     * @return a copy of matrix
     */
    T copyMatrix(Shape shape, MatrixLike matrix);
  }

  /**
   * Created by Isak Karlsson on 03/09/14.
   *
   * @param <T> the type parameter
   */
  @FunctionalInterface
  interface New<T extends Matrix> {

    /**
     * New tensor.
     *
     * @param rows the rows
     * @param cols the cols
     * @return the t
     */
    default T newMatrix(int rows, int cols) {
      return newMatrix(Shape.of(rows, cols));
    }

    /**
     * New tensor.
     *
     * @param shape the shape
     * @return the t
     */
    default T newMatrix(Shape shape) {
      return newMatrix(shape, shape.getArrayOfShape());
    }

    /**
     * Construct a new tensor with the same shape
     *
     * @param tensor the tensor
     * @param values the values
     * @return t t
     */
    T newMatrix(Shape tensor, double[] values);

    /**
     * New matrix.
     *
     * @param rows the rows
     * @param cols the cols
     * @param array the array
     * @return the t
     */
    default T newMatrix(int rows, int cols, double[] array) {
      Shape shape = Shape.of(rows, cols);
      checkArgument(shape.size() == array.length, "shape and value array does not match");
      return newMatrix(shape, array);
    }

    /**
     * New vector.
     *
     * @param size the size
     * @param array the array
     * @return the t
     */
    default T newVector(int size, double[] array) {
      Shape shape = Shape.of(size, 1);
      checkArgument(shape.size() == array.length, "shape and value array does not match");
      return newMatrix(shape, array);
    }
  }
}
