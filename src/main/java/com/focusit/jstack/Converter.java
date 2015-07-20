package com.focusit.jstack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {
	private static final Pattern threadDaemonHeaderRegex = Pattern.compile("\\\"(.*)\\\"\\s+(daemon\\s+)?prio=(\\d+)\\s+tid=0x(\\w+)\\s+nid=0x(\\w+)\\s+(.*)\\s+\\[0x(\\w+)\\]");
	private static final Pattern theadStateRegex = Pattern.compile("\\s+java\\.lang\\.Thread\\.State\\:\\s+(\\w+).*");
	private static final Pattern waitForRegex = Pattern.compile("\\t-\\s+.*wait for\\s+\\<0x(.*)\\>\\s+\\(a\\s+(.*)\\)");
	private static final Pattern lockedObjectedsRegex = Pattern.compile("\\s+Locked ownable synchronizers\\:");
	private static final Pattern noLockedObjectsRegex = Pattern.compile("\\t-\\s+(None)");
	private static final Pattern haveLockedObjectsRegex = Pattern.compile("\\t+\\-\\s+\\<0x(.*)\\>\\s+\\(a (.*)\\)");
	private static final Pattern stacktraceRegex = Pattern.compile("\\t+at\\s+(.*)\\s?\\((.*)\\)");
	
	public Converter() {
		
	}
	
	private ThreadInfo parseThreadHead(String line, Matcher m) {
		ThreadInfo result = new ThreadInfo();
		result.name = m.group(1);
		result.daemon = m.group(2)!=null && m.group(2).trim().length()>0;
		result.prio=Integer.parseInt(m.group(3));
		result.tid=Long.parseLong(m.group(4), 16);
		result.nid=Long.parseLong(m.group(5), 16);
		result.state0 = m.group(6);
		result.conditionPointer = Long.parseLong(m.group(7), 16);
		return result;
	}
	
	private ThreadInfo parseThreadStatus(ThreadInfo info, String line, Matcher m) {
		info.state = m.group(1);
		return info;
	}
	
	private ThreadInfo parseWaitFor(ThreadInfo info, String line, Matcher m) {
		info.waitFor = Long.parseLong(m.group(1), 16);
		info.waitForDetails = m.group(2);
		return info;
	}
	
	private ThreadInfo parseStacktraceItem(ThreadInfo info, String line, Matcher m) {
		StacktraceItem item = new StacktraceItem();
		item.methodFqn = m.group(1);
		item.fileline = m.group(2);
		info.stacktrace.add(item);
		return info;
	}
	
	private ThreadInfo parseOwnedLock(ThreadInfo info, String line, Matcher m) {
		if(m.group(1).equalsIgnoreCase("none")){
			info.onwLockDetails = "None";
			info.ownLock = null;
		} else {
			info.ownLock = Long.parseLong(m.group(1), 16);
			info.onwLockDetails = m.group(2);
		}
		return info;
	}
	

	private ThreadInfo parseThread(PushBackBufferedReader br) throws IOException {
		ThreadInfo thread = null;
		String line = null;	
		
		Matcher threadHead = null;
		
		while ((line = br.readLine()) != null) {
			// Skip empty lines
			if(line.trim().length()==0)
				continue;
			
			threadHead = threadDaemonHeaderRegex.matcher(line);
			if(threadHead.matches()){
				break;
			}
		}
		
		if(line==null || !threadHead.matches()){
			return null;
		}
		
		thread = parseThreadHead(line, threadHead);
		
		line = br.readLine();
		
		Matcher stateMatcher = theadStateRegex.matcher(line);
		if(!stateMatcher.matches())
		{
			System.err.println("After thread info no thread state. Exit");
			System.exit(1);
		}
		thread = parseThreadStatus(thread, line, stateMatcher);
		
		Matcher stackMatcher = null;
		Matcher waitForMatcher = null;
		
		while ((line = br.readLine()) != null) {
			if(line.trim().length()==0)
				continue;
			
			if(stacktraceRegex.matcher(line).matches()){
				stackMatcher = stacktraceRegex.matcher(line);
				stackMatcher.matches();
				thread = parseStacktraceItem(thread, line, stackMatcher);
			} else if(waitForRegex.matcher(line).matches()){
				waitForMatcher = waitForRegex.matcher(line);
				waitForMatcher.matches();
				thread = parseWaitFor(thread, line, waitForMatcher);
			} else if(lockedObjectedsRegex.matcher(line).matches()){
				break;
			}
		}
		
		line = br.readLine();
		if(noLockedObjectsRegex.matcher(line).matches()){
			Matcher m = noLockedObjectsRegex.matcher(line);
			m.matches();
			thread = parseOwnedLock(thread, line, m);
		}
		
		if(haveLockedObjectsRegex.matcher(line).matches()){
			Matcher m = haveLockedObjectsRegex.matcher(line);
			m.matches();
			thread = parseOwnedLock(thread, line, m);
		}
		
		return thread;
	}
	
	public Measure parseJstack(PushBackBufferedReader br) throws IOException {
		
		Measure result = new Measure();
		ThreadInfo thread = parseThread(br);
		while(thread!=null){
			thread = parseThread(br);
			if(thread==null){
				break;
			}
			result.threads.add(thread);
		}
		return result;
	}

	/**
	 * Class to make java's buffered readed possibility to push back just read line.
	 * @author doki
	 *
	 */
	static public class PushBackBufferedReader extends BufferedReader {

		private volatile String prevLine = null;
		private volatile boolean pushedBack = false;
		
		public PushBackBufferedReader(Reader in) {
			super(in);
		}

		@Override
		public String readLine() throws IOException {
			
			if(pushedBack){
				pushedBack = false;
				return prevLine;
			}
			
			prevLine = super.readLine(); 
			return prevLine;
		}
		
		public void pushBack(){
			pushedBack = true;
		}
	}
}
