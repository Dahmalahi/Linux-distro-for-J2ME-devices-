import javax.microedition.lcdui.*;

/**
 * CalculatorScientific.java v2.1
 * Calculatrice scientifique avec fonctions avancées
 */
public class CalculatorScientific extends Form implements CommandListener {
    private DiscoOs mainApp;
    private StringItem display;
    private StringBuffer expression;
    private double result = 0;
    private boolean newNumber = true;
    private Command[] numberCmds = new Command[10];
    private Command addCmd, subCmd, mulCmd, divCmd, equalsCmd;
    private Command clearCmd, backCmd, decimalCmd;
    private Command sqrtCmd, squareCmd, piCmd, sinCmd, cosCmd, tanCmd;
    private Command lparenCmd, rparenCmd, percentCmd;

    public CalculatorScientific(DiscoOs app) {
        super("Calculator Scientific");
        this.mainApp = app;
        expression = new StringBuffer();

        // Display
        display = new StringItem(" ", "0");
        display.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE));
        display.setLayout(Item.LAYOUT_2 | Item.LAYOUT_NEWLINE_AFTER);
        append(display);

        append(new StringItem(" ", "---\n"));

        // Create commands
        for (int i = 0; i <= 9; i++) {
            numberCmds[i] = new Command(" " + i, Command.SCREEN, i);
            addCommand(numberCmds[i]);
        }

        addCmd = new Command("+", Command.SCREEN, 10);
        subCmd = new Command("-", Command.SCREEN, 11);
        mulCmd = new Command("*", Command.SCREEN, 12);
        divCmd = new Command("/", Command.SCREEN, 13);

        sqrtCmd = new Command("sqrt", Command.SCREEN, 14);
        squareCmd = new Command("x^2", Command.SCREEN, 15);
        piCmd = new Command("PI", Command.SCREEN, 16);
        sinCmd = new Command("sin", Command.SCREEN, 17);
        cosCmd = new Command("cos", Command.SCREEN, 18);
        tanCmd = new Command("tan", Command.SCREEN, 19);

        lparenCmd = new Command("(", Command.SCREEN, 20);
        rparenCmd = new Command(")", Command.SCREEN, 21);
        percentCmd = new Command("%", Command.SCREEN, 22);
        decimalCmd = new Command(".", Command.SCREEN, 23);

        equalsCmd = new Command("=", Command.OK, 24);
        clearCmd = new Command("C", Command.SCREEN, 25);
        backCmd = new Command("Back", Command.BACK, 26);

        addCommand(addCmd);
        addCommand(subCmd);
        addCommand(mulCmd);
        addCommand(divCmd);
        addCommand(sqrtCmd);
        addCommand(squareCmd);
        addCommand(piCmd);
        addCommand(sinCmd);
        addCommand(cosCmd);
        addCommand(tanCmd);
        addCommand(lparenCmd);
        addCommand(rparenCmd);
        addCommand(percentCmd);
        addCommand(decimalCmd);
        addCommand(equalsCmd);
        addCommand(clearCmd);
        addCommand(backCmd);

        setCommandListener(this);

        // Instructions
        append(new StringItem(" ",
             "Functions:\n" +
             "Numbers: 0-9\n" +
             "Operations: + - * /\n" +
             "Advanced: sqrt, x^2, PI\n" +
             "Trigonometry: sin, cos, tan\n" +
             "Other: ( ) % .\n" +
             "= to calculate\n" +
             "C to clear"));
    }

    public void show() {
        mainApp.getDisplay().setCurrent(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            mainApp.showMainMenu();
            return;
        }
        if (c == clearCmd) {
            clear();
            return;
        }
        if (c == equalsCmd) {
            calculate();
            return;
        }

        // Numbers
        for (int i = 0; i <= 9; i++) {
            if (c == numberCmds[i]) {
                appendChar(" " + i);
                return;
            }
        }

        // Operations
        if (c == addCmd) appendChar("+");
        else if (c == subCmd) appendChar("-");
        else if (c == mulCmd) appendChar("*");
        else if (c == divCmd) appendChar("/");
        else if (c == decimalCmd) appendChar(".");
        else if (c == lparenCmd) appendChar("(");
        else if (c == rparenCmd) appendChar(")");
        else if (c == percentCmd) appendChar("%");

        // Scientific
        else if (c == sqrtCmd) appendChar("sqrt(");
        else if (c == squareCmd) {
            if (expression.length() > 0) {
                expression.append("^2");
                updateDisplay();
            }
        }
        else if (c == piCmd) appendChar("3.14159");
        else if (c == sinCmd) appendChar("sin(");
        else if (c == cosCmd) appendChar("cos(");
        else if (c == tanCmd) appendChar("tan(");
    }

    private void appendChar(String s) {
        if (newNumber && !s.equals("(") && !s.equals("sqrt(") &&
            !s.equals("sin(") && !s.equals("cos(") && !s.equals("tan(")) {
            expression.setLength(0);
            newNumber = false;
        }
        expression.append(s);
        updateDisplay();
    }

    private void updateDisplay() {
        display.setText(expression.toString());
    }

    private void clear() {
        expression.setLength(0);
        result = 0;
        newNumber = true;
        display.setText("0");
    }

    private void calculate() {
        try {
            String expr = expression.toString();

            // Process scientific functions
            expr = processFunction(expr, "sqrt");
            expr = processFunction(expr, "sin");
            expr = processFunction(expr, "cos");
            expr = processFunction(expr, "tan");

            // Process ^2
            while (expr.indexOf("^2") >= 0) {
                int idx = expr.indexOf("^2");
                int start = idx - 1;
                while (start > 0 && (Character.isDigit(expr.charAt(start)) || expr.charAt(start) == '.' || expr.charAt(start) == '-')) {
                    start--;
                }
                start++;
                String numStr = expr.substring(start, idx);
                double num = Double.parseDouble(numStr);
                double squared = num * num;
                expr = expr.substring(0, start) + squared + expr.substring(idx + 2);
            }

            // Simple expression evaluation
            result = evaluateSimple(expr);

            expression.setLength(0);
            expression.append(" " + result);
            updateDisplay();
            newNumber = true;

        } catch (Exception e) {
            display.setText("Error: " + e.getMessage());
            expression.setLength(0);
            newNumber = true;
        }
    }

    private String processFunction(String expr, String func) throws Exception {
        while (expr.indexOf(func + "(") >= 0) {
            int start = expr.indexOf(func + "(");
            int parenCount = 1;
            int end = start + func.length() + 1;
            while (end < expr.length() && parenCount > 0) {
                if (expr.charAt(end) == '(') parenCount++;
                if (expr.charAt(end) == ')') parenCount--;
                end++;
            }

            String inner = expr.substring(start + func.length() + 1, end - 1);
            double val = evaluateSimple(inner);
            double result;

            if (func.equals("sqrt")) result = Math.sqrt(val);
            else if (func.equals("sin")) result = Math.sin(val);
            else if (func.equals("cos")) result = Math.cos(val);
            else if (func.equals("tan")) result = Math.tan(val);
            else result = val;

            expr = expr.substring(0, start) + result + expr.substring(end);
        }
        return expr;
    }

    private double evaluateSimple(String expr) throws Exception {
        expr = expr.trim();

        // Handle parentheses
        while (expr.indexOf("(") >= 0) {
            int start = -1;
            for (int i = expr.length() - 1; i >= 0; i--) {
                if (expr.charAt(i) == '(') {
                    start = i;
                    break;
                }
            }
            int end = expr.indexOf(")", start);
            String inner = expr.substring(start + 1, end);
            double val = evaluateSimple(inner);
            expr = expr.substring(0, start) + val + expr.substring(end + 1);
        }

        // Handle percentage
        if (expr.indexOf("%") >= 0) {
            String[] parts = split(expr, '%');
            if (parts.length == 2) {
                double left = evaluateSimple(parts[0]);
                return left / 100;
            }
        }

        // Handle operations in order: * / then + -
        // Multiplication and division
        while (expr.indexOf("*") >= 0 || expr.indexOf("/") >= 0) {
            int mulIdx = expr.indexOf("*");
            int divIdx = expr.indexOf("/");

            int idx = (mulIdx >= 0 && (divIdx < 0 || mulIdx < divIdx)) ? mulIdx : divIdx;
            char op = expr.charAt(idx);

            // Find left operand
            int leftStart = idx - 1;
            while (leftStart > 0 && (Character.isDigit(expr.charAt(leftStart)) ||
                   expr.charAt(leftStart) == '.' || expr.charAt(leftStart) == '-')) {
                leftStart--;
            }
            if (leftStart < 0 || (!Character.isDigit(expr.charAt(leftStart)) && expr.charAt(leftStart) != '-')) leftStart++;

            // Find right operand
            int rightEnd = idx + 1;
            if (rightEnd < expr.length() && expr.charAt(rightEnd) == '-') rightEnd++;
            while (rightEnd < expr.length() && (Character.isDigit(expr.charAt(rightEnd)) ||
                   expr.charAt(rightEnd) == '.')) {
                rightEnd++;
            }

            double left = Double.parseDouble(expr.substring(leftStart, idx));
            double right = Double.parseDouble(expr.substring(idx + 1, rightEnd));
            double result = (op == '*') ? left * right : left / right;

            expr = expr.substring(0, leftStart) + result + expr.substring(rightEnd);
        }

        // Addition and subtraction
        String[] parts = split(expr, '+');
        if (parts.length > 1) {
            double sum = 0;
            for (int i = 0; i < parts.length; i++) {
                sum += evaluateSimple(parts[i]);
            }
            return sum;
        }

        // Just a number
        return Double.parseDouble(expr);
    }

    private String[] split(String str, char delim) {
        int count = 1;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == delim) count++;
        }
        String[] result = new String[count];
        int idx = 0;
        int start = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == delim) {
                result[idx++] = str.substring(start, i);
                start = i + 1;
            }
        }
        result[idx] = str.substring(start);

        return result;
    }
}