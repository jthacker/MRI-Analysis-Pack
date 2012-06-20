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
import java.io.File;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.IJ;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JOptionPane;

public class IJAdapter implements ImageSource, ImageSink {

	public static int EACH_SLICE_ALL_TP = 4; // Format code for layout of images
												// in IJ stack such that:
												// slice1_time1,
												// slice1_time2...slice1_timen,
												// slice2_time1
	public static int ALL_SLICE_EACH_TP = 8; // Format code for layout of images
												// in IJ stack such that:
												// slice1_time1,slice2_time1...slice1_time2,
												// slice2_time2

	public static File getWorkingDirectory() {
		String dirstr = System.getProperty("imagej.workingdir");
		// System.out.println("got Working Dir: "+dirstr);
		if (dirstr != null) {
			File f = new File(dirstr);
			if (f.exists())
				return f;
		}
		return null;
	}

	public static void setWorkingDirectory(File f) {
		if (f == null)
			return;
		File file = f;
		if (!file.isDirectory())
			file = new File(f.getParent());
		System.setProperty("imagej.workingdir", file.getPath());
		// System.out.println("set WorkingDir: "+file.getPath());
	}

	// IMAGE SINK API

	/**
	 * Generalized method to accept raw 2d image data and create a 32bit
	 * floating point ImageJ image. data format: double [x][y]
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, double[][] data2d) {
		return -1;
	}

	/**
	 * Generalized method to accept raw 3d image data data format: double
	 * [slice][x][y]
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, double[][][] data3d) {
		// create a new stack of 32 bit floating point pixels
		ImagePlus newimp = NewImage.createFloatImage(name, data3d[0][0].length, data3d[0].length, data3d.length, NewImage.FILL_BLACK);
		ImageStack stack = newimp.getStack();

		// populate the slices of the new stack with the transform
		for (int b = 0; b < stack.getSize(); b++) {
			// set the processor to the 2d data array
			setPixels(stack, data3d[b], b + 1, ImagePlus.GRAY32);
		}

		newimp.setTitle(name);
		newimp.show();
		newimp.updateAndDraw();
		return newimp.getID();
	}

	/**
	 * Generalized method to accept raw 4d (3d + time series) image data data
	 * format: double [repetition][slice][x][y]
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, double[][][][] data4d) {
		// create a new stack of 32 bit floating point pixels
		ImagePlus newimp = NewImage.createFloatImage(name, data4d[0][0].length, data4d[0][0][0].length, data4d.length * data4d[0].length, NewImage.FILL_BLACK);
		ImageStack stack = newimp.getStack();

		// populate the slices of the new stack with the transform
		for (int rep = 0; rep < data4d.length; rep++) {
			for (int slice = 0; slice < data4d[0].length; slice++) {
				// set the processor to the 2d data array
				setPixels(stack, data4d[rep][slice], rep * data4d[0].length + 1 + slice, ImagePlus.GRAY32);
			}
		}

		newimp.setTitle(name);
		newimp.show();

		// set this images dimensions
		int[] dims = new int[4];
		dims[0] = data4d[0][0].length;
		dims[1] = data4d[0][0][0].length;
		dims[2] = data4d[0].length;
		dims[3] = data4d.length;

		setImageDims(newimp.getID(), dims);
		newimp.updateAndDraw();
		newimp.getProcessor().resetMinAndMax();
		return newimp.getID();
	}

	/**
	 * Generalized method to accept BufferedImage data
	 * 
	 * @return An integer representing an image id that can be used to retreive
	 *         this image from an ImageSource
	 */
	public int takeImage(String name, BufferedImage bi) {
		// make sure its a 3byte int
		BufferedImage safe = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = safe.getGraphics();
		while (!g.drawImage(bi, 0, 0, null)) {
		}
		ImagePlus ip = new ImagePlus(name, safe);
		ip.show();
		ip.updateAndDraw();
		return ip.getID();

	}

	// IMAGE SOURCE API

	/**
	 * Returns the type of data contained in the image specified by the id, or
	 * UNDEFINED if the type is unknown, or -1 if the image does not exist
	 */
	public int getImageType(int id) {
		return -1;
	}

	/**
	 * Returns the currently selected depth (or slice) from the image source
	 */
	public int getCurrentSlice(int id) {
		int[] ret = new int[4];
		ImagePlus ip = getImagePlusForId(id);
		// System.out.println("ImagePlus for index: "+id+" is "+ip);
		return ip.getCurrentSlice();
	}

	/**
	 * Sets the number of slices for a particular image in system memory for
	 * retreival later by other plugins [width, height, slices, reps]
	 */
	public void setImageDims(int id, int[] dims) {
		System.setProperty("imagej.imgslicenum." + id, "" + dims[2]);
	}

	public int getSliceLayout(int id) {
		int val = -1;
		String s = System.getProperty("imagej.imgslicelayout." + id);
		if (s != null) {
			val = Integer.parseInt(s);
			if (val == EACH_SLICE_ALL_TP) {
				System.out.println("getSliceLayout is returning EACH_SLICE_ALL_TP");
				return EACH_SLICE_ALL_TP;
			}
		}

		System.out.println("getSliceLayout is returning ALL_SLICE_EACH_TP");
		return ALL_SLICE_EACH_TP;
	}

	/**
	 * Sets the number of slices for a particular image in system memory for
	 * retreival later by other plugins [width, height, slices, reps]
	 */
	public void setSliceLayout(int id, int layout_code) {
		System.setProperty("imagej.imgslicelayout." + id, "" + layout_code);
	}

	/**
	 * Returns an array of the image dimensions in [height, width, depth,
	 * repetitions] format 1 if the image has no dimensionality in that aspect
	 * 
	 */
	public int[] getImageDims(int id) {
		int[] ret = new int[4];
		ImagePlus ip = getImagePlusForId(id);

		if (ip == null)
			return null;

		// set the height/width
		ret[0] = ip.getHeight();
		ret[1] = ip.getWidth();
		ret[2] = 1;
		ret[3] = 1;

		// get the number of anatomical slices, first check the cache
		// in case this is a loaded Stimulate file
		String s = System.getProperty("imagej.imgslicenum." + id);

		if (s != null)
			ret[2] = Integer.parseInt(s);
		else {
			// if nothing, prompt the user if more than one slice
			if (ip.getStackSize() > 1) {
				ret[2] = getDimsFromUser(ip.getStackSize());

				// set the dims in the cache so this happens only once
				setImageDims(id, ret);

				// also set the time series layout
				setSliceLayout(id, getTimeSliceLayoutFromUser());
			}
		}

		ret[3] = ip.getStackSize() / ret[2];
		return ret;
	}

	private int getDimsFromUser(int num_slices) {
		int retval = -1;
		boolean keeptrying = true;
		boolean justfailed = false;
		String val = null;

		String[] msg = new String[2];
		String[] fmsg = new String[2];
		msg[0] = "How many anatomical slices ";
		msg[1] = "are in this image stack? ";

		fmsg[0] = "Try again, enter the number ";
		fmsg[1] = "of anatomical slices. ";

		while (keeptrying) {
			if (!justfailed)
				val = JOptionPane.showInputDialog(null, msg, "" + num_slices);
			else
				val = JOptionPane.showInputDialog(null, fmsg, "" + num_slices);
			try {
				retval = Integer.parseInt(val);
				keeptrying = false;
			} catch (Exception e) {
				keeptrying = true;
				justfailed = true;
			}
		}
		return retval;
	}

	private int getTimeSliceLayoutFromUser() {
		int retval = -1;

		boolean keeptrying = true;
		boolean justfailed = false;
		String val = null;
		String[] msg = new String[3];
		String[] options = new String[2];

		msg[0] = "How are the anatomy & time";
		msg[1] = "slices ordered in the stack?";
		msg[2] = "(Leave as default if unsure)";

		options[0] = "Anat1Time1...AnatNTime1,Anat1Time2...";
		options[1] = "Anat1Time1...Anat1TimeN,Anat2Time1...";

		val = (String) JOptionPane.showInputDialog(null, msg, "Time Series Organization", JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

		if (val.equals(options[1]))
			return EACH_SLICE_ALL_TP;
		else
			return ALL_SLICE_EACH_TP;
	}

	/**
	 * Returns a list of Image names in the same order that the corresponding
	 * IDs will be returned
	 * 
	 */
	public String[] getImageNames() {
		return getOpenWindowList();
	}

	/**
	 * Returns a list of ImageIDs that can be used to retreive images
	 */
	public int[] getImageIds() {
		return getOpenWindowIdList();
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][] get2DDataForIdAndSlice(int idx, int slice) throws Exception {
		double[][] ret = null;
		ImagePlus ip = getImagePlusForId(idx);
		// System.out.println("ImagePlus for index: "+idx+" is "+ip);
		if (ip != null) {
			ImageProcessor iprocessor = ip.getStack().getProcessor(slice);
			int w = ip.getWidth();
			int h = ip.getHeight();
			ret = new double[w][h];
			if (ip.getType() == ImagePlus.GRAY8) {
				byte[] bdata = (byte[]) iprocessor.getPixels();
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						ret[x][y] = (double) (bdata[x + y * w] & 0xff);
					}
				}
			} else if (ip.getType() == ImagePlus.GRAY16) {
				short[] sdata = (short[]) iprocessor.getPixels();
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						ret[x][y] = (double) sdata[x + y * w];
					}
				}
			} else if (ip.getType() == ImagePlus.GRAY32) {
				float[] fdata = (float[]) iprocessor.getPixels();
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						ret[x][y] = (double) fdata[x + y * w];
					}
				}
			} else if (ip.getType() == ImagePlus.COLOR_256 || ip.getType() == ImagePlus.COLOR_RGB) {
				IJ.showMessage("ImageRegistration Problem", "Image for map should be 2D values (greyscale), not color");
			}
		}
		return ret;
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][] get2DDataForIdAndSliceAndRep(int idx, int slice, int rep) throws Exception {

		return null;
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][][] get3DDataForIdAndRep(int idx, int rep) throws Exception {

		return null;
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][] get2DDataForId(int idx) throws Exception {
		double[][] ret = null;
		ImagePlus ip = getImagePlusForId(idx);
		// System.out.println("ImagePlus for index: "+idx+" is "+ip);
		if (ip != null) {
			ImageProcessor iprocessor = ip.getProcessor();
			int w = ip.getWidth();
			int h = ip.getHeight();
			ret = new double[w][h];
			if (ip.getType() == ImagePlus.GRAY8) {
				byte[] bdata = (byte[]) iprocessor.getPixels();
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						ret[x][y] = (double) (bdata[x + y * w] & 0xff);
					}
				}
			} else if (ip.getType() == ImagePlus.GRAY16) {
				short[] sdata = (short[]) iprocessor.getPixels();
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						ret[x][y] = (double) sdata[x + y * w];
					}
				}
			} else if (ip.getType() == ImagePlus.GRAY32) {
				float[] fdata = (float[]) iprocessor.getPixels();
				for (int y = 0; y < h; y++) {
					for (int x = 0; x < w; x++) {
						ret[x][y] = (double) fdata[x + y * w];
					}
				}
			} else if (ip.getType() == ImagePlus.COLOR_256 || ip.getType() == ImagePlus.COLOR_RGB) {
				IJ.showMessage("ImageRegistration Problem", "Image for map should be 2D values (greyscale), not color");
			}
		}
		return ret;
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion
	 */
	public double[][][] get3DDataForId(int idx) throws Exception {
		return null;
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion the returned 4d array
	 * data is in the format: array [repetition][slice][x][y]
	 */
	public double[][][][] get4DDataForId(int idx) throws Exception {
		int[] dims = getImageDims(idx);
		if (dims == null)
			return null;
		double[][][][] ret = new double[dims[3]][dims[2]][dims[1]][dims[0]];
		ImagePlus ip = getImagePlusForId(idx);
		System.out.println("ImagePlus for index: " + idx + " is " + ip);
		if (ip != null) {

			int slicelayout = getSliceLayout(idx);
			for (int rep = 0; rep < ret.length; rep++) {
				for (int slice = 0; slice < ret[0].length; slice++) {

					ImageProcessor iprocessor = null;
					if (slicelayout == EACH_SLICE_ALL_TP) {
						iprocessor = ip.getStack().getProcessor(ret.length * slice + rep + 1);
					} else {
						iprocessor = ip.getStack().getProcessor(ret[0].length * rep + slice + 1);
					}

					int w = ip.getWidth();
					int h = ip.getHeight();
					if (ip.getType() == ImagePlus.GRAY8) {
						byte[] bdata = (byte[]) iprocessor.getPixels();
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								ret[rep][slice][x][y] = (double) (bdata[x + y * w] & 0xff);
							}
						}
					} else if (ip.getType() == ImagePlus.GRAY16) {
						short[] sdata = (short[]) iprocessor.getPixels();
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								ret[rep][slice][x][y] = (double) sdata[x + y * w];
							}
						}
					} else if (ip.getType() == ImagePlus.GRAY32) {
						float[] fdata = (float[]) iprocessor.getPixels();
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								ret[rep][slice][x][y] = (double) fdata[x + y * w];
							}
						}
					} else if (ip.getType() == ImagePlus.COLOR_256 || ip.getType() == ImagePlus.COLOR_RGB) {

					}
				}
			}
		}
		return ret;
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion returns the specified
	 * slice (USE 1 based slice indexing, not zero).
	 */
	public static BufferedImage getImageForId(int idx, int slice) throws Exception {
		// System.out.println("getImageFor("+idx+", "+slice+")");
		BufferedImage bi = null;
		ImagePlus ip = getImagePlusForId(idx);
		if (ip != null) {
			Image i = ip.getStack().getProcessor(slice).createImage();
			while (i.getWidth(null) <= 0 || i.getHeight(null) <= 0) {
			}
			bi = new BufferedImage(i.getWidth(null), i.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics g = bi.getGraphics();
			while (g.drawImage(i, 0, 0, bi.getWidth(), bi.getHeight(), null)) {
			}
			return bi;
		}
		return null;
	}

	/**
	 * Attempts to return the specified image, throws an exception if the image
	 * is not found, or problems surface during conversion returns the currently
	 * visible slice
	 */
	public BufferedImage getImageForId(int idx) throws Exception {
		BufferedImage bi = null;
		ImagePlus ip = getImagePlusForId(idx);
		if (ip != null) {
			Image i = ip.getImage();
			while (i.getWidth(null) <= 0 || i.getHeight(null) <= 0) {
			}
			bi = new BufferedImage(i.getWidth(null), i.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics g = bi.getGraphics();
			while (g.drawImage(i, 0, 0, bi.getWidth(), bi.getHeight(), null)) {
			}
			return bi;
		}
		return null;
	}

	/**
	 * returns the name for a given image id
	 * 
	 */
	public String getNameForImageId(int id) {
		ImagePlus ip = getImagePlusForId(id);
		if (ip == null)
			return null;
		else
			return ip.getTitle();
	}

	// Private helper methods

	private String[] getOpenWindowList() {
		int[] wList = WindowManager.getIDList();
		String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null)
				titles[i] = imp.getTitle();
			else
				titles[i] = "";
		}
		return titles;
	}

	private int[] getOpenWindowIdList() {
		return WindowManager.getIDList();
	}

	private static ImagePlus getImagePlusForId(int idx) {
		ImagePlus retip = WindowManager.getImage(idx);
		// System.out.println("ImagePlus id: "+idx+" object returned: "+retip);
		return retip;
	}

	private void setPixels(ImageStack stack, double[][] data, int slice, int type) {
		int w = data.length;
		int h = data[0].length;
		ImageProcessor ip = stack.getProcessor(slice);
		if (type == ImagePlus.GRAY8) {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					((byte[]) ip.getPixels())[x + y * w] = (byte) data[x][y];
				}
			}
		} else if (type == ImagePlus.GRAY16) {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					((short[]) ip.getPixels())[x + y * w] = (short) data[x][y];
				}
			}
		} else if (type == ImagePlus.GRAY32) {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					((float[]) ip.getPixels())[x + y * w] = (float) data[x][y];
				}
			}
		} else if (type == ImagePlus.COLOR_256 || type == ImagePlus.COLOR_RGB) {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					((int[]) ip.getPixels())[x + y * w] = (int) data[x][y];
				}
			}
		}

	}

	private ImagePlus makeStackBasedOnStack(ImagePlus imp) {
		ImagePlus newimp = null;
		switch (imp.getType()) {
		case ImagePlus.GRAY8:
			newimp = NewImage.createByteImage(imp.getTitle() + "_transformed", imp.getWidth(), imp.getHeight(), imp.getStackSize(), NewImage.FILL_BLACK);
			break;
		case ImagePlus.GRAY16:
			newimp = NewImage.createShortImage(imp.getTitle() + "_transformed", imp.getWidth(), imp.getHeight(), imp.getStackSize(), NewImage.FILL_BLACK);
			break;
		case ImagePlus.GRAY32:
			newimp = NewImage.createFloatImage(imp.getTitle() + "_transformed", imp.getWidth(), imp.getHeight(), imp.getStackSize(), NewImage.FILL_BLACK);
			break;
		case ImagePlus.COLOR_RGB:
			newimp = NewImage.createRGBImage(imp.getTitle() + "_transformed", imp.getWidth(), imp.getHeight(), imp.getStackSize(), NewImage.FILL_BLACK);
			break;
		}
		return newimp;

	}

	public static void openURL(String url) throws Exception {
		ij.plugin.BrowserLauncher.openURL(url);
	}

	public static void showProgress(double prog) {
		IJ.showProgress(prog);
	}

}
