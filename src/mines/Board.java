package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.SecureRandom;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The Board class represents the game board for a Minesweeper game.
 */
public class Board extends JPanel {
    // Constants for cell states
	private static final long serialVersionUID = 6195235521361212179L;
	
	private static final int NUM_IMAGES = 13;
    private static final int CELL_SIZE = 15;

    public static final int COVER_FOR_CELL = 10;
    private static final int MARK_FOR_CELL = 10;
    public static final int EMPTY_CELL = 0;
    private static final int MINE_CELL = 9;
    public static final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private static final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;
    // Constants for drawing images
    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;
    // Instance variables
    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private transient Image[] img;
    private int mines = 40;
    private int rows = 16;
    private int cols = 16;
    private int allCells;
    private JLabel statusbar;

    public boolean isInGame() {
        return inGame;
    }

    public int getMines() {
        return mines;
    }

    public int getMinesLeft() {
        return minesLeft;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getAllCells() {
        return allCells;
    }

    public int[] getField() {
        return field;
    }
    SecureRandom random = new SecureRandom();

    // Constructors
    public Board(JLabel statusbar) {

        this.statusbar = statusbar;

        img = new Image[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
			img[i] =
                    (new ImageIcon(getClass().getClassLoader().getResource((i)
            			    + ".gif"))).getImage();
        }

        setDoubleBuffered(true);

        addMouseListener(new MinesAdapter());
        newGame();
    }


    /**
     * Initializes a new game.
     */
    public void newGame() {
        initializeGame();
        deployMines();
    }

    /**
     * Initializes the game state.
     */
    public void initializeGame() {
        inGame = true;
        minesLeft = mines;
        allCells = rows * cols;
        field = new int[allCells];
        for (int i = 0; i < allCells; i++) {
            field[i] = COVER_FOR_CELL;
        }
        statusbar.setText(Integer.toString(minesLeft));
    }

    /**
     * Deploys the mines on the game board.
     */
    public void deployMines() {

        int minesDeployed = 0;
        while (minesDeployed < mines) {
            int position = (int) (allCells * random.nextDouble());
            if ((position < allCells) && (field[position] != COVERED_MINE_CELL)) {
                field[position] = COVERED_MINE_CELL;
                minesDeployed++;
                incrementAdjacentCells(position);
            }
        }
    }
    /**
     * Increments the count of adjacent cells for a given mine position.
     *
     * @param position The position of the mine cell.
     */
    public void incrementAdjacentCells(int position) {
        int currentCol = position % cols;

        incrementCellIfValid(position - cols - 1);
        incrementCellIfValid(position - cols);
        incrementCellIfValid(position - cols + 1);
        incrementCellIfValid(position - 1);
        incrementCellIfValid(position + 1);
        incrementCellIfValid(position + cols - 1);
        incrementCellIfValid(position + cols);
        incrementCellIfValid(position + cols + 1);

        if (currentCol < (cols - 1)) {
            incrementCellIfValid(position - cols + 1);
            incrementCellIfValid(position + cols + 1);
            incrementCellIfValid(position + 1);
        }
    }

    private void incrementCellIfValid(int cell) {
        if (isValidCell(cell) && field[cell] != COVERED_MINE_CELL) {
            field[cell]++;
        }
    }
    /**
     * Checks if a cell is a valid cell in the game board.
     *
     * @param cell The cell index to check.
     * @return True if the cell is valid, false otherwise.
     */
    public boolean isValidCell(int cell) {
        return cell >= 0 && cell < allCells;
    }


    /**
     * Finds empty cells and merges them recursively.
     *
     * @param j The starting cell index.
     */
    public void findEmptyCells(int j) {
        int currentCol = j % cols;

        mergeIfValid(j - cols - 1);
        mergeIfValid(j - 1);
        mergeIfValid(j + cols - 1);
        mergeIfValid(j - cols);
        mergeIfValid(j + cols);

        if (currentCol < (cols - 1)) {
            mergeIfValid(j - cols + 1);
            mergeIfValid(j + cols + 1);
            mergeIfValid(j + 1);
        }
    }

    /**
     * Merges the cell with its adjacent cells if the cell is valid.
     *
     * @param cell The cell index.
     */
    private void mergeIfValid(int cell) {
        if (isValidCell(cell) && field[cell] > MINE_CELL) {
            field[cell] -= COVER_FOR_CELL;
            if (field[cell] == EMPTY_CELL) {
                findEmptyCells(cell);
            }
        }
    }




    @Override
    public void paint(Graphics g) {
        int uncover = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int cell = field[(i * cols) + j];
                if (inGame && cell == MINE_CELL) {
                    inGame = false;
                }
                cell = getCellDrawing(cell);
                g.drawImage(img[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
                uncover = updateUncoverCount(uncover, cell);
            }
        }
        updateGameStatus(uncover);
    }
    /**
     * Determines the image index for the given cell.
     *
     * @param cell The cell value.
     * @return The image index.
     */
    private int getCellDrawing(int cell) {
        switch (cell) {
            case COVERED_MINE_CELL:
                return (!inGame) ? DRAW_MINE : DRAW_COVER;
            case MARKED_MINE_CELL:
                return DRAW_MARK;
            default:
                if (!inGame && cell > COVERED_MINE_CELL) {
                    return DRAW_WRONG_MARK;
                } else if (cell > COVERED_MINE_CELL) {
                    return DRAW_MARK;
                } else if (cell > MINE_CELL) {
                    return DRAW_COVER;
                }
                return cell;
        }
    }
    /**
     * Updates the count of uncovered cells.
     *
     * @param uncover The current count of uncovered cells.
     * @param cell    The cell value.
     * @return The updated count of uncovered cells.
     */
    private int updateUncoverCount(int uncover, int cell) {
        if (cell > MINE_CELL) {
            return uncover + 1;
        }
        return uncover;
    }
    /**
     * Updates the game status based on the count of uncovered cells.
     *
     * @param uncover The count of uncovered cells.
     */
    private void updateGameStatus(int uncover) {
        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            statusbar.setText("Game lost");
        }
    }



    /**
     * The MinesAdapter class handles mouse events on the game board.
     */
    class MinesAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;
            boolean rep = false;

            if (!inGame) {
                newGame();
                repaint();
            }

            if (isValidCellClick(x, y)) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    handleRightClick(cRow, cCol);
                    rep = true;
                } else {
                    handleLeftClick(cRow, cCol);
                    rep = true;
                }
            }

            if (rep) {
                repaint();
            }
        }
        /**
         * Checks if the cell click is within the valid bounds.
         *
         * @param x The x-coordinate of the click.
         * @param y The y-coordinate of the click.
         * @return True if the cell click is valid, false otherwise.
         */
        private boolean isValidCellClick(int x, int y) {
            return (x < cols * CELL_SIZE) && (y < rows * CELL_SIZE);
        }

        /**
         * Handles the right click on a cell.
         *
         * @param cRow The row index of the clicked cell.
         * @param cCol The column index of the clicked cell.
         */
        private void handleRightClick(int cRow, int cCol) {
            if (field[(cRow * cols) + cCol] > MINE_CELL) {
                if (field[(cRow * cols) + cCol] <= COVERED_MINE_CELL) {
                    if (minesLeft > 0) {
                        field[(cRow * cols) + cCol] += MARK_FOR_CELL;
                        minesLeft--;
                        statusbar.setText(Integer.toString(minesLeft));
                    } else {
                        statusbar.setText("No marks left");
                    }
                } else {
                    field[(cRow * cols) + cCol] -= MARK_FOR_CELL;
                    minesLeft++;
                    statusbar.setText(Integer.toString(minesLeft));
                }
            }
        }

        /**
         * Handles the left click on a cell.
         *
         * @param cRow The row index of the clicked cell.
         * @param cCol The column index of the clicked cell.
         */
        public void handleLeftClick(int cRow, int cCol) {
            if (field[(cRow * cols) + cCol] > COVERED_MINE_CELL) {
                return;
            }
            if ((field[(cRow * cols) + cCol] > MINE_CELL) &&
                    (field[(cRow * cols) + cCol] < MARKED_MINE_CELL)) {
                field[(cRow * cols) + cCol] -= COVER_FOR_CELL;
                if (field[(cRow * cols) + cCol] == MINE_CELL) {
                    inGame = false;
                }
                if (field[(cRow * cols) + cCol] == EMPTY_CELL) {
                    findEmptyCells((cRow * cols) + cCol);
                }
            }
        }

    }
}