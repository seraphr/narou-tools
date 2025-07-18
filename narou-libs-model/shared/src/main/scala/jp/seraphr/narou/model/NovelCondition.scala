package jp.seraphr.narou.model

case class NovelConditionWithSource(condition: NovelCondition, source: String)        {
  def id        = condition.id
  def name      = condition.name
  def predicate = condition.predicate
}
case class NovelCondition(id: String, name: String, predicate: NarouNovel => Boolean) {

  /** 条件を論理AND結合します */
  def and(aThat: NovelCondition): NovelCondition = {
    NovelCondition(
      s"${this.id}_and_${aThat.id}",
      s"${this.name} & ${aThat.name}",
      n => this.predicate(n) && aThat.predicate(n)
    )
  }

  /** 条件を論理AND結合します（名前を変更しません） */
  def andQuietly(aThat: NovelCondition): NovelCondition = {
    this.copy(predicate = n => this.predicate(n) && aThat.predicate(n))
  }

  /** 条件を論理OR結合します */
  def or(aThat: NovelCondition): NovelCondition = {
    NovelCondition(
      s"${this.id}_or_${aThat.id}",
      s"${this.name} | ${aThat.name}",
      n => this.predicate(n) || aThat.predicate(n)
    )
  }

  /** 条件を論理NOT反転します */
  def not: NovelCondition = {
    if (this == NovelCondition.finished) NovelCondition.notFinished
    else if (this == NovelCondition.notFinished) NovelCondition.finished
    else NovelCondition(s"not_${this.id}", s"not ${this.name}", n => !this.predicate(n))
  }

}
object NovelCondition {
  val all: NovelCondition          = NovelCondition("all", "all", _ => true)
  val finished: NovelCondition     = NovelCondition("finished", "完結済み", _.isFinished)
  val notFinished: NovelCondition  = NovelCondition("notFinished", "連載中", !_.isFinished)
  val length100k: NovelCondition   = NovelCondition("length100k", "10万字以上", 100000 <= _.length)
  val length300k: NovelCondition   = NovelCondition("length300k", "30万字以上", 300000 <= _.length)
  val length500k: NovelCondition   = NovelCondition("length500k", "50万字以上", 500000 <= _.length)
  val length1m: NovelCondition     = NovelCondition("length100k", "100万字以上", 1000000 <= _.length)
  val bookmark100: NovelCondition  = NovelCondition("bookmark100", "100 <= bookmark", 100 <= _.bookmarkCount)
  val bookmark1000: NovelCondition = NovelCondition("bookmark1000", "1000 <= bookmark", 1000 <= _.bookmarkCount)

  def bookmark(aMin: Int, aMax: Int): NovelCondition =
    NovelCondition(
      s"bookmark${aMin}-${aMax}",
      s"${aMin} <= bookmark < ${aMax}",
      n => aMin <= n.bookmarkCount && n.bookmarkCount < aMax
    )

  val bookmark0_500: NovelCondition    = bookmark(0, 500)
  val bookmark500_1000: NovelCondition = bookmark(500, 1000)
  val bookmark1k_3k: NovelCondition    = bookmark(1000, 3000)
  val bookmark3k: NovelCondition       = bookmark(3000, 1000000)
}

object NovelConditionParser {
  enum FilterOp {
    case EQ, NQ, GE, GQ, LE, LQ

    override def toString: String = this match {
      case EQ => "=="
      case NQ => "!="
      case GE => ">="
      case GQ => ">"
      case LE => "<="
      case LQ => "<"
    }

  }

  object FilterOp {
    def fromString(str: String): FilterOp = str match {
      case "==" => EQ
      case "!=" => NQ
      case ">=" => GE
      case ">"  => GQ
      case "<=" => LE
      case "<"  => LQ
    }

  }

  private def genFilter(aToValue: NarouNovel => Int, aOp: FilterOp, aValue: Int): NarouNovel => Boolean = aOp match {
    case FilterOp.EQ => aToValue(_) == aValue
    case FilterOp.NQ => aToValue(_) != aValue
    case FilterOp.GE => aToValue(_) >= aValue
    case FilterOp.GQ => aToValue(_) > aValue
    case FilterOp.LE => aToValue(_) <= aValue
    case FilterOp.LQ => aToValue(_) < aValue
  }

  private type GenCondition = (FilterOp, Int) => NovelCondition
  private val mFilterValues: Map[String, GenCondition] = Map(
    "length"      -> { (op, value) =>
      val tName   = s"length ${op} ${value}"
      val tFilter = genFilter(_.length, op, value)

      NovelCondition(tName, tName, tFilter)
    },
    "bookmark"    -> { (op, value) =>
      val tName   = s"bookmark ${op} ${value}"
      val tFilter = genFilter(_.bookmarkCount, op, value)

      NovelCondition(tName, tName, tFilter)
    },
    "globalPoint" -> { (op, value) =>
      val tName   = s"globalPoint ${op} ${value}"
      val tFilter = genFilter(_.globalPoint, op, value)

      NovelCondition(tName, tName, tFilter)
    },
    "evalPoint"   -> { (op, value) =>
      val tName   = s"evalPoint ${op} ${value}"
      val tFilter = genFilter(_.evaluationPoint, op, value)

      NovelCondition(tName, tName, tFilter)
    },
    "evalCount"   -> { (op, value) =>
      val tName   = s"evalCount ${op} ${value}"
      val tFilter = genFilter(_.evaluationCount, op, value)

      NovelCondition(tName, tName, tFilter)
    },
    "genreId"     -> { (op, value) =>
      val tName   = s"genreId ${op} ${value}"
      val tFilter = genFilter(_.genre.id, op, value)

      NovelCondition(tName, tName, tFilter)
    },
    "bigGenreId"  -> { (op, value) =>
      val tName   = s"bigGenreId ${op} ${value}"
      val tFilter = genFilter(_.genre.bigGenre.id, op, value)

      NovelCondition(tName, tName, tFilter)
    }
  )

  private val mBooleanValues = Map(
    "isFinished"   -> NovelCondition.finished,
    "isR15"        -> NovelCondition("isR15", "R15", _.isR15),
    "isBL"         -> NovelCondition("isBL", "BL", _.isBL),
    "isGL"         -> NovelCondition("isGL", "GL", _.isGL),
    "isZankoku"    -> NovelCondition("isZankoku", "残酷", _.isZankoku),
    "isTensei"     -> NovelCondition("isTensei", "転生", _.isTensei),
    "isTenni"      -> NovelCondition("isTenni", "転移", _.isTenni),
    "isRomance"    -> NovelCondition("isRomance", "恋愛", _.genre.bigGenre == BigGenre.Romance),
    "isFantasy"    -> NovelCondition("isFantasy", "ファンタジー", _.genre.bigGenre == BigGenre.Fantasy),
    "isLiterature" -> NovelCondition("isLiterature", "文学", _.genre.bigGenre == BigGenre.Literature),
    "isSF"         -> NovelCondition("isSF", "SF", _.genre.bigGenre == BigGenre.SF),
    "isOther"      -> NovelCondition("isOther", "その他", _.genre.bigGenre == BigGenre.Other),
    "isNonGenre"   -> NovelCondition("isNonGenre", "ノンジャンル", _.genre.bigGenre == BigGenre.NonGenre)
  )

  import fastparse.*
  import fastparse.SingleLineWhitespace.*

  private def space[$: P]: P[Unit] = " ".rep

  private def expr[$: P]: P[NovelCondition] = P(term ~ (space ~ ("&&" | "||").! ~ space ~ term).rep).map {
    case (tHead, tTail) =>
      tTail.foldLeft(tHead) { case (tLeft, (tOp, tRight)) =>
        if (tOp == "&&") tLeft and tRight
        else tLeft or tRight
      }
  }

  private def term[S: P]: P[NovelCondition] = {
    // mapして|で結合すると動かないが、foldLeftで結合すると動く。 何故かは知らん
    def field[S: P]   = mFilterValues.keys.foldLeft[P[Unit]](Fail)(_ | P(_))
    def op[S: P]      = StringIn("==", "!=", ">=", ">", "<=", "<")
    def value[S: P]   = CharsWhile(_.isDigit, min = 1)
    def tFilter[S: P] = P(field.! ~ op.! ~ value.!).map { (aField, aOp, aValue) =>
      mFilterValues(aField)(FilterOp.fromString(aOp), aValue.toInt)
    }

    def tNot[S: P] = P("!" ~ factor).map(_.not)
    tFilter | tNot | factor
  }

  private def factor[$: P]: P[NovelCondition] = {
    // mapして|で結合すると動かないが、foldLeftで結合すると動く。 何故かは知らん
    def tBoolean[S: P]     = mBooleanValues.keys.foldLeft[P[Unit]](Fail)(_ | P(_)).!.map(mBooleanValues(_))
    def trueLiteral[$: P]  = P("true").map(_ => NovelCondition("true", "true", _ => true))
    def allLiteral[$: P]   = P("all").map(_ => NovelCondition("all", "all", _ => true))
    def falseLiteral[$: P] = P("false").map(_ => NovelCondition("false", "false", _ => false))
    def tParen[S: P]       = P("(" ~ expr ~ ")")

    tBoolean | tParen | trueLiteral | allLiteral | falseLiteral
  }

  /** 文字列からNovelConditionをパースします */
  def apply(aSource: String): Either[String, NovelConditionWithSource] = {
    def parser[$: P] = P(expr ~ End)
    parse(aSource, parser(_)) match {
      case Parsed.Success(tResult, _)  => Right(NovelConditionWithSource(tResult, aSource))
      case Parsed.Failure(_, _, extra) => Left(extra.trace().longAggregateMsg)
    }
  }

}
