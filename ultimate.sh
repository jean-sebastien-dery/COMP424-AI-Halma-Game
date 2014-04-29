#!/bin/bash

echo "About to run the server and four instances of my player's implementation.";

echo "Starting the server.";
gnome-terminal --title="Server" -x java -cp ./jar/projectsrc.jar boardgame.Server -p 8123 -t 300000 -b halma.CCBoard
echo "Starting the first player.";
gnome-terminal --title="Player 0" -x java -cp ./jar/projectsrc.jar:/home/jdery/workspace/PrjAIProject/bin/ boardgame.Client s260430688.s260430688Player localhost 8123
echo "Starting the second player.";
gnome-terminal --title="Player 0" -x java -cp ./jar/projectsrc.jar:/home/jdery/workspace/PrjAIProject/bin/ boardgame.Client s260430688.s260430688Player localhost 8123
#gnome-terminal --title="Player 1" -x java -cp ./jar/projectsrc.jar:/home/jdery/workspace/PrjAIProject/bin/ boardgame.Client s260430688.myPlayer1 localhost 8123
echo "Starting the third player.";
gnome-terminal --title="Player 0" -x java -cp ./jar/projectsrc.jar:/home/jdery/workspace/PrjAIProject/bin/ boardgame.Client s260430688.s260430688Player localhost 8123
#gnome-terminal --title="Player 2" -x java -cp ./jar/projectsrc.jar:/home/jdery/workspace/PrjAIProject/bin/ boardgame.Client s260430688.myPlayer2 localhost 8123
echo "Starting the fourth player.";
gnome-terminal --title="Player 0" -x java -cp ./jar/projectsrc.jar:/home/jdery/workspace/PrjAIProject/bin/ boardgame.Client s260430688.s260430688Player localhost 8123
#gnome-terminal --title="Player 3" -x java -cp ./jar/projectsrc.jar:/home/jdery/workspace/PrjAIProject/bin/ boardgame.Client s260430688.myPlayer3 localhost 8123