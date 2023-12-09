import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Objects;
import java.util.Random;

class RandomArray {
    public RandomArray() {
        this.shuffle();
    }

    private final Random random = new Random();
    private final int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9};

    public void shuffle() {
        for (int i = 0; i < arr.length; i++) {
            int index = random.nextInt(arr.length);
            int temp = arr[index];
            arr[index] = arr[i];
            arr[i] = temp;
        }
    }

    public int[] getArr() {
        return arr;
    }
}

class Game {
    private final int EMPTY = 0;
    private final int SIZE = 9;

    private final RandomArray numbers = new RandomArray();
    private final int[][] sudoku = new int[SIZE][SIZE];


    private boolean isValid(int row, int col, int value) {
        for (int i = 0; i < SIZE; i++) {
            if (this.sudoku[i][col] == value || this.sudoku[row][i] == value) {
                return false;
            }
        }

        int startRow = row - row % 3;
        int startCol = col - col % 3;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (this.sudoku[i + startRow][j + startCol] == value) {
                    return false;
                }
            }
        }

        return true;
    }

    private int[] findEmptyCell() {
        int[] result = new int[2];

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (this.sudoku[i][j] == EMPTY) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }

        return null;
    }

    private boolean solve() {
        int[] emptyCellCoordinates = findEmptyCell();
        int row;
        int col;
        try {
            assert emptyCellCoordinates != null;
            row = emptyCellCoordinates[0];
            col = emptyCellCoordinates[1];
        } catch (NullPointerException e) {
            return true;
        }
        numbers.shuffle();
        for (int number : this.numbers.getArr()) {
            if (isValid(row, col, number)) {
                this.sudoku[row][col] = number;
                if (this.solve()) {
                    return true;
                }
            }
            this.sudoku[row][col] = EMPTY;
        }
        return false;
    }

    private void clear() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this.sudoku[i][j] = EMPTY;
            }
        }
    }

    private void removeCells(int count) {
        Random random = new Random();
        while (count > 0) {
            int row = random.nextInt(SIZE);
            int col = random.nextInt(SIZE);

            if (this.sudoku[row][col] != EMPTY) {
                this.sudoku[row][col] = EMPTY;
                count--;
            }
        }
    }

    public void generateSudoku(int count) {
        this.clear();
        this.solve();
        this.removeCells(count);
    }

    public int[][] getSudoku() {
        return sudoku;
    }

    private static boolean checkRowsAndCols(JButton[][] grid) {
        for (int i = 0; i < 9; i++) {
            boolean[] visitedRow = new boolean[9];
            boolean[] visitedCol = new boolean[9];
            for (int j = 0; j < 9; j++) {
                try {
                    if (visitedRow[Integer.parseInt(grid[i][j].getText()) - 1] || visitedCol[Integer.parseInt(grid[j][i].getText()) - 1]) {
                        return false;
                    } else {
                        visitedRow[Integer.parseInt(grid[i][j].getText()) - 1] = true;
                        visitedCol[Integer.parseInt(grid[j][i].getText()) - 1] = true;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }

        }

        return true;
    }

    private static boolean checkSubGrids(JButton[][] grid) {
        for (int row = 0; row < 9; row += 3) {
            for (int col = 0; col < 9; col += 3) {
                boolean[] visited = new boolean[9];
                for (int i = row; i < row + 3; i++) {
                    for (int j = col; j < col + 3; j++) {
                        try {
                            int value = Integer.parseInt(grid[i][j].getText()) - 1;
                            if (visited[value]) {
                                return false;
                            } else {
                                visited[value] = true;
                            }
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkSolution(JButton[][] grid) {
        return checkRowsAndCols(grid) && checkSubGrids(grid);
    }
}

class Window extends JFrame {

    private final Game game = new Game();
    private final JButton[][] inputs = new JButton[9][9];
    private String choice;

    public Window() {
        super();
        this.setSize(600, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new GridBagLayout());

        GridBagConstraints layout = new GridBagConstraints();
        layout.gridx = 0;
        layout.gridy = 0;

        JButton createButton = new JButton("Create game");
        this.add(createButton, layout);

        layout.gridy = 1;

        JComboBox<String> difficulties = new JComboBox<>();
        difficulties.addItem("Easy");
        difficulties.addItem("Medium");
        difficulties.addItem("Hard");
        this.add(difficulties, layout);

        createButton.addActionListener(e -> {
            switch ((String) Objects.requireNonNull(difficulties.getSelectedItem())) {
                case "Easy": {
                    Window.this.game.generateSudoku(30);
                }
                case "Medium": {
                    Window.this.game.generateSudoku(40);
                }
                case "Hard": {
                    Window.this.game.generateSudoku(50);
                }
            }
            int[][] sudoku = game.getSudoku();

            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    Window.this.inputs[i][j].setEnabled(true);
                    Window.this.inputs[i][j].setText("");
                    Window.this.inputs[i][j].setBackground(null);
                    if (sudoku[i][j] != 0) {
                        Window.this.inputs[i][j].setText(Integer.toString(sudoku[i][j]));
                        Window.this.inputs[i][j].setEnabled(false);
                    }
                }
            }
        });

        layout.gridy = 4;

        JButton checkButton = new JButton("Check");
        checkButton.addActionListener(e -> {
            if (Game.checkSolution(Window.this.inputs)) {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        Window.this.inputs[i][j].setBackground(Color.green);
                    }
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        Window.this.inputs[i][j].setBackground(Color.red);
                    }
                }
            }
        });
        this.add(checkButton, layout);

        this.renderGrid();

        this.renderNumbers();

        this.setVisible(true);
    }

    private void renderGrid() {
        GridBagConstraints layout = new GridBagConstraints();
        JPanel grid = new JPanel();
        JPanel[][] boxes = new JPanel[3][3];

        layout.gridx = 0;
        layout.gridy = 2;
        grid.setLayout(new GridBagLayout());
        this.add(grid, layout);

        layout.gridx = 0;
        layout.gridy = 2;

        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes[i].length; j++) {
                boxes[i][j] = new JPanel();
                boxes[i][j].setLayout(new GridBagLayout());
                boxes[i][j].setBorder(new LineBorder(Color.BLACK));
                layout.gridx = i;
                layout.gridy = j;
                grid.add(boxes[i][j], layout);

                int startRow = i * 3;
                int startCol = j * 3;

                for (int k = 0; k < 3; k++) {
                    for (int z = 0; z < 3; z++) {
                        layout.gridx = k;
                        layout.gridy = z;
                        inputs[k + startRow][z + startCol] = new JButton();
                        inputs[k + startRow][z + startCol].setPreferredSize(new Dimension(45, 45));
                        int finalZ = z;
                        int finalK = k;
                        inputs[k + startRow][z + startCol].addActionListener(e -> {
                            JButton button = inputs[finalK + startRow][finalZ + startCol];
                            button.setText(Window.this.choice);
                        });
                        boxes[i][j].add(inputs[k + startRow][z + startCol], layout);
                    }
                }
            }
        }
    }

    private void renderNumbers() {
        GridBagConstraints layout = new GridBagConstraints();
        JPanel panel = new JPanel();
        JButton[] buttons = new JButton[9];

        layout.gridx = 0;
        layout.gridy = 3;

        this.add(panel, layout);

        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton(Integer.toString(i + 1));
            int finalI = i;
            buttons[i].addActionListener(e -> {
                for (JButton button : buttons) {
                    button.setBackground(null);
                }

                Window.this.choice = buttons[finalI].getText();
                buttons[finalI].setBackground(Color.cyan);
            });
            layout.gridy = i;
            panel.add(buttons[i], layout);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        new Window();
    }
}