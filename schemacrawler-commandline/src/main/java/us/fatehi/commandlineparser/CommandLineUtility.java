/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package us.fatehi.commandlineparser;


import static sf.util.Utility.join;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

import picocli.CommandLine;
import schemacrawler.JvmSystemInfo;
import schemacrawler.OperatingSystemInfo;
import schemacrawler.Version;
import schemacrawler.schemacrawler.Config;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;
import sf.util.Utility;
import sf.util.UtilityMarker;

@UtilityMarker
public final class CommandLineUtility
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(CommandLineUtility.class.getName());

  /**
   * Sets the application-wide log level.
   */
  public static void applyApplicationLogLevel(final Level applicationLogLevel)
  {
    Utility.applyApplicationLogLevel(applicationLogLevel);
  }

  public static void logFullStackTrace(final Level level, final Throwable t)
  {
    if (level == null || !LOGGER.isLoggable(level))
    {
      return;
    }
    if (t == null)
    {
      return;
    }

    LOGGER.log(level, t.getMessage(), t);
  }

  public static void logSafeArguments(final String[] args)
  {
    if (!LOGGER.isLoggable(Level.INFO))
    {
      return;
    }

    LOGGER.log(Level.INFO,
               String.format("Environment:%n%s %s%n%s%n%s%n",
                             Version.getProductName(),
                             Version.getVersion(),
                             new OperatingSystemInfo(),
                             new JvmSystemInfo()));

    if (args == null)
    {
      return;
    }

    final StringJoiner argsList = new StringJoiner(System.lineSeparator());
    for (final Iterator<String> iterator = Arrays.asList(args)
      .iterator(); iterator.hasNext(); )
    {
      final String arg = iterator.next();
      if (arg == null)
      {
        continue;
      }
      else if (arg.startsWith("-password="))
      {
        argsList.add("-password=*****");
      }
      else if (arg.startsWith("-password"))
      {
        argsList.add("-password");
        if (iterator.hasNext())
        {
          // Skip over the password
          iterator.next();
          argsList.add("*****");
        }
      }
      else
      {
        argsList.add(arg);
      }
    }

    LOGGER.log(Level.INFO,
               new StringFormat("Command line: %n%s", argsList.toString()));
  }

  public static void logSystemClasspath()
  {
    if (!LOGGER.isLoggable(Level.CONFIG))
    {
      return;
    }

    LOGGER.log(Level.CONFIG,
               String.format("Classpath: %n%s",
                             printPath(System.getProperty("java.class.path"))));
    LOGGER.log(Level.CONFIG,
               String.format("LD_LIBRARY_PATH: %n%s",
                             printPath(System.getenv("LD_LIBRARY_PATH"))));
  }

  public static void logSystemProperties()
  {
    if (!LOGGER.isLoggable(Level.CONFIG))
    {
      return;
    }

    final SortedMap<String, String> systemProperties = new TreeMap<>();
    for (final Entry<Object, Object> propertyValue : System.getProperties()
      .entrySet())
    {
      final String name = (String) propertyValue.getKey();
      if ((name.startsWith("java.") || name.startsWith("os.")) && !name
        .endsWith(".path"))
      {
        systemProperties.put(name, (String) propertyValue.getValue());
      }
    }

    LOGGER.log(Level.CONFIG,
               String.format("System properties: %n%s",
                             join(systemProperties, System.lineSeparator())));
  }

  public static CommandLine newCommandLine(final Object object)
  {
    final CommandLine commandLine;
    commandLine = new CommandLine(object);
    commandLine.setUnmatchedOptionsArePositionalParams(true);
    commandLine.setCaseInsensitiveEnumValuesAllowed(true);
    commandLine.setTrimQuotes(true);
    commandLine.setToggleBooleanFlags(false);
    return commandLine;
  }

  public static CommandLine newCommandLine(final Object object,
                                           final CommandLine.IFactory factory)
  {
    final CommandLine commandLine;
    commandLine = new CommandLine(object, factory);
    commandLine.setUnmatchedArgumentsAllowed(true);
    commandLine.setCaseInsensitiveEnumValuesAllowed(true);
    commandLine.setTrimQuotes(true);
    commandLine.setToggleBooleanFlags(false);
    return commandLine;
  }

  /**
   * Loads configuration from a number of command-line.
   *
   * @param args Command-line arguments
   * @return Parsed command-line arguments
   */
  public static Config parseArgs(final String[] args)
  {
    final CommandLineArgumentsParser argsParser = new CommandLineArgumentsParser(
      args);
    argsParser.parse();
    final Config optionsMap = argsParser.getOptionsMap();
    return optionsMap;
  }

  private static String printPath(final String path)
  {
    if (path == null)
    {
      return "";
    }
    return String.join(System.lineSeparator(), path.split(File.pathSeparator));
  }

  private CommandLineUtility()
  {
    // Prevent instantiation
  }

}
