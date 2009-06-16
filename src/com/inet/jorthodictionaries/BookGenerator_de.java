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
        wikiText = removeHtmlFormating(wikiText);
        int idxGerman = wikiText.indexOf("{{Sprache|Deutsch}}");
        if( idxGerman < 0){
            
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
            Properties props = BookUtils.parseRule(wikiText,"Deklinationsseite Adjektiv", 0);
            if(props != null){
                addDeklinationAdjektiv(props);
                // The declinations are valid, but the lemma (word) not.
                return false;
            }
            
            return false;
        }
        do{
            String chapter = getChapter(wikiText, idxGerman);
            searchFlexion( word, chapter );
            idxGerman = wikiText.indexOf(chapter) + chapter.length();
            //should not occur but it occur
            idxGerman = wikiText.indexOf("{{Sprache|Deutsch}}", idxGerman );
        }while(idxGerman > 0);
        return true;
    }
    
    private final void searchFlexion( String word, String wikiText ){
        int idx = wikiText.indexOf("{{Wortart|Verb}}");
        if(idx <0){
            idx = wikiText.indexOf("{{Wortart|Verb|Deutsch}}");
        }
        while(idx > 0){
            //ascertain flexion of the verbs
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
            String chapter = getChapter(wikiText, idx);
            if( !addDeklinationSubstTable( chapter, 0, word ) && 
                            !addDeklinationSubstM_NStart( chapter, 0 ) &&
                            !addDeklinationSubstMSchwach1( chapter, 0 ) &&
                            !addDeklinationSubstMSchwach3( chapter, 0 ) &&
                            !addDeklinationSubstFStark( chapter, 0 ) ){
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
            // finding the conjugation of the Adjective 
            Properties props = BookUtils.parseRule( wikiText, "Adjektiv-Tabelle", idx);
            if(props == null){
                props = BookUtils.parseRule( wikiText, "Adjektiv-Tabelle (Deklination)", idx);
            }
            if(props == null){
                props = BookUtils.parseRule( wikiText, "Adjektiv-Tabelle (Bild)", idx);
            }
            if(props == null){
                props = BookUtils.parseRule( wikiText, "Adjektiv-Tabelle (Bild) (Deklination)", idx);
            }
            if(props != null){                
                addFormatedWordPhrase(word, "Grundform", props.getProperty( "Grundform" ) );
                addFormatedWordPhrase(word, "1. Steigerung", props.getProperty( "1. Steigerung" ) );
                addFormatedWordPhrase(word, "2. Steigerung", props.getProperty( "2. Steigerung" ) );
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
    }
    
    /**
     * Get the chapter on the current position. If a next chapter line is not find
     * then the completely wikiText is return.
     * @param wikiText
     * @param headerIdx position in the header line
     * @return a chapter or all text.
     */
    private String getChapter(String wikiText, int headerIdx){
        int startIdx = wikiText.lastIndexOf('\n', headerIdx) + 1;
        int endIdx = wikiText.indexOf('\n', headerIdx);
        if(endIdx == -1){
            return wikiText.substring(startIdx);
        }
        String header = wikiText.substring(startIdx, endIdx).trim();
        int prefixSize = 0;
        while(header.length() > prefixSize && header.charAt(prefixSize) == '='){
            prefixSize++;
        }
        if(prefixSize == 0){
            return wikiText.substring(startIdx);
        }
        String marker = header.substring(0,prefixSize);
        if(!header.endsWith(marker)){
            return wikiText.substring(startIdx);
        }

        Pattern pattern = Pattern.compile( "^" + marker + "[^=].*[^=]" + marker + "\\s*$", Pattern.MULTILINE );
        Matcher matcher = pattern.matcher( wikiText );

        if( matcher.find( endIdx ) ) {
            String chapter = wikiText.substring(startIdx, matcher.start() ).trim();
            if(header.equals(chapter)){
                return wikiText.substring(startIdx); //there is some things wrong in the structure
            }
            return chapter;
        }
            
        return wikiText.substring(startIdx);
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
                addFormatedWordPhrase( baseWord, marker, word );
                return true;
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
     * Add a formated word phrase like it used in some format tables.
     * @param baseWord the lemma word, for debugging
     * @param key the key of the format table, for debugging
     * @param phrase the word or phrase, can also be null or empty
     */
    private final void addFormatedWordPhrase( String baseWord, String key, String phrase ){
        if( phrase == null ) {
            return;
        }
        if( phrase.endsWith( "!" ) ) {
            phrase = phrase.substring( 0, phrase.length() - 1 );
        }
        if( phrase.length() <= 1 ) {
            //empty Entry
            return;
        }
        if( phrase.equals( "{{fehlend}}" ) || phrase.equals( "---" ) || phrase.equals( "--" ) ) {
            return;
        }
        int idx3 = phrase.indexOf( "(" );
        int idx4 = phrase.indexOf( ")", idx3 );
        if( idx3 >= 0 && idx4 > 0 ) {
            String word1 = phrase.substring( 0, idx3 );
            String word2 = phrase.substring( idx4 + 1 );
            String word3 = word1.length() + word2.length() > 0 ? word1 + word2 : phrase.substring( idx3 + 1, idx4 );
            if( addWordPhrase( word3 ) ) {
                addWordPhrase( word1 + phrase.substring( idx3 + 1, idx4 ) + word2 );
                return;
            } else {
                System.out.println( "Invalid Word '" + phrase + "' for marker '" + key + "' for base word '"
                                + baseWord + "'" );
            }
        } else {
            if( addWordPhrase( phrase ) ) {
                return;
            } else {
                System.out.println( "Invalid Word '" + phrase + "' for marker '" + key + "' for base word '"
                                + baseWord + "'" );
            }
        }
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
     * Implementation of the template http://de.wiktionary.org/wiki/Vorlage:Deklinationsseite_Adjektiv
     * A sample can see at http://de.wiktionary.org/w/index.php?title=hoch_%28Deklination%29&action=edit
     * @param wikiText
     * @param idx
     */
    private void addDeklinationAdjektiv( Properties props ) {
        addDeklinationAdjektiv( props.getProperty("Positiv-Stamm") );
        addDeklinationAdjektiv( props.getProperty("Komparativ-Stamm") );
        addDeklinationAdjektiv( props.getProperty("Superlativ-Stamm") );
    }
    
    /**
     * Add the Adjective Declination for the given word root.
     * @param wordStamm the word root, can be null
     */
    private void addDeklinationAdjektiv( String wordStamm ) {
        if( wordStamm != null && wordStamm.length() > 1 ) {
            addWord( wordStamm + "e" ); 
            addWord( wordStamm + "er" );
            addWord( wordStamm + "es" );
            addWord( wordStamm + "en" );
            addWord( wordStamm + "em" );
        }
    }
    
    /**
     * Implementation of the templates
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
            props = BookUtils.parseRule( wikiText, template, fromIndex );
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
            addFormatedWordPhrase( baseWord, key, word );
        }
        return true;
    }

    private String removeHtmlFormating( String word ) {
        int idx1 = word.indexOf( '<' );
        while( idx1 >= 0 ) {
            int idx2 = word.indexOf( '>', idx1 );
            if( idx2 > 0 ) {
                String html = word.substring(idx1+1, idx2).toLowerCase().trim();
                if(html.equals("br") || html.equals("p")){
                    html = " ";
                }else{
                    html = "";
                }
                word = word.substring( 0, idx1 ) + html + word.substring( idx2 + 1 );
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
     * Implementation of the template http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_m_stark and
     * http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_n_stark
     */
    private boolean addDeklinationSubstM_NStart( String wikiText, int fromIndex ) {
        Properties props = BookUtils.parseRule( wikiText, "Deutsch Substantiv m stark", fromIndex );
        if( addDeklinationSubstM_NStart( props ) ) {
            return true;
        }
        props = BookUtils.parseRule( wikiText, "Deutsch Substantiv n stark", fromIndex );
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
                addWord( singular + "s" ); //Genitive
            }
            if( genetiv.length() == 0 || "1".equals( genetiv ) ) {
                addWord( singular + "es" ); //Genitive
                addWord( singular + "e" ); //Dative
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
     * Implementation of the template http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_m_schwach_1
     */
    private boolean addDeklinationSubstMSchwach1( String wikiText, int fromIndex ) {
        Properties props = BookUtils.parseRule( wikiText, "Deutsch Substantiv m schwach 1", fromIndex );
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
     * Implementation of the template http://de.wiktionary.org/wiki/Vorlage:Deutsch_Substantiv_m_schwach_3
     */
    private boolean addDeklinationSubstMSchwach3( String wikiText, int fromIndex ) {
        Properties props = BookUtils.parseRule( wikiText, "Deutsch Substantiv m schwach 3", fromIndex );
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
        Properties props = BookUtils.parseRule( wikiText, "Deutsch Substantiv f stark", fromIndex );
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

}
