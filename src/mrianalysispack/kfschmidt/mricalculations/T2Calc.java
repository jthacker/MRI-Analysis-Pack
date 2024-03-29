package mrianalysispack.kfschmidt.mricalculations;

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
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mrianalysispack.kfschmidt.ijcommon.IJAdapter;
import mrianalysispack.kfschmidt.ijcommon.IJImageChooser;

public class T2Calc extends MRICalculation {
	IJImageChooser mScanChooser;
	JTextField mTEText;
	JCheckBox mShowRMap;
	JCheckBox mDoLinear;

	public JPanel getParamPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(4, 1));

		// scan selection
		JPanel subpan0 = new JPanel();
		subpan0.setLayout(new GridLayout(1, 2));
		subpan0.add(new JLabel("Multi TE Scan"));
		mScanChooser = new IJImageChooser();
		subpan0.add(mScanChooser);
		pan.add(subpan0);

		// TE Value specification
		JPanel subpan1 = new JPanel();
		subpan1.setLayout(new GridLayout(1, 2));
		subpan1.add(new JLabel("TE Values (ms):"));
		mTEText = new JTextField(40);
		subpan1.add(mTEText);
		pan.add(subpan1);

		// Show R^2
		JPanel subpan2 = new JPanel();
		subpan2.setLayout(new GridLayout(1, 2));
		subpan2.add(new JLabel("Show R^2 map"));
		mShowRMap = new JCheckBox();
		subpan2.add(mShowRMap);
		pan.add(subpan2);

		// Use linear regression
		JPanel subpan3 = new JPanel();
		subpan3.setLayout(new GridLayout(1, 2));
		subpan3.add(new JLabel("Linear regression"));
		mDoLinear = new JCheckBox();
		subpan3.add(mDoLinear);
		pan.add(subpan3);

		return pan;
	}

	public int getParamPanelHeight() {
		return 70;
	}

	public boolean isReady() {
		if (mScanChooser.userSelectedImage()) {
			return true;
		} else
			return false;
	}

	private double[] getTEVals() {
		String te_vals_str = mTEText.getText();

		if (te_vals_str == null)
			return null;

		String sep = " ";
		if (te_vals_str.indexOf(",") > -1)
			sep = ",";
		if (te_vals_str.indexOf(";") > -1)
			sep = ";";

		// parse out the TE vals
		StringTokenizer tok = new StringTokenizer(te_vals_str, sep);
		Vector v = new Vector();
		while (tok.hasMoreTokens()) {
			v.addElement(tok.nextToken());
		}

		double[] ret = new double[v.size()];
		for (int a = 0; a < ret.length; a++) {
			ret[a] = Double.parseDouble((String) v.elementAt(a)) / 1000d;
		}

		// check that TE vals are appropriate
		return ret;
	}

	public void doCalculation() {

		double[][][][] t2_map = null;
		double[][][][] rSquared_map = null;

		try {
			IJAdapter ijada = new IJAdapter();
			double[][][][] scan = ijada.get4DDataForId(mScanChooser.getSelectedIJId());
			int slices = scan[0].length;
			int width = scan[0][0].length;
			int height = scan[0][0][0].length;
			int reps = scan.length;
			System.out.println("Data set dimensions: height=" + height + " width=" + width + " slices=" + slices + " repetitions=" + reps);

			// te values
			double[] te_vals = getTEVals();
			double[] sn_vals = new double[reps];
			double[] regressed = null;

			setStatusMsg("Calculating T2 map");
			t2_map = new double[1][slices][width][height];
			rSquared_map = new double[1][slices][width][height];

			SimplexBasedRegressor simplexregressor = new SimplexBasedRegressor();
			LinearRegression linearregressor = new LinearRegression();

			// fit T2 and So
			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {

						// for each pixel, collect values for each te
						for (int r = 0; r < reps; r++) {
							if (mDoLinear.isSelected()) {
								// take the log of valid data
								if (scan[r][s][x][y] > 0) {
									sn_vals[r] = Math.log(scan[r][s][x][y]);
								}

							} else {
								sn_vals[r] = scan[r][s][x][y]; // orig
							}
						}

						if (sn_vals[0] == 0) {
							t2_map[0][s][x][y] = 0d;
							rSquared_map[0][s][x][y] = 0d;

						} else {
							if (mDoLinear.isSelected()) {
								// Radhika here is the linear regression
								// modification
								regressed = linearregressor.regress(te_vals, sn_vals);

								t2_map[0][s][x][y] = -1d * (1d / regressed[0]);
								rSquared_map[0][s][x][y] = regressed[2] * regressed[2];

							} else {
								regressed = simplexregressor.regressT2(te_vals, sn_vals, 0.05d);

								t2_map[0][s][x][y] = regressed[0];
								rSquared_map[0][s][x][y] = regressed[2];
							}

						}

					}
					getUI().setProgress(100 * (s * width * height + y * width) / (width * height * slices));
				}
			}

			setStatusMsg("Finished");

			// send the images back to imagej
			ijada.takeImage("T2_map_secs", t2_map);
			if (mShowRMap.isSelected())
				ijada.takeImage("R^2_map", rSquared_map);
			scan = null;
			System.gc();
		} catch (Exception e) {
			getUI().showError(e);
		}

	}

	public String getHelpURL() {
		return "http://www.quickvol.com/ccni/mricalculations.pdf";
	}

	public String getDisplayTitle() {
		return "T2 Calculation";
	}
}
