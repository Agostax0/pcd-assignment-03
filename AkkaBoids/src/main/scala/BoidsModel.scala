package it.unibo.pcd

import Boid.Boid
import BoidsModelMessages.*

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object BoidsModel:
  def actor: ActorBoidsModel = BoidsModel.ActorBoidsModel()

  case class LocalBoidsModel(
      boids: Seq[Boid] = List.empty,
      separationWeight: Double = 1.0,
      alignmentWeight: Double = 1.0,
      cohesionWeight: Double = 1.0,
      width: Double = 100,
      height: Double = 100,
      maxSpeed: Double = 4.0,
      perceptionRadius: Double = 50.0,
      avoidRadius: Double = 20.0
  ):

    val minX: Double = -width / 2
    val maxX: Double = width / 2
    val minY: Double = -height / 2
    val maxY: Double = height / 2

    def initBoids(n: Int): LocalBoidsModel =
      val random = scala.util.Random
      copy(boids = for
        _ <- 0 until n
        pos = Position(random.between(minX, maxX), random.between(minY, maxY))
        vel = Velocity(random.between(0, maxSpeed / 2), random.between(0, maxSpeed / 2)) - Velocity(
          maxSpeed / 4,
          maxSpeed / 4
        )
      yield Boid(pos, vel))

  case class ActorBoidsModel():
    def apply(
        model: LocalBoidsModel = LocalBoidsModel(),
        isRunning: Boolean = false
    ): Behavior[BoidsModelMessages] = Behaviors.receive: (context, message) =>
      message match
        case UpdateDimensions(width, height) =>
          val newModel = model.copy(width = width, height = height)
          apply(newModel, isRunning)
        case UpdateSeparationWeight(weight) =>
          val newModel = model.copy(separationWeight = weight)
          apply(newModel, isRunning)
        case UpdateAlignmentWeight(weight) =>
          val newModel = model.copy(alignmentWeight = weight)
          apply(newModel, isRunning)
        case UpdateCohesionWeight(weight) =>
          val newModel = model.copy(cohesionWeight = weight)
          apply(newModel, isRunning)
        case StartSimulation() =>
          context.log.info("Starting simulation...")
          apply(model, true)
        case StopSimulation() =>
          context.log.info("Stopping simulation...")
          apply(model)
        case ResetSimulation() =>
          context.log.info("Resetting simulation...")
          val newModel = model.initBoids(model.boids.size)
          apply(newModel)

      Behaviors.same

enum BoidsModelMessages:
  case UpdateDimensions(width: Double, height: Double)
  case UpdateSeparationWeight(weight: Double)
  case UpdateAlignmentWeight(weight: Double)
  case UpdateCohesionWeight(weight: Double)
  case StartSimulation()
  case StopSimulation()
  case ResetSimulation()
