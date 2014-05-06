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
 * @author Jean-Sebastien Dery (260 430 688)
 *
 */
public class BoardStateProcessor extends Thread {
	
	/**
	 * The back pointer that points to the player implementation.
	 */
	private s260430688Player playerBackPointer;
	
	/**
	 * The current board configuration.
	 */
	private CCBoard board;
	
	/**
	 * A reference to the main thread, it is used to wake it up after the computation is over.
	 */
	private Thread mainThreadReference;
	
	/**
	 * Defines the maximum depth to which the states will be explored after the first neighbor generation.
	 */
	private final int MAXIMUM_DEPTH = 6;

	/**
	 * Constructor.
	 * 
	 * @param playerBackPointer The back pointer that points to the player implementation.
	 * @param board The current board configuration.
	 * @param mainThreadReference A reference to the main thread, it is used to wake it up after the computation is over.
	 */
	public BoardStateProcessor(s260430688Player playerBackPointer, CCBoard board, Thread mainThreadReference) {
		this.playerBackPointer = playerBackPointer;
		this.board = board;
		this.mainThreadReference = mainThreadReference;
	}

	@Override
	public void run() {
		ArrayList<Point> allMyTokens = this.board.getPieces(this.playerBackPointer.getColor());
		
		// Process the board for the first level.
		for (Point currentPoint : allMyTokens) {
			double valueOfState = this.playerBackPointer.getHeuristicValueCurrentState(allMyTokens);
			BoardState boardConfiguration = new BoardState(this.playerBackPointer, (CCBoard) board.clone(), currentPoint, new LinkedList<CCMove>(), valueOfState);
			boardConfiguration.exploitState();
		}
		
		this.processMoreIterations();
		
		// Wakes up the main thread when the computation is over.
		this.mainThreadReference.interrupt();
	}
	
	/**
	 * Takes the four best desired states and process them further in order to really determine which one will have the best outcome on the long run.
	 */
	private void processMoreIterations() {
		// Take the four best moves and process them for further iterations.
		for (int i = 0 ; i < MAXIMUM_DEPTH ; i++) {
			BoardState[] fourBestBoardStates = this.playerBackPointer.getBestFourBoardState();
			for (BoardState currentBoardState : fourBestBoardStates) {
				if (currentBoardState != null) {
					currentBoardState.exploitState();
				}
			}
		}
	}
}
