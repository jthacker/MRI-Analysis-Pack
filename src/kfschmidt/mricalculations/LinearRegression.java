/**
 *   Class which implements standard linear regression
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
package kfschmidt.mricalculations;

class LinearRegression {

	/**
	 * fits an equation to the line y = mx + b returns array of doubles
	 * containing [m,b,r^2]
	 */
	public static double[] regress(double[] x_points, double[] y_points) {
		assert x_points.length == y_points.length;
		
		double[] ret = new double[3]; // [slope, intercept, correlation_coef]
		ret[2] = correlation_coefficient(x_points, y_points);
		ret[0] = ret[2] * (stdev(y_points) / stdev(x_points));
		ret[1] = average(x_points); // STRANGE! vm crash with: ret[1] =
									// average(y_points) -
									// ret[0]*average(x_points);
		ret[1] = ret[0] * ret[1];
		ret[1] = average(y_points) - ret[1];
		return ret;
	}

	public static double correlation_coefficient(double[] datax, double[] datay) {
		return covariance(datax, datay) / (stdev(datax) * stdev(datay));
	}

	public static double average(double[] data) {
		double reta = 0d;
		for (int a = 0; a < data.length; a++) {
			reta += data[a];
		}
		reta = reta / (double) data.length;
		return reta;
	}

	public static double average_of_squares(double[] data) {
		double ret = 0d;
		for (int a = 0; a < data.length; a++) {
			ret += data[a] * data[a];
		}
		ret = ret / (double) data.length;
		return ret;
	}

	public static double average_of_products(double[] datax, double[] datay) {
		double ret = 0d;
		for (int a = 0; a < datax.length; a++) {
			ret += datax[a] * datay[a];
		}
		ret = ret / (double) datax.length;
		return ret;
	}

	public static double variance(double[] data) {
		double avg_of_sqs = average_of_squares(data);
		double avg = average(data);
		return avg_of_sqs - avg * avg;
	}

	public static double stdev(double[] data) {
		return Math.sqrt(variance(data));
	}

	public static double covariance(double[] datax, double[] datay) {
		double ret = 0d;
		double avgx = average(datax);
		double avgy = average(datay);

		for (int a = 0; a < datax.length; a++) {
			ret += (datax[a] - avgx) * (datay[a] - avgy);
		}
		ret = ret / (double) datax.length;
		return ret;
	}

}
