package org.briljantframework.vector;

import org.briljantframework.exceptions.TypeConversionException;
import org.briljantframework.io.DataEntry;
import org.briljantframework.io.reslover.Resolver;
import org.briljantframework.io.reslover.Resolvers;
import org.briljantframework.matrix.Matrix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Isak Karlsson
 */
public class GenericVector extends AbstractVector {

  private final VectorType type;
  private final Class<?> cls;
  private final List<Object> values;

  @SuppressWarnings("unchecked")
  public <T> GenericVector(Class<T> cls, List<T> values) {
    this(cls, (List<Object>) values, null, true);
  }

  @SuppressWarnings("unchecked")
  public <T> GenericVector(Class<T> cls, List<T> values, Resolver<T> resolver) {
    this(cls, (List<Object>) values, resolver, true);
  }

  protected GenericVector(Class<?> cls, List<Object> values, Resolver<?> resolver, boolean copy) {
    this.cls = cls;
    this.values = copy ? new ArrayList<>(values) : values;
    this.type = VectorType.getInstance(cls);
  }

  @Override
  public Value getAsValue(int index) {
    return new GenericValue(get(cls, index));
  }

  @Override
  public <T> T get(Class<T> cls, int index) {
    Object obj = values.get(index);
    if (obj == null || !cls.isInstance(obj)) {
      return Na.valueOf(cls);
    }
    return cls.cast(obj);
  }

  @Override
  public String toString(int index) {
    Object o = values.get(index);
    return Is.NA(o) ? "NA" : o.toString();
  }

  @Override
  public boolean isNA(int index) {
    return values.get(index) == null;
  }

  @Override
  public double getAsDouble(int index) {
    return get(Double.TYPE, index);
  }

  @Override
  public int getAsInt(int index) {
    return get(Integer.TYPE, index);
  }

  @Override
  public Bit getAsBit(int index) {
    return Bit.valueOf(getAsInt(index));
  }

  @Override
  public String getAsString(int index) {
    return toString(index);
  }

  @Override
  public int size() {
    return values.size();
  }

  @Override
  public VectorType getType() {
    return type;
  }

  @Override
  public Matrix toMatrix() throws TypeConversionException {
    throw new TypeConversionException(String.format("Can't convert vector(%s) to matrix", cls));
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compare(int a, int b) {
    if (Comparable.class.isAssignableFrom(cls)) {
      return get(Comparable.class, a).compareTo(get(Comparable.class, b));
    }
    throw new UnsupportedOperationException();
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compare(int a, Vector other, int b) {
    if (Comparable.class.isAssignableFrom(cls)) {
      return get(Comparable.class, a).compareTo(other.get(Comparable.class, b));
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    return cls.hashCode() + values.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GenericVector) {
      if (!this.cls.equals(((GenericVector) obj).cls)) {
        return false;
      } else {
        return values.equals(((GenericVector) obj).values);
      }
    }
    return super.equals(obj);
  }

  public static class Builder implements Vector.Builder {

    private final Class<?> cls;
    private List<Object> buffer;
    private Resolver<?> resolver = null;

    public <T> Builder(Class<T> cls, Resolver<T> resolver) {
      this.cls = cls;
      this.resolver = resolver;
    }

    public Builder(Class<?> cls) {
      this.cls = cls;
      buffer = new ArrayList<>();
    }

    public Builder(Class<?> cls, int size) {
      this.cls = cls;
      buffer = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        buffer.add(null);
      }
    }

    @Override
    public Vector.Builder setNA(int index) {
      ensureCapacity(index);
      buffer.set(index, null);
      return this;
    }

    @Override
    public Vector.Builder addNA() {
      buffer.add(null);
      return this;
    }

    @Override
    public Vector.Builder add(Vector from, int fromIndex) {
      buffer.add(from.get(cls, fromIndex));
      return this;
    }

    @Override
    public Vector.Builder set(int atIndex, Vector from, int fromIndex) {
      ensureCapacity(atIndex);
      buffer.set(atIndex, from.get(cls, fromIndex));
      return this;
    }

    @Override
    public Vector.Builder set(int index, Object value) {
      ensureCapacity(index);
      if (value != null && cls.isInstance(value)) {
        buffer.set(index, value);
      } else if (value != null) {
        if (value instanceof Value) {
          buffer.set(index, ((Value) value).get(cls));
        } else {
          Resolver<?> resolver = this.resolver == null ? Resolvers.find(cls) : this.resolver;
          if (resolver == null) {
            buffer.set(index, null);
          } else {
            buffer.set(index, resolver.resolve(value));
          }
        }
      } else {
        buffer.set(index, null);
      }
      return this;
    }

    @Override
    public Vector.Builder add(Object value) {
      return set(size(), value);
    }

    @Override
    public Vector.Builder addAll(Vector from) {
      for (int i = 0; i < from.size(); i++) {
        add(from.get(cls, i));
      }
      return this;
    }

    @Override
    public Vector.Builder remove(int index) {
      buffer.remove(index);
      return this;
    }

    @Override
    public int compare(int a, int b) {
      if (Comparable.class.isAssignableFrom(cls)) {
        Comparable va = (Comparable) buffer.get(a);
        Comparable vb = (Comparable) buffer.get(b);
        @SuppressWarnings("unchecked")
        int cmp = va.compareTo(vb);
        return cmp;
      }
      throw new UnsupportedOperationException();
    }

    @Override
    public void swap(int a, int b) {
      Collections.swap(buffer, a, b);
    }

    @Override
    public Vector.Builder read(DataEntry entry) throws IOException {
      return read(size(), entry);
    }

    @Override
    public Vector.Builder read(int index, DataEntry entry) throws IOException {
      ensureCapacity(index);
      buffer.set(index, entry.next(cls));
      return this;
    }

    @Override
    public int size() {
      return buffer.size();
    }

    @Override
    public Vector getTemporaryVector() {
      return new GenericVector(cls, buffer, resolver, false);
    }

    @Override
    public Vector build() {
      Vector vector = new GenericVector(cls, buffer, resolver, false);
      buffer = null;
      return vector;
    }

    private void ensureCapacity(int index) {
      while (index >= buffer.size()) {
        buffer.add(null);
      }
    }
  }
}