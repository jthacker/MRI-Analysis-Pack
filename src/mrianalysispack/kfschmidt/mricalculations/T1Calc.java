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
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;

import mrianalysispack.kfschmidt.ijcommon.IJAdapter;
import mrianalysispack.kfschmidt.ijcommon.IJImageChooser;

public class T1Calc extends MRICalculation {
	IJImageChooser mScanChooser;
	JTextField mTRText;
	JCheckBox mShowRMap;
	JRadioButton mSatRecov;
	JRadioButton mInvRecov;

	public JPanel getParamPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(4, 1));

		// scan selection
		JPanel subpan0 = new JPanel();
		subpan0.setLayout(new GridLayout(1, 2));
		subpan0.add(new JLabel("Multi TR Scan"));
		mScanChooser = new IJImageChooser();
		subpan0.add(mScanChooser);
		pan.add(subpan0);

		// TR Value specification
		JPanel subpan1 = new JPanel();
		subpan1.setLayout(new GridLayout(1, 2));
		subpan1.add(new JLabel("TR Values (ms):"));
		mTRText = new JTextField(40);
		subpan1.add(mTRText);
		pan.add(subpan1);

		// Sat/Inv recovery specification
		JPanel subpan3 = new JPanel();
		subpan3.setLayout(new GridLayout(2, 2));
		subpan3.add(new JLabel("Saturation Recovery"));
		mSatRecov = new JRadioButton();
		subpan3.add(mSatRecov);
		mSatRecov.setSelected(true);
		subpan3.add(new JLabel("Inversion Recovery"));
		mInvRecov = new JRadioButton();
		subpan3.add(mInvRecov);
		ButtonGroup group = new ButtonGroup();
		group.add(mSatRecov);
		group.add(mInvRecov);
		pan.add(subpan3);

		// Show R^2
		JPanel subpan2 = new JPanel();
		subpan2.setLayout(new GridLayout(1, 2));
		subpan2.add(new JLabel("Show R^2 map"));
		mShowRMap = new JCheckBox();
		subpan2.add(mShowRMap);
		pan.add(subpan2);

		return pan;
	}

	public int getParamPanelHeight() {
		return 100;
	}

	public boolean isReady() {
		if (mScanChooser.userSelectedImage()) {
			return true;
		} else
			return false;
	}

	private double[] getTRVals() {
		String tr_vals_str = mTRText.getText();

		if (tr_vals_str == null)
			return null;

		String sep = " ";
		if (tr_vals_str.indexOf(",") > -1)
			sep = ",";
		if (tr_vals_str.indexOf(";") > -1)
			sep = ";";

		// parse out the TR vals
		StringTokenizer tok = new StringTokenizer(tr_vals_str, sep);
		Vector v = new Vector();
		while (tok.hasMoreTokens()) {
			v.addElement(tok.nextToken());
		}

		double[] ret = new double[v.size()];
		for (int a = 0; a < ret.length; a++) {
			ret[a] = Double.parseDouble((String) v.elementAt(a)) / 1000d;
		}

		// check that TR vals are appropriate
		return ret;
	}

	public void doCalculation() {
		double[][][][] t1_map = null;
		double[][][][] r2_map = null;

		try {
			IJAdapter ijada = new IJAdapter();
			double[][][][] scan = ijada.get4DDataForId(mScanChooser.getSelectedIJId());
			int slices = scan[0].length;
			int width = scan[0][0].length;
			int height = scan[0][0][0].length;
			int reps = scan.length;
			System.out.println("Data set dimensions: h=" + height + " width=" + width + " slices=" + slices + " reps=" + reps);

			// te values
			double[] tr_vals = getTRVals();
			double[] sn_vals = new double[reps];

			setStatusMsg("Calculating T1 map");
			t1_map = new double[1][slices][width][height];
			r2_map = new double[1][slices][width][height];

			SimplexBasedRegressor simplexregressor = new SimplexBasedRegressor();

			// fit T2 and So
			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						// for each pixel, collect values for each te
						for (int r = 0; r < reps; r++) {
							sn_vals[r] = scan[r][s][x][y];
						}

						// do the regression here

						double[] regressed = null;
						if (mSatRecov.isSelected()) {
							regressed = simplexregressor.regressT1SaturationRecovery(tr_vals, sn_vals, 0.5d);
						} else if (mInvRecov.isSelected()) {
							regressed = simplexregressor.regressT1InversionRecovery(tr_vals, sn_vals, 0.5d);
						}

						t1_map[0][s][x][y] = regressed[0];
						r2_map[0][s][x][y] = regressed[2];
					}
					getUI().setProgress(100 * (s * width * height + y * width) / (width * height * slices));
				}
			}

			setStatusMsg("Finished");

			// send the images back to imagej
			ijada.takeImage("T1_map_secs", t1_map);
			if (mShowRMap.isSelected())
				ijada.takeImage("R^2_map", r2_map);
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
		return "T1 Calc";
	}
}
