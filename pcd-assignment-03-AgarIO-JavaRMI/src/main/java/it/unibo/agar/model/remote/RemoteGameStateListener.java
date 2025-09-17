package it.unibo.agar.model.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteGameStateListener extends java.io.Serializable, Remote  {

    void setRemoteGameState(RemoteGameStateManager manager) throws RemoteException;
    void gameOver(String winningPlayerId) throws RemoteException;
}
