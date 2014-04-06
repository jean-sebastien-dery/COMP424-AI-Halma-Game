/**
 * 
 */
package s260430688;

import halma.CCBoard;
import halma.CCMove;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;

import boardgame.Board;
import boardgame.Move;
import boardgame.Player;

/**
 * @author Jean-Sebastien Dery (260 430 688)
 *
 */
public class s260430688Player extends Player {
	
	/**
	 * The current board that contains all the information about the game at a given point.
	 */
	CCBoard board;
	
	/**
	 * This reference point will be used to compute the heuristic of the value function.
	 */
	private Point[] borderCellsInGoalZone = new Point[1];

	private PriorityQueue<BoardState> priorityQueueOfBoardStates = new PriorityQueue<BoardState>(100, new BoardStateComparator());
	
	private LinkedList<CCMove> listOfMovesToReachBestState = new LinkedList<CCMove>();
	
	private boolean isPlayerInitialized = false;
	
	private int goalPlayerID = 0;

	/**
	 * Constructor.
	 * 
	 * @param name My McGill student number that will be used to grade the project.
	 */
	public s260430688Player() {
		// Instantiates my implementation with my McGill ID.
		super("260430688");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param s The student ID.
	 */
	public s260430688Player(String s) {
		super(s);
	}
    
	/**
	 * Creates the board for the current player.
	 */
    public Board createBoard() { return new CCBoard(); }
    
    private void initializePlayer() {
    	System.out.println("The current color of my player is " + this.getColor());
    	
    	if (this.board == null) {
    		// No assumptions are made, handles the initialization if the board was not set properly
        	// before this method is called. The points for the heuristic will therefore not reflect
        	// the current configuration of the board.
    		System.err.println("The board was not properly set, cannot initialize the player.");
    		this.borderCellsInGoalZone[0] = new Point(7, 7);
    		return;
    	}
    	
    	// Sets the point that will be used for to calculate the heuristic.
    	if (this.playerID == 0) { // Upper left
//    		this.borderCellsInGoalZone[0] = new Point(15, 12);
//    		this.borderCellsInGoalZone[1] = new Point(14, 12);
//    		this.borderCellsInGoalZone[2] = new Point(13, 13);
//    		this.borderCellsInGoalZone[3] = new Point(12, 14);
//    		this.borderCellsInGoalZone[4] = new Point(12, 15);
    		this.borderCellsInGoalZone[0] = new Point(15, 15);
    		this.goalPlayerID = 3;
    	} else if (this.playerID == 1) { // Lower left
//    		this.borderCellsInGoalZone[0] = new Point(0, 12);
//    		this.borderCellsInGoalZone[1] = new Point(1, 12);
//    		this.borderCellsInGoalZone[2] = new Point(2, 13);
//    		this.borderCellsInGoalZone[3] = new Point(3, 14);
//    		this.borderCellsInGoalZone[4] = new Point(3, 15);
    		this.borderCellsInGoalZone[0] = new Point(0, 15);
    		this.goalPlayerID = 2;
    	} else if (this.playerID == 2) { // Upper right
//    		this.borderCellsInGoalZone[0] = new Point(12, 0);
//    		this.borderCellsInGoalZone[1] = new Point(12, 1);
//    		this.borderCellsInGoalZone[2] = new Point(13, 2);
//    		this.borderCellsInGoalZone[3] = new Point(14, 3);
//    		this.borderCellsInGoalZone[4] = new Point(15, 4);
    		this.borderCellsInGoalZone[0] = new Point(15, 0);
    		this.goalPlayerID = 1;
    	} else if (this.playerID == 3) { // Lower right
//    		this.borderCellsInGoalZone[0] = new Point(3, 0);
//    		this.borderCellsInGoalZone[1] = new Point(3, 1);
//    		this.borderCellsInGoalZone[2] = new Point(2, 2);
//    		this.borderCellsInGoalZone[3] = new Point(0, 3);
//    		this.borderCellsInGoalZone[4] = new Point(1, 3);
    		this.borderCellsInGoalZone[0] = new Point(0, 0);
    		this.goalPlayerID = 0;
    	}
    }

	@Override
	public Move chooseMove(Board board) {
		System.out.println("Need to chose a move.");
		
		// Casts the abstract class into its implementation.
		this.board = (CCBoard) board;

		// Initializes the player based on the current board configuration.
		if (!isPlayerInitialized) {
			this.initializePlayer();
		}
		
		if (this.listOfMovesToReachBestState.isEmpty()) {
			// Clears the priority queue since it will be different for every move.
			priorityQueueOfBoardStates.clear();
			
			this.updatePointsUsedByHeuristic();
			
			ArrayList<Point> allMyTokens = this.board.getPieces(this.playerID);
			
			System.out.println("Will start to analyze moves.");
			
			for (Point currentPoint : allMyTokens) {
				double valueOfState = this.getHeuristicValueCurrentState(allMyTokens);
				BoardState boardConfiguration = new BoardState(this, (CCBoard) board.clone(), currentPoint, new LinkedList<CCMove>(), valueOfState);
				boardConfiguration.exploitState();
			}
			
			System.out.println("The priority queue is completed.");
			
			if (!this.priorityQueueOfBoardStates.isEmpty()) {
				
				// TODO: need to handle the case where there is no good move.
				do {
					// Pops the best desired state in the priority queue.
					BoardState bestBoardState = this.priorityQueueOfBoardStates.poll();
					listOfMovesToReachBestState = bestBoardState.getListOfMoves();
					System.out.println("There are "+this.listOfMovesToReachBestState.size()+" move(s) in order to reach the best state and its heuristic value is "+bestBoardState.valueOfState+", here's the list: ");
					for (CCMove aMove : this.listOfMovesToReachBestState) {
						System.out.println(aMove.toPrettyString());
					}
				} while(listOfMovesToReachBestState.isEmpty());
				
				return (listOfMovesToReachBestState.removeFirst());
				
			} else {
				// If the priority queue is empty, it means that all the tokens are at the goal zone.
				System.out.println("All the tokens are in the goal zone so the game is over.");
				return (new CCMove(this.playerID, null, null));
			}
		} else {
			// If we are here it means that we have not reached the desired state yet.
			return (listOfMovesToReachBestState.removeFirst());
		}
	}
	
	private void updatePointsUsedByHeuristic() {
		synchronized(this.borderCellsInGoalZone) {
			// Sets the point that will be used for to calculate the heuristic.
	    	if (this.playerID == 0) { // Upper left
//	    		this.borderCellsInGoalZone[0] = new Point(15, 12);
//	    		this.borderCellsInGoalZone[1] = new Point(14, 12);
//	    		this.borderCellsInGoalZone[2] = new Point(13, 13);
//	    		this.borderCellsInGoalZone[3] = new Point(12, 14);
//	    		this.borderCellsInGoalZone[4] = new Point(12, 15);
	    		this.borderCellsInGoalZone[0] = new Point(15, 15);
//	    		if (this.board.getPieceAt(new Point(15, 15)) != null && this.board.getPieceAt(new Point(15, 15)) == this.playerID) {
//	    			if () {
//	    				
//	    			}
//	    		}
	    	} else if (this.playerID == 1) { // Lower left
//	    		this.borderCellsInGoalZone[0] = new Point(0, 12);
//	    		this.borderCellsInGoalZone[1] = new Point(1, 12);
//	    		this.borderCellsInGoalZone[2] = new Point(2, 13);
//	    		this.borderCellsInGoalZone[3] = new Point(3, 14);
//	    		this.borderCellsInGoalZone[4] = new Point(3, 15);
	    		this.borderCellsInGoalZone[0] = new Point(0, 15);
	    	} else if (this.playerID == 2) { // Upper right
//	    		this.borderCellsInGoalZone[0] = new Point(12, 0);
//	    		this.borderCellsInGoalZone[1] = new Point(12, 1);
//	    		this.borderCellsInGoalZone[2] = new Point(13, 2);
//	    		this.borderCellsInGoalZone[3] = new Point(14, 3);
//	    		this.borderCellsInGoalZone[4] = new Point(15, 4);
	    		this.borderCellsInGoalZone[0] = new Point(15, 0);
	    	} else if (this.playerID == 3) { // Lower right
//	    		this.borderCellsInGoalZone[0] = new Point(3, 0);
//	    		this.borderCellsInGoalZone[1] = new Point(3, 1);
//	    		this.borderCellsInGoalZone[2] = new Point(2, 2);
//	    		this.borderCellsInGoalZone[3] = new Point(0, 3);
//	    		this.borderCellsInGoalZone[4] = new Point(1, 3);
	    		this.borderCellsInGoalZone[0] = new Point(0, 0);
	    	}
		}
	}
	
	/**
	 * 
	 * @param positionOfToken The Point where the token is placed on the chessboard.
	 * @param playerID The player's ID of the opponent.
	 * @return True if the given token is in the opponen'ts base and false otherwise.
	 */
	@SuppressWarnings("static-access")
	public boolean isTokenInBaseOfPlayer(Point positionOfToken, int playerID) {
		for(Point p: this.board.bases[playerID]){
			if (positionOfToken.equals(p)) {
				return (true);
			}
		}
		return (false);
	}
	
	/**
	 * Note that this is not synchronized since it is read only and immutable after the game starts.
	 * 
	 * @return The cells that define the goal zone.
	 */
	public Point[] getBorderCellsInGoalZone() {
		synchronized(this.borderCellsInGoalZone) {
			return (this.borderCellsInGoalZone);
		}
	}
	
	/**
	 * Updates the board state priority queue that will be used to make a decision.
	 * 
	 * @param boardState
	 */
	public void updatePriorityQueue(BoardState boardState) {
		synchronized(this.priorityQueueOfBoardStates) {
			this.priorityQueueOfBoardStates.add(boardState);
		}
	}
	
	public double getHeuristicValueCurrentState(ArrayList<Point> currentStateTokens) {
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
	public double getHeuristicValueForToken(Point position) {
		// The heuristic that I am using is the Manhattan distance between the 
		// current piece and the closest cell in the goal zone.
		
		// Initializes the 'smallestDistance' variable to an impossible value given the size of the board.
		double smallestDistance = 100;
		
//		if (this.isTokenInBaseOfPlayer(position, this.goalPlayerID)) {
//			// Returns 0 since the token is already in the opponent's base.
//			System.out.println("The heuristic will be 0 because it is in the goal zone for point " + position.toString());
//			smallestDistance = 0; //FIXME: the heuristic will need to be 0.
//		} else {
			Point[] borderCellsInGoalZone = this.getBorderCellsInGoalZone();
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
//		}
		
		System.out.println("\t The heursitic value is " + smallestDistance + " for token at " + position.toString());
		return (smallestDistance);
	}
}
