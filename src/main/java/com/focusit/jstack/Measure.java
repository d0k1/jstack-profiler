package com.focusit.jstack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Measure {
	public Date date;
	public List<ThreadInfo> threads = new ArrayList<>();
	@Override
	public String toString() {
		return "Measure [date=" + date + ", threads=" + threads + "]";
	}
	
}
