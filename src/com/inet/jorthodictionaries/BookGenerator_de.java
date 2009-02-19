/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2008 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 * Created on 03.11.2005
 */
package com.inet.jorthodictionaries;


/**
 * @author Volker Berlin
 */
public class BookGenerator_de extends BookGenerator {

    @Override
    protected boolean isValidWord(String word){
        if(super.isValidWord( word )){
            return true;
        }
        return word.endsWith( "(Deklination)" );
    }
    
    @Override
    boolean isValidLanguage(String word, String wikiText) {
        if(wikiText.indexOf("{{Sprache|Deutsch}}") < 0){
            
            /*
            if(wikiText.indexOf("{{Sprache|") >= 0){
                return false;
            }
            if(wikiText.indexOf("{{Schweizer Schreibweise|") >= 0){
                return false;
            }
            if(wikiText.indexOf("{{Alte Schreibweise|") >= 0){
                return false;
            }
            if(wikiText.indexOf("{{anpassen}}") >= 0){
                return false;
            }
            if(wikiText.toUpperCase().indexOf("#REDIRECT") >= 0){
                return false;
            }*/
            int idx = wikiText.indexOf("{{Deklinationsseite Adjektiv|");
            if(idx >= 0){
                addDeklinationAdjektiv(wikiText, idx);
                // Die Deklinations sind valid, das Lema (Wort) aber nicht.
                return false;
            }
            
            return false;
        }

        int idx = wikiText.indexOf("{{Wortart|Verb}}");
        if(idx <0){
            idx = wikiText.indexOf("{{Wortart|Verb|Deutsch}}");
        }
        while(idx > 0){
            //Flextionen der Verben ermitteln
            String table = getTable( word, wikiText, "{{Verb-Tabelle", idx);
            if(table.length() > 0 && searchWordAndAdd( word, table, "Gegenwart_ich=", 0)){
                searchWordAndAdd( word, table, "Gegenwart_du=", 0);
                searchWordAndAdd( word, table, "Gegenwart_er, sie, es=", 0);
                searchWordAndAdd( word, table, "1.Vergangenheit_ich=", 0);
                searchWordAndAdd( word, table, "Partizip II=", 0);
                searchWordAndAdd( word, table, "Konjunktiv II_ich=", 0);
                searchWordAndAdd( word, table, "Befehl_du=", 0);
                searchWordAndAdd( word, table, "Befehl_ihr=", 0);
            }
            int lastIdx = idx+1;
            idx = wikiText.indexOf("{{Wortart|Verb}}", lastIdx);
            if(idx <0){
                idx = wikiText.indexOf("{{Wortart|Verb|Deutsch}}", lastIdx);
            }
        }
        
        
        idx = wikiText.indexOf("{{Wortart|Substantiv}}");
        if(idx <0){
            idx = wikiText.indexOf("{{Wortart|Substantiv|Deutsch}}");
        }
        while(idx > 0){
            // Konjugation der Substantive ermitteln 
            String table = getTable( word, wikiText, "{{Substantiv-Tabelle", idx);
            if(table.length() > 0 && searchWordAndAdd( word, table, "Wer oder was? (Einzahl)=", 0)){
                searchWordAndAdd( word, table, "Wessen? (Einzahl)=", 0);
                searchWordAndAdd( word, table, "Wem? (Einzahl)=", 0);
                searchWordAndAdd( word, table, "Wen? (Einzahl)=", 0);
                if(table.indexOf("(Mehrzahl 1)", 0) < 0){
                    searchWordAndAdd( word, table, "Wer oder was? (Mehrzahl)=", 0);
                    searchWordAndAdd( word, table, "Wessen? (Mehrzahl)=", 0);
                    searchWordAndAdd( word, table, "Wem? (Mehrzahl)=", 0);
                    searchWordAndAdd( word, table, "Wen? (Mehrzahl)=", 0);
                }else{
                    searchWordAndAdd( word, table, "Wer oder was? (Mehrzahl 1)=", 0);
                    searchWordAndAdd( word, table, "Wer oder was? (Mehrzahl 2)=", 0);
                    searchWordAndAdd( word, table, "Wessen? (Mehrzahl 1)=", 0);
                    searchWordAndAdd( word, table, "Wessen? (Mehrzahl 2)=", 0);
                    searchWordAndAdd( word, table, "Wem? (Mehrzahl 1)=", 0);
                    searchWordAndAdd( word, table, "Wem? (Mehrzahl 2)=", 0);
                    searchWordAndAdd( word, table, "Wen? (Mehrzahl 1)=", 0);
                    searchWordAndAdd( word, table, "Wen? (Mehrzahl 2)=", 0);
                }
            }
            int lastIdx = idx+1;
            idx = wikiText.indexOf("{{Wortart|Substantiv}}", lastIdx);
            if(idx <0){
                idx = wikiText.indexOf("{{Wortart|Substantiv|Deutsch}}", lastIdx);
            }
        }
        
        
        idx = wikiText.indexOf("{{Wortart|Adjektiv}}");
        if(idx <0){
            idx = wikiText.indexOf("{{Wortart|Adjektiv|Deutsch}}");
        }
        while(idx > 0){
            // Konjugation der Substantive ermitteln 
            String table = getTable( word, wikiText, "{{Adjektiv-Tabelle", idx);
            if(table.length() > 0 && searchWordAndAdd( word, table, "Grundform=", 0)){
                searchWordAndAdd( word, table, "1. Steigerung=", 0);
                searchWordAndAdd( word, table, "2. Steigerung=", 0);
            }
            int lastIdx = idx+1;
            idx = wikiText.indexOf("{{Wortart|Adjektiv}}", lastIdx);
            if(idx <0){
                idx = wikiText.indexOf("{{Wortart|Adjektiv|Deutsch}}", lastIdx);
            }
        }
        
        searchExtendsWords( word, wikiText, "{{Synonyme}}" );
        searchExtendsWords( word, wikiText, "{{Unterbegriffe}}" );
        searchExtendsWords( word, wikiText, "{{Abgeleitete Begriffe}}" );
        
        return true;
    }
    
    /**
     * Liefert einen Substring mit der aktuellen Konjugation/Flektion Tabelle des Wikitextes
     */
    private String getTable(String baseWord, String wikiText, String tableName, int fromIndex){
        int start = wikiText.indexOf( tableName, fromIndex);
        if(start > 0){
            final int length = wikiText.length();
            int braces = 0;
            for(int end = start; end<length; end++){
                
                switch(wikiText.charAt( end )){
                    case '{':
                        braces++;
                        break;
                    case '}':
                        if(--braces == 0){
                            return wikiText.substring( start, end+1 );
                        }
                        break;
                }
            }
        }
        System.out.println("Can not find table  '" + tableName + "' for base word '" + baseWord + "'");
        return "";
    }
    
    /**
     * Die Flextionen und Konjugationen eines Wortes sind durch bestimmte Phrasen gemarked.
     * @param baseWord Hauptwort, Name des Artikels
     * @param wikiText der gesamte Wikiartikel in Wikisyntax 
     * @param marker die Phrase, die eine bestimmte Konjugation markiert
     * @param fromIndex Startposition ab der die Phrase gesucht werden soll
     * @return true, wenn ein richtiges Wort gefunden werden konnte.
     */
    private final boolean searchWordAndAdd(String baseWord, String wikiText, String marker, int fromIndex){
        int idx1 = wikiText.indexOf( marker, fromIndex);
        if(idx1>0){
            idx1 += marker.length();
            int idx2 = indexOf( wikiText, new char[]{'|', '<', '}'}, idx1);
            if(idx2>0){
                String word = wikiText.substring( idx1, idx2).trim();
                if(word.length() <= 1){
                    //leerer Eintrag
                    return false;
                }
                if(word.endsWith("!")){
                    word = word.substring(0, word.length()-1);
                }
                int idx3 = word.indexOf("(");
                int idx4 = word.indexOf( ")", idx3 );
                if(idx3>=0 && idx4>0){
                    String word1 = word.substring(0, idx3);
                    String word2 = word.substring(idx4+1);
                    String word3 = word1 + word2;
                    if(addWordPhrase(word3)){
                        addWordPhrase(word1 + word.substring( idx3+1, idx4) + word2);
                        return true;
                    }else{
                        System.out.println("Invalid Word '" + word + "' for marker '" + marker + "' for base word '" + baseWord + "'");
                    }
                }else{
                    if(addWordPhrase(word)){
                        return true;
                    }else{
                        System.out.println("Invalid Word '" + word + "' for marker '" + marker + "' for base word '" + baseWord + "'");
                    }
                }
            }else{
                System.out.println("End not find for marker '" + marker + "' for base word '" + baseWord + "'");
            }
        }else{
            System.out.println("Marker '" + marker + "' was not find for base word '" + baseWord + "'");
        }
        return false;
    }
    
    
    private boolean searchExtendsWords(String baseWord, String wikiText, String marker){
        int idx1 = wikiText.indexOf( marker );
        if(idx1 >= 0){
            idx1 += marker.length();
            int idx2 = indexOf( wikiText, new char[]{'{', '='}, idx1);
            if(idx2<0){
                idx2 = wikiText.length();
            }
            String extendsWords = wikiText.substring( idx1, idx2 ); 
            int idx3 = extendsWords.indexOf( "[[" );
            while(idx3 > 0){
                int idx4 = extendsWords.indexOf( "]]", idx3 );
                if(idx4 > 0){
                    if(idx3+2 < idx4){//leere Werte die vom Template entstehen, aus Performance gleich skippen 
                        String word = extendsWords.substring( idx3+2, idx4 );
                        if(!addWordPhrase(word)){
                            System.out.println("Invalid Extend Word '" + word + "' for marker '" + marker + "' for base word '" + baseWord + "'");
                        }
                    }
                    idx3 = extendsWords.indexOf( "[[", idx4 );
                }else{
                    idx3 = -1;
                }
            }
            return true;
        }
        return false;
    }
    
    
    /**
     * Substantive sind alle mit Artikel abgelegt und einige Verben zerfallen bei der Konjugation 
     */
    private final boolean addWordPhrase(String phrase){
        int idx1 = 0;
        int idx2 = phrase.indexOf(' ', idx1);
        boolean isValid = true;
        while(idx2>0){
            String word = phrase.substring(idx1, idx2).trim();
            if(word.length()>0){
                if(isValidWord(word)){
                    addWord(word);                
                }else{
                    isValid = false;
                }
            }
            idx1 = idx2 + 1;
            idx2 = phrase.indexOf(' ', idx1);
        }
        String word = phrase.substring(idx1).trim();
        if(isValidWord(word)){
            addWord(word);
        }else{
            isValid = false;
        }
        return isValid;
    }

    /**
     * Implemantation of the template http://de.wiktionary.org/wiki/Vorlage:Deklinationsseite_Adjektiv
     * A sample can see at http://de.wiktionary.org/w/index.php?title=hoch_%28Deklination%29&action=edit
     * @param wikiText
     * @param idx
     */
    private void addDeklinationAdjektiv( String wikiText, int idx ) {
        int idx1 = wikiText.indexOf( "Positiv-Stamm=", idx );
        if( idx1 > 0 ) {
            idx1 += "Positiv-Stamm=".length();
            int idx2 = indexOf( wikiText, new char[] { '|', '<', '}' }, idx1 );
            if( idx2 > 0 ) {
                String word = wikiText.substring( idx1, idx2 ).trim();
                if( word.length() > 1 ) {
                    addWord( word + "e" );
                    addWord( word + "er" );
                    addWord( word + "es" );
                    addWord( word + "en" );
                    addWord( word + "em" );
                }
            }
        }
        
        idx1 = wikiText.indexOf( "Komparativ-Stamm=", idx );
        if( idx1 > 0 ) {
            idx1 += "Komparativ-Stamm=".length();
            int idx2 = indexOf( wikiText, new char[] { '|', '<', '}' }, idx1 );
            if( idx2 > 0 ) {
                String word = wikiText.substring( idx1, idx2 ).trim();
                if( word.length() > 1 ) {
                    addWord( word + "e" );
                    addWord( word + "er" );
                    addWord( word + "es" );
                    addWord( word + "en" );
                    addWord( word + "em" );
                }
            }
        }
        
        idx1 = wikiText.indexOf( "Superlativ-Stamm=", idx );
        if( idx1 > 0 ) {
            idx1 += "Superlativ-Stamm=".length();
            int idx2 = indexOf( wikiText, new char[] { '|', '<', '}' }, idx1 );
            if( idx2 > 0 ) {
                String word = wikiText.substring( idx1, idx2 ).trim();
                if( word.length() > 1 ) {
                    addWord( word + "e" );
                    addWord( word + "er" );
                    addWord( word + "es" );
                    addWord( word + "en" );
                    addWord( word + "em" );
                }
            }
        }
    }


}
