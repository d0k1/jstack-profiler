package com.focusit.jstack;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {
	
	public static void main(String[] args) throws IOException, ParseException {
		System.out.println("Start");
		Options options = new Options();
		options.addOption(new Option("d", true, "Directory where jstack file(s) located"));
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args, true);
		
		if(!cmd.hasOption('d')){
			System.err.println("Directory with jstack not provided. Use -d<Directory> option");
			System.exit(1);
		}

		Analyzer analyzer = new Analyzer();
		analyzer.readJstacks(cmd.getOptionValue('d'));
		
		analyzer.printAllMeasuresAllThreads();
	}
}
