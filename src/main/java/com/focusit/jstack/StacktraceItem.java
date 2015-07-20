package com.focusit.jstack;

public class StacktraceItem {
	public String methodFqn;
	public String fileline;

	@Override
	public String toString() {
		return "\t\t"+methodFqn + "("+fileline + ")";
	}	

}
