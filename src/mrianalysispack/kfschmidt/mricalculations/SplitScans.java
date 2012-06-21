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
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mrianalysispack.kfschmidt.ijcommon.IJAdapter;
import mrianalysispack.kfschmidt.ijcommon.IJImageChooser;

public class SplitScans extends MRICalculation {
	IJImageChooser mScanChooser;
	JTextField mAlphaText;
	JTextField mLambdaText;
	JTextField mT1Text;

	public int getParamPanelHeight() {
		return 100;
	}

	public JPanel getParamPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(2, 1));

		// scan selection
		JPanel subpan0 = new JPanel();
		subpan0.setLayout(new GridLayout(1, 2));
		subpan0.add(new JLabel("BOLD+ASL Scan"));
		mScanChooser = new IJImageChooser();
		subpan0.add(mScanChooser);
		pan.add(subpan0);

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

		return pan;
	}

	public boolean isReady() {
		if (mScanChooser.userSelectedImage()) {
			return true;
		} else
			return false;
	}

	public void doCalculation() {
		try {
			IJAdapter ijada = new IJAdapter();
			double[][][][] stimscan = ijada.get4DDataForId(mScanChooser.getSelectedIJId());
			double lambda = Double.parseDouble(mLambdaText.getText());
			double alpha = Double.parseDouble(mAlphaText.getText());
			double t1 = Double.parseDouble(mT1Text.getText());
			double[][][][] aslw = getASLWFromInterleaved(stimscan, alpha, lambda, t1);
			double[][][][] bold = getBOLDFromInterleaved(stimscan);

			// send the images back to imagej
			String name = ijada.getNameForImageId(mScanChooser.getSelectedIJId());
			ijada.takeImage(name + "_CBF", aslw);
			ijada.takeImage(name + "_BOLD", bold);

		} catch (Exception e) {
			getUI().showError(e);
		}
	}

	public String getHelpURL() {
		return "http://www.quickvol.com/ccni/mricalculations.pdf";
	}

	public String getDisplayTitle() {
		return "Split into CBF and BOLD";
	}
}
