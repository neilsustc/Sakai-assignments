import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.UIManager;

public class Main
{
    public static Logger logger = Logger.getLogger(Main.class.getName());

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
        boolean debug = tryParse(config.getValue("debug"));
        if (debug)
        {
            configLogger();
        }
        logger.info("Configurations loaded.");
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

    private static void configLogger()
    {
        try
        {
            FileHandler handler = new FileHandler("sakai.log");
            handler.setFormatter(new SimpleFormatter()
            {
                @Override
                public synchronized String format(LogRecord record)
                {
                    return String.format(
                            "[%s] %s - %s.%s()" + System.lineSeparator(),
                            record.getLevel(), record.getMessage(),
                            record.getSourceClassName(),
                            record.getSourceMethodName());
                }
            });
            logger.addHandler(handler);
        } catch (SecurityException | IOException e)
        {
            e.printStackTrace();
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
