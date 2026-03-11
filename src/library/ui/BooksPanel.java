package library.ui;

import library.dao.BookDAO;
import library.model.Book;
import library.util.Theme;
import library.ui.UIComponents.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class BooksPanel extends JPanel {
    private BookDAO bookDAO = new BookDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private ModernTextField searchField;

    private static final String[] COLUMNS = {"ID", "Title", "Author", "ISBN", "Genre", "Year", "Copies", "Available"};

    public BooksPanel() {
        setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(Theme.PADDING, Theme.PADDING, Theme.PADDING, Theme.PADDING));
        buildUI();
        loadBooks(null);
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("📚  Book Catalog");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchField = new ModernTextField("🔍  Search books...");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.addActionListener(e -> loadBooks(searchField.getText().trim()));
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadBooks(searchField.getText().trim()); }
        });

        ModernButton addBtn = new ModernButton("+ Add Book", Theme.ACCENT_GOLD);
        addBtn.addActionListener(e -> showBookDialog(null));

        actions.add(searchField);
        actions.add(addBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };

        table = new JTable(tableModel);
        styleTable(table);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showBookDialog(getBookFromRow(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scroll.setBackground(Theme.BG_SECONDARY);
        scroll.getViewport().setBackground(Theme.BG_SECONDARY);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        add(scroll, BorderLayout.CENTER);

        // Bottom action bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(10, 0, 0, 0));

        ModernButton editBtn = new ModernButton("✏  Edit", Theme.ACCENT_TEAL);
        ModernButton deleteBtn = new ModernButton("🗑  Delete", Theme.ACCENT_RED);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            showBookDialog(getBookFromRow(row));
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a book first.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            String title2 = (String) tableModel.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "Delete \"" + title2 + "\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try { bookDAO.deleteBook(id); loadBooks(null); }
                catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            }
        });

        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadBooks(String query) {
        SwingWorker<List<Book>, Void> worker = new SwingWorker<List<Book>, Void>() {
            @Override protected List<Book> doInBackground() throws Exception {
                return (query == null || query.isEmpty()) ? bookDAO.getAllBooks() : bookDAO.searchBooks(query);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Book b : get()) {
                        tableModel.addRow(new Object[]{
                            b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(),
                            b.getGenre(), b.getYear(), b.getTotalCopies(), b.getAvailableCopies()
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private Book getBookFromRow(int row) {
        Book b = new Book();
        b.setId((int) tableModel.getValueAt(row, 0));
        b.setTitle((String) tableModel.getValueAt(row, 1));
        b.setAuthor((String) tableModel.getValueAt(row, 2));
        b.setIsbn((String) tableModel.getValueAt(row, 3));
        b.setGenre((String) tableModel.getValueAt(row, 4));
        b.setYear(tableModel.getValueAt(row, 5) instanceof Integer ? (int) tableModel.getValueAt(row, 5) : 0);
        b.setTotalCopies(tableModel.getValueAt(row, 6) instanceof Integer ? (int) tableModel.getValueAt(row, 6) : 1);
        b.setAvailableCopies(tableModel.getValueAt(row, 7) instanceof Integer ? (int) tableModel.getValueAt(row, 7) : 1);
        return b;
    }

    private void showBookDialog(Book existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add New Book" : "Edit Book", true);
        dialog.setSize(520, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ModernTextField titleF    = new ModernTextField("Book title");
        ModernTextField authorF   = new ModernTextField("Author name");
        ModernTextField isbnF     = new ModernTextField("ISBN");
        ModernTextField genreF    = new ModernTextField("Genre");
        ModernTextField publisherF= new ModernTextField("Publisher");
        ModernTextField yearF     = new ModernTextField("Publication year");
        ModernTextField copiesF   = new ModernTextField("Total copies");
        ModernTextArea descF      = new ModernTextArea(3, 20);

        if (existing != null) {
            titleF.setText(existing.getTitle());
            authorF.setText(existing.getAuthor());
            isbnF.setText(existing.getIsbn());
            genreF.setText(existing.getGenre());
            publisherF.setText(existing.getPublisher());
            yearF.setText(String.valueOf(existing.getYear()));
            copiesF.setText(String.valueOf(existing.getTotalCopies()));
            descF.setText(existing.getDescription());
        }

        String[][] fields = {{"Title *", null}, {"Author *", null}, {"ISBN", null}, {"Genre", null},
                              {"Publisher", null}, {"Year", null}, {"Total Copies", null}, {"Description", null}};
        JComponent[] comps = {titleF, authorF, isbnF, genreF, publisherF, yearF, copiesF, new JScrollPane(descF)};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            JLabel lbl = UIComponents.label(fields[i][0]);
            panel.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            if (comps[i] instanceof JScrollPane) {
                    JScrollPane sp = (JScrollPane) comps[i];
                sp.setPreferredSize(new Dimension(280, 70));
                sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
                sp.getViewport().setBackground(Theme.INPUT_BG);
            } else {
                ((JComponent)comps[i]).setPreferredSize(new Dimension(280, 36));
            }
            panel.add(comps[i], gbc);
        }

        // Buttons
        gbc.gridx = 0; gbc.gridy = fields.length; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 5, 0, 5);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        ModernButton cancel = new ModernButton("Cancel", Theme.TEXT_MUTED);
        ModernButton save   = new ModernButton(existing == null ? "Add Book" : "Save Changes", Theme.ACCENT_GOLD);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            if (titleF.getText().trim().isEmpty() || authorF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title and Author are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Book b = existing != null ? existing : new Book();
                b.setTitle(titleF.getText().trim());
                b.setAuthor(authorF.getText().trim());
                b.setIsbn(isbnF.getText().trim());
                b.setGenre(genreF.getText().trim());
                b.setPublisher(publisherF.getText().trim());
                b.setYear(yearF.getText().isEmpty() ? 0 : Integer.parseInt(yearF.getText().trim()));
                int copies = copiesF.getText().isEmpty() ? 1 : Integer.parseInt(copiesF.getText().trim());
                b.setTotalCopies(copies);
                if (existing == null) b.setAvailableCopies(copies);
                b.setDescription(descF.getText().trim());

                if (existing == null) bookDAO.addBook(b);
                else bookDAO.updateBook(b);

                dialog.dispose();
                loadBooks(null);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Year and Copies must be numbers.", "Validation", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancel);
        btnPanel.add(save);
        panel.add(btnPanel, gbc);

        dialog.setContentPane(panel);
        dialog.getContentPane().setBackground(Theme.BG_CARD);
        dialog.setVisible(true);
    }

    // ─── Table Styling ────────────────────────────────────────────────────────
    private void styleTable(JTable table) {
        table.setBackground(Theme.BG_SECONDARY);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0xD4A843, false).darker().darker());
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.TABLE_HEADER);
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setFont(Theme.FONT_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = {40, 200, 150, 130, 100, 60, 70, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBackground(selected ? new Color(0x2A3D52) : (row % 2 == 0 ? Theme.BG_SECONDARY : Theme.TABLE_ALT));
                setForeground(selected ? Theme.TEXT_PRIMARY : (col == 7 ?
                    (value instanceof Integer && (int)value == 0 ? Theme.ACCENT_RED : Theme.ACCENT_GREEN) : Theme.TEXT_PRIMARY));
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    // ─── Scroll Bar UI ────────────────────────────────────────────────────────
    static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = Theme.BORDER;
            trackColor = Theme.BG_SECONDARY;
        }
        @Override protected JButton createDecreaseButton(int o) { return emptyButton(); }
        @Override protected JButton createIncreaseButton(int o) { return emptyButton(); }
        private JButton emptyButton() {
            JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0x3A5068));
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
            g2.dispose();
        }
    }
}
