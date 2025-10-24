package com.aadhapaisa.shared.database

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.aadhapaisa.shared.database.shared.newInstance
import com.aadhapaisa.shared.database.shared.schema
import kotlin.Unit

public interface PortfolioDatabase : Transacter {
  public val portfolioDatabaseQueries: PortfolioDatabaseQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = PortfolioDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): PortfolioDatabase =
        PortfolioDatabase::class.newInstance(driver)
  }
}
