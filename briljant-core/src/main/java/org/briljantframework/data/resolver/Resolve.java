/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Isak Karlsson
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
package org.briljantframework.data.resolver;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexFormat;
import org.briljantframework.data.Is;
import org.briljantframework.data.Logical;
import org.briljantframework.data.Na;
import org.briljantframework.data.reader.DataEntry;
import org.briljantframework.data.series.Series;

/**
 * This class provide options for <em>resolving</em> a value from one class to another. For example,
 * if none of the default resolvers are modified, {@code Resolve.to(Integer.class,
 * "2011")} would produce the {@code int} value {@code 2011}.
 *
 * <p/>
 * This class is thread-safe, i.e. calls to {@link #install(Class, Resolver)} and
 * {@link #find(Class)} can be made in different threads. The guarantees provided are the same as
 * those provided by the {@linkplain ConcurrentHashMap}.
 *
 * <p/>
 * To install a new {@link Resolver} use {@link #install(Class, Resolver)}, for example, given a
 * class
 *
 * <pre>
 * class Employee {
 *   String name, familyName
 *   public Employee(String name, String familyName) {
 *     this.name = name;
 *     this.familyName = familyName;
 *   }
 * 
 *   public String toString(){
 *     return "Employee{name=" +name + ", familyName=" + familyName + "}";
 *   }
 * }
 * </pre>
 *
 * <pre>
 * Resolver&lt;Employee&gt; employeeResolver = new Resolver&lt;&gt;(Employee.class);
 * employeeResolver.put(String.class, v -&gt; new Employee(v.split(&quot;\\s+&quot;)[0], v.split(&quot;\\s+&quot;)[1]));
 * Resolve.install(Employee.class, employeeResolver);
 * Employee e = Resolve.to(Employee.class, &quot;Foo Bar&quot;);
 * </pre>
 *
 * Once installed, {@link Series.Builder#read(DataEntry)} will
 * pick up the resolver and produce vectors accordingly, e.g., we can produce a series of employees
 *
 * <pre>
 * DataEntry entry = new StringDataEntry(new String[] {&quot;Foo Bar&quot;, &quot;Bob Bobson&quot;, &quot;John Doe&quot;});
 * Series series = Series.Builder.of(Employee.class).readAll(entry).build();
 * Employee bob = series.get(Employee.class, 1);
 * </pre>
 *
 * which would render the following series
 *
 * <pre>
 * 0 Employee{name=Foo, familyName=Bar}
 * 1 Employee{name=Bob, familyName=Bobson}
 * 2 Employee{name=John, familyName=Doe}
 * Length: 3 type: Employee
 * </pre>
 *
 * @author Isak Karlsson
 */
public final class Resolve {

  private static final Map<Class<?>, Resolver<?>> RESOLVERS = new ConcurrentHashMap<>();
  private static final ComplexFormat COMPLEX_FORMAT = ComplexFormat.getInstance();

  static {
    Resolver<LocalDate> localDateResolver = initializeLocalDateResolver();
    Resolver<Integer> integerResolver = initializeIntegerResolver();
    Resolver<Double> doubleResolver = initializeDoubleResolver();
    Resolver<String> stringResolver = initializeStringResolver();
    Resolver<Complex> complexResolver = initializeComplexResolver();
    Resolver<Logical> logicalResolver = initializeLogicalResolver();
    Resolver<Object> objectResolver = initializeObjectResolver();

    install(Logical.class, logicalResolver);
    install(LocalDate.class, localDateResolver);
    install(String.class, stringResolver);
    install(Double.class, doubleResolver);
    install(Integer.class, integerResolver);
    install(Complex.class, complexResolver);
    install(Object.class, objectResolver);
  }

  private Resolve() {}

  private static Resolver<Object> initializeObjectResolver() {
    Resolver<Object> objectResolver = new Resolver<>(Object.class);
    objectResolver.put(Object.class, v -> v);
    return objectResolver;
  }

  private static Resolver<Logical> initializeLogicalResolver() {
    Resolver<Logical> logicalResolver = new Resolver<>(Logical.class);
    logicalResolver.put(String.class, v -> Logical.valueOf(v.trim().equalsIgnoreCase("true")));
    logicalResolver.put(Boolean.class, v -> v ? Logical.TRUE : Logical.FALSE);
    logicalResolver.put(Number.class, v -> v.intValue() == 1 ? Logical.TRUE : Logical.FALSE);
    return logicalResolver;
  }

  private static Resolver<Complex> initializeComplexResolver() {
    Resolver<Complex> complexResolver = new Resolver<>(Complex.class);
    complexResolver.put(Number.class, v -> Complex.valueOf(v.doubleValue()));
    complexResolver.put(Double.class, Complex::valueOf);
    complexResolver.put(Integer.class, Complex::valueOf);
    complexResolver.put(Short.class, Complex::valueOf);
    complexResolver.put(Byte.class, Complex::valueOf);
    complexResolver.put(Float.class, Complex::valueOf);
    complexResolver.put(Logical.class, v -> Logical.TRUE.equals(v) ? Complex.ONE : Complex.ZERO);
    complexResolver.put(String.class, s -> {
      try {
        return COMPLEX_FORMAT.parse(s);
      } catch (Exception e) {
        return null;
      }
    });
    return complexResolver;
  }

  private static Resolver<Double> initializeDoubleResolver() {
    Resolver<Double> doubleResolver = new Resolver<>(Double.class);
    doubleResolver.put(Number.class, Number::doubleValue);
    doubleResolver.put(Double.class, Number::doubleValue);
    doubleResolver.put(Double.TYPE, Number::doubleValue);
    doubleResolver.put(Float.class, Number::doubleValue);
    doubleResolver.put(Float.TYPE, Number::doubleValue);
    doubleResolver.put(Long.class, Number::doubleValue);
    doubleResolver.put(Long.TYPE, Number::doubleValue);
    doubleResolver.put(Integer.class, Number::doubleValue);
    doubleResolver.put(Integer.TYPE, Number::doubleValue);
    doubleResolver.put(Short.class, Number::doubleValue);
    doubleResolver.put(Short.TYPE, Number::doubleValue);
    doubleResolver.put(Byte.class, Number::doubleValue);
    doubleResolver.put(Byte.TYPE, Number::doubleValue);

    doubleResolver.put(String.class, s -> {
      Number n = toNumber(s);
      return n != null ? n.doubleValue() : Na.BOXED_DOUBLE;
    });
    return doubleResolver;
  }

  private static Number toNumber(String str) {
    return NumberUtils.isNumber(str) ? NumberUtils.createNumber(str) : null;
  }

  private static Resolver<String> initializeStringResolver() {
    Resolver<String> stringResolver = new Resolver<>(String.class);
    stringResolver.put(Object.class, Object::toString);
    return stringResolver;
  }

  private static Resolver<Integer> initializeIntegerResolver() {
    Resolver<Integer> resolver = new Resolver<>(Integer.class);
    resolver.put(Number.class, Number::intValue);
    resolver.put(Double.class, Number::intValue);
    resolver.put(Double.TYPE, Number::intValue);
    resolver.put(Float.class, Number::intValue);
    resolver.put(Float.TYPE, Number::intValue);
    resolver.put(Long.class, Number::intValue);
    resolver.put(Long.TYPE, Number::intValue);
    resolver.put(Integer.class, Number::intValue);
    resolver.put(Integer.TYPE, Number::intValue);
    resolver.put(Short.class, Number::intValue);
    resolver.put(Short.TYPE, Number::intValue);
    resolver.put(Byte.class, Number::intValue);
    resolver.put(Byte.TYPE, Number::intValue);
    resolver.put(String.class, s -> {
      try {
        return NumberUtils.createNumber(s).intValue();
      } catch (Exception e) {
        return null;
      }
    });
    return resolver;
  }

  private static Resolver<LocalDate> initializeLocalDateResolver() {
    Resolver<LocalDate> resolver = new Resolver<>(LocalDate.class);
    Converter<Long, LocalDate> longToLocalDate =
        (l) -> Instant.ofEpochMilli(l).atZone(ZoneId.systemDefault()).toLocalDate();

    resolver.put(String.class, StringToLocalDate.ISO_DATE);
    resolver.put(Long.class, longToLocalDate);
    resolver.put(Long.TYPE, longToLocalDate);
    return resolver;
  }

  /**
   * Install the specified resolver for the specified class.
   * 
   * @param cls the class
   * @param resolver the resolver
   * @param <T> the type
   */
  public static <T> void install(Class<T> cls, Resolver<T> resolver) {
    RESOLVERS.put(cls, resolver);
  }

  /**
   * Find a resolver for the the given value and convert it to a value of the given class.
   * 
   * @param cls the class to convert to
   * @param value a value to convert
   * @param <T> the type
   * @return an instance of T or NA if no resolver exists or the given value is null.
   */
  public static <T> T to(Class<T> cls, Object value) {
    if (Is.NA(value)) {
      return Na.of(cls);
    } else {
      Resolver<T> resolver = find(cls);
      if (resolver != null) {
        return resolver.resolve(value);
      } else {
        return Na.of(cls);
      }
    }
  }

  /**
   * Get a resolver for the given class.
   * 
   * @param cls the class
   * @param <T> the type
   * @return a resolver or null if no resolver is associated with the given class.
   */
  public static <T> Resolver<T> find(Class<T> cls) {
    @SuppressWarnings("unchecked")
    Resolver<T> resolver = (Resolver<T>) RESOLVERS.get(cls);
    return resolver;
  }

}
