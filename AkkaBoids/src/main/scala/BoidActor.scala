package it.unibo.pcd

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Behaviors.*

sealed trait BoidActorMessages
object BoidActorMessages:
  ???
object BoidActor:
  def apply(
      receptionist: ActorRef[ActorReceptionistMessages],
      index: Int
  ): Behavior[BoidActorMessages] = Behaviors.setup(context =>

    receptionist ! ActorReceptionistMessages.Register(index.toString, context.self)

    Behaviors.same
  )