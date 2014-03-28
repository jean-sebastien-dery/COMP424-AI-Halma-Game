package s260430688;

import halma.CCBoard;
import boardgame.Board;

public class myPlayer3 extends s260430688Player {
	/**
	 * Constructor.
	 * 
	 * @param name My McGill student number that will be used to grade the project.
	 */
	public myPlayer3() {
		// Instantiates my implementation with my McGill ID.
		super("260430683");
	}
	
	/**
	 * Constructor.
	 * 
	 * @param s The student ID.
	 */
	public myPlayer3(String s) {
		super(s);
	}
    
	/**
	 * Creates the board for the current player.
	 */
    public Board createBoard() { return new CCBoard(); }
}
