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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * @author Yasser Ganjisaffar
 */

@Entity
public class WebURL implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKey
    private String url;

    private int docid;
    private int parentDocid;
    private String parentUrl;
    private short depth;
    private String domain;
    private String subDomain;
    private String path;
    private String anchor;
    private byte priority;
    private String tag;
    private Map<String, String> attributes;

    /**
     * @return unique document id assigned to this Url.
     */
    public int getDocid() {
        return docid;
    }

    public void setDocid(int docid) {
        this.docid = docid;
    }

    /**
     * @return Url string
     */
    public String getURL() {
        return url;
    }
    
    static boolean[] coverage = new boolean[9];

    public void setURL(String url) {
        this.url = url;
        coverage[0] = true;
        int domainStartIdx = url.indexOf("//") + 2;
        int domainEndIdx = url.indexOf('/', domainStartIdx);
        domainEndIdx = (domainEndIdx > domainStartIdx) ? domainEndIdx : url.length();
        domain = url.substring(domainStartIdx, domainEndIdx);
        subDomain = "";
        String[] parts = domain.split("\\.");
        if (parts.length > 2) {
            coverage[1] = true;
            domain = parts[parts.length - 2] + "." + parts[parts.length - 1];
            int limit = 2;
            if (TLDList.getInstance().contains(domain)) {
                coverage[2] = true;
                domain = parts[parts.length - 3] + "." + domain;
                limit = 3;
            } else {
                coverage[3] = true;
            }
            for (int i = 0; i < (parts.length - limit); i++) {
                coverage[4] = true;
                if (!subDomain.isEmpty()) {
                    coverage[5] = true;
                    subDomain += ".";
                } else {
                    coverage[6] = true;
                }
                subDomain += parts[i];
            }
        }
        path = url.substring(domainEndIdx);
        int pathEndIdx = path.indexOf('?');
        if (pathEndIdx >= 0) {
            coverage[7] = true;
            path = path.substring(0, pathEndIdx);
        } else {
            coverage[8] = true;
        }
        WebURL.createFile("setURL_coverage", coverage);
    }

    /**
     * @return
     *      unique document id of the parent page. The parent page is the
     *      page in which the Url of this page is first observed.
     */
    public int getParentDocid() {
        return parentDocid;
    }

    public void setParentDocid(int parentDocid) {
        this.parentDocid = parentDocid;
    }

    /**
     * @return
     *      url of the parent page. The parent page is the page in which
     *      the Url of this page is first observed.
     */
    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    /**
     * @return
     *      crawl depth at which this Url is first observed. Seed Urls
     *      are at depth 0. Urls that are extracted from seed Urls are at depth 1, etc.
     */
    public short getDepth() {
        return depth;
    }

    public void setDepth(short depth) {
        this.depth = depth;
    }

    /**
     * @return
     *      domain of this Url. For 'http://www.example.com/sample.htm', domain will be 'example
     *      .com'
     */
    public String getDomain() {
        return domain;
    }

    public String getSubDomain() {
        return subDomain;
    }

    /**
     * @return
     *      path of this Url. For 'http://www.example.com/sample.htm', domain will be 'sample.htm'
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return
     *      anchor string. For example, in <a href="example.com">A sample anchor</a>
     *      the anchor string is 'A sample anchor'
     */
    public String getAnchor() {
        return anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * @return priority for crawling this URL. A lower number results in higher priority.
     */
    public byte getPriority() {
        return priority;
    }

    public void setPriority(byte priority) {
        this.priority = priority;
    }

    /**
     * @return tag in which this URL is found
     * */
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String name) {
        if (attributes == null) {
            return "";
        }
        return attributes.getOrDefault(name, "");
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        WebURL otherUrl = (WebURL) o;
        return (url != null) && url.equals(otherUrl.getURL());

    }

    @Override
    public String toString() {
        return url;
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