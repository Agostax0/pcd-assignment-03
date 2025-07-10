package it.unibo.pcd

import Boid.Boid

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.ActorReceptionistMessages.RelayAll
import it.unibo.pcd.BoidActor.BoidActorMessages

import scala.language.postfixOps
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

  def update(boid: Boid, neighbors: List[Boid]): Boid =
    val separationValue = separation(boid, neighbors) * separationWeight
    val cohesionValue = cohesion(boid, neighbors) * cohesionWeight
    val alignmentValue = alignment(boid, neighbors) * alignmentWeight

    var vel = separationValue + cohesionValue + alignmentValue
    val speed = vel.abs

    if speed > maxSpeed then vel = vel.normalized * maxSpeed

    var pos = boid.position + Position(vel.x, vel.y)

    if pos.x < minX then pos = pos + Position(width, 0)
    if pos.x >= maxX then pos = pos - Position(width, 0)
    if pos.y < minY then pos = pos + Position(0, height)
    if pos.y >= minY then pos = pos - Position(0, height)

    Boid(pos, vel)

  private def alignment(boid: Boid, neighbors: List[Boid]): Velocity =
    val velocities =
      for neighbor <- neighbors
      yield neighbor.velocity
    if velocities.isEmpty then Velocity.zero
    else (velocities.foldRight(Velocity.zero)(_ + _) / velocities.size).normalized
  private def cohesion(boid: Boid, neighbors: List[Boid]): Velocity =
    val positions =
      for neighbor <- neighbors
      yield neighbor.position
    if positions.isEmpty then Velocity.zero
    else
      val center = (positions.foldRight(Position.zero)(_ + _) / positions.size) - boid.position
      Velocity(center.x, center.y).normalized
  private def separation(boid: Boid, neighbors: List[Boid]): Velocity =
    val positions = for
      neighbor <- neighbors
      if neighbor.position.distance(boid.position) < avoidRadius
      position = boid.position - neighbor.position
    yield position
    if positions.isEmpty then Velocity.zero
    else (positions.foldRight(Velocity.zero)((pos, vel) => Velocity(pos.x, pos.x) + vel) / positions.size).normalized
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

trait BoidModelMessages
object BoidModelMessages:
  case class UpdateBoidNumber(n: Int) extends BoidModelMessages
  case class UpdateModel(model: BoidsModel) extends BoidModelMessages
  case class ReceivePosition(pos: Position, size: Int) extends BoidModelMessages
  case object Step extends BoidModelMessages
object BoidModelActor:
  def apply(
      positions: List[Position] = List.empty
  ): Behavior[BoidModelMessages] =
    Behaviors.setup { context =>
      val receptionist = context.spawn(BoidActorsReceptionist(context.self), "boidReceptionist")

      import BoidModelMessages.*
      Behaviors.receiveMessage {
        case UpdateBoidNumber(n) =>
          Behaviors.same
        case UpdateModel(model) =>
          receptionist ! RelayAll(BoidActorMessages.UpdateModel(model))
          Behaviors.same
        case Step =>
          receptionist ! ActorReceptionistMessages.SendPositions
          Behaviors.same
        case ReceivePosition(pos, size) =>
          if positions.size < size then apply(positions :+ pos)
          else
            // TODO view render these positions
            ???
          Behaviors.same
      }
    }
