package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.model.Player

object PlayerActor:
  sealed trait Command
  case class Compute() extends Command

  def apply(player: Player, gameMaster: ActorRef[GameMaster.Command]): Behavior[Command] =
    Behaviors.receive { (ctx, msg) =>
      msg match
        case Compute() =>
          player.aiMove match
            case Some(ai) =>
              // val (dx, dy) = ai.nextMove(player)
              // gameMaster ! GameMaster.MovePlayer(player.id, dx, dy)
              // apply(player.copy(x = dx, y = dy), gameMaster)
              Behaviors.same
            case None => Behaviors.same
    }
