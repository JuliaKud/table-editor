import javax.swing.*
import java.awt.*
import javax.swing.table.DefaultTableModel

class TableEditor : JFrame() {
    private val tableModel: DefaultTableModel
    private val table: JTable

    init {
        title = "Table Editor"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(800, 500)
        setLocationRelativeTo(null)

        tableModel = CustomTableModel(30, 10)
        table = JTable(tableModel)
        table.autoResizeMode = JTable.AUTO_RESIZE_NEXT_COLUMN

        val scrollPane = JScrollPane(table)
        contentPane.add(scrollPane, BorderLayout.CENTER)

    }

    private fun getCellValue(cellRef: String): Any? {
        val column = cellRef[0].uppercaseChar() - 'A'
        val row = cellRef.substring(1).toInt() - 1
        return tableModel.getValueAt(row, column)
    }

    inner class CustomTableModel(rowCount: Int, columnCount: Int) : DefaultTableModel(rowCount, columnCount) {
        override fun setValueAt(value: Any?, row: Int, column: Int) {
            if (value is String && value.startsWith("=")) {
                val cellValue = getCellValue(value.substring(1))
                super.setValueAt(cellValue, row, column)
            } else {
                super.setValueAt(value, row, column)
            }
        }
    }
}

fun main() {
    SwingUtilities.invokeLater {
        val editor = TableEditor()
        editor.isVisible = true
    }
}
