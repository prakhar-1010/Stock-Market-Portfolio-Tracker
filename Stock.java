import java.io.Serializable;

public class Stock implements Serializable, Comparable<Stock> {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String symbol;
    private int quantity;
    private double buyPrice;
    private double currentPrice;
    
    public Stock(String name, String symbol, int quantity, double buyPrice, double currentPrice) {
        this.name = name;
        this.symbol = symbol;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.currentPrice = currentPrice;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getBuyPrice() {
        return buyPrice;
    }
    
    public double getCurrentPrice() {
        return currentPrice;
    }
    
    // Setters
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    // Calculated values
    public double getTotalValue() {
        return quantity * currentPrice;
    }
    
    public double getTotalInvestment() {
        return quantity * buyPrice;
    }
    
    public double getProfit() {
        return getTotalValue() - getTotalInvestment();
    }
    
    public double getProfitPercentage() {
        if (getTotalInvestment() == 0) return 0;
        return (getProfit() / getTotalInvestment()) * 100;
    }
    
    @Override
    public int compareTo(Stock other) {
        return this.symbol.compareTo(other.symbol);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - Qty: %d | Buy: ₹%.2f | Current: ₹%.2f | Profit: ₹%.2f (%.2f%%)",
                name, symbol, quantity, buyPrice, currentPrice, getProfit(), getProfitPercentage());
    }
}

