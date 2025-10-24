package com.aadhapaisa.shared.database

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Holding(
  public val id: Long,
  public val stock_symbol: String,
  public val stock_name: String,
  public val quantity: Long,
  public val buy_price: Double,
  public val purchase_date: Long,
  public val current_price: Double,
  public val current_value: Double,
  public val invested_value: Double,
  public val profit_loss: Double,
  public val profit_loss_percent: Double,
  public val days_held: Long,
  public val day_change: Double,
  public val day_change_percent: Double,
)
