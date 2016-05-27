import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MainWindow extends JFrame
{
    private static final long serialVersionUID = 1L;
    public static MainWindow current;

    private String css = "<style>body{font-size:1em;}</style>";
    private String trHr = "<tr><td colspan='2'><hr /></td></tr>";

    private JLabel jlbStatus = new JLabel();
    private JLabel jlbHwsInfo = new JLabel();

    public MainWindow()
    {
        current = this;

        add(jlbStatus);

        setTitle("Sakai Assignments");
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    public void showErrorMsg(String msg)
    {
        jlbStatus.setText(String
                .format("<html>%s<font color='red'>%s</font><html>", css, msg));
        pack();
        setLocationRelativeTo(null);
    }

    public void showInfoMsg(String msg)
    {
        jlbStatus.setText(String.format("<html>%s<p>%s</p><html>", css, msg));
        pack();
        setLocationRelativeTo(null);
    }

    public void showHws(Map<String, ArrayList<HW>> courseAndHws,
            boolean showOverdueHw)
    {
        setVisible(false);
        trySleep(200);
        remove(jlbStatus);
        add(jlbHwsInfo);

        StringBuilder sb = new StringBuilder("<table><tbody>" + trHr);
        for (Entry<String, ArrayList<HW>> entry : courseAndHws.entrySet())
        {
            ArrayList<HW> hws = entry.getValue();
            // filter
            List<HW> unfinished = hws.stream().filter(hw ->
            {
                if (hw.status.contains("已提交"))
                {
                    return false;
                } else
                {
                    if (hw.isOverdue())
                    {
                        if (showOverdueHw)
                            return true;
                        else
                            return false;
                    } else
                    {
                        return true;
                    }
                }
            }).collect(Collectors.toList());

            sb.append(String.format(
                    "<tr><td colspan='2'><strong>%s</strong></td></tr>",
                    entry.getKey()));
            if (unfinished.size() > 0)
            {
                unfinished.stream()
                        .forEach(hw -> addRow(sb, hw.title, hw.dueDateTime));
            } else
            {
                sb.append("<tr><td>No assignment</td></tr>");
            }
            sb.append(trHr);
        }
        sb.append("</tbody></table>");
        String msg = String.format("<html>%s%s</html>", css, sb.toString());
        jlbHwsInfo.setText(msg);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void trySleep(int i)
    {
        try
        {
            Thread.sleep(i);
        } catch (Exception e)
        {
        }
    }

    private static void addRow(StringBuilder sb, String title,
            String dueDateTime)
    {
        sb.append(String.format(
                "<tr><td>%s&nbsp;&nbsp;&nbsp;&nbsp;</td><td>%s</td></tr>",
                title, dueDateTime));
    }
}
