import java.util.ArrayList;
import java.util.List;

public class ADXAnalyzer {

	public static double getRatio(Stock stock, String buyInDate, String date) {
		date = stock.getClosestDate(date);
		int startingIndexOptionOne = stock.getDates().indexOf(date) - 3;
		int startingIndexOptionTwo = stock.getDates().indexOf(buyInDate);
		int startingADXIndex = Math.max(startingIndexOptionOne, startingIndexOptionTwo);

		double startingADX = stock.getADX(stock.getDates().get(startingADXIndex));
		double currentADX = stock.getADX(date);

		if (currentADX < startingADX * .8) {
			return stock.getClose(date);
		}
		return 0;
	}

	public static boolean buySignal(Stock stock, int timePeriod, String date) {
		int endingIndex = stock.getDates().indexOf(date);
		int startingIndex = (int) (endingIndex - (timePeriod / 2));

		if (adxSignal(stock, startingIndex, endingIndex)) {
			if (positiveDICross(stock, date, timePeriod)) {
				double diDiff = stock.getpDI(date) - stock.getnDI(date);
				if (stock.getADX(date) > 20 && diDiff > 10 && stock.getpDI(date) > 20) {
					int startingDIIndex = stock.getDates().indexOf(date) - 3;
					String startingDIDiffDate = stock.getDates().get(startingDIIndex);
					double startingDIDiff = stock.getpDI(startingDIDiffDate) - stock.getnDI(startingDIDiffDate);
					double endingDIDiff = stock.getpDI(date) - stock.getnDI(date);

					if (endingDIDiff > startingDIDiff * .95) {
						return true;
					}
				}

			}
		}
		return false;
	}

	private static boolean adxSignal(Stock stock, int startingIndex, int endingIndex) {
		int adxCount = 0;
		int consecutiveADXReqForSignal = (endingIndex - startingIndex) / 7;
		for (int i = startingIndex; i <= endingIndex; i++) {
			String iDate = stock.getDates().get(i);
			double currentADX = stock.getADX(iDate);
			if (currentADX > 20) {
				adxCount++;
			} else {
				adxCount = 0;
			}
			if (adxCount > consecutiveADXReqForSignal) {
				return true;
			}
		}
		return false;
	}

	public static boolean positiveDICross(Stock stock, String date, int timePeriod) {
		int currentDateIndex = stock.getDates().indexOf(date);
		int startingDateIndex = currentDateIndex - (timePeriod / 2);
		int reqPointsBelownDI = (int) ((currentDateIndex - startingDateIndex) * .6);

		int countBelownDI = 0;
		for (int i = startingDateIndex; i <= currentDateIndex; i++) {
			String iDate = stock.getDates().get(i);
			if (stock.getpDI(iDate) < stock.getnDI(iDate) * 1.15) {
				countBelownDI++;
			}
		}

		return countBelownDI >= reqPointsBelownDI;
	}

	public static boolean calculateADX(Stock stockIn, int timePeriod) {
		// price lists
		ArrayList<Double> closePrices = new ArrayList<Double>();
		ArrayList<Double> highPrices = new ArrayList<Double>();
		ArrayList<Double> lowPrices = new ArrayList<Double>();

		// dx list
		ArrayList<Double> dx = new ArrayList<Double>();

		// tr, pdm, ndm lists of individual days
		ArrayList<Double> tr = new ArrayList<Double>();
		ArrayList<Double> pDM = new ArrayList<Double>();
		ArrayList<Double> nDM = new ArrayList<Double>();

		for (String date : stockIn.getDates()) {
			lowPrices.add(stockIn.getLow(date));
			highPrices.add(stockIn.getHigh(date));
			closePrices.add(stockIn.getClose(date));
		}

		int startingIndex = timePeriod - 1;
		int endingIndex = closePrices.size() - 1;

		double currentTR = 0;
		double currentpDM = 0;
		double currentnDM = 0;
		for (int i = 0; i < endingIndex; i++) {
			double previousLow = lowPrices.get(i);
			double previousHigh = highPrices.get(i);
			double previousClose = closePrices.get(i);

			double currentLow = lowPrices.get(i + 1);
			double currentHigh = highPrices.get(i + 1);

			double trueRange = calculateTR(currentHigh, currentLow, previousClose);
			double positiveDirectionalMovement = 0;
			double negativeDirectionalMovement = 0;
			if (currentHigh - previousHigh > previousLow - currentLow) {
				positiveDirectionalMovement = calculate_pDM(currentHigh, previousHigh);
				negativeDirectionalMovement = 0;
			} else if (previousLow - currentLow > currentHigh - previousHigh) {
				positiveDirectionalMovement = 0;
				negativeDirectionalMovement = calculate_nDM(currentLow, previousLow);
			}

			tr.add(trueRange);
			pDM.add(positiveDirectionalMovement);
			nDM.add(negativeDirectionalMovement);

			if (i == startingIndex) {
				currentTR = sum(tr, 0, timePeriod);
				currentpDM = sum(pDM, 0, timePeriod);
				currentnDM = sum(nDM, 0, timePeriod);
			} else if (i > startingIndex) {
				currentTR = currentTR - currentTR / timePeriod + tr.get(tr.size() - 1);
				currentpDM = currentpDM - currentpDM / timePeriod + pDM.get(pDM.size() - 1);
				currentnDM = currentnDM - currentnDM / timePeriod + nDM.get(nDM.size() - 1);
			}

			if (i >= startingIndex) {
				double pDI = 100 * currentpDM / currentTR;
				double nDI = 100 * currentnDM / currentTR;

				String date = stockIn.getDates().get(i + 1);
				stockIn.setpDI(date, pDI);
				stockIn.setnDI(date, nDI);

				double diffDI = Math.abs(pDI - nDI);
				double sumDI = pDI + nDI;
				double currentDX = 100 * diffDI / sumDI;
				dx.add(currentDX);

				if (i == startingIndex + timePeriod - 1) {
					double adx = findAverage(dx, 0, timePeriod);
					stockIn.setADX(date, adx);
				} else if (i > startingIndex + timePeriod - 1) {
					String previousDate = stockIn.getDates().get(i);
					double previousADX = stockIn.getADX(previousDate);

					double adx = ((previousADX * (timePeriod - 1)) + currentDX) / timePeriod;
					stockIn.setADX(date, adx);
				}
			}
		}
		return true;
	}

	private static double calcualteSmoothing(double currentGain, double currentLoss, double avgGain, double avgLoss,
			int timePeriod) {
		avgGain = (avgGain * (timePeriod - 1) + currentGain) / timePeriod;
		avgLoss = (avgLoss * (timePeriod - 1) + currentLoss) / timePeriod;
		double smoothedRS = avgGain / avgLoss;
		return 100 - (100 / (1 + smoothedRS));
	}

	private static double sum(List<Double> list, int startingIndex, int endingIndex) {
		double sum = 0;
		for (int i = startingIndex; i < endingIndex; i++) {
			double price = list.get(i);
			sum += price;
		}
		return sum;
	}

	private static double findAverage(ArrayList<Double> list, int startingIndex, int endingIndex) {
		double sum = 0;
		for (int i = startingIndex; i < endingIndex; i++) {
			double price = list.get(i);
			sum += price;
		}
		return sum / (endingIndex - startingIndex);
	}

	public static double calculate_pDM(double currentHigh, double previousHigh) {
		double highDiff = currentHigh - previousHigh;
		return Math.max(highDiff, 0);
	}

	public static double calculate_nDM(double currentLow, double previousLow) {
		double lowDiff = previousLow - currentLow;
		return Math.max(lowDiff, 0);
	}

	public static double calculateTR(double currentHigh, double currentLow, double previousClose) {
		double currentDayRange = currentHigh - currentLow;
		double highGapUp = Math.abs(currentHigh - previousClose);
		double lowGapUp = Math.abs(currentLow - previousClose);

		return Math.max(Math.max(currentDayRange, highGapUp), lowGapUp);
	}

}
