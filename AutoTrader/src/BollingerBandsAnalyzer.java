
public class BollingerBandsAnalyzer {

	public static boolean buySignal(Stock stock, String date, boolean fractal) {
		date = stock.getClosestDate(date);
		
		if (fractal) {
			double price = stock.getClose(date);
			boolean positiveFractalTrend = price > stock.getMiddleBBand(date);
			return positiveFractalTrend;
		}
		else {
			return stock.getPercentBBand(date) > .8;
		}
	}

	public static boolean sellSignal(Stock stock, String date, boolean fractal) {

		return false;
	}

	private static boolean bullishExpandingBollingerBand(Stock stock, String date, double narrowAverage) {
		int timePeriod = stock.getTimePeriod() / 4;
		int startingIndex = stock.getDates().indexOf(date) - timePeriod + 1;
		int endingIndex = stock.getDates().indexOf(date);
		int expandingCount = 0;
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			double distance = stock.getBollingerBandwidth(iDate);
			double percentB = stock.getPercentBBand(iDate);
			if (distance > narrowAverage * 1.5 && percentB > .6) {
				expandingCount++;
			}
		}
		return expandingCount == timePeriod;
	}
	
	private static double narrowingBollingerBand(Stock stock, String date) {
		int timePeriod = stock.getTimePeriod();
		int startingIndex = stock.getDates().indexOf(date) - timePeriod + 1;
		int endingIndex = stock.getDates().indexOf(date);
		
		double avgDistance = 0;
		int narrowingCount = 0;
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			double distance = stock.getBollingerBandwidth(iDate);
			if (distance < 0.04) {
				avgDistance += distance;
				narrowingCount++;
			}
		}
		if (narrowingCount > timePeriod / 2) {
			return avgDistance / narrowingCount;	
		}
		return 0;
	}
	
	public static boolean nonTrendingBuySignal(Stock stock, String date) {
		date = stock.getClosestDate(date);
		boolean lowPriceBBBreak = isLowBelowBB(stock, date);
		boolean closeInsideBB = stock.getClose(date) > stock.getLowerBBand(date);
		boolean lowBandWidth = stock.getBollingerBandwidth(date) < .2;
		return lowPriceBBBreak && closeInsideBB && lowBandWidth;
	}
	
	public static boolean nonTrendingSellSignal(Stock stock, String date) {
		date = stock.getClosestDate(date);
		boolean highPriceBBBreak = isHighAboveBB(stock, date);
		boolean closeInsideBB = stock.getClose(date) < stock.getUpperBBand(date);
		return highPriceBBBreak && closeInsideBB;
	}
	
	
	private static boolean isHighAboveBB(Stock stock, String date) {
		if (stock.getHigh(date) > stock.getUpperBBand(date)) {
			return true;
		}
		return false;
	}
	
	private static boolean isLowBelowBB(Stock stock, String date) {
		if (stock.getLow(date) < stock.getLowerBBand(date)) {
			return true;
		}
		return false;
	}
	
	private static boolean isOverbought(Stock stock, String date) {
		if (stock.getPercentBBand(date) > 1) {
			return true;
		}
		return false;
	}

	private static boolean isOversold(Stock stock, String date) {
		if (stock.getPercentBBand(date) < 0) {
			return true;
		}
		return false;
	}
}
