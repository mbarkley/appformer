/*
 * Copyright 2015 JBoss Inc
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
package org.livespark.formmodeler.editor.client.editor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.guvnor.common.services.shared.metadata.model.Metadata;
import org.guvnor.common.services.shared.metadata.model.Overview;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.widgets.metadata.client.KieEditorWrapperView;
import org.kie.workbench.common.widgets.metadata.client.widget.OverviewWidgetPresenter;
import org.livespark.formmodeler.editor.client.editor.events.FieldDroppedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FieldRemovedEvent;
import org.livespark.formmodeler.editor.client.editor.events.FormContextResponse;
import org.livespark.formmodeler.editor.client.editor.mock.MockFieldManager;
import org.livespark.formmodeler.editor.client.editor.rendering.DraggableFieldComponent;
import org.livespark.formmodeler.editor.client.resources.images.FormEditorImageResources;
import org.livespark.formmodeler.editor.client.type.FormDefinitionResourceType;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.model.FormDefinition;
import org.livespark.formmodeler.editor.model.FormModelerContent;
import org.livespark.formmodeler.editor.model.impl.basic.CheckBoxFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.DateBoxFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.TextAreaFieldDefinition;
import org.livespark.formmodeler.editor.model.impl.basic.TextBoxFieldDefinition;
import org.livespark.formmodeler.editor.service.FormEditorService;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.editor.commons.client.history.VersionRecordManager;
import org.uberfire.ext.layout.editor.client.LayoutEditor;
import org.uberfire.ext.plugin.client.perspective.editor.layout.editor.HTMLLayoutDragComponent;
import org.uberfire.ext.widgets.common.client.common.BusyIndicatorView;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.mvp.PlaceRequest;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;

import junit.framework.TestCase;

@RunWith(GwtMockitoTestRunner.class)
public class FormEditorPresenterTest extends TestCase {
    public static final String EMPLOYEE_TYPE = "org.livespark.test.Employee";
    public static final String EMPLOYEE_NAME = "employee";

    public static final String ADDRESS_TYPE = "org.livespark.test.Address";
    public static final String ADDRESS_NAME = "address";

    public static final String DEPARTMENT_TYPE = "org.livespark.test.Department";
    public static final String DEPARTMENT_NAME = "department";

    private List<FieldDefinition> employeeFields;
    private List<FieldDefinition> addressFields;
    private List<FieldDefinition> departmentFields;

    private FormEditorHelper editorContext;

    @GwtMock
    private FormEditorImageResources formEditorImageResources;

    @Mock
    VersionRecordManager versionRecordManager;

    @Mock
    private FormEditorPresenter.FormEditorView view;

    @Mock
    private FormEditorPresenter.DataHolderAdminView objectsView;

    @GwtMock
    private KieEditorWrapperView kieView;

    @GwtMock
    private ObservablePath path;

    @GwtMock
    private FormDefinitionResourceType formDefinitionResourceType;

    @Mock
    private LayoutEditor layoutEditor;

    @Mock
    private HTMLLayoutDragComponent htmlLayoutDragComponent;

    @Mock
    private SyncBeanManager beanManager;

    @Mock
    private SyncBeanDef<DraggableFieldComponent> draggableFieldDef;

    @Mock
    protected EventSourceMock<FormContextResponse> eventMock;

    private FormEditorPresenter presenter;
    private FormModelerContent content;


    @Override
    @Before
    public void setUp() throws Exception {
        initFields();
    }

    protected void loadContent() {
        when( formDefinitionResourceType.getSuffix() ).thenReturn("form.frm");
        when( formDefinitionResourceType.accept(path) ).thenReturn(true);
//        when( formDefinitionResourceType.accept(path) ).thenReturn(false);

        when( beanManager.lookupBean(eq(DraggableFieldComponent.class)) ).thenReturn( draggableFieldDef );

        when( draggableFieldDef.newInstance() ).thenAnswer(new Answer<DraggableFieldComponent>() {
            @Override
            public DraggableFieldComponent answer(InvocationOnMock invocationOnMock) throws Throwable {
                final DraggableFieldComponent mocked = mock(DraggableFieldComponent.class);
                return mocked;
            }
        });

        editorContext = new FormEditorHelper( new MockFieldManager(), eventMock, new ServiceMock(), beanManager);

        presenter = new FormEditorPresenter( view, objectsView, formDefinitionResourceType, new ServiceMock(), beanManager ) {
            {
                kieView = mock( KieEditorWrapperView.class );
                versionRecordManager = FormEditorPresenterTest.this.versionRecordManager;
                editorContext = FormEditorPresenterTest.this.editorContext;
                busyIndicatorView = mock( BusyIndicatorView.class );
                overviewWidget = mock( OverviewWidgetPresenter.class );
                layoutEditor = FormEditorPresenterTest.this.layoutEditor;
                htmlLayoutDragComponent = FormEditorPresenterTest.this.htmlLayoutDragComponent;
            }

            @Override
            protected void makeMenuBar() {
            }

            @Override
            protected void addSourcePage() {
            }
        };
        presenter.onStartup(path,
                mock(PlaceRequest.class));

        assertTrue("There should be at least 4 base field types", editorContext.getBaseFields().size() == 4);
        assertTrue("There should be at least 4 base field draggables", editorContext.getBaseFieldsDraggables().size() == 4);
    }

    @Test
    public void testLoad() throws Exception {
        loadContent();

        verify( view ).init(presenter);
        verify( objectsView ).init(presenter);
        verify( view ).setupLayoutEditor(layoutEditor);
    }


    @Test
    public void testDataObjectsFields() {
        loadContent();

        testAddRemoveDataTypes();

        testAddRemoveDataTypeFields();

        testDataTypeFieldProperties();
    }

    @Test
    public void testUnbindedFields() {
        loadContent();

        testAddRemoveUnbindedFields();

        testUnbindedFieldProperties();
    }

    protected void testAddRemoveDataTypes() {
        verify(objectsView, never()).addDataType(anyString());

        presenter.initDataObjectsTab();

        verify(objectsView).initView();
        verify(objectsView).addDataType(EMPLOYEE_TYPE);
        verify(objectsView).addDataType(ADDRESS_TYPE);
        verify(objectsView).addDataType(DEPARTMENT_TYPE);

        presenter.addDataHolder(EMPLOYEE_NAME, EMPLOYEE_TYPE);
        presenter.addDataHolder(ADDRESS_NAME, ADDRESS_TYPE);
        presenter.addDataHolder(DEPARTMENT_NAME, DEPARTMENT_TYPE);

        int expectedFields = employeeFields.size() + addressFields.size() + departmentFields.size();
        checkRemoveDataHolder(3, expectedFields);

        presenter.removeDataHolder(ADDRESS_NAME);
        expectedFields = expectedFields - addressFields.size();
        checkRemoveDataHolder(2, expectedFields);

        presenter.removeDataHolder(DEPARTMENT_NAME);
        expectedFields = expectedFields - departmentFields.size();
        checkRemoveDataHolder(1, expectedFields);

        verify(objectsView, times(2)).refreshView();
    }

    protected void checkRemoveDataHolder( int expectedHolders, int expectedFields ) {
        assertTrue( "Form Definition must have " + expectedHolders + " DataHolders", content.getDefinition().getDataHolders().size() == expectedHolders );
        assertTrue( "There should be " + expectedFields + " available fields", editorContext.getAvailableFields().size() == expectedFields);
    }

    public void testAddRemoveDataTypeFields() {
        testAddRemoveFields(employeeFields, editorContext.getAvailableFields().size(), true);
    }

    public void testAddRemoveUnbindedFields() {
        testAddRemoveFields(editorContext.getBaseFields(), 0, false);
    }

    protected void testAddRemoveFields (List<FieldDefinition> fields, int availableFields, boolean checkAvailable ) {
        int formFields = 0;

        for ( FieldDefinition field : fields ) {
            presenter.onFieldDropped(new FieldDroppedEvent(editorContext.getFormDefinition().getId(), field.getId()));
            availableFields --;
            formFields ++;
            checkExpectedFields(availableFields, formFields, checkAvailable);
        }

        List<FieldDefinition> formFieldsList = new ArrayList<FieldDefinition>( editorContext.getFormDefinition().getFields() );

        for ( FieldDefinition field : formFieldsList ) {
            presenter.onFieldRemoved(new FieldRemovedEvent(editorContext.getFormDefinition().getId(), field.getId()));
            availableFields ++;
            formFields --;
            checkExpectedFields(availableFields, formFields, checkAvailable);
        }
    }

    public void testDataTypeFieldProperties() {
        testFieldProperties( "name", true );
    }

    public void testUnbindedFieldProperties() {

        presenter.addDataHolder(EMPLOYEE_NAME, EMPLOYEE_TYPE);

        testFieldProperties( FormEditorHelper.UNBINDED + TextBoxFieldDefinition._CODE, false);
    }

    protected void testFieldProperties( String fieldId, boolean binded) {
        presenter.onFieldDropped(new FieldDroppedEvent(editorContext.getFormDefinition().getId(), fieldId));
        checkExpectedFields(editorContext.getAvailableFields().size(), 1, binded);

        FieldDefinition field = editorContext.getFormDefinition().getFields().get(0);

        checkFieldType(field, TextBoxFieldDefinition.class);

        List<String> compatibleTypes = editorContext.getCompatibleFieldTypes(field);

        assertNotNull("No compatibles types found!", compatibleTypes);
        assertEquals("There should exist 2 compatible types for TextBoxFieldDefinition!", compatibleTypes.size(), 2);
        assertTrue("Missing TextAreaFieldDefinition as a compatible type for TextBoxFieldDefinition", compatibleTypes.contains(TextAreaFieldDefinition._CODE));

        field = editorContext.switchToFieldType( field, TextAreaFieldDefinition._CODE);
        checkFieldType(field, TextAreaFieldDefinition.class);

        List<String> compatibleFields = editorContext.getCompatibleFields(field);

        assertNotNull( "No compatibles fields found!", compatibleFields);

        assertEquals("There should exist 2 compatible fields for " + field.getName() + "!", compatibleFields.size(), 2);

        String expectedBindingExpression = "employee.lastName";

        field = editorContext.switchToField( field, expectedBindingExpression);

        assertEquals("Wrong binding expression after switch field!", field.getBindingExpression(), expectedBindingExpression );

        if ( binded ) {
            assertNotNull("Missing field name", editorContext.getAvailableFields().get(fieldId));
        }

        presenter.onFieldRemoved( new FieldRemovedEvent( editorContext.getFormDefinition().getId(), "lastName" ));
    }

    protected void checkFieldType( FieldDefinition field, Class<? extends FieldDefinition> type ) {
        assertTrue( "Field " + field.getName() + " should be of type " + type.getClass().getName(), field.getClass() == type );
    }

    protected void checkExpectedFields( int expectedAvailable, int expectedFormFields, boolean checkAvailable ) {
        if (checkAvailable) assertEquals("There should be " + expectedAvailable + " available fields", editorContext.getAvailableFields().size(), expectedAvailable);
        assertEquals("The form must contain " + expectedFormFields + " fields ", editorContext.getFormDefinition().getFields().size(), expectedFormFields);
    }

    private class ServiceMock
            implements Caller<FormEditorService> {

        private FormEditorService service = new FormEditorServiceMock();

        RemoteCallback remoteCallback;

        @Override
        public FormEditorService call() {
            return service;
        }

        @Override
        public FormEditorService call( RemoteCallback<?> remoteCallback ) {
            return call( remoteCallback, null );
        }

        @Override
        public FormEditorService call( RemoteCallback<?> remoteCallback, ErrorCallback<?> errorCallback ) {
            this.remoteCallback = remoteCallback;
            return call();
        }

        private class FormEditorServiceMock implements FormEditorService {

            @Override
            public Path createForm(Path path, String formName) {
                return null;
            }

            @Override
            public FormModelerContent loadContent(Path path) {
                FormDefinition form = new FormDefinition();
                form.setName( "EmployeeTestForm" );
                form.setId("_random_id");

                content = new FormModelerContent( );

                content.setDefinition( form );
                content.setOverview(new Overview());
                content.setPath(path);
                content.setAvailableFields( new HashMap<String, List<FieldDefinition>>());
                remoteCallback.callback( content );

                return content;
            }

            @Override
            public List<String> getAvailableDataObjects(Path path) {
                List<String> dataObjects = new ArrayList<String>();

                dataObjects.add( EMPLOYEE_TYPE );
                dataObjects.add( ADDRESS_TYPE );
                dataObjects.add( DEPARTMENT_TYPE );

                remoteCallback.callback( dataObjects );

                return dataObjects;
            }

            @Override
            public List<FieldDefinition> getAvailableFieldsForType(Path path, String holderName, String type) {

                List<FieldDefinition> result = null;

                if ( type.equals( EMPLOYEE_TYPE ) ) {
                    result = employeeFields;
                } else if ( type.equals( ADDRESS_TYPE) ) {
                    result = addressFields;
                } else {
                    result = departmentFields;
                }

                remoteCallback.callback( result );
                return result;
            }

            @Override
            public FieldDefinition resetField(FormDefinition definition, FieldDefinition field, Path path) {
                remoteCallback.callback( field );
                return field;
            }


            // Not implemented yet
            @Override
            public void delete(Path path, String comment) {

            }

            @Override
            public Path rename(Path path, String newName, String comment) {
                return null;
            }

            @Override
            public Path save(Path path, FormModelerContent content, Metadata metadata, String comment) {
                return null;
            }
        }
    }

    protected void initFields() {
        TextBoxFieldDefinition name = new TextBoxFieldDefinition();
        name.setId("name");
        name.setName("employee_name");
        name.setLabel("Name");
        name.setPlaceHolder("Name");
        name.setModelName("employee");
        name.setBoundPropertyName("name");
        name.setStandaloneClassName(String.class.getName());

        TextBoxFieldDefinition lastName = new TextBoxFieldDefinition();
        lastName.setId("lastName");
        lastName.setName("employee_lastName");
        lastName.setLabel("Last Name");
        lastName.setPlaceHolder("Last Name");
        lastName.setModelName("employee");
        lastName.setBoundPropertyName("lastName");
        lastName.setStandaloneClassName(String.class.getName());

        DateBoxFieldDefinition birthday = new DateBoxFieldDefinition();
        birthday.setId("birthday");
        birthday.setName("employee_birthday");
        birthday.setLabel("Birthday");
        birthday.setModelName("employee");
        birthday.setBoundPropertyName("birthday");
        birthday.setStandaloneClassName(Date.class.getName());

        CheckBoxFieldDefinition married = new CheckBoxFieldDefinition();
        married.setId("married");
        married.setName("employee_married");
        married.setLabel("Married=");
        married.setModelName("employee");
        married.setBoundPropertyName("married");
        married.setStandaloneClassName(Boolean.class.getName());

        employeeFields = new ArrayList<FieldDefinition>();
        employeeFields.add( name );
        employeeFields.add( lastName );
        employeeFields.add( birthday );
        employeeFields.add( married );

        TextBoxFieldDefinition streetName = new TextBoxFieldDefinition();
        streetName.setId("streetName");
        streetName.setName("address_street");
        streetName.setLabel("Street Name");
        streetName.setPlaceHolder("Street Name");
        streetName.setModelName("address");
        streetName.setBoundPropertyName("street");
        streetName.setStandaloneClassName(String.class.getName());

        TextBoxFieldDefinition num = new TextBoxFieldDefinition();
        num.setId("num");
        num.setName("address_num");
        num.setLabel("#");
        num.setPlaceHolder("#");
        num.setModelName("address");
        num.setBoundPropertyName("num");
        num.setStandaloneClassName(Integer.class.getName());

        addressFields = new ArrayList<FieldDefinition>();
        addressFields.add(streetName);
        addressFields.add(num);

        TextBoxFieldDefinition depName = new TextBoxFieldDefinition();
        depName.setId("depName");
        depName.setName("department_name");
        depName.setLabel("Department Name");
        depName.setPlaceHolder("Department Name");
        depName.setModelName("department");
        depName.setBoundPropertyName("name");
        depName.setStandaloneClassName(String.class.getName());

        TextBoxFieldDefinition phone = new TextBoxFieldDefinition();
        phone.setId("phone");
        phone.setName("department_phone");
        phone.setLabel("Phone number");
        phone.setPlaceHolder("Phone number");
        phone.setModelName("department");
        phone.setBoundPropertyName("phone");
        phone.setStandaloneClassName(String.class.getName());

        departmentFields = new ArrayList<FieldDefinition>();
        departmentFields.add( depName );
        departmentFields.add( phone );
    }
}
