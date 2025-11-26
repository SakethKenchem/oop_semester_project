import ui.LoginForm;
import ui.RegisterForm;

public class Voter {
    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        new LoginForm();
    }
}