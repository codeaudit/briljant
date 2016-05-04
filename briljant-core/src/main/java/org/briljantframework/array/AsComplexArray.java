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
package org.briljantframework.array;

import org.apache.commons.math3.complex.Complex;
import org.briljantframework.array.api.ArrayBackend;

/**
 * Class for views over complex arrays.
 *
 * @author Isak Karlsson
 */
public abstract class AsComplexArray extends AbstractComplexArray {

  public AsComplexArray(AbstractBaseArray<?> array) {
    super(array.getArrayBackend(), array.getOffset(), array.getShape(), array.getStride());
  }

  AsComplexArray(ArrayBackend bj, int offset, int[] shape, int[] stride) {
    super(bj, offset, shape, stride);
  }

  @Override
  public ComplexArray asView(int offset, int[] shape, int[] stride) {
    return new AsComplexArray(getArrayBackend(), offset, shape, stride) {
      @Override
      protected void setElement(int i, Complex value) {
        AsComplexArray.this.setElement(i, value);
      }

      @Override
      protected Complex getElement(int i) {
        return AsComplexArray.this.getElement(i);
      }

      @Override
      protected int elementSize() {
        return AsComplexArray.this.elementSize();
      }
    };
  }

  @Override
  public ComplexArray newEmptyArray(int... shape) {
    return getArrayBackend().getArrayFactory().newComplexArray(shape);
  }

  @Override
  public boolean isView() {
    return true;
  }
}
