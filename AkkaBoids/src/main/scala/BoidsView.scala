package it.unibo.pcd

import Boid.Boid

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.pcd.BoidsViewMessages.Render

sealed trait BoidsViewMessages
object BoidsViewMessages:
  case class Render(boids: Seq[Boid]) extends BoidsViewMessages

object BoidsView:
  def apply(): Behavior[BoidsViewMessages] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage:
        case Render(boids) =>
          context.log.info("Render")
          Behaviors.same
    }
