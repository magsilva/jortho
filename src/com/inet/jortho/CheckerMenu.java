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

import java.awt.Component;
import java.awt.event.*;
import java.util.List;
import java.util.Locale;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * @author Volker Berlin
 */
class CheckerMenu extends JMenu implements PopupMenuListener, HierarchyListener, LanguageChangeListener {
    
    private Dictionary                    dictionary;

    private Locale                        locale;

    
    CheckerMenu(){
        super( Utils.getResource("spelling"));
        super.addHierarchyListener(this);
        SpellChecker.addLanguageChangeLister( this );
        dictionary = SpellChecker.getCurrentDictionary();
        locale = SpellChecker.getCurrentLocale();
    }


    public void popupMenuCanceled(PopupMenuEvent e) {
    }
    

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }


    public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
       JPopupMenu popup = (JPopupMenu)ev.getSource();
        
        Component invoker = popup.getInvoker();
        if(invoker instanceof JTextComponent){
            final JTextComponent jText = (JTextComponent)invoker;
            int offs = jText.getCaretPosition();
            try {
                final int begOffs = Utilities.getWordStart(jText, offs);
                final int endOffs = Utilities.getWordEnd(jText, offs);
                String word = jText.getText(begOffs, endOffs-begOffs);
                super.removeAll();
                if(dictionary == null){
                    // without dictonary it is disabled
                    this.setEnabled(false);
                    return;
                }
                List list = dictionary.searchSuggestions(word);

                //Disable wenn keine Vorschläge
                this.setEnabled(list.size()>0);
                
                for(int i=0; i<list.size(); i++){
                    Suggestion sugestion = (Suggestion)list.get(i);
                    final String newWord = sugestion.getWord();
                    JMenuItem item = super.add(newWord);
                    item.addActionListener(new ActionListener(){
                        
                        public void actionPerformed(ActionEvent e) {
                            Document doc = jText.getDocument();
                            try {
                                ((AbstractDocument)doc).replace(begOffs, endOffs - begOffs, newWord, null);
                            } catch (BadLocationException e1) {}
                        }
                        
                    });
                }
            } catch (BadLocationException e) { }
        }
    }


    public void hierarchyChanged(HierarchyEvent ev) {
        // Wenn dieses SubMenü zu einem Parent hinzugefügt
        // wird ein Listener hinzugefügt um über das aufpopen des
        // Popupmenüs informiert zu werden
        if(ev.getChangeFlags() == HierarchyEvent.PARENT_CHANGED && ev.getChanged() == this){
            JPopupMenu parent = (JPopupMenu)getParent();
            if(parent != null){
                parent.addPopupMenuListener(this);
            }else{
                ((JPopupMenu)ev.getChangedParent()).removePopupMenuListener(this);
            }
        }
    }


    public void languageChanged( LanguageChangeEvent ev ) {
        dictionary = SpellChecker.getCurrentDictionary();
        locale = SpellChecker.getCurrentLocale();
    }
    
    
}
