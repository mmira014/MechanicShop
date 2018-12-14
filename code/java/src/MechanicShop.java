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
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
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
	
	public static void AddCustomer(MechanicShop esql){//1

		try {
			String query = "";
			System.out.println("\n----Adding Customer----");

			System.out.println("\nEnter customer id: ");
			String c_ID = in.readLine();
			//FIXME: check valid input (even input lengths for each field)
			System.out.print("\nEnter first name: ");
			String c_fname = in.readLine();
			System.out.print("\nEnter last name: ");
			String c_lname = in.readLine();
			System.out.print("\nEnter address: ");
			String c_address = in.readLine();
			
			query = "INSERT INTO Customer VALUES ("+c_ID",'"+c_fname"', '"+c_lname"',"+c_address");";
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
		try {
			String query = "";
			System.out.println("\n----Add Mechanic----");
			
			System.out.print("\nEnter mechanic id: ");
			String m_ID = in.readLine();
			//FIXME: check valid input (even input lengths for each field)
			System.out.print("\nEnter first name: ");
			String m_fname = in.readLine();
			System.out.print("\nEnter last name: ");
			String m_lname = in.readLine();
			System.out.print("\nEnter years of experience: ");
			String m_yearsExp = in.readLine();

			query = "INSERT INTO Mechanic VALUES ("+m_ID+",'"+m_fname+"','"+m_lname+"',"+m_yearsExp+");";

			System.out.println("Query is:\n"+query);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void AddCar(MechanicShop esql){//3
		
		try {

			String query = "";
			System.out.println("\n----Add Car----");
			System.out.print("\nEnter VIN: ");
			String vin = in.readLine();
			System.out.print("\nEnter make: ");
			String make = in.readLine();
			System.out.print("\nEnter model: ");
			String model = in.readLine();
			System.out.print("\nEnter year: ");
			Sting year = in.readLine();

			query = "INSERT INTO Car VALUES ("+vin+", '"+make+"', '"+model+"', "+year+");";

		}

		catch(Exception e) {
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
			String sr_rid, sr_cid, sr_vin, sr_date, sr_odometer, sr_complain;
			String c_id, c_fname, c_lname, c_phone, c_address;
			int userChoiceInt = -1;
			System.out.println("\n----Insert Service Request----");
			System.out.print("Create service request for an existing customer?\n(y/n): ");
			String userChoice = readBinaryChoice();
			switch(userChoice) { // switch statement will get sr_cid, sr_vin
				case "y":
					System.out.print("Enter the customer's last name: ");
					c_lname = in.readLine();

					query = "select * from customer where customer.lname='"+c_lname+"';";
					System.out.println(query);
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
						// if one customer, confirm customer choice and add SR
						if(listOfCustomers.size() == 1) {
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
							else {
								InsertServiceRequest(esql);
								return;
							}
						}
						else {
							System.out.println("Customer does not exist!");
							InsertServiceRequest(esql);
							return;
						}
					}

					// use sr_cid to select a car for the service request
					query = "SELECT Owns.car_vin FROM Owns WHERE Owns.customer_id="+sr_cid+";";
					List<List<String>> carsOwned = esql.executeQueryAndReturnResult(query);

					// if customer owns many cars, prompt user to select which car
					if(carsOwned.size() > 1) {
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
					break;

				case "n":
					// create new customer
					sr_cid = AddCustomer_ReturnID(esql);
					sr_vin = AddCar_ReturnVIN(esql);
					break;
			}
			// at this point we have sr_rid, sr_cid, sr_vin
			// get sr_date, sr_odometer, sr_complain
			System.out.print("Enter service request date: ");
			sr_date = in.readLine();
			System.out.print("Enter odometer reading: ");
			sr_odometer = in.readLine();
			System.out.print("Enter complaint (optional): ");
			sr_complain = in.readLine();
			
			// query = "INSERT INTO Service_Request VALUES ("+sr_rid+","+sr_cid+","+sr_vin
			// +","+sr_date+","+sr_odometer+","+sr_complain+");";
			
			// System.out.println("Query is:\n"+query);
			// esql.executeUpdate(query);
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
					System.out.println("Invalid characters: ");
					break;
				}

				query = "SELECT id FROM Mechanic WHERE Mechanic.id = '"+wid+"';";
				System.out.println(query);
				List<List<String>> listofMechanics = esql.executeQueryAndReturnResult(query);

				if (listofMechanics.isEmpty()) {
					System.out.println("No matching EID found: ");
					break;
				}

				//else valid wid
				else {

					do {

						System.out.println("Enter valid Service Request Number: ");
						rid = in.readLine();

						if (isInt(rid) == false) {
							System.out.println("Please use valid characters: ");
							break;
						}

						query = "SELECT rid FROM Service_Request WHERE Service_Request.rid = '"+rid+"';";
						System.out.println(query);
						List<List<String>> listofRIDS = esql.executeQueryAndReturnResult(query);

						if (listofRIDS.isEmpty()) {
							System.out.println("NO Matching RID found: ");
							break;
						}

						//else valid rid
						else {
							
							do {

								System.out.println("Enter Closing Date: ");
								date = in.readLine();

								if (isInt(date) == false || (date.length() != 8) {
									System.out.println("Invalid date: ")
									break;
								}

								else {

									do {

										System.out.print("Enter final bill: ");
										bill = in.readLine();

										if (isInt(bill) == false || bill < 0) {
											System.out.println("Invalid bill: ");
										}

										else {
											System.out.print("Enter final comments: ");
											comment = in.readLine();
											query = "INSERT INTO Service_Request VALUES ("+rid+", '"+rid+"', '"+wid+"', '"+date+"', '"+comment+"', "+bill+" );";
											esql.executeQuery(query);
											return;
										}

									} while(true);

								}
								

							} while (true);

						}


					} while (true);

				}

			} while (true);


		} catch Exception();

		
	}

	public static boolean isInt(String userString) {
		try { 

			Integer.parseInt(s); 

		} 
		catch(NumberFormatException e) { 
			return false; 
		} 
		catch(NullPointerException e) {
			return false;
		}
		return true;
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		// 
		try{
			String query = "SELECT date, comment, bill FROM Closed_Request WHERE bill < 100;";
			System.out.println("Query is:\n"+query);
			esql.executeQuery(query);
		}catch(Exception e) {
			System.err.println(e.getMessage());
		} 
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try{
			String query = "SELECT DISTINCT make,model, year"
			+ "FROM Car AS C, Service_Request AS S_R"
			+ "WHERE year < 1995 and S_R.car_vin = C.vin and S_R.odometer < 50000;";
			System.out.println("Query is:\n"+query);
			esql.executeUpdate(query);
		}catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		String query = ""+
		"SELECT";
	}
	
}