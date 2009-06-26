package net.kindleit.gae;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.tools.KickStart;
import com.google.appengine.tools.admin.AppCfg;

/** Base MOJO class for working with the Google App Engine SDK.
 *
 * @author rhansen@kindleit.net
 */
public abstract class EngineGoalBase extends AbstractMojo {

  public static final String PLUGIN_VERSION="0.3";
  protected static final String[] ARG_TYPE = new String[0];

  /** The Maven project reference.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /** The Maven settings reference.
  *
  * @parameter expression="${settings}"
  * @required
  * @readonly
  */
 protected Settings settings;

  /** Overrides where the Project War Directory is located.
   *
   * @parameter expression="${project.build.directory}/${project.build.finalName}"
   * @required
   */
  protected String appDir;

  /** Specifies where the Google App Engine SDK is located.
   *
   * @parameter expression="${appengine.sdk.root}"
   * @required
   */
  protected String sdkDir;

  /** Split large jar files (> 10M) into smaller fragments.
   *
   * @parameter expression="${gae.deps.split}" default-value="false"
   */
  protected boolean splitJars;

  /** The username to use. Will prompt if omitted.
   *
   * @parameter expression="${gae.email}"
   */
  protected String emailAccount;

  /** The server to connect to.
   *
   * @parameter expression="${gae.server}"
   */
  protected String uploadServer;

  /** Overrides the Host header sent with all RPCs.
   *
   * @parameter expression="${gae.host}"
   */
  protected String hostString;

  /** Do not delete temporary directory used in uploading.
   *
   * @parameter expression="${gae.keepTemps}" default-value="false"
   */
  protected boolean keepTempUploadDir;

  /** Always read the login password from stdin.
   *
   * @parameter expression="${gae.passin}" default-value="false"
   */
  protected boolean passIn;

  /** Passes command to the Google App Engine AppCfg runner.
  *
  * @param command command to run through AppCfg
  * @param commandArguments arguments to the AppCfg command.
  */
  protected final void runAppCfg(final String command,
      final String ... commandArguments) {

    final List<String> args = getAppCfgArgs();
    args.addAll(Lists.asList(command, commandArguments));

    AppCfg.main(args.toArray(ARG_TYPE));
  }

  /** Passes command to the Google App Engine KickStart runner.
   *
   * @param startClass command to run through KickStart
   * @param commandArguments arguments to the KickStart command.
   */
  protected final void runKickStart(final String startClass,
      final String ... commandArguments) {

    final List<String> args = Lists.asList(startClass, commandArguments);
    args.addAll(getCommonArgs());

    KickStart.main(args.toArray(ARG_TYPE));
  }

  /** Generate all common Google AppEngine Task Parameters for use in all the
   * goals.
   *
   * @return List of arguments to add.
   */
  protected final List<String> getAppCfgArgs () {
    final List<String> args = getCommonArgs();


    addBooleanOption(args, "--disable_prompt", !settings.getInteractiveMode());

    addStringOption(args, "--email=", emailAccount);
    addStringOption(args, "--host=", hostString);
    addBooleanOption(args, "--passin", passIn);
    addBooleanOption(args, "--enable_jar_splitting", splitJars);
    addBooleanOption(args, "--retain_upload_dir", keepTempUploadDir);

    return args;
  }

  protected final List<String> getCommonArgs() {
    final List<String> args = new ArrayList<String>(8);

    args.add("--sdk_root=" + sdkDir);
    addStringOption(args, "--server=", uploadServer);

    return args;
  }

  private final void addBooleanOption(final List<String> args, final String key,
      final boolean var) {
    if (var) {
      args.add(key);
    }
  }

  private final void addStringOption(final List<String> args, final String key,
      final String var) {
    if (var != null && var.length() > 0) {
      args.add(key + var);
    }
  }

}
