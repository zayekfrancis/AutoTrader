import java.util.HashMap;
import java.util.Map;

public class MACD {

	public static boolean calculateMACD(Stock stock, int fastMACD, int slowMACD, int signal) {
		Stock stockCopy = null;
		try {
			stockCopy = (Stock) stock.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Double> fastMACDEMA = new HashMap<String, Double>();
		Map<String, Double> slowMACDEMA = new HashMap<String, Double>();
		Map<String, Double> signalMACDEMA = new HashMap<String, Double>();
		stockCopy.tpSMA = new HashMap<String, Double>();
		stockCopy.tpEMA = new HashMap<String, Double>();
		
		// Calculate and Copy fastMACD to local Map
		MovingAverageAnalyzer.calculateSMA(stockCopy, fastMACD);
		MovingAverageAnalyzer.calculateEMA(stockCopy, fastMACD);
		copyEMAMap(stockCopy, fastMACDEMA);
		stockCopy.tpSMA = new HashMap<String, Double>();
		stockCopy.tpEMA = new HashMap<String, Double>();

		// Calculate and Copy fastMACD to local Map
		MovingAverageAnalyzer.calculateSMA(stockCopy, slowMACD);
		MovingAverageAnalyzer.calculateEMA(stockCopy, slowMACD);
		copyEMAMap(stockCopy, slowMACDEMA);
		stockCopy.tpSMA = new HashMap<String, Double>();
		stockCopy.tpEMA = new HashMap<String, Double>();

		for (String date : stock.getDates()) {
			double macD = fastMACDEMA.get(date) - slowMACDEMA.get(date);
			stock.setMACDLine(date, macD);
		}
		
		calculateSignalEMA(stock, signalMACDEMA, signal);
		String firstDate = stock.getDates().get(signal - 1);
		for (String date : stock.getDates()) {
			int firstDateIndex = stock.getDates().indexOf(firstDate);
			int dateIndex = stock.getDates().indexOf(date);
			if (dateIndex >= firstDateIndex) {
				double signalLineValue = signalMACDEMA.get(date);
				stock.setMACDSignal(date, signalLineValue);
			}
		}
		
		for (String date : stock.getDates()) {
			double macDHistogram = stock.getMACDLine(date) - stock.getMACDSignal(date);
			stock.setMACDHistogram(date, macDHistogram);
		}
		
		MovingAverageAnalyzer.calculateSMA(stock, stock.getTimePeriod());
		MovingAverageAnalyzer.calculateEMA(stock, stock.getTimePeriod());

		return true;
	}

	private static boolean copyEMAMap(Stock stockToCopyFrom, Map<String, Double> ema) {
		for (String date : stockToCopyFrom.getDates()) {
			ema.put(date, stockToCopyFrom.getEMA(date));
		}
		return true;
	}

	public static boolean calculateSignalEMA(Stock stock, Map<String, Double> signalEMA, int signalMACD) {
		String firstDate = stock.getDates().get(signalMACD - 1);
		double sma = calculateSMA(stock, signalMACD);
		signalEMA.put(firstDate, sma);
		int startingIndex = signalMACD;
		int endingIndex = stock.getDates().size() - 1;

		// Multiplier: (2 / (Time periods + 1) )
		// EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day)
		for (int x = startingIndex; x <= endingIndex; x++) {
			double multiplier = (2.0 / (double) (signalMACD + 1));
			double close = stock.getMACDLine(stock.getDates().get(x));
			double previousEMA = signalEMA.get(stock.getDates().get(x - 1));

			double ema = ((close - previousEMA) * multiplier) + previousEMA;
			String date = stock.getDates().get(x);
			signalEMA.put(date, ema);
		}
		return true;
	}

	public static double calculateSMA(Stock stock, int signalMACD) {
		int startingIndex = 0;
		int endingIndex = signalMACD;
		
		double sumPrices = 0;
		for (int x = startingIndex; x < endingIndex; x++) {
			String date = stock.getDates().get(x);
			sumPrices += stock.getMACDLine(date);
		}
		double sma = sumPrices / signalMACD;
		return sma;
	}
	
	

}
