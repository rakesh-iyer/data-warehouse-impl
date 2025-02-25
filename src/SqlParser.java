import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParser {
    String sqlRegex = "SELECT ([a-zA-Z0-9,\\-]+) FROM ([a-zA-Z0-9]+) WHERE" +
            " " +
            "([a-zA-Z0-9=\\- ]+)";
    SqlComponents getSqlComponents(String sqlExpression) throws Exception {
        Pattern pattern = Pattern.compile(sqlRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlExpression);
        // assert the match.
        if (!matcher.matches()) {
            throw new Exception("Not a supported sql expression " + sqlExpression);
        }

        // assert the group count is 3.
        if (matcher.groupCount() != 3) {
            throw new Exception("Not enough groups have matched. " + matcher.groupCount());
        }
        // FYI:: matcher.group(0) represents the entire matched pattern.
        return new SqlComponents(matcher.group(1), matcher.group(2),
                matcher.group(3));
    }
}
