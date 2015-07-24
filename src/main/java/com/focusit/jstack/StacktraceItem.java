package com.focusit.jstack;

public class StacktraceItem {
	public String methodFqn;
	public String fileline;

	@Override
	public String toString() {
		return "\t\t"+methodFqn + "("+fileline + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileline == null) ? 0 : fileline.hashCode());
		result = prime * result + ((methodFqn == null) ? 0 : methodFqn.hashCode());
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
		StacktraceItem other = (StacktraceItem) obj;
		if (fileline == null) {
			if (other.fileline != null)
				return false;
		} else if (!fileline.equals(other.fileline))
			return false;
		if (methodFqn == null) {
			if (other.methodFqn != null)
				return false;
		} else if (!methodFqn.equals(other.methodFqn))
			return false;
		return true;
	}	

}
