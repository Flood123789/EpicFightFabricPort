package yesman.epicfight.client;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import yesman.epicfight.main.EpicFightMod;

public final class ClientHangWatchdog {
	private static final long HANG_WARNING_NANOS = TimeUnit.SECONDS.toNanos(20);
	private static final long WATCHDOG_SLEEP_MILLIS = 5000L;
	private static volatile boolean started;
	private static volatile long lastClientTickNanos = System.nanoTime();
	private static volatile long lastWarningNanos;
	private static volatile Thread clientThread;
	
	private ClientHangWatchdog() {
	}
	
	public static synchronized void start() {
		if (started) {
			return;
		}
		
		started = true;
		Thread watchdogThread = new Thread(ClientHangWatchdog::watch, "Epic Fight Client Hang Watchdog");
		watchdogThread.setDaemon(true);
		watchdogThread.start();
	}
	
	public static void markClientTick() {
		clientThread = Thread.currentThread();
		lastClientTickNanos = System.nanoTime();
	}
	
	private static void watch() {
		while (true) {
			try {
				Thread.sleep(WATCHDOG_SLEEP_MILLIS);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
				return;
			}
			
			Thread observedThread = clientThread;
			long now = System.nanoTime();
			
			if (observedThread != null && now - lastClientTickNanos >= HANG_WARNING_NANOS && now - lastWarningNanos >= HANG_WARNING_NANOS) {
				lastWarningNanos = now;
				EpicFightMod.LOGGER.error("Client render thread has not completed an Epic Fight tick for {} ms. Thread dump follows.\n{}", TimeUnit.NANOSECONDS.toMillis(now - lastClientTickNanos), createThreadDump(observedThread));
			}
		}
	}
	
	private static String createThreadDump(Thread observedThread) {
		StringBuilder builder = new StringBuilder(8192);
		
		for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
			Thread thread = entry.getKey();
			builder.append('"').append(thread.getName()).append('"');
			
			if (thread == observedThread) {
				builder.append(" [observed client thread]");
			}
			
			builder.append(" state=").append(thread.getState()).append('\n');
			
			for (StackTraceElement element : entry.getValue()) {
				builder.append("\tat ").append(element).append('\n');
			}
			
			builder.append('\n');
		}
		
		return builder.toString();
	}
}
