[Back to Help Contents](HelpContents.md)



# Introduction #
The LaTeX export template format is essentially a LaTeX document with markers sprinkled throughout to designate where content should be substituted. The [example at the bottom](#Example.md) is a good way to get an overview quickly.

The root element is a `<template />` that takes no attributes. From there any of the following elements may be included:

# Elements #
## Creator Name ##
```
<name />
```
Name of the creator of the document.

## Class Name ##
```
<class type="short|long" />
```
Name of the class this problem is for. Classes may be given either short names (eg _MA2510_) or long names (eg, _Probability and Statistics_).

Supported attributes:
  * **`type`** - Specifies the type of class name to substitute in. May be `short` or `long`

## Book Information ##
### Chapter ###
```
<chapter />
```
Chapter this problem comes from.

### Section ###
```
<section />
```
Chapter section this problem comes from.

### Problem Number ###
```
<probnum />
```
This problem's problem number in the section/chapter.

## Problem Statement ##
```
<statement />
```
Description of the problem from the book, as entered in the problem wizard. If done outside a loop (see [Loops](#Loops.md)), this gives the main problem statement. If done inside a loop, returns the current subproblem's problem statement.

## Data ##
```
<data type="start|end" />
```
Outputs the specified data as a formatted LaTeX table. If done outside a loop, this will display all starting datasets in a problem (if `type="start"`) or all data at the ends of processing (if `type="end"`). If done in a loop, the same thing occurs, only restricted to the space denoted by the start and end of the solution.

Supported attributes:
  * **`type`** - Whether to show the starting or ending data. May be `start` or `end`

## Solution Steps ##
```
<solution />
```
R code to solve the problem. If done outside a loop (see [Loops](#Loops.md)), this summarizes every operation in the problem. If done inside a loop, summarizes only those operations denoted as in the "solution" to the current subproblem. Between each operation any associated remarks are interleaved.

## Problem Conclusions ##
```
<conclusion />
```
Outputs the user-entered conclusion to a sequence of solution steps. If done outside a loop, this will display the problem conclusion. If done in a loop, the conclusion for the current subproblem is output.

Supported attributes:
  * **`type`** - Whether to show the starting or ending data. May be `start` or `end`

## Loops ##
```
<loop type="subproblem">...</loop>
```
Loops over each of the subproblems. Allows their individual statements and solutions to be examined. See [Problem Statement](#Problem_Statement.md) and [Solution Steps](#Solution_Steps.md).

## Conditionals ##
```
<if [has_subproblems="true|false"] [has_conclsion="true|false"]>
  <then>...</then>
  <else>...</else>
</if>
```

Allows parts of the template to be used only conditionally. If the expression in the `<if>` is true, then the LaTeX template within the `<then>` block is processed. If it is false, then the `<else>` block is used. Either block may be left off if not needed.

Attributes supported:
  * `has_subproblems` - Checks if the problem being exported contains subproblems. May be `true` or `false`
  * `has_conclusion` - Checks if the problem or current subproblem (depending on if in a loop or not) contains a conclusion. May be `true` or `false`

# Example #
A complete, working example may be viewed at [export\_template.xml](http://code.google.com/p/marla/source/browse/trunk/maRlaIDE/test/export_template.xml)

[Back to Help Contents](HelpContents.md)