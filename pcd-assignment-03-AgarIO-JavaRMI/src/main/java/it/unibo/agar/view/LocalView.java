package it.unibo.agar.view;

import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.model.Player;
import it.unibo.agar.model.RemoteGameStateManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.util.Optional;

public class LocalView extends JFrame {
    private static final double SENSITIVITY = 2;
    private GamePanel gamePanel;
    private RemoteGameStateManager gameStateManager;
    private final String playerId;

    public LocalView(RemoteGameStateManager gameStateManager, String playerId) {
        this.gameStateManager = gameStateManager;
        this.playerId = playerId;

        setTitle("Agar.io - Local View (" + playerId + ") (Java)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Dispose only this window
        setPreferredSize(new Dimension(600, 600));

        this.gamePanel = new GamePanel(gameStateManager, playerId);
        add(this.gamePanel, BorderLayout.CENTER);

        setupMouseControls();

        pack();
        setLocationRelativeTo(null); // Center on screen
    }

    public void setOnClose(Runnable onClose){
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                try{
                    onClose.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                finally {
                    System.exit(1);
                }
            }
        });
    }

    public synchronized void setRemoteGameStateManager(RemoteGameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
        this.gamePanel = new GamePanel(gameStateManager, playerId);
    }

    private void setupMouseControls() {
        gamePanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            try {
                Optional<Player> playerOpt = gameStateManager.getWorld().getPlayerById(playerId);
                if (playerOpt.isPresent()) {
                    Point mousePos = e.getPoint();
                    // Player is always in the center of the local view
                    double viewCenterX = gamePanel.getWidth() / 2.0;
                    double viewCenterY = gamePanel.getHeight() / 2.0;

                    double dx = mousePos.x - viewCenterX;
                    double dy = mousePos.y - viewCenterY;

                    // Normalize the direction vector
                    double magnitude = Math.hypot(dx, dy);
                    if (magnitude > 0) { // Avoid division by zero if mouse is exactly at center
                        gameStateManager.setPlayerDirection(playerId, (dx / magnitude) * SENSITIVITY, (dy / magnitude) * SENSITIVITY);
                    } else {
                        gameStateManager.setPlayerDirection(playerId, 0, 0); // Stop if mouse is at center
                    }
                    // Repainting is handled by the main game loop timer
                }
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }

            }
        });
    }

    public void repaintView() {
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }
}
