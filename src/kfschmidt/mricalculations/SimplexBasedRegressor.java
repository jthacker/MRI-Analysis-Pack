/**
 *
 *   @author Karl Schmidt <karl.schmidt@umassmed.edu>
 *   This software is provided for use free of any costs,
 *   Be advised that NO guarantee is made regarding it's quality,
 *   and there is no ongoing support for this codebase.
 *
 *   (c) Karl Schmidt 2003
 *
 *   REVISION HISTORY:
 *
 *
 */
package kfschmidt.mricalculations;

import kfschmidt.simplex.SimplexOptimizer;

public class SimplexBasedRegressor {
	SimplexOptimizer mSimplex = new SimplexOptimizer();
	double[] mXPoints;
	double[] mYPoints;

	/**
	 * Perform a T2 relaxation fit S=So*exp(-TE/T2)) returns [t2, So, r^2]
	 * 
	 */
	public double[] regressT2(double[] te_points, double[] signal_points, double t2_guess) {

		// check if this is two value
		if (te_points.length == 2) {
			double[] ret = new double[3];
			ret[0] = (te_points[1] - te_points[0]) / Math.log((signal_points[0] / signal_points[1]));
			ret[1] = 0d;
			ret[2] = 0d;
			return ret;
		}

		// more than two values, use laborious fit
		try {
			mXPoints = te_points; // t3
			mYPoints = signal_points; // signal
			mSimplex.setObjectAndMethod(this, "calculateResidualT2", 2, 0d);
			mSimplex.setVariableParam(1, t2_guess); // t2
			if (signal_points[0] <= 0)
				signal_points[0] = 0.000000001d;
			mSimplex.setVariableParam(2, signal_points[0]); // So
			mSimplex.initialize();
			mSimplex.setMaxIterations(200);
			Object[] final_params = mSimplex.go();
			double residual = mSimplex.getBestResidual();
			double[] ret = new double[3]; // [t2,So,r^2]
			ret[0] = ((Double) final_params[0]).doubleValue(); // t2
			ret[1] = ((Double) final_params[1]).doubleValue(); // So
			ret[2] = residual;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Perform a b value relaxation fit S=So*exp(-b*D)) returns [D, So, r^2]
	 * 
	 */
	public double[] regressADC(double[] b_vals, double[] signal_points, double d_guess) {

		// check if this is two value
		if (b_vals.length == 2) {
			double[] ret = new double[3];
			ret[0] = Math.log((signal_points[0] / signal_points[1])) / (b_vals[1] - b_vals[0]);
			ret[1] = 0d;
			ret[2] = 0d;
			return ret;
		}

		try {
			mXPoints = b_vals; // t3
			mYPoints = signal_points; // signal
			mSimplex.setObjectAndMethod(this, "calculateResidualADC", 2, 0d);
			mSimplex.setVariableParam(1, d_guess); // t2
			if (signal_points[0] <= 0)
				signal_points[0] = 0.000000001d;
			mSimplex.setVariableParam(2, signal_points[0]); // So
			mSimplex.initialize();
			mSimplex.setMaxIterations(200);
			Object[] final_params = mSimplex.go();
			double residual = mSimplex.getBestResidual();
			double[] ret = new double[3]; // [D,So,r^2]
			ret[0] = ((Double) final_params[0]).doubleValue(); // D
			ret[1] = ((Double) final_params[1]).doubleValue(); // So
			ret[2] = residual;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Perform a T1 relaxation fit S=So(1-2*exp(-TR/T1)) returns [t1, So, r^2]
	 * 
	 */
	public double[] regressT1InversionRecovery(double[] tr_points, double[] signal_points, double t1_guess) {
		try {
			mXPoints = tr_points; // te
			mYPoints = signal_points; // signal
			mSimplex.setObjectAndMethod(this, "calculateResidualT1IR", 2, 0d);
			mSimplex.setVariableParam(1, t1_guess); // t1
			if (signal_points[0] == 0)
				signal_points[0] = 0.000000001d;
			mSimplex.setVariableParam(2, signal_points[0]); // So
			mSimplex.initialize();
			mSimplex.setMaxIterations(200);
			Object[] final_params = mSimplex.go();
			double residual = mSimplex.getBestResidual();
			double[] ret = new double[3]; // [t2,So,r^2]
			ret[0] = ((Double) final_params[0]).doubleValue(); // t2
			ret[1] = ((Double) final_params[1]).doubleValue(); // So
			ret[2] = residual;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Perform a T1 relaxation fit S=So(1-exp(-TR/T1)) returns [t2, So, r^2]
	 * 
	 */
	public double[] regressT1SaturationRecovery(double[] tr_points, double[] signal_points, double t1_guess) {
		try {
			mXPoints = tr_points; // t3
			mYPoints = signal_points; // signal
			mSimplex.setObjectAndMethod(this, "calculateResidualT1SR", 2, 0d);
			mSimplex.setVariableParam(1, t1_guess); // t1
			if (signal_points[0] <= 0)
				signal_points[0] = 0.000000001d;
			mSimplex.setVariableParam(2, signal_points[0]); // So
			mSimplex.initialize();
			mSimplex.setMaxIterations(200);
			Object[] final_params = mSimplex.go();
			double residual = mSimplex.getBestResidual();
			double[] ret = new double[3]; // [t2,So,r^2]
			ret[0] = ((Double) final_params[0]).doubleValue(); // t2
			ret[1] = ((Double) final_params[1]).doubleValue(); // So
			ret[2] = residual;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * returns the residual for a test t2 value
	 */
	public double calculateResidualT2(double t2, double so) {
		if (t2 < 0.00001 || t2 > 100)
			return Double.MAX_VALUE;
		double residual = 0d;
		int iter = 0;
		// X= te, Y=signal
		for (int a = 0; a < mXPoints.length; a++) {
			double testval = so * Math.exp((-1d * mXPoints[a] / t2));
			residual += Math.pow(mYPoints[a] - testval, 2d);
		}
		return residual;
	}

	/**
	 * returns the residual for a test adc value
	 */
	public double calculateResidualADC(double D, double so) {
		if (D < 0.0000001 || D > 100)
			return Double.MAX_VALUE;
		double residual = 0d;
		int iter = 0;
		// X= te, Y=signal
		for (int a = 0; a < mXPoints.length; a++) {
			double testval = so * Math.exp((-1d * mXPoints[a] * D));
			residual += Math.pow(mYPoints[a] - testval, 2d);
		}
		return residual;
	}

	/**
	 * returns the residual for a test t1 value in inversion recov
	 */
	public double calculateResidualT1SR(double t1, double so) {
		if (t1 < 0.001 || t1 > 1000)
			return Double.MAX_VALUE;
		double residual = 0d;
		int iter = 0;
		// X= tr, Y=signal
		for (int a = 0; a < mXPoints.length; a++) {
			double testval = so * (1d - Math.exp((-1d * mXPoints[a] / t1)));
			residual += Math.pow(mYPoints[a] - testval, 2d);
		}
		return residual;
	}

	/**
	 * returns the residual for a test t1 value in inversion recov
	 */
	public double calculateResidualT1IR(double t1, double so) {
		if (t1 < 0.001 || t1 > 1000)
			return Double.MAX_VALUE;
		double residual = 0d;
		int iter = 0;
		// X= tr, Y=signal
		for (int a = 0; a < mXPoints.length; a++) {
			double testval = Math.abs(so * (1d - 2d * Math.exp((-1d * mXPoints[a] / t1))));
			residual += Math.pow(mYPoints[a] - testval, 2d);
		}
		return residual;
	}

}
