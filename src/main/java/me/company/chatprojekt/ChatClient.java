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
    private final Notifier messageNotifier;
    private final Notifier exceptionNotifier;
    private boolean loggedIn;
    public boolean hasOlnSupport;

    public ChatClient(String pHost, int pPort, String pUsername, String pPassword, Notifier pMessageNotifier, Notifier pExceptionNotifier) {
        super(pHost, pPort);
        this.username = pUsername;
        this.password = pPassword;
        this.messages = new Queue<>();
        this.messageNotifier = pMessageNotifier;
        this.exceptionNotifier = pExceptionNotifier;
        this.hasOlnSupport = false;
        this.loggedIn = false;
    }

    @Override
    public void processMessage(String pMessage) {
        if (pMessage.startsWith("+OK Server ready!")) {
            this.send("USER " + this.username);
            this.hasOlnSupport = pMessage.contains("+oln_support");
        } else if (pMessage.equals("+OK Password required")) {
            this.send("PASS " + this.password);
        } else if (pMessage.startsWith("+OK Logged in as")) {
            this.loggedIn = true;
        } else if (pMessage.startsWith("$")) {
            this.messages.enqueue(pMessage);
            this.messageNotifier.call();
        }else if (pMessage.startsWith("-ERR User unknown")){
            this.send("REGISTER " + this.username + "$" + this.password);
            this.send("USER " + this.username);
        } else if (pMessage.startsWith("-ERR")) {
            this.exceptionNotifier.call(pMessage);
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

    public void sendMessage(String pMsg) {
        this.send("SENDTOALL " + pMsg);
    }
    public void sendMessage(String pUser, String pMsg) {
        this.send("SENDTO " + pUser + "$" + pMsg);
    }

}
