/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.uberfire.backend.server.spaces;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.uberfire.commons.services.cdi.Startup;
import org.uberfire.commons.services.cdi.StartupType;
import org.uberfire.spaces.Space;
import org.uberfire.spaces.SpacesAPI;

@ApplicationScoped
@Startup(StartupType.BOOTSTRAP)
public class SpacesAPIImpl implements SpacesAPI {

    private static final String PATH_PATTERN = "^[A-Za-z]+://([^/]+)/.*";
    private static final Pattern PATH_REGEX = Pattern.compile(PATH_PATTERN);

    private Map<String, Space> spaces = new HashMap<>();

    @PostConstruct
    public void setup() {
        //in future load all spaces here by default
        spaces.put(SpacesAPI.DEFAULT_SPACE_NAME,
                   SpacesAPI.DEFAULT_SPACE);
    }

    @Override
    public Space getSpace(String name) {
        //TODO put if absence
        spaces.putIfAbsent(name,
                           new Space(name));
        return spaces.get(name);
    }

    @Override
    public Optional<Space> resolveSpace(String uri) {
        return Optional
        .ofNullable(PATH_REGEX.matcher(uri))
        .filter(matcher -> matcher.matches())
        .map(matcher -> matcher.group(1)).map(this::getSpace);
    }


    public URI resolveFileSystemURI(Scheme scheme,
                                    Space space,
                                    String fsName) {

        URI uri = URI.create(SpacesAPI.resolveFileSystemPath(scheme,
                                                             space,
                                                             fsName));
        return uri;
    }

    @Override
    public Collection<Space> getSpaces() {
        return spaces.values();
    }

    @Override
    public Collection<Space> getUserSpaces() {
        final Collection<Space> userSpaces = spaces.values();
        userSpaces.remove(SpacesAPI.DEFAULT_SPACE);

        return userSpaces;
    }
}
