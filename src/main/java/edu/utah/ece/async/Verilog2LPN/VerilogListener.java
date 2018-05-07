package edu.utah.ece.async.Verilog2LPN;

import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser.*;
import edu.utah.ece.async.lema.verification.lpn.LPN;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class VerilogListener extends Verilog2001BaseListener {
    private HashMap<String, LPN> nets;
    private Stack<String> currentExpression;
    WrappedLPN current;
    String currentName;
    boolean inAlways;
    int place;

    public VerilogListener(HashMap<String, LPN> nets) {
        this.nets = nets;
        this.inAlways = false;
        this.place = 0;
        this.currentExpression = new Stack<>();
    }

    private String nextPlaceName() {
        String placeName = "P" + this.place;
        this.place++;
        return placeName;
    }

    private String getExpression() {
        String expression = "";

        while(!currentExpression.isEmpty()) {
            expression = currentExpression.pop() + expression;
        }

        return expression;
    }

    @Override
    public void enterModule_declaration(Module_declarationContext ctx) {
        String identifierString = ctx.module_identifier().identifier().Simple_identifier().toString();

        this.currentName = identifierString;
        this.current = new WrappedLPN(nextPlaceName());
    }

    @Override
    public void exitModule_declaration(Module_declarationContext ctx) {
        this.nets.put(currentName, current.lpn);
        this.currentName = null;
        this.current = null;
    }

    @Override
    public void enterInput_declaration(Input_declarationContext ctx) {
        List<Port_identifierContext> portNames = ctx.list_of_port_identifiers().port_identifier();

        for(Port_identifierContext portNameCtx : portNames) {
            String portName = portNameCtx.identifier().Simple_identifier().toString();
            this.current.lpn.addInput(portName, "false");
        }
    }

    @Override
    public void enterOutput_declaration(Output_declarationContext ctx) {
        List<Port_identifierContext> portNames = ctx.list_of_port_identifiers().port_identifier();

        for(Port_identifierContext portNameCtx : portNames) {
            String portName = portNameCtx.identifier().Simple_identifier().toString();
            this.current.lpn.addOutput(portName, "false");
        }
    }

    @Override
    public void enterReg_declaration(Reg_declarationContext ctx) {
        List<Variable_typeContext> typeContexts = ctx.list_of_variable_identifiers().variable_type();

        for(Variable_typeContext typeContext : typeContexts) {
            String variableName = typeContext.variable_identifier().identifier().Simple_identifier().toString();
            this.current.lpn.addBoolean(variableName, "false");
        }
    }

    @Override
    public void enterAlways_construct(Always_constructContext ctx) {
        this.inAlways = true;
    }

    @Override
    public void exitAlways_construct(Always_constructContext ctx) {
        this.inAlways = false;
    }

    @Override
    public void exitWait_statement(Wait_statementContext ctx) {
        String transitionName = "wait_" + ctx.start.getLine();
        String postPlaceName = nextPlaceName();

        this.current.lpn.addTransition(transitionName);
        this.current.lpn.addPlace(postPlaceName, false);
        this.current.lpn.addMovement(transitionName, postPlaceName);

        for(String prePlace : this.current.last) {
            this.current.lpn.addMovement(prePlace, transitionName);
        }

        this.current.last.clear();
        this.current.last.add(postPlaceName);

        String LPNExpression = getExpression();
        this.current.lpn.addEnabling(transitionName, LPNExpression);
    }

    @Override
    public void enterHierarchical_identifier(Hierarchical_identifierContext ctx) {
        String identifier = ctx.getText();
        this.currentExpression.push(identifier);
    }

    @Override
    public void enterBinary_operator(Binary_operatorContext ctx) {
        String operator = ctx.getText();

        switch(operator) {
            case "==":
                this.currentExpression.push("==");
                break;
            case "||":
                this.currentExpression.push("|");
                break;
            case "&&":
                this.currentExpression.push("&");
                break;
            default:
                System.err.println("Unexpected binary operator found: \"" + operator + "\"");
        }
    }

}