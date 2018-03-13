
public class CCIAnalyzer {

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
				array1[i - startingIndex1] = stock.getCCI(iDate);
			}
			for (int i = startingIndex2; i <= endingIndex2; i++) {
				String iDate = stock.getDates().get(i);
				array2[i - startingIndex2] = stock.getCCI(iDate);
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
			double cciMin1 = stock.getCCI(date1);

			double priceMin2 = stock.getLow(date2);
			double cciMin2 = stock.getCCI(date2);

			if (priceMin1 > priceMin2 
				&& cciMin1 < cciMin2
				&& cciMin1 < 0
				&& cciMin2 < 0) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean findNegativeDivergence(Stock stock, String date) {
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
				array1[i - startingIndex1] = stock.getCCI(iDate);
			}
			for (int i = startingIndex2; i <= endingIndex2; i++) {
				String iDate = stock.getDates().get(i);
				array2[i - startingIndex2] = stock.getCCI(iDate);
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
			double priceMax1 = stock.getHigh(date1);
			double cciMin1 = stock.getCCI(date1);

			double priceMax2 = stock.getHigh(date2);
			double cciMin2 = stock.getCCI(date2);

			if (priceMax1 < priceMax2 
				&& cciMin1 > cciMin2
				&& cciMin1 > 100
				&& cciMin2 > 100) {
				return true;
			}
		}

		return false;
	}

	public static boolean recentCCIHigh(Stock stock, String date, double threshold) {
		int timePeriod = stock.getTimePeriod();
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		
		boolean extremeHighExists = false;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getCCI(iDate) > threshold) {
				extremeHighExists = true;
			}
		}
		
		return extremeHighExists;
	}
	
	public static boolean positiveCCICross(Stock stock, String date, int timePeriod, double threshold) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 4);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .5);
		
		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getCCI(iDate) < threshold) {
				countBelownDI++;
			}
		}
		
		return (stock.getCCI(date) > threshold) && countBelownDI >= reqPointsBelownDI;
	}

	public static boolean negativeCCICross(Stock stock, String date, int timePeriod, double threshold) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .5);
		
		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getCCI(iDate) > threshold) {
				countBelownDI++;
			}
		}
		
		return (stock.getCCI(date) < threshold) && countBelownDI >= reqPointsBelownDI;
	}
}
