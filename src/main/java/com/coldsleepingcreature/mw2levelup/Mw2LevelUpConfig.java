package com.coldsleepingcreature.mw2levelup;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("mw2levelup")
public interface Mw2LevelUpConfig extends Config
{
	@ConfigItem(
			keyName = "muted",
			name = "Mute Sound",
			description = "Temporarily disable the MW2 level up sound"
	)
	default boolean muted()
	{
		return false;
	}

	@Range(
			max = 100
	)
	@ConfigItem(
			keyName = "volume",
			name = "Sound Volume",
			description = "Adjust the playback volume of the guitar riff"
	)
	default int volume()
	{
		return 11; // Default to safe sweet spot
	}
}