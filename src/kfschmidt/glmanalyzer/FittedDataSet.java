package kfschmidt.glmanalyzer;

/**
 * User Interface for the GLM Analyzer
 * 
 * 
 * @author Karl Schmidt <karl.schmidt@umassmed.edu> This software is provided
 *         for use free of any costs, Be advised that NO guarantee is made
 *         regarding it's quality, and there is no ongoing support for this
 *         codebase.
 * 
 *         (c) Karl Schmidt 2004
 * 
 *         REVISION HISTORY:
 * 
 * 
 */
public class FittedDataSet {
	GLMModel mModel;
	int mImageId;
	double[][][] mCCMap;
	double[][][] mPvalMap;
	double[][][] mPctMap;
	double[][][] mOffsetMap;
	double[][][] mPhaseMap;

	public FittedDataSet(int slices, int width, int height) {
		mCCMap = new double[slices][width][height];
		mPvalMap = new double[slices][width][height];
		mPctMap = new double[slices][width][height];
		mOffsetMap = new double[slices][width][height];
		mPhaseMap = new double[slices][width][height];
	}

	public double[][][] getCCMap() {
		return mCCMap;
	}

	public double[][][] getPvalMap() {
		return mPvalMap;
	}

	public double[][][] getPctMap() {
		return mPctMap;
	}

	public double[][][] getPhaseMap() {
		return mPhaseMap;
	}

	public void setCCMap(double[][][] map) {
		mCCMap = map;
	}

	public void setPvalMap(double[][][] map) {
		mPvalMap = map;
	}

	public void setPctMap(double[][][] map) {
		mPctMap = map;
	}

	public void setPhaseMap(double[][][] map) {
		mPhaseMap = map;
	}

	public void setImageId(int id) {
		mImageId = id;
	}

	public int getImageId() {
		return mImageId;
	}

	public void setModel(GLMModel model) {
		mModel = model;
	}

	public GLMModel getModel() {
		return mModel;
	}

}
