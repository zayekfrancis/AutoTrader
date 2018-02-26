import java.util.ArrayList;

public class MovingAverageAnalyzer {

	private static Stock stock;
	private static double strength;
	private static double slope;

	public static double getRatio(Stock stockIn, String dateIn) {
		double ratio = 1.00;
		/*if (stockIn.getClose(dateIn) <= stockIn.getEMA(dateIn)) {
			return .999 * stockIn.getClose(dateIn);
		}*/
		if (stockIn.getEMA(dateIn) <= (stockIn.getSMA(dateIn) * ratio)) {
			return .999 * stockIn.getClose(dateIn);
		}
		else {
			return 0.0;
		}
	}

	public static int postiveCross(Stock stock, String date, int timePeriod) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - timePeriod * 2;
		if (startingDateIndex < 0){
			return 0;
		}
		int reqPointsBelowMA = (int) ((currentDateIndex - startingDateIndex) * .6);

		int totalCount = 0;
		int countBelowMA = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getEMA(iDate) < stock.getTwentyEMA(date)) {
				countBelowMA++;
			}
		}

		if (countBelowMA > reqPointsBelowMA && stock.getEMA(date) > stock.getTwentyEMA(date)) {
			totalCount++;
		}
		
		countBelowMA = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getTwentyEMA(iDate) < stock.getFiftyEMA(date)) {
				countBelowMA++;
			}
		}

		if (countBelowMA > reqPointsBelowMA && stock.getTwentyEMA(date) > stock.getFiftyEMA(date)) {
			totalCount++;
		}
		
		countBelowMA = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getFiftyEMA(iDate) < stock.getOneHundredEMA(date)) {
				countBelowMA++;
			}
		}

		if (countBelowMA > reqPointsBelowMA && stock.getFiftyEMA(date) > stock.getOneHundredEMA(date)) {
			totalCount++;
		}
		return totalCount;
	}

	public static boolean negativeCross(Stock stockIn, String dateIn) {
		stock = stockIn;
		double currentSMA = stock.getSMA(dateIn);
		double currentEMA = stock.getEMA(dateIn);
		if (currentEMA < currentSMA) {
			return true;
		}
		return false;
	}

	public static boolean calculateSMA(Stock stockIn, int timePeriod) {
		ArrayList<Double> prices = new ArrayList<Double>();
		for (String date : stockIn.getDates()) {
			prices.add(stockIn.getClose(date));
		}

		int startingIndex = timePeriod - 1;
		int endingIndex = prices.size() - 1;
		for (int x = startingIndex; x <= endingIndex; x++) {
			double sumPrices = 0;
			for (int y = (x - timePeriod + 1); y <= x; y++) {
				sumPrices += prices.get(y);
			}
			double sma = sumPrices / timePeriod;
			String date = stockIn.getDates().get(x);
			stockIn.setSMA(date, sma);
		}
		return true;
	}

	private static double getSMASlope(int startingIndex, int endingIndex) {
		double yb = stock.getSMA(stock.getDates().get(endingIndex));
		double ya = stock.getSMA(stock.getDates().get(startingIndex));

		double yDiff = yb - ya;
		double xDiff = endingIndex - startingIndex + 1;

		return yDiff / xDiff;
	}

	public static boolean calculateEMA(Stock stockIn, int timePeriod) {
		ArrayList<Double> prices = new ArrayList<Double>();
		for (String date : stockIn.getDates()) {
			prices.add(stockIn.getClose(date));
		}

		String firstDate = stockIn.getDates().get(timePeriod - 1);
		double sma = stockIn.getSMA(firstDate);
		stockIn.setEMA(firstDate, sma);
		int startingIndex = timePeriod;
		int endingIndex = prices.size() - 1;

		// Multiplier: (2 / (Time periods + 1) )
		// EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day)
		for (int x = startingIndex; x <= endingIndex; x++) {
			double multiplier = (2.0 / (double) (timePeriod + 1));
			double close = stockIn.getClose(stockIn.getDates().get(x));
			double previousEMA = stockIn.getEMA(stockIn.getDates().get(x - 1));

			double ema = ((close - previousEMA) * multiplier) + previousEMA;
			String date = stockIn.getDates().get(x);
			stockIn.setEMA(date, ema);
		}
		return true;
	}

	private static double getEMASlope(int startingIndex, int endingIndex) {
		double yb = stock.getEMA(stock.getDates().get(endingIndex));
		double ya = stock.getEMA(stock.getDates().get(startingIndex));

		double yDiff = yb - ya;
		double xDiff = endingIndex - startingIndex + 1;

		return yDiff / xDiff;
	}

	private static void setStrength() {
		strength = getSlope() - 1.0;
	}

	private static double getStrength() {
		return strength;
	}

	private static void setSlope(double slopeIn) {
		slope = slopeIn;
		setStrength();
	}

	private static double getSlope() {
		return slope;
	}
}
