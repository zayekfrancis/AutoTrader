import java.util.ArrayList;

public class RelativeStrengthIndex {

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

}
