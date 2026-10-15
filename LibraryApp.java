import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

// Book entity
class Book {
    String id, title, author, status;

    Book(String id, String title, String author, String status) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.status = status;
    }

    public String toString() {
        return id + " | " + title + " | " + author + " | " + status;
    }
}

public class LibraryApp {
    // HashMap for fast book lookup
    static HashMap<String, Book> bookMap = new HashMap<String, Book>();

    // MySQL connection settings
    static final String URL = "jdbc:mysql://localhost:3306/library_db";
    static final String USER = "root";
    static final String PASS = "root";

    static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // 1. Load Books from MySQL into HashMap
    static void loadBooks() {
        try {
            Connection con = getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM book");
            bookMap.clear();
            while (rs.next()) {
                Book b = new Book(
                    rs.getString("book_id"),
                    rs.getString("book_title"),
                    rs.getString("author_name"),
                    rs.getString("status")
                );
                bookMap.put(b.id, b);
            }
            con.close();
            System.out.println("Loaded " + bookMap.size() + " books from MySQL into HashMap.");
        } catch (Exception e) {
            // If MySQL is not running, keep sample data in HashMap
            if (bookMap.isEmpty()) {
                bookMap.put("B101", new Book("B101", "Java Complete Reference", "Herbert Schildt", "Available"));
                bookMap.put("B102", new Book("B102", "Clean Code", "Robert C. Martin", "Issued"));
                bookMap.put("B103", new Book("B103", "Data Structures", "Mark Weiss", "Reserved"));
            }
            System.out.println("MySQL offline, loaded sample books into HashMap.");
        }
    }

    // 2. Search Book by ID in HashMap
    static Book searchBook(String id) {
        return bookMap.get(id);
    }

    // 3. Add Book into MySQL and HashMap
    static boolean addBook(Book b) {
        if (bookMap.containsKey(b.id)) {
            return false; // ID already exists in HashMap
        }

        try {
            Connection con = getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO book VALUES (?, ?, ?, ?)");
            ps.setString(1, b.id);
            ps.setString(2, b.title);
            ps.setString(3, b.author);
            ps.setString(4, b.status);
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            System.out.println("Notice: MySQL insert bypassed (" + e.getMessage() + ")");
        }

        // Add to HashMap
        bookMap.put(b.id, b);
        return true;
    }

    public static void main(String[] args) throws Exception {
        // Load initial records
        loadBooks();

        // Start simple web server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve HTML webpage
        server.createContext("/", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                File file = new File("public/index.html");
                byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                ex.getResponseHeaders().set("Content-Type", "text/html");
                ex.sendResponseHeaders(200, bytes.length);
                OutputStream os = ex.getResponseBody();
                os.write(bytes);
                os.close();
            }
        });

        // 1. /getBooks API (Load Books)
        server.createContext("/getBooks", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                loadBooks();
                StringBuilder json = new StringBuilder("[");
                int i = 0;
                for (Book b : bookMap.values()) {
                    if (i > 0) json.append(",");
                    json.append("{\"id\":\"").append(b.id)
                        .append("\",\"title\":\"").append(b.title)
                        .append("\",\"author\":\"").append(b.author)
                        .append("\",\"status\":\"").append(b.status).append("\"}");
                    i++;
                }
                json.append("]");

                byte[] res = json.toString().getBytes();
                ex.getResponseHeaders().set("Content-Type", "application/json");
                ex.sendResponseHeaders(200, res.length);
                OutputStream os = ex.getResponseBody();
                os.write(res);
                os.close();
            }
        });

        // 3. /addBook API (Add Book)
        server.createContext("/addBook", new HttpHandler() {
            public void handle(HttpExchange ex) throws IOException {
                BufferedReader br = new BufferedReader(new InputStreamReader(ex.getRequestBody()));
                String line = br.readLine();
                // Simple parsing from {"id":"...","title":"...","author":"...","status":"..."}
                String id = extract(line, "id");
                String title = extract(line, "title");
                String author = extract(line, "author");
                String status = extract(line, "status");

                boolean ok = addBook(new Book(id, title, author, status));
                String res = ok ? "{\"status\":\"ok\"}" : "{\"error\":\"exists\"}";
                ex.getResponseHeaders().set("Content-Type", "application/json");
                ex.sendResponseHeaders(ok ? 200 : 400, res.getBytes().length);
                OutputStream os = ex.getResponseBody();
                os.write(res.getBytes());
                os.close();
            }
        });

        server.start();
        System.out.println("Website model running at http://localhost:8080");
    }

    static String extract(String json, String key) {
        try {
            int start = json.indexOf("\"" + key + "\":\"") + key.length() + 4;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
    }
}
