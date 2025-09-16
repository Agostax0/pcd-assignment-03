package it.unibo.agar.controller

import it.unibo.agar.Message

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

object ClientMain extends App:
  sealed trait ConnectionMessage extends Message

  case class Initialize(remoteLobby: ActorRef[Lobby.Command], remoteGameMaster: ActorRef[GameMaster.Command])
      extends ConnectionMessage
  case class Connected(remoteLobby: ActorRef[Lobby.Command]) extends ConnectionMessage
  case class ConnectionFailed(ex: Throwable) extends ConnectionMessage

  val name = "agario-client"
  val port = 0 // use 0 for a random available port
  private val config = ConfigFactory
    .parseString(s"""akka.remote.artery.canonical.port=$port""")
    .withFallback(ConfigFactory.load(name))

  private val serverPath = "akka://agario@127.0.0.1:25251/user/lobby"

  private def start = Behaviors.setup[ConnectionMessage] { ctx =>
    import scala.concurrent.duration.*
    import ctx.executionContext
    import scala.util.{Success, Failure}
    import akka.actor.typed.scaladsl.adapter.*

    val classicSelection = ctx.system.classicSystem.actorSelection(serverPath)
    val futureRef = classicSelection.resolveOne(3.seconds)

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

  ActorSystem[ConnectionMessage](start, name, config)
