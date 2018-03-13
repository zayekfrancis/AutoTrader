import java.util.ArrayList;
import java.util.Map;

public class MovingAverageAnalyzer {

	private static Stock stock;

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
	
	// Checks for Positive Momentum
		public static boolean emaMomentumExists(Stock stock, String date, boolean fractal) {
			date = stock.getClosestDate(date);

			int timePeriod = stock.getTimePeriod();
			int startingIndex = stock.getDates().indexOf(date) - timePeriod + 1;
			int endingIndex = stock.getDates().indexOf(date);
			double rateOfChange = 0;
			double previousRateOfChange = 0;
			double[] ratesOfChange = new double[timePeriod];
			for (int i = startingIndex; i < endingIndex; i++) {
				String iDate1 = stock.getDates().get(i);
				String iDate2 = stock.getDates().get(i + 1);
				double ema1 = stock.getEMA(iDate1);
				double ema2 = stock.getEMA(iDate2);
				double emaFifty1 = stock.getFiftyEMA(iDate1);
				double emaFifty2 = stock.getFiftyEMA(iDate2);
				double hist1 = ema1 - emaFifty1;
				double hist2 = ema2 - emaFifty2;
				double change = hist2 - hist1;
				ratesOfChange[i - startingIndex] = change;
			}

			double multiplier = (2.0 / (double) (timePeriod + 1));
			previousRateOfChange = ratesOfChange[0];
			double ema = 0;
			for (int i = 1; i < ratesOfChange.length; i++) {
				rateOfChange = ratesOfChange[i];
				ema = ((rateOfChange - previousRateOfChange) * multiplier) + previousRateOfChange;
				previousRateOfChange = ema;
			}

			if (!fractal && postiveCross(stock, date, stock.getTimePeriod()) && ema > .04) { 
				return true;
			} else if (fractal && ema > 0) {
				return true;
			}
			return false;
		}

	public static boolean postiveCross(Stock stock, String date, int timePeriod) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - timePeriod * 2;
		if (startingDateIndex < 0){
			return false;
		}
		int reqPointsBelowMA = (int) ((currentDateIndex - startingDateIndex) * .6);

		int countBelowMA = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getEMA(iDate) < stock.getFiftyEMA(iDate)) {
				countBelowMA++;
			}
		}

		boolean reqMet = countBelowMA >= reqPointsBelowMA;
		if (reqMet && stock.getEMA(date) > stock.getFiftyEMA(date)) {
			return true;
		}
		
		return false;
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
	
	public static Map<String, Double> calculateSMA(Stock stock, Map<String, Double> map, int timePeriod) {
		ArrayList<Double> prices = new ArrayList<Double>();
		for (String date : stock.getDates()) {
			prices.add(stock.getClose(date));
		}

		int startingIndex = timePeriod - 1;
		int endingIndex = prices.size() - 1;
		for (int x = startingIndex; x <= endingIndex; x++) {
			double sumPrices = 0;
			for (int y = (x - timePeriod + 1); y <= x; y++) {
				sumPrices += prices.get(y);
			}
			double sma = sumPrices / timePeriod;
			String date = stock.getDates().get(x);
			date = stock.getClosestDate(date);
			map.put(date, sma);
		}
		return map;
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

}
