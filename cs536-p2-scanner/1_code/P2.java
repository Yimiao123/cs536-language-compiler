import java.util.*;
import java.io.*;
import java_cup.runtime.*;  // defines Symbol

/**
 * This program is to be used to test the Wumbo scanner.
 * This version is set up to test all tokens, but you are required to test 
 * other aspects of the scanner (e.g., input that causes errors, character 
 * numbers, values associated with tokens)
 */
public class P2 {
    public static void main(String[] args) throws IOException {
                                           // exception may be thrown by yylex
        // test all tokens
        testAllTokens();
        CharNum.num = 1;
    
        // ADD CALLS TO OTHER TEST METHODS HERE
    }

    /**
     * testAllTokens
     *
     * Open and read from file allTokens.txt
     * For each token read, write the corresponding string to allTokens.out
     * If the input file contains all tokens, one per line, we can verify
     * correctness of the scanner by comparing the input and output files
     * (e.g., using a 'diff' command).
     */
    private static void testAllTokens() throws IOException {
        // open input and output files
        FileReader inFile = null;
        PrintWriter outFile = null;
        try {
            inFile = new FileReader("allTokens.in");
            outFile = new PrintWriter(new FileWriter("allTokens.out"));
        } catch (FileNotFoundException ex) {
            System.err.println("File allTokens.in not found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.println("allTokens.out cannot be opened.");
            System.exit(-1);
        }
        

        // create and call the scanner
        Yylex scanner = new Yylex(inFile);
        Symbol token = scanner.next_token();
        while (token.sym != sym.EOF) {
            switch (token.sym) {
            case sym.BOOL:
            	outFile.println("BOOL"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.INT:
            	outFile.println("INT"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.VOID:
                outFile.println("VOID"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.TRUE:
                outFile.println("TRUE"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum); 
                break;
            case sym.FALSE:
                outFile.println("FALSE"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum); 
                break;
            case sym.STRUCT:
                outFile.println("STRUCT"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum); 
                break;
            case sym.CIN:
                outFile.println("CIN"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum); 
                break;
            case sym.COUT:
                outFile.println("COUT"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;				
            case sym.IF:
                outFile.println("IF"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.ELSE:
                outFile.println("ELSE"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.WHILE:
                outFile.println("WHILE"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.RETURN:
                outFile.println("RETURN"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.ID:
                outFile.println("ID"+" "+((IdTokenVal)token.value).linenum+" "+((IdTokenVal)token.value).charnum+" "+((IdTokenVal)token.value).idVal);
                break;
            case sym.INTLITERAL:  
                outFile.println("INTLITERAL"+" "+((IntLitTokenVal)token.value).linenum+" "+((IntLitTokenVal)token.value).charnum+" "+((IntLitTokenVal)token.value).intVal);
                break;
            case sym.STRINGLITERAL: 
                outFile.println("STRINGLITERAL"+" "+((StrLitTokenVal)token.value).linenum+" "+((StrLitTokenVal)token.value).charnum+" "+((StrLitTokenVal)token.value).strVal);
                break;    
            case sym.LCURLY:
                outFile.println("LCURLY"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.RCURLY:
                outFile.println("RCURLY"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.LPAREN:
                outFile.println("LPAREN"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.RPAREN:
                outFile.println("RPAREN"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.SEMICOLON:
                outFile.println("SEMICOLON"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.COMMA:
                outFile.println("COMMA"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.DOT:
                outFile.println("DOT"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.WRITE:
                outFile.println("WRITE"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.READ:
                outFile.println("READ"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;				
            case sym.PLUSPLUS:
                outFile.println("PLUSPLUS"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.MINUSMINUS:
                outFile.println("MINUSMINUS"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;	
            case sym.PLUS:
                outFile.println("PLUS"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.MINUS:
                outFile.println("MINUS"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.TIMES:
                outFile.println("TIMES"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.DIVIDE:
                outFile.println("DIVIDE"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.NOT:
                outFile.println("NOT"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.AND:
                outFile.println("AND"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.OR:
                outFile.println("OR"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.EQUALS:
                outFile.println("EQUALS"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.NOTEQUALS:
                outFile.println("NOTEQUALS"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.LESS:
                outFile.println("LESS"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.GREATER:
                outFile.println("GREATER"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.LESSEQ:
                outFile.println("LESSEQ"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
            case sym.GREATEREQ:
                outFile.println("GREATEREQ"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
			case sym.ASSIGN:
                outFile.println("ASSIGN"+" "+((TokenVal)token.value).linenum+" "+((TokenVal)token.value).charnum);
                break;
			default:
				outFile.println("UNKNOWN TOKEN");
            } // end switch

            token = scanner.next_token();
        } // end while
        outFile.close();
    }
}

