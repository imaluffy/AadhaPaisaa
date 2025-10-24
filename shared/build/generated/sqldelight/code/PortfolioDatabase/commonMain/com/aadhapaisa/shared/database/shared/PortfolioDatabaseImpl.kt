package com.aadhapaisa.shared.database.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.aadhapaisa.shared.database.PortfolioDatabase
import com.aadhapaisa.shared.database.PortfolioDatabaseQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<PortfolioDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = PortfolioDatabaseImpl.Schema

internal fun KClass<PortfolioDatabase>.newInstance(driver: SqlDriver): PortfolioDatabase =
    PortfolioDatabaseImpl(driver)

private class PortfolioDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), PortfolioDatabase {
  override val portfolioDatabaseQueries: PortfolioDatabaseQueries = PortfolioDatabaseQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE holding (
          |    id INTEGER PRIMARY KEY AUTOINCREMENT,
          |    stock_symbol TEXT NOT NULL,
          |    stock_name TEXT NOT NULL,
          |    quantity INTEGER NOT NULL,
          |    buy_price REAL NOT NULL,
          |    purchase_date INTEGER NOT NULL,
          |    current_price REAL NOT NULL,
          |    current_value REAL NOT NULL,
          |    invested_value REAL NOT NULL,
          |    profit_loss REAL NOT NULL,
          |    profit_loss_percent REAL NOT NULL,
          |    days_held INTEGER NOT NULL,
          |    day_change REAL NOT NULL,
          |    day_change_percent REAL NOT NULL
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
