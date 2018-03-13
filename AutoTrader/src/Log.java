
public class Log implements Comparable {

	private String symbol;
	private double buyPrice;
	private String buyDate;
	private String sellDate;
	private double sellPrice;
	private double percentageChange;

	private boolean TrendingBuy;
	private boolean nonTrendingBuy;
	
	private boolean MACD;
	private boolean RSIMomentum;
	private boolean MFIMomentum;
	private boolean CCIMomentum;
	private boolean BBSignal;
	private boolean StochasticMomentum;
	private boolean EMAMomentum;
	
	private boolean StochasticDivergence;
	private boolean RSIDivergence;
	private boolean MFIDivergence;
	private boolean CCIDivergence;
	
	private boolean fractalEnergy;
	
	private boolean railWayTrack;
	
	public Log(String symbol, double buyPrice, String buyDate) {
		setTrendingBuy(false);
		setNonTrendingBuy(false);
		
		setMACD(false);
		setRSIMomentum(false);
		
		setBBSignal(false);
		setStochasticMomentum(false);
		
		setStochasticDivergence(false);
		setRSIDivergence(false);
		setMFIDivergence(false);
		setCCIDivergence(false);
		
		setFractalEnergy(false);
		
		setRailWayTrack(false);
		
		this.symbol = symbol;
		this.buyPrice = buyPrice;
		this.buyDate = buyDate;
		
		this.sellPrice = 0;
		this.sellDate = "";
	}
	
	
	public boolean isSold() {
		if (sellPrice == 0 && sellDate == "") {
			return false;
		}
		return true;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getBuyDate() {
		return buyDate;
	}

	public void setBuyDate(String buyDate) {
		this.buyDate = buyDate;
	}

	public String getSellDate() {
		return sellDate;
	}

	public void setSellDate(String sellDate) {
		this.sellDate = sellDate;
	}

	public double getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(double buyPrice) {
		this.buyPrice = buyPrice;
	}

	public double getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(double sellPrice) {
		this.sellPrice = sellPrice;
	}

	public boolean isMACD() {
		return MACD;
	}

	public void setMACD(boolean mACD) {
		MACD = mACD;
	}

	public boolean isRSIMomentum() {
		return RSIMomentum;
	}

	public void setRSIMomentum(boolean rSIMomentum) {
		RSIMomentum = rSIMomentum;
	}

	public boolean isRSIDivergence() {
		return RSIDivergence;
	}

	public void setRSIDivergence(boolean rSIDivergence) {
		RSIDivergence = rSIDivergence;
	}

	public boolean isBBSignal() {
		return BBSignal;
	}

	public void setBBSignal(boolean bBSignal) {
		BBSignal = bBSignal;
	}
	
	public boolean isMFIDivergence() {
		return MFIDivergence;
	}

	public void setMFIDivergence(boolean mFIDivergence) {
		MFIDivergence = mFIDivergence;
	}

	public double getPercentageChange() {
		return percentageChange;
	}

	public void setPercentageChange(double percentageChange) {
		this.percentageChange = percentageChange;
	}
	
	public boolean isNonTrendingBuy() {
		return nonTrendingBuy;
	}

	public void setNonTrendingBuy(boolean nonTrendingBuy) {
		this.nonTrendingBuy = nonTrendingBuy;
	}
	
	@Override
	public int compareTo(Object o)
	{
		Log log = (Log) o;
		if (this.getPercentageChange() > log.getPercentageChange()) {
			return 1;
		}
		else if (this.getPercentageChange() < log.getPercentageChange()) {
			return -1;
		}
	    
		return 0;
	}


	public boolean isTrendingBuy() {
		return TrendingBuy;
	}


	public void setTrendingBuy(boolean trendingBuy) {
		TrendingBuy = trendingBuy;
	}


	public boolean isStochasticMomentum() {
		return StochasticMomentum;
	}


	public void setStochasticMomentum(boolean stochasticMomentum) {
		StochasticMomentum = stochasticMomentum;
	}


	public boolean isFractalEnergy() {
		return fractalEnergy;
	}


	public void setFractalEnergy(boolean fractalEnergy) {
		this.fractalEnergy = fractalEnergy;
	}


	public boolean isStochasticDivergence() {
		return StochasticDivergence;
	}


	public void setStochasticDivergence(boolean stochasticDivergence) {
		StochasticDivergence = stochasticDivergence;
	}


	public boolean isCCIDivergence() {
		return CCIDivergence;
	}


	public void setCCIDivergence(boolean cCIDivergence) {
		CCIDivergence = cCIDivergence;
	}


	public boolean isEMAMomentum() {
		return EMAMomentum;
	}


	public void setEMAMomentum(boolean eMAMomentum) {
		EMAMomentum = eMAMomentum;
	}


	public boolean isRailWayTrack() {
		return railWayTrack;
	}


	public void setRailWayTrack(boolean railWayTrack) {
		this.railWayTrack = railWayTrack;
	}


	public boolean isMFIMomentum() {
		return MFIMomentum;
	}


	public void setMFIMomentum(boolean mFIMomentum) {
		MFIMomentum = mFIMomentum;
	}


	public boolean isCCIMomentum() {
		return CCIMomentum;
	}


	public void setCCIMomentum(boolean cCIMomentum) {
		CCIMomentum = cCIMomentum;
	}
}
