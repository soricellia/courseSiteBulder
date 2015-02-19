package csb.data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a course to be edited and then used to
 * generate a site.
 * 
 * @author Richard McKenna
 */
public class Course {
    // THESE COURSE DETAILS DESCRIBE WHAT'S REQUIRED BY
    // THE COURSE SITE PAGES
    Subject subject;
    int number;
    String title;
    Instructor instructor;
    LocalDate startingMonday;
    LocalDate endingFriday;
    List<CoursePage> pages;
    List<DayOfWeek> lectureDays;

    /**
     * Constructor for setting up a Course, it initializes the 
     * Instructor, which would have already been loaded from a file.
     * 
     * @param initInstructor The instructor for this course. Note that
     * this can be changed by getting the Instructor and then calling
     * mutator methods on it.
     */
    public Course(Instructor initInstructor) {
        // INITIALIZE THIS OBJECT'S DATA STRUCTURES
        pages = new ArrayList();
        lectureDays = new ArrayList();
        
        // AND KEEP THE INSTRUCTOR
        instructor = initInstructor;
    }

    // BELOW ARE ALL THE ACCESSOR METHODS FOR A COURSE
    // AND THE MUTATOR METHODS. NOTE THAT WE'LL NEED TO CALL
    // THESE AS USERS INPUT VALUES IN THE GUI
     
    public boolean hasCoursePage(CoursePage testPage) {
        return pages.contains(testPage);
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    public LocalDate getStartingMonday() {
        return startingMonday;
    }

    public void setStartingMonday(LocalDate startingMonday) {
        this.startingMonday = startingMonday;
    }

    public LocalDate getEndingFriday() {
        return endingFriday;
    }
    
    public void setEndingFriday(LocalDate endingFriday) {
        this.endingFriday = endingFriday;
    }
    
    public void setScheduleDates(LocalDate initStartingMonday, LocalDate initEndingFriday) {
        setStartingMonday(initStartingMonday);
        setEndingFriday(initEndingFriday);
    }
        
    public void addPage(CoursePage pageToAdd) {
        pages.add(pageToAdd);
    }
    
    public List<CoursePage> getPages() {
        return pages;
    }
    
    public void selectPage(CoursePage coursePage) {
        if (!pages.contains(coursePage))
            pages.add(coursePage);
    }
    
    public void unselectPage(CoursePage coursePage) {
        if (pages.contains(coursePage))
            pages.remove(coursePage);
    }

    public List<DayOfWeek> getLectureDays() {
        return lectureDays;
    }
    
    // BELOW ARE ADDITIONAL METHODS FOR UPDATING A COURSE
    
    public void selectLectureDay(DayOfWeek dayOfWeek) {
        if (!lectureDays.contains(dayOfWeek))
            lectureDays.add(dayOfWeek);
        else
            lectureDays.remove(dayOfWeek);
    }
    
    public void selectLectureDay(DayOfWeek dayOfWeek, boolean isSelected) {
        if (isSelected) {
            if (!lectureDays.contains(dayOfWeek))
                lectureDays.add(dayOfWeek);
        }
        else {
            lectureDays.remove(dayOfWeek);
        }
    }

    public void clearPages() {
        pages.clear();
    }

    public void clearLectureDays() {
        lectureDays.clear();
    }

    public void addLectureDay(DayOfWeek dayOfWeek) {
        lectureDays.add(dayOfWeek);
    }

    public boolean hasLectureDay(DayOfWeek dayOfWeek) {
        return lectureDays.contains(dayOfWeek);
    }
}
