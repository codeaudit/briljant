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
package org.briljantframework.evaluation;


import org.briljantframework.classification.Classifier;
import org.briljantframework.dataframe.DataFrame;
import org.briljantframework.evaluation.result.Result;
import org.briljantframework.vector.Vector;

/**
 * An Evaluator is used to evaluate an algorithm on a particular dataset
 * <p>
 * Created by Isak Karlsson on 20/08/14.
 */
public interface ClassificationValidator {

  /**
   * Evaluate {@code classifier} using the data {@code x} and {@code y}
   *
   * @param classifier classifier to use for classification
   * @param x the data frame to use during evaluation
   */
  Result test(Classifier classifier, DataFrame x, Vector y);

}