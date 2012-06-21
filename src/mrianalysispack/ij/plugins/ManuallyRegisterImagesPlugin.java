package mrianalysispack.ij.plugins;

/**
 *
 *   This plugin for ImageJ can be used to manually register two
 *   stacks of images and then save the transformation for use transforming
 *   raw data
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

import mrianalysispack.kfschmidt.ijcommon.*;
import mrianalysispack.kfschmidt.manuallyregisterimages.*;
import ij.plugin.PlugIn;

public class ManuallyRegisterImagesPlugin implements PlugIn {

	public void run(String arg) {
		// instantiate a manager with this ImageSource
		IJAdapter ijad = new IJAdapter();
		ManRegMgr mMgr = new ManRegMgr(ijad, ijad);
		mMgr.showUI();
	}

}
