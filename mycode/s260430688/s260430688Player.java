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
	 * Defines the number of cells that defines the limit of the goal zone.
	 */
	private final int NUMBER_OF_BORDER_CELLS_IN_GOAL_ZONE = 5;
	
	/**
	 * This reference point will be used to compute the heuristic of the value function.
	 */
	private Point[] borderCellsInGoalZone = new Point[NUMBER_OF_BORDER_CELLS_IN_GOAL_ZONE];

	private PriorityQueue<BoardState> priorityQueueOfBoardStates = new PriorityQueue<BoardState>(100, new BoardStateComparator());
	
	private LinkedList<CCMove> listOfMovesToReachBestState = new LinkedList<CCMove>();
	
	private boolean isPlayerInitialized = false;

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
    		this.borderCellsInGoalZone[1] = new Point(7, 7);
    		this.borderCellsInGoalZone[2] = new Point(7, 7);
    		this.borderCellsInGoalZone[3] = new Point(7, 7);
    		return;
    	}
    	
    	// Sets the point that will be used for to calculate the heuristic.
    	if (this.playerID == 0) { // Upper left
    		this.borderCellsInGoalZone[0] = new Point(15, 12);
    		this.borderCellsInGoalZone[1] = new Point(14, 12);
    		this.borderCellsInGoalZone[2] = new Point(13, 13);
    		this.borderCellsInGoalZone[3] = new Point(12, 14);
    		this.borderCellsInGoalZone[4] = new Point(12, 15);
    	} else if (this.playerID == 1) { // Lower left
    		this.borderCellsInGoalZone[0] = new Point(0, 12);
    		this.borderCellsInGoalZone[1] = new Point(1, 12);
    		this.borderCellsInGoalZone[2] = new Point(2, 13);
    		this.borderCellsInGoalZone[3] = new Point(3, 14);
    		this.borderCellsInGoalZone[4] = new Point(3, 15);
    	} else if (this.playerID == 2) { // Upper right
    		this.borderCellsInGoalZone[0] = new Point(12, 0);
    		this.borderCellsInGoalZone[1] = new Point(12, 1);
    		this.borderCellsInGoalZone[2] = new Point(13, 2);
    		this.borderCellsInGoalZone[3] = new Point(14, 3);
    		this.borderCellsInGoalZone[4] = new Point(15, 4);
    	} else if (this.playerID == 3) { // Lower right
    		this.borderCellsInGoalZone[0] = new Point(3, 0);
    		this.borderCellsInGoalZone[1] = new Point(3, 1);
    		this.borderCellsInGoalZone[2] = new Point(2, 2);
    		this.borderCellsInGoalZone[3] = new Point(0, 3);
    		this.borderCellsInGoalZone[4] = new Point(1, 3);
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
			
			ArrayList<Point> allMyTokens = this.board.getPieces(this.playerID);
			
			System.out.println("Will start to analyze moves.");
			
			for (Point currentPoint : allMyTokens) {
				BoardState boardConfiguration = new BoardState(this, (CCBoard) board.clone(), currentPoint, new LinkedList<CCMove>());
				boardConfiguration.exploitState();
			}
			
			System.out.println("The priority queue is completed.");
			
			// TODO: need to handle the case where there is no good move.
			do {
				BoardState bestBoardState = this.priorityQueueOfBoardStates.poll();
				listOfMovesToReachBestState = bestBoardState.getListOfMoves();
				System.out.println("There are "+this.listOfMovesToReachBestState.size()+" move(s) in order to reach the best state and its heuristic value is "+bestBoardState.valueOfState+", here's the list: ");
				for (CCMove aMove : this.listOfMovesToReachBestState) {
					System.out.println(aMove.toPrettyString());
				}
			} while(listOfMovesToReachBestState.isEmpty());
			
			return (listOfMovesToReachBestState.removeFirst());
		} else {
			return (listOfMovesToReachBestState.removeFirst());
		}
	}
	
	/**
	 * Note that this is not synchronized since it is read only and immutable after the game starts.
	 * 
	 * @return The cells that define the goal zone.
	 */
	public Point[] getBorderCellsInGoalZone() {
		return (this.borderCellsInGoalZone);
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
}
