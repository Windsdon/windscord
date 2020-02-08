package com.windsdon.windscord;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WindscordLog {
	public static Logger LOGGER = LogManager.getLogger("Windscord");

	public static void log(Level level, String text) {
		System.out.println("Log " + text + " on thread " + Thread.currentThread().getName() + "#" + Thread.currentThread().getId() );
		LOGGER.log(level, text);
	}
}
