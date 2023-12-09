from tkinter import Tk, Button, Frame, ttk
from typing import List, Optional, Tuple
import random


class Game:
    sudoku: List[List[int]]
    values: List[int] = [1, 2, 3, 4, 5, 6, 7, 8, 9]

    def is_save(self, row: int, col: int, value: int) -> bool:
        for i in range(9):
            if self.sudoku[row][i] == value or self.sudoku[i][col] == value:
                return False

        start_row: int = row - row % 3
        start_col: int = col - col % 3

        for i in range(3):
            for j in range(3):
                if self.sudoku[i + start_row][j + start_col] == value:
                    return False

        return True

    def find_empty_cell(self) -> Optional[Tuple[int, int]]:
        for i in range(9):
            for j in range(9):
                if self.sudoku[i][j] == 0:
                    return i, j
        return None

    def solve(self) -> bool:
        empty_cell = self.find_empty_cell()
        if not empty_cell:
            return True

        row, col = empty_cell
        random.shuffle(self.values)

        for value in self.values:
            if self.is_save(row, col, value):
                self.sudoku[row][col] = value
                if self.solve():
                    return True
                self.sudoku[row][col] = 0

        return False

    def remove_values(self, count: int) -> None:
        while count > 0:
            row = random.randint(0, 8)
            col = random.randint(0, 8)
            if self.sudoku[row][col] != 0:
                self.sudoku[row][col] = 0
                count -= 1

    def generate_sudoku(self, count: int) -> None:
        self.sudoku = [[0 for _ in range(9)] for _ in range(9)]
        self.solve()
        self.remove_values(count)

    def get_sudoku(self) -> List[List[int]]:
        return self.sudoku

    @staticmethod
    def check_for_rows_cols(grid: List[List[Button]]) -> bool:
        for i in range(9):
            visited_row = [False for _ in range(9)]
            visited_col = [False for _ in range(9)]
            for j in range(9):
                try:
                    index = int(grid[i][j].cget('text'))
                except ValueError:
                    return False

                if visited_row[index - 1] or visited_col[index - 1]:
                    return False
                else:
                    visited_row[index - 1] = True
                    visited_col[index - 1] = True

    @staticmethod
    def check_for_sub_grids(grid: List[List[Button]]) -> bool:
        for row in range(0, 9, 3):
            for col in range(0, 9, 3):
                visited = [False for _ in range(9)]
                for i in range(row, row + 3):
                    for j in range(col, col + 3):
                        try:
                            index = int(grid[i][j].cget('text'))
                        except ValueError:
                            return False
                        if visited[index - 1]:
                            return False
                        else:
                            visited[index - 1] = True

        return True

    @staticmethod
    def check_for_valid_grid(grid: List[List[Button]]):
        return Game.check_for_rows_cols(grid) and Game.check_for_sub_grids(grid)


class Window(Tk):
    createButton: Button
    difficulties: ttk.Combobox
    grid: Frame
    inputs: list[list[Button]]
    checkButton: Button
    numbers: list[Button]
    numbersFrame: Frame
    choice: int
    sudoku: Game

    def __init__(self) -> None:
        super().__init__()
        self.title("Sudoku game")
        self.geometry("500x500")
        self.grid_columnconfigure(0, weight=1)
        self.sudoku = Game()

        self.createButton = Button(self, text="Create game", command=self.create_game_clicked)
        self.createButton.grid(row=0, column=0)

        self.difficulties = ttk.Combobox(self, values=("Easy", "Medium", "Hard"), state="readonly")
        self.difficulties.set("Easy")
        self.difficulties.grid(row=1, column=0)

        self.render_grid()

        self.render_numbers()

        self.checkButton = Button(self, text="Check game", command=self.on_check_press)
        self.checkButton.grid(row=4, column=0)

        self.mainloop()

    def render_grid(self) -> None:
        self.grid = Frame(self, highlightthickness=2, highlightbackground="black")
        self.grid.grid(row=2, column=0)
        boxes = [[Frame(self.grid) for _ in range(3)] for _ in range(3)]
        self.inputs = []

        for i in range(3):
            for j in range(3):
                boxes[i][j].config(highlightthickness=2, highlightbackground="black")
                boxes[i][j].grid(row=i, column=j)
                self.inputs.append([])
                for row_in_box in range(3):
                    for col_in_box in range(3):
                        input_button = Button(boxes[i][j], width=3, height=1)
                        input_button.config(command=lambda button=input_button: self.on_input_press(button))
                        input_button.grid(row=row_in_box, column=col_in_box)
                        self.inputs[-1].append(input_button)

    def render_numbers(self) -> None:
        self.numbersFrame = Frame(self)
        self.numbersFrame.grid(row=3, column=0)
        self.numbers = [Button(self.numbersFrame, text=str(_ + 1)) for _ in range(9)]

        for i in range(len(self.numbers)):
            self.numbers[i].grid(row=0, column=i)
            self.numbers[i].config(command=lambda current=i: self.on_number_press(self.numbers[current]))

    def create_game_clicked(self):
        match self.difficulties.get():
            case "Easy":
                self.sudoku.generate_sudoku(30)
            case "Medium":
                self.sudoku.generate_sudoku(40)
            case "Hard":
                self.sudoku.generate_sudoku(50)

        sudoku = self.sudoku.get_sudoku()

        for i in range(9):
            for j in range(9):
                self.inputs[i][j].config(text="", state="normal", bg="#d9d9d9")
                if sudoku[i][j] != 0:
                    self.inputs[i][j].config(text=str(sudoku[i][j]), state="disabled")

    def on_number_press(self, pressed_button: Button) -> None:
        for number in self.numbers:
            number.config(relief="raised")

        pressed_button.config(relief="sunken")
        self.choice = pressed_button.cget("text")

    def on_input_press(self, button: Button) -> None:
        try:
            button.config(text=self.choice)
        except AttributeError:
            pass

    def on_check_press(self):
        if Game.check_for_valid_grid(self.inputs):
            for i in range(9):
                for j in range(9):
                    self.inputs[i][j].config(state="disabled", bg="green")
        else:
            for i in range(9):
                for j in range(9):
                    self.inputs[i][j].config(state="disabled", bg="red")


def main() -> None:
    Window()


if __name__ == "__main__":
    main()
