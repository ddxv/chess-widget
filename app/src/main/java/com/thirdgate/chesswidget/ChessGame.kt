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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ChessPiece(val charRepresentation: Char) {
    KING('l'), QUEEN('w'), ROOK('t'), BISHOP('n'), KNIGHT('j'), PAWN('o'), NONE('.')
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
    val chessFont = FontFamily(
        Font(R.font.chessalpha) // Replace `chess_font_name` with the name of your font file (without the extension).
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ChessBoard(board = board.value, selectedCell = selectedCell.value, chessFont=chessFont) { row, col ->
            val currentSelected = selectedCell.value
            if (currentSelected == null) {
                selectedCell.value = Pair(row, col)
            } else {
                if (isSafeMove(
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
                    // Delay computer move
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500)  // Wait for half a second
                        val bestMove = bestMove(board.value, PlayerColor.BLACK)
                        if (bestMove != null) {
                            applyMove(board.value, bestMove)
                            val updatedBoard = board.value.deepCopy()  // Create a deep copy
                            board.value = updatedBoard  // Assign the updated board to the state, triggering recomposition
                        }
                    }
                } else {
                    // Invalid move
                    selectedCell.value = null
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isKingInCheck(board.value, PlayerColor.WHITE)) {
            if (isCheckmate(board.value, PlayerColor.WHITE)) {
                Text("White is in checkmate!")
            } else {
                Text("White is in check!")
            }
        } else if (isKingInCheck(board.value, PlayerColor.BLACK)) {
            if (isCheckmate(board.value, PlayerColor.BLACK)) {
                Text("Black is in checkmate!")
            } else {
                Text("Black is in check!")
            }
        }

        ResetButton {
            // This will reset the game state
            board.value = initialChessBoard()
            selectedCell.value = null
        }
    }
}

const val MAX_DEPTH = 3 // You can adjust this value

fun minimax(board: Array<Array<ChessCell>>, depth: Int, isMaximizing: Boolean, alpha: Int, beta: Int, color: PlayerColor): Int {
    if (depth == 0) {
        return evaluateBoard(board)
    }

    var bestScore: Int
    var currentAlpha = alpha
    var currentBeta = beta

    if (isMaximizing) {
        bestScore = Int.MIN_VALUE
        for (move in generatePossibleMoves(board, color)) {
            val copyBoard = board.deepCopy()
            applyMove(copyBoard, move)
            val score = minimax(copyBoard, depth - 1, false, currentAlpha, currentBeta, PlayerColor.BLACK)
            bestScore = maxOf(bestScore, score)
            currentAlpha = maxOf(currentAlpha, score)
            if (currentBeta <= currentAlpha) break
        }
    } else {
        bestScore = Int.MAX_VALUE
        for (move in generatePossibleMoves(board, color)) {
            val copyBoard = board.deepCopy()
            applyMove(copyBoard, move)
            val score = minimax(copyBoard, depth - 1, true, currentAlpha, currentBeta, PlayerColor.WHITE)
            bestScore = minOf(bestScore, score)
            currentBeta = minOf(currentBeta, score)
            if (currentBeta <= currentAlpha) break
        }
    }
    return bestScore
}

fun isKingInCheck(board: Array<Array<ChessCell>>, color: PlayerColor): Boolean {
    var kingRow = -1
    var kingCol = -1
    for (i in board.indices) {
        for (j in board[i].indices) {
            if (board[i][j].piece == ChessPiece.KING && board[i][j].color == color) {
                kingRow = i
                kingCol = j
                break
            }
        }
    }
    val enemyColor = if (color == PlayerColor.WHITE) PlayerColor.BLACK else PlayerColor.WHITE
    return isSquareAttacked(board, kingRow, kingCol, enemyColor)
}

fun bestMove(board: Array<Array<ChessCell>>, color: PlayerColor): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    var bestScore = Int.MIN_VALUE
    var move: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null

    for (possibleMove in generatePossibleMoves(board, color)) {
        val copyBoard = board.deepCopy()
        applyMove(copyBoard, possibleMove)
        val score = minimax(copyBoard, MAX_DEPTH, false, Int.MIN_VALUE, Int.MAX_VALUE, PlayerColor.BLACK)
        if (score > bestScore) {
            bestScore = score
            move = possibleMove
        }
    }
    return move
}

fun applyMove(board: Array<Array<ChessCell>>, move: Pair<Pair<Int, Int>, Pair<Int, Int>>) {
    val (from, to) = move
    board[to.first][to.second] = board[from.first][from.second].copy(hasMoved = true)
    board[from.first][from.second] = ChessCell(ChessPiece.NONE, PlayerColor.NONE)
}

fun Array<Array<ChessCell>>.deepCopy(): Array<Array<ChessCell>> {
    return Array(this.size) { this[it].clone() }
}

fun evaluateBoard(board: Array<Array<ChessCell>>): Int {
    // This is a simple evaluator; you can enhance this for better evaluations.
    var score = 0
    for (row in board) {
        for (cell in row) {
            score += when (cell.piece) {
                ChessPiece.PAWN -> if (cell.color == PlayerColor.WHITE) 10 else -10
                ChessPiece.KNIGHT, ChessPiece.BISHOP -> if (cell.color == PlayerColor.WHITE) 30 else -30
                ChessPiece.ROOK -> if (cell.color == PlayerColor.WHITE) 50 else -50
                ChessPiece.QUEEN -> if (cell.color == PlayerColor.WHITE) 90 else -90
                ChessPiece.KING -> if (cell.color == PlayerColor.WHITE) 900 else -900
                else -> 0
            }
        }
    }
    return score
}






fun generatePossibleMoves(board: Array<Array<ChessCell>>, color: PlayerColor): List<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
    val possibleMoves = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
    for (fromRow in board.indices) {
        for (fromCol in board[fromRow].indices) {
            if (board[fromRow][fromCol].color == color) {
                for (toRow in board.indices) {
                    for (toCol in board[toRow].indices) {
                        if (isSafeMove(board, fromRow, fromCol, toRow, toCol)) {
                            possibleMoves.add(Pair(Pair(fromRow, fromCol), Pair(toRow, toCol)))
                        }
                    }
                }
            }
        }
    }
    return possibleMoves
}

@Composable
fun ChessBoard(board: Array<Array<ChessCell>>, selectedCell: Pair<Int, Int>?, chessFont:FontFamily, onCellClick: (Int, Int) -> Unit) {    Column(modifier = Modifier.background(Color.Gray).fillMaxWidth()) {
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
                        contentAlignment = Alignment.Center
                    ) {
                        Column() {
                            if (cell.piece != ChessPiece.NONE) {
                                Text(
                                    text = cell.piece.charRepresentation.toString(),
                                    fontFamily = chessFont,
                                    color = if (cell.color == PlayerColor.WHITE) Color.Black else Color.White,
                                    style = TextStyle(fontSize = 35.sp)
                                )
                            } else {
                                // Testing only
//                                Text(
//                                    text = "$rowIndex,$colIndex",
//                                    color = if (cell.color == PlayerColor.WHITE) Color.Black else Color.White,
//                                    style = TextStyle(fontSize = 18.sp, textAlign = TextAlign.End),
//                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun isCheckmate(board: Array<Array<ChessCell>>, color: PlayerColor): Boolean {
    if (!isKingInCheck(board, color)) return false

    val possibleMoves = generatePossibleMoves(board, color)

    for (move in possibleMoves) {
        val tempBoard = board.deepCopy()
        applyMove(tempBoard, move)
        if (!isKingInCheck(tempBoard, color)) {
            return false
        }
    }

    return true
}



fun isSafeMove(board: Array<Array<ChessCell>>, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    if (!isValidMove(board, fromRow, fromCol, toRow, toCol)) {
        return false
    }

    val tempBoard = board.deepCopy()
    applyMove(tempBoard, Pair(Pair(fromRow, fromCol), Pair(toRow, toCol)))
    val movingColor = tempBoard[toRow][toCol].color

    return !isKingInCheck(tempBoard, movingColor)
}

fun isValidMove(board: Array<Array<ChessCell>>, fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): Boolean {
    if (fromRow !in 0..7 || fromCol !in 0..7 || toRow !in 0..7 || toCol !in 0..7) {
        return false
    }
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
