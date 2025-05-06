# ALU Simulator

A Java-based Arithmetic Logic Unit (ALU) simulator with a user interface. This application allows users to perform basic arithmetic and logical operations while visualizing the binary representations of numbers.

## Features

- Arithmetic operations (Addition, Subtraction, Multiplication, Division, Modulo)
- Logical operations (AND, OR, NOT)
- Bitwise operations (Left Shift, Right Shift)
- Multiple number base support (Decimal, Binary, Hexadecimal)
- Real-time binary representation of inputs and results
- Calculation history with last 10 operations
- Copy results to clipboard
- user-friendly graphical interface
- Input validation and error handling
- Visual feedback for operations
- Keyboard navigation support
- Accessibility features

## Requirements

- Java Runtime Environment (JRE) 8 or later
- Windows/Linux/macOS operating system

## Getting Started

1. Clone or download this repository
2. Navigate to the project directory
3. Run the application:
   - On Windows: Double-click `run_ALU.bat`
   - On Linux/macOS: Run the following commands:
     ```
     cd src
     javac *.java
     java Main
     ```

## How to Use

1. Select the number base (Decimal, Binary, or Hexadecimal) for input/output
2. Enter the first number in the "Input 1" field
3. Enter the second number in the "Input 2" field (not required for NOT operation)
4. Select an operation from the dropdown menu:
   - ADD: Add two numbers
   - SUBTRACT: Subtract second number from first
   - MULTIPLY: Multiply two numbers
   - DIVIDE: Divide first number by second
   - MODULO: Get remainder after division
   - LEFT SHIFT: Shift bits left
   - RIGHT SHIFT: Shift bits right
   - AND: Perform bitwise AND operation
   - OR: Perform bitwise OR operation
   - NOT: Perform bitwise NOT operation on first input

5. Click "Calculate" or use keyboard shortcuts to perform the operation
6. View the result in your selected number base and binary format
7. Use the "Copy Result" button or Alt+C to copy the result to clipboard

## Keyboard Shortcuts

- Alt+A: Perform Addition
- Alt+S: Perform Subtraction
- Alt+M: Perform Multiplication
- Alt+D: Perform Division
- Alt+R: Perform Modulo
- Alt+L: Perform Left Shift
- Alt+H: Perform Right Shift
- Alt+C: Copy result to clipboard

## Project Structure

- `src/Main.java` - Application entry point
- `src/ALUInterface.java` - GUI implementation with modern UI components
- `src/ALULogic.java` - Core ALU operations implementation
- `run_ALU.bat` - Windows batch file for easy execution

## Features in Detail

- **Multiple Base Support**: Work with numbers in decimal, binary, or hexadecimal
- **Real-time Binary Display**: As you type numbers, their binary representations are shown
- **Input Validation**: Invalid inputs are highlighted with visual feedback
- **Modern UI**: Rounded corners, hover effects, and smooth animations
- **Error Handling**: Clear error messages for invalid operations (overflow, division by zero)
- **History Tracking**: Keep track of your last 10 calculations
- **Accessible Design**: Keyboard navigation and screen reader support
- **Overflow Protection**: Prevents integer overflow in calculations

## Contributing

Feel free to submit issues, fork the repository, and create pull requests for any improvements.