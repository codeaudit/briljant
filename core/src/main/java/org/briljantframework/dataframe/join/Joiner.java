package org.briljantframework.dataframe.join;

/**
 * Created by Isak on 2015-01-08.
 */
public interface Joiner {

  int size();

  int getLeftIndex(int i);

  int getRightIndex(int i);
}