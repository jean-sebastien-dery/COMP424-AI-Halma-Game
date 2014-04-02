/**
 * 
 */
package s260430688;

import java.util.Comparator;

/**
 * This move comparator is used to build a priority queue that is used to select the best move at an iteration.
 * 
 * @author Jean-Sebastien Dery (260 430 688)
 *
 */
public class BoardStateComparator implements Comparator<BoardState> {
	@Override
	public int compare(BoardState configuration1, BoardState configuration2) {
		if (Math.abs(configuration1.valueOfState - configuration2.valueOfState) < 0.01) {
			return (0);
		} else if (configuration1.valueOfState > configuration2.valueOfState) {
			return (1);
		} else {
			return (-1);
		}
	}
}