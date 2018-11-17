wget https://oss.sonatype.org/content/repositories/releases/org/mongodb/mongodb-driver/3.8.1/mongodb-driver-3.8.1.jar
wget https://oss.sonatype.org/content/repositories/releases/org/mongodb/mongodb-driver-core/3.8.1/mongodb-driver-core-3.8.1.jar
wget https://oss.sonatype.org/content/repositories/releases/org/mongodb/bson/3.8.1/bson-3.8.1.jar

javac -d . ../androidClient/app/src/main/java/net/ddns/gingerpi/chessboardnetCommon/*.java
jar cf chessboardnetCommon.jar net
rm -r net
