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
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.*;
import ij.gui.NewImage;
import ij.process.*;
import java.util.*;
import ij.plugin.PlugIn;

public class TimeSeriesTruncator implements PlugIn {

	public void run(String arg) {
		IJAdapter ijada = new IJAdapter();

		// get the selected image
		ImagePlus img = getSelectedImage();

		// if the selected image is available
		if (img != null) {
			int[] dims = ijada.getImageDims(img.getID());

			// 1. prompt the user for the repetitions to delete
			int[] repstodrop = getRepsToRemoveFromUser(dims[3]);

			// 3. remove the slices from the ImageJ image
			int slicelayout = ijada.getSliceLayout(img.getID());
			int width = dims[0];
			int height = dims[1];
			int slices = dims[2];
			int reps = dims[3];

			System.out.println("repstodrop is:");
			for (int n = 0; n < repstodrop.length; n++) {
				System.out.print(" " + repstodrop[n]);
			}

			// work from the back to front so that deleting
			// slices doesn't skew the slice numering
			Arrays.sort(repstodrop);
			int dropslice = 0;
			for (int a = repstodrop.length - 1; a >= 0; a--) {
				for (int s = slices; s > 0; s--) {
					if (slicelayout == IJAdapter.EACH_SLICE_ALL_TP) {
						dropslice = (s - 1) * reps + repstodrop[a];
					} else {
						dropslice = (repstodrop[a] - 1) * slices + s;
					}
					System.out.println("Delete slice: " + dropslice);
					img.getStack().deleteSlice(dropslice);
				}
				reps--;
			}
			img.updateAndRepaintWindow();

			// update the dimensions for this image
			dims[3] = dims[3] - repstodrop.length;
			ijada.setImageDims(img.getID(), dims);
		}
	}

	private String[] getSets(String input) {
		StringTokenizer tok = new StringTokenizer(input, ",");
		Vector v = new Vector();
		while (tok.hasMoreTokens()) {
			v.addElement(tok.nextToken());
		}
		String[] ret = new String[v.size()];
		v.copyInto(ret);
		return ret;
	}

	private int[] getImagesFromSet(String input) {
		StringTokenizer tok = new StringTokenizer(input, "-");
		Vector v = new Vector();
		while (tok.hasMoreTokens()) {
			v.addElement(tok.nextToken());
		}

		int stopint = 0;
		int startint = Integer.parseInt((String) v.elementAt(0));
		if (v.size() > 1)
			stopint = Integer.parseInt((String) v.elementAt(1));
		else
			stopint = startint;
		int[] ret = new int[stopint - startint + 1];

		for (int a = 0; a <= (stopint - startint); a++) {
			ret[a] = startint + a;
		}

		return ret;
	}

	public int[] getRepsToRemoveFromUser(int totalreps) {
		String[] msg = new String[3];
		msg[0] = "Which time points do you want";
		msg[1] = "to remove from the series?";
		msg[2] = "There are " + totalreps + " total repetitions.";
		String ret = JOptionPane.showInputDialog(msg, "1-3");
		String[] sets = getSets(ret);
		Vector v = new Vector();
		int[] repstodiscard;
		for (int a = 0; a < sets.length; a++) {
			repstodiscard = getImagesFromSet(sets[a]);
			for (int b = 0; b < repstodiscard.length; b++) {
				v.add(new Integer(repstodiscard[b]));
			}
		}

		int[] timepoints = new int[v.size()];
		for (int b = 0; b < timepoints.length; b++) {
			timepoints[b] = ((Integer) v.elementAt(b)).intValue();
		}
		return timepoints;
	}

	public ImagePlus getSelectedImage() {
		return WindowManager.getCurrentImage();
	}

}
