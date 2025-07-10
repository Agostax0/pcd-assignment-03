package it.unibo.pcd

import BoidsModel.LocalModel

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
  case class Reset(to: ActorRef[BoidsControllerMessages]) extends BoidsModelMessages
object ActorBoidsModel:
  def apply(
      model: LocalModel = BoidsModel.localModel
  ): Behavior[BoidsModelMessages] = receive: (context, message) =>
    import BoidsModelMessages.*

    val newModel = message match
      case UpdateDimensions(width, height) =>
        model.copy(width = width, height = height)
      case UpdateNumberOfBoids(n) =>
        ???
      case UpdateParameters(separationWeight, alignmentWeight, cohesionWeight) =>
        model.copy(
          separationWeight = separationWeight,
          alignmentWeight = alignmentWeight,
          cohesionWeight = cohesionWeight
        )
      case Step(to) =>
        ???
//        val boids = model.boids.map(_.update(model))
//        to ! BoidsControllerMessages.GetData(boids)
//        model.copy(boids = boids)
      case Reset(to) =>
        ???
//        val tmp = model.copy(boids = model.initBoids(model.boids.size))
//        val boids = tmp.boids
//        to ! BoidsControllerMessages.GetData(boids)
//        tmp
    // apply(newModel)
    Behaviors.same
