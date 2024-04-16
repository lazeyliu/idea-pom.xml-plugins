package com.github.rxyor.plugin.pom.assistant.common.dom.processor;


import com.google.common.collect.Lists;
import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * @author liuyang
 * @since 2020/2/4 周二 21:51:00
 * @since 1.0.0
 */
public class SortPomProcessor extends AbstractPomProcessor {

    public SortPomProcessor(String text) {
        super(text);
    }

    public SortPomProcessor(String text,
                            AbstractPomProcessor processor) {
        super(processor);
    }

    @Override
    protected void doProcess() {
        sort();
    }

    /**
     * 给元素的注释分组
     *
     * @param cur cur
     * @return
     * @author liuyang
     * @date 2020-02-06 周四 00:43:51
     */
    private Map<Element, List<Comment>> groupComment(Element cur) {
        final Map<Element, List<Comment>> group = new HashMap<>(4);
        if (cur == null || !cur.nodeIterator().hasNext()) {
            return group;
        }

        Iterator<Node> it = cur.nodeIterator();
        LinkedList<Comment> comments = new LinkedList<>();
        while (it.hasNext()) {
            Node node = it.next();
            if (node instanceof Comment) {
                comments.add((Comment) node);
            } else if (node instanceof Element) {
                group.put((Element) node, Lists.newArrayList(comments));
                //reset list
                comments.clear();
            }
        }

        return group;
    }

    /**
     * 递归排序
     *
     * @param cur cur
     * @return
     * @author liuyang
     * @date 2020-02-06 周四 00:43:10
     */
    private void recursivelySort(Element cur) {
        if (cur == null || cur.elements().isEmpty()) {
            return;
        }

        List<Element> list = cur.elements();
        Iterator<Element> it = list.iterator();
        while (it.hasNext()) {
            this.recursivelySort(it.next());
        }

        //注释分组
        final Map<Element, List<Comment>> commentGroup = groupComment(cur);
        //标签排序
        list.sort(new SortComparator());
        //清空标签
        cur.clearContent();
        //恢复子级标签以及注释
        list.forEach(e -> {
            List<Comment> comments = commentGroup.get(e);
            comments.forEach(c -> cur.add(c));
            cur.add(e);
        });
    }

    private void sort() {
        this.recursivelySort(super.document.getRootElement());
    }
}
