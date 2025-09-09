package it.unibo.agar;

import it.unibo.agar.model.*;
import it.unibo.agar.view.JFrameRepaintable;
import it.unibo.agar.view.LocalView;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Timer;
import java.util.TimerTask;

import static it.unibo.agar.RunServerSide.GAME_TICK_MS;

public class RunClientSide {

    public static final int DEFAULT_PORT = 2001;

    public static void main(String[] args){

        try {
            var registry = LocateRegistry.getRegistry(RunServerSide.DEFAULT_PORT);

            var manager = (RemoteGameStateManager) registry.lookup(RunServerSide.GAME_STATE_MANAGER_BINDING);

            LocalView localView = new LocalView(manager, "p2");
            localView.setVisible(true);

            final java.util.Timer timer = new Timer(true); // Use daemon thread for timer
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    JFrameRepaintable repaintable = localView::repaintView;
                    SwingUtilities.invokeLater(repaintable::repaintView);
                }
            }, 0, GAME_TICK_MS);


            RemoteGameStateListener playerListener = new RemoteGameStateListener() {
                @Override
                public void setRemoteGameState(RemoteGameStateManager remoteGameStateManager) throws RemoteException {
                    localView.setRemoteGameStateManager(remoteGameStateManager);

                }

                @Override
                public void gameOver(String winningPlayerId) throws RemoteException {
                    System.out.println("Winning player is: " + winningPlayerId);
                }
            };

            manager.addListener(playerListener);

            localView.setOnClose(() -> {
                try {
                    manager.removeListener(playerListener);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
