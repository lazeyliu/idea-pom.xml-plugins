package com.github.rxyor.plugin.pom.assistant.common.maven.util;

import com.github.rxyor.plugin.pom.assistant.common.constant.ValidConst.NoNull;
import com.google.common.base.Preconditions;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiFile;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

/**
 * @author liuyang
 * @since 2020/2/2 周日 21:13:00
 * @since 1.0.0
 */
public class MavenProjectUtil {

    private MavenProjectUtil() {
    }

    /**
     * read MavenDomProjectModel from PsiFile
     *
     * @author liuyang
     * @since 2020-02-03 周一 00:13:03
     */
    public static MavenDomProjectModel getMavenDomProjectModel(PsiFile psiFile) {
        Preconditions.checkNotNull(psiFile, NoNull.PsiFile);

        return MavenDomUtil.getMavenDomModel(psiFile, MavenDomProjectModel.class);
    }

    /**
     * read MavenProject from DataContext
     *
     * @author liuyang
     * @since 2020-02-02 周日 21:16:53
     */
    public static MavenProject getMavenProject(DataContext dataContext) {
        Preconditions.checkNotNull(dataContext, NoNull.DataContext);

        return MavenActionUtil.getMavenProject(dataContext);
    }

}
