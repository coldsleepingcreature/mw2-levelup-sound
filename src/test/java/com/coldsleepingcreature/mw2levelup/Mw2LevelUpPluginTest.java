package com.coldsleepingcreature.mw2levelup;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class Mw2LevelUpPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(Mw2LevelUpPlugin.class);
        RuneLite.main(args);
    }
}