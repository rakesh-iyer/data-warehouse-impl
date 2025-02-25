public class FromClause {
    // Defer joins and more complex cases.
    String table;
    FromClause(String fromClauseString) {
        table = fromClauseString;
    }
}
