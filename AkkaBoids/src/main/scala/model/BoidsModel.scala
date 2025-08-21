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
    val pos = Position(random.between(minX, width), random.between(minY, height))
    val vel = Velocity(random.between(-maxSpeed / 4, maxSpeed / 4), random.between(-maxSpeed / 4, maxSpeed / 4))
    Boid(pos, vel)

  def update(boid: Boid, neighbors: List[Boid]): Boid =
    val separationValue = separation(boid, neighbors) * separationWeight
    val cohesionValue = cohesion(boid, neighbors) * cohesionWeight
    val alignmentValue = alignment(boid, neighbors) * alignmentWeight

    var vel = separationValue + cohesionValue + alignmentValue
    val speed = vel.abs

    if speed > maxSpeed then vel = vel.normalized * maxSpeed

    var pos =
      if false & vel == Velocity.zero then boid.position + Position(Velocity.random.x, Velocity.random.y)
      else boid.position + Position(vel.x, vel.y)

    if pos.x < minX then pos = pos + Position(width / 2, 0)
    if pos.x >= maxX then pos = pos - Position(width / 2, 0)
    if pos.y < minY then pos = pos + Position(0, height / 2)
    if pos.y >= maxY then pos = pos - Position(0, height / 2)

    Boid(pos, vel)

  private def alignment(boid: Boid, neighbors: List[Boid]): Velocity =
//    val velocities =
//      for neighbor <- neighbors
//      yield neighbor.velocity
//    if velocities.isEmpty then Velocity.zero
//    else ((velocities.foldRight(Velocity.zero)(_ + _) / velocities.size) - boid.velocity).normalized

    var avgVx = 0.0
    var avgVy = 0.0

    if neighbors.isEmpty then Velocity.zero
    else
      for otherBoid <- neighbors
      do
        avgVx = avgVx + otherBoid.velocity.x
        avgVy = avgVy + otherBoid.velocity.y

      avgVx = avgVx / neighbors.size
      avgVy = avgVy / neighbors.size

      Velocity(avgVx - boid.velocity.x, avgVy - boid.velocity.y).normalized

  private def cohesion(boid: Boid, neighbors: List[Boid]): Velocity =
//    val positions =
//      for neighbor <- neighbors
//      yield neighbor.position
//    if positions.isEmpty then Velocity.zero
//    else
//      val center = (positions.foldRight(Position.zero)(_ + _) / positions.size) - boid.position
//      Velocity(center.x, center.y).normalized

    var centerX = 0.0
    var centerY = 0.0

    if neighbors.isEmpty then Velocity.zero
    else
      for otherBoid <- neighbors
      do
        centerX = centerX + otherBoid.position.x
        centerY = centerY + otherBoid.position.y

      centerX = centerX / neighbors.size
      centerY = centerY / neighbors.size

      Velocity(centerX - boid.position.x, centerY - boid.position.y).normalized

  private def separation(boid: Boid, neighbors: List[Boid]): Velocity =
//    val positions = for
//      neighbor <- neighbors
//      if neighbor.position.distance(boid.position) < avoidRadius
//      pos = boid.position - neighbor.position
//    yield pos
//    if positions.isEmpty then Velocity.zero
//    else
//      val sum = positions.foldRight(Position.zero)(_ + _)
//      Velocity(sum.x / positions.size, sum.y / positions.size)

    var dx = 0.0
    var dy = 0.0
    val nearby = neighbors.filter(n => n.position.distance(boid.position) < avoidRadius)

    if nearby.isEmpty then Velocity.zero
    else
      for otherBoid <- nearby
      do
        dx = dx + boid.position.x - otherBoid.position.x
        dy = dy + boid.position.y - otherBoid.position.y

      dx = dx / nearby.size
      dy = dy / nearby.size

      Velocity(dx, dy).normalized
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
        // context.log.info(s"Model msg: $msg")

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
