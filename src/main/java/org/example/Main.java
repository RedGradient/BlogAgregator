package org.example;


import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    static final String RESOURCES_PATH = "src/main/resources";
    static final String[] PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "E, d MMM yyyy HH:mm:ss z",
            "E, d MMM yyyy HH:mm z"
    };

    public static void main(String[] args) throws Exception {
        final var app = Javalin.create().start(7070);

        app.get("/", ctx -> {
            final var channels = getLatestPosts(50);
            ctx.render("index.jte", Collections.singletonMap("channels", channels));
        });
    }

    private static Map<String, String> getLatestPosts(int count) throws Exception {
        final var allPosts = getAllPosts();

        final var comp = Comparator.comparing((x) -> {
            final var date = getInnerTagContent((Element) x, "pubDate").orElseThrow();
            return parseDate(date);
        }).reversed();

        return allPosts.stream()
                .sorted(comp)
                .limit(count)
                .collect(Collectors.toMap(
                        el -> getInnerTagContent(el, "title").orElseThrow(),
                        el -> getInnerTagContent(el, "link").orElseThrow()
                ));
    }

    private static List<Element> getAllPosts() throws Exception {
        final var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        final var directory = new File(RESOURCES_PATH);
        final FilenameFilter xmlFilter = (dir, name) -> name.toLowerCase().endsWith(".xml");
        final var files = directory.listFiles(xmlFilter);
        if (files == null) {
            throw new FileNotFoundException("Resource path not found: %s".formatted(RESOURCES_PATH));
        }

        final List<Element> posts = new ArrayList<>();
        for (var file : files) {
            final var doc = builder.parse(file.toString());
            final var nodeList = doc.getDocumentElement().getElementsByTagName("item");
            for (var i = 0; i < nodeList.getLength(); i++) {
                var el = (Element) nodeList.item(i);
                if (el == null) { continue; }
                posts.add(el);
            }
        }

        return posts;
    }

    private static Optional<String> getInnerTagContent(@NotNull Element el, String tagName) {
        var els = el.getElementsByTagName(tagName);
        if (els.getLength() != 1) {
            return Optional.empty();
        }
        var content = els.item(0).getTextContent();
        if (content == null) { return Optional.empty(); }
        return Optional.of(content);
    }

    public static Date parseDate(String date) {
        for (String pattern : PATTERNS) {
            final var sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
            try {
                return sdf.parse(date);
            } catch (Exception ignored) {

            }
        }

        throw new RuntimeException("Cannot parce the date: %s".formatted(date));
    }
}
