
public class CandleStickAnalyzer {

	/*
	 * Find Bullish Railway Tracks
	 * 
	 * 1) Both sticks should be similar magnitudes (high - low) && abs(open -
	 * close) 
	 * 
	 * 2) Candle wicks need to be 25% or less of the candlestick 
	 * 
	 * 3) Highs, Lows, first Open/second Close or first Close/second Open must be
	 * in similar spots
	 */
	public static boolean bullishRailwayTrack(Stock stock, String date) {
		String previousCandleDate = stock.getDates().get(stock.getDates().indexOf(date) - 1);
		
		double open1 = stock.getOpen(previousCandleDate);
		double open2 = stock.getOpen(date);
		
		double close1 = stock.getClose(previousCandleDate);
		double close2 = stock.getClose(date);
		
		double high1 = stock.getHigh(previousCandleDate);
		double high2 = stock.getHigh(date);
		
		double low1 = stock.getLow(previousCandleDate);
		double low2 = stock.getLow(date);
		
		//Make sure first stick is bearish and second stick is bullish
		if (!(open1 > close1 && open2 < close2)) {
			return false;
		}
		
		//1) Both sticks should be similar magnitudes 
		//   (high - low) && abs(open - close)
		double highLowMagnitude1 = high1 - low1;
		double highLowMagnitude2 = high2 - low2;
		double openCloseMagnitude1 = open1 - close1;
		double openCloseMagnitude2 = close2 - open2;
		double magnitudeFlex = .5;
		
		if (highLowMagnitude2 < highLowMagnitude1 * (1 -magnitudeFlex)
			|| highLowMagnitude2 > highLowMagnitude1 * (1 + magnitudeFlex)) {
			return false;
		}
		
		if (openCloseMagnitude2 < openCloseMagnitude1 * (1 -magnitudeFlex)
				|| openCloseMagnitude2 > openCloseMagnitude1 * (1 + magnitudeFlex)) {
			return false;
		}
		
		//2) Candle wicks need to be 50% or less of the candlestick 
		double upperWick1 = high1 - open1;
		double upperWick2 = high2 - close2;
		
		double lowerWick1 = close1 - low1;
		double lowerWick2 = open2 - low1;
		
		double totalWickPercentage1 = (upperWick1 + lowerWick1) / highLowMagnitude1;
		double totalWickPercentage2 = (upperWick2 + lowerWick2) / highLowMagnitude2;
		
		if (totalWickPercentage1 > magnitudeFlex ||  totalWickPercentage2 > magnitudeFlex) {
			return false;
		}
		
		// 3) Highs or Lows && 
		//	  first Open/second Close or first Close/second Open 
		//    must be in similar spots
		
		double positionFlex = .05;
		double positionCount = 0;
		if (high2 > high1 * (1 - positionFlex) 
		 && high2 < high1 * (1 + positionFlex)
		 && high1 > close2) {
			positionCount++;
		}
		
		if (low2 > low1 * (1 - positionFlex) 
		 && low2 < low1 * (1 + positionFlex)
		 && low1 < open2) {
			positionCount++;
		}
		
		if (close2 > open1 * (1 - positionFlex) 
		 && close2 < open1 * (1 + positionFlex)
		 && open1 < high2) {
			positionCount++;
		}
		
		if (open2 > close1 * (1 - positionFlex) 
		 && open2 < close1 * (1 + positionFlex)
		 && close1 > low2) {
			positionCount++;
		}
		
		return positionCount > 2;
	}
}
