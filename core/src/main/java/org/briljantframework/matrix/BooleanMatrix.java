package org.briljantframework.matrix;

import com.google.common.collect.ImmutableTable;
import org.briljantframework.Utils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by Isak Karlsson on 11/10/14.
 */
public class BooleanMatrix implements MatrixLike {

    private final boolean[] values;
    private final int rows, cols;

    /**
     * Instantiates a new Boolean matrix.
     *
     * @param shape the shape
     */
    public BooleanMatrix(Shape shape) {
        this(shape.rows, shape.columns);
    }

    /**
     * Instantiates a new Boolean matrix.
     *
     * @param rows the rows
     * @param cols the cols
     */
    public BooleanMatrix(int rows, int cols) {
        this.values = new boolean[rows * cols];
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Put void.
     *
     * @param i     the i
     * @param j     the j
     * @param value the value
     */
    public void put(int i, int j, boolean value) {
        values[index(i, j)] = value;
    }

    @Override
    public void put(int i, int j, double value) {
        values[index(i, j)] = value != 0;
    }

    @Override
    public double get(int i, int j) {
        boolean value = values[index(i, j)];
        return value ? 1 : 0;
    }

    @Override
    public void put(int index, double value) {
        checkArgument(index > 0 && index < values.length);
        values[index] = value != 0;
    }

    @Override
    public BooleanMatrix copy() {
        BooleanMatrix bm = new BooleanMatrix(getShape());
        System.arraycopy(values, 0, bm.values, 0, values.length);
        return bm;
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public int columns() {
        return cols;
    }

    @Override
    public int size() {
        return rows() * columns();
    }

    @Override
    public double get(int index) {
        checkArgument(index > 0 && index < values.length);
        return values[index] ? 1 : 0;
    }

    @Override
    public double[] asDoubleArray() {
        double[] array = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            array[i] = values[i] ? 1 : 0;
        }
        return array;
    }

    /**
     * Transpose matrix like.
     *
     * @return the matrix like
     */
    public MatrixLike transpose() {
        throw new UnsupportedOperationException();
    }

    /**
     * Index int.
     *
     * @param row the row
     * @param col the col
     * @return the int
     */
    protected int index(int row, int col) {
        if (col >= this.columns() || col < 0) {
            throw new IllegalArgumentException(String.format("index out of bounds; value %d out of bound %d", col,
                    this.columns()));
        } else if (row >= this.rows() || row < 0) {
            throw new IllegalArgumentException(String.format("index out of bounds; value %d out of bound %d", row,
                    this.rows()));
        } else {
            return col * this.rows() + row;
        }
    }

    /**
     * And boolean matrix.
     *
     * @param other the other
     * @return the boolean matrix
     */
    public BooleanMatrix and(BooleanMatrix other) {
        checkArgument(hasCompatibleShape(other.getShape()));

        BooleanMatrix bm = new BooleanMatrix(getShape());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                bm.put(i, j, has(i, j) && other.has(i, j));
            }
        }
        return bm;
    }

    /**
     * Or boolean matrix.
     *
     * @param other the other
     * @return the boolean matrix
     */
    public BooleanMatrix or(BooleanMatrix other) {
        checkArgument(hasCompatibleShape(other.getShape()));

        BooleanMatrix bm = new BooleanMatrix(getShape());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                bm.put(i, j, has(i, j) || other.has(i, j));
            }
        }
        return bm;
    }

    /**
     * Not boolean matrix.
     *
     * @return the boolean matrix
     */
    public BooleanMatrix not() {
        BooleanMatrix bm = new BooleanMatrix(getShape());
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                bm.put(i, j, !has(i, j));
            }
        }
        return bm;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("BooleanMatrix\n");
        ImmutableTable.Builder<Integer, Integer, String> builder = ImmutableTable.builder();
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < columns(); j++) {
                builder.put(i, j, String.format("%s", has(i, j)));
            }
        }
        Utils.prettyPrintTable(out, builder.build(), 0, 2, false, false);
        out.append("Shape: ").append(getShape());
        return out.toString();
    }

    /**
     * Has boolean.
     *
     * @param i the i
     * @param j the j
     * @return the boolean
     */
    public boolean has(int i, int j) {
        return values[index(i, j)];
    }
}
