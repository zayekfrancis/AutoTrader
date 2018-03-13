import java.util.Map;

public class Util {

	public static double getStopLossPrice(Map<String, String> buyInDates, Stock stock, String date) {
		boolean fractalSell = FractalEnergy.sellSignal(stock, date);
		boolean adxSell = ADXAnalyzer.sellSignal(stock, stock.getTimePeriod(), date);
		boolean stochasticSell = StochasticsAnalyzer.sellSignal(stock, date, false);
		boolean momentumSell = MomentumAnalyzer.sellSignal(stock, date, false);
		boolean nonTrendingSell = RSIAnalyzer.nonTrendingSellSignal(stock, date) 
								  || BollingerBandsAnalyzer.nonTrendingSellSignal(stock, date); 
		
		if (stock.getLog().isNonTrendingBuy()) {
			if (nonTrendingSell) {
				return stock.getClose(date);
			}
			
			return SARAnalyzer.getRatio(stock, date);
		}
		if (fractalSell && (adxSell || stochasticSell || momentumSell)) {
			return stock.getClose(date);
		}
		return Math.max(
				SARAnalyzer.getRatio(stock, date), 
				ADXAnalyzer.getRatio(stock, 
									 buyInDates.get(stock.getSymbol()), 
									 date)); 
	}
	
	public static double getStopLossPrice(Stock stock, String date, double buyInPrice, double percentStepLoss, double percentStepTarget) {
		int index = stock.getDates().indexOf(date);
		String date2 = "";
		String date1 = "";
		double low2 = 0;
		double low1 = 0;
		if (index != -1) {
			date2 = stock.getDates().get(index - 2);
			date1 = stock.getDates().get(index - 1);
			low2 = stock.getLow(date2);
			low1 = stock.getLow(date1);
		}
		
		double currentPrice = stock.getClose(date);
		double cutLossPrice = buyInPrice * (1 - percentStepLoss);
		double targetPrice = buyInPrice * (1 + percentStepTarget);
		double minLow = Math.min(low1, low2);
		
		if (stock.getFractalStock().getStochasticK(date) < stock.getFractalStock().getStochasticD(date)) {
			return stock.getClose(date);
		}
		if (currentPrice > cutLossPrice && currentPrice <= targetPrice) {
			return cutLossPrice;
		}
		/*else if (stock.getSAR(date) < currentPrice && stock.getSAR(date) > targetPrice){
			return Math.max(cutLossPrice, minLow);
		}
		else if (currentPrice > targetPrice) {
			return Math.max(targetPrice, minLow);
		}*/
		
		else {
			return cutLossPrice;
		}
	}

	public static int findBottoms(double[] values, int range) {
		return findBottom(values, range);
	}

	public static int findPeaks(double[] values, int range) {
		return findPeak(values, range);
	}

	private static int findPeak(double[] array, int range) {
		int result = 0, l, r;
		int peak = 0;
		// Check main body
		for (int i = 0; i < array.length; i++) {
			boolean isPeak = true;
			// Check from left to right
			l = Math.max(0, i - range);
			r = Math.min(array.length - 1, i + range);
			for (int j = l; j <= r; j++) {
				// Skip if we are on current
				if (i == j) {
					continue;
				}
				if (array[i] < array[j]) {
					isPeak = false;
					break;
				}
			}

			if (isPeak) {
				result++;
				peak = i;
				i += range;
			}
		}

		if (result == 0 || result == array.length - 1) {
			return -1;
		}
		return peak;
	}

	private static int findBottom(double[] array, int range) {
		int result = 0, l, r;
		int bottom = 0;
		// Check main body
		for (int i = 0; i < array.length; i++) {
			boolean isBottom = true;
			// Check from left to right
			l = Math.max(0, i - range);
			r = Math.min(array.length - 1, i + range);
			for (int j = l; j <= r; j++) {
				// Skip if we are on current
				if (i == j) {
					continue;
				}
				if (array[i] > array[j]) {
					isBottom = false;
					break;
				}
			}

			if (isBottom) {
				result++;
				bottom = i;
				i += range;
			}
		}

		if (result == 0 || result == array.length - 1) {
			return -1;
		}
		return bottom;
	}
	
	public static void setLogs(Stock stock, 
			boolean trending,
			boolean nonTrending,
			boolean macDMomentum, 
			boolean rsiMomentum, 
			boolean bBandSignal, 
			boolean stochasticMomentum,
			boolean EMAMomentum,
			boolean rsiDivergence,
			boolean stochasticDivergence, 
			boolean mfiDivergence, 
			boolean cciDivergence, 
			boolean fractalEnergy) {
		Log log = new Log(stock.getSymbol(), 0, "");
		log.setTrendingBuy(trending);
		log.setNonTrendingBuy(nonTrending);
		
		log.setMACD(macDMomentum);
		log.setRSIMomentum(rsiMomentum);
		log.setBBSignal(bBandSignal);
		log.setStochasticMomentum(stochasticMomentum);
		log.setEMAMomentum(EMAMomentum);
		
		log.setRSIDivergence(rsiDivergence);
		log.setStochasticDivergence(stochasticDivergence);
		log.setMFIDivergence(mfiDivergence);
		log.setCCIDivergence(cciDivergence);
		
		log.setFractalEnergy(fractalEnergy);
		
		stock.setLog(log);
	}
}