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
package org.briljantframework.array;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.commons.lang3.ArrayUtils;
import org.briljantframework.Check;
import org.briljantframework.array.api.ArrayFactory;

/**
 * This class provides a skeletal implementation of the {@link BaseArray} interface to minimize the
 * effort required to implement new array types.
 *
 * @author Isak Karlsson
 * @see AbstractArray
 * @see AbstractBooleanArray
 * @see AbstractIntArray
 * @see AbstractDoubleArray
 * @see AbstractComplexArray
 */
public abstract class AbstractBaseArray<E extends BaseArray<E>> implements BaseArray<E> {

  protected static final String INVALID_DIMENSION = "Dimension out of bounds (%s < %s)";
  protected static final String INVALID_VECTOR = "Vector index out of bounds (%s < %s)";
  protected static final String CHANGED_TOTAL_SIZE =
      "Total size of new array must be unchanged. (%s, %s)";
  protected static final String ILLEGAL_DIMENSION_INDEX =
      "Index %s is out of bounds for dimension %s with size %s";

  protected static final String REQUIRE_2D = "Require 2d-array";
  protected static final String REQUIRE_1D = "Require 2d-array";
  protected static final String REQUIRE_ND = "Require %dd-array";

  /**
   * The array factor associated with this array
   */
  protected final ArrayFactory factory;

  /**
   * The index of the major stride
   */
  protected final int majorStride;

  /**
   * The size of the array. Equals to shape[0] * shape[1] * ... * shape[shape.length - 1]
   */
  protected final int size;

  /**
   * The offset of the array, i.e. the position where indexing should start
   */
  protected final int offset;

  /**
   * The i:th position holds the number of elements between elements in the i:th dimension
   */
  protected final int[] stride;

  /**
   * The size of the i:th dimension
   */
  protected final int[] shape;

  /**
   * Construct an empty base array with the specified shape.
   *
   * @param factory the array factor
   * @param shape the shape
   */
  protected AbstractBaseArray(ArrayFactory factory, int[] shape) {
    this.factory = Objects.requireNonNull(factory);
    this.shape = shape.clone();
    this.stride = StrideUtils.computeStride(1, shape);
    this.size = ShapeUtils.size(shape);
    this.offset = 0;
    this.majorStride = 0;
  }

  /**
   * Construct an empty base array with the specified offset (i.e., where elements start), shape,
   * stride and majorStride
   *
   * @param factory the factory
   * @param offset the offset
   * @param shape the shape
   * @param stride the stride
   * @param majorStride the major stride index
   */
  protected AbstractBaseArray(ArrayFactory factory, int offset, int[] shape, int[] stride,
      int majorStride) {
    this.factory = factory;
    // TODO: 04/12/15 note that this is not copied. We should clarify when the shape needs to be
    // copied.
    this.shape = shape;
    this.stride = stride;
    this.size = ShapeUtils.size(shape);
    this.offset = offset;
    this.majorStride = majorStride;
  }

  protected final ArrayFactory getArrayFactory() {
    return factory;
  }

  @Override
  public E reverse() {
    E e = newEmptyArray(getShape());
    int vectors = vectors(0);
    for (int i = 0; i < vectors; i++) {
      E from = getVector(0, i);
      E to = e.getVector(0, i);
      int size = from.size();
      for (int j = 0; j < size; j++) {
        to.set(size - j - 1, from, j);
      }
    }
    return e;
  }

  @Override
  public void assign(E o) {
    Check.size(this, o);
    for (int i = 0; i < o.size(); i++) {
      set(i, o, i);
    }
  }

  @Override
  public void forEach(int dim, Consumer<E> consumer) {
    int size = vectors(dim);
    for (int i = 0; i < size; i++) {
      consumer.accept(getVector(dim, i));
    }
  }

  @Override
  public void setColumn(int i, E vec) {
    getColumn(i).assign(vec);
  }

  @Override
  public E getColumn(int i) {
    Check.state(isMatrix(), "Can only get columns from 2d-arrays");
    return getView(0, i, rows(), 1);
  }

  @Override
  public void setRow(int i, E vec) {
    getRow(i).assign(vec);
  }

  @Override
  public E getRow(int i) {
    Check.state(isMatrix(), "Can only get rows from 2d-arrays");
    return getView(i, 0, 1, columns());
  }

  @Override
  public final E reshape(int... shape) {
    if (shape.length == 0 || (shape.length == 1 && shape[0] == -1)) {
      if (isContiguous()) {
        int[] newShape = {size()};
        return asView(getOffset(), newShape, StrideUtils.computeStride(1, newShape));
      } else {
        return copy().reshape(shape);
      }
    }
    if (ShapeUtils.size(this.shape) != ShapeUtils.size(shape)) {
      throw new IllegalArgumentException(String.format(CHANGED_TOTAL_SIZE,
          Arrays.toString(this.shape), Arrays.toString(shape)));
    }
    if (isContiguous()) {
      return asView(getOffset(), shape.clone(), StrideUtils.computeStride(1, shape));
    } else {
      return copy().reshape(shape);
    }
  }

  @Override
  public E ravel() {
    return reshape(-1);
  }

  @Override
  public E select(int index) {
    Check.argument(dims() > 1, "Can't select in 1-d array");
    Check.argument(index >= 0 && index < size(0), ILLEGAL_DIMENSION_INDEX, index, 0, size(0));
    int dims = dims();
    return asView(getOffset() + index * stride(0), Arrays.copyOfRange(getShape(), 1, dims),
        Arrays.copyOfRange(getStride(), 1, dims));
  }

  @Override
  public E select(int dimension, int index) {
    Check.argument(dimension < dims() && dimension >= 0, "Can't select dimension.");
    Check.argument(index < size(dimension), "Index outside of shape.");
    return asView(getOffset() + index * stride(dimension), ArrayUtils.remove(getShape(), dimension),
                  ArrayUtils.remove(getStride(), dimension));
  }

  @Override
  public E slice(IntArray... indexers) {
    return slice(Arrays.asList(indexers));
  }

  @Override
  public E slice(List<? extends IntArray> indexers) {
    Check.argument(indexers.size() <= dims(), "too many indicies for array");
    Check.argument(indexers.size() > 0, "too few indices for array");
    int dims = indexers.stream().mapToInt(IntArray::dims).max().getAsInt();
    Check.all(indexers).argument(i -> i.dims() == dims);

    int[] shape = new int[dims + dims() - indexers.size()];
    for (int i = 0; i < shape.length; i++) {
      if (i < dims) {
        shape[i] = indexers.get(0).size(i);
      } else {
        shape[i] = size(i - dims);
      }
    }

    E to = newEmptyArray(shape);
    E from = asView(getOffset(), getShape(), getStride());
    IntArray[] i = indexers.toArray(new IntArray[indexers.size()]);
    recursiveSelect(to, from, i, indexers.get(0).dims());
    return to;
  }

  @Override
  public E getVector(int dimension, int index) {
    int dims = dims();
    int vectors = vectors(dimension);
    Check.argument(dimension < dims, INVALID_DIMENSION, dimension, dims);
    Check.argument(index < vectors, INVALID_VECTOR, index, vectors);

    int offset = getOffset();
    int stride = stride(dimension);
    int shape = size(dimension);
    int indexMajorStride = index * stride(majorStride);
    if (indexMajorStride >= stride) {
      offset += (indexMajorStride / stride) * stride * (shape - 1);
    }

    return asView(offset + indexMajorStride, new int[] {size(dimension)},
        new int[] {stride(dimension)});
  }

  @Override
  public void setVector(int dimension, int index, E other) {
    getVector(dimension, index).assign(other);
  }

  @Override
  public E getDiagonal() {
    Check.state(isMatrix(), "Can only get the diagonal of 2d-arrays");
    return asView(getOffset(), new int[] {Math.min(rows(), columns())}, new int[] {rows() + 1});
  }

  @Override
  public E get(RangeIndexer... ranges) {
    return get(Arrays.asList(ranges));
  }

  @Override
  public E get(List<? extends RangeIndexer> ranges) {
    Check.argument(ranges.size() > 0, "Too few ranges to slice");
    Check.argument(ranges.size() <= dims(), "Too many ranges to slice");
    int[] stride = getStride();
    int[] shape = getShape();
    int offset = getOffset();
    for (int i = 0; i < ranges.size(); i++) {
      RangeIndexer r = ranges.get(i);
      int start = r.start();
      int end = r.end(size(i));
      int step = r.step();

      Check.argument(step > 0, "Illegal step size in dimension %s", step);
      Check
          .argument(start >= 0 && start <= start + end, ILLEGAL_DIMENSION_INDEX, start, i, size(i));
      Check.argument(end <= size(i), ILLEGAL_DIMENSION_INDEX, end, i, size(i));
      offset += start * stride[i];
      shape[i] = end;
      stride[i] = stride[i] * step;
    }

    return asView(offset, shape, stride);
  }

  @Override
  public E getView(int rowOffset, int colOffset, int rows, int columns) {
    Check.state(isMatrix(), "Can only get view from 2d-arrays");
    Check.argument(rowOffset + rows <= rows() && colOffset + columns <= columns(),
        "Selected view is to large");
    return asView(getOffset() + rowOffset * stride(0) + colOffset * stride(1), new int[] {rows,
        columns}, getStride(), rows == 1 ? 1 : 0 // change the major stride
    );
  }

  @Override
  public final int size() {
    return size;
  }

  @Override
  public final int size(int dim) {
    Check.argument(dim >= 0 && dim < dims(), "dimension out of bounds");
    return shape[dim];
  }

  @Override
  public final int vectors(int i) {
    return size() / size(i);
  }

  @Override
  public final int stride(int i) {
    return stride[i];
  }

  @Override
  public final int getOffset() {
    return offset;
  }

  @Override
  public final int[] getShape() {
    return shape.clone();
  }

  @Override
  public final int[] getStride() {
    return stride.clone();
  }

  @Override
  public final int getMajorStride() {
    return stride(majorStride);
  }

  @Override
  public final int rows() {
    Check.state(isMatrix(), "Can only get number of rows of 2-d array");
    return shape[0];
  }

  @Override
  public final int columns() {
    Check.state(isMatrix(), "Can only get number of columns of 2-d array");
    return shape[1];
  }

  @Override
  public final int dims() {
    return shape.length;
  }

  @Override
  public final boolean isVector() {
    return dims() == 1 || (dims() == 2 && (rows() == 1 || columns() == 1));
  }

  @Override
  public final boolean isMatrix() {
    return dims() == 2;
  }

  @Override
  public final E asView(int[] shape, int[] stride) {
    return asView(getOffset(), shape, stride);
  }

  @Override
  public final E asView(int offset, int[] shape, int[] stride) {
    return asView(offset, shape, stride, 0);
  }

  @Override
  public boolean isView() {
    return !(majorStride == 0 && offset == 0 && Arrays.equals(stride,
                                                              StrideUtils.computeStride(1, shape)));
  }

  @Override
  public final boolean isContiguous() {
    return majorStride == 0;
  }

  @Override
  public final E transpose() {
    if (dims() == 1) {
      return asView(getOffset(), getShape(), getStride());
    } else {
      return asView(getOffset(), StrideUtils.reverse(shape), StrideUtils.reverse(stride),
          majorStride == 0 ? dims() - 1 : 0 // change the major stride
      );
    }
  }

  /**
   * Recursively select the appropriate indexers and slices in the to array
   */
  private void recursiveSelect(E to, E from, IntArray[] indexers, int dims) {
    if (dims == 1) {
      recursiveSelect2(to, from, indexers);
    } else {
      IntArray[] newIndexers = new IntArray[indexers.length];
      for (int j = 0; j < indexers[0].size(0); j++) {
        for (int i = 0; i < indexers.length; i++) {
          newIndexers[i] = indexers[i].select(j);
        }
        recursiveSelect(to.select(j), from, newIndexers, dims - 1);
      }
    }
  }

  /**
   * Recursively select the correct from slices and assign them to the correct to slices using the
   * indexers, reduced to
   */
  private void recursiveSelect2(E to, E from, IntArray[] indexers) {
    IntArray indexer = indexers[0];
    if (indexers.length == 1) {
      if (to.isVector()) {
        for (int i = 0; i < indexer.size(); i++) {
          to.set(i, from, indexer.get(i));
        }
      } else {
        for (int i = 0; i < indexer.size(); i++) {
          to.select(i).assign(from.select(indexer.get(i)));
        }
      }
    } else {
      for (int i = 0; i < indexer.size(); i++) {
        int fromIndex = indexer.get(i);
        recursiveSelect3(to, from.select(fromIndex), indexers, i, 1);
      }
    }
  }

  private void recursiveSelect3(E to, E from, IntArray[] indexers, int j, int dim) {
    int fromIndex = indexers[dim].get(j);
    if (indexers.length - 1 == dim) {
      if (to.isVector()) {
        to.set(j, from, fromIndex);
      } else {
        to.select(j).assign(from.select(fromIndex));
      }
    } else {
      recursiveSelect3(to, from.select(fromIndex), indexers, j, dim + 1);
    }
  }

  private void validIndexInDim(E from, int dim, int fromIndex) {
    Check.argument(fromIndex >= 0 && fromIndex < from.size(), ILLEGAL_DIMENSION_INDEX, fromIndex,
        dim, from.size(0));
  }

  /**
   * Return the number of elements in the data source.
   *
   * @return the number of elements in the data source
   */
  protected abstract int elementSize();

  protected int getMajorStrideIndex() {
    return majorStride;
  }
}
