package org.briljantframework.dataframe;

import org.briljantframework.vector.Binary;
import org.briljantframework.vector.Type;
import org.briljantframework.vector.Value;
import org.briljantframework.vector.Vector;

/**
 * Created by Isak Karlsson on 11/12/14.
 */
public class DataFrameColumnView implements Vector {

  private final DataFrame parent;
  private final int column;

  public DataFrameColumnView(DataFrame parent, int column) {
    this.parent = parent;
    this.column = column;
  }

  @Override
  public double getAsDouble(int index) {
    return parent.getAsDouble(index, column);
  }

  @Override
  public int getAsInt(int index) {
    return parent.getAsInt(index, column);
  }

  @Override
  public Binary getAsBinary(int index) {
    return parent.getAsBinary(index, column);
  }

  @Override
  public String getAsString(int index) {
    return parent.getAsString(index, column);
  }

  @Override
  public Value getAsValue(int index) {
    return parent.getAsValue(index, column);
  }

  @Override
  public String toString(int index) {
    return parent.toString(index, column);
  }

  @Override
  public boolean isNA(int index) {
    return parent.isNA(index, column);
  }

  @Override
  public int size() {
    return parent.rows();
  }

  @Override
  public Type getType() {
    return parent.getColumnType(column);
  }

  @Override
  public Builder newCopyBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Builder newBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Builder newBuilder(int size) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compare(int a, int b) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compare(int a, int b, Vector other) {
    throw new UnsupportedOperationException();
  }
}
