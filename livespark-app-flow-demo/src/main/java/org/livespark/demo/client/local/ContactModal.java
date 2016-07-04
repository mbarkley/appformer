/*
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

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Form;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.SinkNative;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.livespark.demo.client.shared.Contact;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.TakesValue;

@Templated( "contact-page.html#modal" )
public class ContactModal implements TakesValue<Contact> {

    @Inject
    @DataField
    private Form   modal;

    @Inject
    @DataField( "modal-fields" )
    private Div    editorDiv;

    @Inject
    @DataField( "modal-delete" )
    private Button delete;

    private Optional<ContactEditor> currentEditor = Optional.empty();

    @PostConstruct
    private void init() {
        delete.getStyle().setProperty( "display", "none" );
    }

    public void handleDisplay( @Observes @Named( "ContactModal" ) final Optional<ContactEditor> display) {
        if ( display.isPresent() ) {
            DOMUtil.addCSSClass( modal, "displayed" );
            display.ifPresent( editor -> {
                DOMUtil.removeAllChildren( editorDiv );
                editorDiv.appendChild( editor.getElement() );
                currentEditor = Optional.of( editor );
                editor.init();
            } );
        } else {
            DOMUtil.removeCSSClass( modal, "displayed" );
            DOMUtil.removeAllChildren( editorDiv );
        }
    }

    /**
     * This is an Errai UI native event handler. The element for which this handler is regsitered is in this class's HTML
     * template file and has the {@code modal-submit} CSS class.
     * <p>
     * Because there is no {@code modal-submit} {@link DataField} in this class, this method's parameter is a non-specific
     * {@link Event} (rather than a more specific {@link ClickEvent}). For the same reason, the {@link SinkNative}
     * annotation is required to specify which kinds of DOM events this method should handle.
     * <p>
     * This method displays and persists changes made to a {@link Contact} in the {@link ContactEditor}, whether it is a
     * newly created or an previously existing {@link Contact}.
     */
    @SinkNative( Event.ONCLICK )
    @EventHandler( "modal-submit" )
    public void onModalSubmitClick( final Event event ) {
        if ( modal.checkValidity() ) {
            currentEditor.ifPresent( editor -> {
                editor.submit( false );
            } );
        }
    }

    /**
     * This is an Errai UI native event handler. The element for which this handler is regsitered is in this class's HTML
     * template file and has the {@code modal-cancel} CSS class.
     * <p>
     * Because there is no {@code modal-cancel} {@link DataField} in this class, this method's parameter is a non-specific
     * {@link Event} (rather than a more specific {@link ClickEvent}). For the same reason, the {@link SinkNative}
     * annotation is required to specify which kinds of DOM events this method should handle.
     * <p>
     * This method hides the ContactEditor modal form and resets the bound model.
     */
    @SinkNative( Event.ONCLICK )
    @EventHandler( "modal-cancel" )
    public void onModalCancelClick( final Event event ) {
        currentEditor.ifPresent( editor -> editor.submit( true ) );
    }

    @Override
    public void setValue( final Contact value ) {
        currentEditor.ifPresent( editor -> editor.setValue( value ) );
    }

    @Override
    public Contact getValue() {
        return currentEditor
                .map( editor -> editor.getValue() )
                .orElse( null );
    }

    public void syncStateFromUI() {
        currentEditor.ifPresent( editor -> editor.syncStateFromUI() );
    }

    public void display() {
        DOMUtil.addCSSClass( modal, "displayed" );
    }

}
