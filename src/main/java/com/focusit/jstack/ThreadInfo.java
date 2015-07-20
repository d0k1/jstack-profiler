package com.focusit.jstack;

import java.util.ArrayList;
import java.util.List;

public class ThreadInfo {
	public String name;
	public boolean daemon;
	public int prio;
	public long tid;
	public long nid;
	public long conditionPointer;
	public Long ownLock;
	public String onwLockDetails;
	public String state;
	public long waitFor;
	public String waitForDetails;
	public List<StacktraceItem> stacktrace = new ArrayList<>();
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("\tThread [name=" + name + ", daemon=" + daemon + ", prio=" + prio + ", tid=" + tid + ", nid=" + nid
				+ ", conditionPointer=" + conditionPointer + ", ownLock=" + ownLock + ", onwLockDetails="
				+ onwLockDetails + ", state=" + state + ", waitFor=" + waitFor + ", waitForDetails=" + waitForDetails
				+ "]:");
		builder.append("\n");
		for(StacktraceItem item:stacktrace) {
			builder.append(item.toString()).append('\n');
		}
		
		return builder.toString();
	}
}
