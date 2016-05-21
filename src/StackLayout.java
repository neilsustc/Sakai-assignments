

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class StackLayout implements LayoutManager
{
    public final static int HORIZONTAL = 0;
    public final static int VERTICAL = 1;
    int direction;
    int gap = 0;

    public StackLayout(int direction, int gap)
    {
        this.direction = direction;
        this.gap = gap;
    }

    @Override
    public void addLayoutComponent(String name, Component comp)
    {
    }

    @Override
    public void removeLayoutComponent(Component comp)
    {
    }

    @Override
    public Dimension preferredLayoutSize(Container parent)
    {
        return minimumLayoutSize(parent);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent)
    {
        int width = 0, height = 0, widthSum = 0, heightSum = 0, maxWidth = 0,
                maxHeight = 0;
        for (Component component : parent.getComponents())
        {
            int cWidth = component.getPreferredSize().width;
            int cHeight = component.getPreferredSize().height;
            widthSum += cWidth;
            heightSum += cHeight;
            if (cWidth > maxWidth)
                maxWidth = cWidth;
            if (cHeight > maxHeight)
                maxHeight = cHeight;
        }
        int count = parent.getComponentCount();
        switch (direction)
        {
        case HORIZONTAL:
            width = widthSum + (count + 1) * gap;
            height = maxHeight + 2 * gap;
            break;
        case VERTICAL:
            width = maxWidth + 2 * gap;
            height = heightSum + (count + 1) * gap;
            break;
        }
        return new Dimension(width, height);
    }

    @Override
    public void layoutContainer(Container parent)
    {
        int x = gap, y = gap;
        for (int i = 0; i < parent.getComponentCount(); i++)
        {
            Component component = parent.getComponent(i);
            Dimension dimension = component.getPreferredSize();

            switch (direction)
            {
            case HORIZONTAL:
                component.setBounds(x, y, dimension.width, dimension.height);
                x += dimension.width + gap;
                break;
            case VERTICAL:
                component.setBounds(x, y, dimension.width, dimension.height);
                y += dimension.height + gap;
            }
        }
    }
}
