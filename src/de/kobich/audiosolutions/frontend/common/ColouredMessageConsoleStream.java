package de.kobich.audiosolutions.frontend.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * Coloured messages to console.
 */
public class ColouredMessageConsoleStream extends MessageConsoleStream {
	private String fDefaultEncoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
	private final MessageConsoleStream defaultStream;
	private final boolean needsEncoding;
	private final Map<String, MessageConsoleStream> prefix2Stream;
	
	/**
	 * Returns a standard coloured console stream
	 * @param console
	 * @return
	 */
	public static ColouredMessageConsoleStream getStandardConsoleStream(MessageConsole console) {
		MessageConsoleStream stdStream = console.newMessageStream();
		MessageConsoleStream cmdStream = console.newMessageStream();
		cmdStream.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		MessageConsoleStream msgStream = console.newMessageStream();
		msgStream.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		
		Map<String, MessageConsoleStream> prefix2Stream = new HashMap<String, MessageConsoleStream>();
		prefix2Stream.put("> ", cmdStream);
		prefix2Stream.put("# ", msgStream);
		
		return new ColouredMessageConsoleStream(console, stdStream, prefix2Stream);
	}
	
	/**
	 * Returns a error coloured console stream
	 * @param console
	 * @return
	 */
	public static ColouredMessageConsoleStream getErrorConsoleStream(MessageConsole console) {
		MessageConsoleStream errStream = console.newMessageStream();
		errStream.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		return new ColouredMessageConsoleStream(console, errStream);
	}

	public ColouredMessageConsoleStream(MessageConsole console, MessageConsoleStream defaultStream) {
		this(console, defaultStream, new HashMap<String, MessageConsoleStream>());
	}
	public ColouredMessageConsoleStream(MessageConsole console, MessageConsoleStream defaultStream, Map<String, MessageConsoleStream> prefix2Stream) {
		super(console);
		this.prefix2Stream = prefix2Stream;
		this.defaultStream = defaultStream;
		this.needsEncoding = isEncodingNeeded(console);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		String message = encodeString(b, off, len);
		String[] lines = message.split("\n");
		for (String line : lines) {
			boolean streamFound = false;
			for (String prefix : prefix2Stream.keySet()) {
				if (line.startsWith(prefix)) {
					MessageConsoleStream stream = prefix2Stream.get(prefix);
					if (!stream.isClosed()) {
						stream.println(line);
						streamFound = true;
						break;
					}
				}
			}
			if (!streamFound) {
				if (!defaultStream.isClosed()) {
					defaultStream.println(line);
				}
			}
		}
	}
	
	private String encodeString(byte[] b, int off, int len) throws UnsupportedEncodingException {
		if (needsEncoding) {
			return new String(b, off, len, getConsole().getEncoding());
		}
		return new String(b, off, len);
	}
	
	private boolean isEncodingNeeded(MessageConsole console) {
		return (getConsole().getEncoding()!=null) && (!getConsole().getEncoding().equals(fDefaultEncoding));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.console.IOConsoleOutputStream#close()
	 */
	@Override
	public synchronized void close() throws IOException {
		for (MessageConsoleStream stream : prefix2Stream.values()) {
			stream.close();
		}
		defaultStream.close();
	}

}
