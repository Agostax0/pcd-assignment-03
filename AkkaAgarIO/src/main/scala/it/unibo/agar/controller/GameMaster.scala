package it.unibo.agar.controller

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.model.Direction
import it.unibo.agar.model.EatingManager
import it.unibo.agar.model.Entity.Player
import it.unibo.agar.model.Entity.World

object GameMaster:
  sealed trait Command
  case object Tick extends Command
  case class RegisterPlayer(player: Player, ref: ActorRef[PlayerActor.Command]) extends Command
  case class MovePlayer(id: String, dir: Direction) extends Command
  case class RegisterObserver(observer: ActorRef[World]) extends Command

  def apply(initialWorld: World): Behavior[Command] =
    Behaviors.setup { ctx =>
      def loop(
          world: World,
          directions: Map[String, Direction],
          observers: Set[ActorRef[World]],
          playerRefs: Map[String, ActorRef[PlayerActor.Command]]
      ): Behavior[Command] =
        Behaviors.receiveMessage {
          case Tick =>
            playerRefs.values.foreach(_ ! PlayerActor.Compute(world))

            val newWorld = directions.foldLeft(world) { case (w, (id, dir)) =>
              w.playerById(id) match
                case Some(player) =>
                  val updatedPlayer = updatePlayerPosition(w, player, dir.x, dir.y)
                  updateWorldAfterMovement(w, updatedPlayer)
                case None => w
            }
            observers.foreach(_ ! newWorld)
            loop(newWorld, directions, observers, playerRefs)

          case MovePlayer(id, dir: Direction) =>
            loop(world, directions.updated(id, dir), observers, playerRefs)

          case RegisterPlayer(player, ref) =>
            loop(world.copy(players = world.players :+ player), directions, observers, playerRefs + (player.id -> ref))

          case RegisterObserver(observer) =>
            observer ! world
            loop(world, directions, observers + observer, playerRefs)
        }

      loop(initialWorld, Map.empty, Set.empty, Map.empty)
    }

  private val speed = 10.0

  private def updatePlayerPosition(world: World, player: Player, dx: Double, dy: Double): Player =
    val newX = (player.x + dx * speed).max(0).min(world.width)
    val newY = (player.y + dy * speed).max(0).min(world.height)
    player.copy(x = newX, y = newY)

  private def updateWorldAfterMovement(world: World, player: Player): World =
    val foodEaten = world.foods.filter(food => EatingManager.canEatFood(player, food))
    val playerEatsFood = foodEaten.foldLeft(player)((p, food) => p.grow(food))
    val playersEaten = world
      .playersExcludingSelf(player)
      .filter(other => EatingManager.canEatPlayer(playerEatsFood, other))
    val playerEatPlayers = playersEaten.foldLeft(playerEatsFood)((p, other) => p.grow(other))
    world
      .updatePlayer(playerEatPlayers)
      .removePlayers(playersEaten)
      .removeFoods(foodEaten)
