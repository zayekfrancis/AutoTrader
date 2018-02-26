import java.util.ArrayList;
import java.util.Map;

public class Stochastics {

	/*
	 * %K = (Current Close - Lowest Low)/(Highest High - Lowest Low) * 100 %D =
	 * 3-day SMA of %K
	 * 
	 * Lowest Low = lowest low for the look-back period Highest High = highest
	 * high for the look-back period %K is multiplied by 100 to move the decimal
	 * point two places
	 */

	
	public static boolean calculateStochastics(Stock stock, int timePeriod, int smaSmoothingK, int smaSmoothingD) {
		int startingDateIndex = timePeriod - 1;
		int endingDateIndex = stock.getDates().size() - 1;

		for (int i = startingDateIndex; i <= endingDateIndex; i++) {
			String date = stock.getDates().get(i);
			double basicK = calculateBasicK(stock, date, timePeriod);
			if (basicK > 100) {
				basicK = 100;
			}
			else if (basicK < 0) {
				basicK = 0;
			}
			stock.setBasicStochasticK(date, basicK);
			
			double stochasticK = calculateK(stock, date, smaSmoothingK);
			stock.setStochasticK(date, stochasticK);
			
			double stochasticD = calculateD(stock, date, smaSmoothingD);
			stock.setStochasticD(date, stochasticD);
		}
		return true;
	}

	private static double calculateBasicK(Stock stock, String date, int timePeriod) {
		int endingIndex = stock.getDates().indexOf(date);
		int startingIndex = endingIndex - timePeriod + 1;
		
		double currentClose = stock.getClose(date);
		double extremeLow = getExtremeLow(stock.getLowPrices(), stock.getDates(), startingIndex, endingIndex);
		double extremeHigh = getExtremeHigh(stock.getHighPrices(), stock.getDates(), startingIndex, endingIndex);
		
		double k = 100 * (currentClose - extremeLow) / (extremeHigh - extremeLow) ;
		return k;
	}

	private static double calculateK(Stock stock, String date, int smaSmoothing) {
		int endingIndex = stock.getDates().indexOf(date);
		int startingIndex = endingIndex - (smaSmoothing - 1);
		ArrayList<Double> values = new ArrayList<Double>();
		
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			values.add(stock.getBasicStochasticK(iDate));
		}
		double smoothedK = smooth(stock, values, date, smaSmoothing);
		return smoothedK;
	}
	
	private static double calculateD(Stock stock, String date, int smaSmoothing) {
 		int endingIndex = stock.getDates().indexOf(date);
		int startingIndex = endingIndex - (smaSmoothing - 1);
		ArrayList<Double> values = new ArrayList<Double>();
		
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			values.add(stock.getStochasticK(iDate));
		}
		double smoothedD = smooth(stock, values, date, smaSmoothing);
		return smoothedD;
	}

	private static double smooth(Stock stock, ArrayList<Double> values, String date, int smaSmoothing) {
		double sum = 0;
		for (double value : values) {
			sum += value;
		}

		double sma = sum / smaSmoothing;
		return sma;
	}

	private static double getExtremeHigh(Map<String, Double> highs, ArrayList<String> dates, int startingIndex,
			int endingIndex) {
		String startingDate = dates.get(startingIndex);
		double extremePoint = highs.get(startingDate);
		for (int i = startingIndex; i <= endingIndex; i++) {
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
		for (int i = startingIndex; i <= endingIndex; i++) {
			String currentDate = dates.get(i);
			double currentLow = lows.get(currentDate);
			if (currentLow < extremeLow) {
				extremeLow = currentLow;
			}
		}
		return extremeLow;
	}
}
