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
 *  Created on 02.11.2005
 */
package com.inet.jortho;

import java.util.*;

/**
 * @author Volker Berlin
 */
abstract class DictionaryBase {

    protected char[] tree;
    protected int size;
    protected int idx;
    
    
    protected static final char LAST_CHAR = 0xFFFF;
    
    
    DictionaryBase(char[] tree){
        this.tree = tree;
        size = tree.length;
    }
    
    
    protected DictionaryBase(){
    }
    
    
    /**
     * Check if the word exist in this dictinary.
     * @param word the word to check. Can't be null.
     * @return true if the word exist.
     */
    public boolean exist(String word){
        idx = 0;
        for(int i=0; i<word.length(); i++){
            char c = word.charAt(i);
            while(idx<size && tree[idx] < c){
                idx += 3;
            }
            if((idx>=size || tree[idx] != c)){
                return false;
            }
            if( i == word.length() - 1 && isWordMatch() ) {
                return true;
            }
            idx = readIndex();
            if(idx <= 0) return false;
        }
        return false;
    }
    
    
    
    /**
     * Returns a list of suggestions if the word is not in the dictionary.
     * @param word the wrong spelled word. Can't be null.
     * @return a list of class Suggestion.
     * @see Suggestion
     */
    public List<Suggestion> suggestions(String word){
        ArrayList<Suggestion> list = new ArrayList<Suggestion>();
        if(word.length() == 0 || exist(word)){
            return list;
        }

        idx = 0;
        char[] chars = word.toCharArray();
        suggestions( list, chars, 0, 0, 0);
        Collections.sort( list );
        removeDuplicated( list );
        return list;
    }
    
    
    /**
     * Es wird nach verschiedenen Regeln nach ähnlichen Wörtern gesucht.
     * Je nach Regel gibt es einen anderen diff. Jekleiner der diff desto ähnlicher.
     * Diese Methode ruft sich rekursiv auf.
     * @param list Kontainer für die gefundenen Wörter
     * @param chars bis zur charPosition bereits gemappte Buchstaben, danach noch zu mappende des orignal Wortes
     * @param charPosition Zeichenposition im char array
     * @param lastIdx Position im Suchindex der zur aktuellen Zeichenposition zeigt.
     * @param diff Die Unähnlichkeit bis zur aktuellen Zeichenposition
     */
    private void suggestions( List<Suggestion> list, char[] chars, int charPosition, int lastIdx, int diff){
        if(diff > 15 ||  diff > chars.length * 5 / 2){
            return;
        }
        // Erstmal mit dem richtigen Buchstaben weitermachen 
        idx = lastIdx;
        char c = chars[charPosition];
        if(searchChar(c)){
            if( isWordMatch() ) {
                if(charPosition+1 == chars.length){
                    // exact match at this character position
                    list.add( new Suggestion(chars, diff));
                }else{
                    // a shorter match, we need to cut the string
                    int length = charPosition+1;
                    char[] chars2 = new char[length];
                    System.arraycopy(chars, 0, chars2, 0, length);
                    list.add( new Suggestion(chars2, diff + (chars.length-length)*5));
                }
            }
            idx = readIndex();
            if( idx <= 0 ) {
                // no more charcters in the tree
                return;
            }
            if(charPosition+1 == chars.length){
                suggestionsLonger( list, new String(chars), chars.length, idx, diff + 5);
                return;
            }
            suggestions( list, chars, charPosition + 1, idx, diff );
        }

        
        // Buchstabendreher und Zusatzbuchstaben testen
        if(charPosition+1 < chars.length){
            idx = lastIdx;
            c = chars[charPosition+1];
            if(searchChar(c)){
                int tempIdx = idx;
                
                //Buchstabendreher
                char[] chars2 = chars.clone();
                chars2[charPosition+1] = chars[charPosition];
                chars2[charPosition] = c;
                suggestions( list, chars2, charPosition+1, readIndex(), diff+5);
                
                //Zusatzbuchstaben
                idx = tempIdx;
                char[] chars3 = new char[chars.length-1];
                System.arraycopy(chars, 0, chars3, 0, charPosition);
                System.arraycopy(chars, charPosition+1, chars3, charPosition, chars3.length-charPosition);
                suggestions( list, chars3, charPosition, lastIdx, diff+5);
            }
        }

        // Typus - wrong characters
        if(charPosition < chars.length){
            int tempIdx = idx = lastIdx;
            while( idx < size && tree[idx] < LAST_CHAR ) {
                if( isWordMatch() ){
                    int length = charPosition+1;
                    char[] chars2 = new char[length];
                    System.arraycopy(chars, 0, chars2, 0, length);
                    list.add( new Suggestion( chars2, diff + (chars.length-length)*5 ) );
                }
                if(charPosition + 1 < chars.length){
                    char[] chars2 = chars.clone();
                    chars2[charPosition] = tree[idx];
                    suggestions( list, chars2, charPosition + 1, readIndex(), diff + 5 ); // modfiy the idx value
                }
                idx = tempIdx += 3;
            }
        }
    }
    
    private void suggestionsLonger( List<Suggestion> list, String chars, int originalLength, int lastIdx, int diff){
        idx = lastIdx;
        while(idx<size && tree[idx] < LAST_CHAR){
            if( isWordMatch() ){
                list.add( new Suggestion( (chars + tree[idx]).toCharArray(), diff ) );
            }
            idx += 3;
        }
    }
    
    /**
     * Search if the character exist in the crrent node. If found then the varaible idx point to the locaction.
     * If not found then it point on the next character (char value) item in the node. 
     * @param c the searching character
     * @return true if found
     */
    private boolean searchChar(char c){
        while(idx<size && tree[idx] < c){
            idx += 3;
        }
        if((idx>=size || tree[idx] != c)){
            return false;
        }
        return true;
    }
    
    /**
     * Check if on the current item position a word ends.
     */
    private boolean isWordMatch(){
        return (tree[idx + 1] & 0x8000) > 0;
    }
    
    /**
     * Read the offset in the tree of the next character. 
     */
    final int readIndex(){
        return ((tree[idx+1] & 0x7fff)<<16) + tree[idx+2]; 
    }
    
    
    
    /**
     * Enternt doppelte Einträge in der Liste, dabei werden die Einträge am Anfang der Liste behalten.
     * Sie sollte also bereits sortiert sein.
     * @param list darf nicht null sein 
     */
    private void removeDuplicated(List<Suggestion> list){
        for(int i=0; i<list.size(); i++){
            Object obj = list.get( i );
            for(int j=i+1; j<list.size(); j++){
                if(obj.equals( list.get( j ) )){
                    list.remove( j );
                    j--;
                }
            }
        }
    }
}
