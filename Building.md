[Back to Help Contents](HelpContents.md)

# Quick Start #
maRla builds through an ant script and will build at a basic level in any JDK environment. To fully use the package-for-store and create-installer build targets (producing the Windows installer, most notably), [NSIS](http://nsis.sourceforge.net/), [Python](http://www.python.org/download/), and [launch4j](http://launch4j.sourceforge.net/) need to be installed and on the path.

# Build Environment #
The following environments are recommended for building maRla. Most development can occur with only ant and JDK 1.6+, with the final packaging steps requiring additional setup. The programs listed below should be installed on each OS, with additional notes as needed. Version numbers are those as of the writing of this document, although other version may work as well.

## Windows ##
  1. **[Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)** v1.6+
  1. [NetBeans](http://netbeans.org/downloads/index.html) v7.0+
  1. [launch4j](http://launch4j.sourceforge.net/) v3.0.2
    * Must be installed to `C:\Program Files\launch4j`
    * If moved, modifications to [build.xml](http://code.google.com/p/marla/source/browse/trunk/maRlaIDE/build.xml) will be needed. See the launch4j task definition in that file
  1. [Nullsoft Scriptable Install System](http://nsis.sourceforge.net/) v2.46
    * After install, place the directory with `makensis.exe` on the `PATH`
    * By default, this would be `%Program Files%\NSIS`
  1. [Python](http://www.python.org/download/) v2.6+
    * Must be on `PATH`

## Linux ##
Your distribution may have packages for many of these items:
  1. **[Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)** v1.6+
  1. [NetBeans](http://netbeans.org/downloads/index.html) v7.0+
  1. [launch4j](http://launch4j.sourceforge.net/) v3.0.2
    * Must be installed to `/opt/launch4j/`
    * If moved, modifications to [build.xml](http://code.google.com/p/marla/source/browse/trunk/maRlaIDE/build.xml) will be needed. See the launch4j task definition in that file
  1. [Nullsoft Scriptable Install System](http://nsis.sourceforge.net/) v2.46
    * After install, ensure the directory with `makensis` on the `PATH`
  1. [Python](http://www.python.org/download/) v2.6+
    * Must be on `PATH`

## Mac ##
  1. **[Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html)** v1.6+
  1. [NetBeans](http://netbeans.org/downloads/index.html) v7.0+
  1. [launch4j](http://launch4j.sourceforge.net/) v3.0.2
    * Must be installed to `/Developer/launch4j/`
    * If moved, modifications to [build.xml](http://code.google.com/p/marla/source/browse/trunk/maRlaIDE/build.xml) will be needed. See the launch4j task definition in that file
  1. Nullsoft Scriptable Install System is not officially supported on Mac. Though it can be compiled manually for Darwin or run through Wine, it may not behave exactly the same, thus the installer you create may contain errors. Avoid building the Windows installer from Mac.
  1. [Python](http://www.python.org/download/) v2.6+
    * Must be on `PATH`

# Getting the Source #
Instructions for getting the maRla source code are available under the [Source](http://code.google.com/p/marla/source/checkout) tab.

# Ant Targets #
Once the build environment is setup, building of maRla can be done through ant (or NetBeans interface to ant). The most important special targets are the following.
  * package-for-store - creates a single jar that contains all of maRla and its libraries, plus copies the needed default ops.xml and export\_template.xml to the `./store` directory.
  * create-installer - builds the Windows installer and zips up required files for distribution to other platforms.

[Back to Help Contents](HelpContents.md)