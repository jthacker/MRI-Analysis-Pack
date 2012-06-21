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
import javax.swing.JCheckBox;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextField;
import kfschmidt.ijcommon.IJAdapter;

public class ADCCalc extends MRICalculation {
	IJImageChooser mScanChooser;
	JTextField mBText;
	JCheckBox mShowRMap;

	public JPanel getParamPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(3, 1));

		// scan selection
		JPanel subpan0 = new JPanel();
		subpan0.setLayout(new GridLayout(1, 2));
		subpan0.add(new JLabel("Multi B value scan"));
		mScanChooser = new IJImageChooser();
		subpan0.add(mScanChooser);
		pan.add(subpan0);

		// TE Value specification
		JPanel subpan1 = new JPanel();
		subpan1.setLayout(new GridLayout(1, 2));
		subpan1.add(new JLabel("B Values (s/mm2):"));
		mBText = new JTextField(40);
		subpan1.add(mBText);
		pan.add(subpan1);

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
		return 60;
	}

	public boolean isReady() {
		if (mScanChooser.userSelectedImage()) {
			return true;
		} else
			return false;
	}

	private double[] getBVals() {
		String te_vals_str = mBText.getText();

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
			ret[a] = Double.parseDouble((String) v.elementAt(a));
		}

		// check that TE vals are appropriate
		return ret;
	}

	public void doCalculation() {

		double[][][][] adc_map = null;
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
			double[] b_vals = getBVals();
			double[] sn_vals = new double[reps];

			setStatusMsg("Calculating ADC map");
			adc_map = new double[1][slices][width][height];
			r2_map = new double[1][slices][width][height];

			SimplexBasedRegressor simplexregressor = new SimplexBasedRegressor();

			// fit T2 and So
			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {

						// for each pixel, collect values for each b
						for (int r = 0; r < reps; r++) {
							sn_vals[r] = scan[r][s][x][y];
						}

						// do the regression here
						double[] regressed = simplexregressor.regressADC(b_vals, sn_vals, 0.05d);

						adc_map[0][s][x][y] = regressed[0];
						r2_map[0][s][x][y] = regressed[2];
					}
					getUI().setProgress(100 * (s * width * height + y * width) / (width * height * slices));
				}
			}

			setStatusMsg("Finished");

			// send the images back to imagej
			ijada.takeImage("ADC_map_mm2_per_sec", adc_map);
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
		return "T2 Calculation";
	}
}
