package kfschmidt.imageoverlay;

/**
 *   LUTHelper simplifies the process of translating numbers to 
 *   int ARGB colors. Generally a LUTHelper is initialized by
 *   the manager, and shared by overlay maps and the legend
 *
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
import java.awt.image.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.*;
import java.awt.geom.Area;

public class LUTHelper {
	int mClearPixel; // int color for clear pixels

	double mMin1;
	double mMax1;

	double mMin2;
	double mMax2;

	int[] mLUT1;
	int[] mLUT2;

	String mLUT1Name; // maintained for menu display purposes
	String mLUT2Name;

	public LUTHelper() {
		mClearPixel = getClearPixelIntColor(); // this might be configurable in
												// the future
		setLUT1("Orange");
		setLUT2("Blue");
		setMax1(255d);
		setMin1(0d);
		setMax2(0d);
		setMin2(-255d);
	}

	public double getMin1() {
		return mMin1;
	}

	public double getMax1() {
		return mMax1;
	}

	public double getMin2() {
		return mMin2;
	}

	public double getMax2() {
		return mMax2;
	}

	// general rules: min1 >= max2; min1 < max1; min2 < max2
	public void setMin1(double val) {
		mMin1 = val;
		if (mMin1 > mMax1)
			setMax1(val);
		if (mMin1 < mMax2)
			setMax2(val);
	}

	public void setMax1(double val) {
		mMax1 = val;
		if (mMax1 <= mMin1)
			mMax1 = mMin1 + 0.0001d;
	}

	public void setMin2(double val) {
		mMin2 = val;
		if (mMax2 <= mMin2)
			mMin2 = mMax2 - 0.0001d;
	}

	public void setMax2(double val) {
		mMax2 = val;
		if (mMax2 < mMin2)
			setMin2(val - 0.0001d);
		if (mMax2 > mMin1)
			setMin1(val);
	}

	public String getLUT1() {
		return mLUT1Name;
	}

	public String getLUT2() {
		return mLUT2Name;
	}

	public void setLUT1(String name) {
		mLUT1 = getLutForName(name);
		mLUT1Name = name;
	}

	public void setLUT2(String name) {
		mLUT2 = getLutForName(name);
		mLUT2Name = name;
	}

	public int[] getLutForName(String name) {
		// System.out.println("getLUTForName("+name+")");
		int[] reds = new int[256];
		int[] greens = new int[256];
		int[] blues = new int[256];
		int[] alpha = new int[256];

		for (int a = 0; a < 256; a++) {
			alpha[a] = 255;
		}

		int count = 0;
		int[] lut = null;
		if (name.toLowerCase().equals("fire")) {
			count = fire(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("grays")) {
			count = grays(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("primary color")) {
			count = primaryColor(4, reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("ice")) {
			count = ice(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("spectrum")) {
			count = spectrum(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("red green")) {
			count = redGreen(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("orange")) {
			count = orange_to_bright_yellow(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("blue")) {
			count = bright_blue_to_blue(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("orange2")) {
			count = orange2(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else if (name.toLowerCase().equals("blue2")) {
			count = blue2(reds, greens, blues);
			lut = makeIntLUT(count, alpha, reds, greens, blues);
		} else {
			System.out.println("Unknown lut: " + name);
		}
		return lut;
	}

	public int getClearPixelIntColor() {
		int ret = 0;
		ret = 0 | (0 << 24) | (255 << 16) | (255 << 8) | 255;
		return ret;
	}

	public int[] makeIntLUT(int count, int[] a, int[] r, int[] g, int[] b) {
		int[] ret = new int[count];
		for (int n = count - 1; n >= 0; n--) {
			ret[n] = ret[n] | (a[n] << 24) | (r[n] << 16) | (g[n] << 8) | b[n];
		}
		return ret;
	}

	int orange2(int[] reds, int[] greens, int[] blues) {
		Color c;
		float starthue = 0.05f;
		float finishhue = 0.17f;
		float saturation_scale = 500f;
		float sat = 1f;
		float bright = 1f;
		for (int i = 0; i < 256; i++) {
			c = Color.getHSBColor(((float) i / 256f) * (finishhue - starthue) + starthue, sat, bright);
			reds[i] = c.getRed();
			greens[i] = c.getGreen();
			blues[i] = c.getBlue();
		}
		return 256;
	}

	int blue2(int[] reds, int[] greens, int[] blues) {
		Color c;
		float starthue = 0.5f;
		float finishhue = 0.7f;
		float saturation_scale = 1000f;
		float sat = 1f;
		float bright = 1f;
		for (int i = 0; i < 256; i++) {
			c = Color.getHSBColor(((float) i / 256f) * (finishhue - starthue) + starthue, sat, bright);
			reds[i] = c.getRed();
			greens[i] = c.getGreen();
			blues[i] = c.getBlue();
		}
		return 256;
	}

	int orange_to_bright_yellow(int[] reds, int[] greens, int[] blues) {
		Color c;
		float starthue = 0.05f;
		float finishhue = 0.2f;
		float saturation_scale = 500f;
		float sat = 0f;
		float bright = 0f;
		for (int i = 0; i < 256; i++) {
			// drop saturation to zero over last 20% of lut
			// equation of line: sat = 10 - i/25.6
			sat = 5f - (float) i / 50f;
			if (sat > 1)
				sat = 1f;
			if (sat < 0)
				sat = 0f;

			// increase brightness to 1 over first 20% of lut
			// equation of line: bright = i/50
			bright = (float) i / 50f;
			if (bright > 1)
				bright = 1f;
			if (bright < 0)
				bright = 0f;

			c = Color.getHSBColor(((float) i / 256f) * (finishhue - starthue) + starthue, sat, bright);
			reds[i] = c.getRed();
			greens[i] = c.getGreen();
			blues[i] = c.getBlue();
		}
		return 256;
	}

	int bright_blue_to_blue(int[] reds, int[] greens, int[] blues) {
		Color c;
		float starthue = 0.5f;
		float finishhue = 0.7f;
		float saturation_scale = 1000f;
		float sat = 0f;
		float bright = 0f;
		for (int i = 0; i < 256; i++) {
			// increase saturation from 0.5 to 1 over 1st 10% of lut
			// equation of line: sat = i/25.6
			sat = 0.5f + (float) i / 25.6f;
			if (sat > 1)
				sat = 1f;
			if (sat < 0)
				sat = 0f;

			// decrease brightness to 0 over last 20% of lut
			// equation of line: bright = 5-i/50
			bright = 5f - (float) i / 50f;
			if (bright > 1)
				bright = 1f;
			if (bright < 0)
				bright = 0f;

			c = Color.getHSBColor(((float) i / 256f) * (finishhue - starthue) + starthue, sat, bright);
			reds[i] = c.getRed();
			greens[i] = c.getGreen();
			blues[i] = c.getBlue();
		}
		return 256;
	}

	/** THE BELOW LUTS ARE COPIED FROM IMAGEJ SOURCE **/

	int fire(int[] reds, int[] greens, int[] blues) {
		int[] r = { 0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255 };
		int[] g = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255, 255, 255, 255 };
		int[] b = { 31, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223, 255 };
		for (int i = 0; i < r.length; i++) {
			reds[i] = r[i];
			greens[i] = g[i];
			blues[i] = b[i];
		}
		return r.length;
	}

	int grays(int[] reds, int[] greens, int[] blues) {
		for (int i = 0; i < 256; i++) {
			reds[i] = i;
			greens[i] = i;
			blues[i] = i;
		}
		return 256;
	}

	int primaryColor(int color, int[] reds, int[] greens, int[] blues) {
		for (int i = 0; i < 256; i++) {
			if ((color & 4) != 0)
				reds[i] = i;
			if ((color & 2) != 0)
				greens[i] = i;
			if ((color & 1) != 0)
				blues[i] = i;
		}
		return 256;
	}

	int ice(int[] reds, int[] greens, int[] blues) {
		int[] r = { 0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186, 201, 217, 229, 242, 250, 250, 250, 250, 251, 250, 250, 250, 250, 251, 251, 243,
				230 };
		int[] g = { 156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125, 107, 93, 81, 87, 92, 97, 95, 93, 93, 90, 85, 69, 64, 54, 47, 35, 19, 0, 4, 0 };
		int[] b = { 140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246, 250, 251, 250, 250, 245, 230, 230, 222, 202, 180, 163, 142, 123, 114, 106, 94,
				84, 64, 26, 27 };
		for (int i = 0; i < r.length; i++) {
			reds[i] = r[i];
			greens[i] = g[i];
			blues[i] = b[i];
		}
		return r.length;
	}

	int spectrum(int[] reds, int[] greens, int[] blues) {
		Color c;
		for (int i = 0; i < 256; i++) {
			c = Color.getHSBColor((255 - i) / 360f, 1f, 1f);
			reds[i] = c.getRed();
			greens[i] = c.getGreen();
			blues[i] = c.getBlue();
		}
		return 256;
	}

	int redGreen(int[] reds, int[] greens, int[] blues) {
		for (int i = 0; i < 128; i++) {
			reds[i] = (i * 2);
			greens[i] = 0;
			blues[i] = 0;
		}
		for (int i = 128; i < 256; i++) {
			reds[i] = 0;
			greens[i] = (i * 2);
			blues[i] = 0;
		}
		return 256;
	}

	public static String[] getLUTNames() {
		String[] ret = { "Fire", "Grays", "Primary Color", "Ice", "Spectrum", "Red Green", "Orange", "Blue", "Orange2", "Blue2" };
		return ret;
	}

	public Color getColorForValue(double val) {
		return new Color(getIntColorForValue(val), true);
	}

	public int getIntColorForValue(double val) {
		// if the pixel is in the range of either lut, then return the color
		boolean in_lut1 = false;
		boolean in_lut2 = false;

		if (mMin1 <= val && val < mMax1)
			in_lut1 = true;
		if (mMin2 <= val && val < mMax2)
			in_lut2 = true;

		if (in_lut1) {
			int idx = 0;
			double fraction = (val - mMin1) / (mMax1 - mMin1);
			idx = (int) (fraction * mLUT1.length);
			if (idx >= mLUT1.length) {
				idx = mLUT1.length - 1;
			}
			return mLUT1[idx];
		} else if (in_lut2) {
			int idx = 0;
			double fraction = (val - mMin2) / (mMax2 - mMin2);
			idx = (int) (fraction * mLUT1.length);
			if (idx >= mLUT2.length) {
				idx = mLUT2.length - 1;
			}
			return mLUT2[idx];
		} else {
			return mClearPixel;
		}
	}

}
