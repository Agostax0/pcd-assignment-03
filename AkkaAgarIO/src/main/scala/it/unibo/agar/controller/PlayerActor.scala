package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.model.Entity.World

object PlayerActor:
  sealed trait Command
  case class Compute(world: World) extends Command

  def apply(playerId: String, gameMaster: ActorRef[GameMaster.Command]): Behavior[Command] =
    Behaviors.receiveMessage { case Compute(world) =>
      for
        player <- world.playerById(playerId)
        ai <- player.aiMove
      yield gameMaster ! GameMaster.MovePlayer(playerId, ai.move(playerId, world))
      Behaviors.same
    }
