package it.unibo.pcd

import Boid.Boid

case class BoidsModel(
    boids: Seq[Boid] = List.empty,
    separationWeight: Double = 1.0,
    alignmentWeight: Double = 1.0,
    cohesionWeight: Double = 1.0,
    width: Double = 100,
    height: Double = 100,
    maxSpeed: Double = 10,
    perceptionRadius: Double = 2,
    avoidRadius: Double = 2
):

  def initBoids(n: Int): BoidsModel =
    val random = scala.util.Random
    copy(boids = for
      _ <- 0 until n
      pos = Position(random.between(minX, maxX), random.between(minY, maxY))
      vel = Velocity(random.between(0, maxSpeed / 2), random.between(0, maxSpeed / 2)) - Velocity(
        maxSpeed / 4,
        maxSpeed / 4
      )
    yield Boid(pos, vel))

  def boids_=(newBoids: Seq[Boid]): BoidsModel =
    copy(boids = newBoids)

  def separationWeight_=(weight: Double): BoidsModel =
    copy(separationWeight = weight)

  def alignmentWeight_=(weight: Double): BoidsModel =
    copy(alignmentWeight = weight)

  def cohesionWeight_=(weight: Double): BoidsModel =
    copy(cohesionWeight = weight)

  def width_=(x: Double): BoidsModel =
    copy(width = x)

  def height_=(y: Double): BoidsModel =
    copy(height = y)

  def minX: Double = -width / 2

  def maxX: Double = width / 2

  def minY: Double = -height / 2

  def maxY: Double = height / 2

object BoidsModel:
  def empty: BoidsModel = BoidsModel()
