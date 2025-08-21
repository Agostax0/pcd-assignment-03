package it.unibo.pcd
package model

import it.unibo.pcd.model.Boid.Boid

import scala.annotation.targetName

case class Position(x: Double, y: Double):
  private def op(other: Position, op: (Position, Position) => Position): Position = op(this, other)
  @targetName("sum")
  def +(other: Position): Position = Position(x + other.x, y + other.y)
  def $plus(other: Position): Position = this + other
  def -(other: Position): Position = Position(x - other.x, y - other.y)
  def *(scalar: Double): Position = Position(x * scalar, y * scalar)
  def /(scalar: Double): Position = Position(x / scalar, y / scalar)
  def distance(other: Position): Double = math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2))
object Position:
  def zero: Position = Position(0, 0)
  def one: Position = Position(1, 1)
  def random: Position = Position(math.random(), math.random())
  def apply(p: (Int, Int)): Position = Position(p._1, p._2)

case class Velocity(x: Double, y: Double):
  @targetName("sum")
  def +(other: Velocity): Velocity = Velocity(x + other.x, y + other.y)
  def $plus(other: Velocity): Velocity = this + other
  def -(other: Velocity): Velocity = Velocity(x - other.x, y - other.y)
  def abs: Double = math.sqrt(x * x + y * y)
  def normalized: Velocity = Velocity(x / abs, y / abs)
  def /(scalar: Double): Velocity = Velocity(x / scalar, y / scalar)
  def *(scalar: Double): Velocity = Velocity(x * scalar, y * scalar)
  def $mul(scalar: Double): Velocity = Velocity(x * scalar, y * scalar)

object Velocity:
  def zero: Velocity = Velocity(0, 0)
  def one: Velocity = Velocity(1, 1)
  def random: Velocity = Velocity(math.random(), math.random())
  def apply(v: (Int, Int)): Velocity = Velocity(v._1, v._2)

object Boid:

  case class Boid(position: Position, velocity: Velocity)
  object Boid:
    def apply(pos: (Int, Int), vel: (Int, Int)): Boid =
      Boid(Position(pos), Velocity(vel))
