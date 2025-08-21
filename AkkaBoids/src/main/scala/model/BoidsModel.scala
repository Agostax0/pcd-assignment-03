package it.unibo.pcd
package model

import model.Boid.Boid

import it.unibo.pcd.legacy.UpdateUtil

import java.util

sealed trait BoidsModel:
  val separationWeight: Double
  val alignmentWeight: Double
  val cohesionWeight: Double
  val width: Double
  val height: Double
  val maxSpeed: Double
  val perceptionRadius: Double
  val avoidRadius: Double

  val minX: Double = 0
  val maxX: Double = width
  val minY: Double = 0
  val maxY: Double = height

  def reset: Boid =
    val random = scala.util.Random
    val pos = Position(random.between(minX, width), random.between(minY, height))
    val vel = Velocity(random.between(-maxSpeed / 4, maxSpeed / 4), random.between(-maxSpeed / 4, maxSpeed / 4))
    Boid(pos, vel)

  def update(boid: Boid, neighbors: List[Boid]): Boid =

    val legacyBoid = new UpdateUtil(boid.position, boid.velocity)
    val neighborLegacy = new util.ArrayList[UpdateUtil]()
    neighbors.foreach(b => neighborLegacy.add(legacy.UpdateUtil(b.position, b.velocity)))
    legacyBoid.update(this, neighborLegacy)

    Boid(legacyBoid.getPos, legacyBoid.getVel)

object BoidsModel:
  def localModel: LocalModel = LocalModel()

  case class LocalModel(
      separationWeight: Double = 1.0,
      alignmentWeight: Double = 1.0,
      cohesionWeight: Double = 1.0,
      width: Double = 800,
      height: Double = 600,
      maxSpeed: Double = 4.0,
      perceptionRadius: Double = 50.0,
      avoidRadius: Double = 20.0
  ) extends BoidsModel
