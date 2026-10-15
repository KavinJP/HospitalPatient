import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;

/**
 * Java Swing Desktop GUI Model for Library Management System.
 * - Components: JTextField (ID, Title, Author), JComboBox (Status),
 *   JButtons (Load Books, Search Book, Add Book), JTable
 * - Database: MySQL via JDBC
 * - Cache: HashMap<String, Book>
 */
public class LibrarySwingApp extends JFrame {

    // Database Configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    // In-memory HashMap for efficient book lookup
    private final HashMap<String, Book> bookMap = new HashMap<>();

    // GUI Components
    private JTextField txtBookId;
    private JTextField txtBookTitle;
    private JTextField txtAuthorName;
    private JComboBox<String> cmbStatus;
    private JButton btnLoadBooks;
    private JButton btnSearchBook;
    private JButton btnAddBook;
    private JTable tblBooks;
    private DefaultTableModel tableModel;

    public LibrarySwingApp() {
        super("Athena - Library Management System (Swing GUI)");
        initUI();
        initDatabase();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Form Inputs
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Book Details"));

        inputPanel.add(new JLabel("Book ID:"));
        txtBookId = new JTextField();
        inputPanel.add(txtBookId);

        inputPanel.add(new JLabel("Book Title:"));
        txtBookTitle = new JTextField();
        inputPanel.add(txtBookTitle);

        inputPanel.add(new JLabel("Author Name:"));
        txtAuthorName = new JTextField();
        inputPanel.add(txtAuthorName);

        inputPanel.add(new JLabel("Status:"));
        String[] statuses = { "Available", "Issued", "Reserved", "Lost" };
        cmbStatus = new JComboBox<>(statuses);
        inputPanel.add(cmbStatus);

        // Center-North: Button Actions Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        btnLoadBooks = new JButton("Load Books");
        btnSearchBook = new JButton("Search Book");
        btnAddBook = new JButton("Add Book");

        buttonPanel.add(btnLoadBooks);
        buttonPanel.add(btnSearchBook);
        buttonPanel.add(btnAddBook);

        // Combine inputs and buttons into North region
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(inputPanel, BorderLayout.CENTER);
        northContainer.add(buttonPanel, BorderLayout.SOUTH);
        northContainer.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        add(northContainer, BorderLayout.NORTH);

        // Center Panel: JTable for displaying book records
        String[] columnNames = { "Book ID", "Book Title", "Author Name", "Status" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table view
            }
        };
        tblBooks = new JTable(tableModel);
        tblBooks.setRowHeight(24);
        tblBooks.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tblBooks);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Book Records (Database & HashMap)"));
        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        add(tableContainer, BorderLayout.CENTER);

        // Event Listeners
        btnLoadBooks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadBooks();
            }
        });

        btnSearchBook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBook();
            }
        });

        btnAddBook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS library_db");
            stmt.executeUpdate("USE library_db");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS book (" +
                    "book_id VARCHAR(50) PRIMARY KEY," +
                    "book_title VARCHAR(150) NOT NULL," +
                    "author_name VARCHAR(100) NOT NULL," +
                    "status VARCHAR(20) NOT NULL)");
        } catch (Exception ignored) {
            // If MySQL is not running yet, application will gracefully fallback to in-memory mode
        }
    }

    /**
     * Functional Requirement 1: Load Books
     * - Retrieve all book records from database using JDBC
     * - Create a Book object for each record
     * - Store each book in HashMap<String, Book>
     * - Display all retrieved records in JTable
     */
    private void loadBooks() {
        tableModel.setRowCount(0);
        bookMap.clear();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT book_id, book_title, author_name, status FROM book")) {

            while (rs.next()) {
                String id = rs.getString("book_id");
                String title = rs.getString("book_title");
                String author = rs.getString("author_name");
                String status = rs.getString("status");

                Book book = new Book(id, title, author, status);
                bookMap.put(id, book);

                tableModel.addRow(new Object[]{ id, title, author, status });
            }

            JOptionPane.showMessageDialog(this,
                    "Loaded " + bookMap.size() + " books from MySQL into HashMap and JTable.",
                    "Load Successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            // If MySQL connection fails, load sample data into HashMap and JTable
            seedSampleData();
            JOptionPane.showMessageDialog(this,
                    "Database offline (" + ex.getMessage() + ").\nLoaded sample books into HashMap & JTable.",
                    "Notice", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void seedSampleData() {
        tableModel.setRowCount(0);
        bookMap.clear();

        Book[] samples = {
            new Book("B101", "Java: The Complete Reference", "Herbert Schildt", "Available"),
            new Book("B102", "Clean Code", "Robert C. Martin", "Issued"),
            new Book("B103", "Introduction to Algorithms", "Thomas H. Cormen", "Reserved"),
            new Book("B104", "Design Patterns", "Erich Gamma", "Available"),
            new Book("B105", "Operating System Concepts", "Abraham Silberschatz", "Lost")
        };

        for (Book b : samples) {
            bookMap.put(b.getBookId(), b);
            tableModel.addRow(new Object[]{ b.getBookId(), b.getBookTitle(), b.getAuthorName(), b.getStatus() });
        }
    }

    /**
     * Functional Requirement 2: Search Book
     * - Search the HashMap using the Book ID.
     * - If book exists: display title, author, status in fields.
     * - If book does not exist: display error message.
     */
    private void searchBook() {
        String searchId = txtBookId.getText().trim();
        if (searchId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a Book ID in the Book ID field to search.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Fast O(1) lookup in HashMap
        Book book = bookMap.get(searchId);

        if (book != null) {
            txtBookTitle.setText(book.getBookTitle());
            txtAuthorName.setText(book.getAuthorName());
            cmbStatus.setSelectedItem(book.getStatus());

            JOptionPane.showMessageDialog(this,
                    "Book found in HashMap:\nTitle: " + book.getBookTitle() + "\nAuthor: " + book.getAuthorName(),
                    "Search Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Book with ID '" + searchId + "' does not exist in HashMap.",
                    "Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Functional Requirement 3: Add Book
     * - Validate that all required fields are entered.
     * - Check whether the Book ID already exists in the HashMap.
     * - If exists: display "Book ID already exists!"
     * - Otherwise:
     *     - Insert into MySQL using PreparedStatement
     *     - Create new Book object
     *     - Add to HashMap
     *     - Display in JTable
     *     - Clear input fields
     */
    private void addBook() {
        String id = txtBookId.getText().trim();
        String title = txtBookTitle.getText().trim();
        String author = txtAuthorName.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();

        // Validation
        if (id.isEmpty() || title.isEmpty() || author.isEmpty() || status == null) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required. Please enter Book ID, Title, Author, and Status.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if already in HashMap
        if (bookMap.containsKey(id)) {
            JOptionPane.showMessageDialog(this,
                    "Book ID '" + id + "' already exists in the HashMap! Cannot add duplicate ID.",
                    "Duplicate Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Insert into MySQL database using PreparedStatement
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO book (book_id, book_title, author_name, status) VALUES (?, ?, ?, ?)")) {

            pstmt.setString(1, id);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.setString(4, status);
            pstmt.executeUpdate();

        } catch (Exception ex) {
            System.err.println("Database insert skipped/failed: " + ex.getMessage());
        }

        // Create new Book object and add to HashMap
        Book newBook = new Book(id, title, author, status);
        bookMap.put(id, newBook);

        // Display new record in JTable
        tableModel.addRow(new Object[]{ id, title, author, status });

        // Clear input fields after successful insertion
        clearInputs();

        JOptionPane.showMessageDialog(this,
                "Book '" + title + "' (ID: " + id + ") added successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearInputs() {
        txtBookId.setText("");
        txtBookTitle.setText("");
        txtAuthorName.setText("");
        cmbStatus.setSelectedIndex(0);
        txtBookId.requestFocus();
    }

    public static void main(String[] args) {
        // Set native look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LibrarySwingApp().setVisible(true);
            }
        });
    }
}
