
public class TrendAnalyzer {
	/*
	 * 1) Check if price is above the SMA(50) 
	 * 2) Check if SMA(50) has started to trend upwards 
	 * 3) Check ADX > 20 
	 * 4) Check if difference of +DI and -DI > 10 
	 * 	  and was the change/cross recent 
	 * 5) SAR is below current price
	 */
	public static boolean isPositivelyTrending(Stock stock, String date) {
		// 1) Check if price is above the SMA(50)
		boolean priceAboveSMA = false;
		double currentPrice = stock.getClose(date);
		double sma = stock.getFiftySMA(date);
		priceAboveSMA = currentPrice > sma;

		// 2) Check if SMA(50) has started to trend upwards
		boolean movingUpwards = isSmaMovingUpwards(stock, date);

		// 3) Check ADX > 20
		// 4) Check if difference of +DI and -DI > 10 
		//	  and was the change/cross recent
		boolean adxAndDIBuySignal = ADXAnalyzer.buySignal(stock, stock.getTimePeriod(), date);

		// 5) SAR is below current price
		boolean SARSignal = currentPrice > stock.getSAR(date);
		return (priceAboveSMA && movingUpwards && adxAndDIBuySignal && SARSignal);
	}

	/*
	 * 1) If 75% of the early prices were below the moving average 
	 * 2) If 80% of the the newer prices are above the min moving average
	 */
	private static boolean isSmaMovingUpwards(Stock stock, String date) {
		int timePeriod = stock.getTimePeriod();
		int endingIndex = stock.getDates().indexOf(date) - 14;
		int startingIndex = endingIndex - 49;
		if (startingIndex < 0) {
			return false;
		}

		// 1) If at least 75% of the early prices were below the moving average
		int underCount = 0;
		double min = 0;
		try {
			min = stock.getSMA(stock.getDates().get(startingIndex));
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			double sma = stock.getSMA(iDate);
			if (sma > stock.getClose(iDate)) {
				underCount++;
			}
			
			if (sma < min) {
				sma = min;
			}
		}
 
		if (underCount < (endingIndex - startingIndex) * .75) {
			//return false;
		}

		// 2) If 80% of the the newer prices are above the min moving average
		endingIndex = stock.getDates().indexOf(date);
		startingIndex = endingIndex - timePeriod + 1;
		int upwardsCount = 0;
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			double sma = stock.getSMA(iDate);
			if (sma > min) {
				upwardsCount++;
			}
		}

		if (upwardsCount > timePeriod * .8) {
			return true;
		}
		return false;
	}

}
