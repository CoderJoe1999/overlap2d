/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.uwsoft.editor.view.ui.dialog;

import com.badlogic.gdx.Gdx;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import com.uwsoft.editor.view.stage.Sandbox;
import com.uwsoft.editor.view.ui.widget.ProgressHandler;
import com.uwsoft.editor.Overlap2DFacade;
import com.uwsoft.editor.proxy.ProjectManager;
import com.uwsoft.editor.view.Overlap2DMenuBar;
import com.uwsoft.editor.view.stage.UIStage;
import com.uwsoft.editor.renderer.data.SceneVO;

/**
 * Created by sargis on 4/3/15.
 */
public class AssetsImportDialogMediator extends SimpleMediator<AssetsImportDialog> {
    private static final String TAG = AssetsImportDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;
    private AssetsImportProgressHandler progressHandler;

    public AssetsImportDialogMediator() {
        super(NAME, new AssetsImportDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = Overlap2DFacade.getInstance();
        progressHandler = new AssetsImportProgressHandler();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                Overlap2DMenuBar.IMPORT_TO_LIBRARY,
                AssetsImportDialog.START_IMPORTING_BTN_CLICKED
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case Overlap2DMenuBar.IMPORT_TO_LIBRARY:
                viewComponent.show(uiStage);
                break;
            case AssetsImportDialog.START_IMPORTING_BTN_CLICKED:
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                projectManager.importImagesIntoProject(viewComponent.getImageFiles(), progressHandler);
                projectManager.importParticlesIntoProject(viewComponent.getParticleEffectFiles(), progressHandler);
                projectManager.importStyleIntoProject(viewComponent.getStyleFiles(), progressHandler);
                projectManager.importFontIntoProject(viewComponent.getFontFiles(), progressHandler);
                projectManager.importSpineAnimationsIntoProject(viewComponent.getSpineSpriterFiles(), progressHandler);
                projectManager.importSpriteAnimationsIntoProject(viewComponent.getSpriteAnimationFiles(), progressHandler);
                // save before importing
                SceneVO vo = sandbox.sceneVoFromItems();
//                uiStage.getCompositePanel().updateOriginalItem();
                projectManager.saveCurrentProject(vo);
                break;
        }
    }

    private class AssetsImportProgressHandler implements ProgressHandler {

        @Override
        public void progressStarted() {

        }

        @Override
        public void progressChanged(float value) {

        }

        @Override
        public void progressComplete() {
            Gdx.app.postRunnable(() -> {
                Sandbox sandbox = Sandbox.getInstance();
                UIStage uiStage = sandbox.getUIStage();
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                projectManager.openProjectAndLoadAllData(projectManager.getCurrentProjectVO().projectName);
                sandbox.loadCurrentProject();
                AssetsImportDialogMediator.this.viewComponent.hide();
                facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
            });
        }
    }
}