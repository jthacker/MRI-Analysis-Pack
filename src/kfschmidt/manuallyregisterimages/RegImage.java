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
import javax.swing.JComponent;
import java.awt.image.*;

public class RegImage extends JComponent {
	BufferedImage mImage;
	float[][] mData;
	int[] mLUT;
	double mMaxVal;
	double mMinVal;
	double mExtremeMax;
	double mExtremeMin;
	Color mFloorColor = Color.WHITE;
	Color mCeilingColor = Color.BLUE;
	String mName;

	public RegImage(String name, BufferedImage image) {
		mName = name;
		mImage = image;
	}

	public RegImage(String name, float[][] map) {
		mData = map;
		mName = name;
		setMinMax();
		refreshLUT();
		refreshImage();
	}

	public RegImage(String name, double[][] map) {
		this(name, convertDoubleData(map));
	}

	public void setFloorColor(Color color) {
		mFloorColor = color;
		refreshLUT();
		refreshImage();
	}

	public void setCeilingColor(Color color) {
		System.out.println("RegImage.setCeilingColor(" + color + ")");
		mCeilingColor = color;
		refreshLUT();
		refreshImage();
	}

	public String getName() {
		return mName;
	}

	public Color getFloorColor() {
		return mFloorColor;
	}

	public Color getCeilingColr() {
		return mCeilingColor;
	}

	public void setFloorVal(double min) {
		mMinVal = min;
	}

	public void setCielingVal(double max) {
		mMaxVal = max;
	}

	public double getFloorVal() {
		return mMinVal;
	}

	public double getCielingVal() {
		return mMaxVal;
	}

	public float[][] getData() {
		return mData;
	}

	public BufferedImage getRendering() {
		return mImage;
	}

	public double getMaxVal() {
		return mMaxVal;
	}

	public double getMinVal() {
		return mMinVal;
	}

	// ----------------------- Private methods -------------

	private void refreshLUT() {
		buildLUT(mFloorColor, mCeilingColor);
	}

	private static float[][] convertDoubleData(double[][] map) {
		float[][] ret = new float[map.length][map[0].length];
		for (int y = 0; y < map[0].length; y++) {
			for (int x = 0; x < map.length; x++) {
				ret[x][y] = (float) map[x][y];
			}
		}
		return ret;
	}

	/**
	 * populates the lut with even increments from c1 to c2
	 * 
	 */
	private void buildLUT(Color c1, Color c2) {
		System.out.println("initializing LUT");
		mLUT = new int[256];

		float rbase = (float) c1.getRed();
		float rincrement = (c2.getRed() - c1.getRed()) / 255f;

		float gbase = (float) c1.getGreen();
		float gincrement = (c2.getGreen() - c1.getGreen()) / 255f;

		float bbase = (float) c1.getBlue();
		float bincrement = (c2.getBlue() - c1.getBlue()) / 255f;

		for (int n = 0; n < 256; n++) {
			int r = (int) (rbase + n * rincrement);
			int g = (int) (gbase + n * gincrement);
			int b = (int) (bbase + n * bincrement);
			int a = 255;

			mLUT[n] = getIntForChannels(a, r, g, b);
		}
	}

	protected void refreshImage() {
		if (mData != null) {
			int[] pixels = new int[mData.length * mData[0].length];

			for (int a = 0; a < mData[0].length; a++) {
				for (int b = 0; b < mData.length; b++) {
					pixels[mData.length * a + b] = getIntColorForValue(mData[b][a]);
				}
			}

			mImage = new BufferedImage(mData.length, mData[0].length, BufferedImage.TYPE_INT_ARGB);
			Graphics g = mImage.getGraphics();
			g.drawImage(createImage(new MemoryImageSource(mData.length, mData[0].length, ColorModel.getRGBdefault(), pixels, 0, mData.length)), 0, 0, null);
		}
	}

	private Color getColorForValue(double val) {
		return new Color(getIntColorForValue(val), true);
	}

	private int getIntColorForValue(double val) {
		int idx = 0;
		val = val > mMaxVal ? mMaxVal : val;
		val = val < mMinVal ? mMinVal : val;
		double fraction = (val - mMinVal) / (mMaxVal - mMinVal);
		idx = (int) (fraction * mLUT.length);
		if (idx >= mLUT.length) {
			idx = mLUT.length - 1;
		}
		if (idx < 0) {
			idx = 0;
		}
		return mLUT[idx];
	}

	private void setMinMax() {

		for (int a = 0; a < mData.length; a++) {
			for (int b = 0; b < mData[0].length; b++) {
				if (mData[a][b] < mExtremeMin)
					mExtremeMin = mData[a][b];
				if (mData[a][b] > mExtremeMax)
					mExtremeMax = mData[a][b];
			}
		}
		mMinVal = mExtremeMin * 1.3d;
		mMaxVal = mExtremeMax * .7d;
	}

	private int getIntForChannels(int a, int r, int g, int b) {
		int n = 0;
		n = n | (a << 24) | (r << 16) | (g << 8) | b;
		return n;
	}

	private int[] makeIntLUT(int count, int[] a, int[] r, int[] g, int[] b) {
		int[] ret = new int[count];
		for (int n = count - 1; n >= 0; n--) {
			ret[n] = getIntForChannels(a[n], r[n], g[n], b[n]);
		}
		return ret;
	}
}
