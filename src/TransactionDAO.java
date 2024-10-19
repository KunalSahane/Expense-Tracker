import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAO {
    private static final Logger logger = Logger.getLogger(TransactionDAO.class.getName());

    // Method to retrieve all transactions from the database
    public static List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();

        // Use try-with-resources for automatic resource management
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM transaction_table");
             ResultSet rs = ps.executeQuery()) {

            // Iterate through the result set obtained from the SQL query
            while (rs.next()) {
                int id = rs.getInt("id");
                String type = rs.getString("transaction_type");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");

                // Create a Transaction object with the retrieved details
                Transaction transaction = new Transaction(id, type, description, amount);
                // Add the Transaction object to the list
                transactions.add(transaction);
            }

        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving transactions", ex);
        }

        // Return the list of transactions
        return transactions;
    }
}
