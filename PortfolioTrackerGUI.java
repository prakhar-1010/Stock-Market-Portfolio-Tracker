import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class PortfolioTrackerGUI extends JFrame {
    private Portfolio portfolio;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JLabel totalInvestmentLabel;
    private JLabel totalValueLabel;
    private JLabel totalProfitLabel;
    private final String SAVE_FILE = "portfolio.dat";
    
    public PortfolioTrackerGUI() {
        setTitle("Indian Stock Market Portfolio Tracker (NSE/BSE)");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Try to load existing portfolio
        try {
            portfolio = Portfolio.loadFromFile(SAVE_FILE);
            JOptionPane.showMessageDialog(this, "Portfolio loaded successfully!");
        } catch (Exception e) {
            portfolio = new Portfolio("My Portfolio");
        }
        
        initComponents();
        updateTable();
        updateStatistics();
    }
    
    private void initComponents() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel - Statistics
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Center panel - Table
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Bottom panel - Buttons
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Portfolio Statistics"));
        
        totalInvestmentLabel = new JLabel("Total Investment: ₹0.00", SwingConstants.CENTER);
        totalValueLabel = new JLabel("Current Value: ₹0.00", SwingConstants.CENTER);
        totalProfitLabel = new JLabel("Total Profit: ₹0.00 (0%)", SwingConstants.CENTER);
        
        totalInvestmentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalValueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalProfitLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        panel.add(totalInvestmentLabel);
        panel.add(totalValueLabel);
        panel.add(totalProfitLabel);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Stocks"));
        
        String[] columns = {"Symbol", "Name", "Quantity", "Buy Price", "Current Price", "Total Value", "Profit", "Profit %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton addButton = new JButton("Add Stock");
        JButton removeButton = new JButton("Remove Stock");
        JButton updateButton = new JButton("Update Price");
        JButton sortNameButton = new JButton("Sort by Name");
        JButton sortProfitButton = new JButton("Sort by Profit");
        JButton sortValueButton = new JButton("Sort by Value");
        JButton chartButton = new JButton("Show Pie Chart");
        JButton saveButton = new JButton("Save Portfolio");
        
        addButton.addActionListener(e -> addStock());
        removeButton.addActionListener(e -> removeStock());
        updateButton.addActionListener(e -> updatePrice());
        sortNameButton.addActionListener(e -> sortByName());
        sortProfitButton.addActionListener(e -> sortByProfit());
        sortValueButton.addActionListener(e -> sortByValue());
        chartButton.addActionListener(e -> showPieChart());
        saveButton.addActionListener(e -> savePortfolio());
        
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(updateButton);
        panel.add(sortNameButton);
        panel.add(sortProfitButton);
        panel.add(sortValueButton);
        panel.add(chartButton);
        panel.add(saveButton);
        
        return panel;
    }
    
    private void addStock() {
        // Step 1: Ask for stock symbol
        String symbol = JOptionPane.showInputDialog(this, 
            "Enter Stock Symbol (NSE/BSE):\n\nExamples:\n" +
            "• RELIANCE.NS - Reliance Industries (NSE)\n" +
            "• TCS.NS - Tata Consultancy Services\n" +
            "• INFY.NS - Infosys\n" +
            "• HDFCBANK.NS - HDFC Bank\n" +
            "• ITC.NS - ITC Limited\n\n" +
            "Note: Use .NS for NSE or .BO for BSE", 
            "Add Indian Stock", JOptionPane.QUESTION_MESSAGE);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            return; // User cancelled or entered nothing
        }
        
        symbol = symbol.trim().toUpperCase();
        
        // Step 2: Fetch current price from internet
        JOptionPane.showMessageDialog(this, "Fetching current price for " + symbol + "...", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
        
        double currentPrice = StockPriceAPI.fetchCurrentPrice(symbol);
        
        if (currentPrice <= 0) {
            JOptionPane.showMessageDialog(this, "Could not fetch price for " + symbol + ".\nPlease check the symbol and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Step 3: Fetch stock name
        String stockName = StockPriceAPI.fetchStockName(symbol);
        if (stockName == null || stockName.isEmpty()) {
            stockName = symbol; // Fallback to symbol if name not found
        }
        
        // Step 4: Ask for quantity
        String quantityStr = JOptionPane.showInputDialog(this, 
            "Stock: " + stockName + "\nCurrent Price: ₹" + String.format("%.2f", currentPrice) + "\n\nHow many shares?",
            "Quantity", JOptionPane.QUESTION_MESSAGE);
        
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return;
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr.trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be positive!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Step 5: Ask if bought previously or buying now
        String[] options = {"Bought Previously", "Buying Now"};
        int choice = JOptionPane.showOptionDialog(this,
            "Did you buy this stock previously or are you buying it now?",
            "Purchase Type",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]);
        
        if (choice == -1) {
            return; // User cancelled
        }
        
        double buyPrice;
        
        if (choice == 0) { // Bought Previously
            String buyPriceStr = JOptionPane.showInputDialog(this,
                "At what price did you buy each share?",
                "Buy Price", JOptionPane.QUESTION_MESSAGE);
            
            if (buyPriceStr == null || buyPriceStr.trim().isEmpty()) {
                return;
            }
            
            try {
                buyPrice = Double.parseDouble(buyPriceStr.trim());
                if (buyPrice <= 0) {
                    JOptionPane.showMessageDialog(this, "Price must be positive!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else { // Buying Now
            buyPrice = currentPrice;
        }
        
        // Create and add stock
        Stock stock = new Stock(stockName, symbol, quantity, buyPrice, currentPrice);
        portfolio.addStock(stock);
        updateTable();
        updateStatistics();
        
        String profitInfo = "";
        if (buyPrice < currentPrice) {
            profitInfo = String.format("\n\nProfit: ₹%.2f (%.2f%%)", stock.getProfit(), stock.getProfitPercentage());
        } else if (buyPrice > currentPrice) {
            profitInfo = String.format("\n\nLoss: ₹%.2f (%.2f%%)", stock.getProfit(), stock.getProfitPercentage());
        }
        
        JOptionPane.showMessageDialog(this, 
            "Stock added successfully!" +
            "\n\nStock: " + stockName + " (" + symbol + ")" +
            "\nQuantity: " + quantity +
            "\nBuy Price: ₹" + String.format("%.2f", buyPrice) +
            "\nCurrent Price: ₹" + String.format("%.2f", currentPrice) +
            profitInfo,
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void removeStock() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock to remove!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String symbol = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove " + symbol + "?", 
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            portfolio.removeStock(symbol);
            updateTable();
            updateStatistics();
            JOptionPane.showMessageDialog(this, "Stock removed successfully!");
        }
    }
    
    private void updatePrice() {
        int selectedRow = stockTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a stock to update!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String symbol = (String) tableModel.getValueAt(selectedRow, 0);
        Stock stock = portfolio.findStock(symbol);
        
        String newPriceStr = JOptionPane.showInputDialog(this, "Enter new price for " + symbol + ":", 
                stock.getCurrentPrice());
        
        if (newPriceStr != null) {
            try {
                double newPrice = Double.parseDouble(newPriceStr.trim());
                stock.setCurrentPrice(newPrice);
                updateTable();
                updateStatistics();
                JOptionPane.showMessageDialog(this, "Price updated successfully!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void sortByName() {
        portfolio.sortByName();
        updateTable();
    }
    
    private void sortByProfit() {
        portfolio.sortByProfit();
        updateTable();
    }
    
    private void sortByValue() {
        portfolio.sortByValue();
        updateTable();
    }
    
    private void showPieChart() {
        if (portfolio.getStockCount() == 0) {
            JOptionPane.showMessageDialog(this, "No stocks in portfolio!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Stock stock : portfolio.getStocks()) {
            dataset.setValue(stock.getSymbol() + " (" + stock.getName() + ")", stock.getTotalValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Portfolio Distribution by Value",
            dataset,
            true,  // legend
            true,  // tooltips
            false  // URLs
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        
        JFrame chartFrame = new JFrame("Portfolio Pie Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.add(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(this);
        chartFrame.setVisible(true);
    }
    
    private void savePortfolio() {
        try {
            portfolio.saveToFile(SAVE_FILE);
            JOptionPane.showMessageDialog(this, "Portfolio saved successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving portfolio: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateTable() {
        tableModel.setRowCount(0);
        for (Stock stock : portfolio.getStocks()) {
            Object[] row = {
                stock.getSymbol(),
                stock.getName(),
                stock.getQuantity(),
                String.format("₹%.2f", stock.getBuyPrice()),
                String.format("₹%.2f", stock.getCurrentPrice()),
                String.format("₹%.2f", stock.getTotalValue()),
                String.format("₹%.2f", stock.getProfit()),
                String.format("%.2f%%", stock.getProfitPercentage())
            };
            tableModel.addRow(row);
        }
    }
    
    private void updateStatistics() {
        totalInvestmentLabel.setText(String.format("Total Investment: ₹%.2f", portfolio.getTotalInvestment()));
        totalValueLabel.setText(String.format("Current Value: ₹%.2f", portfolio.getTotalValue()));
        
        double profit = portfolio.getTotalProfit();
        String profitText = String.format("Total Profit: ₹%.2f (%.2f%%)", 
                profit, portfolio.getTotalProfitPercentage());
        totalProfitLabel.setText(profitText);
        
        if (profit >= 0) {
            totalProfitLabel.setForeground(new Color(0, 150, 0));
        } else {
            totalProfitLabel.setForeground(Color.RED);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PortfolioTrackerGUI gui = new PortfolioTrackerGUI();
            gui.setVisible(true);
        });
    }
}

