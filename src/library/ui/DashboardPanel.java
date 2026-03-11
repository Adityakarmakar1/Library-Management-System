package library.ui;

import library.dao.*;
import library.util.Theme;
import library.ui.UIComponents.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;

public class DashboardPanel extends JPanel {
    private BookDAO bookDAO = new BookDAO();
    private MemberDAO memberDAO = new MemberDAO();
    private BorrowingDAO borrowingDAO = new BorrowingDAO();

    private StatCard totalBooksCard, availableCard, membersCard, borrowedCard, overdueCard;

    public DashboardPanel() {
        setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout(0, 0));
        setBorder(new EmptyBorder(Theme.PADDING, Theme.PADDING, Theme.PADDING, Theme.PADDING));
        buildUI();
        refreshStats();
    }

    private void buildUI() {
        // Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel title = new JLabel("Dashboard");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Welcome back · Library overview");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        JPanel titleText = new JPanel(new GridLayout(2, 1, 0, 2));
        titleText.setOpaque(false);
        titleText.add(title);
        titleText.add(subtitle);
        titlePanel.add(titleText, BorderLayout.WEST);

        ModernButton refreshBtn = new ModernButton("↻  Refresh", Theme.ACCENT_TEAL);
        refreshBtn.setPreferredSize(new Dimension(110, 34));
        refreshBtn.addActionListener(e -> refreshStats());
        titlePanel.add(refreshBtn, BorderLayout.EAST);
        add(titlePanel, BorderLayout.NORTH);

        // Stats Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 5, 12, 12));
        statsGrid.setOpaque(false);
        statsGrid.setBorder(new EmptyBorder(0, 0, 20, 0));

        totalBooksCard  = new StatCard("📚", "Total Books",     "0", Theme.ACCENT_GOLD);
        availableCard   = new StatCard("✅", "Available",        "0", Theme.ACCENT_GREEN);
        membersCard     = new StatCard("👥", "Active Members",   "0", Theme.ACCENT_TEAL);
        borrowedCard    = new StatCard("📖", "Currently Borrowed","0", new Color(0x7B68EE));
        overdueCard     = new StatCard("⚠", "Overdue",          "0", Theme.ACCENT_RED);

        statsGrid.add(totalBooksCard);
        statsGrid.add(availableCard);
        statsGrid.add(membersCard);
        statsGrid.add(borrowedCard);
        statsGrid.add(overdueCard);
        add(statsGrid, BorderLayout.CENTER);

        // Bottom info panel
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomPanel.setOpaque(false);

        // Quick info card
        RoundedPanel infoCard = new RoundedPanel(12, Theme.BG_CARD);
        infoCard.setLayout(new BorderLayout());
        infoCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel infoTitle = new JLabel("📋  Quick Tips");
        infoTitle.setFont(Theme.FONT_HEADER);
        infoTitle.setForeground(Theme.ACCENT_GOLD);
        infoTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JTextArea tips = new JTextArea(
            "• Use the Books panel to add, search and manage your catalog.\n" +
            "• Register members in the Members panel.\n" +
            "• Issue and return books in the Borrowings panel.\n" +
            "• Fine is ₹2 per day overdue (configurable in BorrowingDAO).\n" +
            "• Data is stored locally in library.db (SQLite)."
        );
        tips.setEditable(false);
        tips.setFont(Theme.FONT_BODY);
        tips.setForeground(Theme.TEXT_SECONDARY);
        tips.setBackground(Theme.BG_CARD);
        tips.setOpaque(true);
        tips.setBorder(null);
        tips.setLineWrap(true);
        tips.setWrapStyleWord(true);

        infoCard.add(infoTitle, BorderLayout.NORTH);
        infoCard.add(tips, BorderLayout.CENTER);

        // System info card
        RoundedPanel sysCard = new RoundedPanel(12, Theme.BG_CARD);
        sysCard.setLayout(new BorderLayout());
        sysCard.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel sysTitle = new JLabel("⚙  System Info");
        sysTitle.setFont(Theme.FONT_HEADER);
        sysTitle.setForeground(Theme.ACCENT_TEAL);
        sysTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        JTextArea sysInfo = new JTextArea(
            "Application: Library Management System\n" +
            "Version: 1.0.0\n" +
            "Database: SQLite (local)\n" +
            "Java: " + System.getProperty("java.version") + "\n" +
            "OS: " + System.getProperty("os.name") + "\n" +
            "Fine Rate: ₹2.00 / day"
        );
        sysInfo.setEditable(false);
        sysInfo.setFont(Theme.FONT_BODY);
        sysInfo.setForeground(Theme.TEXT_SECONDARY);
        sysInfo.setBackground(Theme.BG_CARD);
        sysInfo.setOpaque(true);
        sysInfo.setBorder(null);
        sysInfo.setLineWrap(true);
        sysInfo.setWrapStyleWord(true);

        sysCard.add(sysTitle, BorderLayout.NORTH);
        sysCard.add(sysInfo, BorderLayout.CENTER);

        bottomPanel.add(infoCard);
        bottomPanel.add(sysCard);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshStats() {
        SwingWorker<int[], Void> worker = new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() throws Exception {
                return new int[]{
                    bookDAO.getTotalBooks(),
                    bookDAO.getAvailableBooks(),
                    memberDAO.getTotalMembers(),
                    borrowingDAO.getActiveBorrowingsCount(),
                    borrowingDAO.getOverdueCount()
                };
            }

            @Override
            protected void done() {
                try {
                    int[] stats = get();
                    totalBooksCard.setValue(String.valueOf(stats[0]));
                    availableCard.setValue(String.valueOf(stats[1]));
                    membersCard.setValue(String.valueOf(stats[2]));
                    borrowedCard.setValue(String.valueOf(stats[3]));
                    overdueCard.setValue(String.valueOf(stats[4]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}
