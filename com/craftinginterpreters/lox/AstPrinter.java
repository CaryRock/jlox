package com.craftinginterpreters.lox;

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    // Not necessary at all, but does run through the (hard-coded) minimal
    // example defined inside to check if the parenthesizing is correct
    //
    // The book "won't bother showing the necessary visit methods for them
    // in AstPrinter. If you want to (and you want the Java compiler to not
    // yell at you), go ahead and add them yourself. It will come in handy in 
    // the next chapter when we start parsing Lox code in to syntax trees. Or,
    // if you don't care to maintain AstPrinter, feel free to delete it. We
    // won't ened it again."
    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                    new Token(TokenType.MINUS, "-", null, 1),
                    new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                    new Expr.Literal(45.67)));
        System.out.println(new AstPrinter().print(expression));
    }
}
