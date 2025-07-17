import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.UUID;

// ---------- Admin Registered ----------
class RegisteredAdmin {
    private String username, password, email;

    public RegisteredAdmin(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
}

// ---------- Customer Register ----------
class RegisteredCustomer {
    private String username, password, email, phone;
    private List<String> purchaseHistory = new ArrayList<>();

    public RegisteredCustomer(String username, String password, String email, String phone) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public List<String> getPurchaseHistory() { return purchaseHistory; }
}

// ---------- Role Selection ----------
class LoginUI extends JFrame implements ActionListener {
    public LoginUI() {
        setTitle("Select Role");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JButton adminBtn = new JButton("Admin");
        JButton customerBtn = new JButton("Customer");

        adminBtn.addActionListener(this);
        customerBtn.addActionListener(this);

        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(adminBtn);
        panel.add(customerBtn);

        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();
        dispose();
        if (source.getText().equals("Admin")) {
            new AdminKeyPrompt(); // Show key prompt before AdminLoginUI
        } else {
            new CustomerLoginUI();   // Redirect to Customer Login
        }
    }
}

// ---------- Admin Key Prompt ----------
class AdminKeyPrompt extends JFrame implements ActionListener {
    private JPasswordField keyField;
    private JButton proceedBtn, backBtn;
    private String actualKey;
    private int attempts = 0;
    private final int MAX_ATTEMPTS = 3;

    public AdminKeyPrompt() {
        loadKeyFromConfig();

        setTitle("Enter Admin Access Key");
        setSize(350, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel label = new JLabel("Enter special admin key:");
        keyField = new JPasswordField();

        proceedBtn = new JButton("Proceed");
        backBtn = new JButton("Back");

        proceedBtn.addActionListener(this);
        backBtn.addActionListener(this);

        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(label);
        panel.add(keyField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(proceedBtn);
        buttonPanel.add(backBtn);

        panel.add(buttonPanel);

        add(panel);
        setVisible(true);
    }

    private void loadKeyFromConfig() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("config.json")));
            JSONObject obj = new JSONObject(content);
            actualKey = obj.getString("adminKey");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading admin key config.", "Error", JOptionPane.ERROR_MESSAGE);
            actualKey = ""; // fallback to empty to prevent access
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == proceedBtn) {
            String enteredKey = new String(keyField.getPassword());
            if (enteredKey.equals(actualKey)) {
                dispose();
                new AdminLoginUI();
            } else {
                attempts++;
                if (attempts >= MAX_ATTEMPTS) {
                    JOptionPane.showMessageDialog(this, "Too many failed attempts.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    dispose();
                    new LoginUI();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid admin key. Attempt " + attempts + " of " + MAX_ATTEMPTS, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == backBtn) {
            dispose();
            new LoginUI();
        }
    }
}

// ---------- Admin Login ----------
class AdminLoginUI extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn, registerBtn, backBtn;

    public AdminLoginUI() {
        setTitle("Admin Login");
        setSize(300, 250); // Increased height for 3 buttons
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        loginBtn = new JButton("Login");
        registerBtn = new JButton("Register");
        backBtn = new JButton("Back");

        loginBtn.addActionListener(this);
        registerBtn.addActionListener(this);
        backBtn.addActionListener(this);

        panel.add(loginBtn);
        panel.add(registerBtn);
        panel.add(backBtn);

        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            RegisteredAdmin loggedInAdmin = getLoggedInAdmin(username, password);
            if (loggedInAdmin != null) {
            JOptionPane.showMessageDialog(this, "Admin login successful!");
            dispose();
            new AdminDashboard(loggedInAdmin); // ✅ pass the object
            } else {
                JOptionPane.showMessageDialog(this, "Invalid admin credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == registerBtn) {
            dispose();
            new AdminRegisterUI(); // Must be defined elsewhere

        } else if (e.getSource() == backBtn) {
            dispose();
            new LoginUI();

        }
    }

    private RegisteredAdmin getLoggedInAdmin(String username, String password) {
    try {
        String content = new String(Files.readAllBytes(Paths.get("admins.json")));
        JSONArray admins = new JSONArray(content);
        for (int i = 0; i < admins.length(); i++) {
            JSONObject admin = admins.getJSONObject(i);
            if (admin.getString("username").equals(username) &&
                admin.getString("password").equals(password)) {
                return new RegisteredAdmin(
                    admin.getString("username"),
                    admin.getString("password"),
                    admin.getString("email")
                );
            }
        }
        } catch (IOException | JSONException e) {
        System.err.println("Error reading admins.json: " + e.getMessage());
        }
        return null;
    }

}

//-----------Admin Registration--------
class AdminRegisterUI extends JFrame implements ActionListener {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField;
    private JButton registerBtn, backBtn;

    public AdminRegisterUI() {
        setTitle("Register Admin");
        setSize(350, 220);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        emailField = new JTextField();

        panel.add(new JLabel("Username:")); panel.add(usernameField);
        panel.add(new JLabel("Password:")); panel.add(passwordField);
        panel.add(new JLabel("Email:")); panel.add(emailField);

        registerBtn = new JButton("Register");
        registerBtn.addActionListener(this);

        backBtn = new JButton("Back to Login");
        backBtn.addActionListener(this);

        panel.add(registerBtn);
        panel.add(backBtn);

        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerBtn) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JSONObject newAdmin = new JSONObject();
            newAdmin.put("username", username);
            newAdmin.put("password", password);
            newAdmin.put("email", email);

            try {
                File file = new File("admins.json");
                JSONArray admins;

                if (file.exists()) {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    admins = new JSONArray(content);
                } else {
                    admins = new JSONArray();
                }

                admins.put(newAdmin);
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(admins.toString(4));
                }

                JOptionPane.showMessageDialog(this, "Admin registered successfully!");
                dispose();
                new AdminLoginUI();
            } catch (IOException | JSONException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving admin info.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == backBtn) {
            dispose();
            new AdminLoginUI();
        }
    }
}

// -------- Admin Storage --------
class AdminStorage {

    public static List<RegisteredAdmin> loadAdmins(String filePath) {
        List<RegisteredAdmin> admins = new ArrayList<>();

        try {
            String jsonData = new String(java.nio.file.Files.readAllBytes(new File(filePath).toPath()));
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                admins.add(new RegisteredAdmin(
                        obj.getString("username"),
                        obj.getString("password"),
                        obj.getString("email")
                ));
            }
        } catch (Exception e) {
            System.out.println("Could not load admins.json: " + e.getMessage());
        }

        return admins;
    }

    public static void saveAdmins(List<RegisteredAdmin> admins, String filePath) {
        JSONArray jsonArray = new JSONArray();

        for (RegisteredAdmin a : admins) {
            JSONObject obj = new JSONObject();
            obj.put("username", a.getUsername());
            obj.put("password", a.getPassword());
            obj.put("email", a.getEmail());
            jsonArray.put(obj);
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonArray.toString(4));
        } catch (IOException e) {
            System.out.println("Could not save admins.json: " + e.getMessage());
        }
    }
}

// ---------- Customer Login ----------
class CustomerLoginUI extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn, registerBtn, backBtn;

    public CustomerLoginUI() {
        setTitle("Customer Login");
        setSize(300, 220);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        panel.add(new JLabel("Username:")); panel.add(usernameField);
        panel.add(new JLabel("Password:")); panel.add(passwordField);

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);

        registerBtn = new JButton("Register");
        registerBtn.addActionListener(this);

        backBtn = new JButton("Back");
        backBtn.addActionListener(this);

        panel.add(loginBtn); panel.add(registerBtn);
        panel.add(backBtn);

        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            RegisteredCustomer loggedInCustomer = getLoggedInCustomer(username, password);
            if (loggedInCustomer != null) {
            JOptionPane.showMessageDialog(this, "Customer login successful!");
            dispose();
            new CustomerDashboard(loggedInCustomer); // ✅ pass the object
            }
                else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == registerBtn) {
            dispose();
            new CustomerRegisterUI();
        } else if (e.getSource() == backBtn) {
            dispose();
            new LoginUI(); // your main role selection screen
        }
    }

    private RegisteredCustomer getLoggedInCustomer(String username, String password) {
    try {
        File file = new File("customers.json");
        if (!file.exists()) return null;

        String content = new String(Files.readAllBytes(file.toPath()));
        JSONArray customers = new JSONArray(content);

        for (int i = 0; i < customers.length(); i++) {
            JSONObject customer = customers.getJSONObject(i);
            if (customer.getString("username").equals(username) &&
                customer.getString("password").equals(password)) {
                return new RegisteredCustomer(
                    customer.getString("username"),
                    customer.getString("password"),
                    customer.getString("email"),
                    customer.getString("phone")
                );
            }
        }
        } catch (IOException | JSONException e) {
        e.printStackTrace();
        }
        return null;
    }

}

// ---------- Customer Registration ----------
class CustomerRegisterUI extends JFrame implements ActionListener {
    private JTextField usernameField, emailField, phoneField;
    private JPasswordField passwordField;
    private JButton registerBtn, backBtn;

    public CustomerRegisterUI() {
        setTitle("Customer Registration");
        setSize(350, 250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        emailField = new JTextField();
        phoneField = new JTextField();

        panel.add(new JLabel("Username:")); panel.add(usernameField);
        panel.add(new JLabel("Password:")); panel.add(passwordField);
        panel.add(new JLabel("Email:")); panel.add(emailField);
        panel.add(new JLabel("Phone Number:")); panel.add(phoneField);

        registerBtn = new JButton("Register");
        registerBtn.addActionListener(this);

        backBtn = new JButton("Back to Login");
        backBtn.addActionListener(this);

        panel.add(registerBtn); panel.add(backBtn);

        add(panel);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerBtn) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText();
            String phone = phoneField.getText();

            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JSONObject newCustomer = new JSONObject();
            newCustomer.put("username", username);
            newCustomer.put("password", password);
            newCustomer.put("email", email);
            newCustomer.put("phone", phone);

            try {
                File file = new File("customers.json");
                JSONArray customers;

                if (file.exists()) {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    customers = new JSONArray(content);
                } else {
                    customers = new JSONArray();
                }

                customers.put(newCustomer);
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write(customers.toString(4));
                }

                JOptionPane.showMessageDialog(this, "Customer registered successfully!");
                dispose();
                new CustomerLoginUI();
            } catch (IOException | JSONException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving customer info.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } else if (e.getSource() == backBtn) {
            dispose();
            new CustomerLoginUI();
        }
    }
}

// ------- Customer Storage ------
class CustomerStorage {

    public static List<RegisteredCustomer> loadCustomers(String filePath) {
        List<RegisteredCustomer> customers = new ArrayList<>();

        try {
            String jsonData = new String(java.nio.file.Files.readAllBytes(new File(filePath).toPath()));
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                customers.add(new RegisteredCustomer(
                        obj.getString("username"),
                        obj.getString("password"),
                        obj.getString("email"),
                        obj.getString("phone")
                ));
            }
        } catch (Exception e) {
            System.out.println("Could not load customers.json: " + e.getMessage());
        }

        return customers;
    }

    public static void saveCustomers(List<RegisteredCustomer> customers, String filePath) {
        JSONArray jsonArray = new JSONArray();

        for (RegisteredCustomer c : customers) {
            JSONObject obj = new JSONObject();
            obj.put("username", c.getUsername());
            obj.put("password", c.getPassword());
            obj.put("email", c.getEmail());
            obj.put("phone", c.getPhone());
            jsonArray.put(obj);
        }

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonArray.toString(4));
        } catch (IOException e) {
            System.out.println("Could not save customers.json: " + e.getMessage());
        }
    }
}

// Admin Dashboard UI
class AdminDashboard extends JFrame {
    private RegisteredAdmin loggedInAdmin;
    private List<MangaBook> inventory;

    public AdminDashboard(RegisteredAdmin admin) {
        this.loggedInAdmin = admin;

        setTitle("Admin Dashboard - Comic Bookstore");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ✅ Load comic inventory from JSON
        inventory = ComicBookStorage.loadComicBooks("comicbooks.json");

        // ✅ (Optional) Print to console or handle via GUI
        for (MangaBook book : inventory) {
            book.displayDetails(); // You could also show in JTable later
        }

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton manageBooksBtn = new JButton("Manage Books");
        JButton manageCustomersBtn = new JButton("Manage Customers");
        JButton viewReportsBtn = new JButton("View Reports");
        JButton viewProfileBtn = new JButton("View Profile");
        JButton logoutBtn = new JButton("Logout");

        panel.add(manageBooksBtn);
        panel.add(new JLabel());
        panel.add(manageCustomersBtn);
        panel.add(viewReportsBtn);
        panel.add(viewProfileBtn);
        panel.add(new JLabel());
        panel.add(logoutBtn);

        add(panel);
        setVisible(true);

        // Manage Books
        manageBooksBtn.addActionListener(e -> new ManageBooksUI());

        // Manage Customers
        manageCustomersBtn.addActionListener(e -> new ManageCustomersUI());

        // View Reports
        viewReportsBtn.addActionListener(e -> showReports());

        // View Profile
        viewProfileBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Admin Profile:\nUsername: " + loggedInAdmin.getUsername() + "\nEmail: " + loggedInAdmin.getEmail(),
                "Profile", JOptionPane.INFORMATION_MESSAGE);
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginUI();
        });
    }

    private void showReports() {
        int totalBooks = DataStore.books.size();
        int totalCustomers = DataStore.customers.size();
        int totalSales = DataStore.sales.size();

        String report = String.format("Reports:\nTotal Books: %d\nTotal Customers: %d\nTotal Sales: %d", totalBooks, totalCustomers, totalSales);
        JOptionPane.showMessageDialog(this, report, "Reports", JOptionPane.INFORMATION_MESSAGE);
    }
}

// ========== Manage Books ==========
class ManageBooksUI extends JFrame {
    private DefaultListModel<String> bookListModel = new DefaultListModel<>();
    private JList<String> bookList = new JList<>(bookListModel);
    private List<MangaBook> books = ComicBookStorage.loadComicBooks("comicbooks.json");

    // Fields made class-level for access in listeners
    private JTextField idField = new JTextField();
    private JTextField titleField = new JTextField();
    private JTextField authorField = new JTextField();
    private JTextField categoryField = new JTextField();
    private JTextField priceField = new JTextField();
    private JTextField stockField = new JTextField();

    public ManageBooksUI() {
        setTitle("Manage Books");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2));
        formPanel.setBorder(BorderFactory.createTitledBorder("Book Information"));

        formPanel.add(new JLabel("Book ID:")); formPanel.add(idField);
        formPanel.add(new JLabel("Title:")); formPanel.add(titleField);
        formPanel.add(new JLabel("Author:")); formPanel.add(authorField);
        formPanel.add(new JLabel("Category:")); formPanel.add(categoryField);
        formPanel.add(new JLabel("Price:")); formPanel.add(priceField);
        formPanel.add(new JLabel("Stock:")); formPanel.add(stockField);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Book");
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Load to list
        refreshBookList();

        // Select to fill fields
        bookList.addListSelectionListener(e -> {
            int index = bookList.getSelectedIndex();
            if (index != -1) {
                MangaBook book = books.get(index);
                idField.setText(book.getBookID());
                titleField.setText(book.getTitle());
                authorField.setText(book.getAuthor());
                categoryField.setText(book.getCategory());
                priceField.setText(String.valueOf(book.getPrice()));
                stockField.setText(String.valueOf(book.getStockQuantity()));
            }
        });

        // Add
        addButton.addActionListener(e -> {
            try {
                String id = idField.getText();
                String title = titleField.getText();
                String author = authorField.getText();
                String category = categoryField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());

                MangaBook book = new MangaBook(id, title, author, category, price, stock);
                books.add(book);
                saveBooks();
                refreshBookList();
                clearFields();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Edit
        editButton.addActionListener(e -> {
            int index = bookList.getSelectedIndex();
            if (index != -1) {
                try {
                    MangaBook book = books.get(index);
                    book.bookID = idField.getText();
                    book.title = titleField.getText();
                    book.author = authorField.getText();
                    book.category = categoryField.getText();
                    book.price = Double.parseDouble(priceField.getText());
                    book.stockQuantity = Integer.parseInt(stockField.getText());

                    saveBooks();
                    refreshBookList();
                    JOptionPane.showMessageDialog(this, "Book updated successfully.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to edit.");
            }
        });

        // Delete
        deleteButton.addActionListener(e -> {
            int index = bookList.getSelectedIndex();
            if (index != -1) {
                books.remove(index);
                saveBooks();
                refreshBookList();
                clearFields();
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookList), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void refreshBookList() {
        bookListModel.clear();
        for (MangaBook b : books) {
            bookListModel.addElement(b.getBookID() + ": " + b.getTitle() + " - RM" + b.getPrice());
        }
    }

    private void saveBooks() {
        ComicBookStorage.saveComicBooks(books, "comicbooks.json");
    }

    private void clearFields() {
        idField.setText("");
        titleField.setText("");
        authorField.setText("");
        categoryField.setText("");
        priceField.setText("");
        stockField.setText("");
    }
}

// Manage Customers UI (view and delete customers)
class ManageCustomersUI extends JFrame {
    private DefaultListModel<String> customerListModel = new DefaultListModel<>();
    private JList<String> customerList = new JList<>(customerListModel);

    public ManageCustomersUI() {
        setTitle("Manage Customers");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        reloadCustomerList();

        JButton deleteBtn = new JButton("Delete Selected Customer");
        JButton editBtn = new JButton("Edit Selected");
        JButton viewHistoryBtn = new JButton("View Purchase History");
        JButton addBtn = new JButton("Add New Customer");

        deleteBtn.addActionListener(e -> {
            int idx = customerList.getSelectedIndex();
            if (idx != -1) {
                DataStore.customers.remove(idx);
                reloadCustomerList();
            }
        });

        editBtn.addActionListener(e -> {
            int idx = customerList.getSelectedIndex();
            if (idx != -1) {
                RegisteredCustomer c = DataStore.customers.get(idx);
                String newUsername = JOptionPane.showInputDialog("Edit Username:", c.getUsername());
                String newEmail = JOptionPane.showInputDialog("Edit Email:", c.getEmail());
                String newPhone = JOptionPane.showInputDialog("Edit Phone:", c.getPhone());

                if (newUsername != null && newEmail != null && newPhone != null) {
                    c.setUsername(newUsername);
                    c.setEmail(newEmail);
                    c.setPhone(newPhone);
                    reloadCustomerList();
                }
            }
        });

        viewHistoryBtn.addActionListener(e -> {
            int idx = customerList.getSelectedIndex();
            if (idx != -1) {
                RegisteredCustomer c = DataStore.customers.get(idx);
                java.util.List<String> history = c.getPurchaseHistory();
                String message = history.isEmpty() ? "No purchase history." : String.join("\n", history);
                JOptionPane.showMessageDialog(this, message, "Purchase History", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        addBtn.addActionListener(e -> {
            JTextField usernameField = new JTextField();
            JTextField emailField = new JTextField();
            JTextField phoneField = new JTextField();
            JTextField passwordField = new JTextField();

            JPanel inputPanel = new JPanel(new GridLayout(4, 2));
            inputPanel.add(new JLabel("Username:")); inputPanel.add(usernameField);
            inputPanel.add(new JLabel("Email:")); inputPanel.add(emailField);
            inputPanel.add(new JLabel("Phone:")); inputPanel.add(phoneField);
            inputPanel.add(new JLabel("Password:")); inputPanel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add New Customer", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                RegisteredCustomer newCustomer = new RegisteredCustomer(
                    usernameField.getText().trim(),
                    passwordField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
                );
                DataStore.customers.add(newCustomer);
                reloadCustomerList();
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(viewHistoryBtn);

        panel.add(new JScrollPane(customerList), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void reloadCustomerList() {
        customerListModel.clear();
        for (RegisteredCustomer c : DataStore.customers) {
            customerListModel.addElement(c.getUsername() + " | Email: " + c.getEmail() + " | Phone: " + c.getPhone());
        }
    }
}

// Customer Dashboard
class CustomerDashboard extends JFrame {
    private RegisteredCustomer loggedInCustomer;

    private DefaultListModel<String> browseListModel = new DefaultListModel<>();
    private JList<String> browseList = new JList<>(browseListModel);
    private JTextField searchField = new JTextField();
    private ArrayList<ComicBook> filteredBooks = new ArrayList<>();
    private Map<ComicBook, Integer> cart = new HashMap<>();

    public CustomerDashboard(RegisteredCustomer customer) {
        this.loggedInCustomer = customer;

        setTitle("Customer Dashboard - Comic Bookstore");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        JButton checkoutBtn = new JButton("Checkout / Payment");
        JButton viewProfileBtn = new JButton("View Profile");
        JButton logoutBtn = new JButton("Logout");

        // Search panel and list for browsing
        JPanel browsePanel = new JPanel(new BorderLayout(5, 5));
        browsePanel.setBorder(BorderFactory.createTitledBorder("Browse Comics (In Stock)"));

        searchField.setToolTipText("Search by title or category");
        browsePanel.add(searchField, BorderLayout.NORTH);

        browsePanel.add(new JScrollPane(browseList), BorderLayout.CENTER);

        JButton addToCartBtn = new JButton("Add Selected Book to Cart");
        browsePanel.add(addToCartBtn, BorderLayout.SOUTH);

        mainPanel.add(browsePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(checkoutBtn);
        bottomPanel.add(viewProfileBtn);
        bottomPanel.add(logoutBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Load all in-stock books
        reloadBookList("");

        // Search filter
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                reloadBookList(searchField.getText());
            }
        });

        addToCartBtn.addActionListener(e -> {
            int idx = browseList.getSelectedIndex();
            if (idx != -1) {
                ComicBook selectedBook = filteredBooks.get(idx);
                if (selectedBook.getStockQuantity() > 0) {
                    cart.put(selectedBook, cart.getOrDefault(selectedBook, 0) + 1);
                    JOptionPane.showMessageDialog(this, selectedBook.getTitle() + " added to cart. Total qty: " + cart.get(selectedBook));
                } else {
                    JOptionPane.showMessageDialog(this, "Selected book is out of stock!", "Out of Stock", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        checkoutBtn.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty!");
                return;
            }
            new CheckoutUI(cart);
        });

        viewProfileBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                "Customer Profile:\nUsername: " + loggedInCustomer.getUsername() +
                "\nEmail: " + loggedInCustomer.getEmail() + "\nPhone: " + loggedInCustomer.getPhone(),
                "Profile", JOptionPane.INFORMATION_MESSAGE);
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginUI();
        });

        setVisible(true);
    }

    private void reloadBookList(String filter) {
        browseListModel.clear();
        filteredBooks.clear();

        for (ComicBook b : DataStore.books) {
            if (b.getStockQuantity() > 0 &&
               (b.getTitle().toLowerCase().contains(filter.toLowerCase()) ||
                b.getCategory().toLowerCase().contains(filter.toLowerCase()))) {
                browseListModel.addElement(String.format("%s (%s) - RM%.2f", b.getTitle(), b.getCategory(), b.getPrice()));
                filteredBooks.add(b);
            }
        }
    }
}

// Checkout UI
class CheckoutUI extends JFrame {
    private Map<ComicBook, Integer> cart;
    private JTextArea cartArea = new JTextArea(10, 30);
    private JTextField voucherField = new JTextField(10);
    private JLabel totalLabel = new JLabel("Total: RM0.00");
    private JLabel gstLabel = new JLabel("GST (6%): RM0.00");
    private JLabel grandTotalLabel = new JLabel("Grand Total: RM0.00");

    private double gstRate = 0.06; // GST 6%
    private double discountRate = 0;

    public CheckoutUI(Map<ComicBook, Integer> cart) {
        this.cart = cart;

        setTitle("Checkout / Payment");
        setSize(450, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        cartArea.setEditable(false);
        panel.add(new JScrollPane(cartArea));

        JPanel voucherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        voucherPanel.add(new JLabel("Voucher Code: "));
        voucherPanel.add(voucherField);
        JButton applyVoucherBtn = new JButton("Apply");
        voucherPanel.add(applyVoucherBtn);
        panel.add(voucherPanel);

        panel.add(totalLabel);
        panel.add(gstLabel);
        panel.add(grandTotalLabel);

        JButton cashBtn = new JButton("Pay with Cash");
        JButton cardBtn = new JButton("Pay with Card");

        JPanel btnPanel = new JPanel();
        btnPanel.add(cashBtn);
        btnPanel.add(cardBtn);
        panel.add(btnPanel);

        add(panel);

        updateCartDisplay();

        applyVoucherBtn.addActionListener(e -> applyVoucher());

        cashBtn.addActionListener(e -> {
            if (processPayment()) {
                JOptionPane.showMessageDialog(this, "Cash payment successful!");
                dispose();
            }
        });

        cardBtn.addActionListener(e -> {
            if (processPayment()) {
                JOptionPane.showMessageDialog(this, "Card payment successful!");
                dispose();
            }
        });

        setVisible(true);
    }

    private void updateCartDisplay() {
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Map.Entry<ComicBook, Integer> entry : cart.entrySet()) {
            ComicBook book = entry.getKey();
            int qty = entry.getValue();
            sb.append(String.format("%s x%d = RM%.2f\n", book.getTitle(), qty, book.getPrice() * qty));
            total += book.getPrice() * qty;
        }
        cartArea.setText(sb.toString());

        totalLabel.setText(String.format("Total: RM%.2f", total));
        double gst = total * gstRate;
        gstLabel.setText(String.format("GST (6%%): RM%.2f", gst));

        double discount = total * discountRate;
        double grandTotal = total + gst - discount;
        grandTotalLabel.setText(String.format("Grand Total (after %.0f%% discount): RM%.2f", discountRate * 100, grandTotal));
    }

    private void applyVoucher() {
        String code = voucherField.getText().trim().toUpperCase();
        if (DataStore.vouchers.containsKey(code)) {
            discountRate = DataStore.vouchers.get(code);
            JOptionPane.showMessageDialog(this, "Voucher applied: " + (discountRate * 100) + "% discount");
        } else {
            discountRate = 0;
            JOptionPane.showMessageDialog(this, "Invalid voucher code");
        }
        updateCartDisplay();
    }

    private boolean processPayment() {
        // Check stock
        for (Map.Entry<ComicBook, Integer> entry : cart.entrySet()) {
            if (entry.getValue() > entry.getKey().getStockQuantity()) {
                JOptionPane.showMessageDialog(this, "Not enough stock for " + entry.getKey().getTitle());
                return false;
            }
        }
        // Deduct stock
        for (Map.Entry<ComicBook, Integer> entry : cart.entrySet()) {
            ComicBook book = entry.getKey();
            book.setStockQuantity(book.getStockQuantity() - entry.getValue());
        }
        // Record sale (simple)
        DataStore.sales.add(UUID.randomUUID().toString());
        return true;
    }
}

// Abstract ComicBook class
abstract class ComicBook {
    protected String bookID;
    protected String title;
    protected String author;
    protected String category;
    protected double price;
    protected int stockQuantity;

    public ComicBook(String bookID, String title, String author, String category, double price, int stockQuantity) {
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.category = category;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public abstract void displayDetails();

    // Getters and Setters
    public String getBookID() { return bookID; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }

    public void setStockQuantity(int qty) { this.stockQuantity = qty; }
}

// Concrete Book class
class MangaBook extends ComicBook {
    public MangaBook(String bookID, String title, String author, String category, double price, int stockQuantity) {
        super(bookID, title, author, category, price, stockQuantity);
    }

    @Override
    public void displayDetails() {
        System.out.println("[" + category + "] " + title + " by " + author + " - RM" + price + " (Stock: " + stockQuantity + ")");
    }
}

// Comic Book Storage to JSON
class ComicBookStorage {

    public static void saveComicBooks(List<? extends ComicBook> books, String filename) {
        JSONArray jsonArray = new JSONArray();

        for (ComicBook book : books) {
            JSONObject obj = new JSONObject();
            obj.put("bookID", book.getBookID());
            obj.put("title", book.getTitle());
            obj.put("author", book.getAuthor());
            obj.put("category", book.getCategory());
            obj.put("price", book.getPrice());
            obj.put("stockQuantity", book.getStockQuantity());
            jsonArray.put(obj);
        }

        try (FileWriter writer = new FileWriter(filename)) {
        writer.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<MangaBook> loadComicBooks(String filePath) {
        List<MangaBook> books = new ArrayList<>();
        try {
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath)));
            JSONArray jsonArray = new JSONArray(content);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                books.add(new MangaBook(
                    obj.getString("bookID"),
                    obj.getString("title"),
                    obj.getString("author"),
                    obj.getString("category"),
                    obj.getDouble("price"),
                    obj.getInt("stockQuantity")
                ));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace(); // If file doesn't exist, return empty list
        }
        return books;
    }
}

// Customer class
class Customer {
    private String customerID;
    private String name;
    private String contactNumber;
    private String email;

    public Customer(String customerID, String name, String contactNumber, String email) {
        this.customerID = customerID;
        this.name = name;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public String getCustomerID() { return customerID; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void displayInfo() {
        System.out.println("Customer: " + name + " (ID: " + customerID + ", Email: " + email + ")");
    }
}

// Payable interface
interface Payable {
    void processPayment(double amount);
}

// Payment classes
class CashPayment implements Payable {
    public void processPayment(double amount) {
        System.out.println("Cash Payment received: RM" + amount);
    }
}

class CardPayment implements Payable {
    public void processPayment(double amount) {
        System.out.println("Card Payment processed: RM" + amount);
    }
}

// Custom Exception
class OutOfStockException extends Exception {
    public OutOfStockException(String message) {
        super(message);
    }
}

// ========== Shared Data ==========
class DataStore {
    public static final ArrayList<ComicBook> books = new ArrayList<>();
    public static final ArrayList<RegisteredCustomer> customers = new ArrayList<>();
    public static final ArrayList<RegisteredAdmin> admins = new ArrayList<>();
    public static final ArrayList<String> sales = new ArrayList<>(); // Simple sales record

    public static final Map<String, Double> vouchers = Map.of(
        "DISCOUNT10", 0.10,
        "SAVE20", 0.20
    );
}

// Main launcher
public class BookstoreSystem {
    public static void main(String[] args) {
        // Load comic books
        List<MangaBook> loadedBooks = ComicBookStorage.loadComicBooks("comicbooks.json");
        if (loadedBooks != null && !loadedBooks.isEmpty()) {
            DataStore.books.addAll(loadedBooks);
        } else {
            DataStore.books.add(new MangaBook("B001", "One Piece", "Eiichiro Oda", "Manga", 25.00, 10));
            DataStore.books.add(new MangaBook("B002", "Naruto", "Masashi Kishimoto", "Manga", 22.00, 8));
            DataStore.books.add(new MangaBook("B003", "Attack on Titan", "Hajime Isayama", "Manga", 30.00, 5));
            ComicBookStorage.saveComicBooks(DataStore.books, "comicbooks.json");
        }

        // Load customers
        List<RegisteredCustomer> loadedCustomers = CustomerStorage.loadCustomers("customers.json");
        if (loadedCustomers != null && !loadedCustomers.isEmpty()) {
            DataStore.customers.addAll(loadedCustomers);
        } else {
            DataStore.customers.add(new RegisteredCustomer("cust1", "pass1", "cust1@example.com", "0123456789"));
            CustomerStorage.saveCustomers(DataStore.customers, "customers.json");
        }

        // Load admins
        List<RegisteredAdmin> loadedAdmins = AdminStorage.loadAdmins("admins.json");
        if (loadedAdmins != null && !loadedAdmins.isEmpty()) {
            DataStore.admins.addAll(loadedAdmins);
        } else {
            DataStore.admins.add(new RegisteredAdmin("admin", "admin123", "admin@comic.com"));
            AdminStorage.saveAdmins(DataStore.admins, "admins.json");
        }

        // Start the UI
        SwingUtilities.invokeLater(() -> new LoginUI());
    }
}