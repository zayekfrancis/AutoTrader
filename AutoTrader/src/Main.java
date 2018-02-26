import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Main {
	public static final String SP_URL = "http://en.wikipedia.org/wiki/List_of_S%26P_500_companies";

	public static void main(String[] args) {
		// updateJsons();

		String interval = IEX.FIVE_YEARS;
		String intraDayInterval = IEX.ONE_MIN;
		int timePeriod = 14;
		double funds = 10000;
		String seriesType = IEX.CLOSE;
		double baseAF = .02;
		double maxAF = .25;
		int stochasticSMAK = 5;
		int stochasticSMAD = 2;
		int fastMACD = 12;
		int slowMACD = 26;
		int signalMACD = 9;
		String[] recentSymbols = { "HRS"};//, "AMD", "XEC", "DE", "UNH", "URBN", "HCA", "ADS", "UHS", "CMA","ALXN", "BAC", "NOC", "HRS", "HAS", "QRVO", "XL", "DPS", "TRIP", "MAT", "CME" };

		// sendMail();

		StockBroker broker = new StockBroker(funds, interval);
		ArrayList<Stock> stocks = new ArrayList<Stock>();
		ArrayList<Stock> oneMinStocks = new ArrayList<Stock>();
		for (String symbol : IEX.getSP500()) {
			System.out.println("Getting " + symbol);
			Stock stock = new Stock(symbol, interval, timePeriod, seriesType, baseAF, maxAF, stochasticSMAK,
					stochasticSMAD, fastMACD, slowMACD, signalMACD, false);
			if (stock.loadSuccessful()) {
				stocks.add(stock);
			}
		}

		
		String oneMonthAgo = "2018-01-29";
		boolean withinMonth = false;
		for (String date : stocks.get(0).getDates()) {
			if (date.contains(oneMonthAgo) || withinMonth) {
				for (Stock stock : stocks) {
					withinMonth = true;
					Stock intraDayStock = new Stock(stock.getSymbol(), intraDayInterval, date, timePeriod, seriesType,
							baseAF, maxAF, stochasticSMAK, stochasticSMAD, fastMACD, slowMACD, signalMACD, false);
					if (intraDayStock.loadSuccessful()) {
						oneMinStocks.add(intraDayStock);
					}
				}
				String startDate = stocks.get(0).getDates().get(100);// stocks.get(0).getDates().size()
				anaylzeRealTimeHistory(broker, oneMinStocks, timePeriod, startDate);
				oneMinStocks = new ArrayList<Stock>();
				broker.sellEverything();
			}
		}
		return;
		// anaylzePreviousDay(broker, stocks, timePeriod);
		//String date = stocks.get(0).getDates().get(100);// stocks.get(0).getDates().size()
		//anaylzeRealTimeHistory(broker, stocks, timePeriod, date);
	}

	public static void anaylzePreviousDay(StockBroker broker, ArrayList<Stock> stocks, int timePeriodIn) {
		int size = stocks.get(0).getDates().size();
		String date = stocks.get(0).getDates().get(size - 1);
		Map<String, Integer> stocksToBuy = new HashMap<String, Integer>();

		stocksToBuy.put("HAS", 1);
		// stocksToBuy.put("HRS", 1);
		// stocksToBuy.put("QRVO", 2);

		stocksToBuy.put("TRIP", 5);
		stocksToBuy.put("MAT", 10);
		stocksToBuy.put("CME", 1);

		System.out.println("Analyzing date: " + date);

		for (Stock stock : stocks) {
			if (stocksToBuy.containsKey(stock.getSymbol())) {
				double marketPrice = stock.getOpen(date);
				int numberOfShares = stocksToBuy.get(stock.getSymbol());
				double totalCost = numberOfShares * marketPrice;
				if (broker.getFundsAvailable() >= totalCost) {
					broker.buy(stock, marketPrice, stocksToBuy.get(stock.getSymbol()), date);
					broker.setStopLoss(stock.getSymbol(), 0, numberOfShares, date, true);
				}
				stocksToBuy.remove(stock.getSymbol());
			}
		}
		for (Stock stock : stocks) {
			if (stock.getDates().indexOf(date) >= timePeriodIn) {
				runPreviousDateStockAnaylsis(broker, stock, date, timePeriodIn, stocksToBuy);
			}
		}
		broker.updateStopLosses(date);
		printBrokerInfo(broker, date);
		sendMail(stocksToBuy, date);
	}

	public static void anaylzeRealTimeHistory(StockBroker broker, ArrayList<Stock> stocks, int timePeriodIn,
			String startDate) {
		Map<String, Integer> stocksToBuy = new HashMap<String, Integer>();
		ArrayList<String> dates = stocks.get(0).getDates();
		double fundsBeforeYear = broker.getFundsAvailable();
		String year = dates.get(0).substring(0, 5);
		System.out.println("Analyzing stocks in realtime");
		for (String date : dates) {
			if (dates.indexOf(date) < dates.indexOf(startDate)) {
				continue;
			}
			System.out.println("Date: " + date);
			int dateIndex = dates.indexOf(date);
			if (dateIndex < timePeriodIn) { // || dateIndex < startDateIndex
				continue;
			}

			/*
			 * if ((dates.size() - 2) == dateIndex) { stocksToBuy.put("HAS", 1);
			 * stocksToBuy.put("HRS", 1); stocksToBuy.put("QRVO", 2); } if
			 * ((dates.size() - 1) == dateIndex) { stocksToBuy.put("TRIP", 5);
			 * stocksToBuy.put("MAT", 10); stocksToBuy.put("CME", 1); }
			 */

			for (Stock stock : stocks) {
				if (stocksToBuy.containsKey(stock.getSymbol())) {
					double marketPrice = stock.getOpen(date);
					int numberOfShares = stocksToBuy.get(stock.getSymbol());
					double totalCost = numberOfShares * marketPrice;
					if (broker.getFundsAvailable() >= totalCost) {
						broker.buy(stock, marketPrice, stocksToBuy.get(stock.getSymbol()), date);
						broker.setStopLoss(stock.getSymbol(), -0.1, numberOfShares, date, true);
					}
					stocksToBuy.remove(stock.getSymbol());
				}
			}
			for (Stock stock : stocks) {
				if (stock.getDates().indexOf(date) >= timePeriodIn) {
					runStockAnaylsisByDate(broker, stock, date, timePeriodIn, stocksToBuy);
				}
			}
			broker.updateStopLosses(date);
			printBrokerInfo(broker, date);

			if (!date.contains(year)) {
				double currentGains = broker.getFundsAndInvestmentsTotal(date) - fundsBeforeYear;
				System.out.println(year + " gains: " + currentGains);
				System.out.println("Percentage increase: " + (currentGains / fundsBeforeYear));
				year = date.substring(0, 5);
				fundsBeforeYear = broker.getFundsAndInvestmentsTotal(date);
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean runStockAnaylsisByDate(StockBroker broker, Stock stock, String dateIn, int timePeriodIn,
			Map<String, Integer> stocksToBuy) {
		String symbol = stock.getSymbol();
		String date = dateIn;
		String interval = stock.getInterval();
		int timePeriod = timePeriodIn;
		if (!stock.loadSuccessful()) {
			System.out.println();
			System.out.println("Symbol: " + symbol);
			System.out.println("ERROR");
			System.out.println("Skipping...");
			return false;
		}

		boolean owns = broker.getOwnedStocks().keySet().contains(stock.getSymbol());
		if (owns) {
			printStockDetail(broker, stock, date, timePeriod, false);
			return true;
		}

		else if (TrendAnalyzer.isPositivelyTrending(stock, date) && MomentumAnalyzer.buySignal(stock, date)
				&& StochasticsAnalyzer.buySignal(stock, date, false) && FractalEnergy.buySignal(stock, date)) {
			double allocationRatio = 1;
			int dateIndex = stock.getDates().indexOf(date);
			printStockDetail(broker, stock, date, timePeriod, true);
			try {
				stock.getOpen(stock.getDates().get(dateIndex + 1));
			} catch (IndexOutOfBoundsException e) {
				return false;
			}
			buyStock(broker, stock, date, stocksToBuy, allocationRatio);
			return true;
		} else {
			printStockDetail(broker, stock, date, timePeriod, false);
			return true;
		}
	}

	public static boolean buyStock(StockBroker broker, Stock stock, String date, Map<String, Integer> stocksToBuy,
			double allocationRatio) {
		int dateIndex = stock.getDates().indexOf(date);

		double amountToAllocate = allocationRatio * broker.getFundsAndInvestmentsTotal(date);// rsiStrength
		double stockPrice = stock.getOpen(stock.getDates().get(dateIndex + 1));
		int numberOfShares = (int) (amountToAllocate / stockPrice);
		if (numberOfShares > 0) {
			stocksToBuy.put(stock.getSymbol(), numberOfShares);
		}
		return true;
	}

	public static void printStockDetail(StockBroker broker, Stock stock, String date, int timePeriod, boolean buy) {
		System.out.println("");
		if (buy) {
			int dateIndex = stock.getDates().indexOf(date);
			System.out.println("*************************************");
			System.out.println("*************************************");
			System.out.println("BUY");
			try {
				System.out.println("Buy-in price will be: " + stock.getOpen(stock.getDates().get(dateIndex + 1)));
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Buy-in not found");
			}
			System.out.println("*************************************");
			System.out.println("*************************************");
		}
		System.out.println("Symbol: " + stock.getSymbol());
		System.out.println("Date: " + date);
		System.out.println("Interval: " + stock.getInterval());
		System.out.println("Time Period: " + timePeriod);
		System.out.println("Current Price: " + stock.getClose(date));
		System.out.println("");
		System.out.println("RSI : " + stock.getRSI(date));
		System.out.println("SMA : " + stock.getSMA(date));
		System.out.println("EMA : " + stock.getEMA(date));
		System.out.println("SMA 20: " + stock.getTwentySMA(date));
		System.out.println("EMA 20: " + stock.getTwentyEMA(date));
		System.out.println("SMA 50: " + stock.getFiftySMA(date));
		System.out.println("EMA 50: " + stock.getFiftyEMA(date));
		System.out.println("SMA 100: " + stock.getOneHundredSMA(date));
		System.out.println("EMA 100: " + stock.getOneHundredEMA(date));
		System.out.println("ADX : " + stock.getADX(date));
		System.out.println("+DI : " + stock.getpDI(date));
		System.out.println("-DI : " + stock.getnDI(date));
		System.out.println("SAR : " + stock.getSAR(date));
		System.out.println("Stochastic Basic K:" + stock.getBasicStochasticK(date));
		System.out.println("Stochastic K(" + 3 + "): " + stock.getStochasticK(date));
		System.out.println("Stochastic D(" + 3 + "): " + stock.getStochasticD(date));
		System.out.println("");

		if (broker.getOwnedStocks().keySet().contains(stock.getSymbol())) {
			double curentStopLoss = broker.getStopLosses().get(stock.getSymbol())[0];
			double sharesOwned = broker.getStopLosses().get(stock.getSymbol())[1];
			System.out.println("*************************************");
			System.out.println("Owned");
			System.out.println(stock.getSymbol() + " current stop loss: " + curentStopLoss);
			System.out.println(stock.getSymbol() + " shares owned: " + sharesOwned);
			System.out.println("*************************************");
			System.out.println("");
		}

	}

	public static void printBrokerInfo(StockBroker broker, String date) {
		double investedFunds = 0;
		for (String symbol : broker.getOwnedStocks().keySet()) {
			investedFunds += broker.getOwnedStocks().get(symbol).getClose(date) * broker.getPortfolio().get(symbol);
		}

		for (String symbol : broker.getOwnedStocks().keySet()) {
			System.out.println();
			System.out.println("Invested in: " + symbol);
			System.out.println("Owned shares: " + broker.getPortfolio().get(symbol));

			System.out.println("Buy-In price for " + symbol + ": " + broker.getBuyInPrices().get(symbol));
			System.out.println("Stop loss for " + symbol + ": " + broker.getStopLosses().get(symbol)[0]);
			System.out.println();
		}

		System.out.println();
		System.out.println("Funds available: " + broker.getFundsAvailable());
		System.out.println("Invested funds: " + investedFunds);
		System.out.println("Total funds : " + (broker.getFundsAndInvestmentsTotal(date)));

	}

	public static boolean runPreviousDateStockAnaylsis(StockBroker broker, Stock stock, String dateIn, int timePeriodIn,
			Map<String, Integer> stocksToBuy) {
		String symbol = stock.getSymbol();
		String date = dateIn;
		String interval = stock.getInterval();
		int timePeriod = timePeriodIn;
		if (stock.loadSuccessful()) {
			// CORRECT
		} else {
			System.out.println();
			System.out.println("Symbol: " + symbol);
			System.out.println("ERROR");
			System.out.println("Skipping...");
			return false;
		}

		double rsiBottomThreshold = 30;
		int consecutivePoints = timePeriod;
		int range = 20;

		double rsiStrength = 1;// RSIAnalyzer.getStrength(stock,
								// rsiBottomThreshold, consecutivePoints, range,
								// date);
		if (rsiStrength > 0) { // && MovingAverageAnalyzer.postiveCross(stock,
								// date, timePeriod)) {

			if (broker.getOwnedStocks().keySet().contains(symbol)) {
				double curentStopLoss = broker.getStopLosses().get(stock.getSymbol())[0];
				double sharesOwned = broker.getStopLosses().get(stock.getSymbol())[1];

				System.out.println("");
				System.out.println("*************************************");
				System.out.println("Owned");
				System.out.println("Symbol: " + symbol);
				System.out.println("Date: " + date);
				System.out.println("Interval: " + interval);
				System.out.println("Time Period: " + timePeriod);
				System.out.println("RSI Strength: " + rsiStrength);
				System.out.println("RSI : " + stock.getRSI(date));
				System.out.println("SMA : " + stock.getSMA(date));
				System.out.println("EMA : " + stock.getEMA(date));
				try {
					System.out.println("Current Price: " + stock.getClose(date));
				} catch (NullPointerException n) {
					System.out.print("getClose() returned a null value for date: " + date);
				}
				System.out.println(stock.getSymbol() + " current stop loss: " + curentStopLoss);
				System.out.println(stock.getSymbol() + " shares owned: " + sharesOwned);
				System.out.println("*************************************");
				System.out.println("");

				/*
				 * if (MovingAverageAnalyzer.negativeCross(stock, date)) {
				 * broker.sell(stock, broker.sharesOwned(stock.getSymbol())); }
				 */
				return true;
			} else if (stock.getRSI(date) < rsiBottomThreshold) {
				int dateIndex = stock.getDates().indexOf(date);
				// System.out.println("BUY: POSITIVE RSI SLOPE");
				System.out.println("BUY: POSITIVE EMA CROSS SMA");
				System.out.println("Symbol: " + symbol);
				System.out.println("Date: " + date);
				System.out.println("Interval: " + interval);
				System.out.println("Time Period: " + timePeriod);
				System.out.println("Current Price: " + stock.getClose(date));
				System.out.println("RSI Strength: " + rsiStrength);
				System.out.println("RSI : " + stock.getRSI(date));
				System.out.println("SMA : " + stock.getSMA(date));
				System.out.println("EMA : " + stock.getEMA(date));

				double amountToAllocate = .038 * broker.getFundsAndInvestmentsTotal(date);// rsiStrength
				double stockPrice = stock.getClose(date);
				int numberOfShares = (int) (amountToAllocate / stockPrice);
				if (numberOfShares > 0) {
					/*
					 * broker.buy(stock, stockPrice, numberOfShares); double
					 * sellPrice = .98 * stockPrice;
					 * broker.setStopLoss(stock.getSymbol(), sellPrice,
					 * numberOfShares, date, true);
					 */
					stocksToBuy.put(stock.getSymbol(), numberOfShares);
					/*
					 * System.out.println(); System.out.println("Buy-in Price: "
					 * + stockPrice);
					 * System.out.println("Number of Shares Bought: " +
					 * numberOfShares); System.out.println("Stop-loss set at : "
					 * + sellPrice); System.out.println(
					 * "*************************************");
					 */
				}

			}
		} else {
			System.out.println("");
			System.out.println("Symbol: " + symbol);
			System.out.println("Date: " + date);
			System.out.println("Interval: " + interval);
			System.out.println("Time Period: " + timePeriod);
			try {
				System.out.println("Current Price: " + stock.getClose(date));
			} catch (NullPointerException n) {
				System.out.println("getClose() returned a null value for date: " + date);
			}
			System.out.println("RSI : " + stock.getRSI(date));
			System.out.println("SMA : " + stock.getSMA(date));
			System.out.println("EMA : " + stock.getEMA(date));
		}
		return true;
	}

	public static void writeJsons() {
		IEX av = new IEX();
		av.writeSP500Locally();
	}

	public static void updateJsons() {
		IEX av = new IEX();
		av.updateSP500Locally();
	}

	public static boolean runStockAnaylsisByDate(StockBroker broker, String symbol, String interval, String date,
			int timePeriod, String seriesType) {
		Stock stock = new Stock(symbol, interval, timePeriod, seriesType, .02, .2, 3, 3, 12, 26, 9, true);

		if (stock.loadSuccessful()) {
			// CORRECT
		} else {
			System.out.println();
			System.out.println("Symbol: " + symbol);
			System.out.println("ERROR");
			System.out.println("Skipping...");
			return false;
		}

		double rsiBottomThreshold = 30;
		int consecutivePoints = timePeriod;
		int range = 20;

		double rsiStrength = RSIAnalyzer.getStrength(stock, rsiBottomThreshold, consecutivePoints, range, date);
		if (rsiStrength > 0) {

			System.out.println("");
			System.out.println("*************************************");

			if (broker.getOwnedStocks().keySet().contains(symbol)) {
				System.out.println("Positive RSI, but already owned");
				System.out.println("Symbol: " + symbol);
				System.out.println("Date: " + date);
				System.out.println("Interval: " + interval);
				System.out.println("Time Period: " + timePeriod);
				System.out.println("RSI Strength: " + rsiStrength);
				return true;
			}

			if (MovingAverageAnalyzer.postiveCross(stock, date, timePeriod) > 1) {
				return false;
			}
			System.out.println("BUY: POSITIVE RSI SLOPE");
			System.out.println("Symbol: " + symbol);
			System.out.println("Date: " + date);
			System.out.println("Interval: " + interval);
			System.out.println("Time Period: " + timePeriod);
			try {
				System.out.println("Current Price: " + stock.getClose(date));
			} catch (NullPointerException n) {
				System.out.print("getClose() returned a null value for date: " + date);
			}
			System.out.println("RSI Strength: " + rsiStrength);

			double amountToAllocate = rsiStrength / 4 * broker.getFundsAvailable();
			double stockPrice = stock.getClose(date);
			int numberOfShares = (int) (amountToAllocate / stockPrice);
			broker.buy(stock, numberOfShares, date);
			double sellPrice = 0.98 * stockPrice;
			broker.setStopLoss(stock.getSymbol(), sellPrice, numberOfShares, date, false);

			System.out.println();
			System.out.println("Buy-in Price: " + stockPrice);
			System.out.println("Number of Shares Bought: " + numberOfShares);
			System.out.println("Stop-loss set at : " + sellPrice);
			System.out.println("*************************************");
		} else {
			System.out.println("");
			System.out.println("Symbol: " + symbol);
			System.out.println("Date: " + date);
			System.out.println("Interval: " + interval);
			System.out.println("Time Period: " + timePeriod);
			try {
				System.out.println("Current Price: " + stock.getClose(date));
			} catch (NullPointerException n) {
				System.out.println("getClose() returned a null value for date: " + date);
			}
			System.out.println("RSI : " + stock.getRSI(date));
			System.out.println("SMA : " + stock.getSMA(date));
			System.out.println("EMA : " + stock.getEMA(date));
		}
		return true;
	}

	public static void singleThreadedStockGet(StockBroker broker, String interval, String date, int timePeriod,
			String seriesType) {
		// Single-threaded {

		for (String symbol : IEX.getSP500()) {
			runStockAnaylsisByDate(broker, symbol, interval, date, timePeriod, seriesType);
		}

	}

	public static void multiThreadedStockGet(StockBroker broker, String interval, String date, int timePeriod,
			String seriesType) {
		// Multi-threaded

		final ExecutorService executor = Executors.newFixedThreadPool(50);
		final List<Future<?>> futures = new ArrayList<>();

		for (final String symbol : IEX.getSP500()) {
			Future<?> future = executor.submit(() -> {
				runStockAnaylsisByDate(broker, symbol, interval, date, timePeriod, seriesType);
			});
			futures.add(future);
		}

		int index = 0;
		do { // wait until current date is done evaluating
			if (futures.get(index).isDone()) {
				futures.get(index).cancel(true);
				index++;
			}
		} while (index < futures.size());
	}

	public static boolean sendMail(Map<String, Integer> stocksToBuy, String date) {
		Email email = new Email();
		String emailAddress = "mycarispimperthanyours@gmail.com";
		String subject = "Stocks to buy for " + date;
		String body = "";

		for (String symbol : stocksToBuy.keySet()) {
			body += symbol + "\n";
			body += "Stop Loss: " + stocksToBuy.get(symbol) + "\n\n";
		}

		if (stocksToBuy.size() > 0) {
			email.sendRealMail(emailAddress, subject, body);
			return true;
		} else {
			return false;
		}
	}

	public static void anaylzeAll(StockBroker broker, ArrayList<String> dates, String interval, int timePeriod,
			String seriesType) {
		for (int i = dates.size() - 17; i >= 0; i--) {
			String date = dates.get(i);
			// singleThreadedStockGet(broker, interval, date, timePeriod,
			// seriesType);
			multiThreadedStockGet(broker, interval, date, timePeriod, seriesType);
			printBrokerInfo(broker, date);
		}
	}

}
