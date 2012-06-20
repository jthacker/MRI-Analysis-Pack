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
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;

public class RegPort extends JPanel {
	ManRegWindow mParent;
	int mBorderWidth = 13;
	AffineTransform mRefToScreenSpace;
	AffineTransform mScreenToRefSpace;
	AffineTransform mSrcToScreenSpace;
	AffineTransform mScreenToSrcSpace;
	AffineTransform mTmpAT;
	BufferedImage mBuffer1;
	boolean mRenderAll = true;
	int mLastWidth;
	int mLastHeight;
	Color mBannerColor = Color.GRAY;
	Font mBannerFont = new Font("Arial", Font.BOLD, 14);
	Color mNoXFormColor = Color.BLUE;
	Font mNoXFormFont = new Font("Arial", Font.BOLD, 25);

	RegPort(ManRegWindow parent) {
		mParent = parent;
	}

	/**
	 * returns an array of double for width/height for the max size of the
	 * original source dimensions that will fit into the screen space while
	 * preserving the original image aspect ratio
	 */
	private double[] fitImageToScreen(int orig_width, int orig_height) {
		double[] ret = new double[2];

		int screenwidth = getWidth();
		int screenheight = getHeight();

		// find the screen rectangle that will contain the bg image,
		// the aspect ratio is determined by the background image
		float img_aspect_ratio = (float) orig_width / (float) orig_height;

		float screen_aspect_ratio = (float) (screenwidth - 2 * mBorderWidth) / (float) (screenheight - 2 * mBorderWidth);

		double img_width = -1;
		double img_height = -1;
		if (img_aspect_ratio > screen_aspect_ratio) {
			// image width will be max screen width
			img_width = (double) (screenwidth - 2 * mBorderWidth);
			img_height = (double) ((float) img_width / img_aspect_ratio);
		} else if (img_aspect_ratio < screen_aspect_ratio) {
			// image height will be max screen height
			img_height = (double) (screenheight - 2 * mBorderWidth);
			img_width = (double) (img_aspect_ratio * (float) img_height);
		} else {
			// image width and height will equal screen max with and height
			img_width = (double) (screenwidth - 2 * mBorderWidth);
			img_height = (double) (screenheight - 2 * mBorderWidth);
		}
		ret[0] = img_width;
		ret[1] = img_height;
		return ret;
	}

	/**
	 * returns the image -> screen transform for the scale factor determined by
	 * the width of the orignal image compared with the width of the source
	 * image. The image is centered on the screen
	 * 
	 */
	private AffineTransform[] getBasicDisplayTransform(double width_on_screen, double height_on_screen, double original_width) {
		AffineTransform[] ret = new AffineTransform[2];
		int screenwidth = getWidth();
		int screenheight = getHeight();

		// setup the bg image transform
		double x_offset = ((double) (screenwidth - 2 * mBorderWidth) - width_on_screen) / 2d + (double) mBorderWidth;
		double y_offset = ((double) (screenheight - 2 * mBorderWidth) - height_on_screen) / 2d + (double) mBorderWidth;
		double scale_factor = width_on_screen / (double) original_width;

		ret[0] = new AffineTransform();
		ret[0].translate(x_offset, y_offset);
		ret[0].scale(scale_factor, scale_factor);
		try {
			ret[1] = ret[0].createInverse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private void initXForm() {
		if (mRefToScreenSpace == null)
			mRefToScreenSpace = new AffineTransform();
		if (mSrcToScreenSpace == null)
			mSrcToScreenSpace = new AffineTransform();

		if (mParent.getMgr().getRefImage() != null) {
			int orig_ref_width = mParent.getMgr().getRefImage().getRendering().getWidth();
			int orig_ref_height = mParent.getMgr().getRefImage().getRendering().getHeight();
			double[] ref_img_dims = fitImageToScreen(orig_ref_width, orig_ref_height);
			AffineTransform[] rXforms = getBasicDisplayTransform(ref_img_dims[0], ref_img_dims[1], (double) orig_ref_width);
			mRefToScreenSpace = rXforms[0];
			mScreenToRefSpace = rXforms[1];
		}

		if (mParent.getMgr().getSourceImage() != null) {
			int orig_src_width = mParent.getMgr().getSourceImage().getRendering().getWidth();
			int orig_src_height = mParent.getMgr().getSourceImage().getRendering().getHeight();
			double[] src_img_dims = fitImageToScreen(orig_src_width, orig_src_height);
			AffineTransform[] sXforms = getBasicDisplayTransform(src_img_dims[0], src_img_dims[1], (double) orig_src_width);
			mSrcToScreenSpace = sXforms[0];
			mScreenToSrcSpace = sXforms[1];
		}

	}

	public Point2D getDisplacementVectorInImageSpace(int x0, int y0, int x1, int y1) {
		Point2D p0 = getScreenCoordsInImageSpace(x0, y0);
		Point2D p1 = getScreenCoordsInImageSpace(x1, y1);
		Point2D ret = new Point2D.Double(p1.getX() - p0.getX(), p1.getY() - p0.getY());
		return ret;
	}

	public Point2D getScreenCoordsInImageSpace(int x, int y) {
		if (mScreenToSrcSpace != null) {
			Point2D.Double ret = new Point2D.Double((double) x, (double) y);
			return mScreenToSrcSpace.transform(ret, ret);
		} else
			return null;
	}

	private void renderBuffer1() {
		if (mBuffer1 == null || mLastWidth != getWidth() || mLastHeight != getHeight()) {
			mBuffer1 = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		}

		// blank background
		Graphics g = mBuffer1.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2 = (Graphics2D) g;

		// init the ref to screenspace xform
		initXForm();

		// draw background image
		RegImage ref_image = mParent.getMgr().getRefImage();
		if (ref_image != null) {
			BufferedImage bg_image = ref_image.getRendering();
			if (bg_image != null) {
				g2.drawImage(bg_image, mRefToScreenSpace, null);
			}
		}
		mLastWidth = getWidth();
		mLastHeight = getHeight();
		mRenderAll = false;
	}

	public void flagCompleteRerender() {
		mRenderAll = true;
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// optionally render the background
		if (mRenderAll || mLastWidth != getWidth() || mLastHeight != getHeight())
			renderBuffer1();

		// copy buffer1 to the screen
		g.drawImage(mBuffer1, 0, 0, getWidth(), getHeight(), null);

		// draw foreground image
		RegImage src_image = mParent.getMgr().getSourceImage();
		if (src_image != null) {
			Composite ac_orig = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
			BufferedImage fg_image = src_image.getRendering();
			if (fg_image != null) {
				if (mTmpAT == null)
					mTmpAT = new AffineTransform();
				mTmpAT.setTransform(mParent.getMgr().getSourceToRefXform());
				mTmpAT.preConcatenate(mSrcToScreenSpace);
				g2.drawImage(fg_image, mTmpAT, null);
			}
			g2.setComposite(ac_orig);
		}

		// draw message banner
		String msg = "";
		if (mParent.getMgr().getUserMode() == ManRegMgr.TRANSLATE) {
			msg = Literals.MSG_TRANSLATE_OP;
		} else if (mParent.getMgr().getUserMode() == ManRegMgr.TRANSLATE) {
			msg = Literals.MSG_TRANSLATE_OP;
		} else if (mParent.getMgr().getUserMode() == ManRegMgr.ROTATE) {
			msg = Literals.MSG_ROTATE_OP;
		} else if (mParent.getMgr().getUserMode() == ManRegMgr.SCALE_UNIFORM) {
			msg = Literals.MSG_SCALE_OP;
		} else if (mParent.getMgr().getUserMode() == ManRegMgr.SCALE_X) {
			msg = Literals.MSG_SCALEX_OP;
		} else if (mParent.getMgr().getUserMode() == ManRegMgr.SCALE_Y) {
			msg = Literals.MSG_SCALEY_OP;
		}
		int curslice = mParent.getMgr().getSlice() + 1;
		int numslices = mParent.getMgr().getSliceCount();

		if (mParent.getMgr().getUserMode() == ManRegMgr.NO_TRANSFORM_PRESENT) {
			drawNoTransform(g2);
		} else if (mParent.getMgr().getUserMode() == ManRegMgr.CALC_IN_PROGRESS) {
			drawCalcInProgress(g2);
		} else {
			drawStatusBanner(g2, msg + " SLICE " + curslice + " OF " + numslices);
		}
	}

	private void drawNoTransform(Graphics2D g2) {
		g2.setColor(mNoXFormColor);
		g2.setFont(mNoXFormFont);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
		g2.drawString(Literals.NO_XFORM_LINE1, (getWidth() / 2) - 70, (getHeight() / 2) - 20);
		g2.drawString(Literals.NO_XFORM_LINE2, (getWidth() / 2) - 70, (getHeight() / 2) + 20);
	}

	private void drawCalcInProgress(Graphics2D g2) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(mNoXFormColor);
		g2.setFont(mNoXFormFont);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
		g2.drawString(mParent.getMgr().getWaitMessage(), (getWidth() / 2) - 70, (getHeight() / 2) - 20);
	}

	private void drawStatusBanner(Graphics2D g2, String msg) {
		g2.setColor(mBannerColor);
		g2.setFont(mBannerFont);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
		g2.drawString(msg, 8, getHeight() - 30);
	}

	public void paint(Graphics g) {
		paintComponent(g);
	}

}