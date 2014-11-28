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

package org.briljantframework.learning.evaluation.result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.briljantframework.learning.Prediction;
import org.briljantframework.learning.Predictions;
import org.briljantframework.vector.Vector;

/**
 * Created by Isak Karlsson on 06/10/14.
 */
public class AreaUnderCurve extends AbstracPerValueMetric {

  /**
   * Instantiates a new Area under curve.
   *
   * @param producer the producer
   */
  public AreaUnderCurve(AbstracPerValueMetric.Producer producer) {
    super(producer);
  }

  /**
   * The constant FACTORY.
   */
  public static Factory getFactory() {
    return Producer::new;
  }

  @Override
  public String getName() {
    return "Area Under ROC Curve";
  }

  // TODO(isak): warn user if a numeric target is used
  private static final class Producer extends AbstracPerValueMetric.Producer {

    @Override
    protected double calculateMetricForValue(String value, Predictions predictions, Vector column) {
      return calculateAreaUnderCurve(predictions, column, value);
    }

    private double calculateAreaUnderCurve(Predictions predictions, Vector targets, String value) {
      List<PredictionProbability> pairs = new ArrayList<>(predictions.size());
      double truePositives = 0, falsePositives = 0, positives = 0;
      for (int i = 0; i < targets.size(); i++) {
        Prediction p = predictions.get(i);

        boolean positive = targets.getAsString(i).equals(value);
        if (positive) {
          positives++;
        }
        pairs.add(new PredictionProbability(positive, p.getProbability(value)));
      }
      Collections.sort(pairs, (a, b) -> Double.compare(b.probability, a.probability));

      double negatives = predictions.size() - positives, previousProbability = -1, auc = 0.0, previousTruePositive =
          0.0, previousFalsePositive = 0.0;
      for (PredictionProbability pair : pairs) {
        double probability = pair.probability;
        if (probability != previousProbability) {
          auc +=
              Math.abs(falsePositives - previousFalsePositive)
                  * (truePositives + previousTruePositive) / 2;

          previousFalsePositive = falsePositives;
          previousTruePositive = truePositives;
          previousProbability = probability;
        }

        if (pair.positive) {
          truePositives++;
        } else {
          falsePositives++;
        }
      }
      if (positives * negatives == 0) {
        return 0;
      } else {
        return (auc + Math.abs(negatives - previousFalsePositive)
            * (positives + previousTruePositive) / 2)
            / (positives * negatives);
      }
    }

    @Override
    public Metric produce() {
      return new AreaUnderCurve(this);
    }

    private static final class PredictionProbability {
      /**
       * True if the AucPair is positive
       */
      public final boolean positive;

      /**
       * The Probability.
       */
      public final double probability;

      private PredictionProbability(boolean positive, double probability) {
        this.positive = positive;
        this.probability = probability;
      }

      @Override
      public String toString() {
        return positive + " " + probability;
      }
    }
  }
}
