/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author john
 */
public class ChatServer extends Server {

    public static void main(String[] args) throws SQLException {
        ChatServer x = new ChatServer();
    }
    private final HashMap<String, ConnectionState> connectionStates;
    private final HashMap<String, String> onlineUsers;
    private final Database db;

    public ChatServer() throws SQLException {
        super(4002);
        this.connectionStates = new HashMap<>();
        this.onlineUsers = new HashMap<>();
        this.db = new Database();
    }

    @Override
    public void processNewConnection(String pClientIP, int pClientPort) {
        this.connectionStates.put(pClientIP + pClientPort, new ConnectionState());
        this.send(pClientIP, pClientPort, "+OK Server ready! User required +oln_support");
    }

    @Override
    public void processMessage(String pClientIP, int pClientPort, String pMessage) {

        ConnectionState state = this.connectionStates.get(pClientIP + pClientPort);

        if (pMessage.equals("LOGOUT")) {
            this.closeConnection(pClientIP, pClientPort);
        } else if (pMessage.startsWith("REGISTER")) {
            try {
                String username = pMessage.split(" ", 2)[1].split("\\$", 2)[0];
                String pass = pMessage.split(" ", 2)[1].split("\\$", 2)[1];
                if (state.loggedIn) {
                    this.send(pClientIP, pClientPort, "-ERR Already Logged in");
                } else if (username.equals("SYSTEM") || username.length() < 3) {
                    this.send(pClientIP, pClientPort, "-ERR Illegal Username");
                } else if (db.exists(username)) {
                    this.send(pClientIP, pClientPort, "-ERR User already exists");
                } else {
                    db.create(username, pass);
                    this.send(pClientIP, pClientPort, "+OK created user " + username);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                this.send(pClientIP, pClientPort, "-ERR Database error contact admin");
            } catch (IndexOutOfBoundsException e) {
                this.send(pClientIP, pClientPort, "-ERR Message malformed");
            }

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
                try {
                    if (suggestedUsername.length() < 3 || suggestedUsername.equals("SYSTEM")) {
                        this.send(pClientIP, pClientPort, "-ERR Username too short");
                    } else if (db.exists(suggestedUsername)) {
                        state.userName = suggestedUsername;
                        this.send(pClientIP, pClientPort, "+OK Password required");
                    } else {
                        this.send(pClientIP, pClientPort, "-ERR User unknown");
                    }
                } catch (SQLException e) {
                    this.send(pClientIP, pClientPort, "-ERR Database error contact server admin");
                }
            }
        } else if (pMessage.startsWith("PASS")) {
            try {
                String password = pMessage.split(" ", 2)[1];
                synchronized (this.onlineUsers) {
                    if (state.loggedIn || this.onlineUsers.containsKey(state.userName)) {
                        this.send(pClientIP, pClientPort, "-ERR Already Logged in");
                    } else if (db.login(state.userName, password)) {
                        for (String s : this.onlineUsers.keySet()) {
                            this.send(pClientIP, pClientPort, "$SYSTEM$0$+" + s);
                        }
                        this.onlineUsers.put(state.userName, pClientIP + ":" + pClientPort);
                        this.send(pClientIP, pClientPort, "+OK Logged in as " + state.userName);
                        this.broadCast("$SYSTEM$0$+" + state.userName);
                        state.loggedIn = true;
                    } else {
                        this.send(pClientIP, pClientPort, "-ERR Password wrong");
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                this.send(pClientIP, pClientPort, "-ERR Message malformed");
            } catch (SQLException e) {
                this.send(pClientIP, pClientPort, "-ERR Database error contact server admin");
            }
        } else if (pMessage.startsWith("SENDTO ")) {
            try {
                String info[] = pMessage.split(" ", 2)[1].split("\\$", 2);
                String username = info[0];
                String msg = info[1];
                if (!this.onlineUsers.containsKey(username)) {
                    this.send(pClientIP, pClientPort, "-ERR User not online/exists");
                } else if (state.lastMessages.size() > 30) {
                    this.send(pClientIP, pClientPort, "-ERR Ratelimited");
                } else {
                    String contact = this.onlineUsers.get(username);
                    String contactIP = contact.split(":")[0];
                    String contactPort = contact.split(":")[1];
                    this.send(contactIP, Integer.parseInt(contactPort), "$" + state.userName + "$1$" + msg);
                    this.send(pClientIP, pClientPort, "+OK Message processed");
                    state.lastMessages.add(msg);
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                        @Override
                        public void run() {
                            state.lastMessages.remove(msg);
                        }
                    },
                            60000
                    );
                }
            } catch (IndexOutOfBoundsException e) {
                this.send(pClientIP, pClientPort, "-ERR Message malformed");
            }
        } else if (pMessage.startsWith("SENDTOALL ")) {
            try {
                if (state.lastMessages.size() > 30) {
                    this.send(pClientIP, pClientPort, "-ERR Ratelimited");
                } else {
                    String msg = pMessage.split(" ", 2)[1];
                    this.broadCast("$" + state.userName + "$0$" + msg);
                    state.lastMessages.add(msg);
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                        @Override
                        public void run() {
                            state.lastMessages.remove(msg);
                        }
                    },
                            60000
                    );
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
        ConnectionState state = this.connectionStates.get(pClientIP + pClientPort);
        if (state.loggedIn) {
            synchronized (this.onlineUsers) {
                this.onlineUsers.remove(state.userName);
                this.broadCast("$SYSTEM$0$-" + state.userName);
            }

        }
        this.connectionStates.remove(pClientIP + pClientPort);

    }

    public void broadCast(String msg) {
        String users[] = new String[this.onlineUsers.size()];
        this.onlineUsers.keySet().toArray(users);
        for (String user : users) {
            String contact = this.onlineUsers.get(user);
            String contactIP = contact.split(":")[0];
            String contactPort = contact.split(":")[1];
            this.send(contactIP, Integer.parseInt(contactPort), msg);
        }
    }

}
