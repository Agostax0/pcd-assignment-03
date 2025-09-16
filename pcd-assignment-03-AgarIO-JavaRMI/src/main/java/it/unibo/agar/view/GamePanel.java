package it.unibo.agar.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.rmi.RemoteException;
import java.util.Optional;

import javax.swing.JPanel;

import it.unibo.agar.model.Player;
import it.unibo.agar.model.World;
import it.unibo.agar.model.remote.RemoteGameStateManager;

public class GamePanel extends JPanel {

    private final RemoteGameStateManager gameStateManager;
    private final String focusedPlayerId; // Null for global view

    public GamePanel(RemoteGameStateManager gameStateManager, String focusedPlayerId) {
        this.gameStateManager = gameStateManager;
        this.focusedPlayerId = focusedPlayerId;
        this.setFocusable(true); // Important for receiving keyboard/mouse events if needed directly
    }

    public GamePanel(RemoteGameStateManager gameStateManager) {
        this(gameStateManager, null); // Constructor for GlobalView
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        try {
            World world = gameStateManager.getWorld();
            if (focusedPlayerId != null) {
                Optional<Player> playerOpt = world.getPlayerById(focusedPlayerId);
                if (playerOpt.isPresent()) {
                    Player player = playerOpt.get();
                    final double offsetX = player.getX() - getWidth() / 2.0;
                    final double offsetY = player.getY() - getHeight() / 2.0;
                    AgarViewUtils.drawWorld(g2d, world, offsetX, offsetY);
                }
            } else {
                AgarViewUtils.drawWorld(g2d, world, 0, 0);
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
