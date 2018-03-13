import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IEX {

	// Indicator Function Names
	public static final String SMA = "SMA";
	public static final String EMA = "EMA";
	public static final String MACD = "MACD";
	public static final String STOCH = "STOCH";
	public static final String RSI = "RSI";
	public static final String ADX = "ADX";
	public static final String CCI = "CCI";
	public static final String AROON = "AROON";
	public static final String AD = "AD";
	public static final String BBANDS = "BBANDS";

	// Function Values
	public static final String BASEURL = "https://api.iextrading.com/1.0/"; // "https://www.alphavantage.co/query?";

	/**
	 * Base Parameters Includes all quote function names and functions
	 * indicators.
	 **/
	// Base Functions
	public static final String STOCK_URL = "stock/";
	public static final String CHART_URL = "chart/";

	/**
	 * Indicator Function Options
	 * 
	 */
	// Intervals
	public static final String BY_DATE = "date/";
	public static final String ONE_MIN = "1min";
	public static final String FIVE_MIN = "5min";
	public static final String FIFTEEN_MIN = "15min";
	public static final String THIRTY_MIN = "30min";
	public static final String SIXTY_MIN = "60min";
	public static final String ONE_DAY = "1d";
	public static final String ONE_MONTH = "1m";
	public static final String THREE_MONTHS = "3m";
	public static final String SIX_MONTHS = "6m";
	public static final String YTD = "ytd";
	public static final String ONE_YEAR = "1y";
	public static final String TWO_YEARS = "2y";
	public static final String FIVE_YEARS = "5y";

	// Series Type
	public static final String AVG = "average"; //intraday only
	public static final String OPEN = "open";
	public static final String CLOSE = "close";
	public static final String HIGH = "high";
	public static final String LOW = "low";
	public static final String VOL = "volume";

	// Client
	private static OkHttpClient client = new OkHttpClient();
	// StockMap
	public Map<String, Map<String, Double>> stockMap;

	public IEX() {
		stockMap = new HashMap<String, Map<String, Double>>();
	}

	public boolean updatePriceData(String symbol, String intervalIn, String date, boolean local) {
		try {
			if (!isIntraDayInterval(intervalIn)) {
				return getHistory(symbol, intervalIn, local);
			} else {
				return getHistoryByDate(symbol, intervalIn, date);
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean getHistory(String symbol, String interval, boolean local) throws IOException, InterruptedException {
		if (local) {
			return false;// readLocalJson(symbol);
		} else {
			String url = BASEURL + STOCK_URL + symbol + "/" + CHART_URL + interval;
			return pullHistoryData(url, false);
		}
	}

	private boolean getHistoryByDate(String symbol, String interval, String date) {
		String url = BASEURL + STOCK_URL + symbol + "/" + CHART_URL + BY_DATE + formatDate(date);
		return pullHistoryData(url, true);
	}

	private String formatDate(String date) {
		String year = date.substring(1, 5);
		String month = date.substring(6, 8);
		String day = date.substring(9, date.length() - 1);

		return (year + month + day);
	}

	private boolean isIntraDayInterval(String interval) {
		switch (interval) {
		case ONE_MIN:
			return true;
		case FIVE_MIN:
			return true;
		case FIFTEEN_MIN:
			return true;
		case THIRTY_MIN:
			return true;
		case SIXTY_MIN:
			return true;
		}
		return false;
	}

	private boolean pullHistoryData(String urlIn, boolean intraDay) {
		JsonArray jArray = null;
		try {
			Response response = sendRequest(urlIn);
			String jsonData = response.body().string();
			JsonReader jReader = Json.createReader(new StringReader(jsonData));
			jArray = jReader.readArray();
			jReader.close();

			for (int i = 0; i < jArray.size(); i++) {
				if (!intraDay) {
					parseForPrices(jArray.getJsonObject(i));
				}
				else {
					parseForIntraDayPrices(jArray.getJsonObject(i));
				}
				
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	private Response sendRequest(String url) throws IOException {
		try {
			// Wait 1/100 a second in order to make calls. Specifically
			// instructed by API provider

			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Request request = new Request.Builder().url(url).build();
		Response response = null;

		try {
			response = client.newCall(request).execute();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response;
	}

	private void parseForPrices(JsonObject jso) {
		String date = jso.get("date").toString();

		double open = Double.parseDouble(jso.get(OPEN).toString());
		setStockMapValue(date, OPEN, open);

		double close = Double.parseDouble(jso.get(CLOSE).toString());
		setStockMapValue(date, CLOSE, close);

		double high = Double.parseDouble(jso.get(HIGH).toString());
		setStockMapValue(date, HIGH, high);

		double low = Double.parseDouble(jso.get(LOW).toString());
		setStockMapValue(date, LOW, low);

		double volume = Double.parseDouble(jso.get(VOL).toString());
		setStockMapValue(date, VOL, volume);
	}

	private void parseForIntraDayPrices(JsonObject jso) {
		String date = jso.get("date").toString();
		String minute = "";
		minute = jso.get("minute").toString();
		date = date + "-" + minute;

		if (Double.parseDouble(jso.get(AVG).toString()) == 0) {
			return;
		}
		double open = Double.parseDouble(jso.get(AVG).toString());
		setStockMapValue(date, OPEN, open);

		double close = Double.parseDouble(jso.get(AVG).toString());
		setStockMapValue(date, CLOSE, close);

		double high = Double.parseDouble(jso.get(HIGH).toString());
		setStockMapValue(date, HIGH, high);

		double low = Double.parseDouble(jso.get(LOW).toString());
		setStockMapValue(date, LOW, low);

		double volume = Double.parseDouble(jso.get(VOL).toString());
		setStockMapValue(date, VOL, volume);
	}

	private void setStockMapValue(String key, String function, Double value) {
		if (stockMap.containsKey(key)) {
			stockMap.get(key).put(function, value);
		} else {
			if (!function.equals(RSI)) {
				stockMap.put(key, new HashMap());
				stockMap.get(key).put(function, value);
			}
		}
	}

	private boolean loadLocalStockFile(String symbol, String interval, String date) {
		JsonObject jso = readJson(symbol, interval);
		String keyIn = "";
		if (jso != null) {
			// populateStockInfo(jso, jso.keySet(), keyIn, symbol, interval,
			// false);
			return true;
		}
		return false;
	}

	private boolean writeJson(JsonObject jso, String symbol, String interval) {
		String path = "/Users/franc/OneDrive/Documents/SP500JSONS/" + symbol + "/" + interval;

		File filePath = new File(path);
		if (!filePath.exists()) {
			boolean success = filePath.mkdirs();
			if (!success) {
				// Directory creation failed
				System.out.println("Folder creation failed...");
				return false;
			}
		}

		try (FileWriter file = new FileWriter(path + "/" + symbol + ".txt")) {
			file.write(jso.toString());
			System.out.println("Successfully Copied JSON Object to File...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private JsonObject readJson(String symbol, String interval) {
		String path = "/Users/franc/OneDrive/Documents/SP500JSONS/" + symbol + "/" + interval;

		File filePath = new File(path);
		if (!filePath.exists()) {
			System.out.println("Folder creation failed...");
			return null;
		}

		try {
			JsonObject jso = readJsonFile(path + "/" + symbol + ".txt");
			System.out.println("Successfully read JSON...");
			return jso;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private JsonObject readJsonFile(String filePath) throws IOException {
		File jsonInputFile = new File(filePath);
		InputStream is = new FileInputStream(jsonInputFile);
		JsonReader reader = Json.createReader(is);
		JsonObject jso = reader.readObject();
		reader.close();
		return jso;
	}

	public void writeSP500Locally() {
		String intervals[] = { TWO_YEARS };
		for (String interval : intervals) {
			for (String symbol : getSP500()) {
				try {
					getHistory(symbol, interval, false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void updateSP500Locally() {
		String intervals[] = { TWO_YEARS };
		for (String interval : intervals) {
			for (String symbol : getSP500()) {
				// Read old data into a map
				JsonObject jsoLocal = readJson(symbol, interval);
				String keyIn = "";
				// populateStockInfo(jsoLocal, jsoLocal.keySet(), keyIn, symbol,
				// interval, false);
				Map<String, Map<String, Double>> localStockMap = stockMap;
				stockMap = new HashMap<String, Map<String, Double>>();

				// Pull new data into a map
				try {
					getHistory(symbol, interval, false);
					Map<String, Map<String, Double>> updateStockMap = stockMap;
					stockMap = new HashMap<String, Map<String, Double>>();
					combineStockData(jsoLocal, keyIn, localStockMap, updateStockMap);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				writeJson(jsoLocal, symbol, interval);
			}
		}
	}

	private void combineStockData(JsonObject jso, String keyIn, Map<String, Map<String, Double>> localMap,
			Map<String, Map<String, Double>> updateMap) {
		ArrayList<String> localMapDates = new ArrayList<String>();
		ArrayList<String> updateMapDates = new ArrayList<String>();
		localMapDates.addAll(localMap.keySet());
		updateMapDates.addAll(updateMap.keySet());
		Collections.reverse(localMapDates);
		Collections.reverse(updateMapDates);

		int startingIndex = 0;
		for (String date : localMapDates) {
			if (updateMapDates.contains(date)) {
				startingIndex = updateMapDates.indexOf(date);
			} else {
				startingIndex++;
				break;
			}
		}

		JsonObject values = (JsonObject) jso.get(keyIn);
		for (int i = startingIndex; i < updateMapDates.size(); i++) {
			String dateKey = updateMapDates.get(i);
			localMap.put(dateKey, updateMap.get(dateKey));
			values.put(dateKey, (JsonValue) localMap.get(dateKey));
		}

		jso.remove(keyIn);
		jso.put(keyIn, values);
	}

	public static ArrayList<String> getSP500() {
		String[] spArray = { "AMD", "ABT", "ABBV", "ACN", "ADBE", "AAP", "AES", "AET", "AFL", "AMG", "APD", "AKAM",
				"AA", "AGN", "ALXN", "ALLE", "ADS", "ALL", "MO", "AMZN", "AEE", "AAL", "AEP", "AXP", "AIG", "AMT",
				"AMP", "ABC", "AME", "AMGN", "APH", "APC", "ADI", "AON", "APA", "AIV", "AMAT", "ADM", "AIZ", "T",
				"ADSK", "ADP", "AN", "AZO", "AVGO", "AVB", "AVY", "BAC", "BK", "BCR", "BAX", "BBT", "BDX", "BBBY",
				"BBY", "BLX", "HRB", "BA", "BWA", "BXP", "BMY", "CHRW", "CA", "COG", "CPB", "COF", "CAH", "HSIC", "KMX",
				"CCL", "CAT", "CBG", "CBS", "CELG", "CNP", "CTL", "CERN", "CF", "SCHW", "CHK", "CVX", "CMG", "CB", "CI",
				"XEC", "CINF", "CTAS", "CSCO", "C", "CTXS", "CLX", "CME", "CMS", "KO", "CCE", "CTSH", "CL", "CMCSA",
				"CMA", "CAG", "COP", "ED", "STZ", "GLW", "COST", "CCI", "CSX", "CMI", "CVS", "DHI", "DRI", "DVA",
				"DE", "DLPH", "DAL", "XRAY", "DVN", "DO", "DFS", "DISCA", "DISCK", "DG", "DLTR", "D", "DOV",
				"DPS", "DTE", "DUK", "DNB", "ETFC", "EMN", "ETN", "EBAY", "ECL", "EIX", "EW", "EA", "EMR", "ENDP",
				"ESV", "ETR", "EOG", "EQT", "EFX", "EQIX", "EQR", "ESS", "EL", "EXC", "EXPE", "EXPD", "ESRX",
				"XOM", "FFIV", "FB", "FAST", "FDX", "FIS", "FITB", "FSLR", "FE", "FLIR", "FLS", "FLR", "FMC", "FTI",
				"F", "FOSL", "BEN", "FCX", "FTR", "GME", "GPS", "GRMN", "GD", "GE", "GGP", "GIS", "GM", "GPC", "GNW",
				"GILD", "GS", "GT", "GOOGL", "GOOG", "GWW", "HAL", "HBI", "HOG", "HRS", "HIG", "HAS", "HCA", "HCP",
				"HCN", "HP", "HES", "HPQ", "HD", "HON", "HRL", "HST", "HUM", "HBAN", "ITW", "IR", "INTC", "ICE", "IBM",
				"IP", "IPG", "IFF", "INTU", "ISRG", "IVZ", "IRM", "JEC", "JBHT", "JNJ", "JCI", "JPM", "JNPR", "KSU",
				"K", "KEY", "KMB", "KIM", "LNC", "LMT", "L", "LOW", "LYB", "MTB", "MAC", "M", "MNK", "MRO", "MPC",
				"MAR", "MMC", "MLM", "MAS", "MA", "MAT", "MKC", "MCD", "MCK", "MDT", "MRK", "MET", "KORS",
				"MCHP", "MU", "MSFT", "MHK", "TAP", "MDLZ", "MON", "MNST", "MCO", "MS", "MOS", "MSI", "MUR", "MYL",
				"NDAQ", "NOV", "NAVI", "NTAP", "NFLX", "NWL", "NFX", "NEM", "NWSA", "NEE", "NLSN", "NKE", "NI", "NE",
				"NBL", "JWN", "NSC", "NTRS", "NOC", "NRG", "NUE", "NVDA", "ORLY", "OXY", "OMC", "OKE", "ORCL", "OI",
				"PCAR", "PH", "PDCO", "PAYX", "PNR", "PBCT", "PEP", "PKI", "PRGO", "PFE", "PCG", "PM", "PSX", "PNW",
				"PXD", "PBI", "PNC", "RL", "PPG", "PPL", "PX", "PCLN", "PFG", "PG", "PGR", "PLD", "PRU", "PEG", "PSA",
				"PHM", "PVH", "QRVO", "PWR", "QCOM", "DGX", "RRC", "RTN", "RHT", "RF", "RSG", "RHI", "ROK",
				"COL", "ROP", "ROST", "R", "CRM", "SCG", "SLB", "SNI", "STX", "SEE", "SRE", "SHW", "SPG", "SWKS", "SLG",
				"SJM", "SNA", "SO", "LUV", "SWN", "SWK", "SBUX", "STT", "SRCL", "SYK", "STI", "SYMC", "SYY", "TROW",
				"TGT", "TEL", "THC", "TDC", "TXN", "TXT", "HSY", "TRV", "TMO", "TIF", "TWX", "TMK", "TSS",
				"TSCO", "RIG", "TRIP", "FOXA", "TSN", "UNP", "UNH", "UPS", "URI", "UTX", "UHS", "UNM", "URBN",
				"VFC", "VLO", "VAR", "VTR", "VRSN", "VZ", "VIAB", "V", "VNO", "VMC", "WMT", "WBA", "DIS", "WM",
				"WAT", "ANTM", "WFC", "WDC", "WU", "WY", "WHR", "WMB", "WEC", "WYN", "WYNN", "XEL", "XRX", "XLNX", "XL",
				"XYL", "YUM", "ZBH", "ZION", "ZTS" };

		ArrayList<String> sp = new ArrayList<String>();
		for (String ticker : spArray) {
			sp.add(ticker);
		}
		return sp;
	}
}
