
# Build

./gradlew clean build shadowJar

# Running
Run the Server with __6549677eb9a7__ being the Zookeeper and Kafka host.

java -jar build/libs/vertx-game-server-1.0-SNAPSHOT-full.jar 6549677eb9a7

# Kafka
I use Docker for creating my actual Kafka-instance:

__docker pull devdb/kafka:latest__ 

__docker run -d --name kafka -p 2181:2181 -p 9092:9092 devdb/Kafka__

don't forget to add the docker-host-name to /etc/hosts!

# Create a game
There is a small REST-API available for managing games.

To create a new one for 3 players named player1/player2/player3 issue the following curl command:

curl -XPOST http://127.0.0.1:8090/games -H "Content-Type: application/json" -d '{"playerNames" : ["player1","player2","player3"]}'

