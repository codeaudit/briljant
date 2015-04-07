package org.briljantframework.classification;

import org.briljantframework.classification.tree.ClassSet;
import org.briljantframework.classification.tree.Example;
import org.briljantframework.classification.tree.RandomSplitter;
import org.briljantframework.classification.tree.Splitter;
import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.matrix.BitMatrix;
import org.briljantframework.vector.Vector;
import org.briljantframework.vector.Vectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * @author Isak Karlsson
 */
public class RandomForest extends Ensemble {

  private Splitter splitter;

  public RandomForest(Splitter splitter, int size) {
    super(size);
    this.splitter = splitter;
  }

  public static Builder withSize(int size) {
    return new Builder().withSize(size);
  }

  @Override
  public DefaultEnsemblePredictor fit(DataFrame x, Vector y) {
    Vector classes = Vectors.unique(y);
    ClassSet classSet = new ClassSet(y, classes);
    List<FitTask> fitTasks = new ArrayList<>();
    BitMatrix oobIndicator = BitMatrix.newMatrix(x.rows(), size());
    for (int i = 0; i < size(); i++) {
      fitTasks.add(new FitTask(classSet, x, y, splitter, classes, oobIndicator.getColumnView(i)));
    }
    try {
      return new DefaultEnsemblePredictor(classes, execute(fitTasks), oobIndicator);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public String toString() {
    return String.format("Random Classification Forest");
  }

  private static final class FitTask implements
                                     Callable<org.briljantframework.classification.Predictor> {

    private final ClassSet classSet;
    private final DataFrame x;
    private final Vector y;
    private final Splitter splitter;
    private final Vector classes;
    private final BitMatrix oobIndicator;

    private FitTask(ClassSet classSet, DataFrame x, Vector y, Splitter splitter, Vector classes,
                    BitMatrix oobIndicator) {
      this.classSet = classSet;
      this.x = x;
      this.y = y;
      this.splitter = splitter;
      this.classes = classes;
      this.oobIndicator = oobIndicator;
    }

    @Override
    public org.briljantframework.classification.Predictor call() throws Exception {
      Random random = new Random(Thread.currentThread().getId() * System.currentTimeMillis());
      ClassSet bootstrap = sample(classSet, random);
      return new DecisionTree(splitter, bootstrap, classes).fit(x, y);
    }

    public ClassSet sample(ClassSet classSet, Random random) {
      ClassSet inBag = new ClassSet(classSet.getDomain());
      int[] bootstrap = bootstrap(classSet, random);
      for (ClassSet.Sample sample : classSet.samples()) {
        ClassSet.Sample inSample = ClassSet.Sample.create(sample.getTarget());
        for (Example example : sample) {
          int id = example.getIndex();
          if (bootstrap[id] > 0) {
            inSample.add(example.updateWeight(bootstrap[id]));
          } else {
            oobIndicator.set(id, true);
          }
        }
        if (!inSample.isEmpty()) {
          inBag.add(inSample);
        }
      }
      return inBag;
    }

    private int[] bootstrap(ClassSet sample, Random random) {
      int[] bootstrap = new int[sample.size()];
      for (int i = 0; i < bootstrap.length; i++) {
        int idx = random.nextInt(bootstrap.length);
        bootstrap[idx]++;
      }

      return bootstrap;
    }
  }

  public static class Builder implements Classifier.Builder<RandomForest> {

    private RandomSplitter.Builder splitter = RandomSplitter.withMaximumFeatures(-1);
    private int size = 100;

    public Builder withSize(int size) {
      this.size = size;
      return this;
    }

    public Builder withMaximumFeatures(int size) {
      splitter.withMaximumFeatures(size);
      return this;
    }

    @Override
    public RandomForest build() {
      return new RandomForest(splitter.create(), size);
    }
  }
}
