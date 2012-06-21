package mrianalysispack.kfschmidt.glmanalyzer;

/**
 *   
 *   Displays a model overlaid on raw data
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
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;

public class GLMModelPanel extends JPanel implements MouseListener, MouseMotionListener {
	int mCeiling = 1; // for display purposes only - does not affect model
	int mFloor = 0; // for display purposes only - does not affect model
	GLMModel mLastModel;

	int mInternalMargin = 30;
	int mSelectedTransition = -1;
	boolean mUserHasSelectedTransitionStartPoint = false;
	int mLastY;
	int mLastX;

	GLMMGR mMgr;

	public GLMModelPanel(GLMMGR mgr) {
		mMgr = mgr;
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	public void mouseMoved(MouseEvent me) {
	}

	public void mouseDragged(MouseEvent me) {
		mLastY = me.getY();
		mLastX = me.getX();
		if (mLastModel != null && mLastModel.getType() == GLMModel.BOXCAR && mSelectedTransition > -1) {
			// recalibrate ceiling or floor & timepoint
			recalibrateCeilingFloor();
			recalibrateTimePoint();
			repaint();
		}

	}

	public void mousePressed(MouseEvent me) {
		mLastY = me.getY();
		mLastX = me.getX();
		// is there a boxcar model displayed?
		if (mLastModel != null && mLastModel.getType() == GLMModel.BOXCAR) {
			// get nearest transition point
			int plot_width = getWidth() - 2 * mInternalMargin;
			int clicked_timepoint = (int) (mLastModel.getTimePoints() * (me.getX() - mInternalMargin) / plot_width);
			mSelectedTransition = mLastModel.getNearestTransition(clicked_timepoint);
			double test1 = Math.abs(mLastModel.getTransition(mSelectedTransition)[0] - clicked_timepoint);

			double test2 = Math.abs(mLastModel.getTransition(mSelectedTransition)[1] - clicked_timepoint);
			if (test1 < test2) {
				mUserHasSelectedTransitionStartPoint = true;
			} else {
				mUserHasSelectedTransitionStartPoint = false;
			}

			// recalibrate ceiling or floor & timepoint
			recalibrateCeilingFloor();
			recalibrateTimePoint();

			repaint();
		}
	}

	public void mouseClicked(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void mouseReleased(MouseEvent me) {
		mLastY = -1;
		mLastX = -1;
		mSelectedTransition = -1;
	}

	public void mouseEntered(MouseEvent me) {
	}

	private void recalibrateTimePoint() {
		int new_tp = mLastModel.getTimePoints() * (mLastX - mInternalMargin) / (getWidth() - 2 * mInternalMargin);
		int t_start = mLastModel.getTransition(mSelectedTransition)[0];
		int t_stop = mLastModel.getTransition(mSelectedTransition)[1];

		if (mUserHasSelectedTransitionStartPoint) {
			// shift the start point
			t_start = new_tp;
			if (t_start >= t_stop)
				t_stop = t_start + 1;
		} else {
			t_stop = new_tp;
			if (t_stop <= t_start) {
				if (t_stop < 2)
					t_stop = 2;
				t_start = t_stop - 1;
			}
		}
		mLastModel.changeTransition(mSelectedTransition, t_start, t_stop);
	}

	private void recalibrateCeilingFloor() {
		boolean adjust_ceiling = false;

		if ((mUserHasSelectedTransitionStartPoint && mLastModel.getTransition(mSelectedTransition)[2] > 0)
				|| (!mUserHasSelectedTransitionStartPoint && mLastModel.getTransition(mSelectedTransition)[2] == 0)) {
			// adjust ceiling
			mCeiling = getHeight() - mInternalMargin - mLastY;

		} else {
			// adjust floor
			mFloor = getHeight() - mInternalMargin - mLastY;
		}
	}

	public void paint(Graphics g) {
		// paint the background white
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());

		drawData(g2, mInternalMargin);
		drawAxes(g2, mInternalMargin);

		// is a model selected?
		if (mMgr.getCurrentModel() != null) {
			if (mMgr.getCurrentModel() != mLastModel) {
				mLastModel = mMgr.getCurrentModel();
				mFloor = 0;
				mCeiling = getHeight() - 2 * mInternalMargin;
			}
			drawModel(mMgr.getCurrentModel(), g2, mInternalMargin);
		} else {

			mLastModel = null;
			mFloor = 0;
			mCeiling = getHeight() - 2 * mInternalMargin;

		}

		if (mMgr.isFitRunning()) {
			drawProgress(g2, mMgr.getFitProgress());
		}

	}

	private void drawProgress(Graphics2D g2, int pct) {
		g2.setColor(Color.BLACK);
		g2.setFont(new Font("SansSerif", Font.BOLD, 25));
		g2.drawString("" + pct + "%", getWidth() / 2 - 20, 60);
	}

	private void drawAxes(Graphics2D g2, int internal_margin) {
		// draw the y axis
		g2.setColor(Color.BLACK);
		g2.drawLine(internal_margin, internal_margin, internal_margin, getHeight() - internal_margin);

		// draw the x axis
		g2.setColor(Color.BLACK);
		g2.drawLine(internal_margin, getHeight() - internal_margin, getWidth() - internal_margin, getHeight() - internal_margin);

		// draw the labels
		g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
		g2.drawString("+1.5z", internal_margin - 25, internal_margin + 6);
		g2.drawString("mean", internal_margin - 25, getHeight() / 2);
		g2.drawString("-1.5z", internal_margin - 25, getHeight() - internal_margin);

	}

	private void drawData(Graphics2D g2, int internal_margin) {
		// see if we have any sample data
		int[] sample = mMgr.getTimeSeriesSample();
		if (sample != null) {
			int plot_width = getWidth() - 2 * internal_margin;
			int plot_height = getHeight() - 2 * internal_margin;
			int x = 0;
			int y = 0;
			int dot_size = 5;
			g2.setColor(Color.GRAY);

			for (int n = 0; n < sample.length; n++) {
				x = internal_margin + n * plot_width / sample.length;
				y = internal_margin + plot_height / 2 + plot_height * (50 - sample[n]) / 100;
				// System.out.println("x="+x+", y="+y+", sample="+sample[n]);
				g2.fillOval(x, y, dot_size, dot_size);
			}

		}

	}

	private void drawModel(GLMModel model, Graphics2D g2, int internal_margin) {

		// draw lines showing the time series
		if (mMgr.isFitRunning()) {
			g2.setColor(Color.GRAY);
		} else {
			g2.setColor(Color.RED);
		}
		double[][] mdata = model.getModelMatrix();
		int plot_width = getWidth() - 2 * internal_margin;
		int plot_height = mCeiling - mFloor;
		int x1 = 0;
		int y1 = 0;
		int x2 = internal_margin;
		int y2 = getHeight() - internal_margin - mFloor - (int) (mdata[0][0] * (double) plot_height);

		g2.setStroke(new BasicStroke(2));
		for (int a = 1; a < mdata.length; a++) {
			x1 = x2;
			y1 = y2;
			x2 = internal_margin + a * plot_width / mdata.length;
			y2 = getHeight() - internal_margin - mFloor - (int) (mdata[a][0] * (double) plot_height);
			g2.drawLine(x1, y1, x2, y2);
		}

	}

}
