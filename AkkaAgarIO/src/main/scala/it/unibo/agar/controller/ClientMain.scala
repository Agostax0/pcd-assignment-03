package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message

object ClientMain extends App:
  sealed private trait ConnectionMessage extends Message
  private case class Connected(remoteGameMaster: ActorRef[GameMaster.Command]) extends ConnectionMessage
  private case class ConnectionFailed(ex: Throwable) extends ConnectionMessage

  private val root = Behaviors.setup[ConnectionMessage] { ctx =>
    val serverPath = "akka://agario@127.0.0.1:25251/user/game-master"
    import scala.concurrent.duration.*
    import ctx.executionContext
    import akka.actor.typed.scaladsl.adapter.*
    val classicSelection = ctx.system.classicSystem.actorSelection(serverPath)
    val futureRef = classicSelection.resolveOne(3.seconds)
    import scala.util.{Success, Failure}
    futureRef.onComplete {
      case Success(ref) =>
        ctx.self ! Connected(ref.toTyped[GameMaster.Command])
      case Failure(ex) =>
        ctx.self ! ConnectionFailed(ex)
    }

    Behaviors.receiveMessage {
      case Connected(remoteGameMaster) =>
        remoteGameMaster ! GameMaster.JoinRequest(ctx.spawn(ClientHandlerActor(remoteGameMaster), "client-handler"))
        Behaviors.same
      case ConnectionFailed(ex) =>
        println(s"Can't connect to the server: $ex")
        Behaviors.stopped
    }
  }

  ActorSystem[ConnectionMessage](root, "agario")
