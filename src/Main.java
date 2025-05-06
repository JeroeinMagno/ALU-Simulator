public class Main {
    public static void main(String[] args) {
        // Launch the GUI using SwingUtilities to ensure thread safety
        javax.swing.SwingUtilities.invokeLater(() -> {
            ALUInterface aluInterface = new ALUInterface();
            aluInterface.setVisible(true);
        });
    }
}