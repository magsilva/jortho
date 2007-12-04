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
 *  Created on 04.11.2005
 */
package com.inet.jortho;


/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 * @author Volker Berlin
 */
final public class Suggestion implements Comparable{

    private final String word;
    private final int diff;
    
    Suggestion(char[] chars, int diff) {
        this.word = new String(chars);
        this.diff = diff;
    }
    
    
    public String toString(){
        return word;
    }


    /**
     * @return
     */
    public String getWord() {
        return word;
    }

    
    /**
     * Return a value that descript dissimilarity to the original word.
     * A vaulue of 0 means that the value is 100% identical. This should not occur.
     * @return the dissimilarity, so larger to differ the word.   
     */
    public int getDissimilarity(){
        return diff;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object sugg){
        if (sugg instanceof Suggestion) {
            return word.equals( ((Suggestion)sugg).word );
        }
        return false;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public int hashCode(){
        return word.hashCode();
    }


    /**
     * {@inheritDoc}
     */
    public int compareTo( Object sugg ) {
        return diff - ((Suggestion)sugg).diff;
    }
}
