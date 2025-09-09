package it.unibo.agar.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RemoteGameStateManagerDelegate implements RemoteGameStateManager {
    private final List<RemoteGameStateListener> listeners = new ArrayList<>();

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
    public void addListener(RemoteGameStateListener l) throws RemoteException {
        listeners.add(l);
    }

    @Override
    public void removeListener(RemoteGameStateListener l) throws RemoteException {
        listeners.remove(l);
    }
}
