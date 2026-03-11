package library.ui;

import library.util.Theme;
import library.ui.UIComponents.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class MainWindow extends JFrame {
    private JPanel contentArea;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private BooksPanel booksPanel;
    private MembersPanel membersPanel;
    private BorrowingsPanel borrowingsPanel;

    private JButton activeNavBtn = null;

    public MainWindow() {
        setTitle("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1280, 800);
        setLocationRelativeTo(null);

        buildUI();
        setVisible(true);
        switchPanel("dashboard");
    }

    private void buildUI() {
        getContentPane().setBackground(Theme.BG_PRIMARY);
        setLayout(new BorderLayout());

        // ── Sidebar ──────────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(Theme.SIDEBAR_WIDTH, 0));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setLayout(new BorderLayout());

        // Logo area
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(Theme.SIDEBAR_BG);
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBorder(new EmptyBorder(28, 20, 24, 20));

        JLabel logoIcon = new JLabel("📚", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        logoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoText = new JLabel("LibraryPro");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoText.setForeground(Theme.ACCENT_GOLD);
        logoText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoSub = new JLabel("Management System");
        logoSub.setFont(Theme.FONT_SMALL);
        logoSub.setForeground(Theme.TEXT_MUTED);
        logoSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logoIcon);
        logoPanel.add(Box.createVerticalStrut(6));
        logoPanel.add(logoText);
        logoPanel.add(logoSub);

        // Nav items
        JPanel navPanel = new JPanel();
        navPanel.setBackground(Theme.SIDEBAR_BG);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel navLabel = new JLabel("NAVIGATION");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        navLabel.setForeground(Theme.TEXT_MUTED);
        navLabel.setBorder(new EmptyBorder(0, 8, 8, 0));
        navLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.add(navLabel);

        JButton dashBtn     = navButton("🏠  Dashboard",  "dashboard");
        JButton booksBtn    = navButton("📚  Books",       "books");
        JButton membersBtn  = navButton("👥  Members",     "members");
        JButton borrowBtn   = navButton("📖  Borrowings",  "borrowings");

        navPanel.add(dashBtn);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(booksBtn);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(membersBtn);
        navPanel.add(Box.createVerticalStrut(4));
        navPanel.add(borrowBtn);

        // Bottom nav area
        JPanel bottomNav = new JPanel();
        bottomNav.setBackground(Theme.SIDEBAR_BG);
        bottomNav.setLayout(new BoxLayout(bottomNav, BoxLayout.Y_AXIS));
        bottomNav.setBorder(new EmptyBorder(8, 12, 20, 12));

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        bottomNav.add(sep);
        bottomNav.add(Box.createVerticalStrut(12));

        JButton exitBtn = navButton("⏻  Exit", "exit");
        exitBtn.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Exit the application?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) System.exit(0);
        });
        bottomNav.add(exitBtn);

        sidebar.add(logoPanel, BorderLayout.NORTH);
        sidebar.add(navPanel, BorderLayout.CENTER);
        sidebar.add(bottomNav, BorderLayout.SOUTH);

        // ── Top Bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, Theme.TOPBAR_HEIGHT));
        topBar.setBackground(Theme.BG_SECONDARY);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        JLabel pageTitle = new JLabel("  Library Management System");
        pageTitle.setFont(Theme.FONT_HEADER);
        pageTitle.setForeground(Theme.TEXT_SECONDARY);
        topBar.add(pageTitle, BorderLayout.WEST);

        JLabel dateLabel = new JLabel(java.time.LocalDate.now().toString() + "   ");
        dateLabel.setFont(Theme.FONT_SMALL);
        dateLabel.setForeground(Theme.TEXT_MUTED);
        topBar.add(dateLabel, BorderLayout.EAST);

        // ── Content ───────────────────────────────────────────────────────────
        dashboardPanel  = new DashboardPanel();
        booksPanel      = new BooksPanel();
        membersPanel    = new MembersPanel();
        borrowingsPanel = new BorrowingsPanel();

        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(Theme.BG_PRIMARY);
        contentArea.add(dashboardPanel,  "dashboard");
        contentArea.add(booksPanel,      "books");
        contentArea.add(membersPanel,    "members");
        contentArea.add(borrowingsPanel, "borrowings");

        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.add(topBar, BorderLayout.NORTH);
        mainArea.add(contentArea, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainArea, BorderLayout.CENTER);
    }

    private JButton navButton(String text, String panel) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = this == activeNavBtn;
                if (isActive) {
                    g2.setColor(Theme.BG_CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(Theme.ACCENT_GOLD);
                    g2.fillRoundRect(0, (getHeight()-24)/2, 3, 24, 3, 3);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0x1C2B3A));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(Theme.FONT_NAV);
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(196, 42));
        btn.setBorder(new EmptyBorder(0, 12, 0, 12));

        if (!panel.equals("exit")) {
            btn.addActionListener(e -> {
                switchPanel(panel);
                setActiveNav(btn);
            });
        }

        return btn;
    }

    private void setActiveNav(JButton btn) {
        activeNavBtn = btn;
        repaint();
    }

    private void switchPanel(String name) {
        cardLayout.show(contentArea, name);
        if (name.equals("dashboard")) dashboardPanel.refreshStats();
    }
}
