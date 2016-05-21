import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HW
{
    String title;
    String status;
    // I don't use LocalDateTime because if date parser go wrong, the original
    // date is still available.
    // e.g. I didn't write a parser for dueDate showed in English 
    String dueDateTime;

    public HW(String hwName, String status, String dueDateTime)
    {
        this.title = hwName;
        this.status = status;
        this.dueDateTime = dueDateTime;
    }

    public boolean isOverdue()
    {
        Pattern zhTimePtn = Pattern.compile(
                "([0-9]+)-([0-9]+)-([0-9]+) ([^\\s]+)([0-9]+):([0-9]+)");
        Matcher matcher = zhTimePtn.matcher(dueDateTime);
        if (matcher.find())
        {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int date = Integer.parseInt(matcher.group(3));
            int offset = matcher.group(4).equals("上午") ? 0 : 12;
            int hour = Integer.parseInt(matcher.group(5)) + offset;
            int minute = Integer.parseInt(matcher.group(6));
            LocalDateTime dueLDT = LocalDateTime.of(year, month, date, hour,
                    minute);
            return dueLDT.compareTo(LocalDateTime.now()) < 0;
        }
        return false;
    }
}
