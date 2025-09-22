package it.unibo.agar.controller

import it.unibo.agar.Message
import it.unibo.agar.model.GameInitializer
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors

object FoodHandler:
  sealed trait Command extends Message
  case class GenerateFood(replayTo: ActorRef[GameMaster.SpawnFood]) extends Command

  def apply(width: Int, height: Int): Behavior[Command] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessage { case GenerateFood(replayTo) =>
        ctx.log.info("Generating food...")
        replayTo ! GameMaster.SpawnFood(
          GameInitializer.initialFoods(1, width, height).head
        )
        Behaviors.same
      }
    }
