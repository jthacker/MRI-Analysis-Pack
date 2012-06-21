package mrianalysispack.ij.plugins;

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

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.IJ;
import ij.WindowManager;
import ij.process.ImageProcessor;
import ij.plugin.PlugIn;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import mrianalysispack.kfschmidt.ijcommon.*;
import mrianalysispack.kfschmidt.imageoverlay.*;

public class ImageOverlayPlugin implements PlugIn {

	public void run(String arg) {
		// instantiate a manager with this ImageSource
		IJAdapter ijada = new IJAdapter();
		OverlayMgr mMgr = new OverlayMgr(ijada, ijada);
		mMgr.showUI();
		mMgr.showViewer();
	}

}
