/* 
Copyright Paul James Mutton, 2001-2004, http://www.jibble.org/

This file is part of JMegaHal.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

$Author: pjm2 $
$Id: JMegaHal.java,v 1.4 2004/02/01 13:24:06 pjm2 Exp $

*/

package com.demigodsrpg.chitchatbot.ai;

import java.util.*;
import java.net.*;
import java.io.*;

public class Brain implements Serializable {
    public static final int LIMIT = 10000; // Hard limit
    
    // These are valid chars for words. Anything else is treated as punctuation.
    public static final String WORD_CHARS = "abcdefghijklmnopqrstuvwxyz" +
                                            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                            "0123456789";
    public static final String END_CHARS = ".!?";
    public static final List<String> COMMON_WORDS = Arrays.asList(
            "the", "of", "to", "and", "a", "in", "is", "it", "you", "that", "he", "was", "for", "on", "are", "with",
            "as", "I", "his", "they", "be", "at", "one", "have", "this", "from", "or", "had", "by", "hot", "but",
            "some", "what", "there", "we", "can", "out", "other", "were", "all", "your", "when", "up", "use", "word"
    );

    // This maps a single word to a HashSet of all the Quads it is in.
    private final Map<String, Set<Quad>> WORDS = new HashMap<>();

    // A self-referential HashMap of Quads.
    private final Map<Quad, Quad> QUADS = new HashMap<>();

    // This maps a Quad onto a Set of Strings that may come next.
    private final Map<Quad, Set<String>> NEXT = new HashMap<>();

    // This maps a Quad onto a Set of Strings that may come before it.
    private final Map<Quad, Set<String>> PREVIOUS = new HashMap<>();

    // Random
    private final Random RANDOM = new Random();
    
    /**
     * Construct an empty brain.
     */
    public Brain() {
    }
    
    /**
     * Adds an entire documents to the 'brain'.  Useful for feeding in
     * stray theses, but be careful not to put too much in, or you may
     * run out of memory!
     */
    public void addDocument(String uri) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(uri).openStream()));
        String str = "";
        int ch;
        while ((ch = reader.read()) != -1) {
            str += ch;
            if (END_CHARS.indexOf((char) ch) >= 0) {
                String sentence = str;
                sentence = sentence.replace('\r', ' ');
                sentence = sentence.replace('\n', ' ');
                add(sentence);
                str = "";
            }
        }
        add(str);
        reader.close();
    }
    
    /**
     * Adds a new sentence to the 'brain'
     */
    public void add(String sentence) {
        if(WORDS.size() >= LIMIT) {
            return;
        }

        List<String> parts = new ArrayList<>();
        sentence = sentence.trim();
        char[] chars = sentence.toCharArray();

        boolean punctuation = false;
        String str = "";

        for (char ch : chars) {
            if ((WORD_CHARS.indexOf(ch) >= 0) == punctuation) {
                punctuation = !punctuation;
                if (str.length() > 0) {
                    parts.add(str);
                }
                str = "";
            } else {
                str += ch;
            }
        }
        if (str.length() > 0) {
            parts.add(str);
        }
        
        if (parts.size() >= 4) {
            for (int i = 0; i < parts.size() - 3; i++) {
                Quad quad = new Quad(parts.get(i), parts.get(i + 1), parts.get(i + 2), parts.get(i + 3));
                if (QUADS.containsKey(quad)) {
                    quad = QUADS.get(quad);
                }
                else {
                    QUADS.put(quad, quad);
                }

                if (i == 0) {
                    quad.setCanStart(true);
                }
                //else if (i == parts.size() - 4) {
                if (i == parts.size() - 4) {
                    quad.setCanEnd(true);
                }
                
                for (int n = 0; n < 4; n++) {
                    String token = parts.get(i + n);
                    if (!WORDS.containsKey(token)) {
                        WORDS.put(token, new HashSet<>(1));
                    }
                    Set<Quad> set = WORDS.get(token);
                    set.add(quad);
                }
                
                if (i > 0) {
                    String previousToken = parts.get(i - 1);
                    if (!PREVIOUS.containsKey(quad)) {
                        PREVIOUS.put(quad, new HashSet<>(1));
                    }
                    Set<String> set = PREVIOUS.get(quad);
                    set.add(previousToken);
                }
                
                if (i < parts.size() - 4) {
                    String nextToken = parts.get(i + 4);
                    if (!NEXT.containsKey(quad)) {
                        NEXT.put(quad, new HashSet<>(1));
                    }
                    Set<String> set = NEXT.get(quad);
                    set.add(nextToken);
                }
                
            }
        }
    }
    
    /**
     * Generate a random sentence from the brain.
     */
    public String getSentence() {
        return getSentence(null);
    }
    
    /**
     * Generate a sentence that includes (if possible) the specified word.
     */
    public String getSentence(String word) {
        if(COMMON_WORDS.contains(word.toLowerCase())) {
            return getSentence();
        }

        LinkedList<String> parts = new LinkedList<>();
        
        Quad[] quads;
        if (WORDS.containsKey(word)) {
            quads = WORDS.get(word).toArray(new Quad[WORDS.size()]);
        }
        else {
            quads = QUADS.keySet().toArray(new Quad[QUADS.size()]);
        }
        
        if (quads.length == 0) {
            return "";
        }
        
        Quad middleQuad = quads[RANDOM.nextInt(quads.length)];
        Quad quad = middleQuad;
        
        for (int i = 0; i < 4; i++) {
            parts.add(quad.getToken(i));
        }
        
        while (!quad.canEnd()) {
            String[] nextTokens = NEXT.get(quad).toArray(new String[NEXT.size()]);
            String nextToken = nextTokens[RANDOM.nextInt(nextTokens.length)];
            quad = QUADS.get(new Quad(quad.getToken(1), quad.getToken(2), quad.getToken(3), nextToken));
            parts.add(nextToken);
        }
        
        quad = middleQuad;
        while (!quad.canStart()) {
            String[] previousTokens = PREVIOUS.get(quad).toArray(new String[PREVIOUS.size()]);
            String previousToken = previousTokens[RANDOM.nextInt(previousTokens.length)];
            quad = QUADS.get(new Quad(previousToken, quad.getToken(0), quad.getToken(1), quad.getToken(2)));
            parts.addFirst(previousToken);
        }
        
        String sentence = "";
        for (Object token : parts) {
            sentence += token;
        }
        
        return sentence;
    }

    /**
     * Purge the brain of all data.
     */
    public void purge() {
        WORDS.clear();
        QUADS.clear();
        NEXT.clear();
        PREVIOUS.clear();
    }
}