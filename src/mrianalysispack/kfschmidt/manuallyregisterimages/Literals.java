package kfschmidt.manuallyregisterimages;

/**
 * 
 * 
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
public class Literals {

	public static final String APPLY_WRONG_DIM_MSG = "WARNING! The number of slices in the transform does not match the number of anatomical slices in the image... proceed?";
	public static final String APPLY_WARNING = "WARNING!";

	public static final String APPLY_TITLE = "Transform Image";
	public static final String APPLY_MSG2 = "Really transform this image: ";

	public static final String WINDOW_TITLE = "Manually Register Images";
	public static final String IMAGE_PICKER_TITLE = "Select An Image";
	public static final String SET_REF_IMG = "Set Reference Image";
	public static final String SET_SRC_IMG = "Set Source Image";
	public static final String SAVE_TRANSFORM = "Save Transformation";

	public static final String BACK_A_SLICE = "<-- Back 1 slice";
	public static final String FORWARD_A_SLICE = "Ahead 1 slice -->";
	public static final String ADD_SLICE = "Add a slice";
	public static final String DELETE_SLICE = "Delete this slice";

	public static final String FILE_MENU = "File";
	public static final String NEW_XFORM = "New Transform";
	public static final String OPEN_XFORM = "Open Transform";
	public static final String APPLY_XFORM = "Apply Transform";

	public static final String COPY_XFORM_PREV = "Copy Prev Slice's XForm";
	public static final String COPY_XFORM_NEXT = "Copy Next Slice's XForm";
	public static final String MULTISLICE_ADD_TITLE = "Add Multiple Slices";
	public static final String MULTISLICE_ADD_MSG = "Do you want to add multiple slices?";

	public static final String IMAGES_MENU = "Transform";
	public static final String EXPORT_MENU = "Export";
	public static final String DISPLAY_MENU = "Display Settings";
	public static final String HELP_MENU = "Help";
	public static final String HELP_ONLINE = "Online Manual";
	public static final String MULTISLICE_ADD_ERROR1 = "There are not enough slices in selected image for this transform";

	public static final String NEW_XFORM_MSG1 = "How many slices are in your new transform?";
	public static final String APPLY_MSG = "Select the image that you want to apply this transform to.";
	public static final String PICK_SRC = "Select the Source Image";
	public static final String PICK_REF = "Select the Reference Image";
	public static final String NO_XFORM_LINE1 = "Create or Open";
	public static final String NO_XFORM_LINE2 = " a Transform";

	// image operations menu
	public static final String OPS_MENU = "[Register Operations]";
	public static final String TRANSLATE_OP = "Translate";
	public static final String SCALE_OP = "Scale";
	public static final String ROTATE_OP = "Rotate";
	public static final String SCALE_X = "Scale X only";
	public static final String SCALE_Y = "Scale Y only";
	public static final String NO_OP = "- Do Nothing -";
	public static final String HELP_ABOUT = "About this plugin";

	public static final String MSG_TRANSLATE_OP = "[Translating]";
	public static final String MSG_ROTATE_OP = "[Rotating]";
	public static final String MSG_SCALE_OP = "[Scaling]";
	public static final String MSG_SCALEX_OP = "[Scaling X]";
	public static final String MSG_SCALEY_OP = "[Scaling Y]";

	public static final String MSG_SLICE_LABEL = "Transform for Slice: ";
}