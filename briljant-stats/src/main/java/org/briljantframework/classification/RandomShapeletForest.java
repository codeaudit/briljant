/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Isak Karlsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.briljantframework.classification;

import org.briljantframework.Bj;
import org.briljantframework.array.BooleanArray;
import org.briljantframework.array.DoubleArray;
import org.briljantframework.classification.tree.ClassSet;
import org.briljantframework.classification.tree.Example;
import org.briljantframework.data.dataframe.DataFrame;
import org.briljantframework.data.vector.Vector;
import org.briljantframework.data.vector.Vectors;
import org.briljantframework.distance.Distance;
import org.briljantframework.evaluation.measure.AbstractMeasure;
import org.briljantframework.evaluation.result.EvaluationContext;
import org.briljantframework.evaluation.result.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * <h1>Publications</h1>
 * <ul>
 * <li>Karlsson, I., Bostrom, H., Papapetrou, P. Forests of Randomized Shapelet Trees In Proc. the
 * 3rd International Symposium on Learning and Data Sciences (SLDS), 2015</li>
 * </ul>
 *
 * @author Isak Karlsson
 */
public class RandomShapeletForest extends Ensemble {

  private final ShapeletTree.Builder builder;

  private RandomShapeletForest(ShapeletTree.Builder builder, int size) {
    super(size);
    this.builder = builder;
  }

  public static Builder withSize(int size) {
    return new Builder().withSize(size);
  }

  @Override
  public Predictor fit(DataFrame x, Vector y) {
    Vector classes = Vectors.unique(y);
    ClassSet classSet = new ClassSet(y, classes);
    List<FitTask> tasks = new ArrayList<>();
    BooleanArray oobIndicator = Bj.booleanArray(x.rows(), size());
    for (int i = 0; i < size(); i++) {
      tasks.add(new FitTask(classSet, x, y, builder, classes, oobIndicator.getColumn(i)));
    }

    try {
      List<ShapeletTree.Predictor> models = Ensemble.execute(tasks);
      DoubleArray lenSum = Bj.doubleArray(x.columns());
      DoubleArray posSum = Bj.doubleArray(x.columns());
      for (ShapeletTree.Predictor m : models) {
        lenSum.addi(m.getLengthImportance());
        posSum.addi(m.getPositionImportance());
      }

      lenSum.update(v -> v / size());
      posSum.update(v -> v / size());

      Map<Object, Integer> counts = Vectors.count(y);
      DoubleArray apriori = Bj.doubleArray(classes.size());
      for (int i = 0; i < classes.size(); i++) {
        apriori.set(i, counts.get(classes.loc().get(Object.class, i)) / (double) y.size());
      }

      return new Predictor(classes, apriori, models, lenSum, posSum, oobIndicator);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

  }

  @Override
  public String toString() {
    return "Ensemble of Randomized Shapelet Trees";
  }

  private static final class FitTask implements Callable<ShapeletTree.Predictor> {

    private final ClassSet classSet;
    private final DataFrame x;
    private final Vector y;
    private final Vector classes;
    private final ShapeletTree.Builder builder;
    private final BooleanArray oobIndicator;


    private FitTask(ClassSet classSet, DataFrame x, Vector y, ShapeletTree.Builder builder,
                    Vector classes, BooleanArray oobIndicator) {
      this.classSet = classSet;
      this.x = x;
      this.y = y;
      this.classes = classes;
      this.builder = builder;
      this.oobIndicator = oobIndicator;
    }

    @Override
    public ShapeletTree.Predictor call() throws Exception {
      Random random = new Random(Thread.currentThread().getId() * System.nanoTime());
      ClassSet sample = sample(classSet, random);
      double low = builder.lowerLength;
      double high = builder.upperLength;
      return new ShapeletTree(low, high, builder, sample, classes).fit(x, y);
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

  public static class Predictor extends DefaultEnsemblePredictor {

    private final DoubleArray lengthImportance;
    private final DoubleArray positionImportance;
    private final DoubleArray apriori;

    public Predictor(
        Vector classes, DoubleArray apriori,
        List<? extends org.briljantframework.classification.Predictor> members,
        DoubleArray lengthImportance, DoubleArray positionImportance, BooleanArray oobIndicator) {
      super(classes, members, oobIndicator);
      this.lengthImportance = lengthImportance;
      this.positionImportance = positionImportance;
      this.apriori = apriori;
    }

    public DoubleArray getLengthImportance() {
      return lengthImportance;
    }

    public DoubleArray getPositionImportance() {
      return positionImportance;
    }

    @Override
    public void evaluate(EvaluationContext ctx) {
      super.evaluate(ctx);
      ctx.getOrDefault(Depth.class, Depth.Builder::new).add(Sample.OUT, getDepth());
    }

    public double getDepth() {
      double depth = 0;
      for (org.briljantframework.classification.Predictor predictor : getPredictors()) {
        if (predictor instanceof ShapeletTree.Predictor) {
          int d = ((ShapeletTree.Predictor) predictor).getDepth();
          depth += d;
        }
      }
      return depth / getPredictors().size();
    }

//    @Override
//    public DoubleArray estimate(Vector record) {
//      List<DoubleArray> predictions = getPredictors().parallelStream()
//          .map(model -> model.estimate(record))
//          .collect(Collectors.toList());
//
//      int estimators = getPredictors().size();
//      Vector classes = getClasses();
//      DoubleArray m = Bj.doubleArray(classes.size());
//      for (DoubleArray prediction : predictions) {
//        m.assign(prediction, (t, o) -> t + o / estimators);
//      }
////      return m.mul(apriori.rsub(1));
//      return m;
//    }
  }

  public static class Depth extends AbstractMeasure {

    protected Depth(Builder builder) {
      super(builder);
    }

    @Override
    public String getName() {
      return "Depth";
    }

    public static class Builder extends AbstractMeasure.Builder<Depth> {

      @Override
      public Depth build() {
        return new Depth(this);
      }
    }
  }

  public static class Builder implements Classifier.Builder<RandomShapeletForest> {

    private final ShapeletTree.Builder shapeletTree = new ShapeletTree.Builder();
    private int size = 100;

    public Builder withMinSplitSize(double minSplitSize) {
      shapeletTree.withMinSplit(minSplitSize);
      return this;
    }

    public Builder withLowerLength(double lower) {
      shapeletTree.withLowerLength(lower);
      return this;
    }

    public Builder withDistanceMeasure(Distance distance) {
      shapeletTree.withDistance(distance);
      return this;
    }

    public Builder withInspectedShapelets(int maxShapelets) {
      shapeletTree.withInspectedShapelets(maxShapelets);
      return this;
    }

    public Builder withDistance(Distance distance) {
      shapeletTree.withDistance(distance);
      return this;
    }

    public Builder withUpperLength(double upper) {
      shapeletTree.withUpperLength(upper);
      return this;
    }

    public Builder withSize(int size) {
      this.size = size;
      return this;
    }

    public Builder withSampleMode(ShapeletTree.SampleMode sampleMode) {
      shapeletTree.withMode(sampleMode);
      return this;
    }

    public Builder withAssessment(ShapeletTree.Assessment assessment) {
      shapeletTree.withAssessment(assessment);
      return this;
    }

    @Override
    public RandomShapeletForest build() {
      return new RandomShapeletForest(shapeletTree, size);
    }

    public Builder withAggregateFraction(double v) {
      shapeletTree.withAggregateFraction(v);
      return this;
    }
  }
}
