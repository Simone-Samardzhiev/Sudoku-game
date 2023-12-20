import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

class RandomArray {
    private final int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final Random random = new Random();

    public RandomArray() {
        this.shuffle();
    }

    public void shuffle() {
        for (int i = 0; i < this.array.length; i++) {
            int index = this.random.nextInt(this.array.length);
            int temp = this.array[i];
            this.array[i] = this.array[index];
            this.array[index] = temp;
        }
    }

    public int getRandomIndex() {
        return this.random.nextInt(this.array.length);
    }

    public int[] getArray() {
        return array;
    }
}

class Game {
    private static final int size = 9;
    private final int[][] sudoku = new int[size][size];

    private final RandomArray array = new RandomArray();
    private int x;
    private int y;

    private void clean() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.sudoku[i][j] = 0;
            }
        }
    }

    private boolean isValid(int row, int col, int val) {
        for (int i = 0; i < size; i++) {
            if (this.sudoku[i][col] == val || this.sudoku[row][i] == val) {
                return false;
            }
        }

        int x0 = row - row % 3;
        int y0 = col - col % 3;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (this.sudoku[i + x0][j + y0] == val) {
                    return false;
                }
            }
        }

        return true;
    }

    private int[] findEmptyCell() {
        int[] result = new int[2];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (this.sudoku[i][j] == 0) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return null;
    }

    private boolean solve() {
        int row;
        int col;
        try {
            int[] coordinates = this.findEmptyCell();
            row = coordinates[0];
            col = coordinates[1];
        } catch (NullPointerException e) {
            return true;
        }

        this.array.shuffle();

        for (int num : this.array.getArray()) {
            if (this.isValid(row, col, num)) {
                this.sudoku[row][col] = num;

                if (this.solve()) {
                    return true;
                }
            }
            this.sudoku[row][col] = 0;
        }
        return false;
    }

    private void removeCells(int count) {
        while (count != 0) {
            int row = array.getRandomIndex();
            int col = array.getRandomIndex();

            if (this.sudoku[row][col] != 0) {
                this.sudoku[row][col] = 0;
                count--;
            }
        }
    }

    public void generateSudoku(int count) {
        this.clean();
        this.solve();
        this.removeCells(count);
    }

    public int[][] getSudoku() {
        return sudoku;
    }

    static boolean checkSudoku(int[][] grid) {
        for (int i = 0; i < size; i++) {
            boolean[] inRow = {false, false, false, false, false, false, false, false, false};
            boolean[] inCol = {false, false, false, false, false, false, false, false, false};
            for (int j = 0; j < size; j++) {
                if (inRow[grid[i][j] - 1]) {
                    return false;
                } else {
                    inRow[grid[i][j] - 1] = true;
                }
                if (inCol[grid[j][i] - 1]) {
                    return false;
                } else {
                    inCol[grid[j][i] - 1] = true;
                }
            }
        }

        for (int i = 0; i < size; i += 3) {
            for (int j = 0; j < size; j += 3) {
                boolean[] inSubGrid = {false, false, false, false, false, false, false, false, false};
                for (int k = i; k < i + 3; k++) {
                    for (int z = j; z < j + 3; z++) {
                        if (inSubGrid[grid[k][z] - 1]) {
                            return false;
                        } else {
                            inSubGrid[grid[k][z] - 1] = true;
                        }
                    }
                }
            }
        }
        return true;
    }
}

class Window extends JFrame {
    private final JComboBox<String> difficulties = new JComboBox<>();
    private final JButton[][] numbers = new JButton[9][9];
    private final JButton[] inputs = new JButton[9];
    private final Game game = new Game();
    private String num = "";

    public Window() {
        super();
        // setting attributes to the window
        this.setSize(800, 800);
        this.setTitle("Sudoku game");
        this.setLayout(new GridBagLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // creating the widgets
        GridBagConstraints layout = new GridBagConstraints();
        JButton buttonCreate = new JButton("Create game");
        JPanel grid = new JPanel();
        JPanel[][] boxes = new JPanel[3][3];
        JPanel inputsHolder = new JPanel();
        JButton buttonCheck = new JButton("Check game");

        // setting attributes to the widgets
        layout.gridx = 0;
        layout.gridy = 0;
        this.difficulties.addItem("Easy");
        this.difficulties.addItem("Normal");
        this.difficulties.addItem("Hard");
        grid.setLayout(new GridBagLayout());
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Border border = BorderFactory.createLineBorder(Color.black);
                boxes[i][j] = new JPanel();
                boxes[i][j].setLayout(new GridBagLayout());
                boxes[i][j].setBorder(border);
            }
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this.numbers[i][j] = new JButton();
                this.numbers[i][j].setPreferredSize(new Dimension(60, 60));
            }
        }
        for (int i = 0; i < this.inputs.length; i++) {
            this.inputs[i] = new JButton(Integer.toString(i + 1));
            this.inputs[i].setBorderPainted(false);
            this.inputs[i].setOpaque(true);
        }

        // adding the actions listeners
        buttonCreate.addActionListener(actionEvent -> Window.this.onCreatePressed());
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                final int x = i;
                final int y = j;
                this.numbers[i][j].addActionListener(actionEvent -> Window.this.onNumberPressed(Window.this.numbers[x][y]));
            }
        }
        for (int i = 0; i < 9; i++) {
            final int index = i;
            inputs[i].addActionListener(actionEvent -> Window.this.onInputPressed(Window.this.inputs[index]));
        }
        buttonCheck.addActionListener(actionEvent -> Window.this.onCheckPressed());

        // adding the widgets
        this.add(buttonCreate, layout);
        layout.gridy++;
        this.add(difficulties, layout);
        layout.gridy++;
        this.add(grid, layout);
        for (int i = 0; i < 3; i++) {
            layout.gridx = i;
            for (int j = 0; j < 3; j++) {
                layout.gridy = j;
                grid.add(boxes[i][j], layout);
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x0 = i * 3;
                int y0 = j * 3;
                for (int k = 0; k < 3; k++) {
                    layout.gridx = k;
                    for (int z = 0; z < 3; z++) {
                        layout.gridy = z;
                        boxes[i][j].add(this.numbers[k + x0][z + y0], layout);
                    }
                }
            }
        }
        layout.gridx = 0;
        layout.gridy = 4;
        this.add(inputsHolder, layout);
        layout.gridy = 0;
        for (int i = 0; i < 9; i++) {
            layout.gridx = i;
            inputsHolder.add(inputs[i], layout);
        }
        layout.gridx = 0;
        layout.gridy = 5;
        this.add(buttonCheck, layout);
    }

    private void onCreatePressed() {
        String difficulty = (String) this.difficulties.getSelectedItem();
        if (Objects.equals(difficulty, "Easy")) {
            this.game.generateSudoku(40);
        } else if (Objects.equals(difficulty, "Normal")) {
            this.game.generateSudoku(50);
        } else {
            this.game.generateSudoku(60);
        }

        final int[][] sudoku = this.game.getSudoku();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sudoku[i][j] == 0) {
                    this.numbers[i][j].setText("");
                    this.numbers[i][j].setEnabled(true);
                } else {
                    this.numbers[i][j].setText(Integer.toString(sudoku[i][j]));
                    this.numbers[i][j].setEnabled(false);
                }
            }
        }
    }

    private void onInputPressed(JButton button) {
        for (int i = 0; i < 9; i++) {
            this.inputs[i].setBackground(null);
        }
        button.setBackground(Color.cyan);
        this.num = button.getText();
    }

    private void onNumberPressed(JButton button) {
        button.setText(this.num);
    }

    private void onCheckPressed() {
        int[][] sudoku = new int[9][9];
        ImageIcon failed = new ImageIcon("exclamation mark.png");
        ImageIcon passed = new ImageIcon("/Users/simonesamardzhiev/Desktop/My projects/Sudoku Game/java/check mark.png");
        JLabel failedLabel = new JLabel();
        failedLabel.setIcon(failed);
        failedLabel.setPreferredSize(new Dimension(150, 150));
        JLabel passedLabel = new JLabel();
        passedLabel.setIcon(passed);
        passedLabel.setPreferredSize(new Dimension(150, 150));

        try {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    sudoku[i][j] = Integer.parseInt(this.numbers[i][j].getText());
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, failedLabel, "You haven't filled the whole grid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (Game.checkSudoku(sudoku)) {
            JOptionPane.showMessageDialog(this, passedLabel, "You solved the suduko right", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, failedLabel, "You did't solved the suduko right", JOptionPane.INFORMATION_MESSAGE);
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this.numbers[i][j].setEnabled(false);
            }
        }
    }

}

class Main {
    public static void main(String[] args) {
        Window window = new Window();
        window.setVisible(true);
    }
}