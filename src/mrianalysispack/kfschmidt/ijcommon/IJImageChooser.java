package mrianalysispack.kfschmidt.ijcommon;

/**
 *   Simple JComboBox extension that populates and 
 *   tracks the ImageJ id of selected images
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

import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.event.*;
import java.awt.GridLayout;

public class IJImageChooser extends JPanel {
	int[] mImageIds;
	String[] mImageNames;
	private int mSelectedIdx = -1;
	JComboBox mComboBox;
	static IJAdapter mIJAdapter;

	/**
	 * accessor for event handling
	 */
	public JComboBox getComboBox() {
		return mComboBox;
	}

	public IJImageChooser() {
		mComboBox = new JComboBox();
		if (mIJAdapter == null)
			mIJAdapter = new IJAdapter();
		mComboBox.addItem("- SELECT -");
		try {
			mImageNames = mIJAdapter.getImageNames();
			mImageIds = mIJAdapter.getImageIds();
		} catch (Exception e) {
		}
		if (mImageNames != null) {
			for (int a = 0; a < mImageNames.length; a++) {
				mComboBox.addItem(mImageNames[a]);
			}
		}
		setLayout(new GridLayout(1, 1));
		add(mComboBox);
	}

	public int getSelectedIJId() throws Exception {
		if (mComboBox.getSelectedIndex() > 0) {
			return mImageIds[mComboBox.getSelectedIndex() - 1];
		} else
			throw new Exception("User has not selected an image");
	}

	public void setSelectedIJId(int id) {
		if (id == 0) {
			mComboBox.setSelectedIndex(0);
			return;
		}
		for (int a = 0; a < mImageIds.length; a++) {
			if (id == mImageIds[a]) {
				mComboBox.setSelectedIndex(a + 1);
			}
		}
	}

	public boolean userSelectedImage() {
		if (mComboBox.getSelectedIndex() > 0)
			return true;
		else
			return false;
	}

}
