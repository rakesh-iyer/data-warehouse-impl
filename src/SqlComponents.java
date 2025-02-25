public class SqlComponents {
    SelectClause selectClause;
    FromClause fromClause;
    WhereClause whereClause;
    SqlComponents(String selectClauseString, String fromClauseString,
                  String whereClauseString) {
        this.selectClause = new SelectClause(selectClauseString);
        this.fromClause = new FromClause(fromClauseString);
        this.whereClause = new WhereClause(whereClauseString);
    }
}