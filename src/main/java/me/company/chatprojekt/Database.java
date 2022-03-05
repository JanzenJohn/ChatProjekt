/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package me.company.chatprojekt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author john
 */
public class Database {

    private Connection connection;
    private String login = """
                           SELECT CASE WHEN EXISTS (
                               SELECT *
                               FROM USERS
                               WHERE NICK = ?
                               AND PASS = ?
                           )
                           THEN CAST(1 AS BIT)
                           ELSE CAST(0 AS BIT) END""";
    private String existance = """
                           SELECT CASE WHEN EXISTS (
                               SELECT *
                               FROM USERS
                               WHERE NICK = ?
                           )
                           THEN CAST(1 AS BIT)
                           ELSE CAST(0 AS BIT) END""";
    private String create = """
                            INSERT INTO USERS (NICK, PASS) VALUES (?, ?)
                            """;

    public Database() throws SQLException {
        String url = "jdbc:sqlite:user.db";
        this.connection = DriverManager.getConnection(url);
        Statement t = this.connection.createStatement();
        t.execute("""
                  CREATE TABLE IF NOT EXISTS USERS(
                  NICK NVARCHAR(255),
                  PASS NVARCHAR(255)
                  )
                  """);

    }

    public boolean login(String pUser, String pPass) throws SQLException {
        PreparedStatement temp = this.connection.prepareStatement(login);
        temp.setString(1, pUser);
        temp.setString(2, pPass);
        ResultSet r = temp.executeQuery();
        r.next();
        return r.getBoolean(1);
    }

    public boolean exists(String pUser) throws SQLException {
        PreparedStatement temp = this.connection.prepareStatement(existance);
        temp.setString(1, pUser);
        ResultSet r = temp.executeQuery();
        r.next();
        return r.getBoolean(1);
    }

    public void create(String pUser, String pPass) throws SQLException {
        if (this.exists(pUser)) {
            throw new SQLException();
        } else {
            PreparedStatement temp = this.connection.prepareStatement(create);
            temp.setString(1, pUser);
            temp.setString(2, pPass);
            boolean execute = temp.execute();
        }

    }

}
