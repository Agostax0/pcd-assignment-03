package it.unibo.agar.model

import it.unibo.agar.model.Entity.Food
import it.unibo.agar.model.Entity.World

case class Direction(x: Double, y: Double)

trait AIMovement:
  /** Moves the AI player in the world
    *
    * @param name
    *   the ID of the AI player to move
    * @param world
    *   the current game world containing players and food
    */
  def move(name: String, world: World): Direction

/** A very simple AI that moves the player to the right */
object StupidAI extends AIMovement:
  override def move(name: String, world: World): Direction =
    world.playerById(name) match
      case Some(_) =>
        Direction(1, 0)
      case None =>
        Direction(0, 0)

object NearestFoodAI extends AIMovement:
  /** Finds the nearest food for a given player in the world
    *
    * @param player
    *   the ID of the player for whom to find the nearest food
    * @param world
    *   the current game world containing players and food
    * @return
    */
  private def nearestFood(player: String, world: World): Option[Food] =
    world.foods
      .sortBy(food => world.playerById(player).map(p => p.distanceTo(food)).getOrElse(Double.MaxValue))
      .headOption

  override def move(name: String, world: World): Direction =
    val aiOpt = world.playerById(name)
    val foodOpt = nearestFood(name, world)
    (aiOpt, foodOpt) match
      case (Some(ai), Some(food)) =>
        val dx = food.x - ai.x
        val dy = food.y - ai.y
        val distance = math.hypot(dx, dy)
        if (distance > 0)
          Direction(dx / distance, dy / distance)
        else
          Direction(0, 0)
      case _ => Direction(0, 0)
