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
public class Notifier {
    public ArrayList<NotifierInterface> inters = new ArrayList<>();
    
    public void addListener(NotifierInterface pInter){
        this.inters.add(pInter);
    }
    
    public void call(){
        this.call("");
    }
    public void call(String msg){
        for (NotifierInterface c : inters){
            c.onMessage(msg);
        }
    }
}
