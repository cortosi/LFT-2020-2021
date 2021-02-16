package com.company;

import java.io.*;
import java.util.Stack;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    private Stack<Integer> multiple_value = new Stack<>();

    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count = 0;

    public Translator(Lexer l, BufferedReader br) {
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

    public void prog() {
        switch (look.tag) {
            case '=', Tag.PRINT, Tag.READ, Tag.COND, Tag.WHILE, '{':
                statlist();
                match(Tag.EOF);
                try {
                    code.toJasmin();
                } catch (java.io.IOException e) {
                    System.out.println("IO error\n");
                }
                break;
            default:
                error("prog error");
        }
    }

    private void statlist() {
        switch (look.tag) {
            case '=', Tag.PRINT, Tag.READ, Tag.COND, Tag.WHILE, '{':
                stat();
                statlistp();
                break;
            default:
                error("statlist error");
        }
    }

    private void statlistp() {
        switch (look.tag) {
            case ';':
                match(';');
                stat();
                statlistp();
                break;
            case Tag.EOF, '}':
                break;
        }
    }

    public void stat() {
        switch (look.tag) {
            case '=': {
                match('=');
                int id_addr = st.lookupAddress(((Word) look).lexeme);
                if (id_addr == -1) {
                    id_addr = count;
                    st.insert(((Word) look).lexeme, count++);
                }
                match(Tag.ID);
                expr();
                code.emit(OpCode.istore, id_addr);
                break;
            }
            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');
                exprlist(OpCode.invokestatic);
                match(')');
                break;
            case Tag.READ:
                match(Tag.READ);
                match('(');
                if (look.tag == Tag.ID) {
                    int id_addr = st.lookupAddress(((Word) look).lexeme);
                    if (id_addr == -1) {
                        id_addr = count;
                        st.insert(((Word) look).lexeme, count++);
                    }
                    match(Tag.ID);
                    match(')');
                    code.emit(OpCode.invokestatic, 0);
                    code.emit(OpCode.istore, id_addr);
                } else
                    error("Error in grammar (stat) after read( with " + look);
                break;
            case Tag.COND:
                int cond_next = code.newLabel();
                match(Tag.COND);
                whenlist(cond_next);
                match(Tag.ELSE);
                stat();
                code.emitLabel(cond_next);
                break;
            case Tag.WHILE: {
                match(Tag.WHILE);
                match('(');
                int while_true = code.newLabel();
                int while_false = code.newLabel();
                int stat_next = code.newLabel();
                code.emitLabel(stat_next);
                bexpr(while_true, while_false);
                match(')');
                code.emitLabel(while_true);
                stat();
                code.emit(OpCode.GOto, stat_next);
                code.emitLabel(while_false);
                break;
            }
            case '{':
                match('{');
                statlist();
                match('}');
                break;
        }
    }

    private void whenlist(int when_next) {
        switch (look.tag) {
            case Tag.WHEN:
                whenitem(when_next);
                whenlistp(when_next);
                break;
            default:
                error("whenlist error");
        }
    }

    private void whenlistp(int when_next) {
        switch (look.tag) {
            case Tag.WHEN:
                whenitem(when_next);
                whenlistp(when_next);
                break;
            case Tag.ELSE:
                break;
            default:
                error("whenlist error");
        }
    }

    private void whenitem(int when_next) {
        switch (look.tag) {
            case Tag.WHEN:
                match(Tag.WHEN);
                match('(');
                int when_true = code.newLabel();
                int when_false = code.newLabel();
                bexpr(when_true, when_false);
                match(')');
                match(Tag.DO);
                code.emitLabel(when_true);
                stat();
                code.emit(OpCode.GOto, when_next);
                code.emitLabel(when_false);
                break;
            default:
                error("whenlist error");
        }
    }

    private void bexpr(int bexpr_true, int bexpr_next) {
        switch (look.tag) {
            case Tag.RELOP:
                String relop = ((Word) look).lexeme;
                match(Tag.RELOP);
                expr();
                expr();
                switch (relop) {
                    case ">":
                        code.emit(OpCode.if_icmpgt, bexpr_true);
                        break;
                    case "<":
                        code.emit(OpCode.if_icmplt, bexpr_true);
                        break;
                    case "==":
                        code.emit(OpCode.if_icmpeq, bexpr_true);
                        break;
                    case ">=":
                        code.emit(OpCode.if_icmpge, bexpr_true);
                        break;
                    case "<=":
                        code.emit(OpCode.if_icmple, bexpr_true);
                        break;
                    case "<>":
                        code.emit(OpCode.if_icmpne, bexpr_true);
                        break;
                }
                break;
        }
        code.emit(OpCode.GOto, bexpr_next);
    }

    private void expr() {
        switch (look.tag) {
            case '+':
                match('+');
                match('(');
                exprlist(OpCode.iadd);
                match(')');
                break;
            case '*':
                match('*');
                match('(');
                exprlist(OpCode.imul);
                match(')');
                break;
            case '-':
                match('-');
                expr();
                expr();
                code.emit(OpCode.isub);
                break;
            case '/':
                match('/');
                expr();
                expr();
                code.emit(OpCode.idiv);
                break;
            case Tag.NUM: {
                code.emit(OpCode.ldc, ((NumberTok) look).n);
                match(Tag.NUM);
                break;
            }
            case Tag.ID:
                code.emit(OpCode.iload, st.lookupAddress(((Word) look).lexeme));
                match(Tag.ID);
                break;
            default:
                error("expr error");
        }
    }

    private void exprlist(OpCode opcode) {
        switch (look.tag) {
            case '+', '-', '*', '/', Tag.NUM, Tag.ID:
                expr();
                if(opcode == OpCode.invokestatic){
                    code.emit(OpCode.invokestatic, 1);
                }
                exprlistp(opcode);
                break;
            default:
                error("exprlist error");
        }
    }

    private void exprlistp(OpCode opcode) {
        switch (look.tag) {
            case '+', '-', '*', '/', Tag.NUM, Tag.ID:
                expr();
                if(opcode == OpCode.invokestatic){
                    code.emit(OpCode.invokestatic, 1);
                }else {
                    code.emit(opcode);
                }
                exprlistp(opcode);
                break;
            case ')':
                break;
            default:
                error("exprlistp error");
        }
    }


    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "/Users/vitto/Desktop/input.lft"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

