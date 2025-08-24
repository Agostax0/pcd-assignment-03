package it.unibo.agar.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteGameStateListener extends Remote {

    void setWorld(World world) throws RemoteException;

}
