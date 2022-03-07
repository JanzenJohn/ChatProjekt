#!/bin/sh

mvn clean compile assembly:single -P Server
cp target/*jar Server.jar
mvn clean package -P Client
cp target/*jar Client.jar
