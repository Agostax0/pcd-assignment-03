package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message

object ClientMain extends App:
  sealed trait ConnectionMessage extends Message
  case class Connected(remoteLobby: ActorRef[Lobby.Command]) extends ConnectionMessage
  case class ConnectionFailed(ex: Throwable) extends ConnectionMessage
  case class Initialize(remoteLobby: ActorRef[Lobby.Command], remoteGameMaster: ActorRef[GameMaster.Command])
      extends ConnectionMessage

  private val root = Behaviors.setup[ConnectionMessage] { ctx =>
    val serverPath = "akka://agario@127.0.0.1:25251/user/lobby"
    import scala.concurrent.duration.*
    import ctx.executionContext
    import akka.actor.typed.scaladsl.adapter.*
    val classicSelection = ctx.system.classicSystem.actorSelection(serverPath)
    val futureRef = classicSelection.resolveOne(3.seconds)
    import scala.util.{Success, Failure}
    futureRef.onComplete {
      case Success(ref) =>
        ctx.self ! Connected(ref.toTyped[Lobby.Command])
      case Failure(ex) =>
        ctx.self ! ConnectionFailed(ex)
    }

    Behaviors.receiveMessage {
      case Connected(remoteLobby) =>
        remoteLobby ! Lobby.Connect(ctx.self)
        Behaviors.same

      case ConnectionFailed(ex) =>
        println(s"Can't connect to the server: $ex")
        Behaviors.stopped

      case Initialize(remoteLobby, remoteGameMaster) =>
        val clientHandler = ctx.spawn(ClientHandlerActor(remoteGameMaster), "client-handler")
        remoteLobby ! Lobby.JoinRequest(clientHandler)
        Behaviors.same
    }
  }

  ActorSystem[ConnectionMessage](root, "agario")
