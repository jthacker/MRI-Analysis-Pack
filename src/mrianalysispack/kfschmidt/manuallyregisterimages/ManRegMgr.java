package mrianalysispack.kfschmidt.manuallyregisterimages;

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
import java.awt.geom.*;
import java.awt.*;
import java.util.*;
import javax.swing.filechooser.*;
import java.text.*;

import mrianalysispack.kfschmidt.ijcommon.IJAdapter;
import mrianalysispack.kfschmidt.ijcommon.ImageSink;
import mrianalysispack.kfschmidt.ijcommon.ImageSource;

import java.io.File;
import javax.swing.*;

public class ManRegMgr {

	ImagePickerDialog mPicker;
	ImageSource mSource;
	ImageSink mSink;
	ManRegWindow mUI;
	int mUserMenuMode;
	String mWaitMsg;

	public static int USER_IDLE = 0;
	public static int USER_SELECTING_REF = 1;
	public static int USER_SELECTING_SOURCE = 2;
	public static int USER_SELECTING_APPLY = 3;

	public static int TRANSLATE = 10;
	public static int ROTATE = 11;
	public static int SCALE_UNIFORM = 12;
	public static int SCALE_X = 13;
	public static int SCALE_Y = 14;
	public static int NO_OP = 0;
	public static int NO_TRANSFORM_PRESENT = 6;
	public static int CALC_IN_PROGRESS = 7;

	boolean mFirstRefAddition = true;
	boolean mFirstSrcAddition = true;
	int mUserMode = NO_OP;

	TransformRecord mCurrentTransform;

	public ManRegMgr(ImageSource isource, ImageSink isink) {
		mSource = isource;
		mSink = isink;
		mUserMode = NO_TRANSFORM_PRESENT;
	}

	// general Xform functions

	public void copyPrevXForm() {
		mCurrentTransform.copyXFormFromSlice(mCurrentTransform.getCurSlice() - 1);
	}

	public void copyNextXForm() {
		mCurrentTransform.copyXFormFromSlice(mCurrentTransform.getCurSlice() + 1);
	}

	public String getWaitMessage() {
		return mWaitMsg;
	}

	public void incrementSlice() {
		mCurrentTransform.incrementCurSlice();
		setUserOp(ManRegMgr.NO_OP);
		completelyRefreshUI();
	}

	public void decrementSlice() {
		mCurrentTransform.decrementCurSlice();
		setUserOp(ManRegMgr.NO_OP);
		completelyRefreshUI();
	}

	public void openXForm() {
		try {
			// get the file, send it to the reader, set the Xform
			File f = getXFormFile();
			setCurrentTransform(TransformReader.readTransform(f));
			mFirstRefAddition = false;
			mFirstSrcAddition = false;
			mCurrentTransform.setCurSlice(0);
			completelyRefreshUI();
		} catch (Exception e) {
			showError("Problem reading XForm file: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void saveXForm() {
		int getcurslice = mCurrentTransform.getCurSlice();
		// file save dialog
		String xformfilename = mCurrentTransform.getSourceImage().getName() + "_xform.xml";
		File transform_file = new File(xformfilename);
		JFileChooser chooser = new JFileChooser();
		chooser.setSelectedFile(transform_file);
		int returnVal = chooser.showSaveDialog(mUI);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			transform_file = chooser.getSelectedFile();

			// write the transform to the file
			try {
				TransformWriter.writeTransform(mCurrentTransform, transform_file);
			} catch (Exception e) {
				showMessage("Problem writing XForm to file: " + e.getMessage());
				e.printStackTrace();
			}
		}
		mCurrentTransform.setCurSlice(getcurslice);
		mUI.syncControls();
	}

	public void setCurrentTransform(TransformRecord trans) {
		mCurrentTransform = trans;
		if (mCurrentTransform == null)
			mUserMode = NO_TRANSFORM_PRESENT;
		else
			mUserMode = NO_OP;
		mUserMenuMode = USER_IDLE;
		completelyRefreshUI();
	}

	public void newXForm() {
		// prompt user for number of slices
		String inputValue = JOptionPane.showInputDialog(Literals.NEW_XFORM_MSG1);
		int slices = 1;
		try {
			slices = Integer.parseInt(inputValue);
		} catch (Exception e) {
			return;
		}
		TransformRecord trans = new TransformRecord();
		int a = 0;
		for (; a < slices; a++) {
			trans.addSlice();
		}
		for (; a > 0; a--) {
			trans.decrementCurSlice();
		}

		setCurrentTransform(trans);
		mFirstRefAddition = true;
		mFirstSrcAddition = true;
		completelyRefreshUI();
	}

	public void applyXForm() {
		mUserMenuMode = USER_SELECTING_APPLY;
		mPicker = null;
		mPicker = new ImagePickerDialog(this, Literals.APPLY_MSG);
	}

	// multislice functions
	public boolean hasCurrentXForm() {
		if (mCurrentTransform != null)
			return true;
		else
			return false;
	}

	public int getSlice() {
		if (!hasCurrentXForm())
			return -1;
		return mCurrentTransform.getCurSlice();
	}

	public int getSliceCount() {
		if (!hasCurrentXForm())
			return -1;
		return mCurrentTransform.getSliceCount();
	}

	public void setSlice(int a) {
		mCurrentTransform.setCurSlice(a);
		completelyRefreshUI();
	}

	public void addSlice() {
		if (mCurrentTransform != null) {
			mCurrentTransform.addSlice();
			mUserMenuMode = USER_IDLE;
			mUserMode = NO_OP;
			completelyRefreshUI();
		}
	}

	public void delSlice() {
		if (mCurrentTransform != null) {
			mCurrentTransform.delSlice();
			mUserMenuMode = USER_IDLE;
			mUserMode = NO_OP;
			completelyRefreshUI();
		}
	}

	private void repaintUI() {
		if (mUI != null) {
			mUI.flagCompleteRerender();
			mUI.repaint();
		}
	}

	protected void completelyRefreshUI() {
		if (mUI != null) {
			mUI.flagCompleteRerender();
			mUI.repaint();
			mUI.syncControls();
		}
	}

	// slice ops
	public ManRegWindow getUI() {
		return mUI;
	}

	public RegImage getRefImage() {
		if (mCurrentTransform != null)
			return mCurrentTransform.getReferenceImage();
		else
			return null;
	}

	public RegImage getSourceImage() {
		if (mCurrentTransform != null)
			return mCurrentTransform.getSourceImage();
		else
			return null;
	}

	public AffineTransform getSourceToRefXform() {
		if (mCurrentTransform != null)
			return mCurrentTransform.getXForm();
		else
			return null;
	}

	public void showUI() {
		if (mUI == null)
			mUI = new ManRegWindow(this);
		mUI.setVisible(true);
		completelyRefreshUI();
	}

	public void userSelectedMap(String imagename, int imgid) {
		try {
			// depending on last image flagged for update, update the image
			if (mUserMenuMode == USER_SELECTING_REF) {
				// check to see if multiple slices should be applied
				int[] dims = getImageSource().getImageDims(imgid);
				if (dims[2] > 1 && mFirstRefAddition && mCurrentTransform.getSliceCount() > 1) {
					int toaddornot = JOptionPane.showConfirmDialog(null, Literals.MULTISLICE_ADD_MSG, Literals.MULTISLICE_ADD_TITLE, JOptionPane.YES_NO_OPTION);
					if (toaddornot == JOptionPane.OK_OPTION) {
						// add as many slices as possible
						int curslice = getImageSource().getCurrentSlice(imgid);
						for (; mCurrentTransform.getCurSlice() > 0;) {
							mCurrentTransform.decrementCurSlice();
							curslice--;
						}
						if (curslice < 1) {
							// not enough slices in ref img to populate xform
							showError(Literals.MULTISLICE_ADD_ERROR1);
							return;
						}
						for (; mCurrentTransform.getCurSlice() < mCurrentTransform.getSliceCount() - 1; curslice++) {
							RegImage ri = new RegImage(imagename, IJAdapter.getImageForId(imgid, curslice));
							mCurrentTransform.setReferenceImage(ri);
							mCurrentTransform.incrementCurSlice();
						}
						// do the last slice
						RegImage ri = new RegImage(imagename, IJAdapter.getImageForId(imgid, curslice));
						mCurrentTransform.setReferenceImage(ri);
						mCurrentTransform.setCurSlice(0);
					}

				} else {
					RegImage ri = new RegImage(imagename, IJAdapter.getImageForId(imgid, 1));
					mCurrentTransform.setReferenceImage(ri);
				}

				mFirstRefAddition = false;
				mUserMenuMode = USER_IDLE;
			} else if (mUserMenuMode == USER_SELECTING_SOURCE) {
				// check to see if multiple slices should be applied
				int[] dims = getImageSource().getImageDims(imgid);
				if (dims[2] > 1 && mFirstSrcAddition && mCurrentTransform.getSliceCount() > 1) {
					int toaddornot = JOptionPane.showConfirmDialog(null, Literals.MULTISLICE_ADD_MSG, Literals.MULTISLICE_ADD_TITLE, JOptionPane.YES_NO_OPTION);
					if (toaddornot == JOptionPane.OK_OPTION) {
						// add as many slices as possible
						int curslice = getImageSource().getCurrentSlice(imgid);
						for (; mCurrentTransform.getCurSlice() > 0;) {
							mCurrentTransform.decrementCurSlice();
							curslice--;
						}
						if (curslice < 1) {
							// not enough slices in ref img to populate xform
							showError(Literals.MULTISLICE_ADD_ERROR1);
							return;
						}
						for (; mCurrentTransform.getCurSlice() < mCurrentTransform.getSliceCount() - 1; curslice++) {
							RegImage ri = new RegImage(imagename, IJAdapter.getImageForId(imgid, curslice));
							ri.setCeilingColor(Color.RED);
							mCurrentTransform.setSourceImage(ri);
							mCurrentTransform.incrementCurSlice();
						}
						// do the last slice
						RegImage ri = new RegImage(imagename, IJAdapter.getImageForId(imgid, curslice));
						ri.setCeilingColor(Color.RED);
						mCurrentTransform.setSourceImage(ri);
						mCurrentTransform.setCurSlice(0);
					}

				} else {
					RegImage ri = new RegImage(imagename, IJAdapter.getImageForId(imgid, 1));
					ri.setCeilingColor(Color.RED);
					mCurrentTransform.setSourceImage(ri);
				}

				mFirstSrcAddition = false;
				mUserMenuMode = USER_IDLE;
			} else if (mUserMenuMode == USER_SELECTING_APPLY) {
				applyXForm(imagename, imgid);
			}

			completelyRefreshUI();
		} catch (Exception e) {
			showError("ManReg problem! " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void applyXForm(String imgname, int imgid) throws Exception {
		mWaitMsg = "Calculating Transform";
		setUserOp(CALC_IN_PROGRESS);
		ThreadedXFormApplyer calcrunner = new ThreadedXFormApplyer(new IJAdapter(), this, imgid, imgname);
		Thread t = new Thread(calcrunner);
		t.start();
		setUserOp(NO_OP);
	}

	/**
	 * transforms the elements in the data matrix by the affine transform
	 * 
	 */
	public static double[][] transform2DData(double[][] data, AffineTransform at) throws Exception {
		AffineTransform atinv = null;
		try {
			atinv = at.createInverse();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		// no interpolation
		double[][] retdata = new double[data.length][data[0].length];

		Point2D.Float orig_loc = new Point2D.Float(0f, 0f);
		Point2D.Float new_loc = new Point2D.Float(0f, 0f);
		int newx;
		int newy;

		// sample the original image at each of the destination pixels
		for (int y = 0; y < data[0].length; y++) {
			for (int x = 0; x < data.length; x++) {
				orig_loc.setLocation((double) x, (double) y);
				atinv.transform(orig_loc, new_loc);
				// System.out.println("orig_loc:"+orig_loc+" transformed_loc: "+new_loc);
				newx = (int) new_loc.getX();
				newy = (int) new_loc.getY();
				if (newx >= 0 && newx < data.length && newy >= 0 && newy < data[0].length) {
					retdata[x][y] = data[newx][newy];
				}
			}
		}
		return retdata;
	}

	public void showError(String msg) {
		JOptionPane.showMessageDialog(null, msg, "ERROR", JOptionPane.ERROR_MESSAGE);
	}

	public void setUserOp(int mode) {
		mUserMode = mode;
		completelyRefreshUI();
	}

	public void showMessage(String msg) {
		System.out.println(msg);
	}

	public void launchOnlineHelp() {
		try {
			IJAdapter.openURL("http://www.quickvol.com/resources/manregman.pdf");
		} catch (Exception e) {
			showError("Unable to open online help: " + e.getMessage());
		}
	}

	public int getUserMode() {
		return mUserMode;
	}

	/**
	 * drag vector is in reference image pixel space dimensions
	 * 
	 */
	public void userDraggedImage(Point2D drag_vector) {
		if (drag_vector != null) {
			// check our operation mode
			if (mUserMode == NO_OP) {

			} else if (mUserMode == TRANSLATE) {
				mCurrentTransform.translate(drag_vector.getX(), drag_vector.getY());
			} else if (mUserMode == ROTATE) {
				double factor = drag_vector.getY() / (double) mCurrentTransform.getReferenceImage().getRendering().getHeight();

				mCurrentTransform.rotate(2 * Math.PI * factor);
			} else if (mUserMode == SCALE_UNIFORM) {
				double factor = 1d + drag_vector.getX() / (double) mCurrentTransform.getReferenceImage().getRendering().getWidth();
				mCurrentTransform.scale(factor, factor);
			} else if (mUserMode == SCALE_X) {
				double factor = 1d + drag_vector.getX() / (double) mCurrentTransform.getReferenceImage().getRendering().getWidth();
				mCurrentTransform.scale(factor, 1d);
			} else if (mUserMode == SCALE_Y) {
				double factor = 1d + drag_vector.getY() / (double) mCurrentTransform.getReferenceImage().getRendering().getHeight();
				mCurrentTransform.scale(1d, factor);
			}

			// adjust the transform and repaint
			repaintUI();
		}
	}

	public void userNeedsToSetRefImage() {
		mUserMenuMode = USER_SELECTING_REF;
		mPicker = null;
		mPicker = new ImagePickerDialog(this, Literals.PICK_REF);
	}

	public void userNeedsToSetSourceImage() {
		mUserMenuMode = USER_SELECTING_SOURCE;
		mPicker = null;
		mPicker = new ImagePickerDialog(this, Literals.PICK_SRC);
	}

	public ImageSource getImageSource() {
		return mSource;
	}

	public ImageSink getImageSink() {
		return mSink;
	}

	public void exit() {
		// destroy all windows
		mUI.setVisible(false);
		mUI.dispose();
	}

	public void finalize() {
		System.out.println("ManRegMgr.finalize()");
	}

	private File getXFormFile() {
		// show the user a file chooser
		File xformfile = null;
		File defaultdir = (new File("afile")).getParentFile();

		JFileChooser chooser = new JFileChooser(defaultdir);

		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("xml");
		filter.setDescription("Transform Files ");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(ij.IJ.getInstance());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			xformfile = chooser.getSelectedFile();
		}

		return xformfile;
	}

}

/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about.
 * 
 * Extensions are of the type ".foo", which is typically found on Windows and
 * Unix boxes, but not on Macinthosh. Case is ignored.
 * 
 * Example - create a new filter that filerts out all files but gif and jpg
 * image files:
 * 
 * JFileChooser chooser = new JFileChooser(); ExampleFileFilter filter = new
 * ExampleFileFilter( new String{"gif", "jpg"}, "JPEG & GIF Images")
 * chooser.addChoosableFileFilter(filter); chooser.showOpenDialog(this);
 * 
 * @version 1.13 06/13/02
 * @author Jeff Dinkins
 */
class ExampleFileFilter extends FileFilter {

	private static String TYPE_UNKNOWN = "Type Unknown";
	private static String HIDDEN_FILE = "Hidden File";

	private Hashtable filters = null;
	private String description = null;
	private String fullDescription = null;
	private boolean useExtensionsInDescription = true;

	/**
	 * Creates a file filter. If no filters are added, then all files are
	 * accepted.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter() {
		this.filters = new Hashtable();
	}

	/**
	 * Creates a file filter that accepts files with the given extension.
	 * Example: new ExampleFileFilter("jpg");
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String extension) {
		this(extension, null);
	}

	/**
	 * Creates a file filter that accepts the given file type. Example: new
	 * ExampleFileFilter("jpg", "JPEG Image Images");
	 * 
	 * Note that the "." before the extension is not needed. If provided, it
	 * will be ignored.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String extension, String description) {
		this();
		if (extension != null)
			addExtension(extension);
		if (description != null)
			setDescription(description);
	}

	/**
	 * Creates a file filter from the given string array. Example: new
	 * ExampleFileFilter(String {"gif", "jpg"});
	 * 
	 * Note that the "." before the extension is not needed adn will be ignored.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String[] filters) {
		this(filters, null);
	}

	/**
	 * Creates a file filter from the given string array and description.
	 * Example: new ExampleFileFilter(String {"gif", "jpg"},
	 * "Gif and JPG Images");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String[] filters, String description) {
		this();
		for (int i = 0; i < filters.length; i++) {
			// add filters one by one
			addExtension(filters[i]);
		}
		if (description != null)
			setDescription(description);
	}

	/**
	 * Return true if this file should be shown in the directory pane, false if
	 * it shouldn't.
	 * 
	 * Files that begin with "." are ignored.
	 * 
	 * @see #getExtension
	 * @see FileFilter#accepts
	 */
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null && filters.get(getExtension(f)) != null) {
				return true;
			}
			;
		}
		return false;
	}

	/**
	 * Return the extension portion of the file's name .
	 * 
	 * @see #getExtension
	 * @see FileFilter#accept
	 */
	public String getExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			}
			;
		}
		return null;
	}

	/**
	 * Adds a filetype "dot" extension to filter against.
	 * 
	 * For example: the following code will create a filter that filters out all
	 * files except those that end in ".jpg" and ".tif":
	 * 
	 * ExampleFileFilter filter = new ExampleFileFilter();
	 * filter.addExtension("jpg"); filter.addExtension("tif");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 */
	public void addExtension(String extension) {
		if (filters == null) {
			filters = new Hashtable(5);
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/**
	 * Returns the human readable description of this filter. For example:
	 * "JPEG and GIF Image Files (*.jpg, *.gif)"
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 * @see FileFilter#getDescription
	 */
	public String getDescription() {
		if (fullDescription == null) {
			if (description == null || isExtensionListInDescription()) {
				fullDescription = description == null ? "(" : description + " (";
				// build the description from the extension list
				Enumeration extensions = filters.keys();
				if (extensions != null) {
					fullDescription += "." + (String) extensions.nextElement();
					while (extensions.hasMoreElements()) {
						fullDescription += ", ." + (String) extensions.nextElement();
					}
				}
				fullDescription += ")";
			} else {
				fullDescription = description;
			}
		}
		return fullDescription;
	}

	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 */
	public void setDescription(String description) {
		this.description = description;
		fullDescription = null;
	}

	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description.
	 * 
	 * Only relevent if a description was provided in the constructor or using
	 * setDescription();
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see isExtensionListInDescription
	 */
	public void setExtensionListInDescription(boolean b) {
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description.
	 * 
	 * Only relevent if a description was provided in the constructor or using
	 * setDescription();
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see setExtensionListInDescription
	 */
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}
}

class ThreadedXFormApplyer implements Runnable {
	IJAdapter mIjada;
	ManRegMgr mMgr;
	int mImgid;
	String mImgname;

	ThreadedXFormApplyer(IJAdapter adapter, ManRegMgr manager, int imgid, String imgname) {
		mIjada = adapter;
		mMgr = manager;
		mImgid = imgid;
		mImgname = imgname;
	}

	public void run() {
		try {
			int[] dims = mIjada.getImageDims(mImgid);

			if (dims[2] != mMgr.mCurrentTransform.getSliceCount()) {
				// show warning about slice number mismatch
				int keepgoing = JOptionPane.showConfirmDialog(null, Literals.APPLY_WRONG_DIM_MSG + mImgname + "?", Literals.APPLY_WARNING,
						JOptionPane.YES_NO_OPTION);
				if (keepgoing == JOptionPane.NO_OPTION)
					return;
			}

			// get the current 4D array
			mMgr.mWaitMsg = "Copying original image data";
			mMgr.completelyRefreshUI();
			double[][][][] origimg = mIjada.get4DDataForId(mImgid);

			// loop through the slices
			mMgr.mWaitMsg = "Transforming slices";
			mMgr.completelyRefreshUI();
			for (int rep = 0; rep < dims[3]; rep++) {
				for (int slice = 0; slice < dims[2]; slice++) {
					mMgr.mCurrentTransform.setCurSlice(slice);
					origimg[rep][slice] = mMgr.transform2DData(origimg[rep][slice], mMgr.mCurrentTransform.getXForm());
					if (((rep * dims[2] + slice) % 10) == 0)
						mIjada.showProgress(((double) rep * (double) dims[2] + (double) slice) / ((double) dims[3] * (double) dims[2]));
				}
			}

			// send the image back to imagej
			mMgr.mWaitMsg = "Sending the new image to ImageJ";
			mMgr.completelyRefreshUI();
			mMgr.getImageSink().takeImage(mImgname + "_transformed", origimg);
			origimg = null;
		} catch (Exception e) {
			mMgr.showError("Error applying transform: " + e.getMessage());
			e.printStackTrace();
		}
	}

}