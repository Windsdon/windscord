package com.windsdon.windscord;

import net.minecraft.text.Text;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class MessageQueue implements Runnable {
	private static BlockingDeque<Message> QUEUE = new LinkedBlockingDeque<>();

	public static void enqueue(String uuid, String message) {
		QUEUE.add(new PlayerMessage(uuid, message));
	}

	public static void enqueue(Text text) {
		QUEUE.add(new ServerMessage(text));
	}

	public static void begin() {
		Thread messageThread = new Thread(new MessageQueue(), "MessageQueue");
		messageThread.start();
	}

	@Override
	public void run() {
		while (true) {
			try {
				Message message = QUEUE.take();
				message.process();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private static class PlayerMessage implements Message {

		public final String uuid;
		public final String message;

		public PlayerMessage(String uuid, String message) {
			this.uuid = uuid;
			this.message = message;
		}

		@Override
		public void process() {
			WindscordLog.LOGGER.info("Processing message queue, uuid {}, message {}", uuid, message);
			Discord.UserInfo userInfo = UserRegistry.getUser(uuid);
			WindscordMod.DISCORD.sendMessage(userInfo, message);
		}
	}

	private static class ServerMessage implements Message {
		private final Text text;

		public ServerMessage(Text text) {
			this.text = text;
		}

		@Override
		public void process() {
			WindscordMod.DISCORD.sendMessage(text.getString());
		}
	}

	private interface Message {
		void process();

	}
}
