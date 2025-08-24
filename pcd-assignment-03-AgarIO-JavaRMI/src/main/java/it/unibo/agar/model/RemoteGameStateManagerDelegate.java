package it.unibo.agar.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class RemoteGameStateManagerDelegate implements RemoteGameStateManager {
    private final List<RemoteGameStateListener> listeners = new ArrayList<>();

    private final GameStateManager manager;

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
        manager.tick();
        for (var listener : listeners) { listener.setWorld(this.getWorld()); }
    }

    @Override
    public void addListener(RemoteGameStateListener l) throws RemoteException {
        listeners.add(l);
    }
}
