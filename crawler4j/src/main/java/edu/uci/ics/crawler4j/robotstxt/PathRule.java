package edu.uci.ics.crawler4j.robotstxt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathRule {
    protected static final Logger logger = LoggerFactory.getLogger(PathRule.class);

    public int type;
    public Pattern pattern;

    /**
     * Match a pattern defined in a robots.txt file to a path
     * Following the pattern definition as stated on:
     * https://support.google.com/webmasters/answer/6062596?hl=en&ref_topic=6061961
     *
     * This page defines the following items:
     *    * matches any sequence of characters, including /
     *    $ matches the end of the line
     *
     * @param pattern The pattern to convert
     * @return The compiled regexp pattern created from the robots.txt pattern
     */
    
	static boolean[] coverage = new boolean[20];

    public static Pattern robotsPatternToRegexp(String pattern) {
    		coverage[0] = true;
        StringBuilder regexp = new StringBuilder();
        regexp.append('^');
        StringBuilder quoteBuf = new StringBuilder();
        boolean terminated = false;

        // If the pattern is empty, match only completely empty entries, e.g., none as
        // there will always be a leading forward slash.
        if (pattern.isEmpty()) {
    			coverage[1] = true;
    			PathRule.createFile("robotsPattern_coverage", coverage);
            return Pattern.compile("^$");
        } else {
    			coverage[2] = true;
        }

        // Iterate over the characters
        for (int pos = 0; pos < pattern.length(); ++pos) {
    			coverage[3] = true;
            char ch = pattern.charAt(pos);

            if (ch == '\\') {
        			coverage[4] = true;
        	
                // Handle escaped * and $ characters
                char nch = pos < pattern.length() - 1 ? pattern.charAt(pos + 1) : 0;
                if (nch == '*' || ch == '$') {
            			coverage[5] = true;
                    quoteBuf.append(nch);
                    ++pos; // We need to skip one character
                } else {
            			coverage[6] = true;
                    quoteBuf.append(ch);
                }
            } else if (ch == '*') {
        			coverage[7] = true;
                // * indicates any sequence of one or more characters
                if (quoteBuf.length() > 0) {
            			coverage[8] = true;
                    // The quoted character buffer is not empty, so add them before adding
                    // the wildcard matcher
                    regexp.append("\\Q").append(quoteBuf).append("\\E");
                    quoteBuf = new StringBuilder();
                } else {
            			coverage[9] = true;
                }
                if (pos == pattern.length() - 1) {
            			coverage[10] = true;
                    terminated = true;
                    // A terminating * may match 0 or more characters
                    regexp.append(".*");
                } else {
            			coverage[11] = true;
                    // A non-terminating * may match 1 or more characters
                    regexp.append(".+");
                }
            } else if (ch == '$' && pos == pattern.length() - 1) {
        			coverage[12] = true;
                // A $ at the end of the pattern indicates that the path should end here in order
              // to match
                // This explicitly disallows prefix matching
                if (quoteBuf.length() > 0) {
            			coverage[13] = true;
                    // The quoted character buffer is not empty, so add them before adding
                    // the end-of-line matcher
                    regexp.append("\\Q").append(quoteBuf).append("\\E");
                    quoteBuf = new StringBuilder();
                } else {
            			coverage[14] = true;
                }
                regexp.append(ch);
                terminated = true;
            } else {
        			coverage[15] = true;
                // Add the character as-is to the buffer for quoted characters
                quoteBuf.append(ch);
            }
        }

        // Add quoted string buffer: enclosed between \Q and \E
        if (quoteBuf.length() > 0) {
    			coverage[16] = true;
            regexp.append("\\Q").append(quoteBuf).append("\\E");
        } else {
    			coverage[17] = true;
        }

        // Add a wildcard pattern after the path to allow matches where this
        // pattern matches a prefix of the path.
        if (!terminated) {
    			coverage[18] = true;
            regexp.append(".*");
        } else {
    			coverage[19] = true;
        }

        // Return the compiled pattern
		PathRule.createFile("robotsPattern_coverage", coverage);
        return Pattern.compile(regexp.toString());
    }

    /**
     * Check if the specified path matches a robots.txt pattern
     *
     * @param pattern The pattern to match
     * @param path The path to match with the pattern
     * @return True when the pattern matches, false if it does not
     */
    public static boolean matchesRobotsPattern(String pattern, String path) {
        return robotsPatternToRegexp(pattern).matcher(path).matches();
    }

    /**
     * Create a new path rule, based on the specified pattern
     *
     * @param type Either HostDirectives.ALLOWS or HostDirectives.DISALLOWS
     * @param pattern The pattern for this rule
     */
    public PathRule(int type, String pattern) {
        this.type = type;
        this.pattern = robotsPatternToRegexp(pattern);
    }

    /**
     * Check if the specified path matches this rule
     *
     * @param path The path to match with this pattern
     * @return True when the path matches, false when it does not
     */
    public boolean matches(String path) {
        return this.pattern.matcher(path).matches();
    }
    
    public static void createFile(String fileName, boolean[] coverage) {
	    	StringBuilder contents = new StringBuilder().append(Arrays.toString(coverage));
	    	int count = 0;
	    	for (int i = 0 ; i < coverage.length ; i++) {
	    		if(coverage[i]) {count++;}
	    	}
	    	float percentCovered = (float) count / coverage.length;
	    	contents.append("\n" + percentCovered * 100 + "%");
	    	try {
	    		Writer output = new BufferedWriter(new FileWriter(new File("test_coverage/" + fileName)));
	    	    output.write(contents.toString());
	    		output.close();
	    	} catch(Exception e) { e.printStackTrace(); }	
	}
}
