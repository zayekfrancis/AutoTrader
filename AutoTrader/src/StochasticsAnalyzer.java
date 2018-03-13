
public class StochasticsAnalyzer {

	public static boolean buySignal(Stock stock, String date, boolean fractal) {
		boolean momentum = stock.getStochasticK(date) > stock.getStochasticD(date);
		if (fractal) {
			boolean positiveKCross = positiveKCross(stock, date);
			return (momentum && positiveKCross);
		} else {
			boolean divergenceExists = findPositiveDivergence(stock, date);
			return (momentum && divergenceExists);
		}
	}
	
	public static boolean momentumBuySignal(Stock stock, String date, boolean fractal) {
		boolean momentum = KMomentumExists(stock, date, fractal);
		return momentum;
	}

	public static boolean sellSignal(Stock stock, String date, boolean fractal) {
		boolean momentum = stock.getStochasticK(date) < stock.getStochasticD(date);
		if (fractal) {
			boolean negativeKCross = negativeKCross(stock, date);
			return (momentum && negativeKCross);
		} else {
			boolean divergenceExists = findNegativeDivergence(stock, date);
			return (momentum && divergenceExists);
		}
	}
	
	// Checks for Positive Momentum
	public static boolean KMomentumExists(Stock stock, String date, boolean fractal) {
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
			double k1 = stock.getStochasticK(iDate1);
			double k2 = stock.getStochasticK(iDate2);
			double d1 = stock.getStochasticD(iDate1);
			double d2 = stock.getStochasticD(iDate2);
			double hist1 = k1 - d1;
			double hist2 = k2 - d2;
			double change = hist2 - hist1;
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

		if (!fractal && positiveKCross(stock, date) && ema > .04) { 
			return true;
		} else if (fractal && ema > .04) {
			return true;
		}
		return false;
	}

	public static boolean findPositiveDivergence(Stock stock, String date) {
		date = stock.getClosestDate(date);
		for (double divider = 1.75; divider < stock.getTimePeriod() / 3; divider = (divider + .25)) {
			// Find 2 ranges to check for different peaks
			int endingIndex2 = stock.getDates().indexOf(date);
			int startingIndex2 = (int) (endingIndex2 - stock.getTimePeriod() / divider);
			int endingIndex1 = startingIndex2;
			int startingIndex1 = (int) (endingIndex1 - stock.getTimePeriod() / divider);
			if (startingIndex1 < 0) { return false; }
			
			// Create the 2 arrays and find the peaks if they exist
			double[] array1 = new double[endingIndex1 - startingIndex1 + 1];
			double[] array2 = new double[endingIndex2 - startingIndex2 + 1];
			for (int i = startingIndex1; i <= endingIndex1; i++) {
				String iDate = stock.getDates().get(i);
				array1[i - startingIndex1] = stock.getStochasticK(iDate);
			}
			for (int i = startingIndex2; i <= endingIndex2; i++) {
				String iDate = stock.getDates().get(i);
				array2[i - startingIndex2] = stock.getStochasticK(iDate);
			}

			int bottom1 = Util.findBottoms(array1, array1.length);
			int bottom2 = Util.findBottoms(array2, array2.length);
			String date1 = "";
			String date2 = "";

			if (bottom1 != -1 && bottom2 != -1) {
				// Peaks found
				int dateIndex1 = startingIndex1 - bottom1;
				int dateIndex2 = startingIndex2 - bottom2;
				if (dateIndex1 < 0 || dateIndex2 < 0) { return false; }
				date1 = stock.getDates().get(dateIndex1);
				date2 = stock.getDates().get(dateIndex2);
			} else {
				continue;
			}

			// Check if divergence exists
			double priceMin1 = stock.getLow(date1);
			double kMin1 = stock.getStochasticK(date1);

			double priceMin2 = stock.getLow(date2);
			double kMin2 = stock.getStochasticK(date2);

			if (priceMin1 > priceMin2 
				&& kMin1 < kMin2
				&& kMin1 < 50
				&& kMin2 < 50) {
				return true;
			}
		}

		return false;
	}

	private static boolean findNegativeDivergence(Stock stock, String date) {
		date = stock.getClosestDate(date);
		for (int divider = (int) (stock.getTimePeriod() / 1.25); divider >= 2; divider--) {

			// Find 2 ranges to check for different peaks
			int endingIndex2 = stock.getDates().indexOf(date);
			int startingIndex2 = (int) (endingIndex2 - stock.getTimePeriod() / divider);
			int endingIndex1 = startingIndex2 - 1;
			int startingIndex1 = (int) (endingIndex1 - stock.getTimePeriod() / divider);

			// Create the 2 arrays and find the peaks if they exist
			double[] array1 = new double[endingIndex1 - startingIndex1 + 1];
			double[] array2 = new double[endingIndex2 - startingIndex2 + 1];
			for (int i = startingIndex1; i <= endingIndex1; i++) {
				String iDate = stock.getDates().get(i);
				array1[i - startingIndex1] = stock.getStochasticK(iDate);
			}
			for (int i = startingIndex2; i <= endingIndex2; i++) {
				String iDate = stock.getDates().get(i);
				array2[i - startingIndex2] = stock.getStochasticK(iDate);
			}

			int peak1 = Util.findPeaks(array1, array1.length);
			int peak2 = Util.findPeaks(array2, array2.length);
			String date1 = "";
			String date2 = "";

			if (peak1 != -1 && peak2 != -1) {
				// Peaks found
				int dateIndex1 = startingIndex1 - peak1;
				int dateIndex2 = startingIndex2 - peak2;
				date1 = stock.getDates().get(dateIndex1);
				date2 = stock.getDates().get(dateIndex2);
			} else {
				continue;
			}

			// Check if divergence exists
			double priceMin1 = stock.getHigh(date1);
			double kMin1 = stock.getStochasticK(date1);

			double priceMin2 = stock.getHigh(date2);
			double kMin2 = stock.getStochasticK(date2);

			if (priceMin1 < priceMin2 && kMin1 > kMin2) {
				return true;
			}
		}

		return false;
	}

	public static boolean positiveKCross(Stock stock, String date) {
		int timePeriod = stock.getTimePeriod();
		int currentDateIndex = stock.getDates().indexOf(date);
		if (currentDateIndex == -1) {
			currentDateIndex = stock.getDates().indexOf(stock.getClosestDate(date));
		}
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		if (startingDateIndex < 0) { return false; }
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .6);

		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getStochasticK(iDate) < stock.getStochasticD(iDate) * 1.15) {
				countBelownDI++;
			}
		}

		return countBelownDI >= reqPointsBelownDI;
	}

	private static boolean negativeKCross(Stock stock, String date) {
		int timePeriod = stock.getTimePeriod();
		int currentDateIndex = stock.getDates().indexOf(date);
		if (currentDateIndex == -1) {
			currentDateIndex = stock.getDates().indexOf(stock.getClosestDate(date));
		}
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .6);

		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getStochasticK(iDate) > stock.getStochasticD(iDate) * 1.15) {
				countBelownDI++;
			}
		}

		return countBelownDI >= reqPointsBelownDI;
	}
}
