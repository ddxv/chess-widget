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
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class ChessPiece(val charRepresentation: Char) {
    KING('l'), QUEEN('w'), ROOK('t'), BISHOP('n'), KNIGHT('j'), PAWN('o'), NONE('.')
}


enum class PieceColor {
    NONE, WHITE, BLACK
}
data class ChessCell(val piece: ChessPiece, val color: PieceColor, var hasMoved:Boolean=false)


// Top-level variable
var currentPlayerColor: PieceColor = PieceColor.NONE
var computerPlayerColor: PieceColor = PieceColor.NONE

fun initialChessBoard(): Array<Array<ChessCell>> {

    // 0, 1
    // 1, 1
    val board = Array(8) { Array(8) { ChessCell(ChessPiece.NONE, PieceColor.NONE) } }

    //Player will always start at Row 7
    val opponentColor: PieceColor
    val kingCol: Int
    val queenCol: Int
    if (currentPlayerColor == PieceColor.WHITE){
        opponentColor = PieceColor.BLACK
        kingCol = 4
        queenCol = 3
        Log.i("Game","Color BLACK=${opponentColor} ")
    }
    else {
        opponentColor = PieceColor.WHITE
        kingCol = 3
        queenCol = 4
        Log.i("Game","Color WHITE=${opponentColor} ")
    }

    // 0,0 Top Left of board
    //board[0][0] = ChessCell(ChessPiece.ROOK, opponentColor)
//    board[0][1] = ChessCell(ChessPiece.KNIGHT, opponentColor)
//    board[0][2] = ChessCell(ChessPiece.BISHOP, opponentColor)
//    board[0][queenCol] = ChessCell(ChessPiece.QUEEN, opponentColor)
//    board[0][kingCol] = ChessCell(ChessPiece.KING, opponentColor)
//    board[0][5] = ChessCell(ChessPiece.BISHOP, opponentColor)
//    board[0][6] = ChessCell(ChessPiece.KNIGHT, opponentColor)
//    board[0][7] = ChessCell(ChessPiece.ROOK, opponentColor)
    // White pawns 1
    for (i in 0..0) {
        board[1][i] = ChessCell(ChessPiece.PAWN, opponentColor)
    }

    // Human Player pawns 6
//    for (i in 0..7) {
//        board[6][i] = ChessCell(ChessPiece.PAWN, currentPlayerColor)
//    }
    // Human Player 7
    board[4][5] = ChessCell(ChessPiece.PAWN, currentPlayerColor)
    board[4][6] = ChessCell(ChessPiece.PAWN, currentPlayerColor)
//    board[7][0] = ChessCell(ChessPiece.ROOK, currentPlayerColor)
//    board[7][1] = ChessCell(ChessPiece.KNIGHT, currentPlayerColor)
//    board[7][2] = ChessCell(ChessPiece.BISHOP, currentPlayerColor)
//    board[7][queenCol] = ChessCell(ChessPiece.QUEEN, currentPlayerColor)
//    board[7][kingCol] = ChessCell(ChessPiece.KING, currentPlayerColor)
//    board[7][5] = ChessCell(ChessPiece.BISHOP, currentPlayerColor)
//    board[7][6] = ChessCell(ChessPiece.KNIGHT, currentPlayerColor)
//    board[7][7] = ChessCell(ChessPiece.ROOK, currentPlayerColor)

    return board
}

@Composable
fun ChessGame(playerColor:PieceColor) {
    currentPlayerColor = playerColor
    computerPlayerColor = if (playerColor == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
    val board = remember { mutableStateOf(initialChessBoard()) }
    val selectedCell = remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val chessFont = FontFamily(
        Font(R.font.chessalpha) // Replace `chess_font_name` with the name of your font file (without the extension).
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        var isGameOver = false
        ChessBoard(
            board = board.value,
            selectedCell = selectedCell.value,
            chessFont = chessFont
        ) { row, col ->
            val currentSelected: Pair<Int, Int>?
            if (!isGameOver) {
                currentSelected = selectedCell.value
            } else {
                currentSelected = null
            }
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
                                ChessCell(ChessPiece.NONE, PieceColor.NONE)
                        }
                    }
                    selectedCell.value = null // clear selection
                    // Delay computer move
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500)  // Wait for half a second
                        //val bestMove = AlphaBetaPlayer().decideMove(board.value, computerPlayerColor)
                        val bestMove = calculateBestMove(board.value, computerPlayerColor)
                        if (bestMove != null) {
                            applyMove(board.value, bestMove)
                            val updatedBoard = board.value.deepCopy()  // Create a deep copy
                            board.value =
                                updatedBoard  // Assign the updated board to the state, triggering recomposition
                        }
                    }
                } else {
                    // Invalid move
                    selectedCell.value = null
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isKingInCheck(board.value, PieceColor.WHITE)) {
            if (isCheckmate(board.value, PieceColor.WHITE)) {
                Text("White is in checkmate!")
                isGameOver = true
            } else {
                Text("White is in check!")
            }
        } else if (isKingInCheck(board.value, PieceColor.BLACK)) {
            if (isCheckmate(board.value, PieceColor.BLACK)) {
                Text("Black is in checkmate!")
                isGameOver = true
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

fun calculateBestMove(board: Array<Array<ChessCell>>, color: PieceColor): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
    var bestScore = Int.MIN_VALUE
    var bestMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null

    Log.i("Game", "bestScore=$bestScore, start $color")
    for (possibleMove in generatePossibleMoves(board, color)) {
        Log.i("Game", "M=$possibleMove bestScore=$bestScore, $color")
        val copyBoard = board.deepCopy()
        applyMove(copyBoard, possibleMove)
        val score = negaMax(copyBoard, depth=MAX_DEPTH, color, mutableListOf(possibleMove))
        Log.w("Game", "M=$possibleMove $color finalScore=$score")
        if (score > bestScore) {
            bestScore = score
            bestMove = possibleMove
            Log.e("Game", "fM=$possibleMove bestScore=$bestScore, $color score=$score NEW BEST MOVE")
        }
    }
    Log.i("Game", "bestMove=$bestMove, $color bestScore=$bestScore")
    return bestMove
}


typealias Move = Pair<Int, Int>
typealias MovePair = Pair<Move, Move>
typealias MovesList = MutableList<MovePair>

fun negaMax(board: Array<Array<ChessCell>>, depth: Int, color: PieceColor, firstMove:MovesList): Int {
    Log.i("Game", "M=$firstMove $color d=$depth start")
    if (depth == 0) {
        val score = evaluateBoard(board, color)
        Log.i("Game","M=$firstMove $color d=$depth finalScore=$score")
        return score
    }

    var max = Int.MIN_VALUE
    val nextColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
    for (move in generatePossibleMoves(board, nextColor)) {
        val logMove = firstMove.toMutableList().apply { add(move) }
        val copyBoard = board.deepCopy()
        applyMove(copyBoard, move)
        val score = -negaMax(copyBoard, depth = depth - 1, color = nextColor, logMove)
        if (score > max) {
            Log.i("Game", "M=$logMove $color d=$depth score=$score NEW NM HIGH")
            max = score
        }
    }
    return max
}



fun isKingInCheck(board: Array<Array<ChessCell>>, color: PieceColor): Boolean {
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
    val enemyColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
    return isSquareAttacked(board, kingRow, kingCol, enemyColor)
}


fun applyMove(board: Array<Array<ChessCell>>, move: Pair<Pair<Int, Int>, Pair<Int, Int>>) {
    val (from, to) = move
    board[to.first][to.second] = board[from.first][from.second].copy(hasMoved = true)
    board[from.first][from.second] = ChessCell(ChessPiece.NONE, PieceColor.NONE)
}

fun Array<Array<ChessCell>>.deepCopy(): Array<Array<ChessCell>> {
    return Array(this.size) { this[it].clone() }
}

fun evaluateBoard(board: Array<Array<ChessCell>>, color:PieceColor): Int {
    // This is a simple evaluator; you can enhance this for better evaluations.
    var score = 0
    board.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { colIndex, cell ->

            val colScore = 5 - abs(4-colIndex).coerceAtLeast(abs(3-colIndex))
            //5-(4-6) = 2
            //5-(4-1) = 3
            var rowScore = if (cell.color == currentPlayerColor) 7-rowIndex else rowIndex
            var centerBonus = rowScore * colScore
            centerBonus = 0

            score += when (cell.piece) {
                ChessPiece.PAWN -> {if (cell.color == computerPlayerColor) 10 else -10
                    //Log.i("Game", "evalBoard: ${cell.color} $rowIndex,$colIndex rowScore=$rowScore colScore=$colScore centerScore=$centerBonus pawnScore=$pawnScore")
                }
                ChessPiece.KNIGHT, ChessPiece.BISHOP -> if (cell.color == computerPlayerColor) 30 else -30
                ChessPiece.ROOK -> if (cell.color == computerPlayerColor) 50 else -50
                ChessPiece.QUEEN -> if (cell.color == computerPlayerColor) 90 else -90
                ChessPiece.KING -> if (cell.color ==computerPlayerColor) 900 else -900
                else -> 0
            }
        }
    }
    Log.i("Game", "evalBoard: $color score=$score")
    return score
}



fun generatePossibleMoves(board: Array<Array<ChessCell>>, color: PieceColor): List<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
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
                                if (isLightSquare) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
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
                                    color = if (cell.color == PieceColor.WHITE) Color.White else Color.Black,
                                    style = TextStyle(fontSize = 35.sp)
                                )
                            } else {
                                // Testing only
                                Text(
                                    text = "$rowIndex,$colIndex",
                                    color = if (cell.color == PieceColor.WHITE) Color.Black else Color.White,
                                    style = TextStyle(fontSize = 18.sp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun isCheckmate(board: Array<Array<ChessCell>>, color: PieceColor): Boolean {
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
        PieceColor.WHITE -> PieceColor.BLACK
        PieceColor.BLACK -> PieceColor.WHITE
        else -> PieceColor.NONE
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
            // Pawn from top
            if (color != currentPlayerColor) {
                if (fromCol == toCol && board[toRow][toCol].piece == ChessPiece.NONE) {
                    if (toRow - fromRow == 1) { // regular advance down
                        if (toRow == 7) { // reached end of row
                            board[fromRow][fromCol] = ChessCell(ChessPiece.QUEEN, color, hasMoved = true)
                        }
                        return true
                    } else if (fromRow == 1 && toRow - fromRow == 2 && board[toRow - 1][toCol].piece == ChessPiece.NONE) { // First move +2
                        return true
                    }
                } else if (Math.abs(fromCol - toCol) == 1 && board[toRow][toCol].color == enemyColor && toRow - fromRow == 1) { //Attack
                    return true
                }
            // Pawn from bottom
            } else {
                if (fromCol == toCol && board[toRow][toCol].piece == ChessPiece.NONE) {
                    if (toRow - fromRow == -1) { // Regular Advance Up
                        if (toRow == 0) { // reached end of rows
                            board[fromRow][fromCol] = ChessCell(ChessPiece.QUEEN, color, hasMoved = true)
                        }
                        return true
                    } else if (fromRow == 6 && toRow - fromRow == -2 && board[toRow + 1][toCol].piece == ChessPiece.NONE) { // Initial first move
                        return true
                    }
                } else if (Math.abs(fromCol - toCol) == 1 && board[toRow][toCol].color == enemyColor && toRow - fromRow == -1) { // Regular attack
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
                if (color == PieceColor.WHITE) {
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
                                    board[7][7] = ChessCell(ChessPiece.NONE, PieceColor.NONE, hasMoved = true)
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
                                    board[7][0] = ChessCell(ChessPiece.NONE, PieceColor.NONE, hasMoved = true)
                                    return true
                                }
                            }
                        }
                    }
                } else if (color == PieceColor.BLACK) {
                    // Similar logic for the black king; assuming row 0 is the back rank for black
                    // You'd need to adjust accordingly
                }
            }
        }

        else -> return false
    }
    return false
}

fun isSquareAttacked(board: Array<Array<ChessCell>>, row: Int, col: Int, byColor: PieceColor): Boolean {
    for (fromRow in board.indices) {
        for (fromCol in board[fromRow].indices) {
            if (board[fromRow][fromCol].color == byColor) {
                // Pretend to make the move
                if (isValidMove(board, fromRow, fromCol, row, col)) {
                    //Log.i("Game", "isSquareAttacked row,col is attacked by $fromRow,$fromCol")
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


class AlphaBetaPlayer() {
    private var currentDepth = 0
    private var bestMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null
    private var globalBestMove: Pair<Pair<Int, Int>, Pair<Int, Int>>? = null
    private var start: Long = 0
    private var timeout = false
    fun decideMove(board: Array<Array<ChessCell>>, color: PieceColor): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
        timeout = false
        start = System.currentTimeMillis()
        var d = 0
        while (true) {
            if (d > 0) {
                globalBestMove = bestMove
                println("Completed search with depth $currentDepth. Best move so far: $globalBestMove")
            }
            currentDepth = INITIAL_DEPTH + d
            val moveLog:MovesList = mutableListOf()
            maximizer(board, color, currentDepth, Int.MIN_VALUE, Int.MAX_VALUE, firstMove = moveLog)
            if (timeout) {
                if (globalBestMove != null) {
                    return globalBestMove
                }
            }
            d++
        }
    }

    private fun maximizer(board: Array<Array<ChessCell>>, color:PieceColor, depth: Int, alpha: Int, beta: Int, firstMove:MovesList?): Int {
        var alpha = alpha
        if (System.currentTimeMillis() - start > TIMEOUT_MILISECONDS) {
            timeout = true
            return alpha
        }
        if (depth == 0) {
            val score = evaluateBoard(board, color)
            Log.i("Game", "M=$firstMove $color d=$depth max-finalScore=$score")
            return score
        }

        var logMove: MovesList
        for (move in generatePossibleMoves(board, color)) {
            if (firstMove != null) {logMove = firstMove.toMutableList().apply { add(move) }}
            else {logMove =  mutableListOf(move)}
            val copyBoard = board.deepCopy()
            applyMove(copyBoard, move)
            var nextColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            val rating = minimizer(copyBoard, nextColor, depth - 1, alpha, beta, logMove)
            Log.i("Game", "minimizer returned rating=$rating")
            //nextColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
            if (rating > alpha) {
                alpha = rating
                Log.i("Game", "minimizer returned rating=$rating check d")
                if (depth == currentDepth) {
                    Log.i("Game", "minimizer returned rating=$rating NEW BEST!")
                    bestMove = move
                }
            }
            if (alpha >= beta) {
                return alpha
            }
        }
        return alpha
    }

    private fun minimizer(board: Array<Array<ChessCell>>, color:PieceColor, depth: Int, alpha: Int, beta: Int, firstMove:MovesList): Int {
        var beta = beta
        if (depth == 0) {
            val score = evaluateBoard(board, color)
            Log.i("Game","M=$firstMove $color d=$depth min-finalScore=$score")
            return score
        }
        var nextColor = if (color == PieceColor.WHITE) PieceColor.BLACK else PieceColor.WHITE
        for (move in generatePossibleMoves(board, color)) {
                val logMove = firstMove.toMutableList().apply { add(move) }
                val copyBoard = board.deepCopy()
                applyMove(copyBoard, move)
                val rating = maximizer(copyBoard, nextColor,depth - 1, alpha, beta, logMove)
                Log.i("game", "Nested Maximizer returned rating=$rating")
                if (rating <= beta) {
                    beta = rating
                }
                if (alpha >= beta) {
                    Log.i("Game","Nested Maximizer returned rating=$rating returning beta$beta")
                    return beta
                }
            }
        return beta
    }

    companion object {
        private const val INITIAL_DEPTH = 3
        private const val TIMEOUT_MILISECONDS = 6000
    }
}