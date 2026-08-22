package jp.seraphr.narou

/** API探索を開始する、小説文字数の0以上の下限です。 */
opaque type MinLength = Int

object MinLength {

  /**
   * 0以上の整数から探索開始文字数を作成します。
   *
   * @param aValue 小説文字数
   */
  def from(aValue: Int): Either[String, MinLength] = {
    Either.cond(0 <= aValue, aValue, "minLengthは0以上の整数を指定してください")
  }

  extension (aMinLength: MinLength) {

    /** 小説文字数を整数として返します。 */
    def value: Int = aMinLength
  }
}
