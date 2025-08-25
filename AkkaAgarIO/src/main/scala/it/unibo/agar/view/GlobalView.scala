package it.unibo.agar.view

import it.unibo.agar.Anchor.Anchor
import it.unibo.agar.model.MockGameStateManager

import java.awt.Graphics2D
import scala.swing.Dimension
import scala.swing.*

class GlobalView(manager: MockGameStateManager, anchor: Anchor) extends MainFrame:

  title = "Agar.io - Global View"
  preferredSize = new Dimension(1050, 1050)

  location = anchor.position(preferredSize)

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      val world = manager.getWorld
      AgarViewUtils.drawWorld(g, world)
