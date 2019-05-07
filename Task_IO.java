package be.intecbrussel;

import java.nio.file.*;
import java.nio.charset.*;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.*;


public class Task_IO {
    /**
     * path of folder with resources
     */
    static Path resources = Paths.get("C:\\Data");
    /**
     * paths of folders, derived from resource path
     */
    static Path unsortedFolder = resources.resolve("unsortedFolder");
    static Path sortedFolder = resources.resolve("sortedFolder");
    static Path summary = sortedFolder.resolve("summary");
    static Path summaryTxt = summary.resolve("summary.txt");
    /**
     * List<String> to hold text for summary
     */
    static List<String> summaryList = new ArrayList<>();
    static Scanner sc;
    /**
     * bool flags used when user is asked to overwrite files.
     */
    static boolean overwriteAll = false, overwriteNone = false;

    public static void main(String... args) {

        sc = new Scanner(System.in);
        // get files and subfolders in sortedFolder
        File[] files = new File(String.valueOf(unsortedFolder)).listFiles();

        copyFilesToExtensionFolders(files);

        // get subfolders in sortedFolder
        files = new File(String.valueOf(sortedFolder)).listFiles();

        // add first sentence
        summaryList.add("name                |      readable       |      writeable      |\n");

        createSummary(files); // first fill list summaryList with strings, it doesn't write it to disk yet, because a write operation is more efficient in one block
        try {
            Files.createDirectories(summary); // we should check for a file with name summary (which would throw an exception), but we're too lazy  ;-)
            for (var s : summaryList)
                System.out.println(s);
            // now write to disk
            Files.write(summaryTxt, summaryList, Charset.forName("UTF-8"), StandardOpenOption.CREATE); // CREATE, must be overwritten when it exists
        } catch (IOException se) {
            System.out.println("Problem with creating file "+summaryTxt);
            se.printStackTrace();
        }
    }

    /**
     * recursive method. copy files of array of passed files and the ones in subfolders to folders with name of their extension
     * @param files
     */
    public static void copyFilesToExtensionFolders(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println("Directory: " + file.getName());
                copyFilesToExtensionFolders(file.listFiles()); // recursive
            } else { // it's a file
                //System.out.print("File: " + file.getName() + " is " + (Files.isWritable(file.toPath()) ? "" : "not") + " writable");
                //System.out.print(" -----  hidden ? " + file.isHidden());
                String extension = "";
                // extract extension
                int i = file.getName().lastIndexOf('.');
                if (i > 0) {
                    extension = file.getName().substring(i + 1); // omit the '.'
                } else // empty name (for example, the .gitignore file)
                    extension = file.getName().substring(1); // 1 in substring(1) because . must be omitted

                Path newPath = sortedFolder.resolve(extension); // path of folder where file has to go
                //sortedFolder
                Path newFile = Paths.get(""); // initialize here to access it in catch block
                try {
                    // create the dir
                    File newDir = new File(String.valueOf(newPath));
                    if ( ! newDir.exists()) { // dir doesn't exist yet, so let's create it
                        Files.createDirectories(newPath);
                        System.out.println("Directory "+String.valueOf(newPath)+" created");
                    }
                    else System.out.println("Directory "+String.valueOf(newPath)+" already exists");

                    if ( ! newDir.isFile() ) { // exists, check if it's a file
                        // copy file to sortedFolder
                        newFile = newPath.resolve(file.getName());

                        if ((new File(String.valueOf(newFile))).exists()) { // file exists in target folder, ask user whether to overwrite it
                            boolean overwrite = false;
                            if (!overwriteAll && !overwriteNone) { // neither of both booleans have been set to true yet
                                System.out.println("File "+String.valueOf(newFile)+" exists, Do you want to overwrite ? No, Yes, Yes to all, No to all");
                                boolean validInput = false;
                                while (!validInput) { // loop until input is valid
                                    validInput = true;
                                    String input = sc.nextLine();
                                    switch (input) {
                                        case "No":
                                            break;
                                        case "Yes":
                                            overwrite = true;
                                            break;
                                        case "Yes to all":
                                            overwriteAll = true;
                                            break;
                                        case "No to all":
                                            overwriteNone = true;
                                            break;
                                        default:
                                            validInput = false;
                                            System.out.println("Wrong answer. File exists, Do you want to overwrite ? No, Yes, Yes to all, No to all");
                                    }
                                }
                            }
                            if (overwrite || overwriteAll) {
                                Files.copy(file.toPath(), newFile, StandardCopyOption.REPLACE_EXISTING);
                                System.out.println(String.valueOf(newFile) + " overwritten");
                            }
                        } else { // file doesn't exist, ordinary copy operation
                            Files.copy(file.toPath(), newFile, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("File "+String.valueOf(newFile) + " written");
                        }
                   }
                    // dir wasn't created, there is already a file with that name
                   else System.out.println("Directory "+String.valueOf(newPath)+" can't be created, as there's already a file with that name");

                } catch (IOException se) {
                    System.out.println("Problem with creating dir "+String.valueOf(newPath)+" or with copying the file "+String.valueOf(newFile));
                    se.printStackTrace();
                }
            }
        }

    }

    /**
     * recursive method , creates summary of files in list summaryList
     * @param files
     */
    public static void createSummary(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) { // var file is a directory
                if (!file.getPath().equals(String.valueOf(summary))) { // ignore the folder summary
                    //System.out.println("Directory: " + file.getName());
                    summaryList.add("\n-----");
                    summaryList.add(file.getName() + ":");
                    summaryList.add("-----");
                    createSummary(file.listFiles()); // Calls same method again.
                }

            } else { // var file is a file
                summaryList.add(String.format("%-20s|%11c          |%11c          |", file.getName(),
                        (Files.isReadable(file.toPath()) ? 'x' : '/'),
                        (Files.isWritable(file.toPath()) ? 'x' : '/')
                ));
            }
        }
    }

}
