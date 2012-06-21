package kfschmidt.manuallyregisterimages;

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
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class ManRegWindow extends JFrame implements ActionListener, WindowListener, MouseInputListener, KeyListener, AdjustmentListener {

	ManRegMgr mMgr;
	int mWidth = 500;
	int mHeight = 500;
	RegPort mPort;
	JMenuBar mMenu;
	JScrollBar mSliceSelector;

	JMenu mFileMenu;
	JMenuItem mNewTransform;
	JMenuItem mOpenTransform;
	JMenuItem mSaveTransform;
	JMenuItem mApplyTransform;

	JMenu mHelpMenu;
	JMenuItem mOnlineHelp;

	JMenu mImagesMenu;
	JMenuItem mSetRefImg;
	JMenuItem mSetSrcImg;
	JMenuItem mDelSlice;
	JMenuItem mAddSlice;

	JMenu mOperationsMenu;
	JMenuItem mOpTrans;
	JMenuItem mOpRot;
	JMenuItem mOpScale;
	JMenuItem mOpScaleX;
	JMenuItem mOpScaleY;
	JMenuItem mNoOp;
	JMenuItem mCopyPrev;
	JMenuItem mCopyNext;

	private boolean mScrollbarIsIn = false;
	int[] mLastXY;

	public ManRegWindow(ManRegMgr parent) {
		mMgr = parent;
		mLastXY = new int[2];
		setupFrame();
	}

	// Public methods

	public void syncControls() {
		// sync the slider
		int slices = mMgr.getSliceCount();
		int curslice = mMgr.getSlice();
		mSliceSelector.removeAdjustmentListener(this);

		if (slices > 1) {
			if (!mScrollbarIsIn) {
				getContentPane().add(mSliceSelector, BorderLayout.SOUTH);
				mScrollbarIsIn = true;
			}
			mSliceSelector.setValues(curslice + 1, 1, 1, slices + 1);
			mSliceSelector.setBlockIncrement(1);

		} else {
			if (mScrollbarIsIn) {
				getContentPane().remove(mSliceSelector);
				mScrollbarIsIn = false;
			}
		}

		mSliceSelector.addAdjustmentListener(this);

		// sync the menus
		if (mMgr.hasCurrentXForm()) {
			// file menu
			mNewTransform.setEnabled(true);
			mOpenTransform.setEnabled(true);
			mSaveTransform.setEnabled(true);
			mApplyTransform.setEnabled(true);

			// Transform (Images) menu
			mSetRefImg.setEnabled(true);
			mSetSrcImg.setEnabled(true);
			mDelSlice.setEnabled(true);
			mAddSlice.setEnabled(true);

			// operations menu
			if (mMgr.getRefImage() != null && mMgr.getSourceImage() != null) {
				mOpTrans.setEnabled(true);
				mOpRot.setEnabled(true);
				mOpScale.setEnabled(true);
				mOpScaleX.setEnabled(true);
				mOpScaleY.setEnabled(true);
				mNoOp.setEnabled(true);
			} else {
				mOpTrans.setEnabled(false);
				mOpRot.setEnabled(false);
				mOpScale.setEnabled(false);
				mOpScaleX.setEnabled(false);
				mOpScaleY.setEnabled(false);
				mNoOp.setEnabled(false);

			}

			if (mMgr.getSlice() > 0) {
				mCopyPrev.setEnabled(true);
			} else {
				mCopyPrev.setEnabled(false);
			}

			if (mMgr.getSlice() < mMgr.getSliceCount() - 1) {
				mCopyNext.setEnabled(true);
			} else {
				mCopyNext.setEnabled(false);
			}

			// help menu
			mOnlineHelp.setEnabled(true);

		} else {
			// file menu
			mNewTransform.setEnabled(true);
			mOpenTransform.setEnabled(true);
			mSaveTransform.setEnabled(false);
			mApplyTransform.setEnabled(false);

			// Transform (Images) menu
			mSetRefImg.setEnabled(false);
			mSetSrcImg.setEnabled(false);
			mDelSlice.setEnabled(false);
			mAddSlice.setEnabled(false);

			// operations menu
			mOpTrans.setEnabled(false);
			mOpRot.setEnabled(false);
			mOpScale.setEnabled(false);
			mOpScaleX.setEnabled(false);
			mOpScaleY.setEnabled(false);
			mNoOp.setEnabled(false);
			mCopyPrev.setEnabled(false);
			mCopyNext.setEnabled(false);

			// help menu
			mOnlineHelp.setEnabled(true);
		}
		getContentPane().doLayout();
		mSliceSelector.doLayout();
		getContentPane().repaint();
	}

	public ManRegMgr getMgr() {
		return mMgr;
	}

	public void flagCompleteRerender() {
		mPort.flagCompleteRerender();
	}

	// event handling

	public void mouseClicked(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
		mLastXY[0] = me.getX();
		mLastXY[1] = me.getY();
	}

	public void mouseReleased(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void mouseDragged(MouseEvent me) {
		// get the drag vector in reference image space and call Mgr.
		mMgr.userDraggedImage(mPort.getDisplacementVectorInImageSpace(mLastXY[0], mLastXY[1], me.getX(), me.getY()));
		mLastXY[0] = me.getX();
		mLastXY[1] = me.getY();
	}

	public void mouseMoved(MouseEvent me) {
	}

	public void adjustmentValueChanged(AdjustmentEvent ae) {
		// handle scroll bar change
		if ((mSliceSelector.getValue() - 1) < mMgr.getSlice()) {
			mMgr.decrementSlice();
		} else if ((mSliceSelector.getValue() - 1) > mMgr.getSlice()) {
			mMgr.incrementSlice();
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		mMgr.exit();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void keyPressed(KeyEvent ke) {
		int x = 0;
		int y = 0;
		if (ke.getKeyCode() == KeyEvent.VK_UP) {
			y = -1;
		} else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
			y = 1;
		} else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
			x = -1;
		} else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
			x = 1;
		}
		// get the drag vector in reference image space and call Mgr.
		mMgr.userDraggedImage(mPort.getDisplacementVectorInImageSpace(0, 0, x, y));
	}

	public void keyReleased(KeyEvent ke) {
	}

	public void keyTyped(KeyEvent ke) {
	}

	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() == mSaveTransform) {
			mMgr.saveXForm();
		} else if (ae.getSource() == mOpenTransform) {
			mMgr.openXForm();
		} else if (ae.getSource() == mCopyPrev) {
			mMgr.copyPrevXForm();
		} else if (ae.getSource() == mOnlineHelp) {
			mMgr.launchOnlineHelp();
		} else if (ae.getSource() == mCopyNext) {
			mMgr.copyNextXForm();
		} else if (ae.getSource() == mNewTransform) {
			mMgr.newXForm();
		} else if (ae.getSource() == mApplyTransform) {
			mMgr.applyXForm();
		} else if (ae.getSource() == mSetRefImg) {
			mMgr.userNeedsToSetRefImage();
		} else if (ae.getSource() == mSetSrcImg) {
			mMgr.userNeedsToSetSourceImage();
		} else if (ae.getSource() == mOpTrans) {
			mMgr.setUserOp(ManRegMgr.TRANSLATE);
		} else if (ae.getSource() == mOpRot) {
			mMgr.setUserOp(ManRegMgr.ROTATE);
		} else if (ae.getSource() == mOpScale) {
			mMgr.setUserOp(ManRegMgr.SCALE_UNIFORM);
		} else if (ae.getSource() == mOpScaleX) {
			mMgr.setUserOp(ManRegMgr.SCALE_X);
		} else if (ae.getSource() == mOpScaleY) {
			mMgr.setUserOp(ManRegMgr.SCALE_Y);
		} else if (ae.getSource() == mNoOp) {
			mMgr.setUserOp(ManRegMgr.NO_OP);
		} else if (ae.getSource() == mAddSlice) {
			mMgr.addSlice();
		} else if (ae.getSource() == mDelSlice) {
			mMgr.delSlice();
		}

		mPort.repaint();
	}

	// ------- private methods --------------

	private void setupFrame() {
		setTitle(Literals.WINDOW_TITLE);
		setSize(mWidth, mHeight);
		getContentPane().setLayout(new BorderLayout());

		// add the menu
		setupMenu();

		// add the regport
		setupPort();
		getContentPane().add(mPort);

		// add the slice selector
		setupSlider();

		addWindowListener(this);
	}

	private void setupSlider() {
		mSliceSelector = new JScrollBar(JScrollBar.HORIZONTAL);
	}

	private void setupPort() {
		mPort = new RegPort(this);
		mPort.addMouseListener(this);
		mPort.addMouseMotionListener(this);
		addKeyListener(this);
	}

	private void setupMenu() {
		mMenu = new JMenuBar();

		// file menu
		mFileMenu = new JMenu(Literals.FILE_MENU);
		mNewTransform = new JMenuItem(Literals.NEW_XFORM);
		mOpenTransform = new JMenuItem(Literals.OPEN_XFORM);
		mSaveTransform = new JMenuItem(Literals.SAVE_TRANSFORM);
		mApplyTransform = new JMenuItem(Literals.APPLY_XFORM);
		mNewTransform.addActionListener(this);
		mOpenTransform.addActionListener(this);
		mSaveTransform.addActionListener(this);
		mApplyTransform.addActionListener(this);
		mFileMenu.add(mNewTransform);
		mFileMenu.add(mOpenTransform);
		mFileMenu.add(mSaveTransform);
		mFileMenu.addSeparator();
		mFileMenu.add(mApplyTransform);

		// Transform (Images) menu
		mImagesMenu = new JMenu(Literals.IMAGES_MENU);
		mSetRefImg = new JMenuItem(Literals.SET_REF_IMG);
		mSetSrcImg = new JMenuItem(Literals.SET_SRC_IMG);
		mDelSlice = new JMenuItem(Literals.DELETE_SLICE);
		mAddSlice = new JMenuItem(Literals.ADD_SLICE);
		mImagesMenu.add(mSetRefImg);
		mImagesMenu.add(mSetSrcImg);
		mImagesMenu.addSeparator();
		mImagesMenu.add(mDelSlice);
		mImagesMenu.add(mAddSlice);
		mSetRefImg.addActionListener(this);
		mSetSrcImg.addActionListener(this);
		mDelSlice.addActionListener(this);
		mAddSlice.addActionListener(this);

		// operations menu
		mOperationsMenu = new JMenu(Literals.OPS_MENU);
		mOpTrans = new JMenuItem(Literals.TRANSLATE_OP);
		mOpRot = new JMenuItem(Literals.ROTATE_OP);
		mOpScale = new JMenuItem(Literals.SCALE_OP);
		mOpScaleX = new JMenuItem(Literals.SCALE_X);
		mOpScaleY = new JMenuItem(Literals.SCALE_Y);
		mNoOp = new JMenuItem(Literals.NO_OP);
		mCopyPrev = new JMenuItem(Literals.COPY_XFORM_PREV);
		mCopyNext = new JMenuItem(Literals.COPY_XFORM_NEXT);
		mOperationsMenu.add(mOpTrans);
		mOperationsMenu.add(mOpRot);
		mOperationsMenu.add(mOpScale);
		mOperationsMenu.add(mOpScaleX);
		mOperationsMenu.add(mOpScaleY);
		mOperationsMenu.add(mNoOp);
		mOperationsMenu.addSeparator();
		mOperationsMenu.add(mCopyPrev);
		mOperationsMenu.add(mCopyNext);
		mOpTrans.addActionListener(this);
		mOpRot.addActionListener(this);
		mOpScale.addActionListener(this);
		mOpScaleX.addActionListener(this);
		mOpScaleY.addActionListener(this);
		mNoOp.addActionListener(this);
		mCopyPrev.addActionListener(this);
		mCopyNext.addActionListener(this);

		// help menu
		mHelpMenu = new JMenu(Literals.HELP_MENU);
		mOnlineHelp = new JMenuItem(Literals.HELP_ONLINE);
		mOnlineHelp.addActionListener(this);
		mHelpMenu.add(mOnlineHelp);

		// add the submenus to the main menu
		mMenu.add(mFileMenu);
		mMenu.add(mImagesMenu);
		mMenu.add(mOperationsMenu);
		mMenu.add(mHelpMenu);
		setJMenuBar(mMenu);

	}

}