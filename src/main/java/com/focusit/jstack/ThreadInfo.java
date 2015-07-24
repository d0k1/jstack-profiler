package com.focusit.jstack;

import java.util.ArrayList;
import java.util.List;

public class ThreadInfo {
	public String name;
	public boolean daemon;
	public int number;
	public int os_prio;
	public int prio;
	public long tid;
	public long nid;
	public long conditionPointer;
	public Long ownLock;
	public String onwLockDetails;
	public String state;
	public long waitFor;
	public String waitForDetails;
	public String state0;
	public Measure measure;
	public List<StacktraceItem> stacktrace = new ArrayList<>();
	
	public long filteredTo = -1;
	public static final String RUNNABLE = "RUNNABLE";

	public long getIdentity(){
		return nid;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("ThreadInfo [name=" + name + ", daemon=" + daemon + ", number=" + number + ", os_prio=" + os_prio
				+ ", prio=" + prio + ", tid=" + tid + ", nid=" + nid + ", conditionPointer=" + conditionPointer
				+ ", ownLock=" + ownLock + ", onwLockDetails=" + onwLockDetails + ", state=" + state + ", waitFor="
				+ waitFor + ", waitForDetails=" + waitForDetails + ", state0=" + state0 + "]:");
		
		builder.append("\n");
		int pos = 0;
		for(StacktraceItem item:stacktrace) {
			if(filteredTo>=0 && filteredTo>=pos){
				builder.append(item.toString()).append('\n');
				pos++;
			} else {
				break;
			}
		}
		return builder.toString();
	}

	public boolean isRunnable(){
		return state.equalsIgnoreCase(RUNNABLE);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (conditionPointer ^ (conditionPointer >>> 32));
		result = prime * result + (daemon ? 1231 : 1237);
		result = prime * result + (int) (filteredTo ^ (filteredTo >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (int) (nid ^ (nid >>> 32));
		result = prime * result + ((onwLockDetails == null) ? 0 : onwLockDetails.hashCode());
		result = prime * result + ((ownLock == null) ? 0 : ownLock.hashCode());
		result = prime * result + prio;
		result = prime * result + ((stacktrace == null) ? 0 : stacktrace.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((state0 == null) ? 0 : state0.hashCode());
		result = prime * result + (int) (tid ^ (tid >>> 32));
		result = prime * result + (int) (waitFor ^ (waitFor >>> 32));
		result = prime * result + ((waitForDetails == null) ? 0 : waitForDetails.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThreadInfo other = (ThreadInfo) obj;
		if (conditionPointer != other.conditionPointer)
			return false;
		if (daemon != other.daemon)
			return false;
		if (filteredTo != other.filteredTo)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nid != other.nid)
			return false;
		if (onwLockDetails == null) {
			if (other.onwLockDetails != null)
				return false;
		} else if (!onwLockDetails.equals(other.onwLockDetails))
			return false;
		if (ownLock == null) {
			if (other.ownLock != null)
				return false;
		} else if (!ownLock.equals(other.ownLock))
			return false;
		if (prio != other.prio)
			return false;
		if (stacktrace == null) {
			if (other.stacktrace != null)
				return false;
		} else if (!stacktrace.equals(other.stacktrace))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (state0 == null) {
			if (other.state0 != null)
				return false;
		} else if (!state0.equals(other.state0))
			return false;
		if (tid != other.tid)
			return false;
		if (waitFor != other.waitFor)
			return false;
		if (waitForDetails == null) {
			if (other.waitForDetails != null)
				return false;
		} else if (!waitForDetails.equals(other.waitForDetails))
			return false;
		return true;
	}
	
	
}
