import java.io.*;
import java.net.*;
import java.util.*;

public class Client {


    public static void main(String[] args) throws IOException{
        Socket  clientSoc = null;
        Scanner clientinput = null;
        Scanner fromServer = null;
        PrintWriter pr = null;
        String input =null;
       
      


        System.out.println("Client Started ...");

       
        try {
            clientSoc = new Socket("localhost",5656);    
            fromServer = new Scanner( clientSoc.getInputStream()); //this is getting message from server we read from the socket's input stream
            clientinput = new Scanner(System.in);
            pr = new PrintWriter(clientSoc.getOutputStream(),true);

            
          
            do{
               
                if (fromServer.hasNextLine()) {
                    String serverResponse = fromServer.nextLine();
                    System.out.println(serverResponse);
                    if (serverResponse.equals("You have successfully logged in. Here is the secured menu:")) {
                        // Read the menu options until "END_MENU" marker is received
                        StringBuilder menuBuilder = new StringBuilder();
                        String menuOption;
                        while (fromServer.hasNextLine() && !(menuOption = fromServer.nextLine()).equals("END_MENU")) {
                            menuBuilder.append(menuOption).append("\n");
                        }
                        System.out.println(menuBuilder.toString().trim()); // Display the complete menu
                    }else if(serverResponse.equals("Please supply your Registered PhoneNumber And MemberNumber")){
                            System.out.println("Please Enter your  MemberNumer :");
                             input = clientinput.nextLine(); 
                             pr.println(input);
                            System.out.println("Please Enter your phoneNumer :");
                            int d =clientinput.nextInt(); 
                            pr.println(d);
                             System.out.println(fromServer.nextLine());

                    }
                }


                

                
                
                input = clientinput.nextLine(); 
                pr.println(input);
                
                
                


            }while (!input.equalsIgnoreCase("logout"));

            



        } catch (Exception e) {
            
            System.out.println(e.getMessage());
        }
        
       
    }

    
    
}
