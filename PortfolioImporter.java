import java.io.*;
import java.util.ArrayList;

public class PortfolioImporter {
    
    /**
     * Imports portfolio from Zerodha CSV format
     * Expected columns: Instrument, Qty., Avg. cost, LTP, P&L
     */
    public static ArrayList<Stock> importFromZerodha(String filePath) throws IOException {
        ArrayList<Stock> stocks = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        
        String line;
        boolean isFirstLine = true;
        
        while ((line = reader.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Skip header
            }
            
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                try {
                    String symbol = parts[0].trim();
                    
                    // Convert Zerodha symbol to Yahoo Finance format
                    if (!symbol.contains(".NS") && !symbol.contains(".BO")) {
                        symbol = symbol + ".NS"; // Default to NSE
                    }
                    
                    int quantity = Integer.parseInt(parts[1].trim());
                    double avgCost = Double.parseDouble(parts[2].trim());
                    double currentPrice = Double.parseDouble(parts[3].trim());
                    
                    Stock stock = new Stock(symbol, symbol, quantity, avgCost, currentPrice);
                    stocks.add(stock);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        }
        
        reader.close();
        return stocks;
    }
    
    /**
     * Imports portfolio from Groww CSV format
     * Expected columns: Stock Name, Quantity, Avg Buy Price, Current Price
     */
    public static ArrayList<Stock> importFromGroww(String filePath) throws IOException {
        ArrayList<Stock> stocks = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        
        String line;
        boolean isFirstLine = true;
        
        while ((line = reader.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Skip header
            }
            
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                try {
                    String name = parts[0].trim();
                    String symbol = extractSymbol(name);
                    
                    // Convert to Yahoo Finance format
                    if (!symbol.contains(".NS") && !symbol.contains(".BO")) {
                        symbol = symbol + ".NS";
                    }
                    
                    int quantity = Integer.parseInt(parts[1].trim());
                    double avgCost = Double.parseDouble(parts[2].trim().replace("₹", "").replace(",", ""));
                    double currentPrice = Double.parseDouble(parts[3].trim().replace("₹", "").replace(",", ""));
                    
                    Stock stock = new Stock(name, symbol, quantity, avgCost, currentPrice);
                    stocks.add(stock);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        }
        
        reader.close();
        return stocks;
    }
    
    /**
     * Generic CSV importer
     * User can specify column mapping
     */
    public static ArrayList<Stock> importFromGenericCSV(String filePath, 
            int symbolCol, int nameCol, int qtyCol, int buyPriceCol, int currentPriceCol) throws IOException {
        
        ArrayList<Stock> stocks = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        
        String line;
        boolean isFirstLine = true;
        
        while ((line = reader.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;
                continue; // Skip header
            }
            
            String[] parts = line.split(",");
            
            try {
                String symbol = parts[symbolCol].trim();
                String name = nameCol >= 0 ? parts[nameCol].trim() : symbol;
                int quantity = Integer.parseInt(parts[qtyCol].trim());
                double buyPrice = Double.parseDouble(parts[buyPriceCol].trim().replace("₹", "").replace(",", ""));
                double currentPrice = Double.parseDouble(parts[currentPriceCol].trim().replace("₹", "").replace(",", ""));
                
                // Ensure Yahoo Finance format
                if (!symbol.contains(".NS") && !symbol.contains(".BO")) {
                    symbol = symbol + ".NS";
                }
                
                Stock stock = new Stock(name, symbol, quantity, buyPrice, currentPrice);
                stocks.add(stock);
            } catch (Exception e) {
                System.err.println("Error parsing line: " + line + " - " + e.getMessage());
            }
        }
        
        reader.close();
        return stocks;
    }
    
    /**
     * Helper method to extract stock symbol from name
     */
    private static String extractSymbol(String name) {
        // Common patterns: "Reliance Industries Ltd.", "TCS", etc.
        name = name.toUpperCase().trim();
        
        // Remove common suffixes
        name = name.replace(" LTD", "")
                   .replace(" LIMITED", "")
                   .replace(" LTD.", "")
                   .replace(".", "")
                   .replace(" ", "");
        
        // Map common names to symbols
        if (name.contains("RELIANCE")) return "RELIANCE";
        if (name.contains("TCS")) return "TCS";
        if (name.contains("INFOSYS") || name.contains("INFY")) return "INFY";
        if (name.contains("HDFC") && name.contains("BANK")) return "HDFCBANK";
        if (name.contains("ITC")) return "ITC";
        if (name.contains("TATA") && name.contains("MOTOR")) return "TATAMOTORS";
        if (name.contains("WIPRO")) return "WIPRO";
        if (name.contains("BHARTI")) return "BHARTIARTL";
        if (name.contains("MARUTI")) return "MARUTI";
        if (name.contains("SBIN") || name.contains("STATE BANK")) return "SBIN";
        
        return name; // Return as is if no mapping found
    }
    
    /**
     * Creates a sample CSV template for user reference
     */
    public static void createSampleTemplate(String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        
        writer.write("Symbol,Name,Quantity,Buy Price,Current Price\n");
        writer.write("RELIANCE.NS,Reliance Industries,10,2450.50,2680.75\n");
        writer.write("TCS.NS,Tata Consultancy Services,5,3200.00,3450.25\n");
        writer.write("INFY.NS,Infosys Limited,15,1450.00,1520.80\n");
        
        writer.close();
    }
}

