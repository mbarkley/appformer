/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.livespark.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.m2repo.client.event.M2RepoRefreshEvent;
import org.guvnor.m2repo.client.event.M2RepoSearchEvent;
import org.guvnor.m2repo.client.upload.UploadFormView;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.kie.workbench.common.widgets.client.search.ContextualSearch;
import org.kie.workbench.common.widgets.client.search.SearchBehavior;
import org.livespark.client.resources.i18n.AppConstants;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPanel;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.util.Layouts;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.Command;
import org.uberfire.security.annotations.Roles;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A Perspective to show M2_REPO related screen
 */
@Roles({ "admin" })
@Dependent
@WorkbenchPerspective(identifier = "org.guvnor.m2repo.client.perspectives.GuvnorM2RepoPerspective")
public class M2RepoPerspective extends FlowPanel {

    @Inject
    private ContextualSearch contextualSearch;

    @Inject
    private Event<M2RepoSearchEvent> searchEvents;

    @Inject
    private Event<M2RepoRefreshEvent> refreshEvents;

    @Inject
    private SyncBeanManager iocManager;

    @WorkbenchPanel(parts = "M2RepoEditor")
    SimplePanel m2RepoEditor = new SimplePanel();

    @PostConstruct
    private void init() {
        Layouts.setToFillParent( m2RepoEditor );
        add( m2RepoEditor );
    }

    @WorkbenchMenu
    public Menus getMenus() {
        return MenuFactory.newTopLevelMenu( AppConstants.INSTANCE.Upload() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        final UploadFormView uploadForm = iocManager.lookupBean( UploadFormView.class ).getInstance();
                        //When pop-up is closed destroy bean to avoid memory leak
                        uploadForm.asWidget().addHandler( new CloseHandler<PopupPanel>() {

                            @Override
                            public void onClose( CloseEvent<PopupPanel> event ) {
                                iocManager.destroyBean( uploadForm );
                            }

                        }, CloseEvent.getType() );
                        uploadForm.show();
                    }
                } )
                .endMenu()
                .newTopLevelMenu( AppConstants.INSTANCE.Refresh() )
                .respondsWith( new Command() {
                    @Override
                    public void execute() {
                        refreshEvents.fire( new M2RepoRefreshEvent() );
                    }
                } )
                .endMenu()
                .build();
    }

    @OnStartup
    public void onStartup() {
        contextualSearch.setDefaultSearchBehavior( new SearchBehavior() {
            @Override
            public void execute( String searchFilter ) {
                searchEvents.fire( new M2RepoSearchEvent( searchFilter ) );
            }

        } );
    }
}
