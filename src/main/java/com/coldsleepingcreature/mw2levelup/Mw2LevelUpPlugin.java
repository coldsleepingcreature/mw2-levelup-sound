package com.coldsleepingcreature.mw2levelup;

import com.google.inject.Provides;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
		name = "MW2 Level Up Sound",
		description = "Plays the iconic MW2 guitar riff upon character level up.",
		tags = {"level", "sound", "mw2"}
)
public class Mw2LevelUpPlugin extends Plugin
{
	@Inject
	private Mw2LevelUpConfig config;

	@Inject
	private EventBus eventBus;

	// Array to store the true level for every skill
	private final int[] oldLevels = new int[Skill.values().length];

	private boolean useRiffTwo = false;

	@Override
	protected void startUp()
	{
		Arrays.fill(oldLevels, 0);
		eventBus.register(this);
	}

	@Override
	protected void shutDown()
	{
		Arrays.fill(oldLevels, 0);
		eventBus.unregister(this);
	}

	@Provides
	Mw2LevelUpConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Mw2LevelUpConfig.class);
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		Skill skill = statChanged.getSkill();

		// Calculate the true level based on raw XP to ignore temporary potion boosts
		int currentXp = statChanged.getXp();
		int trueCurrentLevel = Experience.getLevelForXp(currentXp);
		int previousLevel = oldLevels[skill.ordinal()];

		// If previousLevel is 0, the plugin just started or the user just logged in.
		// We set the baseline without playing the sound.
		if (previousLevel == 0)
		{
			oldLevels[skill.ordinal()] = trueCurrentLevel;
			return;
		}

		// If the true level went up, play the sound and update the tracker
		if (trueCurrentLevel > previousLevel)
		{
			oldLevels[skill.ordinal()] = trueCurrentLevel;
			playMw2Sound();
		}
	}

	private void playMw2Sound()
	{
		if (config.muted())
		{
			return;
		}

		new Thread(() -> {
			try
			{
				// Select the file based on the toggle state, then flip it for the next time
				String fileName = useRiffTwo ? "mw2_levelup_riff02.wav" : "mw2_levelup_riff01.wav";
				useRiffTwo = !useRiffTwo;

				InputStream audioSrc = getClass().getResourceAsStream("/com/coldsleepingcreature/mw2levelup/" + fileName);
				if (audioSrc == null)
				{
					log.error("MW2 Sound file not found: {}", fileName);
					return;
				}

				InputStream bufferedIn = new BufferedInputStream(audioSrc);
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);

				Clip clip = AudioSystem.getClip();
				clip.open(audioStream);

				if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
				{
					FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

					// Get raw config volume (0 to 100) and normalize to 0.0 - 1.0
					float rawVolume = Math.min(Math.max(config.volume(), 0f), 100f) / 100f;

					// Safe decibel ceiling mapping (-40dB to -12dB) so max slider is never ear-shattering
					float minDb = -40.0f;
					float maxDb = -12.0f;

					float dB = minDb + (maxDb - minDb) * rawVolume;
					if (rawVolume == 0f)
					{
						dB = -80.0f; // Total mute cutoff
					}

					gainControl.setValue(dB);
				}

				clip.start();
			}
			catch (Exception e)
			{
				log.error("Failed to play MW2 level up sound", e);
			}
		}).start();
	}
}