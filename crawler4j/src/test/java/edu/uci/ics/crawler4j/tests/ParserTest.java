package edu.uci.ics.crawler4j.tests;

import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.parser.TextParseData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException; 

public class ParserTest {
	
	@Test(expected = NotAllowedContentException.class)
	public void parseTest1()  throws InstantiationException, IllegalAccessException, NotAllowedContentException, ParseException {
		// Contract: parse should throw NotAllowedContentException when feed a binary file and it's specified in CrawlConfig 
		// that binary data should not be included in the crawl
		CrawlConfig config = new CrawlConfig();
		config.setIncludeBinaryContentInCrawling(false);
		Page page = new Page(null);
		page.setContentType("video");
		Parser parser = new Parser(config);
		
		parser.parse(page, "url");
	}
	
	@Test
	public void parseTest2()  throws InstantiationException, IllegalAccessException, NotAllowedContentException, ParseException {
		// Contract: parse should parse page as text if content-type contains the word text and the amount of urls should be equal to the
		// number of urls in the text which in this case is zero.
		CrawlConfig config = new CrawlConfig();
		Page page = new Page(null);
		page.setContentType("text");
		page.setContentCharset("UTF-8");
		byte[] data = { 'A', 'B', 'C' };
		page.setContentData(data);
		Parser parser = new Parser(config);
		parser.parse(page, "url");
		
		assertTrue(page.getParseData().getOutgoingUrls().isEmpty());
		assertEquals(TextParseData.class, page.getParseData().getClass());
	}
	
	@Test
	public void parseTest3()  throws InstantiationException, IllegalAccessException, NotAllowedContentException, ParseException {
		// Contract: parse should parse page as html if content-type contains the word html and the amount of urls should be equal to the
		// number of urls in the text which in this case is one.
		CrawlConfig config = new CrawlConfig();
		Page page = new Page(null);
		page.setContentType("html");
		String data = "<html><body></body></html>";
		page.setContentData(data.getBytes());
		Parser parser = new Parser(config);
		parser.parse(page, "url");
		assertEquals(HtmlParseData.class, page.getParseData().getClass());
		assertEquals(0, page.getParseData().getOutgoingUrls().size());
	}
	
	@Test
	public void parseTest4()  throws InstantiationException, IllegalAccessException, NotAllowedContentException, ParseException {
		// Contract: parse should parse page as binary if content-type contains the word video that binary data parsing is set to true in
		// crawler config.
		CrawlConfig config = new CrawlConfig();
		config.setIncludeBinaryContentInCrawling(true);
		Page page = new Page(null);
		page.setContentType("video");
		String data = "12345";
		page.setContentData(data.getBytes());
		Parser parser = new Parser(config);
		parser.parse(page, "url");
		assertEquals(BinaryParseData.class, page.getParseData().getClass());
		assertEquals(0, page.getParseData().getOutgoingUrls().size());
	}
}
