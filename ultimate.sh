#!/bin/bash

echo "About to run the server and four instances of my player's implementation.";

echo "Starting the server.";
xterm -e java "-cp ./jar/projectsrc.jar boardgame.Server -p 8123 -t 300000 -b halma.CCBoard"

echo "Starting the first player.";
#xterm -e java "-cp ./jar/projectsrc.jar boardgame.Client halma.CCRandomPlayer localhost 8123" &
echo "Starting the second player.";
#xterm -e java "-cp ./jar/projectsrc.jar boardgame.Client halma.CCRandomPlayer localhost 8123" &
echo "Starting the third player.";
#xterm -e java "-cp ./jar/projectsrc.jar boardgame.Client halma.CCRandomPlayer localhost 8123" &
echo "Starting the fourth player.";
#xterm -e java "-cp ./jar/projectsrc.jar boardgame.Client halma.CCRandomPlayer localhost 8123" &

sleep 5