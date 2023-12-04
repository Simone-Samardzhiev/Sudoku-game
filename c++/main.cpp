#include <random>
#include <algorithm>
#include <QApplication>
#include <QWidget>
#include <QGridLayout>
#include <QLineEdit>
#include <QPushButton>
#include <QComboBox>
#include <QFrame>
#include <QIntValidator>


const int SIZE = 9;
const int EMPTY = 0;

class Game {
private:
    std::array<std::array<int, SIZE>, SIZE> sudoku;
    std::array<std::array<int, SIZE>, SIZE> solvedSudoku;

    bool generateSolvedSudoku() {
        int row, col;
        if (!findEmptyCell(row, col)) {
            return true;
        }

        std::array<int, SIZE> numbers{1, 2, 3, 4, 5, 6, 7, 8, 9};
        std::shuffle(numbers.begin(), numbers.end(), std::mt19937(std::random_device()()));

        for (int num: numbers) {
            if (isValidMove(row, col, num)) {
                sudoku[row][col] = num;

                if (generateSolvedSudoku()) {
                    return true;
                }

                sudoku[row][col] = EMPTY;
            }
        }
        return false;
    }

    bool findEmptyCell(int &row, int &col) {
        for (row = 0; row < SIZE; row++) {
            for (col = 0; col < SIZE; col++) {
                if (sudoku[row][col] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    bool isValidMove(int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (sudoku[row][i] == num || sudoku[i][col] == num) {
                return false;
            }
        }

        int boxRow = row - row % 3;
        int boxCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (sudoku[boxRow + i][boxCol + j] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    void deleteValues(int count) {
        std::random_device randomDevice;
        std::mt19937 mt(randomDevice());
        std::uniform_int_distribution<int> generator(0, SIZE - 1);
        while (count) {
            int row = generator(mt);
            int col = generator(mt);

            if (sudoku[row][col] != EMPTY) {
                sudoku[row][col] = EMPTY;
                count--;
            }
        }
    }

    void cleanValues() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                sudoku[i][j] = EMPTY;
            }
        }
    }

public:
    void createGame(int count) {
        this->cleanValues();
        this->generateSolvedSudoku();
        this->solvedSudoku = sudoku;
        this->deleteValues(count);
    }

    const std::array<std::array<int, SIZE>, SIZE> &getSudoku() const {
        return sudoku;
    }

    const std::array<std::array<int, SIZE>, SIZE> &getSolvedSudoku() const {
        return solvedSudoku;
    }
};

class Window : public QWidget {
private:
    Game game;
    QGridLayout *layout = new QGridLayout;
    QLineEdit *inputs[SIZE][SIZE]{};
    QComboBox *difficulties = new QComboBox;

    void renderGrid() {
        auto grid = new QFrame;
        auto gridLayout = new QGridLayout;
        gridLayout->setSpacing(0);
        QFrame *boxes[3][3];
        QGridLayout *layouts[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                boxes[i][j] = new QFrame;
                boxes[i][j]->setStyleSheet("QFrame { border: 2px solid black; }");
                gridLayout->addWidget(boxes[i][j], i, j);
            }
        }

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this->inputs[i][j] = new QLineEdit;
                this->inputs[i][j]->setValidator(new QIntValidator);
                this->inputs[i][j]->setAlignment(Qt::AlignCenter);
                this->inputs[i][j]->setMaxLength(1);
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                layouts[i][j] = new QGridLayout;
                layouts[i][j]->setSpacing(0);
                layouts[i][j]->setVerticalSpacing(0);
                int startRow = i * 3;
                int startCol = j * 3;

                for (int k = 0; k < 3; k++) {
                    for (int z = 0; z < 3; z++) {
                        layouts[i][j]->addWidget(this->inputs[startRow + k][startCol + z], k, z);
                    }
                }
                boxes[i][j]->setLayout(layouts[i][j]);
            }
        }
        grid->setLayout(gridLayout);
        this->layout->addWidget(grid, 2, 0);
    }

private slots:

    void onCreateGamePressed() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this->inputs[i][j]->setText("");
                this->inputs[i][j]->setStyleSheet("");
                this->inputs[i][j]->setReadOnly(false);
            }
        }

        QString difficulty = this->difficulties->currentText();
        if (difficulty == "Easy") {
            this->game.createGame(30);
        } else if (difficulty == "") {
            this->game.createGame(40);
        } else {
            this->game.createGame(50);
        }

        std::array<std::array<int, SIZE>, SIZE> sudoku = this->game.getSudoku();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (sudoku[i][j] != EMPTY) {
                    this->inputs[i][j]->setText(QString("%1").arg(sudoku[i][j]));
                    this->inputs[i][j]->setReadOnly(true);
                }
            }
        }
    };

    void onCheckGamePressed() {
        std::array<std::array<int, SIZE>, SIZE> solvedSudoku = game.getSolvedSudoku();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (this->inputs[i][j]->text() == "" || this->inputs[i][j]->text().toInt() != solvedSudoku[i][j]) {
                    this->inputs[i][j]->setStyleSheet("background-color: red;");
                } else {
                    this->inputs[i][j]->setStyleSheet("background-color: green;");
                }
                this->inputs[i][j]->setReadOnly(true);
            }
        }
    }

public:
    explicit Window(QWidget *parent = nullptr) : QWidget(parent) {

        this->setGeometry(100, 100, 700, 600);
        this->setWindowTitle("Sudoku game");

        auto buttonForCreating = new QPushButton("Create game");
        buttonForCreating->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
        connect(buttonForCreating, &QPushButton::clicked, this, &Window::onCreateGamePressed);
        this->layout->addWidget(buttonForCreating, 0, 0);


        this->difficulties->addItem("Easy");
        this->difficulties->addItem("Hard");
        this->difficulties->addItem("Insane");
        this->difficulties->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
        this->layout->addWidget(difficulties, 1, 0);

        this->renderGrid();

        auto buttonForChecking = new QPushButton("Check");
        buttonForCreating->setSizePolicy(QSizePolicy::Fixed, QSizePolicy::Fixed);
        connect(buttonForChecking, &QPushButton::clicked, this, &Window::onCheckGamePressed);
        this->layout->addWidget(buttonForChecking, 3, 0);

        this->setLayout(this->layout);
    }
};

int main(int argc, char *argv[]) {
    QApplication application(argc, argv);
    Window window;
    window.show();
    return QApplication::exec();
}
