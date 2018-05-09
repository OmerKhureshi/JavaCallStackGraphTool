# Call Stack Graph Tool
This tool generate a tree graph for the call stack using the information in log files for you code. A specific format of log file is required for the tool to parse and process the logs. Please see _**How to generate Log files for you code**_ section below to generate log files for your own code.  
### Instruction to run tool using IntelliJ:
1. Download or clone the repository.
2. In IntelliJ, create a new project and choose `Create Project from Existing Sources...`. Choose the cloned repository to import all the files.

      Or.

    Right click pom.xml file and click open with then choose IntelliJ.
3. Ensure that this project is recognized as a Maven project inside IntelliJ and all the dependencies are downloaded.
6. Run the Main java class at `<project-root>/src/main/java/com/csgt/Main.java`.

### How to use the tool?
#### For first time use:
For the first time, the tool requires the log files that should be used to generate the graph.
1. Click `File -> Select Method Definition log file` (⎇ + m).
2. Click `File -> Select Call Trace file` (⎇ + c).
3. Click `Run -> Run` (⎇ + r).
4. To start over, click `Run -> Reset` (⎇ + ⇧ + r) and redo steps 1 to 3 above.

#### For consecutive uses:
The tool uses an embedded database to store the parsed information from the log files. Therefore for consecutive uses of the same log files, you can point to the previously generated database to generate the graph without processing the log files. The database file can be found at `<project-root>/Databases`. The file is suffixed with the time stamp.
1. Click `File ->  Load existing database` (⎇ + d).
2. Click ` Run -> Run` (⎇ + r)
3. To start over, click `Run -> Reset` (⎇ + ⇧ + r).

### How to generate Log files for your code?
Call Trace and Method Definition log files can be generated by following the instructions [here](https://github.com/omersalar/LogWeaver).

