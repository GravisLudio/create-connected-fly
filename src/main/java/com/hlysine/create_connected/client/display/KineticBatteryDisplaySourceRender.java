package com.hlysine.create_connected.client.display;

import com.hlysine.create_connected.ConnectedLang;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.content.redstone.displayLink.source.SingleLineDisplaySourceRender;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

/**
 * 26.2 split the display link's configuration UI off the {@code DisplaySource} into a client-side
 * {@code DisplaySourceRender}, reached through the source's {@code attachRender} field. Create Fly
 * does the same for its own fill-level source.
 */
public class KineticBatteryDisplaySourceRender extends SingleLineDisplaySourceRender {

    @Override
    public void initConfigurationWidgets(
            DisplaySource source,
            DisplayLinkContext context,
            ModularGuiLineBuilder builder,
            boolean isFirstLine
    ) {
        super.initConfigurationWidgets(source, context, builder, isFirstLine);
        if (isFirstLine)
            return;
        builder.addSelectionScrollInput(0, 120,
                (si, l) -> si.forOptions(ConnectedLang.translatedOptions("display_source.kinetic_battery", "number", "percentage", "progress_bar"))
                        .titled(ConnectedLang.translateDirect("display_source.kinetic_battery.display")),
                "Mode");
    }
}
