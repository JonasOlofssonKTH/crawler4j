package edu.uci.ics.crawler4j.tests;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.uci.ics.crawler4j.robotstxt.PathRule;

public class PathRuleTest {
	
	@Test
	public void PathRuleTest1() {
		// Contract: A String pattern should be turn into String regex 

		int DISALLOWED = 2;
		String actuall = new PathRule(DISALLOWED, "/*.gif$").pattern.toString();
		String expected = "^\\Q/\\E.+\\Q.gif\\E$";
		assertEquals(expected, actuall);
	}
	
	@Test
	public void PathRuleTest2() {
		// Contract: A String pattern should be turn into String regex 
		
		int ALLOWED = 1;
		String actuall = new PathRule(ALLOWED, "").pattern.toString();
		String expected = "^$";
		assertEquals(expected, actuall);
	}
	
	@Test
	public void PathRuleTest3() {
		// Contract: A String pattern should be turn into String regex 

		int UNDEFINED = 3;
		String actual = new PathRule(UNDEFINED, "\\*.htm").pattern.toString();
		String expected = "^\\Q*.htm\\E.*";
		assertEquals(expected, actual);
	}
	
	@Test
	public void PathRuleTest4() {
		// Contract: A String pattern should be turn into String regex 

		int ALLOWS = 1;
		String actual = new PathRule(ALLOWS, "/fish*").pattern.toString();
		String expected = "^\\Q/fish\\E.*";
		assertEquals(expected, actual);
	}
	
	@Test
	public void PathRuleTest5() {
		// Contract: A String pattern should be turn into String regex 

		int DISALLOWED = 2;
		String actual = new PathRule(DISALLOWED, "\\").pattern.toString();
		String expected = "^\\Q\\\\E.*";
		assertEquals(expected, actual);
	}
}
