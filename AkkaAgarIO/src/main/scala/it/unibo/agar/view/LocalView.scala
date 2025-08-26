package it.unibo.agar.view

import it.unibo.agar.Utils.*
import it.unibo.agar.model.World

import java.awt.Graphics2D
import scala.swing.*

class LocalView(anchor: Anchor, playerId: String) extends MainFrame with WorldUpdatable:
  title = s"Agar.io - Local View ($playerId)"
  preferredSize = new Dimension(400, 400)
  location = anchor.position(preferredSize)

  private var currentWorld: World = World(0, 0, Seq(), Seq())

  def updateWorld(world: World): Unit =
    currentWorld = world
    repaint()

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      val playerOpt = currentWorld.players.find(_.id == playerId)
      val (offsetX, offsetY) = playerOpt
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0))
        .getOrElse((0.0, 0.0))
      AgarViewUtils.drawWorld(g, currentWorld, offsetX, offsetY)
