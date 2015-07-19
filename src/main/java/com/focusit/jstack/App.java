package com.focusit.jstack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import com.focusit.jstack.Converter.PushBackBufferedReader;

public class App {

	public static void main(String[] args) throws IOException {
		System.out.println("Start");
		
		File stack = new File("jstack__20151707_171800.txt");
		Date stackDate = new Date(stack.lastModified());
		
		PushBackBufferedReader br = new PushBackBufferedReader(new FileReader(stack));
		
		br.readLine();
		br.readLine();
		br.readLine();
		
		Converter converter = new Converter();
		Measure measure = converter.parseJstack(br);
		measure.date = stackDate;
		
		System.out.println(measure.toString());
		System.out.println(measure.threads.size());
	}
}
