package com.bihju;

import com.bihju.service.CategoryService;
import lombok.extern.log4j.Log4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Log4j
public class CategoryCrawlerTask {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private CategoryService categoryService;
    private List<String> proxyList;
    private int index = 0;
    private static final String AMAZON_URL = "https://www.amazon.com/s/ref=nb_sb_noss_2?url=search-alias=aps&field-keywords=-12345";
    private static final String PRODUCT_LIST_URL = "https://www.amazon.com/s/ref=nb_sb_noss?url=$SEARCH_ALIAS&field-keywords=-12345&page=$PAGE_NO";
    private static final String WHAT_IS_MY_IP_ADDRESS = "https://whatismyipaddress.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private static final String CATEGORY_SELECTOR = "#searchDropdownBox > option:nth-child($NUMBER)";
    private static final int TIMEOUT_IN_MILLISECONDS = 100000;
    private final String AUTH_USER = "bittiger";
    private final String AUTH_PASSWORD = "cs504";
    private Map<String, String> headers;

    @Autowired
    public CategoryCrawlerTask(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void init(String proxyFilePath) {
        initProxyList(proxyFilePath);
        initHeaders();
    }

    @Scheduled(cron = "0 0 0 * * SUN")   // every Sunday
    public void startCrawling() {
        log.info("Start crawling at: " + dateFormat.format(new Date()));

        setProxy();
        Document doc = null;
        try {
            doc = Jsoup.connect(AMAZON_URL).headers(headers).userAgent(USER_AGENT)
                    .timeout(TIMEOUT_IN_MILLISECONDS).get();
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return;
        }

        log.debug("doc = " + doc.text());
        int i = 2;
        while (true) {
            String selector = CATEGORY_SELECTOR.replace("$NUMBER", String.valueOf(i++));
            Elements results = doc.select(selector);
            if (results.isEmpty()) {
                log.debug("All categories are retrieved.");
                break;
            }

            String categoryName = results.get(0).text();
            String categorySearchAlias = results.get(0).attr("value");
//            String categoryName = "Alexa Skills";   // for testing
//            String categorySearchAlias = "search-alias=alexa-skills";  // for testing
            log.debug("category = " + categoryName + ", search-alias = " + categorySearchAlias);
            String productListUrl = PRODUCT_LIST_URL.replace("$SEARCH_ALIAS", categorySearchAlias);
            categoryService.saveCategory(categoryName, productListUrl);

            try {
                Thread.sleep(20000); // wait 2 seconds before next round
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initProxyList(String proxyFilePath) {
        proxyList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(proxyFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                AUTH_USER, AUTH_PASSWORD.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", AUTH_USER);
        System.setProperty("http.proxyPassword", AUTH_PASSWORD);
        System.setProperty("socksProxyPort", "61336"); // set proxy port
    }

    private void setProxy() {
        do {
            String proxy = proxyList.get(index);
            log.debug("proxy = " + proxy);
            System.setProperty("socksProxyHost", proxy); // set proxy server
            index++;
            if (index == proxyList.size()) {
                index = 0;
            }
        } while (!testProxy());
    }

    private boolean testProxy() {
        try {
            Jsoup.connect(WHAT_IS_MY_IP_ADDRESS).headers(headers).userAgent(USER_AGENT)
                    .timeout(TIMEOUT_IN_MILLISECONDS).get();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private void initHeaders() {
        headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
        headers.put("Accept-Language", "en-US,en;q=0.8");
    }
}