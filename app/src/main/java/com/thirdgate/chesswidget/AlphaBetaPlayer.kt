//package com.thirdgate.chesswidget
//
//
//class AlphaBetaPlayer(board: Board?, side: Side?) : ChessPlayer(board, side) {
//    private var currentDepth = 0
//    private var bestMove: ChessMove? = null
//    private var globalBestMove: ChessMove? = null
//    private var start: Long = 0
//    private var timeout = false
//    fun decideMove(): ChessMove? {
//        timeout = false
//        start = System.currentTimeMillis()
//        var d = 0
//        while (true) {
//            if (d > 0) {
//                globalBestMove = bestMove
//                println("Completed search with depth $currentDepth. Best move so far: $globalBestMove")
//            }
//            currentDepth = INITIAL_DEPTH + d
//            maximizer(currentDepth, Int.MIN_VALUE, Int.MAX_VALUE)
//            if (timeout) {
//                println()
//                return globalBestMove
//            }
//            d++
//        }
//    }
//
//    private fun maximizer(depth: Int, alpha: Int, beta: Int): Int {
//        var alpha = alpha
//        if (System.currentTimeMillis() - start > TIMEOUT_MILISECONDS) {
//            timeout = true
//            return alpha
//        }
//        if (depth == 0) {
//            return board.computeRating(Side.BLACK)
//        }
//        val legalMoves: List<ChessMove> = computeAllLegalMoves()
//        for (move in legalMoves) {
//            makeMove(move)
//            side = side.opposite()
//            val rating = minimizer(depth - 1, alpha, beta)
//            side = side.opposite()
//            undoMove(move)
//            if (rating > alpha) {
//                alpha = rating
//                if (depth == currentDepth) {
//                    bestMove = move
//                }
//            }
//            if (alpha >= beta) {
//                return alpha
//            }
//        }
//        return alpha
//    }
//
//    private fun minimizer(depth: Int, alpha: Int, beta: Int): Int {
//        var beta = beta
//        if (depth == 0) {
//            return board.computeRating(Side.BLACK)
//        }
//        val legalMoves: List<ChessMove> = computeAllLegalMoves()
//        for (move in legalMoves) {
//            makeMove(move)
//            side = side.opposite()
//            val rating = maximizer(depth - 1, alpha, beta)
//            side = side.opposite()
//            undoMove(move)
//            if (rating <= beta) {
//                beta = rating
//            }
//            if (alpha >= beta) {
//                return beta
//            }
//        }
//        return beta
//    }
//
//    companion object {
//        private const val INITIAL_DEPTH = 5
//        private const val TIMEOUT_MILISECONDS = 6000
//    }
//}