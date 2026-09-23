package jp.seraphr.narou.webui.component

import org.scalajs.dom.SVGElement
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.narou.webui.{ AxisData, ConvertInput, ScatterData }
import jp.seraphr.recharts.{ Axis, CartesianGrid }

import japgolly.scalajs.react.{ BackendScope, Callback, Reusability, ScalaComponent }
import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.extra.Px
import typings.react.mod.SVGProps
import typings.recharts.{ rechartsStrings, typesCartesianScatterMod as scatterMod }
import typings.recharts.anon.PartialMargin
import typings.recharts.components.{ Scatter, ScatterChart }

object NovelScatterChart {
  import jp.seraphr.recharts.Implicits.*

  import js.JSConverters.*

  /**
   * @param novels
   * @param selectedNovel 選択状態で表示する小説
   * @param axisX
   * @param axisY
   * @param scatters
   * @param selectNovel グラフ上で小説が選択された時のコールバック
   */
  case class Props(
      novels: Seq[NarouNovel],
      selectedNovel: Option[NarouNovel],
      axisX: AxisData,
      axisY: AxisData,
      scatters: Seq[ScatterData],
      selectNovel: NarouNovel => Callback
  )

  object Props {
    implicit val propsReusable: Reusability[Props] = Reusability.by[Props, Seq[Any]](p =>
      Seq(p.axisX, p.axisY, p.novels, p.selectedNovel, p.scatters)
    )(Reusability.by_==)

  }

  @JSExportAll
  case class PointData(x: Double, y: Double, z: String, novel: NarouNovel)
  private def createPointData(
      aNovels: Seq[NarouNovel],
      aAxisX: AxisData,
      aAxisY: AxisData,
      aScatter: ScatterData
  ): Seq[PointData] = {
    aScatter
      .convert(ConvertInput(aNovels, aAxisX, aAxisY))
      .flatMap { n =>
        for {
          x <- aAxisX.toValue(n)
          y <- aAxisY.toValue(n)
        } yield PointData(x, y, s"${n.title} [${n.genre.text}]", n)
      }
  }

  class Backend(aScope: BackendScope[Props, Unit]) {
    case class ScatterInput(name: String, points: Seq[PointData], color: String)
    case class ScattersInput(scatters: Seq[ScatterInput], selectNovel: NarouNovel => Callback)

    object ScattersInput {
      implicit val reusable: Reusability[ScattersInput] =
        Reusability.by[ScattersInput, Seq[ScatterInput]](_.scatters)(Reusability.by_==)

    }

    private val mScatters =
      Px.props(aScope)
        .map { tProps =>
          val tScatters = tProps
            .scatters
            .map { tScatterData =>
              val tPoints = createPointData(tProps.novels, tProps.axisX, tProps.axisY, tScatterData)
              ScatterInput(s"${tScatterData.name}(${tPoints.size})", tPoints, tScatterData.color)
            }
          ScattersInput(tScatters, tProps.selectNovel)
        }
        .withReuse
        .autoRefresh
        .map { tInput =>
          tInput
            .scatters
            .map { tScatterData =>
              Scatter.withProps(
                scatterMod
                  .Props()
                  .setName(tScatterData.name)
                  .setData(tScatterData.points.map(tPoint => tPoint: Any).toJSArray)
                  .setFill(tScatterData.color)
                  .setIsAnimationActive(false)
                  .setOnClick { (tPoint, _, _) =>
                    val tNovel = tPoint.asInstanceOf[js.Dynamic].payload.asInstanceOf[PointData].novel
                    tInput.selectNovel(tNovel)
                  }
              ): ChildArg
            }
        }

    def render(aProps: Props) = {
      import typings.recharts.components.{ Label, Legend, ReferenceDot, Tooltip, XAxis, YAxis, ZAxis }

      val Props(_, tSelectedNovel, tAxisX, tAxisY, _, _) = aProps
      val tChildren: Seq[ChildArg]                       = Seq(
        CartesianGrid(CartesianGrid.Props().setStrokeDasharray("3 3")),
        XAxis
          .`type`(Axis.Type.number)
          .dataKey("x")
          .name(tAxisX.name)
          .label(
            Label.create().value(tAxisX.name).angle(0).position(rechartsStrings.insideBottom).build.rawElement
          )
          .unit(tAxisX.unit),
        YAxis
          .create()
          .dataKey("y")
          .name(tAxisY.name)
          .label(
            Label.create().value(tAxisY.name).angle(-90).position(rechartsStrings.insideLeft).build.rawElement
          )
          .unit(tAxisY.unit),
        ZAxis().`type`(rechartsStrings.category).dataKey("z").range(js.Tuple2(50.0, 50.0)).name("title"),
        Tooltip.cursor(SVGProps[SVGElement]().setStrokeDasharray("3 3")).build,
        Legend.create()
      )
      val tReferenceDot                                  = for {
        tSelected <- tSelectedNovel
        x         <- tAxisX.toValue(tSelected)
        y         <- tAxisY.toValue(tSelected)
      } yield {
        ReferenceDot[Double, Double]().x(x).y(y).r(5).fill("red").build
      }

      ScatterChart[PointData]()
        .width(2400)
        .height(600)
        .margin(PartialMargin().setTop(20).setRight(20).setBottom(10).setLeft(10))(
          (tChildren ++ mScatters.value() ++ tReferenceDot.to(Seq))*
        )
        .build
    }

  }

  val component = ScalaComponent
    .builder[Props]("NovelScatterChart")
    .stateless
    .backend(new Backend(_))
    .renderP((tScope, tProps) => tScope.backend.render(tProps))
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(
      aNovels: Seq[NarouNovel],
      aSelectedNovel: Option[NarouNovel],
      aAxisX: AxisData,
      aAxisY: AxisData,
      aScatters: Seq[ScatterData],
      selectNovel: NarouNovel => Callback
  ) = {
    component(
      Props(
        novels = aNovels,
        selectedNovel = aSelectedNovel,
        axisX = aAxisX,
        axisY = aAxisY,
        scatters = aScatters,
        selectNovel
      )
    )
  }

}
