/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import java.util.HashMap;

/**
 *
 * @author john
 */
public class ChatServer extends Server {

    public static void main(String[] args) {
        ChatServer x = new ChatServer();
    }
    private HashMap<String, ConnectionState> connectionStates;

    public ChatServer() {
        super(4002);
        this.connectionStates = new HashMap<>();
    }

    @Override
    public void processNewConnection(String pClientIP, int pClientPort) {
        this.connectionStates.put(pClientIP + pClientPort, new ConnectionState());
        this.send(pClientIP, pClientPort, "+OK Server ready! User required");
    }

    @Override
    public void processMessage(String pClientIP, int pClientPort, String pMessage) {

        ConnectionState state = this.connectionStates.get(pClientIP + pClientPort);

        if (pMessage.equals("LOGOUT")) {
            this.closeConnection(pClientIP, pClientPort);
        } else if (pMessage.startsWith("USER")) {
            if (state.loggedIn) {
                this.send(pClientIP, pClientPort, "-ERR Already Logged in");
            } else {
                String suggestedUsername = pMessage.split(" ", 2)[1];
                state.userName = suggestedUsername.replaceAll("\\$", "");
                this.send(pClientIP, pClientPort, "+OK Password required");
            }
        } else if (pMessage.startsWith("PASS")) {
            if (state.loggedIn) {
                this.send(pClientIP, pClientPort, "-ERR Already Logged in");
            } else {
                this.send(pClientIP, pClientPort, "+OK Logged in as " + state.userName);
                state.loggedIn = true;
            }

        } else if (pMessage.equals("WHOAMI")) {
            // Undokumentierte anfrage (funktioniert nicht auf allen servern)
            if (!state.loggedIn) {
                this.send(pClientIP, pClientPort, "NOBODY");
            } else {
                this.send(pClientIP, pClientPort, state.userName);
            }
        } else {
            this.send(pClientIP, pClientPort, "-ERR Command unrecognized");
        }
    }

    @Override
    public void processClosingConnection(String pClientIP, int pClientPort) {
        // hier muss aufgeräumt werden was alles in ProcessNewConnection erschaffen wurde
        this.connectionStates.remove(pClientIP + pClientPort);
    }

}
