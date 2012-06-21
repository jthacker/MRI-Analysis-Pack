package kfschmidt.imageoverlay;

/**
 *   Creates a Contour map from a 2d data array
 *   Uses marching squares to parcel 2D scalar grid into 
 *   triangles 
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
import java.awt.geom.*;
import java.util.Vector;

public class ContourAnalyzer {

	double[][] mData;
	int mLevels;
	double mMinVal;
	double mMaxVal;
	Area[] mContours;

	public static Area[] getContours(double[][] data, int numlevels, double min, double max) {
		// find thresholds
		double[] thresholds = getThresholds(numlevels, min, max);

		// get the areas for each threshold
		Area[] ret = new Area[thresholds.length];
		for (int a = 0; a < ret.length; a++) {
			ret[a] = getAreaForThreshold(data, thresholds[a]);
		}

		return ret;
	}

	private static double getAvgValueForPixel(double[][] data, int x, int y, int resolution) {
		return 0d;

	}

	public static Area getAreaForThreshold(double[][] data, double threshold) {
		Vector vec = new Vector(500000);
		System.out.println("getAreaForThreshold(" + threshold + ")");
		Area retarea = null;
		Area tmparea = null;
		Triangle[] triangles = null;
		Point2D.Float[] vert_xys = new Point2D.Float[4];
		double[] vert_values = new double[4];
		for (int a = 0; a < 4; a++) {
			vert_xys[a] = new Point2D.Float();
		}
		GeneralPath tmppath = new GeneralPath();

		// march each square
		for (int y = 0; y < data[0].length - 1; y++) {
			for (int x = 0; x < data.length - 1; x++) {
				vert_values[0] = data[x][y];
				vert_values[0] = data[x + 1][y];
				vert_values[0] = data[x + 1][y + 1];
				vert_values[0] = data[x][y + 1];

				vert_xys[0].x = (float) x;
				vert_xys[0].y = (float) y;
				vert_xys[1].x = (float) (x + 1);
				vert_xys[1].y = (float) y;
				vert_xys[2].x = (float) (x + 1);
				vert_xys[2].y = (float) (y + 1);
				vert_xys[3].x = (float) x;
				vert_xys[3].y = (float) (y + 1);

				triangles = SquareMarcher.marchSquare(vert_xys, vert_values, threshold);
				if (triangles != null) {
					for (int a = 0; a < triangles.length; a++)
						vec.add(triangles[a]);
				}

			}
		}
		Triangle[] tris = new Triangle[vec.size()];
		vec.copyInto(tris);
		return getAreaForTriangles(tris);
	}

	public static Area getAreaForTriangles(Triangle[] triangles) {
		GeneralPath tmppath = new GeneralPath();
		Area retarea = new Area();
		System.out.println("getAreaForTriangles()");
		if (triangles != null) {
			System.out.println("Total triangles: " + triangles.length);
			tmppath.moveTo(triangles[0].v1.x, triangles[0].v1.y);
			for (int n = 0; n < triangles.length; n++) {
				tmppath.lineTo(triangles[n].v1.x, triangles[n].v1.y);
				tmppath.lineTo(triangles[n].v2.x, triangles[n].v2.y);
				tmppath.lineTo(triangles[n].v3.x, triangles[n].v3.y);
			}
			tmppath.closePath();
			return new Area(tmppath);
		}
		return null;
	}

	public static double[] getThresholds(int levels, double min, double max) {
		double[] ret = new double[levels];
		ret[0] = min;
		ret[ret.length - 1] = max;
		for (int a = 1; a < ret.length - 1; a++) {
			ret[a] = ret[0] + (double) a * ((max - min) / (double) ret.length - 2);
		}
		return ret;
	}

}

class SquareMarcher {

	public static Point2D.Float interpolate(Point2D.Float p1, Point2D.Float p2, double val1, double val2, double threshold) {
		double weight = 0;
		Point2D.Float interpolated_pt = new Point2D.Float();
		if (val1 <= threshold) {
			weight = (threshold - val1) / (val2 - val1);
			interpolated_pt.x = (float) (p1.x + (p2.x - p1.x) * weight);
			interpolated_pt.y = (float) (p1.y + (p2.y - p1.y) * weight);
		} else {
			weight = (threshold - val2) / (val1 - val2);
			interpolated_pt.x = (float) (p2.x + (p1.x - p2.x) * weight);
			interpolated_pt.y = (float) (p2.y + (p1.y - p2.y) * weight);
		}
		return interpolated_pt;
	}

	public static Triangle[] marchSquare(Point2D.Float[] vert_xys, double[] verts, double threshold) {
		Triangle[] ret = null;
		// determine case
		boolean a = verts[0] <= threshold ? false : true;
		boolean b = verts[1] <= threshold ? false : true;
		boolean c = verts[2] <= threshold ? false : true;
		boolean d = verts[3] <= threshold ? false : true;

		// 16 cases
		if (!a && !b && !c && !d) {
			// 0: no edges

		} else if (!a && !b && !c && d) {
			// 1: 1 triangle D, AD, DC
			ret = new Triangle[1];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[3];
			ret[0].v2 = interpolate(vert_xys[3], vert_xys[0], verts[3], verts[0], threshold);
			ret[0].v3 = interpolate(vert_xys[3], vert_xys[2], verts[3], verts[2], threshold);

		} else if (!a && !b && c && !d) {
			// 2: 1 triangle C, BC, DC
			ret = new Triangle[1];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[2];
			ret[0].v2 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);
			ret[0].v3 = interpolate(vert_xys[3], vert_xys[2], verts[3], verts[2], threshold);

		} else if (!a && !b && c && d) {
			// 3: 2 triangles D, AD, BC & C, BC, CD
			ret = new Triangle[2];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[3];
			ret[0].v2 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);
			ret[0].v3 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[2];
			ret[1].v2 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);
			ret[1].v3 = interpolate(vert_xys[2], vert_xys[3], verts[2], verts[3], threshold);

		} else if (!a && b && !c && !d) {
			// 4: 1 triangle B, AB, BC
			ret = new Triangle[1];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[1];
			ret[0].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v3 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);

		} else if (!a && b && !c && d) {
			// 5: 2 triangles B, AB, BC & D, DC, AD
			ret = new Triangle[2];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[1];
			ret[0].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v3 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[3];
			ret[1].v2 = interpolate(vert_xys[3], vert_xys[2], verts[3], verts[2], threshold);
			ret[1].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

		} else if (!a && b && c && !d) {
			// 6: 2 triangles B, AB, BC & C, AB, DC
			ret = new Triangle[2];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[1];
			ret[0].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v3 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[2];
			ret[1].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[1].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

		} else if (!a && b && c && d) {
			// 7: 3 triangles AB, B, C & AB, C, AD & AD, D, C
			ret = new Triangle[3];
			ret[0] = new Triangle();
			ret[0].v1 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v2 = vert_xys[1];
			ret[0].v3 = vert_xys[2];

			ret[1] = new Triangle();
			ret[1].v1 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[1].v2 = vert_xys[2];
			ret[1].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

			ret[2] = new Triangle();
			ret[2].v1 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);
			ret[2].v2 = vert_xys[3];
			ret[2].v3 = vert_xys[2];

		} else if (a && !b && !c && !d) {
			// 8: 1 triangle A, AB, AD
			ret = new Triangle[1];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[0];
			ret[0].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

		} else if (a && !b && !c && d) {
			// 9: 2 triangles A, AB, D & AB, CD, D
			ret = new Triangle[2];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[0];
			ret[0].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v3 = vert_xys[3];

			ret[1] = new Triangle();
			ret[1].v1 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[1].v2 = interpolate(vert_xys[2], vert_xys[3], verts[2], verts[3], threshold);
			ret[1].v3 = vert_xys[3];

		} else if (a && !b && c && !d) {
			// 10: 2 triangles A, AB, AD & C, BC, CD
			ret = new Triangle[2];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[0];
			ret[0].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[2];
			ret[1].v2 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);
			ret[1].v3 = interpolate(vert_xys[2], vert_xys[3], verts[2], verts[3], threshold);

		} else if (a && !b && c && d) {
			// 11: 3 triangles AB, BC, D & A, AB, D & BC, C, D
			ret = new Triangle[3];
			ret[0] = new Triangle();
			ret[0].v1 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[0].v2 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);
			ret[0].v3 = vert_xys[3];

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[0];
			ret[1].v2 = interpolate(vert_xys[0], vert_xys[1], verts[0], verts[1], threshold);
			ret[1].v3 = vert_xys[3];

			ret[2] = new Triangle();
			ret[2].v1 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);
			ret[2].v2 = vert_xys[2];
			ret[2].v3 = vert_xys[3];

		} else if (a && b && !c && !d) {
			// 12: 2 triangles A, B, AD & B, BC, AD
			ret = new Triangle[2];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[0];
			ret[0].v2 = vert_xys[1];
			ret[0].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[2];
			ret[1].v2 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);
			ret[1].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

		} else if (a && b && !c && d) {
			// 13: 3 triangles A, B, BC & A, BC, CD & A, CD, D
			ret = new Triangle[3];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[0];
			ret[0].v2 = vert_xys[1];
			ret[0].v3 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[0];
			ret[1].v2 = interpolate(vert_xys[1], vert_xys[2], verts[1], verts[2], threshold);
			ret[1].v3 = interpolate(vert_xys[2], vert_xys[3], verts[2], verts[3], threshold);

			ret[2] = new Triangle();
			ret[2].v1 = vert_xys[0];
			ret[2].v2 = interpolate(vert_xys[2], vert_xys[3], verts[2], verts[3], threshold);
			ret[2].v3 = vert_xys[3];

		} else if (a && b && c && !d) {
			// 14: 3 triangles A, B, AD & B, CD, AD & B, C, DC
			ret = new Triangle[3];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[0];
			ret[0].v2 = vert_xys[1];
			ret[0].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[1];
			ret[1].v2 = interpolate(vert_xys[2], vert_xys[3], verts[2], verts[3], threshold);
			ret[1].v3 = interpolate(vert_xys[0], vert_xys[3], verts[0], verts[3], threshold);

			ret[2] = new Triangle();
			ret[2].v1 = vert_xys[1];
			ret[2].v2 = vert_xys[2];
			ret[2].v3 = interpolate(vert_xys[2], vert_xys[3], verts[2], verts[3], threshold);

		} else if (a && b && c && d) {
			// 15: 2 triangles A, B, D & B, C, D
			ret = new Triangle[2];
			ret[0] = new Triangle();
			ret[0].v1 = vert_xys[0];
			ret[0].v2 = vert_xys[1];
			ret[0].v3 = vert_xys[3];

			ret[1] = new Triangle();
			ret[1].v1 = vert_xys[1];
			ret[1].v2 = vert_xys[2];
			ret[1].v3 = vert_xys[3];
		}

		return ret;
	}

}

class Triangle {

	Point2D.Float v1;
	Point2D.Float v2;
	Point2D.Float v3;

}

class Grid2D {

	double[][] mData;
	float mPixelsPerSquare;

	public Grid2D(double[][] data, float pixels_per_square) {
		mData = data;
		mPixelsPerSquare = pixels_per_square;
	}

	public void getSquare(int x, int y, Point2D.Float[] verts, double[] vert_values) {

	}

	public double getValueAtGridPoint(int x, int y) {
		// average the pixels within the square
		/*
		 * float pixel_widths_per_square = (float) mData[0].length / (float)
		 * mWidth; float pixel_heights_per_square = (float) mData.length /
		 * (float) mWidth; double val; int count; for (int a = (int)((float)
		 * x*pixel_widths_per_square); a< (x+1)*pixel_widths_per_square; a++) {
		 * for (int b = (int)((float) y*pixel_heights_per_square); b<
		 * (y+1)*pixel_heights_per_square; b++) { val += mData[a][b]; count++; }
		 * } return val/(double)count;
		 */
		return 0d;
	}

}
