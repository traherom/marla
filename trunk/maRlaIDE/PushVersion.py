#!/usr/bin/env python

# Import system modules
import getpass, os, sys

# Import my modules
import GoogleCodeUpload, GetVersion

## Push the latest version of maRla to the Google Code server.
#
# @author Alex Laird
# @date 04/22/11
#
# @file PushVersion.py
# @version 1.1

## The relative path to directories
REL_SRC = "src" + os.sep
REL_STORE = "store" + os.sep

## Push the latest installer to the Google Code server.
#
def googleCode(verNum, verPre, windowsSetupFile, zipFile):
    #Established a connection with the Google Code server
    username = raw_input("\nUsername: ")
    password = getpass.getpass()
    try:
        print ("\n::UPLOADS BEGINNING FOR v" + str(verNum) + verPre + "::")
        print ("::DO NOT TERMINATE UNTIL COMPLETE::")
        totalSuccess = True
        project = "marla"
        
        print ("Uploading Windows installation (" + str(round(float(os.path.getsize(windowsSetupFile)) / float(1048576), 1)) + "MB)...")
        code = GoogleCodeUpload.upload(windowsSetupFile, project, username, password, "The maRla Project " + str(verNum) + verPre + " for Windows", "The Windows installer for The maRla Project, a statistics IDE developed for students to help them work through and solve complicated statistical calculations. The maRla Project requires the R statistical package as well as LaTeX to run, though this Windows installer will automatically download, install, and configure these programs for you if you do not already have them. The maRla Project requires the Java Runtime Environment (JRE) to run, though this installer will download and install the JRE for you if you do not already have it.", ["Featured", "OpSys-Windows", "Type-Installer"])[0]
        if code != 201:
            totalSuccess = False
            print ("The upload may not have succeeded, error code " + str(code) + ".  Either there was a miscommunication with the server, or the given filename already exists on Google Code.")
                
        print ("Uploading cross-platform archive (" + str(round(float(os.path.getsize(zipFile)) / float(1048576), 1)) + "MB)...")
        code = GoogleCodeUpload.upload(zipFile, project, username, password, "The maRla Project " + str(verNum) + verPre + " for any operating system", "The cross-platform version of The maRla Project, a statistics IDE developed for students to help them work through and solve complicated statistical calculations. The maRla Project requires the R statistical package as well as LaTeX to run. This version will run on Windows, Mac, Linux, and any other operation system that supports Java.", ["Featured", "OpSys-All", "Type-Archive"])[0]
        if code != 201:
            totalSuccess = False
            print ("The upload may not have succeeded, error code " + str(code) + ".  Either there was a miscommunication with the server, or the given filename already exists on Google Code.")
        
        if totalSuccess:
            print ("::UPLOADS COMPLETE::")
        
            print ("\nThe maRla Project was successfully updated to v" + str(verNum) + verPre + " on Google Code.")
            print ("You will still need to update the revision file on the Wiki so user's are informed of this update.")
        else:
            print ("::UPLOADS ATTEMPTED::")
            
            print ("\nOne or more of the uploads encountered problems. Verify that all files are up to date on the Google Code Downloads page.")
    except Exception as e:
        print (type(e))
        print (e.args)
        print (e)
        print ("\nAn unknown error occured. Try running the push again. If this error persists, it is recommended that you ensure the validity of all version files and installers on Google Code immediately.")

## Calls respective helper methods to complete overall task of ensuring the
# version numbers are validate where needed.
#
# @param args The command-line arguments.
def main(args):
    marlaDir = os.path.abspath(args[0])[:os.path.abspath(args[0]).rfind(os.sep)] + os.sep
    domainSrc = marlaDir + REL_SRC + "marla" + os.sep + "ide" + os.sep + "gui" + os.sep + "Domain.java"
    storeDir = marlaDir + REL_STORE
    
    if os.path.exists(storeDir):
        # Grab the current version number we need to push to the server
        verNum = GetVersion.getVersionNumber(domainSrc)
        verPre = GetVersion.getPreRelease(domainSrc)
        
        windowsSetupFile = storeDir + "The maRla Project Setup " + verNum + verPre + ".exe"
        zipFile = storeDir + "The maRla Project " + verNum + verPre + ".zip"
        
        if os.path.exists(windowsSetupFile) and os.path.exists(zipFile):
            print ("::ALL NECESSARY FILES IN PLACE::")
            print ("Ensure that setup files in place are the LATEST build--this script will not do a build for you, it will only upload the files that already exist.")
                        
            answer = raw_input ("\nWould you like to update the files and installers on Google Code (y/n)? ")
            if "yes" in answer.lower() or answer.lower() == "y":
                googleCode(verNum, verPre, windowsSetupFile, zipFile)
                
            print ("")
    else:
        print ("The directory structure on your system does not match that required by this script.\nEnsure The maRla Project has been built to the latest version and all installation files are in place.")
        print ("")
    
## This is the entry point for the program, which figures off the main method to
# perform operations.
if __name__ == "__main__":
    sys.exit(main(sys.argv))
    
