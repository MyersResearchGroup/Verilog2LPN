package edu.utah.ece.async.Verilog2LPN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser.Module_declarationContext;
import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser.Source_textContext;

public class Compiler {
	private CompilationOptions options;
	private List<Module_declarationContext> modules;
	
	public Compiler(CompilationOptions options) {
		this.modules = new ArrayList<>();
		this.options = options;
		
		for(File file : options.getFiles()) {
			parseFile(file);
		}
	}
	
	public List<String> compile() {
		// Prune out unsupported constructs
		List<String> pruned = this.pruneUnsupportedConstructs();
		return null;
	}
	
	private List<String> pruneUnsupportedConstructs() {
		return null;
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
			
			findModuleDeclarations(source);
			
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void findModuleDeclarations(ParseTree tree) {
		if(tree.getClass() == Module_declarationContext.class) {
			this.modules.add((Module_declarationContext) tree);
			return;
		}
		
		for(int i = 0; i < tree.getChildCount(); i++) {
			findModuleDeclarations(tree.getChild(i));
		}
	}
}
