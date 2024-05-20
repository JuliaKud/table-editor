import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableModel

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

        val rowHeaderTable = RowHeaderTable(table)
        scrollPane.setRowHeaderView(rowHeaderTable)
        scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, JLabel())

        contentPane.add(scrollPane, BorderLayout.CENTER)
    }
}

class RowHeaderTable(mainTable: JTable) : JTable() {
    init {
        model = RowHeaderTableModel(mainTable.rowCount)
        selectionModel = mainTable.selectionModel
        rowHeight = mainTable.rowHeight
        isFocusable = false
        preferredScrollableViewportSize = preferredSize
    }

    override fun getPreferredScrollableViewportSize(): Dimension {
        return Dimension(20, super.getPreferredScrollableViewportSize().height)
    }
}

class RowHeaderTableModel(private val rowCount: Int) : AbstractTableModel() {
    override fun getRowCount(): Int = rowCount

    override fun getColumnCount(): Int = 1

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = rowIndex + 1

    override fun getColumnClass(columnIndex: Int): Class<*> = Number::class.java
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
}
