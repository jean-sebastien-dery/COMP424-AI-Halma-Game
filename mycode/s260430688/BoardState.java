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
	/**
	 * The penalty applied to a move that takes a token out of the origin base.
	 */
	private final int MOVE_PENALTY_IF_MOVING_FROM_BASE = 300;
	
	/**
	 * The penalty applied to a token that is in the origin base.
	 */
	private final int MOVE_PENALTY_IF_IN_BASE = 500;
	
	/**
	 * The current board configuration.
	 */
	private CCBoard currentState;
	
	/**
	 * The token that will be used to generate neighbor states with the aid of valid moves.
	 */
	private Point tokenToConsider;
	
	/**
	 * The list of moves required to reach the current board configuration.
	 */
	private LinkedList<CCMove> listOfPreviousMoves;
	
	/**
	 * The value of the current board configuration used by the priority queue.
	 */
	public double valueOfState;
	
	/**
	 * The back pointer that points to the player implementation.
	 */
	s260430688Player playerBackPointer;

	/**
	 * Constructor.
	 * 
	 * @param playerBackPointer A back pointer that points to the player's implementation.
	 * @param currentBoardConfiguration The current state of the board.
	 * @param tokenToConsider The token to consider for the generation of neighbors and value determination.
	 * @param listOfPreviousMoves The list of moves that were executed to reach the 'currentBoardConfiguration'.
	 * @param valueOfState The value in the priority queue of the 'currentBoardConfiguration'.
	 */
	BoardState(s260430688Player playerBackPointer, CCBoard currentBoardConfiguration, Point tokenToConsider, LinkedList<CCMove> listOfPreviousMoves, double valueOfState) {
		this.currentState = currentBoardConfiguration;
		this.tokenToConsider = tokenToConsider;
		this.listOfPreviousMoves = listOfPreviousMoves;
		this.playerBackPointer = playerBackPointer;
		this.valueOfState = valueOfState;
	}
	
	/**
	 * Exploits the current state by determining valid moves from the current board configuration and determining the value of the generated neighbors.
	 */
	public void exploitState() {
		ArrayList<CCMove> listOfNeighbors = currentState.getLegalMoveForPiece(this.tokenToConsider, this.playerBackPointer.getColor());
		
		// Removing neighbors that have their final destination to places this list of moves already went.
		// This is completed in order to avoid cycles in the process.
		for (int i = 0 ; i < listOfNeighbors.size() ; i++) {
			CCMove potentialNeighbour = listOfNeighbors.get(i);
			
			// Removes cycle by verifying if the potential next state was already visited.
			for (CCMove previousMove : listOfPreviousMoves) {
				if (previousMove != null && potentialNeighbour.getTo().equals(previousMove.getFrom())) {
					listOfNeighbors.remove(i);
					--i; // Since we removed an element and the rest of the list was shifted, we don't want to skip other possible neighbors.
				}
			}
		}
		
		if (listOfNeighbors.size() == 0) {
			// If the size of neighbors is empty, it means that there are no valid moves,
			// therefore we can evaluate the value of the list of moves.
			this.addCurrentStateToPriorityQueue();
		} else {
			// If the size is not 0, it means that we can do more hops and therefore we need to explore
			// these moves and see what value they will have.
			for (CCMove moveToExecute : listOfNeighbors) {
				// Create a copy of the current board configuration.
				CCBoard copyOfBoard = (CCBoard) this.currentState.clone();
				// Executes the move on the copy of the board.
				copyOfBoard.move(moveToExecute);
				
				double valueOfNewState = determineValueOfStateAfterMove(moveToExecute);
				
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
	
	/**
	 * Determines the value of the board after the move has been executed on the initial board configuration.
	 * 
	 * @param moveToExecute The move to execute on the initial board configuration.
	 * @return The value of the state after the move has been executed on the initial board configuration.
	 */
	private double determineValueOfStateAfterMove(CCMove moveToExecute) {
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
		
		return (valueOfNewState);
	}
	
	/**
	 * Adds the current state to the priority queue used to determine what is the best desired state.
	 */
	private void addCurrentStateToPriorityQueue() {
		if (!this.listOfPreviousMoves.isEmpty()) {
			
			// So here the move with null 'to' and 'from' must only be added whenever the last move was a hop since this is how the server gets informed
			// that the turn is now over. If the last move is not a hop, the move with null 'to' and 'from' must not be added.
			for (int i = (this.listOfPreviousMoves.size()-1) ; i >= 0 ; i--) {
				if (this.listOfPreviousMoves.get(i) != null && this.listOfPreviousMoves.get(i).isHop()) {
					listOfPreviousMoves.add(new CCMove(this.playerBackPointer.getColor(), null, null));
					break;
				}
			}
			
			// This null move will trigger the end of the set of states to execute.
			listOfPreviousMoves.add(null);
			// Updates the priority queue in the Player thread.
			this.playerBackPointer.updatePriorityQueue(this);
		}
	}
	
	/**
	 * 
	 * @return The list of moves that were required in order to reach the current state.
	 */
	public LinkedList<CCMove> getListOfMovesToReachState() {
		return (this.listOfPreviousMoves);
	}
}
