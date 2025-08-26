package it.unibo.agar.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteGameStateListener extends java.io.Serializable, Remote  {

    void setRemoteGameState(RemoteGameStateManager manager) throws RemoteException;

}
