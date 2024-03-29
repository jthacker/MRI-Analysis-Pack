package mrianalysispack.kfschmidt.ijcommon;

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

public class ViewManual implements ij.plugin.PlugIn {

	public void run(String args) {
		try {
			// open a browser with the url to the pdf manual
			IJAdapter.openURL("http://www.quickvol.com/resources/MRIAnalysisPakManual.PDF");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}