package library.ui;

import library.dao.MemberDAO;
import library.model.Member;
import library.util.Theme;
import library.ui.UIComponents.*;
import library.ui.BooksPanel.ModernScrollBarUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MembersPanel extends JPanel {
    private MemberDAO memberDAO = new MemberDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private ModernTextField searchField;

    private static final String[] COLUMNS = {"ID", "Name", "Email", "Phone", "Type", "Joined", "Expiry", "Status"};

    public MembersPanel() {
        setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(Theme.PADDING, Theme.PADDING, Theme.PADDING, Theme.PADDING));
        buildUI();
        loadMembers(null);
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("👥  Members");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        searchField = new ModernTextField("🔍  Search members...");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadMembers(searchField.getText().trim()); }
        });

        ModernButton addBtn = new ModernButton("+ Add Member", Theme.ACCENT_TEAL);
        addBtn.addActionListener(e -> showMemberDialog(null));
        actions.add(searchField);
        actions.add(addBtn);
        header.add(actions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) showMemberDialog(getMemberFromRow(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scroll.getViewport().setBackground(Theme.BG_SECONDARY);
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        add(scroll, BorderLayout.CENTER);

        // Bottom
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(10, 0, 0, 0));

        ModernButton editBtn   = new ModernButton("✏  Edit", Theme.ACCENT_TEAL);
        ModernButton deleteBtn = new ModernButton("🗑  Delete", Theme.ACCENT_RED);

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showWarning("Select a member first."); return; }
            showMemberDialog(getMemberFromRow(row));
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showWarning("Select a member first."); return; }
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String name = tableModel.getValueAt(row, 1).toString();
            if (JOptionPane.showConfirmDialog(this, "Delete member \"" + name + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try { memberDAO.deleteMember(id); loadMembers(null); }
                catch (Exception ex) { showError(ex.getMessage()); }
            }
        });

        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void loadMembers(String query) {
        SwingWorker<List<Member>, Void> worker = new SwingWorker<List<Member>, Void>() {
            @Override protected List<Member> doInBackground() throws Exception {
                return query == null || query.isEmpty() ? memberDAO.getAllMembers() : memberDAO.searchMembers(query);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Member m : get()) {
                        tableModel.addRow(new Object[]{
                            m.getId(), m.getName(), m.getEmail(), m.getPhone(),
                            m.getMembershipType(), m.getMembershipDate(), m.getExpiryDate(), m.getStatus()
                        });
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        worker.execute();
    }

    private Member getMemberFromRow(int row) {
        Member m = new Member();
        m.setId(Integer.parseInt(tableModel.getValueAt(row, 0).toString()));
        m.setName(tableModel.getValueAt(row, 1).toString());
        m.setEmail(tableModel.getValueAt(row, 2) != null ? tableModel.getValueAt(row, 2).toString() : "");
        m.setPhone(tableModel.getValueAt(row, 3) != null ? tableModel.getValueAt(row, 3).toString() : "");
        m.setMembershipType(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "Standard");
        m.setMembershipDate(tableModel.getValueAt(row, 5) != null ? tableModel.getValueAt(row, 5).toString() : "");
        m.setExpiryDate(tableModel.getValueAt(row, 6) != null ? tableModel.getValueAt(row, 6).toString() : "");
        m.setStatus(tableModel.getValueAt(row, 7) != null ? tableModel.getValueAt(row, 7).toString() : "Active");
        return m;
    }

    private void showMemberDialog(Member existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "Add New Member" : "Edit Member", true);
        dialog.setSize(480, 460);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_CARD);
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        ModernTextField nameF    = new ModernTextField("Full name");
        ModernTextField emailF   = new ModernTextField("Email address");
        ModernTextField phoneF   = new ModernTextField("Phone number");
        ModernTextField addressF = new ModernTextField("Address");
        ModernComboBox<String> typeBox = new ModernComboBox<>(new String[]{"Standard", "Premium", "Student"});
        ModernTextField expiryF  = new ModernTextField("YYYY-MM-DD");
        ModernComboBox<String> statusBox = new ModernComboBox<>(new String[]{"Active", "Inactive", "Suspended"});

        if (existing != null) {
            nameF.setText(existing.getName());
            emailF.setText(existing.getEmail() != null ? existing.getEmail() : "");
            phoneF.setText(existing.getPhone() != null ? existing.getPhone() : "");
            addressF.setText(existing.getAddress() != null ? existing.getAddress() : "");
            typeBox.setSelectedItem(existing.getMembershipType());
            expiryF.setText(existing.getExpiryDate() != null ? existing.getExpiryDate() : "");
            statusBox.setSelectedItem(existing.getStatus());
        }

        String[] labels = {"Name *", "Email", "Phone", "Address", "Membership Type", "Expiry Date", "Status"};
        JComponent[] comps = {nameF, emailF, phoneF, addressF, typeBox, expiryF, statusBox};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            panel.add(UIComponents.label(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            comps[i].setPreferredSize(new Dimension(260, 36));
            panel.add(comps[i], gbc);
        }

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 5, 0, 5);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        ModernButton cancel = new ModernButton("Cancel", Theme.TEXT_MUTED);
        ModernButton save   = new ModernButton(existing == null ? "Add Member" : "Save Changes", Theme.ACCENT_TEAL);

        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty()) { showWarning("Name is required."); return; }
            try {
                Member m = existing != null ? existing : new Member();
                m.setName(nameF.getText().trim());
                m.setEmail(emailF.getText().trim());
                m.setPhone(phoneF.getText().trim());
                m.setAddress(addressF.getText().trim());
                m.setMembershipType((String) typeBox.getSelectedItem());
                m.setExpiryDate(expiryF.getText().trim());
                m.setStatus((String) statusBox.getSelectedItem());

                if (existing == null) memberDAO.addMember(m);
                else memberDAO.updateMember(m);

                dialog.dispose();
                loadMembers(null);
            } catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnPanel.add(cancel); btnPanel.add(save);
        panel.add(btnPanel, gbc);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void styleTable() {
        table.setBackground(Theme.BG_SECONDARY);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(0x1A3A4A));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.TABLE_HEADER);
        header.setForeground(Theme.TEXT_SECONDARY);
        header.setFont(Theme.FONT_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        int[] widths = {40, 160, 170, 110, 90, 100, 100, 80};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setBackground(selected ? new Color(0x1A3A4A) : (row % 2 == 0 ? Theme.BG_SECONDARY : Theme.TABLE_ALT));
                if (col == 7) {
                    String v = value != null ? value.toString() : "";
                    setForeground(v.equals("Active") ? Theme.ACCENT_GREEN : v.equals("Inactive") ? Theme.ACCENT_RED : Theme.ACCENT_GOLD);
                } else {
                    setForeground(selected ? Theme.TEXT_PRIMARY : Theme.TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    private void showWarning(String msg) { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE); }
    private void showError(String msg)   { JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE); }
}
