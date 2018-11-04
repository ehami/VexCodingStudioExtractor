/*====================================
 -Eric Hamilton
 -Period 9
 -st026
 ====================================*/

import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.json.*;

public class VexCodingStudioExtractor {

    public static void main(String[] args) throws IOException {


        Scanner in = new Scanner(System.in);
        Scanner reader;
        JSONObject obj;


        String folderName = "VEX_Extracted";    //Will be changed to name of vex file (if input as vex file), and date and time will be appended, and will store json file and folder of files
        String outputFolder = "output";         //Folder inside folder to output to (may be empty to dump into same dir as json file)

        String filename;
        if (args.length != 1) {
            System.out.print("please enter a filename: ");
            filename = in.nextLine();
        } else {
            filename = args[0];
        }



        try {

            //update the folder name to match the vex project filename
            if (filename.toLowerCase().endsWith("vex")) {   //Is currently in vex's nonsense container format
                System.out.println("Filename ends with .vex, saving in specific folder.");
                folderName = filename.replace(".vex", "");  //update folder name
            }

            //add date to folder name
            Calendar cal = Calendar.getInstance();
            Date date = cal.getTime();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String formattedDate = dateFormat.format(date);
            folderName += ("_" + formattedDate);


            //extract vex's compressed archive
            if (filename.toLowerCase().endsWith("vex")) {   //Is currently in vex's nonsense container format
                System.out.println("Filename ends with .vex, extracting into folder.");
                decompress(filename, new File(folderName));
                filename = folderName + "/___ThIsisATemPoRaRyFiLE___.json";    //hey vex, I love the name.
            }

            //read json file
            System.out.println("Reading File \"" + filename + "\"");
            reader = new Scanner(new File(filename));
            obj = new JSONObject(reader.nextLine());
            reader.close();

            //create an info file about the project (description and language)
            PrintWriter infoFile = new PrintWriter(new FileWriter(folderName + "/" + obj.getString("title") + ".txt"));
            System.out.printf("Creating new info file \"%s\".\n", folderName + "/" + obj.getString("title") + ".txt");

            infoFile.printf("Description: %s\n", obj.getString("description"));
            infoFile.printf("Language: %s\n", obj.getJSONObject("language").getString("name"));
            infoFile.close();

            //put the extracted files in their own folder
            new File(folderName + "/" + outputFolder).mkdirs();

            //create and traverse the json object of files
            JSONObject fileObj = obj.getJSONObject("files");
            Iterator<String> keys = fileObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String encoded = fileObj.getString(key);

                //decode the base64 encoded files (vex, why did you do this!?)
                byte[] decoded = Base64.getDecoder().decode(encoded);
                String fileLocation = folderName + "/" + outputFolder + "/" + key;
                PrintWriter file = new PrintWriter(new FileWriter(fileLocation));
                System.out.printf("Creating new file \"%s\".\n", fileLocation);
                file.print(new String(decoded));
                file.close();
            }
        } catch (FileNotFoundException fnf) {
            System.out.println("File does not exist. Closing.");
            System.out.println(fnf);
            System.exit(0);
        }
    }


    private static void decompress(String in, File out) throws IOException {
        try (TarArchiveInputStream fin = new TarArchiveInputStream(new FileInputStream(in))) {
            TarArchiveEntry entry;
            while ((entry = fin.getNextTarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                File curfile = new File(out, entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                IOUtils.copy(fin, new FileOutputStream(curfile));
            }
        }
    }


}