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
import java.io.*;

public class TransformReader {

	public static TransformRecord readTransform(File f) throws Exception {
		String xml = getFileContents(f);
		return TransformSerializationHelper.getTransformFromXML(xml);
	}

	public static String getFileContents(File f) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(f));
		StringBuffer bf = new StringBuffer(1000000);
		String line = "";
		while ((line = br.readLine()) != null) {
			bf.append(line);
			bf.append("\n");
		}
		return bf.toString();
	}

}