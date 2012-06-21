package mrianalysispack.kfschmidt.mricalculations;

/**
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
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mrianalysispack.kfschmidt.ijcommon.IJAdapter;
import mrianalysispack.kfschmidt.ijcommon.IJImageChooser;

public class ManualDAVISCMRO2Calc extends MRICalculation {
	IJImageChooser mCO2BChooser;
	IJImageChooser mCO2FChooser;
	IJImageChooser mStimBChooser;
	IJImageChooser mStimFChooser;
	JTextField mAlpha;
	JTextField mBeta;
	double NOISE_VALUE = Double.NaN;

	public int getParamPanelHeight() {
		return 200;
	}

	private JPanel setupSelector(String label, IJImageChooser b_chooser, IJImageChooser f_chooser) {
		JPanel retpan = new JPanel();
		retpan.setLayout(new GridLayout(2, 2));
		retpan.add(new JLabel("BOLD/BOLDo"));
		retpan.add(b_chooser);
		retpan.add(new JLabel("CBF/CBFo"));
		retpan.add(f_chooser);

		TitledBorder title;
		title = BorderFactory.createTitledBorder(label);
		retpan.setBorder(title);

		return retpan;
	}

	public JPanel getParamPanel() {
		mCO2BChooser = new IJImageChooser();
		mCO2FChooser = new IJImageChooser();
		mStimBChooser = new IJImageChooser();
		mStimFChooser = new IJImageChooser();

		JPanel pan = new JPanel();
		pan.setLayout(new BorderLayout());

		JPanel centerpan = new JPanel();
		centerpan.setLayout(new GridLayout(2, 1));
		centerpan.add(setupSelector("Hypercapnic Scan", mCO2BChooser, mCO2FChooser));
		centerpan.add(setupSelector("Stim Scan", mStimBChooser, mStimFChooser));
		pan.add(centerpan);
		pan.add(getConstantsPanel(), BorderLayout.SOUTH);
		return pan;
	}

	public JPanel getConstantsPanel() {
		JPanel pan = new JPanel();

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
		if (mCO2BChooser.userSelectedImage() && mCO2FChooser.userSelectedImage() && mStimBChooser.userSelectedImage() && mStimFChooser.userSelectedImage()) {
			return true;
		} else
			return false;
	}

	public void doCalculation() {

		double[][][][] m_map = null;
		double[][][][] cmro2_map = null;
		double alpha = Double.parseDouble(mAlpha.getText());
		double beta = Double.parseDouble(mBeta.getText());

		try {
			// calculate m_map
			IJAdapter ijada = new IJAdapter();
			double[][][][] co2_bold_ratio = ijada.get4DDataForId(mCO2BChooser.getSelectedIJId());
			double[][][][] co2_cbf_ratio = ijada.get4DDataForId(mCO2FChooser.getSelectedIJId());

			int slices = co2_bold_ratio[0].length;
			int width = co2_bold_ratio[0][0].length;
			int height = co2_bold_ratio[0][0][0].length;

			m_map = new double[1][slices][width][height];

			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {

						// negative bold or cbf ratios are noise, set to near
						// zero to ease math
						if (co2_bold_ratio[0][s][x][y] < 0)
							co2_bold_ratio[0][s][x][y] = NOISE_VALUE;
						if (co2_cbf_ratio[0][s][x][y] < 0)
							co2_cbf_ratio[0][s][x][y] = NOISE_VALUE;

						m_map[0][s][x][y] = (co2_bold_ratio[0][s][x][y] - 1d) / (1d - Math.pow(co2_cbf_ratio[0][s][x][y], (alpha - beta)));

						if (m_map[0][s][x][y] < 0)
							m_map[0][s][x][y] = NOISE_VALUE;
					}
				}
			}

			// calculate the CMRO2 map
			double[][][][] stim_bold_ratio = ijada.get4DDataForId(mStimBChooser.getSelectedIJId());
			double[][][][] stim_cbf_ratio = ijada.get4DDataForId(mStimFChooser.getSelectedIJId());
			cmro2_map = new double[1][slices][width][height];

			for (int s = 0; s < slices; s++) {
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {

						// negative bold or cbf ratios are noise, set to noise
						// value
						if (stim_bold_ratio[0][s][x][y] < 0)
							stim_bold_ratio[0][s][x][y] = NOISE_VALUE;
						if (stim_cbf_ratio[0][s][x][y] < 0)
							stim_cbf_ratio[0][s][x][y] = NOISE_VALUE;

						// the quantity [1 - ln(bold)/M] must be greater than
						// zero, so
						// we set it to zero in the event that it is less,
						// otherwise, our data
						// is riddled with NaN values
						double intermed_val = 1d - ((stim_bold_ratio[0][s][x][y] - 1d) / m_map[0][s][x][y]);
						if (intermed_val < 0)
							intermed_val = NOISE_VALUE;

						cmro2_map[0][s][x][y] = Math.pow(intermed_val / Math.pow(stim_cbf_ratio[0][s][x][y], (alpha - beta)), (1d / beta));
					}
				}
			}

			// send the images back to imagej
			ijada.takeImage("DAVIS_M", m_map);
			ijada.takeImage("DAVIS_CMRO2", cmro2_map);
			System.gc();
		} catch (Exception e) {
			getUI().showError(e);
		}

	}

	public String getHelpURL() {
		return "http://www.quickvol.com/ccni/mricalculations.pdf";
	}

	public String getDisplayTitle() {
		return "Manual Davis CMRO2 Calc";
	}
}
