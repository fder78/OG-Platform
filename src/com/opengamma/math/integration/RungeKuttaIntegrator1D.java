/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 * 
 */
public class RungeKuttaIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {

  private static final double DEF_TOL = 1e-7;
  private static final int DEF_MIN_STEPS = 10;
  private final double _absTol, _relTol;
  private final int _minSteps;

  /**
   * 
   */
  public RungeKuttaIntegrator1D(final double atol, final double rtol, final int minSteps) {

    if (atol < 0.0)
      throw new IllegalArgumentException("Absolute Tolerance must be greater than zero");
    if (rtol < 0.0)
      throw new IllegalArgumentException("Relative Tolerance must be grater than zero");
    if (minSteps < 1)
      throw new IllegalArgumentException("Must have minimum of 1 step");

    _absTol = atol;
    _relTol = rtol;
    _minSteps = minSteps;
  }

  public RungeKuttaIntegrator1D(final double tol, final int minSteps) {
    this(tol, tol, minSteps);
  }

  public RungeKuttaIntegrator1D(final double tol) {
    this(tol, tol, DEF_MIN_STEPS);
  }

  public RungeKuttaIntegrator1D(final int minSteps) {
    this(DEF_TOL, minSteps);
  }

  public RungeKuttaIntegrator1D() {
    this(DEF_TOL, DEF_MIN_STEPS);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.integration.Integrator1D#integrate(java.lang.Object,
   * java.lang.Object, java.lang.Object)
   */
  @Override
  public Double integrate(final Function1D<Double, Double> f, final Double lower, final Double upper) {

    final double h = (upper - lower) / _minSteps;
    double f1, f2, f3, x;
    x = lower;
    f1 = f.evaluate(x);

    double result = 0.0;
    for (int i = 0; i < _minSteps; i++) {
      f2 = f.evaluate(x + h / 2.0);
      f3 = f.evaluate(x + h);

      result += calculateRungeKuttaFourthOrder(f, x, h, f1, f2, f3);
      f1 = f3;
      x += h;
    }
    return result;
  }

  private double calculateRungeKuttaFourthOrder(final Function1D<Double, Double> f, final double x, final double h, final double fl, final double fm, final double fu) {

    final double f1 = f.evaluate(x + 0.25 * h);
    final double f2 = f.evaluate(x + 0.75 * h);
    final double ya = h * (fl + 4.0 * fm + fu) / 6.0;
    final double yb = h * (fl + 2.0 * fm + 4.0 * (f1 + f2) + fu) / 12.0;

    final double diff = Math.abs(ya - yb);
    final double abs = Math.max(Math.abs(ya), Math.abs(yb));

    if (diff < _absTol + _relTol * abs) {
      return yb + (yb - ya) / 15.0;
    }

    // can't keep halving the step size
    if (h < _absTol) {
      return yb + (yb - ya) / 15.0;
    }

    return calculateRungeKuttaFourthOrder(f, x, h / 2.0, fl, f1, fm) + calculateRungeKuttaFourthOrder(f, x + h / 2.0, h / 2.0, fm, f2, fu);
  }

}
