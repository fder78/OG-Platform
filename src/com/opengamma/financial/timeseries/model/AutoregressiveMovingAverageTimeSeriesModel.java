/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.model;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class AutoregressiveMovingAverageTimeSeriesModel {
  private final AutoregressiveTimeSeriesModel _arModel;
  private final MovingAverageTimeSeriesModel _maModel;

  public AutoregressiveMovingAverageTimeSeriesModel(final ProbabilityDistribution<Double> random) {
    ArgumentChecker.notNull(random, "random");
    _maModel = new MovingAverageTimeSeriesModel(random);
    _arModel = new AutoregressiveTimeSeriesModel(random);
  }

  public DoubleTimeSeries<Long> getSeries(final double[] phi, final int p, final double[] theta, final int q, final long[] dates) {
    if (phi == null && p != 0)
      throw new IllegalArgumentException("AR coefficient array was null");
    if (p < 0)
      throw new IllegalArgumentException("p must be positive");
    if (phi != null && phi.length < p + 1)
      throw new IllegalArgumentException("AR coefficient array must contain at least " + (p + 1) + " elements");
    if (theta == null && q != 0)
      throw new IllegalArgumentException("MA coefficient array was null");
    if (q < 0)
      throw new IllegalArgumentException("q must be positive");
    if (theta != null && theta.length < q)
      throw new IllegalArgumentException("MA coefficient array must contain at least " + q + " elements");
    ArgumentChecker.notNull(dates, "dates");
    if (dates.length == 0)
      throw new IllegalArgumentException("Dates array was empty");
    final double[] theta1 = theta == null ? null : new double[theta.length + 1];
    if (theta != null) {
      theta1[0] = 0.;
      for (int i = 0; i < theta.length; i++) {
        theta1[i + 1] = theta[i];
      }
    }
    if (p == 0)
      return _maModel.getSeries(theta1, q, dates);
    if (q == 0)
      return _arModel.getSeries(phi, p, dates);
    return _arModel.getSeries(phi, p, dates).toFastLongDoubleTimeSeries().add(_maModel.getSeries(theta1, q, dates).toFastLongDoubleTimeSeries());
  }
}
