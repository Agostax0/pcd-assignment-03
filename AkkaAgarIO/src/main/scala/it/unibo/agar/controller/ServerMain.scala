package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.Utils.Anchor.SE
import it.unibo.agar.model.Entity.World
import it.unibo.agar.model.GameInitializer
import it.unibo.agar.view.GlobalView
import it.unibo.agar.view.ObserverActor

object ServerMain extends App:
  val width = 1000
  val height = 1000
  val numFoods = 100
  val foods = GameInitializer.initialFoods(numFoods, width, height)
  val world = World(width, height, Seq.empty, foods)

  val root = Behaviors.setup[GameMaster.Command] { ctx =>
    val gameMaster = ctx.spawn(GameMaster(world), "game-master")
    val lobby = ctx.spawn(Lobby(gameMaster, width, height), "lobby")

    val gv = new GlobalView(SE)
    gv.open()
    val gvObs = ctx.spawn(ObserverActor(gv), "gv-obs")
    gameMaster ! GameMaster.RegisterObserver(gvObs)

    Behaviors.empty
  }

  ActorSystem[GameMaster.Command](root, "agario")
