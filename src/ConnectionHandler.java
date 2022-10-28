import netscape.javascript.JSObject;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class ConnectionHandler implements Runnable{
    private Socket client_;
//    private String path; // filename
    ConnectionHandler( Socket clientSocket){
        client_ = clientSocket;
    }
    @Override
    public void run() {
        HTTPRequest request = new HTTPRequest ( client_);
        request.doit();

        String filename = request.getFilename();
        HashMap headers = request.getHeaders();

        HTTPResponse response = new HTTPResponse( client_, filename, headers );
        try {
            response.doit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if( response.getIsWsResponse() ){
            while (true) {
                // READ WebSocket MESSAGE
                DataInputStream message = null;
                String decodedString;

                try {
                    message = new DataInputStream(client_.getInputStream());

                    // step 1: check if masked
                    boolean isMasked = false;
                    byte[] twoBytes = new byte[2];
                    twoBytes = message.readNBytes(2);
                    if ((twoBytes[1] & 0x80) != 0) {
                        isMasked = true;
                    }
                    System.out.println("\nisMasked: " + isMasked );

                    // step 2: check payload length
                    int payloadLength = 0;
                    if ((twoBytes[1] & 0x7F) <= 125) {
                        payloadLength = (twoBytes[1] & 0x7F);
                    } else if ((twoBytes[1] & 0x7F) == 126) {
                        payloadLength = message.readShort();
                    } else if ((twoBytes[1] & 0x7F) == 127) {
                        payloadLength = (int) message.readLong();
                    }
                    System.out.println("length:" + (twoBytes[1] & 0x7F));

                    // step 3: check if reading masking key
                    byte[] maskingKey = new byte[4];
                    if (isMasked) {
                        maskingKey = message.readNBytes(4);
                    }
                    System.out.println("maskingKey: " + maskingKey[0] + " " + maskingKey[1] + " " + maskingKey[2] + " " + maskingKey[3]);

                    // step 4: read payload data based on length
                    byte[] encodedData;
                    byte[] decodedData = new byte[payloadLength];
                    encodedData = message.readNBytes(payloadLength);
                    for (int i = 0; i < encodedData.length; i++) {
                        decodedData[i] = (byte) (encodedData[i] ^ maskingKey[i % 4]);
                    }
                    decodedString = new String(decodedData, StandardCharsets.UTF_8);
                    System.out.println(decodedString);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // SEND RESPONSE TO WSs

                // create json object
                String requestType = decodedString.split(" ")[0];
                String room = "";
                String user = "";
                JSONObject jsonObject = new JSONObject();

                // save json's key-value based on request type
                if( requestType.equals("join") || requestType.equals("leave") ){
                    room = decodedString.split(" ")[2];
                    user = decodedString.split(" ")[1];
                    jsonObject.put("type", requestType );
                    jsonObject.put("room", room );
                    jsonObject.put("user", user );
                    System.out.println( jsonObject );

                    // output the frame object
                    try {
                        OutputStream outputStream = client_.getOutputStream();
                        // step 1: FIN + opcode
                        outputStream.write( (byte) 0x81 );

                        // step 2: mask + payload length
                        byte[] payloadData = jsonObject.toJSONString().getBytes();
                        System.out.println("jsonObject.toJSONString():" + jsonObject.toJSONString()); // test
                        System.out.println("payloadData: " + payloadData); // test

                        if( payloadData.length <= 125 ){
                            outputStream.write( (byte)(payloadData.length & 0x7F) );
                        }else if( (payloadData.length >= 126) && payloadData.length < Math.pow(2,16)){
                            outputStream.write( (byte)0x7E );
                            outputStream.write( (byte) ( (payloadData.length >> 8) & 0xFF) );
                            outputStream.write( (byte) (payloadData.length & 0xFF) );
                        }else if( payloadData.length >= Math.pow(2,16) ){
                            outputStream.write( (byte)0x7F );
                            for( int i = 7; i >= 0; i-- ){
                                outputStream.write( (byte) ( (payloadData.length >> (8*i) ) & 0xFF) );
                            }
                        }
                        // step 3: payload data
                        System.out.println("** line before outputStream.write(payloadData) **"); // for test
                        outputStream.write(payloadData); // THIS CAUSE ERROR !!!????? why client will send one more msg "�WebSocket Protocol Error"
                        System.out.println("** line after outputStream.write(payloadData) **"); // for test
                        outputStream.flush();

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }else if( requestType.equals("message") ){
                    jsonObject.put("message", requestType );
                    jsonObject.put("user", user );
                    jsonObject.put("room", room );
                    jsonObject.put("message", decodedString.split(" ")[1]);

                    // output the frame object (same as above)

                }else{
                    System.out.println("error: type is not join/leave/message");
                    System.out.println("decodedString: " + decodedString);

                }



            }

        }else{ // if not webSocket
            try {
                client_.close();
            } catch (IOException e) {
                System.out.println("close client socket fail");
                throw new RuntimeException(e);
            }
        }

    }
}
