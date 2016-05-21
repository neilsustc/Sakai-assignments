import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum Config
{
    INSTANCE("config.properties");

    private volatile Properties props = new Properties();

    private Config(String path)
    {
        try
        {
            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream(path);
            if (inputStream == null)
            {
                System.out.printf("[Warning] cannot find '%s'.\n", path);
                return;
            }
            props.load(inputStream);
            inputStream.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getValue(String key)
    {
        String property = props.getProperty(key);
        return property != null ? property.trim() : null;
    }
}
