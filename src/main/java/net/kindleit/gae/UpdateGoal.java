/*
 * Copyright 2008 Kindleit Technologies. All rights reserved. This file, all
 * proprietary knowledge and algorithms it details are the sole property of
 * Kindleit Technologies unless otherwise specified. The software this file
 * belong with is the confidential and proprietary information of Kindleit
 * Technologies. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Kindleit.
 */
package net.kindleit.gae;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal to upload a WAR project on Googles servers.
 *
 * @author rhansen@kindleit.net
 *
 * @goal update
 * @execute phase=package
 */
public class UpdateGoal extends EngineGoalBase {

  /** Create or update an app version.
   * This goal uploads your web application to the google app engine server.
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Updating Google App Engine Server...");
    runAppCFG("update", appDir);
  }
}


