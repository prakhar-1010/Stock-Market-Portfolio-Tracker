# Indian Stock Market Portfolio Tracker (Gamified)

A Java Swing desktop app to track NSE/BSE holdings with a modern dark theme, gamified UX (levels, XP, achievements), Quick Stats, CSV import/export, and charts.

Currency: ₹ INR

## Highlights

- Gamified UI: XP, levels, and achievements panel
- AMOLED black theme (customizable colors in `GamifiedPortfolioGUI`)
- Quick Stats panel with:
  - Total investment, current value, overall P/L and %
  - Top gainer/loser, win rate, average buy vs current
  - Diversification (stock count, simple sector count) and Risk Score
- Auto-Save (every 5 min by default) and Auto-Refresh (15 min) toggles
- Import from Zerodha/Groww CSV, Generic CSV with column mapping; Export to CSV
- Charts: Portfolio Pie Chart and Profit/Loss Bar Chart (JFreeChart)

## Project Structure

```
OOTS JAVA PROJECT/
├── GamifiedPortfolioGUI.java   # Main modern GUI (recommended)
├── PortfolioTrackerGUI.java    # Classic GUI
├── Portfolio.java              # Portfolio model and stats
├── Stock.java                  # Stock entity (Serializable)
├── PortfolioImporter.java      # Zerodha/Groww/Generic CSV import + template
├── StockPriceAPI.java          # Price lookup helpers (demo)
├── run.bat                     # One-click build and run on Windows
└── README.md
```

## One‑click Run (Windows)

- Double-click `run.bat` (or run in terminal):
  ```powershell
  .\run.bat            # Gamified UI
  .\run.bat classic    # Classic UI
  ```
- The script will:
  - Create `lib/` if missing and download `jfreechart-1.5.4.jar`
  - Compile all sources
  - Launch the app with `javaw`

## Manual build/run

```powershell
javac -cp ".;lib/jfreechart-1.5.4.jar" *.java
java  -cp ".;lib/jfreechart-1.5.4.jar" GamifiedPortfolioGUI
```

## Using the App

- Add Stock: fetches current price, choose quantity and whether you bought previously or now
- Import/Export: import Zerodha/Groww/Generic CSV or export your portfolio to CSV
- Refresh: updates prices; can be auto-refreshed every 15 minutes
- Auto-Save: enabled by default (every 5 min) from the status bar toggle
- Quick Stats: shows portfolio metrics, diversification and a simple risk score
- Charts: Bar Chart (P/L by stock) and Pie Chart (allocation by value)

## Quick Stats details

- Investment/Value/P&L: from `Portfolio` totals
- Top gainer/loser: best/worst profit % among holdings
- Win rate: winners / total; averages are simple means of buy and current prices
- Diversification: count of positions and a basic sector heuristic (symbol-based)
- Risk Score: 100 - average(|profit%|) clamped to [0,100]

## Shortcuts and Tips

- Status bar toggles: Auto-Save (5 min) and Auto-Refresh (15 min)
- Achievements auto-update as you add/import stocks
- CSV template: Import → “Download Template” to get a sample file

## Screenshots

Place your screenshot(s) in `docs/screenshots/`.

![App Screenshot](docs/screenshots/your-stocks.png)

(Optional) Add more images and links as you like.

## CSV Template

- Download the sample CSV here: [docs/portfolio_template.csv](docs/portfolio_template.csv)
- Columns: `Symbol, Name, Quantity, Buy Price, Current Price`
- You can also generate it from the app via Import → “Download Template”.

## Troubleshooting

- If the batch script can’t download the JAR, manually download `jfreechart-1.5.4.jar` to `lib/` and re-run.
- If the window does not appear, run from terminal to see errors:
  ```powershell
  java -cp ".;lib/jfreechart-1.5.4.jar" GamifiedPortfolioGUI
  ```

## License

Educational project for the fun and  OOTS Java course.

