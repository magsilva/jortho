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
 * Created on 13.12.2007
 */
package com.inet.jorthodictionaries;

import java.util.StringTokenizer;

/**
 * 
 * @author Volker Berlin
 */
public class BookGenerator_es extends BookGenerator {

    @Override
    boolean isValidLanguage( String word, String wikiText ) {
        if(wikiText.indexOf("{{ES}}") < 0){
            return false;
        }

        //the follow rules can be found at http://es.wiktionary.org/wiki/Categor%C3%ADa:Plantillas_de_flexi%C3%B3n
        findRuleAndAddWords( word, wikiText, "inflect.es.adj.-ón", null, new String[] { "ón", "ona", "ones", "onas" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.adj.ad-lib", null, new String[] { "" }, new String[] { "" }, new String[] { "" }, new String[] { "" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.adj.no-género", new String[] { "s" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.adj.no-género", new String[] { "es" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.adj.reg", null, new String[] { "o", "os", "a", "as" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.adj.reg-cons", new String[] { "es", "a", "as" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.sust.-ón", null, new String[] { "ón", "ones" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.adj.ad-lib", null, new String[] { "" }, new String[] { "" } );
        //inflect.es.sust.invariante nothing
        //inflect.es.sust.plur.tantum nothing
        findRuleAndAddWords( word, wikiText, "inflect.es.sust.reg", new String[] { "s" } );
        findRuleAndAddWords( word, wikiText, "inflect.es.sust.reg-cons", new String[] { "es" } );
        //{{inflect.es.sust.sing.tantum}} nothing
        
        return true;
    }
    
    private void findRuleAndAddWords(String word, String wikiText, String rule, String[] endingName){
        findRuleAndAddWords( word, wikiText, rule, endingName, null );
    }
    
    private void findRuleAndAddWords(String word, String wikiText, String rule, String[] endingName, String[] ending1){
        findRuleAndAddWords( word, wikiText, rule, endingName, ending1, null );
    }
    
    private void findRuleAndAddWords(String word, String wikiText, String rule, String[] endingName, String[] ending1, String[] ending2){
        findRuleAndAddWords( word, wikiText, rule, endingName, ending1, ending2, null, null );
    }
    
    private void findRuleAndAddWords( String word, String wikiText, String rule, String[] endingName, String[] ending1, String[] ending2, String[] ending3, String[] ending4 ) {
        int idx = wikiText.indexOf( "{{" + rule + "}}" );
        if( idx < 0 ) {
            idx = wikiText.indexOf( "{{" + rule + "|" );
            if( idx < 0 ) {
                return;
            }
        }
        if( endingName != null ) {
            for( String ending : endingName ) {
                addWord( word + ending );
            }
        }
        if( ending1 == null ) {
            return;
        }
        idx += rule.length() + 3;
        int idx2 = wikiText.indexOf( "}}", idx );
        String params = wikiText.substring( idx, idx2 );

        StringTokenizer tok = new StringTokenizer( params, "|" );
        if( tok.hasMoreElements() ) {
            String first = tok.nextToken().trim();
            if( ending1 != null ) {
                for( String ending : ending1 ) {
                    addWord( first + ending );
                }
            }

            if( tok.hasMoreElements() ) {
                String second = tok.nextToken().trim();
                if( ending2 != null ) {
                    for( String ending : ending2 ) {
                        addWord( second + ending );
                    }
                }

                if( tok.hasMoreElements() ) {
                    String third = tok.nextToken().trim();
                    if( ending3 != null ) {
                        for( String ending : ending3 ) {
                            addWord( third + ending );
                        }
                    }

                    if( tok.hasMoreElements() ) {
                        String four = tok.nextToken().trim();
                        if( ending4 != null ) {
                            for( String ending : ending4 ) {
                                addWord( four + ending );
                            }
                        }
                    }
                }

            }
        }
    }

}
