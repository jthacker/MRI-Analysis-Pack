package kfschmidt.imageoverlay;

/**
 *   Legend creates a buffered image with a legend
 *   corresponding to the LUT used in an overlay map
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

public class Legend {

	BufferedImage mLegend;
	LUTHelper mLUTHelper;
	int mWidth;
	int mHeight;
	int mHatchNum = 6;
	int mHatchLength = 10;
	float mBarWidth = 0.3f;
	int mMargin = 10;

	public Legend(int width, int height, LUTHelper helper) {
		mWidth = width;
		mHeight = height;
		setLUTHelper(helper);
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int width) {
		mWidth = width;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int height) {
		mHeight = height;
	}

	public void setNumHatches(int hatches) {
		mHatchNum = hatches;
	}

	public void setBarWidth(float barwidth) {
		mBarWidth = barwidth;
	}

	public void setMargin(int margin) {
		mMargin = margin;
	}

	public void setLUTHelper(LUTHelper helper) {
		mLUTHelper = helper;
		refreshLegend();
	}

	public BufferedImage getLegend() {
		return mLegend;
	}

	protected void drawLUTFilledBox(Graphics2D g2, double max, double min, int[] lut, Rectangle box_area) {

		// draw the box for the legend
		g2.setColor(Color.BLACK);
		int barwidth = (int) (mBarWidth * box_area.width);
		int barleftx = box_area.x + box_area.width - barwidth;

		g2.fillRect(barleftx, box_area.y, barwidth, box_area.height + 1);

		// draw hatch marks and values
		int dy_per_hatch = box_area.height / mHatchNum;

		// draw top and bottom hatches
		int y;
		for (int a = 0; a <= mHatchNum; a++) {
			y = box_area.y + a * dy_per_hatch;
			g2.drawLine(barleftx - mHatchLength, y, barleftx, y);

			// format the number
			DecimalFormat nf = getDecimalFormater("0.0E0#");
			String str = nf.format(valueForY(y - box_area.y, box_area.height, max, min));
			g2.setFont(new Font("Arial", Font.BOLD, 12));
			g2.drawString(str, barleftx - mHatchLength - 45, y + 5);
		}

		// fill the bar
		for (y = box_area.y + 1; y - box_area.y < box_area.height; y++) {
			// set the color
			double valfory = valueForY(y - box_area.y, box_area.height, max, min);
			g2.setColor(mLUTHelper.getColorForValue(valfory));
			g2.drawLine(barleftx + 1, y, barleftx + barwidth - 2, y);

		}
	}

	protected void refreshLegend() {
		int legwidth = mWidth;
		int legheight = mHeight;
		float barwidthpercent = mBarWidth;
		int margin = mMargin;

		// create a clean canvas
		mLegend = new BufferedImage(legwidth, legheight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) mLegend.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, legwidth, legheight);

		// get a height which is integer multiple of numhatches so top
		// and bottom hatches draw properly
		int internal_box_height = mHatchNum * ((legheight / 2 - 2 * mMargin) / mHatchNum);

		drawLUTFilledBox(g2, mLUTHelper.getMax1(), mLUTHelper.getMin1(), mLUTHelper.getLutForName(mLUTHelper.getLUT1()), new Rectangle(mMargin, mMargin,
				legwidth - 2 * mMargin, internal_box_height));

		drawLUTFilledBox(g2, mLUTHelper.getMax2(), mLUTHelper.getMin2(), mLUTHelper.getLutForName(mLUTHelper.getLUT2()), new Rectangle(mMargin, 2 * mMargin
				+ internal_box_height + 10, legwidth - 2 * mMargin, internal_box_height));

	}

	private double valueForY(int dy, int barheight, double max, double min) {
		return (((double) barheight - (double) dy) / (double) barheight) * (max - min) + min;
	}

	private static DecimalFormat getDecimalFormater(String pattern) {
		DecimalFormat instance = (DecimalFormat) NumberFormat.getInstance();
		instance.applyPattern(pattern);
		return instance;
	}

}
