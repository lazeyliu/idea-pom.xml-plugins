package com.github.rxyor.plugin.pom.assistant.common.maven.util;

import com.github.rxyor.plugin.pom.assistant.common.constant.PluginConst;
import com.github.rxyor.plugin.pom.assistant.common.constant.PluginConst.PomTag;
import com.github.rxyor.plugin.pom.assistant.common.maven.model.DependencyPair;
import com.github.rxyor.plugin.pom.assistant.common.psi.util.PsiUtil;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.psi.xml.XmlToken;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.dom.model.MavenDomDependencies;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomDependencyManagement;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.model.MavenId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Copy from MavenDomUtil
 * </p>
 *
 * @author liuyang
 * @since 2020/2/3 周一 00:48:00
 * @since 1.0.0
 */
public class MavenDependencyUtil {

    private MavenDependencyUtil() {
    }

    /**
     * 添加依赖
     *
     * @author liuyang
     * @since 2020-02-09 周日 17:12:35
     */
    public static MavenDomDependency addDomDependency(
            @NotNull MavenDomDependencyManagement management,
            MavenId mavenId) {
        return addDomDependency(management.getDependencies(), mavenId);
    }

    /**
     * 添加依赖
     *
     * @author liuyang
     * @since 2020-02-09 周日 17:12:48
     */
    public static MavenDomDependency addDomDependency(
            @NotNull MavenDomDependencies dependencies,
            MavenId mavenId) {
        if (mavenId == null) {
            return null;
        }
        MavenDomDependency domDependency = dependencies.addDependency();
        domDependency.getGroupId().setValue(mavenId.getGroupId());
        domDependency.getArtifactId().setValue(mavenId.getArtifactId());
        domDependency.getVersion().setValue(mavenId.getVersion());
        return domDependency;
    }

    /**
     * 比较2个依赖的groupId、artifactId、version是否一致
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:08:55
     */
    public static boolean equals(@NotNull MavenDomDependency d1,
                                 @NotNull MavenDomDependency d2) {
        boolean isSameGroupId = StringUtils.equals(d1.getGroupId().getValue(),
                d2.getGroupId().getValue());
        boolean isSameArtifactId = StringUtils.equals(d1.getArtifactId().getValue(),
                d2.getArtifactId().getValue());
        boolean isSameVersion = StringUtils.equals(d1.getVersion().getValue(),
                d2.getVersion().getValue());

        return isSameGroupId && isSameArtifactId && isSameVersion;
    }

    /**
     * 寻找groupId、artifactId、version一致的依赖
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:10:18
     */
    public static DependencyPair findDependency(
            @NotNull final MavenDomProjectModel model,
            @NotNull final MavenDomDependency dependency) {
        DependencyPair pair = new DependencyPair();
        MavenDomDependency managementFound =
                findDependency(model.getDependencyManagement(), dependency);
        MavenDomDependency found =
                findDependency(model.getDependencies(), dependency);

        pair.setManagementDependency(managementFound);
        pair.setDependency(found);
        return pair;
    }

    /**
     * 寻找groupId、artifactId、version一致的依赖
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:10:18
     */
    public static MavenDomDependency findDependency(
            @NotNull final MavenDomDependencyManagement management,
            @NotNull final MavenDomDependency dependency) {
        if (management.getDependencies() == null) {
            return null;
        }
        return findDependency(management.getDependencies(), dependency);
    }

    /**
     * 寻找groupId、artifactId、version一致的依赖
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:10:18
     */
    public static MavenDomDependency findDependency(
            @NotNull final MavenDomDependencies dependencies,
            @NotNull final MavenDomDependency dependency) {
        List<MavenDomDependency> list = dependencies.getDependencies();
        if (list != null && !list.isEmpty()) {
            for (MavenDomDependency e : list) {
                if (equals(e, dependency)) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * 寻找groupId、artifactId、version一致的依赖
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:10:18
     */
    public static DependencyPair findDependency(
            @NotNull final MavenDomProjectModel model,
            @NotNull final MavenId mavenId) {
        DependencyPair pair = new DependencyPair();
        MavenDomDependency managementFound =
                findDependency(model.getDependencyManagement(), mavenId);
        MavenDomDependency found =
                findDependency(model.getDependencies(), mavenId);

        pair.setManagementDependency(managementFound);
        pair.setDependency(found);
        return pair;
    }

    /**
     * 寻找groupId、artifactId、version一致的依赖
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:10:18
     */
    public static MavenDomDependency findDependency(
            @NotNull final MavenDomDependencyManagement management,
            @NotNull final MavenId mavenId) {
        if (management.getDependencies() == null) {
            return null;
        }
        return findDependency(management.getDependencies(), mavenId);
    }

    /**
     * 寻找groupId、artifactId、version一致的依赖
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:10:18
     */
    public static MavenDomDependency findDependency(
            @NotNull final MavenDomDependencies dependencies,
            @NotNull final MavenId mavenId) {
        List<MavenDomDependency> list = dependencies.getDependencies();
        if (list != null && !list.isEmpty()) {
            for (MavenDomDependency e : list) {
                MavenId item = MavenIdUtil.convert(e);
                if (StringUtils.equals(mavenId.getGroupId(), item.getGroupId())
                        && StringUtils.equals(mavenId.getArtifactId(), item.getArtifactId())) {
                    return e;
                }
            }
        }
        return null;
    }

    /**
     * 寻找点击的dependency标签
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:45:41
     */
    public static XmlTag findDependencyTag(@NotNull PsiElement element) {

        if (element instanceof XmlToken) {
            PsiElement p = element.getParent();
            XmlTag parentTag = null;
            if (p instanceof XmlText parent) {
                XmlToken nextSibling = (XmlToken) parent.getNextSibling();
                parentTag = (XmlTag) nextSibling.getParent();

            } else if (p instanceof XmlTag) {
                parentTag = (XmlTag) p;
            }

            while (parentTag != null
                    && !parentTag.toString()
                    .replaceAll(PluginConst.XmlTag.PREFIX, "")
                    .equalsIgnoreCase(PomTag.DEPENDENCY)) {
                parentTag = parentTag.getParentTag();
            }
            return parentTag;
        }
        return null;
    }

    /**
     * get dependency from <dependencies> or <dependencyManagement> where click
     *
     * @author liuyang
     * @since 2020-02-03 周一 14:44:39
     */
    public static MavenDomDependency getClickDependency(
            @NotNull MavenDomProjectModel model, @Nullable Editor editor) {
        MavenDomDependency dependency = getClickMavenDomDependency(model, editor);
        if (dependency == null) {
            return getClickDomManagementDependency(model, editor);
        }
        return dependency;
    }

    /**
     * get dependency from <dependencyManagement> where click
     *
     * @author liuyang
     * @since 2020-02-03 周一 14:46:49
     */
    public static MavenDomDependency getClickDomManagementDependency(
            @NotNull MavenDomProjectModel model, @Nullable Editor editor) {
        MavenDomDependencyManagement management = model.getDependencyManagement();
        if (management != null) {
            return getClickMavenDomDependency(management.getDependencies(), editor);
        }
        return null;
    }

    /**
     * get dependency from <dependencies> where click
     *
     * @author liuyang
     * @since 2020-02-03 周一 14:46:19
     */
    public static MavenDomDependency getClickMavenDomDependency(
            @NotNull MavenDomProjectModel model, @Nullable Editor editor) {
        return getClickMavenDomDependency(model.getDependencies(), editor);
    }

    /**
     * get dependency from MavenDomDependencies where click
     *
     * @author liuyang
     * @since 2020-02-03 周一 14:47:58
     */
    public static MavenDomDependency getClickMavenDomDependency(
            @NotNull final MavenDomDependencies dependencies,
            @Nullable final Editor editor) {
        if (editor != null) {
            int offset = editor.getCaretModel().getOffset();

            List<MavenDomDependency> dependencyList = dependencies.getDependencies();

            for (int i = 0; i < dependencyList.size(); i++) {
                MavenDomDependency dependency = dependencyList.get(i);
                XmlElement xmlElement = dependency.getXmlElement();

                if (xmlElement != null
                        && xmlElement.getTextRange().getStartOffset() <= offset
                        && xmlElement.getTextRange().getEndOffset() >= offset
                ) {
                    return dependencyList.get(i);
                }
            }
        }
        return null;
    }

    /**
     * 获取点击的MavenId
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:48:45
     */
    public static MavenId getClickMavenId(@NotNull AnActionEvent e) {
        PsiElement psiElement = PsiUtil.getClickPsiElement(e);
        return getClickMavenId(psiElement);
    }

    /**
     * 获取点击的MavenId
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:48:45
     */
    @SuppressWarnings("all")
    public static MavenId getClickMavenId(@NotNull PsiElement psiElement) {

        XmlTag dependencyTag = findDependencyTag(psiElement);
        if (dependencyTag == null) {
            return null;
        }

        //left store xml tag,right store xml value
        List<String> mavenIdTags = Lists.newArrayList(
                PomTag.GROUP_ID, PomTag.ARTIFACT_ID, PomTag.VERSION);

        Map<String, String> map = new HashMap<>(4);
        for (String tag : mavenIdTags) {
            XmlTag xmlTag = dependencyTag.findFirstSubTag(tag);
            if (xmlTag != null) {
                map.put(tag, xmlTag.getValue().getText());
            }
        }

        if (map.size() != 0) {
            return new MavenId(map.get(PomTag.GROUP_ID),
                    map.get(PomTag.ARTIFACT_ID), map.get(PomTag.VERSION));
        }
        return null;
    }

    /**
     * 移除版本号
     *
     * @author liuyang
     * @since 2020-02-03 周一 16:18:57
     */
    public static void removeVersion(@NotNull MavenDomDependency dependency) {
        dependency.getVersion().setStringValue(null);
    }
}
