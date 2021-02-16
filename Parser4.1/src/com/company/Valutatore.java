package com.company;

import java.awt.font.NumericShaper;
import java.io.*;

public class Valutatore {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Valutatore(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF) move();
        } else error("syntax error");
    }

    // <START> ::= <EXPR> EOF { print(EXPR.val) }
    // GUIDA(<START> ::= <EXPR> EOF) = { (, NUM }
    public void start() {
        switch (look.tag) {
            case '(', Tag.NUM: {
                int expr_val = expr();
                match(Tag.EOF);
                System.out.println("Risultato: " + expr_val);
                break;
            }
            default:
                error("start error");
        }
    }

    // <EXPR> ::= <TERM> { EXPRP.i = TERM.val } <EXPRP> { EXPR.val = EXPRP.val }
    // GUIDA(<EXPR> ::= <TERM> <EXPRP>) = { (, NUM }
    private int expr() {
        int term_val, exprp_val = 0;
        switch (look.tag) {
            case '(', Tag.NUM: {
                term_val = term();
                exprp_val = exprp(term_val);
                break;
            }
            default:
                error("expr error");
        }
        return exprp_val;
    }

    // <EXPRP> ::= + <TERM> { EXPRP1.i = EXPRP.i + TERM.val} <EXPRP1> { EXPRP.val = EXPRP1.val }
    // GUIDA(<EXPRP> ::= + <TERM> <EXPRP>) = { + }
    // <EXPRP> ::= - <TERM> { EXPRP1.i = EXPRP.i - TERM.val} <EXPRP> { EXPRP.val = EXPRP1.val }
    // GUIDA(<EXPRP> ::= - <TERM> <EXPRP>) = { - }
    // <EXPRP> ::= eps { EXPRP.val = EXPPR.i }
    // GUIDA(<EXPRP> ::= eps) = { EOF, ) }
    private int exprp(int exprp_i) {
        int term_val, exprp_val = 0;
        switch (look.tag) {
            case '+': {
                match('+');
                term_val = term();
                exprp_val = exprp(exprp_i + term_val);
                break;
            }
            case '-': {
                match('-');
                term_val = term();
                exprp_val = exprp(exprp_i - term_val);
                break;
            }
            case Tag.EOF, ')':
                exprp_val = exprp_i;
                break;
            default:
                error("exprp error");
        }
        return exprp_val;
    }

    // <TERM> ::= <FACT> { TERMP.i = FACT.val } <TERMP> { TERM.val = TERMP.val }
    // GUIDA(<TERM> ::= <FACT> <TERMP>) = { (, NUM }
    private int term() {
        int fact_val, termp_val = 0;
        switch (look.tag) {
            case '(', Tag.NUM: {
                fact_val = fact();
                termp_val = termp(fact_val);
                break;
            }
            default:
                error("term error");
        }
        return termp_val;
    }

    // <TERMP> ::= * <FACT> { TERMP1.i = TERMP.i * FACT.val } <TERMP1> { TERMP.val = TERMP1.val }
    // GUIDA(<TERM> ::= <FACT> <TERMP>) = { * }
    // <TERMP> ::= / <FACT> { TERMP1.i = TERMP.i / FACT.val } <TERMP1> { TERMP.val = TERMP1.val }
    // GUIDA(<TERM> ::= <FACT> <TERMP>) = { / }
    // <TERMP> ::= eps> { TERMP.val = TERMP.i }
    // GUIDA(<TERMP> ::= eps) = { +, -, EOF, ) }
    private int termp(int termp_i) {
        int fact_val, termp1_val = 0;
        switch (look.tag) {
            case '*': {
                match('*');
                fact_val = fact();
                termp1_val = termp(fact_val * termp_i);
                break;
            }
            case '/': {
                match('/');
                fact_val = fact();
                termp1_val = termp(termp_i / fact_val);
                break;
            }
            case '+', '-', ')', Tag.EOF:
                termp1_val = termp_i;
                break;
            default:
                error("termp error");
        }
        return termp1_val;
    }

    // <FACT> ::= (<EXPR>) { FACT.val = EXPR.val }
    // GUIDA(<FACT> ::= (<EXPR>)) = { ( }
    // <FACT> ::= NUM { FACT.val = NUM.value }
    // GUIDA(<FACT> ::= NUM) = { NUM }
    private int fact() {
        int fact_val = 0;
        switch (look.tag) {
            case '(': {
                match('(');
                fact_val = expr();
                match(')');
                break;
            }
            case Tag.NUM:
                fact_val = ((NumberTok) look).n;
                match(look.tag);
                break;
            default:
                error("fact error");
        }
        return fact_val;
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:\\Users\\Vitto\\Documents\\Intellij\\LFT\\Parser4.1\\src\\com\\company\\prova.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Valutatore valutatore = new Valutatore(lex, br);
            valutatore.start();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
