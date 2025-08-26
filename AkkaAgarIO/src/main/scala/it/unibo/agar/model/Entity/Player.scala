package it.unibo.agar.model.Entity

import it.unibo.agar.model.AIMovement

case class Player(id: String, x: Double, y: Double, mass: Double, aiMove: Option[AIMovement] = Option.empty)
    extends Entity:
  def grow(entity: Entity): Player =
    copy(mass = mass + entity.mass)
