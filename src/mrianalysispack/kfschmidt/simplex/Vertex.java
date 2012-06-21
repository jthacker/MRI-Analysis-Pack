/**
 *   Utility class for the SimplexOptimizer class
 *
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
package kfschmidt.simplex;

class Vertex implements Comparable {

	public Vertex(int i) {
		mResidual = -1D;
		mParameters = new double[i];
	}

	public void setResidual(double d) {
		mResidual = d;
	}

	public double getResidual() {
		return mResidual;
	}

	public int compareTo(Object obj) {
		if (mResidual == -1D)
			return 1;
		if (((Vertex) obj).getResidual() < mResidual && ((Vertex) obj).getResidual() > 0.0D)
			return 1;
		return ((Vertex) obj).getResidual() <= mResidual ? 0 : -1;
	}

	public double getParameter(int i) {
		return mParameters[i];
	}

	public void setParameter(int i, double d) {
		mParameters[i] = d;
	}

	public int getNumParams() {
		return mParameters.length;
	}

	public void addVertex(Vertex vertex) {
		for (int i = 0; i < mParameters.length; i++)
			mParameters[i] = mParameters[i] + vertex.getParameter(i);

	}

	public void multiplyByScalar(double d) {
		for (int i = 0; i < mParameters.length; i++)
			mParameters[i] = mParameters[i] * d;

	}

	double mResidual;
	double mParameters[];
}
