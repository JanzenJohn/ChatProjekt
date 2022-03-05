/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JTextArea;

/**
 *
 * @author john
 */
public class ClientControl {

    private ChatClient client;
    private final HashMap<String, String> chats;
    private final Notifier fetcherActivate, chatUpdated, onlineListUpdated;

    public ClientControl(Notifier pChatUpdateSignaler, Notifier pOnlineListUpdatedSignaler) {
        this.chatUpdated = pChatUpdateSignaler;
        this.onlineListUpdated = pOnlineListUpdatedSignaler;
        this.chats = new HashMap<>();
        this.chats.put("$GLOBAL", "");
        this.fetcherActivate = new Notifier();
        NotifierResponder n = new NotifierResponder() {
            @Override
            public void onMessage(String pMessage) {
                ArrayList<String> list = client.getMessages();
                String msgs[] = new String[list.size()];
                list.toArray(msgs);
                for (String msg : msgs) {
                    String[] temp = msg.split("\\$", 4);
                    String messeger = temp[1];
                    String status = temp[2];
                    String message = temp[3];
                    if (messeger.equals("SYSTEM") && client.hasOlnSupport){
                        onlineListUpdated.call(message);
                    }
                    String tempChat = (status.equals("0") || !client.hasOlnSupport) ? chats.get("$GLOBAL") : chats.getOrDefault(messeger, "");
                    tempChat += messeger + " to ";
                    tempChat += status.equals("0") ? "Everyone " : "You ";
                    tempChat += ": " + message + "\n";
                    if(status.equals("0") || !client.hasOlnSupport){
                        chats.put("$GLOBAL", tempChat);
                    } else {
                        chats.put(messeger, tempChat);
                    }
                }
                chatUpdated.call();
            }
        };
        this.fetcherActivate.addListener(n);
    }

    public void login(String pHost, int pPort, String pUsername, String pPassword) {
        if (this.client != null && this.client.isConnected()) {
            this.client.close();
        }
        this.client = new ChatClient(pHost, pPort, pUsername, pPassword, this.fetcherActivate, this.onlineListUpdated);

    }

    public void sendAll(String pMessage) {
        this.client.sendMessage(pMessage);
    }
    
    public void sendTo(String pUser, String pMessage){
        this.client.sendMessage(pUser, pMessage);
        String temp = this.chats.getOrDefault(pUser, "");
        temp += "You to " + pUser + ": " + pMessage + "\n";
        this.chats.put(pUser, temp);
        this.chatUpdated.call();
    }

    public String getChat(String pKey) {
        return this.chats.getOrDefault(pKey, "You dont have a chat with this user yet");
    }
}
