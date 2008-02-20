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
 *  Created on 07.11.2005
 */
package com.inet.jortho;

import java.text.BreakIterator;
import java.util.Locale;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * Break the text and words and search for misspelling.
 * @author Volker Berlin
 */
class Tokenizer {
    
    private final Document doc;
    private final SpellCheckerOptions options;
    /** start offset of current paragraph */
    private int paragraphOffset;
    /** end offset of current paragraph */
    private int endOffset;
    
    private String phrase;
    private final Dictionary dictionary;
    private BreakIterator sentences;
    private int startSentence, endSentence, startWord, endWord;
    private String sentence;
    private BreakIterator words;
    private int wordOffset;
    private boolean isFirstWordInSentence;
    
    /**
     * Create a tokenizer for the completely text document. 
     */
    Tokenizer( JTextComponent jText, Dictionary dictionary, Locale locale, SpellCheckerOptions options ) {
        this( jText, dictionary, locale, 0, jText.getDocument().getLength(), options );
    }

    /**
     * Create a Tokenizer for the current paragraph
     * @param jText the checking JTextComponent
     * @param dictionary the used Dictionary
     * @param locale the used Locale, is needed for the word and sentence breaker
     * @param offset the current offset.
     */
    Tokenizer( JTextComponent jText, Dictionary dictionary, Locale locale, int offset, SpellCheckerOptions options ) {
        this( jText, dictionary, locale, Utilities.getParagraphElement( jText, offset ).getStartOffset(), 
                                         Utilities.getParagraphElement( jText, offset ).getEndOffset(), options );
    }

        /**
     * Create a tokenizer for the selected range.
     */
    Tokenizer( JTextComponent jText, Dictionary dictionary, Locale locale, int startOffset, int endOffset, SpellCheckerOptions options ) {

        this.dictionary = dictionary;
        doc = jText.getDocument();
        this.options = options == null ? SpellChecker.getOptions() : options;
        sentences = BreakIterator.getSentenceInstance( locale );
        words = BreakIterator.getWordInstance( locale );

        paragraphOffset = startOffset;
        this.endOffset = endOffset;
        //loadSentences();
        setSentencesText();
        endSentence = sentences.first();
        endWord = BreakIterator.DONE;
    }

    /**
     * Get the next misspelling word. If not found then it return null.
     */
    String nextInvalidWord() {
        isFirstWordInSentence = false;
        while( true ) {
            if( endWord == BreakIterator.DONE ) {
                startSentence = endSentence;
                endSentence = sentences.next();
                if( endSentence == BreakIterator.DONE ) {
                    if(!nextParagraph()){
                        return null;
                    }
                }else{
                    nextSentence();
                }
            }
            while( endWord != BreakIterator.DONE ) {
                String word = sentence.substring( startWord, endWord ).trim();
                wordOffset = startSentence + startWord;
                startWord = endWord;
                endWord = words.next();
                //only words with 2 or more characters are checked
                if( word.length() > 1 && Character.isLetter( word.charAt( 0 ) )){
                    boolean exist = dictionary.exist( word );
                    if(!exist && !options.isCaseSensitive()){
                        exist = dictionary.exist( Utils.getInvertedCapitalizion( word ) );
                    }else
                    if(isFirstWordInSentence && !exist && Character.isUpperCase( word.charAt( 0 ) )){
                        // Uppercase check on starting of sentence
                        String lowerWord = word.substring( 0, 1 ).toLowerCase() + word.substring( 1 );
                        exist = dictionary.exist( lowerWord );
                    }
                    if( !exist ) {
                        return word;
                    }
                    isFirstWordInSentence = false;
                }
            }
        }
    }
    
    /**
     * Was the last invalid word the first word in a sentence.
     * @return true if it was the first word.
     */
    boolean isFirstWordInSentence(){
        return isFirstWordInSentence;
    }
    
    /**
     * Initialize the variables for the next paragraph.
     * @return true, if there is a next paragraph
     */
    private boolean nextParagraph(){
        if(doc instanceof AbstractDocument){
            paragraphOffset = ((AbstractDocument)doc).getParagraphElement( paragraphOffset ).getEndOffset();
            if(paragraphOffset >= endOffset){
                return false;
            }
        }else{
            return false;
        }
        loadSentences();
        return true;
    }
    
    /**
     * Loads the sentences of the current paragraph.
     */
    private void loadSentences(){
        setSentencesText();

        startSentence = sentences.first();
        endSentence = sentences.next();
        nextSentence();
    }
    
    /**
     * Call sentences.setText( String ) based on the current value of paragraphOffset.
     */
    private void setSentencesText(){
        int end = endOffset;
        if(doc instanceof AbstractDocument){
            end = ((AbstractDocument)doc).getParagraphElement( paragraphOffset ).getEndOffset();
        }
        try {
            phrase = doc.getText( paragraphOffset, end-paragraphOffset );
        } catch( BadLocationException e ) {
            e.printStackTrace();
        }
        sentences.setText( phrase );
    }

    /**
     * Load the next Sentence in the word breaker.
     */
    private void nextSentence() {
        sentence = phrase.substring( startSentence, endSentence );
        words.setText( sentence );
        startWord = words.first();
        endWord = words.next();
        isFirstWordInSentence = true;
    }

    /**
     * Get start offset of the last misspelling in the JTextComponent.
     */
    int getWordOffset() {
        return paragraphOffset + wordOffset;
    }

    /**
     * Update the text after a word was replaced. The changes in the text should be only after the current word offset.
     */
    void updatePhrase() {
        endOffset = doc.getLength();
        setSentencesText();
        
        endSentence = sentences.following( startSentence );
        sentence = phrase.substring( startSentence, endSentence );
        
        words.setText( sentence );
        startWord = words.following( wordOffset );
        endWord = words.next();
    }
}
