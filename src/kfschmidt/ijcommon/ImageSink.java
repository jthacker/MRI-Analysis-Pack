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

public interface ImageSink {

	/**
	 * Generalized method to accept raw 2d image data
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, double[][] data2d);

	/**
	 * Generalized method to accept raw 3d image data
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, double[][][] data3d);

	/**
	 * Generalized method to accept raw 4d (3d + time series) image data
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, double[][][][] data4d);

	/**
	 * Generalized method to accept BufferedImage data
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, BufferedImage bi);

}
