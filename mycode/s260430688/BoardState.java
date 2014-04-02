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
	private CCBoard currentState;
	private Point tokenToConsider;
	private LinkedList<CCMove> listOfPreviousMoves;
	public double valueOfState;
	s260430688Player playerBackPointer;

	BoardState(s260430688Player playerBackPointer, CCBoard currentBoardConfiguration, Point tokenToConsider, LinkedList<CCMove> listOfPreviousMoves) {
		this.currentState = currentBoardConfiguration;
		this.tokenToConsider = tokenToConsider;
		this.listOfPreviousMoves = listOfPreviousMoves;
		this.playerBackPointer = playerBackPointer;
	}
	
	public void exploitState() {
		ArrayList<CCMove> listOfNeighbors = currentState.getLegalMoveForPiece(this.tokenToConsider, this.playerBackPointer.getColor());
		
		// Removing neighbors that have their final destination to places this list of moves already went.
		// This is completed in order to avoid cycles in the process.
		for (int i = 0 ; i < listOfNeighbors.size() ; i++) {
			CCMove potentialNeighbour = listOfNeighbors.get(i);
			
			// Removes cycle by verifying if the potential next state was already visited.
			for (CCMove previousMove : listOfPreviousMoves) {
				if (potentialNeighbour.getTo().equals(previousMove.getFrom())) {
					System.out.println("A cycle was detected, removing it from the list potential neighbors.");
					listOfNeighbors.remove(i);
					--i; // Since we removed an element and the rest of the list was shifted, we don't want to skip other possible neighbors.
				}
			}
			
			// Removes potentialNeighbors that want to go out of the goal zone since it is
			// an illegal move.
			boolean fromInGoalZone = this.isTokenInBaseOfPlayer(potentialNeighbour.getFrom(), this.playerBackPointer.getColor());
			boolean toOutOfGoalZone = this.isTokenInBaseOfPlayer(potentialNeighbour.getTo(), this.playerBackPointer.getColor());
			if (fromInGoalZone && toOutOfGoalZone) {
				listOfNeighbors.remove(i);
				--i; // Since we removed an element and the rest of the list was shifted, we don't want to skip other possible neighbors.
			}
		}
		
		if (listOfNeighbors.size() == 0) {
			// If the size of neighbors is empty, it means that there are no valid moves,
			// therefore we can evaluate the value of the list of moves.
			
			System.out.println("The list of neighbors is equal to 0.");
			
			this.addCurrentStateToPriorityQueue();
		} else {
			// If the size is not 0, it means that we can do more hops and therefore we need to explore
			// these moves and see what value they will have.
			for (CCMove moveToExecute : listOfNeighbors) {
				System.out.println("Creating a new state for move that starts from "+moveToExecute.getFrom().toString()+" and ends in "+moveToExecute.getTo().toString()+".");		
				// Create a copy of the current board configuration.
				CCBoard copyOfBoard = (CCBoard) this.currentState.clone();
				// Executes the move on the copy of the board.
				copyOfBoard.move(moveToExecute);
				// Creates a new list of past move.
				@SuppressWarnings("unchecked")
				LinkedList<CCMove> newListOfPreviousMoves = (LinkedList<CCMove>) this.listOfPreviousMoves.clone();
				newListOfPreviousMoves.add(moveToExecute);
				// Creates a new instance of the board state so that we potentially can exploid
				// more hops.
				BoardState newBoardState = new BoardState(this.playerBackPointer, copyOfBoard, moveToExecute.getTo(), newListOfPreviousMoves);
				newBoardState.exploitState();
			}
		}
	}
	
	private void addCurrentStateToPriorityQueue() {
		if (!this.listOfPreviousMoves.isEmpty()) {
			
			// So here the NULL move must only be added whenever the last move was a hop since this is how the server gets informed
			// that the turn is now over. If the last move is not a hop, the NULL move must not be added.
			if (this.listOfPreviousMoves.getLast().isHop()) {
				listOfPreviousMoves.add(new CCMove(this.playerBackPointer.getColor(), null, null));
			}
			
			// Evaluate the value of the current configuration.
			ArrayList<Point> currentTokensConfiguration = this.currentState.getPieces(this.playerBackPointer.getColor());
			
			// Gets the value of the current configuration.
			this.valueOfState = this.getHeuristicValueCurrentState(currentTokensConfiguration);
			
			System.out.println("The final value of this configuration is " + this.valueOfState);
			// Updates the priority queue in the Player thread.
			this.playerBackPointer.updatePriorityQueue(this);

			System.out.println("List of moves in order to reach this state:");
			for (CCMove aMove : this.listOfPreviousMoves) {
				System.out.println(aMove.toPrettyString());
			}
		} else {
			System.out.println("Detected that no moves were made to reach this state so it will be ignored.");
		}
	}
	
	public LinkedList<CCMove> getListOfMoves() {
		return (this.listOfPreviousMoves);
	}
	
	private double getHeuristicValueCurrentState(ArrayList<Point> currentStateTokens) {
		double heuristicValueBeforeMove = 0;
		for (Point aToken : currentStateTokens) {
			heuristicValueBeforeMove += this.getHeuristicValueForToken(aToken);
		}
		return (heuristicValueBeforeMove);
	}
	
	/**
	 * Will return the heuristic value for the current move.
	 * 
	 * @param currentMove The move to evaluate.
	 * @return The value of the heuristic for the current move.
	 */
	private double getHeuristicValueForToken(Point position) {
		// The heuristic that I am using is the Manhattan distance between the 
		// current piece and the closest cell in the goal zone.
		
		// Initializes the 'smallestDistance' variable to an impossible value given the size of the board.
		double smallestDistance = 100;
		
		int opponentID = this.getOpponentID();
		if (this.isTokenInBaseOfPlayer(position, opponentID)) {
			// Returns 0 since the token is already in the opponent's base.
			smallestDistance = 0;
		} else {
			Point[] borderCellsInGoalZone = this.playerBackPointer.getBorderCellsInGoalZone();
			// Returns the shortest distance between the token and the border of the opponent's base.
			for (int i = 0 ; i < borderCellsInGoalZone.length ; i++) {
				Point boarderPoint = borderCellsInGoalZone[i];
				
				double x = Math.pow((double)(boarderPoint.x-position.x), 2);
				double y = Math.pow((double)(boarderPoint.y-position.y), 2);
				
				double heuristicValue = Math.floor(Math.sqrt(x+y));
				
				if (heuristicValue < smallestDistance) {
					smallestDistance = heuristicValue;
				}
			}
		}
		
		System.out.println("\t The heursitic value is " + smallestDistance);
		return (smallestDistance);
	}
	
	/**
	 * 
	 * @return The oponent's number.
	 */
	private int getOpponentID() {
		switch(this.playerBackPointer.getColor()) {
			case 0:
				return (3);
			case 1:
				return (2);
			case 2:
				return (1);
			case 3:
				return (0);
			default:
				System.err.println("Returned the opponent number to be equal to 0 since it was not matching any of the possible cases.");
				return (0);
		}
	}
	
	/**
	 * 
	 * @param positionOfToken The Point where the token is placed on the chessboard.
	 * @param playerID The player's ID of the opponent.
	 * @return True if the given token is in the opponen'ts base and false otherwise.
	 */
	private boolean isTokenInBaseOfPlayer(Point positionOfToken, int playerID){
		Integer IDInteger= new Integer(playerID);
		return (IDInteger.equals(this.currentState.board.get(positionOfToken)));
	}
}
