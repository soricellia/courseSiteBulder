package csb.file;

import static csb.CSB_StartupConstants.PATH_COURSES;
import csb.data.Course;
import csb.data.CoursePage;
import csb.data.Instructor;
import csb.data.Semester;
import csb.data.Subject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonValue;

/**
 * This is a CourseFileManager that uses the JSON file format to 
 * implement the necessary functions for loading and saving different
 * data for our courses, instructors, and subjects.
 * 
 * @author Richard McKenna
 */
public class JsonCourseFileManager implements CourseFileManager {
    // JSON FILE READING AND WRITING CONSTANTS
    public static String JSON_SUBJECTS = "subjects";
    public static String JSON_SUBJECT = "subject";
    public static String JSON_NUMBER = "number";
    public static String JSON_TITLE = "title";
    public static String JSON_SEMESTER = "semester";
    public static String JSON_YEAR = "year";
    public static String JSON_SECTION = "section";
    public static String JSON_PAGES = "pages";
    public static String JSON_STARTING_MONDAY = "startingMonday";
    public static String JSON_ENDING_FRIDAY = "endingFriday";
    public static String JSON_MONTH = "month";
    public static String JSON_DAY = "day";
    public static String JSON_INSTRUCTOR = "instructor";
    public static String JSON_INSTRUCTOR_NAME = "instructorName";
    public static String JSON_HOMEPAGE_URL = "homepageURL";
    public static String JSON_LECTURE_DAYS = "lectureDays";
    public static String JSON_EXT = ".json";
    public static String SLASH = "/";

    /**
     * This method saves all the data associated with a course to
     * a JSON file.
     * 
     * @param courseToSave The course whose data we are saving.
     * 
     * @throws IOException Thrown when there are issues writing
     * to the JSON file.
     */
    @Override
    public void saveCourse(Course courseToSave) throws IOException {
        // BUILD THE FILE PATH
        String courseListing = "" + courseToSave.getSubject() + courseToSave.getNumber();
        String jsonFilePath = PATH_COURSES + SLASH + courseListing + JSON_EXT;
        
        // INIT THE WRITER
        OutputStream os = new FileOutputStream(jsonFilePath);
        JsonWriter jsonWriter = Json.createWriter(os);  
        
        // MAKE A JSON ARRAY FOR THE PAGES ARRAY
        JsonArray pagesJsonArray = makePagesJsonArray(courseToSave.getPages());
        
        // AND AN OBJECT FOR THE INSTRUCTOR
        JsonObject instructorJsonObject = makeInstructorJsonObject(courseToSave.getInstructor());
        
        // ONE FOR EACH OF OUR DATES
        JsonObject startingMondayJsonObject = makeLocalDateJsonObject(courseToSave.getStartingMonday());
        JsonObject endingFridayJsonObject = makeLocalDateJsonObject(courseToSave.getEndingFriday());
        
        // AND THE LECTURE DAYS ARRAY
        JsonArray lectureDaysJsonArray = makeLectureDaysJsonArray(courseToSave.getLectureDays());
        
        // NOW BUILD THE COURSE USING EVERYTHING WE'VE ALREADY MADE
        JsonObject courseJsonObject = Json.createObjectBuilder()
                                    .add(JSON_SUBJECT, courseToSave.getSubject().toString())
                                    .add(JSON_NUMBER, courseToSave.getNumber())
                                    .add(JSON_TITLE, courseToSave.getTitle())
                                    .add(JSON_PAGES, pagesJsonArray)
                                    .add(JSON_INSTRUCTOR, instructorJsonObject)
                                    .add(JSON_STARTING_MONDAY, startingMondayJsonObject)
                                    .add(JSON_ENDING_FRIDAY, endingFridayJsonObject)
                                    .add(JSON_LECTURE_DAYS, lectureDaysJsonArray)
                .build();
        
        // AND SAVE EVERYTHING AT ONCE
        jsonWriter.writeObject(courseJsonObject);
    }
    
    /**
     * Loads the courseToLoad argument using the data found in the json file.
     * 
     * @param courseToLoad Course to load.
     * @param jsonFilePath File containing the data to load.
     * 
     * @throws IOException Thrown when IO fails.
     */
    @Override
    public void loadCourse(Course courseToLoad, String jsonFilePath) throws IOException {
        // LOAD THE JSON FILE WITH ALL THE DATA
        JsonObject json = loadJSONFile(jsonFilePath);
        
        // NOW LOAD THE COURSE
        courseToLoad.setSubject(Subject.valueOf(json.getString(JSON_SUBJECT)));
        courseToLoad.setNumber(json.getInt(JSON_NUMBER));
        courseToLoad.setTitle(json.getString(JSON_TITLE));
        
        // GET THE PAGES TO INCLUDE 
        courseToLoad.clearPages();
        JsonArray jsonPagesArray = json.getJsonArray(JSON_PAGES);
        for (int i = 0; i < jsonPagesArray.size(); i++)
            courseToLoad.addPage(CoursePage.valueOf(jsonPagesArray.getString(i)));
        
        // GET THE LECTURE DAYS TO INCLUDE
        courseToLoad.clearLectureDays();
        JsonArray jsonLectureDaysArray = json.getJsonArray(JSON_LECTURE_DAYS);
        for (int i = 0; i < jsonLectureDaysArray.size(); i++)
            courseToLoad.addLectureDay(DayOfWeek.valueOf(jsonLectureDaysArray.getString(i)));

        // LOAD AND SET THE INSTRUCTOR
        JsonObject jsonInstructor = json.getJsonObject(JSON_INSTRUCTOR);
        Instructor instructor = new Instructor( jsonInstructor.getString(JSON_INSTRUCTOR_NAME),
                                                jsonInstructor.getString(JSON_HOMEPAGE_URL));
        courseToLoad.setInstructor(instructor);
        
        JsonObject startingMonday = json.getJsonObject(JSON_STARTING_MONDAY);
        int year = startingMonday.getInt(JSON_YEAR);
        int month = startingMonday.getInt(JSON_MONTH);
        int day = startingMonday.getInt(JSON_DAY);
        courseToLoad.setStartingMonday(LocalDate.of(year, month, day));
        
        JsonObject endingFriday = json.getJsonObject(JSON_ENDING_FRIDAY);
        year = endingFriday.getInt(JSON_YEAR);
        month = endingFriday.getInt(JSON_MONTH);
        day = startingMonday.getInt(JSON_DAY);
        courseToLoad.setEndingFriday(LocalDate.of(year, month, day));
    }
    
    /**
     * This function saves the last instructor to a json file. This provides 
     * a convenience to the user, who is likely always the same instructor.
     * @param lastInstructor Instructor to save.
     * @param jsonFilePath File in which to put the data.
     * @throws IOException Thrown when I/O fails.
     */
    @Override
    public void saveLastInstructor(Instructor lastInstructor, String jsonFilePath) throws IOException {
        OutputStream os = new FileOutputStream(jsonFilePath);
        JsonWriter jsonWriter = Json.createWriter(os); 
        JsonObject instructorJsonObject = makeInstructorJsonObject(lastInstructor);
        jsonWriter.writeObject(instructorJsonObject);
    }
    
    /**
     * Loads an instructor from the provided file, returning a constructed
     * object to represent it.
     * @param filePath Path of json file containing instructor data.
     * @return A constructed Instructor initialized with the data from the file
     * @throws IOException Thrown when I/O fails.
     */
    @Override
    public Instructor loadLastInstructor(String filePath) throws IOException {
        JsonObject json = loadJSONFile(filePath);
        return buildInstructorJsonObject(json);
    }
    
    /**
     * Saves the subjects list to a json file.
     * @param subjects List of Subjects to save.
     * @param jsonFilePath Path of json file.
     * @throws IOException Thrown when I/O fails.
     */
    @Override
    public void saveSubjects(List<Object> subjects, String jsonFilePath) throws IOException {
        JsonObject arrayObject = buildJsonArrayObject(subjects);
        OutputStream os = new FileOutputStream(jsonFilePath);
        JsonWriter jsonWriter = Json.createWriter(os);  
        jsonWriter.writeObject(arrayObject);        
    }
    
    /**
     * Loads subjects from the json file.
     * @param jsonFilePath Json file containing the subjects.
     * @return List full of Subjects loaded from the file.
     * @throws IOException Thrown when I/O fails.
     */
    @Override
    public ArrayList<String> loadSubjects(String jsonFilePath) throws IOException {
        return loadArrayFromJSONFile(jsonFilePath, JSON_SUBJECTS);
    }
    
    // AND HERE ARE THE PRIVATE HELPER METHODS TO HELP THE PUBLIC ONES
    
    // LOADS A JSON FILE AS A SINGLE OBJECT AND RETURNS IT
    private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
        InputStream is = new FileInputStream(jsonFilePath);
        JsonReader jsonReader = Json.createReader(is);
        JsonObject json = jsonReader.readObject();
        jsonReader.close();
        is.close();
        return json;
    }    
    
    // LOADS AN ARRAY OF A SPECIFIC NAME FROM A JSON FILE AND
    // RETURNS IT AS AN ArrayList FULL OF THE DATA FOUND
    private ArrayList<String> loadArrayFromJSONFile(String jsonFilePath, String arrayName) throws IOException {
        JsonObject json = loadJSONFile(jsonFilePath);
        ArrayList<String> items = new ArrayList();
        JsonArray jsonArray = json.getJsonArray(arrayName);
        for (JsonValue jsV : jsonArray) {
            items.add(jsV.toString());
        }
        return items;
    }
    
    // MAKES AND RETURNS A JSON OBJECT FOR THE PROVIDED INSTRUCTOR
    private JsonObject makeInstructorJsonObject(Instructor instructor) {
        JsonObject jso = Json.createObjectBuilder().add(JSON_INSTRUCTOR_NAME, instructor.getName())
                                                   .add(JSON_HOMEPAGE_URL, instructor.getHomepageURL())
                                                   .build(); 
        return jso;                
    }

    // MAKES AND RETURNS A JSON OBJECT FOR THE PROVIDED DATE
    private JsonObject makeLocalDateJsonObject(LocalDate dateToSave) {
        JsonObject jso = Json.createObjectBuilder().add(JSON_YEAR, dateToSave.getYear())
                                                   .add(JSON_MONTH, dateToSave.getMonthValue())
                                                   .add(JSON_DAY, dateToSave.getDayOfMonth())
                                                   .build(); 
        return jso;
    }
    
    // BUILDS AND RETURNS THE INSTRUCTOR FOUND IN THE JSON OBJECT
    public Instructor buildInstructorJsonObject(JsonObject json) {
        Instructor instructor = new Instructor( json.getString(JSON_INSTRUCTOR_NAME),
                                                    json.getString(JSON_HOMEPAGE_URL));
        return instructor;
    }

    // BUILDS AND RETURNS A JsonArray CONTAINING ALL THE PAGES FOR THIS COURSE
    public JsonArray makePagesJsonArray(List<CoursePage> data) {
        JsonArrayBuilder jsb = Json.createArrayBuilder();
        for (CoursePage cP : data) {
           jsb.add(cP.toString());
        }
        JsonArray jA = jsb.build();
        return jA;        
    }

    // BUILDS AND RETURNS A JsonArray CONTAINING ALL THE LECTURE DAYS FOR THIS COURSE
    public JsonArray makeLectureDaysJsonArray(List<DayOfWeek> data) {
        JsonArrayBuilder jsb = Json.createArrayBuilder();
        for (DayOfWeek dow : data) {
            jsb.add(dow.toString());
        }
        JsonArray jA = jsb.build();
        return jA;
    }

    // BUILDS AND RETURNS A JsonArray CONTAINING THE PROVIDED DATA
    public JsonArray buildJsonArray(List<Object> data) {
        JsonArrayBuilder jsb = Json.createArrayBuilder();
        for (Object d : data) {
           jsb.add(d.toString());
        }
        JsonArray jA = jsb.build();
        return jA;
    }

    // BUILDS AND RETURNS A JsonObject CONTAINING A JsonArray
    // THAT CONTAINS THE PROVIDED DATA
    public JsonObject buildJsonArrayObject(List<Object> data) {
        JsonArray jA = buildJsonArray(data);
        JsonObject arrayObject = Json.createObjectBuilder().add(JSON_SUBJECTS, jA).build();
        return arrayObject;
    }
}
