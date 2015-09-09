package edu.berkeley.nlp.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Stack;

import edu.berkeley.nlp.util.functional.Fn;
import fig.basic.LogInfo;

public class Logger
{

	public static interface LogInterface
	{
		public void logsf(String s, Object... args);

		public void logs(Object s);

		public void logss(Object s);

		public void startTrack(Object s);

		public void startTrack(Object s, boolean printAllLines);

		public void endTrack();

		public void dbg(Object s);

		public void err(Object s);

		public void errf(String s, Object... args);

		public void warn(Object s);

		public void warnf(String string, Object... args);

		public void logssf(String string, Object... args);

	}

	public static class FigLogger implements LogInterface
	{

		public void dbg(Object s) {
			LogInfo.dbg(s);
		}

		public void endTrack() {
			LogInfo.end_track();
		}

		public void err(Object s) {
			LogInfo.error(s);
		}

		public void errf(String s, Object... args) {
			LogInfo.error(s, args);
		}

		public void logsf(String s, Object... args) {
			LogInfo.logs(s, args);
		}

		public void logs(Object s) {
			LogInfo.logs(s);
		}

		public void logss(Object s) {
			LogInfo.logss(s);
		}

		public void logssf(String string, Object... args) {
			LogInfo.logss(string, args);
		}

		public void startTrack(Object s) {
			LogInfo.track(s);
		}

		public void startTrack(Object s, boolean printAllLines) {
			LogInfo.track(s, printAllLines);
		}

		public void warn(Object s) {
			LogInfo.warning(s);
		}

		public void warnf(String string, Object... args) {
			LogInfo.warning(string, args);
		}
	}

	public static class SystemLogger implements LogInterface
	{

		private PrintStream out;

		private PrintStream err;

		private int trackLevel = 0;

		private boolean debug = true;

		public SystemLogger(PrintStream out, PrintStream err) {
			this.out = out;
			this.err = err;
		}

		public void close() {
			if (out != null) {
				out.close();
			}
			if (err != null) {
				err.close();
			}
		}

		public SystemLogger(String outFile, String errFile) throws FileNotFoundException {
			this(outFile != null ? new PrintStream(new FileOutputStream(outFile)) : null, errFile != null ? new PrintStream(new FileOutputStream(errFile))
				: null);
		}

		public SystemLogger() {
			this(System.out, System.err);
		}

		private Stack<Long> trackStartTimes = new Stack<Long>();

		private String getIndentPrefix() {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < trackLevel; ++i) {
				builder.append("\t");
			}
			return builder.toString();
		}

		private void output(PrintStream out, Object s) {
			if (out == null) return;
			String txt = (s == null ? "null" : s.toString());
			String[] lines = txt.split("\n");
			String prefix = getIndentPrefix();
			for (String line : lines) {
				out.println(prefix + line);
			}
		}

		public void dbg(Object s) {
			if (debug) output(out, "[dbg] " + s);
		}

		private String timeString(double milliSecs) {
			String timeStr = "";
			int hours = (int) (milliSecs / (1000 * 60 * 60));
			if (hours > 0) {
				milliSecs -= hours * 1000 * 60 * 60;
				timeStr += hours + "h";
			}
			int mins = (int) (milliSecs / (1000 * 60));
			if (mins > 0) {
				milliSecs -= mins * 1000.0 * 60.0;
				timeStr += mins + "m";
			}
			int secs = (int) (milliSecs / 1000.0);
			//if (secs > 0) {
			//milliSecs -= secs * 1000.0;
			timeStr += secs + "s";
			//}

			return timeStr;
		}

		public void endTrack() {
			String timeStr = null;
			synchronized (this) {
				trackLevel--;
				double milliSecs = System.currentTimeMillis() - trackStartTimes.pop();
				timeStr = timeString(milliSecs);
			}
			output(out, "} " + (timeStr != null ? "[" + timeStr + "]" : ""));
		}

		public void err(Object s) {
			err.println(s);
		}

		public void logs(Object s) {
			output(out, s);
		}

		public void logss(Object s) {
			output(out, s);
		}

		public void startTrack(Object s) {
			output(out, s + " {");
			synchronized (this) {
				trackLevel++;
				trackStartTimes.push(System.currentTimeMillis());
			}
		}

		public void startTrack(Object s, boolean printAllLines) {
			startTrack(s);
		}

		public void warn(Object s) {
			output(err, "[warn] " + s);
		}

		public void logsf(String s, Object... args) {
			logs(String.format(s, args));
		}

		public void errf(String s, Object... args) {
			output(err, "[err] " + String.format(s, args));
		}

		public void warnf(String string, Object... args) {
			warn(String.format(string, args));
		}

		public void logssf(String string, Object... args) {
			logss(String.format(string, args));
		}
	}

	public static class CompoundLogger implements LogInterface
	{
		private LogInterface[] loggers;

		public CompoundLogger(LogInterface... loggers) {
			this.loggers = loggers;
		}

		public void logsf(String s, Object... args) {
			for (LogInterface logger : loggers) {
				logger.logsf(s, args);
			}
		}

		public void logs(Object s) {
			for (LogInterface logger : loggers) {
				logger.logs(s);
			}
		}

		public void logss(Object s) {
			for (LogInterface logger : loggers) {
				logger.logss(s);
			}
		}

		public void startTrack(Object s) {
			for (LogInterface logger : loggers) {
				logger.startTrack(s);
			}
		}

		public void startTrack(Object s, boolean printAllLines) {
			for (LogInterface logger : loggers) {
				logger.startTrack(s, printAllLines);
			}
		}

		public void endTrack() {
			for (LogInterface logger : loggers) {
				logger.endTrack();
			}
		}

		public void dbg(Object s) {
			for (LogInterface logger : loggers) {
				logger.dbg(s);
			}
		}

		public void err(Object s) {
			for (LogInterface logger : loggers) {
				logger.err(s);
			}
		}

		public void errf(String s, Object... args) {
			for (LogInterface logger : loggers) {
				logger.errf(s, args);
			}
		}

		public void warn(Object s) {
			for (LogInterface logger : loggers) {
				logger.warn(s);
			}
		}

		public void warnf(String string, Object... args) {
			for (LogInterface logger : loggers) {
				logger.warnf(string, args);
			}
		}

		public void logssf(String string, Object... args) {
			for (LogInterface logger : loggers) {
				logger.logssf(string, args);
			}
		}
	}

	public synchronized static void setGlobalLogger(LogInterface logger) {
		instance = logger;
	}

	public synchronized static LogInterface getGlobalLogger() {
		return instance;
	}

	private static LogInterface instance = new SystemLogger();

	public static LogInterface i() {
		return instance;
	}

	public static void setFig() {
		setLogger(new FigLogger());
	}

	public static void setLogger(LogInterface i) {
		instance = i;
	}

	public static void logs(Object s) {
		i().logs(s);
	}

	// Static Logger Methods
	public static void logsf(String s, Object... args) {
		i().logsf(s, args);
	}

	public static void logss(Object s) {
		i().logss(s);
	}

	public static void startTrackf(String s, Object... args) {
		i().startTrack(String.format(s, args));
	}

	public static void startTrack(Object s, boolean printAllLines) {
		i().startTrack(s, printAllLines);
	}

	public static void startTrack(Object s) {
		i().startTrack(s, false);
	}

	public static void endTrack() {
		i().endTrack();
	}

	public static void dbg(Object s) {
		i().dbg(s);
	}

	public static void err(Object s) {
		i().err(s);
	}

	public static void errf(String s, Object... args) {
		i().errf(s, args);
	}

	public static void warn(Object s) {
		i().warn(s);
	}

	public static void warnf(String string, Object... args) {
		i().warnf(string, args);
	}

	public static void logssf(String string, Object... args) {
		i().logssf(string, args);
	}

	public static void track(String s, Fn<Void, Void> fn) {
		Logger.startTrack(s);
		fn.apply(null);
		Logger.endTrack();
	}
}
