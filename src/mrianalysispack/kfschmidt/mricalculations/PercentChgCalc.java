package kfschmidt.mricalculations;

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
import kfschmidt.ijcommon.IJAdapter;
import kfschmidt.ijcommon.IJImageChooser;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import kfschmidt.ijcommon.IJAdapter;

public class PercentChgCalc extends MRICalculation {
	IJImageChooser mScanChooser;
	JTextField mBaseLineStart;
	JTextField mBaseLineStop;
	JTextField mStimStart;
	JTextField mStimStop;

	public int getParamPanelHeight() {
		return 100;
	}

	public JPanel getParamPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(3, 1));

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
		subpanb.add(new JLabel("Baseline start/stop"));
		subsubpan1.add(mBaseLineStart);
		subsubpan1.add(mBaseLineStop);
		subpanb.add(subsubpan1);
		pan.add(subpanb);

		// stim
		JPanel subpan2 = new JPanel();
		subpan2.setLayout(new GridLayout(1, 2));
		mStimStart = new JTextField("60", 4);
		mStimStop = new JTextField("100", 4);
		JPanel subsubpan2 = new JPanel();
		subpan2.add(new JLabel("Stim start/stop"));
		subsubpan2.add(mStimStart);
		subsubpan2.add(mStimStop);
		subpan2.add(subsubpan2);
		pan.add(subpan2);

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

			double[][][][] pct_chg = getPercentChangeMap(scan, Integer.parseInt(mBaseLineStart.getText()), Integer.parseInt(mBaseLineStop.getText()),
					Integer.parseInt(mStimStart.getText()), Integer.parseInt(mStimStop.getText()));
			// send the images back to imagej
			String name = ijada.getNameForImageId(mScanChooser.getSelectedIJId()) + "_PCT";
			ijada.takeImage(name, pct_chg);

		} catch (Exception e) {
			getUI().showError(e);
		}
	}

	public String getHelpURL() {
		return "http://www.quickvol.com/ccni/mricalculations.pdf";
	}

	public String getDisplayTitle() {
		return "%Chg = (X-Xo)/Xo";
	}
}
