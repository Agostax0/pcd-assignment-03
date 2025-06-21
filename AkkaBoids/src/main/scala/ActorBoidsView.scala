package it.unibo.pcd

import Boid.Boid
import BoidsViewMessages.Render

import akka.actor.typed.{ActorRef, Behavior, Props}
import akka.actor.typed.scaladsl.Behaviors

import java.awt.{Color, Dimension, Graphics2D}
import javax.swing.BorderFactory
import scala.swing.*
import scala.swing.Swing.onEDT
import scala.swing.event.*

sealed trait BoidsViewMessages
object BoidsViewMessages:
  case class Render(boids: Seq[Boid], from: ActorRef[BoidsControllerMessages]) extends BoidsViewMessages

object ActorBoidsView extends MainFrame:
  import akka.actor.typed.ActorRef
  def apply(
      view: BoidsView = new BoidsView,
      boids: Seq[Boid] = List.empty
  ): Behavior[BoidsViewMessages] =
    Behaviors.setup { context =>
      val controller: ActorRef[BoidsControllerMessages] =
        context.system.systemActorOf(BoidsController(null, null), "controller")

      view.StartStopCallBack = (x: Boolean) =>
        if x then controller ! BoidsControllerMessages.Start else controller ! BoidsControllerMessages.Stop
      view.ResetCallBack = () => controller ! BoidsControllerMessages.Reset
      view.ParametersCallBack =
        (x: Double, y: Double, z: Double) => controller ! BoidsControllerMessages.UpdateParameters(x, y, z)
      view.BoidsCallBack = (x: Int) => controller ! BoidsControllerMessages.UpdateNumberOfBoids(x)
      view.UpdateDimensionsCallBack =
        (x: Double, y: Double) => controller ! BoidsControllerMessages.UpdateDimensions(x, y)

      Behaviors.receiveMessage:
        case Render(boids, from) =>
          context.log.info("Render")
          view.updateBoids(boids)
          Behaviors.same
    }

sealed class BoidsView extends MainFrame:
  this.visible = true
  this.title = "Boids Simulation"
  this.preferredSize = new Dimension(800, 600)
  this.background = Color.WHITE
  private val initialBoidsCount = 50
  private var isRunning = false

  def updateBoids(boids: Seq[Boid]): Unit =
    this.boids = boids
    onEDT(repaint())
  private var boids: Seq[Boid] = List.empty

  private val simulationPanel = new Panel:
    preferredSize = new Dimension(800, 600)
    background = Color.WHITE

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)

      val w = size.width
      val h = size.height
      val xScale = w / h

      g.setColor(Color.BLUE)
      for boid <- boids do
        val x = boid.position.x
        val y = boid.position.y
        val px = (w / 2 + x * xScale).toInt
        val py = (h / 2 - y * xScale).toInt
        g.fillOval(px, py, 5, 5)

      g.setColor(Color.BLACK)
      g.drawString(s"Num. Boids: ${boids.size}", 10, 25)

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

  contents = new BorderPanel:
    add(simulationPanel, BorderPanel.Position.Center)

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
      BorderPanel.Position.East
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
  var ParametersCallBack: (Double, Double, Double) => Unit = (x, y, z) => {}
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
      ParametersCallBack(weight, alignmentSlider.value / 100.0, cohesionSlider.value / 100.0)

    case ValueChanged(`alignmentSlider`) =>
      val weight = alignmentSlider.value / 100.0
      ParametersCallBack(separationSlider.value / 100.0, weight, cohesionSlider.value / 100.0)

    case ValueChanged(`cohesionSlider`) =>
      val weight = cohesionSlider.value / 100.0
      ParametersCallBack(separationSlider.value / 100.0, alignmentSlider.value / 100.0, weight)

    case ValueChanged(`boidsCountSlider`) =>
      val count = boidsCountSlider.value
      BoidsCallBack(count)

    case event: event.UIElementResized =>
      UpdateDimensionsCallBack(size.width, size.height)
  }

  override def closeOperation(): Unit =
    super.closeOperation()
