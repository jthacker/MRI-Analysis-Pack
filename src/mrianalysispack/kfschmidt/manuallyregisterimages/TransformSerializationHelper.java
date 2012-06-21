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
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.*;
import org.w3c.dom.*;
import javax.swing.text.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;

public class TransformSerializationHelper {

	public static String getXMLForTransform(TransformRecord r) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(100000);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		DOMImplementation di = builder.getDOMImplementation();
		Document doc = di.createDocument("", "MANUAL_TRANSFORMATION", null);
		doc.getDocumentElement().setAttribute("description", "Transformation paramters from Manual Image Registration Plugin");

		// --------- create the document
		if (r != null) {
			r.setCurSlice(0);
			for (int a = 0; a < r.getSliceCount(); a++) {
				doc.getDocumentElement().appendChild(
						getElementForSliceTransform(doc, r.getReferenceImage(), r.getSourceImage(), r.getRotation(), r.getXScale(), r.getYScale(),
								r.getXOffset(), r.getYOffset()));
				r.incrementCurSlice();
			}
		}
		// -------------

		StreamResult res_stream = new StreamResult(baos);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer serializer = transformerFactory.newTransformer();
		serializer.transform(new DOMSource(doc), res_stream);
		return baos.toString();

	}

	public static TransformRecord getTransformFromXML(String xml) throws Exception {
		if (xml == null)
			return null;

		ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document doc = builder.parse(bais);
		TransformRecord trans = new TransformRecord();

		// get the elements for slices
		NodeList list0 = doc.getElementsByTagName("MANUAL_TRANSFORMATION");
		if (list0.getLength() < 1)
			return null;

		Element transform_element = (Element) list0.item(0);
		String transdesc = transform_element.getAttribute("description");

		NodeList list = transform_element.getElementsByTagName("SLICE_XFORM");
		for (int a = 0; a < list.getLength(); a++) {
			Element slice_el = (Element) list.item(a);
			addSliceToTransform(trans, slice_el);
		}
		return trans;
	}

	// -------- Object --> XML ----------------

	private static Element getElementForSliceTransform(Document doc, RegImage ref, RegImage src, double rot, double scalex, double scaley, double offsetx,
			double offsety) throws Exception {
		Element slice_el = doc.createElement("SLICE_XFORM");
		slice_el.setAttribute("rotation", "" + rot);
		slice_el.setAttribute("scalex", "" + scalex);
		slice_el.setAttribute("scaley", "" + scaley);
		slice_el.setAttribute("offsetx", "" + offsetx);
		slice_el.setAttribute("offsety", "" + offsety);
		slice_el.appendChild(getElementForRegImage(doc, ref, "reference"));
		slice_el.appendChild(getElementForRegImage(doc, src, "source"));

		return slice_el;
	}

	private static BufferedImage getImageFromBase64(String base64) throws Exception {
		Base64 decoder = new Base64();

		ByteArrayInputStream in = new ByteArrayInputStream(decoder.decode(base64));
		BufferedImage i = javax.imageio.ImageIO.read(in);
		return i;
	}

	private static String base64EncodeImage(BufferedImage bi) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(100000);
		javax.imageio.ImageIO.write(bi, "png", baos);
		Base64 encoder = new Base64();
		String base64 = encoder.encodeAsString(baos.toByteArray());
		return base64;
	}

	private static Element getElementForRegImage(Document doc, RegImage ri, String type) throws Exception {
		Element ri_el = doc.createElement("REG_IMAGE");
		ri_el.setAttribute("name", ri.getName());
		ri_el.setAttribute("type", type);
		ri_el.setAttribute("height", "" + ri.getRendering().getHeight());
		ri_el.setAttribute("width", "" + ri.getRendering().getWidth());
		ri_el.setAttribute("imgdata", base64EncodeImage(ri.getRendering()));
		return ri_el;
	}

	// -------- XML --> Object ----------------
	private static RegImage getRegImageForElement(Element el) throws Exception {
		// get the attributes
		String name = el.getAttribute("name");
		BufferedImage bi = getImageFromBase64(el.getAttribute("imgdata"));
		RegImage ri = new RegImage(name, bi);
		return ri;
	}

	private static void addSliceToTransform(TransformRecord tr, Element slice_el) throws Exception {

		// add the slice to the xform
		tr.addSlice();

		// set the ref images
		NodeList refimgs = slice_el.getElementsByTagName("REG_IMAGE");
		// expect two only
		Element regimg1 = (Element) refimgs.item(0);
		Element regimg2 = (Element) refimgs.item(1);
		if (regimg1.getAttribute("type").equals("reference")) {
			tr.setReferenceImage(getRegImageForElement(regimg1));
			tr.setSourceImage(getRegImageForElement(regimg2));
		} else {
			tr.setReferenceImage(getRegImageForElement(regimg2));
			tr.setSourceImage(getRegImageForElement(regimg1));
		}

		tr.setRotation(Double.parseDouble(slice_el.getAttribute("rotation")));
		tr.setXScale(Double.parseDouble(slice_el.getAttribute("scalex")));
		tr.setYScale(Double.parseDouble(slice_el.getAttribute("scaley")));
		tr.setXOffset(Double.parseDouble(slice_el.getAttribute("offsetx")));
		tr.setYOffset(Double.parseDouble(slice_el.getAttribute("offsety")));
	}

}