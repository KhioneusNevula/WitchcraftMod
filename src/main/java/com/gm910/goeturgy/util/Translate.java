package com.gm910.goeturgy.util;

import net.minecraft.util.text.TextComponentTranslation;

public class Translate {

	public static String translate(String key, Object...vals) {
		return (new TextComponentTranslation(key, vals)).getFormattedText();
	}
}
