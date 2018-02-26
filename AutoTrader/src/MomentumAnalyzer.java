
public class MomentumAnalyzer {
	
	public static boolean buySignal(Stock stock, String date) {
		boolean macDMomentum = MACDMomentumExists(stock, date);
		boolean rsiMomentum = rsiMomentumExists(stock, date);
		boolean rsiDivergence = findDivergence(stock, date);
		return (macDMomentum || rsiMomentum || rsiDivergence);
	}
	
	private static boolean MACDMomentumExists(Stock stock, String date) {
		String closestDate = stock.getClosestDate(date);
		double currentMACDHist = stock.getMACDHistogram(closestDate);
		double macd = stock.getMACDLine(closestDate);
		double signalline = stock.getMACDSignal(closestDate);
		int previousDateIndex = stock.getDates().indexOf(closestDate) - 1;
		String previousDate = stock.getDates().get(previousDateIndex);
		double previousMACDHist = stock.getMACDHistogram(previousDate);
		int previouspreviousDateIndex = stock.getDates().indexOf(previousDate) - 1;
		String previouspreviousDate = stock.getDates().get(previouspreviousDateIndex);
		double previouspreviousMACDHist = stock.getMACDHistogram(previouspreviousDate);
		if (currentMACDHist + previousMACDHist + previouspreviousMACDHist > .1) {
			return true;
		}
		return false;
	}
	
	private static boolean rsiMomentumExists(Stock stock, String date) {
		String closestDate = stock.getClosestDate(date);
		int timePeriod = stock.getTimePeriod();
		int threshold = 50;
		boolean recentRSICross = (stock.getRSI(closestDate) > threshold) && (RSIAnalyzer.positiveRSICross(stock, closestDate, timePeriod, threshold));
		return recentRSICross;
	}
	
	private static boolean findDivergence(Stock stock, String date) {
		date = stock.getClosestDate(date);
		int endingIndex2 = stock.getDates().indexOf(date);
		int startingIndex2 = endingIndex2 - stock.getTimePeriod() / 5;
		int endingIndex1 = startingIndex2 - 1;
		int startingIndex1 = endingIndex1 - stock.getTimePeriod() / 5;

		double priceMin1 = stock.getLow(stock.getDates().get(startingIndex1));
		double kMin1 = stock.getRSI(stock.getDates().get(startingIndex1));
		for (int i = startingIndex1; i <= endingIndex1; i++) {
			String iDate = stock.getDates().get(i);
			double currentLowPrice = stock.getLow(iDate);
			if (currentLowPrice < priceMin1) {
				priceMin1 = currentLowPrice;
				kMin1 = stock.getRSI(stock.getDates().get(i));
			}
		}

		double priceMin2 = stock.getLow(stock.getDates().get(startingIndex2));
		double kMin2 = stock.getRSI(stock.getDates().get(startingIndex2));
		for (int i = startingIndex1; i <= endingIndex1; i++) {
			String iDate = stock.getDates().get(i);
			double currentLowPrice = stock.getLow(iDate);
			if (currentLowPrice < priceMin2) {
				priceMin2 = currentLowPrice;
				kMin2 = stock.getRSI(stock.getDates().get(i));
			}
		}

		if (priceMin1 > priceMin2 && kMin1 < kMin2) {
			return true;
		}

		return false;
	}
}
