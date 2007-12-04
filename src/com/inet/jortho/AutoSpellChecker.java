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
 *  Created on 05.11.2005
 */
package com.inet.jortho;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenu;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.Highlighter.Highlight;

/**
 * 
 * @author Volker Berlin
 */
public class AutoSpellChecker implements DocumentListener{
    private static final RedZigZagPainter painter = new RedZigZagPainter();

    private JTextComponent jText;
    Dictionary dictionary;
    private CheckerMenu menu;
	
	
    
    public JMenu getMenu(){
        if(menu == null)
            menu = new CheckerMenu(this);
        return menu;
    }
    
    /**
     * Set the TextComponet that should be checked.
     */
    public void setTextComponent(JTextComponent jText){
        if(jText != null){
            jText.getDocument().removeDocumentListener(this);
        }
        this.jText = jText;
        if(jText != null){
            jText.getDocument().addDocumentListener(this);
        }
    }
    
    
    public JTextComponent getTextDocument(){
        return jText;
    }
	

	public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }


    public Dictionary getDictionary() {
        return dictionary;
    }
    
    

    /*====================================================================
     * 
     * Methods of interface DocumentListener
     * 
     */

    
    public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}
	

	public void insertUpdate(DocumentEvent e) {
        int i = jText.getSelectionStart();
		Document document = jText.getDocument();
		Element element = null;

        try{
            element = ((javax.swing.text.StyledDocument)document).getCharacterElement(i);
        }catch(java.lang.Exception exception){
            try{
                element = ((AbstractDocument)document).getParagraphElement(i);
            }catch(java.lang.Exception ex){
                return;
            }
        }
        if(element != null)
			try {
				checkElement(element);
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
	

	public void removeUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	
    private void checkElement(javax.swing.text.Element element) throws BadLocationException{
	    int i = element.getStartOffset();
	    int j = element.getEndOffset();
	    Highlighter highlighter = jText.getHighlighter();
		Highlight[] highlights = highlighter.getHighlights();
	    for(int k = highlights.length; --k >= 0;)
	    {
	        Highlight highlight = highlights[k];
	        if(highlight.getStartOffset() >= i && highlight.getEndOffset() <= j){
	            highlighter.removeHighlight(highlight);
	        }
	    }
	
	    int l = ((AbstractDocument)jText.getDocument()).getLength();
	    j = Math.min(j, l);
	    if(i >= j)
	        return;
	    String phrase = jText.getText(i, j - i);
        
        Pattern pattern = Pattern.compile("[-A-Za-z]*");
        Matcher matcher = pattern.matcher( phrase);
        while(matcher.find()){
            int start = matcher.start();
            int end = matcher.end();
            String word = matcher.group();
            if(!dictionary.exist(word)){
                highlighter.addHighlight(i+start, i+end, painter);
            }
        }
        
		/*StringTokenizer tokenizer = new StringTokenizer(phrase);
		while(tokenizer.hasMoreElements()){
			String word = tokenizer.nextToken();
			if(!dictionary.exist(word))
				highlighter.addHighlight(i, i + word.length(), painter);
			i += word.length() +1;
		}*/
	}



}

