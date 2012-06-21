package kfschmidt.imageoverlay;

/**
 *   The overlay contains the background image, overlaid map and
 *   parameters nec. for the maintenance of these images
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
import java.awt.image.*;
import java.awt.geom.*;
import kfschmidt.ijcommon.IJAdapter;

public class Overlay {
	BufferedImage mBgImage;
	int mBgImageId; // the IJ image id: 0 if no image is set

	LUTHelper mLUTHelper;

	OverlayMap mMap;
	int mOverlayMapId; // the IJ image id: 0 if no map is set

	float mBgBrightness = 1f;
	float mBgContrast = 0f;
	boolean mInterpolateMap = false;
	Area mOutlinedAreas;
	int mImageSlice = 1; // use 1 based slice indexing
	IJAdapter mIJada = new IJAdapter();

	public void setLUTHelper(LUTHelper helper) {
		mLUTHelper = helper;
	}

	public LUTHelper getLUTHelper() {
		return mLUTHelper;
	}

	public void setImageSlice(int slice) {
		mImageSlice = slice;
	}

	public int getImageSlice() {
		return mImageSlice;
	}

	public Area getOutlinedAreas() {
		return mOutlinedAreas;
	}

	public void setOutlinedAreas(Area a) {
		mOutlinedAreas = (Area) a.clone();
	}

	public OverlayMap getMap() {
		return mMap;
	}

	public void setMap(OverlayMap map) {
		mMap = map;
	}

	public int getBackgroundImgId() {
		return mBgImageId;
	}

	public void setBackgroundImgId(int id) {
		mBgImageId = id;
		refreshBgImage();
	}

	public int getMapImgId() {
		return mOverlayMapId;
	}

	public void setMapImgId(int id) {
		mOverlayMapId = id;
		refreshMap();
	}

	public BufferedImage getBackgroundImg() {
		return mBgImage;
	}

	public void setBackgroundImg(BufferedImage bi) {
		mBgImage = bi;
	}

	public boolean getInterpolateMap() {
		return mInterpolateMap;
	}

	public void setInterpolateMap(boolean b) {
		mInterpolateMap = b;
	}

	public void setBgBrightness(float b) {
		mBgBrightness = b;
	}

	public void setBgContrast(float c) {
		mBgContrast = c;
	}

	public float getBgBrightness() {
		return mBgBrightness;
	}

	public float getBgContrast() {
		return mBgContrast;
	}

	public void refreshBgImage() {
		try {
			if (mBgImageId != 0) {
				BufferedImage bi = mIJada.getImageForId(mBgImageId, mImageSlice);
				setBackgroundImg(bi);
			} else {
				setBackgroundImg(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * repaints without reloading map data
	 * 
	 */
	public void repaintMap() {
		OverlayMap map = getMap();
		if (map != null) {
			map.refreshImage();
		}
	}

	/**
	 * reloads map data and repaints
	 */
	public void refreshMap() {
		try {
			if (mOverlayMapId != 0) {
				// recreate the map
				double[][][][] data = mIJada.get4DDataForId(mOverlayMapId);
				if (data != null) {
					OverlayMap om = new OverlayMap(data[0][mImageSlice - 1], mLUTHelper);
					setMap(om);
				}
				data = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
