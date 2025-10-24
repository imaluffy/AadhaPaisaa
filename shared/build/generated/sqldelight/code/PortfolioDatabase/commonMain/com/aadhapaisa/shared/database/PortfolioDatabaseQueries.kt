package com.aadhapaisa.shared.database

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class PortfolioDatabaseQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (
    id: Long,
    stock_symbol: String,
    stock_name: String,
    quantity: Long,
    buy_price: Double,
    purchase_date: Long,
    current_price: Double,
    current_value: Double,
    invested_value: Double,
    profit_loss: Double,
    profit_loss_percent: Double,
    days_held: Long,
    day_change: Double,
    day_change_percent: Double,
  ) -> T): Query<T> = Query(857_882_402, arrayOf("holding"), driver, "PortfolioDatabase.sq",
      "selectAll", "SELECT * FROM holding ORDER BY purchase_date DESC") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!,
      cursor.getLong(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getDouble(13)!!
    )
  }

  public fun selectAll(): Query<Holding> = selectAll { id, stock_symbol, stock_name, quantity,
      buy_price, purchase_date, current_price, current_value, invested_value, profit_loss,
      profit_loss_percent, days_held, day_change, day_change_percent ->
    Holding(
      id,
      stock_symbol,
      stock_name,
      quantity,
      buy_price,
      purchase_date,
      current_price,
      current_value,
      invested_value,
      profit_loss,
      profit_loss_percent,
      days_held,
      day_change,
      day_change_percent
    )
  }

  public fun <T : Any> selectBySymbol(stock_symbol: String, mapper: (
    id: Long,
    stock_symbol: String,
    stock_name: String,
    quantity: Long,
    buy_price: Double,
    purchase_date: Long,
    current_price: Double,
    current_value: Double,
    invested_value: Double,
    profit_loss: Double,
    profit_loss_percent: Double,
    days_held: Long,
    day_change: Double,
    day_change_percent: Double,
  ) -> T): Query<T> = SelectBySymbolQuery(stock_symbol) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!,
      cursor.getLong(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getDouble(13)!!
    )
  }

  public fun selectBySymbol(stock_symbol: String): Query<Holding> = selectBySymbol(stock_symbol) {
      id, stock_symbol_, stock_name, quantity, buy_price, purchase_date, current_price,
      current_value, invested_value, profit_loss, profit_loss_percent, days_held, day_change,
      day_change_percent ->
    Holding(
      id,
      stock_symbol_,
      stock_name,
      quantity,
      buy_price,
      purchase_date,
      current_price,
      current_value,
      invested_value,
      profit_loss,
      profit_loss_percent,
      days_held,
      day_change,
      day_change_percent
    )
  }

  public fun <T : Any> getRecentPurchases(`value`: Long, mapper: (
    id: Long,
    stock_symbol: String,
    stock_name: String,
    quantity: Long,
    buy_price: Double,
    purchase_date: Long,
    current_price: Double,
    current_value: Double,
    invested_value: Double,
    profit_loss: Double,
    profit_loss_percent: Double,
    days_held: Long,
    day_change: Double,
    day_change_percent: Double,
  ) -> T): Query<T> = GetRecentPurchasesQuery(value) { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!,
      cursor.getLong(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getDouble(13)!!
    )
  }

  public fun getRecentPurchases(value_: Long): Query<Holding> = getRecentPurchases(value_) { id,
      stock_symbol, stock_name, quantity, buy_price, purchase_date, current_price, current_value,
      invested_value, profit_loss, profit_loss_percent, days_held, day_change, day_change_percent ->
    Holding(
      id,
      stock_symbol,
      stock_name,
      quantity,
      buy_price,
      purchase_date,
      current_price,
      current_value,
      invested_value,
      profit_loss,
      profit_loss_percent,
      days_held,
      day_change,
      day_change_percent
    )
  }

  public fun <T : Any> getPositiveHoldings(mapper: (
    id: Long,
    stock_symbol: String,
    stock_name: String,
    quantity: Long,
    buy_price: Double,
    purchase_date: Long,
    current_price: Double,
    current_value: Double,
    invested_value: Double,
    profit_loss: Double,
    profit_loss_percent: Double,
    days_held: Long,
    day_change: Double,
    day_change_percent: Double,
  ) -> T): Query<T> = Query(-1_256_332_676, arrayOf("holding"), driver, "PortfolioDatabase.sq",
      "getPositiveHoldings",
      "SELECT * FROM holding WHERE profit_loss >= 0 ORDER BY purchase_date DESC") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!,
      cursor.getLong(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getDouble(13)!!
    )
  }

  public fun getPositiveHoldings(): Query<Holding> = getPositiveHoldings { id, stock_symbol,
      stock_name, quantity, buy_price, purchase_date, current_price, current_value, invested_value,
      profit_loss, profit_loss_percent, days_held, day_change, day_change_percent ->
    Holding(
      id,
      stock_symbol,
      stock_name,
      quantity,
      buy_price,
      purchase_date,
      current_price,
      current_value,
      invested_value,
      profit_loss,
      profit_loss_percent,
      days_held,
      day_change,
      day_change_percent
    )
  }

  public fun <T : Any> getNegativeHoldings(mapper: (
    id: Long,
    stock_symbol: String,
    stock_name: String,
    quantity: Long,
    buy_price: Double,
    purchase_date: Long,
    current_price: Double,
    current_value: Double,
    invested_value: Double,
    profit_loss: Double,
    profit_loss_percent: Double,
    days_held: Long,
    day_change: Double,
    day_change_percent: Double,
  ) -> T): Query<T> = Query(1_693_079_736, arrayOf("holding"), driver, "PortfolioDatabase.sq",
      "getNegativeHoldings",
      "SELECT * FROM holding WHERE profit_loss < 0 ORDER BY purchase_date DESC") { cursor ->
    mapper(
      cursor.getLong(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!,
      cursor.getDouble(4)!!,
      cursor.getLong(5)!!,
      cursor.getDouble(6)!!,
      cursor.getDouble(7)!!,
      cursor.getDouble(8)!!,
      cursor.getDouble(9)!!,
      cursor.getDouble(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getDouble(13)!!
    )
  }

  public fun getNegativeHoldings(): Query<Holding> = getNegativeHoldings { id, stock_symbol,
      stock_name, quantity, buy_price, purchase_date, current_price, current_value, invested_value,
      profit_loss, profit_loss_percent, days_held, day_change, day_change_percent ->
    Holding(
      id,
      stock_symbol,
      stock_name,
      quantity,
      buy_price,
      purchase_date,
      current_price,
      current_value,
      invested_value,
      profit_loss,
      profit_loss_percent,
      days_held,
      day_change,
      day_change_percent
    )
  }

  public fun insertHolding(
    stock_symbol: String,
    stock_name: String,
    quantity: Long,
    buy_price: Double,
    purchase_date: Long,
    current_price: Double,
    current_value: Double,
    invested_value: Double,
    profit_loss: Double,
    profit_loss_percent: Double,
    days_held: Long,
    day_change: Double,
    day_change_percent: Double,
  ) {
    driver.execute(2_043_086_695, """
        |INSERT INTO holding (
        |    stock_symbol,
        |    stock_name,
        |    quantity,
        |    buy_price,
        |    purchase_date,
        |    current_price,
        |    current_value,
        |    invested_value,
        |    profit_loss,
        |    profit_loss_percent,
        |    days_held,
        |    day_change,
        |    day_change_percent
        |) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 13) {
          bindString(0, stock_symbol)
          bindString(1, stock_name)
          bindLong(2, quantity)
          bindDouble(3, buy_price)
          bindLong(4, purchase_date)
          bindDouble(5, current_price)
          bindDouble(6, current_value)
          bindDouble(7, invested_value)
          bindDouble(8, profit_loss)
          bindDouble(9, profit_loss_percent)
          bindLong(10, days_held)
          bindDouble(11, day_change)
          bindDouble(12, day_change_percent)
        }
    notifyQueries(2_043_086_695) { emit ->
      emit("holding")
    }
  }

  public fun updateHolding(
    stock_name: String,
    quantity: Long,
    buy_price: Double,
    purchase_date: Long,
    current_price: Double,
    current_value: Double,
    invested_value: Double,
    profit_loss: Double,
    profit_loss_percent: Double,
    days_held: Long,
    day_change: Double,
    day_change_percent: Double,
    stock_symbol: String,
  ) {
    driver.execute(1_058_643_799, """
        |UPDATE holding SET
        |    stock_name = ?,
        |    quantity = ?,
        |    buy_price = ?,
        |    purchase_date = ?,
        |    current_price = ?,
        |    current_value = ?,
        |    invested_value = ?,
        |    profit_loss = ?,
        |    profit_loss_percent = ?,
        |    days_held = ?,
        |    day_change = ?,
        |    day_change_percent = ?
        |WHERE stock_symbol = ?
        """.trimMargin(), 13) {
          bindString(0, stock_name)
          bindLong(1, quantity)
          bindDouble(2, buy_price)
          bindLong(3, purchase_date)
          bindDouble(4, current_price)
          bindDouble(5, current_value)
          bindDouble(6, invested_value)
          bindDouble(7, profit_loss)
          bindDouble(8, profit_loss_percent)
          bindLong(9, days_held)
          bindDouble(10, day_change)
          bindDouble(11, day_change_percent)
          bindString(12, stock_symbol)
        }
    notifyQueries(1_058_643_799) { emit ->
      emit("holding")
    }
  }

  public fun deleteHolding(stock_symbol: String) {
    driver.execute(-1_945_145_291, """DELETE FROM holding WHERE stock_symbol = ?""", 1) {
          bindString(0, stock_symbol)
        }
    notifyQueries(-1_945_145_291) { emit ->
      emit("holding")
    }
  }

  public fun deleteAllHoldings() {
    driver.execute(134_676_291, """DELETE FROM holding""", 0)
    notifyQueries(134_676_291) { emit ->
      emit("holding")
    }
  }

  private inner class SelectBySymbolQuery<out T : Any>(
    public val stock_symbol: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("holding", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("holding", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(557_257_710, """SELECT * FROM holding WHERE stock_symbol = ?""", mapper,
        1) {
      bindString(0, stock_symbol)
    }

    override fun toString(): String = "PortfolioDatabase.sq:selectBySymbol"
  }

  private inner class GetRecentPurchasesQuery<out T : Any>(
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("holding", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("holding", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_125_701_180,
        """SELECT * FROM holding ORDER BY purchase_date DESC LIMIT ?""", mapper, 1) {
      bindLong(0, value)
    }

    override fun toString(): String = "PortfolioDatabase.sq:getRecentPurchases"
  }
}
