package edu.utah.ece.async.Verilog2LPN;

import edu.utah.ece.async.Verilog2LPN.Verilog2001Parser.*;
import edu.utah.ece.async.lema.verification.lpn.LPN;

import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class VerilogListener extends Verilog2001BaseListener {
    private Stack<String> currentExpression;
    private Stack<String> conditionalExpressions;
    private Stack<String> conditionalPost;
    private Stack<HashSet<String>> oldPrevious;
    private Stack<Boolean> didElse;
    WrappedLPN current;
    boolean inAlways;
    boolean parsingExpression;

    public VerilogListener(LPN lpn) {
        this.current = new WrappedLPN(lpn);
        this.inAlways = false;
        this.parsingExpression = false;
        this.currentExpression = new Stack<>();
        this.conditionalPost = new Stack<>();
        this.conditionalExpressions = new Stack<>();
        this.oldPrevious = new Stack<>();
        this.didElse = new Stack<>();
    }

    private String getExpression() {
        String expression = "";

        while(!currentExpression.isEmpty()) {
            expression = currentExpression.pop() + expression;
        }

        this.parsingExpression = false;

        return expression;
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

        this.current.createNewNet();
    }

    @Override
    public void exitAlways_construct(Always_constructContext ctx) {
        this.inAlways = false;
        this.current.closeNet();
    }

    public void enterWait_statement(Wait_statementContext ctx) {
        this.parsingExpression = true;
    }

    @Override
    public void exitWait_statement(Wait_statementContext ctx) {
        String transitionName = "wait_" + ctx.start.getLine();
        String postPlaceName = this.current.nextPlaceName();

        this.current.addTransition(transitionName);
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
    public void enterSystem_function_identifier(System_function_identifierContext ctx) {
        String functionName = ctx.getText();

        if(!functionName.equals("$random"))
            return;

        ExpressionContext expressionContext = (ExpressionContext) ctx.getParent().getParent().getParent().getParent();
        String argument = expressionContext.term(1).getText();

        Blocking_assignmentContext assignmentContext = (Blocking_assignmentContext) expressionContext.getParent();
        String lvalue = assignmentContext.variable_lvalue().hierarchical_variable_identifier().getText();

        String postPlaceName = this.current.nextPlaceName();
        this.current.lpn.addPlace(postPlaceName, false);

        int branches = Integer.parseInt(argument);

        for(int i = 0; i < branches; i++) {
            String transitionName = this.current.nextTransitionName();
            this.current.addTransition(transitionName);
            this.current.lpn.addIntAssign(transitionName, lvalue, Integer.toString(i));

            for(String prev : this.current.last) {
                this.current.lpn.addMovement(prev, transitionName);
            }

            this.current.lpn.addMovement(transitionName, postPlaceName);
        }

        this.current.last.clear();
        this.current.last.add(postPlaceName);

        this.parsingExpression = false;
        getExpression();
    }

    @Override
    public void enterVariable_assignment(Variable_assignmentContext ctx) {
        if(!this.inAlways)
            return;

        this.parsingExpression = true;
    }

    @Override
    public void exitVariable_assignment(Variable_assignmentContext ctx) {
        if(!this.inAlways && !this.parsingExpression)
            return;

        this.parsingExpression = false;

        String transitionName = "assign_" + ctx.start.getLine();
        String postPlaceName = this.current.nextPlaceName();
        String lvalue = ctx.variable_lvalue().hierarchical_variable_identifier().hierarchical_identifier().getText();

        this.current.addTransition(transitionName);
        this.current.lpn.addPlace(postPlaceName, false);
        this.current.lpn.addMovement(transitionName, postPlaceName);

        for(String prePlace : this.current.last) {
            this.current.lpn.addMovement(prePlace, transitionName);
        }

        this.current.last.clear();
        this.current.last.add(postPlaceName);
        this.current.lpn.addBoolAssign(transitionName, lvalue, getExpression());
    }

    @Override
    public void enterBlocking_assignment(Blocking_assignmentContext ctx) {
        if(!this.inAlways)
            return;

        this.parsingExpression = true;
    }

    @Override
    public void exitBlocking_assignment(Blocking_assignmentContext ctx) {
        if(!this.inAlways && !this.parsingExpression)
            return;

        this.parsingExpression = false;

        String transitionName = "assign_" + ctx.start.getLine();
        String postPlaceName = this.current.nextPlaceName();
        String lvalue = ctx.variable_lvalue().hierarchical_variable_identifier().hierarchical_identifier().getText();

        this.current.addTransition(transitionName);
        this.current.lpn.addPlace(postPlaceName, false);
        this.current.lpn.addMovement(transitionName, postPlaceName);

        for(String prePlace : this.current.last) {
            this.current.lpn.addMovement(prePlace, transitionName);
        }

        this.current.last.clear();
        this.current.last.add(postPlaceName);
        this.current.lpn.addBoolAssign(transitionName, lvalue, getExpression());
    }

    @Override
    public void enterConditional_statement(Conditional_statementContext ctx) {
        this.parsingExpression = true;

        String endPlaceName = this.current.nextPlaceName();
        this.current.lpn.addPlace(endPlaceName, false);
        this.conditionalPost.push(endPlaceName);
    }

    @Override
    public void exitConditional_statement(Conditional_statementContext ctx) {
        String post = this.conditionalPost.pop();

        if(!this.didElse.pop()) {
            String conditionalExpression = "~(" + this.conditionalExpressions.pop() + ")";
            String transitionName = "else_" + ctx.start.getLine();

            this.current.addTransition(transitionName);
            this.current.lpn.addMovement(transitionName, post);
            this.current.lpn.addEnabling(transitionName, conditionalExpression);

            for (String prev : this.current.last) {
                this.current.lpn.addMovement(prev, transitionName);
            }
        }

        this.current.last.clear();
        this.current.last.add(post);
    }

    @Override
    public void enterStatement_or_null(Statement_or_nullContext ctx) {
        if(this.conditionalPost.empty())
            return;

        if(!(ctx.getParent() instanceof Conditional_statementContext))
            return;

        String transitionName;
        String placeName;
        String conditionalExpression;

        if(this.parsingExpression) {
            this.parsingExpression = false;
            this.didElse.push(false);
            // Handle if case
            conditionalExpression = getExpression();
            placeName = this.current.nextPlaceName();
            transitionName = "if_" + ctx.start.getLine();

            this.conditionalExpressions.push(conditionalExpression);
            this.oldPrevious.push(new HashSet<>(this.current.last));
        } else {
            // Handle else case
            this.didElse.pop();
            this.didElse.push(true);
            conditionalExpression = "~(" + this.conditionalExpressions.pop() + ")";
            placeName = this.current.nextPlaceName();
            transitionName = "else_" + ctx.start.getLine();
            this.oldPrevious.push(new HashSet<>(this.current.last));
        }

        this.current.addTransition(transitionName);
        this.current.lpn.addPlace(placeName, false);
        this.current.lpn.addMovement(transitionName, placeName);
        this.current.lpn.addEnabling(transitionName, conditionalExpression);

        for (String prev : this.oldPrevious.peek()) {
            this.current.lpn.addMovement(prev, transitionName);
        }

        this.current.last.clear();
        this.current.last.add(placeName);
    }

    @Override
    public void exitStatement_or_null(Statement_or_nullContext ctx) {
        if(this.conditionalPost.empty())
            return;

        if(!(ctx.getParent() instanceof Conditional_statementContext))
            return;

        String transitionName = this.current.nextTransitionName();

        this.current.addTransition(transitionName);

        for(String place : this.current.last) {
            this.current.lpn.addMovement(place, transitionName);
            this.current.lpn.addMovement(transitionName, this.conditionalPost.peek());
        }

        this.current.last = this.oldPrevious.pop();
    }

    @Override
    public void enterHierarchical_identifier(Hierarchical_identifierContext ctx) {
        if(!this.parsingExpression)
            return;

        if(ctx.getParent().getParent() instanceof Variable_lvalueContext)
            return;

        String identifier = ctx.getText();
        this.currentExpression.push(identifier);
    }

    @Override
    public void enterBinary_operator(Binary_operatorContext ctx) {
        if(!this.parsingExpression)
            return;

        String operator = ctx.getText();

        switch(operator) {
            case "==":
                this.currentExpression.push("==");
                break;
            case "!=":
                this.currentExpression.push("!=");
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

    @Override
    public void enterUnary_operator(Unary_operatorContext ctx) {
        if(!this.parsingExpression)
            return;

        this.currentExpression.push(ctx.getText());
    }

    @Override
    public void enterNumber(NumberContext ctx) {
        if(!this.parsingExpression)
            return;

        String numberText = ctx.getText();
        String[] parts = numberText.split("'");

        if(parts.length < 2) {
            return;
        }

        String numberString = parts[1].substring(1);
        String type = parts[1].substring(0, 1);

        if(type.equals("b")) {
            int number = Integer.parseInt(numberString, 2);

            if(!this.currentExpression.isEmpty() && this.currentExpression.peek().equals("==")) {
                this.currentExpression.pop();

                if (number == 0) {
                    String prev = this.currentExpression.pop();
                    this.currentExpression.push("~" + prev);
                }
            } else if(!this.currentExpression.isEmpty() && this.currentExpression.peek().equals("!=")) {
                this.currentExpression.pop();

                if (number == 1) {
                    String prev = this.currentExpression.pop();
                    this.currentExpression.push("~" + prev);
                }
            } else {
                if(number == 0) {
                    this.currentExpression.push("false");
                } else if(number == 1) {
                    this.currentExpression.push("true");
                } else {
                    this.currentExpression.push(Integer.toString(number));
                }
            }
        }

        return;
    }

}