package com.github.rxyor.plugin.pom.assistant.ui.maven;

import com.github.rxyor.plugin.pom.assistant.common.clipboard.ClipboardUtil;
import com.github.rxyor.plugin.pom.assistant.common.jsoup.parse.DependsSearchHelper;
import com.github.rxyor.plugin.pom.assistant.common.jsoup.parse.ListVersionHelper;
import com.github.rxyor.plugin.pom.assistant.common.jsoup.parse.Page;
import com.github.rxyor.plugin.pom.assistant.common.maven.model.DependencyPair;
import com.github.rxyor.plugin.pom.assistant.common.maven.util.MavenDependencyUtil;
import com.github.rxyor.plugin.pom.assistant.common.maven.util.MavenProjectUtil;
import com.github.rxyor.plugin.pom.assistant.common.notification.util.NotificationUtil;
import com.github.rxyor.plugin.pom.assistant.common.psi.util.PsiUtil;
import com.google.common.base.Splitter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.model.MavenDomDependencies;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.model.MavenId;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyang
 * @since 2020/2/8 周六 12:55:00
 * @since 1.0.0
 */
public class SearchDependencyDialog extends BaseDialog {

    private final static ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private JPanel panel;
    private JTextField keywordTxt;
    private JButton searchBtn;
    private JList<String> searchRetList;
    private JScrollPane scrollPane;
    JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
    private JTextArea dependDetail;
    private JComboBox<String> versionBox;
    private JButton copyBtn;
    private JButton addBtn;
    private JButton updateBtn;
    private DependsSearchHelper searcher = null;
    private MavenId clickMavenId = null;

    public SearchDependencyDialog(@NotNull AnActionEvent e) {
        super(e);
        this.init();
    }

    public SearchDependencyDialog(@NotNull AnActionEvent e, MavenId clickMavenId) {
        super(e);
        this.clickMavenId = clickMavenId;
        this.init();
    }

    /**
     * 添加依赖
     *
     * @param mavenId
     */
    private void addDependency(MavenId mavenId) {
        if (mavenId == null) {
            return;
        }

        final PsiFile psiFile = PsiUtil.getPsiFile(e);
        final MavenDomProjectModel model = MavenProjectUtil
                .getMavenDomProjectModel(psiFile);

        DependencyPair dependencyPair = MavenDependencyUtil
                .findDependency(model, mavenId);
        boolean exist = isExist(dependencyPair);
        if (exist) {
            NotificationUtil.warn("Warn", "Dependency is existed", project);
        } else {
            WriteCommandAction.runWriteCommandAction(
                    PsiUtil.getProject(e),
                    () -> {
                        MavenDomDependencies domDependencies = model
                                .getDependencyManagement().getDependencies();
                        MavenDependencyUtil.addDomDependency(domDependencies, mavenId);
                        PsiUtil.reformat(psiFile);
                    });
        }
    }

    /**
     * 将数据添加到搜索列表当中
     *
     * @param page page
     * @author liuyang
     * @since 2020-02-09 周日 15:18:11
     */
    private void addToSearchListUI(Page<MavenId> page) {
        if (page.getTotal() <= 0) {
            return;
        }

        List<String> listData = page.getItems()
                .stream()
                .map(mavenId -> mavenId.getGroupId() + ":" + mavenId.getArtifactId())
                .toList();

        listData.forEach(s -> {
            if (!listModel.contains(s)) {
                listModel.addElement(s);
            }
        });
        refreshSearchListUI();
    }

    /**
     * 清空依赖面板的内容以及版本列表
     */
    private void clearDependDetailAndVersionListUI() {
        dependDetail.setText("");
        ListComboBoxModel<String> model = new ListComboBoxModel<>(new ArrayList<>(0));
        versionBox.setModel(model);
    }

    @Override
    protected void createUIComponents() {
    }

    /**
     * 发起网络请求搜索依赖列表
     *
     * @author liuyang
     * @since 2020-02-09 周日 15:08:32
     */
    private void doSearch(ActionEvent e) {
        MavenId mavenId = this.parseMavenId(keywordTxt.getText());
        Page<MavenId> page;
        //如果是com.alibaba:fastjson这种唯一标识的关键字，不用模糊搜索
        if (mavenId != null) {
            List<MavenId> list = new ArrayList<>(1);
            list.add(mavenId);
            page = new Page<>(1, 10, 1, list);
        } else {
            searcher = DependsSearchHelper.builder().keyword(keywordTxt.getText()).build();
            page = searcher.search();
        }
        listModel.clear();

        this.addToSearchListUI(page);
    }

    /**
     * 填充预准备数据到UI
     */
    private void fillPreDataToUI() {
        if (clickMavenId != null) {
            keywordTxt.setText(clickMavenId.getGroupId() + ":" + clickMavenId.getArtifactId());
        }
    }

    /**
     * 处理列表项点击事件
     */
    private void handleClickSearchItemEvent() {
        String s = searchRetList.getSelectedValue();
        clickMavenId = parseMavenId(s);

        if (clickMavenId == null) {
            clearDependDetailAndVersionListUI();
            return;
        }

        //查询点击的依赖的所有版本号
        List<String> versions = this.searchAndAddToVersionComboBoxUI(
                clickMavenId.getGroupId(),
                clickMavenId.getArtifactId()
        );
        if (!versions.isEmpty()) {
            clickMavenId = new MavenId(
                    clickMavenId.getGroupId(),
                    clickMavenId.getArtifactId(),
                    versions.get(0)
            );
            //填充到展示面板里
            setToDependDetail(clickMavenId);
        } else {
            clearDependDetailAndVersionListUI();
        }
    }

    /**
     * 处理列表滑动底部事件
     */
    private void handleScrollBottomEvent(AdjustmentEvent e) {
        int curH = e.getValue();
        int barLen = scrollBar.getHeight();
        int listH = searchRetList.getHeight();
        if (isCloseToBottom(listH, curH, barLen)) {
            nextPage();
        }
    }

    /**
     * 处理选择版本事件
     */
    private void handleSelectVersionEvent(ItemEvent e) {
        if (ItemEvent.SELECTED == e.getStateChange()) {
            clickMavenId = new MavenId(
                    clickMavenId.getGroupId(),
                    clickMavenId.getArtifactId(),
                    e.getItem().toString()
            );
            setToDependDetail(clickMavenId);
        }
    }

    /**
     * 初始化
     */
    protected void init() {
        super.setContentPane(panel);
        this.initViewBindListener();
        this.fillPreDataToUI();
    }

    /**
     * 视图绑定监听器
     */
    private void initViewBindListener() {
        //搜索按钮点击事件
        searchBtn.addActionListener(e -> THREAD_POOL.submit(() -> this.doSearch(e)));

        //滑条到底事件
        scrollBar.addAdjustmentListener(e -> THREAD_POOL.submit(() -> this.handleScrollBottomEvent(e)));

        //点击搜索列表事件
        searchRetList.addListSelectionListener(e -> THREAD_POOL.submit(this::handleClickSearchItemEvent));

        //版本选择框选中改变事件
        versionBox.addItemListener(e -> THREAD_POOL.submit(() -> handleSelectVersionEvent(e)));

        //复制按钮选择事件
        copyBtn.addActionListener(e -> {
            String text = dependDetail.getText();
            if (StringUtils.isNotBlank(text)) {
                ClipboardUtil.setToClipboard(dependDetail.getText());
                NotificationUtil.info("Info", "Dependency has copied", project);
            }
        });

        //添加按钮选择事件
        addBtn.addActionListener(e -> this.addDependency(clickMavenId));

        //更新按钮选择事件
        updateBtn.addActionListener(e -> this.updateDependency(clickMavenId));
    }

    /**
     * 滑条是否接近底部了
     *
     * @param listH  listH
     * @param curH   curH
     * @param barLen barLen
     * @author liuyang
     * @since 2020-02-09 周日 15:21:41
     */
    private boolean isCloseToBottom(int listH, int curH, int barLen) {
        if (listH == 0) {
            return true;
        }
        int dValue = listH / 5;
        return listH - (curH + barLen) <= dValue;
    }

    private boolean isExist(DependencyPair pair) {
        return pair != null &&
                (pair.getManagementDependency() != null || pair.getDependency() != null);
    }

    /**
     * 发起网络请求搜索下一页
     *
     * @author liuyang
     * @since 2020-02-09 周日 15:17:38
     */
    private void nextPage() {
        if (searcher == null) {
            return;
        }
        this.addToSearchListUI(searcher.nextPage());
    }

    /**
     * 解析字符串为MavenId
     */
    private MavenId parseMavenId(String s) {
        List<String> splitList = Splitter.on(":")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(s);
        if (splitList.size() < 2) {
            return null;
        }
        return new MavenId(splitList.get(0), splitList.get(1), null);
    }

    /**
     * 刷新搜索列表
     *
     * @author liuyang
     * @since 2020-02-09 周日 15:20:48
     */
    private void refreshSearchListUI() {
        searchRetList.setModel(listModel);
    }

    /**
     * 刷新版本号下拉列表
     */
    private void refreshVersionComboBoxUI(List<String> list) {
        if (list == null) {
            list = new ArrayList<>(0);
        }
        ListComboBoxModel<String> model = new ListComboBoxModel<>(list);
        versionBox.setModel(model);
    }

    /**
     * 搜索特定的groupId,artifactId 并把版本号填充到下拉列表
     *
     * @param groupId    groupId
     * @param artifactId artifactId
     * @author liuyang
     * @since 2020-02-09 周日 15:22:23
     */
    private List<String> searchAndAddToVersionComboBoxUI(String groupId, String artifactId) {
        List<String> list = this.searchVersionList(groupId, artifactId);
        this.refreshVersionComboBoxUI(list);
        return list;
    }

    /**
     * 发起网络请求搜索搜索特定的groupId,artifactId的所有版本号
     */
    private List<String> searchVersionList(String groupId, String artifactId) {
        List<MavenId> list = ListVersionHelper.list(groupId, artifactId);
        return list.stream().map(MavenId::getVersion).filter(StringUtils::isNotBlank)
                .distinct().collect(Collectors.toList());
    }

    /**
     * 设置依赖面板的内容
     */
    private void setToDependDetail(MavenId mavenId) {
        if (mavenId == null) {
            return;
        }

        StringBuilder sb = new StringBuilder("<dependency>\n");
        if (StringUtils.isNotBlank(mavenId.getGroupId())) {
            sb.append("\t<groupId>")
                    .append(mavenId.getGroupId())
                    .append("</groupId>\n");
        }
        if (StringUtils.isNotBlank(mavenId.getArtifactId())) {
            sb.append("\t<artifactId>")
                    .append(mavenId.getArtifactId()).append("</artifactId>\n");
        }
        if (StringUtils.isNotBlank(mavenId.getVersion())) {
            sb.append("\t<version>")
                    .append(mavenId.getVersion())
                    .append("</version>\n");
        }
        sb.append("</dependency>");

        dependDetail.setText(sb.toString());
    }

    /**
     * 更新依赖
     */
    private void updateDependency(MavenId mavenId) {
        if (mavenId == null) {
            return;
        }

        final PsiFile psiFile = PsiUtil.getPsiFile(e);
        final MavenDomProjectModel model = MavenProjectUtil
                .getMavenDomProjectModel(psiFile);

        DependencyPair dependencyPair = MavenDependencyUtil
                .findDependency(model, mavenId);
        boolean exist = isExist(dependencyPair);
        WriteCommandAction.runWriteCommandAction(PsiUtil.getProject(e), () -> {
            if (exist) {
                if (dependencyPair.getDependency() != null) {
                    dependencyPair.getDependency().getVersion().setValue(mavenId.getVersion());
                }
                if (dependencyPair.getManagementDependency() != null) {
                    dependencyPair.getManagementDependency().getVersion().setValue(mavenId.getVersion());
                }
            } else {
                MavenDomDependencies domDependencies = model
                        .getDependencyManagement().getDependencies();
                MavenDependencyUtil.addDomDependency(domDependencies, mavenId);
            }
            PsiUtil.reformat(psiFile);
        });
    }
}
