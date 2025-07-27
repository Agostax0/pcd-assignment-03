package it.unibo.pcd
package controller

import BoidsControllerMessages.{GetData, UpdateNumberOfBoids}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import it.unibo.pcd.model.Boid.Boid
import it.unibo.pcd.model.{BoidModelActor, BoidModelMessages, BoidsModel}
import it.unibo.pcd.view.BoidsViewMessages.UpdateModel
import it.unibo.pcd.view.{ActorBoidsView, BoidsViewMessages}

import scala.concurrent.duration.DurationInt

sealed trait BoidsControllerMessages
object BoidsControllerMessages:
  case class GetData(boids: Seq[Boid]) extends BoidsControllerMessages
  case class UpdateNumberOfBoids(n: Int) extends BoidsControllerMessages
  case class UpdateModel(model: BoidsModel) extends BoidsControllerMessages
  case object Start extends BoidsControllerMessages
  case object Stop extends BoidsControllerMessages
  case object Reset extends BoidsControllerMessages
  case object SetVisibleView extends BoidsControllerMessages

object BoidsController:
  def apply(
      model: ActorRef[BoidModelMessages],
      view: ActorRef[BoidsViewMessages],
      isRunning: Boolean = false
  ): Behavior[BoidsControllerMessages] =
    Behaviors.setup: context =>
      Behaviors.withTimers: timer =>
        val timerKey = "updateTimer"

        import BoidsControllerMessages.*
        Behaviors.receiveMessage {
          case GetData(boids) =>
            view ! BoidsViewMessages.Render(boids)
            if isRunning then
              timer.startSingleTimer(
                timerKey,
                Start,
                300.millis
              )
            Behaviors.same
          case UpdateNumberOfBoids(n) =>
            model ! BoidModelMessages.UpdateBoidNumber(n)
            Behaviors.same

          case BoidsControllerMessages.UpdateModel(newModel) =>
            model ! BoidModelMessages.UpdateModel(newModel)
            Behaviors.same

          case Start =>
            model ! BoidModelMessages.Step
            apply(model, view, true)

          case Stop =>
            timer.cancel(timerKey)
            apply(model, view, false)
            Behaviors.same

          case Reset =>
            timer.cancel(timerKey)
            model ! BoidModelMessages.Reset
            apply(model, view, false)

          case SetVisibleView =>
            view ! BoidsViewMessages.SetVisibleView(context.self)
            Behaviors.same
        }

object Root:
  def apply(): Behavior[Nothing] =
    Behaviors.setup: context =>
      val model: ActorRef[BoidModelMessages] = context.spawn(BoidModelActor(), "model")
      val view: ActorRef[BoidsViewMessages] = context.spawn(ActorBoidsView(), "view")
      val controller: ActorRef[BoidsControllerMessages] = context.spawn(BoidsController(model, view), "controller")

      model ! BoidModelMessages.UpdateBoidNumber(200)
      model ! BoidModelMessages.UpdateModel(BoidsModel.localModel.copy(width = 800, height = 600))
      controller ! BoidsControllerMessages.SetVisibleView

      Behaviors.empty

object Prova:
  @main
  def main(): Unit =
    ActorSystem(Root(), "root")
