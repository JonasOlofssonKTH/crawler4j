package edu.uci.ics.crawler4j.url;

import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


public class UrlResolverTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenBaseUrlNull() {
        //Given
        String baseUrlNull = null;
        String relativeURl = "aRandomRelativeUrl";
        //When
        UrlResolver.resolveUrl(baseUrlNull, relativeURl);
    }

    @Test()
    public void shouldThrowIllegalArgumentExceptionWhenRelativeUrlNull() {
        //Given
        String baseUrl = "ABaseUrl";
        String relativeNull = null;
        //When
        try {
            UrlResolver.resolveUrl(baseUrl, relativeNull);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("Relative URL must not be null", expected.getMessage());
        }
    }

    @Test
    public void shouldReturnParseURLWhenBaseUrlNullWhenResolveUrl() {
        //Given
        String relativeUrl = "http://WebReference.com/foo/bar.html?baz";
        //When
        UrlResolver.Url url = UrlResolver.resolveUrlObject(null, relativeUrl);
        //Then
        assertEquals(url.toString(), "http://WebReference.com/foo/bar.html?baz");
    }

    @Test
    public void shouldReturnSameURLWhenRelativeUrlEmptyWhenResolveUrl() {
        //Given
        //When
        UrlResolver.Url url = UrlResolver.resolveUrlObject(UrlResolver.parseUrl("http://WebReference.com/foo/bar.html?baz"),
                "");
        //Then
        assertEquals(url.toString(), "http://WebReference.com/foo/bar.html?baz");
    }
    
    @Test
    public void shouldReturnSameURLWhenRelativeURLContainsNetworkLocation() {
        //Given
        String relativeUrl = "//WebReference.com/foo/bar.html?baz";
        String baseUrl = "http://WebReference.com/";
        //When
        String url = UrlResolver.resolveUrl(baseUrl, relativeUrl);
        //Then
        assertEquals("http://WebReference.com/foo/bar.html?baz", url);
    }
    
    
    @Test
    public void shouldInheritBaseUrlPathWhenRelativeUrlPathIsNullAndParametersAreNotNull() {
        //Given
        String relativeUrl = ";type=a?baz";
        String baseUrl = "http://WebReference.com/foo/bar.html";
        //When
        String url = UrlResolver.resolveUrl(baseUrl, relativeUrl);
        //Then
        assertEquals("http://WebReference.com/foo/bar.html;type=a?baz", url);
    }
    
    @Test
    public void shouldInheritBaseUrlPathAndQueryWhenRelativePathIsFragment() {
        //Given
        String relativeUrl = "#fragment";
        String baseUrl = "http://WebReference.com/foo/bar.html?baz";
        //When
        String url = UrlResolver.resolveUrl(baseUrl, relativeUrl);
        //Then
        assertEquals("http://WebReference.com/foo/bar.html?baz#fragment", url);
    }
    
    @Test
    public void shouldReturnRelativeUrlAppendedToBaseUrlWhenBaseUrlEndsInSlash() {
        //Given
        String relativeUrl = "foo/bar.html?baz";
        String baseUrl = "http://WebReference.com/";
        //When
        String url = UrlResolver.resolveUrl(baseUrl, relativeUrl);
        //Then
        assertEquals("http://WebReference.com/foo/bar.html?baz", url);
    }
}