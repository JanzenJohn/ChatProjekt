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
    private final HashMap<String, ConnectionState> connectionStates;
    private final HashMap<String, String> onlineUsers;

    public ChatServer() {
        super(4002);
        this.connectionStates = new HashMap<>();
        this.onlineUsers = new HashMap<>();
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
            if (state.loggedIn) {
                synchronized (this.onlineUsers) {
                    this.onlineUsers.remove(state.userName);
                }
            }
            this.closeConnection(pClientIP, pClientPort);
        } else if (pMessage.startsWith("USER")) {
            if (state.loggedIn) {
                this.send(pClientIP, pClientPort, "-ERR Already Logged in");
            } else {

                String suggestedUsername;
                try {
                    suggestedUsername = pMessage.split(" ", 2)[1];
                } catch (IndexOutOfBoundsException e) {
                    suggestedUsername = ":";
                }
                suggestedUsername = suggestedUsername.replaceAll("\\$", "");
                if (suggestedUsername.length() < 3) {
                    this.send(pClientIP, pClientPort, "-ERR Username too short");
                } else {
                    state.userName = suggestedUsername;
                    this.send(pClientIP, pClientPort, "+OK Password required");
                }
            }
        } else if (pMessage.startsWith("PASS")) {
            synchronized (this.onlineUsers) {
                if (state.loggedIn || this.onlineUsers.containsKey(state.userName)) {
                    this.send(pClientIP, pClientPort, "-ERR Already Logged in");
                } else {
                    this.onlineUsers.put(state.userName, pClientIP + ":" + pClientPort);
                    this.send(pClientIP, pClientPort, "+OK Logged in as " + state.userName);
                    state.loggedIn = true;
                }
            }
        } else if (pMessage.startsWith("SENDTO ")) {
            try {
                String info[] = pMessage.split(" ", 2)[1].split("\\$", 2);
                String username = info[0];
                String msg = info[1];
                if (!this.onlineUsers.containsKey(username)) {
                    this.send(pClientIP, pClientPort, "-ERR User not online/exitst");
                } else {
                    String contact = this.onlineUsers.get(username);
                    String contactIP = contact.split(":")[0];
                    String contactPort = contact.split(":")[1];
                    this.send(contactIP, Integer.parseInt(contactPort), "$" + state.userName + "$1$" + msg);
                    this.send(pClientIP, pClientPort, "+OK Message processed");
                }
            } catch (IndexOutOfBoundsException e) {
                this.send(pClientIP, pClientPort, "-ERR Message malformed");
            }
        } else if (pMessage.startsWith("SENDTOALL ")) {
            try {
                String msg = pMessage.split(" ", 2)[1];
                this.send(pClientIP, pClientPort, "+OK Message processed");
                String users[] = new String[this.onlineUsers.size()];
                this.onlineUsers.keySet().toArray(users);
                for (String user : users) {
                    String contact = this.onlineUsers.get(user);
                    String contactIP = contact.split(":")[0];
                    String contactPort = contact.split(":")[1];
                    this.send(contactIP, Integer.parseInt(contactPort), "$" + state.userName + "$0$" + msg);
                }

            } catch (IndexOutOfBoundsException e) {
                this.send(pClientIP, pClientPort, "-ERR Message malformed");
            }
        } else if (pMessage.equals("WHOAMI")) {
            // Undokumentierte anfrage (funktioniert nicht auf allen servern)
            if (!state.loggedIn) {
                this.send(pClientIP, pClientPort, "NOBODY");
            } else {
                this.send(pClientIP, pClientPort, "\"" + state.userName + "\"");
            }
        } else if (pMessage.equals("ISON")) {
            this.send(pClientIP, pClientPort, "+OK online");
            String users[] = new String[this.onlineUsers.size()];
            this.onlineUsers.keySet().toArray(users);
            for (String user : users) {
                this.send(pClientIP, pClientPort, "\"" + user + "\"");
            }
            this.send(pClientIP, pClientPort, ".");
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
