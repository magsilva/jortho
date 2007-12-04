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
            if(i == word.length()-1 && 
              (tree[idx+1] & 0x8000) > 0){
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
    public List suggestions(String word){
        ArrayList list = new ArrayList();
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
    private void suggestions( List list, char[] chars, int charPosition, int lastIdx, int diff){
        
        // Erstmal mit dem richtigen Buchstaben weitermachen 
        idx = lastIdx;
        char c = chars[charPosition];
        if(searchChar(c)){
            if(charPosition+1 == chars.length){
                if((tree[idx+1] & 0x8000) > 0 && diff > 0){
                    list.add( new Suggestion(chars, diff));
                }
                // ToDo Regel für Längere Wörter    
                    
                return;
            }
            suggestions( list, chars, charPosition+1, readIndex(), diff);
        }

        
        // Buchstabendreher und Zusatzbuchstaben testen
        if(charPosition+1 < chars.length){
            idx = lastIdx;
            c = chars[charPosition+1];
            if(searchChar(c)){
                int tempIdx = idx;
                
                //Buchstabendreher
                char[] chars2 = (char[])chars.clone();
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

    }
    
    
    private boolean searchChar(char c){
        while(idx<size && tree[idx] < c){
            idx += 3;
        }
        if((idx>=size || tree[idx] != c)){
            return false;
        }
        return true;
    }
    
    
    protected final int readIndex(){
        return ((tree[idx+1] & 0x7fff)<<16) + tree[idx+2]; 
    }
    

    /**
     * Enternt doppelte Einträge in der Liste, dabei werden die Einträge am Anfang der Liste behalten.
     * Sie sollte also bereits sortiert sein.
     * @param list darf nicht null sein 
     */
    private void removeDuplicated(List list){
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
