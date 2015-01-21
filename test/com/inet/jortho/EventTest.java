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
 * Created on 14.10.2008
 */
package com.inet.jortho;

import javax.swing.*;

import junit.framework.TestCase;

public class EventTest extends TestCase {

    static {
        AllTests.init();
    }

    public void testChangeLanguage() throws Exception{
        JMenu menu1 = SpellChecker.createLanguagesMenu();
        JMenu menu2 = SpellChecker.createLanguagesMenu();

        assertEquals( "Menucount", menu1.getItemCount(), menu2.getItemCount() );
        assertTrue( "2 languages requied:" + menu1.getItemCount(), menu1.getItemCount() >= 2 );

        JRadioButtonMenuItem item1_1 = (JRadioButtonMenuItem)menu1.getItem( 0 );
        JRadioButtonMenuItem item1_2 = (JRadioButtonMenuItem)menu1.getItem( 1 );

        JRadioButtonMenuItem item2_1 = (JRadioButtonMenuItem)menu2.getItem( 0 );
        JRadioButtonMenuItem item2_2 = (JRadioButtonMenuItem)menu2.getItem( 1 );

        assertEquals( "Item 1", item1_1, item2_1 );
        assertEquals( "Item 2", item1_2, item2_2 );

        //Change the selected language
        JRadioButtonMenuItem notSelected = item1_1.isSelected() ? item1_2 : item1_1;
        JRadioButtonMenuItem selected = item1_1.isSelected() ? item1_1 : item1_2;
        assertFalse( "Selected", notSelected.isSelected() );
        assertTrue( "Selected", selected.isSelected() );
        notSelected.doClick(0);
        assertTrue( "Selected", notSelected.isSelected() );
        assertFalse( "Selected", selected.isSelected() );

        assertEquals( "Item 1", item1_1, item2_1 );
        assertEquals( "Item 2", item1_2, item2_2 );
        
        Thread.sleep( 10 ); // for loading thread
        
        notSelected = item2_1.isSelected() ? item2_2 : item2_1;
        selected = item2_1.isSelected() ? item2_1 : item2_2;        
        assertFalse( "Selected", notSelected.isSelected() );
        assertTrue( "Selected", selected.isSelected() );
        notSelected.doClick(0);
        assertTrue( "Selected", notSelected.isSelected() );
        assertFalse( "Selected", selected.isSelected() );

        assertEquals( "Item 1", item1_1, item2_1 );
        assertEquals( "Item 2", item1_2, item2_2 );
        
        Thread.sleep( 10 ); // for loading thread
        
    }

    /**
     * Compare 2 JRadioButtonMenuItem
     */
    private void assertEquals( String description, JRadioButtonMenuItem item1, JRadioButtonMenuItem item2 ) {
        assertEquals( description + ": Name", item1.getName(), item2.getName() );
        assertEquals( description + ": Selected", item1.isSelected(), item2.isSelected() );
    }
}
