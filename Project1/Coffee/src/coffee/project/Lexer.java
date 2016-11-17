package coffee.project;

import coffee.IdentifierList;
import coffee.REPL;
import coffee.TokenList;
import coffee.datatypes.*;
import coffee.exceptions.InvalidTokenException;
import coffee.syntax.Keywords;
import coffee.syntax.Operators;

/**
 * Created by ft on 10/14/15.
 */
public class Lexer implements REPL.LineInputCallback {
    @Override
    public String lineInput(String line) {

        try {
            String[] splits = line.split(" ");

            for (String itr : splits) {
                //System.out.println("Parse :" + itr);
                parseToken(itr);
            }
            System.out.println("--------------------------");
            return "successful";
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            return e.getMessage();
        }
    }

    private Token getKeyword(String str){
        Token token = null;

        if(str.equals(Keywords.AND) || str.equals(Keywords.APPEND) || str.equals(Keywords.CONCAT) ||
                str.equals(Keywords.DEFFUN) || str.equals(Keywords.ELSE) || str.equals(Keywords.EQUAL) ||
                str.equals(Keywords.FOR) || str.equals(Keywords.NOT) || str.equals(Keywords.THEN) ||
                str.equals(Keywords.OR) || str.equals(Keywords.SET) || str.equals(Keywords.WHILE) ||
                str.equals(Keywords.IF) || str.equals(Keywords.NULL))
            token = new Keyword(str);

        else if(str.equals(Keywords.TRUE))
            token = new ValueBinary(true);
        else if(str.equals(Keywords.FALSE))
            token = new ValueBinary(false);

        else token = new Identifier(str);

        return token;
    }

    /**
     * Bir kelime alır id,keyword bakımından kontrol eder
     * [a-zA-Z] aralığını kabul eder
     * Bulduğu tokeni listeye ekler.
     *
     * @param word  analiz edilecek kelime
     * @param index analizin baslayacagi index
     * @return aramanın kaldığı yerden indexi return eder
     * example : asd)) gelirse asd yi token olarak ekler ve 3 return eder
     * a5x gibi hatalı durumda exception fırlatır
     */
    private int analyzeWords(String word, int index) throws InvalidTokenException {
        StringBuilder sb = new StringBuilder();
        int nextIndex = index;
        sb.append(word.charAt(index));
        //System.out.println("sb:"+sb.toString());
        for (int j = index+1; j < word.length(); ++j) { // ilk karakter zaten check edildi
            if (Character.isLetter(word.charAt(j))) { // check [a-zA-Z] range
                sb.append(word.charAt(j));
                nextIndex = j;
            } else if (word.charAt(j) == Operators.RIGHT_PARENTHESIS.charAt(0)) {
                // kelimelerden sonra sadece ) gelebilir
                break;
            } else throw new InvalidTokenException(word, nextIndex);
        }
        Token token = getKeyword(sb.toString()); // get token and add list
        if(token!=null){
            System.out.println(token);
            if(token instanceof Identifier)
                IdentifierList.getInstance().addIdentifier(((Identifier) token).getName());
            TokenList.getInstance().addToken(token);
        }
        return nextIndex;
    }

    private int analyzeNumbers(String word, int index) throws InvalidTokenException {
        StringBuilder sb = new StringBuilder();
        int nextIndex = index; // indexi hemen arttirki tek basamakta direk devam etsin
        sb.append(word.charAt(index));
        for (int j = index+1; j < word.length(); ++j) { // sonraki sayilari oku
            if (Character.isDigit(word.charAt(j))) { // check [0-9]+ range
                sb.append(word.charAt(j));
                nextIndex = j;
            } else if (word.charAt(j) == Operators.RIGHT_PARENTHESIS.charAt(0)) {
                // sayilardan sonra sadece ) gelmeli
                break;
            } else throw new InvalidTokenException(word, nextIndex);
        }

        Token token = new ValueInt(Integer.valueOf(sb.toString()));
        System.out.println(token);
        TokenList.getInstance().addToken(token);
        return nextIndex;
    }

    private void parseToken(String token) throws InvalidTokenException {
        //System.out.println("parseToken is started");

        for (int i = 0; i < token.length(); ++i) {
            String ch = String.valueOf(token.charAt(i));
            System.out.println("CH:"+ch);
            if (Character.isLetter(ch.charAt(0))) {
                i = analyzeWords(token, i); //analize kaldıgı yerden devam etmesi icin indxi guncelle
            } else if (ch.equals(Operators.MINUS)) { // if starts with minus
                if (i == token.length() - 1) { // - den sonra item yoksa operator olarak ekle
                    TokenList.getInstance().addToken(new Operator(ch.toString()));
                } else {
                    i = analyzeNumbers(token, i);
                }
            } else if (Character.isDigit(ch.charAt(0))) { // number check;
                i = analyzeNumbers(token, i);
            } else if (ch.equals(Operators.LEFT_PARENTHESIS)) {
                TokenList.getInstance().addToken(new Operator(ch.toString()));
            } else if (ch.equals(Operators.RIGHT_PARENTHESIS)) {
                    TokenList.getInstance().addToken(new Operator(ch.toString()));
            } else if (ch.equals(Operators.PLUS) || ch.equals(Operators.ASTERISK) || ch.equals(Operators.SLASH)) {
                if (i == token.length() - 1) { // bu op. lerden sonra sadece bosluk gelebilir yani sonda olmalılar
                    TokenList.getInstance().addToken(new Operator(ch.toString()));
                } else throw new InvalidTokenException(token, i + 1);
            } else if (ch.equals(Operators.NAIL)) { // ' operatoru
                TokenList.getInstance().addToken(new Operator(ch.toString()));
            }
        }
    }
}