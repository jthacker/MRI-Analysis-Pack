package mrianalysispack.kfschmidt.manuallyregisterimages;

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
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ImagePickerDialog extends JFrame implements ActionListener {
	ManRegMgr mMgr;
	JComboBox mComboBox;
	int mWidth = 200;
	int mHeight = 100;
	int[] mWinIdList;
	String mMsg;

	public ImagePickerDialog(ManRegMgr mgr) {
		mMgr = mgr;
		setupFrame();
	}

	public ImagePickerDialog(ManRegMgr mgr, String msg) {
		mMsg = msg;
		mMgr = mgr;
		setupFrame();
	}

	public void actionPerformed(ActionEvent ae) {
		// get the image for the index
		int idx = mComboBox.getSelectedIndex();
		String imagename = (String) mComboBox.getSelectedItem();
		closeSelf();
		mMgr.userSelectedMap(imagename, mWinIdList[idx]);
	}

	private void closeSelf() {
		setVisible(false);
		dispose();
	}

	private void setupFrame() {
		setTitle(Literals.IMAGE_PICKER_TITLE);
		setSize(mWidth, mHeight);
		getContentPane().setLayout(new BorderLayout());

		// if the msg is not null, add it at the top
		if (mMsg != null) {
			JTextArea jta = new JTextArea(mMsg, 3, 20);
			jta.setBackground(Color.LIGHT_GRAY);
			getContentPane().add(jta, BorderLayout.NORTH);
		}

		// get the list of open windows
		String[] winlist = mMgr.getImageSource().getImageNames();

		// get the list of cooresponding window ids
		mWinIdList = mMgr.getImageSource().getImageIds();

		// create a jcombo box containing each of these elements
		mComboBox = new JComboBox();
		for (int a = 0; a < winlist.length; a++) {
			mComboBox.addItem(winlist[a]);
		}
		mComboBox.addActionListener(this);

		// add the combobox
		getContentPane().add(mComboBox, BorderLayout.SOUTH);

		// get the correct location and display
		Point location = mMgr.getUI().getLocation();
		location.setLocation(location.getX() + 15, location.getY() + 20);
		setLocation(location);
		setVisible(true);
	}

}