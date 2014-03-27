#!/bin/bash

echo "About to run the client.";

java -cp ./jar/projectsrc.jar boardgame.Client halma.CCRandomPlayer localhost 8123
