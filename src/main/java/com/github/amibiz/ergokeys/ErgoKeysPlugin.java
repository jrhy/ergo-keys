/*
 * Copyright 2018 Ami E. Bizamcher. All rights reserved.
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file.
 */

package com.github.amibiz.ergokeys;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapManagerListener;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

public class ErgoKeysPlugin implements ApplicationComponent {

    private static final Logger LOG = Logger.getInstance(ErgoKeysPlugin.class);
    private static final String ACTION_ID_ERGO_KEYS_COMMAND_MODE = "ErgoKeysCommandMode";

    private final ErgoKeysSettings settings;
    private final KeymapManagerEx keymapManagerEx;
    private final ActionBinding[] bindings = {
            new ActionBinding("AutoIndentLines", KeyEvent.VK_Q, KeyEvent.VK_QUOTE),
            new ActionBinding("EditorDeleteToWordStart", KeyEvent.VK_E, KeyEvent.VK_PERIOD),
            new ActionBinding("EditorDeleteToWordEnd", KeyEvent.VK_R, KeyEvent.VK_P),

            new ActionBinding("EditorToggleStickySelection", KeyEvent.VK_T, KeyEvent.VK_Y),
            new ActionBinding("$Undo", KeyEvent.VK_Y, KeyEvent.VK_F),

            new ActionBinding("GotoAction", KeyEvent.VK_A, KeyEvent.VK_A),
            new ActionBinding("EditorEnter", KeyEvent.VK_S, KeyEvent.VK_O),
            new ActionBinding("EditorBackSpace", KeyEvent.VK_D, KeyEvent.VK_E),
            new ActionBinding("ErgoKeysInsertMode", KeyEvent.VK_F, KeyEvent.VK_U),

            new ActionBinding("CommentByLineComment", KeyEvent.VK_Z, KeyEvent.VK_SEMICOLON),
            new ActionBinding("EditorCut", KeyEvent.VK_X, KeyEvent.VK_Q),
            new ActionBinding("EditorCopy", KeyEvent.VK_C, KeyEvent.VK_J),
            new ActionBinding("EditorPaste", KeyEvent.VK_V, KeyEvent.VK_K),

            new ActionBinding("EditorLeft", KeyEvent.VK_J, KeyEvent.VK_H),
            new ActionBinding("EditorRight", KeyEvent.VK_L, KeyEvent.VK_N),
            new ActionBinding("EditorUp", KeyEvent.VK_I, KeyEvent.VK_C),
            new ActionBinding("EditorDown", KeyEvent.VK_K, KeyEvent.VK_T),
            new ActionBinding("EditorPreviousWord", KeyEvent.VK_U, KeyEvent.VK_G),
            new ActionBinding("EditorNextWord", KeyEvent.VK_O, KeyEvent.VK_R),
            new ActionBinding("EditorLineStart", KeyEvent.VK_H, KeyEvent.VK_D),
            new ActionBinding("EditorLineEnd", KeyEvent.VK_SEMICOLON, KeyEvent.VK_S),
            new ActionBinding("NextSplitter", KeyEvent.VK_COMMA, KeyEvent.VK_W),
            new ActionBinding("Find", KeyEvent.VK_N, KeyEvent.VK_B),

            new ActionBinding("UnsplitAll", KeyEvent.VK_3, KeyEvent.VK_3),
            new ActionBinding("SplitVertically", KeyEvent.VK_4, KeyEvent.VK_4),
            new ActionBinding("$Delete", KeyEvent.VK_5, KeyEvent.VK_5),
            new ActionBinding("EditorSelectLine", KeyEvent.VK_7, KeyEvent.VK_7),
            new ActionBinding("EditorSelectWord", KeyEvent.VK_8, KeyEvent.VK_8),
            new ActionBinding("ErgoKeysSelectString", KeyEvent.VK_9, KeyEvent.VK_9),

            // Tab shortcuts
            new ActionBinding("EditorTab", KeyEvent.VK_TAB, KeyEvent.VK_TAB),
            new ActionBinding("NextTemplateVariable", KeyEvent.VK_TAB, KeyEvent.VK_TAB),
            new ActionBinding("ExpandLiveTemplateByTab", KeyEvent.VK_TAB, KeyEvent.VK_TAB),
            new ActionBinding("NextParameter", KeyEvent.VK_TAB, KeyEvent.VK_TAB),
            new ActionBinding("BraceOrQuoteOut", KeyEvent.VK_TAB, KeyEvent.VK_TAB),
            new ActionBinding("EditorIndentSelection", KeyEvent.VK_TAB, KeyEvent.VK_TAB),
            new ActionBinding("EditorChooseLookupItemReplace", KeyEvent.VK_TAB, KeyEvent.VK_TAB),
            new ActionBinding("EditorChooseLookupItem", KeyEvent.VK_TAB, KeyEvent.VK_TAB),

            // Enter shortcuts
            new ActionBinding("EditorEnter", KeyEvent.VK_ENTER, KeyEvent.VK_ENTER),
            new ActionBinding("EditorChooseLookupItem", KeyEvent.VK_ENTER, KeyEvent.VK_ENTER),

            // Misc. shortcuts
            new ActionBinding("EditorEscape", KeyEvent.VK_ESCAPE, KeyEvent.VK_ESCAPE),
            new ActionBinding("EditorBackSpace", KeyEvent.VK_BACK_SPACE, KeyEvent.VK_BACK_SPACE),

            // Unused keys
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_BACK_QUOTE),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_1, KeyEvent.VK_1),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_2, KeyEvent.VK_2),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_6, KeyEvent.VK_6),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_0, KeyEvent.VK_0),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_MINUS, KeyEvent.VK_OPEN_BRACKET),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_EQUALS, KeyEvent.VK_CLOSE_BRACKET),

            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_W, KeyEvent.VK_COMMA),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_P, KeyEvent.VK_L),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_SLASH),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_EQUALS),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_BACK_SLASH, KeyEvent.VK_BACK_SLASH),

            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_G, KeyEvent.VK_I),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_QUOTE, KeyEvent.VK_MINUS),

            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_B, KeyEvent.VK_X),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_M, KeyEvent.VK_M),
            new ActionBinding("ErgoKeysNoopAction", KeyEvent.VK_PERIOD, KeyEvent.VK_V),

            // Navigation
            new ActionBinding("GotoDeclaration", KeyEvent.VK_SLASH, KeyEvent.VK_Z),
    };
    private Keymap userKeymap;
    private Keymap commandModeKeymap;
    private boolean shouldActivateCommandModeOnFocusGain;

    public ErgoKeysPlugin() {
        settings = ErgoKeysSettings.getInstance();
        keymapManagerEx = KeymapManagerEx.getInstanceEx();
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ErgoKeysPlugin";
    }

    @Override
    public void initComponent() {
        LOG.debug("initComponent");

        ActionManager.getInstance().registerAction("ErgoKeysNoopAction", new AnAction() {
            @Override
            public void actionPerformed(AnActionEvent e) {
                // noop
            }
        });

        userKeymap = keymapManagerEx.getActiveKeymap();
        updateCommandModeKeymap();

        ApplicationManager.getApplication().getMessageBus().connect().subscribe(KeymapManagerListener.TOPIC, new KeymapManagerListener() {
            @Override
            public void activeKeymapChanged(@Nullable Keymap keymap) {
                LOG.debug("activeKeymapChanged:");

                if (keymap == null || keymap.equals(commandModeKeymap)) {
                    return;
                }
                userKeymap = keymap;
            }
        });


        EditorFactory.getInstance().addEditorFactoryListener(
                new EditorFactoryListener() {
                    @Override
                    public void editorCreated(@NotNull EditorFactoryEvent event) {
                        Editor editor = event.getEditor();
                        editor.getContentComponent().addFocusListener(new FocusListener() {
                            @Override
                            public void focusGained(FocusEvent focusEvent) {
                                LOG.debug("editor: " + editor.toString() + " gained focus");

                                if (inCommandMode()) {
                                    editor.getSettings().setBlockCursor(true);
                                } else {
                                    editor.getSettings().setBlockCursor(false);
                                }

                                if (focusEvent.getOppositeComponent() instanceof EditorComponentImpl) {
                                    return;
                                }

                                if (shouldActivateCommandModeOnFocusGain) {
                                    activateCommandMode(editor);
                                    shouldActivateCommandModeOnFocusGain = false;
                                }
                            }

                            @Override
                            public void focusLost(FocusEvent focusEvent) {
                                LOG.debug("editor: " + editor.toString() + " lost focus");

                                if (focusEvent.getOppositeComponent() instanceof EditorComponentImpl) {
                                    shouldActivateCommandModeOnFocusGain = false;
                                    return;
                                }

                                if (inCommandMode()) {
                                    deactivateCommandMode(editor);
                                    shouldActivateCommandModeOnFocusGain = true;
                                }

                            }
                        });
                    }

                    public void editorReleased(@NotNull EditorFactoryEvent event) {
                    }
                },
                new Disposable() {
                    @Override
                    public void dispose() {

                    }
                }
        );
    }

    private void updateCommandModeKeymap() {
        commandModeKeymap = userKeymap.deriveKeymap("CommandModeKeymap");
        for (String actionId : commandModeKeymap.getActionIdList()) {
            if (actionId.equals(ACTION_ID_ERGO_KEYS_COMMAND_MODE)) {
                continue;
            }
            commandModeKeymap.removeAllActionShortcuts(actionId);
        }

        String keyboardLayout = settings.getKeyboardLayout();
        switch (keyboardLayout) {
            case "dvorak":
                for (ActionBinding binding : bindings) {
                    commandModeKeymap.addShortcut(binding.actionId,
                            new KeyboardShortcut(KeyStroke.getKeyStroke(binding.dvorakKeyCode, 0), null));
                }
                break;
            case "qwerty":
                for (ActionBinding binding : bindings) {
                    commandModeKeymap.addShortcut(binding.actionId,
                            new KeyboardShortcut(KeyStroke.getKeyStroke(binding.qwertyKeyCode, 0), null));
                }
                break;
            default:
                throw new RuntimeException(String.format("unsupported keyboard layout: %s", keyboardLayout));
        }
    }

    public void applySettings() {
        updateCommandModeKeymap();
    }

    public void activateCommandMode(Editor editor, boolean fromAction) {
        if (fromAction && inCommandMode() && settings.isCommandModeToggle()) {
            deactivateCommandMode(editor);
            return;
        }
        activateCommandMode(editor);
    }

    private boolean inCommandMode() {
        return keymapManagerEx.getActiveKeymap().equals(commandModeKeymap);
    }


    public void deactivateCommandMode(Editor editor) {
        editor.getSettings().setBlockCursor(false);
        KeymapManagerEx.getInstanceEx().setActiveKeymap(userKeymap);
    }

    // print all shortcuts for the current active keymap
    private void debugActiveShortcuts() {
        KeymapManager keymapManager = KeymapManager.getInstance();
        Keymap activeKeymap = keymapManager.getActiveKeymap();
        for (String actionId : activeKeymap.getActionIdList()) {
            StringBuilder b = new StringBuilder();
            b.append('\t');
            b.append(actionId);
            b.append(" { ");
            for (Shortcut shortcut : activeKeymap.getShortcuts(actionId)) {
                b.append(shortcut.toString());
                b.append(", ");
            }
            b.append(" }");
            LOG.debug(b.toString());
        }
    }

    private void activateCommandMode(Editor editor) {
        // debugActiveShortcuts();

        editor.getSettings().setBlockCursor(true);
        KeymapManagerEx.getInstanceEx().setActiveKeymap(commandModeKeymap);
    }

    class ActionBinding {
        private final String actionId;
        private final int qwertyKeyCode;
        private final int dvorakKeyCode;

        ActionBinding(String actionId, int qwertyKeyCode, int dvorakKeyCode) {
            this.actionId = actionId;
            this.qwertyKeyCode = qwertyKeyCode;
            this.dvorakKeyCode = dvorakKeyCode;
        }
    }
}
