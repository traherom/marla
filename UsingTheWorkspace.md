[Back to Help Contents](HelpContents.md)



# Introduction #
Most work in maRla is done from what is referred to as the workspace. This area is where you layout your solution to a problem, designation solution steps, add comments on why you did an operation, and more. Although maRla strives to be user-friendly and intuitive, this page will help you discover all of the power available to you.

![http://marla.googlecode.com/svn/wiki/screenshots/workspace.png](http://marla.googlecode.com/svn/wiki/screenshots/workspace.png)

# Workspace Elements #
The maRla workspace contains several different sections. The sidebar on the right side contains all loaded operations and data sets, the toolbar contains shortcuts to quickly access commands, and the main workspace allows the user to perform data set operations.
## The Sidebar ##
The sidebar contains all loaded operations. In order to access the operations, select the relevant bar by clicking anywhere on it. For example, if you select the descriptive graphics menu, you have access to many graph operations.
## The Toolbar ##
The toolbar contains common functions that are likely needed when doing extensive data analysis. The user can create, open, and save .marla problem files, add a data set to the existing problem, increase or decrease font size, abbreviate text to fit more into the workspace, or open the setting dialog.
## The Main Workspace ##
The main workspace is the blank slate that can contain the user defined data sets for the problem and the related operations. A simple drag and drop interface allows for easy use and an undo/redo function is provided to under the _Edit_ menu.


# Working With a Data Set #
maRla provides an easy interface to work with data sets. Gone are the days of working your problem in R and having to make sure you did all the steps right to get to a working PDF file from Sweave.

## Adding and Removing Data Sets ##
Data sets are stored on the right hand side, in the sidebar. If you have deleted a data set in the main workspace, you may retrieve it again from the sidebar later. This storage is persistent for the problem. This way, your main workspace can contain exactly what you need, when you need it. However, if you accidentally drag your data set to the trash bin in the bottom of the workspace (which will take the data set out of the workspace), you can simply undo or redo by going to the _Edit_ menu selection. To add another data set, simply select the silver cylinder in the toolbar, which will open a dialog and allow you to create a new data set for your problem.

## Adding and Removing Operations ##
Adding operations to the main workspace is easy. Once you have selected an operation to do (e.g. histogram), simply drag and drop it over to the data set you would like to associate it with. Operations can also be dragged and dropped onto other operations. This means that whatever the results of the previous operation are, they can be used by any children operations as a parameter. Operations can be deleted by dragging the operation to the trash bin in the bottom right. The undo feature, as mentioned above, will allow this to be undone. The student can add as many branches as is necessary to meet the problem requirements.
## Showing Problem Solutions ##
To perform the operation on a given set of data, right-click on the the operation and select "Solution". Be sure to set whatever the proper parameters were for the given analysis before finishing. Graphics will load on request as well.
## Changing Operation Parameters ##
To change operation parameters, right-click on the operation and select "Change Parameters". Then change the parameters as you require. In order to actually display the analysis, simply right-click again and select "Solution" once more.
## Assigning Sub Problems ##
To assign sub problems, right-click on an operation and hover over "Tie to Sub Problem", and select the appropriate sub problem. The colors that have been selected for the sub problems will display properly in the operation structure. When exporting to a PDF, the document will be arranged according to user assignments.
## Export Data Sets ##
To export the values of a data set to a CSV file, see [the exporting section on exporting data sets](#Data_Sets.md).

# Exporting #
maRla is capable of exporting a problem to two different formats, LaTeX and PDF. Data sets are able to be exported to the CSV (comma separated value) format.
## LaTeX ##
The LaTeX document is a .Rnw file which contains the executable R code to be processed by the Sweave module in R. It is editable by a LaTeX editor. This is the intermediate form of the PDF that gets exported. To export to LaTeX, go to the _File_ menu and select "Export for LaTeX..."
## PDF ##
Problems can be exported to a PDF format that includes graphics and text for the problem and each sub problem. This feature can be accessed by going to the _File_ option in the menu bar. There you can select "Export to PDF..." Exporting to PDF will open a save dialog, create the PDF file, and subsequently open it. This file contains the appropriately labeled details as outlined in the problem details and sub problem assignments as arranged in the main workspace.
## Data Sets ##
maRla is able to export any one of the data sets in a problem to an external file. Similar to pdf and LaTeX exporting, go to the _File_ selection in the menu and select "Export to CSV..."

[Back to Help Contents](HelpContents.md)