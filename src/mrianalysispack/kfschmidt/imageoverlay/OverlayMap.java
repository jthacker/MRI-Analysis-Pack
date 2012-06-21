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
 *   20040509 - split out the legend and lut lookup into sep. classes
 *              in overhaul to support multiple slices
 *
 */
import java.awt.image.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.*;
import java.awt.geom.Area;

public class OverlayMap extends Component {
	BufferedImage mImage;
	BufferedImage mLegend;
	public static final int CONTOUR = 1; // contour mode not implemented
	public static final int LUT = 2; // contour mode not implemented
	double[][] mData;
	int mType;
	double mMaxVal;
	double mMinVal;
	double mExtremeMax;
	double mExtremeMin;
	String mName;
	int mContourLevels = 6; // not yet implmented
	LUTHelper mLUTHelper;

	public OverlayMap(double[][] data, LUTHelper helper) {
		mType = LUT;
		mLUTHelper = helper;
		copyData(data);
		cacheImage();
	}

	public LUTHelper getLUTHelper() {
		return mLUTHelper;
	}

	public void setLUTHelper(LUTHelper helper) {
		mLUTHelper = helper;
		refreshImage();
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
		refreshImage();
	}

	public int getWidth() {
		return mData.length;
	}

	public int getHeight() {
		return mData[0].length;
	}

	public void refreshImage() {
		if (mData != null) {
			if (mType == LUT) {
				int[] pixels = new int[mData.length * mData[0].length];

				for (int a = 0; a < mData[0].length; a++) {
					for (int b = 0; b < mData.length; b++) {
						pixels[mData.length * a + b] = mLUTHelper.getIntColorForValue(mData[b][a]);
					}
				}

				mImage = new BufferedImage(mData.length, mData[0].length, BufferedImage.TYPE_INT_ARGB);
				Graphics g = mImage.getGraphics();
				g.drawImage(createImage(new MemoryImageSource(mData.length, mData[0].length, ColorModel.getRGBdefault(), pixels, 0, mData.length)), 0, 0, null);
			} else if (mType == CONTOUR) {
				// not implemented
			}
		}
	}

	private void cacheImage() {
		setMinMax();
		refreshImage();
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
		mMinVal = mExtremeMin;
		mMaxVal = mExtremeMax;
	}

	public double getExtremeMin() {
		return mExtremeMin;
	}

	public double getExtremeMax() {
		return mExtremeMax;
	}

	public void setMax(double max) {
		mMaxVal = max;
	}

	public void setMin(double min) {
		mMinVal = min;
	}

	public BufferedImage getMapImage() {
		return mImage;
	}

	private void copyData(double[][] data) {
		if (data != null) {
			mData = new double[data.length][data[0].length];
			for (int a = 0; a < mData.length; a++) {
				for (int b = 0; b < mData[0].length; b++) {
					mData[a][b] = data[a][b];
				}
			}
		}
	}

}
