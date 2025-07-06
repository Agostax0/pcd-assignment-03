package it.unibo.pcd

import ActorReceptionistMessages.RelayAll
import Boid.Boid
import BoidActor.BoidActorMessages.{NeighborRequest, NeighborStatus, ResetBoid, UpdateModel}

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.language.postfixOps

object BoidActor:
  trait BoidActorMessages
  object BoidActorMessages:
    case class NeighborStatus(
        position: Position,
        velocity: Velocity,
        indexInQueue: Int,
        queueSize: Int
    ) extends BoidActorMessages
    case class NeighborRequest(nQueried: Int) extends BoidActorMessages
    case class UpdateModel(model: BoidsModel) extends BoidActorMessages
    case object ResetBoid extends BoidActorMessages
  def apply(
      receptionist: ActorRef[ActorReceptionistMessages],
      myIndex: Int,
      boid: Boid = Boid(Position.zero, Velocity.zero),
      neighbors: List[Boid] = List.empty,
      model: BoidsModel = BoidsModel.actor
  ): Behavior[BoidActorMessages] =
    Behaviors.receive: (context, msg) =>
      msg match
        case ResetBoid => apply(receptionist, myIndex, model.reset, List.empty, model)
        case UpdateModel(newModel) => apply(receptionist, myIndex, boid, neighbors, newModel)
        case NeighborRequest(n) =>
          receptionist ! RelayAll(NeighborStatus(boid.position, boid.velocity, myIndex, n))
          Behaviors.same
        case NeighborStatus(pos, vel, index, size) =>
          if myIndex != index && boid.position.distance(pos) < model.perceptionRadius then
            if index == size - 1 then
              apply(receptionist, myIndex, boid = model.update(boid, neighbors), neighbors, model)
            else apply(receptionist, myIndex, neighbors = neighbors :+ Boid(pos, vel))
          else Behaviors.same
        case _ => Behaviors.same
