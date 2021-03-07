package com.guoli.search.util;

import com.guoli.search.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * 爬取数据
 *
 * @author guoli
 * @data 2021-03-06 21:43
 */
@Component
public class HtmlParseUtil {
    /**
     * 爬取京东数据
     * @param keywod 搜索关键字
     * @return 爬取的数据
     * @throws IOException IO异常
     */
    public List<Content> parseJd(String keywod) throws IOException {
        String url = "https://search.jd.com/Search?keyword=" + keywod + "&enc=utf-8";
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements li = element.getElementsByTag("li");
        List<Content> contents = new ArrayList<>();
        for (Element el: li) {
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();
            String name = el.getElementsByClass("p-name").eq(0).text();
            Content content = new Content(name, price, img);
            contents.add(content);
        }
        return contents;
    }
}
