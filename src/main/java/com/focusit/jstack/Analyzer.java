package com.focusit.jstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.focusit.jstack.Converter.PushBackBufferedReader;

/**
 * This class implements various method of analyzing thread dumps.
 * It operates with measures(thread dump). Each measure has 1+ threads.
 * <br/>
 * Currently analyzer can:
 * <ul>
 * 	<li>just print everything it has</li>
 *  <li>count running threads by measures and print the data</li>
 *  <li>print only runnable threads in every measure</li>
 *  <li>print runnable threads with filtered stack(i.e. everything that happened before desired method are ignored)</li>
 *  <li>print stuck threads (a stuck thread is a thread hasn't changed status and/or its' stack during several measure)</li>
 * </ul>
 * @author doki
 *
 */
public class Analyzer {

	public List<Measure> measures = new ArrayList<>();
	
	public void readJstacks(String directory) throws IOException{
		Files.walk(Paths.get(directory)).forEach(filePath -> {
		    if (Files.isRegularFile(filePath)) {
				File stack = filePath.toFile();
				Date stackDate = new Date(stack.lastModified());
				
				PushBackBufferedReader br;
				try {
					br = new PushBackBufferedReader(new FileReader(stack));
					br.readLine();
					br.readLine();
					br.readLine();
					
					Converter converter = new Converter();
					Measure measure = converter.parseJstack(br);
					measure.date = stackDate;
					measure.name = stack.getName();
					measures.add(measure);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
		    }
		});
		sortMeasuresByDate();
		System.out.println("Loaded "+measures.size()+" jstack files");
	}
	
	private void sortMeasuresByDate(){
		Collections.sort(measures, new Comparator<Measure>(){

			@Override
			public int compare(Measure o1, Measure o2) {
				long d1 = o1.date.getTime();
				long d2 = o2.date.getTime();
				
				if(d1<d2)
					return -1;
				
				if(d1>d2)
					return 1;
				
				return 0;
			}});

	}

	private List<ThreadInfo> getRunnableThreadsByMeasure(Measure m){
		List<ThreadInfo> result = new ArrayList<>();
		
		for(ThreadInfo info: m.getThreads()) {
			if(info.state.equalsIgnoreCase(ThreadInfo.RUNNABLE)){
				result.add(info);
			}
		}
		return result;
	}
	
	public void printAllMeasuresAllThreads(){
		for(Measure measure:measures){
			System.out.println(measure.toString());
		}
	}
	
	public void printAllMeasuresRunningThreads(){
		for(Measure measure:measures){			
			List<ThreadInfo> runnable = getRunnableThreadsByMeasure(measure);
			StringBuilder builder = new StringBuilder("Measure [Date=" + measure.date + " Threads: "+measure.getThreads().size()+ " Runnable: "+runnable.size()+"]:\n");
			for(ThreadInfo info: runnable) {
					builder.append(info.toString()).append('\n');
			}
			System.out.println(builder.toString());
		}
	}

	public void printAllMeasuresCountRunningThreads(){
		for(Measure measure:measures){
			List<ThreadInfo> runnable = getRunnableThreadsByMeasure(measure);
			StringBuilder builder = new StringBuilder("Measure [Date=" + measure.date + " Threads: "+measure.getThreads().size()+ " Runnable: "+runnable.size()+"]:\n");
			System.out.println(builder.toString());
		}
	}
	
	private void addEmptyStuckInfo(Measure m, ThreadInfo t, LinkedList<StuckThreadInfo> list){
		StuckThreadInfo stuck = new StuckThreadInfo();
		stuck.threads.add(t);
		stuck.count = 1;
		stuck.closed = false;
		list.add(stuck);
	}
	
	public void printAllMeasuresStuckThreads(int stuckLimit, boolean compareStacks){
		Map<Long, LinkedList<StuckThreadInfo>> threadMeasuredCount = new HashMap<>();
		Map<Long, String> previousState = new HashMap<>();
		
		for(Measure measure:measures){
			List<ThreadInfo> threads = measure.getThreads();
			for(ThreadInfo info:threads){
				if(previousState.get(info.getIdentity())==null){
					previousState.put(info.getIdentity(), info.state);
				}
				if(previousState.get(info.getIdentity()).equalsIgnoreCase(info.state)) {
					if(threadMeasuredCount.get(info.getIdentity())==null){
						threadMeasuredCount.put(info.getIdentity(), new LinkedList<>());
						addEmptyStuckInfo(measure, info, threadMeasuredCount.get(info.getIdentity()));
					} else {
						StuckThreadInfo stuck = threadMeasuredCount.get(info.getIdentity()).getLast();
						if(stuck.closed) {
							addEmptyStuckInfo(measure, info, threadMeasuredCount.get(info.getIdentity()));	
						} else {
							if(compareStacks && stuck.threads.size()>0){
								if(stuck.threads.get(0).stacktrace.equals(info.stacktrace)){
									stuck.count++;
									stuck.threads.add(info);
								} else {
									System.err.println("Thread "+ info.name +" is not stuck because it has another stack");
								}
							} else if(!compareStacks || stuck.threads.size()==0){
								stuck.count++;
								stuck.threads.add(info);
							}
						}
					}
				}
				if (!previousState.get(info.getIdentity()).equalsIgnoreCase(info.state)) {
					if(threadMeasuredCount.get(info.getIdentity())!=null){
						StuckThreadInfo stuck = threadMeasuredCount.get(info.getIdentity()).getLast();
						stuck.closed = true;
					}
				}
				
				previousState.put(info.getIdentity(), info.state);
			}
		}
		System.out.println("Found "+threadMeasuredCount.keySet().size()+" unique threads");
		
		List<StuckThreadInfo> stucks = new ArrayList<>();
		for(Map.Entry<Long, LinkedList<StuckThreadInfo>> entry:threadMeasuredCount.entrySet()){
			LinkedList<StuckThreadInfo> stuck = entry.getValue();
			for(StuckThreadInfo info:stuck){
				stucks.add(info);
			}
		}
		
		Collections.sort(stucks, new Comparator<StuckThreadInfo>(){

			@Override
			public int compare(StuckThreadInfo o1, StuckThreadInfo o2) {
				return Integer.compare(o1.count, o2.count);
			}});
		
		for(StuckThreadInfo info:stucks){
			if(info.count>=stuckLimit){
				System.out.println("Stuck thread (found in "+info.count+" measures) "+info.threads.get(0).name);
			}
		}
	}

	public void printAllMeasuresRunnableThreadsFilteredByStackItem(String filter){

		for(Measure measure:measures){
			List<ThreadInfo> runnable = getRunnableThreadsByMeasure(measure);
			for(ThreadInfo thread:runnable){
				int position = 0;
				for(StacktraceItem item: thread.stacktrace){
					if(item.methodFqn.contains(filter)){
						thread.filteredTo = position;
					}
					position++;
				}
			}
			
			StringBuilder builder = new StringBuilder("Measure [Date=" + measure.date + " Threads: "+measure.getThreads().size()+ " Runnable: "+runnable.size()+"]:\n");
			for(ThreadInfo info: runnable) {
					builder.append(info.toString()).append('\n');
			}
			System.out.println(builder.toString());
			
			for(ThreadInfo info: runnable) {
				info.filteredTo = -1;
			}
		}		
	}
	
	class StuckThreadInfo{
		public List<ThreadInfo> threads = new ArrayList<>();
		public int count;
		public boolean closed;
	}
}
