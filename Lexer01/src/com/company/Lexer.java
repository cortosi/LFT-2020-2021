package com.company;

import java.io.*;
import java.util.*;

public class Lexer {

    public static int line = 1;
    private char peek = ' ';
    private ArrayList<Word> identifiers = new ArrayList<Word>();


    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }


    public Token lexical_scan(BufferedReader br) {
        while (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r' || peek == '/') {
            //Gestione commenti
            if (peek == '/') {
                readch(br);
                if (peek == '*') {
                    readch(br);
                    while (peek != '*') {
                        readch(br);
                        if (peek == '\n') {
                            line++;
                        }
                    }
                    readch(br);
                    if (peek == '/') {
                        readch(br);
                    } else {
                        System.err.println("Erroneous character"
                                + " after * : " + peek);
                        return null;
                    }
                } else if (peek == '/') {
                    while (peek != '\n') {
                        readch(br);
                    }
                }
            }
            if (peek == '\n') {
                line++;
            }
            readch(br);
        }

        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;
            // ... gestire i casi di (, ), {, }, +, -, *, /, ; ... //

            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;

            case '{':
                peek = ' ';
                return Token.lpg;

            case '}':
                peek = ' ';
                return Token.rpg;

            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;

            case '/':
                peek = ' ';
                return Token.div;

            case ';':
                peek = ' ';
                return Token.semicolon;

            // ... gestire i casi di ||, <, >, <=, >=, ==, <>, = ... //s
            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : " + peek);
                    return null;
                }

            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after | : " + peek);
                    return null;
                }

            case '<':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.le;
                } else if (peek == '>') {
                    peek = ' ';
                    return Word.ne;
                } else {
                    return Word.lt;
                }

            case '>':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.ge;
                } else {
                    return Word.gt;
                }

            case '=':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.eq;
                } else {
                    return Word.assign;
                }

            case (char) -1:
                return new Token(Tag.EOF);

            default:
                if (Character.isLetter(peek) || peek == '_') {
                    // ... gestire il caso degli identificatori e delle parole chiave //
                    // ([a-zA-Z] | ( _(_)*[a-zA-Z0-9])) ([a-zA-Z0-9] | _ )
                    String id = "" + peek;
                    if(peek == '_'){
                        readch(br);
                        while (peek == '_'){
                            id += peek;
                            readch(br);
                        }
                    }
                    while (Character.isDigit(peek) || Character.isLetter(peek) || peek == '_') {
                        id += peek;
                        readch(br);
                    }
                    switch (id) {
                        case "cond":
                            return Word.cond;
                        case "when":
                            return Word.when;
                        case "then":
                            return Word.then;
                        case "else":
                            return Word.elsetok;
                        case "while":
                            return Word.whiletok;
                        case "do":
                            return Word.dotok;
                        case "seq":
                            return Word.seq;
                        case "print":
                            return Word.print;
                        case "read":
                            return Word.read;
                        default:
                            identifiers.add(new Word(Tag.ID, id));
                            return new Word(Tag.ID, id);
                    }

                } else if (Character.isDigit(peek)) {
                    // ... gestire il caso dei numeri ... //
                    String number = "" + peek;
                    readch(br);
                    while (Character.isDigit(peek)) {
                        number += peek;
                        readch(br);
                    }
                    if (number.charAt(0) == '0') {
                        System.err.println("Error, " + number + " cannot be a number.");
                        return null;
                    } else {
                        return new NumberTok(Tag.NUM, Integer.parseInt(number));
                    }
                } else {
                    System.err.println("Erroneous character: "
                            + peek);
                    return null;
                }
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "C:\\Users\\Vitto\\Documents\\GitHub\\LinguaggiFormali\\Lexer01\\src\\com\\company\\prova.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
