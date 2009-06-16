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
package com.inet.jorthotests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.inet.jortho.*;

public class AllTests {

    private static boolean isInit;

    /**
     * register the dictionaries
     */
    static void init() {
        if( !isInit ) {
            isInit = true;
            int threadCount = Thread.activeCount();
            SpellChecker.registerDictionaries( null, null );

            // wait until the dictionaries are loaded.
            for( int i = 0; i < 50; i++ ) {
                if( threadCount >= Thread.activeCount() ) {
                    break;
                }
                try {
                    Thread.sleep( 100 );
                } catch( InterruptedException e ) {
                    break;
                }
            }
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite( "JOrtho Tests" );
        suite.addTestSuite( EventTest.class );
        suite.addTestSuite( MemoryTest.class );
        suite.addTestSuite( UtilsTest.class );
        return suite;
    }
}
