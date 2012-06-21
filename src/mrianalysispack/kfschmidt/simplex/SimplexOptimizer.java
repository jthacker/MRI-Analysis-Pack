/**
 *   Implementation of the Simplex minimization algorithm
 *   Nelder Mead (1965)
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
package mrianalysispack.kfschmidt.simplex;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class SimplexOptimizer {

	double mLastTolerance;
	Vertex mSimplex[];
	double mRho;
	double mChi;
	double mGamma;
	double mSigma;
	int mMaxIterations;
	int mNumRestarts;
	int mMaxRestarts;
	double mTolerance;
	Object mObject;
	Method mMethod;
	String mMethodName;
	Object mAllParameters[];
	int mParamTypes[];
	double mGoalValue;
	int mNumIters;
	boolean mIsInitialized;
	Hashtable mClassMap;
	Vertex mOrigGuesses;
	boolean DEBUG;

	public SimplexOptimizer() {
		mRho = 1.0D;
		mChi = 1D;
		mGamma = 0.5D;
		mSigma = 0.5D;
		mMaxIterations = 10000;
		mMaxRestarts = 2;
		mTolerance = 0.00000001D;
		mClassMap = new Hashtable();
		mClassMap.put("java.lang.Double", Double.TYPE);
		mClassMap.put("java.lang.Float", Float.TYPE);
		mClassMap.put("java.lang.Integer", Integer.TYPE);
		mClassMap.put("java.lang.Boolean", Boolean.TYPE);
	}

	public Object[] go() throws Exception {
		for (; mNumRestarts <= mMaxRestarts; mNumRestarts++) {
			restart();
		}
		return mAllParameters;
	}

	public void setDebug(boolean debug) {
		DEBUG = debug;
	}

	public void setObjectAndMethod(Object obj, String s, int i, double d) throws Exception {
		Method amethod[] = obj.getClass().getMethods();
		boolean flag = true;
		for (int j = 0; j < amethod.length; j++)
			if (amethod[j].getName().equals(s) && amethod[j].getParameterTypes().length == i)
				flag = false;

		if (flag) {
			throw new Exception("Could not find a method named: " + s + " in object " + obj.getClass().getName());
		} else {
			mAllParameters = new Object[i];
			mParamTypes = new int[i];
			mGoalValue = d;
			mObject = obj;
			mMethodName = s;
			return;
		}
	}

	public void setFixedParamAsString(int i, String s) throws Exception {
		checkMethodIsDefined();
		mAllParameters[i - 1] = s;
		mParamTypes[i - 1] = -1;
	}

	public void setFixedParamAsBoolean(int i, boolean flag) throws Exception {
		checkMethodIsDefined();
		mAllParameters[i - 1] = new Boolean(flag);
		mParamTypes[i - 1] = -1;
	}

	public void setFixedParamAsObject(int i, Object obj) throws Exception {
		checkMethodIsDefined();
		mAllParameters[i - 1] = obj;
		mParamTypes[i - 1] = -1;
	}

	public void setFixedParamAsDouble(int i, double d) throws Exception {
		checkMethodIsDefined();
		mAllParameters[i - 1] = new Double(d);
		mParamTypes[i - 1] = -1;
	}

	public void setFixedParamAsFloat(int i, float f) throws Exception {
		checkMethodIsDefined();
		mAllParameters[i - 1] = new Float(f);
		mParamTypes[i - 1] = -1;
	}

	public void setFixedParamAsInteger(int i, int j) throws Exception {
		checkMethodIsDefined();
		mAllParameters[i - 1] = new Integer(j);
		mParamTypes[i - 1] = -1;
	}

	public void setVariableParam(int i, double d) throws Exception {
		if (d == 0.0D) {
			throw new Exception("Set modifiable variables to small numbers rather than zero");
		} else {
			checkMethodIsDefined();
			mAllParameters[i - 1] = new Double(d);
			mParamTypes[i - 1] = 1;
			return;
		}
	}

	public void initialize() throws Exception {
		mSimplex = null;
		mNumIters = 0;
		mNumRestarts = 0;
		if (mObject == null)
			throw new Exception("Object for optimization not defined");
		if (mMethodName == null)
			throw new Exception("Method name must not be null");
		for (int i = 0; i < mParamTypes.length; i++)
			if (mParamTypes[i] == 0)
				throw new Exception("Parameter " + (i + 1) + " is not defined");

		Class aclass[] = new Class[mAllParameters.length];
		for (int j = 0; j < mAllParameters.length; j++) {
			aclass[j] = mAllParameters[j].getClass();
			if (mClassMap.get(aclass[j].getName()) != null)
				aclass[j] = (Class) mClassMap.get(aclass[j].getName());
		}

		mMethod = mObject.getClass().getDeclaredMethod(mMethodName, aclass);

		Vector vector = new Vector();
		for (int k = 0; k < mParamTypes.length; k++)
			if (mParamTypes[k] == 1)
				vector.addElement(mAllParameters[k]);

		mOrigGuesses = new Vertex(vector.size());
		for (int l = 0; l < vector.size(); l++)
			mOrigGuesses.setParameter(l, ((Double) vector.elementAt(l)).doubleValue());

		calculateResidual(mOrigGuesses);
		mSimplex = generateSimplex(mOrigGuesses);

		for (int i1 = 1; i1 < mSimplex.length; i1++)
			calculateResidual(mSimplex[i1]);
		order(mSimplex);
		mIsInitialized = true;
	}

	public void restart() throws Exception {
		if (DEBUG)
			System.out.println("========== retstart ==========\n");
		mNumIters = 0;
		mSimplex = generateSimplex(mSimplex[0]);
		for (int i1 = 1; i1 < mSimplex.length; i1++)
			calculateResidual(mSimplex[i1]);
		order(mSimplex);
		seek();
	}

	public double getGamma() {
		return mGamma;
	}

	public void setGamma(double d) {
		mGamma = d;
	}

	public double getRho() {
		return mRho;
	}

	public void setRho(double d) {
		mRho = d;
	}

	public double getSigma() {
		return mSigma;
	}

	public void setSigma(double d) {
		mSigma = d;
	}

	public double getChi() {
		return mChi;
	}

	public void setChi(double d) {
		mChi = d;
	}

	public int getMaxIterations() {
		return mMaxIterations;
	}

	public void setMaxIterations(int i) {
		mMaxIterations = i;
	}

	public int getMaxRestarts() {
		return mMaxRestarts;
	}

	public void setMaxRestarts(int i) {
		mMaxRestarts = i;
	}

	public double getTolerance() {
		return mTolerance;
	}

	public void setTolerance(double d) {
		mTolerance = d;
	}

	public Object[] getAllParameters() {
		return mAllParameters;
	}

	public double getBestResidual() {
		return mSimplex[0].getResidual();
	}

	// private helper methods

	private Object[] seek() throws Exception {
		long l = System.currentTimeMillis();
		for (; !testForStop(); iterate())
			;
		long l1 = System.currentTimeMillis();

		return mAllParameters;
	}

	private boolean testForStop() {
		if (mNumIters > mMaxIterations) {
			return true;
		}

		if (mLastTolerance < mTolerance)
			return true;
		else
			return false;
	}

	private String dumpVertex(Vertex vertex) {
		String s = "VERTEX: ";
		s = s + "\nR[" + vertex.getResidual() + "]\t";
		for (int i = 0; i < vertex.getNumParams(); i++)
			s = s + "P" + i + "[" + vertex.getParameter(i) + "]\t";

		System.out.println(s);
		return s;
	}

	private static DecimalFormat getDecimalFormater(String pattern) {
		DecimalFormat instance = (DecimalFormat) NumberFormat.getInstance();
		instance.applyPattern(pattern);
		return instance;
	}

	private String dumpSimplex(Vertex avertex[]) {
		// format the number
		DecimalFormat nf = getDecimalFormater("0.000E0#");
		String s = "";
		for (int i = 0; i < avertex.length; i++) {
			s = s + "\nR[" + nf.format(avertex[i].getResidual()) + "]\t";
			for (int j = 0; j < avertex[0].getNumParams(); j++)
				s = s + "P" + j + "[" + nf.format(avertex[i].getParameter(j)) + "]\t";

		}

		System.out.println(s);
		return s;
	}

	Random random = new Random(10000000); // DEBUG - to make artifacts easier to
											// id.

	/**
	 * creates a simplex of starting vertices
	 * 
	 * 
	 */
	private Vertex[] generateSimplex(Vertex vertex) {
		Vertex avertex[] = new Vertex[vertex.getNumParams() + 1];
		avertex[0] = vertex;
		for (int i = 0; i < vertex.getNumParams(); i++) {
			Vertex vertex1 = new Vertex(vertex.getNumParams());
			// need to combat the problem of local minima found at really big
			// and really small limits
			for (int j = 0; j < vertex1.getNumParams(); j++)
				vertex1.setParameter(j, random.nextDouble() * vertex.getParameter(j) + random.nextDouble() * mOrigGuesses.getParameter(j));

			avertex[i + 1] = vertex1;
		}

		return avertex;
	}

	private double calculateResidual(Vertex vertex) throws Exception {
		int i = 0;
		for (int j = 0; j < mAllParameters.length; j++)
			if (mParamTypes[j] == 1) {
				mAllParameters[j] = new Double(vertex.getParameter(i));
				i++;
			}

		double d = ((Double) mMethod.invoke(mObject, mAllParameters)).doubleValue();
		double d1 = Math.abs(mGoalValue - d);
		vertex.setResidual(d1);
		return d1;
	}

	private void iterate() throws Exception {
		// 4 possible outcomes
		// A-reflect: if the reflected point is better than the second worst but
		// worse than the best
		// B-reflect and expand: if the reflected point is the new best point
		// C-contract: reflected point was still the worst, not the worst after
		// contraction
		// D-shrink: reflected point after contraction was still the worst

		// 1 - Order the simplex
		order(mSimplex);
		if (testForStop())
			return;
		if (DEBUG)
			dumpSimplex(mSimplex);

		mNumIters++;
		Vertex vertex = getCentroidOfAllButWorstVertex(mSimplex);
		Vertex vertex1 = reflect(vertex, mSimplex[mSimplex.length - 1], mRho);

		// 2 - if reflected point is new best, test expansion and terminate (B)
		if (vertex1.getResidual() <= mSimplex[0].getResidual()) {
			Vertex vertex2 = expand(vertex, vertex1, mChi);
			if (vertex2.getResidual() < vertex1.getResidual()) {
				mSimplex[mSimplex.length - 1] = vertex1;
				if (DEBUG)
					System.out.println("REFLECT-EXPAND");
			} else {
				if (DEBUG)
					System.out.println("REFLECT ONLY");
				mSimplex[mSimplex.length - 1] = vertex2;
			}
			return;
		}

		// 3- reflected point is not the new best
		else {
			if (vertex1.getResidual() >= mSimplex[mSimplex.length - 2].getResidual()) {
				// if reflected point is still the worst, contract
				Vertex vertex3 = outsideContract(vertex, vertex1, mGamma);

				// if the reflected-contracted point is still the worst, shrink
				// and return (D)
				if (vertex3.getResidual() >= mSimplex[mSimplex.length - 2].getResidual()) {
					shrink(mSimplex, mSigma);
					if (DEBUG)
						System.out.println("SHRINK");
					return;
				}

				// otherwise, the contracted point is no longer the worst,
				// accept and return (C)
				mSimplex[mSimplex.length - 1] = vertex3;
				if (DEBUG)
					System.out.println("REFLECT-CONTRACT");
				return;
			}

			// otherwise, the reflected point is not the new best, but not the
			// worst, accept the reflected point (A)
			mSimplex[mSimplex.length - 1] = vertex1;
			if (DEBUG)
				System.out.println("REFLECT");
			return;
		}
	}

	private Vertex getCentroidOfAllButWorstVertex(Vertex avertex[]) {
		Vertex vertex = new Vertex(avertex[0].getNumParams());
		for (int i = 0; i < avertex.length - 1; i++) {
			vertex.addVertex(avertex[i]);
		}

		vertex.multiplyByScalar(1.0D / (double) (avertex.length - 1));
		return vertex;
	}

	private Vertex reflect(Vertex vertex, Vertex vertex1, double d) throws Exception {
		Vertex vertex2 = new Vertex(vertex1.getNumParams());
		for (int i = 0; i < vertex1.getNumParams(); i++) {
			double d1 = vertex.getParameter(i);
			double d2 = vertex1.getParameter(i);
			double d3 = d1 + d * (d1 - d2);
			vertex2.setParameter(i, d3);
		}

		calculateResidual(vertex2);
		return vertex2;
	}

	/*
	 * vertex is the centroid, vertex1 is the point to expand
	 */
	private Vertex expand(Vertex vertex, Vertex vertex1, double d) throws Exception {
		Vertex vertex2 = new Vertex(vertex1.getNumParams());
		for (int i = 0; i < vertex1.getNumParams(); i++) {
			double d1 = vertex.getParameter(i);
			double d2 = vertex1.getParameter(i);
			double d3 = d2 + d * (d2 - d1);
			vertex2.setParameter(i, d3);
		}

		calculateResidual(vertex2);
		return vertex2;
	}

	/*
	 * vertex is the centroid, vertex1 is the point to contract
	 */
	private Vertex outsideContract(Vertex vertex, Vertex vertex1, double d) throws Exception {
		Vertex vertex2 = new Vertex(vertex1.getNumParams());
		for (int i = 0; i < vertex1.getNumParams(); i++) {
			double d1 = vertex.getParameter(i);
			double d2 = vertex1.getParameter(i);
			double d3 = d2 + d * (d1 - d2);
			vertex2.setParameter(i, d3);
		}

		calculateResidual(vertex2);
		return vertex2;
	}

	/**
	 * contract all around best point
	 */
	private Vertex[] shrink(Vertex avertex[], double d) throws Exception {
		for (int i = 1; i < avertex.length; i++) {
			for (int j = 0; j < avertex[0].getNumParams(); j++) {
				double d1 = avertex[i].getParameter(j) + d * (avertex[0].getParameter(j) - avertex[i].getParameter(j));
				avertex[i].setParameter(j, d1);
			}

			calculateResidual(avertex[i]);
		}

		return avertex;
	}

	private Vertex[] order(Vertex avertex[]) {
		Arrays.sort(avertex);
		// tolerance is the % difference btn the best and worst residuals
		mLastTolerance = Math.abs((avertex[avertex.length - 1].getResidual() - avertex[0].getResidual()) / avertex[0].getResidual());

		return avertex;
	}

	private static String dumpObjectArray(Object aobj[]) {
		String s = "[";
		for (int i = 0; i < aobj.length; i++)
			s = s + ", " + aobj[i];

		s = s + "]";
		return s;
	}

	private void checkMethodIsDefined() throws Exception {
		if (mAllParameters == null)
			throw new Exception("You must call setObjectAndMethod(...) prior to setting paramters");
		else
			return;
	}

}
