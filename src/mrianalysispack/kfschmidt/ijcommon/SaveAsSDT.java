package mrianalysispack.kfschmidt.ijcommon;

/**
 *
 *   This plugin is used to output SDT files which can be 
 *   viewed in Stimulate
 *   
 *
 *   Please contact me with any feedback, questions or concerns. 
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
import java.io.*;
import javax.swing.*;

import mrianalysispack.ij.*;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.*;
import ij.gui.NewImage;
import ij.process.*;
import ij.plugin.PlugIn;

public class SaveAsSDT implements PlugIn {

	public ImagePlus copyImageTo32Real(ImagePlus imp) throws Exception {
		ImagePlus retimg = NewImage.createFloatImage(imp.getTitle(), imp.getWidth(), imp.getHeight(), imp.getStack().getSize(), NewImage.FILL_BLACK);

		int pxcount = imp.getWidth() * imp.getHeight();
		IJAdapter ijada = new IJAdapter();
		double[][][][] pxdata = ijada.get4DDataForId(imp.getID());
		int slices = pxdata[0].length;
		int width = pxdata[0][0][0].length;

		for (int rep = 0; rep < pxdata.length; rep++) {
			for (int s = 0; s < pxdata[0].length; s++) {
				float[] destpx = (float[]) retimg.getStack().getProcessor(rep * slices + s + 1).getPixels();

				for (int x = 0; x < pxdata[0][0].length; x++) {
					for (int y = 0; y < pxdata[0][0].length; y++) {
						destpx[y * width + x] = (float) pxdata[rep][s][x][y];
					}
				}
			}
		}

		return retimg;
	}

	public void run(String arg) {
		try {
			// get the selected image
			ImagePlus img = getSelectedImage();

			// if the selected image is available
			if (img != null) {

				// 1. make a 32bit REAL copy of this imageplus
				ImagePlus theimage = copyImageTo32Real(img);

				// 2. display a file chooser
				File sdtfile = getTheSDTFile(theimage);

				// 3. write an SPR file based on the image
				writeSPRFile(sdtfile.getPath(), img);

				// 4. save the image as raw
				FileSaver fs = new FileSaver(theimage);
				if (theimage.getStack().getSize() > 1) {
					fs.saveAsRawStack(sdtfile.getPath());
				} else {
					fs.saveAsRaw(sdtfile.getPath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getSlicesFromUser() {
		String msg = "How many anatomy slices are in your scan geometry?";
		String ret = JOptionPane.showInputDialog(msg, "8");
		int slices = Integer.parseInt(ret);
		return slices;
	}

	public void writeSPRFile(String sdtfilename, ImagePlus ip) {
		String sprfilename = sdtfilename.substring(0, sdtfilename.length() - 3);
		sprfilename += "spr";
		File sprfile = new File(sprfilename);

		// get the dims for the imageid
		IJAdapter ijada = new IJAdapter();
		int dims[] = ijada.getImageDims(ip.getID());
		int width = dims[0];
		int height = dims[1];
		int slices = dims[2];
		int reps = dims[3];

		StringBuffer sb = new StringBuffer(10000);
		sb.append("numDim: ");
		if (reps == 1) {
			sb.append("3");
		} else {
			sb.append("4");
		}
		sb.append("\n");
		sb.append("dim: ");
		sb.append(width + " ");
		sb.append(height + " ");
		sb.append(slices);
		if (reps > 1) {
			sb.append(" " + reps);
		}
		sb.append("\n");
		sb.append("dataType: REAL\n");
		sb.append("sdtOrient:ax\n");

		try {
			FileOutputStream fw = new FileOutputStream(sprfile);
			fw.write(sb.toString().getBytes());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File getTheSDTFile(ImagePlus ip) {
		String filename = ip.getTitle();

		File defaultdir = IJAdapter.getWorkingDirectory();
		if (defaultdir != null) {
			filename = defaultdir.getPath() + "/" + filename;
		}

		if (!filename.endsWith(".sdt")) {
			filename += ".sdt";
		}

		File sdtfile = new File(filename);

		JFileChooser chooser = new JFileChooser(sdtfile.getParentFile());
		chooser.setSelectedFile(sdtfile);
		int returnVal = chooser.showSaveDialog(IJ.getInstance());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			sdtfile = chooser.getSelectedFile();
			if (!sdtfile.getPath().toLowerCase().endsWith(".sdt")) {
				sdtfile = new File(sdtfile.getPath() + ".sdt");
			}
		} else {
			sdtfile = null;
		}

		IJAdapter.setWorkingDirectory(sdtfile);
		return sdtfile;
	}

	public ImagePlus getSelectedImage() {
		return WindowManager.getCurrentImage();
	}

}