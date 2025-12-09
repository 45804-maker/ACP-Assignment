import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentDatabaseApp {

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/StudentDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDatabaseApp().createGUI());
    }

    public void createGUI() {

        JFrame frame = new JFrame("Student Database Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // -------------------- INPUT PANEL --------------------
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Student Information"));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField emailField = new JTextField();

        inputPanel.add(new JLabel("First Name:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Last Name:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Age:"));
        inputPanel.add(ageField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // -------------------- TABLE PANEL --------------------
        JTable table = new JTable();
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"ID", "First Name", "Last Name", "Age", "Email"}, 0);
        table.setModel(tableModel);

        JScrollPane tableScroll = new JScrollPane(table);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        // -------------------- BUTTON PANEL --------------------
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addButton = new JButton("Add Student");
        JButton viewButton = new JButton("View Students");
        JButton searchButton = new JButton("Search by ID");
        JTextField searchField = new JTextField(10);

        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(new JLabel("Search ID:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // -------------------- MESSAGE AREA --------------------
        JTextArea messageArea = new JTextArea(5, 20);
        messageArea.setEditable(false);

        JScrollPane msgScroll = new JScrollPane(messageArea);

        // Add message area BELOW buttons
        frame.add(msgScroll, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);

        // -------------------------------------------------
        //                  ACTION LISTENERS
        // -------------------------------------------------

        addButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String ageText = ageField.getText();
            String email = emailField.getText();

            try {
                int age = Integer.parseInt(ageText);
                addStudent(firstName, lastName, age, email, messageArea);
            } catch (NumberFormatException ex) {
                messageArea.append("Invalid age. Please enter a number.\n");
            }
        });

        // Correct SwingWorker syntax
        viewButton.addActionListener(e -> {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    viewStudents(tableModel, messageArea);
                    return null;
                }
            }.execute();
        });

        searchButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(searchField.getText());
                searchStudentById(id, tableModel, messageArea);
            } catch (NumberFormatException ex) {
                messageArea.append("Invalid ID. Please enter a number.\n");
            }
        });
    }

    // -----------------------------------------------------
    //                    DATABASE METHODS
    // -----------------------------------------------------

    private void addStudent(String firstName, String lastName, int age, String email, JTextArea messageArea) {
        String sql = "INSERT INTO students (first_name, last_name, age, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setInt(3, age);
            ps.setString(4, email);

            ps.executeUpdate();
            messageArea.append("Student added successfully!\n");

        } catch (SQLException e) {
            messageArea.append("Error adding student: " + e.getMessage() + "\n");
        }
    }

    private void viewStudents(DefaultTableModel tableModel, JTextArea messageArea) {
        String sql = "SELECT * FROM students";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            tableModel.setRowCount(0);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getString("email")
                });
            }

        } catch (SQLException e) {
            messageArea.append("Error retrieving students: " + e.getMessage() + "\n");
        }
    }

    private void searchStudentById(int id, DefaultTableModel tableModel, JTextArea messageArea) {
        String sql = "SELECT * FROM students WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            if (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getString("email")
                });
            } else {
                messageArea.append("No student found with ID: " + id + "\n");
            }

        } catch (SQLException e) {
            messageArea.append("Error searching student: " + e.getMessage() + "\n");
        }
    }
}
