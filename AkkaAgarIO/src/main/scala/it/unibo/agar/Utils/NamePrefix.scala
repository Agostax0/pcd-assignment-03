package it.unibo.agar.Utils

enum NamePrefix(val label: String):
  case Player extends NamePrefix("player-")
  case Food extends NamePrefix("food-")

  override def toString: String = label
