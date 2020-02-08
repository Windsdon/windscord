package com.windsdon.windscord;

public class Utils {
	public static <T> T interpretAs(Class<T> clazz, Object target) {
		//noinspection unchecked
		return (T) target;
	}
}
