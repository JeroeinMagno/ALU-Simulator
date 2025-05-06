import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.beans.PropertyChangeListener;

public class ALUInterface extends JFrame {
    private ALULogic alu;
    private JTextField input1Field, input2Field, resultField;
    private JLabel binaryInput1Label, binaryInput2Label, binaryResultLabel;
    private JComboBox<String> operationCombo;
    private JButton calculateButton;
    private Color themeColor = new Color(70, 130, 180);
    private Color lightThemeColor = new Color(135, 206, 235);
    private JPanel historyPanel;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    private JComboBox<String> baseSelector;
    private JButton copyButton;

    public ALUInterface() {
        alu = new ALULogic();
        setupGUI();
        setupModelListeners();
        setupKeyboardShortcuts();
    }

    private void setupModelListeners() {
        alu.addPropertyChangeListener((evt) -> {
            if (evt.getPropertyName().equals("binaryResult")) {
                SwingUtilities.invokeLater(() -> {
                    binaryResultLabel.setText((String)evt.getNewValue());
                    animateResultField();
                });
            } else if (evt.getPropertyName().equals("historyUpdate")) {
                SwingUtilities.invokeLater(() -> {
                    updateHistoryDisplay((String[])evt.getNewValue());
                });
            }
        });
    }

    private void updateHistoryDisplay(String[] history) {
        historyModel.clear();
        for (String entry : history) {
            if (entry != null) {
                historyModel.addElement(entry);
            }
        }
    }

    private void setupGUI() {
        setTitle("ALU Visual Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Main content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        // Input section with base selector
        setupInputSection(mainPanel);
        
        // Operation section
        setupOperationSection(mainPanel);
        
        // Result section
        setupResultSection(mainPanel);

        // History panel
        setupHistoryPanel(mainPanel);

        // Add scroll support
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Accessibility improvements
        setupAccessibility();

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 800));
    }

    private void setupInputSection(JPanel mainPanel) {
        JPanel inputPanel = createRoundedPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Number base selector
        baseSelector = new JComboBox<>(new String[]{"Decimal", "Binary", "Hexadecimal"});
        baseSelector.setToolTipText("Select input number base");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        inputPanel.add(baseSelector, gbc);
        gbc.gridwidth = 1;
        
        // Input 1
        input1Field = createModernTextField();
        input1Field.setToolTipText("Enter first number");
        setupInputField(inputPanel, gbc, "Input 1:", input1Field, 1);
        
        // Binary display 1
        binaryInput1Label = createBinaryLabel();
        setupBinaryLabel(inputPanel, gbc, binaryInput1Label, 2);
        
        // Input 2
        input2Field = createModernTextField();
        input2Field.setToolTipText("Enter second number");
        setupInputField(inputPanel, gbc, "Input 2:", input2Field, 3);
        
        // Binary display 2
        binaryInput2Label = createBinaryLabel();
        setupBinaryLabel(inputPanel, gbc, binaryInput2Label, 4);
        
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void setupOperationSection(JPanel mainPanel) {
        JPanel operationPanel = createRoundedPanel();
        operationPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        operationCombo = createModernComboBox();
        operationCombo.setModel(new DefaultComboBoxModel<>(new String[]{
            "ADD (Alt+A)", "SUBTRACT (Alt+S)", "MULTIPLY (Alt+M)", 
            "DIVIDE (Alt+D)", "MODULO (Alt+R)", "LEFT SHIFT (Alt+L)", 
            "RIGHT SHIFT (Alt+H)", "AND", "OR", "NOT"
        }));
        operationCombo.setToolTipText("Select operation to perform");
        gbc.gridx = 0; gbc.gridy = 0;
        operationPanel.add(operationCombo, gbc);

        // Calculate button with hover effect
        calculateButton = createModernButton("Calculate");
        calculateButton.addActionListener(e -> performOperation());
        calculateButton.addMouseListener(new HoverEffect(calculateButton));
        gbc.gridy = 1;
        operationPanel.add(calculateButton, gbc);

        mainPanel.add(operationPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void setupResultSection(JPanel mainPanel) {
        JPanel resultPanel = createRoundedPanel();
        resultPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        resultField = createModernTextField();
        resultField.setEditable(false);
        resultField.setToolTipText("Operation result");
        
        copyButton = createModernButton("Copy Result");
        copyButton.setToolTipText("Copy result to clipboard (Alt+C)");
        copyButton.addActionListener(e -> copyResultToClipboard());

        gbc.gridx = 0; gbc.gridy = 0;
        resultPanel.add(new JLabel("Result:"), gbc);
        gbc.gridy = 1;
        resultPanel.add(resultField, gbc);
        gbc.gridy = 2;
        resultPanel.add(copyButton, gbc);

        binaryResultLabel = createBinaryLabel();
        gbc.gridy = 3;
        resultPanel.add(new JLabel("Binary Result:"), gbc);
        gbc.gridy = 4;
        resultPanel.add(binaryResultLabel, gbc);

        mainPanel.add(resultPanel);
    }

    private void setupHistoryPanel(JPanel mainPanel) {
        historyPanel = createRoundedPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Calculation History"));

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(new Font("Monospace", Font.PLAIN, 12));
        
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setPreferredSize(new Dimension(0, 150));
        
        historyPanel.add(historyScroll, BorderLayout.CENTER);
        mainPanel.add(historyPanel);
    }

    private void setupAccessibility() {
        // Set keyboard mnemonics
        calculateButton.setMnemonic(KeyEvent.VK_C);
        
        // Add keyboard navigation
        input1Field.setNextFocusableComponent(input2Field);
        input2Field.setNextFocusableComponent(operationCombo);
        operationCombo.setNextFocusableComponent(calculateButton);
        
        // Add input validation feedback
        addInputValidationFeedback(input1Field);
        addInputValidationFeedback(input2Field);
    }

    private void setupKeyboardShortcuts() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        // Operation shortcuts
        setupShortcut(inputMap, actionMap, "ADD", KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK), 0);
        setupShortcut(inputMap, actionMap, "SUBTRACT", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK), 1);
        setupShortcut(inputMap, actionMap, "MULTIPLY", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK), 2);
        setupShortcut(inputMap, actionMap, "DIVIDE", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK), 3);
        setupShortcut(inputMap, actionMap, "MODULO", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK), 4);
        setupShortcut(inputMap, actionMap, "LEFT_SHIFT", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK), 5);
        setupShortcut(inputMap, actionMap, "RIGHT_SHIFT", KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK), 6);

        // Copy result shortcut
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK), "COPY");
        actionMap.put("COPY", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyResultToClipboard();
            }
        });
    }

    private void setupShortcut(InputMap inputMap, ActionMap actionMap, String key, KeyStroke keystroke, int operationIndex) {
        inputMap.put(keystroke, key);
        actionMap.put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                operationCombo.setSelectedIndex(operationIndex);
                performOperation();
            }
        });
    }

    private void copyResultToClipboard() {
        if (resultField.getText().isEmpty()) return;
        StringSelection selection = new StringSelection(resultField.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(this, "Result copied to clipboard!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Modern UI component creators
    private JPanel createRoundedPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JTextField createModernTextField() {
        JTextField field = new JTextField(15);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, themeColor),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return field;
    }

    private JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setBackground(themeColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JComboBox<String> createModernComboBox() {
        JComboBox<String> combo = new JComboBox<>(new String[] {
            "ADD", "SUBTRACT", "AND", "OR", "NOT"
        });
        combo.setRenderer(new ModernComboBoxRenderer());
        combo.setBackground(Color.WHITE);
        combo.setForeground(themeColor);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return combo;
    }

    private void setupInputField(JPanel panel, GridBagConstraints gbc, String label, JTextField field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void setupBinaryLabel(JPanel panel, GridBagConstraints gbc, JLabel label, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Binary:"), gbc);
        
        gbc.gridx = 1;
        panel.add(label, gbc);
    }

    private JLabel createBinaryLabel() {
        JLabel label = new JLabel("0000 0000");
        label.setFont(new Font("Consolas", Font.PLAIN, 14));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private void addInputValidationFeedback(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void validate() {
                if (field.getText().isEmpty()) {
                    field.setBackground(Color.WHITE);
                    field.setToolTipText("Enter a number");
                } else if (!alu.isValidInput(field.getText())) {
                    field.setBackground(new Color(255, 200, 200));
                    field.setToolTipText("Please enter a valid integer");
                } else {
                    field.setBackground(new Color(200, 255, 200));
                    field.setToolTipText("Valid input");
                    updateBinaryLabel(field);
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validate(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validate(); }
        });
    }

    private void updateBinaryLabel(JTextField field) {
        try {
            int value = Integer.parseInt(field.getText());
            String binary = String.format("%32s", Integer.toBinaryString(value))
                               .replace(' ', '0')
                               .replaceAll("(.{8})", "$1 ")
                               .trim();
            if (field == input1Field) {
                binaryInput1Label.setText(binary);
            } else if (field == input2Field) {
                binaryInput2Label.setText(binary);
            }
        } catch (NumberFormatException ex) {
            // Invalid number - binary label will be handled by validation
        }
    }

    private void performOperation() {
        try {
            String input1 = input1Field.getText();
            String input2 = input2Field.getText();
            String base = (String) baseSelector.getSelectedItem();
            
            if (!alu.isValidInput(input1)) {
                showError("Please enter a valid number for Input 1");
                input1Field.requestFocus();
                return;
            }

            int num1 = parseInputNumber(input1);
            String op = (String) operationCombo.getSelectedItem();
            int result;

            if (op.contains("NOT")) {
                result = alu.not(num1);
                alu.addToHistory("NOT", num1, 0, result, base);
            } else {
                if (!alu.isValidInput(input2)) {
                    showError("Please enter a valid number for Input 2");
                    input2Field.requestFocus();
                    return;
                }
                int num2 = parseInputNumber(input2);

                if (op.contains("ADD")) {
                    result = alu.add(num1, num2);
                } else if (op.contains("SUBTRACT")) {
                    result = alu.subtract(num1, num2);
                } else if (op.contains("MULTIPLY")) {
                    result = alu.multiply(num1, num2);
                } else if (op.contains("DIVIDE")) {
                    result = alu.divide(num1, num2);
                } else if (op.contains("MODULO")) {
                    result = alu.modulo(num1, num2);
                } else if (op.contains("LEFT SHIFT")) {
                    result = alu.leftShift(num1, num2);
                } else if (op.contains("RIGHT SHIFT")) {
                    result = alu.rightShift(num1, num2);
                } else if (op.contains("AND")) {
                    result = alu.and(num1, num2);
                } else if (op.contains("OR")) {
                    result = alu.or(num1, num2);
                } else {
                    showError("Invalid operation");
                    return;
                }
                String operation = op.split(" ")[0];
                alu.addToHistory(operation, num1, num2, result, base);
            }
            
            displayResult(result);
            
        } catch (ArithmeticException e) {
            showError("Arithmetic error: " + e.getMessage());
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void updateHistory(String operation, int a, int b, int result) {
        String entry = String.format("%d %s %d = %d", a, operation, b, result);
        historyModel.insertElementAt(entry, 0);
        if (historyModel.size() > 10) {
            historyModel.removeElementAt(10);
        }
    }

    private int parseInputNumber(String input) {
        String base = (String) baseSelector.getSelectedItem();
        if (base.equals("Binary")) {
            return Integer.parseInt(input, 2);
        } else if (base.equals("Hexadecimal")) {
            return Integer.parseInt(input.replaceFirst("0x", ""), 16);
        }
        return Integer.parseInt(input);
    }

    private void displayResult(int result) {
        String base = (String) baseSelector.getSelectedItem();
        if (base.equals("Binary")) {
            resultField.setText(Integer.toBinaryString(result));
        } else if (base.equals("Hexadecimal")) {
            resultField.setText(String.format("0x%X", result));
        } else {
            resultField.setText(String.valueOf(result));
        }
        binaryResultLabel.setText(String.format("%32s", Integer.toBinaryString(result))
            .replace(' ', '0')
            .replaceAll("(.{8})", "$1 ")
            .trim());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", 
            JOptionPane.ERROR_MESSAGE);
    }

    private void animateResultField() {
        Color original = resultField.getBackground();
        Color highlight = lightThemeColor;
        resultField.setBackground(highlight);
        Timer timer = new Timer(400, e -> resultField.setBackground(original));
        timer.setRepeats(false);
        timer.start();
    }

    // Custom components
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2.dispose();
        }
    }

    private static class ModernComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            return label;
        }
    }

    private static class HoverEffect extends MouseAdapter {
        private final JButton button;
        private final Color originalColor;

        HoverEffect(JButton button) {
            this.button = button;
            this.originalColor = button.getBackground();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            button.setBackground(button.getBackground().brighter());
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(originalColor);
        }
    }
}