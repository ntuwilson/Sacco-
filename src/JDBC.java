import java.sql.*;


public class JDBC {

    private static JDBC instance = null;
    private Connection connection;

    private JDBC() {
        // Private constructor to prevent  instantiation outside this singleton .
    }

    public void connect() {
        // check connection
        String url = "jdbc:mysql://localhost:3306/deposit";
        String username = "root";
        String password = "";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
           //e.printStackTrace();
           System.out.println(e.getMessage());
        }
    }

    public static JDBC getInstance() {
        if (instance == null) {
            synchronized (JDBC.class) {
                if (instance == null) {
                    instance = new JDBC();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        if (connection == null) {
          
        }
        return connection;
    }
}
