package mrianalysispack.kfschmidt.mricalculations;

/**
 *   Interface for a runnable MRI Calcualtion
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
import javax.swing.JPanel;

public abstract class MRICalculation implements Runnable {
	public static final int STATUS_IDLE = 0;
	public static final int STATUS_WORKING = 1;
	public static final int STATUS_FINISHED = 2;

	private MRICalculationUI mUI;
	private int mStatus;
	private String mStatusMsg = "No Status To Report";

	public MRICalculation() {
		mUI = new MRICalculationUI(this);
	}

	public int getStatus() {
		return mStatus;
	}

	public void run() {
		mStatus = STATUS_WORKING;
		Runtime.getRuntime().gc();
		doCalculation();
		Runtime.getRuntime().gc();
		mStatus = STATUS_FINISHED;
	}

	public int getParamPanelHeight() {
		return 50;
	}

	public String getStatusMsg() {
		return mStatusMsg;
	}

	public void setStatusMsg(String msg) {
		mStatusMsg = msg;
	}

	public MRICalculationUI getUI() {
		return mUI;
	}

	// abstract methods
	public abstract boolean isReady();

	public abstract void doCalculation();

	public abstract String getHelpURL();

	public abstract JPanel getParamPanel();

	public abstract String getDisplayTitle();

	// UTILITY METHODS
	public double[][][][] getASLWFromInterleaved(double[][][][] scandata, double alpha, double lambda, double t1) {
		setStatusMsg("Extracting ASLW");
		int reps = scandata.length;
		int slices = scandata[0].length;
		int width = scandata[0][0].length;
		int height = scandata[0][0][0].length;
		double[][][][] ret = new double[reps / 2][slices][width][height];
		for (int r = 0; r < reps; r = (r + 2)) {
			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						// (Sctl-Slabel)/Sctl
						ret[r / 2][s][x][y] = ((60d * lambda) / t1) * (scandata[r][s][x][y] - scandata[r + 1][s][x][y])
								/ (scandata[r + 1][s][x][y] + (2d * alpha - 1d) * scandata[r][s][x][y]);
					}
				}
			}
			getUI().setProgress(100 * (r / reps));
		}

		setStatusMsg("Finished");
		return ret;
	}

	public double[][][][] getBOLDFromInterleaved(double[][][][] scandata) {
		setStatusMsg("Extracting BOLD");
		int reps = scandata.length;
		int slices = scandata[0].length;
		int width = scandata[0][0].length;
		int height = scandata[0][0][0].length;
		double[][][][] ret = new double[reps / 2][slices][width][height];
		for (int r = 0; r < reps; r = (r + 2)) {
			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						ret[r / 2][s][x][y] = scandata[r][s][x][y];
					}
				}
			}
			getUI().setProgress(100 * (r / reps));
		}

		setStatusMsg("Finished");
		return ret;
	}

	public double[][][][] getPercentChangeMap(double[][][][] scandata, int baseline_start_rep, int baseline_stop_rep, int stim_start_rep, int stim_stop_rep) {
		setStatusMsg("Calculating static %chg map");
		int reps = scandata.length;
		int slices = scandata[0].length;
		int width = scandata[0][0].length;
		int height = scandata[0][0][0].length;
		double[][][][] ret = new double[1][slices][width][height];
		for (int s = 0; s < slices; s++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// get the baseline avg
					double baseline = 0d;
					for (int r = baseline_start_rep; r < baseline_stop_rep; r++) {
						baseline += scandata[r][s][x][y];
					}

					// get the stim avg
					double stim = 0d;
					for (int r = stim_start_rep; r < stim_stop_rep; r++) {
						stim += scandata[r][s][x][y];
					}

					// calc the change
					stim = stim / (double) (stim_stop_rep - stim_start_rep);
					baseline = baseline / (double) (baseline_stop_rep - baseline_start_rep);
					ret[0][s][x][y] = (stim - baseline) / baseline;
				}
				getUI().setProgress(100 * (s * width * height + y * width) / (slices * width * height));
			}
		}

		setStatusMsg("Finished");
		return ret;
	}

	public double[][][][] getRatioMap(double[][][][] scandata, int baseline_start_rep, int baseline_stop_rep, int stim_start_rep, int stim_stop_rep) {
		setStatusMsg("Calculating static ratio map");
		int reps = scandata.length;
		int slices = scandata[0].length;
		int width = scandata[0][0].length;
		int height = scandata[0][0][0].length;
		double[][][][] ret = new double[1][slices][width][height];
		for (int s = 0; s < slices; s++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// get the baseline avg
					double baseline = 0d;
					for (int r = baseline_start_rep; r < baseline_stop_rep; r++) {
						baseline += scandata[r][s][x][y];
					}

					// get the stim avg
					double stim = 0d;
					for (int r = stim_start_rep; r < stim_stop_rep; r++) {
						stim += scandata[r][s][x][y];
					}

					// calc the change
					stim = stim / (double) (stim_stop_rep - stim_start_rep);
					baseline = baseline / (double) (baseline_stop_rep - baseline_start_rep);
					ret[0][s][x][y] = stim / baseline;
				}
				getUI().setProgress(100 * (s * width * height + y * width) / (slices * width * height));
			}
		}

		setStatusMsg("Finished");
		return ret;
	}

	public double[][][][] getTimeAverageMap(double[][][][] scandata, int baseline_start_rep, int baseline_stop_rep) {
		setStatusMsg("Calculating time average map");
		int reps = scandata.length;
		int slices = scandata[0].length;
		int width = scandata[0][0].length;
		int height = scandata[0][0][0].length;
		double[][][][] ret = new double[1][slices][width][height];
		for (int s = 0; s < slices; s++) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					// get the baseline avg
					double baseline = 0d;
					for (int r = baseline_start_rep; r < baseline_stop_rep; r++) {
						baseline += scandata[r][s][x][y];
					}

					// calc the change
					baseline = baseline / (double) (baseline_stop_rep - baseline_start_rep);
					ret[0][s][x][y] = baseline;
				}
				getUI().setProgress(100 * (s * width * height + y * width) / (slices * width * height));
			}
		}

		setStatusMsg("Finished");
		return ret;
	}

	public double[][][][] getRollingPercentChangeMap(double[][][][] scandata, int baseline_start_rep, int baseline_stop_rep, int average_window_width) {
		setStatusMsg("Calculating rolling %chg map");

		setStatusMsg("Finished");
		return null;
	}

}
