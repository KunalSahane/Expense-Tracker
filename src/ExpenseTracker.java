import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class ExpenseTracker {
    private JFrame frame;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private double totalAmount;
    private ArrayList<String> dataPanelValues = new ArrayList<>(3); // Initialize with size if you know it

    public ExpenseTracker() {
        frame = createFrame();
        JPanel dashboardPanel = createDashboard();
        frame.add(dashboardPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setUndecorated(true);
        frame.getRootPane().setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(52, 73, 94)));
        frame.add(createTitleBar(), BorderLayout.NORTH);
        return frame;
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        titleBar.setBackground(new Color(52, 73, 94));
        titleBar.add(createLabel("Expense And Income Tracker", Color.WHITE, 17));
        titleBar.add(createControlLabel("x", e -> System.exit(0), Color.RED));
        titleBar.add(createControlLabel("-", e -> frame.setState(JFrame.ICONIFIED), Color.RED));
        return titleBar;
    }

    private JLabel createLabel(String text, Color color, int fontSize) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Arial", Font.BOLD, fontSize));
        return label;
    }

    private JLabel createControlLabel(String text, Runnable action, Color hoverColor) {
        JLabel label = createLabel(text, Color.WHITE, 17);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) { action.run(); }
            @Override
            public void mouseEntered(MouseEvent mouseEvent) { label.setForeground(hoverColor); }
            @Override
            public void mouseExited(MouseEvent mouseEvent) { label.setForeground(Color.WHITE); }
        });
        return label;
    }

    private JPanel createDashboard() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setBackground(new Color(236, 240, 241));

        // Initialize total amount and data panel values
        totalAmount = 0.0;
        dataPanelValues.add(String.format("-$%,.2f", 0.0));
        dataPanelValues.add(String.format("$%,.2f", 0.0));
        dataPanelValues.add("$" + totalAmount);

        for (String title : new String[]{"Expense", "Income", "Total"}) {
            addDataPanel(panel, title);
        }

        panel.add(createButton("Add Transaction", e -> showAddTransactionDialog()));
        panel.add(createButton("Remove Transaction", e -> removeSelectedTransaction()));

        transactionTable = createTransactionTable();
        panel.add(new JScrollPane(transactionTable));

        return panel;
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(new Color(41, 128, 185));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> action.run());
        return button;
    }

    private JTable createTransactionTable() {
        String[] columnNames = {"ID", "Type", "Description", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setBackground(new Color(236, 240, 241));
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.ITALIC, 16));
        return table;
    }

    private void addDataPanel(JPanel dashboardPanel, String title) {
        JPanel dataPanel = new JPanel(new GridLayout(2, 1));
        dataPanel.setPreferredSize(new Dimension(170, 100));
        dataPanel.setBackground(Color.WHITE);
        dataPanel.setBorder(new LineBorder(new Color(149, 165, 166), 2));
        dashboardPanel.add(dataPanel);

        String value = title.equals("Total") ? fixNegativeValueDisplay(totalAmount) : dataPanelValues.get(title.equals("Expense") ? 0 : 1);
        dataPanel.add(createLabel(title, Color.BLACK, 20));
        dataPanel.add(createLabel(value, Color.BLACK, 16));
    }

    private void showAddTransactionDialog() {
        JDialog dialog = new JDialog(frame, "Add Transaction", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(frame);
        JPanel dialogPanel = new JPanel(new GridLayout(4, 0, 10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Expense", "Income"});
        JTextField descriptionField = new JTextField();
        JTextField amountField = new JTextField();
        JButton addButton = createButton("Add", e -> addTransaction(typeComboBox, descriptionField, amountField));

        dialogPanel.add(createLabel("Type:", Color.BLACK, 14));
        dialogPanel.add(typeComboBox);
        dialogPanel.add(createLabel("Description:", Color.BLACK, 14));
        dialogPanel.add(descriptionField);
        dialogPanel.add(createLabel("Amount:", Color.BLACK, 14));
        dialogPanel.add(amountField);
        dialogPanel.add(new JLabel()); // Empty label for spacing
        dialogPanel.add(addButton);

        dialog.add(dialogPanel);
        dialog.setVisible(true);
    }

    private void addTransaction(JComboBox<String> typeComboBox, JTextField descriptionField, JTextField amountField) {
        String type = (String) typeComboBox.getSelectedItem();
        double newAmount = Double.parseDouble(amountField.getText().replace("$", "").replace(",", ""));
        totalAmount += type.equals("Income") ? newAmount : -newAmount;

        // Ensure dataPanelValues is properly indexed
        dataPanelValues.set(type.equals("Income") ? 1 : 0, String.format("$%,.2f", Double.parseDouble(dataPanelValues.get(type.equals("Income") ? 1 : 0).replace("$", "").replace(",", "")) + (type.equals("Income") ? newAmount : -newAmount)));
        tableModel.addRow(new Object[]{/* Fetch actual ID if available */ 0, type, descriptionField.getText(), "$" + String.format("%.2f", newAmount)});
        addTransactionToDatabase(type, descriptionField.getText(), newAmount);
    }

    private void addTransactionToDatabase(String type, String description, double amount) {
        try (Connection connection = DatabaseConnection.getConnection(); // Ensure this method exists and is correct
             PreparedStatement ps = connection.prepareStatement("INSERT INTO `transaction_table`(`transaction_type`, `description`, `amount`) VALUES (?,?,?)")) {
            ps.setString(1, type);
            ps.setString(2, description);
            ps.setDouble(3, amount);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void removeSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow != -1) {
            int transactionId = (int) transactionTable.getValueAt(selectedRow, 0);
            String type = (String) transactionTable.getValueAt(selectedRow, 1);
            double amount = Double.parseDouble(((String) transactionTable.getValueAt(selectedRow, 3)).replace("$", "").replace(",", ""));
            totalAmount += type.equals("Income") ? -amount : amount;
            tableModel.removeRow(selectedRow);
            removeTransactionFromDatabase(transactionId);
        }
    }

    private void removeTransactionFromDatabase(int transactionId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM `transaction_table` WHERE `id` = ?")) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String fixNegativeValueDisplay(double value) {
        return String.format("$%.2f", value);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTracker::new);
    }
}
