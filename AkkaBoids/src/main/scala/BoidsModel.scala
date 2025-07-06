package it.unibo.pcd

import Boid.Boid
sealed trait BoidsModel:
  val separationWeight: Double
  val alignmentWeight: Double
  val cohesionWeight: Double
  val width: Double
  val height: Double
  val maxSpeed: Double
  val perceptionRadius: Double
  val avoidRadius: Double

  val minX: Double = -width / 2
  val maxX: Double = width / 2
  val minY: Double = -height / 2
  val maxY: Double = height / 2

  def reset: Boid =
    val random = scala.util.Random
    val pos = Position(random.between(minX, maxX), random.between(minY, maxY))
    val vel =
      Velocity(random.between(0, maxSpeed / 2), random.between(0, maxSpeed / 2)) - Velocity(maxSpeed / 4, maxSpeed / 4)
    Boid(pos, vel)

object BoidsModel:
  def actor: ActorBoidsModel = ActorBoidsModel()

  case class ActorBoidsModel(
      separationWeight: Double = 1.0,
      alignmentWeight: Double = 1.0,
      cohesionWeight: Double = 1.0,
      width: Double = 800,
      height: Double = 600,
      maxSpeed: Double = 4.0,
      perceptionRadius: Double = 50.0,
      avoidRadius: Double = 20.0
  ) extends BoidsModel
