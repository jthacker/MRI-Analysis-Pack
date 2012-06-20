package kfschmidt.ijcommon;

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
import java.awt.image.BufferedImage;

public interface ImageSource {
	public static final int RAW2D = 3;
	public static final int RAW3D = 4;
	public static final int RAW4D = 5;
	public static final int IMAGE = 6;
	public static final int UNDEFINED = 0;

	/**
	 * Returns the type of data contained in the image specified by the id, or
	 * UNDEFINED if the type is unknown, or -1 if the image does not exist
	 */
	public int getImageType(int id);

	/**
	 * Returns an array of the image dimensions in [height, width, depth,
	 * repetitions] format zero if the image has no dimensionality in that
	 * aspect
	 * 
	 */
	public int[] getImageDims(int id);

	/**
	 * Returns the currently selected depth (or slice) from the image source
	 */
	public int getCurrentSlice(int id);

	/**
	 * Returns a list of Image names in the same order that the corresponding
	 * IDs will be returned
	 * 
	 */
	public String[] getImageNames();

	/**
	 * Returns a list of ImageIDs that can be used to retreive images
	 */
	public int[] getImageIds();

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][] get2DDataForId(int idx) throws Exception;

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][] get2DDataForIdAndSlice(int idx, int slice) throws Exception;

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][] get2DDataForIdAndSliceAndRep(int idx, int slice, int rep) throws Exception;

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][][] get3DDataForId(int idx) throws Exception;

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][][] get3DDataForIdAndRep(int idx, int rep) throws Exception;

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion the returned 4d array
	 * data is in the format: array [repetition][slice][x][y]
	 */
	public double[][][][] get4DDataForId(int idx) throws Exception;

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public BufferedImage getImageForId(int idx) throws Exception;

}