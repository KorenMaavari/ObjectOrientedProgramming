#ifndef OOP5_BOARDCELL_H
#define OOP5_BOARDCELL_H

#include "CellType.h"
#include "Direction.h"

template<CellType T, Direction D, int L>
struct BoardCell {
    static constexpr CellType type = T;
    static constexpr Direction direction = D;
    static constexpr int length = L;
};

#endif // OOP5_BOARDCELL_H