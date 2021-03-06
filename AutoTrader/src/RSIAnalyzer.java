import java.util.ArrayList;

public class RSIAnalyzer {
	
	public static boolean rsiMomentumExists(Stock stock, String date, int rsiThreshold, int rsiLimit) {
		String closestDate = stock.getClosestDate(date);
		int timePeriod = stock.getTimePeriod();
		boolean recentRSICross = (stock.getRSI(closestDate) > rsiThreshold && stock.getRSI(closestDate) < rsiLimit)
				&& (RSIAnalyzer.positiveRSICross(stock, closestDate, timePeriod, rsiThreshold));
		return recentRSICross;
	}

	public static boolean rsiDyingMomentum(Stock stock, String date, int rsiThreshold) {
		String closestDate = stock.getClosestDate(date);
		int timePeriod = stock.getTimePeriod();
		boolean recentRSICross = (stock.getRSI(closestDate) < rsiThreshold)
				&& (RSIAnalyzer.negativeRSICross(stock, closestDate, timePeriod, rsiThreshold));
		return recentRSICross;
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
				array1[i - startingIndex1] = stock.getRSI(iDate);
			}
			for (int i = startingIndex2; i <= endingIndex2; i++) {
				String iDate = stock.getDates().get(i);
				array2[i - startingIndex2] = stock.getRSI(iDate);
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
			double rsiMin1 = stock.getRSI(date1);

			double priceMin2 = stock.getLow(date2);
			double rsiMin2 = stock.getRSI(date2);

			if (priceMin1 > priceMin2 
				&& rsiMin1 < rsiMin2
				&& rsiMin1 < 50
				&& rsiMin2 < 50) {
				return true;
			}

		}

		return false;
	}

	public static boolean findNegativeDivergence(Stock stock, String date) {
		date = stock.getClosestDate(date);
		for (double divider = 1.75; divider < stock.getTimePeriod() / 4; divider = (divider + .25)) {

			// Find 2 ranges to check for different peaks
			int endingIndex2 = stock.getDates().indexOf(date);
			int startingIndex2 = (int) (endingIndex2 - stock.getTimePeriod() / divider);
			int endingIndex1 = startingIndex2;
			int startingIndex1 = (int) (endingIndex1 - stock.getTimePeriod() / divider);

			// Create the 2 arrays and find the peaks if they exist
			double[] array1 = new double[endingIndex1 - startingIndex1 + 1];
			double[] array2 = new double[endingIndex2 - startingIndex2 + 1];
			for (int i = startingIndex1; i <= endingIndex1; i++) {
				String iDate = stock.getDates().get(i);
				array1[i - startingIndex1] = stock.getRSI(iDate);
			}
			for (int i = startingIndex2; i <= endingIndex2; i++) {
				String iDate = stock.getDates().get(i);
				array2[i - startingIndex2] = stock.getRSI(iDate);
			}

			int peak1 = Util.findPeaks(array1, array1.length);
			int peak2 = Util.findPeaks(array2, array2.length);
			String date1 = "";
			String date2 = "";

			if (peak1 != -1 && peak2 != -1) {
				// Peaks found
				int dateIndex1 = startingIndex1 + peak1;
				int dateIndex2 = startingIndex2 + peak2;
				date1 = stock.getDates().get(dateIndex1);
				date2 = stock.getDates().get(dateIndex2);
			} else {
				continue;
			}

			// Check if divergence exists
			double priceMin1 = stock.getHigh(date1);
			double rsiMin1 = stock.getRSI(date1);

			double priceMin2 = stock.getHigh(date2);
			double rsiMin2 = stock.getRSI(date2);

			if (priceMin1 < priceMin2 && rsiMin1 > rsiMin2) {
				return true;
			}
		}

		return false;
	}
	
	public static boolean nonTrendingBuySignal(Stock stock, String date) {
		date = stock.getClosestDate(date);
		return stock.getRSI(date) < 30;
	}
	
	public static boolean nonTrendingSellSignal(Stock stock, String date) {
		date = stock.getClosestDate(date);
		return stock.getRSI(date) > 70;
	}
	
	public static boolean positiveRSICross(Stock stock, String date, int timePeriod, double threshold) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		if (startingDateIndex < 0) { return false; }
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .5);
		
		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getRSI(iDate) < threshold) {
				countBelownDI++;
			}
		}
		
		return countBelownDI >= reqPointsBelownDI;
	}

	public static boolean negativeRSICross(Stock stock, String date, int timePeriod, double threshold) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .5);
		
		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getRSI(iDate) > threshold) {
				countBelownDI++;
			}
		}
		
		return countBelownDI >= reqPointsBelownDI;
	}
	


}
