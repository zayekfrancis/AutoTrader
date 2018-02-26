import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FractalEnergy {
	
	public static boolean buySignal(Stock stock, String date) {
		Stock fractalStock = setUpFractalEnergy(stock);
		boolean stochasticBuySignal = StochasticsAnalyzer.buySignal(fractalStock, date, true);
		boolean momentumBuySignal = MomentumAnalyzer.buySignal(fractalStock, date);
		return (stochasticBuySignal && momentumBuySignal);
	}
	
	private static Stock setUpFractalEnergy(Stock stock) {
		Stock fractalStock = new Stock(stock.getSymbol());
		ArrayList<String> dates = new ArrayList<String>();
		HashMap<String, Double> open = new HashMap<String, Double>();
		HashMap<String, Double> close = new HashMap<String, Double>();
		HashMap<String, Double> high = new HashMap<String, Double>();
		HashMap<String, Double> low = new HashMap<String, Double>();

		//Set Fractal Dates
		setFractalValues(stock, dates, open, close, high, low);
		Collections.sort(dates);
		
		fractalStock.setDates(dates);
		fractalStock.setOpens(open);
		fractalStock.setCloses(close);
		fractalStock.setHighs(high);
		fractalStock.setLows(low);
		
		fractalStock.setInterval(stock.getInterval());
		fractalStock.setTimePeriod(stock.getTimePeriod());
		fractalStock.setSeriesType(stock.getSeriesType());
		fractalStock.setBaseAF(stock.getBaseAF());
		fractalStock.setMaxAF(stock.getMaxAF());
		fractalStock.setStochasticSMAK(stock.getStochasticSMAK());
		fractalStock.setStochasticSMAD(stock.getStochasticSMAD());
		fractalStock.setFastMACD(stock.getFastMACD());
		fractalStock.setSlowMACD(stock.getSlowMACD());
		fractalStock.setSignalMACD(stock.getSignalMACD());
		
		fractalStock.initIndicators();
		return fractalStock;
	}
	
	private static void setFractalValues(Stock stock, ArrayList<String> dates, HashMap<String, Double> opens, HashMap<String, Double> closes, HashMap<String, Double> highs, HashMap<String, Double> lows) {
		boolean setFractalDate = true;
		int setFractalDateCount = stock.getDates().size() - 1; 
		for (int i = stock.getDates().size() - 3; i >= 0; i --) {
			if (setFractalDate) {
				
				String date = stock.getDates().get(i);
				dates.add(date);
				double open = stock.getOpen(date);
				opens.put(date, open);
				double close = stock.getClose(date);
				closes.put(date, close);
				double high = getExtremeHigh(stock.getHighPrices(), stock.getDates(), i, i+2);
				highs.put(date, high);
				double low = getExtremeLow(stock.getLowPrices(), stock.getDates(), i, i+2);
				lows.put(date, low);
				
				setFractalDateCount = i - 2;
				setFractalDate = false;
			}
			if (i == setFractalDateCount) {
				setFractalDate = true;
			}
		}
	}
	
	private static double getExtremeHigh(Map<String, Double> highs, ArrayList<String> dates, int startingIndex,
			int endingIndex) {
		String startingDate = dates.get(startingIndex);
		double extremePoint = highs.get(startingDate);
		for (int i = startingIndex; i < endingIndex; i++) {
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
		for (int i = startingIndex; i < endingIndex; i++) {
			String currentDate = dates.get(i);
			double currentLow = lows.get(currentDate);
			if (currentLow < extremeLow) {
				extremeLow = currentLow;
			}
		}
		return extremeLow;
	}
}
