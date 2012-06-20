package ij.plugin;

/**
 * 
 * This plugin for ImageJ can be used to produce an image overlay where regions
 * of a color map are superimposed on a reference image
 * 
 * Please contact me with any feedback, questions or concerns.
 * 
 * @author Karl Schmidt <karl.schmidt@umassmed.edu> This software is provided
 *         for use free of any costs, Be advised that NO guarantee is made
 *         regarding it's quality, and there is no ongoing support for this
 *         codebase.
 * 
 *         (c) Karl Schmidt 2003
 * 
 *         REVISION HISTORY:
 * 
 * 
 */

public class KFSLRGCMRO2Calc implements PlugIn {

	public void run(String arg) {
		new kfschmidt.mricalculations.KFSLRGCMRO2Calc();
	}

}
