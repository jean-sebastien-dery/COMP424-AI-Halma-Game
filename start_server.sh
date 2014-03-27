#!/bin/bash

echo "About to start the server that will run the Halma game.";

# Runs the server with the defaults parameters.
java -cp ./jar/projectsrc.jar boardgame.Server -p 8123 -t 300000 -b halma.CCBoard
