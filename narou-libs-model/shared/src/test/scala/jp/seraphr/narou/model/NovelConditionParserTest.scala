package jp.seraphr.narou.model

import monocle.Lens
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class NovelConditionParserTest extends AnyFreeSpec with Matchers with EitherValues with ScalaCheckPropertyChecks {
  val validUploadTypes = Seq(
    UploadType.CellularPhone,
    UploadType.PC,
    UploadType.Both,
    UploadType.Etc(1),
    UploadType.Etc(2),
    UploadType.Etc(3)
  )

  val narouNovelGen: Gen[NarouNovel] = for {
    title             <- Gen.alphaNumStr
    ncode             <- Gen.alphaNumStr
    userId            <- Gen.alphaNumStr
    writer            <- Gen.alphaNumStr
    story             <- Gen.alphaNumStr
    genre             <- Gen.oneOf(Genre.values.toSeq)
    gensaku           <- Gen.alphaNumStr
    keywords          <- Gen.listOf(Gen.alphaNumStr)
    firstUpload       <- Gen.alphaNumStr
    lastUpload        <- Gen.alphaNumStr
    novelTypeId       <- Gen.oneOf(1, 2)
    novelType         <- novelTypeId match {
                           case 1 => Gen.const(NovelType.Serially)
                           case 2 => Gen.const(NovelType.ShortStory)
                         }
    isFinished        <- Gen.oneOf(true, false)
    chapterCount      <- Gen.posNum[Int]
    length            <- Gen.posNum[Int]
    readTimeMinutes   <- Gen.posNum[Int]
    isR15             <- Gen.oneOf(true, false)
    isBL              <- Gen.oneOf(true, false)
    isGL              <- Gen.oneOf(true, false)
    isZankoku         <- Gen.oneOf(true, false)
    isTensei          <- Gen.oneOf(true, false)
    isTenni           <- Gen.oneOf(true, false)
    uploadType        <- Gen.oneOf(validUploadTypes)
    globalPoint       <- Gen.posNum[Int]
    bookmarkCount     <- Gen.posNum[Int]
    reviewCount       <- Gen.posNum[Int]
    evaluationPoint   <- Gen.posNum[Int]
    evaluationCount   <- Gen.posNum[Int]
    illustrationCount <- Gen.posNum[Int]
    novelUpdatedAt    <- Gen.alphaNumStr
    updatedAt         <- Gen.alphaNumStr
  } yield NarouNovel(
    title,
    ncode,
    userId,
    writer,
    story,
    genre,
    gensaku,
    keywords,
    firstUpload,
    lastUpload,
    novelType,
    isFinished,
    chapterCount,
    length,
    readTimeMinutes,
    isR15,
    isBL,
    isGL,
    isZankoku,
    isTensei,
    isTenni,
    uploadType,
    globalPoint,
    bookmarkCount,
    reviewCount,
    evaluationPoint,
    evaluationCount,
    illustrationCount,
    novelUpdatedAt,
    updatedAt
  )

  case class GenWithLens[A, B](gen: Gen[A], lens: Lens[A, B]) {
    def by(b: B): Gen[A] = gen.map(lens.replace(b))
  }

  extension [A](aGen: Gen[A]) {
    def replace[B](aLens: Lens[A, B]): GenWithLens[A, B] = GenWithLens(aGen, aLens)
  }
  import NarouNovel.lens

  def assertParseSuccess(aSource: String, aTest: NarouNovel, aExpect: Boolean): Unit = {
    assert(NovelConditionParser(aSource).value.predicate(aTest) === aExpect)
    NovelConditionParser(aSource).value.source shouldBe aSource
  }

  "比較演算" - {
    "length" - {
      "==" - {
        "match" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length == 1234", tNovel, true)
          }
        }

        "notMatch" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length == 1235", tNovel, false)
          }
        }
      }

      "!=" - {
        "match" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length != 1235", tNovel, true)
          }
        }

        "notMatch" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length != 1234", tNovel, false)
          }
        }
      }

      "<" - {
        "match" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length < 1235", tNovel, true)
          }
        }

        "notMatch" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length < 1234", tNovel, false)
          }
        }
      }

      "<=" - {
        "match" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length <= 1234", tNovel, true)
          }
        }

        "notMatch" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length <= 1233", tNovel, false)
          }
        }
      }

      ">" - {
        "match" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length > 1233", tNovel, true)
          }
        }

        "notMatch" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length > 1234", tNovel, false)
          }
        }
      }

      ">=" - {
        "match" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length >= 1234", tNovel, true)
          }
        }

        "notMatch" in {
          forAll(narouNovelGen replace lens.length by 1234) { tNovel =>
            assertParseSuccess("length >= 1235", tNovel, false)
          }
        }
      }
    }
  }

  "リテラル" - {
    "true" in {
      forAll(narouNovelGen) { tNovel =>
        assertParseSuccess("true", tNovel, true)
      }
    }

    "all" in {
      forAll(narouNovelGen) { tNovel =>
        assertParseSuccess("all", tNovel, true)
      }
    }

    "false" in {
      forAll(narouNovelGen) { tNovel =>
        assertParseSuccess("false", tNovel, false)
      }
    }
  }

  "グルーピング" - {
    "意味のない()" in {
      forAll(narouNovelGen) { tNovel =>
        assertParseSuccess("(true)", tNovel, true)
      }
    }

    "(true || false) && (false ||  true) が 恒真となること" in {
      forAll(narouNovelGen) { tNovel =>
        assertParseSuccess("(true || false) && (false || true)", tNovel, true)
      }
    }
  }

  "結合演算子" - {
    "&&" - {
      "match" in {
        forAll(narouNovelGen) { tNovel =>
          assertParseSuccess("true && true", tNovel, true)
        }
      }

      "notMatch" in {
        forAll(narouNovelGen) { tNovel =>
          assertParseSuccess("true && false", tNovel, false)
          assertParseSuccess("false && true", tNovel, false)
          assertParseSuccess("false && false", tNovel, false)
        }
      }
    }

    "||" - {
      "match" in {
        forAll(narouNovelGen) { tNovel =>
          assertParseSuccess("true || true", tNovel, true)
          assertParseSuccess("true || false", tNovel, true)
          assertParseSuccess("false || true", tNovel, true)
        }
      }

      "notMatch" in {
        forAll(narouNovelGen) { tNovel =>
          assertParseSuccess("false || false", tNovel, false)
        }
      }
    }

    "&&と||には結合順位が無く、左結合であること" in {
      forAll(narouNovelGen) { tNovel =>
        assertParseSuccess("false && false || true", tNovel, true)
        assertParseSuccess("true || false && false", tNovel, false)
      }
    }
  }

  "否定" - {
    "!" - {
      "match" in {
        forAll(narouNovelGen) { tNovel =>
          assertParseSuccess("!false", tNovel, true)
          assertParseSuccess("!(true && false)", tNovel, true)
        }
      }

      "notMatch" in {
        forAll(narouNovelGen) { tNovel =>
          assertParseSuccess("!true", tNovel, false)
          assertParseSuccess("!(true || false)", tNovel, false)
        }
      }
    }
  }
}
