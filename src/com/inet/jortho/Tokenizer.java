/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2007 by i-net software
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
 *  Created on 07.11.2005
 */
package com.inet.jortho;

import java.text.BreakIterator;
import java.util.Locale;

/**
 * Break the text and words and search for misspelling.
 * @author Volker Berlin
 */
class Tokenizer {
    
    private String phrase;
    private final Locale locale;
    private final Dictionary dictionary;
    private BreakIterator sentences;
    private int startSentence, endSentence, startWord, endWord;
    private String sentence;
    private BreakIterator words;
    private int offset;
    
    Tokenizer( String phrase, Dictionary dictionary, Locale locale ) {
        this.phrase = phrase;
        this.locale = locale;
        this.dictionary = dictionary;
        sentences = BreakIterator.getSentenceInstance( locale );
        sentences.setText( phrase );
        words = BreakIterator.getWordInstance( locale );

        startSentence = sentences.first();
        endSentence = sentences.next();
        nextSentence();
    }

    /**
     * Get the next misspelling word.
     */
    String nextInvalidWord() {
        while( true ) {
            if( endWord == BreakIterator.DONE ) {
                startSentence = endSentence;
                endSentence = sentences.next();
                if( endSentence == BreakIterator.DONE ) {
                    return null;
                }
                nextSentence();
            }
            while( endWord != BreakIterator.DONE ) {
                String word = sentence.substring( startWord, endWord ).trim();
                offset = startSentence + startWord;
                startWord = endWord;
                endWord = words.next();
                if( word.length() > 0 && Character.isLetter( word.charAt( 0 ) ) && !dictionary.exist( word ) ) {
                    return word;
                }
            }
        }
    }

    /**
     * Load the next Sentence in the word breaker.
     */
    private void nextSentence() {
        sentence = phrase.substring( startSentence, endSentence );
        words.setText( sentence );
        startWord = words.first();
        endWord = words.next();
    }

    /**
     * Get start offset of the last missspelling.
     */
    int getWordOffset() {
        return offset;
    }

    /**
     * Update the text after a word was replaced. the changes in the text should be only after the current word offset.
     */
    public void updatePhrase( String text ) {
        phrase = text;

        sentences.setText( phrase );
        endSentence = sentences.following( startSentence );
        sentence = phrase.substring( startSentence, endSentence );
        
        words.setText( sentence );
        startWord = words.following( offset );
        endWord = words.next();
    }
}
