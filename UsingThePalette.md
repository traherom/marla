[Back to Help Contents](HelpContents.md)



# Introduction #
The operations palette is the categorized list of operations on the right-hand side of the maRla IDE. These operations can interact with data in the workspace, and they where the core functionality of the maRla IDE resides.

This list of operations is dynamically generated from an XML file and can be appended to if common operations are missing.

# Expanding/Collapsing Categories #
To prevent clutter, operations are categories, and all categories are collapsed by default. Simply click on the category heading (or the plus/minus symbol) to expand/collapse the category.

# Adding from the Palette #
To add an operation to the main workspace from the palette, simply click and drag that operation from the palette into the main workspace. Drop the operation in the main workspace.

# Customizing the Palette #
Operations in the palette can be customized. If an operation in the palette is not complete, a new, more complete operation can be made to overwrite it. If operations are missing from the palette, they can be added.

Since the palette is dynamically generated, changes to the operations XML file are immediate. To update the operations XML file, or create a new one, use the maRla Operation Editor, which can be launched from the "Tools" menu.

## User Operations Files in maRla ##
A user can use their own operations file in the maRla IDE, which will append any operations in the user operations file into the palette with the existing operations. To add new operations from a user operations XML file, specify a path to a file (or files) in the [maRla configuration](ConfiguringMarla#Preferences.md) (see User Operations XML).

### New Operations ###
To add new operations to the palette, create a new user.xml file in the maRla Operation Editor. See the documentation that goes into creating your own operation [here](XMLOperationSpecification.md).

It's important to understand that giving adding an operation into a category that already exists in the primary operations fill will place that operation into that category, and specifying a category name that doesn't already exist will create a new category.

If an operation is given a name that already exists in the primary operations file or another user operations file that is being used, the last operation found with that name will be the only one displayed in the palette.

### Overwriting Exiting Operation ###
If, in your newly created user.xml file in the maRla Operation Editor, you specify a name for an operation that already exists, your operation will overwrite any other operations when displayed in the palette. This allows you to customize a default operation more, if you would like to, or overwrite a broken operation if necessary.