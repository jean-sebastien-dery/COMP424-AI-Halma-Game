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