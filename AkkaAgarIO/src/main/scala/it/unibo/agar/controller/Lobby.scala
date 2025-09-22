package it.unibo.agar.controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import it.unibo.agar.Message

object Lobby:
  sealed trait Command extends Message

  case class Connect(replyTo: ActorRef[ClientMain.ConnectionMessage]) extends Command
  case class JoinRequest(replyTo: ActorRef[ClientHandlerActor.Command]) extends Command
  case class LeaveRequest(playerId: String) extends Command

  def apply(gameMaster: ActorRef[GameMaster.Command]): Behavior[Command] =
    Behaviors.receive { (ctx, msg) =>
      msg match
        case Connect(replyTo) =>
          replyTo ! ClientMain.Initialize(ctx.self, gameMaster)
          Behaviors.same

        case JoinRequest(replyTo) =>
          gameMaster ! GameMaster.RegisterPlayer(replyTo)
          Behaviors.same

        case LeaveRequest(playerId) =>
          ctx.log.info(s"Leave request received for player $playerId")
          gameMaster ! GameMaster.UnregisterPlayer(playerId)
          Behaviors.same
    }
