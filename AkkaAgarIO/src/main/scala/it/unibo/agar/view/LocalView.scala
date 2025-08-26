package it.unibo.agar.view

import akka.actor.typed.ActorRef
import it.unibo.agar.Utils.*
import it.unibo.agar.model.AIMovement
import it.unibo.agar.model.Player
import it.unibo.agar.model.World
import it.unibo.agar.controller.GameMaster

import java.awt.Graphics2D
import scala.swing.*

class LocalView(
    anchor: Anchor,
    playerId: String,
    movePlayerCommand: ActorRef[GameMaster.MovePlayer]
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

    override def paintComponent(g: Graphics2D): Unit =
      val world = currentWorld
      val playerOpt = world.players.find(_.id == playerId)
      val (offsetX, offsetY) = playerOpt
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0))
        .getOrElse((0.0, 0.0))
      AgarViewUtils.drawWorld(g, world, offsetX, offsetY)

    reactions += { case e: event.MouseMoved =>
      val mousePos = e.point
      currentWorld.players.find(_.id == playerId) match
        case Some(Player(_, _, _, _, None)) =>
          val dx = (mousePos.x - size.width / 2) * 0.01
          val dy = (mousePos.y - size.height / 2) * 0.01
          movePlayerCommand ! GameMaster.MovePlayer(playerId, dx, dy)
          repaint()
        case _ => ()
    }
