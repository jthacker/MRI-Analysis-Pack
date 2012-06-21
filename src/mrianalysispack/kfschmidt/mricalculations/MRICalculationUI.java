package mrianalysispack.kfschmidt.mricalculations;

/**
 *   Generic class for implementing basics of MRICalculation
 *   interface. Runs the calculation once user clicks go, etc.
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
import javax.swing.*;

import mrianalysispack.kfschmidt.ijcommon.IJAdapter;

import java.awt.event.*;
import java.awt.*;

public class MRICalculationUI extends JFrame implements ActionListener {
	MRICalculation mCalc;
	int mWidth = 300;
	int mHeight = 100;
	JButton mHelpButton;
	JButton mDoCalcButton;
	JProgressBar mProgressBar;

	public MRICalculationUI(MRICalculation calc) {
		mCalc = calc;
		setupFrame();
		setVisible(true);
	}

	public void showError(String msg) {
		JOptionPane.showMessageDialog(null, msg, "ERROR!", JOptionPane.ERROR_MESSAGE);
	}

	public void showError(Exception e) {
		e.printStackTrace();
		showError(e.toString());
	}

	private void setupFrame() {
		setSize(mWidth, mHeight + mCalc.getParamPanelHeight());
		setTitle(mCalc.getDisplayTitle());
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getStatusPanel(), BorderLayout.NORTH);
		getContentPane().add(mCalc.getParamPanel(), BorderLayout.CENTER);
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
	}

	private Panel getButtonPanel() {
		mHelpButton = new JButton("Help");
		mDoCalcButton = new JButton("Calculate");
		Panel pan = new Panel();
		pan.setLayout(new GridLayout(1, 2));
		pan.add(mHelpButton);
		pan.add(mDoCalcButton);
		mHelpButton.addActionListener(this);
		mDoCalcButton.addActionListener(this);
		return pan;
	}

	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() == mHelpButton) {
			showHelp();
		} else if (ae.getSource() == mDoCalcButton) {
			runCalc();
		}

	}

	private void runCalc() {
		if (mCalc.isReady()) {
			Thread t = new Thread(mCalc);
			t.start();
		} else {
			showError("Calculation is not ready to be run!");
		}
	}

	private void showHelp() {
		try {
			IJAdapter.openURL(mCalc.getHelpURL());
		} catch (Exception e) {
			showError(e);
		}
	}

	JLabel mStatusLabel;

	private JPanel getStatusPanel() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(1, 2));
		mStatusLabel = new JLabel(mCalc.getStatusMsg());
		mProgressBar = new JProgressBar();
		pan.add(mStatusLabel);
		pan.add(mProgressBar);
		return pan;
	}

	public void updateDisplay() {
		mStatusLabel.setText(mCalc.getStatusMsg());
		repaint();
	}

	public void setProgress(int val) {
		mProgressBar.setValue(val);
		updateDisplay();
	}

}
