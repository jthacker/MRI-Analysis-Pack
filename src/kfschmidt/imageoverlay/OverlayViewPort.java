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
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;

public class OverlayViewPort extends JPanel implements MouseMotionListener, MouseListener, KeyListener {
	OverlayMgr mManager;
	int mBorderWidth = 1;
	int mLastWidth;
	int mLastHeight;
	int mLastMapWidth;
	int mLastMapHeight;
	int mLastImageWidth;
	int mLastImageHeight;
	int mMouseX;
	int mMouseY;
	BufferedImage mBuffer1;
	BufferedImage mBuffer2;
	BufferedImage mBuffer3;
	RescaleOp mBgScaleOp;
	float mLastBgBrightness;
	float mLastBgContrast;
	int mPortIndex;

	AffineTransform mBgImageToScreenSpace;
	AffineTransform mScreenSpaceToBgImage;
	AffineTransform mMapToScreenSpace;
	boolean mRepaintBuf1;
	boolean mRepaintBuf2;
	boolean mRepaintBuf3;

	GeneralPath mWorkingOutline;
	boolean mWorkingOutlineIsClosed;

	public OverlayViewPort(OverlayMgr manager, int port_index) {
		mPortIndex = port_index;
		mManager = manager;
		setupFrame();
	}

	public int getPortIndex() {
		return mPortIndex;
	}

	public int getMode() {
		return mManager.getDisplayMode();
	}

	protected void setMode(int mode) {
		mManager.setDisplayMode(mode);
		repaint();
	}

	public void setupFrame() {
		addMouseMotionListener(this);
		addMouseListener(this);
		setSize(400, 400);
	}

	public void mouseMoved(MouseEvent e) {
		mMouseX = e.getX();
		mMouseY = e.getY();
		if (getMode() == mManager.PEEK) {
			mRepaintBuf3 = true;
			repaint();
		}

	}

	public void mouseDragged(MouseEvent e) {
		mMouseX = e.getX();
		mMouseY = e.getY();
		if ((getMode() == mManager.OUTLINE || getMode() == mManager.ERASE) && mWorkingOutline != null) {
			mWorkingOutline.lineTo((float) mMouseX, (float) mMouseY);
			mRepaintBuf3 = true;
			repaint();
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		// move the oval off of the visible image so it's not visible on the
		// edge
		mMouseX = -500;
		mMouseY = -500;
		if (getMode() == mManager.PEEK) {
			mRepaintBuf3 = true;
			repaint();
		}
	}

	public void mousePressed(MouseEvent e) {
		mMouseX = e.getX();
		mMouseY = e.getY();

		if (e.getButton() == MouseEvent.BUTTON1) {
			if (getMode() == mManager.OUTLINE || getMode() == mManager.ERASE) {
				mWorkingOutline = new GeneralPath();
				mWorkingOutline.moveTo((float) mMouseX, (float) mMouseY);
				mWorkingOutlineIsClosed = false;

				mRepaintBuf3 = true;
			}
		} else if (e.getButton() == MouseEvent.BUTTON3 && mWorkingOutline != null && mWorkingOutlineIsClosed) {
			mRepaintBuf3 = true;
			if (getMode() == mManager.OUTLINE) {
				addAreaToMask();
			} else if (getMode() == mManager.ERASE) {
				subtractAreaFromMask();
			}
		}
		repaint();
	}

	private void addAreaToMask() {
		Area newarea = new Area(mWorkingOutline);
		newarea.transform(mScreenSpaceToBgImage);
		if (mManager.getOverlay(mPortIndex).getOutlinedAreas() == null) {
			mManager.getOverlay(mPortIndex).setOutlinedAreas(newarea);
		} else {
			mManager.getOverlay(mPortIndex).getOutlinedAreas().add(newarea);
		}
		mWorkingOutline = null;
	}

	private void subtractAreaFromMask() {
		Area newarea = new Area(mWorkingOutline);
		newarea.transform(mScreenSpaceToBgImage);
		if (mManager.getOverlay(mPortIndex).getOutlinedAreas() != null) {
			mManager.getOverlay(mPortIndex).getOutlinedAreas().subtract(newarea);
		}
	}

	public void mouseReleased(MouseEvent e) {
		mMouseX = e.getX();
		mMouseY = e.getY();
		if ((getMode() == mManager.OUTLINE || getMode() == mManager.ERASE) && mWorkingOutline != null) {
			mWorkingOutline.closePath();
			mWorkingOutlineIsClosed = true;
			mRepaintBuf3 = true;
			repaint();
		}
	}

	public void keyTyped(KeyEvent ke) {
	}

	public void keyPressed(KeyEvent ke) {
	}

	public void keyReleased(KeyEvent ke) {
		mRepaintBuf3 = true;
		if (ke.getKeyCode() == KeyEvent.VK_ENTER && mWorkingOutline != null) {
			if (getMode() == mManager.OUTLINE) {
				addAreaToMask();
			} else if (getMode() == mManager.ERASE) {
				subtractAreaFromMask();
			}
		}
		repaint();
	}

	public BufferedImage getCurrentImage() {
		// copy mRepaintBuf3
		BufferedImage tmp = new BufferedImage(mBuffer3.getWidth(), mBuffer3.getHeight(), mBuffer3.getType());
		Graphics gtmp = tmp.getGraphics();
		while (!gtmp.drawImage(mBuffer3, 0, 0, null)) {
		}
		return tmp;
	}

	public void paint(Graphics g) {
		long t1 = System.currentTimeMillis();
		Graphics2D g2 = (Graphics2D) g;

		// check for resize
		checkBuffers();
		checkTransforms();

		long t2 = System.currentTimeMillis();
		// draw buffer1 (background)
		if (mRepaintBuf1) {
			mRepaintBuf1 = false;
			mRepaintBuf2 = true;
			mRepaintBuf3 = true;

			// clear background
			Graphics2D ga = (Graphics2D) mBuffer1.getGraphics();
			ga.setColor(Color.WHITE);
			ga.fillRect(0, 0, getWidth(), getHeight());

			// draw the background image
			if (mManager.getOverlay(mPortIndex) == null || mManager.getOverlay(mPortIndex).getBackgroundImg() == null)
				drawNoImage(ga);

			else {
				drawBgImage(ga, mManager.getOverlay(mPortIndex).getBackgroundImg());
			}
		}
		long t3 = System.currentTimeMillis();
		// draw buffer2 (decorations)
		if (mRepaintBuf2) {
			mRepaintBuf2 = false;
			mRepaintBuf3 = true;
			// copy buffer1 as background
			Graphics2D gb = (Graphics2D) mBuffer2.getGraphics();
			gb.drawImage(mBuffer1, 0, 0, null);

			// draw legend and scale bar

		}
		long t4 = System.currentTimeMillis();
		// draw buffer3 (map overlay)
		if (mRepaintBuf3) {
			mRepaintBuf3 = false;

			// copy buffer2 as background
			Graphics2D gc = (Graphics2D) mBuffer3.getGraphics();
			gc.drawImage(mBuffer2, 0, 0, null);

			// draw the overlay map
			if (mManager.getOverlay(mPortIndex) != null && mManager.getOverlay(mPortIndex).getMap() != null
					&& mManager.getOverlay(mPortIndex).getMap().getMapImage() != null) {
				drawMap(gc, mManager.getOverlay(mPortIndex).getMap().getMapImage());
			}
		}
		long t5 = System.currentTimeMillis();
		// update the screen
		g.drawImage(mBuffer3, 0, 0, null);
		long t6 = System.currentTimeMillis();
	}

	private void checkBuffers() {

		// check if the window has been resized
		if (mBuffer1 == null || getWidth() != mLastWidth || getHeight() != mLastHeight) {
			// System.out.println("init Buffers");
			mRepaintBuf1 = true;
			mRepaintBuf2 = true;
			mRepaintBuf3 = true;
			mBuffer1 = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			mBuffer2 = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			mBuffer3 = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
	}

	private void drawMap(Graphics2D g2, BufferedImage bi) {
		if (mManager.getOverlay(mPortIndex).getInterpolateMap()) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		// potentially draw existing outlined areas
		if (mManager.getOverlay(mPortIndex).getOutlinedAreas() != null && getMode() != mManager.SHOW_WHOLE_MAP) {
			Shape tempshape = null;
			Area tempshape2 = null;
			tempshape = g2.getClip();
			tempshape2 = (Area) mManager.getOverlay(mPortIndex).getOutlinedAreas();
			tempshape2.transform(mBgImageToScreenSpace);
			g2.setClip(mManager.getOverlay(mPortIndex).getOutlinedAreas());
			g2.drawImage(mManager.getOverlay(mPortIndex).getMap().getMapImage(), mMapToScreenSpace, null);
			g2.setClip(tempshape);
			tempshape2.transform(mScreenSpaceToBgImage);
		}

		if (getMode() == mManager.PEEK) {
			double radius = 100d;
			// set the clip and draw the map
			Ellipse2D.Double ellipse = new Ellipse2D.Double(mMouseX - radius / 2d, mMouseY - radius / 2d, radius, radius);
			g2.setClip(ellipse);
			g2.drawImage(mManager.getOverlay(mPortIndex).getMap().getMapImage(), mMapToScreenSpace, null);

		} else if (getMode() == mManager.SHOW_WHOLE_MAP) {
			g2.drawImage(mManager.getOverlay(mPortIndex).getMap().getMapImage(), mMapToScreenSpace, null);
		} else if (getMode() == mManager.OUTLINE || getMode() == mManager.ERASE) {
			// draw the working outline
			if (mWorkingOutline != null) {
				if (mWorkingOutlineIsClosed) {
					// draw the proposed area as an alpha
					Composite orig_comp = g2.getComposite();
					AlphaComposite tmp_comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .6f);
					g2.setComposite(tmp_comp);
					g2.setColor(Color.YELLOW);
					g2.fill(mWorkingOutline);
					g2.setComposite(orig_comp);
				} else {
					g2.setStroke(new BasicStroke(1.5f));
					g2.setColor(Color.PINK);
					g2.draw(mWorkingOutline);
				}
			}
		}
	}

	private void drawBgImage(Graphics2D g, BufferedImage bi) {
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		BufferedImage tmp = new BufferedImage(bi.getWidth(), bi.getHeight(), bi.getType());
		Graphics gtmp = tmp.getGraphics();
		while (!gtmp.drawImage(bi, 0, 0, null)) {
		}
		// check if our image op should be changed
		if (mBgScaleOp == null || mLastBgBrightness != mManager.getOverlay(mPortIndex).getBgBrightness()
				|| mLastBgContrast != mManager.getOverlay(mPortIndex).getBgContrast()) {
			mLastBgBrightness = mManager.getOverlay(mPortIndex).getBgBrightness();
			mLastBgContrast = mManager.getOverlay(mPortIndex).getBgContrast();

			float[] factors = { mLastBgBrightness, mLastBgBrightness, mLastBgBrightness, 1f };
			float[] offsets = { mLastBgContrast, mLastBgContrast, mLastBgContrast, 0f };
			mBgScaleOp = new RescaleOp(factors, offsets, null);
		}

		mBgScaleOp.filter(tmp, tmp);
		g.drawImage(tmp, mBgImageToScreenSpace, null);

		// g.setColor(Color.RED);
		// g.drawString("Port #"+mPortIndex, 20,20);

	}

	private void checkTransforms() {
		// check if the window has been resized
		if (getWidth() != mLastWidth || getHeight() != mLastHeight) {
			// resize and get appropriate params
			mLastWidth = getWidth();
			mLastHeight = getHeight();
			if (mManager.getOverlay(mPortIndex) != null)
				setupTransforms();
		}

		if (mManager.getOverlay(mPortIndex) == null)
			return;

		// check if the bg image dimensions have changed
		if (mManager.getOverlay(mPortIndex).getBackgroundImg() != null
				&& (mManager.getOverlay(mPortIndex).getBackgroundImg().getWidth() != mLastImageWidth || mManager.getOverlay(mPortIndex).getBackgroundImg()
						.getHeight() != mLastImageHeight)) {
			mLastImageWidth = mManager.getOverlay(mPortIndex).getBackgroundImg().getWidth();
			mLastImageHeight = mManager.getOverlay(mPortIndex).getBackgroundImg().getHeight();
			mRepaintBuf1 = true;
			setupTransforms();
		}

		// check if the map dimensions have changed
		if (mManager.getOverlay(mPortIndex).getMap() != null
				&& (mManager.getOverlay(mPortIndex).getMap().getWidth() != mLastMapWidth || mManager.getOverlay(mPortIndex).getMap().getHeight() != mLastMapHeight)) {
			mLastMapWidth = mManager.getOverlay(mPortIndex).getMap().getWidth();
			mLastMapHeight = mManager.getOverlay(mPortIndex).getMap().getHeight();
			mRepaintBuf2 = true;
			setupTransforms();
		}

	}

	private void setupTransforms() {
		// System.out.println("setupTransforms()");

		int screenwidth = getWidth();
		int screenheight = getHeight();

		if (mManager.getOverlay(mPortIndex) == null || mManager.getOverlay(mPortIndex).getBackgroundImg() == null)
			return;

		// find the screen rectangle that will contain the bg image and map,
		// the aspect ratio is determined by the background image, the map is
		// assumed
		// to transform to the same dimensions as the background image
		float img_aspect_ratio = (float) mManager.getOverlay(mPortIndex).getBackgroundImg().getWidth()
				/ (float) mManager.getOverlay(mPortIndex).getBackgroundImg().getHeight();
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

		// setup the bg image transform
		double x_offset = ((double) (screenwidth - 2 * mBorderWidth) - img_width) / 2d + (double) mBorderWidth;
		double y_offset = ((double) (screenheight - 2 * mBorderWidth) - img_height) / 2d + (double) mBorderWidth;
		double scale_factor = (double) img_width / (double) mManager.getOverlay(mPortIndex).getBackgroundImg().getWidth();

		mBgImageToScreenSpace = new AffineTransform();
		mBgImageToScreenSpace.translate(x_offset, y_offset);
		mBgImageToScreenSpace.scale(scale_factor, scale_factor);
		try {
			mScreenSpaceToBgImage = mBgImageToScreenSpace.createInverse();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mManager.getOverlay(mPortIndex).getMap() == null)
			return;

		// setup the map transform
		double scale_factorx = (double) img_width / (double) mManager.getOverlay(mPortIndex).getMap().getWidth();
		double scale_factory = (double) img_height / (double) mManager.getOverlay(mPortIndex).getMap().getHeight();
		mMapToScreenSpace = new AffineTransform();
		mMapToScreenSpace.translate(x_offset, y_offset);
		mMapToScreenSpace.scale(scale_factorx, scale_factory);

	}

	private void drawNoImage(Graphics2D g2) {
		g2.setColor(Color.GRAY);
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		for (int a = 0; a < Literals.NO_IMAGE.length; a++) {
			g2.drawString(Literals.NO_IMAGE[a], (int) (mLastWidth * .1), (int) (a * 20 + (mLastHeight * .1)));
		}
	}

}
