/*
 * Copyright 2011 Kindleit.net Software Development Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package net.kindleit.gae;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Downloads and unzips the SDK to your maven repository. Use this goal, if you don't wish to
 * specify a <i>gae.home</i> or <i>-Dappegine.sdk.home property</i>. The plugin will now search for
 * the SDK in that default location.
 *
 * @author rhansen@kindleit.net
 * @phase initialize
 * @execute phase="initialize"
 * @requiresProject false
 * @requiresDependencyResolution runtime
 * @goal unpack
 */
public class UnpackGoal extends EngineGoalBase {

  private static final String APPENGINE_API_ARTIFACTID = "appengine-api-1.0-sdk";

  private static final String APPENGINE_API_GROUPID = "com.google.appengine";

  private static final String SDK_ARTIFACT_ID = "appengine-java-sdk";

  private static final String SDK_GROUPID = APPENGINE_API_GROUPID;

  /**
   * Used to look up Artifacts in the remote repository.
   * @component role="org.apache.maven.artifact.factory.ArtifactFactory"
   * @required
   * @readonly
   */
  protected ArtifactFactory factory;

  /**
   * @parameter default-value="${plugin.artifacts}"
   * Used to guess the unpackVersion if not supplied.
   * @readonly
   */
  private List<Artifact> pluginArtifacts;

  /**
   * Used to look up Artifacts in the remote repository.
   * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
   * @required
   * @readonly
   */
  protected ArtifactResolver artifactResolver;

  /**
   * List of Remote Repositories used by the resolver
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  protected List<?> remoteRepos;

  /**
   * Location of the local repository.
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  protected ArtifactRepository localRepo;

  /**
   * To look up Archiver/UnArchiver implementations
   *
   * @component
   */
  protected ArchiverManager archiverManager;

  /**
   * Version of the plugin to unpack.
   * Heuristics are taken now into account to guess the version from the list of
   * pluginArtifacts.
   *
   * @parameter expression="${unpackVersion}" default-value="${gae.version}"
   * @required
   */
  protected String unpackVersion;

  public void execute() throws MojoExecutionException, MojoFailureException {
    try {

      unpackVersion = versionHeuristics();

      final Artifact sdkArtifact =
          factory.createArtifact(SDK_GROUPID, SDK_ARTIFACT_ID, unpackVersion,
              "", "zip");
      artifactResolver.resolve(sdkArtifact, remoteRepos, localRepo);
      final File sdkLocation = sdkArtifact.getFile().getParentFile();
      final File sdkDestination = new File(sdkDir);

      if (sdkDestination.exists()) {
        getLog().info(String.format("Found %s %s.", sdkArtifact.getArtifactId(), sdkArtifact.getBaseVersion()));
      } else {
        getLog().info("Extracting GAE SDK file: " + sdkArtifact.getFile().getAbsolutePath());
        getLog().info("To path: " + sdkLocation.getAbsolutePath());

        final UnArchiver unArchiver = archiverManager.getUnArchiver(sdkArtifact.getFile());
        unArchiver.setSourceFile(sdkArtifact.getFile());
        unArchiver.setDestDirectory(sdkLocation);
        unArchiver.extract();
      }
    } catch (final ArtifactResolutionException e) {
      getLog().error("can't resolve parent pom", e);
    } catch (final ArtifactNotFoundException e) {
      getLog().error("can't resolve parent pom", e);
    } catch (final NoSuchArchiverException e) {
      getLog().error("can't find archiver for the SDK download", e);
    } catch (final ArchiverException e) {
      getLog().error("can't extract the SDK archive", e);
    }

  }

  /** Attempts to guess the desired unpack version from the plugin's dependencies.
   *
   * @return the supplied unpackVersion or the version as specified by the appengine-api dependency.
   */
  private String versionHeuristics()
  {
    if (unpackVersion == null || unpackVersion.isEmpty()) {
      for (final Artifact pa : pluginArtifacts) {
        if (APPENGINE_API_GROUPID.equals(pa.getGroupId())
        && APPENGINE_API_ARTIFACTID.equals(pa.getArtifactId())) {
          return pa.getVersion();
        }
      }
    }
    return unpackVersion;
  }

}
