package com.focusit.jstack;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Measure {
	public Date date;
	public String name;
	private List<ThreadInfo> threads = new ArrayList<>();
	private Map<Long, ThreadInfo> threadMap = new HashMap<>();
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Measure [Date=" + date + " Threads: "+threads.size()+ "]:\n");
		
		for(ThreadInfo info: threads) {
			builder.append(info.toString()).append('\n');
		}
		return builder.toString();	
	}
	
	public List<ThreadInfo> getThreads(){
		return threads;
	}
	
	public void addThread(ThreadInfo info){
		threadMap.put(info.getIdentity(), info);
		threads.add(info);
	}
}
