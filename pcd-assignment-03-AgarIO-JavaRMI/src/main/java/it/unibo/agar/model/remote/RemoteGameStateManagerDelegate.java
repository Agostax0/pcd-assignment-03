package it.unibo.agar.model.remote;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

import it.unibo.agar.model.*;

public class RemoteGameStateManagerDelegate implements RemoteGameStateManager {
    private final List<RemoteGameStateListener> listeners = new CopyOnWriteArrayList<>();
    public final GameStateManager manager;

    public RemoteGameStateManagerDelegate(final World initialWorld) {
        manager = new DefaultGameStateManager(initialWorld);
    }

    @Override
    public synchronized World getWorld() throws RemoteException {
        return manager.getWorld();
    }

    @Override
    public synchronized void setPlayerDirection(String playerId, double dx, double dy) throws RemoteException {
        manager.setPlayerDirection(playerId, dx, dy);
    }

    @Override
    public void tick() throws RemoteException {
        if(this.isGameOver()){

            System.out.println("Game Over");

            var players = manager.getWorld().getPlayers();
            var winningPlayer = players.stream().max(Comparator.comparingDouble(AbstractEntity::getMass));
            for (var listener : listeners){
                listener.gameOver(
                        winningPlayer.get().getId()
                );
            }
        }
        else{
            manager.tick();

            for (var listener : listeners) { listener.setRemoteGameState((RemoteGameStateManager) this); }
        }
    }

    private boolean isGameOver() throws RemoteException {
        return manager.isGameOver();
    }

    @Override
    public synchronized void addListener(RemoteGameStateListener l) throws RemoteException {
        listeners.add(l);
    }

    @Override
    public synchronized void removeListener(RemoteGameStateListener l) throws RemoteException {
        listeners.remove(l);
    }
}
