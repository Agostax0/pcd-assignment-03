package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message
import it.unibo.agar.Utils.Anchor.SW
import it.unibo.agar.model.Entity.Player
import it.unibo.agar.view.LocalView
import it.unibo.agar.view.ObserverActor

object ClientHandlerActor:
  sealed trait Command extends Message
  case class GameJoined(player: Player) extends Command
  private case object AskToLeave extends Command

  def apply(remoteLobby: ActorRef[Lobby.Command], remoteGameMaster: ActorRef[GameMaster.Command]): Behavior[Command] =
    var player: Option[Player] = None

    Behaviors.setup { ctx =>
      Behaviors.receiveMessage {
        case GameJoined(p) =>
          player = Option(p)
          val localView = player match
            case Some(pl) => new LocalView(SW, pl.id, remoteGameMaster, onClose = () => ctx.self ! AskToLeave)
            case None => throw new IllegalStateException("Not initialized player")

          localView.open()
          val playerObs = ctx.spawn(ObserverActor(localView), "player-local-obs")
          remoteGameMaster ! GameMaster.RegisterObserver(playerObs)
          Behaviors.same

        case AskToLeave =>
          remoteLobby ! Lobby.LeaveRequest(playerId =
            player.map(_.id).getOrElse(throw new IllegalStateException("Player non inizializzato"))
          )
          Behaviors.stopped
      }
    }
