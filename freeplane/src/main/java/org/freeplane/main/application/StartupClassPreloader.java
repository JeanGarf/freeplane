package org.freeplane.main.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class StartupClassPreloader {
	private static final String CLASS_LIST_RESOURCE = "/startup-classes.txt";

	static void start() {
		final Thread thread = new Thread(StartupClassPreloader::loadClasses, "Startup class preloader");
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	private static void loadClasses() {
		final ClassLoader classLoader = StartupClassPreloader.class.getClassLoader();
		try (InputStream stream = StartupClassPreloader.class.getResourceAsStream(CLASS_LIST_RESOURCE)) {
			if (stream == null)
				return;
			final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			for (String className = reader.readLine(); className != null; className = reader.readLine())
				loadWithoutInitialization(className.trim(), classLoader);
		}
		catch (IOException | RuntimeException e) {
		}
	}

	private static void loadWithoutInitialization(final String className, final ClassLoader classLoader) {
		if (className.isEmpty())
			return;
		try {
			Class.forName(className, false, classLoader);
		}
		catch (ClassNotFoundException | LinkageError e) {
		}
	}
}
