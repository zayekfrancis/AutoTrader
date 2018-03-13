
public class BullishStrategy {

	/*
	 * MACD, BollingerBand, and Railway Track
	 * 
	 * 1) Wait for positive MACD cross 2) Check lookback period to see if a
	 * railway track occurred and broke the bottom Bollinger Band 3) Make sure
	 * no new low was formed waiting for MACD to flip
	 */
	public static boolean railwayTrackSignal(Stock stock, String date) {
		// 1) Wait for positive MACD cross
		String previousDate = stock.getDates().get(stock.getDates().indexOf(date) - 1);
		boolean positiveMACD = stock.getMACDHistogram(date) > 0 && stock.getMACDHistogram(previousDate) < 0;
		boolean fractalMACD = stock.getFractalStock().getMACDHistogram(date) > 0;
		boolean overbought = stock.getPercentBBand(date) > 1.5;
		if (!positiveMACD || !fractalMACD) {
			return false;
		}

		int lookBackTime = (int) (stock.getTimePeriod() / 2);
		int startingIndex = stock.getDates().indexOf(date) - lookBackTime + 1;
		int endingIndex = stock.getDates().indexOf(date);
		boolean railwayFound = false;
		double lowThreshold = 0;
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			String iDatePrevious = stock.getDates().get(i - 1);
			// 2) Check lookback period to see if a railway track occurred
			// and broke the bottom Bollinger Band
			if (!railwayFound) {
				boolean bollingerBandSignal = stock.getLow(iDate) < stock.getLowerBBand(iDate)
						|| stock.getLow(iDatePrevious) < stock.getLowerBBand(iDatePrevious);
				railwayFound = bollingerBandSignal && CandleStickAnalyzer.bullishRailwayTrack(stock, iDate);
				if (railwayFound) {
					lowThreshold = Math.min(stock.getLow(iDate), stock.getLow(iDatePrevious));
				}
				if (i == endingIndex && !railwayFound) {
					return false;
				}
			} else {
				// 3) Make sure no new low was formed waiting for MACD to flip
				boolean newLow = stock.getLow(iDate) < lowThreshold;
				if (newLow) {
					return false;
				}
			}
		}

		double percentUnderneath = .02;
		Log log = new Log(stock.getSymbol(), 0.0, "");
		log.setRailWayTrack(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.04);
		stock.setLog(log);

		return true;
	}

	public static boolean trendingBuy(StockBroker broker, Stock stock, String date) {
		int multiplier = 0;
		double stepLoss = 0.01;
		double stepGain = 0.02;
		boolean isTrending = TrendAnalyzer.isPositivelyTrending(stock, date);
		boolean fractalEnergy = FractalEnergy.buySignal(broker, stock, date);

		if (!isTrending || !fractalEnergy) {
			return false;
		} else {
			multiplier = 2;
			if (isTrending) {
				multiplier++;
			}
		}
		boolean macdMomentum = MACDAnalyzer.MACDMomentumExists(stock, date, false);
		boolean rsiMomentum = RSIAnalyzer.rsiMomentumExists(stock, date, 60, 70);
		boolean stochasticMomentum = StochasticsAnalyzer.momentumBuySignal(stock, date, false);
		boolean bBandSignal = BollingerBandsAnalyzer.buySignal(stock, date, false);

		if (macdMomentum || rsiMomentum || bBandSignal || stochasticMomentum) {
			multiplier++;
		}

		boolean rsiDivergence = RSIAnalyzer.findPositiveDivergence(stock, date) && stock.getRSI(date) > 50;
		boolean stochasticDivergence = StochasticsAnalyzer.buySignal(stock, date, false)
				&& stock.getStochasticK(date) > 50;
		boolean mfiDivergence = MoneyFlowIndex.buySignal(stock, date, 50, false);
		boolean cciDivergence = CCIAnalyzer.findPositiveDivergence(stock, date) && stock.getCCI(date) > 0;
		if (rsiDivergence || stochasticDivergence || mfiDivergence || cciDivergence) {
			multiplier++;
		}

		if (multiplier > 2) {
			Util.setLogs(stock, isTrending, false, macdMomentum, rsiMomentum, bBandSignal, stochasticMomentum, false,
					rsiDivergence, stochasticDivergence, mfiDivergence, cciDivergence, fractalEnergy);

			stock.setStepLoss(stepLoss * multiplier);
			stock.setStepGain(stepGain * multiplier);
		}
		return multiplier > 2;
	}

	public static boolean trendingBuy2(Stock stock, String date) {
		Log log = new Log(stock.getSymbol(), 0.0, "");

		// Ignore if above bollinger band
		boolean aboveBB = stock.getPercentBBand(date) > 1;
		if (aboveBB) {
			return false;
		}

		// Check if trending
		boolean trending = TrendAnalyzer.isPositivelyTrending(stock, date);
		log.setTrendingBuy(true);
		if (!trending) {
			return false;
		}

		// Bullish momentum
		String previousDate = stock.getDates().get(stock.getDates().indexOf(date) - 1);
		boolean positiveMACD = stock.getMACDHistogram(date) > 0 && stock.getMACDHistogram(previousDate) < 0;
		boolean fractalMACD = stock.getFractalStock().getMACDHistogram(date) > 0;
		log.setMACD(positiveMACD && fractalMACD);

		boolean positiveStochastic = stock.getStochasticK(date) > 50 && stock.getStochasticK(previousDate) < 50;
		boolean fractalStochastic = stock.getFractalStock().getStochasticK(date) > 50;
		log.setStochasticMomentum(positiveStochastic && fractalStochastic);

		boolean positiveRSI = stock.getRSI(date) > 50 && stock.getRSI(previousDate) < 50;
		boolean fractalRSI = stock.getFractalStock().getRSI(date) > 0;
		log.setRSIMomentum(positiveRSI && fractalRSI);

		boolean positiveMFI = stock.getMFI(date) > 50 && stock.getMFI(previousDate) < 50;
		boolean fractalMFI = stock.getFractalStock().getMFI(date) > 50;
		log.setMFIMomentum(positiveMFI && fractalMFI);

		boolean positiveCCI = stock.getCCI(date) > 100 && stock.getCCI(previousDate) < 100;
		boolean fractalCCI = stock.getFractalStock().getCCI(date) > 100;
		log.setCCIMomentum(positiveCCI && fractalCCI);

		if (!(positiveMACD && fractalMACD) && !(positiveStochastic && fractalStochastic) && !(positiveRSI && fractalRSI)
				&& !(positiveMFI && fractalMFI) && !(positiveCCI && fractalCCI)) {
			return false;
		}

		// Bullish divergence
		boolean stochasticDivergence = StochasticsAnalyzer.findPositiveDivergence(stock, date);
		boolean fractalStochasticDivergence = StochasticsAnalyzer.findPositiveDivergence(stock.getFractalStock(), date);
		log.setStochasticDivergence(stochasticDivergence && fractalStochasticDivergence);

		boolean RSIDivergence = RSIAnalyzer.findPositiveDivergence(stock, date);
		boolean fractalRSIDivergence = RSIAnalyzer.findPositiveDivergence(stock.getFractalStock(), date);
		log.setRSIDivergence(RSIDivergence && fractalRSIDivergence);

		boolean MFIDivergence = MoneyFlowIndex.findPositiveDivergence(stock, date);
		boolean fractalMFIDivergence = MoneyFlowIndex.findPositiveDivergence(stock.getFractalStock(), date);
		log.setMFIDivergence(MFIDivergence && fractalMFIDivergence);

		boolean CCIDivergence = CCIAnalyzer.findPositiveDivergence(stock, date);
		boolean fractalCCIDivergence = CCIAnalyzer.findPositiveDivergence(stock.getFractalStock(), date);
		log.setCCIDivergence(CCIDivergence && fractalCCIDivergence);

		if (!(stochasticDivergence && fractalStochasticDivergence) && !(RSIDivergence && fractalRSIDivergence)
				&& !(MFIDivergence && fractalMFIDivergence) && !(CCIDivergence && fractalCCIDivergence)) {
			return false;
		}

		double percentUnderneath = .04;

		log.setRailWayTrack(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.06);
		stock.setLog(log);
		return true;
	}

	public static boolean trendingBuy3(Stock stock, String date) {
		Log log = new Log(stock.getSymbol(), 0.0, "");

		// Ignore if above bollinger band
		boolean aboveBB = stock.getPercentBBand(date) > 1;
		if (aboveBB) {
			return false;
		}

		// Check if trending
		boolean trending = stock.getADX(date) > 25;
		boolean bullish = stock.getpDI(date) > stock.getnDI(date);
		log.setTrendingBuy(trending);
		if (!trending || !bullish) {
			return false;
		}

		// Bullish momentum
		String previousDate = stock.getDates().get(stock.getDates().indexOf(date) - 1);
		boolean positiveMACD = stock.getMACDHistogram(date) > 0 && stock.getMACDHistogram(previousDate) < 0;
		boolean fractalMACD = stock.getFractalStock().getMACDHistogram(date) > 0;
		log.setMACD(positiveMACD && fractalMACD);

		boolean positiveCCI = CCIAnalyzer.positiveCCICross(stock, date, stock.getTimePeriod(), 100);
		boolean fractalCCI = stock.getFractalStock().getCCI(date) > 100;
		log.setCCIMomentum(positiveCCI && fractalCCI);

		if (!(positiveMACD && fractalMACD) || !(positiveCCI && fractalCCI)) {
			return false;
		}

		double percentUnderneath = .04;
		log.setFractalEnergy(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.05);
		stock.setLog(log);
		return true;
	}

	public static boolean trendingBuy4(Stock stock, String date) {
		Log log = new Log(stock.getSymbol(), 0.0, "");

		// Check if trending
		boolean trending = TrendAnalyzer.isPositivelyTrending(stock, date)
				&& stock.getLow(date) < stock.getTwentyEMA(date);
		log.setTrendingBuy(trending);
		if (!trending) {
			return false;
		}

		// Check momentum
		String previousDate = stock.getDates().get(stock.getDates().indexOf(date) - 1);
		boolean positiveMACD = true;// stock.getMACDHistogram(date) > 0;
		if (!positiveMACD) {
			return false;
		}

		// Fractal C
		boolean fractalStochastics = stock.getFractalStock().getStochasticK(date) > 50
				&& stock.getFractalStock().getStochasticK(date) > stock.getFractalStock().getStochasticD(date);

		boolean fractalMACD = stock.getFractalStock().getMACDHistogram(date) > 0;

		if (false) {// (!fractalStochastics && !fractalMACD) {
			return false;
		}

		double percentUnderneath = .05;
		log.setFractalEnergy(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.075);
		stock.setLog(log);
		return true;
	}

	public static boolean nonTrendingBuy(Stock stock, String date) {
		boolean buy = ADXAnalyzer.nonTrendingBuySignal(stock, stock.getTimePeriod(), date)
				&& BollingerBandsAnalyzer.nonTrendingBuySignal(stock, date)
				&& RSIAnalyzer.nonTrendingBuySignal(stock, date)
				&& MACDAnalyzer.nonTrendingMACDMomentumExists(stock, date, false);

		if (buy) {
			Log log = new Log(stock.getSymbol(), 0, "");
			log.setNonTrendingBuy(true);
			stock.setLog(log);
		}
		return buy;
	}

	/*
	 * Moving Average Crosses
	 * 
	 * Buy when EMA(5) crosses EMA(20) and when MACD is positive. (current and
	 * fractal)
	 */
	public static boolean emaCrossSignal(Stock stock, String date) {
		String previousDate = stock.getDates().get(stock.getDates().indexOf(date) - 1);
		boolean positiveMACD = stock.getMACDHistogram(date) > 0;
		boolean fractalMACD = stock.getFractalStock().getMACDHistogram(date) > 0;
		boolean emaCross = stock.getFiveEMA(date) > stock.getTwentyEMA(date)
				&& stock.getFiveEMA(previousDate) < stock.getTwentyEMA(previousDate);

		double percentUnderneath = .10;
		Log log = new Log(stock.getSymbol(), 0.0, "");
		log.setRailWayTrack(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.15);
		stock.setLog(log);

		return positiveMACD && fractalMACD && emaCross;
	}

	/*
	 * Exponential Moving Average and CCI signal
	 * 
	 * Buy when price is above EMA(50) and CCI breaks 100
	 * 
	 * Stats: 3% per trade when selling on fractal K cross D
	 */
	public static boolean emaAndCCISignal(Stock stock, String date) {
		// Check for trend
		boolean trendExists = stock.getADX(date) > 25 && stock.getpDI(date) > stock.getnDI(date);
		if (!trendExists) {
			return false;
		}
		// Current interval signals
		double currentPrice = stock.getClose(date);
		double currentEMA = stock.getFiftyEMA(date);
		boolean bullishCCI = stock.getCCI(date) > 100;
		boolean recentCCICross = CCIAnalyzer.positiveCCICross(stock, date, stock.getTimePeriod(), 100);
		boolean recentCCIHigh = CCIAnalyzer.recentCCIHigh(stock, date, 300);
		boolean bollingerBandBreak = true;// stock.getBollingerBandwidth(date) >
											// .1;
		boolean emaCross = currentPrice > currentEMA;

		if (!emaCross || !bullishCCI || !bollingerBandBreak || !recentCCICross || recentCCIHigh) {
			return false;
		}

		// Fractal interval signals
		double fractalPrice = stock.getFractalStock().getClose(date);
		double fractalEMA = stock.getFractalStock().getFiftyEMA(date);
		boolean fractalCCI = stock.getFractalStock().getCCI(date) > 100;
		boolean fractalEMACross = fractalPrice > fractalEMA;
		boolean fractalBollingerBandBreak = stock.getFractalStock().getPercentBBand(date) < 1;

		if (!fractalEMACross || !fractalCCI || !fractalBollingerBandBreak) {
			return false;
		}

		// Look for bearish divergences
		boolean bearishDivergence = CCIAnalyzer.findNegativeDivergence(stock, date);
		boolean fractalBearishDivergence = CCIAnalyzer.findNegativeDivergence(stock.getFractalStock(), date);

		if (bearishDivergence || fractalBearishDivergence) {
			return false;
		}

		// Look for bullish divergences
		boolean bullishDivergence = CCIAnalyzer.findPositiveDivergence(stock, date);
		boolean fractalBullishDivergence = CCIAnalyzer.findPositiveDivergence(stock.getFractalStock(), date);

		if (!bullishDivergence || !fractalBullishDivergence) {
			return false;
		}

		double percentUnderneath = .07;
		Log log = new Log(stock.getSymbol(), 0.0, "");
		log.setRailWayTrack(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.04);
		stock.setLog(log);

		return true;
	}

	/*
	 * Exponential Moving Average and CCI signal
	 * 
	 * Buy when price is above EMA(50) and CCI breaks 100
	 * 
	 * Stats:
	 */
	public static boolean fiveEnergiesSignal(Stock stock, String date) {
		// Check for trend
		boolean trendExists = TrendAnalyzer.isPositivelyTrending(stock, date);
		if (!trendExists) {
			return false;
		}

		// Check for momentum
		boolean positiveMACD = MACDAnalyzer.recentPositiveMACDCross(stock, date);
		boolean fractalPositiveMACD = MACDAnalyzer.recentPositiveMACDCross(stock.getFractalStock(), date);

		if (!positiveMACD || fractalPositiveMACD) {
			return false;
		}

		double percentUnderneath = .08;
		Log log = new Log(stock.getSymbol(), 0.0, "");
		log.setRailWayTrack(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.06);
		stock.setLog(log);

		return true;
	}
	
	/*
	 * Exponential Moving Average and CCI signal and Divergence
	 * 
	 * Buy when price is above EMA(50) and CCI breaks 100 and CCI Divergence
	 * 
	 * Stats:
	 */
	public static boolean fiveEnergiesSignalwDivergence(Stock stock, String date) {
		// Check for trend
		boolean trendExists = TrendAnalyzer.isPositivelyTrending(stock, date);
		if (!trendExists) {
			return false;
		}

		// Check for momentum
		boolean positiveMACD = MACDAnalyzer.recentPositiveMACDCross(stock, date);
		boolean fractalPositiveMACD = stock.getFractalStock().getMACDHistogram(date) > 0;

		if (!positiveMACD || fractalPositiveMACD) {
			return false;
		}
		
		// Check for divergence
		boolean fractalDivergence = CCIAnalyzer.findPositiveDivergence(stock.getFractalStock(), date);
		boolean bullishCross = stock.getCCI(date) > 100 && stock.getFractalStock().getCCI(date) > 0;
		
		if (!fractalDivergence || !bullishCross) {
			return false;
		}

		double percentUnderneath = .04;
		Log log = new Log(stock.getSymbol(), 0.0, "");
		log.setEMAMomentum(true);
		stock.setStepLoss(percentUnderneath);
		stock.setStepGain(.06);
		stock.setLog(log);

		return true;
	}
}
