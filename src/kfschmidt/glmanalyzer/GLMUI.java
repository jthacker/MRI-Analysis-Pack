package kfschmidt.glmanalyzer;

/**
 *   User Interface for the GLM Analyzer
 *
 *
 *   @author Karl Schmidt <karl.schmidt@umassmed.edu>
 *   This software is provided for use free of any costs,
 *   Be advised that NO guarantee is made regarding it's quality,
 *   and there is no ongoing support for this codebase.
 *
 *   (c) Karl Schmidt 2004
 *
 *   REVISION HISTORY:
 *
 *
 */
import kfschmidt.ijcommon.IJImageChooser;
import javax.swing.*;
import java.awt.event.*;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import kfschmidt.ijcommon.IJAdapter;

public class GLMUI extends JFrame implements WindowListener, ActionListener {
	int mWidth = 550;
	int mHeight = 350;
	GLMMGR mMgr;
	JButton mFitButton;
	JButton mPctButton;
	JButton mCCButton;
	JButton mPvalButton;
	JButton mPhaseButton;
	IJImageChooser mIJChooser;
	JComboBox mModelChooser;
	GLMModelPanel mModelPanel;
	JRadioButton mNoThreshold;
	JRadioButton mThreshold5;
	JRadioButton mThreshold1;

	public GLMUI(GLMMGR manager) {
		mMgr = manager;

		// setup frame
		setSize(mWidth, mHeight);
		setTitle(Literals.WINDOW_TITLE);

		// setup internal panel
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(setupTopPanel(), BorderLayout.NORTH);
		getContentPane().add(setupBottomPanel(), BorderLayout.CENTER);

		addWindowListener(this);
		syncControls();
	}

	public void refresh() {
		// draw the model panel
		mModelPanel.repaint();

		// sync controls
		syncControls();
	}

	public double getCurThreshold() {
		if (mNoThreshold.isSelected())
			return 1d;
		else if (mThreshold5.isSelected())
			return 0.01d;
		else if (mThreshold1.isSelected())
			return 0.001d;
		else
			return 1d;
	}

	public void syncControls() {
		mFitButton.removeActionListener(this);
		mPctButton.removeActionListener(this);
		mCCButton.removeActionListener(this);
		mPvalButton.removeActionListener(this);
		mPhaseButton.removeActionListener(this);
		mIJChooser.getComboBox().removeActionListener(this);
		mModelChooser.removeActionListener(this);
		if (!mMgr.isFitRunning()) {
			mModelChooser.setEnabled(true);
			mIJChooser.getComboBox().setEnabled(true);
		}

		mNoThreshold.setEnabled(false);
		mThreshold5.setEnabled(false);
		mThreshold1.setEnabled(false);

		// use cases
		if (mMgr.isFitRunning()) {
			mFitButton.setEnabled(false);
			mPctButton.setEnabled(false);
			mCCButton.setEnabled(false);
			mPvalButton.setEnabled(false);
			mPhaseButton.setEnabled(false);
			mModelChooser.setEnabled(false);
			mIJChooser.getComboBox().setEnabled(false);
		} else if (mMgr.isValidDataSelected() && mMgr.isValidModelSelected() && mMgr.hasDataBeenFit()) {
			// 1) image + model + fit
			// disable fit, enable other buttons;
			mFitButton.setEnabled(false);
			mPctButton.setEnabled(true);
			mCCButton.setEnabled(true);
			mPvalButton.setEnabled(true);
			mPhaseButton.setEnabled(true);
			mNoThreshold.setEnabled(true);
			mThreshold5.setEnabled(true);
			mThreshold1.setEnabled(true);

		} else if (mMgr.isValidDataSelected() && mMgr.isValidModelSelected() && !mMgr.hasDataBeenFit()) {
			// 2) image + model
			mFitButton.setEnabled(true);
			mPctButton.setEnabled(false);
			mCCButton.setEnabled(false);
			mPvalButton.setEnabled(false);
			mPhaseButton.setEnabled(false);

		} else if (mMgr.isValidDataSelected() && !mMgr.isValidModelSelected() && !mMgr.hasDataBeenFit()) {
			// 3) image + no model + no fit
			mFitButton.setEnabled(false);
			mPctButton.setEnabled(false);
			mCCButton.setEnabled(false);
			mPvalButton.setEnabled(false);
			mPhaseButton.setEnabled(false);

		} else if (!mMgr.isValidDataSelected() && !mMgr.isValidModelSelected() && !mMgr.hasDataBeenFit()) {
			// 4) no image, no model, no fit
			mFitButton.setEnabled(false);
			mPctButton.setEnabled(false);
			mCCButton.setEnabled(false);
			mPvalButton.setEnabled(false);
			mPhaseButton.setEnabled(false);

		} else if (!mMgr.isValidDataSelected() && mMgr.isValidModelSelected() && !mMgr.hasDataBeenFit()) {
			// 5) no image, +model, no fit
			mFitButton.setEnabled(false);
			mPctButton.setEnabled(false);
			mCCButton.setEnabled(false);
			mPvalButton.setEnabled(false);
			mPhaseButton.setEnabled(false);

		} else {
			System.out.println("UKNOWN USE CASE: " + mMgr.isValidDataSelected() + ", " + mMgr.isValidModelSelected() + ", " + mMgr.hasDataBeenFit());
		}

		mIJChooser.setSelectedIJId(mMgr.getCurrentImageId());

		mFitButton.addActionListener(this);
		mPctButton.addActionListener(this);
		mCCButton.addActionListener(this);
		mPvalButton.addActionListener(this);
		mPhaseButton.addActionListener(this);
		mIJChooser.getComboBox().addActionListener(this);
		mModelChooser.addActionListener(this);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == mIJChooser.getComboBox()) {
			int id = 0;
			try {
				id = mIJChooser.getSelectedIJId();
			} catch (Exception e) {
			}
			mMgr.userChoseImage(id);
		} else if (ae.getSource() == mModelChooser) {
			mMgr.userChoseModel((String) mModelChooser.getSelectedItem());
		} else if (ae.getSource() == mFitButton) {
			mMgr.doFit();
		} else if (ae.getSource() == mPctButton) {
			mMgr.showPctMap();
		} else if (ae.getSource() == mCCButton) {
			mMgr.showCCMap();
		} else if (ae.getSource() == mPvalButton) {
			mMgr.showPvalMap();
		} else if (ae.getSource() == mPhaseButton) {
			mMgr.showPhaseMap();
		}

	}

	public void windowActivated(WindowEvent we) {
	}

	public void windowDeactivated(WindowEvent we) {
	}

	public void windowClosed(WindowEvent we) {
	}

	public void windowClosing(WindowEvent we) {
		mMgr.exit();
	}

	public void windowDeiconified(WindowEvent we) {
	}

	public void windowIconified(WindowEvent we) {
	}

	public void windowOpened(WindowEvent we) {
	}

	private JComboBox getModelChooser() {
		return new JComboBox(GLMModel.getModelNames());
	}

	private JPanel getSelectorPan() {
		// panel containing model and
		// image selectors

		JPanel retpan = new JPanel();
		retpan.setLayout(new GridLayout(3, 1));

		JPanel pan1 = new JPanel();
		JPanel pan2 = new JPanel();

		pan1.setLayout(new BorderLayout());
		pan2.setLayout(new BorderLayout());

		pan1.add(new JLabel("  "), BorderLayout.WEST);
		pan1.add(new JLabel(Literals.SERIES_LABEL), BorderLayout.CENTER);
		mIJChooser = new IJImageChooser();
		pan1.add(mIJChooser, BorderLayout.EAST);

		pan2.add(new JLabel("  "), BorderLayout.WEST);
		pan2.add(new JLabel(Literals.MODEL_LABEL), BorderLayout.CENTER);
		mModelChooser = getModelChooser();
		pan2.add(mModelChooser, BorderLayout.EAST);

		retpan.add(pan1);
		retpan.add(pan2);
		return retpan;
	}

	private JPanel setupTopPanel() {
		// contains the image selector
		JPanel retpan = new JPanel();
		retpan.setLayout(new BorderLayout());

		retpan.add(getSelectorPan(), BorderLayout.WEST);

		JPanel buttonpan = new JPanel();
		buttonpan.setLayout(new GridLayout(1, 1));
		buttonpan.add(getButtonPan());
		retpan.add(buttonpan, BorderLayout.EAST);

		retpan.add(getThreshButtonPan(), BorderLayout.CENTER);

		return retpan;
	}

	private JPanel setupBottomPanel() {
		JPanel retpan = new JPanel();
		retpan.setLayout(new BorderLayout());
		mModelPanel = new GLMModelPanel(mMgr);
		retpan.add(mModelPanel);
		return retpan;
	}

	private JPanel getThreshButtonPan() {
		JPanel retpan = new JPanel();
		retpan.setLayout(new GridLayout(3, 1));
		mNoThreshold = new JRadioButton(Literals.THRESH_NO_BUT);
		mThreshold5 = new JRadioButton(Literals.THRESH_5_BUT);
		mThreshold1 = new JRadioButton(Literals.THRESH_1_BUT);
		ButtonGroup grp = new ButtonGroup();
		grp.add(mNoThreshold);
		grp.add(mThreshold5);
		grp.add(mThreshold1);
		mNoThreshold.setSelected(true);
		retpan.add(mNoThreshold);
		retpan.add(mThreshold5);
		retpan.add(mThreshold1);
		return retpan;

	}

	private JPanel getButtonPan() {
		JPanel retpan = new JPanel();
		retpan.setLayout(new BorderLayout());
		mFitButton = new JButton(Literals.FIT_BUTTON);
		retpan.add(mFitButton, BorderLayout.SOUTH);
		retpan.add(getSubButtonPan(), BorderLayout.CENTER);
		return retpan;
	}

	private JPanel getSubButtonPan() {
		JPanel retpan = new JPanel();
		retpan.setLayout(new GridLayout(2, 2));

		mPctButton = new JButton(Literals.PCT_BUTTON);
		mCCButton = new JButton(Literals.CC_BUTTON);
		mPvalButton = new JButton(Literals.PVAL_BUTTON);
		mPhaseButton = new JButton(Literals.PHASE_BUTTON);

		retpan.add(mPctButton);
		retpan.add(mCCButton);
		retpan.add(mPvalButton);
		retpan.add(mPhaseButton);

		return retpan;
	}

}
