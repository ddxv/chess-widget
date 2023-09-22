package com.thirdgate.chesswidget

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

enum class ChessPiece(val charRepresentation: Char) {
    KING('K'), QUEEN('Q'), ROOK('R'), BISHOP('B'), KNIGHT('N'), PAWN('P'), NONE('.')
}


enum class PlayerColor {
    NONE, WHITE, BLACK
}
data class ChessCell(val piece: ChessPiece, val color: PlayerColor, var hasMoved:Boolean=false)

fun initialChessBoard(): Array<Array<ChessCell>> {
    val board = Array(8) { Array(8) { ChessCell(ChessPiece.NONE, PlayerColor.NONE) } }

    // Black 0
    board[0][0] = ChessCell(ChessPiece.ROOK, PlayerColor.BLACK)
    board[0][1] = ChessCell(ChessPiece.KNIGHT, PlayerColor.BLACK)
    board[0][2] = ChessCell(ChessPiece.BISHOP, PlayerColor.BLACK)
    board[0][3] = ChessCell(ChessPiece.QUEEN, PlayerColor.BLACK)
    board[0][4] = ChessCell(ChessPiece.KING, PlayerColor.BLACK)
    board[0][5] = ChessCell(ChessPiece.BISHOP, PlayerColor.BLACK)
    board[0][6] = ChessCell(ChessPiece.KNIGHT, PlayerColor.BLACK)
    board[0][7] = ChessCell(ChessPiece.ROOK, PlayerColor.BLACK)
    // Black pawns 1
    for (i in 0..7) {
        board[1][i] = ChessCell(ChessPiece.PAWN, PlayerColor.BLACK)
    }

    // White pawns 6
    for (i in 0..7) {
        board[6][i] = ChessCell(ChessPiece.PAWN, PlayerColor.WHITE)
    }
    // White 7
    board[7][0] = ChessCell(ChessPiece.ROOK, PlayerColor.WHITE)
    board[7][1] = ChessCell(ChessPiece.KNIGHT, PlayerColor.WHITE)
    board[7][2] = ChessCell(ChessPiece.BISHOP, PlayerColor.WHITE)
    board[7][3] = ChessCell(ChessPiece.QUEEN, PlayerColor.WHITE)
    board[7][4] = ChessCell(ChessPiece.KING, PlayerColor.WHITE)
    board[7][5] = ChessCell(ChessPiece.BISHOP, PlayerColor.WHITE)
    board[7][6] = ChessCell(ChessPiece.KNIGHT, PlayerColor.WHITE)
    board[7][7] = ChessCell(ChessPiece.ROOK, PlayerColor.WHITE)

    return board
}

@Composable
fun ChessGame() {
    val board = remember { mutableStateOf(initialChessBoard()) }
    val selectedCell = remember { mutableStateOf<Pair<Int, Int>?>(null) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChessBoard(board = board.value, selectedCell = selectedCell.value) { row, col ->
            val currentSelected = selectedCell.value
            if (currentSelected == null) {
                selectedCell.value = Pair(row, col)
            } else {
                if (isValidMove(
                        board.value,
                        currentSelected.first,
                        currentSelected.second,
                        row,
                        col
                    )
                ) {
                    board.value[currentSelected.first][currentSelected.second].let {
                        if (it.piece != ChessPiece.NONE) {
                            board.value[row][col] = it.copy()
                            board.value[row][col].hasMoved = true  // Mark piece as moved
                            board.value[currentSelected.first][currentSelected.second] =
                                ChessCell(ChessPiece.NONE, PlayerColor.NONE)
                        }
                    }
                    selectedCell.value = null // clear selection
                } else {
                    // Invalid move
                    selectedCell.value = null
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ResetButton {
            // This will reset the game state
            board.value = initialChessBoard()
            selectedCell.value = null
        }
    }
}

@Composable
fun ChessBoard(board: Array<Array<ChessCell>>, selectedCell: Pair<Int, Int>?, onCellClick: (Int, Int) -> Unit) {    Column(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
        board.forEachIndexed { rowIndex, row ->
            Row(modifier=Modifier.fillMaxWidth()) {
                row.forEachIndexed { colIndex, cell ->
                    val isLightSquare = (rowIndex + colIndex) % 2 == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                if (isLightSquare) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary
                            )
                            .border(2.dp, if (selectedCell == Pair(rowIndex, colIndex)) Color.Red else Color.Transparent)
                            .clickable { onCellClick(rowIndex, colIndex) },
                    ) {
                        Column() {
                        if (cell.piece != ChessPiece.NONE) {
                            Text(
                                text = cell.piece.charRepresentation.toString(),
                                color = if (cell.color == PlayerColor.WHITE) Color.Black else Color.White,
                                style = TextStyle(fontSize = 18.sp)
                            )
                        }
                        // Testing only
                        Text(
                            text = "$rowIndex,$colIndex",
                            color = if (cell.color == PlayerColor.WHITE) Color.Black else Color.White,
                            style = TextStyle(fontSize = 18.sp, textAlign = TextAlign.End),
                        )
                    }
                    }
                }
            }
        }
    }
}

fun isValidMove(board: Array<Array<ChessCell>>, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    val piece = board[fromRow][fromCol].piece
    val color = board[fromRow][fromCol].color
    val enemyColor = when (color) {
        PlayerColor.WHITE -> PlayerColor.BLACK
        PlayerColor.BLACK -> PlayerColor.WHITE
        else -> PlayerColor.NONE
    }

    // Helper function to check if the path is free for sliding pieces.
    fun isPathFree(startRow: Int, startCol: Int, endRow: Int, endCol: Int, rowStep: Int, colStep: Int): Boolean {
        var r = startRow + rowStep
        var c = startCol + colStep
        while (r != endRow || c != endCol) {
            if (board[r][c].piece != ChessPiece.NONE) {
                return false
            }
            r += rowStep
            c += colStep
        }
        return true
    }

    // Can't move to a cell occupied by a piece of the same color.
    if (board[toRow][toCol].color == color) return false

    when (piece) {
        ChessPiece.PAWN -> {
            if (color == PlayerColor.WHITE) {
                if (fromCol == toCol && board[toRow][toCol].piece == ChessPiece.NONE) {
                    if (toRow - fromRow == -1) {
                        return true
                    } else if (fromRow == 6 && toRow - fromRow == -2 && board[toRow + 1][toCol].piece == ChessPiece.NONE) {  // Added condition for 2-space move
                        return true
                    }
                } else if (Math.abs(fromCol - toCol) == 1 && board[toRow][toCol].color == PlayerColor.BLACK && toRow - fromRow == -1) {
                    return true
                }
            } else if (color == PlayerColor.BLACK) {
                if (fromCol == toCol && board[toRow][toCol].piece == ChessPiece.NONE) {
                    if (toRow - fromRow == 1) {
                        return true
                    } else if (fromRow == 1 && toRow - fromRow == 2 && board[toRow - 1][toCol].piece == ChessPiece.NONE) {  // Added condition for 2-space move
                        return true
                    }
                } else if (Math.abs(fromCol - toCol) == 1 && board[toRow][toCol].color == PlayerColor.WHITE && toRow - fromRow == 1) {
                    return true
                }
            }
        }
        ChessPiece.KNIGHT -> {
            val rowDiff = Math.abs(fromRow - toRow)
            val colDiff = Math.abs(fromCol - toCol)
            if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
                return true
            }
        }
        ChessPiece.BISHOP -> {
            if (Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                val rowStep = if (toRow > fromRow) 1 else -1
                val colStep = if (toCol > fromCol) 1 else -1
                return isPathFree(fromRow, fromCol, toRow, toCol, rowStep, colStep)
            }
        }
        ChessPiece.ROOK -> {
            if (fromRow == toRow || fromCol == toCol) {
                val rowStep = if (toRow > fromRow) 1 else if (toRow < fromRow) -1 else 0
                val colStep = if (toCol > fromCol) 1 else if (toCol < fromCol) -1 else 0
                return isPathFree(fromRow, fromCol, toRow, toCol, rowStep, colStep)
            }
        }
        ChessPiece.QUEEN -> {
            if (fromRow == toRow || fromCol == toCol || Math.abs(fromRow - toRow) == Math.abs(fromCol - toCol)) {
                val rowStep = if (toRow > fromRow) 1 else if (toRow < fromRow) -1 else 0
                val colStep = if (toCol > fromCol) 1 else if (toCol < fromCol) -1 else 0
                return isPathFree(fromRow, fromCol, toRow, toCol, rowStep, colStep)
            }
        }
        ChessPiece.KING -> {
            if (Math.abs(fromRow - toRow) <= 1 && Math.abs(fromCol - toCol) <= 1) {
                return true
            } else if (fromRow == toRow) { // Same row, which is a condition for castling
                if (color == PlayerColor.WHITE) {
                    if (fromRow == 7) { // Assuming 0-based indexing and that 7 is the back rank for white
                        if (toCol - fromCol == 2) {  // Kingside castling
                            Log.i("Game", "Kingside castling")
                            // Assuming some function or property hasMoved to check if a piece has moved
                            if (!board[7][4].hasMoved && !board[7][7].hasMoved && board[7][5].piece == ChessPiece.NONE && board[7][6].piece == ChessPiece.NONE) {
                                Log.i("Game", "Kingside castling check passed")
                                // Check if the squares king moves across and to are safe
                                if (!isSquareAttacked(board, row=7, col=4, byColor = enemyColor) && !isSquareAttacked(board, row=7, col=5, byColor=enemyColor) && !isSquareAttacked(board, row=7, col=6, byColor=enemyColor)) {
                                    Log.i("Game", "Kingside castling OK, move rook")
                                    board[7][5] = board[7][7]
                                    board[7][7] = ChessCell(ChessPiece.NONE, PlayerColor.NONE, hasMoved = true)
                                    return true
                                }
                                Log.i("Game", "Kingside castling is attacked")
                            }
                        } else if (fromCol - toCol == 2) {  // Queenside castling
                            Log.i("Game", "queenside castling")
                            if (!board[7][4].hasMoved && !board[7][0].hasMoved && board[7][1].piece == ChessPiece.NONE && board[7][2].piece == ChessPiece.NONE && board[7][3].piece == ChessPiece.NONE) {
                                if (!isSquareAttacked(
                                        board,
                                        row = 7,
                                        col = 4,
                                        byColor = enemyColor
                                    ) && !isSquareAttacked(
                                        board,
                                        row = 7,
                                        col = 3,
                                        byColor = enemyColor
                                    ) && !isSquareAttacked(
                                        board,
                                        row = 7,
                                        col = 2,
                                        byColor = enemyColor
                                    )
                                ) {
                                    board[7][3] = board[7][0]
                                    board[7][0] = ChessCell(ChessPiece.NONE, PlayerColor.NONE, hasMoved = true)
                                    return true
                                }
                            }
                        }
                    }
                } else if (color == PlayerColor.BLACK) {
                    // Similar logic for the black king; assuming row 0 is the back rank for black
                    // You'd need to adjust accordingly
                }
            }
        }

        else -> return false
    }
    return false
}

fun isSquareAttacked(board: Array<Array<ChessCell>>, row: Int, col: Int, byColor: PlayerColor): Boolean {
    for (fromRow in board.indices) {
        for (fromCol in board[fromRow].indices) {
            if (board[fromRow][fromCol].color == byColor) {
                // Pretend to make the move
                if (isValidMove(board, fromRow, fromCol, row, col)) {
                    Log.i("Game","isSquareAttacked: True $fromRow,$fromCol")
                    return true
                }
            }
        }
    }
    return false
}

@Composable
fun ResetButton(onClick: () -> Unit) {
    androidx.compose.material3.Button(onClick = onClick) { Text("Reset")
    }
}
