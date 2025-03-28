package utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class CommonUtils {

	private static Connection connection1 = null;
	private static boolean emailHighImp = false, jobAbend = false, logDebugMsgs, testCaseOverrideValidation, reqOverrideValidation, reqIDExist;
	private static final String outputFormat = "%-38s: %s";
	private static final String reqTCValidationMessage = "Verify Requirement ID for the Test case";
	private static final String subsystemTCValidationMessage = "Verify Subsystem for the Test case";
	private static final String groupingTCValidationMessage = "Verify Grouping for the Test case";
	private static final String bfunctionTCValidationMessage = "Verify Business Function for the Test case";
	private static final String envTCValidationMessage = "Verify Environment for the Test case";
	private static final String idReqValidationMessage = "Requirement ID already exists";
	private static final String nameReqValidationMessage = "Requirement name already exists";
	private static final String typeReqValidationMessage = "Verify type for the requirement";
	private static final String subsystemReqValidationMessage = "Verify subsystem for the requirement";
	private static final String rtmReqValidationMessage = "Verify RTM for the requirement";
	private static final String namOwnerReqValidationMessage = "Verify owner name and access for the requirement";
	private static final String dbPassKey = "$2a$12$RD5XIG/SNGzmmGJx3XcrSu";
	private static final String dbPassword = "q6DF+7CE74bePLsOz0CXX7G5z+mAJRbeWJ2Hba5ef0nX+YVsg/W+0xsKcXVHZNHx6tG73Q==";
	private static final String dbUserName = "rvattumi";
	private static final String dbHost = "10.196.210.13:1521:";
	private static final String dbService = "madocp1";
	private static final String dbURL = "jdbc:oracle:thin:@" + dbHost + dbService;
	private static final String dbDriver = "oracle.jdbc.OracleDriver";
	private static final String sshotPath = System.getProperty("user.dir") + "\\Screenshots\\";
	private static Instant start, finish;
	private static String replaceType;

	public static boolean isReqIDExist() {
		return reqIDExist;
	}

	public static void setReqIDExist(boolean reqIDExist) {
		CommonUtils.reqIDExist = reqIDExist;
	}

	private static boolean isEmailHighImp() {
		return emailHighImp;
	}

	private static String getReplaceType() {
		return replaceType;
	}

	public static void setReplaceType(String replaceType) {
		CommonUtils.replaceType = replaceType;
	}

	private static boolean isTestCaseOverrideValidation() {
		return testCaseOverrideValidation;
	}

	public static void setTestCaseOverrideValidation(boolean testCaseOverrideValidation) {
		CommonUtils.testCaseOverrideValidation = testCaseOverrideValidation;
	}
	
	private static boolean isReqOverrideValidation() {
		return reqOverrideValidation;
	}

	public static void setReqOverrideValidation(boolean reqOverrideValidation) {
		CommonUtils.reqOverrideValidation = reqOverrideValidation;
	}

	public static void setEmailHighImp(boolean setEmailHigh) {
		CommonUtils.emailHighImp = setEmailHigh;
	}

	public static boolean isJobAbend() {
		return jobAbend;
	}

	public static void setJobAbend(boolean jobAbend) {
		CommonUtils.jobAbend = jobAbend;
	}
	
	public static boolean isLogDebugMsgs() {
		return logDebugMsgs;
	}

	public static void setLogDebugMsgs(boolean logDebugMsgs) {
		CommonUtils.logDebugMsgs = logDebugMsgs;
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: executeQuery()                                        */
	/*                                                                      */
	/*   Description: This function executes sql query and returns list of  */
	/*                values for fields passed to the function else null    */
	/*                                                                      */
	/*    Parameters: sqlStatement, ArrayList<String> fields                */
	/*                                                                      */
	/*       Returns: ArrayList<String>                                     */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of executeQuery()         */
	/*                                                                      */
	/************************************************************************/

	public static ArrayList<String> executeQuery(String sqlStatement, ArrayList<String> fields) throws SQLException {

		if(isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Function", "executeQuery()"));
			System.out.println(String.format(outputFormat, "SQL Statement", sqlStatement));
		}
		ArrayList<String> colMIP = new ArrayList<String>();
		Statement st = null;
		ResultSet res = null;
		Connection connection1 = getDBConnection();
		st = connection1.createStatement();
		start = Instant.now();
		res = st.executeQuery(sqlStatement);
		finish = Instant.now();
		long execTime = Duration.between(start, finish).toMillis();
		int counter = 0;
		if(isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Query Execution Time(ms)", execTime));
		}
		if (res.next()) {
			res = st.executeQuery(sqlStatement);
			while (res.next()) {
				for (int i = 0; i < fields.size(); i++) {
					colMIP.add(res.getString(fields.get(i)).trim());
					counter++;
				}
			}
			fields.clear();
			if(isLogDebugMsgs()) {
				System.out.println(String.format(outputFormat, "Total No.of Columns fetched", counter));
			}
			st.close();
			res.close();
			return colMIP;
		} else {
			fields.clear();
			if(isLogDebugMsgs()) {
				System.out.println(String.format(outputFormat, "Total No.of Columns fetched", counter));
			}
			st.close();
			res.close();
			return null;
		}
	}
	
	/************************************************************************/
	/*                                                                      */
	/* Function Name: validateTCData()                                      */
	/*                                                                      */
	/*   Description: This function executes set of validations for TC and  */
	/*                returns boolean value                                 */
	/*                                                                      */
	/*    Parameters: idReq, subsystemTC, groupingTC, bFunctionTC, envTC,   */
	/*                                                                      */
	/*       Returns: boolean                                               */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 11/19/22  R.Vattumilli    Initial Creation of validateTCData()       */
	/*                                                                      */
	/* 09/05/24  R.Vattumilli    Modified Validation                        */
	/*                                                                      */
	/* 10/31/24  R.Vattumilli    Added override switch for validation       */
	/*                                                                      */
	/* 03/05/24  R.Vattumilli    Added TC specific override variable        */
	/*                                                                      */
	/************************************************************************/
	
	public static boolean validateTCData(String idReq, String subsystemTC, String groupingTC, String bFunctionTC, String envTC) throws Exception {

		if(isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Function", "validateTCData()"));
		}
		int req, subsystem, grpng, bf, env;
		ArrayList<String> colValues  = new ArrayList<String>();
		ArrayList<String> colNames   = new ArrayList<String>();
		String validateTCInfo = "select case when (select count(*) from requirement e where trim(e.id_req) = '" + idReq + "') = 1 then 1 else 0 end as req, "
								+ "case when (select count(*) from subsystem_area f where trim(f.nam) = '" + subsystemTC + "') = 1 then 1 else 0 end as subsystem, "
								+ "case when (select count(*) from test_case_grouping g where trim(g.nam) = '" + groupingTC + "') = 1 then 1 else 0 end as grpng, "
								+ "case when (select count(*) from groop h where trim(h.nam_group) = '" + bFunctionTC + "') = 1 then 1 else 0 end as bf, "
								+ "case when (select count(*) from test_case_env i where trim(i.ind_environment) = '" + envTC + "') = 1 then 1 else 0 end as env from dual";		
		colNames.add("REQ");
		colNames.add("SUBSYSTEM");
		colNames.add("GRPNG");
		colNames.add("BF");
		colNames.add("ENV");
		colValues = executeQuery(validateTCInfo, colNames);
		req  = Integer.parseInt(colValues.get(0));
		subsystem = Integer.parseInt(colValues.get(1));
		grpng = Integer.parseInt(colValues.get(2));
		bf = Integer.parseInt(colValues.get(3));
		env = Integer.parseInt(colValues.get(4));
		boolean reqStatus = false;
		boolean subsystemStatus = false;
		boolean groupingStatus = false;
		boolean bfStatus = false;
		boolean envStatus = false;		
		if(req != 1 && !(idReq.equals(""))) {
			System.out.println(String.format(outputFormat, "Requirement", idReq));			
			System.out.println(String.format(outputFormat, "Requirement Validation", reqTCValidationMessage));
			reqStatus = true;
		}
		if(subsystem != 1) {
			System.out.println(String.format(outputFormat, "Subsystem", subsystemTC));
			System.out.println(String.format(outputFormat, "Subsystem Validation", subsystemTCValidationMessage));
			subsystemStatus = true;
		}
		if(grpng != 1) {
			System.out.println(String.format(outputFormat, "Grouping", groupingTC));
			System.out.println(String.format(outputFormat, "Grouping Validation", groupingTCValidationMessage));
			groupingStatus = true;
		}
		if(bf != 1) {
			System.out.println(String.format(outputFormat, "Business Function", bFunctionTC));
			System.out.println(String.format(outputFormat, "Business Function Validation", bfunctionTCValidationMessage));
			bfStatus = true;
		}
		if(env != 1) {
			System.out.println(String.format(outputFormat, "Environment", envTC));
			System.out.println(String.format(outputFormat, "Environment Validation", envTCValidationMessage));
			envStatus = true;
		}
		if(reqStatus || subsystemStatus || groupingStatus || bfStatus || envStatus) {
			if(!isTestCaseOverrideValidation()) {
				return false;
			}
		}
		return true;
	}
	
	/************************************************************************/
	/*                                                                      */
	/* Function Name: validateReqData()                                     */
	/*                                                                      */
	/*   Description: This function executes set of validations for Req and */
	/*                returns boolean value                                 */
	/*                                                                      */
	/*    Parameters: idReq, nameReq, typeReq, subsystemReq, rtmReq,        */
	/*                namOwnerReq                                           */
	/*                                                                      */
	/*       Returns: boolean                                               */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 02/15/24  R.Vattumilli    Initial Creation of validateReqData()      */
	/*                                                                      */
	/* 10/31/24  R.Vattumilli    Added override switch for validation       */
	/*                                                                      */
	/* 03/05/24  R.Vattumilli    Added Req specific override variable       */
	/*                                                                      */
	/************************************************************************/
	
	public static boolean validateReqData(String idReq, String nameReq, String typeReq, String subsystemReq, String rtmReq, String namOwnerReq) throws Exception {

		if(isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Function", "validateReqData()"));
		}
		int idReqCount, nameReqCount, typeReqCount, subsystemReqCount, rtmReqCount, namOwnerReqCount;
		ArrayList<String> colValues  = new ArrayList<String>();
		ArrayList<String> colNames   = new ArrayList<String>();
		String validateReqInfo = "select case when (select count(*) from requirement a where trim(a.id_req) = '" + idReq + "') >= 1 then 1 else 0 end as IDREQSTATUS, "
								+ "case when (select count(*) from requirement b where trim(b.nam_req) = '" + nameReq + "') >= 1 then 1 else 0 end as NAMEREQSTATUS, "
								+ "case when (select count(*) from requirement_type c where trim(c.nam) = '" + typeReq + "') = 1 then 1 else 0 end as TYPEREQSTATUS, "
								+ "case when (select count(*) from subsystem_area d where trim(d.nam) = '" + subsystemReq + "') = 1 then 1 else 0 end as SUBSYSTEMREQSTATUS, "
								+ "case when (select count(*) from rtm e where trim(e.sak_rtm) = '" + rtmReq + "') = 1 then 1 else 0 end as RTMREQSTATUS, "
								+ "case when (select count(*) from object_owner f where ind_req_status = 'Y' and trim(f.nam_owner) = '" + namOwnerReq + "') = 1 then 1 else 0 end as NAMOWNERREQSTATUS from dual";		
		colNames.add("IDREQSTATUS");
		colNames.add("NAMEREQSTATUS");
		colNames.add("TYPEREQSTATUS");
		colNames.add("SUBSYSTEMREQSTATUS");
		colNames.add("RTMREQSTATUS");
		colNames.add("NAMOWNERREQSTATUS");
		colValues = executeQuery(validateReqInfo, colNames);
		idReqCount  = Integer.parseInt(colValues.get(0));
		nameReqCount = Integer.parseInt(colValues.get(1));
		typeReqCount = Integer.parseInt(colValues.get(2));
		subsystemReqCount = Integer.parseInt(colValues.get(3));
		rtmReqCount = Integer.parseInt(colValues.get(4));
		namOwnerReqCount = Integer.parseInt(colValues.get(5));
		boolean idReqStatus = false;
		boolean nameReqStatus = false;
		boolean typeReqStatus = false;
		boolean subsystemReqStatus = false;
		boolean rtmReqStatus = false;
		boolean namOwnerReqStatus = false;
		setReqIDExist(false);
		if(idReqCount != 0 && !(idReq.equals(""))) {
			System.out.println(String.format(outputFormat, "Requirement ID", idReq));
			System.out.println(String.format(outputFormat, "Requirement ID Validation", idReqValidationMessage));
			idReqStatus = true;
		}
		if(nameReqCount != 0 && !(nameReq.equals(""))) {
			System.out.println(String.format(outputFormat, "Requirement Name", nameReq));
			System.out.println(String.format(outputFormat, "Requirement Name Validation", nameReqValidationMessage));
			nameReqStatus = true;
		}
		if(typeReqCount != 1) {
			System.out.println(String.format(outputFormat, "Requirement Type", typeReq));
			System.out.println(String.format(outputFormat, "Requirement Type Validation", typeReqValidationMessage));
			typeReqStatus = true;
		}
		if(subsystemReqCount != 1) {
			System.out.println(String.format(outputFormat, "Requirement Subsystem", subsystemReq));
			System.out.println(String.format(outputFormat, "Requirement Subsystem Validation", subsystemReqValidationMessage));
			subsystemReqStatus = true;
		}
		if(rtmReqCount != 1 && !(rtmReq.equals(""))) {
			System.out.println(String.format(outputFormat, "Requirement RTM", rtmReq));
			System.out.println(String.format(outputFormat, "Requirement RTM Validation", rtmReqValidationMessage));
			rtmReqStatus = true;
		}
		if(namOwnerReqCount != 1) {
			System.out.println(String.format(outputFormat, "Requirement Owner", rtmReq));
			System.out.println(String.format(outputFormat, "Requirement Owner Validation", namOwnerReqValidationMessage));
			namOwnerReqStatus = true;
		}				
		/*if(nameReqStatus || typeReqStatus || subsystemReqStatus || rtmReqStatus || namOwnerReqStatus) {
			if(!isReqOverrideValidation()) {
				return false;
			}
		}*/
		if(nameReqStatus) {
			if(!isReqOverrideValidation()) {
				return false;
			}
		}
		if(idReqStatus) {
			setReqIDExist(true);
			return false;
		}
		return true;
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: getDBConnection()                                     */
	/*                                                                      */
	/*   Description: This function returns database connection object for  */
	/*                MIP DB                                                */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: connection1                                           */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of getDBConnection()      */
	/*                                                                      */
	/************************************************************************/

	public static Connection getDBConnection() {
		
		try {
			Class.forName(dbDriver);
			connection1 = DriverManager.getConnection(dbURL, dbUserName, EncryptorAesGcmPassword.decrypt(dbPassword, dbPassKey));
		} catch (Exception e) {
			connection1 = null;
			e.printStackTrace();
		}
		return connection1;
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: sshot()                                               */
	/*                                                                      */
	/*   Description: This function takes screenshot of web app when it is  */
	/*                called during test execution & saves the img to folder*/
	/*                                                                      */
	/*    Parameters: instance of web driver                                */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of sshot()                */
	/*                                                                      */
	/************************************************************************/

	public static void sshot(WebDriver driver) {

		try {
			DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
			File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			System.out.println(String.format(outputFormat, "Screenshot Path", sshotPath));
			FileUtils.copyFile(scrFile, new File(sshotPath + dateFormat.format(new Date()) + ".png"));
			System.out.println("*****************************************************************");
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: getTCReqData()                                        */
	/*                                                                      */
	/*   Description: This function executes sql query and returns list of  */
	/*                values for fields in the query else returns null      */
	/*                                                                      */
	/*    Parameters: sqlStatement, noOfCols, conn                          */
	/*                                                                      */
	/*       Returns: ArrayList<String[]> or null                           */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of getTCReqData()         */
	/*                                                                      */
	/************************************************************************/

	public static ArrayList<String[]> getTCReqData(String sqlStatement, int noOfCols, Connection conn) throws Exception {
		
		ArrayList<String[]> rowList = new ArrayList<String[]>();
		Statement statement = null;
		ResultSet resultSet = null;
		conn.setReadOnly(true);
		statement = conn.createStatement();
		System.out.println("*****************************************************************");
		if(isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Function", "getTCReqData()"));
		}
		System.out.println(String.format(outputFormat, "SQL Statement", sqlStatement));
		System.out.println(String.format(outputFormat, "No.of Cols", noOfCols));
		start = Instant.now();
		resultSet = statement.executeQuery(sqlStatement);
		finish = Instant.now();
		long execTime = Duration.between(start, finish).toMillis();
		System.out.println(String.format(outputFormat, "Query Execution Time(ms)", execTime));
		String[] colList = null;
		int i, counter = 0;
		if (!(resultSet.isBeforeFirst())) {
			System.out.println(String.format(outputFormat, "Total No.of Rows fetched", counter));
			System.out.println("*****************************************************************");
			System.out.println();
			statement.close();
			resultSet.close();
			return null;
		}
		while (resultSet.next()) {
			colList = new String[noOfCols];
			for (i = 0; i < noOfCols; i++) {
				colList[i] = resultSet.getString(i + 1).trim();
			}
			rowList.add(colList);
			counter++;
		}
		System.out.println(String.format(outputFormat, "Total No.of Rows fetched", counter));
		statement.close();
		resultSet.close();
		return rowList;
	}
	
	public static void printNoDataMessage (String message) {

		System.out.println();
		System.out.println("****************************************************");
		System.out.println("*****************                  *****************");
		System.out.println("***********                              ***********");
		System.out.println("*****" + message + "*****");
		System.out.println("***********                              ***********");
		System.out.println("*****************                  *****************");
		System.out.println("****************************************************");
		System.out.println();
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: updateLoadTableData()                                 */
	/*                                                                      */
	/*   Description: This function performs data manipulation in the table */
	/*                when it finds column passed to the function using     */
	/*                select statement                                      */
	/*                                                                      */
	/*    Parameters: SelSql, col, UpdSql                                   */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of updateLoadTableData()  */
	/*                                                                      */
	/************************************************************************/

	public static void updateLoadTableData(String SelSql, String col, String UpdSql) throws SQLException {

		if(isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Function", "updateLoadTableData()"));
			System.out.println(String.format(outputFormat, "Select SQL", SelSql));
			System.out.println(String.format(outputFormat, "Column Name", col));
			System.out.println(String.format(outputFormat, "Update SQL", UpdSql));
		}
		ArrayList<String> colValues = new ArrayList<String>();
		ArrayList<String> colNames = new ArrayList<String>();
		Connection connection = getDBConnection();
		Statement statement = connection.createStatement();
		colNames.add(col);
		colValues = executeQuery(SelSql, colNames);
		if (!(colValues.get(0).equals("null"))) {
			statement.executeUpdate(UpdSql);
		}
		statement.close();
	}
	
	/************************************************************************/
	/*                                                                      */
	/* Function Name: insertCoReqXref()                                     */
	/*                                                                      */
	/*   Description: This function links requirements to WI/Defect/CO      */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 03/19/25  R.Vattumilli    Initial Creation of insertCoReqXref()      */
	/*                                                                      */
	/************************************************************************/
	
	public static void insertCoReqXref() throws SQLException {

		Connection connection = getDBConnection();
		Statement statement = connection.createStatement();
		String insertSql = "insert into doco.co_req_xref select wi_req, sak_req, null, null, null from load_req a where ind_status = 'P' and "
						+ "dte_loaded = to_char(sysdate, 'yyyymmdd') and not exists (select 1 from doco.co_req_xref where sak_req = a.sak_req and sak_csr = a.wi_req)";
		if(isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Function", "insertCoReqXref()"));
			System.out.println(String.format(outputFormat, "Insert SQL", insertSql));
		}
		int rowsInserted = statement.executeUpdate(insertSql);
		System.out.println(String.format(outputFormat, "Total Reqs linked to WI/Defect/CO", rowsInserted));
		statement.close();
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: sendEmail()                                           */
	/*                                                                      */
	/*   Description: This function sends email to the recipients with file */
	/*                attachment and sets high priority using flags         */
	/*                                                                      */
	/*    Parameters: filePath                                              */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date     A.E. Name      Description                                  */
	/* -------- -------------- ---------------------------------------------*/
	/* 12/05/21 R.Vattumilli   Initial creation of sendEmail()              */
	/*                                                                      */
	/* 12/05/21 R.Vattumilli   sends Email with attachment to Receiver(s)   */
	/*                         when it is called                            */
	/*                                                                      */	
	/************************************************************************/

	public static void sendEmail(String filePath) {

		if(CommonUtils.isLogDebugMsgs()) {
			System.out.println(String.format(outputFormat, "Function", "sendEmail()"));
		}
		ArrayList<String> colValues = new ArrayList<String>();
		ArrayList<String> colNames = new ArrayList<String>();
		ArrayList<String> suiteCompleteToRecipients = new ArrayList<String>();
		suiteCompleteToRecipients.add("rajesh.vattumilli@mass.gov");
		//suiteCompleteToRecipients.add("rabik.shrestha@mass.gov");
		StringBuilder finalEmailBody = new StringBuilder();
		String emailSubject;
		if(isJobAbend()) {
			finalEmailBody.append("Loading in MIP is Complete with failure(s). <br>");
			emailSubject = "Loading in MIP is Complete with failure(s) at " + convertSystemDate();
			CommonUtils.setEmailHighImp(true);
		} else {
			finalEmailBody.append("Loading in MIP is Complete. <br>");
			emailSubject = "Loading in MIP is Complete at " + convertSystemDate();
		}
		finalEmailBody.append("<br>");
		finalEmailBody.append("Attached the log file. <br>");
		try {
			setLogDebugMsgs(false);
			String sqlStatement = "select * from rvattumi.v_user_login where type ='Email' and days_to_reset_pwd > 2";
			colNames.add("USER_ID");
			colNames.add("PASSWORD");
			colNames.add("PASSKEY");
			colValues = executeQuery(sqlStatement, colNames);
			final String username = colValues.get(0);
			final String password = EncryptorAesGcmPassword.decrypt(colValues.get(1), colValues.get(2));
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.ssl.enable", "false");
			props.put("mail.smtp.host", "smtp.office365.com");
			props.put("mail.smtp.port", "587");
			props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
			props.put("mail.smtp.socketFactory.fallback", "true");
			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
			session.setDebug(false);
			// Compose Message
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", suiteCompleteToRecipients.stream().distinct().collect(Collectors.toList()))));
			message.setSubject(emailSubject);
			// Create a multipart message
			Multipart multipart = new MimeMultipart();
			// Create the text message part
			MimeBodyPart textBodyPart = new MimeBodyPart();
			textBodyPart.setContent(finalEmailBody.toString(), "text/html");
			// Set text message part
			multipart.addBodyPart(textBodyPart);
			// Part two is attachment
			MimeBodyPart attachmentBodyPart = new MimeBodyPart();
			String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
			DataSource source = new FileDataSource(filePath);
			attachmentBodyPart.setDataHandler(new DataHandler(source));
			attachmentBodyPart.setFileName(fileName);
			// Set attachement message part
			multipart.addBodyPart(attachmentBodyPart);
			// Send the complete message parts
			message.setContent(multipart);
			if (isEmailHighImp()) {
				message.setHeader("X-Priority", "1");
			}
			Transport.send(message);
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: convertSystemDate()                                   */
	/*                                                                      */
	/*   Description: get system date in MM/DD/YYYY hh:mm:ssaa format       */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: Date string                                           */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 12/05/21  R.Vattumilli    Initial creation of convertSystemDate()    */
	/*                                                                      */
	/************************************************************************/

	private static String convertSystemDate() {

		Date sysdate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ssaa");
		return (sdf.format(sysdate).toString());
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: closeConnection()                                     */
	/*                                                                      */
	/*   Description: Close DB connection                                   */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial creation of closeConnection()      */
	/*                                                                      */
	/************************************************************************/

	public static void closeConnection() {

		try {
			if(connection1 != null) {
				connection1.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/************************************************************************/
	/*                                                                      */
	/* Function Name: replaceChars()                                        */
	/*                                                                      */
	/*   Description: replace tab space & new line chars for input string   */
	/*                                                                      */
	/*    Parameters: origString                                            */
	/*                                                                      */
	/*       Returns: replaced string                                       */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 12/05/21  R.Vattumilli    Initial creation of replaceChars()         */
	/*                                                                      */
	/* 10/31/24  R.Vattumilli    Added replace chars for requirements       */
	/*                                                                      */
	/************************************************************************/
	
	public static String replaceChars(String origString) {
		
		String replacedString;
		if(getReplaceType() == "req" ) {
			replacedString = origString.replaceAll("\t", " ").replaceAll("'", "\"").trim();
		} else {
			replacedString = origString.replaceAll("\t", "").replaceAll(System.lineSeparator(), " ").replaceAll("\n", " ").replaceAll("'", "\"").trim();
		}
		return replacedString;
	}
}