/**
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.livespark.demo.client.local;

import static org.jboss.errai.common.client.dom.Window.getDocument;
import static org.livespark.demo.client.local.Constants.CONTACT_FORM;
import static org.livespark.demo.client.local.Constants.CONTACT_LIST;
import static org.livespark.demo.client.local.Constants.CONTACT_LOADER;
import static org.livespark.demo.client.local.Constants.CONTACT_SAVER;
import static org.livespark.demo.client.local.Constants.CONTACT_UPDATER;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.jboss.errai.common.client.dom.Body;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.nav.client.local.NavigationPanel;
import org.livespark.demo.client.shared.Contact;
import org.livespark.flow.api.AppFlow;
import org.livespark.flow.api.AppFlowFactory;
import org.livespark.flow.api.Command;
import org.livespark.flow.api.CrudOperation;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Unit;
import org.slf4j.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * <p>
 * This bean attaches the {@link NavBar} and {@link NavigationPanel} when the application starts.
 *
 * <p>
 * The {@link EntryPoint} scope is like {@link ApplicationScoped} except that entry points are eagerly initilialized
 * when the IoC container starts. Consequently, the {@link PostConstruct} of this bean will be invoked when the
 * container is initialized.
 */
@EntryPoint
public class AppSetup {

    @Inject
    private NavigationPanel                                      navPanel;

    @Inject
    private NavBar                                               navbar;

    @Inject
    private AppFlowFactory                                       factory;

    @Inject
    @Named( CONTACT_FORM )
    private Step<Contact, Optional<Contact>>                     form;

    @Inject
    @Named( CONTACT_LIST )
    private Step<List<Contact>, Command<CrudOperation, Contact>> list;

    @Inject
    @Named( CONTACT_LOADER )
    private Step<Unit, List<Contact>>                            loader;

    @Inject
    @Named( CONTACT_SAVER )
    private Step<Contact, Unit>                                  saver;

    @Inject
    @Named( CONTACT_UPDATER )
    private Step<Contact, Unit>                                  updater;

    @Inject
    private Logger logger;

    @PostConstruct
    public void init() {
        initUncaughtExceptionHandler();
        initPageBody();
        initProcesses();
    }

    private void initUncaughtExceptionHandler() {
        GWT.setUncaughtExceptionHandler( new UncaughtExceptionHandler() {

            @Override
            public void onUncaughtException( final Throwable t ) {
                logger.error( "Uncaught exception.", t );
            }
        } );
    }

    private void initPageBody() {
        RootPanel.get( "rootPanel" ).add( navPanel );
        final Body body = getDocument().getBody();
        body.insertBefore( navbar.getElement(),
                           DOMUtil.getFirstChildElement( body ).orElse( null ) );
    }

    private void initProcesses() {
        final String MAIN = "Main";
        final Supplier<AppFlow<Unit, Unit>> mainSupplier = () -> factory.getFlow( MAIN );

        final AppFlow<Unit, Unit> mainProcess = factory.buildFromStep( loader ).andThen( list ).transitionTo( command -> {
            final Step<Contact, Unit> op;
            switch ( command.commandType ) {
                case CREATE :
                    op = saver;
                    break;
                case UPDATE :
                    op = updater;
                    break;
                default :
                    throw new RuntimeException();
            }

            return factory
                    .buildFromStep( form )
                    .butFirst( ( final Unit u ) -> command.value )
                    .transitionTo(
                                  res ->
                                      res
                                      .map(
                                           ( final Contact createdOrUpdated ) ->
                                           factory
                                           .buildFromStep( op )
                                           .butFirst( ( final Unit unit ) -> createdOrUpdated )
                                           .andThen( mainSupplier ) )
                                      .orElseGet( mainSupplier ) );
        } );

        factory.registerFlow( MAIN,
                                 mainProcess );
    }

    @Produces
    @Singleton
    @Named( "Main" )
    public AppFlow<Unit, Unit> getFlow() {
        return factory.getFlow( "Main" );
    }

}
