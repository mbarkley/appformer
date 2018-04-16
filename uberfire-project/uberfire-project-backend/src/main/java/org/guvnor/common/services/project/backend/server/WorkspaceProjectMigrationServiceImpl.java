/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.guvnor.common.services.project.backend.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.guvnor.common.services.project.backend.server.utils.PathUtil;
import org.guvnor.common.services.project.events.NewProjectEvent;
import org.guvnor.common.services.project.model.Module;
import org.guvnor.common.services.project.model.WorkspaceProject;
import org.guvnor.common.services.project.project.WorkspaceProjectMigrationService;
import org.guvnor.common.services.project.service.ModuleService;
import org.guvnor.common.services.project.service.WorkspaceProjectService;
import org.guvnor.structure.organizationalunit.OrganizationalUnit;
import org.guvnor.structure.organizationalunit.OrganizationalUnitService;
import org.guvnor.structure.repositories.Branch;
import org.guvnor.structure.repositories.Repository;
import org.guvnor.structure.repositories.RepositoryEnvironmentConfigurations;
import org.guvnor.structure.repositories.RepositoryService;
import org.guvnor.structure.repositories.impl.git.GitRepository;
import org.uberfire.java.nio.file.Path;

import static java.util.stream.Collectors.toList;

public class WorkspaceProjectMigrationServiceImpl
                                                  implements WorkspaceProjectMigrationService {

    private WorkspaceProjectService workspaceProjectService;
    private RepositoryService repositoryService;
    private Event<NewProjectEvent> newProjectEvent;
    private ModuleService<? extends Module> moduleService;
    private PathUtil pathUtil;

    public WorkspaceProjectMigrationServiceImpl() {}

    @Inject
    public WorkspaceProjectMigrationServiceImpl(final WorkspaceProjectService workspaceProjectService,
                                                final RepositoryService repositoryService,
                                                final OrganizationalUnitService organizationalUnitService,
                                                final PathUtil pathUtil,
                                                final Event<NewProjectEvent> newProjectEvent,
                                                final ModuleService<? extends Module> moduleService) {
        this.workspaceProjectService = workspaceProjectService;
        this.repositoryService = repositoryService;
        this.pathUtil = pathUtil;
        this.newProjectEvent = newProjectEvent;
        this.moduleService = moduleService;
    }

    @Override
    public void migrate(final WorkspaceProject legacyWorkspaceProject) {
        Collection<Repository> newRepositories = copyModulesToRepositories(legacyWorkspaceProject);
        fireNewProjectEvents(newRepositories);
    }

    private void fireNewProjectEvents(Collection<Repository> newRepositories) {
        for (final Repository repository : newRepositories) {
            final WorkspaceProject newWorkspaceProject = workspaceProjectService.resolveProject(repository);
            newProjectEvent.fire(new NewProjectEvent(newWorkspaceProject));
        }
    }

    private Collection<Repository> copyModulesToRepositories(WorkspaceProject legacyWorkspaceProject) {

        final OrganizationalUnit ou = legacyWorkspaceProject.getOrganizationalUnit();
        final Repository legacyRepository = legacyWorkspaceProject.getRepository();
        // Partition modules by root path (ignoring branch) and space.
        final Map<Partition, List<Module>> modulesByDirectory = getModulesByRootDirAndSpace(ou, legacyRepository);

        return modulesByDirectory.entrySet()
                                 .stream()
                                 .map(entry -> createSubdirectoryCloneRepository(ou, legacyRepository, entry))
                                 .collect(toList());
    }

    private Repository createSubdirectoryCloneRepository(final OrganizationalUnit ou, final Repository legacyRepository, Entry<Partition, List<Module>> entry) {
        final Partition partition = entry.getKey();
         final List<Module> modules = entry.getValue();
         final String alias = modules.stream()
                                     .map(module -> module.getModuleName())
                                     .findFirst()
                                     .orElse("migratedproject");
         final RepositoryEnvironmentConfigurations configurations = subdirectoryCloneConfiguration(legacyRepository,
                                                                                                   partition,
                                                                                                   modules);

         return repositoryService.createRepository(ou, GitRepository.SCHEME.toString(), alias, configurations);
    }

    private RepositoryEnvironmentConfigurations subdirectoryCloneConfiguration(final Repository legacyRepository, final Partition root, final List<Module> modules) {
        final RepositoryEnvironmentConfigurations configurations = new RepositoryEnvironmentConfigurations();
         configurations.setInit(false);
         configurations.setOrigin(getNiogitRepoPath(legacyRepository));
         final String rootWithoutRepoAndSpace = root.branchlessPath.replaceFirst("^[^/]+/[^/]+/", "");
         configurations.setSubdirectory(rootWithoutRepoAndSpace);
         configurations.setMirror(false);
         final List<String> branches = existingBranchesOf(modules);
        configurations.setBranches(branches);

        return configurations;
    }

    /**
     * @return Branches where all given modules exist.
     */
    private List<String> existingBranchesOf(final List<Module> modules) {
        final List<String> branches =
                 modules.stream()
                        .flatMap(module -> {
                            Optional<String> oBranch = getBranchName(pathUtil.convert(module.getRootPath()));
                            if (oBranch.isPresent()) {
                                return Stream.of(oBranch.get());
                            } else {
                                return Stream.empty();
                            }
                        })
                        .collect(toList());
        return branches;
    }

    private Map<Partition, List<Module>> getModulesByRootDirAndSpace(final OrganizationalUnit ou, final Repository legacyRepository) {
        final Map<Partition, List<Module>> modulesByDirectory = new HashMap<>();
        legacyRepository.getBranches()
                        .stream()
                        .flatMap(branch -> moduleService.getAllModules(branch).stream())
                        .forEach(module -> {
                            final String fullURI = pathUtil.normalizePath(module.getRootPath()).toURI();
                            final String branchlessPath = fullURI.replaceFirst("^[A-Za-z]+://([^@]+@)?", "");
                            final Partition partition = new Partition(branchlessPath, ou);
                            final List<Module> modules = modulesByDirectory.computeIfAbsent(partition,
                                                                                            ignore -> new ArrayList<>());
                            modules.add(module);
                        });
        return modulesByDirectory;
    }

    private static Optional<String> getBranchName(Path path) {
        final String uri = path.toUri().toString();
        final Matcher matcher = Pattern.compile("^[A-Za-z]+://([^@]+)@.*").matcher(uri);

        if (matcher.matches()) {
            return Optional.ofNullable(matcher.group(1));
        } else {
            return Optional.empty();
        }
    }

    private String getNiogitRepoPath(Repository repository) {
        final Branch branch = repository.getDefaultBranch().get();
        final Path path = pathUtil.convert(branch.getPath());
        return pathUtil.getNiogitRepoPath(path);
    }

    private static class Partition {

        final String branchlessPath;
        final OrganizationalUnit ou;

        Partition(String branchlessPath, OrganizationalUnit ou) {
            this.branchlessPath = branchlessPath;
            this.ou = ou;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((branchlessPath == null) ? 0 : branchlessPath.hashCode());
            result = prime * result + ((ou == null) ? 0 : ou.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Partition other = (Partition) obj;
            if (branchlessPath == null) {
                if (other.branchlessPath != null)
                    return false;
            } else if (!branchlessPath.equals(other.branchlessPath))
                return false;
            if (ou == null) {
                if (other.ou != null)
                    return false;
            } else if (!ou.equals(other.ou))
                return false;
            return true;
        }

    }
}
