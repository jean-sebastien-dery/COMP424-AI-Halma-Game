/**
 * 
 */
package s260430688;

import halma.CCBoard;
import halma.CCMove;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Used in the search tree to detect multiple jumps.
 * 
 * @author Jean-Sebastien Dery (260 430 688)
 *
 */
public class BoardConfiguration {
	private int playerID;
	private CCBoard currentBoardConfiguration;
	private Point tokenToConsider;
	private LinkedList<CCMove> listOfPreviousMoves;

	BoardConfiguration(int playerID, CCBoard currentBoardConfiguration, Point tokenToConsider, LinkedList<CCMove> listOfPreviousMoves) {
		this.playerID = playerID;
		this.currentBoardConfiguration = currentBoardConfiguration;
		this.tokenToConsider = tokenToConsider;
		this.listOfPreviousMoves = listOfPreviousMoves;
	}
	
	public void exploitConfiguration() {
		ArrayList<CCMove> listOfNeighbors = currentBoardConfiguration.getLegalMoveForPiece(this.tokenToConsider, this.playerID);
		
		// Removing neighbors that have their final destination to places this list of moves already went.
		// This is completed in order to avoid cycles in the process.
		for (int i = 0 ; i < listOfNeighbors.size() ; i++) {
			CCMove potentialNeighbour = listOfNeighbors.get(i);
			for (CCMove previousMove : listOfPreviousMoves) {
				if (potentialNeighbour.getTo() == previousMove.getFrom()) {
					listOfNeighbors.remove(i);
					--i; // Since we removed an element and the rest of the list was shifted, we don't want to skip other possible neighbors.
				}
			}
		}
		
		if (listOfNeighbors.size() == 0) {
			// If the size of neighbors is empty, it means that there are no valid moves,
			// therefore we can evaluate the value of the list of moves.
			
		} else {
			// If the size is not 0, it means that we can do more hops and therefore we need to explore
			// these moves and see what value they will have.
			for (CCMove moveToExecute : listOfNeighbors) {
				// Create a copy of the current board configuration.
				CCBoard copyOfBoard = (CCBoard) this.currentBoardConfiguration.clone();
				// Executes the move on the copy of the board.
				copyOfBoard.move(moveToExecute);
				// Creates a new list of past move.
				LinkedList<CCMove> newListOfPreviousMoves = (LinkedList<CCMove>) this.listOfPreviousMoves.clone();
				newListOfPreviousMoves.add(moveToExecute);
				// Creates a new instance of the board configuration so that we potentially can exploid
				// more hops.
				BoardConfiguration newBoardConfiguration = new BoardConfiguration(this.playerID, copyOfBoard, moveToExecute.getTo(), newListOfPreviousMoves);
			}
		}
	}
}
