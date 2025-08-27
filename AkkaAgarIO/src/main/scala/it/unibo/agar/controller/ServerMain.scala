package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.model.Entity.World
import it.unibo.agar.view.GlobalView
import it.unibo.agar.view.ObserverActor

object ServerMain extends App:
  val width = 1000
  val height = 1000
  val numFoods = 100
  val foods = it.unibo.agar.model.GameInitializer.initialFoods(numFoods, width, height)
  val world = World(width, height, Seq.empty, foods)

  val root = Behaviors.setup[Nothing] { ctx =>
    val gameMaster = ctx.spawn(GameMaster(world), "game-master")
    val gv = new GlobalView(it.unibo.agar.Utils.Anchor.SE)
    gv.open()
    val gvObs = ctx.spawn(ObserverActor(gv), "gv-obs")
    gameMaster ! GameMaster.RegisterObserver(gvObs)
    import scala.concurrent.duration.*
    import ctx.executionContext
    ctx.system.scheduler.scheduleAtFixedRate(0.millis, 30.millis)(() => gameMaster ! GameMaster.Tick)
    Behaviors.empty
  }

  ActorSystem[Nothing](root, "agario")
