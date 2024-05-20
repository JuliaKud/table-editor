# Table editor with formula support
Here is a table editor implemented in Kotlin with support for various formulas.

## Features
- parentheses
- binary operators: `+`, `-`, `*` and `/`
- unary operation: unary `-`
- cell references: use `$` followed by the cell identifier (e.g. `$A1`) to reference another cell's value in a formula
- named functions

### Supported named functions
- `abs(x)` : absolute value of `x`
- `sqrt(x)` : square root of `x`
- `round(x)` : round `x` to the nearest integer
- `pow(x, y)` : raise `x` to the power of `y`
- `max(x, y)` : return the maximum of `x` and `y`

## Editing cells
To enter a formula, start the cell input with an `=` sign (e.g. `=3+4` or `=$A1*2`)
