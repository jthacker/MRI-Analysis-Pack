/**
 *   RegressionToVer2 performs non-linear regression
 *   of BOLD/BOLD(0) vs. CBF/CBF(0) to the second
 *   version of the adapted davis equation, where:
 *  
 *   Y = BOLD/BOLD(0)     X=CBF/CBF(0)   
 *   Z = CMRO2/CMRO2(0)   and M, C1, C2, alpha and beta are constants  
 * 
 *   ln(Y) = M [1 - Z^beta*X^(alpha-beta) 
 *               - Z^(beta-1)*X^(1+alpha-beta)*C1 + C2] 
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
package mrianalysispack.kfschmidt.simplex;

import mrianalysispack.kfschmidt.simplex.SimplexOptimizer;

public class SimplexTest {
	SimplexOptimizer mSimplex = new SimplexOptimizer();
	static double[] mXPoints = { 0.1d, 0.2d, 0.5d, 1.0d };
	static double[] mYPoints = { 380000d, 563300d, 775500d, 884466d };

	public static void main(String[] args) {
		SimplexTest t = new SimplexTest();
		try {

			System.out.println("residual for t1=0.617 So=613000: " + t.calculateResidualT1SR(0.617d, 613000d));
			double[] ret = t.regressT1SaturationRecovery(mXPoints, mYPoints, .75d);
			printResults(ret);
			while (ret[0] > .15d && ret[0] < .3) {
				ret = t.regressT1SaturationRecovery(mXPoints, mYPoints, .75d);
				printResults(ret);
			}

		} catch (Exception e) {
			e.printStackTrace();
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
			mSimplex.setVariableParam(2, signal_points[0]); // So
			mSimplex.initialize();
			mSimplex.setMaxIterations(2000);
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

	private static void printResults(double[] ret) {
		System.out.print("\n\n[");
		for (int a = 0; a < ret.length; a++) {
			if (a > 0)
				System.out.print(", ");
			System.out.print("" + ret[a]);
		}
		System.out.print("]\n");

	}

	/**
	 * returns the residual for a test t1 value in inversion recov
	 */
	public double calculateResidualT1SR(double t1, double so) {
		double residual = 0d;
		int iter = 0;
		// X= tr, Y=signal
		for (int a = 0; a < mXPoints.length; a++) {
			double testval = so * (1d - Math.exp((-1d * mXPoints[a] / t1)));
			residual += Math.pow(mYPoints[a] - testval, 2d);
		}
		return residual;
	}

}
