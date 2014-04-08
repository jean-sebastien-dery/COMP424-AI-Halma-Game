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
	
	private s260430688Player playerBackPointer;
	
	private CCBoard board;
	
	private Thread mainThreadReference;

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
		
		System.out.println("**********");
		System.out.println("The first level of the tree has been explored.");
		System.out.println("**********");
		
		// TODO: add the case wher the currentBoardState is equal to NULL.
		
		// Take the two best moves and process them.
		BoardState[] twoBestBoardStates = this.playerBackPointer.getBestTwoBoardState();
		for (BoardState currentBoardState : twoBestBoardStates) {
			if (currentBoardState != null) {
				currentBoardState.exploitState();
			}
		}
		
		System.out.println("**********");
		System.out.println("The second level of the tree has been explored.");
		System.out.println("**********");
		
		
		// Process them again in order to have a total of three moves in advance for the
		// selected best moves.
		for (BoardState currentBoardState : twoBestBoardStates) {
			if (currentBoardState != null) {
				currentBoardState.exploitState();
			}
		}
		
		this.mainThreadReference.interrupt();
	}
}
