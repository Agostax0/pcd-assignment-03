package it.unibo.agar.model.remote;

import java.rmi.RemoteException;

import it.unibo.agar.model.GameStateManager;
import it.unibo.agar.model.World;

public class RemoteGameStateManagerImpl extends RemoteGameStateManagerDelegate implements GameStateManager {

    public RemoteGameStateManagerImpl(final World initialWorld) {
        super(initialWorld);
    }

    @Override
    public World getWorld() {
        try {
            return super.getWorld();
        } catch (RemoteException e) {
            log("getWorld: " + e);
        }
        return null;
    }

    @Override
    public void setPlayerDirection(String playerId, double dx, double dy) {
        try {
            super.setPlayerDirection(playerId, dx, dy);
        } catch (RemoteException e) {
            log("setPlayerDirection: " + e);
        }
    }

    @Override
    public void tick() {
        try {
            super.tick();
        } catch (RemoteException e) {
            log("tick: " + e);
        }
    }

    @Override
    public boolean isGameOver() {
        return super.manager.isGameOver();
    }

    private void log(String msg){
        System.out.println("[ " + this.getClass() +  " ]" + "\t" + msg);
    }
}
