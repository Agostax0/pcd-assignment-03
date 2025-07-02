package it.unibo.pcd

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Behaviors.*

trait BoidActorMessages
object BoidActorMessages:
  ???
object BoidActor:
  def apply(
      receptionist: ActorRef[ActorReceptionistMessages],
      index: Int
  ): Behavior[BoidActorMessages | ActorReceptionistResponses] = Behaviors.setup(context =>
    Behaviors.receiveMessage {
      case ActorReceptionistResponses.Response(refs) => Behaviors.same
      case _ => Behaviors.same
    }
  )
