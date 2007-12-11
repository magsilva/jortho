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
 *  Created on 10.11.2005
 */
package com.inet.jortho;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

/**
 * The Dialog for continues checking the orthography.
 * @author Volker Berlin
 */
class SpellCheckerDialog extends JDialog implements ActionListener {


    SpellCheckerDialog(Dialog owner) throws HeadlessException {
        super(owner, Utils.getResource("spelling"));
        init();
    }


    SpellCheckerDialog(Dialog owner, boolean modal){
        super(owner, Utils.getResource("spelling"), modal);
        init();
    }


    SpellCheckerDialog(Frame owner){
        super(owner, Utils.getResource("spelling"));
        init();
    }
    

    public SpellCheckerDialog(Frame owner, boolean modal){
        super(owner, Utils.getResource("spelling"), modal);
        init();
    }


    public SpellCheckerDialog(Dialog owner, String title){
        super(owner, title);
        init();
    }


    public SpellCheckerDialog(Dialog owner, String title, boolean modal){
        super(owner, title, modal);
        init();
    }


    public SpellCheckerDialog(Frame owner, String title){
        super(owner, title);
        init();
    }


    public SpellCheckerDialog(Frame owner, String title, boolean modal){
        super(owner, title, modal);
        init();
    }
    

    public SpellCheckerDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc){
        super(owner, title, modal, gc);
        init();
    }


    public SpellCheckerDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
        super(owner, title, modal, gc);
        init();
    }
    
    
    final private void init(){
        Container cont = getContentPane();
        cont.setLayout(new GridBagLayout());
        Insets insetL = new Insets(8,8,0,8);
        Insets insetR = new Insets(8,0,0,8);
        
        cont.add( new JLabel(Utils.getResource("notInDictionary")+":"), new GridBagConstraints( 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST ,GridBagConstraints.NONE, insetL, 0, 0));
        
        notFound.setForeground(Color.RED);
        notFound.setText("xxxxxxxxxx");
        cont.add( notFound, new GridBagConstraints( 2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST ,GridBagConstraints.NONE, insetL, 0, 0));
        
        cont.add( word, new GridBagConstraints( 1, 2, 2, 1, 1.0, 0.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetL, 0, 0));
        
        cont.add( new JLabel(Utils.getResource("suggestions")+":"), new GridBagConstraints( 1, 3, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST ,GridBagConstraints.NONE, insetL, 0, 0));
        JScrollPane scrollPane = new JScrollPane(suggestionsList);
        cont.add( scrollPane, new GridBagConstraints( 1, 4, 2, 4, 1.0, 1.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.BOTH, new Insets(8,8,8,8), 0, 0));
        
        cont.add( ignore,       new GridBagConstraints( 3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetR, 0, 0));
        cont.add( ignoreAll,    new GridBagConstraints( 3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetR, 0, 0));
        cont.add( addToDic,     new GridBagConstraints( 3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetR, 0, 0));
        cont.add( change,       new GridBagConstraints( 3, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetR, 0, 0));
        cont.add( changeAll,    new GridBagConstraints( 3, 5, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetR, 0, 0));
        cont.add( finish,       new GridBagConstraints( 3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetR, 0, 0));
        cont.add( new JLabel(), new GridBagConstraints( 3, 7, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, insetR, 0, 0));
        
        ignore.addActionListener(this);
        ignoreAll.addActionListener(this);
        addToDic.addActionListener(this);
        change.addActionListener(this);
        changeAll.addActionListener(this);
        finish.addActionListener(this);
        
        addToDic.setEnabled( SpellChecker.getUserDictionaryProvider() != null );
        pack();
    }
    
    
    public void show(JTextComponent jText, Dictionary dictionary){
        this.jText = jText;
        this.dictionary = dictionary;
        
        endIdx = 0;
        searchNext();
        
        setVisible(true);
    }
    
    
    private void searchNext(){
        try {
            if(endIdx == 0)
                beginIdx = Utilities.getWordStart(jText, endIdx);
            else
                beginIdx = Utilities.getNextWord(jText, endIdx);
            endIdx = Utilities.getWordEnd(jText, beginIdx);
            String wordStr = jText.getText(beginIdx, endIdx-beginIdx);
            word.setText(wordStr);
            notFound.setText(wordStr);
            
            List list = dictionary.suggestions(wordStr);
            
            suggestionsVector.clear();
            for(int i=0; i<list.size(); i++){
                Suggestion sugestion = (Suggestion)list.get(i);
                String newWord = sugestion.getWord();
                if(i == 0)
                    word.setText(newWord);
                suggestionsVector.add(newWord);
            }
            suggestionsList.setListData( suggestionsVector );
            
        } catch (BadLocationException e) {
            hide();
            JOptionPane.showMessageDialog( getParent(), Utils.getResource("msgFinish"), this.getTitle(), JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    public void actionPerformed( ActionEvent ev ) {
        Object source = ev.getSource();
        if( source == ignore ) {
            searchNext();
        } else if( source == ignoreAll ) {
            searchNext();
        } else if( source == addToDic ) {
            String wordStr = word.getText();
            UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
            if( provider != null ) {
                provider.addWord( wordStr );
            }
            dictionary.add( wordStr );
            dictionary.trimToSize();
            searchNext();
        } else if( source == change || source == changeAll ) {
            Document doc = jText.getDocument();
            try {
                String wordStr = word.getText();
                ((AbstractDocument)doc).replace( beginIdx, endIdx - beginIdx, word.getText(), null );
                endIdx = beginIdx + wordStr.length();
            } catch( BadLocationException e1 ) {
            }
            searchNext();
        } else if( source == finish ) {
            setVisible(false);
        }
    }
    
    
    private JTextComponent jText;
    private Dictionary dictionary;
    private int endIdx;
    private int beginIdx;
    
    

    final private JLabel notFound = new JLabel();
    final private JTextField word = new JTextField(); 
    final private Vector<String> suggestionsVector = new Vector<String>();
    final private JList suggestionsList = new JList(suggestionsVector);
    
    final private JButton ignore      = new JButton(Utils.getResource("ignore"));
    final private JButton ignoreAll   = new JButton(Utils.getResource("ignoreAll"));
    final private JButton addToDic    = new JButton(Utils.getResource("addToDictionary"));
    final private JButton change      = new JButton(Utils.getResource("change"));
    final private JButton changeAll   = new JButton(Utils.getResource("changeAll"));
    final private JButton finish      = new JButton(Utils.getResource("finish"));
}
