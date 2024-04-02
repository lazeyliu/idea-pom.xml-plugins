package com.github.rxyor.plugin.pom.assistant.common.notification.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import lombok.Getter;
import lombok.ToString;

import javax.swing.*;

/**
 * <p>
 *
 * </p>
 *
 * @author liuyang
 * @date 2020/1/24 周五 17:20:00
 * @since 1.0.0
 */
@Getter
@ToString
public class NotificationBuilder {

    private String myGroupId;
    private Icon myIcon;
    private NotificationType myType;
    private String myTitle;
    private String mySubtitle;
    private String myContent;
    private NotificationAction myAnAction;

    NotificationBuilder() {
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public NotificationBuilder myGroupId(String myGroupId) {
        this.myGroupId = myGroupId;
        return this;
    }

    public NotificationBuilder myIcon(Icon myIcon) {
        this.myIcon = myIcon;
        return this;
    }

    public NotificationBuilder myType(NotificationType myType) {
        this.myType = myType;
        return this;
    }

    public NotificationBuilder myTitle(String myTitle) {
        this.myTitle = myTitle;
        return this;
    }

    public NotificationBuilder mySubtitle(String mySubtitle) {
        this.mySubtitle = mySubtitle;
        return this;
    }

    public NotificationBuilder myContent(String myContent) {
        this.myContent = myContent;
        return this;
    }


    public NotificationBuilder myAnAction(NotificationAction myAnAction) {
        this.myAnAction = myAnAction;
        return this;
    }

    public Notification build() {
        Notification notification = new Notification(
                this.myGroupId,
                this.myContent,
                this.myType);
        return notification.setIcon(this.getMyIcon())
                .setTitle(this.getMyTitle())
                .setSubtitle(this.getMySubtitle())
                .addAction(myAnAction);
    }

}
