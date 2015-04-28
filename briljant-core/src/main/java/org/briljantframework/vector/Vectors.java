package org.briljantframework.vector;

import com.google.common.base.Function;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Ints;

import org.briljantframework.Check;
import org.briljantframework.IndexComparator;
import org.briljantframework.QuickSort;
import org.briljantframework.complex.Complex;
import org.briljantframework.stat.DescriptiveStatistics;
import org.briljantframework.stat.RunningStatistics;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Isak Karlsson
 */
public final class Vectors {

  public static final VectorType STRING = StringVector.TYPE;
  public static final VectorType BIT = BitVector.TYPE;
  public static final VectorType INT = IntVector.TYPE;
  public static final VectorType COMPLEX = ComplexVector.TYPE;
  public static final VectorType DOUBLE = DoubleVector.TYPE;
  public static final VectorType VARIABLE = VariableVector.TYPE;
  public static final VectorType UNDEFINED = Undefined.TYPE;
  public static final Set<VectorType> NUMERIC = Sets.newHashSet();
  public static final Set<VectorType> CATEGORIC = Sets.newHashSet();
  public static final Map<Class<?>, VectorType> CLASS_TO_VECTOR_TYPE;

  static {
    NUMERIC.add(DOUBLE);
    NUMERIC.add(INT);
    NUMERIC.add(COMPLEX);

    CATEGORIC.add(STRING);
    CATEGORIC.add(BIT);

    CLASS_TO_VECTOR_TYPE = ImmutableMap.<Class<?>, VectorType>builder()
        .put(Integer.class, INT)
        .put(Integer.TYPE, INT)
        .put(Double.class, DOUBLE)
        .put(Double.TYPE, DOUBLE)
        .put(String.class, STRING)
        .put(Boolean.class, BIT)
        .put(Bit.class, BIT)
        .put(Complex.class, COMPLEX)
        .build();
  }

  private Vectors() {
  }

  /**
   * Finds the index, in {@code vector}, of the value at {@code index} in {@code values}. This
   * should be preferred over {@link #find(Vector, Value)} when possible. Hence, given {@code
   * Vector
   * a}, {@code Vector b} and the index {@code i}, {@code find(a, b, i)} should be preferred over
   * {@code find(a, b.get(i))}.
   *
   * @param haystack     the vector to search
   * @param needleSource the source of the needle
   * @param needle       the needle in the source
   * @return the (first) index of {@code needleSource.get(needle)} in {@code haystack} or {@code -1}
   */
  public static int find(Vector haystack, Vector needleSource, int needle) {
    for (int i = 0; i < haystack.size(); i++) {
      if (haystack.compare(i, needleSource, needle) == 0) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Finds the index of {@code needle} in {@code haystack} or return {@code -1} if value cannot be
   * found.
   *
   * @param haystack the haystack
   * @param needle   the needle
   * @param <T>      the type of object to be searched for
   * @return the index of {@code needle} or {@code -1}
   */
  public static <T> int find(Vector haystack, T needle) {
    Class<?> cls = needle.getClass();
    for (int i = 0; i < haystack.size(); i++) {
      if (haystack.get(cls, i).equals(needle)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @see #find(Vector, Vector, int)
   */
  public static int find(Vector haystack, Value needle) {
    for (int i = 0; i < haystack.size(); i++) {
      if (haystack.compare(i, needle) == 0) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @see #find(Vector, Object)
   */
  public static int find(Vector haystack, int needle) {
    return find(haystack, Convert.toValue(needle));
  }

  /**
   * @see #find(Vector, Object)
   */
  public static int find(Vector haystack, String needle) {
    return find(haystack, Convert.toValue(needle));
  }

  /**
   * Finds the index of the first value for which {@code predicate} returns true.
   *
   * @param vector    the vector
   * @param predicate the predicate
   * @return the index or {@code -1} if no value matched the predicate {@code true}
   */
  public static int find(Vector vector, Predicate<Value> predicate) {
    for (int i = 0; i < vector.size(); i++) {
      if (predicate.test(vector.getAsValue(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * @param in the vector
   * @return a new vector sorted in ascending order
   */
  public static Vector sortAsc(Vector in) {
    Vector.Builder builder = in.newCopyBuilder();
    QuickSort.quickSort(0, in.size(), builder::compare, builder);
    return builder.build();
  }

  /**
   * @param in the vector
   * @return a new vector sorted in ascending order
   */
  public static Vector sortDesc(Vector in) {
    Vector.Builder builder = in.newCopyBuilder();
    QuickSort.quickSort(0, in.size(), (a, b) -> builder.compare(b, a), builder);
    return builder.build();
  }

  public static Vector sort(Vector in, IndexComparator<? super Vector> cmp) {
    Vector.Builder builder = in.newCopyBuilder();
    Vector tmp = builder.getTemporaryVector();
    QuickSort.quickSort(0, in.size(), (a, b) -> cmp.compare(tmp, a, b), builder);
    return builder.build();
  }

  /**
   * Sorts the vector according to {@code comparator} treating the values as {@code cls}
   *
   * @param cls        the value to sort
   * @param in         the vector
   * @param comparator the comparator
   * @param <T>        the typ
   * @return a new vector; sorted according to comparator
   */
  public static <T> Vector sort(Class<T> cls, Vector in, Comparator<T> comparator) {
    Vector.Builder builder = in.newCopyBuilder();
    Vector tmp = builder.getTemporaryVector();
    QuickSort.quickSort(0, in.size(),
                        (a, b) -> comparator.compare(tmp.get(cls, a), tmp.get(cls, b)),
                        builder);
    return builder.build();
  }

  /**
   * <p> Create a vector of length {@code num} with evenly spaced values between {@code start} and
   * {@code end}. </p>
   *
   * <p> Returns a vector of {@link org.briljantframework.vector.DoubleVector#TYPE} </p>
   *
   * @param start the start value
   * @param stop  the end value
   * @param num   the number of steps (i.e. intermediate values)
   * @return a vector
   */
  public static Vector linspace(double start, double stop, int num) {
    DoubleVector.Builder builder = new DoubleVector.Builder(0, num);
    double step = (stop - start) / (num - 1);
    double value = start;
    for (int index = 0; index < num; index++) {
      builder.set(index, value);
      value += step;
    }

    return builder.build();
  }

  /**
   * Returns a vector of length {@code 50}. With evenly spaced values in the range {@code start} to
   * {@code end}.
   *
   * @param start the start value
   * @param stop  the end value
   * @return a vector
   */
  public static Vector linspace(double start, double stop) {
    return linspace(start, stop, 50);
  }

  /**
   * <p> Split {@code vector} into {@code chunks}. Handles the case when {@code vector.size()} is
   * not evenly dividable by chunks by making some chunks larger. </p>
   *
   * <p> This implementation is lazy, i.e. chunking is done 'on-the-fly'. To get a list, {@code new
   * ArrayList<>(Vectors.split(vec, 10))} </p>
   *
   * <p> Ensures that {@code vector.getType()} is preserved. </p>
   *
   * @param vector the vector
   * @param chunks the number of chunks
   * @return a collection of {@code chunk} chunks
   */
  public static Collection<Vector> split(Vector vector, int chunks) {
    checkArgument(vector.size() >= chunks, "size must be shorter than chunks");
    if (vector.size() == chunks) {
      return Collections.singleton(vector);
    }
    int bin = vector.size() / chunks;
    int remainder = vector.size() % chunks;

    return new AbstractCollection<Vector>() {
      @Override
      public Iterator<Vector> iterator() {
        return new UnmodifiableIterator<Vector>() {
          private int current = 0;
          private int remainders = 0;

          @Override
          public boolean hasNext() {
            return current < vector.size();
          }

          @Override
          public Vector next() {
            int binSize = bin;
            if (remainders < remainder) {
              remainders++;
              binSize += 1;
            }
            Vector.Builder builder = vector.newBuilder();
            for (int i = 0; i < binSize; i++) {
              builder.add(vector, current++);
            }
            return builder.build();
          }
        };
      }

      @Override
      public int size() {
        return chunks;
      }
    };
  }

  /**
   * Computes the {@code Double} descriptive statistics of {@code vector}
   *
   * @param vector a vector (with {@code type = double})
   * @return the descriptive statistics
   */
  public DescriptiveStatistics statistics(Vector vector) {
    RunningStatistics r = new RunningStatistics();
    for (int i = 0; i < vector.size(); i++) {
      if (!vector.isNA(i)) {
        r.add(vector.getAsDouble(i));
      }
    }
    return r;
  }

  /**
   * <p>Computes the population standard deviation of {@code vector}.
   *
   * <p>A vector of all {@code NA} returns {@code NA}
   *
   * @param vector the vector
   * @return the standard deviation
   */
  public static double std(Vector vector) {
    return std(vector, mean(vector));
  }

  /**
   * <p>Computes the population standard deviation of {@code vector} using an already computed
   * {@code mean}.
   *
   * <p>A vector of all {@code NA} returns {@code NA}
   *
   * @param vector the vector
   * @param mean   the mean
   * @return the standard deviation
   */
  public static double std(Vector vector, double mean) {
    double var = var(vector, mean);
    return Is.NA(var) ? DoubleVector.NA : Math.sqrt(var);
  }

  /**
   * <p>Computes the sample mean of {@code vector}.
   *
   * <p>A vector of all {@code NA} returns {@code NA}
   *
   * @param vector the vector
   * @return the mean; or NA
   */
  public static double mean(Vector vector) {
    double mean = 0;
    int nonNA = 0;
    for (int i = 0; i < vector.size(); i++) {
      if (!vector.isNA(i)) {
        mean += vector.getAsDouble(i);
        nonNA += 1;
      }
    }

    return nonNA == 0 ? DoubleVector.NA : mean / (double) nonNA;
  }

  /**
   * <p>Computes the population variance of {@code vector} using an already computed
   * {@code mean}.
   *
   * <p>A vector of all {@code NA} returns {@code NA}
   *
   * @param vector the vector
   * @param mean   the mean
   * @return the variance; or NA
   */
  public static double var(Vector vector, double mean) {
    double var = 0;
    int nonNA = 0;
    for (int i = 0; i < vector.size(); i++) {
      if (!vector.isNA(i)) {
        double residual = vector.getAsDouble(i) - mean;
        var += residual * residual;
        nonNA += 1;
      }
    }
    return nonNA == 0 ? DoubleVector.NA : var / (double) nonNA;
  }

  /**
   * <p>Computes the population variance of {@code vector}.
   *
   * <p>A vector of all {@code NA} returns {@code NA}
   *
   * @param vector the vector
   * @return the variance
   */
  public static double var(Vector vector) {
    return var(vector, mean(vector));
  }

  /**
   * Computes the sum of values in {@code vector}. Ignores {@code NA} values.
   *
   * @param vector the vector
   * @return the sum
   */
  public static double sum(Vector vector) {
    double sum = 0;
    for (int i = 0; i < vector.size(); i++) {
      double d = vector.getAsDouble(i);
      sum += !Is.NA(d) ? d : 0;
    }
    return sum;
  }

  /**
   * Finds the minimum value in {@code v}. Ignores {@code NA} values.
   *
   * @param v the vector
   * @return the minimum value or {@code NA} if all values are {@code NA}
   */
  public static double min(Vector v) {
    return v.doubleStream().filter(Is::NA).min().orElse(DoubleVector.NA);
  }

  /**
   * Finds the maximum value in {@code v}. Ignores {@code NA} values.
   *
   * @param v the vector
   * @return the maximum value or {@code NA} if all values are {@code NA}
   */
  public static double max(Vector v) {
    return v.doubleStream().filter(Is::NA).max().orElse(DoubleVector.NA);
  }

  /**
   * Return the most frequently occurring item in {@code v}
   *
   * @param v the vector
   * @return the most frequent item; or
   */
  public static Value mode(Vector v) {
    Multiset<Value> values = HashMultiset.create();
    v.stream().forEach(values::add);
    return Ordering.natural().onResultOf(new Function<Multiset.Entry<Value>, Integer>() {
      @Override
      public Integer apply(Multiset.Entry<Value> input) {
        return input.getCount();
      }
    }).max(values.entrySet()).getElement();
  }

  /**
   * <p>Returns a vector consisting of the unique values in {@code vectors}
   *
   * <p>For example, given {@code a, b} and {@code c}
   * <pre>{@code
   * Vector a = new IntVector(1,2,3,4);
   * Vector b = new IntVector(2,3,4,5);
   * Vector c = new IntVector(3,4,5,6);
   *
   * Vector d = Vectors.unique(a, b, c);
   * // d == [1,2,3,4,5,6];
   * }</pre>
   */
  public static Vector unique(Vector... vectors) {
    vectors = checkNotNull(vectors);
    checkArgument(vectors.length > 0);
    Vector.Builder builder = vectors[0].newBuilder();
    Set<Value> taken = new HashSet<>();
    for (Vector vector : vectors) {
      for (int i = 0; i < vector.size(); i++) {
        Value value = vector.getAsValue(i);
        if (!taken.contains(value)) {
          taken.add(value);
          builder.add(vector, i);
        }
      }
    }
    return builder.build();
  }

  /**
   * <p> Counts the number of occurrences for each value (of type {@code T}) in {@code vector}
   *
   * <p> Since {@link Vector#get(Class, int)} returns {@code NA} if value is not an instance of
   * {@code T}, the resulting {@code Map} might contain a {@code null} key
   *
   * @param cls    the class
   * @param vector the vector
   * @param <T>    the type
   * @return a map of values to counts
   */
  public static <T> Map<T, Integer> count(Class<T> cls, Vector vector) {
    Map<T, Integer> count = new HashMap<>();
    for (T value : vector.asList(cls)) {
      count.compute(value, (x, v) -> v == null ? 1 : v + 1);
    }
    return Collections.unmodifiableMap(count);
  }

  /**
   * <p> Counts the number of occurrences for each value (wrapping the in a {@link Value}) in
   * {@code
   * vector}
   *
   * <p> {@code NA} values are stored with {@link org.briljantframework.vector.Undefined#INSTANCE}
   * as key
   *
   * @param vector the vector
   * @return a map of values to counts
   */
  public static Map<Value, Integer> count(Vector vector) {
    Map<Value, Integer> freq = new HashMap<>();
    for (Value value : vector.asValueList()) {
      freq.compute(value, (x, i) -> i == null ? 1 : i + 1);
    }
    return Collections.unmodifiableMap(freq);
  }


  /**
   * @param vector the vector
   * @return the indexes of {@code vector} sorted in increasing order by value
   */
  public static int[] indexSort(Vector vector) {
    return indexSort(vector, (o1, o2) -> Double.compare(vector.getAsDouble(o1),
                                                        vector.getAsDouble(o2)));
  }

  /**
   * @param vector     the vector
   * @param comparator the comparator
   * @return the indexes of {@code vector} sorted according to {@code comparator} by value
   */
  public static int[] indexSort(Vector vector, Comparator<Integer> comparator) {
    int[] indicies = new int[vector.size()];
    for (int i = 0; i < indicies.length; i++) {
      indicies[i] = i;
    }
    List<Integer> tempList = Ints.asList(indicies);
    Collections.sort(tempList, comparator);
    return indicies;
  }

  /**
   * Inner product, i.e. the dot product x * y. Handles {@code NA} values.
   *
   * @param x a vector
   * @param y a vector
   * @return the dot product
   */
  public static double dot(Vector x, Vector y) {
    return dot(x, 1, y, 1);
  }

  /**
   * Take the inner product of two vectors (m x 1) and (1 x m) scaling them by alpha and beta
   * respectively
   *
   * @param x     a row vector
   * @param alpha scaling factor for a
   * @param y     a column vector
   * @param beta  scaling factor for y
   * @return the inner product
   */
  public static double dot(Vector x, double alpha, Vector y, double beta) {
    Check.size(x, y);
    int size = y.size();
    double dot = 0;
    for (int i = 0; i < size; i++) {
      if (!x.isNA(i) && !y.isNA(i)) {
        dot += (alpha * x.getAsDouble(i)) * (beta * y.getAsDouble(i));
      }
    }
    return dot;
  }

  /**
   * Compute the sigmoid between a and b, i.e. 1/(1+e^(a'-b))
   *
   * @param a a vector
   * @param b a vector
   * @return the sigmoid
   */
  public static double sigmoid(Vector a, Vector b) {
    return 1.0 / (1 + Math.exp(dot(a, 1, b, -1)));
  }
}
