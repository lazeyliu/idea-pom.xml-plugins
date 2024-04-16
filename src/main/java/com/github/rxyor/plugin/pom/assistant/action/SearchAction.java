package com.github.rxyor.plugin.pom.assistant.action;

import com.github.rxyor.plugin.pom.assistant.common.maven.util.MavenIdUtil;
import com.github.rxyor.plugin.pom.assistant.common.psi.util.PsiUtil;
import com.github.rxyor.plugin.pom.assistant.ui.maven.SearchDependencyDialog;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenId;

import java.awt.*;

/**
 * @author liuyang
 * @since 2020/2/8 周六 14:31:00
 * @since 1.0.0
 */
public class SearchAction extends AbstractPomAction {

    private final static String DIALOG_TITLE = "Search Maven Dependency";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MavenId clickMavenId = getClickMavenId(e);
        SearchDependencyDialog dialog = new SearchDependencyDialog(e, clickMavenId);
        dialog.popup(DIALOG_TITLE, new Dimension(600, 350),
                new Dimension(800, 600));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /**
     * 获取点击到依赖
     *
     * @param e
     * @return
     */
    private MavenId getClickMavenId(@NotNull AnActionEvent e) {
        final PsiElement psiElement = PsiUtil.getClickPsiElement(e);
        //获取点击到依赖
        MavenId mavenId = MavenIdUtil.getClickMavenId(psiElement);
        if (mavenId == null
                || StringUtils.isBlank(mavenId.getGroupId())
                || StringUtils.isBlank(mavenId.getArtifactId())) {
            return null;
        }

        return new MavenId(mavenId.getGroupId(), mavenId.getArtifactId(), null);
    }
}
