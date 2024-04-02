package com.github.rxyor.plugin.pom.assistant.action;

import com.github.rxyor.plugin.pom.assistant.common.constant.PluginConst.App;
import com.github.rxyor.plugin.pom.assistant.common.dom.processor.FormatPomProcessor;
import com.github.rxyor.plugin.pom.assistant.common.dom.processor.GroupSortPomProcessor;
import com.github.rxyor.plugin.pom.assistant.common.psi.util.PsiUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyang
 * @date 2020/2/4 周二 14:47:00
 * @since 1.0.0
 */
public class GroupSortAction extends AbstractPomAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CommandProcessor.getInstance().executeCommand(e.getProject(),
                () -> {
                    sort(e);
                }
                , App.GROUP_ID, App.GROUP_ID);
    }

    private void sort(AnActionEvent e) {
        final PsiFile psiFile = PsiUtil.getPsiFile(e);
        if (checkMavenProjectFile(psiFile)) {
            return;
        }

        GroupSortPomProcessor groupSortPomProcessor = new GroupSortPomProcessor(PsiUtil.getText(psiFile));
        FormatPomProcessor formatPomProcessor = new FormatPomProcessor(groupSortPomProcessor);
        formatPomProcessor.process();
        final String result = formatPomProcessor.text();
        WriteCommandAction.runWriteCommandAction(PsiUtil.getProject(e), () -> {
            writeXmlFile(psiFile, result);
            PsiUtil.reformat(psiFile);
        });
    }

    private void writeXmlFile(@NotNull PsiFile psiFile, @NotNull String context) {
        final Project project = psiFile.getProject();
        final Document document = PsiUtil.getDocument(psiFile);
        final XmlFile xmlFile = (XmlFile) PsiFileFactory.getInstance(project)
                .createFileFromText("", XmlFileType.INSTANCE, context);
        document.setText(xmlFile.getText());
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }


}
