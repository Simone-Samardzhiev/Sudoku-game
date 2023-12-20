#include <random>
#include <QApplication>
#include <QWidget>
#include <QPushButton>
#include <QMessageBox>
#include <QGridLayout>
#include <QComboBox>
#include <QString>
#include <QFrame>

class Game {
private:
    std::array<std::array<int, 9>, 9> sudoku{};
    const int size = 9;

    void clear() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this->sudoku[i][j] = 0;
            }
        }
    }

    bool checkIsValid(int row, int col, int val) {
        for (int i = 0; i < size; i++) {
            if (this->sudoku[i][col] == val || this->sudoku[row][i] == val) {
                return false;
            }
        }

        int x0 = row - row % 3;
        int y0 = col - col % 3;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (this->sudoku[i + x0][j + y0] == val) {
                    return false;
                }
            }
        }
        return true;
    }

    bool findEmptyCell(int &row, int &col) {
        for (row = 0; row < size; row++) {
            for (col = 0; col < size; col++) {
                if (this->sudoku[row][col] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    bool solve() {
        int row;
        int col;

        if (!this->findEmptyCell(row, col)) {
            return true;
        }

        int numbers[9] = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        std::shuffle(numbers, numbers + 9, std::mt19937(std::random_device()()));

        for (int num: numbers) {
            if (this->checkIsValid(row, col, num)) {
                this->sudoku[row][col] = num;
                if (solve()) {
                    return true;
                }
                this->sudoku[row][col] = 0;
            }
        }
        return false;
    }

    void removeCells(int count) {
        std::random_device randomDevice;
        std::mt19937 mt(randomDevice());
        std::uniform_int_distribution<int> generator(0, size - 1);

        while (count) {
            int row = generator(mt);
            int col = generator(mt);

            if (this->sudoku[row][col] != 0) {
                this->sudoku[row][col] = 0;
                count--;
            }
        }
    }


public:
    void generateSudoku(int count) {
        this->clear();
        this->solve();
        this->removeCells(count);
    }

    std::array<std::array<int, 9>, 9> getSudoku() {
        return this->sudoku;
    }

    static bool checkSudoku(int grid[9][9]) {
        for (int i = 0; i < 9; i++) {
            bool inRow[9] = {false, false, false, false, false, false, false, false, false};
            bool inCol[9] = {false, false, false, false, false, false, false, false, false};

            for (int j = 0; j < 9; j++) {
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

        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) {
                bool inSubGrid[9] = {false, false, false, false, false, false, false, false, false};

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
};

class Window : public QWidget {
private:
    Game game;
    QGridLayout *mainLayout;
    QPushButton *buttonForCreating;
    QComboBox *difficulties;
    QGridLayout *gridLayout;
    QFrame *grid;
    QGridLayout *boxesLayout[3][3]{};
    QFrame *boxes[3][3]{};
    QPushButton *numbers[9][9]{};
    QGridLayout *inputsLayout;
    QFrame *inputsHolder;
    QPushButton *inputs[9]{};
    QPushButton *buttonForChecking;
    QString num = "";
private slots:

    void on_number_clicked(QPushButton *button) {
        button->setText(num);
    };

    void on_input_clicked(QPushButton *button) {
        for (int i = 0; i < 9; i++) {
            this->inputs[i]->setStyleSheet("");
        }
        button->setStyleSheet("background-color: blue");
        this->num = button->text();
    }

    void on_create_clicked() {
        QString difficulty = this->difficulties->currentText();
        if (difficulty == "Easy") {
            game.generateSudoku(40);
        } else if (difficulty == "Normal") {
            game.generateSudoku(50);
        } else {
            game.generateSudoku(60);
        }
        std::array<std::array<int, 9>, 9> sudoku = game.getSudoku();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sudoku[i][j] == 0) {
                    this->numbers[i][j]->setText("");
                    this->numbers[i][j]->setEnabled(true);
                } else {
                    this->numbers[i][j]->setText(QString("%1").arg(sudoku[i][j]));
                    this->numbers[i][j]->setEnabled(false);
                }
            }
        }
    }

    void on_checked_clicked() {
        int sudoku[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudoku[i][j] = this->numbers[i][j]->text().toInt();
                if (sudoku[i][j] == 0) {
                    QMessageBox msgBox(QMessageBox::Warning, "Sudoku result", "You didn't solve the sudoku completely",
                                       QMessageBox::Ok, this);
                    msgBox.setIconPixmap(
                            QPixmap("/Users/simonesamardzhiev/Desktop/My projects/Sudoku Game/c++/exclamation mark.png"));
                    msgBox.exec();
                    return;
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this->numbers[i][j]->setEnabled(false);
            }
        }
        if (Game::checkSudoku(sudoku)) {
            QMessageBox msgBox(QMessageBox::Warning, "Sudoku result", "You solved the sudoku right",
                               QMessageBox::Ok, this);
            msgBox.setIconPixmap(
                    QPixmap("/Users/simonesamardzhiev/Desktop/My projects/Sudoku Game/c++/check mark.png"));
            msgBox.exec();
        } else {
            QMessageBox msgBox(QMessageBox::Warning, "Sudoku result", "You didn't solve the sudoku right",
                               QMessageBox::Ok, this);
            msgBox.setIconPixmap(
                    QPixmap("/Users/simonesamardzhiev/Desktop/My projects/Sudoku Game/c++/exclamation mark.png"));
            msgBox.exec();
        }
    }

public:
    explicit Window(QWidget *parent = nullptr) : QWidget(parent) {
        // setting window attributes
        this->setWindowTitle("Sudoku game");
        this->setFixedSize(700, 700);

        // creating the widgets
        this->mainLayout = new QGridLayout;
        this->buttonForCreating = new QPushButton("Create game");
        this->difficulties = new QComboBox;
        this->gridLayout = new QGridLayout;
        this->grid = new QFrame;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this->numbers[i][j] = new QPushButton;
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this->boxesLayout[i][j] = new QGridLayout;
                this->boxes[i][j] = new QFrame;
            }
        }
        this->inputsLayout = new QGridLayout;
        this->inputsHolder = new QFrame;
        for (int i = 0; i < 9; i++) {
            this->inputs[i] = new QPushButton(QString("%1").arg(i + 1));
        }
        this->buttonForChecking = new QPushButton("Check");

        // setting attributes to the widgets
        connect(this->buttonForCreating, &QPushButton::clicked, [=, this]() {
            this->on_create_clicked();
        });
        this->difficulties->addItem("Easy");
        this->difficulties->addItem("Normal");
        this->difficulties->addItem("Hard");
        this->gridLayout->setSpacing(0);
        this->grid->setLayout(this->gridLayout);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this->boxesLayout[i][j]->setVerticalSpacing(0);
                this->boxesLayout[i][j]->setHorizontalSpacing(0);
                this->boxesLayout[i][j]->setContentsMargins(5, 0, 5, 0);
                this->boxes[i][j]->setStyleSheet("QFrame { border: 1px solid black; }");
                this->boxes[i][j]->setLayout(this->boxesLayout[i][j]);
            }
        }
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                this->numbers[i][j]->setMinimumSize(60, 60);
                connect(this->numbers[i][j], &QPushButton::clicked, [=, this]() {
                    this->on_number_clicked(this->numbers[i][j]);
                });
            }
        }
        this->inputsHolder->setLayout(this->inputsLayout);
        for (int i = 0; i < 9; i++) {
            connect(this->inputs[i], &QPushButton::clicked, [=, this]() {
                this->on_input_clicked(this->inputs[i]);
            });
        }
        connect(this->buttonForChecking, &QPushButton::clicked, [=, this]() {
            this->on_checked_clicked();
        });
        // adding the widgets;
        this->mainLayout->addWidget(this->buttonForCreating, 0, 0);
        this->mainLayout->addWidget(this->difficulties, 1, 0);
        this->mainLayout->addWidget(this->grid, 2, 0);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this->gridLayout->addWidget(this->boxes[i][j], i, j);
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int x0 = i * 3;
                int y0 = j * 3;
                for (int k = 0; k < 3; k++) {
                    for (int z = 0; z < 3; z++) {
                        this->boxesLayout[i][j]->addWidget(this->numbers[k + x0][z + y0], k, z);
                    }
                }
            }
        }
        this->mainLayout->addWidget(inputsHolder, 3, 0);
        for (int i = 0; i < 9; i++) {
            this->inputsLayout->addWidget(this->inputs[i], 0, i);
        }
        this->mainLayout->addWidget(buttonForChecking, 4, 0);
        this->setLayout(this->mainLayout);
    }
};

int main(int argc, char *argv[]) {
    QApplication application = QApplication(argc, argv);
    Window window;
    window.show();
    return QApplication::exec();
}