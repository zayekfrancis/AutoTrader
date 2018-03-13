
public class MACDAnalyzer {

	// Checks for Positive Momentum
	public static boolean MACDMomentumExists(Stock stock, String date, boolean fractal) {
		date = stock.getClosestDate(date);

		int timePeriod = stock.getTimePeriod();
		int startingIndex = stock.getDates().indexOf(date) - timePeriod + 1;
		int endingIndex = stock.getDates().indexOf(date);
		if (startingIndex < 0) { return false; }
		
		double rateOfChange = 0;
		double previousRateOfChange = 0;
		double[] ratesOfChange = new double[timePeriod];
		for (int i = startingIndex; i < endingIndex; i++) {
			String iDate1 = stock.getDates().get(i);
			String iDate2 = stock.getDates().get(i + 1);
			double hist1 = stock.getMACDHistogram(iDate1);
			double hist2 = stock.getMACDHistogram(iDate2);
			double change = stock.getMACDHistogram(iDate2) - stock.getMACDHistogram(iDate1);
			ratesOfChange[i - startingIndex] = change;
		}

		rateOfChange = ratesOfChange[0] / timePeriod;
		double multiplier = (2.0 / (double) (timePeriod + 1));
		previousRateOfChange = ratesOfChange[0];
		double ema = 0;
		for (int i = 1; i < ratesOfChange.length; i++) {
			rateOfChange = ratesOfChange[i];
			ema = ((rateOfChange - previousRateOfChange) * multiplier) + previousRateOfChange;
			previousRateOfChange = ema;
		}

		if (!fractal && recentPositiveMACDCross(stock, date) && ema > .04) { 
			return true;
		} else if (fractal && ema > 0) {
			return true;
		}
		return false;
	}

	// Checks for Positive Non-Trending Momentum
	public static boolean nonTrendingMACDMomentumExists(Stock stock, String date, boolean fractal) {
		date = stock.getClosestDate(date);

		int timePeriod = (int) ((int) stock.getTimePeriod() / 2.5);
		int startingIndex = stock.getDates().indexOf(date) - timePeriod + 1;
		int endingIndex = stock.getDates().indexOf(date);
		double rateOfChange = 0;
		double previousRateOfChange = 0;
		double[] ratesOfChange = new double[timePeriod];
		for (int i = startingIndex; i < endingIndex; i++) {
			String iDate1 = stock.getDates().get(i);
			String iDate2 = stock.getDates().get(i + 1);
			double change = stock.getMACDHistogram(iDate2) - stock.getMACDHistogram(iDate1);
			ratesOfChange[i - startingIndex] = change;
		}

		rateOfChange = ratesOfChange[0] / timePeriod / 2;
		double multiplier = (2.0 / (double) (timePeriod + 1));
		previousRateOfChange = ratesOfChange[0];
		double ema = 0;
		for (int i = 1; i < ratesOfChange.length; i++) {
			rateOfChange = ratesOfChange[i];
			ema = ((rateOfChange - previousRateOfChange) * multiplier) + previousRateOfChange;
			previousRateOfChange = ema;
		}

		if (!fractal && recentPositiveMACDCross(stock, date) && ema > .04) {
			return true;
		} else if (fractal && ema > .04) {
			return true;
		}
		return false;
	}

	// Checks for Negative Momentum
	public static boolean MACDDyingMomentum(Stock stock, String date, boolean fractal) {
		date = stock.getClosestDate(date);

		int timePeriod = stock.getTimePeriod();
		int startingIndex = stock.getDates().indexOf(date) - timePeriod + 1;
		int endingIndex = stock.getDates().indexOf(date);
		double rateOfChange = 0;
		double previousRateOfChange = 0;
		double[] ratesOfChange = new double[timePeriod];
		for (int i = startingIndex; i < endingIndex; i++) {
			String iDate1 = stock.getDates().get(i);
			String iDate2 = stock.getDates().get(i + 1);
			double change = stock.getMACDHistogram(iDate2) - stock.getMACDHistogram(iDate1);
			ratesOfChange[i - startingIndex] = change;
		}

		double multiplier = (2.0 / (double) (timePeriod + 1));
		previousRateOfChange = ratesOfChange[0];
		double ema = 0;
		for (int i = 1; i < ratesOfChange.length; i++) {
			rateOfChange = ratesOfChange[i];
			ema = ((rateOfChange - previousRateOfChange) * multiplier) + previousRateOfChange;
			previousRateOfChange = ema;
		}

		if (!fractal && recentNegativeMACDCross(stock, date) && ema < -.04) {
			return true;
		} else if (fractal && ema < -.04) {
			return true;
		}
		return false;
	}

	public static boolean recentPositiveMACDCross(Stock stock, String date) {
		date = stock.getClosestDate(date);
		int startingIndex = stock.getDates().indexOf(date) - (stock.getTimePeriod() / 4);
		int endingIndex = stock.getDates().indexOf(date);

		for (int i = endingIndex; i >= startingIndex; i--) {
			String iDate = stock.getDates().get(i);
			if (stock.getMACDHistogram(iDate) < 0) {
				return true;
			}
		}

		return false;
	}

	private static boolean recentNegativeMACDCross(Stock stock, String date) {
		date = stock.getClosestDate(date);
		int startingIndex = stock.getDates().indexOf(date) - (stock.getTimePeriod() / 4);
		int endingIndex = stock.getDates().indexOf(date);

		for (int i = endingIndex; i >= startingIndex; i--) {
			String iDate = stock.getDates().get(i);
			if (stock.getMACDHistogram(iDate) > 0) {
				return true;
			}
		}

		return false;
	}
}
