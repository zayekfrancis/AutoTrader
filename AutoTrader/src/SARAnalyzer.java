import java.util.ArrayList;
import java.util.Map;

public class SARAnalyzer {
	final static int RISING_SAR = 1;
	final static int FALLING_SAR = 0;

	public static boolean earlyPositiveReversal(Stock stock, String date) {
		int startingIndex = stock.getDates().indexOf(date) - stock.getTimePeriod() / 4;
		int endingIndex = stock.getDates().indexOf(date);
		
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getSAR(iDate) > stock.getHigh(iDate)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean earlyNegativeReversal(Stock stock, String date) {
		int startingIndex = stock.getDates().indexOf(date) - stock.getTimePeriod() / 4;
		int endingIndex = stock.getDates().indexOf(date);
		
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getSAR(iDate) < stock.getHigh(iDate)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static double getRatio(Stock stock, String date) {
			if (stock.getLog().isNonTrendingBuy()){
				if (stock.getNTSAR(date) < stock.getClose(date)) {
					return stock.getNTSAR(date);
				}
				else {
					return 0;
				}
			}
			return stock.getSAR(date);
	}
	
	public static boolean calculateSAR(Stock stock, int timePeriod, double baseAF, double incrementAF, double maxAF) {
		timePeriod = 3;
		ArrayList<String> dates = stock.getDates();
		String firstDate = stock.getDates().get(timePeriod);
		double extremeLow = getExtremeLow(stock.getLowPrices(), dates, 0, timePeriod);
		double accelerationFactor = baseAF;
		double currentSAR = extremeLow;

		double previousSAR = currentSAR;
		double extremePoint = getExtremeHigh(stock.getHighPrices(), dates, 0, timePeriod);
		String date = firstDate;

		boolean reversal = false;
		boolean first = true;
		int currentTrend = FALLING_SAR;
		int baseIndex = 0;
		int lastPeriodRange = 0;
		int beginningOfTrend = 0;
		int firstDateIndex = stock.getDates().indexOf(firstDate);
		for (int i = firstDateIndex + 1; i <= stock.getDates().size(); i++) {
			double high = stock.getHigh(date);
			double low = stock.getLow(date);
			if (first) {
				double lowSAR = getExtremeLow(stock.getLowPrices(), dates, 0, timePeriod);
				double highSAR = getExtremeHigh(stock.getHighPrices(), dates, 0, timePeriod);
				
				if (low > lowSAR) {
					currentTrend = RISING_SAR;
					previousSAR = lowSAR;
				}
				else {
					currentTrend = FALLING_SAR;
					previousSAR = highSAR;
				}
			}
			
			reversal = (currentTrend == FALLING_SAR && high > previousSAR && !first);
			// rising SAR
			if (reversal || low > previousSAR) {
				if (first) {
					beginningOfTrend = i - 1;
				}
				first = false;
				currentTrend = RISING_SAR;
				boolean reset = false;
				if (reversal) {
					beginningOfTrend = i - 1;
					baseIndex = i - lastPeriodRange - 1;
					lastPeriodRange = 0;
					accelerationFactor = baseAF;
					extremePoint = getExtremeHigh(stock.getHighPrices(), dates, beginningOfTrend, i);
					currentSAR = getExtremeLow(stock.getLowPrices(), dates, baseIndex, i);
					reset = true;
				}
				if (reset) {
					stock.setSAR(date, currentSAR);
				} else {
					currentSAR = calculateAndSetRisingSAR(stock, date, previousSAR, accelerationFactor, extremePoint);
				}

				// Update values for next iteration
				if (i != stock.getDates().size()) {
					date = stock.getDates().get(i);
				}
				previousSAR = currentSAR;
				double previousEP = extremePoint;
				extremePoint = getExtremeHigh(stock.getHighPrices(), dates, beginningOfTrend, i);
				if (extremePoint > previousEP) {
					accelerationFactor = ((accelerationFactor + incrementAF) >= maxAF) ? maxAF : (accelerationFactor + incrementAF);
				}
				lastPeriodRange++;
				continue;
			}
			reversal = (currentTrend == RISING_SAR && low <= previousSAR);
			// falling SAR
			if (reversal || high <= previousSAR) { 
				if (first) {
					beginningOfTrend = i - 1;
				}
				first = false;
				currentTrend = FALLING_SAR;
				boolean reset = false;
				if (reversal) {
					beginningOfTrend = i - 1;
					baseIndex = i - lastPeriodRange - 1;
					lastPeriodRange = 0;
					accelerationFactor = baseAF;
					extremePoint = getExtremeLow(stock.getLowPrices(), dates, beginningOfTrend, i);
					currentSAR = getExtremeHigh(stock.getHighPrices(), dates, baseIndex, i);
					reset = true;
				}
				if (reset) {
					stock.setSAR(date, currentSAR);
				} else {
					currentSAR = calculateAndSetFallingSAR(stock, date, previousSAR, accelerationFactor, extremePoint);
				}
				
				// Update values for next iteration
				if (i != stock.getDates().size()) {
					date = stock.getDates().get(i);
				}
				previousSAR = currentSAR;
				double previousEP = extremePoint;
				extremePoint = getExtremeLow(stock.getLowPrices(), dates, beginningOfTrend, i);
				if (extremePoint < previousEP) {
					accelerationFactor = ((accelerationFactor + incrementAF) >= maxAF) ? maxAF : (accelerationFactor + incrementAF);
				}
				lastPeriodRange++;
				continue;
			}
			if (i != stock.getDates().size()) {
				date = stock.getDates().get(i);
			}
		}
		return true;
	}
	
	public static boolean calculateNonTrendingSAR(Stock stock, int timePeriod, double baseAF, double incrementAF, double maxAF) {
		timePeriod = 3;
		ArrayList<String> dates = stock.getDates();
		String firstDate = stock.getDates().get(timePeriod);
		double extremeLow = getExtremeLow(stock.getLowPrices(), dates, 0, timePeriod);
		double accelerationFactor = baseAF;
		double currentSAR = extremeLow;

		double previousSAR = currentSAR;
		double extremePoint = getExtremeHigh(stock.getHighPrices(), dates, 0, timePeriod);
		String date = firstDate;

		boolean reversal = false;
		boolean first = true;
		int currentTrend = FALLING_SAR;
		int baseIndex = 0;
		int lastPeriodRange = 0;
		int beginningOfTrend = 0;
		int firstDateIndex = stock.getDates().indexOf(firstDate);
		for (int i = firstDateIndex + 1; i <= stock.getDates().size(); i++) {
			double high = stock.getHigh(date);
			double low = stock.getLow(date);
			if (first) {
				double lowSAR = getExtremeLow(stock.getLowPrices(), dates, 0, timePeriod);
				double highSAR = getExtremeHigh(stock.getHighPrices(), dates, 0, timePeriod);
				
				if (low > lowSAR) {
					currentTrend = RISING_SAR;
					previousSAR = lowSAR;
				}
				else {
					currentTrend = FALLING_SAR;
					previousSAR = highSAR;
				}
			}
			
			reversal = (currentTrend == FALLING_SAR && high > previousSAR && !first);
			// rising SAR
			if (reversal || low > previousSAR) {
				if (first) {
					beginningOfTrend = i - 1;
				}
				first = false;
				currentTrend = RISING_SAR;
				boolean reset = false;
				if (reversal) {
					beginningOfTrend = i - 1;
					baseIndex = i - lastPeriodRange - 1;
					lastPeriodRange = 0;
					accelerationFactor = baseAF;
					extremePoint = getExtremeHigh(stock.getHighPrices(), dates, beginningOfTrend, i);
					currentSAR = getExtremeLow(stock.getLowPrices(), dates, baseIndex, i);
					reset = true;
				}
				if (reset) {
					stock.setNTSAR(date, currentSAR);
				} else {
					currentSAR = calculateAndSetRisingNTSAR(stock, date, previousSAR, accelerationFactor, extremePoint);
				}

				// Update values for next iteration
				if (i != stock.getDates().size()) {
					date = stock.getDates().get(i);
				}
				previousSAR = currentSAR;
				double previousEP = extremePoint;
				extremePoint = getExtremeHigh(stock.getHighPrices(), dates, beginningOfTrend, i);
				if (extremePoint > previousEP) {
					accelerationFactor = ((accelerationFactor + incrementAF) >= maxAF) ? maxAF : (accelerationFactor + incrementAF);
				}
				lastPeriodRange++;
				continue;
			}
			reversal = (currentTrend == RISING_SAR && low <= previousSAR);
			// falling SAR
			if (reversal || high <= previousSAR) { 
				if (first) {
					beginningOfTrend = i - 1;
				}
				first = false;
				currentTrend = FALLING_SAR;
				boolean reset = false;
				if (reversal) {
					beginningOfTrend = i - 1;
					baseIndex = i - lastPeriodRange - 1;
					lastPeriodRange = 0;
					accelerationFactor = baseAF;
					extremePoint = getExtremeLow(stock.getLowPrices(), dates, beginningOfTrend, i);
					currentSAR = getExtremeHigh(stock.getHighPrices(), dates, baseIndex, i);
					reset = true;
				}
				if (reset) {
					stock.setNTSAR(date, currentSAR);
				} else {
					currentSAR = calculateAndSetFallingNTSAR(stock, date, previousSAR, accelerationFactor, extremePoint);
				}
				
				// Update values for next iteration
				if (i != stock.getDates().size()) {
					date = stock.getDates().get(i);
				}
				previousSAR = currentSAR;
				double previousEP = extremePoint;
				extremePoint = getExtremeLow(stock.getLowPrices(), dates, beginningOfTrend, i);
				if (extremePoint < previousEP) {
					accelerationFactor = ((accelerationFactor + incrementAF) >= maxAF) ? maxAF : (accelerationFactor + incrementAF);
				}
				lastPeriodRange++;
				continue;
			}
			if (i != stock.getDates().size()) {
				date = stock.getDates().get(i);
			}
		}
		return true;
	}

	public static double calculateAndSetRisingSAR(Stock stock, String date, double previousSAR,
			double accelerationFactor, double extremePoint) {
		int currentDateIndex = stock.getDates().indexOf(date);
		double twoPeriodPreviousLow = stock.getLow(stock.getDates().get(currentDateIndex - 2));
		double previousLow = stock.getLow(stock.getDates().get(currentDateIndex - 1));
		previousLow = Math.min(twoPeriodPreviousLow, previousLow);
		// Current SAR = Prior SAR + Prior AF(Prior EP - Prior SAR)
		double currentSAR = previousSAR + accelerationFactor * (extremePoint - previousSAR);
		currentSAR = Math.min(currentSAR, previousLow);
		stock.setSAR(date, currentSAR);
		return currentSAR;
	}

	public static double calculateAndSetFallingSAR(Stock stock, String date, double previousSAR,
			double accelerationFactor, double extremePoint) {
		int currentDateIndex = stock.getDates().indexOf(date);
		double twoPeriodPreviousHigh = stock.getHigh(stock.getDates().get(currentDateIndex - 2));
		double previousHigh = stock.getHigh(stock.getDates().get(currentDateIndex - 1));
		previousHigh = Math.max(twoPeriodPreviousHigh, previousHigh);
		// Current SAR = Prior SAR - Prior AF(Prior EP - Prior SAR)
		double currentSAR = previousSAR - accelerationFactor * (previousSAR - extremePoint);
		currentSAR = Math.max(currentSAR, previousHigh);
		stock.setSAR(date, currentSAR);
		return currentSAR;
	}
	
	public static double calculateAndSetRisingNTSAR(Stock stock, String date, double previousSAR,
			double accelerationFactor, double extremePoint) {
		int currentDateIndex = stock.getDates().indexOf(date);
		double twoPeriodPreviousLow = stock.getLow(stock.getDates().get(currentDateIndex - 2));
		double previousLow = stock.getLow(stock.getDates().get(currentDateIndex - 1));
		previousLow = Math.min(twoPeriodPreviousLow, previousLow);
		// Current SAR = Prior SAR + Prior AF(Prior EP - Prior SAR)
		double currentSAR = previousSAR + accelerationFactor * (extremePoint - previousSAR);
		currentSAR = Math.min(currentSAR, previousLow);
		stock.setNTSAR(date, currentSAR);
		return currentSAR;
	}

	public static double calculateAndSetFallingNTSAR(Stock stock, String date, double previousSAR,
			double accelerationFactor, double extremePoint) {
		int currentDateIndex = stock.getDates().indexOf(date);
		double twoPeriodPreviousHigh = stock.getHigh(stock.getDates().get(currentDateIndex - 2));
		double previousHigh = stock.getHigh(stock.getDates().get(currentDateIndex - 1));
		previousHigh = Math.max(twoPeriodPreviousHigh, previousHigh);
		// Current SAR = Prior SAR - Prior AF(Prior EP - Prior SAR)
		double currentSAR = previousSAR - accelerationFactor * (previousSAR - extremePoint);
		currentSAR = Math.max(currentSAR, previousHigh);
		stock.setNTSAR(date, currentSAR);
		return currentSAR;
	}

	private static double getExtremeHigh(Map<String, Double> highs, ArrayList<String> dates, int startingIndex,
			int endingIndex) {
		String startingDate = dates.get(startingIndex);
		double extremePoint = highs.get(startingDate);
		for (int i = startingIndex; i < endingIndex; i++) {
			String currentDate = dates.get(i);
			double currentHigh = highs.get(currentDate);
			if (currentHigh > extremePoint) {
				extremePoint = currentHigh;
			}
		}
		return extremePoint;
	}

	private static double getExtremeLow(Map<String, Double> lows, ArrayList<String> dates, int startingIndex,
			int endingIndex) {
		String startingDate = dates.get(startingIndex);
		double extremeLow = lows.get(startingDate);
		for (int i = startingIndex; i < endingIndex; i++) {
			String currentDate = dates.get(i);
			double currentLow = lows.get(currentDate);
			if (currentLow < extremeLow) {
				extremeLow = currentLow;
			}
		}
		return extremeLow;
	}
}
