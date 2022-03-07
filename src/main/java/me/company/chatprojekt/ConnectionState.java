/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import java.util.ArrayList;

/**
 * Daten Klasse die alle eigenschaften des Verbindungspartners hält
 * @author john
 */
public class ConnectionState {
    boolean loggedIn = false;
    String userName = null;
    ArrayList<String> lastMessages = new ArrayList<>();
}
