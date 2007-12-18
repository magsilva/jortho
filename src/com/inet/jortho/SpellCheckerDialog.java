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

    private JTextComponent jText;
    private Dictionary dictionary;
    private Tokenizer tok;
    
    

    final private JLabel notFound = new JLabel();
    final private JTextField word = new JTextField(); 
    final private JList suggestionsList = new JList();
    
    final private JButton ignore      = new JButton(Utils.getResource("ignore"));
    final private JButton ignoreAll   = new JButton(Utils.getResource("ignoreAll"));
    final private JButton addToDic    = new JButton(Utils.getResource("addToDictionary"));
    final private JButton change      = new JButton(Utils.getResource("change"));
    final private JButton changeAll   = new JButton(Utils.getResource("changeAll"));
    final private JButton finish      = new JButton(Utils.getResource("finish"));
    
    /** List of ignore all words */  
    final private ArrayList<String> ignoreWords = new ArrayList<String>();
    /** Map of change all words */
    final private HashMap<String,String> changeWords = new HashMap<String,String>();

    SpellCheckerDialog(Dialog owner) throws HeadlessException {
        this(owner, false);
    }


    SpellCheckerDialog(Dialog owner, boolean modal){
        this(owner, Utils.getResource("spelling"), modal);
    }


    SpellCheckerDialog(Frame owner){
        this(owner, false);
    }
    

    public SpellCheckerDialog(Frame owner, boolean modal){
        this(owner, Utils.getResource("spelling"), modal);
    }


    public SpellCheckerDialog(Dialog owner, String title, boolean modal){
        super(owner, title, modal);
        init();
    }


    public SpellCheckerDialog(Frame owner, String title, boolean modal){
        super(owner, title, modal);
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
    
    
    public void show( JTextComponent jTextComponent, Dictionary dic, Locale loc ) {
        this.jText = jTextComponent;
        this.dictionary = dic;

        tok = new Tokenizer( jTextComponent.getText(), dic, loc );
        
        if( searchNext() ){
            setLocationRelativeTo( jTextComponent );
            setVisible( true );
        }
    }
    
    /**
     * Search the next misspelling word. If found it then refresh the dialig with the new informations.
     * ignoreWords and changeWords will handle automaticly.
     * @return true, if found a spell error.
     */
    private boolean searchNext() {
        String wordStr;
        while( true ) {
            wordStr = tok.nextInvalidWord();
            if( wordStr == null ) {
                dispose();
                JOptionPane.showMessageDialog( getParent(), Utils.getResource( "msgFinish" ), this.getTitle(), JOptionPane.INFORMATION_MESSAGE );
                return false;
            }
            if( ignoreWords.contains( wordStr ) ) {
                continue;
            }
            String changeTo = changeWords.get( wordStr );
            if( changeTo != null ) {
                replaceWord( wordStr, changeTo );
                continue;
            }
            break;
        }
        word.setText( wordStr );
        notFound.setText( wordStr );

        List list = dictionary.searchSuggestions( wordStr );

        Vector<String> suggestionsVector = new Vector<String>();
        for( int i = 0; i < list.size(); i++ ) {
            Suggestion sugestion = (Suggestion)list.get( i );
            String newWord = sugestion.getWord();
            if( i == 0 )
                word.setText( newWord );
            suggestionsVector.add( newWord );
        }
        suggestionsList.setListData( suggestionsVector );
        return true;
    }
    
    
    public void actionPerformed( ActionEvent ev ) {
        Object source = ev.getSource();
        if( source == ignore ) {
            searchNext();
        } else if( source == finish ) {
            setVisible(false);
        } else{
            String newWord = word.getText();
            String oldWord = notFound.getText();
            if( source == ignoreAll ) {
                ignoreWords.add( oldWord );
                searchNext();
            } else if( source == addToDic ) {
                UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
                if( provider != null ) {
                    provider.addWord( oldWord );
                }
                dictionary.add( oldWord );
                dictionary.trimToSize();
                searchNext();
            } else if( source == change ) {
                replaceWord( oldWord, newWord );
                searchNext();
            } else if( source == changeAll ) {
                changeWords.put( oldWord, newWord );
                replaceWord( oldWord, newWord );
                searchNext();
            }
        }
    }
    
    private void replaceWord( String oldWord, String newWord ) {
        Document doc = jText.getDocument();
        try {
            ((AbstractDocument)doc).replace( tok.getWordOffset(), oldWord.length(), newWord, null );
            tok.updatePhrase( jText.getText() );
        } catch( BadLocationException e1 ) {
            e1.printStackTrace();
        }
    }



}
