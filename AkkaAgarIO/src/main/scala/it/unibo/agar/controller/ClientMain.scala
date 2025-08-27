package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Message
import it.unibo.agar.Utils.Anchor.SW
import it.unibo.agar.model.GameInitializer
import it.unibo.agar.view.LocalView
import it.unibo.agar.view.ObserverActor

object ClientMain extends App:
  val width = 1000
  val height = 1000
  val player = GameInitializer.initialPlayers(1, width, height).head

  sealed trait ConnectionMessage extends Message
  case class Connected(remoteGameMaster: ActorRef[GameMaster.Command]) extends ConnectionMessage
  case class ConnectionFailed(ex: Throwable) extends ConnectionMessage

  val root = Behaviors.setup[ConnectionMessage] { ctx =>
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
        val localView = new LocalView(SW, player.id, remoteGameMaster)
        localView.open()
        val playerRef = ctx.spawn(PlayerActor(player.id, remoteGameMaster), player.id)
        remoteGameMaster ! GameMaster.RegisterPlayer(player, playerRef)
        val playerObs = ctx.spawn(ObserverActor(localView), "player-local-obs")
        remoteGameMaster ! GameMaster.RegisterObserver(playerObs)
        Behaviors.same
      case ConnectionFailed(ex) =>
        println(s"Can't connect to the server: $ex")
        Behaviors.stopped
    }
  }

  ActorSystem[ConnectionMessage](root, "agario")
