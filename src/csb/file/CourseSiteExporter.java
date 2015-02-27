package csb.file;

import static csb.CSB_StartupConstants.PATH_SITES;
import csb.data.Course;
import csb.data.CoursePage;
import csb.data.Instructor;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import javax.swing.text.html.HTML;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is responsible for exporting schedule.html to its proper
 * directory. Note that it uses a base file in the baseDir directory, which gets
 * loaded first and that each course will have its own file exported to a
 * directory in the sitesDir directory.
 *
 * @author Richard McKenna
 */
public class CourseSiteExporter {
    // THERE ARE A NUMBER OF CONSTANTS THAT WE'LL USE FOR FINDING
    // ELEMENTS IN THE PAGES WE'RE LOADING, AS WELL AS THINGS WE'LL
    // BUILD INTO OUR PAGE WHILE EXPORTING
    public static final String ID_NAVBAR = "navbar";
    public static final String ID_BANNER = "banner";
    public static final String ID_SCHEDULE = "schedule";
    public static final String ID_HOME_LINK = "home_link";
    public static final String ID_SYLLABUS_LINK = "syllabus_link";
    public static final String ID_SCHEDULE_LINK = "schedule_link";
    public static final String ID_HWS_LINK = "hws_link";
    public static final String ID_PROJECTS_LINK = "projects_link";
    public static final String ID_INSTRUCTOR_LINK = "instructor_link";
    public static final String CLASS_SCH = "sch";
    public static final String CLASS_NAV = "nav";
    public static final String CLASS_OPEN_NAV = "open_nav";

    // THIS IS TEXT WE'LL BE ADDING TO OUR PAGE
    public static final String INDEX_HEADER = "Home";
    public static final String SYLLABUS_HEADER = "Syllabus";
    public static final String SCHEDULE_HEADER = "Schedule";
    public static final String HWS_HEADER = "HWs";
    public static final String PROJECTS_HEADER = "Projects";
    public static final String MONDAY_HEADER = "MONDAY";
    public static final String TUESDAY_HEADER = "TUESDAY";
    public static final String WEDNESDAY_HEADER = "WEDNESDAY";
    public static final String THURSDAY_HEADER = "THURSDAY";
    public static final String FRIDAY_HEADER = "FRIDAY";

    // THESE ARE THE POSSIBLE SITE PAGES OUR SCHEDULE PAGE
    // MAY NEED TO LINK TO
    public static String INDEX_PAGE = "index.html";
    public static String SYLLABUS_PAGE = "syllabus.html";
    public static String SCHEDULE_PAGE = "schedule.html";
    public static String HWS_PAGE = "hws.html";
    public static String PROJECTS_PAGE = "projects.html";

    // THIS IS THE DIRECTORY STRUCTURE USED BY OUR SITE
    public static final String CSS_DIR = "css";
    public static final String IMAGES_DIR = "images";
    
    // AND SOME TEXT WE'LL NEED TO ADD ON THE FLY
    public static final String SLASH = "/";
    public static final String DASH = " - ";
    public static final String LINE_BREAK = "<br />";

    // THESE ARE THE DIRECTORIES WHERE OUR BASE SCHEDULE
    // FILE IS AND WHERE OUR COURSE SITES WILL BE EXPORTED TO
    String baseDir;
    String sitesDir;

    /**
     * This constructor initializes this exporter to load the schedule
     * page from the initBaseDir and export course pages to directories
     * found in initSitesDir.
     * 
     * @param initBaseDir Directory that contains the base site files.
     * 
     * @param initSitesDir Directory where course sites will be exported to. Note
     * that each course will have a directory here containing its site.
     */
    public CourseSiteExporter(String initBaseDir, String initSitesDir) {
        baseDir = initBaseDir;
        sitesDir = initSitesDir;
    }

    /**
     * This method is the facade to a lot of work done to export the site. It
     * will setup the necessary course directory if it doesn't already exist
     * and copy the needed stylesheets and images and will then export the
     * necessary pages.
     * 
     * @param courseToExport Course whose site is being built.
     * 
     * @throws IOException This exception is thrown when a problem occurs
     * creating the course site directory and/or files.
     */
    public void exportCourseSite(Course courseToExport) throws IOException {
        // GET THE DIRECTORY TO EXPORT THE SITE
        String courseExportPath = (new File(sitesDir) + SLASH)
                + courseToExport.getSubject() + courseToExport.getNumber();

        // FIRST EXPORT ANCILLARY FILES LIKE STYLE SHEETS AND IMAGES. NOTE
        // THAT THIS ONLY NEEDS TO BE DONE ONCE FOR EACH COURSE
        if (!new File(courseExportPath).exists()) {
            setupCourseSite(courseExportPath);
        }

        // EXPORT THE schedule.html PAGE
        exportSchedulePage(courseToExport, courseExportPath);        
    }

    /**
     * This function exports just the schedule.html page for the
     * courseToExport course's site.
     * 
     * @param courseToExport Course whose site we are to export.
     * @param courseExportPath The directory where courseToExport's site
     * pages are to be exported to.
     * 
     * @throws IOException Thrown when there is a problem exporting
     * the schedule page for this site.
     */
    public void exportSchedulePage(Course courseToExport, String courseExportPath)
            throws IOException {
        try {
            // NOW THAT EVERYTHING IS SETUP, BUILD THE PAGE DOCUMENT
            Document scheduleDoc = buildSchedulePage(courseToExport);

            // AND SAVE IT TO A FILE
            saveDocument(scheduleDoc, courseExportPath + SLASH + SCHEDULE_PAGE);
            
            // NOTE THAT IF ANYTHING GOES WRONG WE WILL REFLECT AND/OR PASS ALL EXCEPTIONS
        } catch(    TransformerException 
                |   SAXException
                |   ParserConfigurationException exception) {
            // WE ARE GOING TO REFLECT ALL OF THESE EXCEPTIONS AS
            // IOExceptions, WHICH WE'LL HANDLE TOGETHER
            throw new IOException(exception.getMessage());
        }
    }

    /**
     * Builds and returns the path to access the type of page denoted by cP
     * for the given course argument.
     * 
     * @param course The course for which we want to access a link.
     * @param cP The particular page in the course site for accessing a link.
     * 
     * @return A textual path to the page we wish to link to.
     */
    public String getPageURLPath(Course course, CoursePage cP) {
        String urlPath = PATH_SITES + course.getSubject() 
                + course.getNumber() 
                + SLASH + this.getLink(cP);
        File webPageFile = new File(urlPath);
        try {
            URL pageURL = webPageFile.toURI().toURL();
            return pageURL.toString();
        } catch (MalformedURLException murle) {
            return null;
        }
    }
    
    // BELOW ARE ALL THE PRIVATE HELPER METHODS

    // BUILDS A SCHEDULE PAGE AND RETURNS IT AS A SINGLE Document
    private Document buildSchedulePage(Course courseToExport) throws SAXException, TransformerException, IOException, ParserConfigurationException {
        // MAKE A PATH FOR THE SCHEDULE PAGE
        String schedulePath = baseDir + SLASH + SCHEDULE_PAGE;

        // NOW LOAD THE DOCUMENT
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document scheduleDoc = docBuilder.parse(schedulePath);

        // UPDATE THE PAGE HEADER
        Node titleNode = scheduleDoc.getElementsByTagName(HTML.Tag.TITLE.toString()).item(0);
        titleNode.setTextContent(courseToExport.getSubject() + " "
                + courseToExport.getNumber());
        //System.out.println(scheduleDoc.toString());
        // SET THE BANNER
        setBanner(scheduleDoc, courseToExport);
        
        //System.out.println(scheduleDoc.getTextContent());
        // NOW BUILD THE SCHEDULE TABLE
        fillScheduleTable(scheduleDoc, courseToExport);
        
        // AND ADD THE INSTRUCTOR
        appendInstructor(scheduleDoc, courseToExport.getInstructor());

        // AND RETURN THE FULL PAGE DOM
        return scheduleDoc;
    }
    
    // INITIALIZES ALL THE HELPER FILES AND DIRECTORIES, LIKE FOR CSS
    private void setupCourseSite(String exportPath) throws IOException {
        // FIRST MAKE THE FOLDERS
        File siteDir = new File(exportPath);
        siteDir.mkdir();
        File cssDir = new File(exportPath + SLASH + CSS_DIR);
        cssDir.mkdir();
        File imagesDir = new File(exportPath + SLASH + IMAGES_DIR);
        imagesDir.mkdir();

        // THEN COPY THE STYLESHEETS OVER
        File baseCSSDir = new File(baseDir + "/" + CSS_DIR);
        File[] cssFiles = baseCSSDir.listFiles();
        for (int i = 0; i < cssFiles.length; i++) {
            File cssFile = new File(cssDir + SLASH + cssFiles[i].getName());
            Files.copy(cssFiles[i].toPath(), cssFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // AND THEN COPY THE IMAGES OVER
        File baseImagesDir = new File(baseDir + "/" + IMAGES_DIR);
        File[] imageFiles = baseImagesDir.listFiles();
        for (int i = 0; i < imageFiles.length; i++) {
            File imageFile = new File(imagesDir + "/" + imageFiles[i].getName());
            Files.copy(imageFiles[i].toPath(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // APPENDS THE ISNTRUCTOR TO THE BOTTOM OF THE PAGE
    private void appendInstructor(Document pageDoc, Instructor courseInstructor) {
        Node instructorSpan = (Element)getNodeWithId(pageDoc, HTML.Tag.SPAN.toString(), ID_INSTRUCTOR_LINK);
        Element instructorLinkElement = pageDoc.createElement(HTML.Tag.A.toString());
        instructorLinkElement.setAttribute(HTML.Attribute.HREF.toString(), courseInstructor.getHomepageURL());
        instructorLinkElement.setTextContent(courseInstructor.getName());
        instructorSpan.appendChild(instructorLinkElement);
    }

    // FILLS IN THE SCHEDULE PAGE'S SCHEDULE TABLE
    private void fillScheduleTable(Document scheduleDoc, Course courseToExport) {
        LocalDate countingDate = courseToExport.getStartingMonday().minusDays(0);
        while (countingDate.isBefore(courseToExport.getEndingFriday())
                || countingDate.isEqual(courseToExport.getEndingFriday())) {
            // ADD THE MONDAY-FRIDAY HEADERS            
            // FIRST FOR EACH WEEK MAKE A TABLE ROW            
            Element dowRowHeaderElement = scheduleDoc.createElement(HTML.Tag.TR.toString());

            // AND ADD DAY OF THE WEEK TABLE HEADERS
            addDayOfWeekHeader(scheduleDoc, dowRowHeaderElement, MONDAY_HEADER);
            addDayOfWeekHeader(scheduleDoc, dowRowHeaderElement, TUESDAY_HEADER);
            addDayOfWeekHeader(scheduleDoc, dowRowHeaderElement, WEDNESDAY_HEADER);
            addDayOfWeekHeader(scheduleDoc, dowRowHeaderElement, THURSDAY_HEADER);
            addDayOfWeekHeader(scheduleDoc, dowRowHeaderElement, FRIDAY_HEADER);
            
            //ADD DAY OF THE WEEK ELEMENT
            //FIRST MAKE A TABLE ROW
            Element dowRowDataElement = scheduleDoc.createElement(HTML.Tag.TR.toString());
            
            //now add monday - friday data elements
            int daysOfWeek = 5;
            for(int x = 0 ; x < daysOfWeek ; x++){
                addDayOfWeekElement(scheduleDoc, dowRowDataElement, countingDate.getMonthValue(),countingDate.getDayOfMonth());
                countingDate = countingDate.plusDays(1);
            }
            // ADVANCE THE COUNTING DATE TO NEXT WEEK
            int nextWeek = 7 - daysOfWeek;
            countingDate = countingDate.plusDays(nextWeek);
            
            // AND PUT IT IN THE TABLE
            Node scheduleTableNode = getNodeWithId(scheduleDoc, HTML.Tag.TABLE.toString(), ID_SCHEDULE);
            scheduleTableNode.appendChild(dowRowHeaderElement);
            scheduleTableNode.appendChild(dowRowDataElement);
        }
    }

    // ADDS A DAY OF WEEK HEADER TO THE SCHEDULE PAGE SCHEDULE TABLE
    private void addDayOfWeekHeader(Document scheduleDoc, Element tableRow, String dayOfWeekText) {
        Element dayOfWeekHeader = scheduleDoc.createElement(HTML.Tag.TH.toString());
        dayOfWeekHeader.setAttribute(HTML.Attribute.CLASS.toString(), CLASS_SCH);
        dayOfWeekHeader.setTextContent(dayOfWeekText);
        tableRow.appendChild(dayOfWeekHeader);
    }
    
    // ADDS DATA TO A DAY OF WEEK TABLE ROW TO THE SCHDULE PAGE SCHEDULE TABLE
    private void addDayOfWeekElement(Document scheduleDoc, Element tableRow,int monthValue, int dayOfMonth){
        Element dayOfWeekElement = scheduleDoc.createElement(HTML.Tag.TD.toString());
        dayOfWeekElement.setAttribute(HTML.Attribute.CLASS.toString(), CLASS_SCH);
        dayOfWeekElement.setTextContent(monthValue+SLASH+dayOfMonth);
        tableRow.appendChild(dayOfWeekElement);
    }

    // FINDS AND RETURNS A NODE IN A DOCUMENT OF A CERTAIN TYPE WITH A CERTIAN ID
    private Node getNodeWithId(Document doc, String tagType, String searchID) {
        NodeList divNodes = doc.getElementsByTagName(tagType);
        for (int i = 0; i < divNodes.getLength(); i++) {
            Node testNode = divNodes.item(i);
            Node testAttr = testNode.getAttributes().getNamedItem(HTML.Attribute.ID.toString());
            //THIS IS ADDED
            if(testAttr != null){
                if (testAttr.getNodeValue().equals(searchID)) {
                    return testNode;
                }
            }
        }
        return null;
    }

    // SAVES THE DOCUMENT OBJECT TO A FILE, WHICH WOULD BE AN HTIM FILE
    private void saveDocument(Document doc, String outputFilePath)
            throws TransformerException, TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        Result result = new StreamResult(new File(outputFilePath));
        Source source = new DOMSource(doc);
        transformer.transform(source, result);
    }

    // SETS THE COURSE PAGE BANNER
    private void setBanner(Document doc, Course courseToExport) {
       //first get the banner div
        Node bannerNode = getNodeWithId(doc, HTML.Tag.DIV.toString(), ID_BANNER);
        
        //then create a node to hold the first bit of text and append it to bannerNodes children
        Node subjectAndTermNode = doc.createTextNode(courseToExport.getSubject().toString() + " " + courseToExport.getNumber()
                +" "+DASH+" "+courseToExport.getSemester()+" "+courseToExport.getYear());
        bannerNode.appendChild(subjectAndTermNode);
        
        //add a line brake element to bannerNode children
        bannerNode.appendChild(doc.createElement(HTML.Tag.BR.toString()));
        
        //now we need the last bit of text added to bannerNodes children
        bannerNode.appendChild(doc.createTextNode(courseToExport.getTitle()));
        
        
        
        /**
        Element brElement = doc.createElement(HTML.Tag.BR.toString());
        bannerNode.appendChild(brElement);
        
        Element courseTitleSpan = doc.createElement(HTML.Tag.SPAN.toString());
        courseTitleSpan.setTextContent(courseToExport.getTitle());
        bannerNode.appendChild(courseTitleSpan);
    **/
    }
    
    // USED FOR GETTING THE PAGE LINKS FOR PAGE LINKS IN THE NAVBAR
    private String getLink(CoursePage page) {
        if (page == CoursePage.INDEX) {
            return INDEX_PAGE;
        } else if (page == CoursePage.SYLLABUS) {
            return SYLLABUS_PAGE;
        } else if (page == CoursePage.SCHEDULE) {
            return SCHEDULE_PAGE;
        } else if (page == CoursePage.HWS) {
            return HWS_PAGE;
        } else {
            return PROJECTS_PAGE;
        }
    }
}
