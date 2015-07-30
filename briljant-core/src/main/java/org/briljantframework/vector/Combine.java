package org.briljantframework.vector;

import org.briljantframework.complex.Complex;

import java.util.function.BiFunction;

/**
 * @author Isak Karlsson
 */
public final class Combine {

  private Combine() {
  }

  /**
   * Returns a {@code BiFunction} that ignores {@code NA} values by returning {@code NA} when
   * either
   * the {@code left} or {@code right} value is {@code NA}.
   *
   * @param combine the function to apply for non-{@code NA} values
   * @param <T>     the input type
   * @param <R>     the output type
   * @return a {@code NA} safe {@code BiFunction}
   */
  public static <T, R> BiFunction<T, T, R> ignoreNA(BiFunction<T, T, R> combine) {
    return (a, b) -> {
      boolean aNA = Is.NA(a);
      boolean bNA = Is.NA(b);
      if (aNA || bNA) {
        return null;
      } else {
        return combine.apply(a, b);
      }
    };
  }

  /**
   * Returns a {@code BiFunction} that ignores {@code NA} values by returning {@code NA} when both
   * the {@code left} or {@code right} value is {@code NA} and fill the {@code left} or {@code
   * right} value when either is {@code NA}.
   *
   * <p>For example:
   * <pre>{@code
   *  BiFunction<Integer, Integer, Integer> adder = ignoreNA((a, b) -> a + b), 10);
   *  adder(null, 1);    // 11
   *  adder(1, null);    // 11
   *  adder(null, null); // NA
   *  adder(1, 1);       // 2
   * }</pre>
   *
   * @param combine the function to apply for non-{@code NA} values
   * @param <T>     the input type
   * @param <R>     the output type
   * @return a {@code NA} safe {@code BiFunction}
   */
  public static <T, R> BiFunction<T, T, R> ignoreNA(BiFunction<T, T, R> combine, T fillValue) {
    return (a, b) -> {
      boolean aNA = Is.NA(a);
      boolean bNA = Is.NA(b);
      if (aNA && bNA) {
        return null;
      } else {
        if (aNA) {
          return combine.apply(fillValue, b);
        } else if (bNA) {
          return combine.apply(a, fillValue);
        } else {
          return combine.apply(a, b);
        }
      }
    };
  }

  /**
   * @return an adder that ignores {@code NA}
   */
  public static BiFunction<Number, Number, Number> add() {
    return ignoreNA(Combine::plusNumber);
  }

  /**
   * @return an adder that ignores {@code NA} and defaults to {@code fillValue}
   * @see #ignoreNA(java.util.function.BiFunction, Object)
   */
  public static BiFunction<Number, Number, Number> add(Number fillValue) {
    return ignoreNA(Combine::plusNumber, fillValue);
  }

  public static BiFunction<Number, Number, Number> mul() {
    return ignoreNA(Combine::multiplyNumber);
  }

  public static BiFunction<Number, Number, Number> multiply(Number fillValue) {
    return ignoreNA(Combine::multiplyNumber, fillValue);
  }

  public static BiFunction<Number, Number, Number> div() {
    return ignoreNA(Combine::divNumber);
  }

  public static BiFunction<Number, Number, Number> div(Number fillValue) {
    return ignoreNA(Combine::divNumber, fillValue);
  }

  public static BiFunction<Number, Number, Number> sub() {
    return ignoreNA(Combine::minusNumber);
  }

  public static BiFunction<Number, Number, Number> sub(Number fillValue) {
    return ignoreNA(Combine::minusNumber, fillValue);
  }

  private static Number plusNumber(Number a, Number b) {
    if (a instanceof Integer && b instanceof Integer ||
        (a instanceof Short && b instanceof Short)) {
      return a.intValue() + b.intValue();
    } else if (a instanceof Complex && b instanceof Complex) {
      return ((Complex) a).plus((Complex) b);
    } else if (a instanceof Long && b instanceof Long) {
      return a.longValue() + b.longValue();
    } else {
      return a.doubleValue() + b.doubleValue();
    }
  }

  private static Number minusNumber(Number a, Number b) {
    if (a instanceof Integer && b instanceof Integer ||
        (a instanceof Short && b instanceof Short)) {
      return a.intValue() - b.intValue();
    } else if (a instanceof Complex && b instanceof Complex) {
      return ((Complex) a).minus((Complex) b);
    } else if (a instanceof Long && b instanceof Long) {
      return a.longValue() - b.longValue();
    } else {
      return a.doubleValue() - b.doubleValue();
    }
  }

  private static Number multiplyNumber(Number a, Number b) {
    if (a instanceof Integer && b instanceof Integer) {
      return a.intValue() * b.intValue();
    } else if (a instanceof Complex && b instanceof Complex) {
      return ((Complex) a).multiply((Complex) b);
    } else if (a instanceof Long && b instanceof Long) {
      return a.longValue() * b.longValue();
    } else {
      return a.doubleValue() * b.doubleValue();
    }
  }

  private static Number divNumber(Number a, Number b) {
    if (a instanceof Integer && b instanceof Integer) {
      return a.intValue() / b.intValue();
    } else if (a instanceof Complex && b instanceof Complex) {
      return ((Complex) a).div((Complex) b);
    } else if (a instanceof Long && b instanceof Long) {
      return a.longValue() / b.longValue();
    } else {
      return a.doubleValue() / b.doubleValue();
    }
  }
}