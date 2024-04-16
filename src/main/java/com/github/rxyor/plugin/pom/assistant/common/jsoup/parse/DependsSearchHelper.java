package com.github.rxyor.plugin.pom.assistant.common.jsoup.parse;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.jetbrains.idea.maven.model.MavenId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyang
 * @date 2020/2/8 周六 21:07:00
 * @since 1.0.0
 */
public class DependsSearchHelper {

    private final static String SEARCH_URL = "https://mvnrepository.com/search?q=%s&p=%s";

    private final static Integer PAGE_SIZE = 10;
    private final String keyword;
    private List<MavenId> items = new ArrayList<>();
    private Integer page = 1;
    private Document document;

    private DependsSearchHelper(String keyword) {
        this.keyword = keyword;
    }

    public static Builder builder() {
        return new Builder();
    }

    private Document connect(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36 Edg/123.0.0.0")
                    .get();
        } catch (Exception e) {
            throw new RuntimeException("request url:[" + url + "] fail, error:" + e.getMessage(), e);
        }
    }

    public Page<MavenId> jumpPage(Integer pageIndex) {
        if (pageIndex == null || pageIndex <= 0) {
            throw new IllegalArgumentException("pageIndex must satisfy [pageIndex  > 0]");
        }
        page = pageIndex;
        return search();
    }

    public Page<MavenId> nextPage() {
        page++;
        return search();
    }

    private List<MavenId> parseItems() {
        if (document == null) {
            return new ArrayList<>(0);
        }

        Document imSubtitleDoc = Jsoup.parse(document
                .getElementsByClass("im-subtitle").html());
        String text = imSubtitleDoc.getElementsByAttribute("href").text();
        List<String> groupIdAndArtifactIdList = Splitter.on(" ").omitEmptyStrings()
                .trimResults().splitToList(text);

        final int len = groupIdAndArtifactIdList.size();
        List<MavenId> mavenIdList = new ArrayList<>(len / 2);
        for (int i = 0; i < len; i += 2) {
            if (i >= len) {
                break;
            }
            String groupId = groupIdAndArtifactIdList.get(i);
            String artifactId = groupIdAndArtifactIdList.get(i + 1);
            mavenIdList.add(new MavenId(groupId, artifactId, null));
        }
        return mavenIdList;
    }

    private int parseTotal() {
        if (document == null) {
            return 0;
        }

        Element maincontentElement = document.getElementById("maincontent");
        String content = maincontentElement.child(0).text();
        Integer total = Integer.parseInt(content.replaceAll("[\\sa-zA-Z]+", ""));
        return Optional.ofNullable(total).orElse(0);
    }

    public Page<MavenId> prePage() {
        if (page > 1) {
            page--;
        }
        return search();
    }

    public Page<MavenId> search() {
        String url = String.format(SEARCH_URL, keyword, page);
        document = connect(url);

        int total = parseTotal();
        if (total == 0) {
            return new Page<>(page, PAGE_SIZE, total, new ArrayList<>(0));
        }
        items = parseItems();
        return new Page<>(page, PAGE_SIZE, total, Lists.newArrayList(items));
    }

    public static class Builder {

        private String keyword;

        public DependsSearchHelper build() {
            return new DependsSearchHelper(keyword);
        }

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }
    }

}
