#javac -d . *.java
javac -cp "bson-3.8.1.jar:mongodb-driver-core-3.8.1.jar:mongodb-driver-3.8.1.jar" -d . *.java
jar cfm chessServer.jar MANIFEST.MF net
