import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StockBroker {

	private Map<String, Stock> ownedStocks;
	private Map<String, Double> buyInPrices;
	private Map<String, String> buyInDates;
	private Map<String, Integer> portfolio;
	private Map<String, Double[]> stopLosses;
	private double funds;

	private String interval;

	public StockBroker(double fundsIn, String intervalIn) {
		ownedStocks = new HashMap<String, Stock>();
		portfolio = new HashMap<String, Integer>();
		buyInPrices = new HashMap<String, Double>();
		buyInDates = new HashMap<String, String>();
		stopLosses = new HashMap<String, Double[]>();
		funds = fundsIn;
		interval = intervalIn;
	}

	public boolean buy(Stock stock, int numberOfShares, String dateIn) {
		double latestPrice = stock.getClose(dateIn);
		double cost = latestPrice * numberOfShares;
		if (funds > cost) {
			addStocks(stock, latestPrice, numberOfShares, dateIn);
			funds -= cost;
			return true;
		}

		return false;
	}

	public boolean buy(Stock stock, double marketPrice, int numberOfShares, String date) {
		double latestPrice = marketPrice;
		double cost = latestPrice * numberOfShares;
		if (funds > cost) {
			addStocks(stock, marketPrice, numberOfShares, date);
			funds -= cost;
			return true;
		}

		return false;
	}

	public boolean sell(Stock stock, int numberOfShares) {
		double latestPrice = stock.getLatestClosingPrice();
		double cost = latestPrice * numberOfShares;

		removeStocks(stock.getSymbol(), numberOfShares);
		funds += cost;
		return true;
	}

	public boolean sell(Stock stock, int numberOfShares, String dateIn) {
		double latestPrice = stock.getClose(dateIn);
		double cost = latestPrice * numberOfShares;

		removeStocks(stock.getSymbol(), numberOfShares);
		funds += cost;
		return true;
	}

	public boolean sell(String symbol, double marketPrice, int numberOfShares) {
		double latestPrice = marketPrice;
		double cost = latestPrice * numberOfShares;

		removeStocks(symbol, numberOfShares);
		funds += cost;
		return true;
	}

	public boolean sell(String symbol, double marketPrice, int numberOfShares, String dateIn) {
		double latestPrice = marketPrice;
		double cost = latestPrice * numberOfShares;

		removeStocks(symbol, numberOfShares);
		funds += cost;
		return true;
	}

	public boolean updateStopLosses() {
		Collection<String> symbols = stopLosses.keySet();
		for (String symbol : symbols) {
			double stopLossRatio = RSIAnalyzer.getRatio(ownedStocks.get(symbol),
					ownedStocks.get(symbol).getDates().get(0));
			double latestPrice = ownedStocks.get(symbol).getLatestClosingPrice();
			double previousStopLoss = stopLosses.get(symbol)[0];
			int numberOfShares = stopLosses.get(symbol)[1].intValue();
			if (previousStopLoss < latestPrice * stopLossRatio) {
				double newStopLoss = latestPrice * stopLossRatio;
				setStopLoss(symbol, newStopLoss, numberOfShares);
			} else {
				setStopLoss(symbol, previousStopLoss, numberOfShares);
			}
		}
		return false;
	}

	public boolean updateStopLosses(String dateIn) {
		Collection<String> symbols = new ArrayList<String>();
		for (String symbol : stopLosses.keySet()) {
			symbols.add(symbol);
		}
		for (String symbol : symbols) {
			Stock stock = ownedStocks.get(symbol);
			double stopLossRatio = SARAnalyzer.getRatio(stock, dateIn);
			// Math.max(RSIAnalyzer.getRatioV2(stock, dateIn, .95),
			// MovingAverageAnalyzer.getRatio(stock, dateIn));
			// stopLossRatio = Math.max(stopLossRatio,
			// ADXAnalyzer.getRatio(stock, dateIn));
			double latestOpeningPrice = stock.getOpen(dateIn);
			double lowestIntraDayPrice = stock.getLow(dateIn);
			double latestClosingPrice = stock.getClose(dateIn);
			double previousStopLoss = stopLosses.get(symbol)[0];

			double sellPrice = 0;
			if (latestOpeningPrice < previousStopLoss) {
				sellPrice = latestOpeningPrice;
			} else if (lowestIntraDayPrice < previousStopLoss || latestClosingPrice < previousStopLoss) {
				sellPrice = previousStopLoss;
			} else {
				sellPrice = latestClosingPrice;
			}
			int numberOfShares = stopLosses.get(symbol)[1].intValue();
			double newStopLoss = Math.max(
					SARAnalyzer.getRatio(stock, dateIn), 
					ADXAnalyzer.getRatio(stock, 
										 buyInDates.get(stock.getSymbol()), 
										 dateIn)); 
			if (latestOpeningPrice < previousStopLoss || lowestIntraDayPrice < previousStopLoss
					|| latestClosingPrice < previousStopLoss) {
				printSaleDetails(symbol, sellPrice, numberOfShares);
				double buyInPrice = buyInPrices.get(symbol);
				sell(symbol, sellPrice, numberOfShares);
				try {
					if (buyInPrice *.99 > sellPrice) {
						Thread.sleep(5000);
					}
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (previousStopLoss < newStopLoss) {
				setStopLoss(symbol, newStopLoss, numberOfShares, dateIn, false);
			} else {
				setStopLoss(symbol, previousStopLoss, numberOfShares, dateIn, false);
			}
		}
		return false;
	}
	
	public void sellEverything() {
		for (Stock stock : ownedStocks.values()) {
			String symbol = stock.getSymbol();
			double sellPrice = stock.getLatestClosingPrice();
			int numberOfShares = portfolio.get(symbol);
			printSaleDetails(symbol, sellPrice, numberOfShares);
			sell(symbol, sellPrice, numberOfShares);
		}
	}

	public boolean setStopLoss(String symbol, double sellPrice, int numberOfShares) {
		double latestPrice = ownedStocks.get(symbol).getLatestClosingPrice();
		double previousStopLoss = 0;
		if (stopLosses.containsKey(symbol)) {
			previousStopLoss = stopLosses.get(symbol)[0];
		}
		if (sellPrice > latestPrice) {
			sell(symbol, latestPrice, numberOfShares);
			printSaleDetails(symbol, latestPrice, numberOfShares);
			return true;
		} else if (sellPrice < latestPrice && previousStopLoss != 0 && sellPrice != previousStopLoss) {
			Double stopLoss[] = { sellPrice, (double) numberOfShares };
			printUpdatedStopLoss(symbol, previousStopLoss, sellPrice, numberOfShares);
			stopLosses.put(symbol, stopLoss);
			return false;
		} else {
			Double stopLoss[] = { sellPrice, (double) numberOfShares };
			stopLosses.put(symbol, stopLoss);
			return false;
		}
	}

	public boolean setStopLoss(String symbol, double sellPrice, int numberOfShares, String dateIn,
			boolean buyingTomorrowsOpening) {
		double previousStopLoss = 0;
		if (stopLosses.containsKey(symbol)) {
			previousStopLoss = stopLosses.get(symbol)[0];
		}

		double latestPrice = 0;
		try {
			latestPrice = ownedStocks.get(symbol).getClose(dateIn);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		if (sellPrice < latestPrice && previousStopLoss != 0 && sellPrice != previousStopLoss) {
			Double stopLoss[] = { sellPrice, (double) numberOfShares };
			printUpdatedStopLoss(symbol, previousStopLoss, sellPrice, numberOfShares);
			stopLosses.put(symbol, stopLoss);
			return false;
		} else {
			Double stopLoss[] = { sellPrice, (double) numberOfShares };
			stopLosses.put(symbol, stopLoss);
			return false;
		}
	}

	private void addStocks(Stock stock, double marketPrice, int numberOfBoughtShares, String date) {
		int numberOfOwnedShares = 0;
		if (portfolio.containsKey(stock.getSymbol())) {
			numberOfOwnedShares = portfolio.get(stock.getSymbol());
			double currentlyInvested = buyInPrices.get(stock.getSymbol()) * numberOfOwnedShares;

			portfolio.put(stock.getSymbol(), numberOfOwnedShares + numberOfBoughtShares);
			double newInvestment = marketPrice * numberOfBoughtShares;
			buyInPrices.put(stock.getSymbol(),
					(currentlyInvested + newInvestment) / (numberOfOwnedShares + numberOfBoughtShares));

		} else {
			ownedStocks.put(stock.getSymbol(), stock);
			portfolio.put(stock.getSymbol(), numberOfBoughtShares);
			buyInPrices.put(stock.getSymbol(), marketPrice);
			buyInDates.put(stock.getSymbol(), date);
		}
	}

	private void addStocks(String symbol, double marketPrice, int numberOfBoughtShares, String date) {
		int numberOfOwnedShares = 0;
		if (portfolio.containsKey(symbol)) {
			numberOfOwnedShares = portfolio.get(symbol);
			numberOfOwnedShares = portfolio.get(symbol);
			double currentlyInvested = buyInPrices.get(symbol) * numberOfOwnedShares;

			portfolio.put(symbol, numberOfOwnedShares + numberOfBoughtShares);
			double newInvestment = marketPrice * numberOfBoughtShares;
			buyInPrices.put(symbol, (currentlyInvested + newInvestment) / (numberOfOwnedShares + numberOfBoughtShares));
		} else {
			ownedStocks.put(symbol, new Stock(symbol, interval));
			portfolio.put(symbol, numberOfBoughtShares);
			buyInPrices.put(symbol, marketPrice);
			buyInDates.put(symbol, date);
		}
	}

	private void removeStocks(String symbol, int numberOfSoldShares) {
		int numberOfOwnedShares = 0;
		if (portfolio.containsKey(symbol)) {
			numberOfOwnedShares = portfolio.get(symbol);

			if (numberOfOwnedShares - numberOfSoldShares == 0) {
				ownedStocks.remove(symbol);
				portfolio.remove(symbol);
				buyInPrices.remove(symbol);
				buyInDates.remove(symbol);
				stopLosses.remove(symbol);
			} else {
				portfolio.put(symbol, numberOfOwnedShares - numberOfSoldShares);
			}

		}
	}

	public double getFundsAvailable() {
		return funds;
	}

	public double getFundsAndInvestmentsTotal(String date) {
		double investedFunds = 0;
		for (String symbol : getOwnedStocks().keySet()) {
			investedFunds += getOwnedStocks().get(symbol).getClose(date) * getPortfolio().get(symbol);
		}
		return getFundsAvailable() + investedFunds;
	}

	public void setFunds(double fundsIn) {
		funds = fundsIn;
	}

	public void addFunds(double fundsIn) {
		funds += fundsIn;
	}

	public int sharesOwned(String symbol) {
		if (portfolio.containsKey(symbol)) {
			return portfolio.get(symbol);
		}
		return 0;
	}

	public int updateStockPrices() {
		int updatedStocks = 0;
		int numberOfStocksOwned = ownedStocks.size();
		for (Stock stock : ownedStocks.values()) {
			if (stock.initPrices(stock.getSymbol(), interval, true)) {
				ownedStocks.put(stock.getSymbol(), stock);
				updatedStocks++;
			}
		}
		return numberOfStocksOwned - updatedStocks;
	}

	public int analyzeStocks(int timePeriodIn, String seriesTypeIn, boolean local) {
		int analyzedStocks = 0;
		int numberOfStocksOwned = ownedStocks.size();
		for (Stock stock : ownedStocks.values()) {
			if (stock.initIndicators()) {
				ownedStocks.put(stock.getSymbol(), stock);
				analyzedStocks++;
			}
		}
		return numberOfStocksOwned - analyzedStocks;
	}

	public void printSaleDetails(String symbol, double sellPrice, int numberOfShares) {
		System.out.println();
		System.out.println("*********************");
		System.out.println("Symbol: " + symbol);
		System.out.println("Buy-In Price: " + buyInPrices.get(symbol));
		System.out.println("Buy-In Date: " + buyInDates.get(symbol));
		System.out.println("Sell Price: " + sellPrice);
		System.out.println("Number of Shares: " + numberOfShares);
		System.out.println("*********************");
		System.out.println();
	}

	public void printUpdatedStopLoss(String symbol, double oldStopLoss, double newStopLoss, int numberOfShares) {
		System.out.println();
		System.out.println("*********************");
		System.out.println("Symbol: " + symbol);
		System.out.println("Buy-In Price: " + buyInPrices.get(symbol));
		System.out.println("Buy-In Date: " + buyInDates.get(symbol));
		System.out.println("Old Stop Loss: " + oldStopLoss);
		System.out.println("New Stop Loss: " + newStopLoss);
		System.out.println("Number of Shares: " + numberOfShares);
		System.out.println("*********************");
		System.out.println();
	}

	public Map<String, Stock> getOwnedStocks() {
		return ownedStocks;
	}

	public Map<String, Integer> getPortfolio() {
		return portfolio;
	}

	public Map<String, Double[]> getStopLosses() {
		return stopLosses;
	}

	public Map<String, Double> getBuyInPrices() {
		return buyInPrices;
	}
	
	public Map<String, String> getBuyInDates() {
		return buyInDates;
	}
}
