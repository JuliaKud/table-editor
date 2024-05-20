import javax.swing.*
import java.awt.*
import javax.swing.table.DefaultTableModel
import kotlin.math.*

enum class TokenType {
    NUMBER, SYMBOL, FUNCTION, END
}

class Tokenizer(private val input: String) {
    var type: TokenType = TokenType.END
        private set
    var number: Double = 0.0
        private set
    var symbol: Char = '0'
        private set
    var invalid = false
        private set
    var function: String = ""
        private set
    private var i = 0

    init {
        consume()
    }

    fun consume() {
        if (i >= input.length) {
            type = TokenType.END
            return
        }

        while (input[i].isWhitespace()) {
            ++i
        }

        symbol = input[i++]

        if (symbol.isDigit()) {
            var strNumber = symbol.toString()
            while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                strNumber += input[i++]
            }
            number = strNumber.toDouble()
            type = TokenType.NUMBER
        } else if (symbol == '$') {
            var cell = ""
            while (i < input.length && input[i].isLetter()) {
                cell += input[i++]
            }
            while (i < input.length && input[i].isDigit()) {
                cell += input[i++]
            }
            val cellValue = getCellValue(cell)
            if(cellValue == null) {
                invalid = true
                return
            }
            number = cellValue.toDouble()
            type = TokenType.NUMBER
        } else if (symbol.isLetter()) {
            var funcName = symbol.toString()
            while (i < input.length && input[i].isLetter()) {
                funcName += input[i++].toString()
            }
            if(funcName !in setOf("abs", "sqrt", "round", "pow", "max", "min")) {
                invalid = true
                return
            }
            function = funcName
            type = TokenType.FUNCTION
            TokenType.FUNCTION
        } else {
            type = TokenType.SYMBOL
        }
    }
}

abstract class Expression {
    abstract fun evaluate(): Double
}

class NumberExpr(private val number: Double) : Expression() {
    override fun evaluate(): Double = number
}

class UnaryMinusExpr(private val expr: Expression) : Expression() {
    override fun evaluate(): Double = -expr.evaluate()
}

class PlusExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Double = lhs.evaluate() + rhs.evaluate()
}

class MinusExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Double = lhs.evaluate() - rhs.evaluate()
}

class MultiplyExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Double = lhs.evaluate() * rhs.evaluate()
}

class DivideExpr(private val lhs: Expression, private val rhs: Expression) : Expression() {
    override fun evaluate(): Double = lhs.evaluate() / rhs.evaluate()
}

class AbsExpr(private val expr: Expression) : Expression() {
    override fun evaluate(): Double = abs(expr.evaluate())
}

class SqrtExpr(private val expr: Expression) : Expression() {
    override fun evaluate(): Double = sqrt(expr.evaluate())
}

class RoundExpr(private val expr: Expression) : Expression() {
    override fun evaluate(): Double = round(expr.evaluate())
}

class PowExpr(private val base: Expression, private val exponent: Expression) : Expression() {
    override fun evaluate(): Double = base.evaluate().pow(exponent.evaluate())
}

class MaxExpr(private val num1: Expression, private val num2: Expression) : Expression() {
    override fun evaluate(): Double = max(num1.evaluate(), num2.evaluate())
}

fun parseExpression(tokenizer: Tokenizer, layer: Int = 0, prev: Expression? = null): Expression? {
    fun next(layer: Int = 0): Expression? {
        tokenizer.consume()
        return parseExpression(tokenizer, layer)
    }
    if (tokenizer.invalid) {
        return null
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

        TokenType.FUNCTION -> when (tokenizer.function) {
            "abs" -> {
                val expr = AbsExpr(next(4)!!)
                return parseExpression(tokenizer, layer, expr)
            }

            "sqrt" -> {
                val expr = SqrtExpr(next(4)!!)
                return parseExpression(tokenizer, layer, expr)
            }

            "round" -> {
                val expr = RoundExpr(next(4)!!)
                return parseExpression(tokenizer, layer, expr)
            }

            "pow" -> {
                tokenizer.consume()
                val expr = PowExpr(next(4)!!, next(4)!!)
                return parseExpression(tokenizer, layer, expr)
            }

            "max" -> {
                tokenizer.consume()
                val expr = MaxExpr(next(4)!!, next(4)!!)
                return parseExpression(tokenizer, layer, expr)
            }
        }

        TokenType.END -> return prev
    }
    return prev
}

fun evaluateExpression(expression: String): Double? {
    val tokenizer = Tokenizer(expression)
    val expr = parseExpression(tokenizer)
    return expr?.evaluate()
}

fun getCellValue(cellRef: String): String? {
    val column = cellRef[0].uppercaseChar() - 'A'
    val row = cellRef.substring(1).toInt() - 1
    return tableModel.getValueAt(row, column)?.toString()
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

        val rowHeader = JViewport()
        val rowHeaderList = JList<String>()
        val rowHeaderModel = DefaultListModel<String>()
        for (i in 0 until table.rowCount) {
            rowHeaderModel.addElement((i + 1).toString())
        }
        rowHeaderList.model = rowHeaderModel
        rowHeader.view = rowHeaderList
        scrollPane.rowHeader = rowHeader
        scrollPane.rowHeader.isVisible = true
    }
}

class CustomTableModel(rowCount: Int, columnCount: Int) : DefaultTableModel(rowCount, columnCount) {
    override fun setValueAt(value: Any?, row: Int, column: Int) {
        if (value is String && value.startsWith("=")) {
            val cellValue = evaluateExpression(value.substring(1))
            if (cellValue != null) {
                super.setValueAt(cellValue, row, column)
            } else {
                super.setValueAt("#INCORRECT_FORMULA", row, column)
            }
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
