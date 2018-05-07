package edu.utah.ece.async.Verilog2LPN;

/**
 * Hello world!
 *
 */
public class CLI {
    public static void main( String[] args ) {
    	CompilationOptions options;
    	
    	try {
    		options = new CompilationOptions(args);
    	} catch (CompilationOptionsException e) {
    		e.printStackTrace();
    		return;
    	}
    	
    	Compiler compiler = new Compiler(options);
    }
}
