package mrianalysispack.kfschmidt.glmanalyzer;

/**
 *   User Interface for the GLM Analyzer
 *
 *
 *   @author Karl Schmidt <karl.schmidt@umassmed.edu>
 *   This software is provided for use free of any costs,
 *   Be advised that NO guarantee is made regarding it's quality,
 *   and there is no ongoing support for this codebase.
 *
 *   (c) Karl Schmidt 2004
 *
 *   REVISION HISTORY:
 *
 *
 */
import mrianalysispack.kfschmidt.math.LinearAlgebra;

public class GLMMath implements Runnable {

	GLMModel mModel;
	GLMMGR mMgr;
	double[][][][] mData;
	double[][][] mInvMtM; // this is storage for precalculated Inv(MtM) matrices
							// the format is mInvMtMMt[phase shifts _zero in
							// middle][row][col]
	double[][][] mMs;

	private void copyMat(double[][] sourcemat, double[][] destmat) {
		for (int r = 0; r < sourcemat.length; r++) {
			for (int c = 0; c < sourcemat[0].length; c++) {
				destmat[r][c] = sourcemat[r][c];
			}
		}
	}

	public void run() {
		// called by the Thread infrastructure.
		// if we have a model and data,
		// then we start the calculation ,
		// and call the manager object periodically
		// to update calculation progress
		// upon termination, we send the results to the
		// manager, and terminate
		int slices = mData[0].length;
		int height = mData[0][0][0].length;
		int width = mData[0][0].length;
		FittedDataSet fitted = new FittedDataSet(slices, width, height);
		int progress = 0;

		// precalculate the InvMtMMt matrices
		double[][] M = mModel.getModelMatrix();
		int phase_samples = (int) ((float) M.length * 0.1) * 2 + 1; // sample
																	// +/- 10%
																	// of
																	// timecourse
																	// in phase
		mInvMtM = new double[phase_samples][M[0].length][M[0].length];
		mMs = new double[phase_samples][M.length][M[0].length];

		double[][] Mt = null;
		double[][] MtM = null;
		double[][] invMtM = null;
		int phase_shift = -1 * (mInvMtM.length - 1) / 2;

		for (int a = 0; a < mInvMtM.length; a++) {
			try {
				M = mModel.getModelMatrixWithPhaseShift(phase_shift + a);
				mMs[a] = mModel.getModelMatrixWithPhaseShift(phase_shift + a);
				Mt = LinearAlgebra.calcTranspose(M);
				MtM = LinearAlgebra.multiplyMatrices(Mt, M);
				invMtM = LinearAlgebra.calcInverse(MtM);
				copyMat(invMtM, mInvMtM[a]);
			} catch (Exception e) {
				e.printStackTrace();
			} // shouldn't happen

		}

		for (int slice = 0; slice < mData[0].length; slice++) {
			for (int y = 0; y < mData[0][0][0].length; y++) {
				for (int x = 0; x < mData[0][0].length; x++) {
					fitPixel(fitted, slice, x, y);

					progress = slice * height * width + width * y + x;
					if (progress % 200 == 0)
						mMgr.updateFitProgress(mModel, (int) (100f * (float) progress / (float) (slices * width * height)));
				}
			}
		}
		mMgr.finishedFit(fitted);
	}

	private void fitPixel(FittedDataSet fitted, int slice, int x, int y) {
		try {
			// collect the data series into a column vector
			double[][] Y = new double[mData.length][1];
			for (int a = 0; a < Y.length; a++) {
				Y[a][0] = mData[a][slice][x][y];
			}

			double[][] B = null;
			double[][] X = null;
			double meanXY = 0d;
			double sumX = 0d;
			double sumY = 0d;
			double sumX2 = 0d;
			double sumY2 = 0d;
			double var_x = 0d;
			double var_y = 0d;
			double covar_xy = 0d;
			double t_score = 0d;
			double[] corr = new double[mInvMtM.length];
			double[] pval = new double[mInvMtM.length];
			double[] pct = new double[mInvMtM.length];
			int best_phase = 0;
			double best_corr2 = 0d;
			double[][] tmpmat = null;

			for (int s = 0; s < mInvMtM.length; s++) {
				// TEST this phase shift

				// get B = Inv(M'M)M'Y and comparison data set
				tmpmat = LinearAlgebra.multiplyMatrices(mInvMtM[s], LinearAlgebra.calcTranspose(mMs[s]));
				B = LinearAlgebra.multiplyMatrices(tmpmat, Y);
				X = LinearAlgebra.multiplyMatrices(mMs[s], B);

				meanXY = 0d;
				sumX = 0d;
				sumY = 0d;
				sumX2 = 0d;
				sumY2 = 0d;
				var_x = 0d;
				var_y = 0d;
				covar_xy = 0d;

				for (int a = 0; a < X.length; a++) {
					meanXY += X[a][0] * Y[a][0];
					sumX += X[a][0];
					sumY += Y[a][0];
					sumX2 += X[a][0] * X[a][0];
					sumY2 += Y[a][0] * Y[a][0];
				}

				meanXY /= X.length;
				var_x = (sumX2 - sumX * sumX / (double) X.length) / (double) (X.length - 1);
				var_y = (sumY2 - sumY * sumY / (double) X.length) / (double) (X.length - 1);
				covar_xy = meanXY - (1d / (double) X.length) * (1d / (double) X.length) * sumX * sumY;

				// correlation = cov(x,y) / stdev(x)stdev(y)
				corr[s] = covar_xy / (Math.sqrt(var_x) * Math.sqrt(var_y));

				// check if this iteration is superior
				if (corr[s] * corr[s] > best_corr2) {
					best_corr2 = corr[s] * corr[s];
					best_phase = s;
				}

				// pval is calculated for null hypoth. using t dist
				// t = r*sqrt(n-2)/sqrt(1-r^2)
				// pval = two tailed integral of t-dist from t out to inf.
				t_score = corr[s] * Math.sqrt(X.length - 2) / Math.sqrt(1 - corr[s] * corr[s]);
				pval[s] = getPVal(t_score, X.length - 2); // n - 2 for df

				pct[s] = B[0][0] / B[1][0];
			}

			fitted.getCCMap()[slice][x][y] = corr[best_phase];
			fitted.getPvalMap()[slice][x][y] = pval[best_phase];
			fitted.getPctMap()[slice][x][y] = pct[best_phase];
			fitted.getPhaseMap()[slice][x][y] = (double) (best_phase - 1 - (mInvMtM.length - 1) / 2);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static double getPVal(double t_score, int df) throws Exception {
		// P val of null hypothesis =
		// A(t|f) = I[f/(f+t^2)](f/2, 1/2) where I is the incomplete
		// Beta function I[x](a,b)
		// NUMERICAL RECIPES IN C: THE ART OF SCIENTIFIC COMPUTING Cambridge
		// UIniversity Press
		// p. 228, 1988-1992
		double x = df / (df + t_score * t_score);
		double a = df / 2d;
		double b = 0.5d;
		return betaIncomplete(a, b, x);
	}

	public GLMMath(GLMMGR manager, double[][][][] data, GLMModel model) {
		mMgr = manager;
		mModel = model;
		mData = data;
	}

	public static void fitModelToData(GLMMGR observer, double[][][][] data, GLMModel model) {
		GLMMath mathobj = new GLMMath(observer, data, model);
		Thread t = new Thread(mathobj);
		t.start();
	}

	/**
	 * returns the value of the incomplete beta function evaluated at a,b,x From
	 * Numerical Recipes in C: the art of scientific computation ISBN
	 * 0-521-43108-5 pp.227-230
	 * 
	 */
	private static double betaIncomplete(double a, double b, double x) throws Exception {
		double bt = 0d;

		if (x < 0 || x > 1)
			throw new Exception("Bad X in betaIncomplete: " + x);
		if (x == 0d || x == 1d) {
			bt = 0d;
		} else {
			bt = Math.exp(lnGamma(a + b) - lnGamma(a) - lnGamma(b) + a * Math.log(x) + b * Math.log(1d - x));
		}
		if (x < (a + 1d) / (a + b + 2d))
			return bt * betaContinuedFraction(a, b, x) / a;
		else
			return 1d - bt * betaContinuedFraction(b, a, 1d - x) / b;
	}

	private static double FPMIN = 0.0000000000000000000000000000001; // 1E-30
	private static double EPS = 10000000; // 1E7
	private static int MAXIT = 100;

	/**
	 * ln(gamma(x)) as described in Numerical Recipes in C
	 * 
	 */
	private static double lnGamma(double xx) {
		double x = 0d;
		double y = 0d;
		double tmp = 0d;
		double ser = 0d;
		double[] cof = new double[6];
		cof[0] = 76.18009172947146;
		cof[1] = -86.50532032941677;
		cof[2] = 24.01409824083091;
		cof[3] = -1.231739572450155;
		cof[4] = 0.1208650973866179e-2;
		cof[5] = -0.5395239384953e-5;

		y = x = xx;
		tmp = x + 5.5;
		tmp -= (x + 0.5) * Math.log(tmp);
		ser = 1.000000000190015;
		for (int j = 0; j <= 5; j++) {
			ser += cof[j] / ++y;
		}
		return -tmp + Math.log(2.5066282746310005 * ser / x);
	}

	/**
	 * betaContinuedFraction(a,b,x) as described in Numerical Recipes in C
	 * 
	 */
	private static double betaContinuedFraction(double a, double b, double x) throws Exception {

		double aa = 0d;
		double c = 0d;
		double d = 0d;
		double del = 0d;
		double h = 0d;
		double qab = 0d;
		double qam = 0d;
		double qap = 0d;

		qab = a + b;
		qap = a + 1d;
		qam = a - 1d;
		c = 1d;
		d = 1d - qab * x / qap;
		if (Math.abs(d) < FPMIN)
			d = FPMIN;
		d = 1d / d;
		h = d;
		double m2 = 0d;
		for (int m = 1; m <= MAXIT; m++) {
			m2 = 2d * m;
			aa = (double) m * (b - (double) m) * x / ((qam + m2) * (a + m2));
			d = 1d + aa * d;
			if (Math.abs(d) < FPMIN)
				d = FPMIN;
			c = 1d + aa / c;
			if (Math.abs(c) < FPMIN)
				c = FPMIN;
			d = 1d / d;
			h *= d * c;
			aa = -1d * (a + (double) m) * (qab + (double) m) * x / ((a + m2) * (qap + m2));
			d = 1d + aa * d;
			if (Math.abs(d) < FPMIN)
				d = FPMIN;
			c = 1d + aa / c;
			if (Math.abs(c) < FPMIN)
				c = FPMIN;
			d = 1d / d;
			del = d * c;
			h *= del;
			if (Math.abs(del - 1d) < EPS)
				break;
			if (m > MAXIT)
				throw new Exception("a or b too big, or MAXIT too small");
		}
		return h;
	}

}
