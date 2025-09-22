package it.unibo.agar.utils

enum NamePrefix(val label: String):
  case Player extends NamePrefix("player-")
  case Food extends NamePrefix("food-")
  case AIPlayer extends NamePrefix("ai-")

  override def toString: String = label
