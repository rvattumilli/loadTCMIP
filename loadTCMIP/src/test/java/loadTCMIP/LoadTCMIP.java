package loadTCMIP;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import utils.CommonUtils;
import utils.EncryptorAesGcmPassword;

public class LoadTCMIP {

	protected WebDriver driver;
	private ArrayList<String> colValues  = new ArrayList<String>();
	private ArrayList<String> colNames   = new ArrayList<String>();
	private ArrayList<String[]> dataList = new ArrayList<String[]>();
	private String url, mipUname, mipPwd, userPwd, consoleLogFileName, consoleLogFilePath, tcID, sakReq;
	private boolean linkTCToWI = false; 
	private boolean linkReqToTC = true;
	private final boolean linkNBToTC = true;
	private final DateFormat dateFormat2 = new SimpleDateFormat("dd/MMM/yyyy hh:mm:ssaa");
	private final String logsDirectory = System.getProperty("user.dir") + File.separator + "Logs";
	private final String outputFormat = "%-38s: %s";
	private final String systemUserName = "rvattumilli";
	private final String selectTestCaseSql = "select * from load_tc where tc_row = ";
	private final String selectReqSql = "select * from load_req where req_row = ";
	private final String testCaseDataMessage = "            NO TC DATA THIS RUN           ";
	private final String reqDataMessage = "           NO REQ DATA THIS RUN           ";
	private final String credQuery = "select a.host_url, a.user_id, a.passkey MIP_KEY, a.password MIP_PWD, b.passkey USER_KEY, b.password USER_PWD from "
			 						+ "v_user_login a, v_user_login b where a.type = 'MIP' and b.type = 'System' and a.days_to_reset_pwd > 2 and b.days_to_reset_pwd > 2";
	private final String testCaseSql = "select a.nb, a.nb_bf, b.sak_participant, upper(trim(b.nam_first)||' '||trim(b.nam_last)) owner, a.env, a.subsystem_tc, "
										+ "a.nam, replace(a.dsc, CHR(9),'') as dsc, replace(a.dsc_expect, CHR(9),'') as dsc_expect, a.grpng, nvl(trim(a.wi_test_case), ' ') wi_test_case, tc_row, "
										+ "nvl(trim(d.dsc), ' ') wi_type, nvl(trim(a.id_req), ' ') id_req from rvattumi.load_tc a, co_participant b, co c, co_type d where "
										+ "c.sak_csr(+) = a.wi_test_case and d.sak_csr_type(+) = c.sak_csr_type and testcase_id is null and "
										+ "upper(trim(b.nam_first)||' '||trim(b.nam_last)) = upper(trim(a.owner)) and a.ind_status != 'P' order by tc_row asc";
	private final String reqSql = "select a.req_row, replace(a.id_req, CHR(9),'') id_req, replace(a.name_req, CHR(9),'') name_req, a.type_req, a.subsystem_req, nvl(a.rtm_req, ' '), replace(a.narrative_req, CHR(9),'') narrative_req, "
								+ "trim(b.nam_owner) nam_owner from load_req a, object_owner b where a.sak_req is null and upper(b.nam_owner) = upper(a.owner_req) and a.ind_status != 'P' order by a.req_row asc";

	/************************************************************************/
	/*                                                                      */
	/* Function Name: enterTC()                                             */
	/*                                                                      */
	/*   Description: This function creates test cases by looping thru every*/
	/*                record                                                */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/*                                                                      */
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of enterTC()              */
	/*                                                                      */
	/* 11/19/22  R.Vattumilli    Add validateTCData() to validate data      */
	/*                                                                      */
	/************************************************************************/

	@Test
	private void enterTC() {

		try {
			System.out.println("****************************************************");
			dataList = CommonUtils.getTCReqData(testCaseSql, 14, CommonUtils.getDBConnection(), testCaseDataMessage);
			if (dataList != null) {
				CommonUtils.setReplaceType("tc");
				System.out.println(String.format(outputFormat, "Link Notebook/Business Function", linkNBToTC));
				System.out.println(String.format(outputFormat, "Link Requirement", linkReqToTC));
				System.out.println(String.format(outputFormat, "Link WI/Defect/CO", linkTCToWI));
				driver.findElement(By.linkText("Testing")).click();
				String noteBook, businessFunction, sakParticipant, owner, env, subsystemTc, nam, dsc, dscExpect, grpng, wiTestCase, tcRow, wiType, idReq, reqType, sakReqType, SelSql, UpdSql;
				Alert alert;
				boolean linkWIToTC = linkTCToWI;
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
				for (int i = 0; i < dataList.size(); i++) {
					String tcdata[]  = dataList.get(i);
					noteBook         = CommonUtils.replaceChars(tcdata[0]);
					businessFunction = CommonUtils.replaceChars(tcdata[1]);
					sakParticipant   = CommonUtils.replaceChars(tcdata[2]);
					owner            = CommonUtils.replaceChars(tcdata[3]);
					env              = CommonUtils.replaceChars(tcdata[4]);
					subsystemTc      = CommonUtils.replaceChars(tcdata[5]);
					nam              = CommonUtils.replaceChars(tcdata[6]);
					dsc              = tcdata[7];
					dscExpect        = tcdata[8];
					grpng            = CommonUtils.replaceChars(tcdata[9]);
					wiTestCase       = CommonUtils.replaceChars(tcdata[10]);
					tcRow            = tcdata[11];
					wiType           = CommonUtils.replaceChars(tcdata[12]);
					idReq            = CommonUtils.replaceChars(tcdata[13]);
					System.out.println("*****************************************************************");
					System.out.println(String.format(outputFormat, "Started at", new Date().toString()));
					if (dataList.size() > 0) {
						System.out.println(String.format(outputFormat, "Creating test case for Row #", tcRow));
						if(idReq.equals("") && !(wiTestCase.equals(""))) {
							linkTCToWI = true;
						}
						Instant start = Instant.now();
						if(CommonUtils.validateTCData(idReq, subsystemTc, grpng, businessFunction, env)) {
							Instant finish = Instant.now();
							long execTime = Duration.between(start, finish).toMillis();
							System.out.println(String.format(outputFormat, "Validation Time(ms)", execTime));
							System.out.println(String.format(outputFormat, "TC Validation status", "PASS"));
							driver.findElement(By.linkText("Add")).click();
							driver.findElement(By.xpath("//input[contains(@name,'TcName')]")).sendKeys(nam);
							System.out.println(String.format(outputFormat, "Short Desc", nam));
							new Select(driver.findElement(By.xpath("//select[contains(@name,'TcEnvInd')]"))).selectByValue(env);
							System.out.println(String.format(outputFormat, "Env", env));
							new Select(driver.findElement(By.xpath("//select[contains(@name,'SubsystemSak')]"))).selectByVisibleText(subsystemTc);
							System.out.println(String.format(outputFormat, "Subsystem", subsystemTc));
							new Select(driver.findElement(By.xpath("//select[contains(@name,'OwnerSak')]"))).selectByValue(sakParticipant);
							System.out.println(String.format(outputFormat, "SAK Participant", sakParticipant));
							System.out.println(String.format(outputFormat, "Owner", owner));
							new Select(driver.findElement(By.xpath("//select[contains(@name,'TcGroupingSak')]"))).selectByVisibleText(grpng);
							System.out.println(String.format(outputFormat, "Grouping", grpng));
							driver.findElement(By.xpath("//textarea[contains(@name,'TcDsc')]")).sendKeys(dsc);
							System.out.println(String.format(outputFormat, "Long Desc",  CommonUtils.replaceChars(dsc)));
							driver.findElement(By.xpath("//textarea[contains(@name,'TcDscExpected')]")).sendKeys(dscExpect);
							System.out.println(String.format(outputFormat, "Expected Results",  CommonUtils.replaceChars(dscExpect)));
							driver.findElement(By.xpath("//input[@value = 'Add Test Case']")).click();
							wait.until(ExpectedConditions.alertIsPresent());
							alert = driver.switchTo().alert();
							alert.accept();
							tcID = driver.findElement(By.xpath("//input[contains(@name,'TcSak')]")).getAttribute("value").trim();
							System.out.println(String.format(outputFormat, "Test Case ID for Row " + tcRow, tcID));
							SelSql = selectTestCaseSql + tcRow;
							UpdSql = "update load_tc set ind_status = 'P', testcase_id = '" + tcID + "' where tc_row = " + tcRow;
							CommonUtils.updateLoadTableData(SelSql, "TC_ROW", UpdSql);
							if (linkReqToTC && !(idReq.equals(""))) {
								System.out.println(String.format(outputFormat, "Requirement ID", idReq));
								String reqQuery = "select b.nam req_type, b.sak_req_type from requirement a, requirement_type b where a.sak_req_type = b.sak_req_type and a.id_req = '" + idReq + "'";
								colNames.add("REQ_TYPE");
								colNames.add("SAK_REQ_TYPE");
								colValues = CommonUtils.executeQuery(reqQuery, colNames);
								reqType = colValues.get(0);
								sakReqType = colValues.get(1);
								System.out.println(String.format(outputFormat, "Requirement Type", reqType));
								System.out.println(String.format(outputFormat, "SAK Requirement Type", sakReqType));
								driver.findElement(By.partialLinkText("Requirement/")).click();
								driver.findElement(By.xpath("/html/body/table[4]/tbody/tr/td[3]/table/tbody/tr/td[1]/table/tbody/tr[1]/td/table/tbody/tr[1]/td[1]/img")).click();
								driver.findElement(By.xpath("//input[contains(@name,'ReqId')]")).sendKeys(idReq);
								driver.findElement(By.xpath("//table[@id = 'tableReq0']//tr[5]//input[1]")).click();
								driver.switchTo().frame("dot_ReqSoQuery1a");
								driver.findElement(By.xpath("//img[@src='../../images/icon-add.gif']")).click();
								wait.until(ExpectedConditions.alertIsPresent());
								alert = driver.switchTo().alert();
								alert.accept();
								System.out.println(String.format(outputFormat, "Requirement linked for test case", tcID));
								driver.switchTo().defaultContent();
								Thread.sleep(150);
								if(sakReqType.equals("26")) {
									linkTCToWI = true;
									System.out.println(String.format(outputFormat, "Link WI/Defect/CO", linkTCToWI));
								}
							}
							if (linkNBToTC && !(businessFunction.equals(""))) {
								System.out.println(String.format(outputFormat, "Notebook", noteBook));
								System.out.println(String.format(outputFormat, "Business Function", businessFunction));
								driver.findElement(By.partialLinkText("Notebook/")).click();
								driver.findElement(By.xpath("//input[contains(@name,'TbName')]")).sendKeys(businessFunction);
								driver.findElement(By.xpath("//input[@value = 'Search Notebooks/Business Functions']")).click();
								driver.switchTo().frame("dot_query");
								driver.findElement(By.xpath("//img[@src='../../images/icon-add.gif']")).click();
								wait.until(ExpectedConditions.alertIsPresent());
								alert = driver.switchTo().alert();
								alert.accept();
								System.out.println(String.format(outputFormat, "Notebook/BF linked for test case", tcID));
								driver.switchTo().defaultContent();
								Thread.sleep(150);
							}
							if (linkTCToWI && (!wiTestCase.equals(""))) {
								System.out.println(String.format(outputFormat, "iWI/Defect/CO", wiTestCase));
								System.out.println(String.format(outputFormat, "iWI Type", wiType));
								if (((wiType.equals("Work Item")) || (wiType.equals("Change Order")))) {
									driver.findElement(By.linkText("Work Item")).click();
									driver.findElement(By.xpath("//input[contains(@name,'CoId')]")).sendKeys(wiTestCase);
									driver.findElement(By.xpath("//input[@value = 'Search Work Items']")).click();
									driver.switchTo().frame("dot_query");
									driver.findElement(By.xpath("//img[@src='../../images/icon-add.gif']")).click();
									wait.until(ExpectedConditions.alertIsPresent());
									alert = driver.switchTo().alert();
									alert.accept();
									System.out.println(String.format(outputFormat, wiType+" linked for test case", tcID));
									Thread.sleep(150);
								}
							}
						} else {
							Instant finish = Instant.now();
							long execTime = Duration.between(start, finish).toMillis();
							System.out.println(String.format(outputFormat, "Validation Time(ms)", execTime));
							SelSql = selectTestCaseSql + tcRow;
							UpdSql = "update load_tc set ind_status = 'F' where tc_row = " + tcRow;
							CommonUtils.updateLoadTableData(SelSql, "TC_ROW", UpdSql);
							System.out.println(String.format(outputFormat, "TC Validation status", "FAIL"));
						}
						driver.switchTo().defaultContent();
					}
					linkTCToWI = linkWIToTC;
					System.out.println(String.format(outputFormat, "Finished at", new Date().toString()));
				}
			}
		} catch (Exception e) {
			CommonUtils.setJobAbend(true);
			CommonUtils.sshot(driver);
			System.out.println(e.getMessage());
		}
	}
	
	/************************************************************************/
	/*                                                                      */
	/* Function Name: enterReq()                                            */
	/*                                                                      */
	/*   Description: This function add requirements by looping thru every  */
	/*                record                                                */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/*                                                                      */
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 02/15/24  R.Vattumilli    Initial Creation of enterReq()             */
	/*                                                                      */
	/************************************************************************/
	
	@Test
	private void enterReq() {

		try {
			driver.findElement(By.linkText("Requirements")).click();
			dataList = CommonUtils.getTCReqData(reqSql, 8, CommonUtils.getDBConnection(), reqDataMessage);
			if (dataList != null) {
				CommonUtils.setReplaceType("req");
				String reqRow, idReq, nameReq, typeReq, subsystemReq, rtmReq, narrativeReq, namOwnerReq, SelSql, UpdSql;
				Alert alert;
				WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
				for (int i = 0; i < dataList.size(); i++) {
					String tcdata[]  = dataList.get(i);
					reqRow          = tcdata[0];
					idReq           = CommonUtils.replaceChars(tcdata[1]);
					nameReq         = CommonUtils.replaceChars(tcdata[2]);
					typeReq         = CommonUtils.replaceChars(tcdata[3]);
					subsystemReq    = CommonUtils.replaceChars(tcdata[4]);
					rtmReq          = CommonUtils.replaceChars(tcdata[5]);
					narrativeReq    = CommonUtils.replaceChars(tcdata[6]);
					namOwnerReq     = CommonUtils.replaceChars(tcdata[7]);
					System.out.println("*****************************************************************");
					System.out.println(String.format(outputFormat, "Started at", new Date().toString()));
					if (dataList.size() > 0) {
						System.out.println(String.format(outputFormat, "Creating requirement for Row #", reqRow));
						Instant start = Instant.now();
						if(CommonUtils.validateReqData(idReq, nameReq, typeReq, subsystemReq, rtmReq, namOwnerReq)) {
							Instant finish = Instant.now();
							long execTime = Duration.between(start, finish).toMillis();
							System.out.println(String.format(outputFormat, "Validation Time(ms)", execTime));
							System.out.println(String.format(outputFormat, "Requirement Validation status", "PASS"));
							driver.findElement(By.linkText("Add")).click();
							driver.findElement(By.xpath("//input[contains(@name,'ReqId') and @type = 'text' and @size = '20']")).sendKeys(idReq);
							System.out.println(String.format(outputFormat, "Requirement ID", idReq));
							driver.findElement(By.xpath("//input[contains(@name,'ReqName')]")).sendKeys(nameReq);
							System.out.println(String.format(outputFormat, "Requirement Name", nameReq));
							new Select(driver.findElement(By.xpath("//select[contains(@name,'ReqTypeSak')]"))).selectByVisibleText(typeReq);
							System.out.println(String.format(outputFormat, "Requirement Type", typeReq));
							new Select(driver.findElement(By.xpath("//select[contains(@name,'SubsystemSak')]"))).selectByVisibleText(subsystemReq);
							System.out.println(String.format(outputFormat, "Requirement Subsystem", subsystemReq));
							if(!rtmReq.equals("")) {
								new Select(driver.findElement(By.xpath("//select[contains(@name,'RtmSak')]"))).selectByValue(rtmReq);
								System.out.println(String.format(outputFormat, "Requirement RTM", rtmReq));
							}
							driver.findElement(By.xpath("//textarea[contains(@name,'ReqDsc')]")).sendKeys(narrativeReq);
							System.out.println(String.format(outputFormat, "Requirement Narrative", narrativeReq));
							driver.findElement(By.xpath("//input[@value = 'Add Requirement']")).click();
							Thread.sleep(150);
							wait.until(ExpectedConditions.alertIsPresent());
							alert = driver.switchTo().alert();
							alert.accept();
							sakReq = driver.findElement(By.xpath("//td[contains(@id,'contentWindow')]//table/tbody/tr[2]/td[4]")).getText().trim();
							System.out.println(String.format(outputFormat, "SAK Req for Row " + reqRow, sakReq));
							SelSql = selectReqSql + reqRow;
							UpdSql = "update load_req set ind_status = 'P', dte_loaded = to_char(sysdate, 'yyyymmdd'), sak_req = " + sakReq + " where req_row = " + reqRow;
							CommonUtils.updateLoadTableData(SelSql, "REQ_ROW", UpdSql);
							driver.findElement(By.partialLinkText("Status")).click();
							driver.findElement(By.xpath("//img[@src='../../images/edit16.gif']")).click();
							driver.switchTo().frame("dot_editForm");
							new Select(driver.findElement(By.xpath("//select[contains(@name,'StatusOwnerSak')]"))).selectByVisibleText(namOwnerReq);
							driver.findElement(By.xpath("//input[@value = 'Update Status']")).click();
							Thread.sleep(150);
							wait.until(ExpectedConditions.alertIsPresent());
							alert = driver.switchTo().alert();
							alert.accept();
						} else {
							Instant finish = Instant.now();
							long execTime = Duration.between(start, finish).toMillis();
							System.out.println(String.format(outputFormat, "Validation Time(ms)", execTime));
							SelSql = selectReqSql + reqRow;
							UpdSql = "update load_req set ind_status = 'F' where req_row = " + reqRow;
							CommonUtils.updateLoadTableData(SelSql, "REQ_ROW", UpdSql);
							System.out.println(String.format(outputFormat, "Requirement Validation status", "FAIL"));
						}
						driver.switchTo().defaultContent();
					}
					System.out.println(String.format(outputFormat, "Finished at", new Date().toString()));
				}
			}
		} catch (Exception e) {
			CommonUtils.setJobAbend(true);
			CommonUtils.sshot(driver);
			e.printStackTrace();
		}
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: beforeTest()                                          */
	/*                                                                      */
	/*   Description: This function launches browser and logs into basic    */
	/*                authentication before logging into MIP                */
	/*                                                                      */
	/*    Parameters: browser                                               */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/*                                                                      */
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of beforeTest()           */
	/*                                                                      */
	/************************************************************************/

	@Parameters({ "browser", "logDebugMsgs", "lastModifiedDate", "overrideValidation" })
	@BeforeTest
	private void beforeTest(String browser, boolean logDebugMsgs, String lastModifiedDate, boolean overrideValidation) {

		try {
			CommonUtils.setLogDebugMsgs(logDebugMsgs);
			CommonUtils.setOverrideValidation(overrideValidation);
			System.out.println(String.format(outputFormat, "Last Modified Date", lastModifiedDate));
			colNames.add("HOST_URL");
			colNames.add("USER_ID");
			colNames.add("MIP_KEY");
			colNames.add("MIP_PWD");
			colNames.add("USER_KEY");
			colNames.add("USER_PWD");
			colValues = CommonUtils.executeQuery(credQuery, colNames);
			if(colValues == null) {
				Assert.assertTrue(false);
			}
			url = colValues.get(0);
			mipUname = colValues.get(1);
			mipPwd = EncryptorAesGcmPassword.decrypt(colValues.get(3), colValues.get(2));
			userPwd = EncryptorAesGcmPassword.decrypt(colValues.get(5), colValues.get(4));
			if(logDebugMsgs) {
				System.out.println(String.format(outputFormat, "MIP URL", url));
			}
			if (browser.equalsIgnoreCase("chrome")) {
				ChromeOptions options = new ChromeOptions();
				options.addArguments("--incognito");
				options.addArguments("--remote-allow-origins=*");
				driver = new ChromeDriver(options);
			} else if(browser.equalsIgnoreCase("edge")) {
				EdgeOptions options = new EdgeOptions();
				options.addArguments("-inprivate");
				options.addArguments("--remote-allow-origins=*");
				driver = new EdgeDriver(options);
			}
			driver.get(url);
			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
			StringSelection stringSelection = new StringSelection(systemUserName);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, stringSelection);
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_TAB);
			Thread.sleep(150);
			stringSelection = new StringSelection(userPwd);
			clipboard.setContents(stringSelection, stringSelection);
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_ENTER);
			Thread.sleep(150);
			driver.findElement(By.xpath("//input[contains(@name, 'txtLogonID')]")).sendKeys(mipUname);
			driver.findElement(By.xpath("//input[contains(@name, 'pssPassword')]")).sendKeys(mipPwd);
			driver.findElement(By.xpath("//input[contains(@name, 'LogonIDSubmit')]")).click();
			driver.findElement(By.linkText("DocoTool")).click();
			//String welcomeText = driver.findElement(By.xpath("//p[@class = 'mainTitleWelcome']/b")).getText().trim();
		} catch (Exception e) {
			CommonUtils.setJobAbend(true);
			CommonUtils.sshot(driver);
			System.out.println(e.getMessage());
		}
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: afterTest()                                           */
	/*                                                                      */
	/*   Description: This function writes teardown text to console and     */
	/*                sends email with log file to recip(s)                 */
	/*                                                                      */
	/*    Parameters: emailExtraRecips                                      */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/*                                                                      */
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of afterTest()            */
	/*                                                                      */
	/* 12/05/21  R.Vattumilli    Add setEmailExtraRecips()                  */
	/*                                                                      */
	/* 12/05/21  R.Vattumilli    Add sendEmail(consoleLogFilePath)          */
	/*                                                                      */
	/* 11/19/22  R.Vattumilli    Commented send message line to skip sending*/
	/*                           email as it needs MFA                      */
	/*                                                                      */
	/************************************************************************/

	@Parameters({ "emailRecips"})
	@AfterTest
	private void afterTest(boolean emailRecips) {

		Wait<WebDriver> fluentWait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(30)).pollingEvery(Duration.ofSeconds(5)).ignoring(NoSuchElementException.class);
		WebElement logout = fluentWait.until(new Function<WebDriver, WebElement>() {
			public WebElement apply(WebDriver driver) {
				return driver.findElement(By.linkText("Logout"));
			}
		});
		logout.click();
		System.out.println("****************************************************");
		System.out.println("*****************                  *****************");
		System.out.println("***********                              ***********");
		System.out.println("*****                Logged out                *****");
		System.out.println("***********                              ***********");
		System.out.println("*****************                  *****************");
		System.out.println("****************************************************");
		driver.quit();
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("****************************************************");
		System.out.println("*****************                  *****************");
		System.out.println("***********                              ***********");
		System.out.println("******                                        ******");
		if (CommonUtils.isJobAbend()) {
			System.out.println("***Job: LoadTCMIP abended " + dateFormat2.format(new Date()) + "****");
		} else {
			System.out.println("***Job: LoadTCMIP completed " + dateFormat2.format(new Date()) + "**");
		}
		System.out.println("******                                        ******");
		System.out.println("***********                              ***********");
		System.out.println("*****************                  *****************");
		System.out.println("****************************************************");
		if(emailRecips) {
			CommonUtils.sendEmail(consoleLogFilePath);
		}
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: beforeSuite()                                         */
	/*                                                                      */
	/*   Description: This function creates log file and directory if not   */
	/*                exists and sets console output to log file and write  */
	/*                startup text to console                               */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/*                                                                      */
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of beforeSuite()          */
	/*                                                                      */
	/************************************************************************/

	@BeforeSuite
	private void beforeSuite() throws IOException {

		DateFormat dateFormat = new SimpleDateFormat("dd_MMM_yyyy_hh_mm_ssaa");
		File logsFolder = new File(logsDirectory);
		if (!logsFolder.exists()) {
			logsFolder.mkdirs();
		}
		consoleLogFileName = "consoleoutput_" + dateFormat.format(new Date()) + ".txt";
		consoleLogFilePath = logsFolder + File.separator + consoleLogFileName;
		File logFile = new File(consoleLogFilePath);
		logFile.createNewFile();
		System.setOut(new PrintStream(new FileOutputStream(logFile)));
		System.out.println("");
		System.out.println("****************************************************");
		System.out.println("*****************                  *****************");
		System.out.println("***********                              ***********");
		System.out.println("******                                        ******");
		System.out.println("***Job: LoadTCMIP started " + dateFormat2.format(new Date()) + "****");
		System.out.println("******                                        ******");
		System.out.println("***********                              ***********");
		System.out.println("*****************                  *****************");
		System.out.println("****************************************************");
		System.out.println();
	}

	/************************************************************************/
	/*                                                                      */
	/* Function Name: afterSuite()                                          */
	/*                                                                      */
	/*   Description: This function closes DB connection                    */
	/*                                                                      */
	/*    Parameters: None                                                  */
	/*                                                                      */
	/*       Returns: None                                                  */
	/*                                                                      */
	/************************************************************************/
	/*                       MODIFICATION LOG                               */
	/************************************************************************/
	/*                                                                      */
	/* Date      A.E. Name       Description                                */
	/* --------  --------------  ------------------------------------------ */
	/* 08/04/20  R.Vattumilli    Initial Creation of afterSuite()           */
	/*                                                                      */
	/************************************************************************/

	@AfterSuite
	private void afterSuite() throws SQLException {

		CommonUtils.closeConnection();
	}
}