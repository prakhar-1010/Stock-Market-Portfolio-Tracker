import java.io.*;
import java.util.*;

public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ArrayList<Stock> stocks;
    private String portfolioName;
    
    public Portfolio(String portfolioName) {
        this.portfolioName = portfolioName;
        this.stocks = new ArrayList<>();
    }
    
    // Add stock
    public void addStock(Stock stock) {
        stocks.add(stock);
    }
    
    // Remove stock
    public boolean removeStock(String symbol) {
        return stocks.removeIf(stock -> stock.getSymbol().equalsIgnoreCase(symbol));
    }
    
    // Get all stocks
    public ArrayList<Stock> getStocks() {
        return new ArrayList<>(stocks);
    }
    
    // Find stock by symbol
    public Stock findStock(String symbol) {
        for (Stock stock : stocks) {
            if (stock.getSymbol().equalsIgnoreCase(symbol)) {
                return stock;
            }
        }
        return null;
    }
    
    // Sort by name
    public void sortByName() {
        stocks.sort(Comparator.comparing(Stock::getName));
    }
    
    // Sort by profit
    public void sortByProfit() {
        stocks.sort(Comparator.comparingDouble(Stock::getProfit).reversed());
    }
    
    // Sort by value
    public void sortByValue() {
        stocks.sort(Comparator.comparingDouble(Stock::getTotalValue).reversed());
    }
    
    // Portfolio statistics
    public double getTotalInvestment() {
        return stocks.stream().mapToDouble(Stock::getTotalInvestment).sum();
    }
    
    public double getTotalValue() {
        return stocks.stream().mapToDouble(Stock::getTotalValue).sum();
    }
    
    public double getTotalProfit() {
        return getTotalValue() - getTotalInvestment();
    }
    
    public double getTotalProfitPercentage() {
        if (getTotalInvestment() == 0) return 0;
        return (getTotalProfit() / getTotalInvestment()) * 100;
    }
    
    // File operations
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }
    
    public static Portfolio loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (Portfolio) ois.readObject();
        }
    }
    
    public String getPortfolioName() {
        return portfolioName;
    }
    
    public int getStockCount() {
        return stocks.size();
    }
}

