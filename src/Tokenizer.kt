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
    private val functions = setOf("abs", "sqrt", "round", "pow", "max", "min")

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
            number = getNumber(symbol)
            type = TokenType.NUMBER
        } else if (symbol == '$') {
            val cellValue = getCellValue(getCell())
            if(cellValue == null) {
                invalid = true
                return
            }
            invalid = cellValue.any { it.isLetter() }
            if(invalid) {
                return
            }
            number = cellValue.toDouble()
            type = TokenType.NUMBER
        } else if (symbol.isLetter()) {
            val funcName = getFuncName(symbol)
            if(funcName !in functions) {
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

    private fun getNumber(symbol : Char) : Double {
        var strNumber = symbol.toString()
        while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
            strNumber += input[i++]
        }
        return strNumber.toDouble()
    }

    private fun getCell() : String {
        var cell = ""
        while (i < input.length && input[i].isLetter()) {
            cell += input[i++]
        }
        while (i < input.length && input[i].isDigit()) {
            cell += input[i++]
        }
        return cell
    }

    private fun getFuncName(symbol: Char) : String {
        var funcName = symbol.toString()
        while (i < input.length && input[i].isLetter()) {
            funcName += input[i++].toString()
        }
        return funcName
    }
}