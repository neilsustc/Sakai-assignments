import java.util.ArrayList;
import java.util.Map;

import javax.swing.UIManager;

public class Main
{
    public static void main(String[] args)
    {
        setDefualtLookAndFeel();
        MainWindow window = new MainWindow();
        window.showInfoMsg("Processing...");
        window.setVisible(true);
        Config config = Config.INSTANCE;
        String username = config.getValue("username");
        String password = config.getValue("password");
        boolean showOverdueHw = tryParse(config.getValue("showOverdueHw"));
        if (username == null || password == null || "".equals(username)
                || "".equals(password))
        {
            window.showErrorMsg("请在 config.properties 中设置相应的 Sakai 账户");
        } else
        {
            Map<String, ArrayList<HW>> courseAndHws = Sakai.getHws(username,
                    password);
            if (courseAndHws != null)
            {
                window.showHws(courseAndHws, showOverdueHw);
            }
        }
    }

    private static void setDefualtLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static boolean tryParse(String str)
    {
        try
        {
            boolean b = Boolean.parseBoolean(str);
            return b;
        } catch (Exception e)
        {
        }
        return false;
    }
}
