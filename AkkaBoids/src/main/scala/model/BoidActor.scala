package it.unibo.pcd
package model

import ActorReceptionistMessages.{RelayAll, RelayTo, Unregister}
import Boid.Boid
import BoidActor.BoidActorMessages.{NeighborRequest, NeighborStatus, ResetBoid, SendPosition, StopBoid, UpdateModel}

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.pcd.model.BoidModelMessages.ReceivePosition

import scala.language.postfixOps

object BoidActor:
  trait BoidActorMessages
  object BoidActorMessages:
    case object SendPosition extends BoidActorMessages
    case class NeighborStatus(
        position: Position,
        velocity: Velocity,
        indexInQueue: Int,
        queueSize: Int
    ) extends BoidActorMessages
    case class NeighborRequest(nQueried: Int) extends BoidActorMessages
    case class UpdateModel(model: BoidsModel) extends BoidActorMessages
    case object ResetBoid extends BoidActorMessages
    case object StopBoid extends BoidActorMessages
  def apply(
      receptionist: ActorRef[ActorReceptionistMessages],
      myIndex: Int,
      boid: Boid = Boid(Position.zero, Velocity.zero),
      neighbors: List[Boid] = List.empty,
      model: BoidsModel = BoidsModel.localModel
  ): Behavior[BoidActorMessages] =
    Behaviors.receive: (context, msg) =>
      msg match
        case SendPosition =>
          receptionist ! RelayTo("", ReceivePosition(boid.position, -1))
          Behaviors.same
        case ResetBoid => apply(receptionist, myIndex, model.reset, List.empty, model)
        case StopBoid =>
          receptionist ! Unregister(myIndex.toString)
          Behaviors.stopped
        case UpdateModel(newModel) => apply(receptionist, myIndex, boid, List.empty, newModel)
        case NeighborRequest(n) =>
          receptionist ! RelayAll(NeighborStatus(boid.position, boid.velocity, myIndex, n))
          Behaviors.same
        case NeighborStatus(pos, vel, index, size) =>
          if myIndex != index && boid.position.distance(pos) < model.perceptionRadius then
            if index == size - 1 then
              apply(receptionist, myIndex, boid = model.update(boid, neighbors), List.empty, model)
            else apply(receptionist, myIndex, neighbors = neighbors :+ Boid(pos, vel))
          else Behaviors.same
        case _ => Behaviors.same
