/* Copyright 2009 Kindleit.net Software Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kindleit.gae.runner;

import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import com.google.appengine.tools.KickStart;

/**
 * An implementation of {@code KickStartRunner} that asynchronously invokes {@link com.google.appengine.tools.KickStart}
 * in a forked process.
 *
 * @author tmoore@incrementalism.net
 * @since 0.5.8
 */
final class BackgroundKickStartRunner extends KickStartRunner {
  private Thread thread;
  private Exception thrown;

  /**
   * Creates a new {@code BackgroundKickStartRunner}.
   *
   * @param log the Maven plugin logger to direct output to
   */
  public BackgroundKickStartRunner(Log log) {
    super(log);
  }

  /**
   * Asynchronously starts a {@code KickStart} instance with the specified arguments.
   * This method method will block until the server starts up, and then allows the current thread to continue while
   * the server runs in the background.
   *
   * @param args the arguments to pass to {@code KickStart}
   */
  public synchronized void start(final List<String> args) throws KickStartExecutionException {
    if (thread != null) {
      throw new IllegalStateException("Already started");
    }

    final Commandline commandline = new Commandline("java");
    commandline.createArg().setValue("-ea");
    commandline.createArg().setValue("-cp");
    commandline.createArg().setValue(System.getProperty("java.class.path"));
    commandline.createArg().setValue("-Dappengine.sdk.root=" + System.getProperty("appengine.sdk.root"));
    commandline.createArg().setValue(KickStart.class.getName());
    commandline.addArguments(args.toArray(new String[args.size()]));

    final StreamConsumer outConsumer = new StreamConsumer() {
      public void consumeLine(final String line) {
        consumeOutputLine(line);
      }
    };

    final StreamConsumer errConsumer = new StreamConsumer() {
      public void consumeLine(final String line) {
        consumeErrorLine(line);
      }
    };

    thread = new Thread(new Runnable() {
      public void run() {
        try {
          CommandLineUtils.executeCommandLine(commandline, outConsumer, errConsumer);
        } catch (Exception e) {
          setThrown(e);
        }
      }
    });
    thread.start();

    waitUntilStarted();
  }

  /**
   * Stops the {@code KickStart} instance started by this runner by interrupting the background thread.
   */
  public synchronized void stop() {
    if (thread != null) {
      getLog().info("Stopping App Engine");
      thread.interrupt();
    }
    thread = null;
  }

  private synchronized void consumeOutputLine(final String line) {
    if (!isStarted() && line.startsWith("The server is running")) {
      setStarted(true);
    }
    System.out.println(line);
  }

  private synchronized void consumeErrorLine(final String line) {
    System.err.println(line);
  }

  private synchronized void setThrown(final Exception thrown) {
    this.thrown = thrown;
    notifyAll();
  }

  private synchronized void waitUntilStarted() throws KickStartExecutionException {
    while (!isStarted() && thrown == null) {
      try {
        wait();
      } catch (InterruptedException e) {
        thrown = e;
        break;
      }
    }

    if (thrown != null) {
      if (thrown instanceof RuntimeException) {
        throw (RuntimeException) thrown;
      }
      throw new KickStartExecutionException(thrown);
    }
  }
}
