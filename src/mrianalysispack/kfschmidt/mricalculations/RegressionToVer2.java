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
package mrianalysispack.kfschmidt.mricalculations;

import mrianalysispack.kfschmidt.simplex.SimplexOptimizer;

public class RegressionToVer2 {
	SimplexOptimizer mSimplex = new SimplexOptimizer();
	double[] mXPoints;
	double[] mYPoints;

	/**
	 * Y = BOLD/BOLD(0) X=CBF/CBF(0) Z = CMRO2/CMRO2(0) and M, C1, C2, alpha and
	 * beta are constants
	 * 
	 * ln(Y) = M [1 - Z^beta*X^(alpha-beta) -
	 * Z^(beta-1)*X^(1+alpha-beta)*(C2-0.085) + C2] assuming that Z = 1 C1 =
	 * C2-0.085 due to decreased arterial [dhb] under hypercapnia, when initial
	 * conditions are o2sat=95% under anesthesia returns array of doubles
	 * containing [m,c1,c2,r^2]
	 */
	public double[] regressHypercapnicFixedCRelationship(double[] x_points, double[] y_points, double alpha, double beta, double startm, double startc2) {
		try {
			mXPoints = x_points;
			mYPoints = y_points;
			mSimplex.setObjectAndMethod(this, "calculateResidualFixedCRelationship", 5, 0d);
			mSimplex.setVariableParam(1, startm); // m
			mSimplex.setFixedParamAsDouble(2, alpha); // alpha
			mSimplex.setFixedParamAsDouble(3, beta); // beta
			mSimplex.setVariableParam(4, startc2); // c2
			mSimplex.setFixedParamAsDouble(5, 1d); // z
			mSimplex.initialize();
			mSimplex.setMaxIterations(200);
			Object[] final_params = mSimplex.go();
			double residual = mSimplex.getBestResidual();
			double[] ret = new double[4]; // [m,c1,c2,r^2]
			ret[0] = ((Double) final_params[0]).doubleValue(); // m
			ret[2] = ((Double) final_params[3]).doubleValue(); // c2
			ret[1] = ret[2] - 0.085d; // c1
			ret[3] = residual;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Y = BOLD/BOLD(0) X=CBF/CBF(0) Z = CMRO2/CMRO2(0) and M, C1, C2, alpha and
	 * beta are constants
	 * 
	 * ln(Y) = M [1 - Z^beta*X^(alpha-beta) -
	 * Z^(beta-1)*X^(1+alpha-beta)*(C2-0.085) + C2] assuming that Z = 1 C1 =
	 * C2-0.085 due to decreased arterial [dhb] under hypercapnia, when initial
	 * conditions are o2sat=95% under anesthesia returns array of doubles
	 * containing [m,c1,c2,r^2]
	 */
	public double[] regressHypercapnicVariableAlpha(double[] x_points, double[] y_points, double start_alpha, double beta) {
		try {
			mXPoints = x_points;
			mYPoints = y_points;
			mSimplex.setObjectAndMethod(this, "calculateResidual", 6, 0d);
			mSimplex.setVariableParam(1, 0.1d); // m
			mSimplex.setFixedParamAsDouble(2, start_alpha); // alpha
			mSimplex.setFixedParamAsDouble(3, beta); // beta
			mSimplex.setFixedParamAsDouble(4, 0d); // c1
			mSimplex.setFixedParamAsDouble(5, 0d); // c2
			mSimplex.setFixedParamAsDouble(6, 1d); // z
			mSimplex.initialize();
			mSimplex.setMaxIterations(200);
			Object[] final_params = mSimplex.go();
			double residual = mSimplex.getBestResidual();
			double[] ret = new double[3]; // [m,alpha,r^2]
			ret[0] = ((Double) final_params[0]).doubleValue(); // m
			ret[1] = ((Double) final_params[2]).doubleValue(); // alpha
			ret[2] = residual;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Y = BOLD/BOLD(0) X=CBF/CBF(0) Z = CMRO2/CMRO2(0) and M, C1, C2, alpha and
	 * beta are constants
	 * 
	 * ln(Y) = M [1 - Z^beta*X^(alpha-beta) - Z^(beta-1)*X^(1+alpha-beta)*C1 +
	 * C2] assuming that Z = 1
	 * 
	 * returns array of doubles containing [z,r^2]
	 */
	public double[] regressForZ(double[] x_points, double[] y_points, double alpha, double beta, double m, double c1, double c2) {
		try {
			mXPoints = x_points;
			mYPoints = y_points;
			mSimplex.setObjectAndMethod(this, "calculateResidual", 6, 0d);
			mSimplex.setFixedParamAsDouble(1, m); // m
			mSimplex.setFixedParamAsDouble(2, alpha); // alpha
			mSimplex.setFixedParamAsDouble(3, beta); // beta
			mSimplex.setFixedParamAsDouble(4, c1); // c1
			mSimplex.setFixedParamAsDouble(5, c2); // c2
			mSimplex.setVariableParam(6, 1d); // z
			mSimplex.initialize();
			mSimplex.setMaxIterations(200);
			Object[] final_params = mSimplex.go();
			double residual = mSimplex.getBestResidual();
			double[] ret = new double[2]; // [z,r^2]
			ret[0] = ((Double) final_params[5]).doubleValue(); // z
			ret[1] = residual;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * called by the Simplex optimizer to calculate the residual for a trial set
	 * of m, c1, c2, or z depending on setup
	 */
	public double calculateResidual(double m, double alpha, double beta, double c1, double c2, double z) {
		double residual = 0d;
		int iter = 0;

		if (c1 < -0.1 || c2 < -0.1 || z < 0 || m < 0 || alpha > 1.5 || alpha < 0) {
			return 1000000000000d;
		}

		for (int a = 0; a < mXPoints.length; a++) {
			// negative BOLD/BOLD(0) and CBF/CBF(0) values are noise, and will
			// break calc
			if (mXPoints[a] > 0 && mYPoints[a] > 0) {
				iter++;
				double testval = m
						* (1d - Math.pow(z, beta) * Math.pow(mXPoints[a], (alpha - beta)) - Math.pow(z, (beta - 1d))
								* Math.pow(mXPoints[a], (1d + alpha - beta)) * c1 + c2);

				residual += Math.pow(Math.log(mYPoints[a]) - testval, 2d);
			}
		}

		// no usable values were obtained, return a zero residual to stop calc
		if ((double) iter < (double) mXPoints.length * .9) {
			return 0d;
		}
		return residual;
	}

	/**
	 * called by the Simplex optimizer to calculate the residual for a trial set
	 * of m, c1, c2, or z depending on setup
	 */
	public double calculateResidualFixedCRelationship(double m, double alpha, double beta, double c2, double z) {
		double residual = 0d;
		int iter = 0;

		if (c2 < 0 || c2 > 5 || z < 0 || m < 0 || alpha > 1.5 || alpha < 0) {
			return 1000000000000d;
		}

		for (int a = 0; a < mXPoints.length; a++) {
			// negative BOLD/BOLD(0) and CBF/CBF(0) values are noise, and will
			// break calc
			if (mXPoints[a] > 0 && mYPoints[a] > 0) {
				iter++;
				double c1 = (c2 - 0.085);
				double testval = m
						* (1d - Math.pow(z, beta) * Math.pow(mXPoints[a], (alpha - beta)) - Math.pow(z, (beta - 1d))
								* Math.pow(mXPoints[a], (1d + alpha - beta)) * c1 + c2);

				residual += Math.pow(Math.log(mYPoints[a]) - testval, 2d);
			}
		}

		// no usable values were obtained, return a zero residual to stop calc
		if ((double) iter < (double) mXPoints.length * .9) {
			return 0d;
		}
		return residual;
	}

}
