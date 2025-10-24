# AadhaPaisa - KMM Portfolio Management App

A modern, dark-themed portfolio management app built with Kotlin Multiplatform (KMM) that works on both Android and iOS. Features real-time stock data, AI-powered insights, and news aggregation.

## 🌟 Features

### 🏠 Home Page
- **Dashboard**: Total invested amount, current value, and profit/loss with color-coded indicators
- **Recent Purchases**: List of recently purchased stocks with days held, quantities, and performance
- **Dark Mode Design**: Modern dark theme with neon green/red accents

### 📊 Stock Page
- **Portfolio Overview**: Complete portfolio summary with key metrics
- **Divide Portfolio**: Split view showing positive vs negative returns
- **Stock Cards**: Detailed view of each holding with performance metrics
- **Interactive Toggle**: Switch between combined and divided views

### 💡 Insights Tab
- **AI Insights**: AI-driven portfolio analysis and recommendations
- **News Aggregation**: Latest news about your holdings from multiple sources
- **Smart Categorization**: Insights categorized by performance, risk, opportunities, and warnings
- **Real-time Updates**: Auto-refreshing news and insights

## 🏗️ Architecture

### Tech Stack
- **Kotlin Multiplatform (KMM)** for shared business logic
- **Jetpack Compose Multiplatform** for Android UI
- **SwiftUI** for iOS UI
- **MVVM Architecture** with shared ViewModels
- **Ktor** for networking and API calls
- **SQLDelight** for local data persistence
- **Coroutines + Flow** for reactive programming

### Project Structure
```
AadhaPaisa/
├── shared/                    # KMM shared module
│   ├── src/commonMain/kotlin/
│   │   ├── models/           # Data models
│   │   ├── repository/       # Repository layer
│   │   ├── viewmodel/        # ViewModels
│   │   ├── ui/              # Compose UI components
│   │   ├── theme/           # Dark mode theme
│   │   ├── api/             # API services
│   │   └── ai/              # AI integration
│   └── src/commonMain/sqldelight/
│       └── database/         # Database schema
├── androidApp/               # Android application
└── iosApp/                  # iOS application
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Xcode 14+ (for iOS development)
- Kotlin 1.9.20+
- JDK 8+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd AadhaPaisa
   ```

2. **Open in Android Studio**
   - Open the project in Android Studio
   - Sync Gradle files
   - Wait for dependencies to download

3. **Run on Android**
   - Select Android device or emulator
   - Click Run button or use `./gradlew :androidApp:installDebug`

4. **Run on iOS**
   - Open `iosApp/iosApp.xcodeproj` in Xcode
   - Select iOS simulator or device
   - Build and run

### Configuration

#### API Keys (Optional - for real data)
Create a `local.properties` file in the root directory:
```properties
# Stock Data API
ALPHA_VANTAGE_API_KEY=your_alpha_vantage_key
YAHOO_FINANCE_API_KEY=your_yahoo_finance_key

# AI Services
OPENAI_API_KEY=your_openai_key
GEMINI_API_KEY=your_gemini_key

# News API
NEWS_API_KEY=your_news_api_key
```

## 🎨 Design System

### Dark Mode Theme
- **Background**: `#121212` (Pure black)
- **Surface**: `#1E1E1E` (Dark gray)
- **Cards**: `#1A1A1A` with subtle gradients
- **Text**: White and light gray variants
- **Accents**: Neon green (`#00FF88`) for profit, red (`#FF3B30`) for loss

### Typography
- **Headlines**: Bold, large sizes for emphasis
- **Body Text**: Clean, readable fonts
- **Labels**: Small, medium weight for metadata

## 📱 Screenshots

### Home Dashboard
- Clean dashboard with key metrics
- Recent purchases with performance indicators
- Dark theme with neon accents

### Portfolio View
- Complete portfolio overview
- Divide portfolio functionality
- Stock cards with detailed metrics

### Insights & News
- AI-powered portfolio insights
- Latest market news
- Smart categorization and filtering

## 🔧 Development

### Adding New Features
1. Create data models in `shared/src/commonMain/kotlin/models/`
2. Implement repository in `shared/src/commonMain/kotlin/repository/`
3. Create ViewModel in `shared/src/commonMain/kotlin/viewmodel/`
4. Build UI components in `shared/src/commonMain/kotlin/ui/`
5. Update navigation in `MainApp.kt`

### Database Schema
The app uses SQLDelight for local storage with the following tables:
- `Stock`: Stock information and current prices
- `Holding`: User's stock holdings
- `Insight`: AI-generated insights
- `NewsItem`: News articles and updates

### API Integration
- **Stock Data**: Alpha Vantage, Yahoo Finance
- **News**: NewsAPI, MarketWatch, Financial Express
- **AI**: OpenAI GPT, Google Gemini

## 🚀 Future Enhancements

### Planned Features
- [ ] Real-time stock price updates
- [ ] Push notifications for price alerts
- [ ] Advanced charting and analytics
- [ ] Social features and sharing
- [ ] Watchlist functionality
- [ ] Tax optimization suggestions
- [ ] Portfolio comparison tools
- [ ] Export to PDF/Excel

### AI Enhancements
- [ ] Sentiment analysis of news
- [ ] Predictive analytics
- [ ] Risk scoring
- [ ] Automated rebalancing suggestions
- [ ] Market trend analysis

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation

---

**AadhaPaisa** - Your intelligent portfolio companion 🚀

