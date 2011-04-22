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
        print ("\n::UPLOADS BEGINNING::")
        print ("::DO NOT TERMINATE UNTIL COMPLETE::")
        totalSuccess = True
        project = "marla"
        description = ""
        
        print ("Uploading Windows installation (" + str(round(float(os.path.getsize(windowsSetupFile)) / float(1048576), 1)) + "MB)...")
        code = GoogleCodeUpload.upload(windowsSetupWithVer, project, username, password, "Get Organized " + verNum + " for Windows", description, ["Featured", "OpSys-Windows", "Type-Installer"])[0]
        if code != 201:
            totalSuccess = False
            print ("The upload did not succeed.  Either there was a miscommunication with the server, or the given filename already exists on Google Code.")
                
        print ("Uploading cross-platform archive (" + str(round(float(os.path.getsize(zipFile)) / float(1048576), 1)) + "MB)...")
        code = GoogleCodeUpload.upload(zipWithVer, project, username, password, "Get Organized " + verNum + " Portable", description, ["Featured", "OpSys-All", "Type-Archive"])
        if code != 201:
            totalSuccess = False
            print ("The upload did not succeed.  Either there was a miscommunication with the server, or the given filename already exists on Google Code.")
        
        if totalSuccess:
            print ("::UPLOADS COMPLETE::")
        
            print ("The maRla Project was successfully updated to v" + verNum + verPre + " on Google Code")
        else:
            print ("::UPLOADS ATTEMPTED::")
            
            print ("One or more of the uploads encountered problems. Verify that all files are up to date on the Google Code Downloads page.")
    except:
        print ("An unknown error occured. Try running the push again. If this error persists, it is recommended that you ensure the validity of all version files and installers on Google Code immediately.")

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
                        
            answer = raw_input ("\nWould you like to update the files and installers on Google Code? ")
            if "yes" in answer.lower() or answer.lower() == "y":
                googleCode(verNum, verPre, windowsSetupFile, zipFile)
                
            print ("")
    else:
        print ("The directory structure on your system does not match that required by this script.\nEnsure Get Organized has been built to the latest version and all portable and installation files are in place.")
        print ("")
    
## This is the entry point for the program, which figures off the main method to
# perform operations.
if __name__ == "__main__":
    sys.exit(main(sys.argv))
    
