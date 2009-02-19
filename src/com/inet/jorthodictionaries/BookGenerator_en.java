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

/**
 * 
 * @author Volker Berlin
 */
public class BookGenerator_en extends BookGenerator {

    @Override
    boolean isValidLanguage( String word, String wikiText ) {
        if( wikiText.indexOf( "==English==" ) < 0 ) {
            return false;
        }

        if( wikiText.indexOf( "{{en-noun}}" ) > 0 || wikiText.indexOf( "{{en-proper noun}}" ) > 0 ) {
            // http://www.englishclub.com/grammar/nouns-possessive.htm
            String genetiv = word + "'s";
            if( isValidWord( genetiv ) ) {
                addWord( genetiv );
            }
            String pluralGenetiv = word + "s'";
            if( isValidWord( pluralGenetiv ) ) {
                addWord( pluralGenetiv );
            }
        }

        int idx = wikiText.indexOf( "{{en-noun|pl=" );
        if( idx > 0 ) {
            // http://www.englishclub.com/grammar/nouns-possessive.htm
            idx += "{{en-noun|pl=".length();
            int idx2 = wikiText.indexOf( "}}", idx );
            if( idx2 > 0 ) {
                String plural = wikiText.substring( idx, idx2 );
                plural = trim( plural );
                if( isValidWord( plural ) ) {
                    addWord( plural );
                    plural += "'s";
                    if( isValidWord( plural ) ) {
                        addWord( plural );
                    }
                }
            }
        }
        return true;
    }

    /**
     * Removes quotes and parenthesis
     * 
     * @param word
     *            the trimming word
     * @return the new word
     */
    private String trim( String word ) {
        word = word.trim();
        if( word.length() >= 6 && word.startsWith( "'''" ) && word.endsWith( "'''" ) ) {
            word = word.substring( 3, word.length() - 3 );
        }
        word = word.trim();
        if( word.length() >= 4 && word.startsWith( "[[" ) && word.endsWith( "]]" ) ) {
            word = word.substring( 2, word.length() - 2 );
        }
        word = word.trim();
        return word;
    }
}
