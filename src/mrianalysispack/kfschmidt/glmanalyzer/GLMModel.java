package mrianalysispack.kfschmidt.glmanalyzer;

/**
 * 
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

public class GLMModel {
	int mType;
	public static int BOXCAR = 1;
	public static int CUSTOM = 2;

	static String HALF_STEP = "BOXCAR 0.5 Steps";
	static String ONE_STEP = "BOXCAR 1 Steps";
	static String TWO_STEP = "BOXCAR 2 Steps";
	static String THREE_STEP = "BOXCAR 3 Steps";
	static String FOUR_STEP = "BOXCAR 4 Steps";

	// BOXCAR MODEL
	int[][] mTransitions; // format [transition_number][0] = start time_point
							// [transition_number][1] = finish time_point
							// [transition_number][2] = 1 if start high, 0 if
							// start low

	int mTimePoints;
	double[][][] mCCMap;
	double[][][] mPCTChgMap;
	double[][][] mPValMap;
	String mModelName;
	double[][] mModelData; // matrix M from y=Mb+e

	/**
	 * Create a model from an arbitrary set of HRF functions
	 * 
	 */
	public GLMModel(String name, double[][] matrix) {
		mModelData = matrix;
		mType = CUSTOM;
	}

	/**
	 * BOXCAR model design
	 * 
	 * @param number_of_transitions
	 *            The number of transitions from hi to low or vice versa
	 * @param time_points
	 *            The numer of time points in the data to be fit
	 * @param start_high_instead_of_low
	 *            The model begins in an activated state and the first
	 *            transition is to the lower level
	 * 
	 */
	public GLMModel(int number_of_transitions, int time_points, boolean start_high_instead_of_low) {
		mType = BOXCAR;
		mTimePoints = time_points;
		mTransitions = new int[number_of_transitions][3];
		initTransitions(start_high_instead_of_low);
	}

	public GLMModel(String model_name, int time_points, boolean start_high) {
		mType = BOXCAR;
		mTimePoints = time_points;
		if (model_name.equals(HALF_STEP)) {
			mTransitions = new int[1][3];
		} else if (model_name.equals(ONE_STEP)) {
			mTransitions = new int[2][3];
		} else if (model_name.equals(TWO_STEP)) {
			mTransitions = new int[4][3];
		} else if (model_name.equals(THREE_STEP)) {
			mTransitions = new int[6][3];
		} else if (model_name.equals(FOUR_STEP)) {
			mTransitions = new int[8][3];
		} else {
			mTransitions = new int[2][3];
		}
		initTransitions(start_high);
	}

	/**
	 * set the transitions to logical points
	 * 
	 */
	private void initTransitions(boolean start_high) {
		// space them evenly
		boolean toggle = start_high;
		for (int t = 0; t < mTransitions.length; t++) {
			mTransitions[t][0] = (t + 1) * mTimePoints / (mTransitions.length + 1);
			mTransitions[t][1] = 1 + (t + 1) * mTimePoints / (mTransitions.length + 1);
			if (toggle) {
				mTransitions[t][2] = 1;
			} else {
				mTransitions[t][2] = 0;
			}
			toggle = !toggle;
		}
	}

	public int getTimePoints() {
		return mTimePoints;
	}

	public int getType() {
		return mType;
	}

	public static String[] getModelNames() {
		String[] ret = new String[6];

		ret[0] = "- SELECT -";
		ret[1] = HALF_STEP;
		ret[2] = ONE_STEP;
		ret[3] = TWO_STEP;
		ret[4] = THREE_STEP;
		ret[5] = FOUR_STEP;
		return ret;
	}

	/**
	 * Creates a basic boxcar model, starting low, with specified number of
	 * steps
	 */
	public GLMModel(int number_of_transitions, int time_points) {
		this(number_of_transitions, time_points, false);
	}

	/**
	 * helper method to return transition nearest to a selected time point,
	 * boxcar only
	 */
	public int getNearestTransition(int timepoint) {
		if (mType != BOXCAR)
			return -1;
		double best_score = (double) mTimePoints;
		int best_transition = -1;
		double test_val;
		for (int a = 0; a < mTransitions.length; a++) {
			for (int b = 0; b < 2; b++) {
				test_val = Math.abs(mTransitions[a][b] - timepoint);
				if (test_val < best_score) {
					best_score = test_val;
					best_transition = a;
				}
			}
		}
		return best_transition + 1;
	}

	/**
	 * helper method to get information on a particular transition, boxcar only
	 * 
	 */
	public int[] getTransition(int transition_number) {
		if (transition_number <= 0 || transition_number > mTransitions.length) {
			return null;
		} else
			return mTransitions[transition_number - 1];
	}

	/**
	 * valid only for a BOXCAR model, move the step to the new location
	 * 
	 */
	public void changeTransition(int transition, int start_time_point, int stop_time_point)

	{
		if (mTransitions.length < transition && mTransitions[transition][0] >= stop_time_point)
			return;

		if (transition > 1 && mTransitions[transition - 2][1] >= start_time_point)
			return;

		if (start_time_point < 0 || start_time_point >= mTimePoints || stop_time_point < 1 || stop_time_point > mTimePoints)
			return;

		mTransitions[transition - 1][0] = start_time_point;
		mTransitions[transition - 1][1] = stop_time_point;

	}

	/**
	 * Changes the number of timepoints in the series, and moves any existing
	 * transitions if nec
	 * 
	 */
	public void changeTimePoints(int time_point_count) {
		mTimePoints = time_point_count;
	}

	public double[][] getModelMatrix() {
		return getModelMatrixWithPhaseShift(0);
	}

	/**
	 * Returns the M matrix of model data
	 * 
	 */
	public double[][] getModelMatrixWithPhaseShift(int shift) {
		if (mType == BOXCAR) {
			// the second term will be the DC offset
			double[][] ret = new double[mTimePoints][2];

			double curpoint = 0d;
			if (mTransitions[0][2] > 0)
				curpoint = 1d;

			// fill the array
			double slope = 0d;
			int next_trans = 0;
			boolean is_interpolating = false;
			for (int a = 0; a < ret.length; a++) {
				ret[a][1] = 1d; // this is the dc offset

				if (next_trans < mTransitions.length && a == (mTransitions[next_trans][0] + shift)) {
					// do nothing to the curpoint value
					// we are starting a new transition
					int trans_time = mTransitions[next_trans][1] - mTransitions[next_trans][0];

					if (trans_time == 0) {
						slope = Double.NaN;
					} else {
						slope = 1d / (double) trans_time;
						if (mTransitions[next_trans][2] > 0)
							slope = -1d * slope;
					}

					next_trans++;
					is_interpolating = true;

				} else if (is_interpolating && a != (mTransitions[next_trans - 1][1] + shift)) {
					// we're in the middle of a transition
					curpoint = curpoint + slope;

				} else if (is_interpolating && a == (mTransitions[next_trans - 1][1] + shift)) {
					// we're finishing a transition
					is_interpolating = false;

					if (slope == Double.NaN) {
						// step function
						if (curpoint == 1d)
							curpoint = 0d;
						else
							curpoint = 1d;
					} else {
						// graduated function
						curpoint = slope + curpoint;
					}
				} else {
					// we're inbetween transitions, do nothing to curpoint
				}

				ret[a][0] = curpoint;
			}
			return ret;
		} else {
			return mModelData;
		}

	}

}
