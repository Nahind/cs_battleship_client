package model;

import java.util.ArrayList;

/**
 * Created by nahind on 24/11/16.
 */
public class Configuration {

    String playerName;
    String opponentName;
    String host;

    final String WELCOME_PLAYER_TEXT = "Welcome";
    public final String OPPONENT_JOINED_TEXT = "Your opponent has joined the game. Good luck !";
    public final String NOT_YOUR_TURN_TEXT = "You are not allowed to play : it is your opponent's turn. Maybe you can wait a little";
    public final String ALREADY_HIT_TEXT = "You have already hit that field, you fool !";

    int port;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private Configuration() {}

    private static Configuration INSTANCE = new Configuration();

    public static Configuration getInstance() {	return INSTANCE; }

    ArrayList<Integer> sizes = new ArrayList<Integer>(){
        {
            add(2);
            add(3);
            add(2);
            add(4);
            add(3);
            add(4);
        }
    };

    public ArrayList<Integer> getSizes() {
        return sizes;
    }


}

