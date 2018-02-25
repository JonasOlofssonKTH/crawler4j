/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.url;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * See http://en.wikipedia.org/wiki/URL_normalization for a reference Note: some
 * parts of the code are adapted from: http://stackoverflow.com/a/4057470/405418
 *
 * @author Yasser Ganjisaffar
 */
public class URLCanonicalizer {
	
	static boolean[] coverage = new boolean[12];
	
    public static String getCanonicalURL(String url) {
        return getCanonicalURL(url, null); 
    }

    public static String getCanonicalURL(String href, String context) {
        return getCanonicalURL(href, context, StandardCharsets.UTF_8);
    }

    public static String getCanonicalURL(String href, String context, Charset charset) {
    		coverage[0]=true;
    	
        try {
        	URL canonicalURL =
                new URL(UrlResolver.resolveUrl((context == null) ? "" : context, href));

            String host = canonicalURL.getHost().toLowerCase();
            if (Objects.equals(host, "")) {
            		coverage[1]=true;
            		createFile("getCanonicalURL_coverage", coverage);
                // This is an invalid Url.
                return null;
            } else {
                coverage[2]=true;
            }
            String path = canonicalURL.getPath();

      /*
       * Normalize: no empty segments (i.e., "//"), no segments equal to
       * ".", and no segments equal to ".." that are preceded by a segment
       * not equal to "..".
       */
            path = new URI(path.replace("\\", "/")).normalize().toString();

            int idx = path.indexOf("//");
            while (idx >= 0) {
            		coverage[3]=true;
                path = path.replace("//", "/");
                idx = path.indexOf("//");
            }

            while (path.startsWith("/../")) {
            		coverage[4]=true;
                path = path.substring(3);
            }

            path = path.trim();

            Map<String, String> params = createParameterMap(canonicalURL.getQuery());
            
            final String queryString;
            if ((params != null) && !params.isEmpty()) {
            		coverage[5]=true;
                String canonicalParams = canonicalize(params, charset);
                queryString = (canonicalParams.isEmpty() ? "" : ("?" + canonicalParams));
            } else {
            		coverage[6]=true;
                queryString = "";
            }

            if (path.isEmpty()) {
            		coverage[7]=true;
                path = "/";
            } else {
        			coverage[8]=true;
            }

            //Drop default port: example.com:80 -> example.com
            int port = canonicalURL.getPort();
            if (port == canonicalURL.getDefaultPort()) {
            		coverage[9]=true;
                port = -1;
            } else {
        			coverage[10]=true;
            }

            String protocol = canonicalURL.getProtocol().toLowerCase();
            String pathAndQueryString = normalizePath(path) + queryString;

            URL result = new URL(protocol, host, port, pathAndQueryString);
    			createFile("getCanonicalURL_coverage", coverage);
            return result.toExternalForm();

        } catch (MalformedURLException | URISyntaxException ex) {
        		coverage[11]=true;
        		createFile("getCanonicalURL_coverage", coverage);
            return null;
        }
    }
    

    /**
     * Takes a query string, separates the constituent name-value pairs, and
     * stores them in a LinkedHashMap ordered by their original order.
     *
     * @return Null if there is no query string.
     */
    static boolean[] mapCoverage = new boolean[10];
    private static Map<String, String> createParameterMap(String queryString) {
    		mapCoverage[0] = true;
        if ((queryString == null) || queryString.isEmpty()) {
    			mapCoverage[1] = true;
            return null;
        } else{
    			mapCoverage[2] = true;
        }
        final String[] pairs = queryString.split("&");
        final Map<String, String> params = new LinkedHashMap<>(pairs.length);

        for (final String pair : pairs) {
			mapCoverage[3] = true;
            if (pair.isEmpty()) {
    				mapCoverage[4] = true;
                continue;
            } else {
            		mapCoverage[5] = true;
            }

            String[] tokens = pair.split("=", 2);
            switch (tokens.length) {
                case 1:
        				mapCoverage[6] = true;
                    if (pair.charAt(0) == '=') {
            				mapCoverage[7] = true;
                        params.put("", tokens[0]);
                    } else {
            				mapCoverage[8] = true;
                        params.put(tokens[0], "");
                    }
                    break;
                case 2:
        				mapCoverage[9] = true;
                    params.put(tokens[0], tokens[1]);
                    break;
            }
        }
        createFile("map_coverage", mapCoverage);
        return new LinkedHashMap<>(params);
    }

    /**
     * Canonicalize the query string.
     *
     * @param paramsMap
     *            Parameter map whose name-value pairs are in order of insertion.
     * @param charset
     *            Charset of html page
     * @return Canonical form of query string.
     */
    static boolean[] converageCanonicalize = new boolean[10];
    private static String canonicalize(Map<String, String> paramsMap, Charset charset) {
    		converageCanonicalize[0] = true;
        if ((paramsMap == null) || paramsMap.isEmpty()) {
    			converageCanonicalize[1] = true;
    			URLCanonicalizer.createFile("canonicalize_converage", converageCanonicalize);
            return "";
        } else {
        		converageCanonicalize[2] = true;
        }
        final StringBuilder sb = new StringBuilder(100);
        for (Map.Entry<String, String> pair : paramsMap.entrySet()) {
    			converageCanonicalize[3] = true;
            final String key = pair.getKey().toLowerCase();
            if ("jsessionid".equals(key) || "phpsessid".equals(key) || "aspsessionid".equals(key)) {
        			converageCanonicalize[4] = true;
                continue;
            } else {
    				converageCanonicalize[5] = true;
            }
            if (sb.length() > 0) {
        			converageCanonicalize[6] = true;
                sb.append('&');
            } else {
    				converageCanonicalize[7] = true;
            }
            sb.append(percentEncodeRfc3986(pair.getKey(), charset));
            if (!pair.getValue().isEmpty()) {
        			converageCanonicalize[8] = true;
                sb.append('=');
                sb.append(percentEncodeRfc3986(pair.getValue(), charset));
            } else {
    				converageCanonicalize[9] = true;
            }
        }
		URLCanonicalizer.createFile("canonicalize_converage", converageCanonicalize);
        return sb.toString();
    }

    private static String normalizePath(final String path) {
        return path.replace("%7E", "~").replace(" ", "%20");
    }

    private static String percentEncodeRfc3986(String string, Charset charset) {
        try {
            string = string.replace("+", "%2B");
            string = URLDecoder.decode(string, "UTF-8");
            string = URLEncoder.encode(string, charset.name());
            return string.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (Exception e) {
            return string;
        }
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