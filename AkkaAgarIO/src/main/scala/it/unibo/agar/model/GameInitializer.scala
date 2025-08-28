package it.unibo.agar.model

import it.unibo.agar.Utils.NamePrefix
import it.unibo.agar.model.Entity.Food
import it.unibo.agar.model.Entity.Player

import scala.util.Random

object GameInitializer:
  private val initialPlayerMass = 120.0
  private val initialFoodMass = 100.0
  
  def spawnPlayer(id: String, width: Int, height: Int, initialMass: Double = initialPlayerMass): Player =
    Player(id, Random.nextInt(width), Random.nextInt(height), initialMass)

  def initialPlayers(numPlayers: Int, width: Int, height: Int, initialMass: Double = initialPlayerMass): Seq[Player] =
    (1 to numPlayers).map[Player](i =>
      Player(NamePrefix.Player.toString + i, Random.nextInt(width), Random.nextInt(height), initialMass)
    )

  def initialAIs(numAIs: Int, width: Int, height: Int, initialMass: Double = initialPlayerMass): Seq[Player] =
    (1 to numAIs).map[Player](i =>
      Player(
        NamePrefix.AIPlayer.toString + i,
        Random.nextInt(width),
        Random.nextInt(height),
        initialMass,
        Option(NearestFoodAI)
      )
    )

  def initialFoods(numFoods: Int, width: Int, height: Int, initialMass: Double = initialFoodMass): Seq[Food] =
    (1 to numFoods).map[Food](i =>
      Food(NamePrefix.Food.toString + i, Random.nextInt(width), Random.nextInt(height), initialMass)
    )
