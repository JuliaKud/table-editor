import javax.swing.*
import java.awt.*
import javax.swing.table.DefaultTableModel
import kotlin.math.abs

enum class TokenType {
    NUMBER, SYMBOL, ABS, END
}

class Tokenizer(private val input: String) {
    var type: TokenType = TokenType.END
        private set
    var number: Long = 0
        private set
    var symbol: Char = '0'
        private set
    private var i = 0

    init {
        consume()
    }

    fun consume() {
        if(i >= input.length) {
            type = TokenType.END
            return
        }

        while(input[i].isWhitespace()) { ++i; }
        symbol = input[i++]

        if (symbol.isDigit()) {
            var strNumber = symbol.toString()
            while(i < input.length && input[i].isDigit()) {
                strNumber += input[i++]
            }
            number = strNumber.toLong()
            type = TokenType.NUMBER
        } else if(symbol == '$') {
            var cell = ""
            while(i < input.length && (input[i].isDigit() || input[i].isLetter())) {
                cell += input[i++]
            }
            val cellValue = getCellValue(cell)
            number = cellValue.toLong()

            type = TokenType.NUMBER
        } else if(symbol.isLetter()) {
            var funcName = symbol.toString()
            while(i < input.length && input[i].isLetter()) {
                funcName += input[i++].toString()
            }
            if(funcName == "abs") {
                type = TokenType.ABS
            }
        } else {
            type = TokenType.SYMBOL
        }
    }
}

abstract class Expression {
    abstract fun evaluate(): Long
}

class NumberExpr(private val number: Long) : Expression() {
    override fun evaluate(): Long = number
}

class UnaryMinusExpr(private val expr: Expression) : Expression() {
    override fun evaluate(): Long = -expr.evaluate()
}

class PlusExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Long = lhs.evaluate() + rhs.evaluate()
}

class MinusExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Long = lhs.evaluate() - rhs.evaluate()
}

class MultiplyExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Long = lhs.evaluate() * rhs.evaluate()
}

class DivideExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Long = lhs.evaluate() / rhs.evaluate()
}

class AbsExpr(private val expr: Expression) : Expression() {
    override fun evaluate(): Long = abs(expr.evaluate())
}

fun parseExpression(tokenizer: Tokenizer, layer: Int = 0, prev: Expression? = null): Expression? {
    fun next(layer: Int = 0) : Expression? {
        tokenizer.consume()
        return parseExpression(tokenizer, layer)
    }
    when (tokenizer.type) {
        TokenType.NUMBER -> {
            val expr = NumberExpr(tokenizer.number)
            tokenizer.consume()
            return parseExpression(tokenizer, layer, expr)
        }
        TokenType.SYMBOL -> when (tokenizer.symbol) {
            '+' -> if (layer < 1) {
                val expr = PlusExpr(prev!!, next(1)!!)
                return parseExpression(tokenizer, layer, expr)
            }
            '-' -> {
                if (prev == null) {
                    val expr = UnaryMinusExpr(next(3)!!)
                    return parseExpression(tokenizer, layer, expr)
                }
                if (layer < 1) {
                    val expr = MinusExpr(prev, next(1)!!)
                    return parseExpression(tokenizer, layer, expr)
                }
            }
            '*' -> if (layer < 2) {
                val expr = MultiplyExpr(prev!!, next(2)!!)
                return parseExpression(tokenizer, layer, expr)
            }
            '/' -> if (layer < 2) {
                val expr = DivideExpr(prev!!, next(2)!!)
                return parseExpression(tokenizer, layer, expr)
            }
            '(' -> return parseExpression(tokenizer, layer, next())
            ')' -> if (layer == 0) {
                tokenizer.consume()
            }
        }
        TokenType.ABS -> {
            val expr = AbsExpr(next(4)!!)
            return parseExpression(tokenizer, layer, expr)
        }
        TokenType.END -> return prev
    }
    return prev
}

fun evaluateExpression(expression: String): Long {
    val tokenizer = Tokenizer(expression)
    val expr = parseExpression(tokenizer)
    return expr?.evaluate() ?: 0
}

fun getCellValue(cellRef: String): String {
    val column = cellRef[0].uppercaseChar() - 'A'
    val row = cellRef.substring(1).toInt() - 1
    return tableModel.getValueAt(row, column).toString()
}

val tableModel: DefaultTableModel = CustomTableModel(30, 10)

class TableEditor : JFrame() {
    private val table: JTable

    init {
        title = "Table Editor"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(800, 500)
        setLocationRelativeTo(null)

        table = JTable(tableModel)
        table.autoResizeMode = JTable.AUTO_RESIZE_NEXT_COLUMN

        val scrollPane = JScrollPane(table)
        contentPane.add(scrollPane, BorderLayout.CENTER)
    }
}

class CustomTableModel(rowCount: Int, columnCount: Int) : DefaultTableModel(rowCount, columnCount) {
    override fun setValueAt(value: Any?, row: Int, column: Int) {
        if (value is String && value.startsWith("=")) {
            val cellValue = evaluateExpression(value.substring(1))
            super.setValueAt(cellValue, row, column)
        } else {
            super.setValueAt(value, row, column)
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        val editor = TableEditor()
        editor.isVisible = true
    }

//    println(evaluateExpression("4532 * 54254*(452- 120 +43 - -(5434 + 451 +541) ) / 67/ 123")) // 202'915'174
}
