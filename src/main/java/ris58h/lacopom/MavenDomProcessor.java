package ris58h.lacopom;

import org.jetbrains.idea.maven.dom.model.*;

public class MavenDomProcessor {
    protected void onDependency(MavenDomDependency dependency) {
    }

    protected void onExclusion(MavenDomExclusion exclusion) {
    }

    protected void onExecution(MavenDomPluginExecution execution) {
    }

    protected void onExtension(MavenDomExtension extension) {
    }

    protected void onParent(MavenDomParent parent) {
    }

    protected void onPlugin(MavenDomPlugin plugin) {
    }

    protected void onProfile(MavenDomProfile profile) {
    }

    public final void process(MavenDomProjectModel projectModel) {
        processProjectModel(projectModel);
    }

    private void processDependencies(MavenDomDependencies dependencies) {
        dependencies.getDependencies().forEach(dependency -> {
            onDependency(dependency);
            dependency.getExclusions().getExclusions().forEach(this::onExclusion);
        });
    }

    private void processExecutions(MavenDomExecutions executions) {
        executions.getExecutions().forEach(this::onExecution);
    }

    private void processModelBase(MavenDomProjectModelBase mb) {
        processDependencies(mb.getDependencies());
        processDependencies(mb.getDependencyManagement().getDependencies());
        mb.getBuild().getPlugins().getPlugins().forEach(this::processPlugin);
        mb.getBuild().getPluginManagement().getPlugins().getPlugins().forEach(this::processPlugin);
    }

    private void processPlugin(MavenDomPlugin plugin) {
        onPlugin(plugin);
        processDependencies(plugin.getDependencies());
        processExecutions(plugin.getExecutions());
    }

    private void processProjectModel(MavenDomProjectModel projectModel) {
        onParent(projectModel.getMavenParent());
        processModelBase(projectModel);
        projectModel.getProfiles()
                .getProfiles()
                .forEach(profile -> {
                    onProfile(profile);
                    processModelBase(profile);
                });
        projectModel.getBuild().getExtensions().getExtensions().forEach(this::onExtension);
    }
}