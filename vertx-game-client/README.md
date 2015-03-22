# Build
./gradlew desktop:dist

# Running
Connect as Player to game server running on __local machine__ using gameId __1000__ and playername __Jochen__

java -jar desktop/build/libs/desktop-1.0.jar player localhost 1000 JOCHEN

Connect as Beholder to game server running on __local machine__ using gameId __1000__ and offset __300__

java -jar desktop/build/libs/desktop-1.0.jar beholder localhost 1000 300



