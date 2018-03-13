import java.util.ArrayList;

public class MoneyFlowIndex {
	/*
	 * Typical Price = (High + Low + Close)/3 Raw Money Flow = Typical Price x
	 * Volume Money Flow Ratio = (14-period Positive Money Flow)/(14-period
	 * Negative Money Flow) Money Flow Index = 100 - 100/(1 + Money Flow Ratio)
	 */

	public static boolean calculateMFI(Stock stock) {
		ArrayList<Double> typicalPrices = new ArrayList<Double>();
		ArrayList<Double> positiveMF = new ArrayList<Double>();
		ArrayList<Double> negativeMF = new ArrayList<Double>();

		String firstDate = stock.getDates().get(0);
		String secondDate = stock.getDates().get(1);

		double firstHigh = stock.getHigh(firstDate);
		double firstLow = stock.getLow(firstDate);
		double firstClose = stock.getClose(firstDate);

		double secondHigh = stock.getHigh(secondDate);
		double secondLow = stock.getLow(secondDate);
		double secondClose = stock.getClose(secondDate);
		double secondVolume = stock.getVolume(secondDate);

		double firstTypicalPrice = calculateTypicalPrice(firstHigh, firstLow, firstClose);
		typicalPrices.add(firstTypicalPrice);

		double secondTypicalPrice = calculateTypicalPrice(secondHigh, secondLow, secondClose);
		double secondRawMoneyFlow = calculateRawMoneyFlow(secondTypicalPrice, secondVolume);
		typicalPrices.add(secondTypicalPrice);

		if (secondTypicalPrice > firstTypicalPrice) {
			positiveMF.add(secondRawMoneyFlow);
			negativeMF.add(0.0);
		} else if (secondTypicalPrice < firstTypicalPrice) {
			positiveMF.add(0.0);
			negativeMF.add(secondRawMoneyFlow);
		} else {
			positiveMF.add(0.0);
			negativeMF.add(0.0);
		}

		int startIndex = stock.getDates().indexOf(stock.getDates().get(2));
		int endIndex = stock.getDates().indexOf(stock.getDates().get(stock.getDates().size() - 1));

		for (int i = startIndex; i <= endIndex; i++) {
			String iDate = stock.getDates().get(i);
			double high = stock.getHigh(iDate);
			double low = stock.getLow(iDate);
			double close = stock.getClose(iDate);
			double volume = stock.getVolume(iDate);

			double previousTypicalPrice = typicalPrices.get(i - 1);
			double typicalPrice = calculateTypicalPrice(high, low, close);
			typicalPrices.add(typicalPrice);
			double rawMoneyFlow = calculateRawMoneyFlow(typicalPrice, volume);

			if (typicalPrice > previousTypicalPrice) {
				positiveMF.add(rawMoneyFlow);
				negativeMF.add(0.0);
			} else if (typicalPrice < previousTypicalPrice) {
				positiveMF.add(0.0);
				negativeMF.add(rawMoneyFlow);
			} else {
				positiveMF.add(0.0);
				negativeMF.add(0.0);
			}

			int timePeriod = stock.getTimePeriod();
			
			if (i >= timePeriod) {
				int startingIndex = i - timePeriod;
				int endingIndex = i - 1;
				double positiveSumRatios = 0;
				double negativeSumRatios = 0;
				for (int x = startingIndex; x <= endingIndex; x++) {
					double pRatio = positiveMF.get(x);
					double nRatio = negativeMF.get(x);
					positiveSumRatios += pRatio;
					negativeSumRatios += nRatio;
				}
				
				double timePeriodMFRatio = positiveSumRatios / negativeSumRatios;
				double mfi = calculateOneMFI(timePeriodMFRatio);
				stock.setMFI(iDate, mfi);
			}
		}
		return true;
	}
	
	public static boolean buySignal(Stock stock, String date, double threshold, boolean fractal) {
		if (fractal) {
			return stock.getMFI(date) > threshold;
		}
		return findPositiveDivergence(stock, date) & positiveMFICross(stock, date, stock.getTimePeriod(), threshold);
	}
	
	public static boolean findPositiveDivergence(Stock stock, String date) {
		date = stock.getClosestDate(date);
		String date1 = "";
		String date2 = "";
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
				array1[i - startingIndex1] = stock.getMFI(iDate);
			}
			for (int i = startingIndex2; i <= endingIndex2; i++) {
				String iDate = stock.getDates().get(i);
				array2[i - startingIndex2] = stock.getMFI(iDate);
			}

			int bottom1 = Util.findBottoms(array1, array1.length);
			int bottom2 = Util.findBottoms(array2, array2.length);

			if (bottom1 != -1 && bottom2 != -1) {
				// Bottoms found
				int dateIndex1 = startingIndex1 + bottom1;
				int dateIndex2 = startingIndex2 + bottom2;
				if (dateIndex1 < 0 || dateIndex2 < 0) { return false; }
				date1 = stock.getDates().get(dateIndex1);
				date2 = stock.getDates().get(dateIndex2);
			} else {
				continue;
			}

			// Check if divergence exists
			double priceMin1 = stock.getLow(date1);
			double mfiMin1 = stock.getMFI(date1);

			double priceMin2 = stock.getLow(date2);
			double mfiMin2 = stock.getMFI(date2);

			if (priceMin1 > priceMin2 
				&& mfiMin1 < mfiMin2
				&& mfiMin1 < 50
				&& mfiMin2 < 50) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean positiveMFICross(Stock stock, String date, int timePeriod, double threshold) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .5);
		
		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getMFI(iDate) < threshold) {
				countBelownDI++;
			}
		}
		
		return (stock.getMFI(date) > threshold) && countBelownDI >= reqPointsBelownDI;
	}

	public static boolean negativeMFICross(Stock stock, String date, int timePeriod, double threshold) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .5);
		
		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getMFI(iDate) > threshold) {
				countBelownDI++;
			}
		}
		
		return (stock.getMFI(date) < threshold) && countBelownDI >= reqPointsBelownDI;
	}

	// 100 - 100/(1 + Money Flow Ratio)
	private static double calculateOneMFI(double mfi) {
		return 100 - 100 / (1 + mfi);
	}
	
	private static double calculateTypicalPrice(double high, double low, double close) {
		return (high + low + close) / 3;
	}

	private static double calculateRawMoneyFlow(double typicalPrice, double volume) {
		return typicalPrice * volume;
	}
}
