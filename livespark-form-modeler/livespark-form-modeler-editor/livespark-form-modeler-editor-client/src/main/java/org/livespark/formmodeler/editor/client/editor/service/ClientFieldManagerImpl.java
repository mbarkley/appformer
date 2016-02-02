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
package org.livespark.formmodeler.editor.client.editor.service;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.livespark.formmodeler.editor.model.FieldDefinition;
import org.livespark.formmodeler.editor.service.AbstractFieldManager;

/**
 * Created by pefernan on 9/25/15.
 */
@ApplicationScoped
public class ClientFieldManagerImpl extends AbstractFieldManager {

    @PostConstruct
    protected void init() {
        Collection<SyncBeanDef<FieldDefinition>> fields = IOC.getBeanManager().lookupBeans(FieldDefinition.class);
        for (SyncBeanDef<FieldDefinition> field : fields) {
            registerFieldDefinition( field.getInstance() );
        }
    }

    @Override
    protected FieldDefinition createNewInstance(FieldDefinition definition) throws Exception {
        return  IOC.getBeanManager().lookupBean(definition.getClass()).newInstance();
    }
}
