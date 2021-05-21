package fi.dy.masa.malilib.gui.widget;

import javax.annotation.Nullable;
import fi.dy.masa.malilib.gui.icon.Icon;
import fi.dy.masa.malilib.gui.util.ScreenContext;

public class IconWidget extends BackgroundWidget
{
    protected boolean doHighlight;
    protected boolean enabled;

    public IconWidget(int x, int y, @Nullable Icon icon)
    {
        super(x, y, icon.getWidth(), icon.getHeight());

        this.setIcon(icon);
    }

    @Override
    public void setIcon(@Nullable Icon icon)
    {
        super.setIcon(icon);

        this.updateWidth();
        this.updateHeight();
    }

    public IconWidget setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        return this;
    }

    public IconWidget setDoHighlight(boolean doHighlight)
    {
        this.doHighlight = doHighlight;
        return this;
    }

    @Override
    public void updateWidth()
    {
        if (this.icon != null)
        {
            int width = this.icon.getWidth();

            if (this.renderNormalBackground)
            {
                width += this.padding.getLeft() + this.padding.getRight() + this.normalBorderWidth * 2;
            }

            this.setWidth(width);
        }
        else
        {
            this.setWidth(0);
        }
    }

    @Override
    public void updateHeight()
    {
        if (this.icon != null)
        {
            int height = this.icon.getHeight();

            if (this.renderNormalBackground)
            {
                height += this.padding.getTop() + this.padding.getBottom() + this.normalBorderWidth * 2;
            }

            this.setHeight(height);
        }
        else
        {
            this.setHeight(0);
        }
    }

    @Override
    protected void renderIcon(int x, int y, float z, boolean enabled, boolean hovered, ScreenContext ctx)
    {
        super.renderIcon(x, y, z, enabled, hovered, ctx);
    }

    @Override
    public void renderAt(int x, int y, float z, ScreenContext ctx)
    {
        if (this.icon != null)
        {
            boolean hovered = this.doHighlight && this.isHoveredForRender(ctx);

            this.renderWidgetBackground(x, y, z, ctx);
            this.renderText(x, y, z, hovered, ctx);

            if (this.renderNormalBackground)
            {
                x += this.padding.getLeft() + this.normalBorderWidth;
                y += this.padding.getTop() + this.normalBorderWidth;
            }

            this.renderIcon(x, y, z, this.enabled, hovered, ctx);
        }
    }
}
