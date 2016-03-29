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
package org.briljantframework.data.dataframe.join;

import org.briljantframework.Check;

/**
 * @author Isak Karlsson
 */
class ArrayJoiner extends Joiner {

  private final int[] leftSorted;
  private final int[] rightSorted;

  ArrayJoiner(int[] leftSorted, int[] rightSorted) {
    Check.argument(leftSorted.length == rightSorted.length, "indexers must have the same length.");
    this.leftSorted = leftSorted;
    this.rightSorted = rightSorted;
  }

  @Override
  public int getLeftIndex(int i) {
    return leftSorted[i];
  }

  @Override
  public int getRightIndex(int i) {
    return rightSorted[i];
  }

  @Override
  public int size() {
    return leftSorted.length;
  }
}
