package kfschmidt.imageoverlay;

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
import kfschmidt.ijcommon.IJImageChooser;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class OverlayPanel extends JFrame implements ActionListener, WindowListener, ChangeListener {
	OverlayMgr mMgr;
	int mFrameWidth = 350;
	int mFrameHeight = 350;

	// overlay controls
	IJImageChooser mMapCombo;
	JCheckBox mInterpolate;

	// bg image controls
	IJImageChooser mBgImageCombo;
	JSlider mBgBrightSlider;
	JSlider mBgContrastSlider;

	// Map display controls
	JComboBox mLUTs1;
	JTextField mMax1;
	JTextField mMin1;
	JSlider mMinSlider1;
	JSlider mMaxSlider1;

	JComboBox mLUTs2;
	JTextField mMax2;
	JTextField mMin2;
	JSlider mMinSlider2;
	JSlider mMaxSlider2;

	JRadioButton mContour;
	JRadioButton mLUT;

	// user controls
	JButton mExportButton;
	JTextField mDisplaySlices;
	JRadioButton mPeek;
	JRadioButton mOutline;
	JRadioButton mErase;
	JRadioButton mMoveScaleBar;
	JRadioButton mMoveLegend;
	JRadioButton mMapOnly;

	// legend
	JCheckBox mShowLegend;
	JCheckBox mLegendOnImage;
	JButton mLegendBgColor;

	// scale bar
	JComboBox mScaleSize;
	JButton mScaleBarColor;

	public OverlayPanel(OverlayMgr manager) {
		mMgr = manager;
		addWindowListener(this);
		setupFrame();
	}

	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource().equals(mBgImageCombo.getComboBox())) {
			try {
				mMgr.userChangedBgImage(mBgImageCombo.getSelectedIJId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (ae.getSource().equals(mMapCombo.getComboBox())) {
			try {
				mMgr.userChangedMapImage(mMapCombo.getSelectedIJId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (ae.getSource().equals(mLUTs1)) {
			mMgr.userChangedLUT1((String) mLUTs1.getSelectedItem());
		} else if (ae.getSource().equals(mLUTs2)) {
			mMgr.userChangedLUT2((String) mLUTs2.getSelectedItem());
		} else if (ae.getSource().equals(mDisplaySlices)) {
			mMgr.userChangedSliceDisplay(mDisplaySlices.getText());
		} else if (ae.getSource().equals(mLUT)) {

		} else if (ae.getSource().equals(mMax1)) {
			mMgr.setMax1(Double.parseDouble(mMax1.getText()));
		} else if (ae.getSource().equals(mMin1)) {
			mMgr.setMin1(Double.parseDouble(mMin1.getText()));
		} else if (ae.getSource().equals(mMax2)) {
			mMgr.setMax2(Double.parseDouble(mMax2.getText()));
		} else if (ae.getSource().equals(mMin2)) {
			mMgr.setMin2(Double.parseDouble(mMin2.getText()));
		} else if (ae.getSource().equals(mInterpolate)) {
			mMgr.userSetInterpolate(mInterpolate.isSelected());
		} else if (ae.getSource().equals(mPeek) && mPeek.isSelected()) {
			mMgr.setViewerMode(OverlayMgr.PEEK);
		} else if (ae.getSource().equals(mOutline) && mOutline.isSelected()) {
			mMgr.setViewerMode(OverlayMgr.OUTLINE);
		} else if (ae.getSource().equals(mErase) && mErase.isSelected()) {
			mMgr.setViewerMode(OverlayMgr.ERASE);
		} else if (ae.getSource().equals(mMoveScaleBar) && mMoveScaleBar.isSelected()) {
			mMgr.setViewerMode(OverlayMgr.MOVE_SCALE);
		} else if (ae.getSource().equals(mMoveLegend) && mMoveLegend.isSelected()) {
			mMgr.setViewerMode(OverlayMgr.MOVE_LEGEND);
		} else if (ae.getSource().equals(mMapOnly) && mMapOnly.isSelected()) {
			mMgr.setViewerMode(OverlayMgr.SHOW_WHOLE_MAP);
		} else if (ae.getSource().equals(mExportButton)) {
			mMgr.sendImagesToIJ();
		}
		syncControls();
	}

	/**
	 * Sliders
	 * 
	 */
	public void stateChanged(ChangeEvent ce) {
		if (ce.getSource().equals(mBgBrightSlider)) {
			mMgr.setBgBrightness(mBgBrightSlider.getValue() / 50f);
		} else if (ce.getSource().equals(mBgContrastSlider)) {
			mMgr.setBgContrast(mBgContrastSlider.getValue());
		} else if (ce.getSource().equals(mMinSlider1) || ce.getSource().equals(mMaxSlider1) || ce.getSource().equals(mMinSlider2)
				|| ce.getSource().equals(mMaxSlider2)) {
			// set slider values
			double x1 = mMgr.getOverlay(0).getMap().getExtremeMin() - 0.2d
					* (mMgr.getOverlay(0).getMap().getExtremeMax() - mMgr.getOverlay(0).getMap().getExtremeMin());

			double x2 = mMgr.getOverlay(0).getMap().getExtremeMax() + 0.2d
					* (mMgr.getOverlay(0).getMap().getExtremeMax() - mMgr.getOverlay(0).getMap().getExtremeMin());
			double increment = (x2 - x1) / 100d;

			if (ce.getSource().equals(mMinSlider1)) {
				mMgr.setMin1(x1 + ((double) mMinSlider1.getValue()) * ((x2 - x1) / 100d));
			} else if (ce.getSource().equals(mMaxSlider1)) {
				mMgr.setMax1(x1 + ((double) mMaxSlider1.getValue()) * ((x2 - x1) / 100d));
			} else if (ce.getSource().equals(mMinSlider2)) {
				mMgr.setMin2(x1 + ((double) mMaxSlider1.getValue()) * ((x2 - x1) / 100d));
			} else if (ce.getSource().equals(mMaxSlider2)) {
				mMgr.setMax2(x1 + ((double) mMaxSlider1.getValue()) * ((x2 - x1) / 100d));
			}

		}
		syncControls();
	}

	public int getBgId() {
		int ret = 0;
		try {
			ret = mBgImageCombo.getSelectedIJId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public int getMapId() {
		int ret = 0;
		try {
			ret = mMapCombo.getSelectedIJId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void windowActivated(WindowEvent we) {
	}

	public void windowClosed(WindowEvent we) {
	}

	public void windowClosing(WindowEvent we) {
		mMgr.exit();
	}

	public void windowDeactivated(WindowEvent we) {
	}

	public void windowDeiconified(WindowEvent we) {
	}

	public void windowIconified(WindowEvent we) {
	}

	public void windowOpened(WindowEvent we) {
	}

	// ----------- helper methods ---------------

	private void setupFrame() {
		setSize(new Dimension(mFrameWidth, mFrameHeight));
		setResizable(true);
		setTitle(Literals.PANEL_TITLE);

		JPanel leftpan = new JPanel();
		JPanel rightpan = new JPanel();
		getContentPane().setLayout(new GridLayout(1, 2));

		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		leftpan.setLayout(gbl);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		// add the background image panel
		JPanel pan1 = setupBgSubpan();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbl.setConstraints(pan1, gbc);
		leftpan.add(pan1);

		// add the contour panel
		JPanel pan4 = setupOverlaySubpan();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbl.setConstraints(pan4, gbc);
		leftpan.add(pan4);

		// add the mode panel
		JPanel pan5 = setupModeSubpan();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 3;
		gbl.setConstraints(pan5, gbc);
		leftpan.add(pan5);

		// add the map panel
		JPanel pan2 = setupMapDispSubpan();
		rightpan.setLayout(new BorderLayout());
		rightpan.add(pan2);

		// add the export button
		mExportButton = new JButton(Literals.EXPORT_BUTTON);
		rightpan.add(mExportButton, BorderLayout.SOUTH);

		// old stuff -
		setupLegendSubpan();
		setupScaleBarSubpan();

		getContentPane().add(leftpan);
		getContentPane().add(rightpan);

		syncControls();
	}

	private JPanel setupModeSubpan() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(5, 1));
		setupBorder(pan, Literals.DISPLAY_SETTINGS_BORDER);

		mPeek = new JRadioButton(Literals.PEEK);
		mOutline = new JRadioButton(Literals.OUTLINE);
		mErase = new JRadioButton(Literals.ERASE);
		mMoveScaleBar = new JRadioButton(Literals.MOVE_SCALE);
		mMoveLegend = new JRadioButton(Literals.MOVE_LEGEND);
		mMapOnly = new JRadioButton(Literals.MAP_ONLY);
		ButtonGroup group2 = new ButtonGroup();
		group2.add(mPeek);
		group2.add(mOutline);
		group2.add(mErase);
		group2.add(mMoveScaleBar);
		group2.add(mMoveLegend);
		group2.add(mMapOnly);
		pan.add(mPeek);
		pan.add(mOutline);
		pan.add(mErase);
		// pan.add(mMoveScaleBar);
		// pan.add(mMoveLegend);
		pan.add(mMapOnly);

		// add the multislice display box
		JPanel subpan = new JPanel();
		subpan.setLayout(new GridLayout(1, 2));
		subpan.add(new JLabel(Literals.DISPLAY_SLICES));
		mDisplaySlices = new JTextField();
		subpan.add(mDisplaySlices);
		pan.add(subpan);

		return pan;
	}

	private JPanel setupBgSubpan() {
		JPanel bgpan = new JPanel();
		setupBorder(bgpan, Literals.BG_PAN_BORDER);
		bgpan.setLayout(new GridLayout(3, 1));

		// image combo
		mBgImageCombo = new IJImageChooser();
		bgpan.add(mBgImageCombo);

		// sliders
		mBgBrightSlider = new JSlider(JSlider.HORIZONTAL);
		mBgContrastSlider = new JSlider(-255, 255, 0);
		bgpan.add(mBgBrightSlider);
		bgpan.add(mBgContrastSlider);
		return bgpan;
	}

	private void setupBorder(JPanel panel, String text) {

		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border title = BorderFactory.createTitledBorder(border, text);
		panel.setBorder(title);
	}

	private JPanel setupOverlaySubpan() {
		JPanel overlaypan = new JPanel();
		setupBorder(overlaypan, Literals.OVERLAY_PAN_BORDER);
		overlaypan.setLayout(new GridLayout(2, 1));

		// setup the list combo
		mMapCombo = new IJImageChooser();
		overlaypan.add(mMapCombo);

		//
		mInterpolate = new JCheckBox();
		JPanel subpan = new JPanel();
		subpan.setLayout(new GridLayout(1, 2));
		subpan.add(new JLabel(Literals.INTERPOLATE_MAP));
		subpan.add(mInterpolate);
		overlaypan.add(subpan);

		return overlaypan;
	}

	private JPanel setupMapDispSubpan() {
		JPanel pan = new JPanel();
		setupBorder(pan, Literals.MAP_DISPLAY_BORDER);
		pan.setLayout(new GridLayout(11, 1));

		// lut1 selector
		JPanel subpan1 = new JPanel();
		subpan1.setLayout(new GridLayout(1, 2));
		subpan1.add(new JLabel(Literals.LUT_CHOOSER));
		mLUTs1 = new JComboBox(mMgr.getLUTNames());
		subpan1.add(mLUTs1);
		pan.add(subpan1);

		// min max 1 selectors
		mMax1 = new JTextField();
		mMin1 = new JTextField();
		JPanel subpan2 = new JPanel();
		JPanel subpan3 = new JPanel();
		subpan2.setLayout(new GridLayout(1, 2));
		subpan3.setLayout(new GridLayout(1, 2));
		mMinSlider1 = new JSlider(JSlider.HORIZONTAL);
		mMaxSlider1 = new JSlider(JSlider.HORIZONTAL);
		subpan2.add(new JLabel(Literals.MAP_MIN));
		subpan2.add(mMin1);
		subpan3.add(new JLabel(Literals.MAP_MAX));
		subpan3.add(mMax1);
		pan.add(subpan2);
		pan.add(mMinSlider1);
		pan.add(subpan3);
		pan.add(mMaxSlider1);

		pan.add(new JLabel(" "));

		// lut2 selector
		JPanel subpan4 = new JPanel();
		subpan4.setLayout(new GridLayout(1, 2));
		subpan4.add(new JLabel(Literals.LUT_CHOOSER));
		mLUTs2 = new JComboBox(mMgr.getLUTNames());
		subpan4.add(mLUTs2);
		pan.add(subpan4);

		// min max 1 selectors
		mMax2 = new JTextField();
		mMin2 = new JTextField();
		JPanel subpan5 = new JPanel();
		JPanel subpan6 = new JPanel();
		subpan5.setLayout(new GridLayout(1, 2));
		subpan6.setLayout(new GridLayout(1, 2));
		mMinSlider2 = new JSlider(JSlider.HORIZONTAL);
		mMaxSlider2 = new JSlider(JSlider.HORIZONTAL);
		subpan5.add(new JLabel(Literals.MAP_MIN));
		subpan5.add(mMin2);
		subpan6.add(new JLabel(Literals.MAP_MAX));
		subpan6.add(mMax2);
		pan.add(subpan5);
		pan.add(mMinSlider2);
		pan.add(subpan6);
		pan.add(mMaxSlider2);

		return pan;
	}

	public void syncControls() {
		// remove the listeners
		mDisplaySlices.removeActionListener(this);

		mBgImageCombo.getComboBox().removeActionListener(this);
		mBgBrightSlider.removeChangeListener(this);
		mBgContrastSlider.removeChangeListener(this);
		mMapCombo.getComboBox().removeActionListener(this);
		// mContour.removeActionListener(this);
		// mLUT.removeActionListener(this);

		mLUTs1.removeActionListener(this);
		mMax1.removeActionListener(this);
		mMin1.removeActionListener(this);
		mMinSlider1.removeChangeListener(this);
		mMaxSlider1.removeChangeListener(this);

		mLUTs2.removeActionListener(this);
		mMax2.removeActionListener(this);
		mMin2.removeActionListener(this);
		mMinSlider2.removeChangeListener(this);
		mMaxSlider2.removeChangeListener(this);

		mInterpolate.removeActionListener(this);
		mExportButton.removeActionListener(this);
		mPeek.removeActionListener(this);
		mOutline.removeActionListener(this);
		mErase.removeActionListener(this);
		mMoveScaleBar.removeActionListener(this);
		mMoveLegend.removeActionListener(this);
		mMapOnly.removeActionListener(this);
		mShowLegend.removeActionListener(this);
		mLegendOnImage.removeActionListener(this);
		mLegendBgColor.removeActionListener(this);
		mScaleSize.removeActionListener(this);
		mScaleBarColor.removeActionListener(this);

		// initialize background panel controls
		if (mMgr.getOverlay(0) == null || mMgr.getOverlay(0).getBackgroundImg() == null) {
			mBgBrightSlider.setEnabled(false);
			mBgContrastSlider.setEnabled(false);
			mDisplaySlices.setEnabled(false);
			mDisplaySlices.setText("- no image selected -");
		} else {
			mBgBrightSlider.setEnabled(true);
			mBgContrastSlider.setEnabled(true);
			mBgBrightSlider.setValue((int) (50 * mMgr.getOverlay(0).getBgBrightness()));
			mBgContrastSlider.setValue((int) (mMgr.getOverlay(0).getBgContrast()));
			mDisplaySlices.setEnabled(true);
			mDisplaySlices.setText(mMgr.getCurrentSliceDisplayString());
		}

		// initialize map panel controls
		if (mMgr.getOverlay(0) == null || mMgr.getOverlay(0).getMap() == null) {
			// mContour.setEnabled(false);
			// mLUT.setEnabled(false);
			mMax1.setEnabled(false);
			mMin1.setEnabled(false);
			mMinSlider1.setEnabled(false);
			mMaxSlider1.setEnabled(false);
			mLUTs1.setEnabled(false);

			mMax2.setEnabled(false);
			mMin2.setEnabled(false);
			mMinSlider2.setEnabled(false);
			mMaxSlider2.setEnabled(false);
			mLUTs2.setEnabled(false);
			mInterpolate.setEnabled(false);
		} else {
			// mContour.setEnabled(false); // until contour functionality is
			// fixed
			// mLUT.setEnabled(true);
			mMax1.setEnabled(true);
			mMin1.setEnabled(true);
			mMinSlider1.setEnabled(true);
			mMaxSlider1.setEnabled(true);
			mLUTs1.setEnabled(true);

			mMax2.setEnabled(true);
			mMin2.setEnabled(true);
			mMinSlider2.setEnabled(true);
			mMaxSlider2.setEnabled(true);
			mLUTs2.setEnabled(true);

			mInterpolate.setEnabled(true);

			// set interpolate map
			if (mMgr.getInterpolated()) {
				mInterpolate.setSelected(true);
			} else {
				mInterpolate.setSelected(false);
			}

			// LUT1
			// set min max text boxes
			DecimalFormat nf = getDecimalFormater("0.0E0#");
			mMax1.setText(nf.format(mMgr.getMax1()));
			mMin1.setText(nf.format(mMgr.getMin1()));

			// set slider values
			double x1 = mMgr.getOverlay(0).getMap().getExtremeMin() - 0.2d
					* (mMgr.getOverlay(0).getMap().getExtremeMax() - mMgr.getOverlay(0).getMap().getExtremeMin());

			double x2 = mMgr.getOverlay(0).getMap().getExtremeMax() + 0.2d
					* (mMgr.getOverlay(0).getMap().getExtremeMax() - mMgr.getOverlay(0).getMap().getExtremeMin());
			double increment = (x2 - x1) / 100d;

			mMinSlider1.setValue((int) ((mMgr.getMin1() - x1) / increment));
			mMaxSlider1.setValue((int) ((mMgr.getMax1() - x1) / increment));
			mLUTs1.setSelectedItem(mMgr.getLUT1Name());

			// LUT2
			// set min max text boxes
			mMax2.setText(nf.format(mMgr.getMax2()));
			mMin2.setText(nf.format(mMgr.getMin2()));

			// set slider values
			x1 = mMgr.getOverlay(0).getMap().getExtremeMin() - 0.2d
					* (mMgr.getOverlay(0).getMap().getExtremeMax() - mMgr.getOverlay(0).getMap().getExtremeMin());

			x2 = mMgr.getOverlay(0).getMap().getExtremeMax() + 0.2d
					* (mMgr.getOverlay(0).getMap().getExtremeMax() - mMgr.getOverlay(0).getMap().getExtremeMin());

			increment = (x2 - x1) / 100d;

			mMinSlider2.setValue((int) ((mMgr.getMin2() - x1) / increment));
			mMaxSlider2.setValue((int) ((mMgr.getMax2() - x1) / increment));
			mLUTs2.setSelectedItem(mMgr.getLUT2Name());

		}

		if (mMgr.getViewerMode() == OverlayMgr.PEEK) {
			mPeek.setSelected(true);
		} else if (mMgr.getViewerMode() == OverlayMgr.OUTLINE) {
			mOutline.setSelected(true);
		} else if (mMgr.getViewerMode() == OverlayMgr.ERASE) {
			mErase.setSelected(true);
		} else if (mMgr.getViewerMode() == OverlayMgr.MOVE_LEGEND) {
			mMoveLegend.setSelected(true);
		} else if (mMgr.getViewerMode() == OverlayMgr.MOVE_SCALE) {
			mMoveScaleBar.setSelected(true);
		} else if (mMgr.getViewerMode() == OverlayMgr.SHOW_WHOLE_MAP) {
			mMapOnly.setSelected(true);
		}

		mShowLegend.setEnabled(false);
		mLegendOnImage.setEnabled(false);
		mLegendBgColor.setEnabled(false);
		mScaleSize.setEnabled(false);
		mScaleBarColor.setEnabled(false);

		// restore listeners
		mDisplaySlices.addActionListener(this);

		mMapOnly.addActionListener(this);
		mBgImageCombo.getComboBox().addActionListener(this);
		mBgBrightSlider.addChangeListener(this);
		mBgContrastSlider.addChangeListener(this);
		mMapCombo.getComboBox().addActionListener(this);
		// mContour.addActionListener(this);
		// mLUT.addActionListener(this);

		mLUTs1.addActionListener(this);
		mMax1.addActionListener(this);
		mMin1.addActionListener(this);
		mMinSlider1.addChangeListener(this);
		mMaxSlider1.addChangeListener(this);

		mLUTs2.addActionListener(this);
		mMax2.addActionListener(this);
		mMin2.addActionListener(this);
		mMinSlider2.addChangeListener(this);
		mMaxSlider2.addChangeListener(this);

		mInterpolate.addActionListener(this);
		mExportButton.addActionListener(this);
		mPeek.addActionListener(this);
		mOutline.addActionListener(this);
		mErase.addActionListener(this);
		mMoveScaleBar.addActionListener(this);
		mMoveLegend.addActionListener(this);
		mShowLegend.addActionListener(this);
		mLegendOnImage.addActionListener(this);
		mLegendBgColor.addActionListener(this);
		mScaleSize.addActionListener(this);
		mScaleBarColor.addActionListener(this);

	}

	private static DecimalFormat getDecimalFormater(String pattern) {
		DecimalFormat instance = (DecimalFormat) NumberFormat.getInstance();
		instance.applyPattern(pattern);
		return instance;
	}

	private String[] getScaleBarLengths() {
		String[] ret = { Literals.NO_SCALEBAR, "1mm", "1um", "1cm" };
		return ret;
	}

	private JPanel setupScaleBarSubpan() {
		JPanel pan = new JPanel();
		pan.setLayout(new GridLayout(2, 1));
		setupBorder(pan, Literals.SCALEBAR_PAN_BORDER);
		mScaleSize = new JComboBox(getScaleBarLengths());
		mScaleBarColor = new JButton(Literals.SCALEBAR_COLOR);
		pan.add(mScaleSize);
		pan.add(mScaleBarColor);
		return pan;
	}

	private JPanel setupLegendSubpan() {
		JPanel pan = new JPanel();
		setupBorder(pan, Literals.LEGEND_PAN_BORDER);
		pan.setLayout(new GridLayout(3, 1));
		mShowLegend = new JCheckBox(Literals.SHOW_LEGEND);
		mLegendOnImage = new JCheckBox(Literals.DOCK_LEGEND);
		mLegendBgColor = new JButton(Literals.LEGEND_BG_COLOR);
		pan.add(mShowLegend);
		pan.add(mLegendOnImage);
		pan.add(mLegendBgColor);
		return pan;

	}

}
