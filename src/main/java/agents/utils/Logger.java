package agents.utils;

import java.util.Arrays;

public class Logger {

	private static int ROW_LENGTH = 100;

	private static boolean FINISHED_WITH_NEW_LINE = false;

	public static void info(String message) {
		System.out.println("[INFO] : " + message);
		FINISHED_WITH_NEW_LINE = false;
	}

	public static void process(String message) {
		System.out.println("[PROCESS] : " + message);
		FINISHED_WITH_NEW_LINE = false;
	}

	public static void supervisor(String message) {
		System.out.println("[SUPERVISOR] : " + message);
		FINISHED_WITH_NEW_LINE = false;
	}

	public static void breaks(String message) {
		int messageLength = message.length();
		char[] pad = new char[(ROW_LENGTH - messageLength + 4) / 2];
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.fill(pad, '✕');
		System.out.println(stringBuilder
				.append(formatNewLine())
				.append(pad)
				.append(" [")
				.append(message)
				.append("] ")
				.append(pad)
				.append(widthFiller(messageLength,'✕'))
				.append("\n")
				.toString());
		FINISHED_WITH_NEW_LINE = true;
	}

	public static void summary(String message, boolean centered) {
		int messageLength = message.length();
		char[] frame = new char[ROW_LENGTH + 8];
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.fill(frame, '=');
		if (centered) {
			char[] pad = new char[(ROW_LENGTH - messageLength) / 2];
			Arrays.fill(pad, ' ');
			System.out.println(stringBuilder
					.append(formatNewLine())
					.append(frame)
					.append("\n")
					.append(pad)
					.append(message)
					.append(pad)
					.append("\n")
					.append(frame)
					.append("\n")
					.toString());
		} else {
			System.out.println(stringBuilder
					.append(formatNewLine())
					.append(frame)
					.append("\n")
					.append(message)
					.append("\n")
					.append(frame)
					.append("\n")
					.toString());
		}
		FINISHED_WITH_NEW_LINE = true;
	}

	private static String formatNewLine() {
		return FINISHED_WITH_NEW_LINE ? "" : "\n";
	}

	private static String widthFiller(int messageLength, char c) {
		return messageLength % 2 == 0 ? "" : "" + c;
	}

}
