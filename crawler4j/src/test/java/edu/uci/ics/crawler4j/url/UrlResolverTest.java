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
}