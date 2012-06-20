package kfschmidt.glmanalyzer;

/**
 *   This is the managerial class for the GLM Analyzer plugin.
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
import kfschmidt.ijcommon.IJAdapter;

public class GLMMGR {
	GLMUI mUI;
	boolean mFitCurrentlyRunning = false;
	int mLastFitProgress;
	FittedDataSet mFitResults;
	GLMModel mCurrentModel;
	int mCurrentImageId;
	IJAdapter mIJada = new IJAdapter();
	double[][][][] mSeriesData;
	int[] mSeriesDataSample; // 35 = 1 stdev, mean set to 50

	/**
	 * Instantiates the nec. GLM Analyzer classes
	 * 
	 */
	public GLMMGR() {
		mUI = new GLMUI(this);
		showUI();
	}

	/**
	 * Displays the GLM Analyzer UI
	 * 
	 */
	public void showUI() {
		mUI.show();
	}

	public boolean isFitRunning() {
		return mFitCurrentlyRunning;
	}

	public int getFitProgress() {
		return mLastFitProgress;
	}

	public void updateFitProgress(GLMModel mod, int pct_finished) {
		mLastFitProgress = pct_finished;
		mUI.refresh();
	}

	public void finishedFit(FittedDataSet results) {
		// got the results
		mFitResults = results;
		mFitCurrentlyRunning = false;
		mLastFitProgress = 0;
		mUI.refresh();
	}

	public GLMModel getCurrentModel() {
		return mCurrentModel;
	}

	public boolean isValidDataSelected() {
		if (mCurrentImageId != 0)
			return true;
		else
			return false;
	}

	public boolean isValidModelSelected() {
		if (mCurrentModel != null)
			return true;
		else
			return false;
	}

	public boolean hasDataBeenFit() {
		if (mFitResults != null)
			return true;
		else
			return false;
	}

	public int[] getTimeSeriesSample() {
		return mSeriesDataSample;
	}

	public void doFit() {
		System.out.println("GLMMGR.doFit()");
		mFitCurrentlyRunning = true;
		updateFitProgress(mCurrentModel, 0);
		GLMMath.fitModelToData(this, mSeriesData, mCurrentModel);
	}

	public void showCCMap() {
		System.out.println("GLMMGR.showCCMap()");
		mIJada.takeImage("CC Map", getFitResults().getCCMap());
	}

	private double[][][] maskMap(double[][][] map_to_mask, double[][][] mask, double threshold) {
		double[][][] ret = new double[mask.length][mask[0].length][mask[0][0].length];
		for (int s = 0; s < mask.length; s++) {
			for (int y = 0; y < mask[0][0].length; y++) {
				for (int x = 0; x < mask[0].length; x++) {
					if (mask[s][x][y] > threshold)
						ret[s][x][y] = 0d;
					else
						ret[s][x][y] = map_to_mask[s][x][y];
				}
			}
		}
		return ret;
	}

	public void showPctMap() {
		System.out.println("GLMMGR.showPctMap()");
		mIJada.takeImage("Pct Chg Map", maskMap(getFitResults().getPctMap(), getFitResults().getPvalMap(), mUI.getCurThreshold()));
	}

	public void showPhaseMap() {
		System.out.println("GLMMGR.showPctMap()");
		mIJada.takeImage("Phase shift map", getFitResults().getPhaseMap());
	}

	public void showPvalMap() {
		System.out.println("GLMMGR.showPvalMap()");
		mIJada.takeImage("P Value Map", getFitResults().getPvalMap());
	}

	public FittedDataSet getFitResults() {
		return mFitResults;
	}

	public int getCurrentImageId() {
		return mCurrentImageId;
	}

	public void userChoseModel(String modelname) {
		System.out.println("GLMMgr user chose model: " + modelname);
		if (modelname.indexOf("SELECT") > -1) {
			mCurrentModel = null;
		} else {
			int timepoints = 100;
			if (mCurrentImageId != 0) {
				// get the time points for the image
				int[] dims = mIJada.getImageDims(mCurrentImageId);
				timepoints = dims[3];
			}

			mCurrentModel = new GLMModel(modelname, timepoints, false);
		}
		mFitResults = null;
		mUI.refresh();
	}

	public void userChoseImage(int ij_id) {
		System.out.println("GLMMGR user chose image: " + ij_id);
		mCurrentImageId = ij_id;
		mSeriesData = null;
		mSeriesDataSample = null;
		if (ij_id != 0) {
			System.out.println("Starting data load and down sample");
			// sample the center of the image for the whole time series
			try {
				mSeriesData = mIJada.get4DDataForId(mCurrentImageId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int sample_slice = mSeriesData[0].length / 2;
			int roi_width = mSeriesData[0][0].length / 4;
			int roi_startx = mSeriesData[0][0].length / 2;
			int roi_height = mSeriesData[0][0][0].length / 4;
			int roi_starty = mSeriesData[0][0][0].length / 2 - roi_height;

			double[] sample = new double[mSeriesData.length];
			for (int t = 0; t < mSeriesData.length; t++) {
				for (int y = 0; y < roi_height; y++) {
					for (int x = 0; x < roi_width; x++) {
						sample[t] += mSeriesData[t][sample_slice][roi_startx + x][roi_starty + y];
					}
				}
				sample[t] = sample[t] / (double) (roi_height * roi_width);
			}

			// normalize, set mean to 50, scale 50-0 = -1.5 stdev, 50-100 +1.5
			// stev stdev
			double sum_of_squares = 0d;
			double square_of_sum = 0d;
			for (int n = 0; n < sample.length; n++) {
				sum_of_squares += sample[n] * sample[n];
				square_of_sum += sample[n];
			}
			double mean = square_of_sum / (double) sample.length;
			square_of_sum = square_of_sum * square_of_sum;
			double var = (sum_of_squares - square_of_sum / (double) sample.length) / (double) sample.length;
			double stdev = Math.sqrt(var);

			mSeriesDataSample = new int[mSeriesData.length];
			for (int n = 0; n < sample.length; n++) {
				mSeriesDataSample[n] = 50 + (int) (35d * (sample[n] - mean) / stdev);
			}

			// if the model is not null, set the model time points to image set
			if (mCurrentModel != null)
				mCurrentModel.changeTimePoints(sample.length);
			System.out.println("finished data load and down sample.");
		}

		mCurrentImageId = ij_id;

		mFitResults = null;
		mUI.refresh();
	}

	/**
	 * exits
	 */
	public void exit() {
		// destroy any UI objects, forcing exit
		mUI.dispose();
		mUI = null;
		System.out.println("GLMMGR exited.");
	}

}
