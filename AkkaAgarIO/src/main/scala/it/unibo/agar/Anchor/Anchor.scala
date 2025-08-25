package it.unibo.agar.Anchor

import java.awt.Toolkit
import scala.swing.{Dimension, Point}

enum Anchor:

  case NW, NE, SW, SE, CENTER

  def position(winSize: Dimension): Point =
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    this match
      case NW => new Point(0, 0)
      case NE => new Point(screenSize.width - winSize.width, 0)
      case SW => new Point(0, screenSize.height - winSize.height)
      case SE => new Point(screenSize.width - winSize.width, screenSize.height - winSize.height)
      case CENTER => new Point((screenSize.width - winSize.width) / 2, (screenSize.height - winSize.height) / 2)
