public class ALULogic {
    private int result;
    private String binaryResult;
    private java.beans.PropertyChangeSupport changes;

    private String[] recentCalculations = new String[10];
    private int historyIndex = 0;

    public ALULogic() {
        changes = new java.beans.PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    private void updateResult(int newResult) {
        int oldResult = this.result;
        this.result = newResult;
        this.binaryResult = String.format("%32s", Integer.toBinaryString(newResult))
                               .replace(' ', '0')
                               .replaceAll("(.{8})", "$1 ")
                               .trim();
        changes.firePropertyChange("result", oldResult, newResult);
        changes.firePropertyChange("binaryResult", null, binaryResult);
    }

    // Arithmetic Operations
    public int add(int a, int b) {
        long result = (long) a + b;
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new ArithmeticException("Addition overflow");
        }
        updateResult((int) result);
        return (int) result;
    }

    public int subtract(int a, int b) {
        long result = (long) a - b;
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new ArithmeticException("Subtraction overflow");
        }
        updateResult((int) result);
        return (int) result;
    }

    public int multiply(int a, int b) {
        long result = (long) a * b;
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new ArithmeticException("Multiplication overflow");
        }
        updateResult((int) result);
        return (int) result;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero");
        }
        if (a == Integer.MIN_VALUE && b == -1) {
            throw new ArithmeticException("Division overflow");
        }
        int result = a / b;
        updateResult(result);
        return result;
    }

    public int modulo(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Modulo by zero");
        }
        int result = a % b;
        updateResult(result);
        return result;
    }

    // Logical Operations
    public int and(int a, int b) {
        int result = a & b;
        updateResult(result);
        return result;
    }

    public int or(int a, int b) {
        int result = a | b;
        updateResult(result);
        return result;
    }

    public int not(int a) {
        int result = ~a;
        updateResult(result);
        return result;
    }

    public int leftShift(int a, int b) {
        if (b < 0 || b >= 32) {
            throw new IllegalArgumentException("Shift amount must be between 0 and 31");
        }
        int result = a << b;
        updateResult(result);
        return result;
    }

    public int rightShift(int a, int b) {
        if (b < 0 || b >= 32) {
            throw new IllegalArgumentException("Shift amount must be between 0 and 31");
        }
        int result = a >> b;
        updateResult(result);
        return result;
    }

    public String getBinaryResult() {
        return binaryResult;
    }

    // Input validation
    public boolean isValidInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Number base conversion utilities
    public String toHexString(int value) {
        return String.format("0x%08X", value);
    }

    public void addToHistory(String operation, int a, int b, int result, String base) {
        String entry;
        if (operation.equals("NOT")) {
            entry = formatHistoryEntry(operation, a, 0, result, base, true);
        } else {
            entry = formatHistoryEntry(operation, a, b, result, base, false);
        }
        recentCalculations[historyIndex] = entry;
        historyIndex = (historyIndex + 1) % 10;
        changes.firePropertyChange("historyUpdate", null, getHistory());
    }

    private String formatHistoryEntry(String operation, int a, int b, int result, String base, boolean isUnary) {
        if (base.equals("Binary")) {
            if (isUnary) {
                return String.format("%s %s = %s",
                    Integer.toBinaryString(a),
                    operation,
                    Integer.toBinaryString(result));
            }
            return String.format("%s %s %s = %s",
                Integer.toBinaryString(a),
                operation,
                Integer.toBinaryString(b),
                Integer.toBinaryString(result));
        } else if (base.equals("Hexadecimal")) {
            if (isUnary) {
                return String.format("0x%X %s = 0x%X",
                    a, operation, result);
            }
            return String.format("0x%X %s 0x%X = 0x%X",
                a, operation, b, result);
        } else {
            if (isUnary) {
                return String.format("%d %s = %d",
                    a, operation, result);
            }
            return String.format("%d %s %d = %d",
                a, operation, b, result);
        }
    }

    public String[] getHistory() {
        return recentCalculations;
    }
}