package mrianalysispack.kfschmidt.imageoverlay;

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
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;

public class OverlayViewer extends JFrame implements WindowListener {
	OverlayViewPort[] mVP;
	OverlayMgr mMgr;
	boolean mIsClosing;
	int mNumViewports = 1;
	JPanel mInternalPanel;

	public OverlayViewer(OverlayMgr manager) {
		mMgr = manager;
		addWindowListener(this);
		repaintAll();
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

	public void closeAll() {
		mIsClosing = true;
		this.hide();
		this.dispose();
	}

	public void setNumberOfViewports(int number_of_viewports) {
		getContentPane().removeAll();
		int width = number_of_viewports * 300;
		if (width < 400)
			width = 400;
		if (width > 1200)
			width = 1200;
		setSize(width, 400);
		mInternalPanel = new JPanel();
		mInternalPanel.setLayout(new GridLayout(1, mNumViewports));
		setTitle(Literals.VIEWER_TITLE);
		int mNumViewports = number_of_viewports;
		mVP = new OverlayViewPort[mNumViewports];
		for (int a = 0; a < mVP.length; a++) {
			mVP[a] = new OverlayViewPort(mMgr, a);
			addKeyListener(mVP[a]);
			mInternalPanel.add(mVP[a]);
		}
		getContentPane().add(mInternalPanel);
		// mInternalPanel.revalidate();
		invalidate();
		validate();
		repaintAll();
	}

	public void repaintAll() {
		if (mMgr.getOverlay(0) == null) {
			getContentPane().removeAll();
			// draw blank screen
			setSize(300, 300);
			mInternalPanel = new JPanel();
			mInternalPanel.setLayout(new GridLayout(1, 1));
			mInternalPanel.add(new JLabel("No viewports opened"));
			getContentPane().add(mInternalPanel);
			// mInternalPanel.revalidate();
			invalidate();
			validate();
		} else {
			// draw the viewports
			if (mMgr.getOverlay(0).getMap() != null) {
				setTitle(Literals.VIEWER_TITLE + ": " + mMgr.getMapName());
			}
			if (!isVisible() && !mIsClosing)
				setVisible(true);
			for (int a = 0; a < mVP.length; a++) {
				mVP[a].mRepaintBuf1 = true;
				mVP[a].repaint();
			}
		}
	}

	public BufferedImage getRGBOverlayImage() {
		// create a BufferedImage similar to the window contents
		BufferedImage bi = new BufferedImage(mInternalPanel.getWidth(), mInternalPanel.getHeight(), BufferedImage.TYPE_INT_RGB);

		Graphics2D g2 = (Graphics2D) bi.getGraphics();
		mInternalPanel.paint(g2);
		return bi;
	}

}
