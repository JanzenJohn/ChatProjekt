/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import java.util.ArrayList;
import javax.swing.JTextArea;

/**
 *
 * @author john
 */
public class MessageFetcherThread extends Thread {

    private final JTextArea textArea;
    private final Signaler fetchSignal;
    private final ChatClient client;

    public MessageFetcherThread(JTextArea pTextArea, Signaler pSignal, ChatClient pClient) {
        this.textArea = pTextArea;
        this.fetchSignal = pSignal;
        this.client = pClient;
    }

    @Override
    public void run() {
        while (true) {
            this.fetchSignal.doWait();
            ArrayList<String> list = this.client.getMessages();
            String msgs[] = new String[list.size()];
            list.toArray(msgs);
            for (String msg : msgs) {
                String[] temp = msg.split("\\$", 4);
                String messeger = temp[1];
                String status = temp[2];
                String message = temp[3];
                this.textArea.append(messeger + " to ");
                this.textArea.append(status.equals("0") ? "Everyone " : "You ");
                this.textArea.append(": " + message + "\n");
            }
        }
    }
}
