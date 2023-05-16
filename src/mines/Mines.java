package mines;

import java.awt.BorderLayout;

import javax.swing.*;

// Source: http://zetcode.com/tutorials/javagamestutorial/minesweeper/
/**
 * The Mines class represents the main application window for the Minesweeper game.
 * It extends the JFrame class and creates the game window.
 */
public class Mines extends JFrame {
	private static final long serialVersionUID = 4772165125287256837L;
	
	private static final int WIDTH = 250;
    private static final int HEIGHT = 290;

    private JLabel statusbar;
    /**
     * Constructs a new Mines object.
     * Sets up the game window and initializes the game board.
     */
    public Mines() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setTitle("Minesweeper");

        statusbar = new JLabel("");
        add(statusbar, BorderLayout.SOUTH);
        // Add an instance of the Board class (the game board) to the frame

        add(new Board(statusbar));

        setResizable(false);
        setVisible(true);
    }
    /**
     * The main method of the Mines class.
     * Creates an instance of the Mines class to start the game.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        // Create an instance of the Mines class to start the game

        new Mines();
    }
}