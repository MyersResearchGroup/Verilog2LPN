package edu.utah.ece.async.Verilog2LPNTest;

import edu.utah.ece.async.Verilog2LPN.CompilationOptions;
import edu.utah.ece.async.Verilog2LPN.CompilationOptionsException;
import edu.utah.ece.async.Verilog2LPN.Compiler;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.fail;

public class CompilerTest {

    private void compilerExample(File verilogFile, File lpnFile) {
        CompilationOptions options = new CompilationOptions();
        File resultFile = new File(lpnFile.getName() + "-check");
        resultFile.deleteOnExit();

        try {
            options.addFile(verilogFile);
        } catch(CompilationOptionsException e) {
            fail();
        }

        Compiler compiler = new Compiler(options);
        compiler.compile(resultFile.getName());

        try {
            byte[] expectedLPN = Files.readAllBytes(lpnFile.toPath());
            byte[] lpn = Files.readAllBytes(resultFile.toPath());

            Assert.assertArrayEquals(expectedLPN, lpn);
        } catch(IOException e) {
            fail();
        }
    }

    @Test
    public void ParityCheckTest() {
        File verilogFile = new File(this.getClass().getResource("/parity-checker.v").getFile());
        File lpnFile = new File(this.getClass().getResource("/parity-checker.lpn").getFile());

        compilerExample(verilogFile, lpnFile);
    }

}