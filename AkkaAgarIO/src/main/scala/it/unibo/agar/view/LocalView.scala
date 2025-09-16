package it.unibo.agar.view

import it.unibo.agar.Utils.*
import it.unibo.agar.model.Entity.Player
import it.unibo.agar.model.Entity.World
import it.unibo.agar.model.Direction

import java.awt.Graphics2D
import scala.swing.*

class LocalView(
    anchor: Anchor,
    playerId: String,
    onPlayerMove: Direction => Unit,
    onClose: () => Unit
) extends MainFrame
    with WorldUpdatable:
  title = s"Agar.io - Local View ($playerId)"
  preferredSize = new Dimension(400, 400)
  location = anchor.position(preferredSize)

  private var currentWorld: World = World(0, 0, Seq(), Seq())

  def updateWorld(world: World): Unit =
    currentWorld = world
    repaint()

  contents = new Panel:
    listenTo(keys, mouse.moves)
    focusable = true
    requestFocusInWindow()

    private def getPlayer(id: String): Option[Player] =
      currentWorld.players.find(_.id == id)

    override def paintComponent(g: Graphics2D): Unit =
      val (offsetX, offsetY) = getPlayer(playerId)
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0))
        .getOrElse((0.0, 0.0))
      AgarViewUtils.drawWorld(g, currentWorld, offsetX, offsetY)

      getPlayer(playerId).foreach { player =>
        g.setColor(java.awt.Color.BLACK)
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16))
        g.drawString(s"Score: ${player.score}", 10, 20)
      }

    reactions += { case e: event.MouseMoved =>
      val mousePos = e.point
      getPlayer(playerId) match
        case Some(Player(_, _, _, _, None)) =>
          val dx = (mousePos.x - size.width / 2) * 0.01
          val dy = (mousePos.y - size.height / 2) * 0.01
          onPlayerMove(Direction(dx, dy))
          repaint()
        case _ => ()
    }

  peer.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
  peer.addWindowListener(new java.awt.event.WindowAdapter {
    override def windowClosing(e: java.awt.event.WindowEvent): Unit = {
      onClose()
      peer.dispose()
    }
  })
