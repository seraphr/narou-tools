package jp.seraphr.recharts

object Axis {

  type Type = String
  object Type {
    val category = "category"
    val number = "number"
  }

  type ScaleType = String
  object ScaleType {
    val auto = "auto"
    val linear = "linear"
    val pow = "pow"
    val sqrt = "sqrt"
    val log = "log"
    val identity = "identity"
    val time = "time"
    val band = "band"
    val point = "point"
    val ordinal = "ordinal"
    val quantile = "quantile"
    val quantize = "quantize"
    val utc = "utc"
    val sequential = "sequential"
    val threshold = "threshold"
  }

  type AxisType = String
  object AxisType {
    val xAxis = "xAxis"
    val yAxis = "yAxis"
    val angleAxis = "angleAxis"
    val radiusAxis = "radiusAxis"
  }
}