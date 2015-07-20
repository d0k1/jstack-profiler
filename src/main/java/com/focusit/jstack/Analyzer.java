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
import java.util.List;

import com.focusit.jstack.Converter.PushBackBufferedReader;

public class Analyzer {

	private static final String RUNNABLE = "RUNNABLE";
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

	public List<ThreadInfo> getRunnableThreadsByMeasure(Measure m){
		List<ThreadInfo> result = new ArrayList<>();
		
		for(ThreadInfo info: m.getThreads()) {
			if(info.state.equalsIgnoreCase(RUNNABLE)){
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
	
	public void printAllMeasuresStuckRunnableThreads(String filter){
		
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
}
