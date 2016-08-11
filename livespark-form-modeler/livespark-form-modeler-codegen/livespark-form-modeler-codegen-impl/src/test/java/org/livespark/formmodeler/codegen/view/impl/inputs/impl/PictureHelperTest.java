/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.livespark.formmodeler.codegen.view.impl.inputs.impl;

import java.util.Arrays;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.runner.RunWith;
import org.kie.workbench.common.forms.model.FieldDefinition;
import org.kie.workbench.common.forms.model.impl.basic.image.PictureFieldDefinition;
import org.kie.workbench.common.forms.model.impl.basic.image.PictureSize;
import org.livespark.formmodeler.codegen.view.impl.inputs.AbstractInputHelperTest;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.InputCreatorHelper;
import org.livespark.formmodeler.codegen.view.impl.java.inputs.impl.PictureHelper;
import org.mockito.runners.MockitoJUnitRunner;

import static org.livespark.formmodeler.codegen.util.SourceGenerationUtil.*;

@RunWith( MockitoJUnitRunner.class )
public class PictureHelperTest extends AbstractInputHelperTest {

    @Override
    protected void runFieldTests( FieldDefinition field, InputCreatorHelper helper ) {
        super.runFieldTests( field, helper );

        MethodSource<JavaClassSource> beforeDisplayMethod = classSource.getMethod( BEFORE_DISPLAY_METHOD );

        assertNotNull( "Class must have a '" + BEFORE_DISPLAY_METHOD + "'", beforeDisplayMethod );

        PictureHelper pictureHelper = (PictureHelper) helper;

        String methodBody = beforeDisplayMethod.getBody();

        String initialization = removeEmptySpaces( pictureHelper.getWidgetInitialization( (PictureFieldDefinition) field ) );

        assertTrue( methodBody.contains( initialization ) );
    }

    @Override
    protected List<FieldDefinition> getFieldsToTest() {

        PictureFieldDefinition picture1 = new PictureFieldDefinition();
        picture1.setSize( PictureSize.SMALL );

        PictureFieldDefinition picture2 = new PictureFieldDefinition();
        picture1.setSize( PictureSize.MEDIUM );

        return Arrays.asList( initFieldDefinition( picture1 ),
                initFieldDefinition( picture2 ) );
    }

    @Override
    protected List<InputCreatorHelper> getInputHelpersToTest() {
        return Arrays.asList( new PictureHelper() );
    }
}
