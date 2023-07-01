package com.Bing_Bong.scripts.InfernalScatter;

import com.runemate.ui.setting.annotation.open.*;
import com.runemate.ui.setting.open.*;
@SettingsGroup(group = "InfernalScatter")
public interface ScatterConfig extends Settings {
    @SettingsSection(title = "Consumables", description = "Consumable settings", order = 1)
    String consumableSettings = "consumableSettings";

    @Setting(key = "ashes", title = "Ashes", section = consumableSettings, order = 1)
    default Ashes ashes() {
        return Ashes.INFERNAL;
    }

}
