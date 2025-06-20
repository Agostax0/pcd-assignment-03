package it.unibo.pcd

import Boid.Boid
sealed trait BoidsModel:
  val boids: Seq[Boid]
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
object BoidsModel:
  def local: LocalBoidsModel = LocalBoidsModel()

  case class LocalBoidsModel(
      boids: Seq[Boid] = List.empty,
      separationWeight: Double = 1.0,
      alignmentWeight: Double = 1.0,
      cohesionWeight: Double = 1.0,
      width: Double = 100,
      height: Double = 100,
      maxSpeed: Double = 4.0,
      perceptionRadius: Double = 50.0,
      avoidRadius: Double = 20.0
  ) extends BoidsModel:

    def initBoids(n: Int): Seq[Boid] =
      val random = scala.util.Random
      for
        _ <- 0 until n
        pos = Position(random.between(minX, maxX), random.between(minY, maxY))
        vel = Velocity(random.between(0, maxSpeed / 2), random.between(0, maxSpeed / 2)) - Velocity(
          maxSpeed / 4,
          maxSpeed / 4
        )
      yield Boid(pos, vel)
