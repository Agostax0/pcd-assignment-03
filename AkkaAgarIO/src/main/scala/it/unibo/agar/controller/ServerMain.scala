package it.unibo.agar.controller

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.ClusterSingleton
import akka.cluster.typed.SingletonActor
import it.unibo.agar.utils.Anchor.SE
import it.unibo.agar.model.Entity.World
import it.unibo.agar.view.GlobalView
import it.unibo.agar.view.ObserverActor

object ServerMain extends App:
  val width = 1000
  val height = 1000
  val numFoods = 100
  private val foodHandler = FoodHandler(width, height)
  private val world = World(width, height, Seq.empty, Seq.empty)

  private def start = Behaviors.setup[GameMaster.Command] { ctx =>
    val foodHandlerRef = ctx.spawn(foodHandler, "food-handler")
    val singletonManager = ClusterSingleton(ctx.system)
    val gameMaster = singletonManager.init(
      SingletonActor(GameMaster(world, foodHandlerRef, width, height), "game-master")
    )

    val lobby = ctx.spawn(Lobby(gameMaster), "lobby")

    if ctx.system.address.host.isDefined then
      val gv = new GlobalView(SE)
      gv.open()
      val gvObs = ctx.spawn(ObserverActor(gv), "gv-obs")
      gameMaster ! GameMaster.RegisterObserver(gvObs)

    Behaviors.same
  }

  ActorSystem[GameMaster.Command](start, "agario")
