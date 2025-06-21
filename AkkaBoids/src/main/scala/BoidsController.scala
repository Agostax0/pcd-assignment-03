package it.unibo.pcd

import Boid.Boid
import BoidsControllerMessages.GetData

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.concurrent.duration.DurationInt

sealed trait BoidsControllerMessages
object BoidsControllerMessages:
  case class GetData(boids: Seq[Boid]) extends BoidsControllerMessages
  case class UpdateDimensions(width: Double, height: Double) extends BoidsControllerMessages
  case class UpdateNumberOfBoids(n: Int) extends BoidsControllerMessages
  case class UpdateParameters(separationWeight: Double, alignmentWeight: Double, cohesionWeight: Double)
      extends BoidsControllerMessages
  case object Start extends BoidsControllerMessages
  case object Stop extends BoidsControllerMessages
  case object Reset extends BoidsControllerMessages
object BoidsController:
  def apply(
      model: ActorRef[BoidsModelMessages],
      view: ActorRef[BoidsViewMessages],
      isRunning: Boolean = false
  ): Behavior[BoidsControllerMessages] =
    Behaviors.setup: context =>
      Behaviors.withTimers: timer =>
        import BoidsControllerMessages.*
        Behaviors.receiveMessage {
          case GetData(boids) =>
            view ! BoidsViewMessages.Render(boids, context.self)
            if isRunning then
              timer.startSingleTimer(
                Start,
                300.millis
              )
            Behaviors.same
          case UpdateDimensions(width, height) =>
            model ! BoidsModelMessages.UpdateDimensions(width, height)
            Behaviors.same
          case BoidsControllerMessages.UpdateNumberOfBoids(n) =>
            model ! BoidsModelMessages.UpdateNumberOfBoids(n)
            Behaviors.same
          case BoidsControllerMessages.UpdateParameters(separationWeight, alignmentWeight, cohesionWeight) =>
            model ! BoidsModelMessages.UpdateParameters(separationWeight, alignmentWeight, cohesionWeight)
            Behaviors.same
          case Start =>
            context.log.info("Start")
            model ! BoidsModelMessages.Step(context.self)
            apply(model, view, true)
          case Stop =>
            context.log.info("Stop")
            apply(model, view, false)
          case Reset =>
            context.log.info("Reset")
            model ! BoidsModelMessages.Reset
            Behaviors.same
        }
object Root:
  def apply(): Behavior[Nothing] =
    Behaviors.setup: context =>
      val model: ActorRef[BoidsModelMessages] = context.spawn(ActorBoidsModel(), "model")
      val view: ActorRef[BoidsViewMessages] = context.spawn(ActorBoidsView(), "view")
      val controller = context.spawn(BoidsController(model, view, false), "controller")
      Behaviors.empty

object Prova:
  @main
  def main(): Unit =
    println("start")
    ActorSystem(Root(), "root")
