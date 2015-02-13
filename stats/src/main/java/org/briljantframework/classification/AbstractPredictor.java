package org.briljantframework.classification;

import static org.briljantframework.matrix.Matrices.argmax;
import static org.briljantframework.matrix.Matrices.newDoubleMatrix;

import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.matrix.DoubleMatrix;
import org.briljantframework.vector.StringVector;
import org.briljantframework.vector.Value;
import org.briljantframework.vector.Vector;

import com.google.common.base.Preconditions;

/**
 * @author Isak Karlsson
 */
public abstract class AbstractPredictor implements Predictor {

  private final Vector classes;

  protected AbstractPredictor(Vector classes) {
    this.classes = Preconditions.checkNotNull(classes);
  }

  @Override
  public final Vector getClasses() {
    return classes;
  }

  @Override
  public Vector predict(DataFrame x) {
    Vector.Builder labels = new StringVector.Builder(x.rows());
    for (int i = 0; i < x.rows(); i++) {
      labels.set(i, predict(x.getRecord(i)));
    }
    return labels.build();
  }

  @Override
  public Value predict(Vector row) {
    return classes.getAsValue(argmax(predictProba(row)));
  }

  @Override
  public DoubleMatrix predictProba(DataFrame x) {
    DoubleMatrix proba = newDoubleMatrix(x.rows(), getClasses().size());
    for (int i = 0; i < x.rows(); i++) {
      proba.setRow(i, predictProba(x.getRecord(i)));
    }
    return proba;
  }
}
