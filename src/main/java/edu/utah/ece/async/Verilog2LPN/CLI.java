package edu.utah.ece.async.Verilog2LPN;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;

import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser;


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
    	compiler.compile();
    }
}
