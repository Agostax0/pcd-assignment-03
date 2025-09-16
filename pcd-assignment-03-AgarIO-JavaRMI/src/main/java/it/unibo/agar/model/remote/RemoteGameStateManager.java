package it.unibo.agar.model.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import it.unibo.agar.model.World;

public interface RemoteGameStateManager extends Remote {
    World getWorld() throws RemoteException;
    void setPlayerDirection(final String playerId, final double dx, final double dy) throws RemoteException;
    void tick() throws RemoteException;

    void addListener(RemoteGameStateListener l) throws RemoteException;
}
