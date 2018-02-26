import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Stock implements Cloneable {

	private IEX av;

	private String symbol;
	private String interval;

	private ArrayList<String> dates;
	private Map<String, Double> open;
	private Map<String, Double> close;
	private Map<String, Double> high;
	private Map<String, Double> low;

	public Map<String, Double> tpSMA;
	public Map<String, Double> tpEMA;
	private Map<String, Double> twentySMA;
	private Map<String, Double> twentyEMA;
	private Map<String, Double> fiftySMA;
	private Map<String, Double> fiftyEMA;
	private Map<String, Double> oneHundredSMA;
	private Map<String, Double> oneHundredEMA;
	private Map<String, Double> ADX;
	private Map<String, Double> pDI;
	private Map<String, Double> nDI;
	private Map<String, Double> SAR;
	private Map<String, Double> basicStochasticK;
	private Map<String, Double> stochasticK;
	private Map<String, Double> stochasticD;
	private Map<String, Double> MACDLine;
	private Map<String, Double> MACDSignal;
	private Map<String, Double> MACDHistogram;
	private Map<String, Double> RSI;

	private boolean loadSuccessful;

	private int timePeriod;
	private String seriesType;
	private double baseAF;
	private double maxAF;
	private int stochasticSMAK;
	private int stochasticSMAD;
	private int fastMACD;
	private int slowMACD;
	private int signalMACD;
	public double diMaxDistance = 0;

	// Semaphore
	public static Semaphore sem = new Semaphore(1);

	public Stock(String symbolIn) {
		av = new IEX();
		setSymbol(symbolIn);
	}

	public Stock(String symbolIn, String intervalIn) {
		av = new IEX();
		setSymbol(symbolIn);
		setInterval(intervalIn);

		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (initPrices(symbolIn, intervalIn, false)) {
			loadSuccessful = true;
		}
		sem.release();
	}

	// Daily or greater intervals
	public Stock(String symbolIn, String intervalIn, int timePeriodIn, String seriesType, double baseAF, double maxAF,
			int stochasticSMAK, int stochasticSMAD, int fastMACD, int slowMACD, int signalMACD, boolean local) {
		av = new IEX();
		setSymbol(symbolIn);
		setInterval(intervalIn);
		setTimePeriod(timePeriodIn);
		setSeriesType(seriesType);
		setBaseAF(baseAF);
		setMaxAF(maxAF);
		setStochasticSMAK(stochasticSMAK);
		setStochasticSMAD(stochasticSMAD);
		setFastMACD(fastMACD);
		setSlowMACD(slowMACD);
		setSignalMACD(signalMACD);

		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (initPrices(symbolIn, intervalIn, local)) {
			if (dates.size() < timePeriodIn) {
				loadSuccessful = false;
			} else if (initIndicators()) {
				loadSuccessful = true;
			}
		}
		sem.release();
	}

	// IntraDay intervals
	public Stock(String symbolIn, String intervalIn, String date, int timePeriodIn, String seriesType, double baseAF,
			double maxAF, int stochasticSMAK, int stochasticSMAD, int fastMACD, int slowMACD, int signalMACD,
			boolean local) {
		av = new IEX();
		setSymbol(symbolIn);
		setInterval(intervalIn);
		setTimePeriod(timePeriodIn);
		setSeriesType(seriesType);
		setBaseAF(baseAF);
		setMaxAF(maxAF);
		setStochasticSMAK(stochasticSMAK);
		setStochasticSMAD(stochasticSMAD);
		setFastMACD(fastMACD);
		setSlowMACD(slowMACD);
		setSignalMACD(signalMACD);

		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (initPrices(symbolIn, intervalIn, date, local)) {
			if (dates.size() < timePeriodIn) {
				loadSuccessful = false;
			} else if (initIndicators()) {
				loadSuccessful = true;
			}
		}
		sem.release();
	}

	// Daily or greater intervals
	public boolean initPrices(String symbolIn, String intervalIn, boolean local) {
		if (av.updatePriceData(symbolIn, intervalIn, "", local)) {
			dates = new ArrayList<String>();
			open = new HashMap<String, Double>();
			close = new HashMap<String, Double>();
			high = new HashMap<String, Double>();
			low = new HashMap<String, Double>();

			dates.addAll(av.stockMap.keySet());
			Collections.sort(dates);
			for (String key : dates) {
				setOpen(key, av.stockMap.get(key).get(av.OPEN));
				setClose(key, av.stockMap.get(key).get(av.CLOSE));
				setHigh(key, av.stockMap.get(key).get(av.HIGH));
				setLow(key, av.stockMap.get(key).get(av.LOW));
			}
			return true;
		}

		return false;
	}

	// IntraDay intervals
	public boolean initPrices(String symbolIn, String intervalIn, String date, boolean local) {
		if (av.updatePriceData(symbolIn, intervalIn, date, local)) {
			dates = new ArrayList<String>();
			open = new HashMap<String, Double>();
			close = new HashMap<String, Double>();
			high = new HashMap<String, Double>();
			low = new HashMap<String, Double>();

			dates.addAll(av.stockMap.keySet());
			Collections.sort(dates);
			for (String key : dates) {
				setOpen(key, av.stockMap.get(key).get(av.OPEN));
				setClose(key, av.stockMap.get(key).get(av.CLOSE));
				setHigh(key, av.stockMap.get(key).get(av.HIGH));
				setLow(key, av.stockMap.get(key).get(av.LOW));
			}
			return true;
		}
		return false;
	}

	public boolean initIndicators() {
		// SMAs and EMAs
		tpSMA = new HashMap<String, Double>();
		tpEMA = new HashMap<String, Double>();
		twentySMA = new HashMap<String, Double>();
		twentyEMA = new HashMap<String, Double>();
		fiftySMA = new HashMap<String, Double>();
		fiftyEMA = new HashMap<String, Double>();
		oneHundredSMA = new HashMap<String, Double>();
		oneHundredEMA = new HashMap<String, Double>();

		// ADX, +DI, -DI
		ADX = new HashMap<String, Double>();
		pDI = new HashMap<String, Double>();
		nDI = new HashMap<String, Double>();

		// Parabolic SAR
		SAR = new HashMap<String, Double>();

		// Stochastics
		basicStochasticK = new HashMap<String, Double>();
		stochasticK = new HashMap<String, Double>();
		stochasticD = new HashMap<String, Double>();

		// MACD
		MACDLine = new HashMap<String, Double>();
		MACDSignal = new HashMap<String, Double>();
		MACDHistogram = new HashMap<String, Double>();

		// RSI
		RSI = new HashMap<String, Double>();

		if (close.size() > 20) {
			MovingAverageAnalyzer.calculateSMA(this, 20);
			MovingAverageAnalyzer.calculateEMA(this, 20);
			twentySMA = tpSMA;
			twentyEMA = tpEMA;
			tpSMA = new HashMap<String, Double>();
			tpEMA = new HashMap<String, Double>();
		}
		
		if (close.size() > 50) {
			MovingAverageAnalyzer.calculateSMA(this, 50);
			MovingAverageAnalyzer.calculateEMA(this, 50);
			fiftySMA = tpSMA;
			fiftyEMA = tpEMA;
			tpSMA = new HashMap<String, Double>();
			tpEMA = new HashMap<String, Double>();
		}
		
		if (close.size() > 100) {
			MovingAverageAnalyzer.calculateSMA(this, 100);
			MovingAverageAnalyzer.calculateEMA(this, 100);
			oneHundredSMA = tpSMA;
			oneHundredEMA = tpEMA;
			tpSMA = new HashMap<String, Double>();
			tpEMA = new HashMap<String, Double>();
		}
		
		return (MovingAverageAnalyzer.calculateSMA(this, getTimePeriod())
				&& MovingAverageAnalyzer.calculateEMA(this, getTimePeriod())
				&& RSIAnalyzer.calculateRSI(this, getTimePeriod()) && ADXAnalyzer.calculateADX(this, getTimePeriod())
				&& SARAnalyzer.calculateSAR(this, getTimePeriod(), getBaseAF(), getMaxAF())
				&& Stochastics.calculateStochastics(this, getTimePeriod(), getStochasticSMAK(), getStochasticSMAD())
				&& MACD.calculateMACD(this, getFastMACD(), getSlowMACD(), getSignalMACD()));
	}

	public boolean loadSuccessful() {
		return loadSuccessful;
	}

	public void setSymbol(String symbolIn) {
		symbol = symbolIn;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setInterval(String intervalIn) {
		interval = intervalIn;
	}

	public String getInterval() {
		return interval;
	}

	public void setTimePeriod(int timePeriodIn) {
		timePeriod = timePeriodIn;
	}

	public int getTimePeriod() {
		return timePeriod;
	}

	public void setSeriesType(String seriesTypeIn) {
		seriesType = seriesTypeIn;
	}

	public String getSeriesType() {
		return seriesType;
	}

	public void setBaseAF(double baseAFIn) {
		baseAF = baseAFIn;
	}

	public double getBaseAF() {
		return baseAF;
	}

	public void setMaxAF(double maxAFIn) {
		maxAF = maxAFIn;
	}

	public double getMaxAF() {
		return maxAF;
	}

	public void setStochasticSMAK(int SMAKIn) {
		stochasticSMAK = SMAKIn;
	}

	public int getStochasticSMAK() {
		return stochasticSMAK;
	}

	public void setStochasticSMAD(int SMADIn) {
		stochasticSMAD = SMADIn;
	}

	public int getStochasticSMAD() {
		return stochasticSMAD;
	}

	public void setFastMACD(int fastMACDIn) {
		fastMACD = fastMACDIn;
	}

	public int getFastMACD() {
		return fastMACD;
	}

	public void setSlowMACD(int slowMACDIn) {
		slowMACD = slowMACDIn;
	}

	public int getSlowMACD() {
		return slowMACD;
	}

	public void setSignalMACD(int signalMACDIn) {
		signalMACD = signalMACDIn;
	}

	public int getSignalMACD() {
		return signalMACD;
	}

	public void setOpen(String dateIn, Double openIn) {
		open.put(dateIn, openIn);
	}

	public double getOpen(String dateIn) {
		if (open.containsKey(dateIn)) {
			return open.get(dateIn);
		} else {
			return getClose(dateIn);
		}
	}

	public void setClose(String dateIn, Double closeIn) {
		close.put(dateIn, closeIn);
	}

	public double getClose(String dateIn) {
		return getMapValue(close, dateIn);
	}

	public Map<String, Double> getClosePrices() {
		return close;
	}

	public Map<String, Double> getOpenPrices() {
		return open;
	}

	public Map<String, Double> getHighPrices() {
		return high;
	}

	public Map<String, Double> getLowPrices() {
		return low;
	}

	public void setHigh(String dateIn, Double highIn) {
		high.put(dateIn, highIn);
	}

	public double getHigh(String dateIn) {
		return high.get(dateIn);
	}

	public void setLow(String dateIn, Double lowIn) {
		low.put(dateIn, lowIn);
	}

	public double getLow(String dateIn) {
		if (low.containsKey(dateIn)) {
			return low.get(dateIn);
		} else {
			return getClose(dateIn);
		}
	}

	public void setRSI(String dateIn, Double rsiIn) {
		RSI.put(dateIn, rsiIn);
	}

	public void setSMA(String dateIn, Double smaIn) {
		tpSMA.put(dateIn, smaIn);
	}

	public void setEMA(String dateIn, Double emaIn) {
		tpEMA.put(dateIn, emaIn);
	}

	public void setADX(String dateIn, Double adxIn) {
		ADX.put(dateIn, adxIn);
	}

	public void setpDI(String dateIn, Double pDIIn) {
		pDI.put(dateIn, pDIIn);
	}

	public void setnDI(String dateIn, Double nDIIn) {
		nDI.put(dateIn, nDIIn);
	}

	public void setSAR(String dateIn, Double sarIn) {
		SAR.put(dateIn, sarIn);
	}

	public void setBasicStochasticK(String dateIn, Double kIn) {
		basicStochasticK.put(dateIn, kIn);
	}

	public void setStochasticK(String dateIn, Double kIn) {
		stochasticK.put(dateIn, kIn);
	}

	public void setStochasticD(String dateIn, Double dIn) {
		stochasticD.put(dateIn, dIn);
	}

	public void setMACDLine(String dateIn, Double macdLineIn) {
		MACDLine.put(dateIn, macdLineIn);
	}

	public void setMACDSignal(String dateIn, Double macdSignalIn) {
		MACDSignal.put(dateIn, macdSignalIn);
	}

	public void setMACDHistogram(String dateIn, Double macdHistogramIn) {
		MACDHistogram.put(dateIn, macdHistogramIn);
	}

	public double getSMA(String dateIn) {
		return getMapValue(tpSMA, dateIn);
	}

	public double getEMA(String dateIn) {
		return getMapValue(tpEMA, dateIn);
	}

	public double getTwentySMA(String dateIn) {
		return getMapValue(twentySMA, dateIn);
	}

	public double getTwentyEMA(String dateIn) {
		return getMapValue(twentyEMA, dateIn);
	}

	public double getFiftySMA(String dateIn) {
		return getMapValue(fiftySMA, dateIn);
	}

	public double getFiftyEMA(String dateIn) {
		return getMapValue(fiftyEMA, dateIn);
	}

	public double getOneHundredSMA(String dateIn) {
		return getMapValue(oneHundredSMA, dateIn);
	}

	public double getOneHundredEMA(String dateIn) {
		return getMapValue(oneHundredEMA, dateIn);
	}

	public double getADX(String dateIn) {
		return getMapValue(ADX, dateIn);
	}

	public double getpDI(String dateIn) {
		return getMapValue(pDI, dateIn);
	}

	public double getnDI(String dateIn) {
		return getMapValue(nDI, dateIn);
	}

	public double getSAR(String dateIn) {
		return getMapValue(SAR, dateIn);
	}

	public double getBasicStochasticK(String dateIn) {
		return getMapValue(basicStochasticK, dateIn);
	}

	public double getStochasticK(String dateIn) {
		return getMapValue(stochasticK, dateIn);
	}

	public double getStochasticD(String dateIn) {
		return getMapValue(stochasticD, dateIn);
	}

	public double getMACDLine(String dateIn) {
		return getMapValue(MACDLine, dateIn);
	}

	public double getMACDSignal(String dateIn) {
		return getMapValue(MACDSignal, dateIn);
	}

	public double getMACDHistogram(String dateIn) {
		return getMapValue(MACDHistogram, dateIn);
	}

	public double getRSI(String dateIn) {
		return getMapValue(RSI, dateIn);
	}

	public double getLatestClosingPrice() {
		return getClose(getDates().get(0));
	}

	public double getLatestOpeningPrice() {
		return getOpen(getDates().get(0));
	}

	public double getLatestHighPrice() {
		return getHigh(getDates().get(0));
	}

	public double getLatestLowPrice() {
		return getLow(getDates().get(0));
	}

	public void setDates() {
		dates.addAll(close.keySet());
	}

	public void setDates(ArrayList<String> datesIn) {
		dates = datesIn;
	}

	public void setOpens(HashMap<String, Double> opens) {
		open = opens;
	}

	public void setCloses(HashMap<String, Double> closes) {
		close = closes;
	}

	public void setHighs(HashMap<String, Double> highs) {
		high = highs;
	}

	public void setLows(HashMap<String, Double> lows) {
		low = lows;
	}

	public ArrayList<String> getDates() {
		return dates;
	}

	public String getClosestDate(String dateIn) {
		if (getDates().contains(dateIn)) {
			return dateIn;
		}
		int highestIndex = 0;
		String closestDate = "";
		for (String date : getDates()) {
			for (int i = 0; i < date.length(); i++) {
				if (date.substring(0, i).equals(dateIn.substring(0, i))) {
					if (i > highestIndex) {
						highestIndex = i;
						closestDate = date;
					}
				}
			}
		}
		if (!closestDate.equals("")) {
			return closestDate;
		}
		return null;
	}

	private double getMapValue(Map<String, Double> map, String dateIn) {
		if (map.containsKey(dateIn) && map.get(dateIn) != null) {
			return map.get(dateIn);
		} else {
			int highestIndex = 0;
			String closestDate = "";
			for (String date : map.keySet()) {
				for (int i = 0; i < date.length(); i++) {
					if (date.substring(0, i).equals(dateIn.substring(0, i)) && map.get(date) != null) {
						if (i > highestIndex) {
							highestIndex = i;
							closestDate = date;
						}
					}
				}
			}
			if (!closestDate.equals("")) {
				return map.get(closestDate);
			}
			return 0.0;
		}
	}

	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
