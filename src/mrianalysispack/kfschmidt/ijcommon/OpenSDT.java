package mrianalysispack.kfschmidt.ijcommon;

/**
 *
 *   This plugin is used to read SDT files produced for Stimulate
 *   the SPR file is parsed
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

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.filechooser.*;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;
import ij.io.OpenDialog;
import ij.io.FileInfo;
import ij.io.FileOpener;

public class OpenSDT implements PlugIn {
	String mSPRFileContents;
	public static final int REAL = 1;
	public static final int LONG = 2;
	public static final int WORD = 3;

	public void run(String arg) {
		// get the spr file
		File[] sprfiles = getTheSPRFiles();
		if (sprfiles == null)
			return;

		for (int a = 0; a < sprfiles.length; a++) {
			// get the sdt file
			File sdtfile = getSDTFile(sprfiles[a]);
			if (sdtfile == null) {
				IJ.showMessage("The SDT file does not exist");
				return;
			}

			// get the dimensions and type from the spr file
			try {
				int[] dims = getDims(sprfiles[a]); // format [height, width,
													// slices, reps]
				int dattype = getImageType(sprfiles[a]);

				// open the file in imagej
				int id = openInImageJ(sdtfile, dims[1], dims[0], dims[2], dims[3], dattype, isLittleEndian(sprfiles[a]));

				// set the dimensions in memory for other plugins
				mrianalysispack.kfschmidt.ijcommon.IJAdapter ijada = new mrianalysispack.kfschmidt.ijcommon.IJAdapter();

				ijada.setImageDims(id, dims); // [height, width, slices, reps]

			} catch (Exception e) {
				IJ.showMessage("There was a problem opening the SDT file: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private int getImageType(File sprfile) throws Exception {
		String line = getLineContaining("dataType:", sprfile);
		if (line.indexOf("REAL") > 0)
			return REAL;
		else if (line.indexOf("LONG") > 0 || line.indexOf("LWORD") > 0)
			return LONG;
		else if (line.indexOf("WORD") > 0)
			return WORD;
		else
			return 0;
	}

	public static String getFileContents(File f) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(f));
		StringBuffer bf = new StringBuffer(1000);
		String line = "";
		while ((line = br.readLine()) != null) {
			bf.append(line);
			bf.append("\n");
		}
		return bf.toString();
	}

	private String getLineContaining(String snip, File sprfile) throws Exception {
		if (mSPRFileContents == null)
			mSPRFileContents = getFileContents(sprfile);

		BufferedReader br = new BufferedReader(new StringReader(mSPRFileContents));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.indexOf(snip) > -1)
				return line;
		}
		return line;
	}

	private int[] getDims(File sprfile) throws Exception {
		int[] ret = new int[4];
		String line = getLineContaining("dim:", sprfile);
		if (line == null)
			throw new Exception("Could not find the dimensions in the SPR file!");
		StringTokenizer tok = new StringTokenizer(line);
		tok.nextToken();

		for (int a = 0; a < 4; a++) {
			if (tok.hasMoreTokens())
				ret[a] = Integer.parseInt(tok.nextToken());
		}

		return ret;
	}

	private boolean isLittleEndian(File sprfile) throws Exception {
		String line = getLineContaining("endian:", sprfile);
		if (line != null && line.indexOf("-le") > 0)
			return true;
		return false;
	}

	private int openInImageJ(File sdtfile, int w, int h, int numslices, int numreps, int type, boolean littleendian) {
		FileInfo fi = new FileInfo();
		fi.width = w;
		fi.height = h;
		fi.offset = 0;
		int slices = numslices;
		if (numreps > 1)
			slices = numslices * numreps;
		fi.nImages = slices;
		fi.fileName = sdtfile.getName();
		fi.directory = sdtfile.getParent();
		if (type == REAL) {
			fi.fileType = FileInfo.GRAY32_FLOAT;
		} else if (type == LONG) {
			fi.fileType = FileInfo.GRAY32_UNSIGNED;
		} else if (type == WORD) {
			fi.fileType = FileInfo.GRAY16_UNSIGNED;
		}
		if (littleendian)
			fi.intelByteOrder = true;

		ImagePlus ip = new FileOpener(fi).open(true);
		return ip.getID();
	}

	private File getSDTFile(File sprfile) {
		File sdtfile = null;
		String parentdir = sprfile.getParent();
		String sdtname = sprfile.getName();
		sdtname = sdtname.substring(0, sdtname.length() - 4);
		sdtname += ".sdt";
		sdtfile = new File(parentdir + "/" + sdtname);
		if (!sdtfile.exists())
			return null;
		else
			return sdtfile;
	}

	private File[] getTheSPRFiles() {
		// show the user a file chooser
		File[] sprfiles = null;
		File defaultdir = (new File("afile")).getParentFile();
		defaultdir = IJAdapter.getWorkingDirectory();
		JFileChooser chooser = null;

		if (defaultdir != null) {
			chooser = new JFileChooser(defaultdir);
		} else {
			chooser = new JFileChooser();
		}

		ExampleFileFilter filter = new ExampleFileFilter();
		filter.addExtension("spr");
		filter.setDescription("SPR Files");
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(ij.IJ.getInstance());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			sprfiles = chooser.getSelectedFiles();
		}
		if (sprfiles != null && sprfiles.length > 0)
			IJAdapter.setWorkingDirectory(sprfiles[0]);
		return sprfiles;
	}
}

/*
 * Copyright (c) 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

/*
 * @(#)ExampleFileFilter.java 1.13 02/06/13
 */

/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about.
 * 
 * Extensions are of the type ".foo", which is typically found on Windows and
 * Unix boxes, but not on Macinthosh. Case is ignored.
 * 
 * Example - create a new filter that filerts out all files but gif and jpg
 * image files:
 * 
 * JFileChooser chooser = new JFileChooser(); ExampleFileFilter filter = new
 * ExampleFileFilter( new String{"gif", "jpg"}, "JPEG & GIF Images")
 * chooser.addChoosableFileFilter(filter); chooser.showOpenDialog(this);
 * 
 * @version 1.13 06/13/02
 * @author Jeff Dinkins
 */
class ExampleFileFilter extends FileFilter {

	private static String TYPE_UNKNOWN = "Type Unknown";
	private static String HIDDEN_FILE = "Hidden File";

	private Hashtable filters = null;
	private String description = null;
	private String fullDescription = null;
	private boolean useExtensionsInDescription = true;

	/**
	 * Creates a file filter. If no filters are added, then all files are
	 * accepted.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter() {
		this.filters = new Hashtable();
	}

	/**
	 * Creates a file filter that accepts files with the given extension.
	 * Example: new ExampleFileFilter("jpg");
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String extension) {
		this(extension, null);
	}

	/**
	 * Creates a file filter that accepts the given file type. Example: new
	 * ExampleFileFilter("jpg", "JPEG Image Images");
	 * 
	 * Note that the "." before the extension is not needed. If provided, it
	 * will be ignored.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String extension, String description) {
		this();
		if (extension != null)
			addExtension(extension);
		if (description != null)
			setDescription(description);
	}

	/**
	 * Creates a file filter from the given string array. Example: new
	 * ExampleFileFilter(String {"gif", "jpg"});
	 * 
	 * Note that the "." before the extension is not needed adn will be ignored.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String[] filters) {
		this(filters, null);
	}

	/**
	 * Creates a file filter from the given string array and description.
	 * Example: new ExampleFileFilter(String {"gif", "jpg"},
	 * "Gif and JPG Images");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 * 
	 * @see #addExtension
	 */
	public ExampleFileFilter(String[] filters, String description) {
		this();
		for (int i = 0; i < filters.length; i++) {
			// add filters one by one
			addExtension(filters[i]);
		}
		if (description != null)
			setDescription(description);
	}

	/**
	 * Return true if this file should be shown in the directory pane, false if
	 * it shouldn't.
	 * 
	 * Files that begin with "." are ignored.
	 * 
	 * @see #getExtension
	 * @see FileFilter#accepts
	 */
	public boolean accept(File f) {
		if (f != null) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null && filters.get(getExtension(f)) != null) {
				return true;
			}
			;
		}
		return false;
	}

	/**
	 * Return the extension portion of the file's name .
	 * 
	 * @see #getExtension
	 * @see FileFilter#accept
	 */
	public String getExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			}
			;
		}
		return null;
	}

	/**
	 * Adds a filetype "dot" extension to filter against.
	 * 
	 * For example: the following code will create a filter that filters out all
	 * files except those that end in ".jpg" and ".tif":
	 * 
	 * ExampleFileFilter filter = new ExampleFileFilter();
	 * filter.addExtension("jpg"); filter.addExtension("tif");
	 * 
	 * Note that the "." before the extension is not needed and will be ignored.
	 */
	public void addExtension(String extension) {
		if (filters == null) {
			filters = new Hashtable(5);
		}
		filters.put(extension.toLowerCase(), this);
		fullDescription = null;
	}

	/**
	 * Returns the human readable description of this filter. For example:
	 * "JPEG and GIF Image Files (*.jpg, *.gif)"
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 * @see FileFilter#getDescription
	 */
	public String getDescription() {
		if (fullDescription == null) {
			if (description == null || isExtensionListInDescription()) {
				fullDescription = description == null ? "(" : description + " (";
				// build the description from the extension list
				Enumeration extensions = filters.keys();
				if (extensions != null) {
					fullDescription += "." + (String) extensions.nextElement();
					while (extensions.hasMoreElements()) {
						fullDescription += ", ." + (String) extensions.nextElement();
					}
				}
				fullDescription += ")";
			} else {
				fullDescription = description;
			}
		}
		return fullDescription;
	}

	/**
	 * Sets the human readable description of this filter. For example:
	 * filter.setDescription("Gif and JPG Images");
	 * 
	 * @see setDescription
	 * @see setExtensionListInDescription
	 * @see isExtensionListInDescription
	 */
	public void setDescription(String description) {
		this.description = description;
		fullDescription = null;
	}

	/**
	 * Determines whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description.
	 * 
	 * Only relevent if a description was provided in the constructor or using
	 * setDescription();
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see isExtensionListInDescription
	 */
	public void setExtensionListInDescription(boolean b) {
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/**
	 * Returns whether the extension list (.jpg, .gif, etc) should show up in
	 * the human readable description.
	 * 
	 * Only relevent if a description was provided in the constructor or using
	 * setDescription();
	 * 
	 * @see getDescription
	 * @see setDescription
	 * @see setExtensionListInDescription
	 */
	public boolean isExtensionListInDescription() {
		return useExtensionsInDescription;
	}
}
