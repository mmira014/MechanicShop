/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import java.util.ArrayList;
// import 

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence, one of: 
	 * @param rid_sequence
	 * @param cid_sequence
	 * @param mid_sequence
	 * @param Ownsid_sequence 
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select nextval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Creates and initializes sequences for rid, cid, mid, ownsid
	 * @param ridSize number of service request ids
	 * @param cidSize number of customer ids
	 * @param midSize number of mechanic ids
	 * @param OwnsSize number of Owns ids
	 * @throws SQLException
	 */
	public void initiateSequence(int ridSize, int cidSize, int midSize, int OwnsSize) throws SQLException {
		//initiate sequence for sr_rid

		Statement stmtTemp = this._connection.createStatement();
		
		String dropIfExist = "DROP SEQUENCE IF EXISTS rid_sequence;"
		+" DROP SEQUENCE IF EXISTS cid_sequence;"
		+" DROP SEQUENCE IF EXISTS mid_sequence;"
		+" DROP SEQUENCE IF EXISTS Ownsid_sequence;";
		stmtTemp.execute(dropIfExist);
		
		String initSeq = "CREATE SEQUENCE rid_sequence START WITH "+ridSize+";"
		+" CREATE SEQUENCE cid_sequence START WITH "+cidSize+";"
		+" CREATE SEQUENCE mid_sequence START WITH "+midSize+";"
		+" CREATE SEQUENCE Ownsid_sequence START WITH "+OwnsSize+";";
		stmtTemp.execute(initSeq);

		String nextVal = "SELECT nextval('rid_sequence');"
		+" SELECT nextval('cid_sequence');"
		+" SELECT nextval('mid_sequence');"
		+" SELECT nextval('Ownsid_sequence');";
		stmtTemp.execute(nextVal);
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			int ridSize = Integer.parseInt(esql.executeQueryAndReturnResult("Select count(rid) from Service_Request;").get(0).get(0));
			int cidSize = Integer.parseInt(esql.executeQueryAndReturnResult("Select count(id) from Customer;").get(0).get(0));
			int midSize = Integer.parseInt(esql.executeQueryAndReturnResult("Select count(id) from Mechanic;").get(0).get(0));
			int ownsSize = Integer.parseInt(esql.executeQueryAndReturnResult("Select count(ownership_id) from Owns;").get(0).get(0));

			System.out.println("Sizes (rid, cid, mid, owns) are : ("+ridSize+","+cidSize+","+midSize+","+ownsSize+")");
			esql.initiateSequence(ridSize, cidSize, midSize, ownsSize); // initiate sequence for ids

			boolean keepon = true;
			while(keepon){
				System.out.println("\nMAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	// -----------------INPUT VERIFICATION FUNCTIONS---------------------
	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static int readChoice(int maxVal) {
		int input;
		// returns only if a correct value is given.
		do {
			
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				if(input > maxVal || input < 1)
					throw new Exception();
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				System.out.print("Please choose a number between 1 and "+ maxVal +": ");
				continue;
			}//end try
		}while (true);
		System.out.println();
		return input;
	}

	/**
	 * Used to read user input for (y/n) questions
	 * 
	 * @return - "y" or "n" (as strings not char)
	 */
	public static String readBinaryChoice() {
		String input;
		do {
			try { 
				input = in.readLine();
				input = input.toLowerCase();
				if( !(input.equals("y") || input.equals("n")) ) {
					throw new Exception();
				}
				else {
					break;
				}
			}catch (Exception e) {
				System.out.print("Please enter 'y' or 'n': ");
				continue;
			}
		}while(true);
		return input;
	}

	/**
	 * Reads input from user and checks that user entered a positive integer
	 * @return inputInt - postive integer defined by user
	 */
	public static int readUserInteger() {
		String input;
		int inputInt;
		do {
			try {
				input = in.readLine();
				inputInt = Integer.parseInt(input);
				if(inputInt < 0) {
					throw new Exception();
				}
				else {
					break;
				}
			} catch (Exception e) {
				System.out.print("Invalid Input!\nPlease enter a positive integer: ");
				continue;
			}
		}while(true);
		return inputInt;
	}

	public static boolean isInt(String userString) {
		try { 
			Integer.parseInt(userString); 
		} 
		catch(NumberFormatException e) { 
			return false; 
		} 
		catch(NullPointerException e) {
			return false;
		}
		return true;
	}

	/**
	 * Reads user input for a name and returns string if valid name. 
	 * Valid names contain no spaces, user should use '-' for composite names.
	 * 
	 * @return valid name as string
	 */
	public static String readName() {
		String input;
		do {
			try { 
				input = in.readLine();
				input = input.trim();
				if( input.isEmpty() || input.contains(" ") || input.length() > 32) {
					throw new Exception();
				}
				else {
					break;
				}
			}catch (Exception e) {
				System.out.println("\nInvalid name! Names should be at most 32 characters and contain no spaces (use '-' if needed).");
				System.out.print("Please enter a valid name: ");
				continue;
			}
		}while(true);
		return input;
	}

	public static String readPhoneNum() {
		String input;
		do {
			try {
				input = in.readLine();
				input = input.trim();
				if(!input.matches("^\\d{3}-\\d{3}-\\d{4}$")) {
					throw new Exception();
				}
				else {
					break;
				}
			} catch (Exception e) {
				System.out.println("\nInvalid phone #! Format is \"###-###-####\"");
				System.out.print("Please enter a valid phone #: ");
				continue;
			}
		}while(true);
		return input;
	}

	/**
	 * 
	 * @param stringType the string the user is inputting for (i.e. vin, make, model, etc.)
	 * @param maxSize maximum size for stringType
	 * @return valid string for this stringType
	 */
	public static String readUserString(String stringType,int maxSize) {
		String input;
		do {
			try {
				input = in.readLine().trim();
				if(input.length() > maxSize) {
					throw new Exception();
				}
				else {
					break;
				}
			} catch (Exception e) {
				System.out.print("\nInvalid "+stringType+"! Max size is " + maxSize + " characters.");
				System.out.print("Please enter a valid "+stringType+": ");
			}
		}while(true);
		return input;
	}

	public static int readYEAR_Domain() {
		String input;
		int inputInt;
		do {
			try {
				input = in.readLine();
				inputInt = Integer.parseInt(input);
				if(inputInt < 1970) {
					throw new Exception();
				}
				else {
					break;
				}
			} catch (Exception e) {
				System.out.print("Invalid Input!\nPlease enter a year after 1969: ");
				continue;
			}
		}while(true);
		return inputInt;
	}

	public static int readYEARS_Domain() {
		String input;
		int inputInt;
		do {
			try {
				input = in.readLine();
				inputInt = Integer.parseInt(input);
				if(inputInt < 0 || inputInt >= 100) {
					throw new Exception();
				}
				else {
					break;
				}
			} catch (Exception e) {
				System.out.print("Invalid Input!\nPlease enter an integer between (0-100): ");
				continue;
			}
		}while(true);
		return inputInt;
	}

	// -----------------END OF INPUT VERIFICATION FUNCTIONS---------------------
	
	public static void AddCustomer(MechanicShop esql){//1
		int c_ID;
		String c_fname, c_lname, c_address, c_phone;

		try {
			String query = "";
			System.out.println("\n----Adding Customer----");

			c_ID = esql.getCurrSeqVal("cid_sequence"); // c_ID gets val from sequence

			System.out.print("\nEnter first name: ");
			c_fname = readName();
			System.out.print("\nEnter last name: ");
			c_lname = readName();
			System.out.print("\nEnter a phone number in format ###-###-####: ");
			c_phone = readPhoneNum();

			// put phone number in format (###)###-####
			char[] c_phoneArr = c_phone.toCharArray();
			c_phoneArr[3] = ')';
			c_phone = String.valueOf(c_phoneArr);
			c_phone = "(" + c_phone;

			System.out.print("\nEnter address: ");
			c_address = readUserString("address", 256);
			
			query = "INSERT INTO Customer VALUES ("+c_ID+",'"+c_fname+"','"+c_lname+"','"+c_phone+"','"+c_address+"');";
			System.out.println("Query is:\n"+query);
			esql.executeUpdate(query);

		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
		
	}

	public static String AddCustomer_ReturnID(MechanicShop esql){//1
		String cid = "";
		return cid;
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		/*
		Mechanic:
			id INTEGER NOT NULL,
			fname CHAR(32) NOT NULL,
			lname CHAR(32) NOT NULL,
			experience _YEARS NOT NULL 
		*/
		String m_fname, m_lname;
		int m_ID, m_yearsExp;
		try {
			String query = "";
			System.out.println("\n----Add Mechanic----");
			
			// System.out.print("\nEnter mechanic id: ");
			// String m_ID = in.readLine();
			m_ID = esql.getCurrSeqVal("mid_sequence");

			System.out.print("\nEnter first name: ");
			m_fname = readName();
			System.out.print("\nEnter last name: ");
			m_lname = readName();
			System.out.print("\nEnter years of experience: ");
			m_yearsExp = readYEARS_Domain();

			query = "INSERT INTO Mechanic VALUES ("+m_ID+",'"+m_fname+"','"+m_lname+"',"+m_yearsExp+");";
			System.out.println("Query is:\n"+query);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3	
		String vin, make, model;
		int year;
		try {
			String query = "";
			System.out.println("\n----Add Car----");
			System.out.print("\nEnter VIN: "); 
			vin = readUserString("vin", 16);
			System.out.print("\nEnter make: ");
			make = readUserString("make", 32);
			System.out.print("\nEnter model: ");
			model = readUserString("model", 32);
			System.out.print("\nEnter year: ");
			year = readYEAR_Domain(); 

			query = "INSERT INTO Car VALUES ("+vin+", '"+make+"', '"+model+"', "+year+");";
			System.out.println("Query is:\n"+query);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static String AddCar_ReturnVIN(MechanicShop esql){//3
		String vin = "";
		return vin;
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		/*
		Service_Request:
			rid INTEGER NOT NULL,
			customer_id INTEGER NOT NULL,
			car_vin VARCHAR(16) NOT NULL,
			date DATE NOT NULL,
			odometer _PINTEGER NOT NULL,
			complain TEXT
		*/
		try {
			String query ="";
			String sr_cid, sr_vin, sr_date, sr_complain;
			int sr_rid, sr_odometer;
			String c_lname; // used to query db for cars owned by c_lname
			int userChoiceInt = -1;
			System.out.println("\n----Insert Service Request----");
			System.out.print("Create service request for an existing customer?\n(y/n): ");
			String userChoice = readBinaryChoice();
			if(userChoice.equals("y")) { // user will create service request for existing customer
				System.out.print("Enter the customer's last name: ");
				c_lname = readName();

				query = "select * from customer where customer.lname='"+c_lname+"';";
				System.out.println(query);

				// search database for customer(s) and save result
				List<List<String>> listOfCustomers = esql.executeQueryAndReturnResult(query);

				if(listOfCustomers.size() > 1) { // more than one customer found for this lname
					System.out.println("(" + listOfCustomers.size() + ") Customers found!:");

					// print list of customers
					for (int curCustomer = 0; curCustomer < listOfCustomers.size(); ++curCustomer) { // iterate over customers
						System.out.print(curCustomer+1 + ". ");
						listOfCustomers.get(curCustomer).set(4, listOfCustomers.get(curCustomer).get(4).trim()); // trim trailing whitespace on address
						for(int curCol = 0; curCol < listOfCustomers.get(curCustomer).size(); ++curCol) { // iterate over customer columns
							// listOfCustomers.get(curCustomer).set( curCol, listOfCustomers.get(curCustomer).get(curCol).trim() );
							System.out.print(listOfCustomers.get(curCustomer).get(curCol) + " ");
						}
						System.out.println();
					}

					//prompt user to select from list of customers
					System.out.print("\nSelect customer (1-" + listOfCustomers.size() + "): ");
					userChoiceInt = readChoice(listOfCustomers.size());

					// get car for this customer's SR; need to query DB for car
					sr_cid = listOfCustomers.get(userChoiceInt-1).get(0); // get customer id

				}
				else { // 1 or no customers with lname found
					if(listOfCustomers.size() == 1) { // if one customer, confirm customer choice and add SR
						listOfCustomers.get(0).set(4, listOfCustomers.get(0).get(4).trim()); // trim trailing whitespace on address
						for (int colIter = 0; colIter < listOfCustomers.get(0).size(); ++colIter) {
							System.out.print(listOfCustomers.get(0).get(colIter) + " ");
						}
						System.out.println();
						System.out.print("\nInitiate request for this customer?\n(y/n): ");
						userChoice = readBinaryChoice();
						if(userChoice.equals("y")) { 
							System.out.print("Adding request for this customer");
							sr_cid = listOfCustomers.get(0).get(0);
							// break;
						}
						else { // user does not want to add request for found customer; reprompt insert service request
							InsertServiceRequest(esql);
							return;
						}
					}
					else { // customer not found
						System.out.println("Customer does not exist!");
						InsertServiceRequest(esql);
						return;
					}
				}

				// use sr_cid to fetch cars owned by customer
				query = "SELECT Owns.car_vin FROM Owns WHERE Owns.customer_id="+sr_cid+";";
				List<List<String>> carsOwned = esql.executeQueryAndReturnResult(query);

				// if customer owns many cars, prompt user to select which car
				if(carsOwned.size() >= 1) {
					System.out.println("("+carsOwned.size() + ") cars owned by this customer");

					// print list of cars owned by this customer
					for(int curCar = 0; curCar < carsOwned.size(); ++curCar) {
						System.out.print(curCar+1 + ". ");
						System.out.print(carsOwned.get(curCar).get(0) + " ");
						System.out.println();
					}
					
					// prompt user to select from list of cars
					System.out.print("\nSelect car (1-" + carsOwned.size() + "): ");
					userChoiceInt = readChoice(carsOwned.size());
					
					// get vin for sr_vin from userChoice
					sr_vin = carsOwned.get(userChoiceInt-1).get(0);
				}
				else { // customer owns no cars
					sr_vin = AddCar_ReturnVIN(esql);
				}
			}
			else {
				// create new customer
				sr_cid = AddCustomer_ReturnID(esql);
				sr_vin = AddCar_ReturnVIN(esql);
			}
			// at this point we have sr_rid, sr_cid, sr_vin
			// get sr_date, sr_odometer, sr_complain
			System.out.print("Enter service request date: ");
			sr_date = in.readLine();
			System.out.print("Enter odometer reading: ");
			sr_odometer = readUserInteger();
			System.out.print("Enter complaint (optional): ");
			sr_complain = in.readLine();
			
			String rid_sequence = "rid_sequence";
			sr_rid = esql.getCurrSeqVal(rid_sequence);
			query = "INSERT INTO Service_Request VALUES ("+sr_rid+","+sr_cid+",'"+sr_vin
			+"','"+sr_date+"',"+sr_odometer+",'"+sr_complain+"');";
			
			System.out.println("Query is:\n"+query);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5

		try {

			String query = "";
			String wid = "";
			String rid = "";
			String mid = "";
			String date = "";
			String comment = "";
			String bill = "";
			int userChoiceInt = -1;

			//we do not delete service requests to maintain a history of when the request was intially opened

			System.out.println("\n----Close Service Request----");

			do {

				System.out.print("Enter valid EID to close a service request: ");
				wid = in.readLine();
				if (isInt(wid) == false) {
					System.out.println("Invalid characters!");
					continue;
				}

				query = "SELECT id FROM Mechanic WHERE Mechanic.id = '"+wid+"';";
				System.out.println(query);
				List<List<String>> listofMechanics = esql.executeQueryAndReturnResult(query);

				if (listofMechanics.isEmpty()) {
					System.out.println("No matching EID found!");
					continue;
				}
				else { //else valid wid

					do {

						System.out.println("Enter valid Service Request Number: ");
						rid = in.readLine();

						if (isInt(rid) == false) {
							System.out.println("Please use valid characters! Service Request Number must be a number!");
							continue;
						}

						query = "SELECT rid FROM Service_Request WHERE Service_Request.rid = '"+rid+"';";
						System.out.println(query);
						List<List<String>> listofRIDS = esql.executeQueryAndReturnResult(query);

						if (listofRIDS.isEmpty()) {
							System.out.println("No matching RID found!");
							continue;
						}
						
						break; // break if input for rid correct
					} while (true);

<<<<<<< HEAD
						//else valid rid
							
							do {

								System.out.println("Enter Closing Date: ");
								date = in.readLine().trim();

								if (((validDate(date) == false)))  {
									System.out.println("Please enter valid chars: ");
									continue;
								}
								
								break;
=======
					// get closing date
					do {

						System.out.println("Enter Closing Date: ");
						date = in.readLine();
>>>>>>> f3625943da86a5a4dd8cc407a1fda8e20a2e9758

						if (isInt(date) == false || (Integer.parseInt(date) < 0) || (date.length() != 8))  {
							System.out.println("Please enter valid date. Dates must be in format!"); //FIXME: date format
							continue;
						}
						
						break;

					} while (true);

					//valid rid
					do {

						System.out.print("Enter final bill: ");
						bill = in.readLine();

<<<<<<< HEAD
								System.out.print("Enter final comments: ");
								comment = in.readLine();
				


								query = "INSERT INTO Closed_Request VALUES (40001, '"+rid+"', 1, '"+date+"', '"+comment+"', "+bill+" );";
								System.out.println(query);
								esql.executeUpdate(query);
								return;
=======
						if (isInt(bill) == false || Integer.parseInt(bill) < 0) {
							System.out.println("Invalid bill!");
							continue;
						}
>>>>>>> f3625943da86a5a4dd8cc407a1fda8e20a2e9758

						System.out.print("Enter final comments: ");
						comment = in.readLine();
						query = "INSERT INTO Service_Request VALUES ("+rid+", '"+rid+"', '"+wid+"', '"+date+"', '"+comment+"', "+bill+" );";
						esql.executeQuery(query);
						return;
					} while(true);	
				}
			} while (true);
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
<<<<<<< HEAD

		
	}

	public static boolean validDate(String s) {

		System.out.print("A");

		for (int i = 0; i < s.length(); ++i) {
		
			if ((s.charAt(i) == '/') || (s.charAt(i) == '0') || (s.charAt(i) == '1') || (s.charAt(i) == '2') || (s.charAt(i) == '3')
			|| (s.charAt(i) == '4') || (s.charAt(i) == '5') || (s.charAt(i) == '6') ||
			(s.charAt(i) == '7') || (s.charAt(i) == '8') || (s.charAt(i) == '9')) {

				continue;

			}

			else {
				return false;
			}
		}

		System.out.print("B");

		int count = 0;
		boolean found = false;
		int index1 = 0;
		int index2 = 0;

		//if contains two consecutive /
		if (s.contains("//")) {
			return false;
		}

		System.out.print("C");
		

		//verify only 2 '/'
		for (int j = 0; j < s.length(); ++j) {

			if (s.charAt(j)== '/') {
				++count;
			}

		}

		if (count != 2) {
			return false;
		}

		index1 = s.indexOf('/');

		for (int k = index1 + 1; k < s.length(); ++k) {

			if (s.charAt(k) == '/') {
				index2 = k;
				break;
			}

		}

		System.out.print("D");


		System.out.print("E");


		//partition each section of the date by the / character
		String s1, s2, s3 = "";

		System.out.print("INDEX 1: " + index1);
		System.out.print("INDEX 2: "+ index2);

		if ((s.substring(0, index1)).length() != 1 && ((s.substring(0, index1)).length() != 2)) {
			System.out.print("F");
			return false;
		}


		else {
			s1 = (s.substring(0, index1));
		}

		//fixme finish this
		if ((s.substring(index1 + 1, index2)).length() != 1 && ((s.substring(index1 + 1, index2)).length() != 2)) {
			System.out.print(s.substring(index1, index2));
			System.out.print("G");
			return false;
		}



		else {
			s2 = (s.substring(index1, index2));
		}

		if ((s.substring(index2 + 1, s.length()).length() != 4)) {
			System.out.print(s.substring(index2 + 1, s.length()));
			System.out.print("H");
			return false;
		}

		else {
			s3 = (s.substring(index2, s.length() -1));
		}

		if (s1.length() == 1) {

			if ((Integer.parseInt(s1) < 1) || (Integer.parseInt(s1) > 9)) {
				System.out.print("I");
				return false;
			}

		}

		else if (s1.length() == 2) {

			if ((Integer.parseInt(s1) < 10) || (Integer.parseInt(s1) > 32)) {
				System.out.print("J");
				return false;
			}

		}

		if (s2.length() == 1) {

			if ((Integer.parseInt(s2) < 1) || (Integer.parseInt(s2) > 9)) {
				System.out.print("K");
				return false;
			}
			
		}

		else if (s2.length() == 2) {
			int x = Integer.parseInt(s1);
			if ((x == 1) || (x == 3) || (x == 5) || (x == 7) || (x == 9)) {

				if ((Integer.parseInt(s2) < 1 ) || (Integer.parseInt(s2)) > 31) {
					System.out.print("L");
					return false;
				}
			}

			else if ((Integer.parseInt(s1)) == 2) {
				if ((Integer.parseInt(s2) < 1) || (Integer.parseInt(s2) > 29)) {
					System.out.print("M");
					return false;

				}
			}

			else if ((x == 4) || (x == 6) || (x == 9) || (x == 11)) {
				if ((Integer.parseInt(s2) < 1 ) || (Integer.parseInt(s2)) > 30) {
					System.out.print("N");
					return false;
				}
			}
			
		}

		

	return true;



	}

	public static boolean isInt(String userString) {
		try { 

			Integer.parseInt(userString); 

		} 
		catch(NumberFormatException e) { 
			return false; 
		} 
		catch(NullPointerException e) {
			return false;
		}
		return true;
=======
>>>>>>> f3625943da86a5a4dd8cc407a1fda8e20a2e9758
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		// 
		try{
			String query = "SELECT date, comment, bill FROM Closed_Request WHERE bill < 100;";
			// System.out.println("Query is:\n"+query);
			System.out.println("\nPrinting date, comment, and bill for all closed requests with bill lower than 100:");
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		} 
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try {
			String query = "SELECT C.fname, C.lname"
			+" FROM Customer C, ("
			+" SELECT customer_id"
			+" FROM Owns"
			+" GROUP BY customer_id"
			+" HAVING COUNT(customer_id)>20) AS temp"
			+" WHERE C.id=temp.customer_id;";
			// System.out.println("Query is:\n"+query);
			System.out.println("\nPrinting first and last name of customers having more than 20 different cars:");
			esql.executeQueryAndPrintResult(query); //FIXME: Fix whitespace issues in output?
		} catch (Exception e) {
			System.err.println(e.getMessage());	
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try{
			String query = "SELECT DISTINCT make,model,year"
			+ " FROM Car C, Service_Request S_R"
			+ " WHERE year < 1995 AND S_R.car_vin = C.vin AND S_R.odometer<50000;";
			// System.out.println("Query is:\n"+query);
			System.out.println("\nPrinting make, model, and year of all cars built before 1995 having less than 50,000 miles:");
			esql.executeQueryAndPrintResult(query); //FIXME: Fix whitespace issues in output?
		}catch(Exception e){
			System.err.println(e.getMessage());	
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		try {
			int userk;
			// returns only if a correct value is given.
			do {
				System.out.print("Please enter a k: ");
				try { // read the integer, parse it and break.
					userk = Integer.parseInt(in.readLine());
					if(userk < 1) {
						throw new Exception();
					}
					break;
				}catch (Exception e) {
					System.out.println("Your input is invalid!");
					continue;
				}//end try
			}while (true);
			
			String query = "SELECT C.make, C.model, temp.numRequests"
			+" FROM Car C, ("
			+" SELECT car_vin, COUNT(rid) AS numRequests"
			+" FROM Service_Request"
			+" GROUP BY car_vin ) AS temp"
			+" WHERE C.vin=temp.car_vin"
			+" ORDER BY temp.numRequests DESC LIMIT "+userk+";";

			// System.out.println("Query is:\n"+query);
			System.out.println("\nPrinting the make, model and number of service requests for the first <"+ userk + "> cars with the highest number of service orders:");
			esql.executeQueryAndPrintResult(query);
		} catch (Exception e) {
			System.err.println(e.getMessage());		
		}
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		try {
			String query = "SELECT C.fname, C.lname, totalBill"
			+" FROM Customer C, ("
			+" SELECT SR.customer_id, SUM(CR.bill) AS totalBill"
			+" FROM Closed_Request CR, Service_Request SR"
			+" WHERE CR.rid=SR.rid"
			+" GROUP BY SR.customer_id) AS temp"
			+" WHERE C.id=temp.customer_id"
			+" ORDER BY temp.totalBill DESC;";
			// System.out.println("Query is:\n"+query);
			System.out.println("\nPrinting the first name, last name and total bill of customers in descending order of their total bill for all cars brought to the mechanic:");
			esql.executeQueryAndPrintResult(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
}