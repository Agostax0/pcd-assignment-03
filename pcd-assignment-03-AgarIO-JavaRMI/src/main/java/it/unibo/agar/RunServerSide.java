package it.unibo.agar;

import it.unibo.agar.model.*;
import it.unibo.agar.view.GlobalView;
import it.unibo.agar.view.JFrameRepaintable;

import javax.swing.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RunServerSide {
    private static final int WORLD_WIDTH = 1000;
    private static final int WORLD_HEIGHT = 1000;
    private static final int NUM_PLAYERS = 4; // p1, p2, p3, p4
    private static final int NUM_FOODS = 100;
    public static final long GAME_TICK_MS = 30; // Corresponds to ~33 FPS

    public static final String GAME_STATE_MANAGER_BINDING = "GameStateManager";
    public static final int DEFAULT_PORT = 2000;


    public static final String SERVER_SIDE_REGISTRY_KEY = "RunServerSide";

    public static void main(String[] args){
        final List<Player> initialPlayers = GameInitializer.initialPlayers(NUM_PLAYERS, WORLD_WIDTH, WORLD_HEIGHT);
        final List<Food> initialFoods = GameInitializer.initialFoods(NUM_FOODS, WORLD_WIDTH, WORLD_HEIGHT);
        final World initialWorld = new World(WORLD_WIDTH, WORLD_HEIGHT, initialPlayers, initialFoods);
        final RemoteGameStateManagerImpl remoteManager =  new RemoteGameStateManagerImpl(initialWorld);

        try {
            LocateRegistry.createRegistry(DEFAULT_PORT);

            var managerStub = UnicastRemoteObject.exportObject(remoteManager, DEFAULT_PORT);

            LocateRegistry.getRegistry(DEFAULT_PORT).bind(GAME_STATE_MANAGER_BINDING, managerStub);

        } catch (RemoteException | AlreadyBoundException e) {
            throw new RuntimeException(e);
        }

        final GlobalView globalView = new GlobalView(remoteManager);
        final JFrameRepaintable repaintable = globalView::repaintView;
        globalView.setVisible(true);

        final Timer timer = new Timer(true); // Use daemon thread for timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // AI movement for p1, p3, p4
                AIMovement.moveAI("p1", remoteManager);
                AIMovement.moveAI("p3", remoteManager); // Assuming p3 is AI
                AIMovement.moveAI("p4", remoteManager); // Assuming p4 is AI

                System.out.println("[Server]: Tick");
                remoteManager.tick();


                SwingUtilities.invokeLater(repaintable::repaintView);
            }
        }, 0, GAME_TICK_MS);
    }
}
