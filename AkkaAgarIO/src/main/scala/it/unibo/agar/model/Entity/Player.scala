package it.unibo.agar.model.Entity

import it.unibo.agar.model.AIMovement

case class Player(
    id: String,
    x: Double,
    y: Double,
    mass: Double = Player.defaultMass,
    aiMove: Option[AIMovement] = Option.empty
) extends Entity:
  import Player.*

  val score: Double = mass - defaultMass

  def grow(entity: Entity): Player =
    copy(mass = mass + entity.mass)

object Player:
  val defaultMass = 120.0
