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
	private ArrayList<Point> borderCellsInGoalZone = new ArrayList<Point>(10);

	/**
	 * Holds the priority queue that will be used to chose the desired state.
	 */
	private PriorityQueue<BoardState> priorityQueueOfBoardStates = new PriorityQueue<BoardState>(100, new BoardStateComparator());
	
	/**
	 * Holds all the hops move when a desired state has been chosen.
	 */
	private LinkedList<CCMove> listOfMovesToReachBestState = new LinkedList<CCMove>();
	
	/**
	 * Keeps track of whether or not the player has been initialized.
	 */
	private boolean isPlayerInitialized = false;
	
	/**
	 * Holds the target goal's player ID.
	 */
	private int goalPlayerID = 0;
	
	/**
	 * A reference to the main thread which is used by the BoardStateProcessor thread to wake it up when the computation is over.
	 */
	private Thread mainThreadReference;

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
    
    /**
     * Initializes the player once the configuration of the board is known. This should only be executed once.
     */
    private void initializePlayer() {    	
    	mainThreadReference = Thread.currentThread();
    	listOfMovesToReachBestState.add(null);
    	
    	if (this.board == null) {
    		// No assumptions are made, handles the initialization if the board was not set properly
        	// before this method is called. The points for the heuristic will therefore not reflect
        	// the current configuration of the board.
    		this.borderCellsInGoalZone.add(new Point(7, 7));
    		return;
    	}
    	
    	// Sets the point that will be used for to calculate the heuristic.
    	if (this.playerID == 0) { // Upper left
    		this.borderCellsInGoalZone.add(new Point(15, 15));
    		this.goalPlayerID = 3;
    	} else if (this.playerID == 1) { // Lower left
    		this.borderCellsInGoalZone.add(new Point(0, 15));
    		this.goalPlayerID = 2;
    	} else if (this.playerID == 2) { // Upper right
    		this.borderCellsInGoalZone.add(new Point(15, 0));
    		this.goalPlayerID = 1;
    	} else if (this.playerID == 3) { // Lower right
    		this.borderCellsInGoalZone.add(new Point(0, 0));
    		this.goalPlayerID = 0;
    	}
    }

	@SuppressWarnings("static-access")
	@Override
	public Move chooseMove(Board board) {
		System.out.println("Need to chose a move.");
		
		// Casts the abstract class into its implementation.
		this.board = (CCBoard) board;

		// Initializes the player based on the current board configuration.
		if (!isPlayerInitialized) {
			this.initializePlayer();
			this.isPlayerInitialized = true;
		}
		
		// Checks if all the player's token are in the goal base.
		boolean win=true;
		Integer IDInteger= new Integer(playerID);
		for(Point p: this.board.bases[goalPlayerID]){
			win &= IDInteger.equals(this.board.board.get(p));
		}
		if (win) {
			return (new CCMove(playerID, null, null));
		}
		
		if (!this.listOfMovesToReachBestState.isEmpty() && this.listOfMovesToReachBestState.getFirst() == null) {
			
			// Clears the priority queue since it will be different for every move.
			priorityQueueOfBoardStates.clear();
			
			// Creates and start the thread that will process the current board and create the 
			// priority list of desired states.
			BoardStateProcessor boardStateProcessor = new BoardStateProcessor(this, this.board, mainThreadReference);
			boardStateProcessor.start();
			
			// This block of code will sleep the main thread until one second has elapsed, or will be
			// waked up by the BoardStateProcessor thread.
			try {
				Thread.sleep(850);
			} catch (InterruptedException e) {
				System.out.println("The main thread has been waken up since the computation is over.");
			}
			
			if (!this.priorityQueueOfBoardStates.isEmpty()) {
				// The while loop is present because there can be a situation where the best move is a node
				// where no actions were executed to get there. I obviously cannot return such a move so 
				// I will return the next best valid set of moves.
				do {
					// Pops the best desired state in the priority queue.
					BoardState bestBoardState = this.priorityQueueOfBoardStates.poll();
					listOfMovesToReachBestState = bestBoardState.getListOfMovesToReachState();
				} while(listOfMovesToReachBestState.isEmpty());
				
				return (listOfMovesToReachBestState.removeFirst());
				
			} else {
				// return a NULL move if the game is not over yet.
				// If the priority queue is empty, it means that all the tokens are at the goal zone.
				return (new CCMove(this.playerID, null, null));
			}
		} else {
			// If we are here it means that we have not reached the desired state yet.
			return (listOfMovesToReachBestState.removeFirst());
		}
	}
	
	/**
	 * 
	 * @return The best moves that will be considered for further search.
	 */
	public BoardState[] getBestFourBoardState() {
		synchronized(this.priorityQueueOfBoardStates) {
			BoardState[] boardStates = new BoardState[4];
			boardStates[0] = this.priorityQueueOfBoardStates.poll();
			boardStates[1] = this.priorityQueueOfBoardStates.poll();
			boardStates[2] = this.priorityQueueOfBoardStates.poll();
 			boardStates[3] = this.priorityQueueOfBoardStates.peek();
 			if (boardStates[0] != null) {
 				this.priorityQueueOfBoardStates.add(boardStates[0]);
 			}
 			if (boardStates[1] != null) {
 				this.priorityQueueOfBoardStates.add(boardStates[1]);
 			}
 			if (boardStates[2] != null) {
 				this.priorityQueueOfBoardStates.add(boardStates[2]);
 			}
 			return (boardStates);
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
	public ArrayList<Point> getBorderCellsInGoalZone() {
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
	
	/**
	 * Computes the heuristic value for the given state of the board.
	 * 
	 * @param currentStateTokens The position of all the player's token on the board.
	 * @return The heuristic value of this state.
	 */
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
		
		// The heuristic value will be equal to 0 if the token is in the goal zone.
		if (this.isTokenInBaseOfPlayer(position, goalPlayerID)) {
			return (0);
		} else {
			ArrayList<Point> borderCellsInGoalZone = this.getBorderCellsInGoalZone();
			// Returns the shortest distance between the token and the border of the opponent's base.
			for (int i = 0 ; i < borderCellsInGoalZone.size() ; i++) {
				Point boarderPoint = borderCellsInGoalZone.get(i);
				
				double x = Math.pow((double)(boarderPoint.x-position.x), 2);
				double y = Math.pow((double)(boarderPoint.y-position.y), 2);
				
				double heuristicValue = Math.floor(Math.sqrt(x+y));
				
				if (heuristicValue < smallestDistance) {
					smallestDistance = heuristicValue;
				}
			}
			
			return (smallestDistance);
		}
	}
}
