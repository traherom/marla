[Back to Help Contents](HelpContents.md)



# What is The maRla project? #
The maRla IDE is intended to be a work environment for students in early statistical classes. It has the ability to take a student all the way from typing the problem statement--straight from the book--out
to a final, polished PDF to turn into a professor.

maRla IDE does this in a graphical environment, where most work is done by dragging around data and operations.
Operations may be attached to data, data can have multiple operations working off of it, and operations may chain together to perform more complex calculations. Once the needed calculations are done by the user,
maRla can export to a PDF file that includes the original problem statement, data that was given, the steps (and associated analysis) needed to solve the various parts of the problem, resulting data, and
any conclusions the user drew from their work.

# Who should use maRla? #
First year statistics students are the primary target. However, operations may be added and modified in the maRla Operation Editor, so the maRla IDE can be used for any sort of mathematical operation that the R package supports.

# What do I do if I tried exporting to a PDF and got the error message "Sweave does not appear to be registered correctly with LaTeX"? #
The Windows installer attempts to fix it for you, so try that first.

Assuming it didn't work or you're on a different operating system, the message means
that LaTeX doesn't know about R. To fix that you need to register the "Sweave.sty" file
with LaTeX. Usually this file is in R's share/texmf folder, but how exactly to register
it depends on your system and can be complicated. Try Googling it, but if you still need help, submit
a [support request](http://code.google.com/p/marla/issues/entry?template=Support%20Request). Be sure
to tell us what operation system you're using and where R and LaTeX are installed.

# How do I configure R and/or MiKTeX manually? #
If you have a limited user account, it will break the automatic installation of R and MiKTeX, so you may be asked to configure R and/or LaTeX manually. The work-around is to install both of these manually and then re-run the maRla installer. The latest R installer is available from [www.r-project.org](http://cran.case.edu/bin/windows/base/release.htm). MiKTeX can be found at [miktex.org](http://miktex.org/2.9/setup).

The installer may handle this more gracefully in the future.

# What do I do if maRla refuses to recognize LaTeX and/or R? #
On occasion an R or LaTeX install will be corrupt and maRla is unable to use them. Usually reinstalling the offending
program will solve the problem (if on Windows, the [maRla installer](http://code.google.com/p/marla/downloads/list)
can make it easy).

There are some versions of pdfTeX that are just plain incompatible with maRla, possibily due
to their age. Ensure you have a fairly recent version. If you still have problems, submit a
[support request](http://code.google.com/p/marla/issues/entry?template=Support%20Request) and we
can try to help you out.

# Why does maRla want to use my internet connection? #
At start up maRla checks to ensure it's up-to-date and connects to our error server.
If maRla encounters any internal issues, it tries to send information about it to our servers,
allowing us to fix issues quicker.

If you want to disable error reporting, go into the maRla preferences and uncheck the relevant
option. If you do run into issues that you need help with, maRla also logs them to a file
called log.dat in whatever directory it runs from (on Windows, probably the installation
directory).

# Where does maRla save my preferences? #
It varies by operating system, but generally speaking in `%UserHome%/.marla/`.
Common ones are the following
  * Windows 7/Vista: `C:\Users\<your user>\.marla\`
  * Cedarville University computers: `H:\NET\CFG\.marla\`
  * Linux: `~/.marla/`

# How can I see what maRla is doing in the background, or how can I debug an issue myself? #
Go into maRla's preferences and check the "Debug Mode" checkbox. A console with data should appear at the bottom of the window.

You'll probably have more fun actually debugging it in NetBeans though. Follow the instructions
on setup your [build environment](Building.md)

# What do I do if I found a bug? #
If you find a problem with the software, you may submit an Issue [here](http://code.google.com/p/marla/issues/entry). Be aware that The maRla Project comes with no guarantee or warranty. No other support other than these help page is offered for maRla.

# I'm interested in just utilizing the R Processor you developed for The maRla Project #
The source for the R Processor was developed under the GNU GPL v3, so you are welcome to take and reuse the R Processor code. The source for our R Processor can be found [here](http://code.google.com/p/marla/source/browse/#svn%2Ftrunk%2FmaRlaIDE%2Fsrc%2Fmarla%2Fide%2Fr). If you would like to checkout the entire project (or even just that portion of the folder), see the read-only checkout instructions [here](http://code.google.com/p/marla/source/checkout).

# Can I contribute to The maRla Project? #
The maRla Project can be checked out using Subversion by following the instructions on [this page](http://code.google.com/p/marla/source/checkout).  However, this checkout will be a read-only checkout if you do not have commit access to the repository.  If you are interested in helping further develop The maRla Project, please contact one of the owners of this Google Code site.

# I downloaded the source and fixed a bug myself. Will you include this in the next release? #
If you have encountered a bug with The maRla Project, downloaded the source, and developed a fix yourself that you would like to see incorporated into a future version of the software, contact an owner of this Google Code page and we will try to implement that fix into the next major software release.

[Back to Help Contents](HelpContents.md)