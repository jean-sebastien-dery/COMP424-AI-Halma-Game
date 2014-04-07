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
		
		System.out.println("Will start to analyze moves.");
		
		// Process the board once.
		for (Point currentPoint : allMyTokens) {
			double valueOfState = this.playerBackPointer.getHeuristicValueCurrentState(allMyTokens);
			BoardState boardConfiguration = new BoardState(this.playerBackPointer, (CCBoard) board.clone(), currentPoint, new LinkedList<CCMove>(), valueOfState);
			boardConfiguration.exploitState();
		}
		
//		// Take the two best moves and process them
//		BoardState[] twoBestBoardStates = this.playerBackPointer.getBestTwoBoardState();
//		for (BoardState currentBoardState : twoBestBoardStates) {
//			currentBoardState.exploitState();
//		}
		
		this.mainThreadReference.interrupt();
	}
}
