/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2009 by i-net software
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
 *  Created on 24.12.2007
 */
package com.inet.jortho;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.*;



/**
 * Implements edit dialog for the user dictionary.
 * @author Volker Berlin
 */
class DictionaryEditDialog extends JDialog{
    
    private final JList list;
    private final JButton delete;
    private final JButton export;
    private final JButton importBtn;
    final private JButton close;
    private boolean isModify;

    DictionaryEditDialog( JDialog parent ){
        super( parent, Utils.getResource("userDictionary"), true );
        setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        Container content = getContentPane();
        content.setLayout( new GridBagLayout() );
        DefaultListModel data = new DefaultListModel();
        loadWordList( data );
        list = new JList( data );
        content.add( new JScrollPane(list), new GridBagConstraints( 1, 1, 1, 5, 1.0, 1.0, GridBagConstraints.NORTH ,GridBagConstraints.BOTH, new Insets( 8,8,8,8 ), 0, 0) );
        
        delete = Utils.getButton( "delete" );
        content.add( delete, new GridBagConstraints( 2, 1, 1, 1, 0, 0, GridBagConstraints.NORTH ,GridBagConstraints.BOTH, new Insets( 8,0,0,8 ), 0, 0) );
        DeleteAction deleteAction = new DeleteAction();
        delete.addActionListener( deleteAction );
        // DELETE Key
        getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0, false ), "DELETE" );
        getRootPane().getActionMap().put( "DELETE", deleteAction );

        export = Utils.getButton( "export" );
        content.add( export, new GridBagConstraints( 2, 2, 1, 1, 0, 0, GridBagConstraints.NORTH ,GridBagConstraints.BOTH, new Insets( 8,0,0,8 ), 0, 0) );
        export.addActionListener( new ExportAction() );

        importBtn = Utils.getButton( "import" );
        content.add( importBtn, new GridBagConstraints( 2, 3, 1, 1, 0, 0, GridBagConstraints.NORTH ,GridBagConstraints.BOTH, new Insets( 8,0,0,8 ), 0, 0) );
        importBtn.addActionListener( new ImportAction() );

        close       = Utils.getButton( "close" );
        content.add( close, new GridBagConstraints( 2, 4, 1, 1, 0, 0, GridBagConstraints.NORTH ,GridBagConstraints.BOTH, new Insets( 8,0,0,8 ), 0, 0) );
        AbstractAction closeAction = new AbstractAction() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        };
        close.addActionListener( closeAction );

        content.add( Utils.getLabel( null ), new GridBagConstraints( 2, 5, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST ,GridBagConstraints.HORIZONTAL, new Insets(8,0,0,8), 0, 0));

        //ESCAPE Key
        getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0, false ), "ESCAPE" );
        getRootPane().getActionMap().put( "ESCAPE", closeAction );

        pack();
        setLocationRelativeTo( parent );
    }
    
    /**
     * A hack for the layout manger to prevent that the dialog is to small to show the title line. The problem occur
     * only if there are small words in the list. With a empty list there are no problems.
     */
    @Override
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        String title = getTitle();
        int titleWidth = getFontMetrics(getFont()).stringWidth(title) + 80;
        if( dim.width < titleWidth ){
            dim.width = titleWidth;
        }
        return dim;
    }

    /**
     * Load all words from the user dictionary if available
     * @param data the target of the words
     */
    private void loadWordList( DefaultListModel data ){
        UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
        if( provider != null ) {
            Iterator<String> userWords = provider.getWords( SpellChecker.getCurrentLocale() );
            if( userWords != null ) {
                HashSet<String> wordList = new HashSet<String>();
                loadWordList( data, wordList, userWords );
            }
        }
    }

    /**
     * Load all words from wordList and the userWords in the ModelList. The list will be clear before.
     * @param model the target of the words
     * @param wordSet set with words, can be empty
     * @param wordIterator iterator with words
     */
    private void loadWordList( DefaultListModel model, HashSet<String> wordSet, Iterator<String> wordIterator ){
        while(wordIterator.hasNext()){
            String word = wordIterator.next();
            if( word != null && word.length() > 1 ) {
                wordSet.add( word );
            }
        }

        // List alphabetical sorting with the user language
        Object[] wordArray = wordSet.toArray();
        Arrays.sort( wordArray, Collator.getInstance() );
        model.clear();
        for(Object str : wordArray){
            model.addElement( str );
        }
    }

    private class DeleteAction extends AbstractAction{
        /**
         * Delete the selected entries. The "Delete" Button it the only Listener.
         */
        public void actionPerformed(ActionEvent e){
            int[] selected = list.getSelectedIndices();
            Arrays.sort( selected );
            for( int i=selected.length-1; i>=0; i-- ){
                ((DefaultListModel)list.getModel()).remove( selected[i] );
                isModify = true;
            }
        }
    }

    /**
     * The suggested file name for export and import.
     * @return a file without path
     */
    private static File getSuggestedFile() {
        return new File( Utils.getResource( "userDictionary" ) + "_" + SpellChecker.getCurrentLocale() + ".txt" );
    }
    
    private class ExportAction extends AbstractAction{
        /**
         * Export the list. The "Export" Button it the only Listener.
         */
        public void actionPerformed(ActionEvent e){
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile( getSuggestedFile() );
            int returnVal = chooser.showSaveDialog( DictionaryEditDialog.this );
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if( selectedFile == null ){
                    return;
                }
                try {
                    Writer writer = new OutputStreamWriter(  new FileOutputStream( selectedFile ), "UTF8" );
                    writer.write( getWordList() );
                    writer.close();
                } catch( Exception ex ) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog( DictionaryEditDialog.this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE );
                }
            }
        }
    }

    private class ImportAction extends AbstractAction{
        /**
         * Import a word list. The "Import" Button it the only Listener.
         */
        public void actionPerformed(ActionEvent e){
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile( getSuggestedFile() );
            int returnVal = chooser.showOpenDialog( DictionaryEditDialog.this );
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if( selectedFile == null ){
                    return;
                }
                try {
                    HashSet<String> wordSet = new HashSet<String>();
                    DefaultListModel<String> model = (DefaultListModel<String>)list.getModel();
                    for( int i=0; i<model.getSize(); i++){
                        wordSet.add( model.getElementAt(i) );
                    }

                    FileInputStream input = new FileInputStream( selectedFile );
                    Iterator<String> words = new WordIterator( input, "UTF8" );

                    loadWordList( model, wordSet, words );
                } catch( Exception ex ) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog( DictionaryEditDialog.this, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(){
        super.dispose();
        if( isModify ) {
            // save the user dictionary
            UserDictionaryProvider provider = SpellChecker.getUserDictionaryProvider();
            if( provider != null ) {
                provider.setUserWords( getWordList() );
            }
            // reload the dictionary
            JMenu menu = SpellChecker.createLanguagesMenu( null );
            Component[] comps = menu.getMenuComponents();
            for( Component comp : comps ) {
                if( comp instanceof JRadioButtonMenuItem ){
                    JRadioButtonMenuItem item = (JRadioButtonMenuItem)comp;
                    if( item.isSelected() ){
                        item.doClick();
                    }
                }
            }
        }
    }
    
    /**
     * Get a List of all words as String
     * @return the word list.
     */
    private String getWordList() {
        ListModel model = list.getModel();
        StringBuilder builder = new StringBuilder();
        for( int i=0; i<model.getSize(); i++){
            if( builder.length() != 0 ){
                builder.append( '\n' );
            }
            builder.append( model.getElementAt(i) );
        }
        return builder.toString();
    }
}
