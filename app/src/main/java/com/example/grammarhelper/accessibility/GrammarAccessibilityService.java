package com.example.grammarhelper.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.example.grammarhelper.service.FloatingBubbleService;

public class GrammarAccessibilityService extends AccessibilityService {

    private static GrammarAccessibilityService instance;

    public static GrammarAccessibilityService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED || event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            AccessibilityNodeInfo node = event.getSource();
            if (node != null && node.isEditable()) {
                CharSequence text = node.getText();
                if (text != null) {
                    // Send text to FloatingBubbleService for analysis
                    Intent intent = new Intent(this, FloatingBubbleService.class);
                    intent.setAction("ANALYZE_TEXT");
                    intent.putExtra("text", text.toString());
                    startService(intent);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {}

    public void injectText(String newText) {
        AccessibilityNodeInfo focusNode = getRootInActiveWindow().findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (focusNode != null && focusNode.isEditable()) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText);
            focusNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
    }
}
