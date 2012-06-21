package kfschmidt.mricalculations;

/**
 *
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

public class TimeAverageCalc extends MRICalculation {
	IJImageChooser mScanChooser;
	JTextField mBaseLineStart;
	JTextField mBaseLineStop;

	public int getParamPanelHeight() {
		return 70;
	}

	public JPanel getParamPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(2, 1));

		// scan selection
		JPanel subpanX = new JPanel();
		subpanX.setLayout(new GridLayout(1, 2));
		subpanX.add(new JLabel("Scan"));
		mScanChooser = new IJImageChooser();
		subpanX.add(mScanChooser);
		pan.add(subpanX);

		// baseline
		JPanel subpanb = new JPanel();
		subpanb.setLayout(new GridLayout(1, 2));
		mBaseLineStart = new JTextField("2", 4);
		mBaseLineStop = new JTextField("50", 4);
		JPanel subsubpan1 = new JPanel();
		subpanb.add(new JLabel("Timeperiod start/stop"));
		subsubpan1.add(mBaseLineStart);
		subsubpan1.add(mBaseLineStop);
		subpanb.add(subsubpan1);
		pan.add(subpanb);

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
			double[][][][] scan = ijada.get4DDataForId(mScanChooser.getSelectedIJId());

			double[][][][] ratio = getTimeAverageMap(scan, Integer.parseInt(mBaseLineStart.getText()), Integer.parseInt(mBaseLineStop.getText()));

			// send the images back to imagej
			String name = ijada.getNameForImageId(mScanChooser.getSelectedIJId()) + "_TIME_AVG";
			ijada.takeImage(name, ratio);

		} catch (Exception e) {
			getUI().showError(e);
		}
	}

	public String getHelpURL() {
		return "http://www.quickvol.com/ccni/mricalculations.pdf";
	}

	public String getDisplayTitle() {
		return "Ratio = X/Xo";
	}
}
