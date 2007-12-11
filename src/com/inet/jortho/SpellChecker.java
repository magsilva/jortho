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
 *  Created on 05.12.2007
 */
package com.inet.jortho;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;

/**
 * This class is the major class of the Spell Checker JOrtho (Java Orthography Checker). 
 * In the most cases this is the only class that you need to add spell feature to your application.
 * First you need to register your dictionaries one times. This can look in standalone application like:<code><pre>
 * SpellChecker.registerDictionaries( new URL("file", null, ""), "en,de", "de" );
 * </pre></code>and in a applet like:<code><pre>
 * SpellChecker.registerDictionaries( getCodeBase(), "en,de", "en" );
 * </pre></code>
 * After this you can register your text component that should have all spell checker feature (Highlighter. context menu, spelling dialog). 
 * This look like:<code><pre>
 * JTextPane text = new JTextPane();
 * SpellChecker.register( text );
 * </pre></code>
 * @author Volker Berlin
 */
public class SpellChecker {
    
    private final static ArrayList<LanguageAction> languages = new ArrayList<LanguageAction>();
    private static Dictionary currentDictionary;
    private static Locale currentLocale;
    private static UserDictionaryProvider userDictionaryProvider;
    private final static java.util.Map<LanguageChangeListener, Object> listeners = Collections.synchronizedMap( new WeakHashMap<LanguageChangeListener, Object>() );
    
    
    /**
     * There is no instance needed of SpellChecker. All methods are static.
     */
    private SpellChecker(){/*nothing*/}
    
    /**
     * Set the UserDictionaryProvider. This is needed if the user should be able to add its own words. This method must
     * be call before registerDictionaries.
     * 
     * @param userDictionaryProvider
     *            The new UserDictionaryProvider or null
     * @see #getUserDictionaryProvider()
     * @see #registerDictionaries(URL, String, String)
     */
    public static void setUserDictionaryProvider( UserDictionaryProvider userDictionaryProvider ) {
        SpellChecker.userDictionaryProvider = userDictionaryProvider;
    }

    /**
     * Get the current UserDictionaryProvider. if not set then it return null.
     * 
     * @see #setUserDictionaryProvider(UserDictionaryProvider)
     */
    static UserDictionaryProvider getUserDictionaryProvider() {
        return SpellChecker.userDictionaryProvider;
    }

    /**
     * Register the available dictionaries. The dictionaries URLs must have the form "dictionary_xx.ortho" and relative
     * to the baseURL. Without the dictionary of the activeLocale no other dictionary is loaded.
     * 
     * @param baseURL
     *            the base URL where the dictionaries can be found
     * @param availableLocales
     *            a comma separated list of locales
     * @param aktiveLocale
     *            the locale that should be loaded and mak active.
     * @see #setUserDictionaryProvider(UserDictionaryProvider)
     */
    public static void registerDictionaries( URL baseURL, String availableLocales, String aktiveLocale ) {
        aktiveLocale = aktiveLocale.trim();
        String[] locales = availableLocales.split( "," );
        for( String locale : locales ) {
            LanguageAction action = new LanguageAction( baseURL, new Locale( locale ) );
            languages.add( action );
            if( locale.equals( aktiveLocale ) ) {
                action.setSelected( true );
                action.actionPerformed( null );
            }
        }

    }
    
    /**
     * Activate the spell checker for the given <code>JTextComponent</code>. The call is equals to
     * register( text, true, true ).
     * @param text the JTextComponent
     * @throws NullPointerException if text is null
     */
    public static void register( final JTextComponent text) throws NullPointerException{
        register( text, true, true );
    }
    
    /**
     * Activate the spell checker for the given <code>JTextComponent</code>. 
     * You does not need to unregister if the JTextComponent is not needed anymore.
     * @param text the JTextComponent
     * @param hasPopup true, the JTextComponent should have a Popup menu the menu item "Orthography" and "Languages". 
     * @param hasShortKey true, the pressing of the F7 key will display the spell check dialog.
     * @throws NullPointerException if text is null
     */
    public static void register( final JTextComponent text, boolean hasPopup, boolean hasShortKey) throws NullPointerException{
        new AutoSpellChecker(text);
        if(hasPopup){
            final JPopupMenu menu = new JPopupMenu();
            menu.add( createCheckerMenu() );
            menu.add( createLanguagesMenu() );
            text.addMouseListener( new PopupListener(menu) );
        }
        if(hasShortKey){
            text.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_F7, 0 ), "spell-checking" );
            text.getActionMap().put( "spell-checking", new AbstractAction(){
                public void actionPerformed( ActionEvent e ) {
                    Dictionary dictionary = currentDictionary;
                    if( dictionary != null ) {
                        Window parent = SwingUtilities.getWindowAncestor( text );
                        SpellCheckerDialog dialog;
                        if( parent instanceof Frame ) {
                            dialog = new SpellCheckerDialog( (Frame)parent );
                        } else {
                            dialog = new SpellCheckerDialog( (Dialog)parent );
                        }
                        dialog.show( text, dictionary );
                    }
                }
            });
        }
    }
    
    /**
     * Remove all spell checker features from the JTextComponent.
     * @param text the JTextComponent
     */
    public static void unregister( JTextComponent text ){
        text.getActionMap().remove( "spell-checking" ); 
        for(MouseListener listener : text.getMouseListeners()){
            if(listener instanceof PopupListener){
                text.removeMouseListener( listener );
            }
        }
        AbstractDocument doc = (AbstractDocument)text.getDocument();
        for(DocumentListener listener : doc.getDocumentListeners()){
            if(listener instanceof AutoSpellChecker){
                doc.removeDocumentListener( listener );
            }
        }
    }
    
    /**
     * Add the LanguageChangeListener. You does not need to remove if the LanguageChangeListener is not needed anymore.
     */
    public static void addLanguageChangeLister(LanguageChangeListener listener){
        listeners.put( listener, null );
    }
    
    /**
     * Remove the LanguageChangeListener.
     */
    public static void removeLanguageChangeLister(LanguageChangeListener listener){
        listeners.remove( listener );
    }
    
    /**
     * Helper method to fire an Language change event.
     */
    private static void fireLanguageChanged( Locale oldLocale ) {
        LanguageChangeEvent ev = new LanguageChangeEvent( currentLocale, oldLocale );
        for( LanguageChangeListener listener : listeners.keySet() ) {
            listener.languageChanged( ev );
        }
    }
    
    /**
     * Create menu item "Orthography" (depends on the user language) with a submenu that include suggestions for a correct spelling.
     * You can use it to add this menu item to your own popup.
     * @return the new menu.
     */
    public static JMenu createCheckerMenu(){
        return new CheckerMenu();
    }
    
    /**
     * Create menu item "Languages" (depends on the user language) with a submenu that list all available dictionary languagages. 
     * You can use it to add this menu item to your own popup.
     * @return the new menu.
     */
    public static JMenu createLanguagesMenu(){
        JMenu menu = new JMenu(Utils.getResource("languages"));
        ButtonGroup group = new ButtonGroup();
        
        //TODO sorting
        for(LanguageAction action : languages){
            JRadioButtonMenuItem item = new JRadioButtonMenuItem( action );
            //Hack that all items of the action have the same state.
            //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4133141
            item.setModel( action.getButtonModel() );
            menu.add( item );
            group.add( item );
        }
        
        return menu;
    }

    /**
     * Action for change the current dictionary language.
     */
    private static class LanguageAction extends AbstractAction{
        
        private final URL baseURL;
        private final Locale locale;
        private final ButtonModel model = new JToggleButton.ToggleButtonModel();
        
        LanguageAction(URL baseURL, Locale locale){
            super( locale.getDisplayLanguage() );
            this.baseURL = baseURL;
            this.locale = locale;
        }

        /**
         * Get the shared ButtonModel.
         */
        ButtonModel getButtonModel() {
            return model;
        }

        public void setSelected( boolean b ) {
            model.setSelected( true );
        }

        public void actionPerformed( ActionEvent ev ) {
            if( currentLocale == locale ){
                //because multiple MenuItems share the same action that
                //also the event occur multiple time
                return;
            }
            // TODO Auto-generated method stub
            DictionaryFactory factory = new DictionaryFactory();
            try {
                factory.loadWordList( new URL(baseURL, "dictionary_" + locale + ".ortho") );
                UserDictionaryProvider provider = userDictionaryProvider;
                if(provider != null){
                    String userWords = provider.getUserWords(locale);
                    if(userWords != null){
                        factory.loadPlainWordList( new StringReader(userWords) );
                    }
                }
            } catch( Exception ex ) {
                JOptionPane.showMessageDialog( null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE );
            }
            Locale oldLocale = locale;
            currentDictionary = factory.create();
            currentLocale = locale;
            fireLanguageChanged( oldLocale );
        }
        
    }

    /**
     * Get the current <code>Dictionary</code>. The current dictionary will be set if the user one select or on calling <code>registerDictionaries</code>.
     * @return the current <code>Dictionary</code> or null if not set.
     * @see #registerDictionaries(URL, String, String)
     */
    static Dictionary getCurrentDictionary() {
        return currentDictionary;
    }

    /**
     * Get the current <code>Locale</code>. The current Locale will be set if the user one select or on calling <ode>registerDictionaries</code>.
     * @return the current <code>Locale</code> or null if not set.
     * @see #registerDictionaries(URL, String, String)
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}
