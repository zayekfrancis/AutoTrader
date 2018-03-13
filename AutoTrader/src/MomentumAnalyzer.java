
public class MomentumAnalyzer {

	public static boolean buySignal(StockBroker broker, Stock stock, String date, boolean fractal) {
		boolean macDMomentum;
		boolean rsiMomentum;
		boolean rsiDivergence;
		boolean bBandSignal;
		boolean stochasticSignal;
		boolean mfiDivergence;

		macDMomentum = MACDAnalyzer.MACDMomentumExists(stock, date, fractal);
		rsiMomentum = RSIAnalyzer.rsiMomentumExists(stock, date, 60, 70);
		rsiDivergence = RSIAnalyzer.findPositiveDivergence(stock, date) 
						&& stock.getRSI(date) > 50 
						&& stock.getRSI(date) < 80;
		bBandSignal = BollingerBandsAnalyzer.buySignal(stock, date, fractal);
		stochasticSignal = StochasticsAnalyzer.buySignal(stock, date, fractal);
		mfiDivergence = MoneyFlowIndex.buySignal(stock, date, 80, fractal);

		boolean main = (rsiMomentum || rsiDivergence || mfiDivergence || (bBandSignal || mfiDivergence));
		if (!fractal && ((main && (macDMomentum || rsiDivergence || bBandSignal || stochasticSignal || mfiDivergence))
				|| (rsiDivergence
						&& (macDMomentum || rsiMomentum || bBandSignal || stochasticSignal || mfiDivergence)))) {
			//Util.setLogs(stock, macDMomentum, rsiMomentum, rsiDivergence, bBandSignal, stochasticSignal, mfiDivergence);
		}

		return (main && (macDMomentum || rsiDivergence || bBandSignal || stochasticSignal || mfiDivergence))
				|| (rsiDivergence && (macDMomentum || rsiMomentum || bBandSignal || stochasticSignal || mfiDivergence));
	}

	public static boolean sellSignal(Stock stock, String date, boolean fractal) {
		boolean macDMomentum = MACDAnalyzer.MACDDyingMomentum(stock, date, fractal);
		boolean rsiMomentum = RSIAnalyzer.rsiDyingMomentum(stock, date, 50);
		boolean rsiDivergence = RSIAnalyzer.findNegativeDivergence(stock, date);
		return (macDMomentum || rsiMomentum || rsiDivergence);
	}


	
}
