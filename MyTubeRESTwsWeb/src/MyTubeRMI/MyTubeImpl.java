package MyTubeRMI;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import classData.fileData;
import classData.serverData;

import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;

public class MyTubeImpl extends UnicastRemoteObject implements MyTubeInterface {

    private static Vector callbackObjects; //Vector for all the clients we need to do a callback

    public MyTubeImpl() throws RemoteException {
        super();
        callbackObjects = new Vector();
    }

    //Adds client to callback vector
    @Override
    public void addCallback(CallbackInterface CallbackObject) {
        System.out.println("Server got an 'addCallback' call.");
        callbackObjects.addElement(CallbackObject);
    }

    //Removes client from callback vector
    @Override
    public void unregisterForCallback(
            CallbackInterface callbackClientObject)
            throws java.rmi.RemoteException {
        if (callbackObjects.removeElement(callbackClientObject)) {
            System.out.println("Unregistered client ");
        } else {
            System.out.println("Unregister: client wasn't registered.");
        }
    }

    //Sends file to client searched by name
    @Override
    public byte[] downloadName(String name, CallbackInterface c, String server_id) throws RemoteException {
        fileData[] af = getFileByNameAndServer(name, server_id);

        if (af != null) {
            if (af.length > 1) {
                int j = c.chooseD(af);
                String path = "Database/" + af[j].getKey() + "/" + af[j].getName();
                File userFile;
                userFile = new File(path);
                byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
                try {
                    BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                    input.read(buffer, 0, buffer.length);
                    input.close();
                    return (buffer); //Server sends the array of bytes
                } catch (IOException e) {
                    System.out.println("Error!");
                    return new byte[0];
                }
            } else {
                String path = "Database/" + af[0].getKey() + "/" + af[0].getName();
                File userFile;
                userFile = new File(path);
                byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
                try {
                    BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                    input.read(buffer, 0, buffer.length);
                    input.close();
                    return (buffer); //Server sends the array of bytes
                } catch (IOException e) {
                    System.out.println("Error!");
                    return new byte[0];
                }
            }
        } else {
            af = getFileByName(name);

            if (af != null) {
                int num = c.chooseD(af);

                if (num == -1) {
                    return new byte[0];
                }

                serverData sd = getServer(af[num].getServerId());

                try {
                    String registryURL = "rmi://" + sd.getIp() + ":" + sd.getPort() + "/mytube/" + sd.getId();
                    MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
                    return i.downloadKey(af[num].getKey(), c, sd.getId());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }

            return new byte[0];
        }
    }

    //Sends file to client searched by description
    @Override
    public byte[] downloadDescription(String description, CallbackInterface c, String server_id) throws RemoteException {
        fileData[] af = getFileByDescriptionAndServer(description, server_id);

        if (af != null) {
            if (af.length > 1) {
                int j = c.chooseD(af);
                String path = "Database/" + af[j].getKey() + "/" + af[j].getName();
                File userFile;
                userFile = new File(path);
                byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
                try {
                    BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                    input.read(buffer, 0, buffer.length);
                    input.close();
                    return (buffer); //Server sends the array of bytes
                } catch (IOException e) {
                    System.out.println("Error!");
                    return new byte[0];
                }
            } else {
                String path = "Database/" + af[0].getKey() + "/" + af[0].getName();
                File userFile;
                userFile = new File(path);
                byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
                try {
                    BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                    input.read(buffer, 0, buffer.length);
                    input.close();
                    return (buffer); //Server sends the array of bytes
                } catch (IOException e) {
                    System.out.println("Error!");
                    return new byte[0];
                }
            }
        } else {
            af = getFileByDescription(description);
            if (af != null) {
                int num = c.chooseD(af);

                if (num == -1) {
                    return new byte[0];
                }

                serverData sd = getServer(af[num].getServerId());

                try {
                    String registryURL = "rmi://" + sd.getIp() + ":" + sd.getPort() + "/mytube/" + sd.getId();
                    MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
                    return i.downloadKey(af[num].getKey(), c, sd.getId());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
            return new byte[0];
        }
    }

    //Sends file to client searched by key
    @Override
    public byte[] downloadKey(String key, CallbackInterface c, String server_id) throws RemoteException {
        fileData af = getFileByKeyAndServer(key, server_id);

        if (af != null) {
            String path = "Database/" + key + "/" + af.getName();
            File userFile;
            userFile = new File(path);
            byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent

            try {
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                input.read(buffer, 0, buffer.length);
                input.close();
                return (buffer); //Server sends the array of bytes
            } catch (IOException e) {
                System.out.println("Error!");
                return new byte[0];
            }
        } else {
            af = getFileByKey(key);
            if (af != null) {
                serverData sd = getServer(af.getServerId());

                try {
                    String registryURL = "rmi://" + sd.getIp() + ":" + sd.getPort() + "/mytube/" + sd.getId();
                    MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
                    return i.downloadKey(key, c, sd.getId());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
            return new byte[0];
        }
    }

    //Sends file to client searched by name and description
    @Override
    public byte[] downloadNameDescription(String name, String description, CallbackInterface c, String server_id) throws RemoteException {
        fileData[] af = getFileByNameDescriptionAndServer(name, description, server_id);

        if (af != null) {
            if (af.length > 1) {
                int j = c.chooseD(af);
                String path = "Database/" + af[j].getKey() + "/" + name;
                File userFile;
                userFile = new File(path);
                byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
                try {
                    BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                    input.read(buffer, 0, buffer.length);
                    input.close();
                    return (buffer); //Server sends the array of bytes
                } catch (IOException e) {
                    System.out.println("Error!");
                    return new byte[0];
                }
            } else {
                String path = "Database/" + af[0].getKey() + "/" + name;
                File userFile;
                userFile = new File(path);
                byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent
                try {
                    BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
                    input.read(buffer, 0, buffer.length);
                    input.close();
                    return (buffer); //Server sends the array of bytes
                } catch (IOException e) {
                    System.out.println("Error!");
                    return new byte[0];
                }
            }
        } else {
            af = getFileByNameAndDescription(name, description);
            if (af != null) {
                int num = c.chooseD(af);

                if (num == -1) {
                    return new byte[0];
                }

                serverData sd = getServer(af[num].getServerId());

                try {
                    String registryURL = "rmi://" + sd.getIp() + ":" + sd.getPort() + "/mytube/" + sd.getId();
                    MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
                    return i.downloadKey(af[num].getKey(), c, sd.getId());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
            return new byte[0];
        }
    }

    //Receives and saves file from client
    @Override
    public String upload(byte[] file, String name, String server_id, String description) {
        String ID;

        fileData f = new fileData();
        f.setKey("");
        f.setName(name);
        f.setServerId(server_id);
        f.setDescription(description);

        try {
            // Posting File on Database
            ID = postFile(f);

            //get to check
            File dir = new File("Database/" + ID);
            dir.mkdir();
            String path = "Database/" + ID + "/" + name;

            try {
                FileOutputStream FOS = new FileOutputStream(path);
                BufferedOutputStream Output = new BufferedOutputStream(FOS);
                Output.write(file, 0, file.length);
                Output.flush();
                Output.close();
                //Server receives the byte array and flushes it into a file
                callback();
                //Server does a callback to all clients announcing that a new file has been uploaded
                return ID;
            } catch (IOException e) {
                System.out.println("Error!" + e.getMessage());
                return null;
            }
        } catch (IOException ex) {
            Logger.getLogger(MyTubeImpl.class.getName())
                    .log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //Deletes a file from a user using its key
    public void delete(String key) throws RemoteException {
        fileData f = getFileByKey(key);
        serverData sd = getServer(f.getServerId());

        try {
            String registryURL = "rmi://" + sd.getIp() + ":" + sd.getPort() + "/mytube/" + sd.getId();
            MyTubeInterface i = (MyTubeInterface) Naming.lookup(registryURL);
            i.deleteInit(key);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    //Auxiliary file so files can be deleted regardless of where the user is connected
    @Override
    public void deleteInit(String key) {
        File folder = new File("Database/" + key + "/");
        File[] directory = folder.listFiles();
        directory[0].delete();
        folder.delete();
    }

    //Finds files by given name
    @Override
    public fileData[] findName(String name) {
        return getFileByName(name);
    }

    //Finds files by given description
    @Override
    public fileData[] findDescription(String description) {
        return getFileByDescription(description);
    }

    //Finds files by given key
    @Override
    public fileData findKey(String key) {
        return getFileByKey(key);
    }

    //Finds files by given name and description
    @Override
    public fileData[] findNameDescription(String name, String description) {
        return getFileByNameAndDescription(name, description);
    }

    //It announces to all clients that a new file has been uploaded
    private void callback() {
        for (int i = 0; i < callbackObjects.size(); i++) {
            System.out.println("Now performing the " + i + "th callback\n");
            CallbackInterface client
                    = (CallbackInterface) callbackObjects.elementAt(i);
            try {
                client.callMe("New content has been uploaded!");
            } catch (Exception e) {
                System.out.println("Client has disconnected, removing from callback list");
                try {
                    unregisterForCallback(client);
                } catch (Exception e2) {
                    System.out.println("Error!");
                }
            }
        }
    }
    
    //------------------------------ WebService auxiliary functions ------------------------------

    //Calls WebService to post file on DataBase
    public String postFile(fileData f)
            throws IOException {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/file/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            os.write(f.getJson().getBytes());
            os.flush();

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_CREATED) {
                throw new IOException();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String key = br.readLine();
            conn.disconnect();
            return key;

        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }

    //Calls WebService to get file from DataBase by name
    public fileData[] getFileByName(String name) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filen/" + name);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            int i;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData[] af = g.fromJson(output, fileData[].class);
            for (i = 0; i < af.length; i++) {
                af[i].setKey(af[i].getKey().trim());
                af[i].setName(af[i].getName().trim());
                af[i].setDescription(af[i].getDescription().trim());
                af[i].setServerId(af[i].getServerId().trim());
            }
            return af;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to get file from DataBase by name and server
    public fileData[] getFileByNameAndServer(String name, String server_id) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filenas/" + name + "_" + server_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            int i;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData[] af = g.fromJson(output, fileData[].class);
            for (i = 0; i < af.length; i++) {
                af[i].setKey(af[i].getKey().trim());
                af[i].setName(af[i].getName().trim());
                af[i].setDescription(af[i].getDescription().trim());
                af[i].setServerId(af[i].getServerId().trim());
            }
            return af;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to get file from DataBase by description
    public fileData[] getFileByDescription(String description) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filed/" + description);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            int i;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData[] af = g.fromJson(output, fileData[].class);
            for (i = 0; i < af.length; i++) {
                af[i].setKey(af[i].getKey().trim());
                af[i].setName(af[i].getName().trim());
                af[i].setDescription(af[i].getDescription().trim());
                af[i].setServerId(af[i].getServerId().trim());
            }
            return af;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to get file from DataBase by description and server
    public fileData[] getFileByDescriptionAndServer(String description, String server_id) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filedas/" + description + "_" + server_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            int i;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData[] af = g.fromJson(output, fileData[].class);
            for (i = 0; i < af.length; i++) {
                af[i].setKey(af[i].getKey().trim());
                af[i].setName(af[i].getName().trim());
                af[i].setDescription(af[i].getDescription().trim());
                af[i].setServerId(af[i].getServerId().trim());
            }
            return af;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to get file from DataBase by key
    public fileData getFileByKey(String key) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filek/" + key);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData f = g.fromJson(output, fileData.class);
            f.setKey(f.getKey().trim());
            f.setName(f.getName().trim());
            f.setDescription(f.getDescription().trim());
            f.setServerId(f.getServerId().trim());
            return f;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to get file from DataBase by key and server
    public fileData getFileByKeyAndServer(String key, String server_id) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filekas/" + key + "_" + server_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData f = g.fromJson(output, fileData.class);
            f.setKey(f.getKey().trim());
            f.setName(f.getName().trim());
            f.setDescription(f.getDescription().trim());
            f.setServerId(f.getServerId().trim());
            return f;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to get file from DataBase by name and description
    public fileData[] getFileByNameAndDescription(String name, String description) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filenad/" + name + "_" + description);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            int i;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData[] af = g.fromJson(output, fileData[].class);
            for (i = 0; i < af.length; i++) {
                af[i].setKey(af[i].getKey().trim());
                af[i].setName(af[i].getName().trim());
                af[i].setDescription(af[i].getDescription().trim());
                af[i].setServerId(af[i].getServerId().trim());
            }
            return af;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to get file from DataBase by name, description and server
    public fileData[] getFileByNameDescriptionAndServer(String name, String description, String server_id) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/filendas/" + name + "_" + description + "_" + server_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            int i;
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            fileData[] af = g.fromJson(output, fileData[].class);
            for (i = 0; i < af.length; i++) {
                af[i].setKey(af[i].getKey().trim());
                af[i].setName(af[i].getName().trim());
                af[i].setDescription(af[i].getDescription().trim());
                af[i].setServerId(af[i].getServerId().trim());
            }
            return af;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    //Calls WebService to delete file from DataBase
    public void deleteFile(String key) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/file/" + key + "/delete");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 204) {
                System.out.println(conn.getResponseCode());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //Calls WebService to get server from DataBase
    public serverData getServer(String id) {
        try {
            URL url = new URL("http://localhost:8080/MyTubeRESTwsWeb/rest/server/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output = br.readLine();
            conn.disconnect();

            Gson g = new Gson();
            serverData as = g.fromJson(output, serverData.class);
            as.setId(as.getId().trim());
            as.setPort(as.getPort().trim());
            as.setIp(as.getIp().trim());
            return as;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}

