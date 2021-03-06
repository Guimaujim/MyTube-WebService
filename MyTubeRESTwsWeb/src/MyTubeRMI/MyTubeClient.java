package MyTubeRMI;

import java.rmi.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Scanner;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

import classData.fileData;

public class MyTubeClient {

    public static void main(String args[]) throws java.net.UnknownHostException {
        MyTubeClient client = new MyTubeClient();
        File directory = new File("ClientMem"); //Directory where clients will save their files
        directory.mkdir();
        Scanner reader = new Scanner(System.in);
        String port;
        String IP;
        String input;
        String server_id;

        System.out.println("Enter the IP of the server:");
        IP = reader.nextLine();
        System.out.println("Enter the port of the server:");
        port = reader.nextLine();
        System.out.println("Enter the id for the server:");
        server_id = reader.nextLine();
        server_id = server_id.replaceAll("\\s", "_");
        System.out.println("Enter the IP of your client:");
        String clientIP = reader.nextLine();
        System.setProperty("java.rmi.server.hostname", clientIP); //Set so callbacks can work properly

        try {
            String registryURL = "rmi://" + IP + ":" + port + "/mytube/" + server_id;
            MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
            CallbackInterface callbackObj = new CallbackImpl();
            i.addCallback(callbackObj); //Client adds to callback list

            while (true) {
                System.out.println("What do you want to do?");
                System.out.println("do = download | u = upload | f = find | l = list | de = delete | e = exit");
                input = reader.nextLine();
                //Client waits for user's to decide what to do

                if ("do".equals(input) || "download".equals(input)) {
                    clientDownload(i, callbackObj, server_id);
                } else if ("u".equals(input) || "upload".equals(input)) {
                    clientUpload(i, server_id);
                    System.out.println("Upload completed!");
                } else if ("f".equals(input) || "find".equals(input)) {
                    clientFind(i);
                } else if ("l".equals(input) || "list".equals(input)) {
                    System.out.println("You currently have this files on your system:");
                    clientList("ClientMem");
                } else if ("de".equals(input) || "delete".equals(input)) {
                    clientDelete(i);
                } else if ("e".equals(input) || "exit".equals(input)) {
                    i.unregisterForCallback(callbackObj); //Clients is removed from callback list
                    System.exit(0);
                } else {
                    System.out.println("Sorry, I couldn't understand that, please try again");
                }
            }
        } catch (Exception e) {
            System.out.println("Error! " + e);
        }
    }

    //Downloads file from server to client
    public static void clientDownload(MyTubeInterface i, CallbackInterface c, String server_id) throws RemoteException {
        String name = "";
        String description = "";
        String path = "";
        int in;
        byte[] file;
        Boolean option = false;
        Scanner reader = new Scanner(System.in);

        System.out.println("Do you want to download the file by name, by description, both or by key? Use n for name and d for description");
        System.out.println("Use n for name, d for description, b for both or k for key");

        while (option == false) {
            name = reader.nextLine();
            if (name.equals("d") || name.equals("description")) {
                option = true;
            } else if (name.equals("n") || name.equals("name")) {
                option = true;
            } else if (name.equals("k") || name.equals("key")) {
                option = true;
            } else if (name.equals("b") || name.equals("both")) {
                option = true;
            } else {
                System.out.println("Sorry, I couldn't understand that, please try again");
            }
        }

        if (name.equals("n") || name.equals("name")) {
            System.out.println("Please insert the name of the file you want to download:");
            name = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            path = "ClientMem/" + name;
            file = i.downloadName(name, c, server_id); //Client calls server to execute implementation's method to download the file by name
        } else if (name.equals("k") || name.equals("key")) {
            File keys = new File("ClientMem/keys");

            System.out.println("You currently have uploaded this files: ");

            try {
                BufferedReader br = new BufferedReader(new FileReader(keys));
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                br.close();
            } catch (Exception e) {
                System.out.println("Error!");
            }
            System.out.println("Please insert the key of the file you want to download:");
            name = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            path = "ClientMem/" + name;
            file = i.downloadKey(name, c, server_id); //Client calls server to execute implementation's method to download the file by key
        } else if (name.equals("b") || name.equals("both")) {
            System.out.println("Please insert the and the description of the file you want to download:");
            System.out.println("Name:");
            name = reader.nextLine();
            System.out.println("Description:");
            description = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            description = description.replaceAll("\\s", "_");
            file = i.downloadNameDescription(name, description, c, server_id); //Client calls server to execute implementation's method to download the file
            path = "ClientMem/" + name;										   //by name and description
        } else {
            System.out.println("Please insert the description of the file you want to download:");
            name = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            path = "ClientMem/" + name;
            file = i.downloadDescription(name, c, server_id); //Client calls server to execute implementation's method to download the file by description
        }

        if (file.length == 0) {
            System.out.println("File not found");
        } else {

            try {
                FileOutputStream FOS = new FileOutputStream(path);
                BufferedOutputStream Output = new BufferedOutputStream(FOS);
                Output.write(file, 0, file.length);
                Output.flush();
                Output.close();
                System.out.println("Download completed!");
                //Client flushes the byte array received into a file
            } catch (IOException e) {
                System.out.println("Error!" + e.getMessage());
            }
        }
    }

    //Auxiliary method so the client can find the file to upload
    public static String search(File[] Files, String path, String name) {
        String found = null;
        for (File e : Files) {
            if (e.isDirectory()) {
                File folder = new File(path + "/" + e.getName());
                found = search(folder.listFiles(), path + "/" + folder.getName(), name);
                if (found != null) {
                    return found;
                }
            } else if (e.isFile()) {
                if (e.getName().equalsIgnoreCase(name)) {
                    return path + "/" + name;
                }
            }
        }
        return found;
    }

    //Lists files from client
    public static void clientList(String Directory) {
        File folder = new File(Directory);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (!"keys".equals(listOfFiles[i].getName())) {
                    System.out.println(listOfFiles[i].getName());
                }
            } else if (listOfFiles[i].isDirectory()) {
                clientList(Directory + "/" + listOfFiles[i].getName());
            }
        }
    }

    //Uploads file from client to server
    public static void clientUpload(MyTubeInterface i, String server_id) throws RemoteException {
        String name;
        String key;
        String description;
        File keys = new File("ClientMem/keys");
        Scanner reader = new Scanner(System.in);

        System.out.println("Please insert the name of the file you want to upload:");
        name = reader.nextLine();

        System.out.println("Please insert a description for the file you want to upload:");
        description = reader.nextLine();

        name = name.replaceAll("\\s", "_");
        description = description.replaceAll("\\s", "_");

        File folder = new File("ClientMem");
        String path = "ClientMem";
        File[] directory = folder.listFiles();
        File userFile;
        path = search(directory, path, name);
        //Client searches for the file

        if (path != null) {
            userFile = new File(path);
            byte buffer[] = new byte[(int) userFile.length()]; //Client converts file into an array of bytes to be sent

            try {
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                input.read(buffer, 0, buffer.length);
                input.close();
                key = i.upload(buffer, name, server_id, description); //Client calls server to execute implementation's method to upload the file

                if (key != null) {
                    try {
                        Writer output = null;
                        output = new BufferedWriter(new FileWriter(keys, true));
                        output.write(key + ", " + name);
                        output.write(System.lineSeparator());
                        output.flush();
                        output.close();
                    } catch (Exception e) {
                        System.out.println("Error!");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error!");
            }
        } else {
            System.out.println("There isn't any file named like that");
        }
    }

    //Searches files on server that have relation with the item provided
    public static void clientFind(MyTubeInterface i) throws RemoteException {
        String name = "";
        String description = "";
        int in;
        Boolean option = false;
        Scanner reader = new Scanner(System.in);

        System.out.println("Do you want to find the file by name, by description, both or by key? Use n for name and d for description");
        System.out.println("Use n for name, d for description, b for both or k for key");

        while (option == false) {
            name = reader.nextLine();
            if (name.equals("d") || name.equals("description")) {
                option = true;
            } else if (name.equals("n") || name.equals("name")) {
                option = true;
            } else if (name.equals("k") || name.equals("key")) {
                option = true;
            } else if (name.equals("b") || name.equals("both")) {
                option = true;
            } else {
                System.out.println("Sorry, I couldn't understand that, please try again");
            }
        }

        if (name.equals("n") || name.equals("name")) {
            System.out.println("Please insert the name of the file you want to find:");
            name = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            fileData[] af = i.findName(name); //Client calls server to execute implementation's method to find the file by name

            if (af != null) {
                System.out.println("These files have been found with your name:");

                for (in = 0; in < af.length; in++) {
                    System.out.println("Name: " + af[in].getName() + ", description: " + af[in].getDescription() + ", server id: " + af[in].getServerId());
                }
            } else {
                System.out.println("File not found");
            }
        } else if (name.equals("k") || name.equals("key")) {
            File keys = new File("ClientMem/keys");

            System.out.println("You currently have uploaded this files: ");

            try {
                BufferedReader br = new BufferedReader(new FileReader(keys));
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                br.close();
            } catch (Exception e) {
                System.out.println("Error!");
            }

            System.out.println("Please insert the key of the file you want to find:");
            name = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            fileData af = i.findKey(name); //Client calls server to execute implementation's method to download the file by key

            if (af != null) {
                System.out.println("These file has been found with your key:");
                System.out.println("Name: " + af.getName() + ", description: " + af.getDescription() + ", server id: " + af.getServerId());
            } else {
                System.out.println("File not found");
            }

        } else if (name.equals("b") || name.equals("both")) {
            System.out.println("Please insert the name of the file you want to find:");
            System.out.println("Please insert the and the description of the file you want to download:");
            System.out.println("Name:");
            name = reader.nextLine();
            System.out.println("Description:");
            description = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            description = description.replaceAll("\\s", "_");
            fileData[] af = i.findNameDescription(name, description); //Client calls server to execute implementation's method to find the file
            //by name and description

            if (af != null) {
                System.out.println("These files have been found with your name:");

                for (in = 0; in < af.length; in++) {
                    System.out.println("Name: " + af[in].getName() + ", description: " + af[in].getDescription() + ", server id: " + af[in].getServerId());
                }
            } else {
                System.out.println("File not found");
            }
        } else {
            System.out.println("Please insert the description of the file you want to find:");
            name = reader.nextLine();

            name = name.replaceAll("\\s", "_");
            fileData[] af = i.findDescription(name); //Client calls server to execute implementation's method to find the file by description

            if (af != null) {
                System.out.println("These files have been found with your description:");

                for (in = 0; in < af.length; in++) {
                    System.out.println("Name: " + af[in].getName() + ", description: " + af[in].getDescription() + ", server id: " + af[in].getServerId());
                }
            } else {
                System.out.println("File not found");
            }
        }
    }

    //Deletes files from server
    public static void clientDelete(MyTubeInterface i) throws RemoteException, IOException {
        File keys = new File("ClientMem/keys");
        Scanner reader = new Scanner(System.in);
        String key;

        System.out.println("You currently have uploaded this files: ");

        try {
            BufferedReader br = new BufferedReader(new FileReader(keys));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Error!");
        }

        System.out.println("Please insert the key from the file you want to delete (write nothing to go back):");
        key = reader.nextLine();

        if (!"".equals(key)) {
            i.delete(key);
            removeLineFromFile(key);
            System.out.println("File deleted!");
        }
    }

    //Removes key from user's key file
    public static void removeLineFromFile(String lineToRemove) throws FileNotFoundException, IOException {
        File inFile = new File("ClientMem/keys");

        //Construct the new file that will later be renamed to the original filename.
        File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

        BufferedReader br = new BufferedReader(new FileReader(inFile));
        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

        String line = null;

        //Read from the original file and write to the new
        //unless content matches data to be removed.
        while ((line = br.readLine()) != null) {

            if (!line.trim().contains(lineToRemove)) {

                pw.println(line);
                pw.flush();
            }
        }
        pw.close();
        br.close();

        //Delete the original file
        inFile.delete();

        //Rename the new file to the filename the original file had.
        tempFile.renameTo(inFile);

    }
}