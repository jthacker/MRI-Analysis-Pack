package kfschmidt.mricalculations;

public class LinearRegressionTest {

	public static void main(String[] argv) {
		double[] datax = { 1.94d, 1.322d, 1.98944d, 2.324d, 1.9234d };
		double[] datay = { 1.74d, 1.422d, 1.88944d, 2.124d, 1.7234d };
		double[] result = null;
		result = LinearRegression.regress(datax, datax);
		System.out.println("Regression results, exact fit: m=" + result[0] + " b=" + result[1] + " r=" + result[2]);
		result = LinearRegression.regress(datax, datay);
		System.out.println("Regression results, near fit: m=" + result[0] + " b=" + result[1] + " r=" + result[2]);
	}

}