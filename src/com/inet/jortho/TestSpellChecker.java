package com.inet.jortho;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.inet.jortho.*;

/*
 * Created on 04.11.2005
 */

/**
 * @author Volker
 */
public class TestSpellChecker {
    
    public static void main3(String[] args) throws Exception {
        loadFactory();
        load();
        loadFactory();
        load();
        char[] f = loadFactory();
        char[] l = load();
    }
    
    static char[] loadFactory() throws IOException{
        DictionaryFactory factory = new DictionaryFactory();
        long zeit = System.currentTimeMillis();
        factory.loadWordList( new URL("file",null,"words_de.txt" ) );
        Dictionary dictionary = factory.create();
        System.out.println("F:"+(System.currentTimeMillis()-zeit)+"  "+(dictionary.getDataSize()*2)); 
        return dictionary.toArray();
    }

    static char[] load() throws IOException{
        Dictionary dictionary = new Dictionary();
        long zeit = System.currentTimeMillis();
        dictionary.load( "book_de.dict" );
        System.out.println("L:"+(System.currentTimeMillis()-zeit)+"  "+(dictionary.getDataSize()*2)); 
        return dictionary.toArray();
    }

    public static void main2(String[] args) throws Exception {
        Dictionary dictonary = new Dictionary();
        long zeit = System.currentTimeMillis();
        dictonary.load("book_de.bin");
        System.out.println(System.currentTimeMillis()-zeit);
        
        System.out.println(dictonary.exist("gjqqjqText"));
        List list = dictonary.searchSuggestions("gjqqjqText");
        System.out.println(list);
        for(int i=0; i<list.size(); i++){
            Suggestion sugg = (Suggestion)list.get( i );
            System.out.println(sugg+" "+sugg.getDissimilarity());
        }
        
        System.out.println("Ok");
    }

    public static void main(String[] args) throws Exception {
        DictionaryFactory factory = new DictionaryFactory();
        long zeit = System.currentTimeMillis();
        System.out.println(System.currentTimeMillis()-zeit);

        //factory.add( "lieben" );
        factory.add( "doppelten" );
        Dictionary dictionary = factory.create();
        
        Dictionary dic2 = new Dictionary();        
        //dic2.add( "lieben" );
        dic2.add( "doppelten" );
        
        factory = new DictionaryFactory();
        factory.loadWordList( new URL("file",null,"dictionary_de.ortho" ) );
        Dictionary dic3 = factory.create();
        System.out.println(dic3.exist("dopt"));
        System.out.println(dic3.exist("dopenlte"));
        
        List<Suggestion> sugg = dic3.searchSuggestions( "doppelten" );
        System.out.println(sugg.size()+":"+sugg);
        for(Suggestion s : sugg){
            System.out.println(s+", "+s.getDissimilarity());
        }
        
        char[] a1 = dictionary.toArray();
        char[] a2 = dic2.toArray();
        System.out.println(dictionary.exist("lieben"));
        System.out.println(dictionary.exist("doppelten"));
        System.out.println(dic2.exist("lieben"));
        System.out.println(dic2.exist("doppelten"));
        for(int i=0; i<a1.length; i++){
            if(a1[i] != a2[i]){
                System.err.println(i+": "+(int)a1[i] + " " + (int)a2[i]);
            }
        }
        
        System.out.println(dictionary.exist("leiben"));
        List list = dictionary.searchSuggestions("zlieben");
        System.out.println(list);
        
        System.out.println("Ok");
    }
}
