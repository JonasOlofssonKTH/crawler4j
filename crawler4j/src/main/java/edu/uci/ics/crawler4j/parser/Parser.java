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

package edu.uci.ics.crawler4j.parser;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.parser.html.HtmlParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.util.Net;
import edu.uci.ics.crawler4j.util.Util;

/**
 * @author Yasser Ganjisaffar
 */
public class Parser extends Configurable {

    protected static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private final HtmlParser htmlParser;
    private final ParseContext parseContext;

    public Parser(CrawlConfig config) throws InstantiationException, IllegalAccessException {
        super(config);
        htmlParser = new HtmlParser();
        parseContext = new ParseContext();
        parseContext.set(HtmlMapper.class, AllTagMapper.class.newInstance());
    }
    
	static boolean[] coverage = new boolean[32];

    /**
     * Parses the content of the page given as argument The parsed data is saved in the 
     * in the parseData property of the page given as argument. If the content of the page
     * contains url, these are saved in the outgoingUrls propety of the page. 
     * 
     * @param page the page to be parsed
     * @param contextURL ?
     * @throws NotAllowedContentException if the page contains binary data when this kind of pages are prohibited in the crawl config.
     * @throws ParseException if the parsing of the page content fails
     */ 

    public void parse(Page page, String contextURL)
        throws NotAllowedContentException, ParseException {
    		coverage[0] = true;
        if (Util.hasBinaryContent(page.getContentType())) { // BINARY
    			coverage[1] = true;
            BinaryParseData parseData = new BinaryParseData();
            if (config.isIncludeBinaryContentInCrawling()) {
        			coverage[2] = true;
                if (config.isProcessBinaryContentInCrawling()) {
            			coverage[3] = true;
                    parseData.setBinaryContent(page.getContentData());
                } else {
            			coverage[4] = true;
                    parseData.setHtml("<html></html>");
                }
                page.setParseData(parseData);
                if (parseData.getHtml() == null) {
            			coverage[5] = true;
            			Parser.createFile("parse_coverage", coverage);
                    throw new ParseException();
                } else {
            			coverage[6] = true;
                }
                parseData.setOutgoingUrls(Net.extractUrls(parseData.getHtml()));
            } else {
        			coverage[7] = true;
                throw new NotAllowedContentException();
            }
        } else if (Util.hasPlainTextContent(page.getContentType())) { // plain Text
    			coverage[8] = true;
            try {
                TextParseData parseData = new TextParseData();
                if (page.getContentCharset() == null) {
            			coverage[9] = true;
                    parseData.setTextContent(new String(page.getContentData()));
                } else {
            			coverage[10] = true;
                    parseData.setTextContent(
                        new String(page.getContentData(), page.getContentCharset()));
                }
                parseData.setOutgoingUrls(Net.extractUrls(parseData.getTextContent()));
                page.setParseData(parseData);
            } catch (Exception e) {
        			coverage[11] = true;
                logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
    				Parser.createFile("parse_coverage", coverage);
                throw new ParseException();
            }
        } else { // isHTML
    			coverage[12] = true;
            Metadata metadata = new Metadata();
            HtmlContentHandler contentHandler = new HtmlContentHandler();
            try (InputStream inputStream = new ByteArrayInputStream(page.getContentData())) {
                htmlParser.parse(inputStream, contentHandler, metadata, parseContext);
            } catch (Exception e) {
        			coverage[13] = true;
                logger.error("{}, while parsing: {}", e.getMessage(), page.getWebURL().getURL());
    				Parser.createFile("parse_coverage", coverage);
                throw new ParseException();
            }

            if (page.getContentCharset() == null) {
        			coverage[14] = true;
                page.setContentCharset(metadata.get("Content-Encoding"));
            } else {
        			coverage[15] = true;
            }

            HtmlParseData parseData = new HtmlParseData();
            parseData.setText(contentHandler.getBodyText().trim());
            parseData.setTitle(metadata.get(DublinCore.TITLE));
            parseData.setMetaTags(contentHandler.getMetaTags());
            // Please note that identifying language takes less than 10 milliseconds
            LanguageIdentifier languageIdentifier = new LanguageIdentifier(parseData.getText());
            page.setLanguage(languageIdentifier.getLanguage());

            Set<WebURL> outgoingUrls = new HashSet<>();

            String baseURL = contentHandler.getBaseUrl();
            if (baseURL != null) {
        			coverage[16] = true;
                contextURL = baseURL;
            } else {
        			coverage[17] = true;
            }

            int urlCount = 0;
            for (ExtractedUrlAnchorPair urlAnchorPair : contentHandler.getOutgoingUrls()) {
        			coverage[18] = true;
                String href = urlAnchorPair.getHref();
                if ((href == null) || href.trim().isEmpty()) {
            			coverage[21] = true;
                    continue;
                } else {
            			coverage[22] = true;
                }

                String hrefLoweredCase = href.trim().toLowerCase();
                if (!hrefLoweredCase.contains("javascript:") &&
                    !hrefLoweredCase.contains("mailto:") && !hrefLoweredCase.contains("@")) {
            			coverage[23] = true;
                    // Prefer page's content charset to encode href url
                    Charset hrefCharset = ((page.getContentCharset() == null) || page.getContentCharset().isEmpty()) ?
                                          StandardCharsets.UTF_8 : Charset.forName(page.getContentCharset());
                    String url = URLCanonicalizer.getCanonicalURL(href, contextURL, hrefCharset);
                    if (url != null) {
                			coverage[24] = true;
                        WebURL webURL = new WebURL();
                        webURL.setURL(url);
                        webURL.setTag(urlAnchorPair.getTag());
                        webURL.setAnchor(urlAnchorPair.getAnchor());
                        webURL.setAttributes(urlAnchorPair.getAttributes());
                        outgoingUrls.add(webURL);
                        urlCount++;
                        if (urlCount > config.getMaxOutgoingLinksToFollow()) {
                    			coverage[25] = true;
                            break;
                        } else {
                    			coverage[26] = true;
                        }
                    } else {
                			coverage[27] = true;
                    }
                } else {
            			coverage[28] = true;
                }
            }
            parseData.setOutgoingUrls(outgoingUrls);

            try {
                if (page.getContentCharset() == null) {
            			coverage[29] = true;
                    parseData.setHtml(new String(page.getContentData()));
                } else {
            			coverage[30] = true;
                    parseData.setHtml(new String(page.getContentData(), page.getContentCharset()));
                }

                page.setParseData(parseData);
            } catch (UnsupportedEncodingException e) {
        			coverage[31] = true;
        			Parser.createFile("parse_coverage", coverage);
                logger.error("error parsing the html: " + page.getWebURL().getURL(), e);
                throw new ParseException();
            }
        }
		Parser.createFile("parse_coverage", coverage);
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
