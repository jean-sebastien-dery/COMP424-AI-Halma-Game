/**
 * 
 */
package s260430688;

import halma.CCBoard;
import halma.CCMove;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
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
	 * Defines the number of tokens any player has on the board.
	 */
	private final int NUMBER_OF_TOKEN_PER_PLAYER = 13;
	
	/**
	 * Defines the cost of the path so far and will be used in the value function.
	 */
	private int costOfPathSoFar = 0;
	
	/**
	 * Defines the number of cells that definest the limit of the goal zone.
	 */
	private final int NUMBER_OF_BORDER_CELLS_IN_GOAL_ZONE = 5;
	
	/**
	 * This reference point will be used to compute the heuristic of the value function.
	 */
	private Point[] borderCellsInGoalZone = new Point[NUMBER_OF_BORDER_CELLS_IN_GOAL_ZONE];
	
	/**
	 * The priority queue that will be used to return the best move at the end of a turn.
	 */
	private PriorityQueue<WeightedMove> priorityQueue = null;
	
	/**
	 * This move comparator is used to build a priority queue that is used to select the best move at an iteration.
	 * 
	 * @author Jean-Sebastien Dery (260 430 688)
	 *
	 */
	public class MoveComparator implements Comparator<WeightedMove> {
		@Override
		public int compare(WeightedMove move1, WeightedMove move2) {
			if (move1.moveValue == move2.moveValue) {
				return (0);
			} else if (move1.moveValue > move2.moveValue) {
				return (1);
			} else {
				return (-1);
			}
		}
	}

	/**
	 * A container that will be used for the priority queue and stores useful information for the current move.
	 * 
	 * @author Jean-Sebastien Dery (260 430 688)
	 *
	 */
	public class WeightedMove {
		public int moveValue;
		public Move currentMove;
		public int costOfPathAfterMove;
		
		public WeightedMove(Move currentMove, int moveValue) {
			this.moveValue = moveValue;
			this.currentMove = currentMove;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param name My McGill student number that will be used to grade the project.
	 */
	public s260430688Player() {
		// Instantiates my implementation with my McGill ID.
		super("260430688");
		this.initializePlayer();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param s The student ID.
	 */
	public s260430688Player(String s) {
		super(s);
		this.initializePlayer();
	}
    
	/**
	 * Creates the board for the current player.
	 */
    public Board createBoard() { return new CCBoard(); }
    
    private void initializePlayer() {
    	// Sets the point that will be used for to calculate the heuristic.
    	if (this.getColor() == 0) { // Upper left
    		this.borderCellsInGoalZone[0] = new Point(16, 13);
    		this.borderCellsInGoalZone[1] = new Point(15, 13);
    		this.borderCellsInGoalZone[2] = new Point(14, 14);
    		this.borderCellsInGoalZone[3] = new Point(13, 15);
    		this.borderCellsInGoalZone[4] = new Point(13, 16);
    	} else if (this.getColor() == 1) { // Lower left
    		this.borderCellsInGoalZone[0] = new Point(13, 1);
    		this.borderCellsInGoalZone[1] = new Point(13, 2);
    		this.borderCellsInGoalZone[2] = new Point(3, 3);
    		this.borderCellsInGoalZone[3] = new Point(15, 4);
    		this.borderCellsInGoalZone[4] = new Point(16, 4);
    	} else if (this.getColor() == 2) { // Upper right
    		this.borderCellsInGoalZone[0] = new Point(1, 13);
    		this.borderCellsInGoalZone[1] = new Point(2, 13);
    		this.borderCellsInGoalZone[2] = new Point(3, 14);
    		this.borderCellsInGoalZone[3] = new Point(4, 15);
    		this.borderCellsInGoalZone[4] = new Point(5, 16);
    	} else if (this.getColor() == 3) { // Lower right
    		this.borderCellsInGoalZone[0] = new Point(4, 1);
    		this.borderCellsInGoalZone[1] = new Point(4, 2);
    		this.borderCellsInGoalZone[2] = new Point(3, 3);
    		this.borderCellsInGoalZone[3] = new Point(1, 4);
    		this.borderCellsInGoalZone[4] = new Point(2, 4);
    	}
    }

	@Override
	public Move chooseMove(Board board) {
		// Casts the abstract class into its implementation.
		this.board = (CCBoard) board;
		
		ArrayList<CCMove> myTokenMoves = new ArrayList<CCMove>(30);
		
		this.priorityQueue = new PriorityQueue<WeightedMove>(board.getNumberOfPlayers()*NUMBER_OF_TOKEN_PER_PLAYER, new MoveComparator());
		
		// Gets the list of possible moves for all players given the current configuration of the board.
		ArrayList<CCMove> moves = this.board.getLegalMoves();
		
		// Goes through the set of all possible moves for the current board configuration.
		for (int i = 0 ; i < moves.size() ; i++) {
			CCMove moveForCurrentPiece = moves.get(i);
			
			System.out.println("\nInspecting move number " + (i+1) + " out of " + moves.size() + " possible moves.");
			
			// Verifies if, in the list of possible moves, there is the token that will inform us that we can end the current turn.
			if (moveForCurrentPiece.getFrom() == null || moveForCurrentPiece.getTo() == null) {
				System.out.println("The current move had either its 'from' or 'to' that was null. Ending the turn.");
				return (new CCMove(this.playerID, null, null));
			}
			
			if (CCBoard.getTeamIndex(moveForCurrentPiece.getPlayer_id()) == CCBoard.getTeamIndex(this.getColor())) {
				// The move is either for one of my piece of my partner.
				
				if (moveForCurrentPiece.getPlayer_id() == this.getColor()) {
					
					System.out.println("Evaluating a move that starts at: (" + moveForCurrentPiece.getFrom().x + ":" + moveForCurrentPiece.getFrom().y + ")" + " and ends in (" + moveForCurrentPiece.getTo().x + ":" + moveForCurrentPiece.getTo().y + ")");
					
					if (this.board.getPieceAt(moveForCurrentPiece.getTo()) != null) {
						System.out.println("\t This move is not valid since there is a token at the destination. Skipping it.");
					} else {
						// The move is for one of my piece so I add it to the array list that contains all my possible moves.
						myTokenMoves.add(moveForCurrentPiece);
						
//						int moveValue = this.giveMoveValue(moveForCurrentPiece);
//						this.priorityQueue.add(new WeightedMove(moveForCurrentPiece, moveValue));
					}
					
				} else {
					// The move is for my partner.
//					System.out.println("Skipping a move that is for my partner.");
				}
				
			} else {
				// The move is for one of the opponent.
//				System.out.println("Skipping a move that is for my opponent.");
			}
		}
		
		// At this point in the code, I have the list of all the valid moves for my tokens. I need to calculate the current heuristic of all
		// the moves. The heuristic will be the summation of the Manhattan distance between the token and the closest cell in the goal zone.
		
		// Here I am going through all my possible move and computing the current heuristic 		
		
		
		// After I have built the priority queue, I need to make a decision so I take the move that has the lowest value in the queue.
		
		WeightedMove moveToReturn = this.priorityQueue.peek();
		this.costOfPathSoFar += moveToReturn.costOfPathAfterMove;
		
		System.out.println("The value of the move returned is " + moveToReturn.moveValue);
		
		return (moveToReturn.currentMove);
	}
	
	/**
	 * Will give the value of the current move based on the value function f(x) = g(x) + h(x)
	 * 
	 * @param currentMove The move to evaluate.
	 * @return The value of the current move.
	 */
	private int giveMoveValue(CCMove currentMove) {
		// Here I increment the cost of path by one because I want to encourage moves
		// that goes over another token.
		
		int travelledDistance = giveTravelledDistanceForMove(currentMove.getFrom(), currentMove.getTo());
		
		int costOfTravell;
		if (travelledDistance == 1) {
			costOfTravell = 2;
		} else if (travelledDistance == 2) {
			costOfTravell = 1;
		} else {
			costOfTravell = 2;
		}
		
		int moveValue = (this.costOfPathSoFar + costOfTravell) + this.calculateHeuristic(currentMove.getTo());
		
		System.out.println("\t The cost of travell is " + costOfTravell);
		System.out.println("\t The move value is " + moveValue);
		
		return (moveValue);
	}
	
	/**
	 * Returns the traveled distance for a given move.
	 * 
	 * @param currentMove The move to analyze.
	 * @return The distance the token will travel after the move will be executed.
	 */
	private int giveTravelledDistanceForMove(Point start, Point end) {
		double x = Math.pow((double)(start.x-end.x), 2);
		double y = Math.pow((double)(start.y-end.y), 2);
		
		int travelledDistance = (int) Math.floor(Math.sqrt(x+y));
		
		System.out.println("\t The travelled distance will be " + travelledDistance);

		return (travelledDistance);
	}
	
	/**
	 * Will return the heuristic value for the current move.
	 * 
	 * @param currentMove The move to evaluate.
	 * @return The value of the heuristic for the current move.
	 */
	private int calculateHeuristic(Point position) {
		
		// The heuristic that I am using is the Manhattan distance between the 
		// current piece and the closest cell in the goal zone.
		
		// Initializes the 'smallestDistance' variable to an impossible value given the size of the board.
		int smallestDistance = 100;
		for (int i = 0 ; i < this.borderCellsInGoalZone.length ; i++) {
			Point boarderPoint = this.borderCellsInGoalZone[i];
			
			double x = Math.pow((double)(boarderPoint.x-position.x), 2);
			double y = Math.pow((double)(boarderPoint.y-position.y), 2);
			
			int heuristicValue = (int) Math.floor(Math.sqrt(x+y));
			
			if (heuristicValue < smallestDistance) {
				smallestDistance = heuristicValue;
			}
		}
		
		System.out.println("\t The heursitic value is " + smallestDistance);

		return (smallestDistance);
	}
	
	// Try to have a heuristic that would be the combined distance of all the pieces
	// from their current position to the nearest position of the goal state.
	
	// The strategy will be to first do the MiniMax (with pruning) tree and then do the
	// Monte Carlo search.
}
