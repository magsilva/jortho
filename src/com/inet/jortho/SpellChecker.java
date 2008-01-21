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
 *  Created on 05.12.2007
 */
package com.inet.jortho;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * This class is the major class of the spellchecker JOrtho (Java Orthography Checker). 
 * In the most cases this is the only class that you need to add spellchecking to your application.
 * First you need to do a one-time registration of your dictionaries. In standalone applications this can
 * look like:
 * <code><pre>
 * SpellChecker.registerDictionaries( new URL("file", null, ""), "en,de", "de" );
 * </pre></code>
 * and in an applet this will look like:
 * <code><pre>
 * SpellChecker.registerDictionaries( getCodeBase(), "en,de", "en" );
 * </pre></code>
 * After this you can register your text component that should have the spell checker features
 * (Highlighter, context menu, spell checking dialog). 
 * This looks like:<code><pre>
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
    private static String applicationName;
    
    /**
     * There is no instance needed of SpellChecker. All methods are static.
     */
    private SpellChecker(){/*nothing*/}
    
    /**
     * Sets the UserDictionaryProvider. This is needed if the user should be able to add their own words.
     * This method must be called before {@link #registerDictionaries(URL, String, String)}.
     * 
     * @param userDictionaryProvider the new UserDictionaryProvider or null
     * @see #getUserDictionaryProvider()
     * @see #registerDictionaries(URL, String, String)
     */
    public static void setUserDictionaryProvider( UserDictionaryProvider userDictionaryProvider ) {
        SpellChecker.userDictionaryProvider = userDictionaryProvider;
    }

    /**
     * Gets the currently set UserDictionaryProvider. If none has been set then null is returned.
     * 
     * @see #setUserDictionaryProvider(UserDictionaryProvider)
     */
    static UserDictionaryProvider getUserDictionaryProvider() {
        return SpellChecker.userDictionaryProvider;
    }
    
    /**
     * Registers the available dictionaries. The dictionaries' URLs must have the form "dictionary_xx.xxxxx" and must be
     * relative to the baseURL. The available languages and extension of the dictionaries is load from a config file.
     * The config file must also relative to the baseURL and must be named dictionaries.cnf, dictionaries.properties or
     * dictionaries.txt. If the dictionary of the active Locale does not exist, the first dictionary is loaded. The
     * config file has a Java Properties format. Currently there are the follow options:
     * <ul>
     * <li>languages</li>
     * <li>extension</li>
     * </ul>
     * 
     * @param baseURL
     *            the base URL where the dictionaries and config file can be found
     * @param activeLocale
     *            the locale that should be loaded and made active. If null or empty then the default locale is used.
     */
    public static void registerDictionaries( URL baseURL, String activeLocale ) {
        InputStream input;
        try {
            input = new URL( baseURL, "dictionaries.cnf" ).openStream();
        } catch( Exception e1 ) {
            try {
                input = new URL( baseURL, "dictionaries.properties" ).openStream();
            } catch( Exception e2 ) {
                try {
                    input = new URL( baseURL, "dictionaries.txt" ).openStream();
                } catch( Exception e3 ) {
                    System.err.println( "JOrtho configuration file not found!" );
                    e1.printStackTrace();
                    e2.printStackTrace();
                    e3.printStackTrace();
                    return;
                }
            }
        }
        Properties props = new Properties();
        try {
            props.load( input );
        } catch( IOException e ) {
            e.printStackTrace();
            return;
        }
        String availableLocales = props.getProperty( "languages" );
        String extension = props.getProperty( "extension", ".ortho" );
        registerDictionaries( baseURL, availableLocales, activeLocale, extension );
    }

    /**
     * Registers the available dictionaries. The dictionaries' URLs must have the form "dictionary_xx.ortho" and must be
     * relative to the baseURL. If the dictionary of the active Locale does not exist, the first dictionary is loaded.
     * 
     * @param baseURL
     *            the base URL where the dictionaries can be found
     * @param availableLocales
     *            a comma separated list of locales
     * @param activeLocale
     *            the locale that should be loaded and made active. If null or empty then the default locale is used.
     * @see #setUserDictionaryProvider(UserDictionaryProvider)
     */
    public static void registerDictionaries( URL baseURL, String availableLocales, String activeLocale ) {
        registerDictionaries( baseURL, availableLocales, activeLocale, ".ortho" );
    }

    /**
     * Registers the available dictionaries. The dictionaries' URLs must have the form "dictionary_xx.xxxxx" and must be
     * relative to the baseURL. The extension can be set via parameter.
     * If the dictionary of the active Locale does not exist, the first dictionary is loaded.
     * 
     * @param baseURL
     *            the base URL where the dictionaries can be found
     * @param availableLocales
     *            a comma separated list of locales
     * @param activeLocale
     *            the locale that should be loaded and made active. If null or empty then the default locale is used.
     * @param extension
     *            the file extension of the dictionaries. Some web server like the IIS6 does not support the default ".ortho".
     * @see #setUserDictionaryProvider(UserDictionaryProvider)
     */
    public static void registerDictionaries( URL baseURL, String availableLocales, String activeLocale, String extension ) {
        if( activeLocale == null ) {
            activeLocale = "";
        }
        activeLocale = activeLocale.trim();
        if( activeLocale.length() == 0 ) {
            activeLocale = Locale.getDefault().getLanguage();
        }
        
        boolean activeSelected = false;
        for( String locale : availableLocales.split( "," ) ) {
            locale = locale.trim().toLowerCase();
            if(locale.length() > 0){
                LanguageAction action = new LanguageAction( baseURL, new Locale( locale ), extension );
                languages.remove( action );
                languages.add( action );
                if( locale.equals( activeLocale ) ) {
                    action.setSelected( true );
                    action.actionPerformed( null );
                    activeSelected = true;
                }
            }
        }
        // if nothing selected then select the first entry
        if( !activeSelected && languages.size() > 0 ) {
            LanguageAction action = languages.get( 0 );
            action.setSelected( true );
            action.actionPerformed( null );
        }
        
        //sort the display names in order of the current language 
        Collections.sort( languages );
    }
    
    /**
     * Activate the spell checker for the given <code>JTextComponent</code>. The call is equal to register( text,
     * true, true ).
     * 
     * @param text
     *            the JTextComponent
     * @throws NullPointerException
     *             if text is null
     */
    public static void register( final JTextComponent text) throws NullPointerException{
        register( text, true, true, true );
    }

    /**
     * Activates the spell checker for the given <code>JTextComponent</code>. You do not need to unregister if the
     * JTextComponent is not needed anymore.
     * 
     * @param text
     *            the JTextComponent
     * @param hasPopup
     *            if true, the JTextComponent is to have a popup menu with the menu item "Orthography" and "Languages".
     * @param hasShortKey
     *            if true, pressing the F7 key will display the spell check dialog.
     * @param hasAutoSpell
     *            if true, the JTextComponent has a auto spell checking.
     * @throws NullPointerException
     *             if text is null
     */
    public static void register( final JTextComponent text, boolean hasPopup, boolean hasShortKey, boolean hasAutoSpell ) throws NullPointerException {
        if( hasPopup ) {
            enablePopup( text, true );
        }
        if( hasShortKey ) {
            enableShortKey( text, true );
        }
        if( hasAutoSpell ) {
            enableAutoSpell( text, true );
        }
    }
    
    /**
     * Removes all spell checker features from the JTextComponent. This does not need to be called
     * if the text component is no longer needed.
     * @param text the JTextComponent
     */
    public static void unregister( JTextComponent text ){
        enableShortKey( text, false );
        enablePopup( text, false );
        enableAutoSpell( text, false );
    }
    
    /**
     * Enable or disable the F7 key. Pressing the F7 key will display the spell check dialog. This also
     * register an Action with the name "spell-checking".
     * @param text the JTextComponent that should change
     * @param enable true, enable the feature.
     */
    public static void enableShortKey( final JTextComponent text, boolean enable ){
        if( enable ){
            text.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_F7, 0 ), "spell-checking" );
            text.getActionMap().put( "spell-checking", new AbstractAction(){
                public void actionPerformed( ActionEvent e ) {
                    if( !text.isEditable() ){
                        // only editable text component have spell checking
                        return;
                    }
                    Dictionary dictionary = currentDictionary;
                    if( dictionary != null ) {
                        Window parent = SwingUtilities.getWindowAncestor( text );
                        SpellCheckerDialog dialog;
                        if( parent instanceof Frame ) {
                            dialog = new SpellCheckerDialog( (Frame)parent, true );
                        } else {
                            dialog = new SpellCheckerDialog( (Dialog)parent, true );
                        }
                        dialog.show( text, dictionary, currentLocale );
                    }
                }
            });
        }else{
            text.getActionMap().remove( "spell-checking" ); 
        }
    }
    /**
     * Enable or disable the a popup menu with the menu item "Orthography" and "Languages". 
     * @param text the JTextComponent that should change
     * @param enable true, enable the feature.
     */
    public static void enablePopup( JTextComponent text, boolean enable ){
        if( enable ){
            final JPopupMenu menu = new JPopupMenu();
            menu.add( createCheckerMenu() );
            menu.add( createLanguagesMenu() );
            text.addMouseListener( new PopupListener(menu) );
        } else {
            for(MouseListener listener : text.getMouseListeners()){
                if(listener instanceof PopupListener){
                    text.removeMouseListener( listener );
                }
            }
        }
    }
    
    /**
     * Enable or disable the auto spell checking feature (red zigzag line) for a text component.
     * If you change the document then you need to reenable it.
     * 
     * @param text
     *            the JTextComponent that should change
     * @param enable
     *            true, enable the feature.
     */
    public static void enableAutoSpell( JTextComponent text, boolean enable ){
        if( enable ){
            new AutoSpellChecker(text);
        } else {
            AutoSpellChecker.disable( text );
        }
    }
    
    /**
     * Adds the LanguageChangeListener. You do not need to remove if the
     * LanguageChangeListener is not needed anymore.
     * @param listener listener to add
     * @see LanguageChangeListener
     */
    public static void addLanguageChangeLister(LanguageChangeListener listener){
        listeners.put( listener, null );
    }
    
    /**
     * Removes the LanguageChangeListener.
     * @param listener listener to remove
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
     * Creates a menu item "Orthography" (or the equivalent depending on the user language) with a
     * sub-menu that includes suggestions for a correct spelling.
     * You can use this to add this menu item to your own popup.
     * @return the new menu.
     */
    public static JMenu createCheckerMenu(){
        return new CheckerMenu();
    }
    
    /**
     * Creates a menu item "Languages" (or the equivalent depending on the user language) with a sub-menu
     * that lists all available dictionary languages. 
     * You can use this to add this menu item to your own popup.
     * @return the new menu.
     */
    public static JMenu createLanguagesMenu(){
        JMenu menu = new JMenu(Utils.getResource("languages"));
        ButtonGroup group = new ButtonGroup();
        menu.setEnabled( languages.size() > 0 );
        
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
    private static class LanguageAction extends AbstractAction implements Comparable<LanguageAction>{
        
        private final URL baseURL;
        private final Locale locale;
        private final ButtonModel model = new JToggleButton.ToggleButtonModel();
        private String extension;
        
        LanguageAction(URL baseURL, Locale locale, String extension){
            super( locale.getDisplayLanguage() );
            this.baseURL = baseURL;
            this.locale = locale;
            this.extension = extension;
        }

        /**
         * Get the shared ButtonModel.
         */
        ButtonModel getButtonModel() {
            return model;
        }

        /**
         * Selects or deselects the menu item.
         * 
         * @param b
         *            true selects the menu item, false deselects the menu item.
         */
        public void setSelected( boolean b ) {
            model.setSelected( b );
        }

        public void actionPerformed( ActionEvent ev ) {
            if( !isEnabled() ){
                //because multiple MenuItems share the same action that
                //also the event occur multiple time
                return;
            }
            setEnabled( false );
            
            Thread thread = new Thread( new Runnable() {
                public void run() {
                    try {
                        DictionaryFactory factory = new DictionaryFactory();
                        try {
                            factory.loadWordList( new URL( baseURL, "dictionary_" + locale + extension ) );
                            UserDictionaryProvider provider = userDictionaryProvider;
                            if( provider != null ) {
                                String userWords = provider.getUserWords( locale );
                                if( userWords != null ) {
                                    factory.loadPlainWordList( new StringReader( userWords ) );
                                }
                            }
                        } catch( Exception ex ) {
                            JOptionPane.showMessageDialog( null, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE );
                        }
                        Locale oldLocale = locale;
                        currentDictionary = factory.create();
                        currentLocale = locale;
                        fireLanguageChanged( oldLocale );
                    } finally {
                        setEnabled( true );
                    }
                }
            });
            thread.setPriority( Thread.NORM_PRIORITY );
            thread.setDaemon( true );
            thread.start();
        }
        
        @Override
        public boolean equals(Object obj){
            if(obj instanceof LanguageAction){
                return locale.equals( ((LanguageAction)obj).locale );
            }
            return false;
        }
        
        @Override
        public int hashCode(){
            return locale.hashCode();
        }

        /**
         * Sort the displaynames in the order of the current language
         */
        public int compareTo( LanguageAction obj ) {
            return toString().compareTo( obj.toString() );
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
     * Gets the current <code>Locale</code>. The current Locale will be set if the user selects
     * one, or when calling <ode>registerDictionaries</code>.
     * @return the current <code>Locale</code> or null if none is set.
     * @see #registerDictionaries(URL, String, String)
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Set the title of your application. This valuse is used as title for info boxes (JOptionPane).
     * If not set then the translated "Spelling" is used.
     */
    public static void setApplicationName( String name ){
        applicationName = name;
    }

    /**
     * Get the title of your application.
     */
    public static String getApplicationName(){
        return applicationName;
    }
}
