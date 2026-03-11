package library;

import library.util.DatabaseManager;
import library.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Initialize database
        DatabaseManager.initializeDatabase();

        // Launch UI on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // Try to use FlatLaf or system look
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}

                // Override key defaults for dark theme
                UIManager.put("OptionPane.background",         new java.awt.Color(0x1C2B3A));
                UIManager.put("Panel.background",              new java.awt.Color(0x1C2B3A));
                UIManager.put("OptionPane.messageForeground",  new java.awt.Color(0xEEF2F7));
                UIManager.put("Button.background",             new java.awt.Color(0x2A3F54));
                UIManager.put("Button.foreground",             new java.awt.Color(0xEEF2F7));
                UIManager.put("ComboBox.background",           new java.awt.Color(0x111D27));
                UIManager.put("ComboBox.foreground",           new java.awt.Color(0xEEF2F7));
                UIManager.put("ComboBox.selectionBackground",  new java.awt.Color(0xD4A843));
                UIManager.put("TextField.background",          new java.awt.Color(0x111D27));
                UIManager.put("TextField.foreground",          new java.awt.Color(0xEEF2F7));
                UIManager.put("TextArea.background",           new java.awt.Color(0x111D27));
                UIManager.put("TextArea.foreground",           new java.awt.Color(0xEEF2F7));
                UIManager.put("ScrollPane.background",         new java.awt.Color(0x162030));
                UIManager.put("Viewport.background",           new java.awt.Color(0x162030));
                UIManager.put("Table.background",              new java.awt.Color(0x162030));
                UIManager.put("Table.foreground",              new java.awt.Color(0xEEF2F7));
                UIManager.put("TableHeader.background",        new java.awt.Color(0x0D1720));
                UIManager.put("TableHeader.foreground",        new java.awt.Color(0x8A9BB0));
                UIManager.put("ScrollBar.background",          new java.awt.Color(0x162030));
                UIManager.put("ScrollBar.thumb",               new java.awt.Color(0x2A3F54));
                UIManager.put("Dialog.background",             new java.awt.Color(0x1C2B3A));

                new MainWindow();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to launch application:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::closeConnection));
    }
}
