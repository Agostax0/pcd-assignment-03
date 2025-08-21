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
      Behaviors.receiveMessage {
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
