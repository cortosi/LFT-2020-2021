package com.company;

import java.io.*;
import java.util.*;

public class Lexer {

    public static int line = 1;
    private char peek = ' ';


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
                        peek = ' ';
                    } else {
                        System.err.println("Erroneous character"
                                + " after * : " + peek);
                        return null;
                    }
                } else if (peek == '/') {
                    while (peek != '\n' && peek != (char) -1) {
                        readch(br);
                    }
                } else {
                    return Token.div;
                }
            }
            if (peek == '\n') {
                line++;
            }
            readch(br);
        }

        switch (peek) {
            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;

            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;

            case (char) -1:
                return new Token(Tag.EOF);

            default:
                if (Character.isDigit(peek)) {
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
}
