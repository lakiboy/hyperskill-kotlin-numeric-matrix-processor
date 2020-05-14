package processor

import java.text.DecimalFormat

object MatrixReader {
    fun read(block: () -> Unit): Matrix {
        block()

        val (rows, cols) = readLine()!!
            .trim()
            .split(' ')
            .map(String::toInt)

        return Matrix(rows, cols).also { matrix ->
            repeat(rows) { row -> matrix[row] = readLine()!! }
        }
    }
}

class Matrix(private val rows: Int, private val cols: Int) {
    private var elements = Array(rows) { DoubleArray(cols) }

    //
    // Array extensions
    //

    private operator fun DoubleArray.plus(other: DoubleArray): DoubleArray {
        return mapIndexed { index, elem -> elem + other[index] }.toDoubleArray()
    }

    private operator fun DoubleArray.times(value: Double): DoubleArray {
        return map { it * value }.toDoubleArray()
    }

    private operator fun DoubleArray.times(other: DoubleArray): Double {
        return zip(other) { a, b -> a * b }.sum()
    }

    //
    // Operators
    //

    private operator fun set(row: Int, items: DoubleArray) {
        elements[row] = items
    }

    operator fun set(row: Int, line: String) {
        this[row] = line
            .trim()
            .split(' ')
            .map(kotlin.String::toDouble)
            .toDoubleArray()
    }

    private operator fun set(coords: Pair<Int, Int>, value: Double) {
        this[coords.first][coords.second] = value
    }

    private operator fun get(row: Int): DoubleArray {
        return elements[row]
    }

    operator fun plus(other: Matrix): Matrix {
        require(rows == other.rows && cols == other.cols) { "Incomparable matrices." }

        return Matrix(rows, cols).also { matrix ->
            repeat(rows) { row ->
                matrix[row] = this[row] + other[row]
            }
        }
    }

    operator fun times(value: Double): Matrix {
        return Matrix(rows, cols).also { matrix ->
            repeat(rows) { row -> matrix[row] = this[row] * value }
        }
    }

    operator fun times(other: Matrix): Matrix {
        require(cols == other.rows) { "Incomparable matrices: cols != other.rows" }

        return Matrix(rows, other.cols).also { matrix ->
            for (i in 0 until rows) {
                for (j in 0 until other.cols) {
                    matrix[i][j] = this[i] * other.column(j)
                }
            }
        }
    }

    override fun toString(): String {
        return elements.joinToString("\n") { row ->
            row.joinToString(" ") { DecimalFormat("#0.##").format(it) }
        }
    }

    fun transposeMainDiagonal() = transpose { i, j -> j to i }

    fun transposeSideDiagonal() = transpose { i, j -> (rows - 1 - j) to (cols - 1 - i) }

    fun transposeVerticalLine() = transpose { i, j -> i to (cols - 1 - j) }

    fun transposeHorizontalLine() = transpose { i, j -> (rows - 1 - i) to j }

    fun determinant(): Double {
        require(rows == cols) { "Incomparable matrix: rows != cols." }

        return when (rows) {
            1 -> this[0][0]
            2 -> this[0][0] * this[1][1] - this[1][0] * this[0][1]
            else -> this[0].mapIndexed { j, _ -> minor(0, j) * cofactor(0, j) }.sum()
        }
    }

    fun inverse(): Matrix {
        require(rows == cols) { "Incomparable matrix: rows != cols." }

        val d = determinant()
        require(d != 0.0) { "Determinant can not be zero." }

        return minors().cofactors().transposeMainDiagonal() * (1.0 / d)
    }

    private fun column(col: Int): DoubleArray {
        return DoubleArray(rows) { row -> this[row][col] }
    }

    private fun transpose(block: (i: Int, j: Int) -> Pair<Int, Int>): Matrix {
        require(rows == cols) { "Incomparable matrix: rows != cols." }

        return Matrix(rows, cols).also { matrix ->
            elements.mapIndexed { i, row ->
                row.mapIndexed { j, elem -> matrix[block(i, j)] = elem }
            }
        }
    }

    private fun minors(): Matrix {
        return Matrix(rows, cols).also { matrix ->
            elements.mapIndexed { i, row ->
                row.mapIndexed { j, _ -> matrix[i][j] = minor(i, j) }
            }
        }
    }

    private fun cofactors(): Matrix {
        return Matrix(rows, cols).also { matrix ->
            elements.mapIndexed { i, row ->
                row.mapIndexed { j, _ -> matrix[i][j] = cofactor(i, j) }
            }
        }
    }

    private fun cofactor(i: Int, j: Int) = if ((i + j) % 2 == 0) this[i][j] else -this[i][j]

    private fun minor(i: Int, j: Int): Double {
        val minorElements = elements
            .map { row ->
                val items = row.toMutableList()
                items.removeAt(j)
                items.toDoubleArray()
            }
            .toMutableList()

        minorElements.removeAt(i)

        return Matrix(rows - 1, cols - 1)
            .apply { elements = minorElements.toTypedArray() }
            .determinant()
    }
}

fun main() {
    do {
        println("""
            1. Add matrices
            2. Multiply matrix to a constant
            3. Multiply matrices
            4. Transpose matrix
            5. Calculate a determinant
            6. Inverse matrix
            0. Exit
        """.trimIndent())
        print("Your choice: ")

        val op = readLine()!!.toInt()

        try {
            when (op) {
                1 -> {
                    val a = MatrixReader.read { print("Enter size of first matrix: ") }
                    val b = MatrixReader.read { print("Enter size of second matrix: ") }
                    println("The addition result is:")
                    println(a + b)
                }
                2 -> {
                    val m = MatrixReader.read { print("Enter matrix size: ") }
                    print("Enter constant: ")
                    val c = readLine()!!.toDouble()
                    println("The result is:")
                    println(m * c)
                }
                3 -> {
                    val a = MatrixReader.read { print("Enter size of first matrix: ") }
                    val b = MatrixReader.read { print("Enter size of second matrix: ") }
                    println("The multiplication result is:")
                    println(a * b)
                }
                4 -> {
                    println()
                    println("""
                        1. Main diagonal
                        2. Side diagonal
                        3. Vertical line
                        4. Horizontal line
                    """.trimIndent())
                    print("Your choice: ")

                    val transposeOp = readLine()!!.toInt()
                    val m = MatrixReader.read { print("Enter matrix size: ") }
                    val transposed = when (transposeOp) {
                        1 -> m.transposeMainDiagonal()
                        2 -> m.transposeSideDiagonal()
                        3 -> m.transposeVerticalLine()
                        4 -> m.transposeHorizontalLine()
                        else -> m
                    }
                    println("The result is:")
                    println(transposed)
                }
                5 -> {
                    val m = MatrixReader.read { print("Enter matrix size: ") }
                    println("The result is:")
                    println(m.determinant())
                }
                6 -> {
                    val m = MatrixReader.read { print("Enter matrix size: ") }
                    println("The result is:")
                    println(m.inverse())
                }
            }
        } catch (e: IllegalArgumentException) {
            println("ERROR")
        }

        if (op > 0) {
            println()
        }
    } while (op > 0)
}
