package jp.seraphr.narou.webui

import org.scalajs.dom.SVGElement
import org.scalajs.dom.console
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

import jp.seraphr.narou.model.NarouNovel
import jp.seraphr.recharts.{ Axis, CartesianGrid, ScatterChart }

import japgolly.scalajs.react.{ BackendScope, Callback, Reusability, ScalaComponent }
import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.extra.Px
import typings.react.mod.SVGProps
import typings.recharts.{ rechartsStrings, typesCartesianScatterMod => scatterMod }
import typings.recharts.components.Scatter
import typings.recharts.typesUtilTypesMod.Margin

object NovelScatterChart {
  import jp.seraphr.recharts.Implicits._

  import js.JSConverters._

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
    implicit val propsReusable: Reusability[Props] = Reusability
      .by[Props, Seq[Any]](p => Seq(p.axisX, p.axisY, p.novels, p.selectedNovel, p.scatters))(Reusability.by_==)

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
        } yield PointData(x, y, n.title, n)
      }
  }

  class Backend(scope: BackendScope[Props, Unit]) {
    case class ScatterInput(
        name: String,
        points: Seq[PointData],
        color: String
    )

    case class ScattersInput(
        scatters: Seq[ScatterInput],
        selectNovel: NarouNovel => Callback
    )
    object ScattersInput {
      implicit val reusable: Reusability[ScattersInput] =
        Reusability.by[ScattersInput, Seq[Any]](input => Seq(input.scatters))(Reusability.by_==)

    }
    val scatterPropss =
      Px.props(scope)
        .map { props =>
          val tScatters = props
            .scatters
            .map { tScatterData =>
              val tPoints = createPointData(props.novels, props.axisX, props.axisY, tScatterData)
              val tName   = s"${tScatterData.name}(${tPoints.size})"

              ScatterInput(tName, tPoints, tScatterData.color)
            }
          ScattersInput(tScatters, props.selectNovel)
        }
        .withReuse
        .autoRefresh
        .map { input =>

          console.debug(s"create scatter props !!")
          input
            .scatters
            .map { tScatterData =>
              scatterMod
                .Props()
                .setName(tScatterData.name)
                .setData(tScatterData.points.map(a => a: Any).toJSArray)
                .setFill(tScatterData.color)
                .setIsAnimationActive(false)
                .setOnClick { (a1, _, _) =>
                  val novel = a1.asInstanceOf[js.Dynamic].payload.asInstanceOf[PointData].novel
                  input.selectNovel(novel)
                }
            }
        }

    var lastScatters: Seq[ChildArg] = null
    def render(props: Props)        = {
      import typings.recharts.components.{ XAxis, YAxis, ZAxis, Tooltip, Legend, Label, ReferenceDot }

      val Props(_, aSelectedNovel, aAxisX, aAxisY, _, _) = props
      val tScatters: Seq[ChildArg]                       = scatterPropss.value().map(Scatter.withProps)
      console.debug(s"scatters eq lastScatters => ${tScatters eq lastScatters}")
      lastScatters = tScatters

      val tChildren: Seq[ChildArg] = Seq(
        CartesianGrid(CartesianGrid.Props().setStrokeDasharray("3 3")),
        XAxis
          .`type`(Axis.Type.number)
          .dataKey("x")
          .name(aAxisX.name)
          .label(
            Label.create().value(aAxisX.name).angle(0).position(rechartsStrings.insideBottom).build.rawElement
          )
          .unit(aAxisX.unit),
        YAxis
          .create()
          .dataKey("y")
          .name(aAxisY.name)
          .label(
            Label.create().value(aAxisY.name).angle(-90).position(rechartsStrings.insideLeft).build.rawElement
          )
          .unit(aAxisY.unit),
        ZAxis().`type`(rechartsStrings.category).dataKey("z").range(js.Array(20, 20)).name("title"),
        Tooltip.cursor(SVGProps[SVGElement]().setStrokeDasharray("3 3")).build,
        Legend.create()
      )
      val tReDot                   = for {
        tSelected <- aSelectedNovel
        x         <- aAxisX.toValue(tSelected)
        y         <- aAxisY.toValue(tSelected)
      } yield {
        ReferenceDot
          .create(
            className = js.undefined,
            cx = x,
            cy = y,
            r = 5
          )
          .x(x)
          .y(y)
          .isFront(true)
          .fill("red")
          .build
      }

      ScatterChart(
        ScatterChart
          .Props()
          .setWidth(2400)
          .setHeight(600)
          .setMargin(
            Margin().setTop(20).setRight(20).setBottom(10).setLeft(10)
          )
      )(tChildren ++ tScatters ++ tReDot.to(Seq): _*)
    }

  }

  val component = ScalaComponent
    .builder[Props]("NovelScatterChart")
    .stateless
    .renderBackend[Backend]
    .configure(Reusability.shouldComponentUpdate)
    .build

  //  val component = ScalaFnComponent[Props] { case Props(aNovels, aSelectedNovel, aAxisX, aAxisY, aScatters, selectNovel) =>
  //    val tScatters: Seq[ChildArg] = aScatters.map { tScatterData =>
  //      val tPoints = createPointData(aNovels, aAxisX, aAxisY, tScatterData).toJSArray
  //      val tName = s"${tScatterData.name}(${tPoints.size})"
  //      Scatter.create(tName, js.undefined)
  //        .data(tPoints)
  //        .fill(tScatterData.color)
  //        .isAnimationActive(false)
  //        .onClick((a1, _, _) => Callback {
  //          val novel = a1.asInstanceOf[js.Dynamic].payload.asInstanceOf[PointData].novel
  //          selectNovel(novel)
  //        })
  //        .build
  //    }
  //
  //    import typings.recharts.components.{ XAxis, YAxis, ZAxis, Tooltip, Legend, Label, ReferenceDot }
  //
  //    val tChildren: Seq[ChildArg] = Seq(
  //      CartesianGrid(CartesianGrid.Props().setStrokeDasharray("3 3")),
  //      XAxis
  //        .`type`(Axis.Type.number)
  //        .dataKey("x")
  //        .name(aAxisX.name)
  //        .label(
  //          Label.create()
  //            .value(aAxisX.name)
  //            .angle(0)
  //            .position(rechartsStrings.insideBottom)
  //            .build.rawElement
  //        )
  //        .unit(aAxisX.unit),
  //      YAxis.create()
  //        .dataKey("y")
  //        .name(aAxisY.name)
  //        .label(
  //          Label.create()
  //            .value(aAxisY.name)
  //            .angle(-90)
  //            .position(rechartsStrings.insideLeft)
  //            .build.rawElement
  //        )
  //        .unit(aAxisY.unit),
  //      ZAxis().
  //        `type`(rechartsStrings.category)
  //        .dataKey("z")
  //        .range(js.Array(20, 20))
  //        .name("title"),
  //      Tooltip.cursor(SVGProps[SVGElement]().setStrokeDasharray("3 3").asInstanceOf[Boolean | Element | SVGProps[SVGElement]]),
  //      Legend.create()
  //    )
  //    val tReDot = for {
  //      tSelected <- aSelectedNovel
  //      x <- aAxisX.toValue(tSelected)
  //      y <- aAxisY.toValue(tSelected)
  //    } yield {
  //      ReferenceDot.create(
  //        className = js.undefined,
  //        cx = x,
  //        cy = y,
  //        r = 10
  //      )
  //        .x(x)
  //        .y(y)
  //        .isFront(true)
  //        .fill("red")
  //        .build
  //    }
  //
  //    ScatterChart(
  //      ScatterChart.Props().setWidth(2400).setHeight(600).setMargin(
  //        Margin().setTop(20)
  //          .setRight(20)
  //          .setBottom(10)
  //          .setLeft(10)
  //      )
  //    )(tChildren ++ tScatters ++ tReDot.to(Seq): _*)
  //  }

  //  private val memo = React.memo(component)
  def apply(
      aNovels: Seq[NarouNovel],
      aSelectedNovel: Option[NarouNovel],
      aScatters: Seq[ScatterData],
      selectNovel: NarouNovel => Callback
  ) = {
    component(
      Props(
        novels = aNovels,
        selectedNovel = aSelectedNovel,
        axisX = AxisData.bookmark,
        axisY = AxisData.evaluationPerBookmark,
        scatters = aScatters,
        selectNovel
      )
    )
  }

}
