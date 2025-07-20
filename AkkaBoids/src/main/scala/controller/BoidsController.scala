package it.unibo.pcd
package controller

import BoidsControllerMessages.GetData
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import it.unibo.pcd.model.Boid.Boid
import it.unibo.pcd.model.{BoidModelActor, BoidModelMessages}
import it.unibo.pcd.view.{ActorBoidsView, BoidsViewMessages}

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
          case UpdateDimensions(width, height) =>
            // model ! BoidsModelMessages.UpdateDimensions(width, height)
            Behaviors.same
          case UpdateNumberOfBoids(n) =>
            // model ! BoidsModelMessages.UpdateNumberOfBoids(n)
            Behaviors.same
          case UpdateParameters(separationWeight, alignmentWeight, cohesionWeight) =>
            // model ! BoidsModelMessages.UpdateParameters(separationWeight, alignmentWeight, cohesionWeight)
            Behaviors.same
          case Start =>
            // model ! BoidsModelMessages.Step(context.self)
            // apply(model, view, true)
            Behaviors.same

          case Stop =>
            timer.cancel(timerKey)
            //          apply(model, view, false)
            Behaviors.same

          case Reset =>
            timer.cancel(timerKey)
//            model ! BoidsModelMessages.Reset(context.self)
//            apply(model, view, false)
            Behaviors.same

          case SetVisibleView =>
            view ! BoidsViewMessages.SetVisibleView(context.self)
            Behaviors.same
        }

object Root:
  def apply(): Behavior[Nothing] =
    Behaviors.setup: context =>
      val model: ActorRef[BoidModelMessages] = context.spawn(BoidModelActor(), "model")
      val view: ActorRef[BoidsViewMessages] = context.spawn(ActorBoidsView(), "view")
      //  val controller = context.spawn(BoidsController(model, view, false), "controller")

      //  model ! BoidsModelMessages.UpdateDimensions(800, 600)
      //  model ! BoidsModelMessages.UpdateNumberOfBoids(200)
      //  controller ! BoidsControllerMessages.SetVisibleView

      Behaviors.empty

object Prova:
  @main
  def main(): Unit =
    ActorSystem(Root(), "root")
