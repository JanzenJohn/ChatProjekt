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
public class ChatServer extends Server{
    
    public static void main(String[] args){
        ChatServer x = new ChatServer();
    }
    private HashMap<String, ConnectionState> connectionStates;
    
    public ChatServer(){
        super(4002);
        this.connectionStates = new HashMap<>();
    }

    @Override
    public void processNewConnection(String pClientIP, int pClientPort) {
        this.connectionStates.put(pClientIP + pClientPort, new ConnectionState());
        this.send(pClientIP, pClientPort, "+OK Server ready");
    }

    @Override
    public void processMessage(String pClientIP, int pClientPort, String pMessage) {
        if (pMessage.equals("EXIT"))this.closeConnection(pClientIP, pClientPort);
        System.out.println(pClientIP + ":" +  pClientPort);
    }

    @Override
    public void processClosingConnection(String pClientIP, int pClientPort) {
        // hier muss aufgeräumt werden was alles in ProcessNewConnection erschaffen wurde
        this.connectionStates.remove(pClientIP + pClientPort);
    }
    
}
