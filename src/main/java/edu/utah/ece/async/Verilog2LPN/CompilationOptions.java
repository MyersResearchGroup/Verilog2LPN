package edu.utah.ece.async.Verilog2LPN;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompilationOptions {
	private List<File> files;

	public CompilationOptions(String[] cmdline) throws CompilationOptionsException {
		this.files = new ArrayList<>();
		
		for(String argument : cmdline) {
			// For now, assume all arguments are files
			System.out.println(argument);
			File fileArgument = new File(argument);
			addFile(fileArgument);
		}
	}
	
	private void addFile(File file) throws CompilationOptionsException {
		if(!file.exists()) {
			throw new CompilationOptionsException();
		}
			
		if(file.isFile()) {
			files.add(file);
		}
			
		if(file.isDirectory()) {
			addDirectory(file);
		}
	}
	
	private void addDirectory(File directory) throws CompilationOptionsException {
		for(File file : directory.listFiles()) {
			addFile(file);
		}
	}
	
	public List<File> getFiles() {
		return this.files;
	}
}
