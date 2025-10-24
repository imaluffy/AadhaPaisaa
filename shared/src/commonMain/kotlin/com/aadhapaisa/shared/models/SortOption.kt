package com.aadhapaisa.shared.models

enum class SortOption(val displayName: String) {
    DAYS("Days Held"),
    INVESTED("Invested Amount"),
    PROFIT("Profit/Loss"),
    SHARES("Number of Shares"),
    DAILY_PROFIT("Daily Profit/Loss")
}

enum class SortDirection(val displayName: String) {
    ASCENDING("Low to High"),
    DESCENDING("High to Low")
}

