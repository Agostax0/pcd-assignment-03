package it.unibo.agar.model;

import java.rmi.RemoteException;

public class RemoteGameStateManagerImpl implements GameStateManager {
    private final RemoteGameStateManagerDelegate delegate;

    public RemoteGameStateManagerImpl(final World initialWorld) {
        this.delegate = new RemoteGameStateManagerDelegate(initialWorld);
    }

    @Override
    public World getWorld() {
        try {
            return delegate.getWorld();
        } catch (RemoteException e) {
            log("getWorld: " + e);
        }
        return null;
    }

    @Override
    public void setPlayerDirection(String playerId, double dx, double dy) {
        try {
            delegate.setPlayerDirection(playerId, dx, dy);
        } catch (RemoteException e) {
            log("setPlayerDirection: " + e);
        }
    }

    @Override
    public void tick() {
        try {
            delegate.tick();
        } catch (RemoteException e) {
            log("tick: " + e);
        }
    }

    private void log(String msg){
        System.out.println("[ " + this.getClass() +  " ]" + "\t" + msg);
    }
}
