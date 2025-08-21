package it.unibo.pcd
package view

import it.unibo.pcd.model.Boid.Boid
import BoidsViewMessages.*
import akka.actor.typed.{ActorRef, Behavior, Props}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.controller.BoidsControllerMessages
import it.unibo.pcd.model.{BoidsModel, Position}
import it.unibo.pcd.view.BoidsViewMessages

import java.awt.{Color, Dimension, Graphics2D}
import javax.swing.BorderFactory
import scala.concurrent.Future
import scala.language.postfixOps
import scala.swing.*
import scala.swing.Swing.onEDT
import scala.swing.event.*
import scala.util.Success

sealed trait BoidsViewMessages
object BoidsViewMessages:
  case class Render(points: List[Position]) extends BoidsViewMessages
  case object Start extends BoidsViewMessages
  case object Stop extends BoidsViewMessages
  case object Reset extends BoidsViewMessages
  case class UpdateModel(model: BoidsModel) extends BoidsViewMessages
  case class UpdateBoids(count: Int) extends BoidsViewMessages
  case class SetVisibleView(ref: ActorRef[BoidsControllerMessages]) extends BoidsViewMessages

object ActorBoidsView:
  import akka.actor.typed.ActorRef
  def apply(
      view: BoidsView = new BoidsView,
      boids: Seq[Boid] = List.empty,
      controller: ActorRef[BoidsControllerMessages] = null
  ): Behavior[BoidsViewMessages] =
    Behaviors.setup { context =>

      val callBacks: () => Unit = () =>
        view.StartStopCallBack = isRunning =>
          context.pipeToSelf(Future.successful(isRunning)) {
            case Success(true) => Start
            case Success(false) => Stop
            case scala.util.Failure(_) => Stop
          }
        view.ResetCallBack = () =>
          context.pipeToSelf(Future.successful(null)) {
            case Success(value) => Reset
            case scala.util.Failure(_) => Stop
          }
        view.ParametersCallBack = (separation, alignment, cohesion, width, height) =>
          context.pipeToSelf(
            Future.successful(
              BoidsModel.localModel.copy(
                separationWeight = separation, alignmentWeight = alignment, cohesionWeight = cohesion, width = width,
                height = height
              )
            )
          ) {
            case Success(model) => UpdateModel(model)
            case scala.util.Failure(_) => Stop
          }
        view.BoidsCallBack = count =>
          context.pipeToSelf(Future.successful(count)) {
            case Success(value) => UpdateBoids(count)
            case scala.util.Failure(_) => Stop
          }

      Behaviors.receiveMessage:
        case SetVisibleView(ref) =>
          view.visible = true
          callBacks()
          apply(view = view, controller = ref)

        case Render(points) =>
          view.updateBoids(points)
          Behaviors.same

        case Start =>
          controller ! BoidsControllerMessages.Start
          Behaviors.same

        case Stop =>
          controller ! BoidsControllerMessages.Stop
          Behaviors.same

        case Reset =>
          controller ! BoidsControllerMessages.Reset
          Behaviors.same

        case UpdateBoids(count) =>
          controller ! BoidsControllerMessages.UpdateNumberOfBoids(count)
          view.updateBoidsLabel(count)
          Behaviors.same
        case UpdateModel(model) =>
          controller ! BoidsControllerMessages.UpdateModel(model)
          Behaviors.same
    }

sealed class BoidsView extends MainFrame:

  this.title = "Boids Simulation"
  this.preferredSize = new Dimension(800, 800)
  this.background = Color.WHITE
  private val initialBoidsCount = 50
  private var isRunning = false

  def updateBoids(boids: List[Position]): Unit =
    this.boids = boids
    updateBoidsLabel(boids.size)
    onEDT(simulationPanel.repaint())
  private var boids: List[Position] = List.empty

  private val simulationPanel = new Panel:
    preferredSize = new Dimension(400, 400)
    background = Color.WHITE

    listenTo(mouse.clicks)

    reactions += { case MousePressed(_, point, _, _, _) =>
      println(point)
    }

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)

      g.setColor(Color.BLUE)
      for boid <- boids do
        val x = boid.x.toInt
        val y = boid.y.toInt
        g.fillOval(x, y, 5, 5)

  private val startStopButton = new Button:
    text = "Start"

  private val resetButton = new Button:
    text = "Reset"

  private def createWeightSlider() = new Slider:
    min = 100
    max = 200
    value = 100
    majorTickSpacing = 20
    minorTickSpacing = 10
    paintTicks = true
    paintLabels = true

  val separationSlider = createWeightSlider()
  val alignmentSlider = createWeightSlider()
  val cohesionSlider = createWeightSlider()

  private val boidsCountSlider = new Slider:
    min = 15
    max = 150
    value = initialBoidsCount
    majorTickSpacing = 25
    minorTickSpacing = 5
    paintTicks = true
    paintLabels = true

  def updateBoidsLabel(newCount: Int): Unit = boidCountLabel.text = boidLabelString + s"$newCount"

  private val boidLabelString = "Num of boids: "
  private val boidCountLabel = new Label(boidLabelString + s"$initialBoidsCount")

  contents = new BorderPanel:
    add(simulationPanel, BorderPanel.Position.Center)
    add(boidCountLabel, BorderPanel.Position.North)
    add(
      new BoxPanel(Orientation.Vertical):
        contents ++= Seq(
          new FlowPanel:
            contents ++= Seq(startStopButton, resetButton)
          ,
          createLabeledSlider("Separation Weight", separationSlider),
          createLabeledSlider("Alignment Weight", alignmentSlider),
          createLabeledSlider("Cohesion Weight", cohesionSlider),
          createLabeledSlider("Number of Boids", boidsCountSlider)
        )
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
      ,
      BorderPanel.Position.South
    )

  private def createLabeledSlider(label: String, slider: Slider) = new BoxPanel(Orientation.Vertical):
    contents ++= Seq(
      new Label(label),
      slider
    )
    border = BorderFactory.createEmptyBorder(5, 0, 5, 0)

  listenTo(startStopButton, resetButton)
  listenTo(separationSlider, alignmentSlider, cohesionSlider, boidsCountSlider)
  listenTo(this)

  var StartStopCallBack: (Boolean) => Unit = x => {}
  var ResetCallBack: () => Unit = () => {}
  var ParametersCallBack: (Double, Double, Double, Int, Int) => Unit = (a, b, c, d, e) => {}
  var BoidsCallBack: (Int) => Unit = n => {}
  var UpdateDimensionsCallBack: (Double, Double) => Unit = (x, y) => {}

  reactions += {
    case ButtonClicked(`startStopButton`) =>
      isRunning = !isRunning
      StartStopCallBack(isRunning)
      startStopButton.text = if isRunning then "Stop" else "Start"

    case ButtonClicked(`resetButton`) =>
      isRunning = false
      ResetCallBack()
      startStopButton.text = "Start"

    case ValueChanged(`separationSlider`) =>
      val weight = separationSlider.value / 100.0
      ParametersCallBack(
        weight,
        alignmentSlider.value / 100.0,
        cohesionSlider.value / 100.0,
        simulationPanel.size.width,
        simulationPanel.size.height
      )

    case ValueChanged(`alignmentSlider`) =>
      val weight = alignmentSlider.value / 100.0
      ParametersCallBack(
        separationSlider.value / 100.0,
        weight,
        cohesionSlider.value / 100.0,
        simulationPanel.size.width,
        simulationPanel.size.height
      )

    case ValueChanged(`cohesionSlider`) =>
      val weight = cohesionSlider.value / 100.0
      ParametersCallBack(
        separationSlider.value / 100.0,
        alignmentSlider.value / 100.0,
        weight,
        simulationPanel.size.width,
        simulationPanel.size.height
      )

    case ValueChanged(`boidsCountSlider`) =>
      val count = boidsCountSlider.value
      BoidsCallBack(count)

  }

  override def closeOperation(): Unit =
    super.closeOperation()
