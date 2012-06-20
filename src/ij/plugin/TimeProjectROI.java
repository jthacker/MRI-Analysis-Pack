package ij.plugin;

/**
 *
 *   This plugin for ImageJ can be used to produce an image overlay
 *   where regions of a color map are superimposed on a reference image
 *
 *   Please contact me with any feedback, questions or concerns. 
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
import kfschmidt.ijcommon.IJAdapter;
import ij.*;
import ij.process.*;
import ij.measure.*;
import ij.gui.*;
import java.awt.Color;
import ij.plugin.filter.Analyzer;
import java.awt.Rectangle;

public class TimeProjectROI implements PlugIn {

	public void run(String arg) {
		IJAdapter ijada = new IJAdapter();
		ImagePlus ip = IJ.getImage();
		if (ip == null)
			return;
		Roi roi = ip.getRoi();
		int curslice = ip.getCurrentSlice();
		int[] dims = ijada.getImageDims(ip.getID());
		curslice = ((curslice - 1) % dims[2]) + 1;
		// System.out.println("Curslice is: "+curslice
		// +" and dims[3] is:"+dims[3]);
		float[] y = getZAxisProfile(roi, ip, dims[2], curslice);

		if (y != null) {
			float[] x = new float[y.length];
			for (int i = 0; i < x.length; i++)
				x[i] = i + 1;
			Rectangle r = ip.getRoi().getBoundingRect();
			PlotWindow pw = new PlotWindow(ip.getTitle() + "-" + r.x + "-" + r.y, "Repetition", "Mean", x, y);
			pw.setColor(Color.RED);
			pw.draw();
		}
	}

	float[] getZAxisProfile(Roi roi, ImagePlus imp, int anatslices, int sliceoffset) {
		ImageStack stack = imp.getStack();
		int size = stack.getSize();
		float[] values = new float[size / anatslices];
		ImageProcessor mask = imp.getMask();
		Rectangle r = imp.getRoi().getBoundingRect();
		Calibration cal = imp.getCalibration();
		Analyzer analyzer = new Analyzer(imp);
		int measurements = analyzer.getMeasurements();
		boolean showResults = measurements != 0 && measurements != Measurements.LIMIT;
		measurements |= Measurements.MEAN;
		if (showResults) {
			if (!analyzer.resetCounter())
				return null;
		}
		for (int i = 0; i < size / anatslices; i++) {
			ImageProcessor ip = stack.getProcessor(sliceoffset + i * anatslices);
			ip.setMask(mask);
			ip.setRoi(r);
			ImageStatistics stats = ImageStatistics.getStatistics(ip, measurements, cal);
			analyzer.saveResults(stats, roi);
			if (showResults)
				analyzer.displayResults();
			values[i] = (float) stats.mean;
		}
		return values;
	}

}
