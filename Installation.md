[Back to Help Contents](HelpContents.md)



# Windows #
The maRla Windows installer, available on the [Downloads](http://code.google.com/p/marla/downloads/list) page, attempts to do all configuration necessary for Windows XP, Vista, and 7 systems. This is the recommended path to take. However, if maRla fails to run after using the installer, manual installation may be required. Instructions for installing and configuring each package are below:

## Java ##
Download and install the Java Runtime Environment from [here](http://www.java.com/en/download).

## R ##
maRla requires R to be installed on your system. No special configuration is required of R, so a default installation should work fine. Go to the  [R site](http://cran.r-project.org/mirrors.html) and [download](http://cran.case.edu/bin/windows/base/release.htm) an installer. Install it and accept the default options. If R is installed in Program Files (something like `C:\Program Files\R\2-2.x`), maRla should be able to locate it just fine when it starts.

If you need to use an R installation that maRla isn't able to automatically locate, it will pop up a dialog for you to choose the location of the main R binary. You want to choose the command-line R.exe file. In a default installation, this would be located at `C:\Program Files\R\R-2.12.1\bin\R.exe`. If the file you choose is not correct, maRla will prompt you again. If you want to use a relative path, you can set this manually after maRla has launched in the [settings](SettingsHelp.md).

## MiKTeX ##
Secondly, maRla depends on Latex for generating PDFs. This, unfortunately, takes a bit more work to configure correctly. Follow the steps below to get everything configured correctly for use with Miktex. Skip to the appropriate point, depending on what you have completed.

  * [Install R](#R.md)
  * Download [MiKTeX 2.9 installer](http://miktex.org/2.9/setup)
  * Install MiKTeX, preferably turning on automatic package installation.
  * Register Sweave with MiKTeX (TBD, only rough currently)
    * Open up the MiKTeX Settings by going to Start->All Programs->MiKTeX 2.9->Maintenance->Settings
    * Switch to the "Roots" tab and click the "Add..." button
    * Select R's `texmf` directory. If R was installed at the default location, this is `C:\Program Files\R\R-2.12.x\share\texmf`. Your window should now look like this:
> > ![http://marla.googlecode.com/svn/wiki/screenshots/miktex_roots.png](http://marla.googlecode.com/svn/wiki/screenshots/miktex_roots.png)
  * Turn on automatic package installation for MiKTeX
    * If it isn't open, open up the MiKTeX Settings by going to Start->All Programs->MiKTeX 2.9->Maintenance->Settings
    * Switch to the "General" tab and find the "Package Installation" section
    * Change the "Install missing packages on-the-fly" select box to "Yes"
    * Hit the OK button to complete setup

# Linux #
Configuration is typically much easier on Linux than on Windows. The exact steps depend on your distribution, but you will need to ensure the latest JRE, R, and pdflatex are installed on your system. From there, the cross-platform archive of maRla, available on the [Downloads](http://code.google.com/p/marla/downloads/list) page, should be runnable from any directory.

More detailed directions for select distributions are below.

## Ubuntu/Derivative ##
  1. Run
```
sudo apt-get update
sudo apt-get install default-jre r-base-core texlive-base
```
  1. Download current release archive from Downloads page.
  1. Extract and run where desired
  1. (If you double-click on a JAR executable from maRla and it will not launch, your distribution may not allow desktop execution of JAR files. You can still run maRla, you just need to launch it from the command line. For instance, to launch the maRla IDE, navigate to the directory you unzipped the archive to and type
```
java -jar maRla\ IDE.jar
```

# Mac #
Configuration is typically much easier on Mac than on Windows.

If you have the Xcode development environment installed on your Mac, you may already have R and LaTeX, so try just download and running the cross-platform version of maRla.

If you do not Xcode installed on your Mac, or if your distribution of Xcode did not include R and LaTeX, follow the instructions below:

  1. Download and install the latest version of Java from [here](http://www.java.com/download)
  1. Download and install R from [here](http://cran.r-project.org/bin/macosx/)
  1. Download and install MacTex from [here](http://www.tug.org/mactex/)
  1. Download and install The maRla Project from [here](http://code.google.com/p/marla/downloads/list) (cross-platform version)
  1. Extract The maRla Project cross-platform zip file to a new folder, "The maRla Project", in your Applications folder

[Back to Help Contents](HelpContents.md)