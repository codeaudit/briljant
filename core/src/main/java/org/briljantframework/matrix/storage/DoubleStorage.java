package org.briljantframework.matrix.storage;

import static com.google.common.primitives.Ints.checkedCast;

import java.util.Arrays;

import org.briljantframework.complex.Complex;

/**
 * @author Isak Karlsson
 */
public class DoubleStorage extends AbstractStorage {
  private final double[] values;

  public DoubleStorage(double[] values) {
    super(values.length);
    this.values = values;
  }

  public static DoubleStorage withSize(long size) {
    return new DoubleStorage(new double[checkedCast(size)]);
  }

  @Override
  public int getAsInt(int index) {
    return (int) values[((int) index)];
  }

  @Override
  public void setInt(int index, int value) {
    setDouble(index, value);
  }

  @Override
  public long getAsLong(int index) {
    return (long) getAsDouble(index);
  }

  @Override
  public void setLong(int index, long value) {
    setDouble(index, value);
  }

  @Override
  public double getAsDouble(int index) {
    return values[((int) index)];
  }

  @Override
  public void setDouble(int index, double value) {
    values[((int) index)] = value;
  }

  @Override
  public Complex getComplex(int index) {
    return Complex.valueOf(getAsDouble(index));
  }

  @Override
  public void setComplex(int index, Complex complex) {
    setDouble(index, complex.doubleValue());
  }

  @Override
  public void setNumber(int index, Number value) {
    setDouble(index, value.doubleValue());
  }

  @Override
  public Number getNumber(int index) {
    return getAsDouble(index);
  }

  @Override
  public boolean isArrayBased() {
    return true;
  }

  @Override
  public double[] asDoubleArray() {
    return values;
  }

  @Override
  public Class<?> getNativeType() {
    return Double.TYPE;
  }

  @Override
  public Storage copy() {
    return new DoubleStorage(Arrays.copyOf(values, values.length));
  }
}