Getting Started for Passerelle-related dvp
==========================================

This file is a paradox, it explains how to get started to obtain a working
development set-up for Passerelle-related development.
But in order to get this file, you should already have executed the first 3 steps! ;-)
 
DVP PREPARATION
1. Create a new eclipse workspace. 
   Ensure a JDK 1.5 or 1.6 is used as Java runtime.
   Switch off automatic building on workspace changes (in Window>Preferences>General>Workspace).
2. Define Subversion repository https://svn.codehaus.org/passerelle.
3. Check out this project!

The next steps depend on the type of Passerelle-related development you need to perform.
For each type, you need to set the corresponding OSGi/PDE target platform, 
and then check out a limited nr of projects into your workspace.

Below, to check out the projects for a team project set : do Import context menu > Team > team project set
and select the mentioned file.
To set a PDE dvp target : double-click on the target file and then click on the "Set as target platform" link on top right.

Passerelle dvp
--------------------   
4. Check out the projects using the included team project set passerelle-dev-projectset.psf
5. From the project com.isencia.passerelle.target, set the passerelle-dev-target.target as PDE target platform
6. Switch on automatic building. There should be no compilation errors.

Passerelle HMI dvp
---------------------   
TBD
