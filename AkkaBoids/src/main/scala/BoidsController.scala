package it.unibo.pcd

import it.unibo.pcd.Boid.Boid
import BoidsControllerMessages.GetData
import it.unibo.pcd.BoidsModelMessages.{Step, UpdateNumberOfBoids}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import it.unibo.pcd.{ActorBoidsModel, BoidsModelMessages, BoidsView, BoidsViewMessages}
import scala.concurrent.duration.DurationInt

sealed trait BoidsControllerMessages
object BoidsControllerMessages:
  case class GetData(boids: Seq[Boid]) extends BoidsControllerMessages
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
            view ! BoidsViewMessages.Render(boids)

            if isRunning then
              timer.startSingleTimer(
                Start,
                300.millis
              )

            Behaviors.same
          case Start =>
            context.log.info("Start")
            model ! Step(context.self)
            apply(model, view, true)
          case Stop =>
            apply(model, view, false)
          case Reset =>
            model ! BoidsModelMessages.Reset
            Behaviors.same
        }
object Root:
  def apply(): Behavior[Nothing] =
    Behaviors.setup: context =>
      val model: ActorRef[BoidsModelMessages] = context.spawn(ActorBoidsModel(), "model")
      val view: ActorRef[BoidsViewMessages] = context.spawn(BoidsView(), "view")

      val controller = context.spawn(BoidsController(model, view, true), "controller")
      import BoidsControllerMessages.*
      controller ! Start
      Behaviors.empty

object Prova:
  @main
  def main(): Unit =
    println("start")
    ActorSystem(Root(), "root")
