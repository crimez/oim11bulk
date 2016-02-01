import au.com.bytecode.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.LoginException;

import oracle.iam.identity.exception.NoSuchRoleException;
import oracle.iam.identity.exception.NoSuchUserException;
import oracle.iam.identity.exception.RoleGrantException;
import oracle.iam.identity.exception.RoleGrantRevokeException;
import oracle.iam.identity.exception.SearchKeyNotUniqueException;
import oracle.iam.identity.exception.UserAlreadyExistsException;
import oracle.iam.identity.exception.UserCreateException;
import oracle.iam.identity.exception.UserDisableException;
import oracle.iam.identity.exception.UserEnableException;
import oracle.iam.identity.exception.UserLockException;
import oracle.iam.identity.exception.UserManagerException;
import oracle.iam.identity.exception.UserModifyException;
import oracle.iam.identity.exception.UserSearchException;
import oracle.iam.identity.exception.UserUnlockException;
import oracle.iam.identity.exception.ValidationFailedException;
import oracle.iam.identity.rolemgmt.api.RoleManager;
import oracle.iam.identity.usermgmt.api.UserManager;
import oracle.iam.identity.usermgmt.vo.User;
import oracle.iam.platform.OIMClient;
import oracle.iam.platform.entitymgr.vo.SearchCriteria;

public class oimbulk {
     
    private static UserManager userManager;
    private static RoleManager roleManager;
    //private static String logPath = System.getProperty("java.io.tmpdir") + "/oimBulk.log";     
    private static String logPath = System.getProperty("user.dir") + "/oimBulk.log";     
    private static String inputCSV = System.getProperty("user.dir") + "/inpOIM.csv";
    private static String oimUser = "xellerate";
    private static String oimPass = "xyz";
    private static String weblogicServer = "weblogic01.domain.local:14000";
    private static String authwlFile = System.getProperty("user.dir") + "\\authwl.conf";
    private static String userLogin = "";
 
    public static void main(String[] args) {
        //System.out.println(System.getProperty ("user.dir"));   
        
        // guardo se esiste il file di configurazione per connettersi a Weblogic
        File f = new File(authwlFile);
        if(!f.exists() || f.isDirectory()) { 
            System.out.println("File " + authwlFile + " not found\n\n");
            System.exit(0);
        }
        // guardo se esiste il file di input con i dati da elaborare
        f = new File(inputCSV);
        if(!f.exists() || f.isDirectory()) { 
            System.out.println("File " + inputCSV + " not found\n\n");
            System.exit(0);
        }
        
        boolean ret = false;
        oimbulk oim=new oimbulk();
      
        int length = args.length;
        if (length < 4 ) {
            printUsage();
            System.exit(0);
        }
        else {
            inputCSV = args[0];
            oimUser = args[1];
            oimPass = args[2];
            weblogicServer = args[3];
        }

      
        oim.OIMConnection(); 
      //boolean r = oim.setEmail("CLCNRC74H21F839C", "test@test.it");
      //System.out.println(r);
      //System.exit(0);
        
        CSVReader reader;
        try {
            
            reader = new CSVReader(new FileReader(inputCSV), ';');

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                
                String CF = nextLine[0];
                String opType = nextLine[1];
                String idOim = nextLine[2];
                String nome = nextLine[3];
                String cognome = nextLine[4];
                String pass = nextLine[5];
                String email = nextLine[6];
                String company = nextLine[7];
                String tipoDipendente = nextLine[8]; //HH SC ES PO
                String enddate = nextLine[9];
                String cfManager = nextLine[10];
                String oimGrp = nextLine[11];
                //String fullName = nextLine[0];
                if ((CF != null) && (CF.equalsIgnoreCase("CF") == false)) {

                    System.out.println("\nProcessing..." + nextLine[0] );
                    if (opType.equalsIgnoreCase("CREATE")==true )
                               {
                                    System.out.println("\nCreating user..." + idOim );
                                    ret = oim.createUser(idOim,nome,cognome,pass,email,company,CF,tipoDipendente,enddate, getKEYfromCF(cfManager) );
                                    if (ret==true && oimGrp!=null){ret = oim.assignRole(CF, oimGrp);}
                               }
                    else if (opType.equalsIgnoreCase("PROROGATE")==true) 
                                {
                                    System.out.println("\nProrogating CF..." + CF );
                                    ret = oim.setEndDateUser(CF, enddate);
                                    
                                }
                    else if (opType.equalsIgnoreCase("SETMANAGER")==true) 
                                {
                                    System.out.println("\nSetting manager CF..." + CF );
                                    ret = oim.setManagerID(CF, getKEYfromCF(cfManager));
                                    
                                }
                    else if (opType.equalsIgnoreCase("SETMAIL")==true) 
                                {
                                    System.out.println("\nSetting email CF..." + CF );
                                    ret = oim.setEmail(CF, email);
                                    
                                }
                    else if (opType.equalsIgnoreCase("SETCOMPANY")==true) 
                                {
                                    System.out.println("\nSetting company CF..." + CF );
                                    ret = oim.setCompany(CF, company);
                                    
                                }
                    else if (opType.equalsIgnoreCase("ENABLE")==true) 
                                {
                                    System.out.println("\nEnabling CF..." + CF );
                                    userLogin =getIDfromCF(CF);           
                                    ret= oim.enableUser(userLogin );
                                } 
                    else if (opType.equalsIgnoreCase("DISABLE")==true) 
                                {
                                    System.out.println("\nDisabling CF..." + CF );
                                    userLogin =getIDfromCF(CF);           
                                    ret= oim.disableUser(userLogin );
                                }  
                    else if (opType.equalsIgnoreCase("LOCK")==true) 
                                {
                                    System.out.println("\nlocking CF..." + CF );
                                    userLogin =getIDfromCF(CF);           
                                    ret= oim.lockUser(userLogin );
                                }  
                    else if (opType.equalsIgnoreCase("UNLOCK")==true) 
                                {
                                    System.out.println("\nUnlocking CF..." + CF );
                                    userLogin =getIDfromCF(CF);           
                                    ret= oim.unLockUser(userLogin );
                                }  
                    else if (opType.equalsIgnoreCase("CHANGEPASSWORD")==true) 
                                {
                                    System.out.println("\nChanging password CF..." + CF );
                                    userLogin =getIDfromCF(CF);           
                                    ret= oim.changePassword(userLogin,pass );
                                }  
                    else if (opType.equalsIgnoreCase("ADDGROUP")==true) 
                                {
                                    System.out.println("\nAdding group/role at CF..." + CF );
                                    ret = oim.assignRole(CF, oimGrp);
                                }
                    else if (opType.equalsIgnoreCase("REMOVEGROUP")==true) 
                                {
                                    System.out.println("\nRemoving group/role at CF..." + CF );
                                    ret = oim.revokeRole(CF, oimGrp);
                                }
                    Logga( strJoin(nextLine,";")  + ";" + ret);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }        
        
        
        //oim.setEndDateUser("ASSSDSDAASCDCASDCASD", "15-01-2025 11:35:42");
        //oim.createUser("test9", "nome", "cognome", "Imola2000", "aaa9@aaa.it", "codfisc123", "HH", "");    // userId, firstName, lastName, password, email, codiceFiscale
        //oim.lockUser("sachinTen");    //uncomment to lock user
        //oim.unLockUser("sachinten");  //uncomment to unlock user 
        //oim.disableUser("sachinTen");   //uncomment to disabel user
        //oim.enableUser("sachinTen");    //uncomment to enable user
        //oim.resetPassword("sachinTen");   //uncommnet to reset password
        /*
        try {
            oim.assignRole("ASSSDSDAASCDCASDCASD2","DSI Import CR");
        } catch (NoSuchRoleException e) {
            e.printStackTrace();
        }
        */
    }    
    public void OIMConnection(){             //Function to Connection to OIM
         
        Hashtable<Object, Object> env = new Hashtable<Object, Object>();
        env.put(OIMClient.JAVA_NAMING_FACTORY_INITIAL, "weblogic.jndi.WLInitialContextFactory");
        env.put(OIMClient.JAVA_NAMING_PROVIDER_URL, "t3://" + weblogicServer);        //Update localhost with your OIM machine IP
         
        System.setProperty("java.security.auth.login.config", authwlFile);   //Update path of authwl.conf file according to your environment
        System.setProperty("OIM.AppServerType", "wls");  
        System.setProperty("APPSERVER_TYPE", "wls");
        oracle.iam.platform.OIMClient oimClient = new oracle.iam.platform.OIMClient(env);
 
         try {                        
               oimClient.login(oimUser, oimPass.toCharArray());         //Update password of Admin with your environment password
               System.out.print("Successfully Connected with OIM ");
             } catch (LoginException e) {
               System.out.print("Login Exception "+ e);
            }            
          
        userManager = oimClient.getService(UserManager.class);
        roleManager = oimClient.getService(RoleManager.class);
    }
    public boolean createUser(String userId, String firstName, String lastName, String password, String email, String company, String codiceFiscale, String Role, String strEndDate, String usrManagerKey  ) {                                             //Function to create User
        HashMap<String, Object> userAttributeValueMap = new HashMap<String, Object>();
        boolean ret = false;
                userAttributeValueMap.put("act_key", new Long(4));
                userAttributeValueMap.put("User Login", userId);
                userAttributeValueMap.put("First Name", firstName);
                userAttributeValueMap.put("Last Name", lastName);
                userAttributeValueMap.put("Email", email);
                if (company != null && !company.isEmpty()) {userAttributeValueMap.put("SOCIETA", company);}
                userAttributeValueMap.put("usr_password", password);
                userAttributeValueMap.put("Role", Role);
                userAttributeValueMap.put("CF", codiceFiscale);
                userAttributeValueMap.put("usr_manager_key",  Long.parseLong(usrManagerKey));
                User user = new User(userId, userAttributeValueMap);
                //user.setOrganizationKey("4");

                if(strEndDate != null && !strEndDate.isEmpty())   {             
                    SimpleDateFormat dateformat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");                    //String strdate = "15-01-2016 11:35:42";
                    Date newdate = new Date();

                    try {
                    newdate = dateformat.parse(strEndDate);
                    user.setEndDate(newdate);
                    } catch (ParseException e) {
                    e.printStackTrace();
                    }
                }
    
        try {
            userManager.create(user);
            ret = true;
            System.out.println("\nUser got created....");
        } catch (ValidationFailedException e) {
            //e.printStackTrace();
            System.out.println("\n" + e.getMessage());
        } catch (UserAlreadyExistsException e) {
            System.out.println("\nUser Already Exists....");
        } catch (UserCreateException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public boolean disableUser(String userId) {                        //Function to disable user
    boolean ret=false;
        try {
            userManager.disable(userId, true);
            ret=true;
            System.out.print("\n Disabled user Successfully");
        } catch (ValidationFailedException e) {
            e.printStackTrace();
        } catch (UserDisableException e) {
            e.printStackTrace();
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public boolean enableUser(String userId) {                         //Function to enable user
    boolean ret=false;
        try {
            userManager.enable(userId, true);
            ret=true;
            System.out.print("\n Enabled user Successfully");
        } catch (ValidationFailedException e) {
            e.printStackTrace();
        } catch (UserEnableException e) {
            e.printStackTrace();
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public boolean changePassword(String userId, String newPassword){                       //Function to change user password
    boolean ret=false;
        try {
            userManager.changePassword(userId, newPassword.toCharArray(), true, null,true,false);
            System.out.println("Change Password done...");
            ret=true;
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        } catch (UserManagerException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public boolean lockUser(String userId) {       //Function to Lock User
    boolean ret = false;
        try {
            userManager.lock(userId, true,true);
            System.out.println("Lock user done...");
            ret=true;
        } catch (ValidationFailedException e) {
            e.printStackTrace();
        } catch (UserLockException e) {
            e.printStackTrace();
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public boolean unLockUser(String userId) {       //Function to Unlock user
    boolean ret = false;
        try {
            userManager.unlock(userId, true);
            
            System.out.println("Unlock user done...");
            ret=true;
        } catch (ValidationFailedException e) {
            e.printStackTrace();
        } catch (UserUnlockException e) {
            e.printStackTrace();
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public boolean setEndDateUser(String cod_fisc,  String newEndDateString) {                         //Function to Set End Date user
    boolean ret = false;
    Object val = null;
    Set<String> attrNames = null;
    Set<String> keys = null;
    List<User> users = null;
    HashMap<String, Object> parameters = null;
    HashMap<String, Object> attributes = null;        
    attrNames = new HashSet<String>();
    attrNames.add("User Login");
    attrNames.add("First Name");
    attrNames.add("Last Name");
    attrNames.add("usr_end_date");
    attrNames.add("CF");
    
    SearchCriteria criteria =  new SearchCriteria("CF", cod_fisc, SearchCriteria.Operator.EQUAL);

        try {
            users = userManager.search(criteria, attrNames, parameters);
        } catch (UserSearchException e) {
            e.printStackTrace();
        }
                 
        if (users != null && !users.isEmpty())
              {
                  //System.out.print("\nsearch results, quantity=" + users.size()); 
                  SimpleDateFormat dateformat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
                  Date newdate = new Date();

            try {
                newdate = dateformat.parse(newEndDateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            
            for (User user : users)
                 {
                    attributes = user.getAttributes();
                    StringBuilder buf =  new StringBuilder();

                    keys = attributes.keySet();
                    for (String key : keys)
                    {
                       val = attributes.get(key);
                       buf.append(key).append("='").append(val).append("', ");
                    }
                
                    //System.out.print("\nEntityId: " + user.getEntityId()  + ", Id: " + user.getId() + ", Attributes: " + buf.toString() );
                HashMap<String, Object> mapAttrs = null;    
                mapAttrs = new HashMap<String, Object>();
                //mapAttrs.put("usr_end_date", newdate);
                // mapAttrs.put("Title", "Engineer3");
                
                User user2 = new User((String)attributes.get("User Login"), mapAttrs);
                //user.setEndDate(newdate);
                user2.setEndDate(newdate);

                try {
                    userManager.modify("User Login" ,attributes.get("User Login") , user2);
                    System.out.print("\nNew End Date setted"  );
                    ret = true;
                    //System.out.print("\n" + userManager.modify(user2) );
                } catch (ValidationFailedException e) {
                    e.printStackTrace();
                } catch (UserModifyException e) {
                    e.printStackTrace();
                } catch (NoSuchUserException e) {
                   // System.out.print("\n" + attributes.get("User Login")) ;
                    e.printStackTrace();
                } catch (SearchKeyNotUniqueException e) {
                    e.printStackTrace();
                }
            }
        }
              else
              {
                 System.out.print("\n search result is empty");
              }
               return ret;
    }
    public boolean assignRole(String codFisc,String role)  {      
        boolean ret=false;
            try {
            //roleManager.grantRole("Role Name",role,"User Login",userId);
            roleManager.grantRole("Role Name",role,"CF",codFisc);
            
            System.out.println("\nAssigned Role To user..\n ");
            ret = true;
        } catch (ValidationFailedException e) {
            e.printStackTrace();
        } catch (RoleGrantException e) {
            e.printStackTrace();
        } catch (SearchKeyNotUniqueException e) {            
            e.printStackTrace();
        } catch (NoSuchRoleException e) {
            e.printStackTrace();
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        }       
            return ret;
    }    
    private static void printUsage() {
        System.out.println("Usage: java -jar oimBulk.jar csvInputFile oimUser oimPass oimServer");
        //System.out.println("");
        System.out.println("example: java -jar oimBulk.jar e:\\inpOIM.csv xelsysadm Password123 srvoimt01.adn.intra:14000\n");
        System.out.println(" * Powered by Christian Mezzetti * ");
        //System.out.println("opType = 4 => Set End Users Date");
    }
    private static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }
    public static void Logga(String txt) {

        try {

            DateFormat dateFormat =
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            //System.out.println(dateFormat.format(date));

            String filename = logPath; // "e:/bi/report.log";
            FileWriter fw =
                new FileWriter(filename, true); //the true will append the new data
            fw.write(dateFormat.format(date) + ";" + txt + "\r\n"); //appends the string to the file

            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
    public static String getIDfromCF(String cod_fisc) {                         
    String ret = "";
    Object val = null;
    Set<String> attrNames = null;
    Set<String> keys = null;
    List<User> users = null;
    HashMap<String, Object> parameters = null;
    HashMap<String, Object> attributes = null;        
    attrNames = new HashSet<String>();
    attrNames.add("User Login");

    
    SearchCriteria criteria =  new SearchCriteria("CF", cod_fisc, SearchCriteria.Operator.EQUAL);

        try {
            users = userManager.search(criteria, attrNames, parameters);
        } catch (UserSearchException e) {
            e.printStackTrace();
                        return "";
        }
                 
        if (users != null && !users.isEmpty())
              {
                //System.out.print("\nsearch results, quantity=" + users.size()); 
                //attributes = users.get(0).getAttributes();  
                ret= users.get(0).getLogin();
                //ret = (String)attributes.get("User Login")
            }
                   else
              {
                 System.out.print("\n search result is empty");
              }
               return ret;
    }
    public static String getKEYfromCF(String cod_fisc) {                         
    String ret = "";
    Object val = null;
    Set<String> attrNames = null;
    Set<String> keys = null;
    List<User> users = null;
    HashMap<String, Object> parameters = null;
    HashMap<String, Object> attributes = null;        
    attrNames = new HashSet<String>();
    attrNames.add("User Login");

    
    SearchCriteria criteria =  new SearchCriteria("CF", cod_fisc, SearchCriteria.Operator.EQUAL);

        try {
            users = userManager.search(criteria, attrNames, parameters);
        } catch (UserSearchException e) {
            e.printStackTrace();
                        return "";
        }
                 
        if (users != null && !users.isEmpty())
              {
                //System.out.print("\nsearch results, quantity=" + users.size()); 
                //attributes = users.get(0).getAttributes();  
                ret= users.get(0).getEntityId();
                
                //ret = (String)attributes.get("User Login")
            }
                   else
              {
                 System.out.print("\n search result is empty");
              }
               return ret;
    }
    public boolean revokeRole(String codFisc,String role)  {      
        boolean ret=false;
            try {
            //roleManager.grantRole("Role Name",role,"User Login",userId);
            roleManager.revokeRoleGrant("Role Name",role,"CF",codFisc);
            System.out.println("\nUnassigned Role To user..\n ");
            ret = true;
        } catch (ValidationFailedException e) {
            e.printStackTrace();
        } catch (SearchKeyNotUniqueException e) {            
            e.printStackTrace();
        } catch (NoSuchRoleException e) {
            e.printStackTrace();
        } catch (NoSuchUserException e) {
            e.printStackTrace();
        } catch (RoleGrantRevokeException e) {
            e.printStackTrace();
        }
        return ret;
    }          
    public static String strJoin(String[] aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.length; i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }
    public boolean setManagerID(String cod_fisc,  String newManagerIDString) {                         //Function to Set Manager ID 
        boolean ret = false;
        Object val = null;
        Set<String> attrNames = null;
        Set<String> keys = null;
        List<User> users = null;
        HashMap<String, Object> parameters = null;
        HashMap<String, Object> attributes = null;        
        attrNames = new HashSet<String>();
        attrNames.add("User Login");
        attrNames.add("CF");
        
        SearchCriteria criteria =  new SearchCriteria("CF", cod_fisc, SearchCriteria.Operator.EQUAL);

            try {
                users = userManager.search(criteria, attrNames, parameters);
            } catch (UserSearchException e) {
                e.printStackTrace();
            }
                     
            if (users != null && !users.isEmpty())
                  {

               
                for (User user : users)
                     {
                        attributes = user.getAttributes();
                        StringBuilder buf =  new StringBuilder();

                        keys = attributes.keySet();
                        for (String key : keys)
                        {
                           val = attributes.get(key);
                           buf.append(key).append("='").append(val).append("', ");
                        }
                    
                        //System.out.print("\nEntityId: " + user.getEntityId()  + ", Id: " + user.getId() + ", Attributes: " + buf.toString() );
                    HashMap<String, Object> mapAttrs = null;    
                    mapAttrs = new HashMap<String, Object>();
                    mapAttrs.put("usr_manager_key", Long.parseLong(newManagerIDString));
                    
                    User user2 = new User((String)attributes.get("User Login"), mapAttrs);
                    //user.setEndDate(newdate);
                    //user2.setEndDate(newdate);

                    try {
                        userManager.modify("User Login" ,attributes.get("User Login") , user2);
                        System.out.print("\nNew Manager setted");
                        ret = true;
                        //System.out.print("\n" + userManager.modify(user2) );
                    } catch (ValidationFailedException e) {
                        e.printStackTrace();
                    } catch (UserModifyException e) {
                        e.printStackTrace();
                    } catch (NoSuchUserException e) {
                       // System.out.print("\n" + attributes.get("User Login")) ;
                        e.printStackTrace();
                    } catch (SearchKeyNotUniqueException e) {
                        e.printStackTrace();
                    }
                }
            }
                  else
                  {
                     System.out.print("\n search result is empty");
                  }
                   return ret;
        }
    public boolean setEmail(String cod_fisc,  String newMAIL) {                         //Function to Set Manager ID 
    boolean ret = false;
    Object val = null;
    Set<String> attrNames = null;
    Set<String> keys = null;
    List<User> users = null;
    HashMap<String, Object> parameters = null;
    HashMap<String, Object> attributes = null;        
    attrNames = new HashSet<String>();
    attrNames.add("User Login");
    attrNames.add("CF");
            attrNames.add("Email");
    
    SearchCriteria criteria =  new SearchCriteria("CF", cod_fisc, SearchCriteria.Operator.EQUAL);

        try {
            users = userManager.search(criteria, attrNames, parameters);
        } catch (UserSearchException e) {
            e.printStackTrace();
        }
                 
        if (users != null && !users.isEmpty())
              {

           
            for (User user : users)
                 {
                    attributes = user.getAttributes();
                    StringBuilder buf =  new StringBuilder();

                    keys = attributes.keySet();
                    for (String key : keys)
                    {
                       val = attributes.get(key);
                       buf.append(key).append("='").append(val).append("', ");
                    }
                
                    //System.out.print("\nEntityId: " + user.getEntityId()  + ", Id: " + user.getId() + ", Attributes: " + buf.toString() );
                HashMap<String, Object> mapAttrs = null;    
                mapAttrs = new HashMap<String, Object>();
                mapAttrs.put("Email", newMAIL);
                
                User user2 = new User((String)attributes.get("User Login"), mapAttrs);
                //user.setEndDate(newdate);
                //user2.setEndDate(newdate);

                try {
                    userManager.modify("User Login" ,attributes.get("User Login") , user2);
                    System.out.print("\nNew Email setted");
                    ret = true;
                    //System.out.print("\n" + userManager.modify(user2) );
                } catch (ValidationFailedException e) {
                    System.out.println("\n" + e.getMessage());
                } catch (UserModifyException e) {
                    e.printStackTrace();
                } catch (NoSuchUserException e) {
                   // System.out.print("\n" + attributes.get("User Login")) ;
                    e.printStackTrace();
                } catch (SearchKeyNotUniqueException e) {
                    e.printStackTrace();
                }
            }
        }
              else
              {
                 System.out.print("\n search result is empty");
              }
               return ret;
    }
    public boolean setCompany(String cod_fisc,  String newCompany) {                         //Function to Set Manager ID 
    boolean ret = false;
    Object val = null;
    Set<String> attrNames = null;
    Set<String> keys = null;
    List<User> users = null;
    HashMap<String, Object> parameters = null;
    HashMap<String, Object> attributes = null;        
    attrNames = new HashSet<String>();
    attrNames.add("User Login");
    attrNames.add("CF");
    attrNames.add("SOCIETA");
    
    SearchCriteria criteria =  new SearchCriteria("CF", cod_fisc, SearchCriteria.Operator.EQUAL);

        try {
            users = userManager.search(criteria, attrNames, parameters);
        } catch (UserSearchException e) {
            e.printStackTrace();
        }
                 
        if (users != null && !users.isEmpty())
              {

           
            for (User user : users)
                 {
                    attributes = user.getAttributes();
                    StringBuilder buf =  new StringBuilder();

                    keys = attributes.keySet();
                    for (String key : keys)
                    {
                       val = attributes.get(key);
                       buf.append(key).append("='").append(val).append("', ");
                    }
                
                    //System.out.print("\nEntityId: " + user.getEntityId()  + ", Id: " + user.getId() + ", Attributes: " + buf.toString() );
                HashMap<String, Object> mapAttrs = null;    
                mapAttrs = new HashMap<String, Object>();
                mapAttrs.put("SOCIETA", newCompany);
                
                User user2 = new User((String)attributes.get("User Login"), mapAttrs);
                //user.setEndDate(newdate);
                //user2.setEndDate(newdate);

                try {
                    userManager.modify("User Login" ,attributes.get("User Login") , user2);
                    System.out.print("\nNew Company setted");
                    ret = true;
                    //System.out.print("\n" + userManager.modify(user2) );
                } catch (ValidationFailedException e) {
                    e.printStackTrace();
                } catch (UserModifyException e) {
                    e.printStackTrace();
                } catch (NoSuchUserException e) {
                   // System.out.print("\n" + attributes.get("User Login")) ;
                    e.printStackTrace();
                } catch (SearchKeyNotUniqueException e) {
                    e.printStackTrace();
                }
            }
        }
              else
              {
                 System.out.print("\n search result is empty");
              }
               return ret;
    } 
        
}

