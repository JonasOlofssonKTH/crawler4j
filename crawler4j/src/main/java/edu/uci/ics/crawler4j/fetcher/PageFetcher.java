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

package edu.uci.ics.crawler4j.fetcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo.AuthenticationType;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.FormAuthInfo;
import edu.uci.ics.crawler4j.crawler.authentication.NtAuthInfo;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @author Yasser Ganjisaffar
 */
public class PageFetcher extends Configurable {
    protected static final Logger logger = LoggerFactory.getLogger(PageFetcher.class);
    protected final Object mutex = new Object();
    protected PoolingHttpClientConnectionManager connectionManager;
    protected CloseableHttpClient httpClient;
    protected long lastFetchTime = 0;
    protected IdleConnectionMonitorThread connectionMonitorThread = null;
    
	boolean[] coverage = new boolean[21];

    public Map<AuthScope, Credentials> credentialsMap;
    public HttpHost proxy;

    public PageFetcher(CrawlConfig config) {
        super(config);
        
        coverage[0] = true;

        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setExpectContinueEnabled(false)
                                                   .setCookieSpec(config.getCookiePolicy())
                                                   .setRedirectsEnabled(false)
                                                   .setSocketTimeout(config.getSocketTimeout())
                                                   .setConnectTimeout(config.getConnectionTimeout())
                                                   .build();

        RegistryBuilder<ConnectionSocketFactory> connRegistryBuilder = RegistryBuilder.create();
        connRegistryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
        if (config.isIncludeHttpsPages()) {
            coverage[1] = true;
            try { // Fixing: https://code.google.com/p/crawler4j/issues/detail?id=174
                // By always trusting the ssl certificate
                SSLContext sslContext =
                    SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                        @Override
                        public boolean isTrusted(final X509Certificate[] chain, String authType) {
                				converagePatchFetch[2] = true;
                            return true;
                        }
                    }).build();
                SSLConnectionSocketFactory sslsf =
                    new SniSSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
                connRegistryBuilder.register("https", sslsf);
            } catch (Exception e) {
                coverage[3] = true;
                logger.warn("Exception thrown while trying to register https");
                logger.debug("Stacktrace", e);
            }
        } else {
            coverage[4]= true;
        }

        Registry<ConnectionSocketFactory> connRegistry = connRegistryBuilder.build();
        connectionManager =
                new SniPoolingHttpClientConnectionManager(connRegistry, config.getDnsResolver());
        connectionManager.setMaxTotal(config.getMaxTotalConnections());
        connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        if (config.getCookieStore() != null) {
            coverage[5] = true;
            clientBuilder.setDefaultCookieStore(config.getCookieStore());
        } else {
            coverage[6] = true;
        }
        clientBuilder.setDefaultRequestConfig(requestConfig);
        clientBuilder.setConnectionManager(connectionManager);
        clientBuilder.setUserAgent(config.getUserAgentString());
        clientBuilder.setDefaultHeaders(config.getDefaultHeaders());

        credentialsMap = new HashMap<>();
        if (config.getProxyHost() != null) {
            coverage[7] = true;
            if (config.getProxyUsername() != null) {
                coverage[8] = true;
                AuthScope authScope = new AuthScope(config.getProxyHost(), config.getProxyPort());
                Credentials credentials = new UsernamePasswordCredentials(config.getProxyUsername(),
                        config.getProxyPassword());
                credentialsMap.put(authScope, credentials);
            } else{
            		coverage[9] = true;
            }

            proxy = new HttpHost(config.getProxyHost(), config.getProxyPort());
            clientBuilder.setProxy(proxy);
            logger.debug("Working through Proxy: {}", proxy.getHostName());
        } else {
        		coverage[10] = true;
        }

        List<AuthInfo> authInfos = config.getAuthInfos();
        if (authInfos != null) {
            coverage[11] = true;
            for (AuthInfo authInfo : authInfos) {
                coverage[12] = true;
                if (authInfo.getAuthenticationType() == AuthenticationType.BASIC_AUTHENTICATION) {
                    coverage[13] = true;
                    addBasicCredentials((BasicAuthInfo) authInfo, credentialsMap);
                } else if (authInfo.getAuthenticationType() == AuthenticationType
                        .NT_AUTHENTICATION) {
                    coverage[14] = false;
                    addNtCredentials((NtAuthInfo) authInfo, credentialsMap);
                } else{
                		coverage[15] = true;
                }
            }

            if (!credentialsMap.isEmpty()) {
                coverage[16] = true;
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsMap.forEach(credentialsProvider::setCredentials);
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            } else {
                coverage[17] = true;
            }
            httpClient = clientBuilder.build();

            authInfos.stream()
                    .filter(info -> info.getAuthenticationType() == AuthenticationType
                            .FORM_AUTHENTICATION)
                    .map(FormAuthInfo.class::cast)
                    .forEach(this::doFormLogin);
        } else {
            coverage[18] = true;
            httpClient = clientBuilder.build();
        }

        if (connectionMonitorThread == null) {
            coverage[19] = true;
            connectionMonitorThread = new IdleConnectionMonitorThread(connectionManager);
        } else {
            coverage[20] = true;
        }
        connectionMonitorThread.start();
        PageFetcher.createFile("pageFetcher_coverage", coverage);
    }

    /**
     * BASIC authentication<br/>
     * Official Example: https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org
     * /apache/http/examples/client/ClientAuthentication.java
     */
    private void addBasicCredentials(BasicAuthInfo authInfo,
            Map<AuthScope, Credentials> credentialsMap) {
        logger.info("BASIC authentication for: {}", authInfo.getLoginTarget());
        Credentials credentials = new UsernamePasswordCredentials(authInfo.getUsername(),
                authInfo.getPassword());
        credentialsMap.put(new AuthScope(authInfo.getHost(), authInfo.getPort()), credentials);
    }

    /**
     * Do NT auth for Microsoft AD sites.
     */
    private void addNtCredentials(NtAuthInfo authInfo, Map<AuthScope, Credentials> credentialsMap) {
        logger.info("NT authentication for: {}", authInfo.getLoginTarget());
        try {
            Credentials credentials = new NTCredentials(authInfo.getUsername(),
                    authInfo.getPassword(), InetAddress.getLocalHost().getHostName(),
                    authInfo.getDomain());
            credentialsMap.put(new AuthScope(authInfo.getHost(), authInfo.getPort()), credentials);
        } catch (UnknownHostException e) {
            logger.error("Error creating NT credentials", e);
        }
    }

    /**
     * FORM authentication<br/>
     * Official Example: https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org
     * /apache/http/examples/client/ClientFormLogin.java
     */
    private void doFormLogin(FormAuthInfo authInfo) {
        logger.info("FORM authentication for: {}", authInfo.getLoginTarget());
        String fullUri =
            authInfo.getProtocol() + "://" + authInfo.getHost() + ":" + authInfo.getPort() +
            authInfo.getLoginTarget();
        HttpPost httpPost = new HttpPost(fullUri);
        List<NameValuePair> formParams = new ArrayList<>();
        formParams.add(
            new BasicNameValuePair(authInfo.getUsernameFormStr(), authInfo.getUsername()));
        formParams.add(
            new BasicNameValuePair(authInfo.getPasswordFormStr(), authInfo.getPassword()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);

        try {
            httpClient.execute(httpPost);
            logger.debug("Successfully Logged in with user: {} to: {}", authInfo.getUsername(),
                    authInfo.getHost());
        } catch (ClientProtocolException e) {
            logger.error("While trying to login to: {} - Client protocol not supported",
                    authInfo.getHost(), e);
        } catch (IOException e) {
            logger.error("While trying to login to: {} - Error making request", authInfo.getHost(),
                    e);
        }
    }

    static boolean[] converagePatchFetch = new boolean[25];

    public PageFetchResult fetchPage(WebURL webUrl)
        throws InterruptedException, IOException, PageBiggerThanMaxSizeException {
    		converagePatchFetch[0] = true;
        // Getting URL, setting headers & content
        PageFetchResult fetchResult = new PageFetchResult();
        String toFetchURL = webUrl.getURL();
        HttpUriRequest request = null;
        try {
            request = newHttpUriRequest(toFetchURL);
            if (config.getPolitenessDelay() > 0) {
        			converagePatchFetch[1] = true;
                // Applying Politeness delay
                synchronized (mutex) {
                    long now = (new Date()).getTime();
                    if ((now - lastFetchTime) < config.getPolitenessDelay()) {
                			converagePatchFetch[2] = true;
                        Thread.sleep(config.getPolitenessDelay() - (now - lastFetchTime));
                    } else {
            				converagePatchFetch[3] = true;
                    }
                    lastFetchTime = (new Date()).getTime();
                }
            } else {
    				converagePatchFetch[4] = true;
            }

            CloseableHttpResponse response = httpClient.execute(request);
            fetchResult.setEntity(response.getEntity());
            fetchResult.setResponseHeaders(response.getAllHeaders());

            // Setting HttpStatus
            int statusCode = response.getStatusLine().getStatusCode();

            // If Redirect ( 3xx )
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
                statusCode == HttpStatus.SC_MULTIPLE_CHOICES ||
                statusCode == HttpStatus.SC_SEE_OTHER ||
                statusCode == HttpStatus.SC_TEMPORARY_REDIRECT ||
                statusCode == 308) { // todo follow
                // https://issues.apache.org/jira/browse/HTTPCORE-389
        			converagePatchFetch[5] = true;
                Header header = response.getFirstHeader("Location");
                if (header != null) {
            			converagePatchFetch[6] = true;
                    String movedToUrl =
                        URLCanonicalizer.getCanonicalURL(header.getValue(), toFetchURL);
                    fetchResult.setMovedToUrl(movedToUrl);
                } else {
        				converagePatchFetch[7] = true;
                }
            } else if (statusCode >= 200 && statusCode <= 299) { // is 2XX, everything looks ok
        			converagePatchFetch[8] = true;
                fetchResult.setFetchedUrl(toFetchURL);
                String uri = request.getURI().toString();
                if (!uri.equals(toFetchURL)) {
            			converagePatchFetch[9] = true;
                    if (!URLCanonicalizer.getCanonicalURL(uri).equals(toFetchURL)) {
                			converagePatchFetch[10] = true;
                        fetchResult.setFetchedUrl(uri);
                    } else {
            				converagePatchFetch[9] = true;
                    }
                } else{
        				converagePatchFetch[10] = true;
                }

                // Checking maximum size
                if (fetchResult.getEntity() != null) {
            			converagePatchFetch[11] = true;
                    long size = fetchResult.getEntity().getContentLength();
                    if (size == -1) {
                			converagePatchFetch[12] = true;
                        Header length = response.getLastHeader("Content-Length");
                        if (length == null) {
                    			converagePatchFetch[13] = true;
                            length = response.getLastHeader("Content-length");
                        } else {
                				converagePatchFetch[14] = true;
                        }
                        if (length != null) {
                    			converagePatchFetch[15] = true;
                            size = Integer.parseInt(length.getValue());
                        } else {
                				converagePatchFetch[16] = true;
                        }
                    } else {
        					converagePatchFetch[17] = true;
                    }
                    if (size > config.getMaxDownloadSize()) {
                			converagePatchFetch[18] = true;
                        //fix issue #52 - consume entity
                        response.close();
                        PageFetcher.createFile("fetchPage_coverage", converagePatchFetch);
                        throw new PageBiggerThanMaxSizeException(size);
                    } else {
        					converagePatchFetch[19] = true;
                    }
                } else {
    					converagePatchFetch[20] = true;
                }
            } else {
    				converagePatchFetch[21] = true;
            }
            
            fetchResult.setStatusCode(statusCode);
            PageFetcher.createFile("fetchPage_coverage", converagePatchFetch);
            return fetchResult;

        } finally { // occurs also with thrown exception
			converagePatchFetch[22] = true;
            if ((fetchResult.getEntity() == null) && (request != null)) {
    				converagePatchFetch[23] = true;
                request.abort();
            } else {
    				converagePatchFetch[24] = true;
            }
            PageFetcher.createFile("fetchPage_coverage", converagePatchFetch);
        }
    }

    public synchronized void shutDown() {
        if (connectionMonitorThread != null) {
            connectionManager.shutdown();
            connectionMonitorThread.shutdown();
        }
    }

    /**
     * Creates a new HttpUriRequest for the given url. The default is to create a HttpGet without
     * any further configuration. Subclasses may override this method and provide their own logic.
     *
     * @param url the url to be fetched
     * @return the HttpUriRequest for the given url
     */
    protected HttpUriRequest newHttpUriRequest(String url) {
        return new HttpGet(url);
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
