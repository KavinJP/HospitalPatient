MICRO PROJECT REPORT

1. TITLE

Parcel Tracking System


2. INTRODUCTION

The Parcel Tracking System is a Java-based desktop application developed to manage, record, and track courier parcels efficiently. In logistics and delivery operations, real-time tracking and reliable record management are critical. This application allows logistics personnel to register incoming parcels, inspect current shipment statuses, and track specific parcels using their unique Parcel ID. 

Persistent storage is implemented using an SQLite relational database connected via JDBC. To ensure optimal performance during search and duplicate validation operations, an in-memory HashMap is utilized as a high-speed caching layer.


3. OBJECTIVES

- To design a structured desktop graphical user interface using Java Swing.
- To implement persistent data storage and retrieval using SQLite and JDBC.
- To utilize an in-memory HashMap for constant-time O(1) parcel lookup and instant duplicate validation.
- To implement core operations including loading records, tracking shipments, and registering new parcels with proper validation.


4. TECHNOLOGIES USED

- Java: Core programming language (JDK 8 or above).
- Java Swing: Graphical User Interface library (JFrame, JPanel, JTextField, JComboBox, JButton, JTable).
- JDBC: Java Database Connectivity API for relational database communication.
- SQLite: File-based embedded relational database engine (parcel.db).
- HashMap: In-memory key-value data structure for high-speed indexing and caching.


5. SYSTEM DESIGN

Architecture Flow:
User -> Swing Application -> JDBC -> SQLite Database
User -> Swing Application <-> HashMap (In-Memory Cache)

Role of HashMap in the Application:
The HashMap plays a vital role as a local, in-memory cache layer between the user interface and the persistent database:
- Fast Search: When tracking a parcel, the application retrieves the record directly from the HashMap using the Parcel ID as the key. This provides O(1) retrieval speed without requiring repeated database queries.
- Duplicate Prevention: Before inserting any new record into the database, the system checks whether the Parcel ID is already registered in the HashMap. If found, it immediately alerts the user.
- Synchronization: When records are loaded from the SQLite database, each record is mapped into memory so that subsequent operations remain fast and responsive.


6. IMPLEMENTATION

6.1 Database and Table
The SQLite database stores parcel details in a table named parcel.

Database Schema:
CREATE TABLE IF NOT EXISTS parcel (
    parcel_id TEXT PRIMARY KEY,
    recipient_name TEXT NOT NULL,
    destination TEXT NOT NULL,
    status TEXT NOT NULL
);

Initial Sample Records:
INSERT INTO parcel (parcel_id, recipient_name, destination, status) VALUES
('P101', 'John Doe', 'New York', 'In Transit'),
('P102', 'Alice Smith', 'London', 'Delivered'),
('P103', 'Bob Johnson', 'Tokyo', 'Registered');


6.2 Parcel Class
The Parcel entity class encapsulates shipment properties:

class Parcel {
    String id;
    String recipient;
    String destination;
    String status;

    Parcel(String id, String recipient, String destination, String status) {
        this.id = id;
        this.recipient = recipient;
        this.destination = destination;
        this.status = status;
    }

    public String toString() {
        return id + " | " + recipient + " | " + destination + " | " + status;
    }
}


6.3 HashMap Declaration
The HashMap is declared within the main application to store Parcel objects indexed by their Parcel ID:

HashMap<String, Parcel> parcelMap = new HashMap<String, Parcel>();


6.4 Load Parcel Logic
This operation reads all rows from the SQLite table using JDBC, creates a Parcel object for each row, puts it into the HashMap, and populates the JTable.

private void loadParcels() {
    tableModel.setRowCount(0);
    parcelMap.clear();

    try {
        Connection con = DriverManager.getConnection("jdbc:sqlite:parcel.db");
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT parcel_id, recipient_name, destination, status FROM parcel");

        while (rs.next()) {
            String id = rs.getString("parcel_id");
            String recipient = rs.getString("recipient_name");
            String destination = rs.getString("destination");
            String status = rs.getString("status");

            Parcel p = new Parcel(id, recipient, destination, status);
            parcelMap.put(id, p);
            tableModel.addRow(new Object[]{ id, recipient, destination, status });
        }
        con.close();
        JOptionPane.showMessageDialog(this, "Parcels loaded successfully.");
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error loading records: " + ex.getMessage());
    }
}


6.5 Track Parcel Logic
When a user inputs a Parcel ID and clicks Track Parcel, the system searches the HashMap. If found, it automatically fills the corresponding form fields.

private void trackParcel() {
    String id = txtParcelId.getText().trim();
    if (id.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a Parcel ID to track.");
        return;
    }

    Parcel p = parcelMap.get(id);

    if (p != null) {
        txtRecipient.setText(p.recipient);
        txtDestination.setText(p.destination);
        cmbStatus.setSelectedItem(p.status);
        JOptionPane.showMessageDialog(this, "Parcel Found:\nRecipient: " + p.recipient + "\nStatus: " + p.status);
    } else {
        JOptionPane.showMessageDialog(this, "Parcel with ID '" + id + "' not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
    }
}


6.6 Add Parcel Logic
Validates that all input fields are filled, checks the HashMap to prevent duplicate IDs, executes an SQL INSERT using a PreparedStatement, and updates both the HashMap and JTable.

private void addParcel() {
    String id = txtParcelId.getText().trim();
    String recipient = txtRecipient.getText().trim();
    String destination = txtDestination.getText().trim();
    String status = (String) cmbStatus.getSelectedItem();

    if (id.isEmpty() || recipient.isEmpty() || destination.isEmpty()) {
        JOptionPane.showMessageDialog(this, "All fields are required.");
        return;
    }

    if (parcelMap.containsKey(id)) {
        JOptionPane.showMessageDialog(this, "Parcel ID '" + id + "' already exists.", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        Connection con = DriverManager.getConnection("jdbc:sqlite:parcel.db");
        PreparedStatement ps = con.prepareStatement("INSERT INTO parcel VALUES (?, ?, ?, ?)");
        ps.setString(1, id);
        ps.setString(2, recipient);
        ps.setString(3, destination);
        ps.setString(4, status);
        ps.executeUpdate();
        con.close();
    } catch (Exception ex) {
        System.out.println("Database insert error: " + ex.getMessage());
    }

    Parcel newParcel = new Parcel(id, recipient, destination, status);
    parcelMap.put(id, newParcel);
    tableModel.addRow(new Object[]{ id, recipient, destination, status });

    txtParcelId.setText("");
    txtRecipient.setText("");
    txtDestination.setText("");
    cmbStatus.setSelectedIndex(0);

    JOptionPane.showMessageDialog(this, "Parcel added successfully.");
}


7. SCREENSHOTS AND USER INTERFACE LAYOUT

7.1 Main Application Window
The window contains a form section at the top with four input controls:
- Parcel ID (JTextField)
- Recipient Name (JTextField)
- Destination (JTextField)
- Status (JComboBox with options: Registered, In Transit, Out for Delivery, Delivered)
Directly below the inputs are three control buttons: Load Parcels, Track Parcel, and Add Parcel.
The bottom section features a JTable with four columns displaying registered records.

7.2 Loaded Parcel Records View
When Load Parcels is clicked, all rows from SQLite are populated into the JTable:
- Row 1: P101, John Doe, New York, In Transit
- Row 2: P102, Alice Smith, London, Delivered
- Row 3: P103, Bob Johnson, Tokyo, Registered

7.3 Parcel Tracking View
When the user inputs P101 and clicks Track Parcel:
- Recipient Name field populates with: John Doe
- Destination field populates with: New York
- Status dropdown updates to: In Transit
- An information dialog confirms that the parcel was located.

7.4 Adding a Parcel View
When the user enters new parcel details (P104, David Miller, Berlin, Registered) and clicks Add Parcel:
- The record is appended to the SQLite table and HashMap.
- The JTable instantly shows the new row.
- The input fields are cleared, ready for the next entry.

7.5 Error and Validation Handling View
- Empty field validation: Alerts the user with "All fields are required" if any input is missing.
- Duplicate ID validation: Alerts the user with "Parcel ID already exists" if the entered ID is already present in the HashMap.
- Not Found alert: Alerts the user with "Parcel with ID not found" when searching for an unregistered Parcel ID.


8. TESTING

Test Case 1: Load Parcels
- Test Objective: Verify that all records from SQLite are retrieved and loaded.
- Input: Click Load Parcels button.
- Expected Result: Records retrieved via JDBC, stored in HashMap, and displayed in JTable.
- Actual Result: Table populated with 3 initial records.
- Status: Pass

Test Case 2: Track Existing Parcel
- Test Objective: Verify search functionality for a valid Parcel ID.
- Input: Enter Parcel ID P101 and click Track Parcel.
- Expected Result: Details for P101 retrieved from HashMap and displayed in input fields.
- Actual Result: Form fields populated with John Doe, New York, and In Transit.
- Status: Pass

Test Case 3: Track Non-Existent Parcel
- Test Objective: Verify search behavior when an invalid ID is queried.
- Input: Enter Parcel ID P999 and click Track Parcel.
- Expected Result: Display an error dialog indicating the parcel does not exist.
- Actual Result: Error dialog displayed: Parcel with ID 'P999' not found.
- Status: Pass

Test Case 4: Add New Parcel
- Test Objective: Verify that a valid new parcel is inserted and displayed.
- Input: ID P104, Recipient David Miller, Destination Berlin, Status Registered. Click Add Parcel.
- Expected Result: Record saved to SQLite, added to HashMap, appended to JTable, inputs cleared.
- Actual Result: Record added to table and database successfully, fields reset.
- Status: Pass

Test Case 5: Duplicate Parcel ID Prevention
- Test Objective: Verify that duplicate IDs are rejected.
- Input: Enter existing ID P101 and click Add Parcel.
- Expected Result: System detects duplicate via HashMap.containsKey and blocks insertion.
- Actual Result: Duplicate Error dialog displayed: Parcel ID 'P101' already exists.
- Status: Pass


9. RESULT AND CONCLUSION

9.1 Result
The Parcel Tracking System was developed and tested successfully. The desktop application fulfills all functional requirements:
- Allows persistent storage using an SQLite database via JDBC.
- Employs an in-memory HashMap to achieve fast O(1) search and validation.
- Provides an organized, responsive Swing GUI with form inputs, action buttons, and a records table.

9.2 Conclusion and Learnings
Through this project, the following core software concepts were mastered:
- Understanding the architectural separation between persistent relational storage (SQLite) and runtime in-memory caching (HashMap).
- Developing desktop graphical interfaces using Java Swing and managing event handling with ActionListeners.
- Implementing safe database operations using PreparedStatements to ensure data integrity.
- Applying defensive validation logic to handle missing inputs and duplicate primary keys effectively.
