import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

public class HTTPRequest {
    private Socket client_;
    private String filename_;
    private HashMap<String, String> headers_ = new HashMap<>();


    HTTPRequest ( Socket clientSocket ){
        client_ = clientSocket;
    }

    public void doit(){
        InputStream inputstream = null;
        try {
            inputstream = client_.getInputStream();
            Scanner sc = new Scanner(inputstream);

            String line = sc.nextLine();                 // only read the 1st line once (not value pair)
            String[] splitLine = line.split(" ");  // split into 3 pieces
            filename_ = splitLine[1];


            while (!line.equals("")) {   // while the line is not blank
                line = sc.nextLine();    // read next header line
                System.out.println(line);
                if( !line.equals("")){   // if the line is not blank, save data to hashmap
                    String key = line.split(": ")[0];
                    String value = line.split(": ")[1];
                    headers_.put( key, value );
                }
            }
        } catch (
                IOException e) {
            System.out.println("HTTP Request fail");
            throw new RuntimeException(e);
        }
    }

    public String getFilename(){
        return filename_;
    }

    public boolean isWsRequest(){
        if( headers_.containsKey("Sec-WebSocket-Key") ){
            return true;
        }else{
            return false;
        }
    }

    public HashMap<String, String> getHeaders() {
        return headers_;
    }
}
