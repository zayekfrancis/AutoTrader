 import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockBroker {

	private Map<String, Stock> ownedStocks;
	private Map<String, Double> buyInPrices;
	private Map<String, String> buyInDates;
	private Map<String, Integer> portfolio;
	private Map<String, Double[]> stopLosses;
	private Map<String, Log> logs;
	private double funds;

	private String interval;

	public StockBroker(double fundsIn, String intervalIn) {
		ownedStocks = new HashMap<String, Stock>();
		portfolio = new HashMap<String, Integer>();
		buyInPrices = new HashMap<String, Double>();
		buyInDates = new HashMap<String, String>();
		stopLosses = new HashMap<String, Double[]>();
		logs = new HashMap<String, Log>();
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

		double buyPrice = buyInPrices.get(stock.getSymbol());
		double percentageChange = (latestPrice / buyPrice * 100) - 100; 
		logs.get(stock.getSymbol() + buyInDates.get(stock.getSymbol())).setSellDate(dateIn);
		logs.get(stock.getSymbol() + buyInDates.get(stock.getSymbol())).setSellPrice(latestPrice);
		logs.get(stock.getSymbol() + buyInDates.get(stock.getSymbol())).setPercentageChange(percentageChange);
		
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

		double buyPrice = buyInPrices.get(symbol);
		double percentageChange = (latestPrice / buyPrice * 100) - 100;
		logs.get(symbol + buyInDates.get(symbol)).setSellDate(dateIn);
		logs.get(symbol + buyInDates.get(symbol)).setSellPrice(latestPrice);
		logs.get(symbol + buyInDates.get(symbol)).setPercentageChange(percentageChange);
		
		removeStocks(symbol, numberOfShares);
		funds += cost;
		return true;
	}

	public boolean updateStopLosses(String dateIn) {
		Collection<String> symbols = new ArrayList<String>();
		for (String symbol : stopLosses.keySet()) {
			symbols.add(symbol);
		}
		for (String symbol : symbols) {
			Stock stock = ownedStocks.get(symbol);
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
			double buyInPrice = buyInPrices.get(symbol);
			//double newStopLoss = Util.getStopLossPrice(buyInDates, stock, dateIn);
			double newStopLoss = Util.getStopLossPrice(stock, dateIn, buyInPrice, stock.getStepLoss(), stock.getStepGain());
			if (latestOpeningPrice < previousStopLoss || lowestIntraDayPrice < previousStopLoss
					|| latestClosingPrice < previousStopLoss) {
				printSaleDetails(symbol, sellPrice, numberOfShares);
				
				sell(symbol, sellPrice, numberOfShares, dateIn);
			} else if (previousStopLoss < newStopLoss) {
				setStopLoss(symbol, newStopLoss, numberOfShares, dateIn, false);
			} else {
				setStopLoss(symbol, previousStopLoss, numberOfShares, dateIn, false);
			}
		}
		return false;
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
			logs.put((stock.getSymbol() + date), stock.getLog());
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
			logs.put((symbol + date), new Log(symbol, marketPrice, date));
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

	public void sellEverything() {
		for (Stock stock : ownedStocks.values()) {
			String symbol = stock.getSymbol();
			double sellPrice = stock.getLatestClosingPrice();
			int numberOfShares = portfolio.get(symbol);
			printSaleDetails(symbol, sellPrice, numberOfShares);
			sell(symbol, sellPrice, numberOfShares);
		}
	}
	
	public void splitFunds(double allocationAmount, String date)
	{
		int numberOfStocksOwned = ownedStocks.size();
		double priceTargetForStock = allocationAmount / numberOfStocksOwned;
		for (Stock stock : ownedStocks.values()) {
			double currentPrice = stock.getOpen(date);
			int sharesToSell = (int) (priceTargetForStock / currentPrice);
			String symbol = stock.getSymbol();
			sell(symbol, currentPrice, sharesToSell);
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

	public void printLogData() {
		ArrayList<Log> originalLogs = new ArrayList<Log>();
		originalLogs.addAll(logs.values());
		for (Log log : originalLogs) {
			if (!log.isSold()) {
				logs.remove(log.getSymbol() + log.getBuyDate());
				continue;
			}
		}
		List<Log> sortedLogs = new ArrayList<Log>();
		sortedLogs.addAll(logs.values());
		Collections.sort(sortedLogs);
		
		int tWins = 0;
		int tLosses = 0;
		int ntWins = 0;
		int ntLosses = 0;
		
		int macdCountWins = 0;
		int macdCountLosses = 0;
		int rsiMomentumWins = 0;
		int rsiMomentumLosses = 0;
		int cciMomentumWins = 0;
		int cciMomentumLosses = 0;
		int mfiMomentumWins = 0;
		int mfiMomentumLosses = 0;
		int BBWins = 0;
		int BBLosses = 0;
		int stochasticMomentumWins = 0;
		int stochasticMomentumLosses = 0;
		int EMAWins = 0;
		int EMALosses = 0;
		
		int stochasticWins = 0;
		int stochasticLosses = 0;
		int rsiDivergenceWins = 0;
		int rsiDivergenceLosses = 0;
		int mfiDivergenceWins = 0;
		int mfiDivergenceLosses = 0;
		int cciDivergenceWins = 0;
		int cciDivergenceLosses = 0;
		
		int fractalEnergyWins = 0;
		int fractalEnergyLosses = 0;
		
		int railWayWins = 0;
		int railWayLosses = 0;
		
		double totalPercentageGains = 0;
		for (Log log : sortedLogs) {
			System.out.println();
			System.out.println("*********************");
			System.out.println("Symbol: " + log.getSymbol());
			System.out.println("Percentage Change: " + log.getPercentageChange());
			System.out.println("Buy-In Price: " + log.getBuyPrice());
			System.out.println("Buy-In Date: " + log.getBuyDate());
			System.out.println("Sell Price: " + log.getSellPrice());
			System.out.println("Sell Date: " + log.getSellDate());
			
			System.out.println("Trending: " + log.isTrendingBuy());
			System.out.println("Non Trending: " + log.isNonTrendingBuy());
			
			System.out.println("MACD: " + log.isMACD());
			System.out.println("RSI Momentum: " + log.isRSIMomentum());
			System.out.println("CCI Momentum: " + log.isCCIMomentum());
			System.out.println("MFI Momentum: " + log.isMFIMomentum());
			System.out.println("Bollinger Band Signal: " + log.isBBSignal());
			System.out.println("Stochastics Momentum: " + log.isStochasticMomentum());
			System.out.println("EMA Momentum: " + log.isEMAMomentum());
			
			System.out.println("Stochastics Signal: " + log.isStochasticDivergence());
			System.out.println("RSI Divergence: " + log.isRSIDivergence());
			System.out.println("MFI Divergence Signal: " + log.isMFIDivergence());
			System.out.println("CCI Divergence Signal: " + log.isCCIDivergence());

			System.out.println("Fractal Energy Signal: " + log.isFractalEnergy());
			
			System.out.println("RailWay Track Signal: " + log.isRailWayTrack());
			
			System.out.println("*********************");
			System.out.println();
			
			totalPercentageGains += log.getPercentageChange(); 
			if (log.getPercentageChange() >= 0) {
				if (log.isTrendingBuy()) { tWins++; }
				if (log.isNonTrendingBuy()) { ntWins++; }
				
				if (log.isMACD()) { macdCountWins++; }
				if (log.isRSIMomentum()) { rsiMomentumWins++; }
				if (log.isCCIMomentum()) { cciMomentumWins++; }
				if (log.isMFIMomentum()) { mfiMomentumWins++; }
				if (log.isBBSignal()) { BBWins++; }
				if (log.isStochasticMomentum()) { stochasticMomentumWins++; }
				if (log.isEMAMomentum()) { EMAWins++; }
				
				if (log.isRSIDivergence()) { rsiDivergenceWins++; }
				if (log.isStochasticDivergence()) { stochasticWins++; }
				if (log.isMFIDivergence()) { mfiDivergenceWins++; }
				if (log.isCCIDivergence()) { cciDivergenceWins++; }
				
				if (log.isFractalEnergy()) { fractalEnergyWins++; }
				
				if (log.isRailWayTrack()) { railWayWins++; }
			}
			else {
				if (log.isTrendingBuy()) { tLosses++; }
				if (log.isNonTrendingBuy()) { ntLosses++; }
				
				if (log.isMACD()) { macdCountLosses++; }
				if (log.isRSIMomentum()) { rsiMomentumLosses++; }
				if (log.isCCIMomentum()) { cciMomentumLosses++; }
				if (log.isMFIMomentum()) { mfiMomentumLosses++; }
				if (log.isBBSignal()) { BBLosses++; }
				if (log.isStochasticMomentum()) { stochasticMomentumLosses++; }
				if (log.isEMAMomentum()) { EMALosses++; }
				
				if (log.isRSIDivergence()) { rsiDivergenceLosses++; }
				if (log.isStochasticDivergence()) { stochasticLosses++; }
				if (log.isMFIDivergence()) { mfiDivergenceLosses++; }
				if (log.isCCIDivergence()) { cciDivergenceLosses++; }
				
				if (log.isFractalEnergy()) { fractalEnergyLosses++; }
				
				if (log.isRailWayTrack()) { railWayLosses++; }
			}
		}
		
		System.out.println();
		System.out.println("*********************");
		System.out.println("Trending: ");
		System.out.println("Total Wins: " + tWins);
		System.out.println("Total Losses: " + tLosses);
		System.out.println();
		System.out.println("Non Trending: ");
		System.out.println("Total Wins: " + ntWins);
		System.out.println("Total Losses: " + ntLosses);
		System.out.println();
		System.out.println("MACD: ");
		System.out.println("Total Wins: " + macdCountWins);
		System.out.println("Total Losses: " + macdCountLosses);
		System.out.println();
		System.out.println("RSI Momentum: ");
		System.out.println("Total Wins: " + rsiMomentumWins);
		System.out.println("Total Losses: " + rsiMomentumLosses);
		System.out.println();
		System.out.println("CCI Momentum: ");
		System.out.println("Total Wins: " + cciMomentumWins);
		System.out.println("Total Losses: " + cciMomentumLosses);
		System.out.println();
		System.out.println("MFI Momentum: ");
		System.out.println("Total Wins: " + mfiMomentumWins);
		System.out.println("Total Losses: " + mfiMomentumLosses);
		System.out.println();
		System.out.println("Bollinger Band Signal: ");
		System.out.println("Total Wins: " + BBWins);
		System.out.println("Total Losses: " + BBLosses);
		System.out.println();
		System.out.println("Stochastics Momentum Signal: ");
		System.out.println("Total Wins: " + stochasticMomentumWins);
		System.out.println("Total Losses: " + stochasticMomentumLosses);
		System.out.println();
		System.out.println("EMA Momentum: ");
		System.out.println("Total Wins: " + EMAWins);
		System.out.println("Total Losses: " + EMALosses);
		System.out.println();
		System.out.println("RSI Divergence: ");
		System.out.println("Total Wins: " + rsiDivergenceWins);
		System.out.println("Total Losses: " + rsiDivergenceLosses);
		System.out.println();
		System.out.println("Stochastics Signal: ");
		System.out.println("Total Wins: " + stochasticWins);
		System.out.println("Total Losses: " + stochasticLosses);	
		System.out.println();
		System.out.println("MFI Divergence Signal: ");
		System.out.println("Total Wins: " + mfiDivergenceWins);
		System.out.println("Total Losses: " + mfiDivergenceLosses);
		System.out.println();
		System.out.println("CCI Divergence Signal: ");
		System.out.println("Total Wins: " + cciDivergenceWins);
		System.out.println("Total Losses: " + cciDivergenceLosses);
		System.out.println();
		System.out.println("Fractal Energy Signal: ");
		System.out.println("Total Wins: " + fractalEnergyWins);
		System.out.println("Total Losses: " + fractalEnergyLosses);
		System.out.println();
		System.out.println("RailWay Track Signal: ");
		System.out.println("Total Wins: " + railWayWins);
		System.out.println("Total Losses: " + railWayLosses);
		System.out.println("*********************");
		
		System.out.println("*********************");
		System.out.println("*********************");
		System.out.println("Total Percentage Gains: " + totalPercentageGains);
		System.out.println("*********************");
		System.out.println("*********************");
		
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
	
	public Map<String, Log> getLogs() {
		return logs;
	}
	
}
