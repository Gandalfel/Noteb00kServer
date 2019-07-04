public class GlobalObjects {

    private static SQLConnector sqlConnector = new SQLConnector();

    public static SQLConnector getSqlConnector() {
        return sqlConnector;
    }
}
