/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import java.util.ArrayList;

/**
 *
 * @author john
 */
public class ChatClient extends Client {
    
    private final String username;
    private final String password;
    private final Queue<String> messages;
    private final Signaler messageSignaler;
    private boolean loggedIn;

    public ChatClient(String pHost, int pPort, String pUsername, String pPassword, Signaler pMessageSignaler) {
        super(pHost, pPort);
        this.username = pUsername;
        this.password = pPassword;
        this.messages = new Queue<>();
        this.messageSignaler = pMessageSignaler;
        this.loggedIn = false;
    }

    @Override
    public void processMessage(String pMessage) {
        if (pMessage.startsWith("+OK Server ready!")) {
            this.send("USER " + this.username);
        } else if (pMessage.equals("+OK Password required")) {
            this.send("PASS " + this.password);
        } else if (pMessage.startsWith("+OK Logged in as")) {
            this.loggedIn = true;
        } else if (pMessage.startsWith("$")) {
            this.messages.enqueue(pMessage);
            this.messageSignaler.doNotify();
        } else if (pMessage.startsWith("-ERR")) {
            if (this.loggedIn) {
                throw new RuntimeException("Server responsed with " + pMessage);
            } else {
                this.close();
            }
        }
    }

    public ArrayList<String> getMessages() {
        ArrayList<String> list = new ArrayList<>();
        while (!this.messages.isEmpty()) {
            list.add(this.messages.front());
            this.messages.dequeue();
        }
        return list;
    }

    public void sendMessage(String msg) {
        this.send("SENDTOALL " + msg);
    }

}
