package org.briljantframework.classification;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.briljantframework.array.DoubleArray;
import org.briljantframework.classifier.evaluation.ProbabilityEvaluator;
import org.briljantframework.classifier.evaluation.ZeroOneLossEvaluator;
import org.briljantframework.data.dataframe.DataFrame;
import org.briljantframework.data.vector.Vector;
import org.briljantframework.evaluation.Evaluator;
import org.briljantframework.evaluation.MutableEvaluationContext;
import org.briljantframework.evaluation.Validator;
import org.briljantframework.evaluation.partition.FoldPartitioner;
import org.briljantframework.evaluation.partition.LeaveOneOutPartitioner;
import org.briljantframework.evaluation.partition.Partition;
import org.briljantframework.evaluation.partition.Partitioner;
import org.briljantframework.evaluation.partition.SplitPartitioner;

/**
 * @author Isak Karlsson <isak-kar@dsv.su.se>
 */
public class ClassifierValidator<T extends Classifier> extends Validator<T> {

  /**
   * The leave one out partitioner
   */
  public static final LeaveOneOutPartitioner LOO_PARTITIONER = new LeaveOneOutPartitioner();

  /**
   * The default evaluators for classifiers
   */
  private static final Set<? extends Evaluator<? super Classifier>> EVALUATORS = new HashSet<>(
      Arrays.asList(ZeroOneLossEvaluator.getInstance(), ProbabilityEvaluator.getInstance()));

  public ClassifierValidator(Set<? extends Evaluator<? super T>> evaluators, Partitioner partitioner) {
    super(evaluators, partitioner);
  }

  public ClassifierValidator(Partitioner partitioner) {
    super(partitioner);
  }

  @Override
  protected void predict(MutableEvaluationContext<? extends T> ctx) {
    T p = ctx.getPredictor();
    Partition partition = ctx.getEvaluationContext().getPartition();
    DataFrame x = partition.getValidationData();
    Vector y = partition.getValidationTarget();
    Vector.Builder builder = y.newBuilder();

    // For the case where the classifier reports the ESTIMATOR characteristic
    // improve the performance by avoiding to recompute the classifications twice.
    if (p.getCharacteristics().contains(ClassifierCharacteristic.ESTIMATOR)) {
      Vector classes = p.getClasses();
      DoubleArray estimate = p.estimate(x);
      ctx.setEstimates(estimate);
      for (int i = 0; i < estimate.rows(); i++) {
        builder.loc()
            .set(i, classes, org.briljantframework.array.Arrays.argmax(estimate.getRow(i)));
      }
      ctx.setPredictions(builder.build());
    } else {
      ctx.setPredictions(p.predict(x));
    }
  }

  public static <T extends Classifier> Validator<T> holdout(DataFrame testX, Vector testY) {
    return createValidator((x, y) -> Collections.singleton(new Partition(x, testX, y, testY)));
  }

  public static <T extends Classifier> Validator<T> splitValidation(double testFraction) {
    return createValidator(new SplitPartitioner(testFraction));
  }

  public static <T extends Classifier> Validator<T> leaveOneOutValidation() {
    return createValidator(LOO_PARTITIONER);
  }

  public static <T extends Classifier> Validator<T> crossValidation(int folds) {
    return createValidator(new FoldPartitioner(folds));
  }

  private static <T extends Classifier> Validator<T> createValidator(Partitioner partitioner) {
    return new ClassifierValidator<T>(EVALUATORS, partitioner);
  }
}