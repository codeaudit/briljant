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

package org.briljantframework.io;

import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.vector.VectorType;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The {@code DataFrameInputStream} is supposed to read a {@code DataFrame} from an input source.
 * <p>
 * There are three steps associated with this
 * <ol>
 * <li>Read the types of the Columns via {@link #readColumnTypes()}</li>
 * <li>Read the names of the Columns via {@link #readColumnIndex()}</li>
 * <li>Read values</li>
 * </ol>
 * <p>
 * The simplest is to use the convince methods {@link #readColumnTypes()} and
 * {@link #readColumnIndex()} constructing a {@link DataFrame.Builder} and use its
 * {@link org.briljantframework.dataframe.DataFrame.Builder#read(EntryReader)} method.
 * <p>
 * For example: <code>
 * <pre>
 *      DataFrameInputStream dfis = ...;
 *      Collection<Type> types = dfis.readColumnTypes();
 *      Collection<String> names = dfis.readColumnNames();
 *      DataFrame.Builder builder = new MixedDataFrame(names, types);
 *      DataFrame dataFrame = builder.read(dfis).build();
 * </pre>
 * </code>
 *
 * Entries returned by {@link #next()} are returned in row-major order and typed according to the
 * {@link org.briljantframework.vector.VectorType}s returned by {@link #readColumnTypes()}.
 *
 * For example, given the dataset, where the first and second row are names and types respectively:
 *
 * <pre>
 *     a       b       c
 *   double  string   int
 *    3.2     hello    1
 *    2.0     sx       3
 *    2       dds     100
 * </pre>
 *
 * {@link #readColumnIndex()} should return {@code ["a", "b", "c"]} and {@link #readColumnTypes()}
 * should return {@code [DoubleVector.TYPE, StringVector.TYPE, IntVector.TYPE]}.
 *
 * Then, subsequent calls to {@link #next()} should return a
 * {@link org.briljantframework.io.DataEntry} with {@code [3.2, "hello", 1]}, {@code [2.0 "sx", 3]}
 * and {@code [2, "dds", 100]} in sequence.
 *
 * Hence, summing the columns of
 *
 * <pre>
 *     a       b       c
 *   double  double   int
 *    3.2     3        1
 *    2.0     4        3
 *    2       7       100
 * </pre>
 *
 * Is as simple as
 *
 * <pre>
 * try (DataFrameInputStream dfis = new CsvInputStream(&quot;file.txt&quot;)) {
 *   Map&lt;Integer, Double&gt; sum = new HashMap&lt;&gt;();
 *   dfis.readColumnNames();
 *   dfis.readColumnTypes();
 *   while (dfis.hasNext()) {
 *     DataEntry entry = dfis.next();
 *     for (int i = 0; i &lt; entry.size() &amp;&amp; entry.hasNext(); i++) {
 *       sum.put(i, entry.nextDouble());
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 *
 * @author Isak Karlsson
 */
public abstract class DataInputStream extends FilterInputStream implements EntryReader {

  protected static final String NAMES_BEFORE_TYPE = "Can't read name before types";
  protected static final String UNEXPECTED_EOF = "Unexpected EOF.";
  protected static final String VALUES_BEFORE_NAMES_AND_TYPES =
      "Reading values before names and types";
  protected static final String MISMATCH = "Types and values does not match (%d, %d) at line %d";


  /**
   * @param in the underlying input stream
   */
  protected DataInputStream(InputStream in) {
    super(in);
  }

  /**
   * Reads the column types of this data frame input stream. Returns {@code null} when there are no
   * more types to read.
   *
   * @return a type or {@code null}
   */
  protected abstract VectorType readColumnType() throws IOException;

  /**
   * Reads the column names from the input stream. Returns {@code null} when there are no more
   * column names.
   *
   * @return a column name or {@code null}
   */
  protected abstract String readColumnName() throws IOException;

  /**
   * For convenience. This method reads all column types from the input stream.
   * <p>
   * Same as:
   *
   * <pre>
   * Type t = null;
   * while ((t = f.readColumnType()) != null) {
   *   coll.add(t);
   * }
   * </pre>
   *
   * @return a collection of types
   */
  public Collection<VectorType> readColumnTypes() throws IOException {
    List<VectorType> types = new ArrayList<>();
    for (VectorType type = readColumnType(); type != null; type = readColumnType()) {
      types.add(type);
    }
    return Collections.unmodifiableCollection(types);
  }

  /**
   * For convenience. This method read all the column names from the input stream.
   * <p>
   * Same as:
   *
   * <pre>
   * String n = null;
   * while ((n = f.readColumnName()) != null) {
   *   coll.add(t);
   * }
   * </pre>
   *
   * @return a collection of column names
   */
  public Collection<Object> readColumnIndex() throws IOException {
    List<Object> names = new ArrayList<>();
    for (String type = readColumnName(); type != null; type = readColumnName()) {
      names.add(type);
    }
    return Collections.unmodifiableCollection(names);
  }

}