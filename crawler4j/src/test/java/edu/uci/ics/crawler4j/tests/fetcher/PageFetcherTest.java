package edu.uci.ics.crawler4j.tests.fetcher;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.NtAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import org.junit.Test;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PageFetcherTest {

    @Test
    public void PageFetcherTest1(){
        // Contract: Should successfully create a Map with the AuthScope and the Credentials
        // with the config provided and create a HttpHost proxy
        CrawlConfig config = new CrawlConfig();
        config.setProxyHost("1");
        config.setProxyPort(8010);
        config.setProxyUsername("user");
        config.setProxyPassword("pwd");
        PageFetcher pf = new PageFetcher(config);
        AuthScope as = new AuthScope("1", 8010);
        Credentials cred = new UsernamePasswordCredentials("user", "pwd");
        assertEquals(pf.credentialsMap.get(as), cred);
        assertEquals(pf.proxy.getHostName(), config.getProxyHost());
    }

    @Test
    public void PageFetcherTest2(){
        // Contract: Should not attempt to create credentials but successfully create a HttpHost proxy
        CrawlConfig config = new CrawlConfig();
        config.setProxyHost("1");
        PageFetcher pf = new PageFetcher(config);
        Map<AuthScope, Credentials> emptyMap = new HashMap<>();
        assertEquals(pf.credentialsMap, emptyMap);
        assertEquals(pf.proxy.getHostName(), config.getProxyHost());
    }

    @Test
    public void PageFetcherTest3() throws MalformedURLException {
        // Contract: Creates credentials successfully based on BasicAuthInfo
        CrawlConfig config = new CrawlConfig();
        List<AuthInfo> authInfos = new ArrayList<>();
        AuthInfo authInfo = new BasicAuthInfo("user", "pwd", "http://localhost:8080/");
        authInfo.setAuthenticationType(AuthInfo.AuthenticationType.BASIC_AUTHENTICATION);
        authInfo.setHost("host");
        authInfo.setPort(8080);
        authInfos.add(authInfo);
        config.setAuthInfos(authInfos);
        PageFetcher pf = new PageFetcher(config);
        Credentials cred = new UsernamePasswordCredentials("user", "pwd");
        assertEquals(pf.credentialsMap.get(new AuthScope("host", 8080)), cred);
    }

    @Test
    public void PageFetcherTest4() throws UnknownHostException, MalformedURLException {
        // Contract: Creates credentials successfully based on NTAuthInfo
        CrawlConfig config = new CrawlConfig();
        List<AuthInfo> authInfos = new ArrayList<>();
        AuthInfo authInfo = new NtAuthInfo("user", "pwd", "http://localhost:8080", ".com");
        authInfo.setAuthenticationType(AuthInfo.AuthenticationType.NT_AUTHENTICATION);
        authInfo.setHost("host");
        authInfo.setPort(8080);
        authInfos.add(authInfo);
        config.setAuthInfos(authInfos);
        PageFetcher pf = new PageFetcher(config);
        assertEquals(pf.credentialsMap.get(new AuthScope("host", 8080)).getPassword(), "pwd");
    }
}
