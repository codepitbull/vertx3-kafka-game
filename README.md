#Vert.x 3 + Kafka + Event Sourcing

This project was meant for playing around with (the not yet released) [Vert.x 3](http://vertx.io/) and versions 0.8.2.1 of [Apache Kafka](http://kafka.apache.org/).

I picked a little "game" (well, currently it only allows to move around characters without much more ...) to show how both can be used to implement event sourcing on top.

# Warning
Before you start building it:

__This is a spare time project and it's definitely not in the state I want it to be. __

## Requirements
I moved the code for accessing Kafka from Vert.x into a separate [project](https://github.com/codepitbull/vertx3-kafka). You will have to build this locally and install into your maven repo. This should change soon, I am currently trying to get this refactored and soon after wards into maven-central.

## Building and Running
The package consists of two distinct modules, vertx-game-server and vertx-game-client. Follow the Build instructions in their __README.md__

Roughly:

- Build and install the Kafka-connectors: [project](https://github.com/codepitbull/vertx3-kafka)
- Build and install the server
- Build and install the client







