package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.SecureRandom;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Board extends JPanel {
	private static final long serialVersionUID = 6195235521361212179L;
	
	private static final int NUM_IMAGES = 13;
    private static final int CELL_SIZE = 15;

    private static final int COVER_FOR_CELL = 10;
    private static final int MARK_FOR_CELL = 10;
    private static final int EMPTY_CELL = 0;
    private static final int MINE_CELL = 9;
    private static final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private static final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;

    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private transient Image[] img;
    private int mines = 40;
    private int rows = 16;
    private int cols = 16;
    private int allCells;
    private JLabel statusbar;
    SecureRandom random = new SecureRandom();


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


    public void newGame() {
        initializeGame();
        deployMines();
    }

    private void initializeGame() {
        inGame = true;
        minesLeft = mines;
        allCells = rows * cols;
        field = new int[allCells];
        for (int i = 0; i < allCells; i++) {
            field[i] = COVER_FOR_CELL;
        }
        statusbar.setText(Integer.toString(minesLeft));
    }

    private void deployMines() {

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

    private void incrementAdjacentCells(int position) {
        int currentCol = position % cols;

        incrementCellIfValid(position - 1 - cols);
        incrementCellIfValid(position - 1);
        incrementCellIfValid(position + cols - 1);
        incrementCellIfValid(position - cols);
        incrementCellIfValid(position + cols);

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

    private boolean isValidCell(int cell) {
        return cell >= 0 && cell < allCells;
    }



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

    private int updateUncoverCount(int uncover, int cell) {
        if (cell > MINE_CELL) {
            return uncover + 1;
        }
        return uncover;
    }

    private void updateGameStatus(int uncover) {
        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            statusbar.setText("Game lost");
        }
    }



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

        private boolean isValidCellClick(int x, int y) {
            return (x < cols * CELL_SIZE) && (y < rows * CELL_SIZE);
        }

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

        private void handleLeftClick(int cRow, int cCol) {
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