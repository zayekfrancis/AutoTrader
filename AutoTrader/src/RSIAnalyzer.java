import java.util.ArrayList;

public class RSIAnalyzer {

	private static Stock stock;
	private static double strength;
	private static double slope;

	public static boolean calculateRSI(Stock stockIn, int timePeriod) {
		ArrayList<Double> prices = new ArrayList<Double>();
		ArrayList<Double> posDelta = new ArrayList<Double>();
		ArrayList<Double> negDelta = new ArrayList<Double>();

		for (String date : stockIn.getDates()) {
			prices.add(stockIn.getClose(date));
		}

		int startingIndex = timePeriod;
		int endingIndex = prices.size() - 1;

		for (int i = 0; i < startingIndex; i++) {
			double priceA = prices.get(i);
			double priceB = prices.get(i + 1);
			double delta = priceB - priceA;
			if (delta > 0) {
				posDelta.add(Math.abs(delta));
				negDelta.add(0.0);
			} else if (delta < 0) {
				negDelta.add(Math.abs(delta));
				posDelta.add(0.0);
			} else {
				posDelta.add(0.0);
				negDelta.add(0.0);
			}
		}

		double avgGain = findAverage(posDelta, 0, startingIndex);
		double avgLoss = findAverage(negDelta, 0, startingIndex);
		for (int i = startingIndex; i < endingIndex; i++) {
			if ((i - startingIndex) == 0) {
				double baseRS = calculateFirstRSI(avgGain, avgLoss);
				stockIn.setRSI(stockIn.getDates().get(i), baseRS);
			}

			double priceA = prices.get(i);
			double priceB = prices.get(i + 1);
			double delta = priceB - priceA;
			String date = stockIn.getDates().get(i + 1);
			if (delta > 0) {
				double rsi = calculateContinuedRSI(Math.abs(delta), 0, avgGain, avgLoss, timePeriod);
				stockIn.setRSI(date, rsi);
				avgGain = (avgGain * (timePeriod - 1) + Math.abs(delta)) / timePeriod;
				avgLoss = (avgLoss * (timePeriod - 1) + 0.0) / timePeriod;
			} else if (delta < 0) {
				double rsi = calculateContinuedRSI(0, Math.abs(delta), avgGain, avgLoss, timePeriod);
				stockIn.setRSI(date, rsi);
				avgGain = (avgGain * (timePeriod - 1) + 0.0) / timePeriod;
				avgLoss = (avgLoss * (timePeriod - 1) + Math.abs(delta)) / timePeriod;
			} else {
				double rsi = calculateContinuedRSI(0, 0, avgGain, avgLoss, timePeriod);
				stockIn.setRSI(date, rsi);
				avgGain = (avgGain * (timePeriod - 1) + 0.0) / timePeriod;
				avgLoss = (avgLoss * (timePeriod - 1) + 0.0) / timePeriod;
			}
		}
		return true;
	}
	
	public static boolean positiveRSICross(Stock stock, String date, int timePeriod, double threshold) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .5);
		
		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getRSI(iDate) < threshold) {
				countBelownDI++;
			}
		}
		
		return countBelownDI >= reqPointsBelownDI;
	}

	public static double getStrength(Stock stockIn, double rsiBottomThreshold, int consecutivePoints, int range,
			String dateIn) {
		stock = stockIn;
		strength = 0;
		slope = 0;
		if (searchForLowRSI(rsiBottomThreshold, consecutivePoints, range, dateIn)) {
			return getStrength();
		}
		return 0;
	}

	private static double calculateFirstRSI(double avgGain, double avgLoss) {
		double rs = avgGain / avgLoss;
		return 100 - (100 / (1 + rs));
	}

	private static double calculateContinuedRSI(double currentGain, double currentLoss, double avgGain, double avgLoss,
			int timePeriod) {
		avgGain = (avgGain * (timePeriod - 1) + currentGain) / timePeriod;
		avgLoss = (avgLoss * (timePeriod - 1) + currentLoss) / timePeriod;
		double smoothedRS = avgGain / avgLoss;
		return 100 - (100 / (1 + smoothedRS));
	}

	private static double findAverage(ArrayList<Double> list, int startingIndex, int endingIndex) {
		double sum = 0;
		for (int i = startingIndex; i < endingIndex; i++) {
			double price = list.get(i);
			sum += price;
		}
		return sum / (endingIndex - startingIndex);
	}

	public static double getRatio(Stock stockIn, String dateIn) {
		double ratio = 0.97;
		double oversoldThreshold = 30;
		double overboughtThreshold = 70;
		double rsiValue = stockIn.getRSI(dateIn);
		// base (2 percent)
		if (rsiValue >= oversoldThreshold && rsiValue < overboughtThreshold) {
			ratio += (rsiValue - oversoldThreshold) * (0.05 / (overboughtThreshold - oversoldThreshold));
		} else if (rsiValue >= overboughtThreshold) {
			ratio = .995;
			ratio += (rsiValue - overboughtThreshold) * (0.005 / (100 - overboughtThreshold));
		}
		return ratio;
	}

	public static double getRatioV2(Stock stockIn, String dateIn, double ratioStart) {
		double ratio = ratioStart;
		double oversoldThreshold = 30;
		double overboughtThreshold = 70;
		double rsiValue = stockIn.getRSI(dateIn);

		if (rsiValue >= oversoldThreshold && rsiValue < overboughtThreshold) {
			ratio += (rsiValue - oversoldThreshold) * ((.997 - ratioStart) / (overboughtThreshold - oversoldThreshold));
		} else if (rsiValue >= overboughtThreshold) {
			ratio = .997;
			ratio += (rsiValue - overboughtThreshold) * ((1.0 - ratio) / (100 - overboughtThreshold));
		}
		return ratio;
	}

	private static boolean searchForLowRSI(double rsiBottomThreshold, int consecutivePoints, int range, String dateIn) {
		int currentDateIndex = stock.getDates().indexOf(dateIn);
		if (currentDateIndex == -1) {
			return false;
		}
		int lastDateIndex = currentDateIndex - consecutivePoints + 1;
		if (didNotExceedRange(rsiBottomThreshold, lastDateIndex, currentDateIndex, consecutivePoints, range)) {
			for (int i = lastDateIndex; i <= currentDateIndex; i++) {
				double rsiValue = stock.getRSI(stock.getDates().get(i));
				if (rsiValue < rsiBottomThreshold) {
					int bottomIndex = bouncedOffBottom(rsiBottomThreshold, lastDateIndex, currentDateIndex);
					if (bottomIndex != -1) {
						return searchForEntryBySlope(rsiBottomThreshold, lastDateIndex, currentDateIndex, bottomIndex);
					}
				}
			}
		}
		return false;
	}

	private static boolean didNotExceedRange(double rsiBottomThreshold, int lastDateIndex, int currentDateIndex,
			int consecutivePoints, int range) {
		double currentRSI = 0;
		for (int i = lastDateIndex; i <= currentDateIndex; i++) {
			try {
				currentRSI = stock.getRSI(stock.getDates().get(i));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("dates.size(): " + stock.getDates().size());
				System.out.println("index: " + i);
				System.out.println("rsiBottomThreshold: " + rsiBottomThreshold);
				System.out.println("consecutivePoints: " + consecutivePoints);
			}
			if ((currentRSI > rsiBottomThreshold + range) || (currentRSI < rsiBottomThreshold - range)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find the bottom and ensure that it bounces with strength
	 * 
	 * @param keys
	 * @param index
	 * @param rsiBottomThreshold
	 * @param consecutivePoints
	 * @param range
	 * @return
	 */
	private static int bouncedOffBottom(double rsiBottomThreshold, int lastDateIndex, int currentDateIndex) {
		int bottomIndex = lastDateIndex;
		double bottomRSI = stock.getRSI(stock.getDates().get(bottomIndex));

		for (int i = lastDateIndex; i <= currentDateIndex; i++) {
			double currentRSI = stock.getRSI(stock.getDates().get(i));
			if (currentRSI < bottomRSI) {
				bottomIndex = i;
				bottomRSI = currentRSI;
			}
		}
		return bottomIndex;
	}

	private static boolean searchForEntryBySlope(double rsiBottomThreshold, int lastDateIndex, int currentDateIndex,
			int bottomIndex) {
		double ySum = 0;
		for (int i = bottomIndex; i <= currentDateIndex; i++) {
			try {
				ySum += stock.getRSI(stock.getDates().get(i));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("dates.size(): " + stock.getDates().size());
				System.out.println("index: " + i);
				System.out.println("rsiBottomThreshold: " + rsiBottomThreshold);
			}

		}
		if (currentDateIndex - bottomIndex >= 4) {
			setSlope((ySum / (rsiBottomThreshold * (currentDateIndex - (bottomIndex)))));
		}
		return (getSlope() > 1.4);
	}

	private static void setStrength() {
		strength = getSlope() - 1.0;
	}

	private static double getStrength() {
		return strength;
	}

	private static void setSlope(double slopeIn) {
		slope = slopeIn;
		setStrength();
	}

	private static double getSlope() {
		return slope;
	}

}
