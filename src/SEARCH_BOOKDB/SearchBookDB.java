package SEARCH_BOOKDB;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.*;

import COMMON_BOOKDB.FileIO;
import COMMON_BOOKDB.Book;

public class SearchBookDB {

	private JFrame frmSearchBookDatabase;
	private JLabel lblSearchForBook;
	private JLabel lblPickSearchAttribute;
	private JComboBox<String> comboBoxAttribute;
	private JTextField textFieldSearchValue;
	private JLabel lblEnterSearchValue;
	private JButton btnSearch;
	private JScrollPane scrollPaneSearchResults;
	private JLabel lblBookSearchResults;
	private JTextArea textAreaSearchResults;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchBookDB window = new SearchBookDB();
					window.frmSearchBookDatabase.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SearchBookDB() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		/** Create frame content */
		frmSearchBookDatabase = new JFrame();
		frmSearchBookDatabase.setResizable(false);
		frmSearchBookDatabase.setTitle("Search Book Database");
		frmSearchBookDatabase.setBounds(100, 100, 537, 417);
		frmSearchBookDatabase.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSearchBookDatabase.getContentPane().setLayout(null);
		
		lblSearchForBook = new JLabel("Search for Book in Library");
		lblSearchForBook.setHorizontalAlignment(SwingConstants.CENTER);
		lblSearchForBook.setFont(new Font("Times New Roman", Font.BOLD, 28));
		lblSearchForBook.setBounds(102, 11, 327, 33);
		frmSearchBookDatabase.getContentPane().add(lblSearchForBook);
		
		/** Add search content */
		lblPickSearchAttribute = new JLabel("Select Attribute to Search:");
		lblPickSearchAttribute.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		lblPickSearchAttribute.setBounds(20, 65, 188, 22);
		frmSearchBookDatabase.getContentPane().add(lblPickSearchAttribute);
		
		String [] bookAttributeList = {"All", "ISBN Number", "Title", "Author", "Publisher", "Month and Year", "Edition"};
		comboBoxAttribute = new JComboBox(bookAttributeList);
		lblPickSearchAttribute.setLabelFor(comboBoxAttribute);
		comboBoxAttribute.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		comboBoxAttribute.setBounds(219, 66, 151, 20);
		frmSearchBookDatabase.getContentPane().add(comboBoxAttribute);
		
		textFieldSearchValue = new JTextField();
		textFieldSearchValue.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		textFieldSearchValue.setBounds(219, 109, 277, 20);
		textFieldSearchValue.setColumns(10);
		frmSearchBookDatabase.getContentPane().add(textFieldSearchValue);
		
		btnSearch = new JButton("Search");
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				try {
					btnSearch_CLICK();
				}
				catch (Exception e) {
					System.out.println(e.toString());
				}
			}
		});
		btnSearch.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		btnSearch.setBounds(188, 152, 151, 28);
		frmSearchBookDatabase.getContentPane().add(btnSearch);
		
		lblEnterSearchValue = new JLabel("Enter Value to Search:");
		lblEnterSearchValue.setLabelFor(textFieldSearchValue);
		lblEnterSearchValue.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		lblEnterSearchValue.setBounds(20, 112, 188, 14);
		frmSearchBookDatabase.getContentPane().add(lblEnterSearchValue);
		
		scrollPaneSearchResults = new JScrollPane();
		scrollPaneSearchResults.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneSearchResults.setViewportBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		scrollPaneSearchResults.setBounds(10, 186, 511, 192);
		frmSearchBookDatabase.getContentPane().add(scrollPaneSearchResults);
		
		lblBookSearchResults = new JLabel("Book Search Results:");
		lblBookSearchResults.setFont(new Font("Times New Roman", Font.PLAIN, 18));
		scrollPaneSearchResults.setColumnHeaderView(lblBookSearchResults);
		
		textAreaSearchResults = new JTextArea();
		textAreaSearchResults.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		scrollPaneSearchResults.setViewportView(textAreaSearchResults);
		
	} // End of method initialize()
	
	private void btnSearch_CLICK() {
		
		// Refresh the text area of search results --> make it blank
		textAreaSearchResults.setText("");

		//-------------- First, validate all the text fields
		// If any problem, a dialog warning pops up to stop the program
		boolean isValidated = validateTextFields();
		
		if (! isValidated) return;
				
				
		//-------------- All the text fields have been validated
		FileIO fileIOHandler = new FileIO();
		
		// Declare the target file database: bookDatabase.txt
		// MUST use \ to qualify '\' in the path of the file
		File fileDB = new File("E:\\\\JAVA\\OUTPUTS\\bookDatabase.txt");
		
		int numBooksInDb = 0; // number of books in DB
		int numFoundBooks = 0; // number of found books
		String[] booksDbArray = null; // Array of all book records in DB --> array of String
		Book [] foundBooksArray = new Book[FileIO.getMaxNumLines()]; // Array of found books --> array of Book
				
		try {			
			// Read all the records from book file database 
			numBooksInDb = fileIOHandler.readLinesFromFile(fileDB);
			booksDbArray = fileIOHandler.getStrLinesArray();
			
			// Get the selected item from ComboBox
			String strSelectedAttribute = (String) comboBoxAttribute.getSelectedItem();
			
			// Get the search value
			String strIsbn = textFieldSearchValue.getText();
			
			// Invoke searchBooks()
			numFoundBooks = searchBooks (numBooksInDb, booksDbArray, foundBooksArray, strSelectedAttribute, strIsbn);
			
			// If no matched book is found, display warning
			if (numFoundBooks == 0) {
				textAreaSearchResults.append("No matched book is found in the database. \n");
			}

		}
		catch (IOException ex){ 
      		ex.printStackTrace();
    	}

	} // End of method btnSearch_CLICK()

	/****************************
	 * Name: validateTextFields
	 * Parameters: None
	 * Return: boolean
	 * --> TRUE: The search value text field is successfully validate
	 * --> FALSE: The text field has failed the validation
	 * Description:
	 * --> This method verify to be sure the search value text field contains valid data: 
	 * --> Valid data: not null, not zero-size data, not empty String, not filled only with blank space
	 * --> If ISBN is selected as search attribute: valid data must also be numeric, i.e. only consisting of digits
	 * 
	 ****************************/
	 
	private boolean validateTextFields() {
		
		boolean isValidated = true;
		
		//----------- Validate the text field of search value
		// Need to validate for every search attribute except for "All"
		
		if( ! comboBoxAttribute.getSelectedItem().equals("All")) {
			
			try{
				Validate.notBlank(textFieldSearchValue.getText());
			}catch(Exception e){
				JOptionPane.showMessageDialog(frmSearchBookDatabase, "The text field of search value must have valid values - Cannot be blank !!!.");
				textFieldSearchValue.requestFocusInWindow(); // make it ready to enter the value
				textFieldSearchValue.selectAll(); // select all text in the text field to delete it or to replace it
				isValidated = false;
			}
		}
		
		// If any problem, stop the program
		if (! isValidated) return (isValidated);
		
		// For ISBN, also need to verify the entered value is a valid numeric
		if(comboBoxAttribute.getSelectedItem().equals("ISBN")){
    		try{
    			Long.parseLong(textFieldSearchValue.getText());
    		}catch(Exception e){
    			JOptionPane.showMessageDialog(frmSearchBookDatabase, "ISBN must have a Numeric Value.");
				textFieldSearchValue.requestFocusInWindow(); // make it ready to enter the value
				textFieldSearchValue.selectAll(); // select all text in the text field to delete it ot to replace it
				isValidated = false;
    		}
    	}
		
		return (isValidated);
		
	} // validateTextFields


	/*******************
	 * Name: searchBooks
	 * Parameters:
	 * --> numBooks: number of books in the bookArray
	 * --> booksArray: array of strings, each string is a book record (in DB) of CSV format
	 * --> foundBooksArray: array of class Book elements that are found in the database
	 * --> aStrSearchAttr: a String that represents an attribute that is used to search
	 * --> aStrSearchValue: a String that represents the value of the attribute used to search
	 * Return
	 * --> This method returns an array of Book objects - books that are found in the database
	 * Description:
	 * This method performs a search for books whose attribute has the value that is 
	 * matched with books in the book array
	********************/
	
	private int searchBooks(int numBooks, 
								String[] booksArray, 
								Book[] foundBooksArray, 
								String aStrSearchAttr, 
								String aStrSearchValue) {
		
		int numFoundBooks = 0;
		
		if (aStrSearchAttr.equals("All")) {
			
			// ISBN is used to search for books
			numFoundBooks = searchBookByAll(numBooks, booksArray, foundBooksArray);
			
		} // End of if (ISBN)
		
		if (aStrSearchAttr.equals("ISBN Number")) {
		
			// ISBN is used to search for books
			numFoundBooks = searchBookByIsbn(numBooks, aStrSearchValue, booksArray, foundBooksArray);
			
		} // End of if (ISBN)
			
		if (aStrSearchAttr.equals("Title")) {
				
			// Title is used to search for books
			numFoundBooks = searchBookByTitle(numBooks, aStrSearchValue, booksArray, foundBooksArray);
				
				
		} // End of if (Title)
			
			
		if (aStrSearchAttr.equals("Author")) {
				
			// Author is used to search for books
			numFoundBooks = searchBookByAuthor(numBooks, aStrSearchValue, booksArray, foundBooksArray);
				
		} // End of if (Author)
			
			
		if (aStrSearchAttr.equals("Publisher")) {
				
			// Publisher is used to search for books
			numFoundBooks = searchBookByPublisher(numBooks, aStrSearchValue, booksArray, foundBooksArray);
			
		} // End of if (Publisher)
			
			
		if (aStrSearchAttr.equals("Month and Year")) {
				
			// Month&Year is used to search for books
			numFoundBooks = searchBookByMonthYear(numBooks, aStrSearchValue, booksArray, foundBooksArray);
				
		} // End of if (Month&Year)
			
			
		if (aStrSearchAttr.equals("Edition")) {
				
			// Edition is used to search for books
			numFoundBooks = searchBookByEdition(numBooks, aStrSearchValue, booksArray, foundBooksArray);
				
		} // End of if (Edition)
			
		
		return (numFoundBooks);
		
		
	} // End of searchBooks
	
	
	private int searchBookByAll(int numBooks, String[] booksArray, Book[] foundBooksArray) {
		
		int numFoundBooks = 0;
		String aStrBookRecord = "";
		
		for (int i = 0; i < numBooks; i++) {
			
			aStrBookRecord = booksArray[i];
	
			// a book is found --> add book into found-book array
			Book aFoundBook = new Book (aStrBookRecord);
				
			foundBooksArray[numFoundBooks] = aFoundBook;
				
			// Increment numFoundBooks to indicate one more book is found
			numFoundBooks++;
							
		} // End of for (scan book array)

		// Write book record of each found book into the text area (book search results)
		for (int j = 0; j < numFoundBooks; j++) {
			
			// Append a book record into the search results text area
			textAreaSearchResults.append((foundBooksArray[j]).toString() + "\n"); 
			
		}
		
		
		return (numFoundBooks);

		
	} // End of searchBookByAll
	
	
	private int searchBookByIsbn(int numBooks, String strIsbn, String[] booksArray, Book[] foundBooksArray) {
		
		Book aBook;
		int numFoundBooks = 0;
		String aStrBookRecord = "";
		
		for (int i = 0; i < numBooks; i++) {
			
			aStrBookRecord = booksArray[i];
			aBook = Book.recreateBookFromString(aStrBookRecord);
	
			// First convert string value to long
			long anIsbn = Long.parseLong(strIsbn);
			
			if (anIsbn == aBook.getIsbn()){
				// a book is found --> add book into found-book array
				Book aFoundBook = new Book (aStrBookRecord);
				
				foundBooksArray[numFoundBooks] = aFoundBook;
				
				// Increment numFoundBooks to indicate one more book is found
				numFoundBooks++;
			}			
		
		} // End of for (scan book array)

		// Write book record of each found book into the text area (book search results)
		for (int j = 0; j < numFoundBooks; j++) {
			
			// Append a book record into the search results text area
			textAreaSearchResults.append((foundBooksArray[j]).toString() + "\n"); 
			
		}
		
		
		return (numFoundBooks);
		
		
	} // End of searchBookByIsbn
	

	private int searchBookByTitle(int numBooks, String strTitle, String[] booksArray, Book[] foundBooksArray) {
		
		Book aBook;
		String strDbBookTitle = ""; // Title of book in database
		int numFoundBooks = 0;
		String aStrBookRecord = "";
		
		for (int i = 0; i < numBooks; i++) {
			
			aStrBookRecord = booksArray[i];
			aBook = Book.recreateBookFromString(aStrBookRecord);
	
			strDbBookTitle = aBook.getTitle(); // Get title of the book in db
			
			if ((strDbBookTitle.equals(strTitle))){
				// a book is found --> add book into found-book array
				Book aFoundBook = new Book (aStrBookRecord);
				
				foundBooksArray[numFoundBooks] = aFoundBook;
				
				// Increment numFoundBooks to indicate one more book is found
				numFoundBooks++;
			}			
		
		} // End of for (scan book array)

		
		// Write book record of each found book into the text area (book search results)
		for (int j = 0; j < numFoundBooks; j++) {
			
			// Append a book record into the search results text area
			textAreaSearchResults.append((foundBooksArray[j]).toString() + "\n"); 
			
		}

		
		
		return (numFoundBooks);
		
		
	} // End of searchBookByTitle
	

	private int searchBookByAuthor(int numBooks, String strAuthor, String[] booksArray, Book[] foundBooksArray) {
		
		Book aBook;
		String strDbBookAuthor = ""; // Author of book in database
		int numFoundBooks = 0;
		String aStrBookRecord = "";
		
		for (int i = 0; i < numBooks; i++) {
			
			aStrBookRecord = booksArray[i];
			aBook = Book.recreateBookFromString(aStrBookRecord);
	
			strDbBookAuthor = aBook.getAuthor(); // Get author of the book in db
			
			if ((strDbBookAuthor.equals(strAuthor))){
				// a book is found --> add book into found-book array
				Book aFoundBook = new Book (aStrBookRecord);
				
				foundBooksArray[numFoundBooks] = aFoundBook;
				
				// Increment numFoundBooks to indicate one more book is found
				numFoundBooks++;
			}			
		
		} // End of for (scan book array)
		
		// Write book record of each found book into the text area (book search results)
		for (int j = 0; j < numFoundBooks; j++) {
			
			// Append a book record into the search results text area
			textAreaSearchResults.append((foundBooksArray[j]).toString() + "\n"); 
			
		}

		return (numFoundBooks);
		
		
	} // End of searchBookByAuthor	


	private int searchBookByPublisher(int numBooks, String strPublisher, String[] booksArray, Book[] foundBooksArray) {
		
		Book aBook;
		String strDbBookPublisher = ""; // Publisher of book in database
		int numFoundBooks = 0;
		String aStrBookRecord = "";
		
		for (int i = 0; i < numBooks; i++) {
			
			aStrBookRecord = booksArray[i];
			aBook = Book.recreateBookFromString(aStrBookRecord);
	
			strDbBookPublisher = aBook.getPublisher(); // Get publisher of the book in db
			
			if ((strDbBookPublisher.equals(strPublisher))){
				// a book is found --> add book into found-book array
				Book aFoundBook = new Book (aStrBookRecord);
				
				foundBooksArray[numFoundBooks] = aFoundBook;
				
				// Increment numFoundBooks to indicate one more book is found
				numFoundBooks++;
			}			
		
		} // End of for (scan book array)

		// Write book record of each found book into the text area (book search results)
		for (int j = 0; j < numFoundBooks; j++) {
			
			// Append a book record into the search results text area
			textAreaSearchResults.append((foundBooksArray[j]).toString() + "\n"); 
			
		}

		return (numFoundBooks);
		
		
	} // End of searchBookByPublisher	


	private int searchBookByMonthYear(int numBooks, String strMonthYear, String[] booksArray, Book[] foundBooksArray) {
		
		Book aBook;
		String strDbBookMonthYear = ""; // Month and year of book in database
		int numFoundBooks = 0;
		String aStrBookRecord = "";
		
		for (int i = 0; i < numBooks; i++) {
			
			aStrBookRecord = booksArray[i];
			aBook = Book.recreateBookFromString(aStrBookRecord);
	
			strDbBookMonthYear = aBook.getMonth_year(); // Get month & year of the book in db
			
			if ((strDbBookMonthYear.equals(strMonthYear))){
				// a book is found --> add book into found-book array
				Book aFoundBook = new Book (aStrBookRecord);
				
				foundBooksArray[numFoundBooks] = aFoundBook;
				
				// Increment numFoundBooks to indicate one more book is found
				numFoundBooks++;
			}			
		
		} // End of for (scan book array)


		// Write book record of each found book into the text area (book search results)
		for (int j = 0; j < numFoundBooks; j++) {
			
			// Append a book record into the search results text area
			textAreaSearchResults.append((foundBooksArray[j]).toString() + "\n"); 
			
		}

		return (numFoundBooks);
		
		
	} // End of searchBookByMonthYear	


	private int searchBookByEdition(int numBooks, String strEdition, String[] booksArray, Book[] foundBooksArray) {
		
		Book aBook;
		String strDbBookEdition = ""; // Edition of book in database
		int numFoundBooks = 0;
		String aStrBookRecord = "";
		
		for (int i = 0; i < numBooks; i++) {
			
			aStrBookRecord = booksArray[i];
			aBook = Book.recreateBookFromString(aStrBookRecord);
	
			strDbBookEdition = aBook.getEdition(); // Get edition of the book in db
			
			if ((strDbBookEdition.equals(strEdition))){
				// a book is found --> add book into found-book array
				Book aFoundBook = new Book (aStrBookRecord);
				
				foundBooksArray[numFoundBooks] = aFoundBook;
				
				// Increment numFoundBooks to indicate one more book is found
				numFoundBooks++;
			}
		
		} // End of for (scan book array)

				
		// Write book record of each found book into the text area (book search results)
		for (int j = 0; j < numFoundBooks; j++) {
			
			// Append a book record into the search results text area
			textAreaSearchResults.append((foundBooksArray[j]).toString() + "\n"); 
			
		}

		return (numFoundBooks);
		
		
	} // End of searchBookByEdition	
	
} // End of class SearchBookDB
