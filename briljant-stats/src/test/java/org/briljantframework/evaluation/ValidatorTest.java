package org.briljantframework.evaluation;

import java.util.Random;

import org.briljantframework.classification.Classifier;
import org.briljantframework.classification.ClassifierValidator;
import org.briljantframework.classification.LogisticRegression;
import org.briljantframework.classification.NearestNeighbours;
import org.briljantframework.classifier.evaluation.ProbabilityEvaluator;
import org.briljantframework.classifier.evaluation.ZeroOneLossEvaluator;
import org.briljantframework.data.Is;
import org.briljantframework.data.dataframe.DataFrame;
import org.briljantframework.data.dataframe.DataFrames;
import org.briljantframework.data.vector.Vector;
import org.briljantframework.dataset.io.Datasets;
import org.briljantframework.evaluation.partition.FoldPartitioner;
import org.briljantframework.evaluation.partition.Partitioner;
import org.junit.Test;

/**
 * @author Isak Karlsson <isak-kar@dsv.su.se>
 */
public class ValidatorTest {


  @Test
  public void testCreateValidator() throws Exception {
    Partitioner partitioner = new FoldPartitioner(10);
    Validator<Classifier> validator = new Validator<Classifier>(partitioner) {
      @Override
      protected void predict(MutableEvaluationContext<? extends Classifier> ctx) {
        DataFrame validationData = ctx.getPartition().getValidationData();
        Classifier classifier = ctx.getPredictor();
        ctx.setEstimates(classifier.estimate(validationData));
        ctx.setPredictions(classifier.predict(validationData));
      }
    };
    validator.add(ZeroOneLossEvaluator.getInstance());
    validator.add(ProbabilityEvaluator.getInstance());

    // This validator is also implemented here:
    Validator<Classifier> cvd = new ClassifierValidator<>(partitioner);

    LogisticRegression.Learner learner = new LogisticRegression.Learner();
    DataFrame iris = DataFrames.permuteRecords(Datasets.loadIris(), new Random(10));

    // Take all columns except 'Class', and replace any NA values with the mean of that column
    // as the training data
    DataFrame x = iris.drop("Class").apply(v -> v.set(v.where(Object.class, Is::NA), v.mean()));
    Vector y = iris.get("Class");

    // Test the classifier learner (in this class a logistic regression model)
    Result<Classifier> result = validator.test(learner, x, y);

    // Get a data frame of measures (the ones computed by ZeroOneLossEvaluator and
    // ProbabilityEvaluator)
    DataFrame measures = result.getMeasures();
    System.out.println(measures);

    NearestNeighbours.Learner knn = new NearestNeighbours.Learner(3);
    Result<Classifier> knnResult = validator.test(knn, x, y);
    System.out.println(knnResult.getMeasures().mean());

    System.out.println(measures.mean().sub(knnResult.getMeasures().mean()).abs());
  }
}