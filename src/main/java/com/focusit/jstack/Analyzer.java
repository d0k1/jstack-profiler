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
	
	public void printAllMeasuresAllThreads(){
		for(Measure measure:measures){
			System.out.println(measure.toString());
		}
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

	public void printAllMeasuresRunningThreads(){
		for(Measure measure:measures){
			StringBuilder builder = new StringBuilder("Measure [Date=" + measure.date + " Threads: "+measure.threads.size()+ "]:\n");
			
			int running = 0;
			for(ThreadInfo info: measure.threads) {
				if(info.state.equalsIgnoreCase("RUNNABLE")){
					builder.append(info.toString()).append('\n');
					running++;
				}
			}
			System.out.println(builder.toString());
			System.out.println("Runnable threads "+running+" of "+measure.threads.size()+" threads!");
		}
	}

	public void printAllMeasuresCountRunningThreads(){
		
		for(Measure measure:measures){
			
			int running = 0;
			for(ThreadInfo info: measure.threads) {
				if(info.state.equalsIgnoreCase("RUNNABLE")){
					running++;
				}
			}
			StringBuilder builder = new StringBuilder("Measure [Date=" + measure.date + " Threads: "+measure.threads.size()+" Runnable: ");
			builder.append(""+running);
			builder.append("]:\n");
			System.out.println(builder.toString());
		}
	}
}
