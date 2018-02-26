
public class StochasticsAnalyzer {

	public static boolean buySignal(Stock stock, String date, boolean fractal) {
		boolean momentum = stock.getStochasticK(date) > stock.getStochasticD(date);
		if (fractal) {
			boolean positiveKCross = positiveKCross(stock, date);
			return (momentum && positiveKCross);
		}
		else {
			boolean divergenceExists = findDivergence(stock, date); //findDivergence(stock, date);
			return (momentum && divergenceExists);
		}
	}
	
	private static boolean findDivergence(Stock stock, String date) {
		int endingIndex2 = stock.getDates().indexOf(date);
		int startingIndex2 = endingIndex2 - stock.getTimePeriod() / 5;
		int endingIndex1 = startingIndex2 - 1;
		int startingIndex1 = endingIndex1 - stock.getTimePeriod() / 5;

		double priceMin1 = stock.getLow(stock.getDates().get(startingIndex1));
		double kMin1 = stock.getStochasticK(stock.getDates().get(startingIndex1));
		for (int i = startingIndex1; i <= endingIndex1; i++) {
			String iDate = stock.getDates().get(i);
			double currentLowPrice = stock.getLow(iDate);
			if (currentLowPrice < priceMin1) {
				priceMin1 = currentLowPrice;
				kMin1 = stock.getStochasticK(stock.getDates().get(i));
			}
		}

		double priceMin2 = stock.getLow(stock.getDates().get(startingIndex2));
		double kMin2 = stock.getStochasticK(stock.getDates().get(startingIndex2));
		for (int i = startingIndex1; i <= endingIndex1; i++) {
			String iDate = stock.getDates().get(i);
			double currentLowPrice = stock.getLow(iDate);
			if (currentLowPrice < priceMin2) {
				priceMin2 = currentLowPrice;
				kMin2 = stock.getStochasticK(stock.getDates().get(i));
			}
		}

		if (priceMin1 > priceMin2 && kMin1 < kMin2) {
			return true;
		}

		return false;
	}
	
	private static boolean positiveKCross(Stock stock, String date) {
		int timePeriod = stock.getTimePeriod();
		int currentDateIndex = stock.getDates().indexOf(date);
		if (currentDateIndex == -1) {
			currentDateIndex = stock.getDates().indexOf(stock.getClosestDate(date));
		}
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .6);

		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getStochasticK(iDate) < stock.getStochasticD(iDate) * 1.15) {
				countBelownDI++;
			}
		}

		return countBelownDI >= reqPointsBelownDI;
	}
}
