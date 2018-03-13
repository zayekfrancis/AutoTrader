import java.util.ArrayList;

public class CommodityChannelIndex {

	/*
	 * CCI = (Typical Price - 20-period SMA of TP) / (.015 x Mean Deviation)
	 * 
	 * Typical Price (TP) = (High + Low + Close)/3
	 * 
	 * Constant = .015
	 * 
	 */
	public static boolean calculateCCI(Stock stock) {
		ArrayList<Double> typicalPrices = new ArrayList<Double>();

		int startDateIndex = 0;
		int endDateIndex = stock.getTimePeriod() - 2;

		for (int i = startDateIndex; i <= endDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			double high = stock.getHigh(iDate);
			double low = stock.getLow(iDate);
			double close = stock.getClose(iDate);

			double typicalPrice = calculateTypicalPrice(high, low, close);
			typicalPrices.add(typicalPrice);
		}

		int startingIndex = stock.getTimePeriod() - 1;
		int endingIndex = stock.getDates().size() - 1;
		for (int x = startingIndex; x <= endingIndex; x++) {
			// 1) Calculate Typical Price
			String iDate = stock.getDates().get(x);
			iDate = stock.getClosestDate(iDate);
			double high = stock.getHigh(iDate);
			double low = stock.getLow(iDate);
			double close = stock.getClose(iDate);

			double typicalPrice = calculateTypicalPrice(high, low, close);
			typicalPrices.add(typicalPrice);
			
			// 2) Calculate TimePeriod SMA of TP
			double sumPrices = 0;
			for (int y = (x - stock.getTimePeriod() + 1); y <= x; y++) {
				sumPrices += typicalPrices.get(y);
			}
			double tpSMA = sumPrices / stock.getTimePeriod();
			
			// 3) Set Constant
			double constant = 0.015;
			
			// 4) Calculate Mean Deviation
			int startI = typicalPrices.size() - stock.getTimePeriod();
			int endI = typicalPrices.size() - 1;
			double meanDeviation = calculateMeanDeviation(stock, tpSMA, typicalPrices, startI, endI);
			
			// Calculate CCI
			double cci = (typicalPrice - tpSMA) / (constant * meanDeviation);
			stock.setCCI(iDate, cci);
		}

		return true;
	}

	/*
	 * There are four steps to calculating the Mean Deviation: 
	 * 
	 * First, subtract the most recent 20-period average of the 
	 * typical price from each period's typical price. 
	 * 
	 * Second, take the absolute values of these numbers. Third,
	 * sum the absolute values. Fourth, divide by the total number of periods
	 * (20).
	 */
	private static double calculateMeanDeviation(Stock stock, double recentSMA, ArrayList<Double> typicalPrices, int startingIndex, int endingIndex) {
		double absoluteSum = 0;
		for (int i = startingIndex; i <= endingIndex; i++) {
			double iterativeSMA = typicalPrices.get(i);
			double diffSMA = recentSMA - iterativeSMA;
			double absDiffSMA = Math.abs(diffSMA);
			absoluteSum += absDiffSMA;
		}
		
		double meanDeviation = absoluteSum / stock.getTimePeriod();
		return meanDeviation;
	}

	private static double calculateTypicalPrice(double high, double low, double close) {
		return (high + low + close) / 3;
	}

}
