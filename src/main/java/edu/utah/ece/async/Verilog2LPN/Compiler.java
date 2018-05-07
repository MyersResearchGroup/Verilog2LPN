package edu.utah.ece.async.Verilog2LPN;

import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser.Module_declarationContext;
import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser.Source_textContext;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
	private CompilationOptions options;
	private List<Module_declarationContext> modules;
	private LPN lpn;

	/**
	 * Constructor for the Compiler class
	 * @param options the configuration for the compiler
	 */
	public Compiler(CompilationOptions options) {
		this.modules = new ArrayList<>();
		this.options = options;
		this.lpn = new LPN();

		for(File file : options.getFiles()) {
			parseFile(file);
		}
	}


	private void parseFile(File file) {
		InputStream inputStream;
		
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			Lexer lexer = new Verilog2001Lexer(CharStreams.fromStream(inputStream));
			TokenStream tokenStream = new CommonTokenStream(lexer);
			Verilog2001Parser parser = new Verilog2001Parser(tokenStream);
			
			Source_textContext source = parser.source_text();

			VerilogListener vl = new VerilogListener(this.lpn);
            ParseTreeWalker.DEFAULT.walk(vl, source);

            this.lpn.save("result.lpn");

			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
