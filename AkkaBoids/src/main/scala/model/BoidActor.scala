package it.unibo.pcd
package model

import ActorReceptionistMessages.{RelayAll, RelayTo, Unregister}
import Boid.Boid
import BoidActor.BoidActorMessages.{NeighborRequest, NeighborStatus, ResetBoid, SendPosition, StopBoid, UpdateModel}
import akka.actor.typed.javadsl.ActorContext
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
      otherBoidsSeen: Int = 0,
      neighbors: List[Boid] = List.empty,
      model: BoidsModel = BoidsModel.localModel
  ): Behavior[BoidActorMessages] =
    Behaviors.receive: (context, msg) =>
      val parameters = BoidParameters(myIndex, otherBoidsSeen)
      msg match
        case SendPosition =>
          receptionist ! RelayTo("model", ReceivePosition(boid.position, -1))
          Behaviors.same
        case ResetBoid => apply(receptionist, myIndex, model.reset, model = model)
        case StopBoid =>
          receptionist ! Unregister(myIndex.toString)
          Behaviors.stopped
        case UpdateModel(newModel) => apply(receptionist, myIndex, boid, model = newModel)
        case NeighborRequest(n) =>
          receptionist ! RelayAll(NeighborStatus(boid.position, boid.velocity, myIndex, n))
          Behaviors.same
        case NeighborStatus(pos, vel, index, size) =>
          if myIndex == index then Behaviors.same
          else
            var newNeighbors = neighbors
            val newOtherBoidsSeen: Int = otherBoidsSeen + 1

            if newOtherBoidsSeen == size - 1 then
              context.log.info(s"Boid $myIndex has seen all other boids")
              val updatedBoid = model.update(boid, neighbors)
              receptionist ! RelayTo("model", ReceivePosition(updatedBoid.position, -1))
              apply(receptionist, myIndex, boid = updatedBoid, neighbors = List.empty, model = model)
            else
              if boid.position.distance(pos) < model.perceptionRadius then newNeighbors = newNeighbors :+ Boid(pos, vel)
              apply(
                receptionist, myIndex, boid, otherBoidsSeen = newOtherBoidsSeen, neighbors = newNeighbors, model = model
              )
        case _ => Behaviors.same

  private case class BoidParameters(myIndex: Int, /*boid: Boid*/ neighborsSeen: Int)
