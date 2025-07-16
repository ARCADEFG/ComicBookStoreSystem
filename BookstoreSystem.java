import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

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

    // Voucher codes: code -> discount rate
    public static final Map<String, Double> vouchers = Map.of(
        "DISCOUNT10", 0.10,
        "SAVE20", 0.20
    );
}

// Admin Dashboard UI
class AdminDashboard extends JFrame {
    private RegisteredAdmin loggedInAdmin;

    public AdminDashboard(RegisteredAdmin admin) {
        this.loggedInAdmin = admin;

        setTitle("Admin Dashboard - Comic Bookstore");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
    private ArrayList<ComicBook> books = DataStore.books;

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

        for (ComicBook b : books) {
            bookListModel.addElement(b.getBookID() + ": " + b.getTitle() + " - RM" + b.getPrice());
        }

        //Select to fill fields
        bookList.addListSelectionListener(e -> {
            int index = bookList.getSelectedIndex();
            if (index != -1) {
                ComicBook book = books.get(index);
                idField.setText(book.getBookID());
                titleField.setText(book.getTitle());
                authorField.setText(book.getAuthor());
                categoryField.setText(book.getCategory());
                priceField.setText(String.valueOf(book.getPrice()));
                stockField.setText(String.valueOf(book.getStockQuantity()));
            }
        });

        //Add
        addButton.addActionListener(e -> {
            try {
                String id = idField.getText();
                String title = titleField.getText();
                String author = authorField.getText();
                String category = categoryField.getText();
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());

                ComicBook book = new MangaBook(id, title, author, category, price, stock);
                books.add(book);
                bookListModel.addElement(id + ": " + title + " - RM" + price);

                clearFields();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input format.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        //Edit
        editButton.addActionListener(e -> {
            int index = bookList.getSelectedIndex();
            if (index != -1) {
                try {
                    ComicBook book = books.get(index);

                    // Update values
                    book.bookID = idField.getText();
                    book.title = titleField.getText();
                    book.author = authorField.getText();
                    book.category = categoryField.getText();
                    book.price = Double.parseDouble(priceField.getText());
                    book.stockQuantity = Integer.parseInt(stockField.getText());

                    // Update list display
                    bookListModel.set(index, book.getBookID() + ": " + book.getTitle() + " - RM" + book.getPrice());
                    JOptionPane.showMessageDialog(this, "Book updated successfully.");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input format.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a book to edit.");
            }
        });

        //Delete
        deleteButton.addActionListener(e -> {
            int index = bookList.getSelectedIndex();
            if (index != -1) {
                books.remove(index);
                bookListModel.remove(index);
                clearFields();
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookList), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
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

// Login UI
class LoginUI extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginUI() {
        setTitle("Comic Bookstore Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);

        panel.add(new JLabel()); // filler
        panel.add(loginButton);

        // Buttons for Registration
        JButton registerCustomerBtn = new JButton("Register as Customer");
        JButton registerAdminBtn = new JButton("Register as Admin");

        registerCustomerBtn.addActionListener(e -> new CustomerRegistrationUI());
        registerAdminBtn.addActionListener(e -> new AdminRegistrationUI());

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(registerCustomerBtn);
        bottomPanel.add(registerAdminBtn);

        add(panel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());

        // Hardcoded admin for default login
        if (username.equals("admin") && password.equals("admin123")) {
            JOptionPane.showMessageDialog(this, "Admin login successful!");
            dispose();
            new AdminDashboard(new RegisteredAdmin("admin", "admin123", "admin@comic.com"));
            return;
        }

        // Hardcoded customer for default login
        if (username.equals("cust1") && password.equals("pass1")) {
            JOptionPane.showMessageDialog(this, "Customer login successful!");
            dispose();
            RegisteredCustomer dummyCust = new RegisteredCustomer("cust1", "pass1", "cust1@example.com", "0123456789");
            new CustomerDashboard(dummyCust);
            return;
        }

        // Check Registered Admins
        for (RegisteredAdmin admin : DataStore.admins) {
            if (admin.getUsername().equals(username) && admin.getPassword().equals(password)) {
                JOptionPane.showMessageDialog(this, "Admin login successful!");
                dispose();
                new AdminDashboard(admin);
                return;
            }
        }

        // Check Registered Customers
        for (RegisteredCustomer cust : DataStore.customers) {
            if (cust.getUsername().equals(username) && cust.getPassword().equals(password)) {
                JOptionPane.showMessageDialog(this, "Customer login successful!");
                dispose();
                new CustomerDashboard(cust);
                return;
            }
        }

        JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// RegisteredCustomer class for customer registration
class RegisteredCustomer {
    private String username, password, email, phone;
    private final ArrayList<String> purchaseHistory = new ArrayList<>();

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
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public void addPurchaseRecord(String record) { purchaseHistory.add(record); }
    public java.util.List<String> getPurchaseHistory() { return purchaseHistory;}
}

// Customer Registration UI
class CustomerRegistrationUI extends JFrame {
    private JTextField usernameField, emailField, phoneField;
    private JPasswordField passwordField;
    private JButton registerButton;

    public CustomerRegistrationUI() {
        setTitle("Register as Customer");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Phone:"));
        phoneField = new JTextField();
        add(phoneField);

        registerButton = new JButton("Register");
        add(new JLabel()); // spacer
        add(registerButton);

        registerButton.addActionListener(e -> registerCustomer());

        setVisible(true);
    }

    private void registerCustomer() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (RegisteredCustomer cust : DataStore.customers) {
            if (cust.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        DataStore.customers.add(new RegisteredCustomer(username, password, email, phone));
        JOptionPane.showMessageDialog(this, "Registration successful!");
        this.dispose();
    }
}

// RegisteredAdmin class for admin registration
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

// Admin Registration UI
class AdminRegistrationUI extends JFrame {
    private JTextField usernameField, emailField;
    private JPasswordField passwordField;
    private JButton registerButton;

    public AdminRegistrationUI() {
        setTitle("Register as Admin");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        registerButton = new JButton("Register");
        add(new JLabel()); // spacer
        add(registerButton);

        registerButton.addActionListener(e -> registerAdmin());

        setVisible(true);
    }

    private void registerAdmin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (RegisteredAdmin admin : DataStore.admins) {
            if (admin.getUsername().equalsIgnoreCase(username)) {
                JOptionPane.showMessageDialog(this, "Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        DataStore.admins.add(new RegisteredAdmin(username, password, email));
        JOptionPane.showMessageDialog(this, "Admin registration successful!");
        this.dispose();
    }
}

// Main launcher
public class BookstoreSystem {
    public static void main(String[] args) {
        // Load some sample data for demo
        if (DataStore.books.isEmpty()) {
            DataStore.books.add(new MangaBook("B001", "One Piece", "Eiichiro Oda", "Manga", 25.00, 10));
            DataStore.books.add(new MangaBook("B002", "Naruto", "Masashi Kishimoto", "Manga", 22.00, 8));
            DataStore.books.add(new MangaBook("B003", "Attack on Titan", "Hajime Isayama", "Manga", 30.00, 5));
        }
        if (DataStore.customers.isEmpty()) {
            DataStore.customers.add(new RegisteredCustomer("cust1", "pass1", "cust1@example.com", "0123456789"));
        }
        if (DataStore.admins.isEmpty()) {
            DataStore.admins.add(new RegisteredAdmin("admin", "admin123", "admin@comic.com"));
        }

        SwingUtilities.invokeLater(() -> new LoginUI());
    }
}