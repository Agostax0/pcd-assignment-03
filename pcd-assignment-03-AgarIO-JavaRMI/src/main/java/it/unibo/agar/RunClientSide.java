package it.unibo.agar;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import static it.unibo.agar.RunServerSide.GAME_TICK_MS;
import it.unibo.agar.model.remote.*;
import it.unibo.agar.view.JFrameRepaintable;
import it.unibo.agar.view.LocalView;

public class RunClientSide {

    public static final int DEFAULT_PORT = 2001;

    public static void main(String[] args){

        try {
            var registry = LocateRegistry.getRegistry(RunServerSide.DEFAULT_PORT);

            var manager = (RemoteGameStateManager) registry.lookup(RunServerSide.GAME_STATE_MANAGER_BINDING);
            LocalView localViewP2 = new LocalView(manager, (args[0] != null) ? args[0] : "p*");
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
                    localViewP2.setRemoteGameStateManager(remoteGameStateManager);
                }

                @Override
                public void gameOver(String winningPlayerId) throws RemoteException {
                    manager.removeListener(this);

                    System.out.println("Winner is " + winningPlayerId);
                }
            });

        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
}
