package library.ui;

import library.util.Theme;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UIComponents {

    // ─── Rounded Panel ───────────────────────────────────────────────────────
    public static class RoundedPanel extends JPanel {
        private int radius;
        private Color bg;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg = bg;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }

    // ─── Modern Button ────────────────────────────────────────────────────────
    public static class ModernButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private Color pressColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ModernButton(String text, Color color) {
            super(text);
            this.normalColor = color;
            this.hoverColor = color.brighter();
            this.pressColor = color.darker();
            setFont(Theme.FONT_BOLD);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(120, 36));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { isHovered = false; isPressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e){ isPressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = isPressed ? pressColor : (isHovered ? hoverColor : normalColor);
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─── Icon Button (small square) ───────────────────────────────────────────
    public static class IconButton extends JButton {
        private Color normalColor;
        private boolean isHovered;

        public IconButton(String text, Color color) {
            super(text);
            this.normalColor = color;
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(70, 30));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { isHovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered ? normalColor.brighter() : normalColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─── Modern TextField ─────────────────────────────────────────────────────
    public static class ModernTextField extends JTextField {
        private String placeholder;

        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(Theme.FONT_BODY);
            setForeground(Theme.TEXT_PRIMARY);
            setBackground(Theme.INPUT_BG);
            setCaretColor(Theme.ACCENT_GOLD);
            setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
            ));
            setPreferredSize(new Dimension(200, 36));

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    setBorder(new CompoundBorder(
                        new LineBorder(Theme.BORDER_FOCUS, 1, true),
                        new EmptyBorder(6, 10, 6, 10)
                    ));
                    repaint();
                }
                @Override public void focusLost(FocusEvent e) {
                    setBorder(new CompoundBorder(
                        new LineBorder(Theme.BORDER, 1, true),
                        new EmptyBorder(6, 10, 6, 10)
                    ));
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Theme.FONT_BODY);
                Insets ins = getInsets();
                g2.drawString(placeholder, ins.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                g2.dispose();
            }
        }
    }

    // ─── Modern TextArea ──────────────────────────────────────────────────────
    public static class ModernTextArea extends JTextArea {
        public ModernTextArea(int rows, int cols) {
            super(rows, cols);
            setFont(Theme.FONT_BODY);
            setForeground(Theme.TEXT_PRIMARY);
            setBackground(Theme.INPUT_BG);
            setCaretColor(Theme.ACCENT_GOLD);
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
            ));
        }
    }

    // ─── Modern ComboBox ──────────────────────────────────────────────────────
    public static class ModernComboBox<T> extends JComboBox<T> {
        public ModernComboBox(T[] items) {
            super(items);
            setFont(Theme.FONT_BODY);
            setForeground(Theme.TEXT_PRIMARY);
            setBackground(Theme.INPUT_BG);
            setBorder(new CompoundBorder(
                new LineBorder(Theme.BORDER, 1, true),
                new EmptyBorder(2, 4, 2, 4)
            ));
            setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setBackground(isSelected ? Theme.ACCENT_GOLD : Theme.INPUT_BG);
                    setForeground(isSelected ? Theme.BG_PRIMARY : Theme.TEXT_PRIMARY);
                    setBorder(new EmptyBorder(5, 10, 5, 10));
                    return this;
                }
            });
        }
    }

    // ─── Stat Card ────────────────────────────────────────────────────────────
    public static class StatCard extends RoundedPanel {
        private JLabel numberLabel;
        private JLabel titleLabel;
        private JLabel iconLabel;
        private Color accentColor;

        public StatCard(String icon, String title, String value, Color accent) {
            super(12, Theme.BG_CARD);
            this.accentColor = accent;
            setLayout(new BorderLayout(0, 6));
            setBorder(new EmptyBorder(18, 20, 18, 20));

            JPanel top = new JPanel(new BorderLayout());
            top.setOpaque(false);
            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            iconLabel.setForeground(accent);
            top.add(iconLabel, BorderLayout.WEST);

            numberLabel = new JLabel(value);
            numberLabel.setFont(Theme.FONT_STAT_NUM);
            numberLabel.setForeground(Theme.TEXT_PRIMARY);

            titleLabel = new JLabel(title);
            titleLabel.setFont(Theme.FONT_STAT_LBL);
            titleLabel.setForeground(Theme.TEXT_SECONDARY);

            add(top, BorderLayout.NORTH);
            add(numberLabel, BorderLayout.CENTER);
            add(titleLabel, BorderLayout.SOUTH);
        }

        public void setValue(String value) {
            numberLabel.setText(value);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Left accent bar
            g2.setColor(accentColor);
            g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            g2.dispose();
        }
    }

    // ─── Section Header ───────────────────────────────────────────────────────
    public static JLabel sectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_HEADER);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(0, 0, 12, 0));
        return lbl;
    }

    // ─── Label ────────────────────────────────────────────────────────────────
    public static JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        return lbl;
    }

    // ─── Separator ────────────────────────────────────────────────────────────
    public static JSeparator separator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setBackground(Theme.BORDER);
        return sep;
    }

    // ─── Status Badge ────────────────────────────────────────────────────────
    public static JLabel badge(String text) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                if (text.equalsIgnoreCase("Active") || text.equalsIgnoreCase("Available") || text.equalsIgnoreCase("Returned")) {
                    bg = new Color(0x1E4D35);
                    setForeground(Theme.ACCENT_GREEN);
                } else if (text.equalsIgnoreCase("Overdue") || text.equalsIgnoreCase("Inactive")) {
                    bg = new Color(0x4D1E1E);
                    setForeground(Theme.ACCENT_RED);
                } else {
                    bg = new Color(0x3A3010);
                    setForeground(Theme.ACCENT_GOLD);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(3, 8, 3, 8));
        return lbl;
    }
}
