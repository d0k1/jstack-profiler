package com.focusit.jstack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Measure {
	public Date date;
	public List<ThreadInfo> threads = new ArrayList<>();
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Measure [Date=" + date + " Threads: "+threads.size()+ "]:\n");
		
		for(ThreadInfo info: threads) {
			builder.append(info.toString()).append('\n');
		}
		return builder.toString();	
	}
}
