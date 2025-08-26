package it.unibo.agar.view

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.model.Entity.World

import java.awt.Window
import scala.swing.Swing.onEDT

trait WorldUpdatable:
  def updateWorld(world: World): Unit

object ObserverActor:
  def apply(view: WorldUpdatable): Behavior[World] =
    Behaviors.receive { (ctx, world) =>
      onEDT {
        view.updateWorld(world)
        Window.getWindows.foreach(_.repaint())
      }
      Behaviors.same
    }
