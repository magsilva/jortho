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

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
            int idx = findTemplate( wikiText, "Deklinationsseite Adjektiv", 0);
            if(idx >= 0){
                addDeklinationAdjektiv(wikiText, idx);
                // Die Deklinations sind valid, das Lema (Wort) aber nicht.
                return false;
            }
            
            return false;
        }

        wikiText = removeHtmlFormating(wikiText);
        
        int idx = wikiText.indexOf("{{Wortart|Verb}}");
        if(idx <0){
            idx = wikiText.indexOf("{{Wortart|Verb|Deutsch}}");
        }
        while(idx > 0){
            //Flextionen der Verben ermitteln
            String table = getTable( wikiText, "Verb-Tabelle", idx );
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
            if( !addDeklinationSubstTable( wikiText, idx, word ) && 
                            !addDeklinationSubstM_NStart( wikiText, idx ) &&
                            !addDeklinationSubstMSchwach1( wikiText, idx ) &&
                            !addDeklinationSubstMSchwach3( wikiText, idx ) &&
                            !addDeklinationSubstFStark( wikiText, idx ) ){
                // no Deklination found
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
            // Konjugation der Adjektive ermitteln 
            String table = getTable( wikiText, "Adjektiv-Tabelle", idx);
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
    private String getTable( String wikiText, String tableName, int fromIndex ){
        int start = wikiText.indexOf( "{{" + tableName, fromIndex);
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
        boolean isValid = true;
        String[] words = phrase.split( "\\s+" );
        for( int i = 0; i < words.length; i++ ) {
            String word = words[i];
            if(word.length()>0){
                if(isValidWord(word)){
                    addWord(word);                
                }else{
                    isValid = false;
                }
            }
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
    
    /**
     * Implemantation of the templates
     * http://de.wiktionary.org/wiki/Vorlage:Substantiv-Tabelle
     * http://de.wiktionary.org/wiki/Vorlage:Substantiv-Tabelle_(2_Pluralformen)
     * http://de.wiktionary.org/wiki/Vorlage:Substantiv-Tabelle_(2_Singularformen)
     * http://de.wiktionary.org/wiki/Vorlage:Substantiv-Tabelle_(3_Singularformen)
     * http://de.wiktionary.org/wiki/Vorlage:Substantiv-Tabelle_(Bild)
     * http://de.wiktionary.org/wiki/Vorlage:Substantiv-Tabelle_(2_Bilder)
     */
    private boolean addDeklinationSubstTable( String wikiText, int fromIndex, String baseWord ) {
        String[] templates =
                        { "Substantiv-Tabelle", "Substantiv-Tabelle (2 Pluralformen)",
                                        "Substantiv-Tabelle (2 Singularformen)",
                                        "Substantiv-Tabelle (3 Singularformen)", 
                                        "Substantiv-Tabelle (Bild)",
                                        "Substantiv-Tabelle (2 Bilder)", 
                                        "Substantiv-Tabelle (2 Pluralformen) (Bild)",
                                        "Substantiv-Tabelle (2 Pluralformen) (2 Bilder)",
                                        "Substantiv-Tabelle (3 Bilder)", 
                                        "Substantiv-Tabelle (3 Pluralformen)", 
                                        "Substantiv-Tabelle-Singular"};
        Properties props = null;
        for( int i = 0; i < templates.length; i++ ) {
            String template = templates[i];
            props = parseRule( wikiText, template, fromIndex );
            if( props != null ) {
                break;
            }
        }
        if( props == null ) {
            return false;
        }

        String[] keyWords =
                        { "Wer oder was? (Einzahl)", "Wessen? (Einzahl)", "Wem? (Einzahl)", "Wen? (Einzahl)",
                                        "(Mehrzahl 1)", "Wer oder was? (Mehrzahl)", "Wessen? (Mehrzahl)",
                                        "Wem? (Mehrzahl)", "Wen? (Mehrzahl)", "Wer oder was? (Mehrzahl 1)",
                                        "Wer oder was? (Mehrzahl 2)", "Wessen? (Mehrzahl 1)", "Wessen? (Mehrzahl 2)",
                                        "Wem? (Mehrzahl 1)", "Wem? (Mehrzahl 2)", "Wen? (Mehrzahl 1)",
                                        "Wen? (Mehrzahl 2)" };

        for( String key : keyWords ) {
            String word = props.getProperty( key );
            if( word == null ) {
                continue;
            }
            if( word.endsWith( "!" ) ) {
                word = word.substring( 0, word.length() - 1 );
            }
            if( word.length() <= 1 ) {
                //leerer Eintrag
                continue;
            }
            if( word.equals( "{{fehlend}}" ) || word.equals( "---" ) || word.equals( "--" ) ) {
                continue;
            }
            int idx3 = word.indexOf( "(" );
            int idx4 = word.indexOf( ")", idx3 );
            if( idx3 >= 0 && idx4 > 0 ) {
                String word1 = word.substring( 0, idx3 );
                String word2 = word.substring( idx4 + 1 );
                String word3 = word1.length() + word2.length() > 0 ? word1 + word2 : word.substring( idx3 + 1, idx4 );
                if( addWordPhrase( word3 ) ) {
                    addWordPhrase( word1 + word.substring( idx3 + 1, idx4 ) + word2 );
                    continue;
                } else {
                    System.out.println( "Invalid Word '" + word + "' for marker '" + key + "' for base word '"
                                    + baseWord + "'" );
                }
            } else {
                if( addWordPhrase( word ) ) {
                    continue;
                } else {
                    System.out.println( "Invalid Word '" + word + "' for marker '" + key + "' for base word '"
                                    + baseWord + "'" );
                }
            }
        }
        return true;
    }

    private String removeHtmlFormating( String word ) {
        int idx1 = word.indexOf( '<' );
        while( idx1 >= 0 ) {
            int idx2 = word.indexOf( '>', idx1 );
            if( idx2 > 0 ) {
                word = word.substring( 0, idx1 ) + word.substring( idx2 + 1 );
                idx1 = word.indexOf( '<' );
            } else {
                idx1 = -1;
            }
        }
        idx1 = word.indexOf( "&nbsp;" );
        while( idx1 >= 0 ) {
            word = word.substring( 0, idx1 ) + " " + word.substring( idx1 + 6 );
            idx1 = word.indexOf( "&nbsp;" );
        }
        return word;
    }

    /**
     * Implemantation of the template http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_m_stark and
     * http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_n_stark
     */
    private boolean addDeklinationSubstM_NStart( String wikiText, int fromIndex ) {
        Properties props = parseRule( wikiText, "Deutsch Substantiv m stark", fromIndex );
        if( addDeklinationSubstM_NStart( props ) ) {
            return true;
        }
        props = parseRule( wikiText, "Deutsch Substantiv n stark", fromIndex );
        return addDeklinationSubstM_NStart( props );
    }

    private boolean addDeklinationSubstM_NStart( Properties props ) {
        if( props == null ) {
            return false;
        }
        String singular = props.getProperty( "SINGULAR", "" );
        String plural = props.getProperty( "PLURAL", "" );
        String genetiv = props.getProperty( "GENITIV-E", "" );
        String endung = props.getProperty( "ENDUNGS-N", "" );
        if( singular.length() > 0 ) {
            if( genetiv.length() == 0 || "0".equals( genetiv ) ) {
                addWord( singular + "s" ); //Genetiv
            }
            if( genetiv.length() == 0 || "1".equals( genetiv ) ) {
                addWord( singular + "es" ); //Genetiv
                addWord( singular + "e" ); //Dativ
            }
        }
        if( plural.length() > 0 ) {
            addWord( plural );
            if( endung.length() == 0 || "0".equals( endung ) ) {
                addWord( plural + "n" );
            }
        }
        return true;
    }

    /**
     * Implemantation of the template http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_m_schwach_1
     */
    private boolean addDeklinationSubstMSchwach1( String wikiText, int fromIndex ) {
        Properties props = parseRule( wikiText, "Deutsch Substantiv m schwach 1", fromIndex );
        if( props == null ) {
            return false;
        }
        String singular = props.getProperty( "SINGULAR", "" );
        String plural = props.getProperty( "PLURAL", "" );
        String genetiv = props.getProperty( "GENITIV-E", "" );
        if( singular.length() > 0 ) {
            if( "1".equals( genetiv ) ) {
                addWord( singular + "n" );
            }
        }
        if( plural.length() > 0 ) {
            addWord( plural );
        }
        return true;
    }

    /**
     * Implemantation of the template http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_m_schwach_3
     */
    private boolean addDeklinationSubstMSchwach3( String wikiText, int fromIndex ) {
        Properties props = parseRule( wikiText, "Deutsch Substantiv m schwach 1", fromIndex );
        if( props == null ) {
            return false;
        }
        String singular = props.getProperty( "SINGULAR", "" );
        String plural = props.getProperty( "PLURAL", "" );
        String genetiv = props.getProperty( "GENITIV-E", "" );
        if( singular.length() > 0 ) {
            if( "1".equals( genetiv ) ) {
                addWord( singular + "es" );
            } else {
                addWord( singular + "s" );
            }
        }
        if( plural.length() > 0 ) {
            addWord( plural );
        }
        return true;
    }

    /**
     * Implementation of the template http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_f_stark
     */
    private boolean addDeklinationSubstFStark( String wikiText, int fromIndex ) {
        Properties props = parseRule( wikiText, "Deutsch Substantiv m schwach 1", fromIndex );
        if( props == null ) {
            return false;
        }
        //String singular = props.getProperty( "SINGULAR", "" );
        String plural = props.getProperty( "PLURAL", "" );
        String pluralN = props.getProperty( "PLURAL AUF N?", "" );
        if( plural.length() > 0 ) {
            addWord( plural );
            if( !"ja".equals( pluralN ) ) {
                addWord( plural + "n" );
            }
        }
        return true;
    }

    /**
     * Read the inforamtions of the template placeholder
     * 
     * @return null if nothing find
     */
    private Properties parseRule( String wikiText, String tempalateName, int fromIndex ) {
        int start = findTemplate( wikiText, tempalateName, fromIndex );
        if( start > 0 ) {
            final int length = wikiText.length();
            int braces = 2;
            for( int end = start; end < length; end++ ) {

                switch( wikiText.charAt( end ) ) {
                    case '{':
                        braces++;
                        break;
                    case '}':
                        if( --braces == 0 ) {
                            return parseRule( wikiText, start, end - 2 );
                        }
                        break;
                }
            }
        }
        return null;
    }

    /**
     * Read the inforamtions of the template placeholder
     */
    private Properties parseRule( String wikiText, int idxStart, int idxEnd ) {
        Properties props = new Properties();

        String[] tokens = wikiText.substring( idxStart, idxEnd ).split( "\\|" );
        for( int i = 0; i < tokens.length; i++ ) {
            String value = tokens[i].trim();
            int idx = value.indexOf( '=' );
            if( idx > 0 ) {
                String name = value.substring( 0, idx );
                value = value.substring( idx + 1 );
                props.setProperty( name, value.trim() );
            } else {
                props.setProperty( String.valueOf( i ), value.trim() );
            }
        }
        return props;
    }

    /**
     * Find a template name in the wiki text. the problem are possible whitespaces.
     * 
     * @param wikiText
     * @param tempalateName
     * @return the index after the first | or -1.
     */
    private int findTemplate( String wikiText, String tempalateName, int fromIndex ) {
        //find {{  tempalateName  |
        Pattern pattern = Pattern.compile( "\\{\\{\\s*\\Q" + tempalateName.replace( " ", "\\E\\s+\\Q" ) + "\\E\\s*\\|" );
        Matcher matcher = pattern.matcher( wikiText );

        if( matcher.find( fromIndex ) ) {
            return matcher.end();
        }

        return -1;
    }

}
