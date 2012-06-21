package kfschmidt.math;

/**
 *   Collection of static LinearAlgebra methods, calculated
 *   primarily from Quickvol source.
 *
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
import java.text.DecimalFormat;

public class LinearAlgebra {

	public static double[][] calcTranspose(double[][] mat) {
		double[][] transpose = new double[mat[0].length][mat.length];
		for (int a = 0; a < transpose.length; a++) {
			for (int b = 0; b < transpose[0].length; b++) {
				transpose[a][b] = mat[b][a];
			}
		}
		return transpose;
	}

	public static double[][] calcInverse(double[][] mat) throws Exception {
		int dim = mat.length;

		if (dim < 2)
			throw new Exception("Matrix is not 2x2 or larger: " + toString(mat));
		if (mat.length != mat[0].length)
			throw new Exception("Matrix must be square: " + toString(mat));

		// get the adjoint
		double[][] adj = new double[dim][dim];
		adj = calcAdjoint(mat);

		// get the determinant
		double det = calcDeterminant(mat);
		if (det == 0) {
			throw new Exception("Matrix is not invertible\n" + toString(mat));
		}

		double[][] inv = scalarMultiply(adj, (1 / det));

		return inv;
	}

	/**
	 * returns the cofactor matrix of the matrix passed in
	 * 
	 */
	public static double[][] calcCofactorMatrix(double[][] mat) {
		int dim = mat[0].length;
		double[][] cof = new double[dim][dim];
		for (int r = 0; r < dim; r++) {
			for (int c = 0; c < dim; c++) {
				cof[r][c] = getCofactor(mat, r + 1, c + 1);
			}
		}
		return cof;
	}

	/**
	 * returns the transpose of the cofactor matrix or Adjoint matrix
	 * 
	 */
	public static double[][] calcAdjoint(double[][] mat) {
		return calcTranspose(calcCofactorMatrix(mat));
	}

	/**
	 * calculates the determinant of the matrix passed.
	 * 
	 */
	public static double calcDeterminant(double[][] mat) {
		int dim = mat[0].length;
		double ret = 0d;
		if (dim == 1)
			return mat[0][0]; // not sure if this is defined
		if (dim == 2) {
			// if dim mat == 2 then return determinant
			ret = mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
		} else if (dim > 2) {
			// else, expand
			for (int a = 1; a <= dim; a++) {
				ret += mat[0][a - 1] * getCofactor(mat, 1, a);
			}
		}
		return ret;
	}

	/**
	 * Returns the cofactor of the matrix at the row, col
	 * 
	 */
	public static double getCofactor(double[][] mat, int row, int col) {
		double sign = Math.pow(-1, (row + col));
		double det = calcDeterminant(getMinor(mat, row, col));
		double ret = sign * det;
		return ret;

	}

	/**
	 * Scalar multiplies this matrix by the scalar and returns the resulting
	 * matrix
	 * 
	 */
	public static double[][] scalarMultiply(double[][] mat, double scalar) {
		for (int r = 0; r < mat.length; r++) {
			for (int c = 0; c < mat[0].length; c++) {
				mat[r][c] *= scalar;
			}
		}
		return mat;
	}

	/**
	 * multiplies the matrices and populates destmat with the result
	 */
	public static double[][] multiplyMatrices(double[][] mat1, double[][] mat2) {
		double[][] tmpmat = new double[mat1.length][mat2[0].length];

		// multiply the mats into the temp mat
		for (int r = 0; r < tmpmat.length; r++) {
			for (int c = 0; c < tmpmat[0].length; c++) {
				for (int n = 0; n < mat1[0].length; n++) {
					tmpmat[r][c] += mat1[r][n] * mat2[n][c];
				}
			}
		}

		return tmpmat;
	}

	/**
	 * Return the minor of the matrix by excluding row and col; NOTE: row and
	 * col are ONE based, not zero based like the underlying arrays
	 * 
	 */
	public static double[][] getMinor(double[][] mat, int row, int col) {
		int dim = mat[0].length - 1;
		double[][] minor = new double[dim][dim];
		int minor_row = 0;
		int minor_col = 0;

		// extract the minor
		for (int a = 0; a <= dim; a++) {
			if ((row - 1) != a) {
				for (int b = 0; b <= dim; b++) {
					if ((col - 1) != b) {
						minor[minor_row][minor_col] = mat[a][b];
						minor_col++;
					}
				}
				minor_row++;
				minor_col = 0;
			}
		}

		return minor;

	}

	public static String toString(double[][] mat) {
		DecimalFormat f = new DecimalFormat(" 0.00E00 ;-0.00E00");
		StringBuffer sb = new StringBuffer(1000);
		sb.append("\n");
		for (int a = 0; a < mat.length; a++) {
			sb.append("\n");
			for (int b = 0; b < mat[0].length; b++) {
				sb.append(f.format(mat[a][b]));
				sb.append(" ");
			}
		}
		sb.append("\n");
		return sb.toString();

	}

}