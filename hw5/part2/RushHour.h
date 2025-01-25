#ifndef OOP5_RUSHHOUR_H
#define OOP5_RUSHHOUR_H

#include "BoardCell.h"
#include "CellType.h"
#include "Direction.h"
#include "GameBoard.h"
#include "List.h"
#include "Utilities.h"

#include "MoveVehicle.h"



/*arguments:
 * Row - the row of red car.
 * Co - current cell index to check.
 * end - the last column index.
 * */
template<typename Row, int Co, int end>
struct checkIfAllEmpty{
    typedef typename GetAtIndex<Co,Row>::value cell_i;
    constexpr static bool current_res=ConditionalInteger<cell_i ::type == EMPTY || cell_i ::type == X , true, false>::value;

    constexpr static bool result =
            ConditionalInteger<current_res,
                    checkIfAllEmpty<Row, Co+1, end>::result ,false>::value;
};

template<typename Row, int end>
struct checkIfAllEmpty<Row,end,end>{
    typedef typename GetAtIndex<end,Row>::value cell_i;
    constexpr static bool result =
            //checks if type is empty
            ConditionalInteger<cell_i ::type== EMPTY || cell_i ::type == X , true ,false>::value;
};




template<typename Bo>
struct CheckWin {
    typedef typename Bo::board mainList;


    static constexpr int red_x = FindCar<X,Bo>::X_row_idx; //first x coordinate of the red car
    static constexpr int red_y = FindCar<X,Bo>::X_col_idx; //first y coordinate of the red car
    typedef typename GetAtIndex<red_x,mainList>::value red_row; // the list of the red row
    typedef typename GetAtIndex<red_y,red_row>::value red_cell; //the first cell of the red car
    static constexpr int red_col_end = Dir<RIGHT,red_x,red_y,red_cell::length>::col_i; //the index of the last cell of the red car

    static constexpr bool result = checkIfAllEmpty<red_row,red_col_end,Bo::width-1>::result;

};





// Helper structure to apply the sequence of moves to the game board
template<typename BoardType, typename MovesList>
struct ApplyMoves;

// Recursive case: apply the first move to the board and continue applying the remaining moves
template<typename BoardType, typename Head, typename... Tail>
struct ApplyMoves<BoardType, List<Head, Tail...>> {
    static constexpr int ro = FindCar<Head::type,BoardType>::X_row_idx; //first x coordinate of the red car
    static constexpr int co = FindCar<Head::type,BoardType>::X_col_idx; //first y coordinate of the red car

    typedef typename MoveVehicle<BoardType, ro, co, Head::direction, Head::amount>::board newBoard; // Apply the move to the board
    typedef typename ApplyMoves<newBoard, List<Tail...>>::result result; // Recursively apply the remaining moves
};

// Base case: reached the end of the moves
template<typename BoardType>
struct ApplyMoves<BoardType, List<>> {
    typedef BoardType result; // Return the final board after applying all moves
};



// Structure to check if the given sequence of moves solves the game
template<typename Board, typename Moves>
struct CheckSolution {
    typedef typename ApplyMoves<Board, Moves>::result newBoard; // Apply the sequence of moves to the initial board
    //static_assert(GetAtIndex<6,typename newBoard::board::head>::value::type!=X,"is X");
   // static_assert(GetAtIndex<3,typename newBoard::board::head>::value::type!=EMPTY,"is empty");
    static constexpr bool result = CheckWin<newBoard>::result; // Check if the game is won after applying the moves
};

#endif // OOP5_RUSHHOUR_H