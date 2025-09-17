package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message
import it.unibo.agar.model.Entity.World

object PlayerActor:
  sealed trait Command extends Message
  case class Compute(world: World) extends Command
  case class GameOver(winnerId: String) extends Command

  def apply(
      playerId: String,
      client: ActorRef[ClientHandlerActor.Command],
      gameMaster: ActorRef[GameMaster.Command]
  ): Behavior[Command] =
    Behaviors.receiveMessage {
      case Compute(world) =>
        for
          player <- world.playerById(playerId)
          ai <- player.aiMove
        yield gameMaster ! GameMaster.MovePlayer(playerId, ai.move(playerId, world))
        Behaviors.same

      case GameOver(winnerId) =>
        client ! ClientHandlerActor.GameOver(playerId)
        client ! ClientHandlerActor.Disconnect
        Behaviors.stopped
    }
