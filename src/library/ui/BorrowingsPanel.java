package library.ui;

import library.dao.*;
import library.model.*;
import library.util.Theme;
import library.ui.UIComponents.*;
import library.ui.BooksPanel.ModernScrollBarUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BorrowingsPanel extends JPanel {
    private BorrowingDAO borrowingDAO = new BorrowingDAO();
    private BookDAO bookDAO = new BookDAO();
    private MemberDAO memberDAO = new MemberDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterBox;

    private static final String[] COLUMNS = {"ID", "Book Title", "Member Name", "Borrow Date", "Due Date", "Return Date", "Fine (₹)", "Status"};

    public BorrowingsPanel() {
        setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(Theme.PADDING, Theme.PADDING, Theme.PADDING, Theme.PADDING));
        buildUI();
        loadBorrowings("All");
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("📖  Borrowings");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        filterBox = new JComboBox<>(new String[]{"All", "Active", "Overdue", "Returned"});
        filterBox.setFont(Theme.FONT_BODY);
        filterBox.setBackground(Theme.INPUT_BG);
        filterBox.setForeground(Theme.TEXT_PRIMARY);
        filterBox.setPreferredSize(new Dimension(130, 36));
        filterBox.addActionListener(e -> loadBorrowings((String) filterBox.getSelectedItem()));

        ModernButton issueBtn  = new ModernButton("+ Issue Book", Theme.ACCENT_GOLD);
        ModernButton returnBtn = new ModernButton("↩  Return", Theme.ACCENT_GREEN);

        issueBtn.addActionListener(e -> showIssueDialog());
        returnBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showWarning("Select a borrowing record to return."); return; }
            String status = tableModel.getValueAt(row, 7).toString();
            if (!status.equals("Borrowed")) { showWarning("This book has already been returned."); return; }
            showReturnDialog(row);
        });

        actions.add(filterBox);
        actions.add(issueBtn);
        actions.add(returnBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scroll.getViewport().setBackground(Theme.BG_SECONDARY);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        add(scroll, BorderLayout.CENTER);

        // Summary bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel hint = new JLabel("💡  Double-click a row to see details. Fine rate: ₹2/day overdue.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        bottomBar.add(hint);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadBorrowings(String filter) {
        SwingWorker<List<Borrowing>, Void> worker = new SwingWorker<List<Borrowing>, Void>() {
            @Override protected List<Borrowing> doInBackground() throws Exception {
                if ("Active".equals(filter))  return borrowingDAO.getActiveBorrowings();
                if ("Overdue".equals(filter)) return borrowingDAO.getOverdueBorrowings();
                return borrowingDAO.getAllBorrowings();
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    List<Borrowing> list = get();
                    LocalDate today = LocalDate.now();
                    for (Borrowing b : list) {
                        String statusDisplay = b.getStatus();
                        if ("Borrowed".equals(b.getStatus()) && b.getDueDate() != null) {
                            try {
                                LocalDate due = LocalDate.parse(b.getDueDate());
                                if (due.isBefore(today)) statusDisplay = "Overdue";
                            } catch (Exception ignored) {}
                        }
                        tableModel.addRow(new Object[]{
                            b.getId(), b.getBookTitle(), b.getMemberName(),
                            b.getBorrowDate(), b.getDueDate(),
                            b.getReturnDate() != null ? b.getReturnDate() : "-",
                            String.format("%.2f", b.getFineAmount()),
                            statusDisplay
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private void showIssueDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Issue Book", true);
        dialog.setSize(480, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Book combo
        JComboBox<String> bookBox = new JComboBox<>();
        bookBox.setFont(Theme.FONT_BODY);
        bookBox.setBackground(Theme.INPUT_BG);
        bookBox.setForeground(Theme.TEXT_PRIMARY);
        bookBox.setPreferredSize(new Dimension(280, 36));

        // Member combo
        JComboBox<String> memberBox = new JComboBox<>();
        memberBox.setFont(Theme.FONT_BODY);
        memberBox.setBackground(Theme.INPUT_BG);
        memberBox.setForeground(Theme.TEXT_PRIMARY);
        memberBox.setPreferredSize(new Dimension(280, 36));

        ModernTextField dueDateF = new ModernTextField(LocalDate.now().plusDays(14).toString());
        dueDateF.setPreferredSize(new Dimension(280, 36));

        // Load data
        try {
            List<Book> books = bookDAO.getAllBooks();
            for (Book b : books) {
                if (b.getAvailableCopies() > 0)
                    bookBox.addItem(b.getId() + " | " + b.getTitle() + " (" + b.getAvailableCopies() + " avail)");
            }
            List<Member> members = memberDAO.getAllMembers();
            for (Member m : members) {
                if ("Active".equals(m.getStatus()))
                    memberBox.addItem(m.getId() + " | " + m.getName());
            }
        } catch (Exception e) { e.printStackTrace(); }

        String[] labels = {"Select Book *", "Select Member *", "Due Date *"};
        JComponent[] comps = {bookBox, memberBox, dueDateF};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            panel.add(UIComponents.label(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.65;
            panel.add(comps[i], gbc);
        }

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 5, 0, 5);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        ModernButton cancel = new ModernButton("Cancel", Theme.TEXT_MUTED);
        ModernButton issue  = new ModernButton("Issue Book", Theme.ACCENT_GOLD);

        cancel.addActionListener(e -> dialog.dispose());
        issue.addActionListener(e -> {
            if (bookBox.getSelectedItem() == null || memberBox.getSelectedItem() == null) {
                showWarning("Select a book and a member."); return;
            }
            try {
                int bookId   = Integer.parseInt(bookBox.getSelectedItem().toString().split(" \\| ")[0].trim());
                int memberId = Integer.parseInt(memberBox.getSelectedItem().toString().split(" \\| ")[0].trim());
                String due   = dueDateF.getText().trim();
                LocalDate.parse(due); // validate
                borrowingDAO.borrowBook(bookId, memberId, due);
                dialog.dispose();
                loadBorrowings((String) filterBox.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Book issued successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (java.time.format.DateTimeParseException ex) {
                showWarning("Due date must be in YYYY-MM-DD format.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        btnPanel.add(cancel); btnPanel.add(issue);
        panel.add(btnPanel, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void showReturnDialog(int row) {
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String bookTitle  = tableModel.getValueAt(row, 1).toString();
        String dueDate    = tableModel.getValueAt(row, 4).toString();

        double fine = 0;
        try {
            LocalDate due   = LocalDate.parse(dueDate);
            LocalDate today = LocalDate.now();
            if (today.isAfter(due)) {
                long days = ChronoUnit.DAYS.between(due, today);
                fine = days * 2.0; // ₹2 per day
            }
        } catch (Exception ignored) {}

        String msg = "Return \"" + bookTitle + "\"?\n" +
                     "Due: " + dueDate + "\n" +
                     (fine > 0 ? "⚠ Fine: ₹" + String.format("%.2f", fine) : "✅ No fine");

        int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirm Return", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                borrowingDAO.returnBook(id, fine);
                loadBorrowings((String) filterBox.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Book returned successfully!" +
                    (fine > 0 ? "\nFine collected: ₹" + String.format("%.2f", fine) : ""), "Returned", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { showError(ex.getMessage()); }
        }
    }

    private void styleTable() {
        table.setBackground(Theme.BG_SECONDARY);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0x2A3D52));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.TABLE_HEADER);
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setFont(Theme.FONT_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        int[] widths = {40, 200, 150, 100, 100, 100, 80, 90};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBackground(selected ? new Color(0x2A3D52) : (row % 2 == 0 ? Theme.BG_SECONDARY : Theme.TABLE_ALT));
                if (col == 7 && value != null) {
                    String v = value.toString();
                    setForeground(v.equals("Returned") ? Theme.ACCENT_GREEN : v.equals("Overdue") ? Theme.ACCENT_RED : Theme.ACCENT_GOLD);
                } else {
                    setForeground(Theme.TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void showError(String msg)   { JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE); }
}
