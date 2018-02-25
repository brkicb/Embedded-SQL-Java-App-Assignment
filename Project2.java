import static java.lang.System.out;
import java.util.*;
import java.net.*;
import java.text.*;
import java.lang.*;
import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
  
public class Project2
{
    private Connection conDB;   // Connection to the database system.
    private String url;         // URL: Which database?
 
    private Integer custID;     // Who are we tallying?
    private String  custName;   // Name of that customer.

    // Constructor
    public Project2 (String[] args) 
	{
    // Set up the DB connection.
        try 
		{
            // Register the driver with DriverManager.
            Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
        } 
		catch (ClassNotFoundException e) 
		{
			System.out.println("[!] Error: registering the driver with DriverManager");
            e.printStackTrace();
            System.exit(0);
        } 
		catch (InstantiationException e) 
		{
			System.out.println("[!] Error: registering the driver with DriverManager");
            e.printStackTrace();
            System.exit(0);
        } 
		catch (IllegalAccessException e) 
		{
			System.out.println("[!] Error: registering the driver with DriverManager");
            e.printStackTrace();
            System.exit(0);
        }
 
        // URL: Which database?
        url = "jdbc:db2:c3421a";
 
        // Initialize the connection.
        try 
		{
            // Connect with a fall-thru id & password
	    	conDB = DriverManager.getConnection(url);
        } 
		catch(SQLException e) 
		{
            System.out.print("\nSQL: database connection error.\n");
            System.out.println(e.toString());
            System.exit(0);
        }    
 
        // Let's have autocommit turned off.  No particular reason here.
        try 
		{
            conDB.setAutoCommit(false);
        } 
		catch(SQLException e) 
		{
            System.out.print("\nFailed trying to turn autocommit off.\n");
            e.printStackTrace();
            System.exit(0);
        }    
 
        // Who are we tallying?
        if (args.length != 1) 
		{
            // Don't know what's wanted.  Bail.
            System.out.println("\nUsage: java CustTotal cust#");
            System.exit(0);
        }
		else 
		{
            try 
	    	{
	     	   custID = new Integer(args[0]);
            } 
	    	catch (NumberFormatException e) 
	    	{
                System.out.println("\nUsage: java CustTotal cust#");
                System.out.println("Provide an INT for the cust#.");
                System.exit(0);
            }
        }
 
        // Is this custID for real?
        if (!customerCheck()) 
        {
	    	while (!customerCheck())
	    	{
                System.out.print("There is no customer #");
                System.out.print(custID);
                System.out.println(" in the database.");
                System.out.println("Please enter a valid ID.");
                Scanner scanner = new Scanner(System.in);
				custID = scanner.nextInt();
				customerCheck();
				if (customerCheck() == true)
				{
		    		find_customer();
		    		System.exit(0);
				}
	    	}
        }

		find_customer();
 
        // Commit.  Okay, here nothing to commit really, but why not...
        try 
		{
            conDB.commit();
        } 
		catch(SQLException e) 
		{
            System.out.print("\nFailed trying to commit.\n");
            e.printStackTrace();
            System.exit(0);
        }     
        // Close the connection.
        try 
		{
            conDB.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("\nFailed trying to close the connection.\n");
            e.printStackTrace();
            System.exit(0);
        }    
 
    }
 
    public boolean customerCheck() 
    {
    	String            queryText = "";     // The SQL text.
        PreparedStatement querySt   = null;   // The query handle.
        ResultSet         answers   = null;   // A cursor.
 
        boolean           inDB      = false;  // Return.
 
        queryText =
            "SELECT name       "
          + "FROM yrb_customer "
          + "WHERE cid = ?     ";

        // Prepare the query.
        try 
		{
            querySt = conDB.prepareStatement(queryText);
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: failed in prepare");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Execute the query.
        try 
		{
            querySt.setInt(1, custID.intValue());
            answers = querySt.executeQuery();
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: failed in execute (customerCheck)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        try 
		{
            if (answers.next()) 
	    	{
                inDB = true;
                custName = answers.getString("name");
        	} 
	    	else 
	    	{
                inDB = false;
                custName = null;
            }
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: failed in cursor. (customerCheck)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Close the cursor.
        try 
		{
            answers.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("[!] Eror: failed closing cursor. (customerCheck)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // We're done with the handle.
        try 
		{
            querySt.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("[!] Error: failed closing the handle. (customerCheck)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        return inDB;
    }

	public void find_customer()
	{
		String			  queryText = "";	  // The SQL text.
		PreparedStatement querySt 	= null;	  // The query handle.
		ResultSet		  answers 	= null;	  // A cursor.

		queryText =
			"SELECT city"
          + "    FROM yrb_customer"
          + "    WHERE cid = ?"
		  + "    GROUP BY city";

		// Prepare the query.
		try
		{
			querySt = conDB.prepareStatement(queryText);
		}
		catch(SQLException e)
		{
			System.out.println("[!] Error: in preparing query (find_customer)");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
        try 
		{
            querySt.setInt(1, custID.intValue());
            answers = querySt.executeQuery();
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: in execute (find_customer)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Variables to hold the column value(s).
        String city = "";
 
        // Walk through the results and present them.
        try 
		{
            if (answers.next()) 
	    		city = answers.getString("city");
	    	
			System.out.print("\n(" + custID + ", " + custName + ", ");
			System.out.println(city + ")\n");
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: failed in cursor. (find_customer)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Close the cursor.
        try 
		{
            answers.close();
        } 
		catch(SQLException e) 
		{
			System.out.print("[!] Error: failed closing cursor. (find_customer)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // We're done with the handle.
        try 
		{
            querySt.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("[!] Error: failed closing the handle. (find_customer)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
		
			
		System.out.println("Would you like to update the customer information? [Y/N]");
		Scanner info_update = new Scanner(System.in);
		String check = info_update.nextLine();

		// keep looping until user enters Y or N
		while (!check.equals("Y") && !check.equals("N"))
		{
			System.out.println("Please Enter 'Y' or 'N'");
			Scanner x = new Scanner(System.in);
			check = x.nextLine();
		}

		if (check.equals("Y"))
		{
			System.out.println("Your Current Information:");
			System.out.println("-+-+-+-+-+-+-+-+-+-+-");
			System.out.println("Name: " + custName);
			System.out.println("City: " + city);
			System.out.println("-+-+-+-+-+-+-+-+-+-+-\n");
			update_customer();
		}
			
		// See if user wants to make another update
		System.out.println("Are you finished making updates? [Y/N]");
		Scanner cont_update = new Scanner(System.in);
		String cont = cont_update.nextLine();
		// if 'Y' or 'N' is not entered, keep running loop
		while (!cont.equals("Y") && !cont.equals("N"))
		{
			System.out.println("Please Enter 'Y' or 'N'");
			Scanner s_check = new Scanner(System.in);
			cont = s_check.nextLine();
		}
		while (cont.equals("N"))
		{
			update_customer();
			System.out.println("Are you finished making updates? [Y/N]");
			Scanner c = new Scanner(System.in);
			cont = c.nextLine();
			// if 'Y' or 'N' is not entered, keep running loop
			while (!cont.equals("Y") && !cont.equals("N"))
			{
				System.out.println("Please Enter 'Y' or 'N'\n");
				Scanner c_check = new Scanner(System.in);
				cont = c_check.nextLine();
			}
		}
		if (cont.equals("Y"))
		{
			fetch_categories();
			find_book();
		}
	}
 
	public void update_customer()
	{
		String			  queryText = "";	  // The SQL text.
		PreparedStatement querySt 	= null;	  // The query handle.
		int				  answers 	= 0;	  // A cursor.

		String nameUpdate = "";				  // name to update to
		String cityUpdate = "";				  // city to update to

		System.out.println("Enter the item that needs updating: [NAME, CITY]");
		Scanner scan = new Scanner(System.in);
		String item = scan.nextLine();
		// update name
		if (item.equals("NAME"))
		{
			System.out.println("Enter the name to update to: ");
			Scanner scan2 = new Scanner(System.in);
			nameUpdate = scan2.nextLine();
			queryText =
			"UPDATE yrb_customer"
          + "    SET name = ?"
          + "    WHERE cid = ?";	
		}
		// update city
		else if (item.equals("CITY"))
		{
			System.out.println("Enter the city to update to: ");
			Scanner scan3 = new Scanner(System.in);
			cityUpdate = scan3.nextLine();
			queryText =
			"UPDATE yrb_customer"
          + "    SET city = ?"
          + "    WHERE cid = ?";
		}

		// Prepare the query.
		try
		{
			querySt = conDB.prepareStatement(queryText);
		}
		catch(SQLException e)
		{
			System.out.println("[!] Error preparing query (update_customer)");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
        try 
		{
			if (item.equals("NAME"))
			{
            	querySt.setString(1, nameUpdate);
				querySt.setInt(2, custID.intValue());
            	answers = querySt.executeUpdate();
			}
			else if (item.equals("CITY"))
			{
				querySt.setString(1, cityUpdate);
				querySt.setInt(2, custID.intValue());
				answers = querySt.executeUpdate();
			}
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error in execute (update_customer)");
            System.out.println(e.toString());
            System.exit(0);
        }
	}

	public void fetch_categories()
	{
		String			  queryText = "";	  // The SQL text.
		PreparedStatement querySt 	= null;	  // The query handle.
		ResultSet		  answers 	= null;	  // A cursor.

		queryText =
			"SELECT cat"
          + "    FROM yrb_category";

		// Prepare the query.
		try
		{
			querySt = conDB.prepareStatement(queryText);
		}
		catch(SQLException e)
		{
			System.out.println("[!] Error preparing query (fetch_categories)");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
        try 
		{
            answers = querySt.executeQuery();
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error in execute (fetch_categories)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Walk through the results and present them.
        try 
		{
			ResultSetMetaData rsmd = answers.getMetaData();
			int column_num = rsmd.getColumnCount();
			System.out.println("\nCategories: ");
			System.out.println("-+-+-+-+-+-+-+-+-+-+-");
			while (answers.next())
			{
				for (int i = 1; i <= column_num; i++)
				{
					if (i > 1)
						System.out.print(", ");
					String col_content = answers.getString(i);
					System.out.print(col_content);	
				}
				System.out.println();
			}
			System.out.println("-+-+-+-+-+-+-+-+-+-+-");
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: failed in cursor. (fetch_categories)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Close the cursor.
        try 
		{
            answers.close();
        } 
		catch(SQLException e) 
		{
			System.out.print("[!] Error: failed closing cursor. (fetch_categories)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // We're done with the handle.
        try 
		{
            querySt.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("[!] Error: failed closing the handle. (fetch_categories)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
	}

	public void find_book()
	{
		String			  queryText = "";	  // The SQL text.
		PreparedStatement querySt 	= null;	  // The query handle.
		ResultSet		  answers 	= null;	  // A cursor.

		String book = "";				// the book we want to search for
		String category = "";			// the category we want to search for

		System.out.println("\nEnter the name of the category you are looking for: ");
		Scanner cat_scanner = new Scanner(System.in);
		category = cat_scanner.nextLine();
		System.out.println("\nEnter the name of the book you are looking for: ");
		Scanner book_scanner = new Scanner(System.in);
		book = book_scanner.nextLine();		

		queryText =
			"SELECT title, year, weight, language"
          + "    FROM yrb_book b"
          + "    WHERE b.cat = ?"
          + "      AND b.title = ?";

		// Prepare the query.
		try
		{
			querySt = conDB.prepareStatement(queryText);
		}
		catch(SQLException e)
		{
			System.out.println("[!] Error preparing query (find_book)");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
        try 
		{
			querySt.setString(1, category);
            querySt.setString(2, book);
            answers = querySt.executeQuery();
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error in execute (find_book)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Variables to hold the book information so we can return it in the next code segment.
		String title = "";
        String year = "";
		String weight = "";
		String language = "";
		String option = "";
 
        // Walk through the results and present them.
        try 
		{
			// if the category and book were found
            if (answers.next())
			{
	    		title = answers.getString("title");
				year = answers.getString("year");
				weight = answers.getString("weight");
				language = answers.getString("language");

				System.out.println("\nBook Information: ");
				System.out.println("-+-+-+-+-+-+-+-+-+-+-");
				System.out.println("Title: " + title);
				System.out.println("Year: " + year);
				System.out.println("Weight: " + weight);
				System.out.println("Language: " + language);
				System.out.println("-+-+-+-+-+-+-+-+-+-+-");

				min_price(title, year, category);
	    	}
			// if the book wasn't found in the database
			else
			{
            	System.out.println();
				System.out.print("Book not found, would you like to search for another book [Y/N]: ");
				Scanner book_search = new Scanner(System.in);
				option = book_search.nextLine();
				// if Y or N weren't entered, keep running this loop
				while (!option.equals("Y") && !option.equals("N"))
				{
					System.out.println("Please Enter 'Y' or 'N'");
					Scanner valid_choice = new Scanner(System.in);
					option = valid_choice.nextLine();
				}
				// if user would like to search for another book
				if (option.equals("Y"))
					find_book();

				// if user doesn't want to search for another book
				else if (option.equals("N"))
				{
					System.out.println("-+-+-+-+-+-+-+-+-+-+-");
					System.out.println("Goodbye!");
					System.out.println("-+-+-+-+-+-+-+-+-+-+-");
				}
			}
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: failed in cursor. (find_book)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Close the cursor.
        try 
		{
            answers.close();
        } 
		catch(SQLException e) 
		{
			System.out.print("[!] Error: failed closing cursor. (find_book)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // We're done with the handle.
        try 
		{
            querySt.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("[!] Error: failed closing the handle. (find_book)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
	}

	public void min_price(String title, String year, String category)
	{
		String			  queryText = "";	  // The SQL text.
		PreparedStatement querySt 	= null;	  // The query handle.
		ResultSet		  answers 	= null;	  // A cursor.

		float price = 0;				// holds the price of the book
		float total = 0;				// the total cost the user will pay
		String club = "";				// the club of the book that was offered

		queryText =
			"SELECT MIN(o.price), o.club"
          + "    FROM yrb_offer o, yrb_member m, yrb_book b"
          + "    WHERE o.club = m.club"
		  + "	 	AND o.title = b.title"
		  + "		AND o.year = b.year"
		  + "		AND m.cid = ?"
		  + "		AND o.title = ?"
		  + "		AND o.year = ?"
		  + "		AND b.cat = ?"
		  + "		GROUP BY o.club"
		  + "	 ORDER BY MIN(o.price)";

		// Prepare the query.
		try
		{
			querySt = conDB.prepareStatement(queryText);
		}
		catch(SQLException e)
		{
			System.out.println("[!] Error preparing query (min_price)");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Execute the query.
        try 
		{
            querySt.setInt(1, custID.intValue());
			querySt.setString(2, title);
			querySt.setString(3, year);
			querySt.setString(4, category);
            answers = querySt.executeQuery();
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error in execute (min_price)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Variables do hole the decimal format and the quantity.
        DecimalFormat df = new DecimalFormat("####0.00");    // make format for 2 decimal places
		int qnty = 0;										 // the quantity of books
 
        // Walk through the results and present them.
        try 
		{
            if (answers.next()) 
	    		price = answers.getFloat("1");
	    	
			System.out.print("\n(" + title + ", $" + price + ")\n");
			System.out.println("Enter the amount of books that you would like to purchase:");

			boolean check = false;
			while (!check)
			{
				Scanner qnty_scanner = new Scanner(System.in);
				qnty = qnty_scanner.nextInt();	
				// the amount of books user wants to purchase must be greater than zero
				if (qnty > 0)
				{
					check = true;				// if positive quantity entered, exit loop
					total = (float) qnty * (float) price;	// total cost
					club = answers.getString("club");		// club of the offered book
				}
				else
					System.out.println("Enter a positive value: ");
			}
			System.out.println("\nThe total price is $" + df.format(total) + "\n");
			System.out.println("Make Purchase? [Y/N]");
			Scanner choice = new Scanner(System.in);
			String make_purchase = choice.nextLine();
			// keep looping until user enters Y or N
			while (!make_purchase.equals("Y") && !make_purchase.equals("N"))
			{
				System.out.println("Please Enter 'Y' or 'N'");
				Scanner select = new Scanner(System.in);
				make_purchase = select.nextLine();
			}
			String decision = "";
			// if user wants to make purchase
			if (make_purchase.equals("Y"))
			{
				insert_purchase(club, title, year, qnty);
				System.out.println("You have purchased " +qnty + "copies of " + title + "\n");
				System.out.println("Would you like to look for another book? [Y/N]");
				Scanner scanner = new Scanner(System.in);
				decision = scanner.nextLine();
				// keep looping until user enters Y or N
				while (!decision.equals("Y") && !decision.equals("N"))
				{
					System.out.println("Please Enter 'Y' or 'N'");
					Scanner sc = new Scanner(System.in);
					decision = sc.nextLine();
				}
				// if user wants to search for another book
				if (decision.equals("Y"))
				{
					fetch_categories();
					find_book();
				}
				// if user is done looking for books to purchase
				else if (decision.equals("N"))
				{
					System.out.println("-+-+-+-+-+-+-+-+-+-+-");
					System.out.println("Goodbye!");
					System.out.println("-+-+-+-+-+-+-+-+-+-+-");
				}
			}
			// if the user did not want to commit to the purchase
			else
			{
				System.out.println("Would you like to look for another book? [Y/N]");
				Scanner scanner = new Scanner(System.in);
				decision = scanner.nextLine();
				// keep looping until user enters Y or N
				while (!decision.equals("Y") && !decision.equals("N"))
				{
					System.out.println("Please Enter 'Y' or 'N'");
					Scanner sc = new Scanner(System.in);
					decision = sc.nextLine();
				}
				// if user wants to search for another book
				if (decision.equals("Y"))
				{
					fetch_categories();
					find_book();
				}
				// if user is done looking for books to purchase
				else if (decision.equals("N"))
				{
					System.out.println("-+-+-+-+-+-+-+-+-+-+-");
					System.out.println("Goodbye!");
					System.out.println("-+-+-+-+-+-+-+-+-+-+-");
				}
			}
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error: failed in cursor. (min_price)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // Close the cursor.
        try 
		{
            answers.close();
        } 
		catch(SQLException e) 
		{
			System.out.print("[!] Error: failed closing cursor. (min_price)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
 		 
        // We're done with the handle.
        try 
		{
            querySt.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("[!] Error: failed closing the handle. (min_price)\n");    
            System.out.println(e.toString());
            System.exit(0);
        }
	}

	public void insert_purchase(String club, String title, String year, int qnty)
	{
		String			  queryText = "";	  // The SQL text.
		PreparedStatement querySt 	= null;	  // The query handle.
		int				  answers 	= 0;	  // A cursor.

		queryText =
			"INSERT INTO yrb_purchase (cid,club,title,year,when,qnty) VALUES"
          + "    (?, ?, ?, ?, ?, ?)";

		// Prepare the query.
		try
		{
			querySt = conDB.prepareStatement(queryText);
		}
		catch(SQLException e)
		{
			System.out.println("[!] Error preparing query");
			System.out.println(e.toString());
			System.exit(0);
		}

		// Variables to hold the current time.
		DateFormat when = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
		Calendar calendar = Calendar.getInstance();

		// Execute the query.
        try 
		{
            querySt.setInt(1, custID.intValue());
			querySt.setString(2, club);
			querySt.setString(3, title);
			querySt.setString(4, year);
			querySt.setString(5, when.format(calendar.getTime()));
			querySt.setInt(6, qnty);
			answers = querySt.executeUpdate();
        } 
		catch(SQLException e) 
		{
            System.out.println("[!] Error in execute (insert_purchase)");
            System.out.println(e.toString());
            System.exit(0);
        }
 
        // We're done with the handle.
        try 
		{
            querySt.close();
        } 
		catch(SQLException e) 
		{
            System.out.print("[!] Error: failed closing the handle. (insert_purchase)\n");
            System.out.println(e.toString());
            System.exit(0);
        }
	}

    public static void main(String[] args) 
    {
        Project2 ct = new Project2(args);
    }
}
