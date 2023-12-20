from random import shuffle, randint
from functools import partial
import sys

from PyQt6.QtGui import QPixmap
from PyQt6.QtWidgets import QApplication, QWidget, QPushButton, QGridLayout, QComboBox, QFrame, QHBoxLayout, \
    QSizePolicy, QMessageBox
from PyQt6.QtCore import pyqtSlot


class Game:
    sudoku: list[list[int]]

    def clear(self):
        self.sudoku = [[0 for _ in range(9)] for _ in range(9)]

    def is_valid(self, row: int, col: int, val: int) -> bool:
        for i in range(9):
            if self.sudoku[row][i] == val or self.sudoku[i][col] == val:
                return False

        x0 = row - row % 3
        y0 = col - col % 3

        for i in range(3):
            for j in range(3):
                if self.sudoku[i + x0][j + y0] == val:
                    return False

        return True

    def find_empty_cell(self) -> tuple[int, int]:
        for i in range(9):
            for j in range(9):
                if self.sudoku[i][j] == 0:
                    return i, j

    def solve(self) -> bool:
        row: int
        col: int

        cell = self.find_empty_cell()
        if cell is not None:
            row, col = cell
        else:
            return True

        numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9]
        shuffle(numbers)

        for num in numbers:
            if self.is_valid(row, col, num):
                self.sudoku[row][col] = num

                if self.solve():
                    return True

                self.sudoku[row][col] = 0

        return False

    def remove_cell(self, count: int) -> None:
        while count != 0:
            row = randint(0, 8)
            col = randint(0, 8)

            if self.sudoku[row][col] != 0:
                self.sudoku[row][col] = 0
                count -= 1

    def generate_sudoku(self, count: int) -> None:
        self.clear()
        self.solve()
        self.remove_cell(count)

    @staticmethod
    def check_sudoku(sudoku: list[list[int]]) -> bool:
        for i in range(9):
            in_row = [False for i in range(9)]
            in_col = [False for i in range(9)]
            for j in range(9):
                if in_row[sudoku[i][j] - 1]:
                    return False
                else:
                    in_row[sudoku[i][j] - 1] = True
                if in_col[sudoku[j][i] - 1]:
                    return False
                else:
                    in_col[sudoku[j][i] - 1] = True

        for i in range(0, 9, 3):
            for j in range(0, 9, 3):
                in_sub_grid = [False for i in range(9)]
                for k in range(3):
                    for z in range(3):
                        if in_sub_grid[sudoku[k + i][z + j] - 1]:
                            return False
                        else:
                            in_sub_grid[sudoku[k + i][z + j] - 1] = True
        return True


class Window(QWidget):
    game = Game()
    difficulties: QComboBox
    numbers: list
    inputs: list[QPushButton]
    num: str = ''

    def __init__(self) -> None:
        super().__init__()
        # setting the attributes of the window
        self.setWindowTitle("Sudoku game")
        self.setGeometry(100, 100, 600, 600)

        # creating the widgets
        layout = QGridLayout()
        create_button = QPushButton("Create")
        self.difficulties = QComboBox()
        grid_layout = QGridLayout()
        grid = QFrame()
        boxes_layout = [[QGridLayout() for _ in range(3)] for _ in range(3)]
        boxes = [[QFrame() for _ in range(3)] for _ in range(3)]
        self.numbers = [[QPushButton() for _ in range(9)] for _ in range(9)]
        inputs_layout = QGridLayout()
        inputs_holder = QFrame()
        self.inputs = [QPushButton(str(i + 1)) for i in range(9)]
        check_button = QPushButton("Check")

        # setting attributes to the widgets
        create_button.clicked.connect(self.on_create_clicked)

        self.difficulties.addItems(["Easy", "Medium", "Hard"])
        grid_layout.setContentsMargins(0, 0, 0, 0)
        grid_layout.setSpacing(0)
        grid.setLayout(grid_layout)
        for i in range(3):
            for j in range(3):
                boxes_layout[i][j].setContentsMargins(0, 0, 0, 0)
                boxes_layout[i][j].setSpacing(0)
                boxes[i][j].setLayout(boxes_layout[i][j])
                boxes[i][j].setStyleSheet("border: 2px solid black;")
        for i in range(9):
            for j in range(9):
                self.numbers[i][j].clicked.connect(partial(self.on_number_clicked, self.numbers[i][j]))
                self.numbers[i][j].setMinimumSize(50, 50)
                self.numbers[i][j].setSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding)
        inputs_holder.setLayout(inputs_layout)
        for i in range(9):
            self.inputs[i].clicked.connect(partial(self.on_inputs_clicked, self.inputs[i]))
        check_button.clicked.connect(self.on_check_clicked)

        # adding the widgets

        layout.addWidget(create_button, 0, 0)
        layout.addWidget(self.difficulties, 1, 0)
        layout.addWidget(grid, 2, 0)
        for i in range(3):
            for j in range(3):
                grid_layout.addWidget(boxes[i][j], i, j)
        for i in range(3):
            for j in range(3):
                x0 = i * 3
                y0 = j * 3
                for k in range(3):
                    for z in range(3):
                        boxes_layout[i][j].addWidget(self.numbers[k + x0][z + y0], k, z)
        layout.addWidget(inputs_holder, 4, 0)
        for i in range(9):
            inputs_layout.addWidget(self.inputs[i], 0, i)
        layout.addWidget(check_button, 5, 0)

        self.setLayout(layout)

    @pyqtSlot()
    def on_create_clicked(self) -> None:
        difficulty = self.difficulties.currentText()
        if difficulty == "Easy":
            self.game.generate_sudoku(1)
        elif difficulty == "Normal":
            self.game.generate_sudoku(50)
        else:
            self.game.generate_sudoku(60)

        for i in range(9):
            for j in range(9):
                if self.game.sudoku[i][j] != 0:
                    self.numbers[i][j].setText(str(self.game.sudoku[i][j]))
                    self.numbers[i][j].setEnabled(False)
                else:
                    self.numbers[i][j].setText("")
                    self.numbers[i][j].setEnabled(True)

    @pyqtSlot()
    def on_number_clicked(self, button: QPushButton) -> None:
        button.setText(self.num)

    @pyqtSlot()
    def on_inputs_clicked(self, button: QPushButton) -> None:
        for i in range(9):
            self.inputs[i].setStyleSheet("")
        self.num = button.text()
        button.setStyleSheet("background-color:blue")

    @pyqtSlot()
    def on_check_clicked(self) -> None:
        sudoku = [[0 for _ in range(9)] for _ in range(9)]
        for i in range(9):
            for j in range(9):
                try:
                    sudoku[i][j] = int(self.numbers[i][j].text())
                except ValueError:
                    message = QMessageBox(parent=self)
                    message.setIconPixmap(QPixmap(
                        "/Users/simonesamardzhiev/Desktop/My projects/Sudoku Game/python/exclamation mark.png"))
                    message.setText("The sudoku is not filled")
                    message.exec()
                    return

        if Game.check_sudoku(sudoku):
            message = QMessageBox(parent=self)
            message.setIconPixmap(QPixmap(
                "/Users/simonesamardzhiev/Desktop/My projects/Sudoku Game/python/check mark.png"))
            message.setText("The sudoku is solved right")
            message.exec()
        else:
            message = QMessageBox(parent=self)
            message.setIconPixmap(QPixmap(
                "/Users/simonesamardzhiev/Desktop/My projects/Sudoku Game/python/exclamation mark.png"))
            message.setText("The sudoku is not solved right")
            message.exec()
        for i in range(9):
            for j in range(9):
                self.numbers[i][j].setEnabled(False)


if __name__ == "__main__":
    app = QApplication(sys.argv)
    wnd = Window()
    wnd.show()
    sys.exit(app.exec())
