package kfschmidt.mricalculations;

/**
 *   Modification of M calculation to accomodate [dHb] in arterial side.
 *   This is original mod sent around; assumes 50%arterial/venous volumes
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
import java.awt.GridLayout;
import kfschmidt.ijcommon.IJAdapter;
import kfschmidt.ijcommon.IJImageChooser;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import kfschmidt.ijcommon.IJAdapter;

public class DAVISCMRO2Calc extends MRICalculation {
	IJImageChooser mCO2ScanChooser;
	IJImageChooser mStimScanChooser;
	JTextField mCO2BaseLineStart;
	JTextField mCO2BaseLineStop;
	JTextField mCO2Start;
	JTextField mCO2Stop;
	JTextField mStimBaseLineStart;
	JTextField mStimBaseLineStop;
	JTextField mStimStart;
	JTextField mStimStop;
	JTextField mAlpha;
	JTextField mBeta;
	JTextField mAlphaText;
	JTextField mLambdaText;
	JTextField mT1Text;
	double NOISE_VALUE = Double.NaN;

	public int getParamPanelHeight() {
		return 250;
	}

	private JPanel getCO2Panel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(3, 1));

		// ASLW scan selection
		JPanel subpan0 = new JPanel();
		subpan0.setLayout(new GridLayout(1, 2));
		subpan0.add(new JLabel("O2+CO2 Scan"));
		mCO2ScanChooser = new IJImageChooser();
		subpan0.add(mCO2ScanChooser);
		pan.add(subpan0);

		// baseline
		JPanel subpan1 = new JPanel();
		subpan1.setLayout(new GridLayout(1, 2));
		mCO2BaseLineStart = new JTextField("2", 4);
		mCO2BaseLineStop = new JTextField("50", 4);
		JPanel subsubpan1 = new JPanel();
		subpan1.add(new JLabel("Baseline start/stop"));
		subsubpan1.add(mCO2BaseLineStart);
		subsubpan1.add(mCO2BaseLineStop);
		subpan1.add(subsubpan1);
		pan.add(subpan1);

		// hypercap
		JPanel subpan2 = new JPanel();
		subpan2.setLayout(new GridLayout(1, 2));
		mCO2Start = new JTextField("60", 4);
		mCO2Stop = new JTextField("100", 4);
		JPanel subsubpan2 = new JPanel();
		subpan2.add(new JLabel("O2+CO2 start/stop"));
		subsubpan2.add(mCO2Start);
		subsubpan2.add(mCO2Stop);
		subpan2.add(subsubpan2);
		pan.add(subpan2);
		return pan;
	}

	private JPanel getStimPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(3, 1));

		// Stim scan selection
		JPanel subpan0 = new JPanel();
		subpan0.setLayout(new GridLayout(1, 2));
		subpan0.add(new JLabel("fMRI Scan"));
		mStimScanChooser = new IJImageChooser();
		subpan0.add(mStimScanChooser);
		pan.add(subpan0);

		// baseline
		JPanel subpan1 = new JPanel();
		subpan1.setLayout(new GridLayout(1, 2));
		mStimBaseLineStart = new JTextField("2", 4);
		mStimBaseLineStop = new JTextField("50", 4);
		JPanel subsubpan1 = new JPanel();
		subpan1.add(new JLabel("Baseline start/stop"));
		subsubpan1.add(mStimBaseLineStart);
		subsubpan1.add(mStimBaseLineStop);
		subpan1.add(subsubpan1);
		pan.add(subpan1);

		// hypercap
		JPanel subpan2 = new JPanel();
		subpan2.setLayout(new GridLayout(1, 2));
		mStimStart = new JTextField("60", 4);
		mStimStop = new JTextField("100", 4);
		JPanel subsubpan2 = new JPanel();
		subpan2.add(new JLabel("Stimulation start/stop"));
		subsubpan2.add(mStimStart);
		subsubpan2.add(mStimStop);
		subpan2.add(subsubpan2);
		pan.add(subpan2);
		return pan;
	}

	public JPanel getParamPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(3, 1));
		pan.add(getCO2Panel());
		pan.add(getStimPanel());
		pan.add(getConstantsPanel());
		return pan;
	}

	public JPanel getConstantsPanel() {
		JPanel pan = new JPanel();

		// perfusion params
		JPanel subpan1 = new JPanel();
		subpan1.setLayout(new GridLayout(3, 2));
		subpan1.add(new JLabel("ASL: alpha"));
		mAlphaText = new JTextField("0.7", 4);
		subpan1.add(mAlphaText);
		subpan1.add(new JLabel("ASL: lambda"));
		mLambdaText = new JTextField("0.9", 4);
		subpan1.add(mLambdaText);
		subpan1.add(new JLabel("ASL: brain T1 (s)"));
		mT1Text = new JTextField("1.475", 5);
		subpan1.add(mT1Text);
		pan.add(subpan1);

		// alpha and beta
		JPanel subpan3 = new JPanel();
		subpan3.setLayout(new GridLayout(1, 4));
		subpan3.add(new JLabel("alpha"));
		mAlpha = new JTextField("0.35", 4);
		subpan3.add(mAlpha);
		subpan3.add(new JLabel("beta"));
		mBeta = new JTextField("1.5", 4);
		subpan3.add(mBeta);
		pan.add(subpan3);

		return pan;
	}

	public boolean isReady() {
		if (mCO2ScanChooser.userSelectedImage() && mStimScanChooser.userSelectedImage()) {
			return true;
		} else
			return false;
	}

	private double average_pixel_over_reps(double[][][][] data, int x, int y, int s, int start_rep, int stop_rep) {
		double ret = 0d;
		for (int a = start_rep; a <= stop_rep; a++) {
			ret += data[a][s][x][y];
		}
		ret = ret / (double) (stop_rep - start_rep);
		return ret;
	}

	public void doCalculation() {

		double[][][][] m_map = null;
		double[][][][] c1_map = null;
		double[][][][] c2_map = null;
		try {
			// first, process the hypercapnic scan
			IJAdapter ijada = new IJAdapter();
			double[][][][] stimscan = ijada.get4DDataForId(mCO2ScanChooser.getSelectedIJId());
			double lambda = Double.parseDouble(mLambdaText.getText());
			double label_eff = Double.parseDouble(mAlphaText.getText());
			double t1 = Double.parseDouble(mT1Text.getText());
			double[][][][] aslw = getASLWFromInterleaved(stimscan, label_eff, lambda, t1);
			double[][][][] bold = getBOLDFromInterleaved(stimscan);

			setStatusMsg("Calculating M, and C2 maps");

			int slices = bold[0].length;
			int width = bold[0][0].length;
			int height = bold[0][0][0].length;
			int reps = bold.length;
			System.out.println("Data set dimensions: h=" + height + " width=" + width + " slices=" + slices + " reps=" + reps);
			m_map = new double[1][slices][width][height];
			c1_map = new double[1][slices][width][height];
			double[][][][] r_map = new double[1][slices][width][height];
			double alpha = Double.parseDouble(mAlpha.getText());
			double beta = Double.parseDouble(mBeta.getText());
			int baseline_start = Integer.parseInt(mCO2BaseLineStart.getText());
			int baseline_stop = Integer.parseInt(mCO2BaseLineStop.getText());
			int co2_start = Integer.parseInt(mCO2Start.getText());
			int co2_stop = Integer.parseInt(mCO2Stop.getText());

			// initialize these only once
			double[] co2_aslw_array = new double[co2_stop - co2_start];
			double[] co2_bold_array = new double[co2_stop - co2_start];
			double delta_cbf = 0d;
			double baseline_perf = 0d;
			double baseline_bold = 0d;
			double hc_bold = 0d;
			double hc_perf = 0d;

			// assume that C1 is zero, so that we can determine C2 and M via
			// linear regresison
			System.out.print("***************************");
			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {

						// for each pixel, calc baseline values,
						baseline_perf = average_pixel_over_reps(aslw, x, y, s, baseline_start, baseline_stop);

						baseline_bold = average_pixel_over_reps(bold, x, y, s, baseline_start, baseline_stop);

						// calc hypercapnic values
						hc_perf = average_pixel_over_reps(aslw, x, y, s, co2_start, co2_stop);

						hc_bold = average_pixel_over_reps(bold, x, y, s, co2_start, co2_stop);

						if (hc_perf < 0)
							hc_perf = NOISE_VALUE;
						if (hc_bold < 0)
							hc_bold = NOISE_VALUE;
						if (baseline_perf < 0)
							baseline_perf = NOISE_VALUE;
						if (baseline_bold < 0)
							baseline_bold = NOISE_VALUE;

						m_map[0][s][x][y] = ((hc_bold - baseline_bold) / baseline_bold) / (1d - Math.pow((hc_perf / baseline_perf), (alpha - beta)));

						if (m_map[0][s][x][y] < 0)
							m_map[0][s][x][y] = NOISE_VALUE;
					}

					getUI().setProgress(100 * (s * width * height + y * width) / (width * height * slices));
				}
			}
			setStatusMsg("Finished");

			// send the images back to imagej
			ijada.takeImage("DAVIS_M", m_map);
			aslw = null;
			bold = null;
			System.gc();

		} catch (Exception e) {
			getUI().showError(e);
		}

		try {
			// now, process the stimulation scan, only R and C2 are unknown
			IJAdapter ijada = new IJAdapter();
			double[][][][] stimscan = ijada.get4DDataForId(mStimScanChooser.getSelectedIJId());
			double lambda = Double.parseDouble(mLambdaText.getText());
			double label_eff = Double.parseDouble(mAlphaText.getText());
			double t1 = Double.parseDouble(mT1Text.getText());
			double[][][][] aslw = getASLWFromInterleaved(stimscan, label_eff, lambda, t1);
			double[][][][] bold = getBOLDFromInterleaved(stimscan);

			setStatusMsg("Calculating CMRO2 and C2 maps");
			int slices = bold[0].length;
			int width = bold[0][0].length;
			int height = bold[0][0][0].length;
			double[][][][] cmro2_map = new double[1][slices][width][height];
			c2_map = new double[1][slices][width][height];
			double[][][][] r_map = new double[1][slices][width][height];
			double alpha = Double.parseDouble(mAlpha.getText());
			double beta = Double.parseDouble(mBeta.getText());
			int stim_baseline_start = Integer.parseInt(mStimBaseLineStart.getText());
			int stim_baseline_stop = Integer.parseInt(mStimBaseLineStop.getText());
			int stim_start = Integer.parseInt(mStimStart.getText());
			int stim_stop = Integer.parseInt(mStimStop.getText());

			// initialize these only once
			double[] stim_aslw_array = new double[stim_stop - stim_start];
			double[] stim_bold_array = new double[stim_stop - stim_start];
			double baseline_perf = 0d;
			double baseline_bold = 0d;
			double stim_bold = 0d;
			double stim_perf = 0d;

			System.out.print("***********   CMRO2 MAP  ****************");
			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {

						// for each pixel, calc baseline values,
						baseline_perf = average_pixel_over_reps(aslw, x, y, s, stim_baseline_start, stim_baseline_stop);

						baseline_bold = average_pixel_over_reps(bold, x, y, s, stim_baseline_start, stim_baseline_stop);

						// calc stim values
						stim_perf = average_pixel_over_reps(aslw, x, y, s, stim_start, stim_stop);

						stim_bold = average_pixel_over_reps(bold, x, y, s, stim_start, stim_stop);

						if (stim_perf < 0)
							stim_perf = NOISE_VALUE;
						if (stim_bold < 0)
							stim_bold = NOISE_VALUE;
						if (baseline_perf < 0)
							baseline_perf = NOISE_VALUE;
						if (baseline_bold < 0)
							baseline_bold = NOISE_VALUE;

						double intermed_val = 1d - (stim_bold - baseline_bold) / (baseline_bold * m_map[0][s][x][y]);

						cmro2_map[0][s][x][y] = Math.pow(intermed_val / Math.pow((stim_perf / baseline_perf), (alpha - beta)), (1d / beta));

					}

					getUI().setProgress(100 * (s * width * height + y * width) / (width * height * slices));
				}
			}
			setStatusMsg("Finished");

			// send the images back to imagej
			ijada.takeImage("DAVIS_CMRO2", cmro2_map);
			aslw = null;
			bold = null;
			System.gc();

		} catch (Exception e) {
			getUI().showError(e);
		}

	}

	public String getHelpURL() {
		return "http://www.quickvol.com/ccni/mricalculations.pdf";
	}

	public String getDisplayTitle() {
		return "Davis CMRO2 Calc";
	}
}
