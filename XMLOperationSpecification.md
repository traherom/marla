[Back to Help Contents](HelpContents.md)



# maRla Operation Editor #
A tool called the maRla Operation Editor is included in The maRla Project. If you would like to add, edit, or remove operations, it is easiest to do so with this tool. The documentation below is for editing the inner XML of each operation, which is the XML shown in the maRla Operation Editor when a particular operation is created or selected for editing.

# Getting Started #
Within maRla, most operations are defined by one or more operation XML files. The "primary" XML file (see [ops.xml](http://code.google.com/p/marla/source/browse/trunk/maRlaIDE/test/ops.xml) in the repository) should be consulted as a guide to creating your own operations in addition to the documentation on this page.

A basic operation XML file with a single operation looks like this:
```
<?xml version="1.0" ?>
<operations>
  <operation name="boring">
    <computation>
      <cmd>print("yawn")</cmd>
    </computation>
  </operation>
</operations>
```

From there, additional [`&lt;operation /&gt;`](#Operation.md) elements may be placed inside. Each `operation` is stand-alone, unaffected by other `operations` within the same or other files.

In the discussion below, attributes given in bold are required.

**NOTE:** In general, users should not directly edit the `ops.xml` file distributed with maRla, as an upgrade will overwrite your changes without warning. Instead, save your new operations as a separate XML file and then set them as User Operations XML in maRla's [settings](SettingsHelp.md). If you want to override an existing operation, name it the same thing as the current one.

# Operation #
```
<operation name="operation name" [list="true|false"]>
```
Each `<operation />` takes a name that will be displayed to the user and can be used to
create that operation. If more than one operation with the same name exists, later ones overwrite earlier ones. If an operation should not appear on the side panel, set `list="false"`.

Attributes supported:
  * **`name`** - Arbitrary name of the operation. Must be unique within the XML file
  * `list` - Specifies if this operation is listed under normal displays. Maybe to set to `true` or `false`. Default is `true`.

## Categories ##
```
<category>Name of category</category>
```
An operation may fall into one or more categories. This allows maRla to organize the palette a bit more and make finding a given operation quicker. To specify which categories on operation falls into, use the `category` element. Zero or may be given.

A `category` uses whatever text is given in the element as the name of the category. This string is arbitrary. If multiple operations have the same category name, they will be placed together..

## Information Prompts ##
```
<query type="column|checkbox|string|numeric|combo" [column-type="all|numeric|string"] name="internal name" prompt="User prompt" />
```

Inside of an operation there can be multiple `<query />` elements. These queries will be presented
to the user with the given `prompt` when they add the operation.

Attributes supported:
  * **`name`** - A unique name to identify the query by, may not be used by any other `query` in the operation.
  * **`prompt`** - Any arbitrary string to display to the user.
  * **`type`** - One of the following:
    * `checkbox` - Simple checkbox that the user may select
    * `combo` - Operation-specified selectable list. See [Combo](#Combo.md) for detailed information
    * `column` - Presents the user with a list of the parent columns. `column-type` must be given to specify which columns to display.
    * `fixed` - Query with a single answer. Intened for user informational purposes (IE, so they know a parameter you will be using). Must specify a `value`
    * `numeric` - Arbitrary user-entered number. May specify a minimum or maximum through `min` and `max`, both of which are optional
    * `string` - Arbitrary user-entered string
  * `column-type` - One of the following:
    * `all` - All parent columns will be selectable
    * `numeric` - All numeric parent columns
    * `string` - All string parent columns
  * `min` - Minimum value for a numeric prompt
  * `max` - Maximum value for a numeric prompt
  * `value` - Required by `fixed` query types, specifies the answer

### Combo ###
A column type allows the selection of one of the columns in the parent data set and/or
operation. A combo requires that `<option />`s be specified. For example:
```
<query type="combo" name="test_type" prompt="Select the test to perform">
  <option>Two sample</option>
  <option>Paired</option>
</query>
```

The user's response will be returned back and saved under the name given.

## Dynamic Name ##
```
<displayname>Specification</displayname>
```
Operations may have a name which changes based on what it is actually doing. For example, an operation which adds a user-specified value to a given column might be called "Add" but be displayed as "ColName + 5." This behavior is specified by the `<displayname />` element.

The main `displayname` element takes no attributes. Inside it may have verbatim text--which will be copied as-is--or `<response />` elements. These may be intermixed freely as desired.

An example of the add operation discussed above:
```
<operation name="Add">
  <query type="column" name="col" prompt="Column?" />
  <query type="numeric" name="val" prompt="Add" />

  <displayname><response name="col" /> + <response name="val" /></displayname>

  <computation>
    <!-- ...operations to perform addition... -->
  </computation>
</operation>
```

### Response ###
```
<response name="query name" />
```
The query referenced by `name` will be copied into the location of this element. If the user has not yet provided an answer to the query, the name of the query will be used instead.

Supported attributes:
  * **`name`** - Name used for a query (see [Information Prompts](#Information_Prompts.md)). The text to this answer will be copied into the place of this element

## Computation ##
```
<computation>
  <cmd />
  <if />
  <loop />
  <set />
  <error />
  <save />
  <plot />
  <fake-plot />
</computation>
```
Inside each operation there must be a `<computation />` element. This section allows five types of elements which may be specified in any number of times and in any order. Elements will be executed in the order they are encountered.

### Commands ###
```
<cmd>R code</cmd>
```
The simplest computation element is the command. A `<cmd />` takes a single R command and executes
it. An exception will be thrown if more than one command is placed here. If a newline is needed
in a string then use `\n`, not a hard line break.

### Setting Variables From User Answers ###
```
<set name="query name" [rvar="R variable name"] [use="name|values"] />
```
A `<set />` element allows for an R variable to be set from one of the user-prompted
query values.

Attributes supported:
  * **`name`** - Name specified for the desired query element (see [Information Prompts](#Information_Prompts.md)).
  * `rvar` - Specifies the R variable name to set. If not given, an R variable with the same name as `name` is used.
  * `use` - For `column` queries, the R variable may be set with either the string name of the column selected or the actual values in that column. This attribute specifies which processing method to use.
    * `name` - Use the column name
    * `values` - Use the column values

For example, to ask a user to select a column and then save the values for use:
```
<operation>
  <query type="column" name="selected_column" prompt="Select a column" />
  <computation>
    <set rvar="col" name="selected_column" use="values" />
  <computation>
</operation>
```

### Loops ###
```
<loop type="parent|numeric|string" [index-var="R variable name"] [key-var="R variable name"] [value-var="R variable name"] [loop-var="R expression"]>
  ...
</loop>
```
`<loop />` elements allow repetition over parent data or arbitrary lists. A loop may use any of the other elements inside itself, including other loops.

Attributes supported:
  * **`type`** - One of the following:
    * `parent` - Loop over every column in the parent data
    * `numeric` - Iterate over every element of the vector given by `loop-var`, processing them as numeric values
    * `string` - Iterate over every element of the vector given by `loopVar`, processing them as string values
  * `loop-var` - If using `numeric` or `string` loop types, this specifies an R statement that generates the vector to loop over
  * `index-var` - Loop number the loop is on (1 based)
  * `key-var` - For `parent` loops, gives the name of the current column. For array loops it is the same as indexVar
  * `value-var` - For `parent` loops, gives the values in the current column. For array loops it gives the current value in the array

### Conditionals ###
```
<if [expr="R expression"] [vartype="type"] [rvar="R variable"] [colexists="column name"]>
  <then>...</then>
  <else>...</else>
</if>
```
Conditional work is supported through the `<if />` element. If the condition specified is true, then commands in a `<then />` block are executed. If it is false, then commands in the `<else />` block are executed. Either block may be left out if not needed.

Supported attributes:
  * `vartype`. Makes the if check if the given `rvar` (see below) is of the given type. The type is one of the following:
    * `string` - String or multiple strings of data
    * `numeric` - Number or multiple numbers
  * `rvar` - Gives the R variable which should have its type examined
  * `expr` - R expression to evaluate. Must evaluate to a single TRUE or FALSE value and be only a single statement. If more complexity is needed, save the result to a variable and give that as the expression.
  * `colexists` - Evaluates to true if a column with the given name exists

### Error Messages ###
```
<error msg="Message to display to user" />
```
The `error` element allows an operation to stop its own execution and present an error message to the user. For example, an operation might have additional requirements on query answers beyond what could be specified in the `query` element.

Attributes supported:
  * **`msg`** - Arbitrary message to present to the user that explains the error

Example:

```
<query type="numeric" name="i" prompt="Var 1" />
<query type="numeric" name="j" prompt="Var 2" />

<computation>
  <set name="i" rvar="i" />
  <set name="j" rvar="j" />

  <if type="expr" expr="i &lt;= j">
    <then>
      <error msg="Var 1 must be greater than Var 2" />
    </then>
  </if>
</computation>
```

### Library Loading ###
```
<load [library="library"] [r-library="R variable"] />
```
Loads the given library into R. If the library is not installed an attempt is made to install it. This should be used in
preference to something like `<cmd>library(<lib>)</cmd>`.

Attributes supported:
  * `library` - Verbatim name of the library to load
  * `r-library` - R variable that contains the name of the library to load

### Saving Results ###
```
<save [type="numeric|string|auto"] [column="Column name"] [r-column="R command"]>R command</save>
```
To save the final values for the computation, `<save />` elements may be specified. The value saved is given by the single R command inside the save element.

Attributes supported:
  * **`type`** - One of the following. Defaults to `auto`
    * `numeric` - Process the result as one or more numeric values
    * `string` - Process the result as one or more string values
    * `auto` - Automatically chooses the correct column type. Attempts numeric first, then string
  * `column` - Static name of the column to save these values into
  * `r-column` - R command from which to pull the name of the column to save into. Must return a single string

For example:
```
<comuptation>
  <cmd>testing = 75.6</cmd>
  <save type="numeric" column="ex">testing</save>
</computation>
```

### Plots ###
```
<plot>...</plot>
```
A single `<plot />` or ['&lt;fake-plot /&gt;`](#Fake_Plots.md) element is allowed in an operation. A plot creates a new device and commands may be placed inside may draw onto it. Commands should not manually change the device or close it, that will be taken care of by the plot. A plot takes no attributes but allows any of the other elements to be place inside it, just as with the main computation section.

### Fake Plots ###
```
<fake-plot>R command</fake-plot>
```
A single [`&lt;plot /&gt;`](#Plots.md) or `<fake-plot />` element is allowed in an operation. A fake plot is intended for commands which do not produce a true plot yet are not amendable to being saved into standard columns. For example, a stem-and-leaf plot in R is produced as text. By wrapping the call to `stem()` in a `fake-plot`, this text can be saved as a graph.

Only a single command by be given to a `fake-plot`. If a more complex "plot" is needed, save it into a string variable and use that as the command.

# Example #
Good, working examples of operations are available at [ops.xml](http://code.google.com/p/marla/source/browse/trunk/maRlaIDE/test/ops.xml) in the repository.

[Back to Help Contents](HelpContents.md)