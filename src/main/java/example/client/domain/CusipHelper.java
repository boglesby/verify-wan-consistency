package example.client.domain;

import java.util.Random;

public class CusipHelper {
    
  private static final Random RANDOM = new Random();

  private static final String[] CUSIPS = new String[] {
    "AAPL", "MSFT", "AMZN", "GOOGL", "FB", "JPM", "V", "JNJ", "WMT", "PG",
    "BAC", "XOM", "MA", "UNH", "VZ", "INTC", "HD", "KO", "MRK", "WFC", "PFE",
    "NVS", "TM", "CMCSA", "BA", "PEP", "CSCO", "ORCL", "C", "SAP", "HSBC",
    "ADBE", "MCD", "NKE", "NFLX", "COST", "BUD", "HON", "PYPL", "AVGO", "CRM",
    "UNP", "IBM", "LLY", "TXN", "LMT", "SBUX", "UPS", "QCOM", "CVS", "AXP",
    "MMM", "BMY", "GE"
  };
  
  public static String getCusip()  {
    return CUSIPS[RANDOM.nextInt(CUSIPS.length)];
  }
}
