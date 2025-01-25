#ifndef OOP5_MOVEVEHICLE_H
#define OOP5_MOVEVEHICLE_H

// ===============================
// ========== INCLUDES ===========
// ===============================
// Including necessary headers that define various types and utilities
#include "CellType.h"    // Defines the different types of cells that can exist on the board
#include "Direction.h"   // Defines the possible directions (UP, DOWN, LEFT, RIGHT) for movement
#include "GameBoard.h"   // Defines the structure of the game board
#include "Utilities.h"   // Provides utility functions for operations like GetAtIndex, SetAtIndex, etc.
#include "TransposeList.h" // Utility to transpose a list, used for moving cars UP and DOWN by reinterpreting rows as columns
#include <iostream>      // Standard I/O library, used for debugging or outputting information

// ===============================
// ========== MOVE STRUCT ========
// ===============================
// Move struct represents a movement command for a vehicle on the board
// The struct is templated with the type of the vehicle (t_type), the direction of the movement (d_dir),
// and the number of steps to move the vehicle (a_amount).
// This struct is used to encapsulate a movement operation as a compile-time constant.
template<CellType t_type, Direction d_dir, int a_amount>
struct Move {
    // ===============================
    // ========== STATIC ASSERTIONS ==
    // ===============================
    // Ensuring that the move is valid at compile time using static assertions

    // Ensure that the number of steps (a_amount) is non-negative, because you can't move a vehicle a negative distance
    static_assert(a_amount >= 0, "Invalid move: amount of steps must be non negative");

    // Ensure that the cell type is not EMPTY, because an empty cell doesn't represent a vehicle that can be moved
    static_assert((t_type != EMPTY), "Invalid move: EMPTY CellType cannot be moved");

    // ===============================
    // ========== CONSTANTS ==========
    // ===============================
    // Compile-time constants representing the type, direction, and amount for this movement

    static constexpr CellType type = t_type;       // Type of the vehicle being moved (e.g., RED, BLUE)
    static constexpr Direction direction = d_dir; // Direction of movement (e.g., UP, DOWN, LEFT, RIGHT)
    static constexpr int amount = a_amount;       // Number of steps to move the vehicle
};

// ===============================
// ========== USING NAMESPACE ====
// ===============================
// Using the standard namespace so we can use standard I/O and other standard utilities without prefixing them with std::
using namespace std;

// =============================================
// ========== FIND CAR HELPER STRUCT ===========
// =============================================
// Find_Car_Helper is a recursive struct that helps locate a specific car on the game board.
// It scans the board from the top-left corner (row 0, col 0) to the bottom-right corner.
// The search proceeds row by row, column by column until the car is found.
// The struct is templated with the car type to find, the type of the current cell being examined,
// the current row and column, a boolean flag indicating whether the search is done, and the board type.
template <CellType type, CellType type2, int row, int col, bool done, typename B>
struct Find_Car_Helper {
    // =============================================
    // ========== TYPE DEFINITIONS ================
    // =============================================
    // Define a type alias for the main list of the board, which represents the 2D array of cells
    typedef typename GameBoard<B>::board mainList;

    // =============================================
    // ========== CONSTANTS ========================
    // =============================================
    // Define constants to check various conditions during the search

    // Check if the current row is the last row on the board
    static constexpr bool last_row = (mainList::size == row + 1);

    // Check if the current cell contains the car we're looking for
    static constexpr bool found = (type == type2);

    // Check if we're currently on the last cell of the entire board
    static constexpr bool last_cell_in_board = (last_row && (col + 1 == GameBoard<B>::width));

    // =============================================
    // ========== STATIC ASSERTIONS ================
    // =============================================
    // If the car is not found by the time we reach the last cell, trigger a compile-time error
    static_assert(!(!found && last_cell_in_board), "Type was not found!");

    // =============================================
    // ========== NEXT CELL CALCULATION ============
    // =============================================
    // Determine the row and column indices for the next cell to examine
    // If we're at the end of the current row, move to the start of the next row
    static constexpr int next_row = ConditionalInteger<col + 1 == GameBoard<B>::width, ConditionalInteger<last_cell_in_board, row, row + 1>::value, row>::value;
    static constexpr int next_col = ConditionalInteger<col + 1 == GameBoard<B>::width, 0, ConditionalInteger<last_cell_in_board, col, col + 1>::value>::value;

    // =============================================
    // ========== TYPE DEFINITIONS (NEXT CELL) =====
    // =============================================
    // Define type aliases for the next row's list and the type of the next cell
    typedef typename GetAtIndex<next_row, typename GameBoard<B>::board>::value next_row_list; // Get the list representing the next row
    typedef typename GetAtIndex<next_col, next_row_list>::value next_cell;                   // Get the type of the next cell

    // =============================================
    // ========== RECURSIVE SEARCH =================
    // =============================================
    // Continue searching for the car in the next cell recursively
    typedef Find_Car_Helper<type, next_cell::type, next_row, next_col, found, B> next_helper;

    // =============================================
    // ========== FINAL COORDINATES ================
    // =============================================
    // If the car is found, store the current coordinates; otherwise, continue searching
    static constexpr int X_row = ConditionalInteger<found, row, next_helper::X_row >::value; // Row index where the car was found
    static constexpr int X_col = ConditionalInteger<found, col, next_helper::X_col >::value; // Column index where the car was found
};

// =============================================
// ========== FIND CAR HELPER SPECIALIZATION ===
// =============================================
// This specialization of Find_Car_Helper is used when the car has been found (done == true).
// It simply stores the coordinates of the car's location on the board.
template <CellType type, CellType type2, int row, int col, typename B>
struct Find_Car_Helper<type, type2, row, col, true, B> {
    // Constants to store the coordinates of the car's location
    static constexpr int X_row = row; // Row index where the car was found
    static constexpr int X_col = col; // Column index where the car was found
};

// =============================================
// ========== FIND CAR STRUCT ==================
// =============================================
// FindCar is a wrapper around Find_Car_Helper that provides a simple interface to find a car on the board.
// It locates the first cell of the specified car type and provides the coordinates of the car's location.
// This struct is used to start the search process and retrieve the results.
template<CellType type, typename Bo>
struct FindCar {
    // =============================================
    // ========== TYPE DEFINITIONS =================
    // =============================================
    // Define type aliases for the game board and main list
    typedef Bo game_board;                 // The game board we're working with
    typedef typename game_board::board mainList; // The main list representing the 2D array of cells

    // =============================================
    // ========== INITIAL CELL =====================
    // =============================================
    // Get the type of the first cell in the main list (top-left corner of the board)
    typedef typename mainList::head::head first_cell;

    // =============================================
    // ========== FIND CAR =========================
    // =============================================
    // Use Find_Car_Helper to locate the car and store its coordinates
    typedef Find_Car_Helper<type, first_cell::type, 0, 0, false, mainList> car_loc;

    // =============================================
    // ========== CAR COORDINATES ==================
    // =============================================
    // Store the row and column indices where the car was found
    static constexpr int X_row_idx = car_loc::X_row; // Row index of the car's location
    static constexpr int X_col_idx = car_loc::X_col; // Column index of the car's location
};

// =============================================
// ========== DIRECTION STRUCT =================
// =============================================
// Dir is a struct that calculates the coordinates of the furthest end of a car
// in a given direction of movement. It is templated with the direction, starting
// row and column indices, and the length of the car.
//
// Parameters:
// - c: The direction of movement (e.g., UP, DOWN, LEFT, RIGHT)
// - Ro: The starting row index of the car
// - Col: The starting column index of the car
// - len: The length of the car
template<Direction c, int Ro, int Col, int len>
struct Dir {};

// =============================================
// ========== DIRECTION SPECIALIZATION: RIGHT ==
// =============================================
// This specialization of Dir is used to calculate the furthest end of a car
// when moving to the RIGHT. It increments the column index by the car's length.
template<int Ro, int Col, int len>
struct Dir<RIGHT, Ro, Col, len> {
    static constexpr int row_i = Ro;                 // The row index remains the same when moving RIGHT
    static constexpr int col_i = Col + (len - 1);    // The column index is incremented by the car's length minus one
};

// =============================================
// ========== DIRECTION SPECIALIZATION: LEFT ===
// =============================================
// This specialization of Dir is used to calculate the furthest end of a car
// when moving to the LEFT. It decrements the column index by the car's length.
template<int Ro, int Col, int len>
struct Dir<LEFT, Ro, Col, len> {
    static constexpr int row_i = Ro;                 // The row index remains the same when moving LEFT
    static constexpr int col_i = Col + len - 1;      // The column index is decremented by the car's length minus one
};

// =============================================
// ========== DIRECTION SPECIALIZATION: UP =====
// =============================================
// This specialization of Dir is used to calculate the furthest end of a car
// when moving UP. It decrements the row index by the car's length.
template<int Ro, int Col, int len>
struct Dir<UP, Ro, Col, len> {
    static constexpr int row_i = Ro + len - 1;       // The row index is decremented by the car's length minus one
    static constexpr int col_i = Col;                // The column index remains the same when moving UP
};

// =============================================
// ========== DIRECTION SPECIALIZATION: DOWN ===
// =============================================
// This specialization of Dir is used to calculate the furthest end of a car
// when moving DOWN. It increments the row index by the car's length.
template<int Ro, int Col, int len>
struct Dir<DOWN, Ro, Col, len> {
    static constexpr int row_i = Ro + len - 1;       // The row index is incremented by the car's length minus one
    static constexpr int col_i = Col;                // The column index remains the same when moving DOWN
};

// =============================================
// ========== DIRECT STRUCT ====================
// =============================================
// The direct struct is responsible for moving a car on the board in a given
// direction by updating the board's cells as the car moves. The movement is
// performed recursively, step by step, until the specified number of steps
// is completed. The struct is templated with the direction, the remaining steps,
// the main list of the board, the cell containing the car, and the starting
// and ending coordinates of the car.
//
// Parameters:
// - d: The direction of movement (e.g., UP, DOWN, LEFT, RIGHT)
// - counter: The number of steps remaining to move the car
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Ro1, Co1: The starting coordinates of the car
// - Ro2, Co2: The coordinates of the car's furthest end
template<Direction d, int counter, typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct {};

// =============================================
// ========== DIRECT SPECIALIZATION: RIGHT =====
// =============================================
// This specialization of direct is used to move a car to the RIGHT.
// The movement is performed recursively by decrementing the counter and
// updating the board's cells as the car moves.
//
// Parameters:
// - counter: The number of steps remaining to move the car
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<int counter, typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<RIGHT, counter, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    // Recursively move the car RIGHT by decrementing the counter and updating the board
    typedef typename direct<RIGHT, counter - 1, myL, my_cell, Co1, Ro1, Co2, Ro2>::moved mainList;

    // Get the list representing the row of the car's current position
    typedef typename GetAtIndex<Ro1, mainList>::value subList;

    // Get the cell that the car will occupy after moving RIGHT
    typedef typename GetAtIndex<(Co2 + counter), subList>::value celli;

    // Ensure that the target cell is EMPTY before moving the car into it
    static_assert(celli::type == EMPTY, "Error, Collision cell MoveVehicle");

    // Update the board by setting the new cell to the car's type and clearing the old cell
    typedef typename SetAtIndex<(Co2 + counter), my_cell, subList>::list first; // Set the target cell to the car
    typedef typename SetAtIndex<(Co1 + counter - 1), BoardCell<EMPTY, RIGHT, 1>, first>::list second; // Clear the old cell
    typedef typename SetAtIndex<Ro1, second, mainList>::list LL; // Update the row list

    // Define a type alias for the updated board after the move
    typedef LL moved;
};

// =============================================
// ========== DIRECT SPECIALIZATION: NO MOVE ==
// =============================================
// This specialization of direct is used when no more steps are needed (counter == 0).
// The board remains unchanged, and the struct simply returns the current state of the board.
//
// Parameters:
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<RIGHT, 0, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    typedef myL moved; // The board remains unchanged as no more steps are needed
};

// =============================================
// ========== DIRECT SPECIALIZATION: LEFT ======
// =============================================
// This specialization of direct is used to move a car to the LEFT.
// The movement is performed recursively by decrementing the counter and
// updating the board's cells as the car moves.
//
// Parameters:
// - counter: The number of steps remaining to move the car
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<int counter, typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<LEFT, counter, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    // Recursively move the car LEFT by decrementing the counter and updating the board
    typedef typename direct<LEFT, counter - 1, myL, my_cell, Co1, Ro1, Co2, Ro2>::moved mainList;

    // Get the list representing the row of the car's current position
    typedef typename GetAtIndex<Ro1, mainList>::value subList;

    // Get the cell that the car will occupy after moving LEFT
    typedef typename GetAtIndex<(Co1 - counter), subList>::value celli;

    // Ensure that the target cell is EMPTY before moving the car into it
    static_assert(celli::type == EMPTY, "Error, Collision cell MoveVehicle");

    // Update the board by setting the new cell to the car's type and clearing the old cell
    typedef typename SetAtIndex<(Co1 - counter), my_cell, subList>::list first; // Set the target cell to the car
    typedef typename SetAtIndex<(Co2 - counter + 1), BoardCell<EMPTY, LEFT, 1>, first>::list second; // Clear the old cell
    typedef typename SetAtIndex<Ro1, second, mainList>::list LL; // Update the row list

    // Define a type alias for the updated board after the move
    typedef LL moved;
};

// =============================================
// ========== DIRECT SPECIALIZATION: NO MOVE ==
// =============================================
// This specialization of direct is used when no more steps are needed (counter == 0).
// The board remains unchanged, and the struct simply returns the current state of the board.
//
// Parameters:
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<LEFT, 0, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    typedef myL moved; // The board remains unchanged as no more steps are needed
};

// =============================================
// ========== DIRECT SPECIALIZATION: DOWN ======
// =============================================
// This specialization of direct is used to move a car DOWN on the board.
// To move the car DOWN, the board is first transposed so that rows become columns
// and vice versa. The car is then moved RIGHT on the transposed board, and finally,
// the board is transposed back to its original orientation.
//
// Parameters:
// - counter: The number of steps remaining to move the car
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<int counter, typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<DOWN, counter, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    typedef typename Transpose<myL>::matrix transposed; // Transpose the board to move the car DOWN
    typedef typename direct<RIGHT, counter, transposed, my_cell, Ro1, Co1, Ro2, Co2>::moved trans_moved; // Move the car RIGHT on the transposed board
    typedef typename Transpose<trans_moved>::matrix moved; // Transpose the board back to its original orientation
};

// =============================================
// ========== DIRECT SPECIALIZATION: NO MOVE ==
// =============================================
// This specialization of direct is used when no more steps are needed (counter == 0).
// The board remains unchanged, and the struct simply returns the current state of the board.
//
// Parameters:
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<DOWN, 0, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    typedef myL moved; // The board remains unchanged as no more steps are needed
};

// =============================================
// ========== DIRECT SPECIALIZATION: UP ========
// =============================================
// This specialization of direct is used to move a car UP on the board.
// To move the car UP, the board is first transposed so that rows become columns
// and vice versa. The car is then moved LEFT on the transposed board, and finally,
// the board is transposed back to its original orientation.
//
// Parameters:
// - counter: The number of steps remaining to move the car
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<int counter, typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<UP, counter, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    typedef typename Transpose<myL>::matrix transposed; // Transpose the board to move the car UP
    typedef typename direct<LEFT, counter, transposed, my_cell, Ro1, Co1, Ro2, Co2>::moved trans_moved; // Move the car LEFT on the transposed board
    typedef typename Transpose<trans_moved>::matrix moved; // Transpose the board back to its original orientation
};

// =============================================
// ========== DIRECT SPECIALIZATION: NO MOVE ==
// =============================================
// This specialization of direct is used when no more steps are needed (counter == 0).
// The board remains unchanged, and the struct simply returns the current state of the board.
//
// Parameters:
// - myL: The main list representing the game board
// - my_cell: The cell containing the car to be moved
// - Co1, Ro1: The starting coordinates of the car
// - Co2, Ro2: The coordinates of the car's furthest end
template<typename myL, typename my_cell, int Co1, int Ro1, int Co2, int Ro2>
struct direct<UP, 0, myL, my_cell, Co1, Ro1, Co2, Ro2> {
    typedef myL moved; // The board remains unchanged as no more steps are needed
};

// =============================================
// ========== MOVE VEHICLE STRUCT ==============
// =============================================
// The MoveVehicle struct provides an interface to perform a move operation on
// the game board. It uses the direct struct to move a vehicle from one position
// to another on the board. The movement is validated to ensure it stays within
// the bounds of the board and that the movement direction matches the car's orientation.
//
// Parameters:
// - gameBoard: The current state of the game board
// - R, C: The starting coordinates of the vehicle to move
// - D: The direction of the move
// - A: The number of steps to move
template<typename gameBoard, int R, int C, Direction D, int A>
struct MoveVehicle {};

// =============================================
// ========== MOVE VEHICLE SPECIALIZATION ======
// =============================================
// This specialization of MoveVehicle is used to perform the actual move operation.
// It validates the movement, locates the vehicle on the board, and updates the board
// with the vehicle's new position.
//
// Parameters:
// - B: The main list representing the game board
// - R1, C1: The starting coordinates of the vehicle
// - Dl: The direction of the move
// - A: The number of steps to move
template<typename B, int R1, int C1, Direction Dl, int A>
struct MoveVehicle<GameBoard<B>, R1, C1, Dl, A> {
    // Define type aliases for the previous board state and main list
    typedef GameBoard<B> PrevBoard;            // The previous state of the game board
    typedef typename PrevBoard::board mainList; // The main list representing the 2D array of cells

    // Define type aliases for the row's list and the cell containing the vehicle
    typedef GetAtIndex<R1, mainList> subList;     // The list representing the row of the car's current position
    typedef GetAtIndex<C1, typename subList::value> cell; // The cell containing the car at the starting position
    typedef typename cell::value my_cell;         // The type of the car in the cell

    // Static assertions to ensure that the movement stays within bounds and is valid
    static_assert(R1 < PrevBoard::length, "Error Row, Move"); // Ensure that the row is within bounds
    static_assert(C1 < PrevBoard::width, "Error column, Move"); // Ensure that the column is within bounds

    // Static assertion to ensure that the cell is not empty and that the move direction is valid
    static_assert(my_cell::type != EMPTY, "Error, empty cell MoveVehicle"); // Ensure that the cell is not empty
    static_assert((((Dl == UP || Dl == DOWN) && (my_cell::direction == UP || my_cell::direction == DOWN)) ||
                   ((Dl == LEFT || Dl == RIGHT) && (my_cell::direction == LEFT || my_cell::direction == RIGHT))),
                  "Error, direction cell MoveVehicle"); // Ensure that the move direction is valid

    // Compile-time constants to find the car's initial coordinates on the board
    static constexpr int R2 = FindCar<my_cell::type, PrevBoard>::X_row_idx; // Find the row index of the car's starting position
    static constexpr int C2 = FindCar<my_cell::type, PrevBoard>::X_col_idx; // Find the column index of the car's starting position

    // Compile-time constants to determine the coordinates of the car's furthest end based on the direction
    static constexpr int R3 = Dir<Dl, R2, C2, my_cell::length>::row_i; // Calculate the row index of the car's furthest end
    static constexpr int C3 = Dir<Dl, R2, C2, my_cell::length>::col_i; // Calculate the column index of the car's furthest end

    // Define a type alias for the updated board after moving the car
    typedef typename direct<Dl, A, B, my_cell, C2, R2, C3, R3>::moved o1;

    // Define a type alias for the new board state after the move
    typedef GameBoard<o1> board;
};

// =============================================
// ========== END OF FILE =====================
// =============================================

#endif // OOP5_MOVEVEHICLE_H
