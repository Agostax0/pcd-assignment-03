package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message
import it.unibo.agar.Utils.Anchor.SW
import it.unibo.agar.model.Entity.Player
import it.unibo.agar.view.LocalView
import it.unibo.agar.view.ObserverActor

object ClientHandlerActor {
  sealed trait Command extends Message
  case class GameJoined(player: Player) extends Command

  def apply(remoteGameMaster: ActorRef[GameMaster.Command]): Behavior[Command] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { case GameJoined(player) =>
        val localView = new LocalView(SW, player.id, remoteGameMaster)
        localView.open()

        val playerObs = ctx.spawn(ObserverActor(localView), "player-local-obs")
        remoteGameMaster ! GameMaster.RegisterObserver(playerObs)
        Behaviors.same
      }
    }

}
