package org.briljantframework.dataframe;

import java.util.Arrays;

import junit.framework.TestCase;

import org.briljantframework.vector.IntVector;
import org.briljantframework.vector.StringVector;

public class InnerJoinTest extends TestCase {

  public void testJoin() throws Exception {
    DataFrame cats =
        MixedDataFrame.of("User", new StringVector("a", "b", "c", "d"), "Number of Cats",
            new IntVector(1, 2, 3, 4));

    DataFrame dogs =
        MixedDataFrame
            .of("User", new StringVector("b", "a"), "Number of dogs", new IntVector(1, 2));


    System.out.println(cats);
    System.out.println(dogs);
    System.out.println(DataFrames.innerJoin(cats, dogs, Arrays.asList(0)));


    long s = System.nanoTime();
    DataFrames.innerJoin(cats, dogs, Arrays.asList(0));
    System.out.println((System.nanoTime() - s) / 1e6);
  }
}