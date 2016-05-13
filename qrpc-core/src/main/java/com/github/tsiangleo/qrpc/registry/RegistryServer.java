package com.github.tsiangleo.qrpc.registry;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class RegistryServer {
	private int port;

	public RegistryServer(int port) {
		this.port = port;
	}

	public void start() {
		System.out.println("RegistryServer started , port " + port);
		
		try {
			new Reactor(port).run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

class Reactor implements Runnable {
	final Selector selector;
	final ServerSocketChannel serverSocket;

	Reactor(int port) throws IOException {
		selector = Selector.open();
		serverSocket = ServerSocketChannel.open();
		serverSocket.socket().bind(new InetSocketAddress(port));
		serverSocket.configureBlocking(false);
		SelectionKey sk = serverSocket.register(selector,
				SelectionKey.OP_ACCEPT);
		sk.attach(new Acceptor());
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				selector.select();
				Set<SelectionKey> selected = selector.selectedKeys();
				Iterator<SelectionKey> it = selected.iterator();
				while (it.hasNext())
					dispatch(it.next());
				selected.clear();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	void dispatch(SelectionKey k) {
		Runnable r = (Runnable) (k.attachment());
		if (r != null)
			r.run();
	}

	class Acceptor implements Runnable {

		@Override
		public void run() {
			try {
				SocketChannel c = serverSocket.accept();
				if (c != null)
					new Handler(selector, c);	//注册该SocketChannel感兴趣的时间，并绑定对应的attachment。
			} catch (IOException ex) { 
				ex.printStackTrace();
			}

		}

	}

}

final class Handler implements Runnable {
	private static final int MAXIN = 1024;
	private static final int MAXOUT = 1024;
	final SocketChannel socketChannel;
	final SelectionKey selectionKey;
	ByteBuffer input = ByteBuffer.allocate(MAXIN);
	ByteBuffer output = ByteBuffer.allocate(MAXOUT);
	static final int READING = 0, SENDING = 1;
	int state = READING;

	Handler(Selector selector, SocketChannel c) throws IOException {
		socketChannel = c;
		c.configureBlocking(false);
		// Optionally try first read now
		selectionKey = socketChannel.register(selector, 0);
		selectionKey.attach(this);	//该socketChannel发生了相应的时间就调用下面的run方法。
		
		selectionKey.interestOps(SelectionKey.OP_READ);
		selector.wakeup();
	}

	boolean inputIsComplete() {
		return false; /* ... */
	}

	boolean outputIsComplete() {
		return false; /* ... */
	}

	void process() { /* ... */
	}

	@Override
	public void run() {
		try {
			if (state == READING)
				read();
			else if (state == SENDING)
				send();
		} catch (IOException ex) { /* ... */
			ex.printStackTrace();
		}
	}

	void read() throws IOException {
		socketChannel.read(input);
		if (inputIsComplete()) {
			process();
			state = SENDING;
			// Normally also do first write now
			selectionKey.interestOps(SelectionKey.OP_WRITE);
		}
	}

	void send() throws IOException {
		socketChannel.write(output);
		if (outputIsComplete())
			selectionKey.cancel();
	}
}
