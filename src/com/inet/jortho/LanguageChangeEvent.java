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
 * This Event is used from <code>LanguageChangeListener</code>.
 * @see LanguageChangeListener
 * @author Volker Berlin
 */
public class LanguageChangeEvent{

    private final Locale currentLocale;
    private final Locale oldLocale;
    
    /**
     * Create a new LanguageChangeEvent
     * @param currentLocale the new Locale
     * @param oldLocale the old Locale
     */
    public LanguageChangeEvent( Locale currentLocale, Locale oldLocale ) {
        this.currentLocale = currentLocale;
        this.oldLocale = oldLocale;
    }

    /**
     * Get the value of the old Locale before the point of firing this Event.
     * @return the old Locale
     * @see SpellChecker#getCurrentLocale()
     */
    public Locale getOldLocale() {
        return oldLocale;
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
