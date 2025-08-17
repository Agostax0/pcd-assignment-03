package it.unibo.pcd
package model

import model.ActorReceptionistMessages.RelayAll
import model.Boid.Boid
import model.BoidActor.BoidActorMessages

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.pcd.controller.BoidsControllerMessages
import it.unibo.pcd.controller.BoidsControllerMessages.GetData

import scala.::
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

  val minX: Double = 0
  val maxX: Double = width
  val minY: Double = 0
  val maxY: Double = height

  def reset: Boid =
    val random = scala.util.Random
    val pos = Position(random.between(minX, maxX), random.between(minY, maxY))
    val vel = Velocity(random.between(0, maxSpeed / 2), random.between(0, maxSpeed / 2))
    Boid(pos, vel)

  def update(boid: Boid, neighbors: List[Boid]): Boid =
    val separationValue = separation(boid, neighbors) * separationWeight
    val cohesionValue = cohesion(boid, neighbors) * cohesionWeight
    val alignmentValue = alignment(boid, neighbors) * alignmentWeight

    var vel = separationValue + cohesionValue + alignmentValue
    val speed = vel.abs

    if speed > maxSpeed then vel = vel.normalized * maxSpeed

    var pos = boid.position + Position(vel.x, vel.y)

    if pos.x < minX then pos = pos + Position(width / 2, 0)
    if pos.x >= maxX then pos = pos - Position(width / 2, 0)
    if pos.y < minY then pos = pos + Position(0, height / 2)
    if pos.y >= minY then pos = pos - Position(0, height / 2)

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
    else
      val positionsPlusVel = positions.foldRight(Velocity.zero)((pos, vel) => Velocity(pos.x, pos.x) + vel)
      val positionPlusVelAvg = positionsPlusVel / positions.size
      val positionPlusVelAvgNormalized = positionPlusVelAvg.normalized
      positionPlusVelAvgNormalized
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
  case class Step(ref: ActorRef[BoidsControllerMessages]) extends BoidModelMessages
  case object Reset extends BoidModelMessages
object BoidModelActor:
  var receptionist: Option[ActorRef[ActorReceptionistMessages]] = None
  def apply(
      positions: List[Position] = List.empty,
      controller: ActorRef[BoidsControllerMessages] = null
  ): Behavior[BoidModelMessages] =
    Behaviors.setup { context =>
      if receptionist.isEmpty then
        receptionist = Option.apply(context.spawnAnonymous(BoidActorsReceptionist(context.self)))

      import BoidModelMessages.*
      Behaviors.receiveMessage { msg =>
        context.log.info(s"Model msg: $msg")

        msg match
          case UpdateBoidNumber(n) =>
            receptionist.get ! ActorReceptionistMessages.UpdateBoidNumber(n)
            apply(List.empty)
          case UpdateModel(model) =>
            receptionist.get ! RelayAll(BoidActorMessages.UpdateModel(model))
            apply(List.empty)
          case Step(ref) =>
            receptionist.get ! ActorReceptionistMessages.SendPositions
            apply(List.empty, ref)
          case Reset =>
            receptionist.get ! ActorReceptionistMessages.RelayAll(BoidActorMessages.ResetBoid)
            Behaviors.same
          case ReceivePosition(pos, size) =>
            val newPositions = pos :: positions
            if newPositions.size == size then controller ! GetData(newPositions)
            apply(positions = newPositions, controller = controller)
      }
    }
