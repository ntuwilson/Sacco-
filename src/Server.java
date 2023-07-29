import java.io.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;




public class Server {

    
   
   private static int receiptNum =-90;  //receipt Number to be checked 


   

    


    public static void main(String[] args) {
        ServerSocket serverSoc =null;
        Socket ss = null;
        Scanner fromclient = null;
        PrintWriter pr = null;
        String message = null;
        String userInput =null;
        String[] command  = null;
        String SecureMenu = null;
        int PhoneNumber =0;
        String MemberNumber = null;
       // String receiptNumber =null;
       String loggedInUsername = null;
       String loggedInPassword = null;
       
        
        

        //db connect
        try {
            JDBC.getInstance().connect();
            //
        } catch (Exception e) {
           
            System.out.println(e.getMessage());
        } 
      



        try {
            serverSoc = new ServerSocket(5656);
            System.out.println("Server running ...");
            ss = serverSoc.accept();
            System.out.println("         New Client connection        ");
            pr = new PrintWriter(ss.getOutputStream(),true);
            
            fromclient = new Scanner(ss.getInputStream());
            
            
             
            message ="Welcome to Uprise Sacco program please login";
            pr.println(message);

            
            //secure menu to send to the client 
            SecureMenu = "1. Deposit amount datedeposited receiptNumber\n" +
             "2. CheckStatement dateFrom dateTo\n" +
             "3. requestLoan amount paymentPeriodinMonths\n" +
             "4. LoanRequestStatus LoanApplicationNumber";

             
            while ((userInput = fromclient.nextLine()) != null) {
                command = userInput.split(" ");
                if (command.length > 1 && command.length <= 5) {
                    System.out.println(userInput);
                    switch (command[0]) {
                        case "login":
                            if (isValidCredentials(command[1], command[2])) {
                                loggedInUsername = command[1];
                                loggedInPassword = command[2];
                                pr.println("You have successfully logged in. Here is the secured menu:");

                                String[] menuOptions = SecureMenu.split("\n");
                                for (String option : menuOptions) {
                                    pr.println(option);
                                }
                                pr.println("END_MENU");

                                // Loop to handle user commands within the logged-in session
                                while (true) {
                                    userInput = fromclient.nextLine();
                                    command = userInput.split(" ");

                                    
                                    switch (command[0]) {
                                        case "logout":
                                            //pr.println("You have been logged out. Thank you for using our service.");
                                            System.out.println("user logged out of the system!");
                                            return; // Exit the loop and terminate the session
                                        case "deposit":
                                            if (command.length == 4) {
                                                
                                                String output = deposit(loggedInUsername, command[1], command[2],command[3]);
                                                double inputamount = Double.parseDouble(command[1]);
                                                int fg = Integer.parseInt(command[3]);
                                                
                                                
                                                    
                                                
                                                 
                                                

                                                 if (output.equals("yes")) {
                                                    updateBalance(fg,inputamount);
                                                    pr.println("Dear "+ loggedInUsername+" Your deposit of amount : "+inputamount+" has been successfully made and your account balance is now  : "+getFinalBalance(loggedInUsername));
                                                   
                                                } else if (output.startsWith("Error: ")|| output.equals("oops!")) {
                                                    System.out.println("Deposit failed !!");
                                                    pr.println("Your deposit was NOT successful. Receipt number : "+fg+" is already used");
                                                }
                                                
                                            } else {
                                                pr.println("Invalid deposit command format. Please provide all the required parameters.");
                                            }
                                            break;

                                        case "requestLoan":
                                            // Handle requestLoan command
                                            break;
                                        case "checkLoanStatus":
                                            // Handle checkLoanStatus command
                                            break;
                                        case "CheckStatement":
                                            // Handle CheckStatement command
                                            break;
                                        default:
                                            pr.println("Please follow the menu to acces the services.");
                                    }
                                   
                                }

                            } else {
                                pr.println(
                                        "Authentication failed. Invalid credentials. If you have forgotten your password, use: forgotPassword <membernumber> <phonenumber>");
                            }
                            break;
                        case "forgotPassword":
                            // Handle forgotPassword command
                            if (validateMemberInformation(command[1], command[2]).equals("One match")) {
                                pr.println(
                                        "Please return after a day while your issue has been resolved. Your reference number is: "
                                                + ReferenceNumber(MemberNumber,PhoneNumber));
                            } else if (validateMemberInformation(command[1], command[2]) == null) {
                                break;
                            } else {
                                pr.println(validateMemberInformation(command[1], command[2]));
                            }
                            break;
                        default:
                            pr.println("Unknown command");
                            break;
                    }
                } else {
                    pr.println("Please log into the system to access the secured menu.");
                }
            }

                    
               
            
            
        }catch (Exception  e) {
            if (userInput.equalsIgnoreCase("logout")) {
                System.out.println("user logged out of system");
               
            }

            System.out.println("Error !"+e.getMessage());
            pr.println("Internal Server run down please try again later!");
        }


                
    }

    

    //deposit method to call
    private static String deposit(String username, String amount, String dateDeposited, String receiptNumber) {
        try {
             receiptNum = Integer.parseInt(receiptNumber);

           // Check if receipt number exists in the database
            int receiptExists = checkReceiptExists();
            if (receiptExists == receiptNum) {
                System.out.println("receiptNumber expired ");

                return "Error: Receipt number already exists. Please use a different receipt number.";
                
                
            }else{

                JDBC jdbcInstance = JDBC.getInstance();
                Connection connection = jdbcInstance.getConnection();

                String sql = "INSERT INTO deposits (userId, amount, dateDeposited, receiptNumber) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(sql);
                insertStatement.setInt(1, getUserIdByUsername(username));
                insertStatement.setDouble(2, Double.parseDouble(amount));
                insertStatement.setDate(3, Date.valueOf(dateDeposited));
                insertStatement.setInt(4, receiptNum);
                insertStatement.executeUpdate();

                System.out.println("Deposit successful");
                return "yes";
            }
            

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Failed to deposit. Please check the server logs for more information.";
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "Error: Invalid receiptNumber format.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: An unexpected error occurred during deposit.";
        }
    }
    
    private static final int NO_BALANCE_EXISTS = -1;

    private static boolean isValidCredentials(String username, String password) {
        
        try {
           

            JDBC jdbcInstance = JDBC.getInstance();
            Connection connection = jdbcInstance.getConnection();
   
            

            String sql = "SELECT * FROM users WHERE Username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            boolean isValid = resultSet.next();

            

            return isValid;
        } catch (SQLException e) {
           
           System.out.println(e.getMessage());
            
        }return false;
    }
  

    private static int ReferenceNumber(String MemberNumber, int phoneNumber) {
       
        String DateofRequest = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss"));
        int referenceNumber = 0; // Initialize the referenceNumber variable to store the generated value.
    
        try {
            

            JDBC jdbcInstance = JDBC.getInstance();
            Connection connection = jdbcInstance.getConnection();
    
            // Use prepared statement with placeholders to insert the values
            String insertSql = "INSERT INTO issues (MemberNumber, phoneNumber, DateofRequest) VALUES (?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setString(1, MemberNumber);
            insertStatement.setInt(2, phoneNumber);
            insertStatement.setString(3, DateofRequest);
            insertStatement.executeUpdate();
    
            // Retrieve the generated ReferenceNumber
            ResultSet generatedKeys = insertStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                referenceNumber = generatedKeys.getInt(1); // Since the ReferenceNumber is an INT column.
                return referenceNumber;
            }
    
            // Close resources
            generatedKeys.close();
            insertStatement.close();
            connection.close();
    
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    
        return 0;
    }
  

    private static int getUserIdByUsername(String username) {       
         
        
        int userId = -5;  // this is to show that by default the user is not found (thats why we give it a negative)

        try {
            

            JDBC jdbcInstance = JDBC.getInstance();
            Connection connection = jdbcInstance.getConnection();
           
            String selectSql = "SELECT ID FROM users WHERE username = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, username);
            ResultSet resultSet = selectStatement.executeQuery();

            
            if (resultSet.next()) {
                userId = resultSet.getInt("ID");
                return userId;
            }else {
                System.out.println("No ID found for the above username");
                
            }

            resultSet.close();
            selectStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
        
    }



    private static String validateMemberInformation(String MemberNumber, String phonenumber) {
        String user_password = null;
       

        try {

            JDBC jdbcInstance = JDBC.getInstance();
            Connection connection = jdbcInstance.getConnection();
            int phoneNumberInt = Integer.parseInt(phonenumber); // Convert the input phonenumber to an integer

            // Use a PreparedStatement to create a parameterized query
            String query = "SELECT * FROM users WHERE MemberNumber = ? OR phoneNumber = ?";
            PreparedStatement statement = connection.prepareStatement(query);

            // Set the parameters for the query
            statement.setString(1, MemberNumber);
            statement.setInt(2, phoneNumberInt); // Use setInt to set the phoneNumber parameter

            ResultSet result = statement.executeQuery();

            boolean memberFound = false;
            boolean phoneNumberFound = false;

            while (result.next()) {
                String foundMemberNumber = result.getString("MemberNumber");
                int foundPhoneNumber = result.getInt("phoneNumber"); // Get the phoneNumber as an integer from the
                                                                    

                if (MemberNumber.equals(foundMemberNumber)) {
                    memberFound = true;
                    user_password = result.getString("password");
                }

                if (phoneNumberInt == foundPhoneNumber) {
                    phoneNumberFound = true;
                    user_password = result.getString("password");
                }
            }

            if (memberFound && phoneNumberFound) {
                return user_password; // Both match
            } else if (memberFound || phoneNumberFound) {
                //to insert the phone number and the MemberNumber into the issues table for the admin to find out the issue 
                ReferenceNumber(MemberNumber, phoneNumberInt);
                return "One match"; // One of them matches
            } else {
                return "No record found. Return after a day"; // None match
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid phoneNumber format.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return "Error: An unexpected error occurred.";
    }

 
    //returns oldaccountbalance
    public static double getOldAccountBalance()  {
        double balance = NO_BALANCE_EXISTS;
       
        try{


            JDBC jdbcInstance = JDBC.getInstance();
            Connection connection = jdbcInstance.getConnection();

            String query = "SELECT u.accountBalance FROM users u  INNER JOIN deposits d ON u.ID = d.userId  WHERE d.receiptNumber = ?";

            PreparedStatement statement = connection.prepareStatement(query);
             
            statement.setInt(1, receiptNum);

            ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    balance = resultSet.getDouble("accountBalance");
                   
                }
            
        }catch(Exception e){
            System.out.println("Error: " +e.getMessage());
        }

        return balance;
    }




    ////method to update the balance in the members table     
    public static String updateBalance(int receiptNum,double inamount) {
        try {

            
            
            double oldbalance = getOldAccountBalance();

            if (oldbalance == NO_BALANCE_EXISTS) {

                return "Error: No balance exists for the given receipt number.";
                
            }


            //update the balance with the deposited amount 
            double newBalance = oldbalance + inamount;

            // Save the new balance to the database (you need an UPDATE query)

            // For example:
            String updateQuery = "UPDATE users s " +
                    "JOIN deposits d ON s.ID = d.userId " +
                    "SET s.accountBalance = ? " +
                    "WHERE d.receiptNumber = ?";

            
                JDBC jdbcInstance = JDBC.getInstance();
                Connection connection = jdbcInstance.getConnection();
                PreparedStatement statement = connection.prepareStatement(updateQuery) ;
                statement.setDouble(1, newBalance);
                statement.setInt(2, receiptNum);
                statement.executeUpdate();
            

            // Log the successful deposit and return true
            System.out.println("balance updated successfuly");
            return "yay";
        } catch (SQLException e) {
            System.out.println("Error: "+e.getMessage());
            // If there's an error, log the failure and return false
            System.out.println("Balance not updated");
            return "oops!";
        }
    }



    //method to get the final balance after update
     public static int getFinalBalance(String username)  {
        //double balance =  Double.MIN_VALUE;
        int balance =  0;

        try{

            JDBC jdbcInstance = JDBC.getInstance();
            Connection connection = jdbcInstance.getConnection();
            String query = "SELECT accountBalance FROM users where username = ?";
            PreparedStatement statement = connection.prepareStatement(query);
             
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {

                    balance = resultSet.getInt("accountBalance");
                    return balance;

                }
                
            
        }catch(Exception e){
            System.out.println("Error: " +e.getMessage());
        }

        return 0;
    }





    

    // Helper method to check if the receipt number already exists in the deposits table
    private static int checkReceiptExists() {
        int checkedReceipt =-1;
        try {
            JDBC jdbcInstance = JDBC.getInstance();
            Connection connection = jdbcInstance.getConnection();
            String query = "SELECT receiptNumber FROM deposits WHERE receiptNumber = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, receiptNum);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                checkedReceipt = resultSet.getInt("receiptNumber");
                return checkedReceipt;  //returns the receiptnumber if it exists
            }
                
            
            
        } catch (SQLException e) {
            e.printStackTrace();
            
        }
        return 0;  //returns a zero if it does not exist
    }


}




 
