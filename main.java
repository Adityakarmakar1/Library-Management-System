import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class LibraryManagementSystem extends JFrame {
    // Data files
    private static final String BOOKS_FILE = "books.dat";
    private static final String MEMBERS_FILE = "members.dat";
    private static final String ISSUES_FILE = "issues.dat";

    // In-memory data
    private java.util.List<Book> books = new ArrayList<>();
    private java.util.List<Member> members = new ArrayList<>();
    // Map bookId -> memberId for issued books
    private Map<String, String> issued = new HashMap<>();

    // UI components
    private JTable booksTable;
    private DefaultTableModel booksModel;
    private JTextField searchField;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSystem app = new LibraryManagementSystem();
            app.setVisible(true);
        });
    }

    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        loadData();
        initUI();
        refreshBooksTable(books);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Top toolbar with actions
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton addBookBtn = new JButton("Add Book");
        addBookBtn.addActionListener(e -> openAddBookDialog());
        toolbar.add(addBookBtn);

        JButton addMemberBtn = new JButton("Add Member");
        addMemberBtn.addActionListener(e -> openAddMemberDialog());
        toolbar.add(addMemberBtn);

        JButton issueBtn = new JButton("Issue Book");
        issueBtn.addActionListener(e -> openIssueDialog());
        toolbar.add(issueBtn);

        JButton returnBtn = new JButton("Return Book");
        returnBtn.addActionListener(e -> openReturnDialog());
        toolbar.add(returnBtn);

        JButton viewMembersBtn = new JButton("View Members");
        viewMembersBtn.addActionListener(e -> showMembersDialog());
        toolbar.add(viewMembersBtn);

        root.add(toolbar, BorderLayout.NORTH);

        // Center: books table
        booksModel = new DefaultTableModel(new Object[]{"ID", "Title", "Author", "Year", "Total", "Available", "Issued To"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        booksTable = new JTable(booksModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(booksTable);
        root.add(tableScroll, BorderLayout.CENTER);

        // Bottom: search and other controls
        JPanel bottom = new JPanel(new BorderLayout(8, 8));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(30);
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Go");
        searchBtn.addActionListener(e -> doSearch());
        searchPanel.add(searchBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshBooksTable(books));
        searchPanel.add(refreshBtn);

        bottom.add(searchPanel, BorderLayout.WEST);

        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save Data");
        saveBtn.addActionListener(e -> { saveData(); JOptionPane.showMessageDialog(this, "Data saved."); });
        savePanel.add(saveBtn);

        JButton exportBtn = new JButton("Export Books CSV");
        exportBtn.addActionListener(e -> exportBooksCSV());
        savePanel.add(exportBtn);

        bottom.add(savePanel, BorderLayout.EAST);

        root.add(bottom, BorderLayout.SOUTH);

        add(root);
    }

    // --- Dialogs and actions ---
    private void openAddBookDialog() {
        JDialog d = new JDialog(this, "Add New Book", true);
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST;
        p.add(new JLabel("Book ID:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST;
        JTextField idField = new JTextField(15); p.add(idField, c);

        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Title:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; JTextField titleField = new JTextField(15); p.add(titleField, c);

        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Author:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; JTextField authorField = new JTextField(15); p.add(authorField, c);

        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Year:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; JTextField yearField = new JTextField(6); p.add(yearField, c);

        c.gridx = 0; c.gridy++; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Quantity:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.WEST; JTextField qtyField = new JTextField(6); p.add(qtyField, c);

        c.gridx = 0; c.gridy++; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        JButton addBtn = new JButton("Add Book");
        addBtn.addActionListener(ev -> {
            String id = idField.getText().trim();
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            int year = parseIntSafe(yearField.getText().trim());
            int qty = Math.max(1, parseIntSafe(qtyField.getText().trim()));
            if (id.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(d, "ID and Title are required."); return;
            }
            if (findBookById(id) != null) {
                JOptionPane.showMessageDialog(d, "Book with this ID already exists."); return;
            }
            Book b = new Book(id, title, author, year, qty);
            books.add(b);
            saveData();
            refreshBooksTable(books);
            d.dispose();
        });
        p.add(addBtn, c);

        d.add(p);
        d.setVisible(true);
    }

    private void openAddMemberDialog() {
        JDialog d = new JDialog(this, "Add New Member", true);
        d.setSize(350, 220);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx=0; c.gridy=0; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Member ID:"), c);
        c.gridx=1; c.anchor = GridBagConstraints.WEST; JTextField idField = new JTextField(12); p.add(idField, c);

        c.gridx=0; c.gridy++; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Name:"), c);
        c.gridx=1; c.anchor = GridBagConstraints.WEST; JTextField nameField = new JTextField(12); p.add(nameField, c);

        c.gridx=0; c.gridy++; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Contact:"), c);
        c.gridx=1; c.anchor = GridBagConstraints.WEST; JTextField contactField = new JTextField(12); p.add(contactField, c);

        c.gridx=0; c.gridy++; c.gridwidth=2; c.anchor = GridBagConstraints.CENTER;
        JButton addBtn = new JButton("Add Member");
        addBtn.addActionListener(ev -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            if (id.isEmpty() || name.isEmpty()) { JOptionPane.showMessageDialog(d, "ID and Name required"); return; }
            if (findMemberById(id) != null) { JOptionPane.showMessageDialog(d, "Member ID exists"); return; }
            members.add(new Member(id, name, contact));
            saveData();
            d.dispose();
        });
        p.add(addBtn, c);
        d.add(p);
        d.setVisible(true);
    }

    private void openIssueDialog() {
        JDialog d = new JDialog(this, "Issue Book", true);
        d.setSize(380, 240);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);

        c.gridx=0; c.gridy=0; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Book ID:"), c);
        c.gridx=1; c.anchor = GridBagConstraints.WEST; JTextField bookField = new JTextField(12); p.add(bookField, c);

        c.gridx=0; c.gridy++; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Member ID:"), c);
        c.gridx=1; c.anchor = GridBagConstraints.WEST; JTextField memberField = new JTextField(12); p.add(memberField, c);

        c.gridx=0; c.gridy++; c.gridwidth=2; c.anchor = GridBagConstraints.CENTER;
        JButton issueBtn = new JButton("Issue");
        issueBtn.addActionListener(ev -> {
            String bookId = bookField.getText().trim();
            String memberId = memberField.getText().trim();
            Book b = findBookById(bookId);
            Member m = findMemberById(memberId);
            if (b == null) { JOptionPane.showMessageDialog(d, "Book not found"); return; }
            if (m == null) { JOptionPane.showMessageDialog(d, "Member not found"); return; }
            if (issued.containsKey(bookId)) { JOptionPane.showMessageDialog(d, "Book already issued."); return; }
            if (b.getAvailable() <= 0) { JOptionPane.showMessageDialog(d, "No copies available."); return; }
            issued.put(bookId, memberId);
            b.setAvailable(b.getAvailable() - 1);
            saveData();
            refreshBooksTable(books);
            d.dispose();
        });
        p.add(issueBtn, c);
        d.add(p);
        d.setVisible(true);
    }

    private void openReturnDialog() {
        JDialog d = new JDialog(this, "Return Book", true);
        d.setSize(360, 200);
        d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);

        c.gridx=0; c.gridy=0; c.anchor = GridBagConstraints.EAST; p.add(new JLabel("Book ID:"), c);
        c.gridx=1; c.anchor = GridBagConstraints.WEST; JTextField bookField = new JTextField(12); p.add(bookField, c);

        c.gridx=0; c.gridy++; c.gridwidth=2; c.anchor = GridBagConstraints.CENTER;
        JButton returnBtn = new JButton("Return");
        returnBtn.addActionListener(ev -> {
            String bookId = bookField.getText().trim();
            if (!issued.containsKey(bookId)) { JOptionPane.showMessageDialog(d, "This book is not issued."); return; }
            String memberId = issued.remove(bookId);
            Book b = findBookById(bookId);
            if (b != null) b.setAvailable(b.getAvailable() + 1);
            saveData();
            refreshBooksTable(books);
            d.dispose();
        });
        p.add(returnBtn, c);
        d.add(p);
        d.setVisible(true);
    }

    private void showMembersDialog() {
        JDialog d = new JDialog(this, "Members", true);
        d.setSize(480, 400);
        d.setLocationRelativeTo(this);
        String[] cols = {"ID", "Name", "Contact"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Member mem : members) m.addRow(new Object[]{mem.getId(), mem.getName(), mem.getContact()});
        JTable t = new JTable(m);
        d.add(new JScrollPane(t));
        d.setVisible(true);
    }

    private void doSearch() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) { refreshBooksTable(books); return; }
        List<Book> filtered = new ArrayList<>();
        for (Book b : books) {
            if (b.getId().toLowerCase().contains(q) || b.getTitle().toLowerCase().contains(q) || b.getAuthor().toLowerCase().contains(q)) filtered.add(b);
        }
        refreshBooksTable(filtered);
    }

    // --- Utility methods ---
    private void refreshBooksTable(java.util.List<Book> list) {
        booksModel.setRowCount(0);
        for (Book b : list) {
            String issuedTo = issued.containsKey(b.getId()) ? issued.get(b.getId()) : "-";
            booksModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getYear(), b.getTotalQuantity(), b.getAvailable(), issuedTo});
        }
    }

    private Book findBookById(String id) {
        for (Book b : books) if (b.getId().equals(id)) return b;
        return null;
    }

    private Member findMemberById(String id) {
        for (Member m : members) if (m.getId().equals(id)) return m;
        return null;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private void exportBooksCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("books_export.csv"));
        int r = chooser.showSaveDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(f)) {
            pw.println("ID,Title,Author,Year,Total,Available");
            for (Book b : books) {
                pw.printf("%s,%s,%s,%d,%d,%d\n", escapeCsv(b.getId()), escapeCsv(b.getTitle()), escapeCsv(b.getAuthor()), b.getYear(), b.getTotalQuantity(), b.getAvailable());
            }
            JOptionPane.showMessageDialog(this, "Exported to " + f.getAbsolutePath());
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage()); }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\n") || s.contains("\"")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    // --- Persistence ---
    @SuppressWarnings("unchecked")
    private void loadData() {
        // books
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BOOKS_FILE))) {
            books = (List<Book>) ois.readObject();
        } catch (Exception e) {
            books = new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MEMBERS_FILE))) {
            members = (List<Member>) ois.readObject();
        } catch (Exception e) { members = new ArrayList<>(); }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ISSUES_FILE))) {
            issued = (Map<String, String>) ois.readObject();
        } catch (Exception e) { issued = new HashMap<>(); }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BOOKS_FILE))) {
            oos.writeObject(books);
        } catch (Exception e) { e.printStackTrace(); }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MEMBERS_FILE))) {
            oos.writeObject(members);
        } catch (Exception e) { e.printStackTrace(); }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ISSUES_FILE))) {
            oos.writeObject(issued);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Data models ---
    static class Book implements Serializable {
        private String id, title, author;
        private int year;
        private int totalQuantity;
        private int available;
        public Book(String id, String title, String author, int year, int qty) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.totalQuantity = qty;
            this.available = qty;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author == null ? "" : author; }
        public int getYear() { return year; }
        public int getTotalQuantity() { return totalQuantity; }
        public int getAvailable() { return available; }
        public void setAvailable(int a) { this.available = a; }
    }

    static class Member implements Serializable {
        private String id, name, contact;
        public Member(String id, String name, String contact) { this.id = id; this.name = name; this.contact = contact; }
        public String getId() { return id; }
        public String getName() { return name; }
        public String getContact() { return contact; }
    }
}
