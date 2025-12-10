import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class GamifiedPortfolioGUI extends JFrame {
    private GamifiedPortfolio portfolio;
    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JLabel levelLabel;
    private JProgressBar expBar;
    private JProgressBar healthBar;
    private JLabel statsLabel;
    private JLabel dailyProfitLabel;
    private JLabel achievementCountLabel;
    private JPanel achievementPanel;
    private JLabel statusLabel;
    private JCheckBox autoRefreshCheckBox;
    private Timer autoRefreshTimer;
    private JCheckBox autoSaveCheckBox;
    private Timer autoSaveTimer;
    private final String SAVE_FILE = "portfolio.dat";
    
    // AMOLED black theme palette
    private final Color PRIMARY_COLOR = new Color(14, 165, 233); // Cyan accent
    private final Color SUCCESS_COLOR = new Color(34, 197, 94);  // Green
    private final Color DANGER_COLOR = new Color(239, 68, 68);   // Red
    private final Color WARNING_COLOR = new Color(251, 191, 36); // Amber
    private final Color BG_COLOR = new Color(0, 0, 0);           // True black background
    private final Color CARD_BG = new Color(18, 18, 18);         // Dark card background
    private final Color TEXT_COLOR = new Color(240, 240, 240);   // Light text
    
    public GamifiedPortfolioGUI() {
        setTitle("Indian Stock Market Portfolio - Gamified Edition");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Try to load existing portfolio
        try {
            Portfolio loaded = Portfolio.loadFromFile(SAVE_FILE);
            if (loaded instanceof GamifiedPortfolio) {
                portfolio = (GamifiedPortfolio) loaded;
            } else {
                portfolio = new GamifiedPortfolio("My Portfolio");
            }
        } catch (Exception e) {
            portfolio = new GamifiedPortfolio("My Portfolio");
        }
        
        // Set modern look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initComponents();
        updateAll();
        
        // Start animation timer
        Timer animationTimer = new Timer(50, e -> updateAnimations());
        animationTimer.start();
        
        // Auto-save on window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                autoSave();
                if (autoSaveTimer != null) {
                    autoSaveTimer.stop();
                    autoSaveTimer = null;
                }
                if (autoRefreshTimer != null) {
                    autoRefreshTimer.stop();
                    autoRefreshTimer = null;
                }
            }
        });
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Top panel - Player stats
        JPanel topPanel = createPlayerStatsPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center - Split between table and side panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(750);
        splitPane.setBackground(BG_COLOR);
        
        // Left - Stock table
        JPanel tablePanel = createModernTablePanel();
        splitPane.setLeftComponent(tablePanel);
        
        // Right - Achievements & Stats
        JPanel sidePanel = createSidePanel();
        JScrollPane sideScroll = new JScrollPane(sidePanel);
        sideScroll.setBorder(null);
        sideScroll.setBackground(BG_COLOR);
        sideScroll.getViewport().setBackground(BG_COLOR);
        sideScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        splitPane.setRightComponent(sideScroll);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Bottom - Action buttons and status bar
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BG_COLOR);
        
        JPanel buttonPanel = createModernButtonPanel();
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        JPanel statusBar = createStatusBar();
        bottomPanel.add(statusBar, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createPlayerStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Level and title
        JPanel levelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        levelPanel.setBackground(CARD_BG);
        
        levelLabel = new JLabel("Level 1 - Novice Trader");
        levelLabel.setFont(new Font("Arial", Font.BOLD, 24));
        levelLabel.setForeground(PRIMARY_COLOR);
        levelPanel.add(levelLabel);
        
        panel.add(levelPanel);
        panel.add(Box.createVerticalStrut(10));
        
        // Experience bar
        JPanel expPanel = new JPanel(new BorderLayout(5, 5));
        expPanel.setBackground(CARD_BG);
        
        JLabel expLabel = new JLabel("Experience:");
        expLabel.setForeground(TEXT_COLOR);
        expLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        expBar = new JProgressBar(0, 100);
        expBar.setStringPainted(true);
        expBar.setForeground(PRIMARY_COLOR);
        expBar.setBackground(BG_COLOR);
        expBar.setPreferredSize(new Dimension(300, 25));
        
        expPanel.add(expLabel, BorderLayout.WEST);
        expPanel.add(expBar, BorderLayout.CENTER);
        panel.add(expPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Health score bar
        JPanel healthPanel = new JPanel(new BorderLayout(5, 5));
        healthPanel.setBackground(CARD_BG);
        
        JLabel healthLabel = new JLabel("Portfolio Health:");
        healthLabel.setForeground(TEXT_COLOR);
        healthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        healthBar = new JProgressBar(0, 100);
        healthBar.setStringPainted(true);
        healthBar.setForeground(SUCCESS_COLOR);
        healthBar.setBackground(BG_COLOR);
        healthBar.setPreferredSize(new Dimension(300, 25));
        
        healthPanel.add(healthLabel, BorderLayout.WEST);
        healthPanel.add(healthBar, BorderLayout.CENTER);
        panel.add(healthPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 15, 0));
        statsRow.setBackground(CARD_BG);
        
        statsLabel = new JLabel("Total Trades: 0", SwingConstants.CENTER);
        statsLabel.setForeground(TEXT_COLOR);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        dailyProfitLabel = new JLabel("Daily P/L: ₹0.00", SwingConstants.CENTER);
        dailyProfitLabel.setForeground(TEXT_COLOR);
        dailyProfitLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        achievementCountLabel = new JLabel("Achievements: 0/6", SwingConstants.CENTER);
        achievementCountLabel.setForeground(TEXT_COLOR);
        achievementCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        statsRow.add(statsLabel);
        statsRow.add(dailyProfitLabel);
        statsRow.add(achievementCountLabel);
        
        panel.add(statsRow);
        
        return panel;
    }
    
    private JPanel createModernTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel("Your Stocks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"Symbol", "Name", "Qty", "Buy Price", "Current", "Value", "Profit", "P/L %"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        stockTable = new JTable(tableModel);
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setRowHeight(30);
        stockTable.setFont(new Font("Arial", Font.PLAIN, 12));
        stockTable.setBackground(CARD_BG);
        stockTable.setForeground(TEXT_COLOR);
        stockTable.setGridColor(BG_COLOR);
        stockTable.getTableHeader().setBackground(PRIMARY_COLOR);
        stockTable.getTableHeader().setForeground(Color.BLACK);
        stockTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Force consistent header rendering for readability (handles pressed/hover states)
        javax.swing.table.JTableHeader header = stockTable.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, false, false, row, column);
                Color bg = PRIMARY_COLOR;
                lbl.setBackground(bg);
                int lum = (int) (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue());
                lbl.setForeground(lum > 180 ? Color.BLACK : Color.WHITE);
                lbl.setFont(new Font("Arial", Font.BOLD, 12));
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });
        
        stockTable.setFillsViewportHeight(true);
        
        // Custom cell renderer for colorful profit/loss
        stockTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(CARD_BG);
                    // Specific column colors
                    if (column == 2 || column == 3) { // Qty and Buy Price columns
                        // Use readable color based on background brightness
                        double lum = 0.299 * CARD_BG.getRed() + 0.587 * CARD_BG.getGreen() + 0.114 * CARD_BG.getBlue();
                        if (lum < 140) {
                            c.setForeground(TEXT_COLOR);
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } else if (column == 6 || column == 7) { // Profit columns
                        String val = value.toString();
                        if (val.contains("-")) {
                            c.setForeground(DANGER_COLOR);
                        } else {
                            c.setForeground(SUCCESS_COLOR);
                        }
                    } else {
                        c.setForeground(TEXT_COLOR);
                    }
                }
                
                // If the background is very light (e.g., white filler/column), ensure text is black when there is content
                Color bg = c.getBackground();
                int lumBg = (int)(0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue());
                if (lumBg > 200 && value != null && !value.toString().trim().isEmpty()) {
                    c.setForeground(Color.BLACK);
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.getViewport().setBackground(CARD_BG);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);
        
        // Achievements panel
        achievementPanel = new JPanel();
        achievementPanel.setLayout(new BoxLayout(achievementPanel, BoxLayout.Y_AXIS));
        achievementPanel.setBackground(CARD_BG);
        achievementPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARNING_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel achTitle = new JLabel("Achievements");
        achTitle.setFont(new Font("Arial", Font.BOLD, 18));
        achTitle.setForeground(WARNING_COLOR);
        achTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        achievementPanel.add(achTitle);
        achievementPanel.add(Box.createVerticalStrut(15));
        
        panel.add(achievementPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // Quick stats panel
        JPanel quickStatsPanel = createQuickStatsPanel();
        panel.add(quickStatsPanel);
        
        return panel;
    }
    
    private JPanel createQuickStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel title = new JLabel("Quick Stats");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(SUCCESS_COLOR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        
        updateQuickStatsContent(panel);
        
        return panel;
    }
    
    private void updateQuickStatsContent(JPanel panel) {
        // Remove all except title and spacer
        if (panel.getComponentCount() > 2) {
            for (int i = panel.getComponentCount() - 1; i >= 2; i--) {
                panel.remove(i);
            }
        }
        
        if (portfolio.getStockCount() == 0) {
            JLabel noDataLabel = new JLabel("No stocks yet - add some to see stats!");
            noDataLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            noDataLabel.setForeground(Color.GRAY);
            noDataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(noDataLabel);
            panel.revalidate();
            panel.repaint();
            return;
        }
        
        // Total investment, value, P/L
        double totalInvestment = portfolio.getTotalInvestment();
        double totalValue = portfolio.getTotalValue();
        double totalProfit = portfolio.getTotalProfit();
        double totalProfitPct = portfolio.getTotalProfitPercentage();
        
        addStatLabel(panel, "Investment:", String.format("₹%.2f", totalInvestment), TEXT_COLOR);
        addStatLabel(panel, "Current Value:", String.format("₹%.2f", totalValue), TEXT_COLOR);
        addStatLabel(panel, "Overall P/L:", 
            String.format("₹%.2f (%.2f%%)", totalProfit, totalProfitPct), 
            totalProfit >= 0 ? SUCCESS_COLOR : DANGER_COLOR);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Top gainer/loser
        Stock topGainer = null;
        Stock topLoser = null;
        double maxGain = Double.NEGATIVE_INFINITY;
        double maxLoss = Double.POSITIVE_INFINITY;
        
        for (Stock stock : portfolio.getStocks()) {
            double pct = stock.getProfitPercentage();
            if (pct > maxGain) {
                maxGain = pct;
                topGainer = stock;
            }
            if (pct < maxLoss) {
                maxLoss = pct;
                topLoser = stock;
            }
        }
        
        if (topGainer != null) {
            addStatLabel(panel, "Top Gainer:", 
                String.format("%s (+%.2f%%)", topGainer.getSymbol(), topGainer.getProfitPercentage()), 
                SUCCESS_COLOR);
        }
        
        if (topLoser != null && topLoser != topGainer) {
            addStatLabel(panel, "Top Loser:", 
                String.format("%s (%.2f%%)", topLoser.getSymbol(), topLoser.getProfitPercentage()), 
                DANGER_COLOR);
        }
        
        panel.add(Box.createVerticalStrut(10));
        
        // Win rate and averages
        int winners = 0;
        double totalBuyPrice = 0;
        double totalCurrentPrice = 0;
        
        for (Stock stock : portfolio.getStocks()) {
            if (stock.getProfit() > 0) winners++;
            totalBuyPrice += stock.getBuyPrice();
            totalCurrentPrice += stock.getCurrentPrice();
        }
        
        int stockCount = portfolio.getStockCount();
        double winRate = (double) winners / stockCount * 100;
        double avgBuyPrice = totalBuyPrice / stockCount;
        double avgCurrentPrice = totalCurrentPrice / stockCount;
        
        addStatLabel(panel, "Win Rate:", 
            String.format("%.1f%% (%d/%d)", winRate, winners, stockCount), 
            winRate >= 50 ? SUCCESS_COLOR : WARNING_COLOR);
        
        addStatLabel(panel, "Avg Buy Price:", String.format("₹%.2f", avgBuyPrice), TEXT_COLOR);
        addStatLabel(panel, "Avg Current:", String.format("₹%.2f", avgCurrentPrice), TEXT_COLOR);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Diversification
        Set<String> sectors = new HashSet<>();
        for (Stock stock : portfolio.getStocks()) {
            // Simple sector classification based on symbol patterns
            String symbol = stock.getSymbol();
            if (symbol.contains("BANK") || symbol.contains("HDFC") || symbol.contains("ICICI")) {
                sectors.add("Banking");
            } else if (symbol.contains("IT") || symbol.contains("TCS") || symbol.contains("INFY") || 
                      symbol.contains("WIPRO") || symbol.contains("HCL")) {
                sectors.add("IT");
            } else if (symbol.contains("RELIANCE") || symbol.contains("ONGC") || symbol.contains("IOC")) {
                sectors.add("Energy");
            } else if (symbol.contains("PHARMA") || symbol.contains("SUN") || symbol.contains("CIPLA")) {
                sectors.add("Pharma");
            } else {
                sectors.add("Other");
            }
        }
        
        addStatLabel(panel, "Stocks:", String.valueOf(stockCount), TEXT_COLOR);
        addStatLabel(panel, "Sectors:", String.valueOf(sectors.size()), TEXT_COLOR);
        
        // Risk/Health index (simple calculation)
        double volatilityScore = 0;
        for (Stock stock : portfolio.getStocks()) {
            double pctChange = Math.abs(stock.getProfitPercentage());
            volatilityScore += pctChange;
        }
        volatilityScore /= stockCount;
        
        int riskScore = Math.max(0, Math.min(100, (int)(100 - volatilityScore)));
        Color riskColor = riskScore >= 70 ? SUCCESS_COLOR : 
                         riskScore >= 40 ? WARNING_COLOR : DANGER_COLOR;
        
        addStatLabel(panel, "Risk Score:", riskScore + "/100", riskColor);
        
        panel.revalidate();
        panel.repaint();
    }
    
    private void addStatLabel(JPanel panel, String label, String value, Color valueColor) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        row.setBackground(CARD_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel keyLabel = new JLabel(label + " ");
        keyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        keyLabel.setForeground(TEXT_COLOR);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
        valueLabel.setForeground(valueColor);
        
        row.add(keyLabel);
        row.add(valueLabel);
        panel.add(row);
    }
    
    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, PRIMARY_COLOR),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // Left side - status message
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Right side - auto-refresh toggle
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(CARD_BG);
        
        autoRefreshCheckBox = new JCheckBox("Auto-Refresh (15 min)");
        autoRefreshCheckBox.setForeground(TEXT_COLOR);
        autoRefreshCheckBox.setBackground(CARD_BG);
        autoRefreshCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
        autoRefreshCheckBox.addActionListener(e -> toggleAutoRefresh());

        autoSaveCheckBox = new JCheckBox("Auto-Save (5 min)");
        autoSaveCheckBox.setForeground(TEXT_COLOR);
        autoSaveCheckBox.setBackground(CARD_BG);
        autoSaveCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
        autoSaveCheckBox.addActionListener(e -> toggleAutoSave());
        autoSaveCheckBox.setSelected(true);

        rightPanel.add(autoSaveCheckBox);
        rightPanel.add(autoRefreshCheckBox);

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        // Start auto-save by default
        toggleAutoSave();

        return panel;
    }
    
    private JPanel createModernButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(BG_COLOR);
        
        JButton addBtn = createModernButton("Add Stock", PRIMARY_COLOR);
        JButton importBtn = createModernButton("Import", new Color(139, 92, 246));
        JButton exportBtn = createModernButton("Export", new Color(139, 92, 246));
        JButton removeBtn = createModernButton("Remove", DANGER_COLOR);
        JButton updateBtn = createModernButton("Refresh", WARNING_COLOR);
        JButton barChartBtn = createModernButton("Bar Chart", SUCCESS_COLOR);
        JButton pieChartBtn = createModernButton("Pie Chart", SUCCESS_COLOR);
        
        // Add tooltips
        addBtn.setToolTipText("Add a new stock to your portfolio");
        importBtn.setToolTipText("Import portfolio from Zerodha/Groww CSV");
        exportBtn.setToolTipText("Export portfolio to CSV file");
        removeBtn.setToolTipText("Remove selected stock from portfolio");
        updateBtn.setToolTipText("Refresh prices from internet");
        barChartBtn.setToolTipText("View profit/loss bar chart");
        pieChartBtn.setToolTipText("View portfolio distribution pie chart");
        
        addBtn.addActionListener(e -> addStock());
        importBtn.addActionListener(e -> importFromCSV());
        exportBtn.addActionListener(e -> exportToCSV());
        removeBtn.addActionListener(e -> removeStock());
        updateBtn.addActionListener(e -> updatePrice());
        barChartBtn.addActionListener(e -> showBarChart());
        pieChartBtn.addActionListener(e -> showPieChart());
        
        panel.add(addBtn);
        panel.add(importBtn);
        panel.add(exportBtn);
        panel.add(removeBtn);
        panel.add(updateBtn);
        panel.add(barChartBtn);
        panel.add(pieChartBtn);
        
        return panel;
    }
    
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void importFromCSV() {
        String[] options = {"Zerodha Format", "Groww Format", "Generic CSV", "Download Template"};
        int choice = JOptionPane.showOptionDialog(this,
            "Select import format:\n\n" +
            "• Zerodha: Export holdings from Zerodha Console\n" +
            "• Groww: Export portfolio from Groww app\n" +
            "• Generic: Custom CSV format\n" +
            "• Template: Download sample CSV template",
            "Import Portfolio",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == -1) return; // User cancelled
        
        if (choice == 3) {
            // Download template
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Sample Template");
            fileChooser.setSelectedFile(new File("portfolio_template.csv"));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    PortfolioImporter.createSampleTemplate(fileChooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(this, 
                        "Sample template saved!\n\nFill in your stock details and import using 'Generic CSV' option.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error creating template: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }
        
        // Select CSV file to import
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV file to import");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        
        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        
        try {
            ArrayList<Stock> importedStocks = null;
            
            if (choice == 0) {
                // Zerodha format
                importedStocks = PortfolioImporter.importFromZerodha(filePath);
            } else if (choice == 1) {
                // Groww format
                importedStocks = PortfolioImporter.importFromGroww(filePath);
            } else if (choice == 2) {
                // Generic CSV - ask for column mapping
                String mapping = JOptionPane.showInputDialog(this,
                    "Enter column numbers (0-indexed) separated by commas:\n" +
                    "Format: Symbol,Name,Quantity,BuyPrice,CurrentPrice\n\n" +
                    "Example: 0,1,2,3,4\n" +
                    "(Use -1 for Name if not available)",
                    "0,1,2,3,4");
                
                if (mapping == null || mapping.trim().isEmpty()) return;
                
                String[] cols = mapping.split(",");
                if (cols.length != 5) {
                    JOptionPane.showMessageDialog(this, "Invalid column mapping!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int symbolCol = Integer.parseInt(cols[0].trim());
                int nameCol = Integer.parseInt(cols[1].trim());
                int qtyCol = Integer.parseInt(cols[2].trim());
                int buyCol = Integer.parseInt(cols[3].trim());
                int currentCol = Integer.parseInt(cols[4].trim());
                
                importedStocks = PortfolioImporter.importFromGenericCSV(filePath, symbolCol, nameCol, qtyCol, buyCol, currentCol);
            }
            
            if (importedStocks == null || importedStocks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No stocks found in CSV file!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Add imported stocks to portfolio
            int oldLevel = portfolio.getLevel();
            for (Stock stock : importedStocks) {
                portfolio.addStock(stock);
            }
            
            autoSave();
            updateAll();
            
            // Check if leveled up
            if (portfolio.getLevel() > oldLevel) {
                showLevelUpAnimation(oldLevel, portfolio.getLevel());
            }
            
            // Show achievement notification
            showAchievementNotification();
            
            JOptionPane.showMessageDialog(this,
                String.format("Successfully imported %d stocks!\n\n+%d XP Earned!\n\nDon't forget to save your portfolio!",
                    importedStocks.size(), importedStocks.size() * 10),
                "Import Successful", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error importing CSV:\n" + e.getMessage() + "\n\nMake sure the CSV format matches the selected option.",
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addStock() {
        // Use same logic as before
        String symbol = JOptionPane.showInputDialog(this, 
            "Enter Stock Symbol (NSE/BSE):\n\nExamples:\n" +
            "• RELIANCE.NS - Reliance Industries (NSE)\n" +
            "• TCS.NS - Tata Consultancy Services\n" +
            "• INFY.NS - Infosys\n" +
            "• HDFCBANK.NS - HDFC Bank\n" +
            "• ITC.NS - ITC Limited\n\n" +
            "Note: Use .NS for NSE or .BO for BSE", 
            "Add Indian Stock", JOptionPane.QUESTION_MESSAGE);
        
        if (symbol == null || symbol.trim().isEmpty()) return;
        
        symbol = symbol.trim().toUpperCase();
        
        JOptionPane.showMessageDialog(this, "Fetching current price for " + symbol + "...", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
        
        double currentPrice = StockPriceAPI.fetchCurrentPrice(symbol);
        
        if (currentPrice <= 0) {
            JOptionPane.showMessageDialog(this, "Could not fetch price for " + symbol + ".\nPlease check the symbol and try again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String stockName = StockPriceAPI.fetchStockName(symbol);
        if (stockName == null || stockName.isEmpty()) {
            stockName = symbol;
        }
        
        String quantityStr = JOptionPane.showInputDialog(this, 
            "Stock: " + stockName + "\nCurrent Price: ₹" + String.format("%.2f", currentPrice) + "\n\nHow many shares?",
            "Quantity", JOptionPane.QUESTION_MESSAGE);
        
        if (quantityStr == null || quantityStr.trim().isEmpty()) return;
        
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
        
        String[] options = {"Bought Previously", "Buying Now"};
        int choice = JOptionPane.showOptionDialog(this,
            "Did you buy this stock previously or are you buying it now?",
            "Purchase Type",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]);
        
        if (choice == -1) return;
        
        double buyPrice;
        
        if (choice == 0) {
            String buyPriceStr = JOptionPane.showInputDialog(this,
                "At what price did you buy each share?",
                "Buy Price", JOptionPane.QUESTION_MESSAGE);
            
            if (buyPriceStr == null || buyPriceStr.trim().isEmpty()) return;
            
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
        } else {
            buyPrice = currentPrice;
        }
        
        Stock stock = new Stock(stockName, symbol, quantity, buyPrice, currentPrice);
        int oldLevel = portfolio.getLevel();
        portfolio.addStock(stock);
        autoSave();
        updateAll();
        
        // Check if leveled up
        if (portfolio.getLevel() > oldLevel) {
            showLevelUpAnimation(oldLevel, portfolio.getLevel());
        }
        
        // Check for new achievements
        showAchievementNotification();
        
        String profitInfo = "";
        if (buyPrice < currentPrice) {
            profitInfo = String.format("\n\nProfit: ₹%.2f (%.2f%%)", stock.getProfit(), stock.getProfitPercentage());
        } else if (buyPrice > currentPrice) {
            profitInfo = String.format("\n\nLoss: ₹%.2f (%.2f%%)", stock.getProfit(), stock.getProfitPercentage());
        }
        
        JOptionPane.showMessageDialog(this, 
            "Stock added successfully!\n\n+10 XP Earned!" +
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
            autoSave();
            updateAll();
            JOptionPane.showMessageDialog(this, "Stock removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updatePrice() {
        if (portfolio.getStockCount() == 0) {
            JOptionPane.showMessageDialog(this, "No stocks in portfolio to refresh!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Refresh prices for all %d stocks?\n\nThis may take a few seconds...", portfolio.getStockCount()),
            "Confirm Refresh",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        refreshAllPrices();
    }
    
    private void showPieChart() {
        if (portfolio.getStockCount() == 0) {
            JOptionPane.showMessageDialog(this, "No stocks in portfolio!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Stock stock : portfolio.getStocks()) {
            dataset.setValue(stock.getSymbol(), stock.getTotalValue());
        }
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Portfolio Distribution by Value",
            dataset,
            true, true, false
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
            JOptionPane.showMessageDialog(this, "Portfolio saved successfully!\n\n+5 XP Earned!", "Success", JOptionPane.INFORMATION_MESSAGE);
            portfolio.addExperience(5);
            updateAll();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving portfolio: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showLevelUpAnimation(int oldLevel, int newLevel) {
        JDialog dialog = new JDialog(this, "Level Up!", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PRIMARY_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("LEVEL UP!");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel levelInfo = new JLabel("Level " + oldLevel + " → Level " + newLevel);
        levelInfo.setFont(new Font("Arial", Font.BOLD, 20));
        levelInfo.setForeground(Color.YELLOW);
        levelInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(portfolio.getLevelTitle());
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(levelInfo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        
        dialog.add(panel);
        
        Timer timer = new Timer(2000, e -> dialog.dispose());
        timer.setRepeats(false);
        timer.start();
        
        dialog.setVisible(true);
    }
    
    private void showAchievementNotification() {
        // Check for newly unlocked achievements
        ArrayList<String> achievements = portfolio.getAchievements();
        if (!achievements.isEmpty()) {
            updateAchievementPanel();
        }
    }
    
    private void updateAchievementPanel() {
        // Clear existing achievements
        achievementPanel.removeAll();
        
        JLabel title = new JLabel("Achievements");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(WARNING_COLOR);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        achievementPanel.add(title);
        achievementPanel.add(Box.createVerticalStrut(15));
        
        ArrayList<String> achievements = portfolio.getAchievements();
        String[] allAchievements = {"First Stock", "Portfolio Builder", "Diversified Investor", 
                                    "Profit Maker", "Big Winner", "Millionaire"};
        
        for (String ach : allAchievements) {
            JLabel achLabel = new JLabel(achievements.contains(ach) ? "[x] " + ach : "[ ] " + ach);
            achLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            achLabel.setForeground(achievements.contains(ach) ? SUCCESS_COLOR : Color.GRAY);
            achLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            achievementPanel.add(achLabel);
            achievementPanel.add(Box.createVerticalStrut(8));
        }
        
        achievementPanel.revalidate();
        achievementPanel.repaint();
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
    
    private void updateAll() {
        portfolio.checkAchievements();
        portfolio.updateDailyProfitLoss();
        
        // Update level info
        levelLabel.setText(String.format("Level %d - %s", portfolio.getLevel(), portfolio.getLevelTitle()));
        
        // Update exp bar
        int exp = portfolio.getExperience();
        int expNeeded = portfolio.getExpForNextLevel();
        expBar.setMaximum(expNeeded);
        expBar.setValue(exp);
        expBar.setString(String.format("%d / %d XP", exp, expNeeded));
        
        // Update health bar
        int health = portfolio.getPortfolioHealthScore();
        healthBar.setValue(health);
        healthBar.setString(health + "% Healthy");
        
        // Update stats
        statsLabel.setText("Total Trades: " + portfolio.getStats().get("totalTrades"));
        
        double dailyProfit = portfolio.getDailyProfitLoss();
        dailyProfitLabel.setText(String.format("Daily P/L: ₹%.2f", dailyProfit));
        dailyProfitLabel.setForeground(dailyProfit >= 0 ? SUCCESS_COLOR : DANGER_COLOR);
        
        // Achievements count
        int unlocked = portfolio.getAchievements() != null ? portfolio.getAchievements().size() : 0;
        achievementCountLabel.setText(String.format("Achievements: %d/6", unlocked));
        achievementCountLabel.setForeground(unlocked > 0 ? SUCCESS_COLOR : TEXT_COLOR);
        
        // Update table
        updateTable();
        
        // Update achievements
        updateAchievementPanel();
        
        // Update Quick Stats content (handles JScrollPane wrapper)
        JSplitPane sp = (JSplitPane) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        Component rightComp = sp.getRightComponent();
        Container container = null;
        if (rightComp instanceof JScrollPane) {
            container = (Container) ((JScrollPane) rightComp).getViewport().getView();
        } else if (rightComp instanceof Container) {
            container = (Container) rightComp;
        }
        if (container != null) {
            for (Component comp : container.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel) {
                        JLabel lbl = (JLabel) panel.getComponent(0);
                        if ("Quick Stats".equals(lbl.getText())) {
                            updateQuickStatsContent(panel);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void updateAnimations() {
        // Simple pulse animation for profit label
        long time = System.currentTimeMillis();
        int alpha = (int) (128 + 127 * Math.sin(time / 500.0));
        // This would create smooth animations
    }
    
    private void autoSave() {
        try {
            portfolio.saveToFile(SAVE_FILE);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
            statusLabel.setText("Auto-saved at " + sdf.format(new java.util.Date()));
            statusLabel.setForeground(SUCCESS_COLOR);
            
            // Reset color after 3 seconds
            Timer resetTimer = new Timer(3000, e -> {
                statusLabel.setText("Ready");
                statusLabel.setForeground(TEXT_COLOR);
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
        } catch (Exception e) {
            statusLabel.setText("Save failed: " + e.getMessage());
            statusLabel.setForeground(DANGER_COLOR);
        }
    }
    
    private void exportToCSV() {
        if (portfolio.getStockCount() == 0) {
            JOptionPane.showMessageDialog(this, "No stocks to export!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Portfolio to CSV");
        fileChooser.setSelectedFile(new File("my_portfolio_export.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".csv")) {
                    filePath += ".csv";
                }
                
                java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(filePath));
                writer.write("Symbol,Name,Quantity,Buy Price,Current Price,Total Value,Profit,Profit %\n");
                
                for (Stock stock : portfolio.getStocks()) {
                    writer.write(String.format("%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                        stock.getSymbol(),
                        stock.getName(),
                        stock.getQuantity(),
                        stock.getBuyPrice(),
                        stock.getCurrentPrice(),
                        stock.getTotalValue(),
                        stock.getProfit(),
                        stock.getProfitPercentage()));
                }
                
                writer.close();
                
                portfolio.addExperience(15);
                updateAll();
                
                JOptionPane.showMessageDialog(this,
                    "Portfolio exported successfully!\n\n+15 XP Earned!\n\nFile: " + filePath,
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showBarChart() {
        if (portfolio.getStockCount() == 0) {
            JOptionPane.showMessageDialog(this, "No stocks in portfolio!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        
        for (Stock stock : portfolio.getStocks()) {
            dataset.addValue(stock.getProfit(), "Profit/Loss", stock.getSymbol());
        }
        
        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            "Profit/Loss per Stock",
            "Stock Symbol",
            "Profit/Loss (₹)",
            dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            false,
            true,
            false
        );
        
        // Customize colors
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        org.jfree.chart.renderer.category.BarRenderer renderer = (org.jfree.chart.renderer.category.BarRenderer) plot.getRenderer();
        
        for (int i = 0; i < portfolio.getStockCount(); i++) {
            Stock stock = portfolio.getStocks().get(i);
            if (stock.getProfit() >= 0) {
                renderer.setSeriesPaint(0, SUCCESS_COLOR);
            } else {
                renderer.setSeriesPaint(0, DANGER_COLOR);
            }
        }
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 500));
        
        JFrame chartFrame = new JFrame("Profit/Loss Bar Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.add(chartPanel);
        chartFrame.pack();
        chartFrame.setLocationRelativeTo(this);
        chartFrame.setVisible(true);
    }
    
    private void toggleAutoRefresh() {
        if (autoRefreshCheckBox.isSelected()) {
            // Start auto-refresh (15 minutes = 900000 ms)
            autoRefreshTimer = new Timer(900000, e -> refreshAllPrices());
            autoRefreshTimer.start();
            statusLabel.setText("Auto-refresh enabled");
            statusLabel.setForeground(SUCCESS_COLOR);
        } else {
            // Stop auto-refresh
            if (autoRefreshTimer != null) {
                autoRefreshTimer.stop();
                autoRefreshTimer = null;
            }
            statusLabel.setText("Auto-refresh disabled");
            statusLabel.setForeground(WARNING_COLOR);
        }
        
        Timer resetTimer = new Timer(3000, e -> {
            statusLabel.setText("Ready");
            statusLabel.setForeground(TEXT_COLOR);
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }

    private void toggleAutoSave() {
        if (autoSaveCheckBox.isSelected()) {
            // Start auto-save (5 minutes = 300000 ms)
            if (autoSaveTimer != null) {
                autoSaveTimer.stop();
            }
            autoSaveTimer = new Timer(300000, e -> autoSave());
            autoSaveTimer.start();
            statusLabel.setText("Auto-save enabled");
            statusLabel.setForeground(SUCCESS_COLOR);
        } else {
            // Stop auto-save
            if (autoSaveTimer != null) {
                autoSaveTimer.stop();
                autoSaveTimer = null;
            }
            statusLabel.setText("Auto-save disabled");
            statusLabel.setForeground(WARNING_COLOR);
        }

        Timer resetTimer = new Timer(3000, e -> {
            statusLabel.setText("Ready");
            statusLabel.setForeground(TEXT_COLOR);
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }
    
    private void refreshAllPrices() {
        if (portfolio.getStockCount() == 0) return;
        
        statusLabel.setText("Refreshing prices...");
        statusLabel.setForeground(WARNING_COLOR);
        
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                int updated = 0;
                for (Stock stock : portfolio.getStocks()) {
                    double newPrice = StockPriceAPI.fetchCurrentPrice(stock.getSymbol());
                    if (newPrice > 0) {
                        stock.setCurrentPrice(newPrice);
                        updated++;
                    }
                }
                return null;
            }
            
            @Override
            protected void done() {
                autoSave();
                updateAll();
                portfolio.addExperience(5);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
                statusLabel.setText("Prices refreshed at " + sdf.format(new java.util.Date()));
                statusLabel.setForeground(SUCCESS_COLOR);
                
                Timer resetTimer = new Timer(5000, e -> {
                    statusLabel.setText("Ready");
                    statusLabel.setForeground(TEXT_COLOR);
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            }
        };
        
        worker.execute();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GamifiedPortfolioGUI gui = new GamifiedPortfolioGUI();
            gui.setVisible(true);
        });
    }
}

