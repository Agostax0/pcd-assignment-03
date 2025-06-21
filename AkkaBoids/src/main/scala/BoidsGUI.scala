package it.unibo.pcd

import scala.swing.*
import scala.swing.event.*
import java.awt.{Color, Dimension, Graphics2D}
import javax.swing.{BorderFactory, JSlider, Timer}
import javax.swing.Timer
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
/*
class BoidsGUI extends MainFrame:
  title = "Boids Simulation"
  private var isRunning = false
  private val initialBoidsCount = 50
  private var model = BoidsModel.empty.initBoids(initialBoidsCount)

  private val updateDelay = 16 // approximately 60 FPS
  private val timer = new Timer(
    updateDelay,
    _ =>
      if isRunning then
        model = model.copy(boids = model.boids.map(_.update(model)))
        simulationPanel.repaint()
  )

  timer.start()

  // Panel for simulation visualization
  private val simulationPanel = new Panel:
    preferredSize = new Dimension(800, 600)
    background = Color.WHITE

    override def paintComponent(g: Graphics2D): Unit =
      super.paintComponent(g)

      val w = size.width
      val h = size.height
      val envWidth = model.width // Assuming you have a reference to your model
      val xScale = w / envWidth

      // Draw boids
      g.setColor(Color.BLUE)
      for boid <- model.boids do
        val x = boid.position.x
        val y = boid.position.y
        val px = (w / 2 + x * xScale).toInt
        val py = (h / 2 - y * xScale).toInt
        g.fillOval(px, py, 5, 5)

      // Draw statistics
      g.setColor(Color.BLACK)
      g.drawString(s"Num. Boids: ${model.boids.size}", 10, 25)

  // Control components
  private val startStopButton = new Button:
    text = "Start"

  private val resetButton = new Button:
    text = "Reset"

  // Custom slider factory method
  private def createWeightSlider() = new Slider:
    min = 100 // Will represent 1.0
    max = 200 // Will represent 2.0
    value = 100
    majorTickSpacing = 20
    minorTickSpacing = 10
    paintTicks = true
    paintLabels = true

  private val separationSlider = createWeightSlider()
  private val alignmentSlider = createWeightSlider()
  private val cohesionSlider = createWeightSlider()

  private val boidsCountSlider = new Slider:
    min = 15
    max = 150
    value = initialBoidsCount
    majorTickSpacing = 25
    minorTickSpacing = 5
    paintTicks = true
    paintLabels = true

  // Layout definition
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

  // Helper method to create a labeled slider panel
  private def createLabeledSlider(label: String, slider: Slider) = new BoxPanel(Orientation.Vertical):
    contents ++= Seq(
      new Label(label),
      slider
    )
    border = BorderFactory.createEmptyBorder(5, 0, 5, 0)

  // Event handling
  listenTo(startStopButton, resetButton)
  listenTo(separationSlider, alignmentSlider, cohesionSlider, boidsCountSlider)

  reactions += {
    case ButtonClicked(`startStopButton`) =>
      isRunning = !isRunning
      startStopButton.text = if isRunning then "Stop" else "Start"

    case ButtonClicked(`resetButton`) =>
      isRunning = false
      startStopButton.text = "Start"
      model = model.initBoids(boidsCountSlider.value)
      simulationPanel.repaint()

    case ValueChanged(`separationSlider`) =>
      val weight = separationSlider.value / 100.0
      model = model.separationWeight = weight

    case ValueChanged(`alignmentSlider`) =>
      val weight = alignmentSlider.value / 100.0
      model = model.alignmentWeight = weight

    case ValueChanged(`cohesionSlider`) =>
      val weight = cohesionSlider.value / 100.0
      model = model.cohesionWeight = weight

    case ValueChanged(`boidsCountSlider`) =>
      val count = boidsCountSlider.value
      model = model.initBoids(count)
  }

  override def closeOperation(): Unit =
    timer.stop()
    super.closeOperation()

object BoidsGUI:
  def main(args: Array[String]): Unit =
    val ui = new BoidsGUI
    ui.visible = true
*/