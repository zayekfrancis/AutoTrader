import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FractalEnergy {

	public static boolean buySignal(StockBroker broker, Stock stock, String date) {
		Stock fractalStock = stock.getFractalStock();
		date = stock.getClosestDate(date);
		boolean macdMomentum = MACDAnalyzer.MACDMomentumExists(fractalStock, date, true);
		boolean rsiMomentum = RSIAnalyzer.rsiMomentumExists(fractalStock, date, 50, 80);
		boolean stochasticMomentum = StochasticsAnalyzer.buySignal(fractalStock, date, true);
		boolean bBandSignal = BollingerBandsAnalyzer.buySignal(stock, date, true);
		return (macdMomentum || rsiMomentum || stochasticMomentum || bBandSignal);
	}

	public static boolean sellSignal(Stock stock, String date) {
		Stock fractalStock = stock.getFractalStock();
		// CHANGE TO CYCLEANALZYER----->
		boolean stochasticSellSignal = StochasticsAnalyzer.sellSignal(fractalStock, date, true);
		boolean momentumSellSignal = MomentumAnalyzer.sellSignal(fractalStock, date, true);
		return (stochasticSellSignal && momentumSellSignal);
	}

	public static Stock setUpFractalEnergy(Stock stock) {
		Stock fractalStock = new Stock(stock.getSymbol());
		ArrayList<String> dates = new ArrayList<String>();
		HashMap<String, Double> open = new HashMap<String, Double>();
		HashMap<String, Double> close = new HashMap<String, Double>();
		HashMap<String, Double> high = new HashMap<String, Double>();
		HashMap<String, Double> low = new HashMap<String, Double>();
		HashMap<String, Double> volume = new HashMap<String, Double>();

		// Set Fractal Dates
		setFractalValues(stock, dates, open, close, high, low, volume);
		Collections.sort(dates);

		fractalStock.setDates(dates);
		fractalStock.setOpens(open);
		fractalStock.setCloses(close);
		fractalStock.setHighs(high);
		fractalStock.setLows(low);
		fractalStock.setAllVolume(volume);

		fractalStock.setInterval(stock.getInterval());
		fractalStock.setTimePeriod(stock.getTimePeriod());
		fractalStock.setSeriesType(stock.getSeriesType());
		fractalStock.setBaseAF(stock.getBaseAF());
		fractalStock.setIncrementAF(stock.getIncrementAF());
		fractalStock.setMaxAF(stock.getMaxAF());
		fractalStock.setNTBaseAF(stock.getNTBaseAF());
		fractalStock.setNTIncrementAF(stock.getNTIncrementAF());
		fractalStock.setNTMaxAF(stock.getNTMaxAF());
		fractalStock.setStochasticSMAK(stock.getStochasticSMAK());
		fractalStock.setStochasticSMAD(stock.getStochasticSMAD());
		fractalStock.setFastMACD(stock.getFastMACD());
		fractalStock.setSlowMACD(stock.getSlowMACD());
		fractalStock.setSignalMACD(stock.getSignalMACD());
		fractalStock.setBBTimePeriod(stock.getBBTimePeriod());
		fractalStock.setBBMultiplier(stock.getBBMultiplier());

		fractalStock.initIndicators();
		return fractalStock;
	}

	private static void setFractalValues(Stock stock, ArrayList<String> dates, HashMap<String, Double> opens,
			HashMap<String, Double> closes, HashMap<String, Double> highs, HashMap<String, Double> lows,
			HashMap<String, Double> volume) {

		int endIndex = 0;
		for (int i = 0; i < stock.getDates().size(); i = endIndex + 1) {
			endIndex = getEndOfWeek(stock, i);
			if (endIndex == -1) {
				break;
			}
			String date = stock.getDates().get(i);
			String endDate = stock.getDates().get(endIndex);
			dates.add(date);
			double open = stock.getOpen(date);
			opens.put(date, open);
			double close = stock.getClose(endDate);
			closes.put(date, close);
			double high = getExtremeHigh(stock.getHighPrices(), stock.getDates(), i, endIndex);
			highs.put(date, high);
			double low = getExtremeLow(stock.getLowPrices(), stock.getDates(), i, endIndex);
			lows.put(date, low);
			double combinedVolume = combinedVolume(stock, i, endIndex);
			volume.put(date, combinedVolume);

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
		for (int i = startingIndex; i <= endingIndex; i++) {
			String currentDate = dates.get(i);
			double currentLow = lows.get(currentDate);
			if (currentLow < extremeLow) {
				extremeLow = currentLow;
			}
		}
		return extremeLow;
	}

	private static double combinedVolume(Stock stock, int startingIndex, int endingIndex) {
		double combinedVolume = 0;
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			double volume = stock.getVolume(iDate);
			combinedVolume += volume;
		}
		return combinedVolume;
	}

	private static int getEndOfWeek(Stock stock, int index) {
		String date = stock.getDates().get(index);
		// Extract Year, Month, Day
		int year = Integer.parseInt(date.substring(1, 5));
		int month = Integer.parseInt(date.substring(6, 8)) - 1;
		int day = Integer.parseInt(date.substring(9, date.length() - 1));

		// Get calendar set to current date and time
		Calendar cBegin = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();
		cBegin.clear();
		cEnd.clear();
		cBegin.set(year, month, day);
		cEnd.set(year, month, day);

		// Set the calendar to monday of the current week
		cBegin.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		cEnd.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		cEnd.set(Calendar.DATE, cEnd.get(Calendar.DATE) + 7);

		int endOfWeek = 0;
		int count = index;
		while (endOfWeek == 0) {
			DateFormat df = new SimpleDateFormat("EEE yyyy-MM-dd");

			// Pull current date data
			if (count >= stock.getDates().size()) {
				return -1;
			}
			String currentDate = stock.getDates().get(count);
			int currentYear = Integer.parseInt(currentDate.substring(1, 5));
			int currentMonth = Integer.parseInt(currentDate.substring(6, 8)) - 1;
			int currentDay = Integer.parseInt(currentDate.substring(9, currentDate.length() - 1));

			Calendar cCurrent = Calendar.getInstance();
			cCurrent.clear();
			cCurrent.set(currentYear, currentMonth, currentDay);

			int dateCompare = cCurrent.compareTo(cEnd);

			if (dateCompare >= 0) {
				df = new SimpleDateFormat("yyyy-MM-dd");
				String dateContruct = df.format(cCurrent.getTime());
				for (String dt : stock.getDates()) {
					if (dt.contains(dateContruct)) {
						endOfWeek = stock.getDates().indexOf(dt) - 1;
						break;
					}
				}
			}
			count++;
		}

		return endOfWeek;
	}
}
