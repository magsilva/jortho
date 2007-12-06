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
 *  Created on 06.12.2007
 */
package com.inet.jortho;

import java.util.Locale;

/**
 * This Event is used from <code>DictionaryChangeListener</code>.
 * @see DictionaryChangeListener
 * @author Volker Berlin
 */
public class DictionaryChangeEvent{

    private final Dictionary currentDictionary;
    private final Locale currentLocale;
    private final Dictionary oldDictionary;
    private final Locale oldLocale;
    
    /**
     * Create a new DictionaryChangeEvent
     * @param currentDictionary the new Dictionary
     * @param currentLocale the new Locale
     * @param oldDictionary the old Dictionary
     * @param oldLocale the old Locale
     */
    public DictionaryChangeEvent( Dictionary currentDictionary, Locale currentLocale, Dictionary oldDictionary, Locale oldLocale ) {
        this.currentDictionary = currentDictionary;
        this.currentLocale = currentLocale;
        this.oldDictionary = oldDictionary;
        this.oldLocale = oldLocale;
    }

    /**
     * Get the value of the current Dictonary at the point of firing this Event. It general it should be equals to SpellChecker.getCurrentDictionary().
     * @return the current Dictonary
     * @see SpellChecker#getCurrentDictionary()
     */
    public Dictionary getCurrentDictionary() {
        return currentDictionary;
    }

    /**
     * Get the value of the current Locale at the point of firing this Event. It general it should be equals to SpellChecker.getCurrentLocale().
     * @return the current Locale
     * @see SpellChecker#getCurrentLocale()
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }

}
