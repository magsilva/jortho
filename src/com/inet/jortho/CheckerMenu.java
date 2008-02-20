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

import java.awt.Component;
import java.awt.Point;
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
        /* empty */
    }
    

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        /* empty */
    }


    public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
       JPopupMenu popup = (JPopupMenu)ev.getSource();
        
        Component invoker = popup.getInvoker();
        if(invoker instanceof JTextComponent){
            final JTextComponent jText = (JTextComponent)invoker;
            if( !jText.isEditable() ){
                // Suggestions only for editable text components
                setEnabled( false );
                return;
            }
            Caret caret = jText.getCaret();
            int offs = Math.min(caret.getDot(), caret.getMark());
            Point p = jText.getMousePosition();
            if(p != null){
                // use position from mouse click and not from editor cursor position 
                offs = jText.viewToModel( p );
            }
            try {
                Document doc = jText.getDocument();
                if( offs >0 && (offs >= doc.getLength() || Character.isWhitespace(doc.getText(offs, 1).charAt(0)))){
                    // if the next character is a white space then use the word on the left site
                    offs--;
                }
                // get the word from current position
                final int begOffs = Utilities.getWordStart(jText, offs);
                final int endOffs = Utilities.getWordEnd(jText, offs);
                String word = jText.getText(begOffs, endOffs-begOffs);
                
                //find the first invalid word from current position
                Tokenizer tokenizer = new Tokenizer(jText, dictionary, locale );
                String invalidWord;
                do{
                    invalidWord = tokenizer.nextInvalidWord();
                }while(tokenizer.getWordOffset() < begOffs);
                super.removeAll();
                
                if(!word.equals( invalidWord )){
                    // the current word is not invalid
                    this.setEnabled(false);
                    return;
                }
                
                if(dictionary == null){
                    // without dictionary it is disabled
                    this.setEnabled(false);
                    return;
                }
                
                List<Suggestion> list = dictionary.searchSuggestions(word);

                //Disable then menu item if there are no suggestions
                this.setEnabled(list.size()>0);
                
                boolean needCapitalization = tokenizer.isFirstWordInSentence() && Utils.isCapitalization( word );
                
                for(int i=0; i<list.size(); i++){
                    Suggestion sugestion = list.get(i);
                    String sugestionWord = sugestion.getWord();
                    if( needCapitalization ){
                        sugestionWord = Utils.getCapitalization( sugestionWord );
                    }
                    JMenuItem item = super.add(sugestionWord);
                    final String newWord = sugestionWord;
                    item.addActionListener(new ActionListener(){
                        
                        public void actionPerformed(ActionEvent e) {
                            jText.setSelectionStart( begOffs );
                            jText.setSelectionEnd( endOffs );
                            jText.replaceSelection( newWord );
                        }
                        
                    });
                }
            } catch (BadLocationException ex) { 
                ex.printStackTrace();
            }
        }
    }


    public void hierarchyChanged(HierarchyEvent ev) {
        // If this sub menu is added to a parent
        // then an Listener is added to request show popup events of the parent
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
