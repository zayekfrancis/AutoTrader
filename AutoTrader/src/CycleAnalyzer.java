
public class CycleAnalyzer {

	public static boolean buySignal(Stock stock, String date, boolean fractal) {
		boolean bBandSignal = BollingerBandsAnalyzer.buySignal(stock, date, fractal);
		boolean stochasticSignal = StochasticsAnalyzer.buySignal(stock, date, fractal);
		
		return (bBandSignal || stochasticSignal);
	}
}
