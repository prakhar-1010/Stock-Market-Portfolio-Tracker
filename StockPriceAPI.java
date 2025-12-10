import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Real-time stock price integration using Yahoo Finance API
 * No API key required!
 */
public class StockPriceAPI {
    
    // Using Yahoo Finance query API (free, no API key needed)
    private static final String QUOTE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=1d";
    private static final String SEARCH_URL = "https://query1.finance.yahoo.com/v1/finance/search?q=%s&quotesCount=1";
    
    /**
     * Fetches the current stock price from Yahoo Finance API
     * 
     * @param symbol Stock symbol (e.g., "AAPL", "GOOGL")
     * @return Current stock price, or -1 if error occurs
     */
    public static double fetchCurrentPrice(String symbol) {
        try {
            String urlString = String.format(QUOTE_URL, symbol);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String jsonResponse = response.toString();
                double price = parseYahooPrice(jsonResponse);
                
                return price;
            }
        } catch (Exception e) {
            System.err.println("Error fetching price for " + symbol + ": " + e.getMessage());
        }
        
        return -1; // Return -1 if error occurs
    }
    
    /**
     * Parses Yahoo Finance JSON response to extract current stock price
     * 
     * @param jsonResponse JSON string from Yahoo Finance API
     * @return Extracted price
     */
    private static double parseYahooPrice(String jsonResponse) {
        try {
            // Look for "regularMarketPrice" in the JSON response
            String searchKey = "\"regularMarketPrice\":";
            int priceIndex = jsonResponse.indexOf(searchKey);
            
            if (priceIndex != -1) {
                int startIndex = priceIndex + searchKey.length();
                int endIndex = jsonResponse.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = jsonResponse.indexOf("}", startIndex);
                }
                
                if (endIndex != -1) {
                    String priceStr = jsonResponse.substring(startIndex, endIndex).trim();
                    return Double.parseDouble(priceStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing price: " + e.getMessage());
        }
        
        return -1;
    }
    
    /**
     * Fetches the stock name (company name) from Yahoo Finance
     * 
     * @param symbol Stock symbol (e.g., "AAPL")
     * @return Company name, or null if error occurs
     */
    public static String fetchStockName(String symbol) {
        try {
            String urlString = String.format(SEARCH_URL, URLEncoder.encode(symbol, StandardCharsets.UTF_8.toString()));
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                String jsonResponse = response.toString();
                return parseStockName(jsonResponse, symbol);
            }
        } catch (Exception e) {
            System.err.println("Error fetching name for " + symbol + ": " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Parses the company name from Yahoo Finance search response
     * 
     * @param jsonResponse JSON response
     * @param symbol Stock symbol
     * @return Company name
     */
    private static String parseStockName(String jsonResponse, String symbol) {
        try {
            // Look for "longname" or "shortname" in the response
            String[] searchKeys = {"\"longname\":\"", "\"shortname\":\""};
            
            for (String searchKey : searchKeys) {
                int nameIndex = jsonResponse.indexOf(searchKey);
                if (nameIndex != -1) {
                    int startIndex = nameIndex + searchKey.length();
                    int endIndex = jsonResponse.indexOf("\"", startIndex);
                    
                    if (endIndex != -1) {
                        return jsonResponse.substring(startIndex, endIndex);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing name: " + e.getMessage());
        }
        
        return symbol; // Fallback to symbol
    }
    
    /**
     * Updates all stocks in a portfolio with current prices
     * 
     * @param portfolio Portfolio to update
     */
    public static void updateAllPrices(Portfolio portfolio) {
        System.out.println("Fetching current prices...");
        int updated = 0;
        
        for (Stock stock : portfolio.getStocks()) {
            double currentPrice = fetchCurrentPrice(stock.getSymbol());
            if (currentPrice > 0) {
                stock.setCurrentPrice(currentPrice);
                updated++;
                System.out.println("Updated " + stock.getSymbol() + " to $" + currentPrice);
            }
        }
        
        System.out.println("Updated " + updated + " out of " + portfolio.getStockCount() + " stocks.");
    }
    
}

