package project;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import java.util.Random;
import java.util.Stack;

public class Minesweeper extends JFrame {
    private JButton[][] buttons;
    private boolean[][] mines;
    private boolean[][] flagged;
    private int[][] surroundingMines;
    private int uncoveredCells;
    private Timer timer;
    private int secondsElapsed;
    private JLabel timerLabel;
    private Stack<Cell> moveHistory;

    public Minesweeper() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setOpaque(false);

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartGame());

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undoMove());

        topPanel.add(restartButton);
        topPanel.add(undoButton);

        add(topPanel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(10, 10));
        buttons = new JButton[10][10];
        mines = new boolean[10][10];
        flagged = new boolean[10][10];
        surroundingMines = new int[10][10];
        uncoveredCells = 0;
        moveHistory = new Stack<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].addActionListener(new CellClickListener(i, j));
                buttons[i][j].addMouseListener(new CellRightClickListener(i, j));
                gridPanel.add(buttons[i][j]);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        timerLabel = new JLabel("Time: 0 seconds.");
        add(timerLabel, BorderLayout.SOUTH);

        secondsElapsed = 0;
        timer = new Timer(1000, e -> {
            secondsElapsed++;
            timerLabel.setText("Time: " + secondsElapsed + " seconds");
        });

        timer.start();
        pack();
        setVisible(true);

        placeMines();
        countSurroundingMines();
    }
    

    private void placeMines() {
        Random random = new Random();
        int placedMines = 0;
        while (placedMines < 10) {
            int i = random.nextInt(10);
            int j = random.nextInt(10);
            if (!mines[i][j]) {
                mines[i][j] = true;
                placedMines++;
            }
        }
    }

    private void countSurroundingMines() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (!mines[i][j]) {
                    int count = 0;
                    if (i > 0 && mines[i - 1][j]) count++;
                    if (i < 9 && mines[i + 1][j]) count++;
                    if (j > 0 && mines[i][j - 1]) count++;
                    if (j < 9 && mines[i][j + 1]) count++;
                    if (i > 0 && j > 0 && mines[i - 1][j - 1]) count++;
                    if (i < 9 && j < 9 && mines[i + 1][j + 1]) count++;
                    if (i > 0 && j < 9 && mines[i - 1][j + 1]) count++;
                    if (i < 9 && j > 0 && mines[i + 1][j - 1]) count++;
                    surroundingMines[i][j] = count;
                }
            }
        }
    }

    private void uncoverCell(int i, int j) {
        if (mines[i][j]) {
            loseGame();
        } else {
            buttons[i][j].setText(Integer.toString(surroundingMines[i][j]));
            buttons[i][j].setEnabled(false);
            uncoveredCells++;
            moveHistory.push(new Cell(i, j));

            if (uncoveredCells == 90) {
                winGame();
            }
            if (surroundingMines[i][j] == 0) {
                uncoverSurroundingCells(i, j);
            }
        }
    }

    private void uncoverSurroundingCells(int i, int j) {
        if (i > 0 && buttons[i - 1][j].isEnabled()) uncoverCell(i - 1, j);
        if (i < 9 && buttons[i + 1][j].isEnabled()) uncoverCell(i + 1, j);
        if (j > 0 && buttons[i][j - 1].isEnabled()) uncoverCell(i, j - 1);
        if (j < 9 && buttons[i][j + 1].isEnabled()) uncoverCell(i, j + 1);
        if (i > 0 && j > 0 && buttons[i - 1][j - 1].isEnabled()) uncoverCell(i - 1, j - 1);
        if (i < 9 && j < 9 && buttons[i + 1][j + 1].isEnabled()) uncoverCell(i + 1, j + 1);
        if (i > 0 && j < 9 && buttons[i - 1][j + 1].isEnabled()) uncoverCell(i - 1, j + 1);
        if (i < 9 && j > 0 && buttons[i + 1][j - 1].isEnabled()) uncoverCell(i + 1, j - 1);
    }

    private void winGame() {
        timer.stop();
        int option = JOptionPane.showConfirmDialog(this, "You won! Do you want to restart?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    private void loseGame() {
        timer.stop();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (mines[i][j]) {
                    buttons[i][j].setText("💣");
                }
                buttons[i][j].setEnabled(false);
            }
        }
        int option = JOptionPane.showConfirmDialog(this, "Nice try! Do you want to restart?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    private void restartGame() {
        timer.stop();

        secondsElapsed = 0;
        timerLabel.setText("Time: 0 seconds.");

        uncoveredCells = 0;
        moveHistory.clear();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
                flagged[i][j] = false;
                mines[i][j] = false;
                surroundingMines[i][j] = 0;
            }
        }

        placeMines();
        countSurroundingMines();
        timer.start();
    }

    private void undoMove() {
        if (!moveHistory.isEmpty()) {
            Cell lastMove = moveHistory.pop();
            int i = lastMove.i;
            int j = lastMove.j;

            buttons[i][j].setText("");
            buttons[i][j].setEnabled(true);
            uncoveredCells--;
        }
    }

    private void toggleFlag(int i, int j) {
        if (buttons[i][j].isEnabled()) {
            if (flagged[i][j]) {
                buttons[i][j].setText("");
                flagged[i][j] = false;
            } else {
                buttons[i][j].setText("F");
                flagged[i][j] = true;
            }
        }
    }

    private class Cell {
        int i, j;

        public Cell(int i, int j) {
            this.i = i;
            this.j = j;
        }
    }

    private class CellClickListener implements ActionListener {
        private int i;
        private int j;

        public CellClickListener(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public void actionPerformed(ActionEvent e) {
            uncoverCell(i, j);
        }
    }

    private class CellRightClickListener extends MouseAdapter {
        private int i;
        private int j;

        public CellRightClickListener(int i, int j) {
            this.i = i;
            this.j = j;
        }

     
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                toggleFlag(i, j);
            }
        }
    }

    public static void main(String[] args) {
        new Minesweeper();
    }
}
