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
 * Created on 20.02.2008
 */
package com.inet.jortho;

/**
 * This class hold some options for spell checking. You can change it global see ({@link SpellChecker#getOptions()}
 * or for every JTextComponent on registering.
 * @author Volker Berlin
 */
public class SpellCheckerOptions {
    
    private int suggestionsLimitMenu = 15;
    private int suggestionsLimitDialog = 15;
    private boolean caseSensitive = true;

    /**
     * Create a SpellCheckerOptions with default values.
     */
    public SpellCheckerOptions(){
        //empty block
    }
    /**
     * Set the maximun count of enties for the suggestion menu.
     * @param count the suggestions limit
     */
    public void setSuggestionsLimitMenu( int count ) {
        this.suggestionsLimitMenu = count;
    }

    /**
     * Get the maximun count of enties for the suggestion menu. The default is 15.
     * @return the suggestions limit
     */
    public int getSuggestionsLimitMenu() {
        return suggestionsLimitMenu;
    }

    /**
     * Set the maximun count of enties for the suggestion list in the spell checker dialog.
     * @param count the suggestions limit
     */
    public void setSuggestionsLimitDialog( int count ) {
        this.suggestionsLimitDialog = count;
    }

    /**
     * Get the maximun count of enties for the suggestion list in the spell checker dialog. The default is 15.
     * @return the suggestions limit
     */
    public int getSuggestionsLimitDialog() {
        return suggestionsLimitDialog;
    }

    /**
     * Set if the spell checker is case sensitive. This has only an effect to the first letter of a word.
     * The default value is true. 
     * @param caseSensitive 
     */
    public void setCaseSensitive( boolean caseSensitive ) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Get if the spell checker is case sensitive.
     * @return the value of the flag
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    

}
