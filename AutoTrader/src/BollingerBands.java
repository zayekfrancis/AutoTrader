import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BollingerBands {

	public static boolean calculateBBands(Stock stock, int timePeriod, int multipler) {
		// Calculate and set middle band
		Map<String, Double> midMap = new HashMap<String, Double>();
		midMap = MovingAverageAnalyzer.calculateSMA(stock, midMap, timePeriod);
		for (String date : stock.getDates()) {
			stock.setMiddleBBand(date, midMap.get(date));
		}

		// Calculate and set upper and lower bands
		calculateAndSetUpperAndLower(stock, timePeriod, multipler);
		
		// Calculate and set %B
		calculateAndSetPercentB(stock);
		
		// Calculate and set Bollinger BandWidth
		calculateAndSetBandWidth(stock);
		return true;
	}

	private static void calculateAndSetUpperAndLower(Stock stock, int timePeriod, int multipler) {
		ArrayList<Double> prices = new ArrayList<Double>();
		for (String date : stock.getDates()) {
			prices.add(stock.getClose(date));
		}

		Map<String, Double> standardDeviations = new HashMap<String, Double>();
		int startingIndex = timePeriod - 1;
		int endingIndex = prices.size() - 1;
		for (int x = startingIndex; x <= endingIndex; x++) {
			double sumPrices = 0;
			String date = stock.getDates().get(x);
			for (int y = (x - timePeriod + 1); y <= x; y++) {
				String insideDate = stock.getDates().get(y);
				double currentPrice = stock.getClose(insideDate);
				double middleBandPrice = stock.getMiddleBBand(date);
				sumPrices += Math.pow(currentPrice - middleBandPrice, 2);
			}
			double sma = sumPrices / timePeriod;
			double standardDeviation = Math.sqrt(sma);
			standardDeviations.put(date, standardDeviation);

			sma = stock.getMiddleBBand(date);
			double upperBBandValue = sma + (standardDeviation * multipler);
			double lowerBBandValue = sma - (standardDeviation * multipler);
			stock.setUpperBBand(date, upperBBandValue);
			stock.setLowerBBand(date, lowerBBandValue);
		}
	}

	// %B = (Price - Lower Band)/(Upper Band - Lower Band)
	private static void calculateAndSetPercentB(Stock stock) {
		for (String date : stock.getDates()) {
			double currentPrice = stock.getClose(date);
			double upperBandValue = stock.getUpperBBand(date);
			double lowerBandValue = stock.getLowerBBand(date);
			
			double percentB = (currentPrice - lowerBandValue) / (upperBandValue - lowerBandValue);
			stock.setPercentBBand(date, percentB);
		}
	}
	
	// Bollinger BandWidth = Upper Band - LowerBand
		private static void calculateAndSetBandWidth(Stock stock) {
			for (String date : stock.getDates()) {
				double currentPrice = stock.getClose(date);
				double upperBandValue = stock.getUpperBBand(date);
				double lowerBandValue = stock.getLowerBBand(date);
				double middleBandValue = stock.getMiddleBBand(date);
				
				double bandwidth = ((upperBandValue - lowerBandValue) / middleBandValue) * 100;
				double ratio = bandwidth / stock.getClose(date);
				stock.setBollingerBandwidth(date, ratio);
			}
		}
}
