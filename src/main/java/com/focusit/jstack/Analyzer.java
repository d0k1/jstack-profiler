package com.focusit.jstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
		System.out.println("Loaded "+measures.size()+" jstack files");
	}
	
	public void printAllMeasuresAllThreads(){
		for(Measure measure:measures){
			System.out.println(measure.toString());
		}
	}

	public void printAllMeasuresRunningThreads(){
		for(Measure measure:measures){
			StringBuilder builder = new StringBuilder("Measure [Date=" + measure.date + " Threads: "+measure.threads.size()+ "]:\n");
			
			int running = 0;
			for(ThreadInfo info: measure.threads) {
				if(info.state.equalsIgnoreCase("RUNNING")){
					builder.append(info.toString()).append('\n');
					running++;
				}
			}
			System.out.println(builder.toString());
			System.out.println("Running threads "+running+" of "+measure.threads.size()+" threads!");
		}
	}
}
