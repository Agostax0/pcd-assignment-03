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

            LocalView localViewP2 = new LocalView(manager, "p2");
            localViewP2.setVisible(true);

            final java.util.Timer timer = new Timer(true); // Use daemon thread for timer
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    JFrameRepaintable repaintable = localViewP2::repaintView;
                    SwingUtilities.invokeLater(repaintable::repaintView);
                }
            }, 0, GAME_TICK_MS);


            manager.addListener(new RemoteGameStateListener() {
                @Override
                public void setRemoteGameState(RemoteGameStateManager remoteGameStateManager) throws RemoteException {
                    System.out.println("[Client]: Update");
                    localViewP2.setRemoteGameStateManager(remoteGameStateManager);

                }
            });

        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
