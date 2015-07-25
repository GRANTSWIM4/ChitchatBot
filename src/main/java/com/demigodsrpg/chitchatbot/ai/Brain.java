package com.demigodsrpg.chitchatbot.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Brain implements Serializable {
    public static final int LIMIT = 10000; // Hard limit
    
    // These are valid chars for words. Anything else is treated as punctuation.
    public static final String WORD_CHARS = "abcdefghijklmnopqrstuvwxyz" +
                                            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                            "0123456789";
    public static final String END_CHARS = ".!?";
    public static final List<String> COMMON_WORDS = Arrays.asList(
            "the", "of", "to", "and", "a", "in", "is", "it", "you", "that", "he", "was", "for", "on", "are", "with",
            "as", "I", "his", "they", "be", "at", "one", "have", "this", "from", "or", "had", "by", "no", "but", "some",
            "what", "there", "we", "can", "out", "other", "were", "all", "your", "when", "up", "use", "yes", "hot"
    );

    // This maps an Id to a Quad
    Map<String, Quad<String>> QUADS = new ConcurrentHashMap<>();

    // This maps a single word to a Set of all the Quads it is in.
    Map<String, Set<String>> WORDS = new ConcurrentHashMap<>();

    // This maps a Quad onto a Set of Strings that may come next.
    Map<String, Set<String>> NEXT = new ConcurrentHashMap<>();

    // This maps a Quad onto a Set of Strings that may come before it.
    Map<String, Set<String>> PREVIOUS = new ConcurrentHashMap<>();

    // Random
    private final transient Random RANDOM = new Random();
    
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
            }
            str += ch;
        }
        if (str.length() > 0) {
            parts.add(str);
        }
        
        if (parts.size() >= 4) {
            for (int i = 0; i < parts.size() - 3; i++) {
                Quad<String> quad = new Quad<>(parts.get(i), parts.get(i + 1), parts.get(i + 2), parts.get(i + 3));
                if (QUADS.containsKey(quad.getId())) {
                    quad = QUADS.get(quad.getId());
                } else if (quad.isValid()) {
                    QUADS.put(quad.getId(), quad);
                } else {
                    continue;
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
                    WORDS.get(token).add(quad.getId());
                }
                
                if (i > 0) {
                    String previousToken = parts.get(i - 1);
                    if (!PREVIOUS.containsKey(quad.getId())) {
                        PREVIOUS.put(quad.getId(), new HashSet<>(1));
                    }
                    PREVIOUS.get(quad.getId()).add(previousToken);
                }
                
                if (i < parts.size() - 4) {
                    String nextToken = parts.get(i + 4);
                    if (!NEXT.containsKey(quad.getId())) {
                        NEXT.put(quad.getId(), new HashSet<>(1));
                    }
                    NEXT.get(quad.getId()).add(nextToken);
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
        if (word != null && COMMON_WORDS.contains(word.toLowerCase())) {
            return getSentence();
        }

        LinkedList<String> parts = new LinkedList<>();

        List<Quad<String>> quads;
        if (word != null && WORDS.containsKey(word)) {
            quads = new ArrayList<>(WORDS.get(word).stream().map(QUADS::get).collect(Collectors.toList()));
        }
        else {
            quads = new ArrayList<>(QUADS.values());
        }

        if (quads.size() == 0) {
            return "";
        }

        Quad<String> middleQuad = quads.get(RANDOM.nextInt(quads.size()));
        Quad<String> quad = middleQuad;
        
        for (int i = 0; i < 4; i++) {
            parts.add(quad.getToken(i));
        }
        
        while (!quad.canEnd()) {
            List<String> nextTokens = new ArrayList<>(NEXT.get(quad.getId()));
            String nextToken = nextTokens.get(RANDOM.nextInt(nextTokens.size()));
            quad = QUADS.get(new Quad<>(quad.getToken(1), quad.getToken(2), quad.getToken(3), nextToken).getId());
            parts.add(nextToken);
        }
        
        quad = middleQuad;
        while (!quad.canStart()) {
            List<String> previousTokens = new ArrayList<>(PREVIOUS.get(quad.getId()));
            String previousToken = previousTokens.get(RANDOM.nextInt(previousTokens.size()));
            quad = QUADS.get(new Quad<>(previousToken, quad.getToken(0), quad.getToken(1), quad.getToken(2)).getId());
            parts.addFirst(previousToken);
        }
        
        String sentence = "";
        for (Object token : parts) {
            sentence += token;
        }

        return sentence.trim();
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