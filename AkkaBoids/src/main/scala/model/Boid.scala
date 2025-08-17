package it.unibo.pcd
package model

import it.unibo.pcd.model.Boid.Boid

import scala.annotation.targetName

case class Position(x: Double, y: Double):
  private def op(other: Position, op: (Position, Position) => Position): Position = op(this, other)
  @targetName("sum")
  def +(other: Position): Position = op(other, (a, b) => Position(a.x + b.x, a.y + b.y))
  def -(other: Position): Position = op(other, (a, b) => Position(a.x - b.x, a.y - b.y))
  def *(scalar: Double): Position = Position(x * scalar, y * scalar)
  def /(scalar: Double): Position = Position(x / scalar, y / scalar)
  def distance(other: Position): Double = math.sqrt((other.x - x) * (other.x - x) + (other.y - y) * (other.y - y))
object Position:
  def zero: Position = Position(0, 0)
  def one: Position = Position(1, 1)
  def random: Position = Position(math.random(), math.random())
  def apply(p: (Int, Int)): Position = Position(p._1, p._2)

case class Velocity(x: Double, y: Double):
  @targetName("sum")
  def +(other: Velocity): Velocity = Velocity(x + other.x, y + other.y)
  def -(other: Velocity): Velocity = Velocity(x - other.x, y - other.y)
  def abs: Double = math.sqrt(x * x + y * y)
  def normalized: Velocity = Velocity(x / abs, y / abs)
  def /(scalar: Double): Velocity = Velocity(x / scalar, y / scalar)
  def *(scalar: Double): Velocity = Velocity(x * scalar, y * scalar)
object Velocity:
  def zero: Velocity = Velocity(0, 0)
  def one: Velocity = Velocity(1, 1)
  def random: Velocity = Velocity(math.random(), math.random())
  def apply(v: (Int, Int)): Velocity = Velocity(v._1, v._2)

object Boid:

  case class Boid(position: Position, velocity: Velocity)

//    def update(model: BoidsModel): Boid =
//      //  println("boid before" + this)
//      val sep = separation(model) * model.separationWeight
//      val ali = alignment(model) * model.alignmentWeight
//      val coh = cohesion(model) * model.cohesionWeight
//
//      val newVelocity = velocity + sep + ali + coh
//      val speed = newVelocity.abs
//      val newVelocityNormalized =
//        if speed > model.maxSpeed then newVelocity.normalized * model.maxSpeed else newVelocity
//
//      val newPosition = position + Position(newVelocityNormalized.x, newVelocityNormalized.y)
//
//      val tmp = Boid(wrapPosition(newPosition, model), newVelocityNormalized)
//      // println("boid after" + tmp)
//      tmp
//
//    private def wrapPosition(pos: Position, model: BoidsModel): Position =
//      val newX = pos.x match
//        case x if x < model.minX => x + model.width
//        case x if x >= model.maxX => x - model.width
//        case x => x
//
//      val newY = pos.y match
//        case y if y < model.minY => y + model.height
//        case y if y >= model.maxY => y - model.height
//        case y => y
//
//      Position(newX, newY)
//
//    private def neighbors(implicit model: BoidsModel): Seq[Boid] =
//      for
//        boid <- model.boids
//        if boid != this && boid.position.distance(position) < model.perceptionRadius
//      yield boid
//
//    private def separation(implicit model: BoidsModel): Velocity =
//      val boids = for
//        boid <- neighbors
//        if boid.position.distance(position) < model.avoidRadius
//      yield boid.position
//
//      if boids.isEmpty then Velocity.zero
//      else
//        val res = boids.foldLeft(position)(_ - _) / boids.size
//        Velocity(res.x, res.y).normalized
//
//    private def cohesion(implicit mode: BoidsModel): Velocity =
//      if neighbors.isEmpty then Velocity.zero
//      else
//        val center = neighbors.map(_._1).foldLeft(Position.zero)(_ + _) / neighbors.size
//        Velocity(center.x - position.x, center.y - position.y).normalized
//
//    private def alignment(implicit mode: BoidsModel): Velocity =
//      if neighbors.isEmpty then Velocity.zero
//      else
//        val avgVel = neighbors.map(_._2).foldLeft(Velocity.zero)(_ + _) / neighbors.size
//        Velocity(avgVel.x - velocity.x, avgVel.y - velocity.y).normalized
  object Boid:
    def apply(pos: (Int, Int), vel: (Int, Int)): Boid =
      Boid(Position(pos), Velocity(vel))
