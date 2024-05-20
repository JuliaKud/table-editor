import kotlin.math.*

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

class MinExpr(private val num1: Expression, private val num2: Expression) : Expression() {
    override fun evaluate(): Double = min(num1.evaluate(), num2.evaluate())
}

private val functionsMap = mapOf<String, (List<Expression>) -> Expression>(
    "abs" to { args -> AbsExpr(args[0]) },
    "sqrt" to { args -> SqrtExpr(args[0]) },
    "round" to { args -> RoundExpr(args[0]) },
    "pow" to { args -> PowExpr(args[0], args[1]) },
    "max" to { args -> MaxExpr(args[0], args[1]) },
    "min" to { args -> MinExpr(args[0], args[1]) }
)

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
                val expr = PlusExpr(prev ?: return null, next(1) ?: return null)
                return parseExpression(tokenizer, layer, expr)
            }

            '-' -> {
                if (prev == null) {
                    val expr = UnaryMinusExpr(next(3) ?: return null)
                    return parseExpression(tokenizer, layer, expr)
                }
                if (layer < 1) {
                    val expr = MinusExpr(prev, next(1) ?: return null)
                    return parseExpression(tokenizer, layer, expr)
                }
            }

            '*' -> if (layer < 2) {
                val expr = MultiplyExpr(prev ?: return null, next(2) ?: return null)
                return parseExpression(tokenizer, layer, expr)
            }

            '/' -> if (layer < 2) {
                val expr = DivideExpr(prev ?: return null, next(2) ?: return null)
                return parseExpression(tokenizer, layer, expr)
            }

            '(' -> return parseExpression(tokenizer, layer, next())
            ')' -> if (layer == 0) {
                tokenizer.consume()
            }
        }

        TokenType.FUNCTION -> {
            val func = functionsMap[tokenizer.function]
            val args = mutableListOf<Expression>()
            var nextValue = next(4) ?: return null
            args.add(nextValue)
            if (tokenizer.symbol == ',') {
                nextValue = next(4) ?: return null
                args.add(nextValue)
            }
            val expr = func?.let { it(args) }
            return parseExpression(tokenizer, layer, expr)
        }

        TokenType.END -> return prev
    }
    return prev
}