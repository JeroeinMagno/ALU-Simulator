package src;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernUIUtils {

    // Styling Constants
    public static final Color APP_THEME_COLOR = new Color(70, 130, 180); // Steel Blue
    public static final Color APP_LIGHT_THEME_COLOR = new Color(135, 206, 235); // Sky Blue
    public static final Color FOCUS_HIGHLIGHT_COLOR = APP_THEME_COLOR.darker(); // Color for focus indication
    public static final Color BUTTON_HOVER_COLOR = APP_THEME_COLOR.brighter();
    public static final Color DEFAULT_PANEL_BACKGROUND = Color.WHITE;

    public static final int GENERAL_BORDER_RADIUS = 15; // For panels, buttons
    public static final int TEXT_FIELD_BORDER_RADIUS = 8; // For text fields

    public static final Font SEGOE_UI_PLAIN_14 = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SEGOE_UI_BOLD_14 = new Font("Segoe UI", Font.BOLD, 14);

    public static final int TEXT_FIELD_HORIZONTAL_PADDING = 12;
    public static final int TEXT_FIELD_VERTICAL_PADDING = 8;

    public static final int COMBO_BOX_RENDERER_PADDING_VERTICAL = 5;
    public static final int COMBO_BOX_RENDERER_PADDING_HORIZONTAL = 10;


    public static JPanel createRoundedPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // Important for proper painting of children if any
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, GENERAL_BORDER_RADIUS, GENERAL_BORDER_RADIUS);
                g2.dispose();
            }
        };
        panel.setOpaque(false); // True if super.paintComponent is not called and you fill background
        panel.setBackground(DEFAULT_PANEL_BACKGROUND); // Set a default background
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Default padding
        return panel;
    }

    public static JTextField createModernTextField() {
        JTextField field = new JTextField(15);
        Border roundedPart = new RoundedBorder(TEXT_FIELD_BORDER_RADIUS, APP_THEME_COLOR, FOCUS_HIGHLIGHT_COLOR);
        Border paddingPart = BorderFactory.createEmptyBorder(
                TEXT_FIELD_VERTICAL_PADDING, TEXT_FIELD_HORIZONTAL_PADDING,
                TEXT_FIELD_VERTICAL_PADDING, TEXT_FIELD_HORIZONTAL_PADDING
        );
        field.setBorder(BorderFactory.createCompoundBorder(roundedPart, paddingPart));
        field.setFont(SEGOE_UI_PLAIN_14);
        field.setOpaque(false); // To see the rounded background from potential parent panel if any, or set background
        // field.setBackground(Color.WHITE); // If you want it to have its own solid background

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.repaint(); // Repaint to update border based on focus
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.repaint(); // Repaint to update border based on focus
            }
        });
        return field;
    }

    public static JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, GENERAL_BORDER_RADIUS, GENERAL_BORDER_RADIUS);

                // Focus Indication
                if (this.isFocusOwner()) {
                    g2.setColor(FOCUS_HIGHLIGHT_COLOR);
                    g2.setStroke(new BasicStroke(2f)); // Make stroke slightly thicker for focus
                    // Draw inset border for focus
                    g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, GENERAL_BORDER_RADIUS - 2, GENERAL_BORDER_RADIUS - 2);
                }
                
                // Text
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setOpaque(false); // We are custom painting the background
        button.setContentAreaFilled(false); // No default fill
        button.setBorderPainted(false); // No default border
        button.setBackground(APP_THEME_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(SEGOE_UI_BOLD_14);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new HoverEffect(button, APP_THEME_COLOR, BUTTON_HOVER_COLOR));
        
        // Ensure focus is paintable
        button.setFocusPainted(false); // We handle focus painting ourselves

        return button;
    }

    public static <E> JComboBox<E> createModernComboBox(E[] items) {
        JComboBox<E> combo = new JComboBox<>(items);
        combo.setRenderer(new ModernComboBoxRenderer());
        combo.setBackground(DEFAULT_PANEL_BACKGROUND);
        combo.setForeground(APP_THEME_COLOR);
        combo.setFont(SEGOE_UI_PLAIN_14);
        // Basic focus indication is handled by L&F, further customization is complex
        return combo;
    }

    // --- Inner helper classes for UI elements ---
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color defaultBorderColor;
        private final Color focusBorderColor;

        public RoundedBorder(int radius, Color defaultBorderColor, Color focusBorderColor) {
            this.radius = radius;
            this.defaultBorderColor = defaultBorderColor;
            this.focusBorderColor = focusBorderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Stroke currentStroke;
            if (c.isFocusOwner()) {
                g2.setColor(focusBorderColor);
                currentStroke = new BasicStroke(1.5f); // Slightly thicker or different stroke for focus
            } else {
                g2.setColor(defaultBorderColor);
                currentStroke = new BasicStroke(1f);
            }
            g2.setStroke(currentStroke);
            
            // Ensure we are working with BasicStroke to get its width
            float strokeWidth = 1f; // Default if not BasicStroke for some reason, though unlikely here
            if (currentStroke instanceof BasicStroke) {
                strokeWidth = ((BasicStroke) currentStroke).getLineWidth();
            }

            // Adjust x,y,width,height for stroke not to be clipped
            int offset = (int) (strokeWidth / 2);
            g2.drawRoundRect(x + offset, y + offset, 
                             width - (offset * 2) -1, height - (offset * 2) -1, // -1 for outer boundary of roundrect
                             radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // Basic insets, can be adjusted based on stroke width and desired padding
            int ins = radius / 3; 
            return new Insets(ins, ins, ins, ins);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int ins = radius / 3;
            insets.left = insets.top = insets.right = insets.bottom = ins;
            return insets;
        }
    }

    public static class ModernComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(
                    COMBO_BOX_RENDERER_PADDING_VERTICAL,
                    COMBO_BOX_RENDERER_PADDING_HORIZONTAL,
                    COMBO_BOX_RENDERER_PADDING_VERTICAL,
                    COMBO_BOX_RENDERER_PADDING_HORIZONTAL
            ));
            if (isSelected) {
                label.setBackground(APP_THEME_COLOR);
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(DEFAULT_PANEL_BACKGROUND); // Use consistent background
                label.setForeground(Color.DARK_GRAY); // Or APP_THEME_COLOR
            }
            
            // Optional: Visual cue for focused item in the dropdown list (not the combo box itself)
            if (cellHasFocus && !isSelected) { // if it has focus but is not selected yet
                 label.setBackground(APP_LIGHT_THEME_COLOR);
                 label.setForeground(Color.BLACK);
            }
            
            return label;
        }
    }

    public static class HoverEffect extends MouseAdapter {
        private final JButton button;
        private final Color originalColor;
        private final Color hoverColor;

        public HoverEffect(JButton button, Color originalColor, Color hoverColor) {
            this.button = button;
            this.originalColor = originalColor;
            this.hoverColor = hoverColor;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (button.isEnabled()) {
                button.setBackground(hoverColor);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(originalColor);
        }
    }
} 