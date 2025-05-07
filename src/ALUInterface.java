import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import src.ModernUIUtils;

public class ALUInterface extends JFrame {
    private ALULogic alu;
    private JTextField input1Field, input2Field, resultField;
    private JLabel binaryInput1Label, binaryInput2Label, binaryResultLabel;
    private JComboBox<OperationItem> operationCombo;
    private JButton calculateButton;
    private JPanel historyPanel;
    private JList<String> historyList;
    private DefaultListModel<String> historyModel;
    private JComboBox<String> baseSelector;
    private JButton copyButton;
    private Map<String, BiFunction<Integer, Integer, Integer>> binaryOperations;
    private Map<String, Function<Integer, Integer>> unaryOperations;
    private Timer animationTimer;
    private static final Color VALID_INPUT_BACKGROUND = new Color(200, 255, 200);
    private static final Color INVALID_INPUT_BACKGROUND = new Color(255, 200, 200);
    private static final int DEFAULT_PADDING = 8;
    private static final int COMPONENT_SPACING = 15;
    private static final Font HISTORY_FONT = new Font("Monospace", Font.PLAIN, 12);
    private static final Font BINARY_LABEL_FONT = new Font("Consolas", Font.PLAIN, 14);
    private static final String DEFAULT_BINARY_STRING = "0000 0000";
    private static final int RESULT_ANIMATION_DURATION = 400;

    // Static inner class for JComboBox items
    private static class OperationItem {
        private final String displayString;
        private final String operationKey;

        public OperationItem(String displayString, String operationKey) {
            this.displayString = displayString;
            this.operationKey = operationKey;
        }

        public String getKey() {
            return operationKey;
        }

        @Override
        public String toString() {
            return displayString; // This is what JComboBox will display
        }
    }

    public ALUInterface() {
        alu = new ALULogic();
        setupOperations();
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
        setLayout(new BorderLayout(COMPONENT_SPACING, COMPONENT_SPACING));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Main content
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(ModernUIUtils.DEFAULT_PANEL_BACKGROUND);

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
        JPanel inputPanel = ModernUIUtils.createRoundedPanel();
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Number base selector
        baseSelector = new JComboBox<>(new String[]{"Decimal", "Binary", "Hexadecimal"});
        baseSelector.setToolTipText("Select input number base");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        inputPanel.add(baseSelector, gbc);
        gbc.gridwidth = 1;
        
        // Input 1
        input1Field = ModernUIUtils.createModernTextField();
        input1Field.setToolTipText("Enter first number");
        setupInputField(inputPanel, gbc, "Input 1:", input1Field, 1);
        
        // Binary display 1
        binaryInput1Label = createBinaryLabel();
        setupBinaryLabel(inputPanel, gbc, binaryInput1Label, 2);
        
        // Input 2
        input2Field = ModernUIUtils.createModernTextField();
        input2Field.setToolTipText("Enter second number");
        setupInputField(inputPanel, gbc, "Input 2:", input2Field, 3);
        
        // Binary display 2
        binaryInput2Label = createBinaryLabel();
        setupBinaryLabel(inputPanel, gbc, binaryInput2Label, 4);
        
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, COMPONENT_SPACING)));
    }

    private void setupOperationSection(JPanel mainPanel) {
        JPanel operationPanel = ModernUIUtils.createRoundedPanel();
        operationPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Define OperationItem array for the JComboBox
        OperationItem[] operationItems = new OperationItem[]{
            new OperationItem("ADD (Alt+A)", "ADD"),
            new OperationItem("SUBTRACT (Alt+S)", "SUBTRACT"),
            new OperationItem("MULTIPLY (Alt+M)", "MULTIPLY"),
            new OperationItem("DIVIDE (Alt+D)", "DIVIDE"),
            new OperationItem("MODULO (Alt+R)", "MODULO"),
            new OperationItem("LEFT SHIFT (Alt+L)", "LEFT"),     // Key "LEFT" for "LEFT SHIFT"
            new OperationItem("RIGHT SHIFT (Alt+H)", "RIGHT"),   // Key "RIGHT" for "RIGHT SHIFT"
            new OperationItem("AND", "AND"),
            new OperationItem("OR", "OR"),
            new OperationItem("NOT", "NOT")
        };
        operationCombo = ModernUIUtils.createModernComboBox(operationItems);
        operationCombo.setToolTipText("Select operation to perform");
        gbc.gridx = 0; gbc.gridy = 0;
        operationPanel.add(operationCombo, gbc);

        // Calculate button with hover effect
        calculateButton = ModernUIUtils.createModernButton("Calculate");
        calculateButton.addActionListener(e -> performOperation());
        gbc.gridy = 1;
        operationPanel.add(calculateButton, gbc);

        mainPanel.add(operationPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, COMPONENT_SPACING)));
    }

    private void setupResultSection(JPanel mainPanel) {
        JPanel resultPanel = ModernUIUtils.createRoundedPanel();
        resultPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING, DEFAULT_PADDING);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        resultField = ModernUIUtils.createModernTextField();
        resultField.setEditable(false);
        resultField.setToolTipText("Operation result");
        
        copyButton = ModernUIUtils.createModernButton("Copy Result");
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
        historyPanel = ModernUIUtils.createRoundedPanel();
        historyPanel.setLayout(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Calculation History"));

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setFont(HISTORY_FONT);
        
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

        // Operation shortcuts - pass operation key instead of index
        // The key passed here (e.g., "ADD", "SUBTRACT") must match the keys in OperationItem instances
        setupShortcut(inputMap, actionMap, "ADD", KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK));
        setupShortcut(inputMap, actionMap, "SUBTRACT", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
        setupShortcut(inputMap, actionMap, "MULTIPLY", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK));
        setupShortcut(inputMap, actionMap, "DIVIDE", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK));
        setupShortcut(inputMap, actionMap, "MODULO", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
        // For LEFT and RIGHT, ensure the key matches what's in OperationItem (e.g., "LEFT", "RIGHT")
        setupShortcut(inputMap, actionMap, "LEFT", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK));
        setupShortcut(inputMap, actionMap, "RIGHT", KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK));

        // Copy result shortcut
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK), "COPY");
        actionMap.put("COPY", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyResultToClipboard();
            }
        });
    }

    private void setupShortcut(InputMap inputMap, ActionMap actionMap, String operationKey, KeyStroke keystroke) {
        inputMap.put(keystroke, operationKey); // Use operationKey for the action map key as well
        actionMap.put(operationKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ComboBoxModel<OperationItem> model = operationCombo.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    OperationItem item = model.getElementAt(i);
                    if (item.getKey().equals(operationKey)) {
                        operationCombo.setSelectedIndex(i);
                        break;
                    }
                }
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
        JLabel label = new JLabel(DEFAULT_BINARY_STRING);
        label.setFont(BINARY_LABEL_FONT);
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private void addInputValidationFeedback(JTextField field) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void validate() {
                if (field.getText().isEmpty()) {
                    field.setBackground(Color.WHITE);
                    field.setToolTipText("Enter a number");
                } else if (!alu.isValidInput(field.getText(), (String) baseSelector.getSelectedItem())) {
                    field.setBackground(INVALID_INPUT_BACKGROUND);
                    field.setToolTipText("Please enter a valid integer for the selected base");
                } else {
                    field.setBackground(VALID_INPUT_BACKGROUND);
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
        String inputText = field.getText();
        if (inputText.isEmpty()) { // Handle empty case directly if necessary
            setBinaryLabelText(field, DEFAULT_BINARY_STRING);
            return;
        }
        try {
            // Use parseInputNumber to respect the selected base
            int value = parseInputNumber(inputText); // parseInputNumber already uses baseSelector
            String binary = String.format("%32s", Integer.toBinaryString(value))
                               .replace(' ', '0')
                               .replaceAll("(.{8})", "$1 ")
                               .trim();
            setBinaryLabelText(field, binary);
        } catch (NumberFormatException ex) {
            // If parseInputNumber fails, the input is invalid for the current base
            setBinaryLabelText(field, "Invalid Input");
        }
    }

    // Helper to avoid duplicating if/else for input1Field/input2Field
    private void setBinaryLabelText(JTextField field, String text) {
        if (field == input1Field) {
            binaryInput1Label.setText(text);
        } else if (field == input2Field) {
            binaryInput2Label.setText(text);
        }
    }

    private void setupOperations() {
        binaryOperations = new HashMap<>();
        binaryOperations.put("ADD", (n1, n2) -> alu.add(n1, n2));
        binaryOperations.put("SUBTRACT", (n1, n2) -> alu.subtract(n1, n2));
        binaryOperations.put("MULTIPLY", (n1, n2) -> alu.multiply(n1, n2));
        binaryOperations.put("DIVIDE", (n1, n2) -> alu.divide(n1, n2));
        binaryOperations.put("MODULO", (n1, n2) -> alu.modulo(n1, n2));
        binaryOperations.put("LEFT", (n1, n2) -> alu.leftShift(n1, n2));
        binaryOperations.put("RIGHT", (n1, n2) -> alu.rightShift(n1, n2));
        binaryOperations.put("AND", (n1, n2) -> alu.and(n1, n2));
        binaryOperations.put("OR", (n1, n2) -> alu.or(n1, n2));

        unaryOperations = new HashMap<>();
        unaryOperations.put("NOT", (n1) -> alu.not(n1));
    }

    private void performOperation() {
        try {
            String input1Text = input1Field.getText();
            String input2Text = input2Field.getText();
            String base = (String) baseSelector.getSelectedItem();
            
            if (!alu.isValidInput(input1Text, base)) {
                showError("Please enter a valid number for Input 1 for the selected base (" + base + ")");
                input1Field.requestFocus();
                return;
            }

            int num1 = parseInputNumber(input1Text);
            // Get OperationItem and then its key
            OperationItem selectedItem = (OperationItem) operationCombo.getSelectedItem();
            if (selectedItem == null) {
                showError("No operation selected.");
                return;
            }
            String operationKey = selectedItem.getKey();

            int operationOutcome;

            if (unaryOperations.containsKey(operationKey)) {
                Function<Integer, Integer> operation = unaryOperations.get(operationKey);
                operationOutcome = operation.apply(num1);
                alu.addToHistory(operationKey, num1, 0, operationOutcome, base);
            } else if (binaryOperations.containsKey(operationKey)) {
                if (!alu.isValidInput(input2Text, base)) {
                    showError("Please enter a valid number for Input 2 for the selected base (" + base + ")");
                    input2Field.requestFocus();
                    return;
                }
                int num2 = parseInputNumber(input2Text);
                BiFunction<Integer, Integer, Integer> operation = binaryOperations.get(operationKey);
                operationOutcome = operation.apply(num1, num2);
                alu.addToHistory(operationKey, num1, num2, operationOutcome, base);
            } else {
                showError("Invalid operation selected: " + selectedItem.displayString + " (Key: " + operationKey + ")");
                return;
            }
            
            displayResult(operationOutcome);
            
        } catch (ArithmeticException e) {
            showError("Arithmetic error: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Invalid number format for operation: " + e.getMessage());
        } catch (Exception e) {
            showError("An unexpected error occurred: " + e.getMessage());
        }
    }

    private int parseInputNumber(String input) {
        String base = (String) baseSelector.getSelectedItem();
        if (base.equals("Binary")) {
            return Integer.parseInt(input, 2);
        } else if (base.equals("Hexadecimal")) {
            return Integer.parseInt(input, 16);
        }
        return Integer.parseInt(input);
    }

    private void displayResult(int calcResult) {
        String base = (String) baseSelector.getSelectedItem();
        if (base.equals("Binary")) {
            resultField.setText(Integer.toBinaryString(calcResult));
        } else if (base.equals("Hexadecimal")) {
            resultField.setText(String.format("0x%X", calcResult));
        } else {
            resultField.setText(String.valueOf(calcResult));
        }
        binaryResultLabel.setText(String.format("%32s", Integer.toBinaryString(calcResult))
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
        Color highlight = ModernUIUtils.APP_LIGHT_THEME_COLOR;
        
        // Stop any existing animation before starting a new one
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
            resultField.setBackground(original); // Reset to original if stopped mid-animation
        }
        
        resultField.setBackground(highlight);
        animationTimer = new Timer(RESULT_ANIMATION_DURATION, e -> resultField.setBackground(original));
        animationTimer.setRepeats(false);
        animationTimer.start();
    }
}