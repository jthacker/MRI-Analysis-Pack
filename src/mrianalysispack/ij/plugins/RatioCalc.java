package mrianalysispack.ij.plugins;

import ij.plugin.PlugIn;

/**
 * 
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

public class RatioCalc implements PlugIn {

	public void run(String arg) {
		new mrianalysispack.kfschmidt.mricalculations.RatioCalc();
	}

}
