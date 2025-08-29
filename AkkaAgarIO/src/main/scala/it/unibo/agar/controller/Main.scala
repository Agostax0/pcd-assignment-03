package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Utils.Anchor.*
import it.unibo.agar.model.GameInitializer
import it.unibo.agar.model.Entity.Player
import it.unibo.agar.model.Entity.World
import it.unibo.agar.view.GlobalView
import it.unibo.agar.view.LocalView
import it.unibo.agar.view.ObserverActor

import scala.swing.*

object Main extends SimpleSwingApplication:

  private val width = 1000
  private val height = 1000
  private val numPlayers = 4
  private val numAIs = 1
  private val numFoods = 100
  private val players = GameInitializer.initialPlayers(numPlayers, width, height)
  private val aiPlayers = GameInitializer.initialAIs(numAIs, width, height)
  private val foods = GameInitializer.initialFoods(numFoods, width, height)

  private val root: Behavior[Nothing] =
    Behaviors.setup[Nothing] { ctx =>
      val gameMaster = ctx.spawn(GameMaster(World(width, height, Seq.empty, foods)), "game-master")

      registerPlayers(players, ctx, gameMaster)
      registerPlayers(aiPlayers, ctx, gameMaster)

      val gv = new GlobalView(SE)
      gv.open()

      val playerAIView = new LocalView(NW, aiPlayers.head.id, gameMaster)
      playerAIView.open()
      val playerLocalView = new LocalView(SW, players.head.id, gameMaster)
      playerLocalView.open()

      val gvObs = ctx.spawn(ObserverActor(gv), "gv-obs")
      val playerAIObs = ctx.spawn(ObserverActor(playerAIView), "player-ai-obs")
      val playerLocalObs = ctx.spawn(ObserverActor(playerLocalView), "player-local-obs")

      gameMaster ! GameMaster.RegisterObserver(gvObs)
      gameMaster ! GameMaster.RegisterObserver(playerAIObs)
      gameMaster ! GameMaster.RegisterObserver(playerLocalObs)

      // scheduler tick
      import scala.concurrent.duration.*
      import ctx.executionContext
      ctx.system.scheduler.scheduleAtFixedRate(0.millis, 30.millis)(() => gameMaster ! GameMaster.Tick)

      Behaviors.same
    }

  private val system = ActorSystem[Nothing](root, "agar")

  override def top: Frame = new Frame {
    visible = false
  }

  private def registerPlayers(
      players: Seq[Player],
      ctx: akka.actor.typed.scaladsl.ActorContext[Nothing],
      gameMaster: akka.actor.typed.ActorRef[GameMaster.Command]
  ): Unit =
    players.foreach { p =>
      val ref = ctx.spawn(PlayerActor(p.id, gameMaster), p.id)
      gameMaster ! GameMaster.RegisterPlayer(p, ref)
    }
