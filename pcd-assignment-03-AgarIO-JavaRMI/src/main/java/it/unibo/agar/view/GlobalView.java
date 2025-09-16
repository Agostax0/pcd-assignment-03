package it.unibo.agar.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;

import it.unibo.agar.model.remote.RemoteGameStateManager;

public class GlobalView extends JFrame {

    private final GamePanel gamePanel;

    public GlobalView(RemoteGameStateManager gameStateManager) {
        setTitle("Agar.io - Global View (Java)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Or DISPOSE_ON_CLOSE if multiple windows
        setPreferredSize(new Dimension(800, 800));

        this.gamePanel = new GamePanel(gameStateManager);
        add(this.gamePanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    public void repaintView() {
        if (gamePanel != null) {
            gamePanel.repaint();
        }
    }
}
