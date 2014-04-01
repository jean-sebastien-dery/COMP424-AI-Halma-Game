/**
 * 
 */
package s260430688;

import boardgame.Move;

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