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
public class BoardState {
	private final int MOVE_PENALTY_IF_MOVING_FROM_BASE = 300;
	private final int MOVE_PENALTY_IF_IN_BASE = 500;
	
	private CCBoard currentState;
	private Point tokenToConsider;
	private LinkedList<CCMove> listOfPreviousMoves;
	public double valueOfState;
	s260430688Player playerBackPointer;

	BoardState(s260430688Player playerBackPointer, CCBoard currentBoardConfiguration, Point tokenToConsider, LinkedList<CCMove> listOfPreviousMoves, double valueOfState) {
		this.currentState = currentBoardConfiguration;
		this.tokenToConsider = tokenToConsider;
		this.listOfPreviousMoves = listOfPreviousMoves;
		this.playerBackPointer = playerBackPointer;
		this.valueOfState = valueOfState;
	}
	
	public void exploitState() {
		ArrayList<CCMove> listOfNeighbors = currentState.getLegalMoveForPiece(this.tokenToConsider, this.playerBackPointer.getColor());
		
		// Removing neighbors that have their final destination to places this list of moves already went.
		// This is completed in order to avoid cycles in the process.
		for (int i = 0 ; i < listOfNeighbors.size() ; i++) {
			CCMove potentialNeighbour = listOfNeighbors.get(i);
			
			// Removes cycle by verifying if the potential next state was already visited.
			for (CCMove previousMove : listOfPreviousMoves) {
				if (previousMove != null && potentialNeighbour.getTo().equals(previousMove.getFrom())) {
//					System.out.println("A cycle was detected, removing it from the list potential neighbors.");
					listOfNeighbors.remove(i);
					--i; // Since we removed an element and the rest of the list was shifted, we don't want to skip other possible neighbors.
				}
			}
		}
		
		if (listOfNeighbors.size() == 0) {
			// If the size of neighbors is empty, it means that there are no valid moves,
			// therefore we can evaluate the value of the list of moves.
			
//			System.out.println("The list of neighbors is equal to 0, a leaf of the tree has been reached. Adding the current state to the priority queue.");
			
			this.addCurrentStateToPriorityQueue();
		} else {
			// If the size is not 0, it means that we can do more hops and therefore we need to explore
			// these moves and see what value they will have.
			for (CCMove moveToExecute : listOfNeighbors) {
//				System.out.println("Creating a new state for move that starts from "+moveToExecute.getFrom().toString()+" and ends in "+moveToExecute.getTo().toString()+".");		
				// Create a copy of the current board configuration.
				CCBoard copyOfBoard = (CCBoard) this.currentState.clone();
				// Executes the move on the copy of the board.
				copyOfBoard.move(moveToExecute);
				
				// Updates the value of the heuristic.
				double valueOfNewState = this.valueOfState - this.playerBackPointer.getHeuristicValueForToken(moveToExecute.getFrom()) + this.playerBackPointer.getHeuristicValueForToken(moveToExecute.getTo());
				
				// This section will add the penalty to the move if it is in the goal zone and/or goes out of it.
				boolean isMoveOriginInMyBase;
				if (this.listOfPreviousMoves.isEmpty()) {
					isMoveOriginInMyBase = this.playerBackPointer.isTokenInBaseOfPlayer(moveToExecute.getFrom(), this.playerBackPointer.getColor());
				} else {
					isMoveOriginInMyBase = this.playerBackPointer.isTokenInBaseOfPlayer(this.listOfPreviousMoves.getFirst().getFrom(), this.playerBackPointer.getColor());
				}
				boolean isMoveDestinationInMyBase = this.playerBackPointer.isTokenInBaseOfPlayer(moveToExecute.getTo(), this.playerBackPointer.getColor());
				
				// Makes sure that the tokens go out of the initial goal zone as soon as possible.
				if (isMoveOriginInMyBase && !isMoveDestinationInMyBase) {
					valueOfNewState -= MOVE_PENALTY_IF_MOVING_FROM_BASE;
				}
				// Handles the situation where a token is not near the boarder of the goal zone.
				if (isMoveOriginInMyBase) {
					valueOfNewState -= MOVE_PENALTY_IF_IN_BASE;
				}
				
				// Creates a new list of past move.
				@SuppressWarnings("unchecked")
				LinkedList<CCMove> newListOfPreviousMoves = (LinkedList<CCMove>) this.listOfPreviousMoves.clone();
				newListOfPreviousMoves.add(moveToExecute);
				// Creates a new instance of the board state so that we potentially can exploid
				// more hops.
				BoardState newBoardState = new BoardState(this.playerBackPointer, copyOfBoard, moveToExecute.getTo(), newListOfPreviousMoves, valueOfNewState);
				newBoardState.exploitState();
			}
		}
	}
	
	private void addCurrentStateToPriorityQueue() {
		if (!this.listOfPreviousMoves.isEmpty()) {
			
			// So here the move with null 'to' and 'from' must only be added whenever the last move was a hop since this is how the server gets informed
			// that the turn is now over. If the last move is not a hop, the move with null 'to' and 'from' must not be added.
			for (int i = (this.listOfPreviousMoves.size()-1) ; i >= 0 ; i--) {
				if (this.listOfPreviousMoves.get(i) != null && this.listOfPreviousMoves.get(i).isHop()) {
//					System.out.println("Adding a move with NULL 'to' and 'from' at " + i);
					listOfPreviousMoves.add(new CCMove(this.playerBackPointer.getColor(), null, null));
					break;
				} else {
					if (this.listOfPreviousMoves.get(i) == null) {
//						System.out.println("The NULL move has been encountered at i="+i);
					} else {
//						System.out.println("This move was encountered: " + this.listOfPreviousMoves.get(i).toPrettyString() + " at i=" + i);
					}
				}
			}
			
			// This null move will trigger the end of the set of states to execute.
			listOfPreviousMoves.add(null);

//			System.out.println("The final value of this leaf state is " + this.valueOfState);
			// Updates the priority queue in the Player thread.
			this.playerBackPointer.updatePriorityQueue(this);
		}
	}
	
	public LinkedList<CCMove> getListOfMoves() {
		return (this.listOfPreviousMoves);
	}
}
