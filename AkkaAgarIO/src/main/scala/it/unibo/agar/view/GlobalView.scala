package it.unibo.agar.view

import it.unibo.agar.utils.Anchor
import it.unibo.agar.model.Entity.World

import java.awt.Graphics2D
import scala.swing.Dimension
import scala.swing.*

class GlobalView(anchor: Anchor) extends MainFrame with WorldUpdatable:
  title = "Agar.io - Global View"
  preferredSize = new Dimension(1050, 1100)
  location = anchor.position(preferredSize)

  private var currentWorld: World = World(0, 0, Seq(), Seq())

  def updateWorld(world: World): Unit =
    currentWorld = world
    repaint()

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      AgarViewUtils.drawWorld(g, currentWorld)
