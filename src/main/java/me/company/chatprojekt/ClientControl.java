/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import javax.swing.JTextArea;

/**
 *
 * @author john
 */
public class ClientControl {

    private JTextArea textArea;
    private ChatClient client;
    private MessageFetcherThread fetcher;
    private Signaler messageSignaler;

    public ClientControl(JTextArea pTextArea) {
        this.textArea = pTextArea;
        this.messageSignaler = new Signaler();
    }
    
    public void login(String pHost, int pPort, String pUsername, String pPassword){
        if (this.client != null && this.client.isConnected()){
            this.client.close();
        }
        if (this.fetcher != null && this.fetcher.isAlive()){
            this.fetcher.stop();
        }
        this.client = new ChatClient(pHost, pPort, pUsername, pPassword, this.messageSignaler);
        MessageFetcherThread f = new MessageFetcherThread((this.textArea), messageSignaler, this.client);
        f.start();
        this.fetcher = f;
    }
    
    public void sendAll(String pMessage){
        this.client.sendMessage(pMessage);
    }
}
