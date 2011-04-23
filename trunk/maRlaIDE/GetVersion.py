#!/usr/bin/env python

# Import system modules
import os, sys, datetime

## Retrieves the current version number from Domain.javaW
#
# @author Alex Laird
# @date 04/22/11
#
# @file GetVersion.py
# @version 1.0

## The relative path to the directories
REL_SRC = "src" + os.sep

##
# Retrieve the version number from the Domain.java file.
#
# @param domainSrc The Domain.java file.
def getVersionNumber(domainSrc):
    # Open a stream to the file
    fileStream = open (domainSrc, "r")
    verNum = ""
    
    for line in fileStream:
        if line.find ("final String VERSION =") != -1:
            section = line.split("=")[1]
            verNum = section[2:len(section) - 3]

    # Close the file stream
    fileStream.close ()
    
    return verNum
    
def getPreRelease(domainSrc):
    # Open a stream to the file
    fileStream = open (domainSrc, "r")
    verPre = ""
    
    for line in fileStream:
        if line.find ("final String PRE_RELEASE =") != -1:
            section = line.split("=")[1]
            verPre = section[2:len(section) - 3]

    # Close the file stream
    fileStream.close ()
    
    if len(verPre) > 0:
        return " " + verPre
    else:
        return ""

## Calls respective helper methods to complete overall task of ensuring the
# version numbers are validate where needed.
#
# @param args The command-line arguments.
def main(args):
    if len(args) < 2:
        print ("No arguments found.\nPass this script a 0 to retrieve the ones digit, 1 to retrieve the tenths digit, or 2 to retrieve the hundreths digit, 3 to retrieve pre-release version string.")
        return
    
    # Get the absolute path of the Get Organized directory directory
    marlaDir = os.path.abspath(args[0])[:os.path.abspath(args[0]).rfind(os.sep)] + os.sep
    # Setup paths to source files
    domainSrc = marlaDir + REL_SRC + "marla" + os.sep + "ide" + os.sep + "gui" + os.sep + "Domain.java"
    # Ensure all source files actually exist
    if os.path.exists (domainSrc):
        if args[1] == "0":
            verNum = getVersionNumber(domainSrc)
            return verNum[:verNum.find(".")]
        elif args[1] == "1":
            verNum = getVersionNumber(domainSrc)
            return verNum[verNum.find(".") + 1:verNum.find(".") + 2]
        elif args[1] == "2":
            verNum = getVersionNumber(domainSrc)
            return verNum[verNum.find(".") + 2:]
        elif args[1] == "3":
            verPre = getPreRelease(domainSrc)
            sys.stdout.write (verPre)
            return ""
        else:
            print ("The argument specified is not a valid selection.")
    else:
        print ("The directory structure on your system does not match that required by this script.")

## This is the entry point for the program, which figures off the main method to
# perform operations.
if __name__ == "__main__":
    sys.exit(main(sys.argv))
    
