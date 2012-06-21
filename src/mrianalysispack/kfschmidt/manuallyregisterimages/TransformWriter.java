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
import java.io.File;
import java.util.Date;
import java.text.DateFormat;
import java.lang.StringBuffer;
import java.io.FileOutputStream;

public class TransformWriter {

	public static void writeTransform(TransformRecord rec, File f) throws Exception {
		String str = TransformSerializationHelper.getXMLForTransform(rec);
		FileOutputStream fw = new FileOutputStream(f);
		fw.write(str.getBytes());
		fw.flush();
		fw.close();
	}

}