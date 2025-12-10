import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GamifiedPortfolio extends Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int level;
    private int experience;
    private double dailyProfitLoss;
    private ArrayList<String> achievements;
    private Map<String, Integer> stats;
    
    public GamifiedPortfolio(String portfolioName) {
        super(portfolioName);
        this.level = 1;
        this.experience = 0;
        this.dailyProfitLoss = 0;
        this.achievements = new ArrayList<>();
        this.stats = new HashMap<>();
        initStats();
    }
    
    private void initStats() {
        stats.put("totalTrades", 0);
        stats.put("winningTrades", 0);
        stats.put("losingTrades", 0);
        stats.put("daysActive", 0);
    }
    
    // Override addStock to add gamification
    @Override
    public void addStock(Stock stock) {
        super.addStock(stock);
        
        // Add experience for adding stock
        addExperience(10);
        
        // Update stats
        stats.put("totalTrades", stats.get("totalTrades") + 1);
        
        // Check for achievements
        checkAchievements();
    }
    
    // Level system
    public void addExperience(int exp) {
        experience += exp;
        
        // Level up logic
        int expNeeded = getExpForNextLevel();
        while (experience >= expNeeded) {
            level++;
            experience -= expNeeded;
            expNeeded = getExpForNextLevel();
        }
    }
    
    public int getExpForNextLevel() {
        return 100 * level; // 100, 200, 300, etc.
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getExperience() {
        return experience;
    }
    
    public String getLevelTitle() {
        if (level < 3) return "Novice Trader";
        if (level < 5) return "Learning Investor";
        if (level < 8) return "Smart Trader";
        if (level < 12) return "Expert Investor";
        if (level < 20) return "Portfolio Master";
        return "Warren Buffett Jr.";
    }
    
    // Daily profit/loss
    public void updateDailyProfitLoss() {
        dailyProfitLoss = getTotalProfit();
    }
    
    public double getDailyProfitLoss() {
        return dailyProfitLoss;
    }
    
    // Achievements
    public void checkAchievements() {
        // First Stock Achievement
        if (getStockCount() == 1 && !achievements.contains("First Stock")) {
            achievements.add("First Stock");
        }
        
        // Portfolio Builder
        if (getStockCount() >= 5 && !achievements.contains("Portfolio Builder")) {
            achievements.add("Portfolio Builder");
            addExperience(50);
        }
        
        // Diversified Investor
        if (getStockCount() >= 10 && !achievements.contains("Diversified Investor")) {
            achievements.add("Diversified Investor");
            addExperience(100);
        }
        
        // Profit Maker
        if (getTotalProfit() > 0 && !achievements.contains("Profit Maker")) {
            achievements.add("Profit Maker");
            addExperience(30);
        }
        
        // Big Winner
        if (getTotalProfit() >= 10000 && !achievements.contains("Big Winner")) {
            achievements.add("Big Winner");
            addExperience(200);
        }
        
        // Millionaire Portfolio
        if (getTotalValue() >= 1000000 && !achievements.contains("Millionaire")) {
            achievements.add("Millionaire");
            addExperience(500);
        }
    }
    
    public ArrayList<String> getAchievements() {
        return new ArrayList<>(achievements);
    }
    
    public int getAchievementCount() {
        return achievements.size();
    }
    
    // Stats
    public void updateStats(boolean isWinning) {
        if (isWinning) {
            stats.put("winningTrades", stats.get("winningTrades") + 1);
        } else {
            stats.put("losingTrades", stats.get("losingTrades") + 1);
        }
    }
    
    public int getWinRate() {
        int total = stats.get("totalTrades");
        if (total == 0) return 0;
        return (stats.get("winningTrades") * 100) / total;
    }
    
    public Map<String, Integer> getStats() {
        return new HashMap<>(stats);
    }
    
    // Portfolio health score (0-100)
    public int getPortfolioHealthScore() {
        int score = 50; // Base score
        
        // Diversification bonus
        if (getStockCount() >= 5) score += 15;
        if (getStockCount() >= 10) score += 10;
        
        // Profit bonus
        if (getTotalProfit() > 0) score += 15;
        if (getTotalProfitPercentage() > 10) score += 10;
        
        return Math.min(score, 100);
    }
}

