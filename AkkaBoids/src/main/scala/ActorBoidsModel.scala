package it.unibo.pcd

import BoidsModel.LocalBoidsModel
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Behaviors.*

sealed trait BoidsModelMessages
object BoidsModelMessages:
  case class UpdateDimensions(width: Double, height: Double) extends BoidsModelMessages
  case class UpdateNumberOfBoids(n: Int) extends BoidsModelMessages
  case class UpdateParameters(separationWeight: Double, alignmentWeight: Double, cohesionWeight: Double)
      extends BoidsModelMessages
  case class Step(to: ActorRef[BoidsControllerMessages]) extends BoidsModelMessages
  case object Reset extends BoidsModelMessages
object ActorBoidsModel:
  def apply(
        model: LocalBoidsModel = LocalBoidsModel()
    ): Behavior[BoidsModelMessages] = receive: (context, message) =>
      import BoidsModelMessages.*

      val newModel = message match
        case UpdateDimensions(width, height) =>
          model.copy(width = width, height = height)
        case UpdateNumberOfBoids(n) =>
          model.copy(boids = model.initBoids(n))
        case UpdateParameters(separationWeight, alignmentWeight, cohesionWeight) =>
          model.copy(
            separationWeight = separationWeight,
            alignmentWeight = alignmentWeight,
            cohesionWeight = cohesionWeight
          )
        case Step(to) =>
          context.log.info("Step")
          model.copy(boids = model.boids.map(_.update(model)))
          to ! BoidsControllerMessages.GetData(model.boids)
          model

      apply(newModel)
