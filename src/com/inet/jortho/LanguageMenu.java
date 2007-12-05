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
 *  Created on 04.12.2007
 */
package com.inet.jortho;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

public class LanguageMenu extends JMenu {
    
    private final AutoSpellChecker checker;
    private final ButtonGroup group = new ButtonGroup();
    private final URL baseURL;
    
    public LanguageMenu(AutoSpellChecker checker, URL baseURL){
        super( Utils.getResource("languages"));
        this.checker = checker;
        this.baseURL = baseURL;
        setEnabled( false );
    }
    
    LanguageMenu(AutoSpellChecker checker, URL baseURL, String availableLocales, String aktiveLocale){
        this( checker, baseURL );
        aktiveLocale = aktiveLocale.trim();
        String[] locales = availableLocales.split( "," );
        for(String locale : locales){
            JRadioButtonMenuItem item = addLanguage( locale );
            if(locale.equals( aktiveLocale )){
                item.setSelected( true );
            }
        }
    }
    
    JRadioButtonMenuItem addLanguage( String localeStr ){
        return addLanguage( new Locale( localeStr ));
    }
    
    public JRadioButtonMenuItem addLanguage( final Locale locale ){
        //TODO sorting
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(locale.getDisplayLanguage());
        group.add( item );
        super.add(item);
        item.addActionListener(new ActionListener(){
            
            public void actionPerformed(ActionEvent e) {
                try {
                    checker.setDictionary( new URL(baseURL, "dictionary_" + locale + ".ortho"), locale );
                } catch( Exception e1 ) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            
        });
        setEnabled( true );
        return item;
    }
    
    

}
